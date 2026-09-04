package com.divan.dto;

import com.divan.entity.ContaAReceber.StatusContaEnum;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ContaAReceberEdicaoDTO {
    private BigDecimal valor;
    private String descricao;
    private LocalDate dataVencimento;
    private StatusContaEnum status;
    private String motivo;
    private com.divan.entity.Pagamento.FormaPagamentoEnum formaPagamento;

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public StatusContaEnum getStatus() { return status; }
    public void setStatus(StatusContaEnum status) { this.status = status; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
	public com.divan.entity.Pagamento.FormaPagamentoEnum getFormaPagamento() {
		return formaPagamento;
	}
	public void setFormaPagamento(com.divan.entity.Pagamento.FormaPagamentoEnum formaPagamento) {
		this.formaPagamento = formaPagamento;
	}
    
}
