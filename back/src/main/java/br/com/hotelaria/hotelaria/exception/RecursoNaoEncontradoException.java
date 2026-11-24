package br.com.hotelaria.hotelaria.exception;

/** Registro pedido pelo identificador não existe. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
