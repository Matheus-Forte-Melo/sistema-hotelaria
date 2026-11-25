package br.com.hotelaria.hotelaria.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.hotelaria.hotelaria.dto.ReservaRequest;
import br.com.hotelaria.hotelaria.dto.ReservaResponse;
import br.com.hotelaria.hotelaria.exception.ConflitoReservaException;
import br.com.hotelaria.hotelaria.exception.RecursoNaoEncontradoException;
import br.com.hotelaria.hotelaria.exception.RegraNegocioException;
import br.com.hotelaria.hotelaria.model.Quarto;
import br.com.hotelaria.hotelaria.model.Reserva;
import br.com.hotelaria.hotelaria.model.SituacaoReserva;
import br.com.hotelaria.hotelaria.repository.QuartoRepository;
import br.com.hotelaria.hotelaria.repository.ReservaRepository;

/**
 * Criação e edição de reservas (issue #3, RF08, RF12). Capacidade e conflito são verificados na mesma
 * transação da gravação, com a linha do quarto bloqueada para serializar pedidos simultâneos (regra 3).
 * Também localiza, detalha e cancela reservas (issue #4).
 */
@Service
public class ReservaService {

    public static final String MOTIVO_PADRAO_CANCELAMENTO = "Cancelada pela recepção";

    private final ReservaRepository reservas;
    private final QuartoRepository quartos;

    public ReservaService(ReservaRepository reservas, QuartoRepository quartos) {
        this.reservas = reservas;
        this.quartos = quartos;
    }

    @Transactional
    public ReservaResponse criar(ReservaRequest pedido) {
        validarPeriodo(pedido);
        Quarto quarto = bloquearQuarto(pedido.quartoId());
        verificarConflito(quarto, pedido, null);
        Reserva reserva = new Reserva(quarto, pedido.nomeHospede(), pedido.dataEntrada(), pedido.dataSaida(),
            quantidade(pedido), diaria(pedido, quarto));
        return ReservaResponse.de(reservas.saveAndFlush(reserva));
    }

    /**
     * Revalida quarto, período e pessoas desconsiderando a própria reserva. A verificação de conflito
     * acontece antes de alterar a entidade, de modo que uma recusa mantém os dados anteriores.
     */
    @Transactional
    public ReservaResponse editar(Long id, ReservaRequest pedido) {
        Reserva reserva = reservas.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva " + id + " não encontrada"));
        validarPeriodo(pedido);
        Quarto quarto = bloquearQuarto(pedido.quartoId());
        verificarConflito(quarto, pedido, reserva.getId());
        reserva.alterar(quarto, pedido.nomeHospede(), pedido.dataEntrada(), pedido.dataSaida(),
            quantidade(pedido), diaria(pedido, quarto));
        return ReservaResponse.de(reservas.saveAndFlush(reserva));
    }

    /**
     * Localiza reservas pelo nome de referência (issue #4, RF08), por trecho e sem diferenciar maiúsculas.
     * Inclui as canceladas, que continuam consultáveis (regra 4). Ordena por entrada para distinguir
     * reservas do mesmo nome por período e quarto.
     */
    @Transactional(readOnly = true)
    public List<ReservaResponse> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("Informe o nome do hóspede para buscar");
        }
        return reservas.findByNomeHospedeContainingIgnoreCaseOrderByDataEntradaPrevistaAscIdAsc(nome.strip())
            .stream().map(ReservaResponse::de).toList();
    }

    /** Detalhes da reserva: quarto, período, pessoas, diária, total e situação (issue #4). */
    @Transactional(readOnly = true)
    public ReservaResponse consultar(Long id) {
        return ReservaResponse.de(buscar(id));
    }

    /**
     * Cancela a reserva (issue #4, regra 4). O registro é preservado com situação Cancelada e deixa de
     * bloquear o período, pois a busca de conflitos considera só reservas vigentes. Sem motivo informado,
     * registra {@link #MOTIVO_PADRAO_CANCELAMENTO}, já que o banco exige motivo em toda reserva cancelada.
     */
    @Transactional
    public ReservaResponse cancelar(Long id, String motivo) {
        Reserva reserva = buscar(id);
        if (reserva.getSituacao() == SituacaoReserva.CANCELADA) {
            throw new RegraNegocioException("A reserva " + id + " já está cancelada");
        }
        reserva.cancelar(Instant.now(), motivo == null || motivo.isBlank() ? MOTIVO_PADRAO_CANCELAMENTO : motivo.strip());
        return ReservaResponse.de(reservas.saveAndFlush(reserva));
    }

    private Reserva buscar(Long id) {
        return reservas.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva " + id + " não encontrada"));
    }

    private Quarto bloquearQuarto(Long quartoId) {
        if (quartoId == null) {
            throw new RegraNegocioException("Quarto é obrigatório");
        }
        return quartos.findByIdParaAtualizacao(quartoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Quarto " + quartoId + " não encontrado"));
    }

    private void verificarConflito(Quarto quarto, ReservaRequest pedido, Long ignorarId) {
        if (reservas.existeConflito(quarto.getId(), pedido.dataEntrada(), pedido.dataSaida(), ignorarId)) {
            throw new ConflitoReservaException("O quarto " + quarto.getIdentificacao()
                + " já está reservado em alguma noite de " + pedido.dataEntrada() + " a " + pedido.dataSaida());
        }
    }

    /** Rejeita período inválido antes da consulta de conflito, que pressupõe saída posterior à entrada. */
    private static void validarPeriodo(ReservaRequest pedido) {
        if (pedido.dataEntrada() == null || pedido.dataSaida() == null
                || !pedido.dataSaida().isAfter(pedido.dataEntrada())) {
            throw new RegraNegocioException("Data de saída deve ser posterior à data de entrada");
        }
    }

    private static int quantidade(ReservaRequest pedido) {
        return pedido.quantidadePessoas() == null ? 0 : pedido.quantidadePessoas();
    }

    private static BigDecimal diaria(ReservaRequest pedido, Quarto quarto) {
        return pedido.valorDiaria() != null ? pedido.valorDiaria() : quarto.getValorDiariaPadrao();
    }
}
