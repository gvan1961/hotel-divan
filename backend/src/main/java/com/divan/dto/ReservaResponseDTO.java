package com.divan.dto;

import com.divan.entity.ExtratoReserva;
import com.divan.entity.HistoricoHospede;
import com.divan.entity.Reserva;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


public class ReservaResponseDTO {
    
    private Long id;
    private ClienteResponseDTO cliente;
    private ApartamentoResponseDTO apartamento;      
    
    
    private Integer quantidadeHospede;
    private LocalDateTime checkin;
    private LocalDateTime checkout;
    
    private Integer quantidadeDiarias;
    private BigDecimal valorDiaria;
    private BigDecimal totalDiarias;
    private BigDecimal totalHospedagem;
    private BigDecimal totalRecebido;
    private BigDecimal totalApagar;
    
    private Reserva.StatusReservaEnum status;
    private String observacoes;
    
    
    private LocalDateTime dataCheckin;
    private LocalDateTime dataCheckout;
    private Integer quantidadeDiaria;
    
    private BigDecimal totalDiaria;
    
    private List<ExtratoReserva> extratos;
    private List<HistoricoHospede> historicos;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public ClienteResponseDTO getCliente() {
		return cliente;
	}
	public void setCliente(ClienteResponseDTO cliente) {
		this.cliente = cliente;
	}
	public ApartamentoResponseDTO getApartamento() {
		return apartamento;
	}
	public void setApartamento(ApartamentoResponseDTO apartamento) {
		this.apartamento = apartamento;
	}
	public Integer getQuantidadeHospede() {
		return quantidadeHospede;
	}
	public void setQuantidadeHospede(Integer quantidadeHospede) {
		this.quantidadeHospede = quantidadeHospede;
	}
	public LocalDateTime getCheckin() {
		return checkin;
	}
	public void setCheckin(LocalDateTime checkin) {
		this.checkin = checkin;
	}
	public LocalDateTime getCheckout() {
		return checkout;
	}
	public void setCheckout(LocalDateTime checkout) {
		this.checkout = checkout;
	}
	public Integer getQuantidadeDiarias() {
		return quantidadeDiarias;
	}
	public void setQuantidadeDiarias(Integer quantidadeDiarias) {
		this.quantidadeDiarias = quantidadeDiarias;
	}
	public BigDecimal getValorDiaria() {
		return valorDiaria;
	}
	public void setValorDiaria(BigDecimal valorDiaria) {
		this.valorDiaria = valorDiaria;
	}
	public BigDecimal getTotalDiarias() {
		return totalDiarias;
	}
	public void setTotalDiarias(BigDecimal totalDiarias) {
		this.totalDiarias = totalDiarias;
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
	public String getObservacoes() {
		return observacoes;
	}
	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
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
	public List<ExtratoReserva> getExtratos() {
		return extratos;
	}
	public void setExtratos(List<ExtratoReserva> extratos) {
		this.extratos = extratos;
	}
	public List<HistoricoHospede> getHistoricos() {
		return historicos;
	}
	public void setHistoricos(List<HistoricoHospede> historicos) {
		this.historicos = historicos;
	}
                
}
