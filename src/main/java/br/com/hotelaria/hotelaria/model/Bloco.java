package br.com.hotelaria.hotelaria.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Agrupamento físico de andares (RF03). Primeiro nível de navegação do mapa.
 */
@Entity
@Table(name = "bloco",
    uniqueConstraints = @UniqueConstraint(name = "uk_bloco_nome", columnNames = "nome"))
public class Bloco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String nome;

    @OneToMany(mappedBy = "bloco")
    @OrderBy("numero")
    private List<Andar> andares = new ArrayList<>();

    protected Bloco() {
    }

    public Bloco(String nome) {
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Andar> getAndares() {
        return Collections.unmodifiableList(andares);
    }
}
