package com.divan.entity;

public enum StatusManutencao {
    PENDENTE("Pendente"),
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDO("Concluído"),
    CANCELADO("Cancelado");

    private final String descricao;
    StatusManutencao(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}

