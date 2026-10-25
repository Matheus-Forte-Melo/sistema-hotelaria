package br.com.hotelaria.hotelaria.exception;

/**
 * Violação de regra de negócio detectada pelo domínio.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String message) {
        super(message);
    }
}
