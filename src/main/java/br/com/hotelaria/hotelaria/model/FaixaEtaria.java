package br.com.hotelaria.hotelaria.model;

/**
 * Faixas estatísticas da RN12.
 */
public enum FaixaEtaria {
    CRIANCA,
    ADOLESCENTE,
    ADULTO;

    public static FaixaEtaria daIdade(int idade) {
        if (idade < 0) {
            throw new IllegalArgumentException("Idade não pode ser negativa");
        }
        if (idade <= 11) {
            return CRIANCA;
        }
        if (idade <= 17) {
            return ADOLESCENTE;
        }
        return ADULTO;
    }
}
