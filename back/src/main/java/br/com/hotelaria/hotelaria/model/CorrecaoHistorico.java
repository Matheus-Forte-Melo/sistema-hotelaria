package br.com.hotelaria.hotelaria.model;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Registro imutável de correção dos horários efetivos de uma estadia encerrada (RF20):
 * autor, justificativa e valores anteriores. Criado somente por {@link Reserva#corrigirHorariosEfetivos}.
 */
@Entity
@Table(name = "correcao_historico",
    check = @CheckConstraint(name = "ck_correcao_periodo", constraint = "check_out_novo > check_in_novo"))
public class CorrecaoHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_correcao_reserva"))
    private Reserva reserva;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_correcao_funcionario"))
    private Funcionario autor;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500, updatable = false)
    private String justificativa;

    @Column(name = "check_in_anterior", updatable = false)
    private Instant checkInAnterior;

    @Column(name = "check_out_anterior", updatable = false)
    private Instant checkOutAnterior;

    @NotNull
    @Column(name = "check_in_novo", nullable = false, updatable = false)
    private Instant checkInNovo;

    @NotNull
    @Column(name = "check_out_novo", nullable = false, updatable = false)
    private Instant checkOutNovo;

    @CreationTimestamp
    @Column(name = "corrigida_em", nullable = false, updatable = false)
    private Instant corrigidaEm;

    protected CorrecaoHistorico() {
    }

    CorrecaoHistorico(Reserva reserva, Funcionario autor, String justificativa, Instant checkInAnterior,
            Instant checkOutAnterior, Instant checkInNovo, Instant checkOutNovo) {
        this.reserva = reserva;
        this.autor = autor;
        this.justificativa = justificativa;
        this.checkInAnterior = checkInAnterior;
        this.checkOutAnterior = checkOutAnterior;
        this.checkInNovo = checkInNovo;
        this.checkOutNovo = checkOutNovo;
    }

    public Long getId() {
        return id;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public Funcionario getAutor() {
        return autor;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public Instant getCheckInAnterior() {
        return checkInAnterior;
    }

    public Instant getCheckOutAnterior() {
        return checkOutAnterior;
    }

    public Instant getCheckInNovo() {
        return checkInNovo;
    }

    public Instant getCheckOutNovo() {
        return checkOutNovo;
    }

    public Instant getCorrigidaEm() {
        return corrigidaEm;
    }
}
