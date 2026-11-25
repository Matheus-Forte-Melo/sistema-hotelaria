import assert from 'node:assert/strict'
import test from 'node:test'
import { QueryClient } from '@tanstack/react-query'
import { consultaDisponibilidade, consultaQuartos, mutacaoCadastrarQuarto } from './consultas.js'
import { criarQuartosHttp } from './quartosHttp.js'
import { criarQuartosMock } from './quartosMock.js'
import { formatarMoeda, validarBusca, validarQuarto } from './quartos.js'

// Cenário das issues #2 e #7: 101 de duas vagas e 102 de quatro no 1º andar.
function cenario(reservas = []) {
  const estrutura = {
    async listar() {
      return {
        andares: [{ id: 1, numero: 1 }, { id: 2, numero: 2 }],
        quartos: [
          { id: 1, andarId: 1, numero: '101', capacidade: 2, diaria: 180 },
          { id: 2, andarId: 1, numero: '102', capacidade: 4, diaria: 300 },
        ],
      }
    },
  }
  return criarQuartosMock({ estrutura, reservas: { async listar() { return structuredClone(reservas) } } })
}

const reserva102 = { id: 9, quartoId: 2, hospede: 'Ana', entrada: '2026-11-10', saida: '2026-11-12', pessoas: 2, situacao: 'Vigente' }
const numeros = quartos => quartos.map(q => q.numero)

test('buscar três vagas exclui o quarto de duas quando ambos estão livres', async () => {
  const quartos = cenario()

  const encontrados = await quartos.buscarDisponiveis({ entrada: '2026-11-11', saida: '2026-11-13', pessoas: 3 })

  assert.deepEqual(encontrados, [{ id: 2, andarId: 1, andar: 1, numero: '102', capacidade: 4, diaria: 300 }])
})

test('reserva de 10 a 12 impede entrada no dia 11 e permite entrada no dia 12', async () => {
  const quartos = cenario([reserva102])

  assert.deepEqual(await quartos.buscarDisponiveis({ entrada: '2026-11-11', saida: '2026-11-13', pessoas: 3 }), [])
  assert.deepEqual(numeros(await quartos.buscarDisponiveis({ entrada: '2026-11-12', saida: '2026-11-14', pessoas: 3 })), ['102'])
})

test('reserva cancelada não bloqueia o quarto', async () => {
  const quartos = cenario([{ ...reserva102, situacao: 'Cancelada' }])

  assert.deepEqual(numeros(await quartos.buscarDisponiveis({ entrada: '2026-11-11', saida: '2026-11-13', pessoas: 3 })), ['102'])
})

test('busca recusa período invertido e menos de uma pessoa com orientação', async () => {
  const quartos = cenario()

  await assert.rejects(quartos.buscarDisponiveis({ entrada: '2026-11-13', saida: '2026-11-13', pessoas: 2 }),
    { message: 'A data de saída deve ser posterior à data de entrada' })
  await assert.rejects(quartos.buscarDisponiveis({ entrada: '2026-11-11', saida: '2026-11-13', pessoas: 0 }),
    { message: 'A quantidade de pessoas deve ser pelo menos 1' })
})

test('cadastrar vincula o quarto ao andar e ele passa a aparecer na lista e na busca', async () => {
  const quartos = cenario()

  const criado = await quartos.cadastrar({ andarId: 2, numero: ' 201 ', capacidade: 5, diaria: 400 })

  assert.deepEqual(criado, { id: 3, andarId: 2, andar: 2, numero: '201', capacidade: 5, diaria: 400 })
  assert.deepEqual(numeros(await quartos.listar(2)), ['201'])
  assert.deepEqual(numeros(await quartos.listar(1)), ['101', '102'])
  assert.deepEqual(numeros(await quartos.buscarDisponiveis({ entrada: '2026-11-11', saida: '2026-11-13', pessoas: 5 })), ['201'])
})

test('cadastrar recusa identificação repetida sem diferenciar maiúsculas', async () => {
  const quartos = cenario()
  await quartos.cadastrar({ andarId: 2, numero: '20A', capacidade: 2, diaria: 100 })

  await assert.rejects(quartos.cadastrar({ andarId: 1, numero: '20a', capacidade: 2, diaria: 100 }),
    { message: 'Já existe um quarto com a identificação 20a' })
})

test('validarBusca orienta antes de consultar', () => {
  assert.equal(validarBusca({ entrada: '', saida: '2026-11-13', pessoas: '2' }), 'Informe as datas de entrada e de saída')
  assert.equal(validarBusca({ entrada: '2026-11-13', saida: '2026-11-11', pessoas: '2' }), 'A data de saída deve ser posterior à data de entrada')
  assert.equal(validarBusca({ entrada: '2026-11-11', saida: '2026-11-13', pessoas: '0' }), 'A quantidade de pessoas deve ser pelo menos 1')
  assert.equal(validarBusca({ entrada: '2026-11-11', saida: '2026-11-13', pessoas: '1.5' }), 'A quantidade de pessoas deve ser pelo menos 1')
  assert.equal(validarBusca({ entrada: '2026-11-11', saida: '2026-11-13', pessoas: '3' }), null)
})

test('validarQuarto exige andar, identificação, capacidade positiva e diária não negativa', () => {
  assert.equal(validarQuarto({ andarId: '', numero: '101', capacidade: '2', diaria: '180' }), 'Selecione o andar')
  assert.equal(validarQuarto({ andarId: '1', numero: '  ', capacidade: '2', diaria: '180' }), 'Informe a identificação do quarto')
  assert.equal(validarQuarto({ andarId: '1', numero: '12345678901', capacidade: '2', diaria: '180' }), 'A identificação deve ter até 10 caracteres')
  assert.equal(validarQuarto({ andarId: '1', numero: '101', capacidade: '0', diaria: '180' }), 'A capacidade deve ser pelo menos 1')
  assert.equal(validarQuarto({ andarId: '1', numero: '101', capacidade: '2', diaria: '' }), 'Informe a diária padrão')
  assert.equal(validarQuarto({ andarId: '1', numero: '101', capacidade: '2', diaria: '-1' }), 'A diária não pode ser negativa')
  assert.equal(validarQuarto({ andarId: '1', numero: '101', capacidade: '2', diaria: '180' }), null)
})

test('formatarMoeda usa reais', () => {
  assert.equal(formatarMoeda(300).replace(/\s/g, ' '), 'R$ 300,00')
})

test('consulta de disponibilidade usa os filtros na chave e só roda com uma busca válida', async () => {
  const cliente = new QueryClient()
  const filtro = { entrada: '2026-11-11', saida: '2026-11-13', pessoas: 3 }
  try {
    assert.deepEqual(consultaDisponibilidade(cenario(), filtro).queryKey, ['disponibilidade', '2026-11-11', '2026-11-13', 3])
    assert.equal(consultaDisponibilidade(cenario(), null).enabled, false)
    assert.deepEqual(numeros(await cliente.fetchQuery(consultaDisponibilidade({ quartos: cenario() }, filtro))), ['102'])
    assert.deepEqual(consultaQuartos({ quartos: cenario() }, 1).queryKey, ['quartos', 1])
  } finally {
    cliente.clear()
  }
})

test('cadastrar um quarto invalida quartos, disponibilidade, estrutura e mapa', async () => {
  const invalidadas = []
  const cliente = { invalidateQueries: ({ queryKey }) => { invalidadas.push(queryKey[0]); return Promise.resolve() } }
  const mutacao = mutacaoCadastrarQuarto({ quartos: cenario() }, cliente)

  const criado = await mutacao.mutationFn({ andarId: 1, numero: '103', capacidade: 2, diaria: 150 })
  await mutacao.onSuccess(criado)

  assert.equal(criado.numero, '103')
  assert.deepEqual(invalidadas.sort(), ['disponibilidade', 'estrutura', 'mapa', 'quartos'])
})

test('repositório HTTP converte os campos da API e repassa a mensagem das recusas', async () => {
  const chamadas = []
  const daApi = { id: 2, andarId: 1, andarNumero: 1, identificacao: '102', capacidade: 4, valorDiariaPadrao: 300.0 }
  const respostas = [
    { ok: true, status: 200, json: async () => [daApi] },
    { ok: true, status: 201, json: async () => daApi },
    { ok: false, status: 422, json: async () => ({ mensagem: 'A data de saída deve ser posterior à data de entrada' }) },
  ]
  const quartos = criarQuartosHttp(async (url, opcoes) => { chamadas.push([url, opcoes]); return respostas.shift() })

  const esperado = [{ id: 2, andarId: 1, andar: 1, numero: '102', capacidade: 4, diaria: 300 }]
  assert.deepEqual(await quartos.buscarDisponiveis({ entrada: '2026-11-12', saida: '2026-11-14', pessoas: 3 }), esperado)
  assert.equal(chamadas[0][0], '/api/quartos/disponiveis?entrada=2026-11-12&saida=2026-11-14&pessoas=3')

  assert.deepEqual(await quartos.cadastrar({ andarId: 1, numero: '102', capacidade: 4, diaria: 300 }), esperado[0])
  assert.equal(chamadas[1][0], '/api/quartos')
  assert.deepEqual(JSON.parse(chamadas[1][1].body), { andarId: 1, identificacao: '102', capacidade: 4, valorDiariaPadrao: 300 })

  await assert.rejects(quartos.buscarDisponiveis({ entrada: '2026-11-13', saida: '2026-11-11', pessoas: 3 }),
    { message: 'A data de saída deve ser posterior à data de entrada' })
})
