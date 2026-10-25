package br.com.hotelaria.hotelaria.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

/**
 * Pessoa hospedada (RF06). Não há papéis entre ocupantes (RN08). O documento é opcional,
 * mas quando informado é único e evita duplicação.
 */
@Entity
@Table(name = "hospede",
    uniqueConstraints = @UniqueConstraint(name = "uk_hospede_documento",
        columnNames = { "tipo_documento", "numero_documento" }),
    check = @CheckConstraint(name = "ck_hospede_documento_completo",
        constraint = "(tipo_documento IS NULL AND numero_documento IS NULL)"
            + " OR (tipo_documento IS NOT NULL AND numero_documento IS NOT NULL)"))
public class Hospede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @NotNull
    @PastOrPresent
    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Email
    @Size(max = 160)
    @Column(length = 160)
    private String email;

    @Size(max = 30)
    @Column(length = 30)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 20)
    private TipoDocumento tipoDocumento;

    @Size(max = 30)
    @Column(name = "numero_documento", length = 30)
    private String numeroDocumento;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected Hospede() {
    }

    public Hospede(String nome, LocalDate dataNascimento) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
    }

    /** Idade calculada na data de referência (RN13). */
    public int idadeEm(LocalDate data) {
        if (data.isBefore(dataNascimento)) {
            throw new IllegalArgumentException("Data anterior ao nascimento do hóspede");
        }
        return Period.between(dataNascimento, data).getYears();
    }

    public FaixaEtaria faixaEtariaEm(LocalDate data) {
        return FaixaEtaria.daIdade(idadeEm(data));
    }

    public boolean isAdultoEm(LocalDate data) {
        return !data.isBefore(dataNascimento) && faixaEtariaEm(data) == FaixaEtaria.ADULTO;
    }

    public void informarDocumento(TipoDocumento tipo, String numero) {
        this.tipoDocumento = tipo;
        this.numeroDocumento = numero;
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

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Hospede outro)) {
            return false;
        }
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
