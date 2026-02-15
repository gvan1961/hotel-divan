package com.divan.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

public class VendaBalcaoRequest {
	
	private Long usuarioId;
    
    private TipoVenda tipoVenda; // VISTA ou FATURADO
    private Long clienteId; // Obrigatório apenas para FATURADO
    private String formaPagamento; // Obrigatório para VISTA
    private BigDecimal valorPago; // Para calcular troco
    private String observacao;
    private List<ItemVenda> itens;
    
    public enum TipoVenda {
        VISTA,
        FATURADO
    }
    
    
    
    public TipoVenda getTipoVenda() {
		return tipoVenda;
	}



	public void setTipoVenda(TipoVenda tipoVenda) {
		this.tipoVenda = tipoVenda;
	}



	public Long getClienteId() {
		return clienteId;
	}



	public void setClienteId(Long clienteId) {
		this.clienteId = clienteId;
	}



	public String getFormaPagamento() {
		return formaPagamento;
	}



	public void setFormaPagamento(String formaPagamento) {
		this.formaPagamento = formaPagamento;
	}



	public BigDecimal getValorPago() {
		return valorPago;
	}



	public void setValorPago(BigDecimal valorPago) {
		this.valorPago = valorPago;
	}



	public String getObservacao() {
		return observacao;
	}



	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}



	public List<ItemVenda> getItens() {
		return itens;
	}



	public void setItens(List<ItemVenda> itens) {
		this.itens = itens;
	}

    

	public Long getUsuarioId() {
		return usuarioId;
	}



	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}



	public static class ItemVenda {
        private Long produtoId;
        private Integer quantidade;
        private BigDecimal valorUnitario; // Pode ser diferente do cadastrado (desconto/acréscimo)
        
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
