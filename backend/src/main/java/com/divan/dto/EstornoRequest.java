package com.divan.dto;

import lombok.Data;


public class EstornoRequest {
    private Long extratoId; // ID do lançamento a estornar (para reservas)
    private Long notaVendaId; // ID da nota (para vendas balcão)
    private String motivo; // Obrigatório
    private Boolean criarLancamentoCorreto; // true/false
    private DadosCorrecao correcao; // Dados do lançamento correto (opcional)
    
   
    public static class DadosCorrecao {
        private Long produtoId;
        private Integer quantidade;
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


	public Long getExtratoId() {
		return extratoId;
	}


	public void setExtratoId(Long extratoId) {
		this.extratoId = extratoId;
	}


	public Long getNotaVendaId() {
		return notaVendaId;
	}


	public void setNotaVendaId(Long notaVendaId) {
		this.notaVendaId = notaVendaId;
	}


	public String getMotivo() {
		return motivo;
	}


	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}


	public Boolean getCriarLancamentoCorreto() {
		return criarLancamentoCorreto;
	}


	public void setCriarLancamentoCorreto(Boolean criarLancamentoCorreto) {
		this.criarLancamentoCorreto = criarLancamentoCorreto;
	}


	public DadosCorrecao getCorrecao() {
		return correcao;
	}


	public void setCorrecao(DadosCorrecao correcao) {
		this.correcao = correcao;
	}
    
    
}
