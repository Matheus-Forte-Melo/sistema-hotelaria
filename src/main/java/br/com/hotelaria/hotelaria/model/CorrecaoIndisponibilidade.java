package br.com.hotelaria.hotelaria.model;

import java.time.Instant;
import java.time.LocalDate;

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
 * Registro imutável de alteração de um bloqueio que já afetou o passado (RN19): autor, justificativa,
 * valores anteriores e novos. Criado somente por {@link Indisponibilidade#alterar}.
 */
@Entity
@Table(name = "correcao_indisponibilidade",
    check = @CheckConstraint(name = "ck_correcao_indisponibilidade_periodo", constraint = "fim_novo > inicio_novo"))
public class CorrecaoIndisponibilidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "indisponibilidade_id", nullable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_correcao_indisponibilidade_indisponibilidade"))
    private Indisponibilidade indisponibilidade;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false, updatable = false,
        foreignKey = @ForeignKey(name = "fk_correcao_indisponibilidade_funcionario"))
    private Funcionario autor;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500, updatable = false)
    private String justificativa;

    @NotNull
    @Column(name = "inicio_anterior", nullable = false, updatable = false)
    private LocalDate inicioAnterior;

    @NotNull
    @Column(name = "fim_anterior", nullable = false, updatable = false)
    private LocalDate fimAnterior;

    @NotBlank
    @Column(name = "motivo_anterior", nullable = false, length = 255, updatable = false)
    private String motivoAnterior;

    @NotNull
    @Column(name = "inicio_novo", nullable = false, updatable = false)
    private LocalDate inicioNovo;

    @NotNull
    @Column(name = "fim_novo", nullable = false, updatable = false)
    private LocalDate fimNovo;

    @NotBlank
    @Column(name = "motivo_novo", nullable = false, length = 255, updatable = false)
    private String motivoNovo;

    @CreationTimestamp
    @Column(name = "corrigida_em", nullable = false, updatable = false)
    private Instant corrigidaEm;

    protected CorrecaoIndisponibilidade() {
    }

    CorrecaoIndisponibilidade(Indisponibilidade indisponibilidade, Funcionario autor, String justificativa,
            LocalDate inicioAnterior, LocalDate fimAnterior, String motivoAnterior,
            LocalDate inicioNovo, LocalDate fimNovo, String motivoNovo) {
        this.indisponibilidade = indisponibilidade;
        this.autor = autor;
        this.justificativa = justificativa;
        this.inicioAnterior = inicioAnterior;
        this.fimAnterior = fimAnterior;
        this.motivoAnterior = motivoAnterior;
        this.inicioNovo = inicioNovo;
        this.fimNovo = fimNovo;
        this.motivoNovo = motivoNovo;
    }

    public Long getId() {
        return id;
    }

    public Indisponibilidade getIndisponibilidade() {
        return indisponibilidade;
    }

    public Funcionario getAutor() {
        return autor;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public LocalDate getInicioAnterior() {
        return inicioAnterior;
    }

    public LocalDate getFimAnterior() {
        return fimAnterior;
    }

    public String getMotivoAnterior() {
        return motivoAnterior;
    }

    public LocalDate getInicioNovo() {
        return inicioNovo;
    }

    public LocalDate getFimNovo() {
        return fimNovo;
    }

    public String getMotivoNovo() {
        return motivoNovo;
    }

    public Instant getCorrigidaEm() {
        return corrigidaEm;
    }
}
