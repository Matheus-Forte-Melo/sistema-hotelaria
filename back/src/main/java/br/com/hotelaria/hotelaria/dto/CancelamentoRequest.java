package br.com.hotelaria.hotelaria.dto;

import jakarta.validation.constraints.Size;

/** Motivo opcional do cancelamento. Sem ele, o serviço registra o motivo padrão. */
public record CancelamentoRequest(@Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres") String motivo) {
}
