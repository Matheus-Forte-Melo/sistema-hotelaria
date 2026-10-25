package br.com.hotelaria.hotelaria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.hotelaria.hotelaria.model.Bloco;

public interface BlocoRepository extends JpaRepository<Bloco, Long> {

    List<Bloco> findAllByOrderByNome();

    boolean existsByNomeIgnoreCase(String nome);
}
