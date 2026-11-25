import assert from 'node:assert/strict'
import test from 'node:test'
import { haConflito, quartosDisponiveis } from './regrasReserva.js'

const quartos = [
  { id: 1, andarId: 1, numero: '101', capacidade: 2, diaria: 150 },
  { id: 2, andarId: 1, numero: '102', capacidade: 4, diaria: 300 },
]
const reservas = [
  { id: 10, quartoId: 2, hospede: 'Ana', entrada: '2026-11-10', saida: '2026-11-12', pessoas: 2, diaria: 300, situacao: 'Vigente' },
]

test('a saída é exclusiva: reserva de 10 a 12 libera entrada no dia 12', () => {
  assert.equal(haConflito(reservas, { quartoId: 2, entrada: '2026-11-11', saida: '2026-11-13' }), true)
  assert.equal(haConflito(reservas, { quartoId: 2, entrada: '2026-11-12', saida: '2026-11-14' }), false)
  assert.equal(haConflito(reservas, { quartoId: 2, entrada: '2026-11-08', saida: '2026-11-10' }), false)
})

test('desconsidera a própria reserva e as canceladas', () => {
  assert.equal(haConflito(reservas, { quartoId: 2, entrada: '2026-11-11', saida: '2026-11-13' }, 10), false)
  const canceladas = reservas.map(r => ({ ...r, situacao: 'Cancelada' }))
  assert.equal(haConflito(canceladas, { quartoId: 2, entrada: '2026-11-11', saida: '2026-11-13' }), false)
})

test('lista apenas quartos que comportam o grupo e estão livres em todas as noites', () => {
  const ids = filtro => quartosDisponiveis(quartos, reservas, filtro).map(q => q.id)
  assert.deepEqual(ids({ entrada: '2026-11-11', saida: '2026-11-13', pessoas: 3 }), [])
  assert.deepEqual(ids({ entrada: '2026-11-12', saida: '2026-11-14', pessoas: 3 }), [2])
  assert.deepEqual(ids({ entrada: '2026-11-11', saida: '2026-11-13', pessoas: 2 }), [1])
  assert.deepEqual(ids({ entrada: '2026-11-11', saida: '2026-11-13', pessoas: 3, ignorarReservaId: 10 }), [2])
})

test('sem período válido devolve lista vazia', () => {
  assert.deepEqual(quartosDisponiveis(quartos, reservas, { entrada: '2026-11-12', saida: '2026-11-12', pessoas: 1 }), [])
})
