import assert from 'node:assert/strict'
import test from 'node:test'
import {
  calcularPrevisao, formularioDaReserva, formularioVazio, opcoesDeQuarto, paraPedido, validarFormulario,
} from './logicaFormulario.js'

const quarto101 = { id: 1, andarId: 1, numero: '101', capacidade: 2, diaria: 150 }
const quarto102 = { id: 2, andarId: 1, numero: '102', capacidade: 4, diaria: 300 }

const valido = {
  hospede: 'Ana', quartoId: '1', entrada: '2026-11-10', saida: '2026-11-12', pessoas: '2', diaria: '180',
}

test('duas noites a R$ 180 preveem duas diárias e total de R$ 360', () => {
  assert.deepEqual(calcularPrevisao(valido), { noites: 2, total: 360 })
})

test('não calcula previsão sem período válido ou sem diária', () => {
  assert.equal(calcularPrevisao({ ...valido, saida: '2026-11-10' }), null)
  assert.equal(calcularPrevisao({ ...valido, saida: '' }), null)
  assert.equal(calcularPrevisao({ ...valido, diaria: '' }), null)
})

test('arredonda o total para centavos', () => {
  assert.deepEqual(calcularPrevisao({ ...valido, saida: '2026-11-13', diaria: '99.99' }), { noites: 3, total: 299.97 })
})

test('aceita o formulário completo', () => {
  assert.deepEqual(validarFormulario(valido, quarto101), {})
})

test('indica cada campo inválido com o motivo', () => {
  const erros = validarFormulario(
    { hospede: ' ', quartoId: '', entrada: '2026-11-12', saida: '2026-11-12', pessoas: '0', diaria: '-1' }, null)
  assert.deepEqual(Object.keys(erros).sort(), ['diaria', 'hospede', 'pessoas', 'quartoId', 'saida'])
})

test('recusa mais pessoas que a capacidade do quarto escolhido', () => {
  const erros = validarFormulario({ ...valido, pessoas: '3' }, quarto101)
  assert.match(erros.pessoas, /capacidade/)
  assert.deepEqual(validarFormulario({ ...valido, pessoas: '3' }, quarto102), {})
})

test('recusa quantidade de pessoas fracionada', () => {
  assert.ok(validarFormulario({ ...valido, pessoas: '1.5' }, quarto101).pessoas)
})

test('converte os campos no pedido enviado ao repositório', () => {
  assert.deepEqual(paraPedido({ ...valido, hospede: '  Ana Souza ' }), {
    hospede: 'Ana Souza', quartoId: 1, entrada: '2026-11-10', saida: '2026-11-12', pessoas: 2, diaria: 180,
  })
})

test('no modo editar recupera os valores vigentes da reserva', () => {
  const reserva = { id: 7, quartoId: 2, hospede: 'Bruno', entrada: '2026-11-12', saida: '2026-11-14', pessoas: 3, diaria: 260 }
  assert.deepEqual(formularioDaReserva(reserva), {
    hospede: 'Bruno', quartoId: '2', entrada: '2026-11-12', saida: '2026-11-14', pessoas: '3', diaria: '260',
  })
  assert.equal(formularioVazio().hospede, '')
})

test('mantém na lista o quarto escolhido mesmo quando a busca não o devolve', () => {
  assert.deepEqual(opcoesDeQuarto([quarto102], [quarto101, quarto102], '1'), [
    { quarto: quarto102, disponivel: true },
    { quarto: quarto101, disponivel: false },
  ])
  assert.deepEqual(opcoesDeQuarto([quarto102], [quarto101, quarto102], '2'), [{ quarto: quarto102, disponivel: true }])
})
