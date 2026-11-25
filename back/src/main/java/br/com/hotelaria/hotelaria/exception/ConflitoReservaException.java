package br.com.hotelaria.hotelaria.exception;

/** O quarto já tem reserva vigente em alguma noite do período pedido (regra 3). */
public class ConflitoReservaException extends RegraNegocioException {

    public ConflitoReservaException(String message) {
        super(message);
    }
}
