package br.com.hotelaria.hotelaria.dto;

import java.math.BigDecimal;

import br.com.hotelaria.hotelaria.model.Quarto;

/** Quarto como a recepção o escolhe: andar, identificação, capacidade e diária padrão. */
public record QuartoResponse(
    Long id,
    Long andarId,
    int andarNumero,
    String identificacao,
    int capacidade,
    BigDecimal valorDiariaPadrao) {

    /** Deve ser chamado dentro da transação ou com o andar já carregado. */
    public static QuartoResponse de(Quarto quarto) {
        return new QuartoResponse(
            quarto.getId(),
            quarto.getAndar().getId(),
            quarto.getAndar().getNumero(),
            quarto.getIdentificacao(),
            quarto.getCapacidade(),
            quarto.getValorDiariaPadrao());
    }
}
