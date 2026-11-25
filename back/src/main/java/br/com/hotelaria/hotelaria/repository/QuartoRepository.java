package br.com.hotelaria.hotelaria.repository;

import java.time.LocalDate;
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

    /** Quartos de um andar com o andar já carregado, para a listagem da recepção. */
    @Query("select q from Quarto q join fetch q.andar a where a.id = :andarId order by q.identificacao")
    List<Quarto> listarDoAndar(@Param("andarId") Long andarId);

    /** Todos os quartos com o andar já carregado, ordenados por andar e identificação. */
    @Query("select q from Quarto q join fetch q.andar a order by a.numero, q.identificacao")
    List<Quarto> listarTodos();

    /**
     * Quartos que comportam {@code pessoas} e não têm reserva vigente (confirmada ou em andamento)
     * em nenhuma noite de [entrada, saida). A saída é exclusiva: uma reserva de 10 a 12 não impede
     * a entrada no dia 12. Reservas canceladas não bloqueiam (regras 1 a 4 do escopo revisado).
     */
    @Query("""
        select q from Quarto q join fetch q.andar a
        where q.capacidade >= :pessoas
          and not exists (
            select 1 from Reserva r
            where r.quarto = q
              and r.situacao in (br.com.hotelaria.hotelaria.model.SituacaoReserva.CONFIRMADA,
                                 br.com.hotelaria.hotelaria.model.SituacaoReserva.EM_ANDAMENTO)
              and r.dataEntradaPrevista < :saida
              and r.dataSaidaPrevista > :entrada)
        order by a.numero, q.identificacao
        """)
    List<Quarto> findDisponiveis(@Param("entrada") LocalDate entrada, @Param("saida") LocalDate saida,
        @Param("pessoas") int pessoas);

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
