package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


public class RelatorioOcupacaoDTO {
    
    private LocalDate data;
    private Integer totalApartamentos;
    private Integer apartamentosOcupados;
    private Integer apartamentosDisponiveis;
    private Integer apartamentosLimpeza;
    private Integer apartamentosManutencao;
    private Double taxaOcupacao; // Percentual
    private Integer totalHospedes;
    private BigDecimal receitaDiaria;
	public LocalDate getData() {
		return data;
	}
	public void setData(LocalDate data) {
		this.data = data;
	}
	public Integer getTotalApartamentos() {
		return totalApartamentos;
	}
	public void setTotalApartamentos(Integer totalApartamentos) {
		this.totalApartamentos = totalApartamentos;
	}
	public Integer getApartamentosOcupados() {
		return apartamentosOcupados;
	}
	public void setApartamentosOcupados(Integer apartamentosOcupados) {
		this.apartamentosOcupados = apartamentosOcupados;
	}
	public Integer getApartamentosDisponiveis() {
		return apartamentosDisponiveis;
	}
	public void setApartamentosDisponiveis(Integer apartamentosDisponiveis) {
		this.apartamentosDisponiveis = apartamentosDisponiveis;
	}
	public Integer getApartamentosLimpeza() {
		return apartamentosLimpeza;
	}
	public void setApartamentosLimpeza(Integer apartamentosLimpeza) {
		this.apartamentosLimpeza = apartamentosLimpeza;
	}
	public Integer getApartamentosManutencao() {
		return apartamentosManutencao;
	}
	public void setApartamentosManutencao(Integer apartamentosManutencao) {
		this.apartamentosManutencao = apartamentosManutencao;
	}
	public Double getTaxaOcupacao() {
		return taxaOcupacao;
	}
	public void setTaxaOcupacao(Double taxaOcupacao) {
		this.taxaOcupacao = taxaOcupacao;
	}
	public Integer getTotalHospedes() {
		return totalHospedes;
	}
	public void setTotalHospedes(Integer totalHospedes) {
		this.totalHospedes = totalHospedes;
	}
	public BigDecimal getReceitaDiaria() {
		return receitaDiaria;
	}
	public void setReceitaDiaria(BigDecimal receitaDiaria) {
		this.receitaDiaria = receitaDiaria;
	}
    
    
    
}
