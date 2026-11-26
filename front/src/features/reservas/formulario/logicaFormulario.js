// Lógica do formulário de reserva, separada do componente para ser testada com node --test.
// Os valores ficam como texto, do jeito que os campos do formulário os guardam.

const MS_POR_DIA = 24 * 60 * 60 * 1000

export const formularioVazio = () => ({ hospede: '', quartoId: '', entrada: '', saida: '', pessoas: '1', diaria: '' })

export function formularioDaReserva(reserva) {
  return {
    hospede: reserva.hospede,
    quartoId: String(reserva.quartoId),
    entrada: reserva.entrada,
    saida: reserva.saida,
    pessoas: String(reserva.pessoas),
    diaria: String(reserva.diaria),
  }
}

function noitesEntre(entrada, saida) {
  if (!entrada || !saida) return 0
  const [a, b] = [entrada, saida].map(data => Date.UTC(...data.split('-').map((parte, i) => Number(parte) - (i === 1 ? 1 : 0))))
  return Math.round((b - a) / MS_POR_DIA)
}

/** Noites e total previsto pela diária acordada, ou null enquanto faltar período válido ou diária. */
export function calcularPrevisao({ entrada, saida, diaria }) {
  const noites = noitesEntre(entrada, saida)
  const valor = Number(diaria)
  if (noites < 1 || diaria === '' || !(valor >= 0)) return null
  return { noites, total: Math.round(noites * valor * 100) / 100 }
}

/** Erros por campo; objeto vazio quando o formulário pode ser enviado. */
export function validarFormulario(valores, quarto) {
  const erros = {}
  if (!valores.hospede.trim()) erros.hospede = 'Informe o nome do hóspede.'
  if (!valores.quartoId) erros.quartoId = 'Escolha um quarto.'
  if (!valores.entrada) erros.entrada = 'Informe a data de entrada.'
  if (!valores.saida) erros.saida = 'Informe a data de saída.'
  else if (valores.entrada && valores.saida <= valores.entrada) erros.saida = 'A saída deve ser depois da entrada.'
  const pessoas = Number(valores.pessoas)
  if (!Number.isInteger(pessoas) || pessoas < 1) erros.pessoas = 'Informe pelo menos 1 pessoa.'
  else if (quarto && pessoas > quarto.capacidade) {
    erros.pessoas = `O quarto ${quarto.numero} tem capacidade para ${quarto.capacidade} pessoa(s).`
  }
  if (valores.diaria === '' || !(Number(valores.diaria) >= 0)) erros.diaria = 'Informe uma diária de R$ 0 ou mais.'
  return erros
}

export function paraPedido(valores) {
  return {
    hospede: valores.hospede.trim(),
    quartoId: Number(valores.quartoId),
    entrada: valores.entrada,
    saida: valores.saida,
    pessoas: Number(valores.pessoas),
    diaria: Number(valores.diaria),
  }
}

/**
 * Quartos devolvidos pela busca de disponibilidade. O quarto já escolhido continua na lista, marcado como
 * indisponível, para que a recepção possa tentar salvar e ver o motivo da recusa da API.
 */
export function opcoesDeQuarto(disponiveis, todos, quartoIdEscolhido) {
  const opcoes = disponiveis.map(quarto => ({ quarto, disponivel: true }))
  const escolhido = todos.find(quarto => String(quarto.id) === quartoIdEscolhido)
  if (escolhido && !disponiveis.some(quarto => quarto.id === escolhido.id)) {
    opcoes.push({ quarto: escolhido, disponivel: false })
  }
  return opcoes
}

const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
export const formatarMoeda = valor => moeda.format(valor)
