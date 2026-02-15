package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class RelatorioCheckinDTO {
    
    private Long codigoReserva;
    private LocalDateTime dataHoraCheckin;
    private LocalDateTime dataCheckout;
    private String apartamento;
    private Integer quantidadeHospede;
    private BigDecimal valorDiaria;
    private String hospedePrincipal;
    private String hospedesSecundarios;
    private String avisos;
	public Long getCodigoReserva() {
		return codigoReserva;
	}
	public void setCodigoReserva(Long codigoReserva) {
		this.codigoReserva = codigoReserva;
	}
	public LocalDateTime getDataHoraCheckin() {
		return dataHoraCheckin;
	}
	public void setDataHoraCheckin(LocalDateTime dataHoraCheckin) {
		this.dataHoraCheckin = dataHoraCheckin;
	}
	public LocalDateTime getDataCheckout() {
		return dataCheckout;
	}
	public void setDataCheckout(LocalDateTime dataCheckout) {
		this.dataCheckout = dataCheckout;
	}
	public String getApartamento() {
		return apartamento;
	}
	public void setApartamento(String apartamento) {
		this.apartamento = apartamento;
	}
	public Integer getQuantidadeHospede() {
		return quantidadeHospede;
	}
	public void setQuantidadeHospede(Integer quantidadeHospede) {
		this.quantidadeHospede = quantidadeHospede;
	}
	public BigDecimal getValorDiaria() {
		return valorDiaria;
	}
	public void setValorDiaria(BigDecimal valorDiaria) {
		this.valorDiaria = valorDiaria;
	}
	public String getHospedePrincipal() {
		return hospedePrincipal;
	}
	public void setHospedePrincipal(String hospedePrincipal) {
		this.hospedePrincipal = hospedePrincipal;
	}
	public String getHospedesSecundarios() {
		return hospedesSecundarios;
	}
	public void setHospedesSecundarios(String hospedesSecundarios) {
		this.hospedesSecundarios = hospedesSecundarios;
	}
	public String getAvisos() {
		return avisos;
	}
	public void setAvisos(String avisos) {
		this.avisos = avisos;
	}
    
    
}
