package br.com.hotelaria.hotelaria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import br.com.hotelaria.hotelaria.dto.QuartoRequest;
import br.com.hotelaria.hotelaria.dto.QuartoResponse;
import br.com.hotelaria.hotelaria.exception.RecursoNaoEncontradoException;
import br.com.hotelaria.hotelaria.exception.RegraNegocioException;
import br.com.hotelaria.hotelaria.model.Andar;
import br.com.hotelaria.hotelaria.model.Bloco;
import br.com.hotelaria.hotelaria.model.Quarto;
import br.com.hotelaria.hotelaria.repository.AndarRepository;
import br.com.hotelaria.hotelaria.repository.BlocoRepository;
import br.com.hotelaria.hotelaria.repository.QuartoRepository;

/** Cadastro e listagem de quartos da issue #2 (RF03, RF04) com H2 e os repositórios JPA reais. */
@DataJpaTest
@Import(QuartoService.class)
class QuartoServiceTest {

    @Autowired
    private QuartoService service;

    @Autowired
    private BlocoRepository blocos;

    @Autowired
    private AndarRepository andares;

    @Autowired
    private QuartoRepository quartos;

    private Andar primeiro;
    private Andar segundo;

    @BeforeEach
    void setUp() {
        Bloco bloco = blocos.save(new Bloco("Principal"));
        primeiro = andares.save(new Andar(bloco, 1));
        segundo = andares.save(new Andar(bloco, 2));
    }

    private QuartoRequest pedido(Long andarId, String identificacao, int capacidade, String diaria) {
        return new QuartoRequest(andarId, identificacao, capacidade, new BigDecimal(diaria));
    }

    @Test
    void cadastraQuartoVinculadoAoAndarComCapacidadeEDiaria() {
        QuartoResponse criado = service.cadastrar(pedido(primeiro.getId(), "101", 2, "180.00"));

        assertThat(criado.id()).isNotNull();
        assertThat(criado.andarId()).isEqualTo(primeiro.getId());
        assertThat(criado.andarNumero()).isEqualTo(1);
        assertThat(criado.identificacao()).isEqualTo("101");
        assertThat(criado.capacidade()).isEqualTo(2);
        assertThat(criado.valorDiariaPadrao()).isEqualByComparingTo("180.00");

        Quarto gravado = quartos.findById(criado.id()).orElseThrow();
        assertThat(gravado.getAndar().getId()).isEqualTo(primeiro.getId());
        assertThat(gravado.getCapacidade()).isEqualTo(2);
    }

    @Test
    void cadastraNoMesmoAndarQuartosDeCapacidadesDiferentes() {
        service.cadastrar(pedido(primeiro.getId(), "101", 2, "180.00"));
        service.cadastrar(pedido(primeiro.getId(), "102", 4, "300.00"));

        assertThat(service.listar(primeiro.getId()))
            .extracting(QuartoResponse::identificacao, QuartoResponse::capacidade)
            .containsExactly(tuple("101", 2), tuple("102", 4));
    }

    @Test
    void removeEspacosDaIdentificacao() {
        QuartoResponse criado = service.cadastrar(pedido(primeiro.getId(), "  103 ", 2, "180.00"));

        assertThat(criado.identificacao()).isEqualTo("103");
    }

    @Test
    void andarInexistenteEhRecusado() {
        assertThatThrownBy(() -> service.cadastrar(pedido(999_999L, "101", 2, "180.00")))
            .isInstanceOf(RecursoNaoEncontradoException.class)
            .hasMessage("Andar 999999 não encontrado");
    }

    @Test
    void identificacaoRepetidaNoHotelEhRecusadaSemDiferenciarMaiusculas() {
        service.cadastrar(pedido(primeiro.getId(), "10A", 2, "180.00"));

        assertThatThrownBy(() -> service.cadastrar(pedido(segundo.getId(), "10a", 4, "300.00")))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("Já existe um quarto com a identificação 10a");
        assertThat(quartos.count()).isEqualTo(1);
    }

    @Test
    void listaApenasOsQuartosDoAndarPedidoOuTodosOrdenadosPorAndar() {
        service.cadastrar(pedido(segundo.getId(), "201", 3, "250.00"));
        service.cadastrar(pedido(primeiro.getId(), "102", 4, "300.00"));
        service.cadastrar(pedido(primeiro.getId(), "101", 2, "180.00"));

        assertThat(service.listar(segundo.getId())).extracting(QuartoResponse::identificacao).containsExactly("201");
        assertThat(service.listar(null)).extracting(QuartoResponse::identificacao).containsExactly("101", "102", "201");
    }
}
