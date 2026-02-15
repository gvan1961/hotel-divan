package com.divan.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_movimentacao_caixa")
public class LogMovimentacaoCaixa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "caixa_id", nullable = false)
    private FechamentoCaixa caixa;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(name = "tipo_operacao", length = 50, nullable = false)
    private String tipoOperacao; // PAGAMENTO, DESCONTO, VENDA, etc
    
    @Column(name = "descricao", length = 500)
    private String descricao;
    
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;
    
    @Column(name = "reserva_id")
    private Long reservaId;
    
    @Column(name = "pagamento_id")
    private Long pagamentoId;
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public FechamentoCaixa getCaixa() {
        return caixa;
    }
    
    public void setCaixa(FechamentoCaixa caixa) {
        this.caixa = caixa;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public String getTipoOperacao() {
        return tipoOperacao;
    }
    
    public void setTipoOperacao(String tipoOperacao) {
        this.tipoOperacao = tipoOperacao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public LocalDateTime getDataHora() {
        return dataHora;
    }
    
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
    
    public Long getReservaId() {
        return reservaId;
    }
    
    public void setReservaId(Long reservaId) {
        this.reservaId = reservaId;
    }
    
    public Long getPagamentoId() {
        return pagamentoId;
    }
    
    public void setPagamentoId(Long pagamentoId) {
        this.pagamentoId = pagamentoId;
    }
}