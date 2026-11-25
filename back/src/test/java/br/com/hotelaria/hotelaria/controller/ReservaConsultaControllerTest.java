package br.com.hotelaria.hotelaria.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import br.com.hotelaria.hotelaria.dto.ReservaRequest;
import br.com.hotelaria.hotelaria.model.Andar;
import br.com.hotelaria.hotelaria.model.Bloco;
import br.com.hotelaria.hotelaria.model.Quarto;
import br.com.hotelaria.hotelaria.repository.AndarRepository;
import br.com.hotelaria.hotelaria.repository.BlocoRepository;
import br.com.hotelaria.hotelaria.repository.QuartoRepository;
import br.com.hotelaria.hotelaria.service.ReservaService;

/** Contrato HTTP da busca por nome, dos detalhes e do cancelamento de reservas (issue #4). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser
class ReservaConsultaControllerTest {

    private static final LocalDate DIA_10 = LocalDate.of(2026, 11, 10);
    private static final LocalDate DIA_12 = LocalDate.of(2026, 11, 12);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ReservaService service;

    @Autowired
    private BlocoRepository blocos;

    @Autowired
    private AndarRepository andares;

    @Autowired
    private QuartoRepository quartos;

    private Quarto q101;
    private Quarto q202;

    @BeforeEach
    void setUp() {
        Bloco bloco = blocos.save(new Bloco("C"));
        q101 = quartos.save(new Quarto(andares.save(new Andar(bloco, 1)), "101", "Standard", 2,
            new BigDecimal("150.00"), LocalDate.of(2026, 1, 1)));
        q202 = quartos.save(new Quarto(andares.save(new Andar(bloco, 2)), "202", "Família", 4,
            new BigDecimal("300.00"), LocalDate.of(2026, 1, 1)));
    }

    private Long reservar(String nome, Quarto quarto) {
        return service.criar(new ReservaRequest(nome, quarto.getId(), DIA_10, DIA_12, 2, new BigDecimal("180.00"))).id();
    }

    @Test
    void buscaPorNomeListaAsReservasComQuartoEPeriodo() throws Exception {
        reservar("Ana Souza", q101);
        reservar("Ana Souza", q202);
        service.criar(new ReservaRequest("Bruno Lima", q101.getId(), DIA_12, DIA_12.plusDays(2), 1, null));

        mvc.perform(get("/api/reservas").param("nome", "ana"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].nomeHospede").value("Ana Souza"))
            .andExpect(jsonPath("$[0].quartoIdentificacao").value("101"))
            .andExpect(jsonPath("$[0].dataEntrada").value("2026-11-10"))
            .andExpect(jsonPath("$[0].dataSaida").value("2026-11-12"))
            .andExpect(jsonPath("$[1].quartoIdentificacao").value("202"));
    }

    @Test
    void buscaSemNomeDevolve422ComMotivo() throws Exception {
        mvc.perform(get("/api/reservas"))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.mensagem").value("Informe o nome do hóspede para buscar"));
    }

    @Test
    void consultaDetalhesPorIdentificador() throws Exception {
        Long id = reservar("Ana Souza", q101);

        mvc.perform(get("/api/reservas/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.quantidadePessoas").value(2))
            .andExpect(jsonPath("$.valorDiaria").value(180.0))
            .andExpect(jsonPath("$.valorTotal").value(360.0))
            .andExpect(jsonPath("$.situacao").value("CONFIRMADA"));
    }

    @Test
    void consultarReservaInexistenteDevolve404() throws Exception {
        mvc.perform(get("/api/reservas/{id}", 999_999))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void cancelaComMotivoEConsultaNovamenteComoCancelada() throws Exception {
        Long id = reservar("Ana Souza", q101);

        mvc.perform(post("/api/reservas/{id}/cancelamento", id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\": \"Hóspede desistiu\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.situacao").value("CANCELADA"));

        mvc.perform(get("/api/reservas/{id}", id))
            .andExpect(jsonPath("$.situacao").value("CANCELADA"));
    }

    @Test
    void cancelaSemCorpo() throws Exception {
        Long id = reservar("Ana Souza", q101);

        mvc.perform(post("/api/reservas/{id}/cancelamento", id).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.situacao").value("CANCELADA"));
    }

    @Test
    void cancelarDuasVezesDevolve422() throws Exception {
        Long id = reservar("Ana Souza", q101);
        mvc.perform(post("/api/reservas/{id}/cancelamento", id).with(csrf())).andExpect(status().isOk());

        mvc.perform(post("/api/reservas/{id}/cancelamento", id).with(csrf()))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void cancelarReservaInexistenteDevolve404() throws Exception {
        mvc.perform(post("/api/reservas/{id}/cancelamento", 999_999).with(csrf()))
            .andExpect(status().isNotFound());
    }
}
