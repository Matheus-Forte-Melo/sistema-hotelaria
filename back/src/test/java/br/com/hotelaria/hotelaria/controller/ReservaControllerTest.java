package br.com.hotelaria.hotelaria.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.com.hotelaria.hotelaria.model.Andar;
import br.com.hotelaria.hotelaria.model.Bloco;
import br.com.hotelaria.hotelaria.model.Quarto;
import br.com.hotelaria.hotelaria.repository.AndarRepository;
import br.com.hotelaria.hotelaria.repository.BlocoRepository;
import br.com.hotelaria.hotelaria.repository.QuartoRepository;

/** Contrato HTTP de criação e edição de reservas e o formato das mensagens de recusa. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser
class ReservaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BlocoRepository blocos;

    @Autowired
    private AndarRepository andares;

    @Autowired
    private QuartoRepository quartos;

    private Quarto quarto;

    @BeforeEach
    void setUp() {
        Andar andar = andares.save(new Andar(blocos.save(new Bloco("B")), 3));
        quarto = quartos.save(new Quarto(andar, "301", "Standard", 2, new BigDecimal("150.00"),
            LocalDate.of(2026, 1, 1)));
    }

    private String corpo(String nome, String entrada, String saida, int pessoas) {
        return """
            {"nomeHospede": "%s", "quartoId": %d, "dataEntrada": "%s", "dataSaida": "%s",
             "quantidadePessoas": %d, "valorDiaria": 180.00}
            """.formatted(nome, quarto.getId(), entrada, saida, pessoas);
    }

    private Long criarAna() throws Exception {
        String resposta = mvc.perform(post("/api/reservas").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Ana", "2026-11-10", "2026-11-12", 2)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return Long.valueOf(resposta.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void criaReservaEDevolveTotalELocalizacao() throws Exception {
        mvc.perform(post("/api/reservas").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Ana", "2026-11-10", "2026-11-12", 2)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.nomeHospede").value("Ana"))
            .andExpect(jsonPath("$.quartoIdentificacao").value("301"))
            .andExpect(jsonPath("$.andarNumero").value(3))
            .andExpect(jsonPath("$.dataEntrada").value("2026-11-10"))
            .andExpect(jsonPath("$.quantidadeDiarias").value(2))
            .andExpect(jsonPath("$.valorTotal").value(360.0))
            .andExpect(jsonPath("$.situacao").value("CONFIRMADA"));
    }

    @Test
    void conflitoDevolve409ComMotivo() throws Exception {
        criarAna();

        mvc.perform(post("/api/reservas").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Bruno", "2026-11-11", "2026-11-13", 1)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void capacidadeExcedidaDevolve422ComMotivo() throws Exception {
        mvc.perform(post("/api/reservas").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Ana", "2026-11-10", "2026-11-12", 3)))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void camposObrigatoriosAusentesDevolvem400() throws Exception {
        mvc.perform(post("/api/reservas").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quartoId\": %d}".formatted(quarto.getId())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensagem").isNotEmpty())
            .andExpect(jsonPath("$.campos.nomeHospede").exists());
    }

    @Test
    void editaReserva() throws Exception {
        Long id = criarAna();

        mvc.perform(put("/api/reservas/{id}", id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Ana", "2026-11-10", "2026-11-13", 2)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dataSaida").value("2026-11-13"))
            .andExpect(jsonPath("$.valorTotal").value(540.0));
    }

    @Test
    void editarReservaInexistenteDevolve404() throws Exception {
        mvc.perform(put("/api/reservas/{id}", 999_999).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Ana", "2026-11-10", "2026-11-12", 2)))
            .andExpect(status().isNotFound());
    }
}
