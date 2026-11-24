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
