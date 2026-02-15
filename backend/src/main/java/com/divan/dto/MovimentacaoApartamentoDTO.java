package com.divan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MovimentacaoApartamentoDTO {
    
    private String numeroApartamento;
    private String clienteNome;
    private Long reservaId;
    private Integer quantidadeHospedes;
    private List<PagamentoDetalhado> pagamentos;
    private List<DescontoDetalhado> descontos;
    private BigDecimal totalPagamentos;
    private BigDecimal totalDescontos;
    private BigDecimal totalFinal;
    
    // ═══════════════════════════════════════════
    // CLASSES INTERNAS
    // ═══════════════════════════════════════════
    
    public static class PagamentoDetalhado {
        private Long id;
        private String formaPagamento;
        private BigDecimal valor;
        private LocalDateTime dataHora;
        
        // Getters e Setters
        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
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
        public LocalDateTime getDataHora() {
            return dataHora;
        }
        public void setDataHora(LocalDateTime dataHora) {
            this.dataHora = dataHora;
        }
    }
    
    public static class DescontoDetalhado {
        private Long id;
        private BigDecimal valor;
        private String motivo;
        private LocalDateTime dataHora;
        
        // Getters e Setters
        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public BigDecimal getValor() {
            return valor;
        }
        public void setValor(BigDecimal valor) {
            this.valor = valor;
        }
        public String getMotivo() {
            return motivo;
        }
        public void setMotivo(String motivo) {
            this.motivo = motivo;
        }
        public LocalDateTime getDataHora() {
            return dataHora;
        }
        public void setDataHora(LocalDateTime dataHora) {
            this.dataHora = dataHora;
        }
    }
    
    // ═══════════════════════════════════════════
    // GETTERS E SETTERS
    // ═══════════════════════════════════════════
    
    public String getNumeroApartamento() {
        return numeroApartamento;
    }
    
    public void setNumeroApartamento(String numeroApartamento) {
        this.numeroApartamento = numeroApartamento;
    }
    
    public String getClienteNome() {
        return clienteNome;
    }
    
    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }
    
    public Long getReservaId() {
        return reservaId;
    }
    
    public void setReservaId(Long reservaId) {
        this.reservaId = reservaId;
    }
    
    public Integer getQuantidadeHospedes() {
        return quantidadeHospedes;
    }
    
    public void setQuantidadeHospedes(Integer quantidadeHospedes) {
        this.quantidadeHospedes = quantidadeHospedes;
    }
    
    public List<PagamentoDetalhado> getPagamentos() {
        return pagamentos;
    }
    
    public void setPagamentos(List<PagamentoDetalhado> pagamentos) {
        this.pagamentos = pagamentos;
    }
    
    public List<DescontoDetalhado> getDescontos() {
        return descontos;
    }
    
    public void setDescontos(List<DescontoDetalhado> descontos) {
        this.descontos = descontos;
    }
    
    public BigDecimal getTotalPagamentos() {
        return totalPagamentos;
    }
    
    public void setTotalPagamentos(BigDecimal totalPagamentos) {
        this.totalPagamentos = totalPagamentos;
    }
    
    public BigDecimal getTotalDescontos() {
        return totalDescontos;
    }
    
    public void setTotalDescontos(BigDecimal totalDescontos) {
        this.totalDescontos = totalDescontos;
    }
    
    public BigDecimal getTotalFinal() {
        return totalFinal;
    }
    
    public void setTotalFinal(BigDecimal totalFinal) {
        this.totalFinal = totalFinal;
    }
}
