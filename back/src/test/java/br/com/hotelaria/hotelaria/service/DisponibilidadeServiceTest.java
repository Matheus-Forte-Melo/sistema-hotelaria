package br.com.hotelaria.hotelaria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import br.com.hotelaria.hotelaria.dto.QuartoResponse;
import br.com.hotelaria.hotelaria.exception.RegraNegocioException;
import br.com.hotelaria.hotelaria.model.Andar;
import br.com.hotelaria.hotelaria.model.Bloco;
import br.com.hotelaria.hotelaria.model.Quarto;
import br.com.hotelaria.hotelaria.model.Reserva;
import br.com.hotelaria.hotelaria.repository.AndarRepository;
import br.com.hotelaria.hotelaria.repository.BlocoRepository;
import br.com.hotelaria.hotelaria.repository.QuartoRepository;
import br.com.hotelaria.hotelaria.repository.ReservaRepository;

/** Busca de disponibilidade da issue #2 (RF07, regras 1 a 4) com H2 e os repositórios JPA reais. */
@DataJpaTest
@Import(DisponibilidadeService.class)
class DisponibilidadeServiceTest {

    private static final LocalDate DIA_10 = LocalDate.of(2026, 11, 10);
    private static final LocalDate DIA_11 = LocalDate.of(2026, 11, 11);
    private static final LocalDate DIA_12 = LocalDate.of(2026, 11, 12);
    private static final LocalDate DIA_13 = LocalDate.of(2026, 11, 13);
    private static final LocalDate DIA_14 = LocalDate.of(2026, 11, 14);

    @Autowired
    private DisponibilidadeService service;

    @Autowired
    private BlocoRepository blocos;

    @Autowired
    private AndarRepository andares;

    @Autowired
    private QuartoRepository quartos;

    @Autowired
    private ReservaRepository reservas;

    private Andar andar;
    private Quarto q101;
    private Quarto q102;

    @BeforeEach
    void setUp() {
        andar = andares.save(new Andar(blocos.save(new Bloco("A")), 1));
        q101 = quartos.save(new Quarto(andar, "101", "Padrão", 2, new BigDecimal("150.00"), LocalDate.of(2026, 1, 1)));
        q102 = quartos.save(new Quarto(andar, "102", "Padrão", 4, new BigDecimal("300.00"), LocalDate.of(2026, 1, 1)));
    }

    private static List<String> identificacoes(List<QuartoResponse> encontrados) {
        return encontrados.stream().map(QuartoResponse::identificacao).toList();
    }

    @Test
    void tresPessoasEncontramApenasOQuartoDeQuatroVagasQuandoAmbosEstaoLivres() {
        assertThat(identificacoes(service.buscar(DIA_11, DIA_13, 3))).containsExactly("102");
    }

    @Test
    void quartosQueComportamOGrupoSaoOrdenadosEMostramAndarCapacidadeEDiaria() {
        List<QuartoResponse> encontrados = service.buscar(DIA_11, DIA_13, 2);

        assertThat(identificacoes(encontrados)).containsExactly("101", "102");
        QuartoResponse q = encontrados.get(1);
        assertThat(q.id()).isEqualTo(q102.getId());
        assertThat(q.andarId()).isEqualTo(andar.getId());
        assertThat(q.andarNumero()).isEqualTo(1);
        assertThat(q.capacidade()).isEqualTo(4);
        assertThat(q.valorDiariaPadrao()).isEqualByComparingTo("300.00");
    }

    @Test
    void reservaVigenteDe10a12RetiraOQuartoDaBuscaDe11a13() {
        reservas.save(new Reserva(q102, "Ana", DIA_10, DIA_12, 2, new BigDecimal("300.00")));

        assertThat(service.buscar(DIA_11, DIA_13, 3)).isEmpty();
    }

    @Test
    void quartoVoltaParaEntradaNoDiaDaSaidaDaReservaAnterior() {
        reservas.save(new Reserva(q102, "Ana", DIA_10, DIA_12, 2, new BigDecimal("300.00")));

        assertThat(identificacoes(service.buscar(DIA_12, DIA_14, 3))).containsExactly("102");
    }

    @Test
    void reservaQueComecaNaNoiteDeSaidaDoPedidoNaoBloqueia() {
        reservas.save(new Reserva(q102, "Bruno", DIA_13, DIA_14, 2, new BigDecimal("300.00")));

        assertThat(identificacoes(service.buscar(DIA_11, DIA_13, 3))).containsExactly("102");
    }

    @Test
    void reservaQueOcupaUmaNoiteNoMeioDoPeriodoBloqueia() {
        reservas.save(new Reserva(q102, "Bruno", DIA_12, DIA_13, 1, new BigDecimal("300.00")));

        assertThat(service.buscar(DIA_10, DIA_14, 3)).isEmpty();
    }

    @Test
    void reservaCanceladaLiberaOQuartoNaBusca() {
        Reserva ana = new Reserva(q102, "Ana", DIA_10, DIA_12, 2, new BigDecimal("300.00"));
        ana.cancelar(Instant.parse("2026-11-01T12:00:00Z"), "Hóspede desistiu");
        reservas.save(ana);

        assertThat(identificacoes(service.buscar(DIA_11, DIA_13, 3))).containsExactly("102");
    }

    @Test
    void reservaDeOutroQuartoNaoAfetaABusca() {
        reservas.save(new Reserva(q101, "Ana", DIA_10, DIA_12, 2, new BigDecimal("150.00")));

        assertThat(identificacoes(service.buscar(DIA_11, DIA_13, 2))).containsExactly("102");
    }

    @Test
    void saidaIgualOuAnteriorAEntradaOrientaACorrigirOPeriodo() {
        assertThatThrownBy(() -> service.buscar(DIA_12, DIA_12, 2))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("A data de saída deve ser posterior à data de entrada");
        assertThatThrownBy(() -> service.buscar(DIA_13, DIA_12, 2))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("A data de saída deve ser posterior à data de entrada");
    }

    @Test
    void datasAusentesOrientamAInformarOPeriodo() {
        assertThatThrownBy(() -> service.buscar(null, DIA_12, 2))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("Informe as datas de entrada e de saída");
        assertThatThrownBy(() -> service.buscar(DIA_11, null, 2))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("Informe as datas de entrada e de saída");
    }

    @Test
    void quantidadeDePessoasMenorQueUmOuAusenteEhRecusada() {
        assertThatThrownBy(() -> service.buscar(DIA_11, DIA_13, 0))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("A quantidade de pessoas deve ser pelo menos 1");
        assertThatThrownBy(() -> service.buscar(DIA_11, DIA_13, null))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("A quantidade de pessoas deve ser pelo menos 1");
    }
}
