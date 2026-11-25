import { createContext, useContext } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { consultaBuscaReservas, consultaEstrutura, consultaReserva, consultaReservas, mutacaoCancelarReserva } from '../data/consultas'

const RepositoriosContext = createContext(null)

export function HotelProvider({ repositories, children }) {
  return <RepositoriosContext.Provider value={repositories}>{children}</RepositoriosContext.Provider>
}

function useLista(opcoes) {
  const { data, isPending, isError } = useQuery(opcoes)
  return { dados: data, carregando: isPending, erro: isError }
}

// Para consultas que só rodam sob demanda: desativadas, não contam como carregando.
function useConsultaSobDemanda(opcoes) {
  const { data, isLoading, isError, error } = useQuery(opcoes)
  return { dados: data, carregando: isLoading, erro: isError, mensagemErro: error?.message }
}

export function useEstrutura() {
  const repositories = useContext(RepositoriosContext)
  return useLista(consultaEstrutura(repositories))
}

export function useReservas() {
  const repositories = useContext(RepositoriosContext)
  return useLista(consultaReservas(repositories))
}

export function useBuscaReservas(nome) {
  const repositories = useContext(RepositoriosContext)
  return useConsultaSobDemanda(consultaBuscaReservas(repositories, nome))
}

export function useReserva(id) {
  const repositories = useContext(RepositoriosContext)
  return useConsultaSobDemanda(consultaReserva(repositories, id))
}

export function useCancelarReserva() {
  const repositories = useContext(RepositoriosContext)
  const queryClient = useQueryClient()
  return useMutation(mutacaoCancelarReserva(repositories, queryClient))
}
