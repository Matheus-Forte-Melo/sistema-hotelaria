package br.com.hotelaria.hotelaria.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.hotelaria.hotelaria.dto.QuartoResponse;
import br.com.hotelaria.hotelaria.exception.RegraNegocioException;
import br.com.hotelaria.hotelaria.repository.QuartoRepository;

/**
 * Busca de quartos para entrada, saída e quantidade de pessoas (issue #2, RF07). Um quarto atende
 * quando comporta o grupo e está livre em todas as noites de [entrada, saída), considerando só as
 * reservas vigentes. A consulta é apenas informativa: a gravação da reserva revalida o conflito.
 */
@Service
public class DisponibilidadeService {

    private final QuartoRepository quartos;

    public DisponibilidadeService(QuartoRepository quartos) {
        this.quartos = quartos;
    }

    @Transactional(readOnly = true)
    public List<QuartoResponse> buscar(LocalDate entrada, LocalDate saida, Integer pessoas) {
        if (entrada == null || saida == null) {
            throw new RegraNegocioException("Informe as datas de entrada e de saída");
        }
        if (!saida.isAfter(entrada)) {
            throw new RegraNegocioException("A data de saída deve ser posterior à data de entrada");
        }
        if (pessoas == null || pessoas < 1) {
            throw new RegraNegocioException("A quantidade de pessoas deve ser pelo menos 1");
        }
        return quartos.findDisponiveis(entrada, saida, pessoas).stream().map(QuartoResponse::de).toList();
    }
}
