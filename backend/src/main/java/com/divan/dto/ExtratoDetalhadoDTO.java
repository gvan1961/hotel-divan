package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ExtratoDetalhadoDTO {
    
    // Informações da Reserva
    private Long reservaId;
    private String numeroApartamento;
    private String tipoApartamento;
    private String clienteNome;
    private String clienteCpf;
    private Integer quantidadeHospede;
    private LocalDateTime dataCheckin;
    private LocalDateTime dataCheckout;
    private Integer quantidadeDiarias;
    private String statusReserva;
    
    // Histórico de Hóspedes
    private List<HistoricoHospedeDTO> historicoHospedes;
    
    // Lançamentos (Extratos)
    private List<LancamentoDTO> lancamentos;
    
    // Totais
    private BigDecimal totalDiarias;
    private BigDecimal totalProdutos;
    private BigDecimal totalHospedagem;
    private BigDecimal totalRecebido;
    private BigDecimal desconto;
    private BigDecimal saldoDevedor;
	public Long getReservaId() {
		return reservaId;
	}
	public void setReservaId(Long reservaId) {
		this.reservaId = reservaId;
	}
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
	public String getClienteNome() {
		return clienteNome;
	}
	public void setClienteNome(String clienteNome) {
		this.clienteNome = clienteNome;
	}
	public String getClienteCpf() {
		return clienteCpf;
	}
	public void setClienteCpf(String clienteCpf) {
		this.clienteCpf = clienteCpf;
	}
	public Integer getQuantidadeHospede() {
		return quantidadeHospede;
	}
	public void setQuantidadeHospede(Integer quantidadeHospede) {
		this.quantidadeHospede = quantidadeHospede;
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
	public Integer getQuantidadeDiarias() {
		return quantidadeDiarias;
	}
	public void setQuantidadeDiarias(Integer quantidadeDiarias) {
		this.quantidadeDiarias = quantidadeDiarias;
	}
	public String getStatusReserva() {
		return statusReserva;
	}
	public void setStatusReserva(String statusReserva) {
		this.statusReserva = statusReserva;
	}
	public List<HistoricoHospedeDTO> getHistoricoHospedes() {
		return historicoHospedes;
	}
	public void setHistoricoHospedes(List<HistoricoHospedeDTO> historicoHospedes) {
		this.historicoHospedes = historicoHospedes;
	}
	public List<LancamentoDTO> getLancamentos() {
		return lancamentos;
	}
	public void setLancamentos(List<LancamentoDTO> lancamentos) {
		this.lancamentos = lancamentos;
	}
	public BigDecimal getTotalDiarias() {
		return totalDiarias;
	}
	public void setTotalDiarias(BigDecimal totalDiarias) {
		this.totalDiarias = totalDiarias;
	}
	public BigDecimal getTotalProdutos() {
		return totalProdutos;
	}
	public void setTotalProdutos(BigDecimal totalProdutos) {
		this.totalProdutos = totalProdutos;
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
	public BigDecimal getSaldoDevedor() {
		return saldoDevedor;
	}
	public void setSaldoDevedor(BigDecimal saldoDevedor) {
		this.saldoDevedor = saldoDevedor;
	}
    
    
}
