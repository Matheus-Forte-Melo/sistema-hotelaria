package br.com.hotelaria.hotelaria.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.hotelaria.hotelaria.model.Hospede;
import br.com.hotelaria.hotelaria.model.TipoDocumento;

public interface HospedeRepository extends JpaRepository<Hospede, Long> {

    Optional<Hospede> findByTipoDocumentoAndNumeroDocumento(TipoDocumento tipo, String numero);

    /** Possíveis duplicados a sinalizar para conferência, sem fusão automática (RF06). */
    List<Hospede> findByNomeIgnoreCaseAndDataNascimento(String nome, LocalDate dataNascimento);

    Page<Hospede> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
