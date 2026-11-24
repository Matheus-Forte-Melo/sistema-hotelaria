import assert from 'node:assert/strict'
import test from 'node:test'
import { QueryClient } from '@tanstack/react-query'
import { consultaEstrutura, consultaReservas } from './consultas.js'
import { mockRepositories } from './mockRepositories.js'

test('consulta a estrutura mock e as reservas da fonte injetada sem misturar as listas', async () => {
  const cliente = new QueryClient()
  const repositorios = {
    ...mockRepositories,
    reservas: { async listar() { return [{ id: 42, hospede: 'Ana' }] } },
  }
  try {
    const estrutura = await cliente.fetchQuery(consultaEstrutura(repositorios))
    const reservas = await cliente.fetchQuery(consultaReservas(repositorios))
    assert.equal(estrutura.andares[0].numero, 1)
    assert.deepEqual(reservas, [{ id: 42, hospede: 'Ana' }])
    assert.deepEqual(cliente.getQueryData(['estrutura']), estrutura)
    assert.deepEqual(cliente.getQueryData(['reservas']), reservas)
  } finally {
    cliente.clear()
  }
})
