package br.com.hotelaria.hotelaria.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import br.com.hotelaria.hotelaria.model.Andar;
import br.com.hotelaria.hotelaria.model.Bloco;
import br.com.hotelaria.hotelaria.model.Reserva;
import br.com.hotelaria.hotelaria.repository.AndarRepository;
import br.com.hotelaria.hotelaria.repository.BlocoRepository;
import br.com.hotelaria.hotelaria.repository.QuartoRepository;
import br.com.hotelaria.hotelaria.repository.ReservaRepository;

/** Contrato HTTP do cadastro de quartos e da busca de disponibilidade (issue #2). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser
class QuartoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BlocoRepository blocos;

    @Autowired
    private AndarRepository andares;

    @Autowired
    private QuartoRepository quartos;

    @Autowired
    private ReservaRepository reservas;

    private Andar andar;

    @BeforeEach
    void setUp() {
        andar = andares.save(new Andar(blocos.save(new Bloco("Teste")), 7));
    }

    private ResultActions cadastrar(String identificacao, int capacidade, String diaria) throws Exception {
        return mvc.perform(post("/api/quartos").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"andarId": %d, "identificacao": "%s", "capacidade": %d, "valorDiariaPadrao": %s}
                """.formatted(andar.getId(), identificacao, capacidade, diaria)));
    }

    private ResultActions buscar(String entrada, String saida, String pessoas) throws Exception {
        return mvc.perform(get("/api/quartos/disponiveis")
            .param("entrada", entrada).param("saida", saida).param("pessoas", pessoas));
    }

    @Test
    void cadastraQuartoEDevolveLocalizacaoEAndar() throws Exception {
        cadastrar("701", 2, "180.00")
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.andarId").value(andar.getId()))
            .andExpect(jsonPath("$.andarNumero").value(7))
            .andExpect(jsonPath("$.identificacao").value("701"))
            .andExpect(jsonPath("$.capacidade").value(2))
            .andExpect(jsonPath("$.valorDiariaPadrao").value(180.0));
    }

    @Test
    void camposAusentesOuInvalidosDevolvem400ComOMotivoDeCadaCampo() throws Exception {
        mvc.perform(post("/api/quartos").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identificacao\": \" \", \"capacidade\": 0, \"valorDiariaPadrao\": -1}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.andarId").value("Informe o andar"))
            .andExpect(jsonPath("$.campos.identificacao").value("Informe a identificação do quarto"))
            .andExpect(jsonPath("$.campos.capacidade").value("A capacidade deve ser pelo menos 1"))
            .andExpect(jsonPath("$.campos.valorDiariaPadrao").value("A diária não pode ser negativa"));
    }

    @Test
    void andarInexistenteDevolve404() throws Exception {
        mvc.perform(post("/api/quartos").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"andarId\": 999999, \"identificacao\": \"X1\", \"capacidade\": 2, \"valorDiariaPadrao\": 100}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensagem").value("Andar 999999 não encontrado"));
    }

    @Test
    void identificacaoRepetidaDevolve422() throws Exception {
        cadastrar("701", 2, "180.00").andExpect(status().isCreated());

        cadastrar("701", 4, "300.00")
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.mensagem").value("Já existe um quarto com a identificação 701"));
    }

    @Test
    void listaOsQuartosDoAndar() throws Exception {
        cadastrar("702", 4, "300.00");
        cadastrar("701", 2, "180.00");

        mvc.perform(get("/api/quartos").param("andarId", andar.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].identificacao").value("701"))
            .andExpect(jsonPath("$[1].identificacao").value("702"));
    }

    /** Cenário da issue: 101 de duas vagas, 102 de quatro e reserva vigente no 102 de 10 a 12. */
    @Test
    void cenarioDaIssueBuscaTresVagasConsiderandoReservasVigentes() throws Exception {
        cadastrar("701", 2, "180.00");
        cadastrar("702", 4, "300.00");
        buscar("2026-11-11", "2026-11-13", "3")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].identificacao").value("702"))
            .andExpect(jsonPath("$[0].andarNumero").value(7))
            .andExpect(jsonPath("$[0].capacidade").value(4))
            .andExpect(jsonPath("$[0].valorDiariaPadrao").value(300.0));

        Reserva ana = reservas.save(new Reserva(quartos.findByAndarIdOrderByIdentificacao(andar.getId()).get(1),
            "Ana", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12), 2, new BigDecimal("300.00")));

        buscar("2026-11-11", "2026-11-13", "3").andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        buscar("2026-11-12", "2026-11-14", "3").andExpect(jsonPath("$[0].identificacao").value("702"));

        ana.cancelar(Instant.parse("2026-11-01T12:00:00Z"), "Desistência");
        reservas.saveAndFlush(ana);

        buscar("2026-11-11", "2026-11-13", "3").andExpect(jsonPath("$[0].identificacao").value("702"));
    }

    @Test
    void periodoInvalidoDevolve422OrientandoACorrecao() throws Exception {
        buscar("2026-11-13", "2026-11-11", "2")
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.mensagem").value("A data de saída deve ser posterior à data de entrada"));
    }

    @Test
    void menosDeUmaPessoaDevolve422() throws Exception {
        buscar("2026-11-11", "2026-11-13", "0")
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.mensagem").value("A quantidade de pessoas deve ser pelo menos 1"));
    }

    @Test
    void parametrosAusentesDevolvem422ComOrientacao() throws Exception {
        mvc.perform(get("/api/quartos/disponiveis"))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.mensagem").value("Informe as datas de entrada e de saída"));
    }

    @Test
    void dataForaDoFormatoDevolve400() throws Exception {
        buscar("11/11/2026", "2026-11-13", "2")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensagem").value("Parâmetro entrada inválido. Datas devem usar o formato AAAA-MM-DD."));
    }
}
