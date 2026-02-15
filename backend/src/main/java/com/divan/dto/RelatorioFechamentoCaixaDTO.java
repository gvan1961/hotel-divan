package com.divan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class RelatorioFechamentoCaixaDTO {
    
    // INFORMAÇÕES DO CAIXA
    private Long caixaId;
    private String recepcionistaNome;
    private LocalDateTime dataHoraAbertura;
    private LocalDateTime dataHoraFechamento;
    private String turno;
    
    // MOVIMENTAÇÕES POR APARTAMENTO
    private List<MovimentacaoApartamentoDTO> movimentacoesReservas;
    
    // VENDAS AVULSAS
    private List<VendaAvulsaDTO> vendasAvulsas;
    private List<VendaAvulsaDTO> vendasAvulsasFaturadas;
    
    // RESUMO POR FORMA DE PAGAMENTO
    private ResumoPorFormaPagamento resumoReservas;
    private ResumoPorFormaPagamento resumoAvulsas;
    private ResumoPorFormaPagamento resumoGeral;
    
    // ═══════════════════════════════════════════
    // CLASSE INTERNA: RESUMO POR FORMA
    // ═══════════════════════════════════════════
    
    public static class ResumoPorFormaPagamento {
        private BigDecimal dinheiro;
        private BigDecimal pix;
        private BigDecimal cartaoDebito;
        private BigDecimal cartaoCredito;
        private BigDecimal transferencia;
        private BigDecimal faturado;
        private BigDecimal totalDescontos;
        private BigDecimal total;
        
        public ResumoPorFormaPagamento() {
            this.dinheiro = BigDecimal.ZERO;
            this.pix = BigDecimal.ZERO;
            this.cartaoDebito = BigDecimal.ZERO;
            this.cartaoCredito = BigDecimal.ZERO;
            this.transferencia = BigDecimal.ZERO;
            this.faturado = BigDecimal.ZERO;
            this.totalDescontos = BigDecimal.ZERO;
            this.total = BigDecimal.ZERO;
        }
        
        // Getters e Setters
        public BigDecimal getDinheiro() {
            return dinheiro;
        }
        public void setDinheiro(BigDecimal dinheiro) {
            this.dinheiro = dinheiro;
        }
        public BigDecimal getPix() {
            return pix;
        }
        public void setPix(BigDecimal pix) {
            this.pix = pix;
        }
        public BigDecimal getCartaoDebito() {
            return cartaoDebito;
        }
        public void setCartaoDebito(BigDecimal cartaoDebito) {
            this.cartaoDebito = cartaoDebito;
        }
        public BigDecimal getCartaoCredito() {
            return cartaoCredito;
        }
        public void setCartaoCredito(BigDecimal cartaoCredito) {
            this.cartaoCredito = cartaoCredito;
        }
        public BigDecimal getTransferencia() {
            return transferencia;
        }
        public void setTransferencia(BigDecimal transferencia) {
            this.transferencia = transferencia;
        }
        public BigDecimal getFaturado() {
            return faturado;
        }
        public void setFaturado(BigDecimal faturado) {
            this.faturado = faturado;
        }
        public BigDecimal getTotalDescontos() {
            return totalDescontos;
        }
        public void setTotalDescontos(BigDecimal totalDescontos) {
            this.totalDescontos = totalDescontos;
        }
        public BigDecimal getTotal() {
            return total;
        }
        public void setTotal(BigDecimal total) {
            this.total = total;
        }
        
        
    }
    
    // ═══════════════════════════════════════════
    // GETTERS E SETTERS PRINCIPAIS
    // ═══════════════════════════════════════════
    
    public Long getCaixaId() {
        return caixaId;
    }
    
    public void setCaixaId(Long caixaId) {
        this.caixaId = caixaId;
    }
    
    public String getRecepcionistaNome() {
        return recepcionistaNome;
    }
    
    public void setRecepcionistaNome(String recepcionistaNome) {
        this.recepcionistaNome = recepcionistaNome;
    }
    
    public LocalDateTime getDataHoraAbertura() {
        return dataHoraAbertura;
    }
    
    public void setDataHoraAbertura(LocalDateTime dataHoraAbertura) {
        this.dataHoraAbertura = dataHoraAbertura;
    }
    
    public LocalDateTime getDataHoraFechamento() {
        return dataHoraFechamento;
    }
    
    public void setDataHoraFechamento(LocalDateTime dataHoraFechamento) {
        this.dataHoraFechamento = dataHoraFechamento;
    }
    
    public String getTurno() {
        return turno;
    }
    
    public void setTurno(String turno) {
        this.turno = turno;
    }
    
    public List<MovimentacaoApartamentoDTO> getMovimentacoesReservas() {
        return movimentacoesReservas;
    }
    
    public void setMovimentacoesReservas(List<MovimentacaoApartamentoDTO> movimentacoesReservas) {
        this.movimentacoesReservas = movimentacoesReservas;
    }
    
    public List<VendaAvulsaDTO> getVendasAvulsas() {
        return vendasAvulsas;
    }
    
    public void setVendasAvulsas(List<VendaAvulsaDTO> vendasAvulsas) {
        this.vendasAvulsas = vendasAvulsas;
    }
    
    public ResumoPorFormaPagamento getResumoReservas() {
        return resumoReservas;
    }
    
    public void setResumoReservas(ResumoPorFormaPagamento resumoReservas) {
        this.resumoReservas = resumoReservas;
    }
    
    public ResumoPorFormaPagamento getResumoAvulsas() {
        return resumoAvulsas;
    }
    
    public void setResumoAvulsas(ResumoPorFormaPagamento resumoAvulsas) {
        this.resumoAvulsas = resumoAvulsas;
    }
    
    public ResumoPorFormaPagamento getResumoGeral() {
        return resumoGeral;
    }
    
    public void setResumoGeral(ResumoPorFormaPagamento resumoGeral) {
        this.resumoGeral = resumoGeral;
    }

	public List<VendaAvulsaDTO> getVendasAvulsasFaturadas() {
		return vendasAvulsasFaturadas;
	}

	public void setVendasAvulsasFaturadas(List<VendaAvulsaDTO> vendasAvulsasFaturadas) {
		this.vendasAvulsasFaturadas = vendasAvulsasFaturadas;
	}   
        
    
}
