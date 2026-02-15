package com.divan.dto;

import com.divan.entity.Reserva;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReservaDTO {
    
    private Long id;
    
    @NotNull(message = "Apartamento é obrigatório")
    private Long apartamentoId;
    
    private String apartamentoNumero;
    
    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;
    
    private String clienteNome;
    
    @Min(value = 1, message = "Quantidade de hóspedes deve ser no mínimo 1")
    private Integer quantidadeHospede;
    
    private Long diariaId;
    
    @NotNull(message = "Data de check-in é obrigatória")
    private LocalDateTime dataCheckin;
    
    @NotNull(message = "Data de check-out é obrigatória")
    private LocalDateTime dataCheckout;
    
    private Integer quantidadeDiaria;
    
    @DecimalMin(value = "0.0", message = "Total de diária não pode ser negativo")
    private BigDecimal totalDiaria;
    
    @DecimalMin(value = "0.0", message = "Total de produto não pode ser negativo")
    private BigDecimal totalProduto;
    
    @DecimalMin(value = "0.0", message = "Total de hospedagem não pode ser negativo")
    private BigDecimal totalHospedagem;
    
    @DecimalMin(value = "0.0", message = "Total recebido não pode ser negativo")
    private BigDecimal totalRecebido;
    
    @DecimalMin(value = "0.0", message = "Desconto não pode ser negativo")
    private BigDecimal desconto;
    
    private BigDecimal totalApagar;
    private Reserva.StatusReservaEnum status;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getApartamentoId() {
		return apartamentoId;
	}
	public void setApartamentoId(Long apartamentoId) {
		this.apartamentoId = apartamentoId;
	}
	public String getApartamentoNumero() {
		return apartamentoNumero;
	}
	public void setApartamentoNumero(String apartamentoNumero) {
		this.apartamentoNumero = apartamentoNumero;
	}
	public Long getClienteId() {
		return clienteId;
	}
	public void setClienteId(Long clienteId) {
		this.clienteId = clienteId;
	}
	public String getClienteNome() {
		return clienteNome;
	}
	public void setClienteNome(String clienteNome) {
		this.clienteNome = clienteNome;
	}
	public Integer getQuantidadeHospede() {
		return quantidadeHospede;
	}
	public void setQuantidadeHospede(Integer quantidadeHospede) {
		this.quantidadeHospede = quantidadeHospede;
	}
	public Long getDiariaId() {
		return diariaId;
	}
	public void setDiariaId(Long diariaId) {
		this.diariaId = diariaId;
	}
	public LocalDateTime getDataCheckin() {
		return dataCheckin;
	}
	public void setDataCheckin(LocalDateTime dataCheckin) {
		this.dataCheckin = dataCheckin;
	}
	public LocalDateTime getDataCheckout() {
		return dataCheckout;
	}
	public void setDataCheckout(LocalDateTime dataCheckout) {
		this.dataCheckout = dataCheckout;
	}
	public Integer getQuantidadeDiaria() {
		return quantidadeDiaria;
	}
	public void setQuantidadeDiaria(Integer quantidadeDiaria) {
		this.quantidadeDiaria = quantidadeDiaria;
	}
	public BigDecimal getTotalDiaria() {
		return totalDiaria;
	}
	public void setTotalDiaria(BigDecimal totalDiaria) {
		this.totalDiaria = totalDiaria;
	}
	public BigDecimal getTotalProduto() {
		return totalProduto;
	}
	public void setTotalProduto(BigDecimal totalProduto) {
		this.totalProduto = totalProduto;
	}
	public BigDecimal getTotalHospedagem() {
		return totalHospedagem;
	}
	public void setTotalHospedagem(BigDecimal totalHospedagem) {
		this.totalHospedagem = totalHospedagem;
	}
	public BigDecimal getTotalRecebido() {
		return totalRecebido;
	}
	public void setTotalRecebido(BigDecimal totalRecebido) {
		this.totalRecebido = totalRecebido;
	}
	public BigDecimal getDesconto() {
		return desconto;
	}
	public void setDesconto(BigDecimal desconto) {
		this.desconto = desconto;
	}
	public BigDecimal getTotalApagar() {
		return totalApagar;
	}
	public void setTotalApagar(BigDecimal totalApagar) {
		this.totalApagar = totalApagar;
	}
	public Reserva.StatusReservaEnum getStatus() {
		return status;
	}
	public void setStatus(Reserva.StatusReservaEnum status) {
		this.status = status;
	}
    
    
}
