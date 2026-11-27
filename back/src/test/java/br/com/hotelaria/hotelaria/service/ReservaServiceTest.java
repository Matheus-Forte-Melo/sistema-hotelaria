package br.com.hotelaria.hotelaria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import br.com.hotelaria.hotelaria.dto.ReservaRequest;
import br.com.hotelaria.hotelaria.dto.ReservaResponse;
import br.com.hotelaria.hotelaria.exception.ConflitoReservaException;
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

/** Regras da issue #3 com entidades reais, H2 e os repositórios JPA. */
@DataJpaTest
@Import(ReservaService.class)
class ReservaServiceTest {

    private static final LocalDate DIA_10 = LocalDate.of(2026, 11, 10);
    private static final LocalDate DIA_11 = LocalDate.of(2026, 11, 11);
    private static final LocalDate DIA_12 = LocalDate.of(2026, 11, 12);
    private static final LocalDate DIA_13 = LocalDate.of(2026, 11, 13);
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
    private Quarto q102;

    @BeforeEach
    void setUp() {
        Andar andar = andares.save(new Andar(blocos.save(new Bloco("A")), 1));
        q101 = quartos.save(new Quarto(andar, "101", "Standard", 2, new BigDecimal("150.00"), LocalDate.of(2026, 1, 1)));
        q102 = quartos.save(new Quarto(andar, "102", "Família", 4, new BigDecimal("300.00"), LocalDate.of(2026, 1, 1)));
    }

    private ReservaRequest pedido(String nome, Quarto quarto, LocalDate entrada, LocalDate saida, int pessoas,
            BigDecimal diaria) {
        return new ReservaRequest(nome, quarto.getId(), entrada, saida, pessoas, diaria);
    }

    private ReservaResponse reservaAna() {
        return service.criar(pedido("Ana", q101, DIA_10, DIA_12, 2, R180));
    }

    @Test
    void criaReservaDeDuasNoitesComNomeDeReferencia() {
        ReservaResponse criada = reservaAna();

        assertThat(criada.id()).isNotNull();
        assertThat(criada.nomeHospede()).isEqualTo("Ana");
        assertThat(criada.quartoId()).isEqualTo(q101.getId());
        assertThat(criada.quartoIdentificacao()).isEqualTo("101");
        assertThat(criada.andarNumero()).isEqualTo(1);
        assertThat(criada.quantidadeDiarias()).isEqualTo(2);
        assertThat(criada.valorTotal()).isEqualByComparingTo("360.00");
        assertThat(criada.situacao()).isEqualTo(SituacaoReserva.CONFIRMADA);

        em.clear();
        Reserva gravada = reservas.findById(criada.id()).orElseThrow();
        assertThat(gravada.getNomeHospede()).isEqualTo("Ana");
        assertThat(gravada.getCriadaPor()).isNull();
    }

    @Test
    void preservaDiariaCombinadaQuandoPadraoDoQuartoMuda() {
        ReservaResponse criada = reservaAna();
        q101.alterarValorDiariaPadrao(new BigDecimal("200.00"));
        quartos.saveAndFlush(q101);
        em.clear();

        Reserva gravada = reservas.findById(criada.id()).orElseThrow();
        assertThat(gravada.getValorDiaria()).isEqualByComparingTo("180.00");
        assertThat(gravada.valorPrevisto()).isEqualByComparingTo("360.00");
    }

    @Test
    void usaDiariaPadraoDoQuartoQuandoNaoInformada() {
        ReservaResponse criada = service.criar(pedido("Ana", q101, DIA_10, DIA_12, 1, null));

        assertThat(criada.valorDiaria()).isEqualByComparingTo("150.00");
        assertThat(criada.valorTotal()).isEqualByComparingTo("300.00");
    }

    @Test
    void recusaPeriodoInvalidoECapacidade() {
        assertThatThrownBy(() -> service.criar(pedido("Ana", q101, DIA_12, DIA_12, 1, R180)))
            .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> service.criar(pedido("Ana", q101, DIA_12, DIA_10, 1, R180)))
            .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> service.criar(pedido("Ana", q101, DIA_10, DIA_12, 0, R180)))
            .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> service.criar(pedido("Ana", q101, DIA_10, DIA_12, 3, R180)))
            .isInstanceOf(RegraNegocioException.class);
        assertThat(reservas.count()).isZero();
    }

    @Test
    void recusaSobreposicaoEPermiteEntradaNoDiaDaSaida() {
        reservaAna();

        assertThatThrownBy(() -> service.criar(pedido("Bruno", q101, DIA_11, DIA_13, 1, R180)))
            .isInstanceOf(ConflitoReservaException.class);

        ReservaResponse bruno = service.criar(pedido("Bruno", q101, DIA_12, DIA_14, 1, R180));
        assertThat(bruno.dataEntrada()).isEqualTo(DIA_12);
    }

    @Test
    void reservaCanceladaNaoBloqueiaOPeriodo() {
        ReservaResponse ana = reservaAna();
        Reserva gravada = reservas.findById(ana.id()).orElseThrow();
        gravada.cancelar(Instant.now(), "Desistência");
        reservas.saveAndFlush(gravada);

        ReservaResponse bruno = service.criar(pedido("Bruno", q101, DIA_11, DIA_13, 1, R180));
        assertThat(bruno.id()).isNotNull();
    }

    @Test
    void recusaQuartoInexistente() {
        assertThatThrownBy(() -> service.criar(new ReservaRequest("Ana", 999L, DIA_10, DIA_12, 1, R180)))
            .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void edicaoDesconsideraAPropriaReservaEAtualizaTotal() {
        ReservaResponse ana = reservaAna();
        q101.alterarValorDiariaPadrao(new BigDecimal("200.00"));
        quartos.saveAndFlush(q101);

        ReservaResponse editada = service.editar(ana.id(), pedido("Ana", q101, DIA_10, DIA_13, 2, R180));

        assertThat(editada.dataSaida()).isEqualTo(DIA_13);
        assertThat(editada.valorTotal()).isEqualByComparingTo("540.00");
        em.clear();
        assertThat(reservas.findById(ana.id()).orElseThrow().getDataSaidaPrevista()).isEqualTo(DIA_13);
    }

    @Test
    void edicaoSemDiariaPreservaADiariaCombinada() {
        ReservaResponse ana = reservaAna();
        q101.alterarValorDiariaPadrao(new BigDecimal("200.00"));
        quartos.saveAndFlush(q101);

        ReservaResponse editada = service.editar(ana.id(), pedido("Ana", q101, DIA_10, DIA_13, 2, null));

        assertThat(editada.valorDiaria()).isEqualByComparingTo("180.00");
        assertThat(editada.valorTotal()).isEqualByComparingTo("540.00");
    }

    @Test
    void edicaoSemDiariaAoTrocarDeQuartoTambemPreservaADiariaCombinada() {
        ReservaResponse ana = reservaAna();

        ReservaResponse movida = service.editar(ana.id(), pedido("Ana", q102, DIA_10, DIA_12, 2, null));

        assertThat(movida.quartoIdentificacao()).isEqualTo("102");
        assertThat(movida.valorDiaria()).isEqualByComparingTo("180.00");
    }

    @Test
    void recusaEdicaoDeReservaCanceladaComMensagemParaATela() {
        ReservaResponse ana = reservaAna();
        Reserva gravada = reservas.findById(ana.id()).orElseThrow();
        gravada.cancelar(Instant.now(), "Desistência");
        reservas.saveAndFlush(gravada);

        assertThatThrownBy(() -> service.editar(ana.id(), pedido("Ana", q101, DIA_10, DIA_13, 2, R180)))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("Reservas canceladas não podem ser editadas");
    }

    @Test
    void extensaoComConflitoMantemReservaAnterior() {
        ReservaResponse ana = reservaAna();
        service.criar(pedido("Bruno", q101, DIA_12, DIA_14, 1, R180));

        assertThatThrownBy(() -> service.editar(ana.id(), pedido("Ana", q101, DIA_10, DIA_13, 2, R180)))
            .isInstanceOf(ConflitoReservaException.class);

        em.clear();
        Reserva gravada = reservas.findById(ana.id()).orElseThrow();
        assertThat(gravada.getDataSaidaPrevista()).isEqualTo(DIA_12);
        assertThat(gravada.valorPrevisto()).isEqualByComparingTo("360.00");
    }

    @Test
    void moverParaQuartoSemCapacidadeMantemReservaAnterior() {
        ReservaResponse grupo = service.criar(pedido("Carla", q102, DIA_10, DIA_12, 3, R180));

        assertThatThrownBy(() -> service.editar(grupo.id(), pedido("Carla", q101, DIA_10, DIA_12, 3, R180)))
            .isInstanceOf(RegraNegocioException.class);

        em.clear();
        Reserva gravada = reservas.findById(grupo.id()).orElseThrow();
        assertThat(gravada.getQuarto().getId()).isEqualTo(q102.getId());
        assertThat(gravada.getQuantidadePessoas()).isEqualTo(3);
    }

    @Test
    void moverParaOutroQuartoVerificaConflitoNoQuartoDeDestino() {
        ReservaResponse ana = reservaAna();
        service.criar(pedido("Bruno", q102, DIA_11, DIA_12, 1, R180));

        assertThatThrownBy(() -> service.editar(ana.id(), pedido("Ana", q102, DIA_10, DIA_12, 2, R180)))
            .isInstanceOf(ConflitoReservaException.class);

        ReservaResponse movida = service.editar(ana.id(), pedido("Ana", q102, DIA_12, DIA_14, 2, R180));
        assertThat(movida.quartoIdentificacao()).isEqualTo("102");
    }

    @Test
    void recusaEdicaoDeReservaInexistente() {
        assertThatThrownBy(() -> service.editar(999L, pedido("Ana", q101, DIA_10, DIA_12, 1, R180)))
            .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
