package br.com.hotelaria.hotelaria.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import br.com.hotelaria.hotelaria.exception.RegraNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Reserva de um quarto para uma lista de hóspedes (RN08). Também representa a hospedagem:
 * a partir do check-in a situação passa a EM_ANDAMENTO e, no check-out, a FINALIZADA (RN11).
 * O período previsto usa o intervalo de pernoite [entrada, saída) (RN02); os horários efetivos
 * de check-in e check-out ficam em campos próprios.
 */
@Entity
@Table(name = "reserva",
    indexes = @Index(name = "ix_reserva_quarto_periodo",
        columnList = "quarto_id, data_entrada_prevista, data_saida_prevista"),
    check = {
        @CheckConstraint(name = "ck_reserva_periodo", constraint = "data_saida_prevista > data_entrada_prevista"),
        @CheckConstraint(name = "ck_reserva_quantidade", constraint = "quantidade_pessoas > 0"),
        @CheckConstraint(name = "ck_reserva_valor_diaria", constraint = "valor_diaria >= 0"),
        @CheckConstraint(name = "ck_reserva_check_out",
            constraint = "check_out_em IS NULL OR (check_in_em IS NOT NULL AND check_out_em > check_in_em)"),
        @CheckConstraint(name = "ck_reserva_cancelamento",
            constraint = "(situacao = 'CANCELADA' AND cancelada_em IS NOT NULL AND motivo_cancelamento IS NOT NULL)"
                + " OR (situacao <> 'CANCELADA' AND cancelada_em IS NULL)")
    })
public class Reserva {

    public static final String AVISO_ADULTO = "Cada quarto deve ter pelo menos um hóspede com 18 anos ou mais. "
        + "Não é permitida hospedagem apenas de crianças e/ou adolescentes.";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "quarto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reserva_quarto"))
    private Quarto quarto;

    /** Nome de referência usado pela recepção para localizar a reserva (escopo revisado, RF08). */
    @NotBlank
    @Size(max = 120)
    @Column(name = "nome_hospede", nullable = false, length = 120)
    private String nomeHospede;

    @ManyToMany
    @JoinTable(name = "reserva_hospede",
        joinColumns = @JoinColumn(name = "reserva_id", foreignKey = @ForeignKey(name = "fk_reserva_hospede_reserva")),
        inverseJoinColumns = @JoinColumn(name = "hospede_id",
            foreignKey = @ForeignKey(name = "fk_reserva_hospede_hospede")))
    private Set<Hospede> hospedes = new LinkedHashSet<>();

    @Positive
    @Column(name = "quantidade_pessoas", nullable = false)
    private int quantidadePessoas;

    @NotNull
    @Column(name = "data_entrada_prevista", nullable = false)
    private LocalDate dataEntradaPrevista;

    @NotNull
    @Column(name = "data_saida_prevista", nullable = false)
    private LocalDate dataSaidaPrevista;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "valor_diaria", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDiaria;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoReserva situacao;

    @Column(name = "check_in_em")
    private Instant checkInEm;

    @Column(name = "check_out_em")
    private Instant checkOutEm;

    @Column(name = "cancelada_em")
    private Instant canceladaEm;

    @Size(max = 255)
    @Column(name = "motivo_cancelamento", length = 255)
    private String motivoCancelamento;

    /** Opcional: a gestão de acesso ficou fora do escopo revisado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criada_por_id", foreignKey = @ForeignKey(name = "fk_reserva_funcionario"))
    private Funcionario criadaPor;

    @CreationTimestamp
    @Column(name = "criada_em", nullable = false, updatable = false)
    private Instant criadaEm;

    @OneToMany(mappedBy = "reserva", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @OrderBy("corrigidaEm")
    private List<CorrecaoHistorico> correcoes = new ArrayList<>();

    @Version
    private Long versao;

    protected Reserva() {
    }

    public Reserva(Quarto quarto, String nomeHospede, LocalDate dataEntradaPrevista, LocalDate dataSaidaPrevista,
            int quantidadePessoas, BigDecimal valorDiaria) {
        this(quarto, nomeHospede, dataEntradaPrevista, dataSaidaPrevista, quantidadePessoas, valorDiaria, null);
    }

    public Reserva(Quarto quarto, String nomeHospede, LocalDate dataEntradaPrevista, LocalDate dataSaidaPrevista,
            int quantidadePessoas, BigDecimal valorDiaria, Funcionario criadaPor) {
        String nome = validarDados(quarto, nomeHospede, dataEntradaPrevista, dataSaidaPrevista,
            quantidadePessoas, valorDiaria);
        this.quarto = quarto;
        this.nomeHospede = nome;
        this.dataEntradaPrevista = dataEntradaPrevista;
        this.dataSaidaPrevista = dataSaidaPrevista;
        this.quantidadePessoas = quantidadePessoas;
        this.valorDiaria = valorDiaria;
        this.criadaPor = criadaPor;
        this.situacao = SituacaoReserva.CONFIRMADA;
    }

    /**
     * Altera os dados combinados da reserva (RF08, regra 3). Valida tudo antes de mudar qualquer campo,
     * de modo que uma tentativa inválida preserva a reserva anterior. O conflito com outras reservas
     * depende do banco e é verificado no serviço antes desta chamada.
     */
    public void alterar(Quarto novoQuarto, String novoNome, LocalDate novaEntrada, LocalDate novaSaida,
            int novaQuantidade, BigDecimal novaDiaria) {
        exigirSituacao(SituacaoReserva.CONFIRMADA);
        String nome = validarDados(novoQuarto, novoNome, novaEntrada, novaSaida, novaQuantidade, novaDiaria);
        if (novaQuantidade < hospedes.size()) {
            throw new RegraNegocioException("Quantidade de pessoas menor que a de hóspedes já identificados");
        }
        this.quarto = novoQuarto;
        this.nomeHospede = nome;
        this.dataEntradaPrevista = novaEntrada;
        this.dataSaidaPrevista = novaSaida;
        this.quantidadePessoas = novaQuantidade;
        this.valorDiaria = novaDiaria;
    }

    private static String validarDados(Quarto quarto, String nomeHospede, LocalDate entrada, LocalDate saida,
            int quantidadePessoas, BigDecimal valorDiaria) {
        if (quarto == null) {
            throw new RegraNegocioException("Quarto é obrigatório");
        }
        if (nomeHospede == null || nomeHospede.isBlank()) {
            throw new RegraNegocioException("Nome do hóspede é obrigatório");
        }
        String nome = nomeHospede.strip();
        if (nome.length() > 120) {
            throw new RegraNegocioException("Nome do hóspede deve ter no máximo 120 caracteres");
        }
        validarPeriodo(entrada, saida);
        if (!quarto.comporta(quantidadePessoas)) {
            throw new RegraNegocioException("Quantidade de pessoas deve ser de 1 a " + quarto.getCapacidade()
                + ", a capacidade do quarto " + quarto.getIdentificacao());
        }
        if (valorDiaria == null || valorDiaria.signum() < 0) {
            throw new RegraNegocioException("Valor da diária deve ser zero ou positivo");
        }
        return nome;
    }

    public long quantidadeDiarias() {
        return ChronoUnit.DAYS.between(dataEntradaPrevista, dataSaidaPrevista);
    }

    /** Diárias multiplicadas pelo valor acordado (RF12). Não é cobrança. */
    public BigDecimal valorPrevisto() {
        return valorDiaria.multiply(BigDecimal.valueOf(quantidadeDiarias()));
    }

    /** Sobreposição de intervalos semiabertos [inicio, fim) (RN02, RN09). */
    public boolean sobrepoe(LocalDate inicio, LocalDate fim) {
        return dataEntradaPrevista.isBefore(fim) && dataSaidaPrevista.isAfter(inicio);
    }

    public boolean incluiPernoite(LocalDate data) {
        return !data.isBefore(dataEntradaPrevista) && data.isBefore(dataSaidaPrevista);
    }

    public boolean isListaCompleta() {
        return hospedes.size() == quantidadePessoas;
    }

    public int quantidadeNaoIdentificada() {
        return quantidadePessoas - hospedes.size();
    }

    public boolean possuiAdultoIdentificadoEm(LocalDate data) {
        return hospedes.stream().anyMatch(h -> h.isAdultoEm(data));
    }

    /** Lista incompleta sem adulto identificado: validação pendente e check-in bloqueado (RN14). */
    public boolean isValidacaoAdultoPendente() {
        return !possuiAdultoIdentificadoEm(dataEntradaPrevista);
    }

    public void adicionarHospede(Hospede hospede) {
        exigirSituacao(SituacaoReserva.CONFIRMADA);
        if (hospedes.contains(hospede)) {
            throw new RegraNegocioException("Hóspede já está na lista desta reserva");
        }
        if (hospedes.size() >= quantidadePessoas) {
            throw new RegraNegocioException("Lista de hóspedes excede a quantidade de pessoas da reserva");
        }
        hospedes.add(hospede);
        if (isListaCompleta() && !possuiAdultoIdentificadoEm(dataEntradaPrevista)) {
            hospedes.remove(hospede);
            throw new RegraNegocioException(AVISO_ADULTO);
        }
    }

    public void removerHospede(Hospede hospede) {
        exigirSituacao(SituacaoReserva.CONFIRMADA);
        hospedes.remove(hospede);
    }

    /** Disponibilidade no novo período deve ser revalidada no serviço (RF08). */
    public void alterarPeriodo(LocalDate novaEntrada, LocalDate novaSaida) {
        exigirSituacao(SituacaoReserva.CONFIRMADA);
        validarPeriodo(novaEntrada, novaSaida);
        if (isListaCompleta() && !possuiAdultoIdentificadoEm(novaEntrada)) {
            throw new RegraNegocioException(AVISO_ADULTO);
        }
        this.dataEntradaPrevista = novaEntrada;
        this.dataSaidaPrevista = novaSaida;
    }

    public void alterarQuantidadePessoas(int quantidade) {
        exigirSituacao(SituacaoReserva.CONFIRMADA);
        if (!quarto.comporta(quantidade) || quantidade < hospedes.size()) {
            throw new RegraNegocioException("Quantidade inválida para a capacidade ou para os hóspedes já identificados");
        }
        if (quantidade == hospedes.size() && !possuiAdultoIdentificadoEm(dataEntradaPrevista)) {
            throw new RegraNegocioException(AVISO_ADULTO);
        }
        this.quantidadePessoas = quantidade;
    }

    /**
     * Registra a entrada efetiva (RF10). A verificação de quarto ainda ocupado e de conflitos
     * em entradas antecipadas depende de outras reservas e é feita no serviço.
     */
    public void fazerCheckIn(Instant momento, ZoneId zonaHotel) {
        exigirSituacao(SituacaoReserva.CONFIRMADA);
        if (!isListaCompleta()) {
            throw new RegraNegocioException("Todos os ocupantes devem estar identificados para o check-in");
        }
        if (!possuiAdultoIdentificadoEm(LocalDate.ofInstant(momento, zonaHotel))) {
            throw new RegraNegocioException(AVISO_ADULTO);
        }
        this.checkInEm = momento;
        this.situacao = SituacaoReserva.EM_ANDAMENTO;
    }

    public void fazerCheckOut(Instant momento) {
        exigirSituacao(SituacaoReserva.EM_ANDAMENTO);
        if (!momento.isAfter(checkInEm)) {
            throw new RegraNegocioException("Check-out deve ser posterior ao check-in");
        }
        this.checkOutEm = momento;
        this.situacao = SituacaoReserva.FINALIZADA;
    }

    public void cancelar(Instant momento, String motivo) {
        exigirSituacao(SituacaoReserva.CONFIRMADA);
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("Motivo do cancelamento é obrigatório");
        }
        this.canceladaEm = momento;
        this.motivoCancelamento = motivo;
        this.situacao = SituacaoReserva.CANCELADA;
    }

    /**
     * Corrige horários efetivos de uma estadia encerrada, preservando os valores anteriores (RF20).
     * Conflitos com outras reservas devem ser verificados no serviço antes de salvar.
     */
    public CorrecaoHistorico corrigirHorariosEfetivos(Instant novoCheckIn, Instant novoCheckOut,
            Funcionario autor, String justificativa) {
        exigirSituacao(SituacaoReserva.FINALIZADA);
        if (!autor.isAdministrador()) {
            throw new RegraNegocioException("Somente administradores podem corrigir o histórico");
        }
        if (justificativa == null || justificativa.isBlank()) {
            throw new RegraNegocioException("Justificativa é obrigatória");
        }
        if (!novoCheckOut.isAfter(novoCheckIn)) {
            throw new RegraNegocioException("Check-out deve ser posterior ao check-in");
        }
        CorrecaoHistorico correcao = new CorrecaoHistorico(this, autor, justificativa,
            checkInEm, checkOutEm, novoCheckIn, novoCheckOut);
        correcoes.add(correcao);
        this.checkInEm = novoCheckIn;
        this.checkOutEm = novoCheckOut;
        return correcao;
    }

    private void exigirSituacao(SituacaoReserva esperada) {
        if (situacao != esperada) {
            throw new RegraNegocioException("Operação exige reserva " + esperada + ", mas está " + situacao);
        }
    }

    private static void validarPeriodo(LocalDate entrada, LocalDate saida) {
        if (entrada == null || saida == null || !saida.isAfter(entrada)) {
            throw new RegraNegocioException("Data de saída deve ser posterior à data de entrada");
        }
    }

    public Long getId() {
        return id;
    }

    public Quarto getQuarto() {
        return quarto;
    }

    public String getNomeHospede() {
        return nomeHospede;
    }

    public Set<Hospede> getHospedes() {
        return Collections.unmodifiableSet(hospedes);
    }

    public int getQuantidadePessoas() {
        return quantidadePessoas;
    }

    public LocalDate getDataEntradaPrevista() {
        return dataEntradaPrevista;
    }

    public LocalDate getDataSaidaPrevista() {
        return dataSaidaPrevista;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public SituacaoReserva getSituacao() {
        return situacao;
    }

    public Instant getCheckInEm() {
        return checkInEm;
    }

    public Instant getCheckOutEm() {
        return checkOutEm;
    }

    public Instant getCanceladaEm() {
        return canceladaEm;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public Funcionario getCriadaPor() {
        return criadaPor;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public List<CorrecaoHistorico> getCorrecoes() {
        return Collections.unmodifiableList(correcoes);
    }

    public Long getVersao() {
        return versao;
    }
}
