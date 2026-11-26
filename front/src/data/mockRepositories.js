import { haConflito, periodoValido } from './regrasReserva.js'

// Os identificadores ligam cada reserva ao quarto e cada quarto ao andar.
const andares = [{ id: 1, numero: 1 }, { id: 2, numero: 2 }]
const quartos = [
  { id: 1, andarId: 1, numero: '101', capacidade: 2, diaria: 180 },
  { id: 2, andarId: 1, numero: '102', capacidade: 3, diaria: 220 },
  { id: 3, andarId: 2, numero: '201', capacidade: 2, diaria: 190 },
  { id: 4, andarId: 2, numero: '202', capacidade: 4, diaria: 260 },
]
const reservas = [
  { id: 1, quartoId: 1, hospede: 'Marina Costa', entrada: '2026-11-24', saida: '2026-11-26', pessoas: 2, diaria: 180, situacao: 'Vigente' },
  { id: 2, quartoId: 4, hospede: 'Rafael Lima', entrada: '2026-11-25', saida: '2026-11-28', pessoas: 3, diaria: 260, situacao: 'Vigente' },
  { id: 3, quartoId: 2, hospede: 'Clara Alves', entrada: '2026-11-26', saida: '2026-11-27', pessoas: 1, diaria: 220, situacao: 'Cancelada' },
]

const latencia = () => new Promise(resolve => setTimeout(resolve, 150))

// Reproduz as recusas da API (issue #3) para que a tela trate os mesmos motivos antes da integração.
function validarPedido(dados, pedido, ignorarReservaId) {
  const quarto = dados.quartos.find(q => q.id === pedido.quartoId)
  if (!quarto) throw new Error(`Quarto ${pedido.quartoId} não encontrado`)
  if (!pedido.hospede?.trim()) throw new Error('Nome do hóspede é obrigatório')
  if (!periodoValido(pedido)) throw new Error('Data de saída deve ser posterior à data de entrada')
  if (!(pedido.pessoas >= 1 && pedido.pessoas <= quarto.capacidade)) {
    throw new Error(`Quantidade de pessoas deve ser de 1 a ${quarto.capacidade}, a capacidade do quarto ${quarto.numero}`)
  }
  if (haConflito(dados.reservas, pedido, ignorarReservaId)) {
    throw new Error(`O quarto ${quarto.numero} já está reservado em alguma noite de ${pedido.entrada} a ${pedido.saida}`)
  }
  return quarto
}

function campos(pedido, quarto) {
  return {
    quartoId: quarto.id, hospede: pedido.hospede.trim(), entrada: pedido.entrada, saida: pedido.saida,
    pessoas: pedido.pessoas, diaria: pedido.diaria ?? quarto.diaria,
  }
}

/** Cada chamada cria uma cópia própria dos dados, para os testes não interferirem entre si. */
export function criarRepositoriosMock() {
  const dados = structuredClone({ andares, quartos, reservas })
  let proximoId = Math.max(...dados.reservas.map(r => r.id)) + 1

  return {
    estrutura: {
      async listar() { return structuredClone({ andares: dados.andares, quartos: dados.quartos }) },
    },
    reservas: {
      async listar() { return structuredClone(dados.reservas) },
      async criar(pedido) {
        await latencia()
        const quarto = validarPedido(dados, pedido, null)
        const reserva = { id: proximoId++, ...campos(pedido, quarto), situacao: 'Vigente' }
        dados.reservas.push(reserva)
        return structuredClone(reserva)
      },
      async editar(id, pedido) {
        await latencia()
        const atual = dados.reservas.find(r => r.id === id)
        if (!atual) throw new Error(`Reserva ${id} não encontrada`)
        if (atual.situacao !== 'Vigente') throw new Error('Somente reservas vigentes podem ser editadas')
        const quarto = validarPedido(dados, pedido, id)
        Object.assign(atual, campos(pedido, quarto))
        return structuredClone(atual)
      },
    },
  }
}

export const mockRepositories = criarRepositoriosMock()
