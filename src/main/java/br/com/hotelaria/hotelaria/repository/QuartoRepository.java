package br.com.hotelaria.hotelaria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.hotelaria.hotelaria.model.Quarto;
import jakarta.persistence.LockModeType;

public interface QuartoRepository extends JpaRepository<Quarto, Long> {

    List<Quarto> findByAndarIdOrderByIdentificacao(Long andarId);

    /** Identificação única no bloco (RF03). */
    boolean existsByAndarBlocoIdAndIdentificacaoIgnoreCase(Long blocoId, String identificacao);

    /**
     * Bloqueia a linha do quarto até o fim da transação. Serializa reservas, check-ins e bloqueios
     * concorrentes do mesmo quarto antes da verificação de conflito (RN09, RNF07).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from Quarto q where q.id = :id")
    Optional<Quarto> findByIdParaAtualizacao(@Param("id") Long id);
}
