import { createContext, useContext } from 'react'
import { useQuery } from '@tanstack/react-query'
import { consultaEstrutura, consultaReservas } from '../data/consultas'

const RepositoriosContext = createContext(null)

export function HotelProvider({ repositories, children }) {
  return <RepositoriosContext.Provider value={repositories}>{children}</RepositoriosContext.Provider>
}

/** Repositórios injetados no HotelProvider, para os hooks das features. */
export function useRepositorios() {
  return useContext(RepositoriosContext)
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
