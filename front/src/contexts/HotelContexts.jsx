import { createContext, useContext } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { consultaEstrutura, consultaReservas, mutacaoSalvarReserva } from '../data/consultas'

const RepositoriosContext = createContext(null)

export function HotelProvider({ repositories, children }) {
  return <RepositoriosContext.Provider value={repositories}>{children}</RepositoriosContext.Provider>
}

function useLista(opcoes) {
  const { data, isPending, isError } = useQuery(opcoes)
  return { dados: data, carregando: isPending, erro: isError }
}

export function useEstrutura() {
  const repositories = useContext(RepositoriosContext)
  return useLista(consultaEstrutura(repositories))
}

export function useReservas() {
  const repositories = useContext(RepositoriosContext)
  return useLista(consultaReservas(repositories))
}

/**
 * Cria ou edita uma reserva: `salvar({ pedido })` cria e `salvar({ id, pedido })` edita.
 * `erro` traz a mensagem da recusa para a tela exibir como motivo.
 */
export function useSalvarReserva() {
  const repositories = useContext(RepositoriosContext)
  const queryClient = useQueryClient()
  const mutacao = useMutation(mutacaoSalvarReserva(repositories, queryClient))
  return {
    salvar: mutacao.mutate,
    salvando: mutacao.isPending,
    erro: mutacao.error?.message ?? null,
    reiniciar: mutacao.reset,
  }
}
