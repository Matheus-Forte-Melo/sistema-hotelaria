package br.com.hotelaria.hotelaria.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.hotelaria.hotelaria.model.Reserva;
import br.com.hotelaria.hotelaria.model.SituacaoReserva;

/** Reserva como a interface a exibe: quarto, período, pessoas, diária acordada e total previsto. */
public record ReservaResponse(
    Long id,
    String nomeHospede,
    Long quartoId,
    String quartoIdentificacao,
    int andarNumero,
    LocalDate dataEntrada,
    LocalDate dataSaida,
    int quantidadePessoas,
    BigDecimal valorDiaria,
    long quantidadeDiarias,
    BigDecimal valorTotal,
    SituacaoReserva situacao) {

    /** Deve ser chamado dentro da transação, pois quarto e andar são carregados sob demanda. */
    public static ReservaResponse de(Reserva reserva) {
        return new ReservaResponse(
            reserva.getId(),
            reserva.getNomeHospede(),
            reserva.getQuarto().getId(),
            reserva.getQuarto().getIdentificacao(),
            reserva.getQuarto().getAndar().getNumero(),
            reserva.getDataEntradaPrevista(),
            reserva.getDataSaidaPrevista(),
            reserva.getQuantidadePessoas(),
            reserva.getValorDiaria(),
            reserva.quantidadeDiarias(),
            reserva.valorPrevisto(),
            reserva.getSituacao());
    }
}
