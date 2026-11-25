// Regras de entrada da tela de quartos (issue #7). As mensagens são as mesmas que a API devolve (issue #2).
export const MENSAGENS = {
  datas: 'Informe as datas de entrada e de saída',
  periodo: 'A data de saída deve ser posterior à data de entrada',
  pessoas: 'A quantidade de pessoas deve ser pelo menos 1',
}

const vazio = valor => valor == null || String(valor).trim() === ''
const inteiroPositivo = valor => !vazio(valor) && Number.isInteger(Number(valor)) && Number(valor) >= 1

/** Datas no formato AAAA-MM-DD, com saída exclusiva: comparar o texto equivale a comparar as datas. */
export function validarBusca({ entrada, saida, pessoas }) {
  if (vazio(entrada) || vazio(saida)) return MENSAGENS.datas
  if (saida <= entrada) return MENSAGENS.periodo
  if (!inteiroPositivo(pessoas)) return MENSAGENS.pessoas
  return null
}

export function validarQuarto({ andarId, numero, capacidade, diaria }) {
  if (vazio(andarId)) return 'Selecione o andar'
  if (vazio(numero)) return 'Informe a identificação do quarto'
  if (String(numero).trim().length > 10) return 'A identificação deve ter até 10 caracteres'
  if (!inteiroPositivo(capacidade)) return 'A capacidade deve ser pelo menos 1'
  if (vazio(diaria)) return 'Informe a diária padrão'
  if (!(Number(diaria) >= 0)) return 'A diária não pode ser negativa'
  return null
}

export const formatarMoeda = valor => Number(valor).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
