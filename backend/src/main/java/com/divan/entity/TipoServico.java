package com.divan.entity;

public enum TipoServico {
    AR_CONDICIONADO("Ar Condicionado"),
    LIMPEZA_FILTRO_AR("Limpeza de Filtro de Ar"),
    CHUVEIRO("Chuveiro"),
    ELETRICA("Elétrica"),
    HIDRAULICA("Hidráulica"),
    MOVEIS("Móveis"),
    PINTURA("Pintura"),
    TV("TV / Eletrônicos"),
    FECHADURA("Fechadura / Chaves"),
    LIMPEZA_PESADA("Limpeza Pesada"),
    OUTROS("Outros");

    private final String descricao;
    TipoServico(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}