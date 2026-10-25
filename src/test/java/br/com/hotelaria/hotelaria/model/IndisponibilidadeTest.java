package br.com.hotelaria.hotelaria.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.hotelaria.hotelaria.exception.RegraNegocioException;

class IndisponibilidadeTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 10);

    private Funcionario admin;
    private Funcionario recepcionista;
    private Quarto quarto;

    @BeforeEach
    void setUp() {
        admin = new Funcionario("Root", "root@hotel.com", "hash", Perfil.ADMINISTRADOR);
        recepcionista = new Funcionario("Ana", "ana@hotel.com", "hash", Perfil.RECEPCIONISTA);
        quarto = new Quarto(new Andar(new Bloco("A"), 1), "101", "Standard", 2, BigDecimal.TEN,
            LocalDate.of(2026, 1, 1));
    }

    @Test
    void usaIntervaloSemiaberto() {
        Indisponibilidade bloqueio = new Indisponibilidade(quarto, HOJE, HOJE.plusDays(3), "Reforma", admin);

        assertThat(bloqueio.abrange(HOJE)).isTrue();
        assertThat(bloqueio.abrange(HOJE.plusDays(3))).isFalse();
        assertThat(bloqueio.sobrepoe(HOJE.plusDays(3), HOJE.plusDays(5))).isFalse();
    }

    @Test
    void alteraBloqueioFuturoSemRegistrarCorrecao() {
        Indisponibilidade bloqueio = new Indisponibilidade(quarto, HOJE.plusDays(5), HOJE.plusDays(7), "Pintura", admin);

        Optional<CorrecaoIndisponibilidade> correcao = bloqueio.alterar(HOJE.plusDays(5), HOJE.plusDays(9),
            "Pintura e piso", admin, null, HOJE);

        assertThat(correcao).isEmpty();
        assertThat(bloqueio.getFim()).isEqualTo(HOJE.plusDays(9));
    }

    @Test
    void alteracaoDeBloqueioPassadoExigeJustificativaERegistraValoresAnteriores() {
        Indisponibilidade bloqueio = new Indisponibilidade(quarto, HOJE.minusDays(5), HOJE.minusDays(2), "Vazamento",
            admin);

        assertThatThrownBy(() -> bloqueio.alterar(HOJE.minusDays(5), HOJE.minusDays(3), "Vazamento", admin, " ", HOJE))
            .isInstanceOf(RegraNegocioException.class);

        CorrecaoIndisponibilidade correcao = bloqueio.alterar(HOJE.minusDays(5), HOJE.minusDays(3), "Vazamento",
            admin, "Quarto liberado um dia antes", HOJE).orElseThrow();

        assertThat(correcao.getFimAnterior()).isEqualTo(HOJE.minusDays(2));
        assertThat(correcao.getFimNovo()).isEqualTo(HOJE.minusDays(3));
        assertThat(bloqueio.getCorrecoes()).containsExactly(correcao);
    }

    @Test
    void somenteAdministradorAlteraBloqueio() {
        Indisponibilidade bloqueio = new Indisponibilidade(quarto, HOJE.plusDays(1), HOJE.plusDays(2), "Reforma", admin);

        assertThatThrownBy(() -> bloqueio.alterar(HOJE.plusDays(1), HOJE.plusDays(3), "Reforma", recepcionista, null,
            HOJE)).isInstanceOf(RegraNegocioException.class);
    }
}
