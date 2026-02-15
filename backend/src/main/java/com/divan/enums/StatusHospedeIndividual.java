package com.divan.enums;

public enum StatusHospedeIndividual {
    HOSPEDADO("Hospedado"),
    SAIU("Saiu");
    
    private final String descricao;
    
    StatusHospedeIndividual(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}
