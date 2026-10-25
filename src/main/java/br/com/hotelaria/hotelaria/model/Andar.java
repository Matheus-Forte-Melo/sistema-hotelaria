package br.com.hotelaria.hotelaria.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

/**
 * Andar de um bloco (RF03). Seleciona a grade de quartos exibida no mapa.
 */
@Entity
@Table(name = "andar",
    uniqueConstraints = @UniqueConstraint(name = "uk_andar_bloco_numero", columnNames = { "bloco_id", "numero" }))
public class Andar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "bloco_id", nullable = false, foreignKey = @ForeignKey(name = "fk_andar_bloco"))
    private Bloco bloco;

    @NotNull
    @Column(nullable = false)
    private Integer numero;

    @OneToMany(mappedBy = "andar")
    @OrderBy("identificacao")
    private List<Quarto> quartos = new ArrayList<>();

    protected Andar() {
    }

    public Andar(Bloco bloco, Integer numero) {
        this.bloco = bloco;
        this.numero = numero;
    }

    public Long getId() {
        return id;
    }

    public Bloco getBloco() {
        return bloco;
    }

    public Integer getNumero() {
        return numero;
    }

    public List<Quarto> getQuartos() {
        return Collections.unmodifiableList(quartos);
    }
}
