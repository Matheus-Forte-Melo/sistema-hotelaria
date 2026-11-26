import { useEstrutura, useReservas } from '../../../contexts/HotelContexts'
import { quartosDisponiveis } from '../../../data/regrasReserva'

/**
 * Substituto temporário da busca de disponibilidade da issue #7 (frontend) / #2 (backend).
 * Calcula a disponibilidade com os dados já carregados e devolve { dados, carregando, erro }.
 * Trocar pelo hook compartilhado de `features/quartos/` quando ele estiver disponível.
 */
export function useQuartosDisponiveisTemporario({ entrada, saida, pessoas, ignorarReservaId = null }) {
  const estrutura = useEstrutura()
  const reservas = useReservas()
  const carregando = estrutura.carregando || reservas.carregando
  const erro = estrutura.erro || reservas.erro
  const dados = carregando || erro
    ? []
    : quartosDisponiveis(estrutura.dados.quartos, reservas.dados, { entrada, saida, pessoas, ignorarReservaId })
  return { dados, carregando, erro }
}
