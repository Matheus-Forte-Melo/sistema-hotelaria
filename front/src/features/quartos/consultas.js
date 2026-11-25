import { queryOptions } from '@tanstack/react-query'

export function consultaQuartos(repositories, andarId) {
  return queryOptions({
    queryKey: ['quartos', andarId ?? null],
    queryFn: () => repositories.quartos.listar(andarId),
  })
}

/** Só consulta depois que a recepção pede uma busca válida (`filtro` diferente de null). */
export function consultaDisponibilidade(repositories, filtro) {
  return queryOptions({
    queryKey: ['disponibilidade', filtro?.entrada, filtro?.saida, filtro?.pessoas],
    queryFn: () => repositories.quartos.buscarDisponiveis(filtro),
    enabled: filtro != null,
  })
}

/** Um quarto novo muda a lista do andar, a busca, os totais da estrutura e a grade do mapa. */
export function mutacaoCadastrarQuarto(repositories, queryClient) {
  return {
    mutationFn: quarto => repositories.quartos.cadastrar(quarto),
    onSuccess: () => Promise.all(['quartos', 'disponibilidade', 'estrutura', 'mapa'].map(chave =>
      queryClient.invalidateQueries({ queryKey: [chave] }))),
  }
}
