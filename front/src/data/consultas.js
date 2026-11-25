import { queryOptions } from '@tanstack/react-query'

export function consultaEstrutura(repositories) {
  return queryOptions({
    queryKey: ['estrutura'],
    queryFn: () => repositories.estrutura.listar(),
  })
}

export function consultaReservas(repositories) {
  return queryOptions({
    queryKey: ['reservas'],
    queryFn: () => repositories.reservas.listar(),
  })
}

export function consultaBuscaReservas(repositories, nome) {
  const termo = nome.trim()
  return queryOptions({
    queryKey: ['reservas', 'busca', termo],
    queryFn: () => repositories.reservas.buscarPorNome(termo),
    enabled: termo.length > 0,
  })
}

export function consultaReserva(repositories, id) {
  return queryOptions({
    queryKey: ['reservas', 'detalhe', id],
    queryFn: () => repositories.reservas.detalhar(id),
    enabled: id != null,
  })
}

/** Após cancelar, a reserva sai da grade e libera o período: invalida reservas, disponibilidade e mapa. */
export function mutacaoCancelarReserva(repositories, queryClient) {
  return {
    mutationFn: ({ reservaId, motivo }) => repositories.reservas.cancelar(reservaId, motivo),
    onSuccess: reserva => {
      queryClient.setQueryData(['reservas', 'detalhe', reserva.id], reserva)
      return Promise.all(['reservas', 'disponibilidade', 'mapa'].map(chave =>
        queryClient.invalidateQueries({ queryKey: [chave] })))
    },
  }
}
