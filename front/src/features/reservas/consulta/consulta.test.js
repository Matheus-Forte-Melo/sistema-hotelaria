import assert from 'node:assert/strict'
import test from 'node:test'
import { QueryClient } from '@tanstack/react-query'
import { consultaBuscaReservas, consultaReserva, mutacaoCancelarReserva } from '../../../data/consultas.js'
import { criarMockRepositories } from '../../../data/mockRepositories.js'
import { criarReservasHttp } from '../../../data/reservasHttp.js'
import { fluxoCancelamento, formatarMoeda, rotuloPeriodo } from './consulta.js'

test('busca pelo nome devolve as duas reservas de Marina distinguíveis por quarto e período', async () => {
  const { reservas } = criarMockRepositories()

  const encontradas = await reservas.buscarPorNome('  marina ')

  assert.deepEqual(encontradas.map(r => [r.hospede, r.quarto, r.entrada, r.saida]), [
    ['Marina Costa', '101', '2026-11-24', '2026-11-26'],
    ['Marina Costa', '201', '2026-11-28', '2026-11-30'],
  ])
  assert.deepEqual(await reservas.buscarPorNome('Inexistente'), [])
})

test('detalhes trazem andar, pessoas, diária, diárias e total', async () => {
  const { reservas } = criarMockRepositories()

  const reserva = await reservas.detalhar(1)

  assert.equal(reserva.andar, 1)
  assert.equal(reserva.pessoas, 2)
  assert.equal(reserva.diaria, 180)
  assert.equal(reserva.diarias, 2)
  assert.equal(reserva.total, 360)
  assert.equal(reserva.situacao, 'Vigente')
})

test('desistir do cancelamento mantém a reserva vigente', async () => {
  const { reservas } = criarMockRepositories()
  let estado = fluxoCancelamento(undefined, { tipo: 'pedir', reservaId: 1 })
  assert.deepEqual(estado, { etapa: 'confirmando', reservaId: 1, erro: null })

  estado = fluxoCancelamento(estado, { tipo: 'desistir' })

  assert.equal(estado.etapa, 'inativo')
  assert.equal((await reservas.detalhar(1)).situacao, 'Vigente')
})

test('confirmar o cancelamento mostra Cancelada e preserva a reserva na busca', async () => {
  const { reservas } = criarMockRepositories()

  const cancelada = await reservas.cancelar(1)

  assert.equal(cancelada.situacao, 'Cancelada')
  assert.deepEqual((await reservas.buscarPorNome('Marina')).map(r => [r.id, r.situacao]), [[1, 'Cancelada'], [4, 'Vigente']])
})

test('cancelar duas vezes é recusado com o motivo', async () => {
  const { reservas } = criarMockRepositories()
  await reservas.cancelar(1)

  await assert.rejects(reservas.cancelar(1), { message: 'A reserva 1 já está cancelada' })
})

test('fluxo de cancelamento só registra o sucesso depois da resposta e guarda a mensagem de recusa', () => {
  let estado = fluxoCancelamento(undefined, { tipo: 'pedir', reservaId: 7 })
  estado = fluxoCancelamento(estado, { tipo: 'confirmar' })
  assert.equal(estado.etapa, 'enviando')

  const recusado = fluxoCancelamento(estado, { tipo: 'falhar', mensagem: 'A reserva 7 já está cancelada' })
  assert.deepEqual(recusado, { etapa: 'erro', reservaId: 7, erro: 'A reserva 7 já está cancelada' })

  assert.equal(fluxoCancelamento(estado, { tipo: 'concluir' }).etapa, 'inativo')
})

test('busca só é executada com nome preenchido e usa o nome na chave', () => {
  const repositorios = criarMockRepositories()

  assert.deepEqual(consultaBuscaReservas(repositorios, ' Ana ').queryKey, ['reservas', 'busca', 'Ana'])
  assert.equal(consultaBuscaReservas(repositorios, ' Ana ').enabled, true)
  assert.equal(consultaBuscaReservas(repositorios, '   ').enabled, false)
  assert.deepEqual(consultaReserva(repositorios, 3).queryKey, ['reservas', 'detalhe', 3])
  assert.equal(consultaReserva(repositorios, null).enabled, false)
})

test('cancelar invalida as consultas de reservas, disponibilidade e mapa', async () => {
  const cliente = new QueryClient()
  const repositorios = criarMockRepositories()
  try {
    await cliente.fetchQuery(consultaBuscaReservas(repositorios, 'Marina'))
    await cliente.fetchQuery({ queryKey: ['mapa', 1, '2026-11-24'], queryFn: async () => ({}) })
    await cliente.fetchQuery({ queryKey: ['disponibilidade'], queryFn: async () => [] })
    const opcoes = mutacaoCancelarReserva(repositorios, cliente)

    const cancelada = await opcoes.mutationFn({ reservaId: 1 })
    await opcoes.onSuccess(cancelada)

    for (const chave of [['reservas', 'busca', 'Marina'], ['mapa', 1, '2026-11-24'], ['disponibilidade']]) {
      assert.equal(cliente.getQueryState(chave).isInvalidated, true, chave.join('/'))
    }
    assert.equal(cliente.getQueryData(['reservas', 'detalhe', 1]).situacao, 'Cancelada')
  } finally {
    cliente.clear()
  }
})

test('repositório HTTP converte a resposta da API e envia o cancelamento', async () => {
  const chamadas = []
  const api = {
    id: 9, nomeHospede: 'Ana', quartoId: 2, quartoIdentificacao: '102', andarNumero: 1,
    dataEntrada: '2026-11-10', dataSaida: '2026-11-12', quantidadePessoas: 2,
    valorDiaria: 180, quantidadeDiarias: 2, valorTotal: 360, situacao: 'CANCELADA',
  }
  const fetchFalso = async (url, opcoes = {}) => {
    chamadas.push([url, opcoes.method ?? 'GET', opcoes.body])
    return { ok: true, json: async () => (url.includes('nome=') ? [api] : api) }
  }
  const reservas = criarReservasHttp(fetchFalso)

  const [encontrada] = await reservas.buscarPorNome('Ana Souza')
  await reservas.cancelar(9, 'Desistiu')

  assert.deepEqual(encontrada, {
    id: 9, hospede: 'Ana', quartoId: 2, quarto: '102', andar: 1, entrada: '2026-11-10', saida: '2026-11-12',
    pessoas: 2, diaria: 180, diarias: 2, total: 360, situacao: 'Cancelada',
  })
  assert.deepEqual(chamadas, [
    ['/api/reservas?nome=Ana%20Souza', 'GET', undefined],
    ['/api/reservas/9/cancelamento', 'POST', JSON.stringify({ motivo: 'Desistiu' })],
  ])
})

test('repositório HTTP repassa a mensagem de recusa da API', async () => {
  const reservas = criarReservasHttp(async () => ({ ok: false, status: 422, json: async () => ({ mensagem: 'A reserva 9 já está cancelada' }) }))

  await assert.rejects(reservas.cancelar(9), { message: 'A reserva 9 já está cancelada' })
})

test('formata período e valores para distinguir as reservas na lista', () => {
  assert.equal(rotuloPeriodo('2026-11-24', '2026-11-26'), '24/11/2026 a 26/11/2026')
  assert.equal(formatarMoeda(360).replace(/\s/g, ' '), 'R$ 360,00')
})
