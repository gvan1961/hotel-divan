package com.divan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VendaAvulsaDTO {
    
    private Long notaVendaId;
    private LocalDateTime dataHora;
    private String formaPagamento;
    private BigDecimal valor;
    private String descricao;
    private String clienteNome;
    
    public VendaAvulsaDTO() {}
    
    // Getters e Setters
    public Long getNotaVendaId() {
        return notaVendaId;
    }
    
    public void setNotaVendaId(Long notaVendaId) {
        this.notaVendaId = notaVendaId;
    }
    
    public LocalDateTime getDataHora() {
        return dataHora;
    }
    
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
    
    public String getFormaPagamento() {
        return formaPagamento;
    }
    
    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }
    
    public BigDecimal getValor() {
        return valor;
    }
    
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

	public String getClienteNome() {
		return clienteNome;
	}

	public void setClienteNome(String clienteNome) {
		this.clienteNome = clienteNome;
	}
    
    
}
