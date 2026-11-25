package br.com.hotelaria.hotelaria.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.hotelaria.hotelaria.dto.QuartoRequest;
import br.com.hotelaria.hotelaria.dto.QuartoResponse;
import br.com.hotelaria.hotelaria.exception.RecursoNaoEncontradoException;
import br.com.hotelaria.hotelaria.exception.RegraNegocioException;
import br.com.hotelaria.hotelaria.model.Andar;
import br.com.hotelaria.hotelaria.model.Quarto;
import br.com.hotelaria.hotelaria.repository.AndarRepository;
import br.com.hotelaria.hotelaria.repository.QuartoRepository;

/**
 * Cadastro e listagem de quartos (issue #2, RF03 e RF04). O quarto pertence a um andar e guarda
 * identificação, capacidade e diária padrão. Categoria e data de ativação existem no modelo, mas
 * ficaram fora do escopo revisado: recebem valores padrão.
 */
@Service
public class QuartoService {

    static final String CATEGORIA_PADRAO = "Padrão";
    private static final ZoneId ZONA_HOTEL = ZoneId.of("America/Sao_Paulo");

    private final QuartoRepository quartos;
    private final AndarRepository andares;

    public QuartoService(QuartoRepository quartos, AndarRepository andares) {
        this.quartos = quartos;
        this.andares = andares;
    }

    @Transactional
    public QuartoResponse cadastrar(QuartoRequest pedido) {
        Andar andar = andares.findById(pedido.andarId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Andar " + pedido.andarId() + " não encontrado"));
        String identificacao = pedido.identificacao().trim();
        if (quartos.existsByAndarBlocoIdAndIdentificacaoIgnoreCase(andar.getBloco().getId(), identificacao)) {
            throw new RegraNegocioException("Já existe um quarto com a identificação " + identificacao);
        }
        Quarto quarto = new Quarto(andar, identificacao, CATEGORIA_PADRAO, pedido.capacidade(),
            pedido.valorDiariaPadrao(), LocalDate.now(ZONA_HOTEL));
        return QuartoResponse.de(quartos.saveAndFlush(quarto));
    }

    /** Quartos do andar informado ou, sem andar, de todo o hotel ordenados por andar. */
    @Transactional(readOnly = true)
    public List<QuartoResponse> listar(Long andarId) {
        List<Quarto> encontrados = andarId == null ? quartos.listarTodos() : quartos.listarDoAndar(andarId);
        return encontrados.stream().map(QuartoResponse::de).toList();
    }
}
