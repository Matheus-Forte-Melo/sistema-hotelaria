// Os identificadores ligam cada reserva ao quarto e cada quarto ao andar.
const andares = [{ id: 1, numero: 1 }, { id: 2, numero: 2 }]
const quartos = [
  { id: 1, andarId: 1, numero: '101', capacidade: 2, diaria: 180 },
  { id: 2, andarId: 1, numero: '102', capacidade: 3, diaria: 220 },
  { id: 3, andarId: 2, numero: '201', capacidade: 2, diaria: 190 },
  { id: 4, andarId: 2, numero: '202', capacidade: 4, diaria: 260 },
]
const reservasIniciais = [
  { id: 1, quartoId: 1, hospede: 'Marina Costa', entrada: '2026-11-24', saida: '2026-11-26', pessoas: 2, diaria: 180, situacao: 'Vigente' },
  { id: 2, quartoId: 4, hospede: 'Rafael Lima', entrada: '2026-11-25', saida: '2026-11-28', pessoas: 3, diaria: 260, situacao: 'Vigente' },
  { id: 3, quartoId: 2, hospede: 'Clara Alves', entrada: '2026-11-26', saida: '2026-11-27', pessoas: 1, diaria: 220, situacao: 'Cancelada' },
  { id: 4, quartoId: 3, hospede: 'Marina Costa', entrada: '2026-11-28', saida: '2026-11-30', pessoas: 2, diaria: 190, situacao: 'Vigente' },
]

const DIA_MS = 24 * 60 * 60 * 1000

// Mesmos campos que o repositório HTTP devolve na busca, nos detalhes e no cancelamento.
function detalhar(reserva) {
  const quarto = quartos.find(q => q.id === reserva.quartoId)
  const diarias = (Date.parse(reserva.saida) - Date.parse(reserva.entrada)) / DIA_MS
  return {
    id: reserva.id, hospede: reserva.hospede, quartoId: reserva.quartoId, quarto: quarto?.numero ?? '—',
    andar: andares.find(a => a.id === quarto?.andarId)?.numero ?? null, entrada: reserva.entrada, saida: reserva.saida,
    pessoas: reserva.pessoas, diaria: reserva.diaria, diarias, total: diarias * reserva.diaria, situacao: reserva.situacao,
  }
}

/** Cada chamada cria uma cópia independente dos dados, para que testes e telas não interfiram entre si. */
export function criarMockRepositories() {
  const reservas = structuredClone(reservasIniciais)
  const encontrar = id => {
    const reserva = reservas.find(r => r.id === Number(id))
    if (!reserva) throw new Error(`Reserva ${id} não encontrada`)
    return reserva
  }
  return {
    estrutura: {
      async listar() { return structuredClone({ andares, quartos }) },
    },
    reservas: {
      async listar() { return structuredClone(reservas) },
      async buscarPorNome(nome) {
        const termo = nome.trim().toLowerCase()
        return reservas.filter(r => r.hospede.toLowerCase().includes(termo))
          .sort((a, b) => a.entrada.localeCompare(b.entrada) || a.id - b.id)
          .map(detalhar)
      },
      async detalhar(id) { return detalhar(encontrar(id)) },
      async cancelar(id) {
        const reserva = encontrar(id)
        if (reserva.situacao === 'Cancelada') throw new Error(`A reserva ${id} já está cancelada`)
        reserva.situacao = 'Cancelada'
        return detalhar(reserva)
      },
    },
  }
}

export const mockRepositories = criarMockRepositories()
