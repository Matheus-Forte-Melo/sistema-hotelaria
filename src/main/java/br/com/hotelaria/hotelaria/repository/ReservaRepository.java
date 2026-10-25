package br.com.hotelaria.hotelaria.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.hotelaria.hotelaria.model.Reserva;
import br.com.hotelaria.hotelaria.model.SituacaoReserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /**
     * Existe reserva ativa (confirmada ou em andamento) do quarto sobrepondo [inicio, fim)?
     * Reservas canceladas ou finalizadas não bloqueiam (RN09). {@code ignorarId} exclui a própria
     * reserva em alterações; use {@code null} na criação.
     */
    @Query("""
        select count(r) > 0 from Reserva r
        where r.quarto.id = :quartoId
          and r.situacao in (br.com.hotelaria.hotelaria.model.SituacaoReserva.CONFIRMADA,
                             br.com.hotelaria.hotelaria.model.SituacaoReserva.EM_ANDAMENTO)
          and r.dataEntradaPrevista < :fim
          and r.dataSaidaPrevista > :inicio
          and (:ignorarId is null or r.id <> :ignorarId)
        """)
    boolean existeConflito(@Param("quartoId") Long quartoId, @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim, @Param("ignorarId") Long ignorarId);

    /** Reservas do quarto que ocupam alguma noite de [inicio, fim), para mapa e detalhes. */
    @Query("""
        select r from Reserva r
        where r.quarto.id = :quartoId
          and r.dataEntradaPrevista < :fim
          and r.dataSaidaPrevista > :inicio
        order by r.dataEntradaPrevista
        """)
    List<Reserva> findByQuartoNoPeriodo(@Param("quartoId") Long quartoId, @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);

    List<Reserva> findByQuartoIdAndSituacao(Long quartoId, SituacaoReserva situacao);

    /** Histórico do hóspede (RF18). */
    List<Reserva> findByHospedesIdOrderByDataEntradaPrevistaDesc(Long hospedeId);
}
