package com.divan.dto;

import com.divan.entity.ContaAReceber.StatusContaEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


public class ContaAReceberDTO {
    private Long id;
    private Long reservaId;
    private String clienteNome;
    private String empresaNome;
    private BigDecimal valor;
    private BigDecimal valorPago;
    private BigDecimal saldo;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private StatusContaEnum status;
    private String descricao;
    private Integer diasVencido;
    
    private String numeroApartamento;
    private Integer quantidadeHospede;
    private Integer quantidadeDiaria;
    private BigDecimal totalDiaria;
    private BigDecimal totalConsumo;
    private BigDecimal totalHospedagem;
    private BigDecimal totalRecebido;
    private BigDecimal desconto;
    private BigDecimal totalApagar;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getReservaId() {
		return reservaId;
	}
	public void setReservaId(Long reservaId) {
		this.reservaId = reservaId;
	}
	public String getClienteNome() {
		return clienteNome;
	}
	public void setClienteNome(String clienteNome) {
		this.clienteNome = clienteNome;
	}
	public String getEmpresaNome() {
		return empresaNome;
	}
	public void setEmpresaNome(String empresaNome) {
		this.empresaNome = empresaNome;
	}
	public BigDecimal getValor() {
		return valor;
	}
	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}
	public BigDecimal getValorPago() {
		return valorPago;
	}
	public void setValorPago(BigDecimal valorPago) {
		this.valorPago = valorPago;
	}
	public BigDecimal getSaldo() {
		return saldo;
	}
	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}
	public LocalDate getDataVencimento() {
		return dataVencimento;
	}
	public void setDataVencimento(LocalDate dataVencimento) {
		this.dataVencimento = dataVencimento;
	}
	public LocalDate getDataPagamento() {
		return dataPagamento;
	}
	public void setDataPagamento(LocalDate dataPagamento) {
		this.dataPagamento = dataPagamento;
	}
	public StatusContaEnum getStatus() {
		return status;
	}
	public void setStatus(StatusContaEnum status) {
		this.status = status;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	public Integer getDiasVencido() {
		return diasVencido;
	}
	public void setDiasVencido(Integer diasVencido) {
		this.diasVencido = diasVencido;
	}
	public String getNumeroApartamento() {
		return numeroApartamento;
	}
	public void setNumeroApartamento(String numeroApartamento) {
		this.numeroApartamento = numeroApartamento;
	}
	public Integer getQuantidadeHospede() {
		return quantidadeHospede;
	}
	public void setQuantidadeHospede(Integer quantidadeHospede) {
		this.quantidadeHospede = quantidadeHospede;
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
	public BigDecimal getTotalConsumo() {
		return totalConsumo;
	}
	public void setTotalConsumo(BigDecimal totalConsumo) {
		this.totalConsumo = totalConsumo;
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
    
    
}
