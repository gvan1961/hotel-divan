package com.divan.dto;

import java.util.List;

public class ConsumoJantarDTO {
    
    private Long reservaId;
    private String observacao;
    private List<ItemConsumo> itens;
    
    public static class ItemConsumo {
        private Long produtoId;
        private Integer quantidade;
        
        // Getters e Setters
        public Long getProdutoId() {
            return produtoId;
        }
        
        public void setProdutoId(Long produtoId) {
            this.produtoId = produtoId;
        }
        
        public Integer getQuantidade() {
            return quantidade;
        }
        
        public void setQuantidade(Integer quantidade) {
            this.quantidade = quantidade;
        }
    }
    
    // Getters e Setters
    public Long getReservaId() {
        return reservaId;
    }
    
    public void setReservaId(Long reservaId) {
        this.reservaId = reservaId;
    }
    
    public String getObservacao() {
        return observacao;
    }
    
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
    
    public List<ItemConsumo> getItens() {
        return itens;
    }
    
    public void setItens(List<ItemConsumo> itens) {
        this.itens = itens;
    }
}