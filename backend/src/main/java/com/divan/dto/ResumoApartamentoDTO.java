package com.divan.dto;

import java.math.BigDecimal;

public class ResumoApartamentoDTO {
    
    private String numeroApartamento;
    private Long reservaId;
    private String clienteNome;
    private BigDecimal totalDiarias;
    private BigDecimal totalProdutos;
    private BigDecimal totalPagamentos;
    private BigDecimal saldo;
    private String status;

    // GETTERS E SETTERS
    
    public String getNumeroApartamento() {
        return numeroApartamento;
    }

    public void setNumeroApartamento(String numeroApartamento) {
        this.numeroApartamento = numeroApartamento;
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

    public BigDecimal getTotalPagamentos() {
        return totalPagamentos;
    }

    public void setTotalPagamentos(BigDecimal totalPagamentos) {
        this.totalPagamentos = totalPagamentos;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
