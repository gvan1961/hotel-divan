package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


public class RelatorioApartamentoDTO {
    
    private String numeroApartamento;
    private String tipoApartamento;
    private Integer quantidadeReservas;
    private Integer diasOcupados;
    private Double taxaOcupacao;
    private BigDecimal receitaTotal;
    private BigDecimal receitaMedia;
	public String getNumeroApartamento() {
		return numeroApartamento;
	}
	public void setNumeroApartamento(String numeroApartamento) {
		this.numeroApartamento = numeroApartamento;
	}
	public String getTipoApartamento() {
		return tipoApartamento;
	}
	public void setTipoApartamento(String tipoApartamento) {
		this.tipoApartamento = tipoApartamento;
	}
	public Integer getQuantidadeReservas() {
		return quantidadeReservas;
	}
	public void setQuantidadeReservas(Integer quantidadeReservas) {
		this.quantidadeReservas = quantidadeReservas;
	}
	public Integer getDiasOcupados() {
		return diasOcupados;
	}
	public void setDiasOcupados(Integer diasOcupados) {
		this.diasOcupados = diasOcupados;
	}
	public Double getTaxaOcupacao() {
		return taxaOcupacao;
	}
	public void setTaxaOcupacao(Double taxaOcupacao) {
		this.taxaOcupacao = taxaOcupacao;
	}
	public BigDecimal getReceitaTotal() {
		return receitaTotal;
	}
	public void setReceitaTotal(BigDecimal receitaTotal) {
		this.receitaTotal = receitaTotal;
	}
	public BigDecimal getReceitaMedia() {
		return receitaMedia;
	}
	public void setReceitaMedia(BigDecimal receitaMedia) {
		this.receitaMedia = receitaMedia;
	}
          
    
      
    
    
    
}
