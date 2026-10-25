package br.com.hotelaria.hotelaria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.hotelaria.hotelaria.model.Andar;

public interface AndarRepository extends JpaRepository<Andar, Long> {

    List<Andar> findByBlocoIdOrderByNumero(Long blocoId);
}
