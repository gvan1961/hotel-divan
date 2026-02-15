package com.divan.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;


public class ComandaConsumoRequest {
    
    private Long reservaId; // Obrigatório
    private String observacao;
    private List<ItemConsumo> itens;
    
    
    
    
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




	
    public static class ItemConsumo {
        private Long produtoId;
        private Integer quantidade;
        private BigDecimal valorUnitario; // Opcional - usa valor do produto se null
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
		public BigDecimal getValorUnitario() {
			return valorUnitario;
		}
		public void setValorUnitario(BigDecimal valorUnitario) {
			this.valorUnitario = valorUnitario;
		}
        
        
        
    }
}
