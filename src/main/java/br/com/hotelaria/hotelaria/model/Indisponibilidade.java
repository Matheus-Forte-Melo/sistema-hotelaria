package br.com.hotelaria.hotelaria.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hibernate.annotations.CreationTimestamp;

import br.com.hotelaria.hotelaria.exception.RegraNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Bloqueio de um quarto por período [inicio, fim) com motivo (RF05, RN19). Alterações de bloqueios
 * que já começaram geram uma {@link CorrecaoIndisponibilidade}.
 */
@Entity
@Table(name = "indisponibilidade",
    indexes = @Index(name = "ix_indisponibilidade_quarto_periodo", columnList = "quarto_id, inicio, fim"),
    check = @CheckConstraint(name = "ck_indisponibilidade_periodo", constraint = "fim > inicio"))
public class Indisponibilidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "quarto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_indisponibilidade_quarto"))
    private Quarto quarto;

    @NotNull
    @Column(nullable = false)
    private LocalDate inicio;

    @NotNull
    @Column(nullable = false)
    private LocalDate fim;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String motivo;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "registrada_por_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_indisponibilidade_funcionario"))
    private Funcionario registradaPor;

    @CreationTimestamp
    @Column(name = "registrada_em", nullable = false, updatable = false)
    private Instant registradaEm;

    @OneToMany(mappedBy = "indisponibilidade", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @OrderBy("corrigidaEm")
    private List<CorrecaoIndisponibilidade> correcoes = new ArrayList<>();

    protected Indisponibilidade() {
    }

    public Indisponibilidade(Quarto quarto, LocalDate inicio, LocalDate fim, String motivo, Funcionario registradaPor) {
        if (inicio == null || fim == null || !fim.isAfter(inicio)) {
            throw new RegraNegocioException("Fim do bloqueio deve ser posterior ao início");
        }
        this.quarto = quarto;
        this.inicio = inicio;
        this.fim = fim;
        this.motivo = motivo;
        this.registradaPor = registradaPor;
    }

    /** O bloqueio já afetou algum dia anterior a {@code hoje} e, portanto, os indicadores históricos. */
    public boolean afetaPassado(LocalDate hoje) {
        return inicio.isBefore(hoje);
    }

    /**
     * Altera período e motivo (RF05). Somente administradores. Se o bloqueio já afetou o passado,
     * exige justificativa e registra os valores anteriores (RN19). Conflitos com reservas e outros
     * bloqueios no novo período devem ser verificados no serviço.
     */
    public Optional<CorrecaoIndisponibilidade> alterar(LocalDate novoInicio, LocalDate novoFim, String novoMotivo,
            Funcionario autor, String justificativa, LocalDate hoje) {
        if (!autor.isAdministrador()) {
            throw new RegraNegocioException("Somente administradores podem alterar bloqueios");
        }
        if (novoInicio == null || novoFim == null || !novoFim.isAfter(novoInicio)) {
            throw new RegraNegocioException("Fim do bloqueio deve ser posterior ao início");
        }
        CorrecaoIndisponibilidade correcao = null;
        if (afetaPassado(hoje)) {
            if (justificativa == null || justificativa.isBlank()) {
                throw new RegraNegocioException("Alteração de bloqueio passado exige justificativa");
            }
            correcao = new CorrecaoIndisponibilidade(this, autor, justificativa, inicio, fim, motivo,
                novoInicio, novoFim, novoMotivo);
            correcoes.add(correcao);
        }
        this.inicio = novoInicio;
        this.fim = novoFim;
        this.motivo = novoMotivo;
        return Optional.ofNullable(correcao);
    }

    public boolean sobrepoe(LocalDate outroInicio, LocalDate outroFim) {
        return inicio.isBefore(outroFim) && fim.isAfter(outroInicio);
    }

    public boolean abrange(LocalDate data) {
        return !data.isBefore(inicio) && data.isBefore(fim);
    }

    public Long getId() {
        return id;
    }

    public Quarto getQuarto() {
        return quarto;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFim() {
        return fim;
    }

    public String getMotivo() {
        return motivo;
    }

    public Funcionario getRegistradaPor() {
        return registradaPor;
    }

    public Instant getRegistradaEm() {
        return registradaEm;
    }

    public List<CorrecaoIndisponibilidade> getCorrecoes() {
        return Collections.unmodifiableList(correcoes);
    }
}
