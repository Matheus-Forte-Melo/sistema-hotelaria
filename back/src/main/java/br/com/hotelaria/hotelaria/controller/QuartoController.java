package br.com.hotelaria.hotelaria.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.hotelaria.hotelaria.dto.QuartoRequest;
import br.com.hotelaria.hotelaria.dto.QuartoResponse;
import br.com.hotelaria.hotelaria.service.DisponibilidadeService;
import br.com.hotelaria.hotelaria.service.QuartoService;
import jakarta.validation.Valid;

/**
 * Cadastro de quartos e busca de disponibilidade (issue #2). Datas no formato AAAA-MM-DD, com saída
 * exclusiva. Recusas devolvem {@code {"mensagem": ...}}: 400 para campos ausentes ou mal formatados,
 * 404 para andar inexistente e 422 para as demais regras (identificação repetida, período inválido,
 * menos de uma pessoa).
 */
@RestController
@RequestMapping("/api/quartos")
public class QuartoController {

    private final QuartoService quartos;
    private final DisponibilidadeService disponibilidade;

    public QuartoController(QuartoService quartos, DisponibilidadeService disponibilidade) {
        this.quartos = quartos;
        this.disponibilidade = disponibilidade;
    }

    @PostMapping
    public ResponseEntity<QuartoResponse> cadastrar(@Valid @RequestBody QuartoRequest pedido) {
        QuartoResponse criado = quartos.cadastrar(pedido);
        return ResponseEntity.created(URI.create("/api/quartos/" + criado.id())).body(criado);
    }

    /** Quartos de um andar ou, sem {@code andarId}, de todo o hotel. */
    @GetMapping
    public List<QuartoResponse> listar(@RequestParam(required = false) Long andarId) {
        return quartos.listar(andarId);
    }

    @GetMapping("/disponiveis")
    public List<QuartoResponse> disponiveis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entrada,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate saida,
            @RequestParam(required = false) Integer pessoas) {
        return disponibilidade.buscar(entrada, saida, pessoas);
    }
}
