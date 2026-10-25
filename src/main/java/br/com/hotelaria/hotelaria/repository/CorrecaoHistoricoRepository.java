package br.com.hotelaria.hotelaria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.hotelaria.hotelaria.model.CorrecaoHistorico;

public interface CorrecaoHistoricoRepository extends JpaRepository<CorrecaoHistorico, Long> {

    List<CorrecaoHistorico> findByReservaIdOrderByCorrigidaEmDesc(Long reservaId);
}
