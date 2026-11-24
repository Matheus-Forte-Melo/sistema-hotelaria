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

export const mockRepositories = {
  estrutura: {
    async listar() { return structuredClone({ andares, quartos }) },
  },
  reservas: {
    async listar() { return structuredClone(reservas) },
  },
}
