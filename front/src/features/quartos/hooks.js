import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useRepositorios } from '../../contexts/HotelContexts'
import { consultaDisponibilidade, consultaQuartos, mutacaoCadastrarQuarto } from './consultas'

// Consultas desativadas não contam como carregando.
function useConsulta(opcoes) {
  const { data, isLoading, isError, error } = useQuery(opcoes)
  return { dados: data, carregando: isLoading, erro: isError, mensagemErro: error?.message }
}

export function useQuartos(andarId) {
  return useConsulta(consultaQuartos(useRepositorios(), andarId))
}

/** Quartos livres para `{ entrada, saida, pessoas }`. Com `filtro` null, não consulta. Usado também pela #8. */
export function useDisponibilidade(filtro) {
  return useConsulta(consultaDisponibilidade(useRepositorios(), filtro))
}

export function useCadastrarQuarto() {
  return useMutation(mutacaoCadastrarQuarto(useRepositorios(), useQueryClient()))
}
