const ESTADO_INICIAL = { etapa: 'inativo', reservaId: null, erro: null }

/**
 * Confirmação do cancelamento: pedir → confirmando → (desistir | confirmar → enviando → concluir | falhar).
 * A situação “Cancelada” só aparece depois da resposta da API, quando as consultas são atualizadas.
 */
export function fluxoCancelamento(estado = ESTADO_INICIAL, acao) {
  switch (acao.tipo) {
    case 'pedir': return { etapa: 'confirmando', reservaId: acao.reservaId, erro: null }
    case 'desistir': return ESTADO_INICIAL
    case 'confirmar': return { ...estado, etapa: 'enviando', erro: null }
    case 'concluir': return ESTADO_INICIAL
    case 'falhar': return { ...estado, etapa: 'erro', erro: acao.mensagem }
    default: return estado
  }
}

const formatarData = data => data.split('-').reverse().join('/')

export const rotuloPeriodo = (entrada, saida) => `${formatarData(entrada)} a ${formatarData(saida)}`

export const formatarMoeda = valor => valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
