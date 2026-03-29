package com.divan.dto;

import java.util.List;

public class ComandaRapidaDTO {
    
    private Long reservaId;
    private List<ItemComanda> itens;
    
    public static class ItemComanda {
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
    
    public List<ItemComanda> getItens() {
        return itens;
    }
    
    public void setItens(List<ItemComanda> itens) {
        this.itens = itens;
    }
}
