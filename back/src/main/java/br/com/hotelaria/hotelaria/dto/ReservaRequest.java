package br.com.hotelaria.hotelaria.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Dados para criar ou editar uma reserva. Datas no formato AAAA-MM-DD; a saída é exclusiva
 * (uma reserva de 10 a 12 ocupa as noites de 10 e 11). Sem {@code valorDiaria}, usa a diária
 * padrão do quarto no momento da gravação.
 */
public record ReservaRequest(
    @NotBlank(message = "Informe o nome do hóspede") @Size(max = 120) String nomeHospede,
    @NotNull(message = "Informe o quarto") Long quartoId,
    @NotNull(message = "Informe a data de entrada") LocalDate dataEntrada,
    @NotNull(message = "Informe a data de saída") LocalDate dataSaida,
    @NotNull(message = "Informe a quantidade de pessoas")
    @Positive(message = "A quantidade de pessoas deve ser pelo menos 1") Integer quantidadePessoas,
    @DecimalMin(value = "0.00", message = "A diária não pode ser negativa") BigDecimal valorDiaria) {
}
