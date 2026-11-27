package br.com.hotelaria.hotelaria.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.hotelaria.hotelaria.exception.RegraNegocioException;

class ReservaTest {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate ENTRADA = LocalDate.of(2026, 11, 10);
    private static final LocalDate SAIDA = LocalDate.of(2026, 11, 12);

    private Quarto quarto;
    private Funcionario recepcionista;
    private Hospede adulto;
    private Hospede crianca;
    private Hospede adolescente;

    @BeforeEach
    void setUp() {
        Andar andar = new Andar(new Bloco("A"), 1);
        quarto = new Quarto(andar, "101", "Standard", 3, new BigDecimal("200.00"), LocalDate.of(2026, 1, 1));
        recepcionista = new Funcionario("Ana", "ana@hotel.com", "hash", Perfil.RECEPCIONISTA);
        adulto = new Hospede("Carlos", LocalDate.of(1990, 5, 1));
        crianca = new Hospede("Bia", LocalDate.of(2020, 3, 1));
        adolescente = new Hospede("Leo", LocalDate.of(2012, 1, 1));
    }

    private Reserva reserva(int pessoas) {
        return new Reserva(quarto, "Ana", ENTRADA, SAIDA, pessoas, new BigDecimal("180.00"), recepcionista);
    }

    @Test
    void calculaValorPrevistoComDiariaAcordada() {
        Reserva reserva = reserva(2);
        quarto.alterarValorDiariaPadrao(new BigDecimal("999.00"));

        assertThat(reserva.quantidadeDiarias()).isEqualTo(2);
        assertThat(reserva.valorPrevisto()).isEqualByComparingTo("360.00");
    }

    @Test
    void usaIntervaloSemiabertoNaSobreposicao() {
        Reserva reserva = reserva(1);

        assertThat(reserva.sobrepoe(SAIDA, SAIDA.plusDays(2))).isFalse();
        assertThat(reserva.sobrepoe(ENTRADA.minusDays(2), ENTRADA)).isFalse();
        assertThat(reserva.sobrepoe(ENTRADA.plusDays(1), SAIDA.plusDays(1))).isTrue();
        assertThat(reserva.incluiPernoite(SAIDA)).isFalse();
    }

    @Test
    void rejeitaPeriodoInvalidoECapacidadeExcedida() {
        assertThatThrownBy(() -> new Reserva(quarto, "Ana", ENTRADA, ENTRADA, 1, BigDecimal.TEN, recepcionista))
            .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> reserva(4)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void rejeitaListaCompletaSomenteComMenores() {
        Reserva reserva = reserva(2);
        reserva.adicionarHospede(crianca);

        assertThatThrownBy(() -> reserva.adicionarHospede(adolescente))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage(Reserva.AVISO_ADULTO);
        assertThat(reserva.getHospedes()).containsExactly(crianca);
    }

    @Test
    void listaIncompletaSemAdultoFicaPendenteEBloqueiaCheckIn() {
        Reserva reserva = reserva(2);
        reserva.adicionarHospede(crianca);

        assertThat(reserva.isValidacaoAdultoPendente()).isTrue();
        assertThat(reserva.quantidadeNaoIdentificada()).isEqualTo(1);
        assertThatThrownBy(() -> reserva.fazerCheckIn(Instant.now(), ZONA))
            .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void impedeHospedeDuplicadoNaLista() {
        Reserva reserva = reserva(2);
        reserva.adicionarHospede(adulto);

        assertThatThrownBy(() -> reserva.adicionarHospede(adulto)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void percorreCicloDeVidaCompleto() {
        Reserva reserva = reserva(2);
        reserva.adicionarHospede(adulto);
        reserva.adicionarHospede(crianca);
        Instant entrada = ENTRADA.atTime(14, 0).atZone(ZONA).toInstant();
        Instant saida = SAIDA.atTime(11, 0).atZone(ZONA).toInstant();

        reserva.fazerCheckIn(entrada, ZONA);
        assertThat(reserva.getSituacao()).isEqualTo(SituacaoReserva.EM_ANDAMENTO);
        assertThatThrownBy(() -> reserva.cancelar(saida, "desistência")).isInstanceOf(RegraNegocioException.class);

        reserva.fazerCheckOut(saida);
        assertThat(reserva.getSituacao()).isEqualTo(SituacaoReserva.FINALIZADA);
        assertThat(reserva.getCheckOutEm()).isEqualTo(saida);
    }

    @Test
    void correcaoDeHistoricoExigeAdministradorEPreservaValoresAnteriores() {
        Reserva reserva = reserva(1);
        reserva.adicionarHospede(adulto);
        Instant entrada = ENTRADA.atTime(14, 0).atZone(ZONA).toInstant();
        Instant saida = SAIDA.atTime(11, 0).atZone(ZONA).toInstant();
        reserva.fazerCheckIn(entrada, ZONA);
        reserva.fazerCheckOut(saida);
        Funcionario admin = new Funcionario("Root", "root@hotel.com", "hash", Perfil.ADMINISTRADOR);
        Instant novaSaida = saida.plusSeconds(3600);

        assertThatThrownBy(() -> reserva.corrigirHorariosEfetivos(entrada, novaSaida, recepcionista, "erro"))
            .isInstanceOf(RegraNegocioException.class);

        CorrecaoHistorico correcao = reserva.corrigirHorariosEfetivos(entrada, novaSaida, admin, "Saída registrada tarde");

        assertThat(correcao.getCheckOutAnterior()).isEqualTo(saida);
        assertThat(correcao.getCheckOutNovo()).isEqualTo(novaSaida);
        assertThat(reserva.getCheckOutEm()).isEqualTo(novaSaida);
        assertThat(reserva.getCorrecoes()).containsExactly(correcao);
    }

    @Test
    void criaReservaSomenteComNomeDeReferenciaESemFuncionario() {
        Reserva reserva = new Reserva(quarto, "  Ana Souza ", ENTRADA, SAIDA, 2, new BigDecimal("180.00"));

        assertThat(reserva.getNomeHospede()).isEqualTo("Ana Souza");
        assertThat(reserva.getCriadaPor()).isNull();
        assertThat(reserva.getSituacao()).isEqualTo(SituacaoReserva.CONFIRMADA);
        assertThat(reserva.valorPrevisto()).isEqualByComparingTo("360.00");
    }

    @Test
    void rejeitaNomeVazioQuantidadeNaoPositivaEDiariaNegativa() {
        assertThatThrownBy(() -> new Reserva(quarto, " ", ENTRADA, SAIDA, 1, BigDecimal.TEN))
            .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> new Reserva(quarto, "Ana", ENTRADA, SAIDA, 0, BigDecimal.TEN))
            .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> new Reserva(quarto, "Ana", ENTRADA, SAIDA, 1, new BigDecimal("-1")))
            .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void alteracaoValidaAtualizaQuartoPeriodoPessoasEDiaria() {
        Reserva reserva = reserva(2);
        Quarto maior = new Quarto(quarto.getAndar(), "102", "Família", 4, new BigDecimal("300.00"),
            LocalDate.of(2026, 1, 1));

        reserva.alterar(maior, "Ana Lima", ENTRADA, SAIDA.plusDays(1), 4, new BigDecimal("180.00"));

        assertThat(reserva.getQuarto()).isSameAs(maior);
        assertThat(reserva.getNomeHospede()).isEqualTo("Ana Lima");
        assertThat(reserva.getDataSaidaPrevista()).isEqualTo(SAIDA.plusDays(1));
        assertThat(reserva.getQuantidadePessoas()).isEqualTo(4);
        assertThat(reserva.valorPrevisto()).isEqualByComparingTo("540.00");
    }

    @Test
    void alteracaoInvalidaPreservaTodosOsDadosAnteriores() {
        Reserva reserva = reserva(2);
        Quarto pequeno = new Quarto(quarto.getAndar(), "103", "Single", 1, new BigDecimal("100.00"),
            LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> reserva.alterar(pequeno, "Outro", ENTRADA, SAIDA.plusDays(3), 2, BigDecimal.ONE))
            .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> reserva.alterar(quarto, "Outro", SAIDA, SAIDA, 2, BigDecimal.ONE))
            .isInstanceOf(RegraNegocioException.class);

        assertThat(reserva.getQuarto()).isSameAs(quarto);
        assertThat(reserva.getNomeHospede()).isEqualTo("Ana");
        assertThat(reserva.getDataSaidaPrevista()).isEqualTo(SAIDA);
        assertThat(reserva.getValorDiaria()).isEqualByComparingTo("180.00");
    }

    @Test
    void naoAlteraReservaCancelada() {
        Reserva reserva = reserva(2);
        reserva.cancelar(Instant.now(), "Desistência");

        assertThatThrownBy(() -> reserva.alterar(quarto, "Ana", ENTRADA, SAIDA, 2, BigDecimal.TEN))
            .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void classificaFaixaEtariaNaDataDeReferencia() {
        Hospede aniversariante = new Hospede("Duda", LocalDate.of(2008, 11, 10));

        assertThat(aniversariante.faixaEtariaEm(LocalDate.of(2026, 11, 9))).isEqualTo(FaixaEtaria.ADOLESCENTE);
        assertThat(aniversariante.faixaEtariaEm(LocalDate.of(2026, 11, 10))).isEqualTo(FaixaEtaria.ADULTO);
    }
}
