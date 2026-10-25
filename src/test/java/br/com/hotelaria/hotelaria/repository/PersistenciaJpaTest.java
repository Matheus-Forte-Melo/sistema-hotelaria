package br.com.hotelaria.hotelaria.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.hotelaria.hotelaria.model.Andar;
import br.com.hotelaria.hotelaria.model.Bloco;
import br.com.hotelaria.hotelaria.model.CorrecaoHistorico;
import br.com.hotelaria.hotelaria.model.CorrecaoIndisponibilidade;
import br.com.hotelaria.hotelaria.model.Funcionario;
import br.com.hotelaria.hotelaria.model.Hospede;
import br.com.hotelaria.hotelaria.model.Indisponibilidade;
import br.com.hotelaria.hotelaria.model.Perfil;
import br.com.hotelaria.hotelaria.model.Quarto;
import br.com.hotelaria.hotelaria.model.Reserva;
import br.com.hotelaria.hotelaria.model.TipoDocumento;
import jakarta.persistence.PersistenceException;

@DataJpaTest
class PersistenciaJpaTest {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    @Autowired
    private TestEntityManager em;

    @Autowired
    private FuncionarioRepository funcionarios;

    @Autowired
    private BlocoRepository blocos;

    @Autowired
    private AndarRepository andares;

    @Autowired
    private QuartoRepository quartos;

    @Autowired
    private HospedeRepository hospedes;

    @Autowired
    private ReservaRepository reservas;

    @Autowired
    private IndisponibilidadeRepository indisponibilidades;

    @Autowired
    private CorrecaoHistoricoRepository correcoes;

    @Autowired
    private CorrecaoIndisponibilidadeRepository correcoesIndisponibilidade;

    private Funcionario admin;
    private Bloco bloco;
    private Andar andar;
    private Quarto quarto;
    private Hospede adulto;
    private Hospede crianca;

    @BeforeEach
    void setUp() {
        admin = funcionarios.save(new Funcionario("Admin", "admin@hotel.com", "hash", Perfil.ADMINISTRADOR));
        bloco = blocos.save(new Bloco("A"));
        andar = andares.save(new Andar(bloco, 1));
        quarto = quartos.save(new Quarto(andar, "101", "Standard", 3, new BigDecimal("200.00"),
            LocalDate.of(2026, 1, 1)));
        adulto = new Hospede("Carlos", LocalDate.of(1990, 5, 1));
        adulto.informarDocumento(TipoDocumento.CPF, "12345678900");
        adulto = hospedes.save(adulto);
        crianca = hospedes.save(new Hospede("Bia", LocalDate.of(2020, 3, 1)));
    }

    private Reserva novaReserva(LocalDate entrada, LocalDate saida) {
        Reserva reserva = new Reserva(quarto, entrada, saida, 2, new BigDecimal("180.00"), admin);
        reserva.adicionarHospede(adulto);
        reserva.adicionarHospede(crianca);
        return reservas.saveAndFlush(reserva);
    }

    @Test
    void gravaELeEstruturaFisicaComAssociacoes() {
        em.clear();

        Bloco lido = blocos.findById(bloco.getId()).orElseThrow();
        assertThat(lido.getAndares()).extracting(Andar::getNumero).containsExactly(1);
        assertThat(lido.getAndares().get(0).getQuartos()).extracting(Quarto::getIdentificacao).containsExactly("101");
        assertThat(quartos.existsByAndarBlocoIdAndIdentificacaoIgnoreCase(bloco.getId(), "101")).isTrue();
        assertThat(andares.findByBlocoIdOrderByNumero(bloco.getId())).hasSize(1);
    }

    @Test
    void gravaReservaComHospedesEPreservaDiariaAcordada() {
        Reserva salva = novaReserva(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12));
        em.clear();

        Reserva lida = reservas.findById(salva.getId()).orElseThrow();
        assertThat(lida.getHospedes()).extracting(Hospede::getNome).containsExactlyInAnyOrder("Carlos", "Bia");
        assertThat(lida.getValorDiaria()).isEqualByComparingTo("180.00");
        assertThat(lida.valorPrevisto()).isEqualByComparingTo("360.00");
        assertThat(lida.getCriadaEm()).isNotNull();
        assertThat(lida.getVersao()).isNotNull();
        assertThat(reservas.findByHospedesIdOrderByDataEntradaPrevistaDesc(crianca.getId())).hasSize(1);
    }

    @Test
    void detectaConflitoComIntervaloSemiabertoEIgnoraCanceladas() {
        Reserva reserva = novaReserva(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12));
        Long q = quarto.getId();

        assertThat(reservas.existeConflito(q, LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 14), null)).isFalse();
        assertThat(reservas.existeConflito(q, LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 13), null)).isTrue();
        assertThat(reservas.existeConflito(q, LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 13),
            reserva.getId())).isFalse();

        reserva.cancelar(Instant.now(), "Desistência");
        reservas.saveAndFlush(reserva);

        assertThat(reservas.existeConflito(q, LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 13), null)).isFalse();
    }

    @Test
    void detectaSobreposicaoDeIndisponibilidade() {
        indisponibilidades.saveAndFlush(new Indisponibilidade(quarto, LocalDate.of(2026, 12, 1),
            LocalDate.of(2026, 12, 5), "Reforma", admin));
        Long q = quarto.getId();

        assertThat(indisponibilidades.existeSobreposicao(q, LocalDate.of(2026, 12, 4), LocalDate.of(2026, 12, 6), null))
            .isTrue();
        assertThat(indisponibilidades.existeSobreposicao(q, LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 6), null))
            .isFalse();
    }

    @Test
    void persisteCorrecaoDeBloqueioPassadoPorCascata() {
        LocalDate hoje = LocalDate.of(2026, 12, 10);
        Indisponibilidade bloqueio = indisponibilidades.saveAndFlush(new Indisponibilidade(quarto,
            LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5), "Reforma", admin));
        bloqueio.alterar(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 4), "Reforma", admin,
            "Obra terminou antes", hoje);
        indisponibilidades.saveAndFlush(bloqueio);
        em.clear();

        CorrecaoIndisponibilidade correcao = correcoesIndisponibilidade
            .findByIndisponibilidadeIdOrderByCorrigidaEmDesc(bloqueio.getId()).get(0);
        assertThat(correcao.getFimAnterior()).isEqualTo(LocalDate.of(2026, 12, 5));
        assertThat(correcao.getFimNovo()).isEqualTo(LocalDate.of(2026, 12, 4));
        assertThat(indisponibilidades.findById(bloqueio.getId()).orElseThrow().getFim())
            .isEqualTo(LocalDate.of(2026, 12, 4));
    }

    @Test
    void persisteCorrecaoDeHistoricoPorCascata() {
        Reserva reserva = novaReserva(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12));
        Instant entrada = LocalDate.of(2026, 11, 10).atTime(14, 0).atZone(ZONA).toInstant();
        Instant saida = LocalDate.of(2026, 11, 12).atTime(11, 0).atZone(ZONA).toInstant();
        reserva.fazerCheckIn(entrada, ZONA);
        reserva.fazerCheckOut(saida);
        reserva.corrigirHorariosEfetivos(entrada, saida.plusSeconds(1800), admin, "Saída registrada com atraso");
        reservas.saveAndFlush(reserva);
        em.clear();

        CorrecaoHistorico correcao = correcoes.findByReservaIdOrderByCorrigidaEmDesc(reserva.getId()).get(0);
        assertThat(correcao.getCheckOutAnterior()).isEqualTo(saida);
        assertThat(correcao.getAutor().getId()).isEqualTo(admin.getId());
    }

    @Test
    void rejeitaEmailDeFuncionarioDuplicado() {
        assertThatThrownBy(() -> funcionarios.saveAndFlush(
            new Funcionario("Outro", "admin@hotel.com", "hash", Perfil.RECEPCIONISTA)))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaDocumentoDeHospedeDuplicado() {
        Hospede duplicado = new Hospede("Carlos Silva", LocalDate.of(1991, 1, 1));
        duplicado.informarDocumento(TipoDocumento.CPF, "12345678900");

        assertThatThrownBy(() -> hospedes.saveAndFlush(duplicado)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaIdentificacaoDeQuartoDuplicadaNoAndar() {
        assertThatThrownBy(() -> quartos.saveAndFlush(new Quarto(andar, "101", "Luxo", 2, BigDecimal.TEN,
            LocalDate.of(2026, 1, 1)))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void bancoRejeitaPeriodoDeReservaInvalido() {
        Reserva reserva = novaReserva(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12));

        assertThatThrownBy(() -> em.getEntityManager()
            .createNativeQuery("update reserva set data_saida_prevista = data_entrada_prevista where id = :id")
            .setParameter("id", reserva.getId())
            .executeUpdate())
            .isInstanceOf(PersistenceException.class);
    }

    @Test
    void bancoImpedeHospedeDuplicadoNaMesmaReserva() {
        Reserva reserva = novaReserva(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12));

        assertThatThrownBy(() -> em.getEntityManager()
            .createNativeQuery("insert into reserva_hospede (reserva_id, hospede_id) values (:r, :h)")
            .setParameter("r", reserva.getId())
            .setParameter("h", adulto.getId())
            .executeUpdate())
            .isInstanceOf(PersistenceException.class);
    }

    @Test
    void impedeExclusaoDeHospedeReferenciadoEmReserva() {
        novaReserva(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12));
        em.clear();

        assertThatThrownBy(() -> {
            hospedes.deleteById(crianca.getId());
            hospedes.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
