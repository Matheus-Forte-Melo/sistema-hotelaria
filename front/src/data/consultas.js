import { mutationOptions, queryOptions } from '@tanstack/react-query'

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

/** Consultas cujo resultado muda quando uma reserva é criada, editada ou cancelada. */
export const consultasAfetadasPorReserva = [['reservas'], ['disponibilidade'], ['mapa']]

/** Cria (sem `id`) ou edita (com `id`) uma reserva e atualiza as consultas afetadas. */
export function mutacaoSalvarReserva(repositories, queryClient) {
  return mutationOptions({
    mutationFn: ({ id, pedido }) => id == null
      ? repositories.reservas.criar(pedido)
      : repositories.reservas.editar(id, pedido),
    onSuccess: () => Promise.all(consultasAfetadasPorReserva
      .map(queryKey => queryClient.invalidateQueries({ queryKey }))),
  })
}
