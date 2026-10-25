package br.com.hotelaria.hotelaria.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.hotelaria.hotelaria.model.Indisponibilidade;

public interface IndisponibilidadeRepository extends JpaRepository<Indisponibilidade, Long> {

    /** Existe bloqueio do quarto sobrepondo [inicio, fim)? (RN09, RN19) */
    @Query("""
        select count(i) > 0 from Indisponibilidade i
        where i.quarto.id = :quartoId
          and i.inicio < :fim
          and i.fim > :inicio
          and (:ignorarId is null or i.id <> :ignorarId)
        """)
    boolean existeSobreposicao(@Param("quartoId") Long quartoId, @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim, @Param("ignorarId") Long ignorarId);

    List<Indisponibilidade> findByQuartoIdOrderByInicio(Long quartoId);
}
