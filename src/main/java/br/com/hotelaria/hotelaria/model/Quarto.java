package br.com.hotelaria.hotelaria.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.hotelaria.hotelaria.exception.RegraNegocioException;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Unidade reservável (RF04). Guarda o período de atividade para não aplicar a configuração
 * atual ao passado (RN15). A unicidade da identificação no bloco (RF03) é verificada no serviço
 * com {@code QuartoRepository.existsByAndarBlocoIdAndIdentificacaoIgnoreCase}; o banco garante
 * a unicidade no andar.
 */
@Entity
@Table(name = "quarto",
    uniqueConstraints = @UniqueConstraint(name = "uk_quarto_andar_identificacao",
        columnNames = { "andar_id", "identificacao" }),
    check = {
        @CheckConstraint(name = "ck_quarto_capacidade", constraint = "capacidade > 0"),
        @CheckConstraint(name = "ck_quarto_valor_diaria", constraint = "valor_diaria_padrao >= 0"),
        @CheckConstraint(name = "ck_quarto_periodo_ativo",
            constraint = "desativado_em IS NULL OR desativado_em >= ativado_em")
    })
public class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "andar_id", nullable = false, foreignKey = @ForeignKey(name = "fk_quarto_andar"))
    private Andar andar;

    @NotBlank
    @Size(max = 10)
    @Column(nullable = false, length = 10)
    private String identificacao;

    @NotBlank
    @Size(max = 40)
    @Column(nullable = false, length = 40)
    private String categoria;

    @Positive
    @Column(nullable = false)
    private int capacidade;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "valor_diaria_padrao", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDiariaPadrao;

    @NotNull
    @Column(name = "ativado_em", nullable = false)
    private LocalDate ativadoEm;

    @Column(name = "desativado_em")
    private LocalDate desativadoEm;

    protected Quarto() {
    }

    public Quarto(Andar andar, String identificacao, String categoria, int capacidade,
            BigDecimal valorDiariaPadrao, LocalDate ativadoEm) {
        this.andar = andar;
        this.identificacao = identificacao;
        this.categoria = categoria;
        this.capacidade = capacidade;
        this.valorDiariaPadrao = valorDiariaPadrao;
        this.ativadoEm = ativadoEm;
    }

    /** Quarto ativo na data: ativado até ela e não desativado ainda (RN15). */
    public boolean isAtivoEm(LocalDate data) {
        return !data.isBefore(ativadoEm) && (desativadoEm == null || data.isBefore(desativadoEm));
    }

    public boolean comporta(int pessoas) {
        return pessoas > 0 && pessoas <= capacidade;
    }

    /** Conflitos com reservas a partir da data devem ser resolvidos antes, no serviço (RN17). */
    public void desativar(LocalDate aPartirDe) {
        if (aPartirDe.isBefore(ativadoEm)) {
            throw new RegraNegocioException("Desativação não pode ser anterior à ativação do quarto");
        }
        this.desativadoEm = aPartirDe;
    }

    /** Não altera reservas existentes, que guardam a diária acordada (RF12). */
    public void alterarValorDiariaPadrao(BigDecimal valor) {
        this.valorDiariaPadrao = valor;
    }

    public Long getId() {
        return id;
    }

    public Andar getAndar() {
        return andar;
    }

    public String getIdentificacao() {
        return identificacao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public BigDecimal getValorDiariaPadrao() {
        return valorDiariaPadrao;
    }

    public LocalDate getAtivadoEm() {
        return ativadoEm;
    }

    public LocalDate getDesativadoEm() {
        return desativadoEm;
    }
}
