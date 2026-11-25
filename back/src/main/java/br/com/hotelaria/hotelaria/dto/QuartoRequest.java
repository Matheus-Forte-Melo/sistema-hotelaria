package br.com.hotelaria.hotelaria.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Dados para cadastrar um quarto em um andar (RF04). */
public record QuartoRequest(
    @NotNull(message = "Informe o andar") Long andarId,
    @NotBlank(message = "Informe a identificação do quarto")
    @Size(max = 10, message = "A identificação deve ter até 10 caracteres") String identificacao,
    @NotNull(message = "Informe a capacidade")
    @Positive(message = "A capacidade deve ser pelo menos 1") Integer capacidade,
    @NotNull(message = "Informe a diária padrão")
    @DecimalMin(value = "0.00", message = "A diária não pode ser negativa") BigDecimal valorDiariaPadrao) {
}
