// Regras de pernoite usadas pelos dados de demonstração, iguais às do backend:
// uma reserva de 10 a 12 ocupa as noites de 10 e 11, e o dia 12 fica livre para outra entrada.
// Datas no formato AAAA-MM-DD podem ser comparadas como texto.

export const periodoValido = ({ entrada, saida }) => Boolean(entrada && saida && saida > entrada)

export function haConflito(reservas, { quartoId, entrada, saida }, ignorarReservaId = null) {
  return reservas.some(reserva => reserva.situacao === 'Vigente'
    && reserva.quartoId === Number(quartoId)
    && reserva.id !== ignorarReservaId
    && reserva.entrada < saida
    && reserva.saida > entrada)
}

export function quartosDisponiveis(quartos, reservas, { entrada, saida, pessoas, ignorarReservaId = null }) {
  if (!periodoValido({ entrada, saida })) return []
  return quartos.filter(quarto => quarto.capacidade >= Number(pessoas || 1)
    && !haConflito(reservas, { quartoId: quarto.id, entrada, saida }, ignorarReservaId))
}
