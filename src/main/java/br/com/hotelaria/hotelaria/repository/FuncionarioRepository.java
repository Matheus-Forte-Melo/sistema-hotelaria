package br.com.hotelaria.hotelaria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.hotelaria.hotelaria.model.Funcionario;
import br.com.hotelaria.hotelaria.model.Perfil;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByEmailIgnoreCase(String email);

    /** Usado para impedir a desativação do último administrador ativo (RF02). */
    long countByPerfilAndAtivoTrue(Perfil perfil);
}
