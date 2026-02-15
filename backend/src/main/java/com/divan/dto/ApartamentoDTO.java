package com.divan.dto;

import com.divan.entity.Apartamento;
import com.divan.entity.TipoApartamento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class ApartamentoDTO {
    
    private Long id;
    
    @NotBlank(message = "Número do apartamento é obrigatório")
    private String numeroApartamento;
    
    @NotNull(message = "Tipo de apartamento é obrigatório")
    private Long tipoApartamentoId;
    
    private TipoApartamento.TipoEnum tipoApartamento;
    
    @Min(value = 1, message = "Capacidade deve ser no mínimo 1")
    private Integer capacidade;
    
    @NotBlank(message = "Camas do apartamento é obrigatório")
    private String camasDoApartamento;
    
    private Apartamento.StatusEnum status;
    private String tv;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNumeroApartamento() {
		return numeroApartamento;
	}
	public void setNumeroApartamento(String numeroApartamento) {
		this.numeroApartamento = numeroApartamento;
	}
	public Long getTipoApartamentoId() {
		return tipoApartamentoId;
	}
	public void setTipoApartamentoId(Long tipoApartamentoId) {
		this.tipoApartamentoId = tipoApartamentoId;
	}
	public TipoApartamento.TipoEnum getTipoApartamento() {
		return tipoApartamento;
	}
	public void setTipoApartamento(TipoApartamento.TipoEnum tipoApartamento) {
		this.tipoApartamento = tipoApartamento;
	}
	public Integer getCapacidade() {
		return capacidade;
	}
	public void setCapacidade(Integer capacidade) {
		this.capacidade = capacidade;
	}
	public String getCamasDoApartamento() {
		return camasDoApartamento;
	}
	public void setCamasDoApartamento(String camasDoApartamento) {
		this.camasDoApartamento = camasDoApartamento;
	}
	public Apartamento.StatusEnum getStatus() {
		return status;
	}
	public void setStatus(Apartamento.StatusEnum status) {
		this.status = status;
	}
	public String getTv() {
		return tv;
	}
	public void setTv(String tv) {
		this.tv = tv;
	}
    
    
}
