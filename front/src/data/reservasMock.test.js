import assert from 'node:assert/strict'
import test from 'node:test'
import { QueryClient } from '@tanstack/react-query'
import { criarRepositoriosMock } from './mockRepositories.js'
import { mutacaoSalvarReserva } from './consultas.js'

const pedido = { hospede: 'Ana', quartoId: 1, entrada: '2030-01-10', saida: '2030-01-12', pessoas: 2, diaria: 180 }

test('cria a reserva vigente e a inclui na listagem', async () => {
  const repositorios = criarRepositoriosMock()
  const criada = await repositorios.reservas.criar(pedido)

  assert.equal(criada.situacao, 'Vigente')
  assert.equal(criada.hospede, 'Ana')
  assert.ok((await repositorios.reservas.listar()).some(r => r.id === criada.id))
})

test('recusa conflito e capacidade como a API, com motivo', async () => {
  const repositorios = criarRepositoriosMock()
  await repositorios.reservas.criar(pedido)

  await assert.rejects(repositorios.reservas.criar({ ...pedido, entrada: '2030-01-11', saida: '2030-01-13' }), /reservado/)
  await assert.rejects(repositorios.reservas.criar({ ...pedido, quartoId: 1, pessoas: 3, entrada: '2030-02-01', saida: '2030-02-02' }), /capacidade/)
  await assert.doesNotReject(repositorios.reservas.criar({ ...pedido, entrada: '2030-01-12', saida: '2030-01-14' }))
})

test('edição válida atualiza período; edição com conflito preserva a reserva gravada', async () => {
  const repositorios = criarRepositoriosMock()
  const ana = await repositorios.reservas.criar(pedido)
  await repositorios.reservas.criar({ ...pedido, hospede: 'Bruno', entrada: '2030-01-12', saida: '2030-01-14' })

  await assert.rejects(repositorios.reservas.editar(ana.id, { ...pedido, saida: '2030-01-13' }), /reservado/)
  const gravada = (await repositorios.reservas.listar()).find(r => r.id === ana.id)
  assert.equal(gravada.saida, '2030-01-12')

  const editada = await repositorios.reservas.editar(ana.id, { ...pedido, entrada: '2030-01-09' })
  assert.equal(editada.entrada, '2030-01-09')
})

test('cada repositório mock tem seus próprios dados', async () => {
  const a = criarRepositoriosMock()
  const b = criarRepositoriosMock()
  await a.reservas.criar(pedido)
  assert.equal((await a.reservas.listar()).length, (await b.reservas.listar()).length + 1)
})

test('salvar pela mutação grava e invalida reservas, disponibilidade e mapa', async () => {
  const cliente = new QueryClient()
  const repositorios = criarRepositoriosMock()
  try {
    for (const chave of [['reservas'], ['disponibilidade', 'x'], ['mapa', 1, '2030-01-10']]) {
      cliente.setQueryData(chave, [])
    }
    const salvar = cliente.getMutationCache().build(cliente, mutacaoSalvarReserva(repositorios, cliente))
    const criada = await salvar.execute({ pedido })
    assert.equal(criada.hospede, 'Ana')

    for (const chave of [['reservas'], ['disponibilidade', 'x'], ['mapa', 1, '2030-01-10']]) {
      assert.equal(cliente.getQueryState(chave).isInvalidated, true, JSON.stringify(chave))
    }

    const editada = await cliente.getMutationCache().build(cliente, mutacaoSalvarReserva(repositorios, cliente))
      .execute({ id: criada.id, pedido: { ...pedido, hospede: 'Ana Souza' } })
    assert.equal(editada.hospede, 'Ana Souza')
  } finally {
    cliente.clear()
  }
})
