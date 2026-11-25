package br.com.hotelaria.hotelaria.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.hotelaria.hotelaria.dto.CancelamentoRequest;
import br.com.hotelaria.hotelaria.dto.ReservaRequest;
import br.com.hotelaria.hotelaria.dto.ReservaResponse;
import br.com.hotelaria.hotelaria.service.ReservaService;
import jakarta.validation.Valid;

/**
 * Criação, edição, busca, consulta e cancelamento de reservas. Recusas devolvem {@code {"mensagem": ...}}: 400 para campos ausentes,
 * 404 para quarto ou reserva inexistente, 409 para conflito de período e 422 para as demais regras.
 */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservaResponse> criar(@Valid @RequestBody ReservaRequest pedido) {
        ReservaResponse criada = service.criar(pedido);
        return ResponseEntity.created(URI.create("/api/reservas/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    public ReservaResponse editar(@PathVariable Long id, @Valid @RequestBody ReservaRequest pedido) {
        return service.editar(id, pedido);
    }

    /** Busca pelo nome de referência, por trecho e sem diferenciar maiúsculas. Inclui as canceladas. */
    @GetMapping
    public List<ReservaResponse> buscarPorNome(@RequestParam(required = false) String nome) {
        return service.buscarPorNome(nome);
    }

    @GetMapping("/{id}")
    public ReservaResponse consultar(@PathVariable Long id) {
        return service.consultar(id);
    }

    /** Cancela e devolve a reserva preservada com situação CANCELADA. O corpo, com o motivo, é opcional. */
    @PostMapping("/{id}/cancelamento")
    public ReservaResponse cancelar(@PathVariable Long id,
            @Valid @RequestBody(required = false) CancelamentoRequest pedido) {
        return service.cancelar(id, pedido == null ? null : pedido.motivo());
    }
}
