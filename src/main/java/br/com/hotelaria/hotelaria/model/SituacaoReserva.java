package br.com.hotelaria.hotelaria.model;

/**
 * Ciclo de vida da reserva (RN11). A hospedagem é a própria reserva a partir do check-in.
 */
public enum SituacaoReserva {
    CONFIRMADA,
    EM_ANDAMENTO,
    FINALIZADA,
    CANCELADA;

    /** Situações que ocupam o quarto e bloqueiam disponibilidade (RN09). */
    public boolean isAtiva() {
        return this == CONFIRMADA || this == EM_ANDAMENTO;
    }
}
