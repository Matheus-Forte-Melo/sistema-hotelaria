package br.com.hotelaria.hotelaria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import br.com.hotelaria.hotelaria.dto.ReservaRequest;
import br.com.hotelaria.hotelaria.dto.ReservaResponse;
import br.com.hotelaria.hotelaria.exception.RecursoNaoEncontradoException;
import br.com.hotelaria.hotelaria.exception.RegraNegocioException;
import br.com.hotelaria.hotelaria.model.Andar;
import br.com.hotelaria.hotelaria.model.Bloco;
import br.com.hotelaria.hotelaria.model.Quarto;
import br.com.hotelaria.hotelaria.model.Reserva;
import br.com.hotelaria.hotelaria.model.SituacaoReserva;
import br.com.hotelaria.hotelaria.repository.AndarRepository;
import br.com.hotelaria.hotelaria.repository.BlocoRepository;
import br.com.hotelaria.hotelaria.repository.QuartoRepository;
import br.com.hotelaria.hotelaria.repository.ReservaRepository;

/** Busca por nome, consulta de detalhes e cancelamento (issue #4) com entidades reais, H2 e os repositórios JPA. */
@DataJpaTest
@Import(ReservaService.class)
class ReservaConsultaCancelamentoTest {

    private static final LocalDate DIA_10 = LocalDate.of(2026, 11, 10);
    private static final LocalDate DIA_12 = LocalDate.of(2026, 11, 12);
    private static final LocalDate DIA_14 = LocalDate.of(2026, 11, 14);
    private static final BigDecimal R180 = new BigDecimal("180.00");

    @Autowired
    private ReservaService service;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BlocoRepository blocos;

    @Autowired
    private AndarRepository andares;

    @Autowired
    private QuartoRepository quartos;

    @Autowired
    private ReservaRepository reservas;

    private Quarto q101;
    private Quarto q202;

    @BeforeEach
    void setUp() {
        Bloco bloco = blocos.save(new Bloco("A"));
        Andar primeiro = andares.save(new Andar(bloco, 1));
        Andar segundo = andares.save(new Andar(bloco, 2));
        q101 = quartos.save(new Quarto(primeiro, "101", "Standard", 2, new BigDecimal("150.00"), LocalDate.of(2026, 1, 1)));
        q202 = quartos.save(new Quarto(segundo, "202", "Família", 4, new BigDecimal("300.00"), LocalDate.of(2026, 1, 1)));
    }

    private ReservaResponse reservar(String nome, Quarto quarto, LocalDate entrada, LocalDate saida, int pessoas) {
        return service.criar(new ReservaRequest(nome, quarto.getId(), entrada, saida, pessoas, R180));
    }

    @Test
    void buscaPorNomeDevolveAsDuasReservasDistinguiveisPorQuartoEPeriodo() {
        reservar("Ana Souza", q101, DIA_10, DIA_12, 2);
        reservar("Ana Souza", q202, DIA_12, DIA_14, 3);
        reservar("Bruno Lima", q101, DIA_12, DIA_14, 1);
        em.clear();

        List<ReservaResponse> encontradas = service.buscarPorNome("Ana Souza");

        assertThat(encontradas)
            .extracting(ReservaResponse::quartoIdentificacao, ReservaResponse::dataEntrada, ReservaResponse::dataSaida)
            .containsExactly(tuple("101", DIA_10, DIA_12), tuple("202", DIA_12, DIA_14));
    }

    @Test
    void buscaIgnoraMaiusculasEspacosEAceitaParteDoNome() {
        reservar("Ana Souza", q101, DIA_10, DIA_12, 2);
        reservar("Mariana Costa", q202, DIA_10, DIA_12, 2);

        assertThat(service.buscarPorNome("  ana  ")).extracting(ReservaResponse::nomeHospede)
            .containsExactlyInAnyOrder("Ana Souza", "Mariana Costa");
        assertThat(service.buscarPorNome("SOUZA")).extracting(ReservaResponse::nomeHospede)
            .containsExactly("Ana Souza");
        assertThat(service.buscarPorNome("Carla")).isEmpty();
    }

    @Test
    void buscaSemNomeEhRecusada() {
        assertThatThrownBy(() -> service.buscarPorNome("   "))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("nome");
        assertThatThrownBy(() -> service.buscarPorNome(null))
            .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void consultaDetalhesComPessoasDiariaETotal() {
        Long id = reservar("Ana Souza", q101, DIA_10, DIA_12, 2).id();
        em.clear();

        ReservaResponse detalhes = service.consultar(id);

        assertThat(detalhes.nomeHospede()).isEqualTo("Ana Souza");
        assertThat(detalhes.quartoIdentificacao()).isEqualTo("101");
        assertThat(detalhes.andarNumero()).isEqualTo(1);
        assertThat(detalhes.quantidadePessoas()).isEqualTo(2);
        assertThat(detalhes.valorDiaria()).isEqualByComparingTo("180.00");
        assertThat(detalhes.quantidadeDiarias()).isEqualTo(2);
        assertThat(detalhes.valorTotal()).isEqualByComparingTo("360.00");
        assertThat(detalhes.situacao()).isEqualTo(SituacaoReserva.CONFIRMADA);
    }

    @Test
    void consultarReservaInexistenteInformaQueNaoFoiEncontrada() {
        assertThatThrownBy(() -> service.consultar(999_999L))
            .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void cancelarPreservaORegistroComSituacaoCanceladaEMantemAOutraVigente() {
        Long primeira = reservar("Ana Souza", q101, DIA_10, DIA_12, 2).id();
        Long segunda = reservar("Ana Souza", q202, DIA_10, DIA_12, 2).id();

        ReservaResponse cancelada = service.cancelar(primeira, "Hóspede desistiu");
        em.clear();

        assertThat(cancelada.situacao()).isEqualTo(SituacaoReserva.CANCELADA);
        assertThat(service.consultar(primeira).situacao()).isEqualTo(SituacaoReserva.CANCELADA);
        assertThat(service.consultar(segunda).situacao()).isEqualTo(SituacaoReserva.CONFIRMADA);
        assertThat(service.buscarPorNome("Ana")).extracting(ReservaResponse::id, ReservaResponse::situacao)
            .containsExactlyInAnyOrder(tuple(primeira, SituacaoReserva.CANCELADA), tuple(segunda, SituacaoReserva.CONFIRMADA));

        Reserva gravada = reservas.findById(primeira).orElseThrow();
        assertThat(gravada.getMotivoCancelamento()).isEqualTo("Hóspede desistiu");
        assertThat(gravada.getCanceladaEm()).isNotNull();
    }

    @Test
    void cancelarSemMotivoRegistraMotivoPadrao() {
        Long id = reservar("Ana Souza", q101, DIA_10, DIA_12, 2).id();

        service.cancelar(id, "  ");
        em.clear();

        assertThat(reservas.findById(id).orElseThrow().getMotivoCancelamento()).isEqualTo(ReservaService.MOTIVO_PADRAO_CANCELAMENTO);
    }

    @Test
    void cancelamentoLiberaOPeriodoParaNovaReserva() {
        Long id = reservar("Ana Souza", q101, DIA_10, DIA_12, 2).id();
        service.cancelar(id, null);

        ReservaResponse nova = reservar("Bruno Lima", q101, DIA_10, DIA_12, 1);

        assertThat(nova.situacao()).isEqualTo(SituacaoReserva.CONFIRMADA);
        assertThat(nova.quartoIdentificacao()).isEqualTo("101");
    }

    @Test
    void cancelarDuasVezesEhRecusadoEPreservaOPrimeiroCancelamento() {
        Long id = reservar("Ana Souza", q101, DIA_10, DIA_12, 2).id();
        service.cancelar(id, "Primeiro motivo");

        assertThatThrownBy(() -> service.cancelar(id, "Segundo motivo"))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("já está cancelada");
        em.clear();
        assertThat(reservas.findById(id).orElseThrow().getMotivoCancelamento()).isEqualTo("Primeiro motivo");
    }

    @Test
    void cancelarReservaInexistenteInformaQueNaoFoiEncontrada() {
        assertThatThrownBy(() -> service.cancelar(999_999L, null))
            .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
