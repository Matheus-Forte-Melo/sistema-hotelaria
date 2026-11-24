package br.com.hotelaria.hotelaria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.hotelaria.hotelaria.model.CorrecaoIndisponibilidade;

public interface CorrecaoIndisponibilidadeRepository extends JpaRepository<CorrecaoIndisponibilidade, Long> {

    List<CorrecaoIndisponibilidade> findByIndisponibilidadeIdOrderByCorrigidaEmDesc(Long indisponibilidadeId);
}
