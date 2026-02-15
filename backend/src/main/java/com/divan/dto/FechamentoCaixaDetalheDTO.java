package com.divan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FechamentoCaixaDetalheDTO {
    
    private Long id;
    private String tipo;
    private String descricao;
    private String apartamentoNumero;
    private Long reservaId;
    private BigDecimal valor;
    private String formaPagamento;
    private LocalDateTime dataHora;
    
    // GETTERS E SETTERS
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getApartamentoNumero() {
        return apartamentoNumero;
    }
    
    public void setApartamentoNumero(String apartamentoNumero) {
        this.apartamentoNumero = apartamentoNumero;
    }
    
    public Long getReservaId() {
        return reservaId;
    }
    
    public void setReservaId(Long reservaId) {
        this.reservaId = reservaId;
    }
    
    public BigDecimal getValor() {
        return valor;
    }
    
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    
    public String getFormaPagamento() {
        return formaPagamento;
    }
    
    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }
    
    public LocalDateTime getDataHora() {
        return dataHora;
    }
    
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
    
}
