package com.divan.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "fechamento_caixa")
public class FechamentoCaixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 20)
    private String turno;

    @Column(name = "data_hora_abertura", nullable = false)
    private LocalDateTime dataHoraAbertura;

    @Column(name = "data_hora_fechamento")
    private LocalDateTime dataHoraFechamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCaixaEnum status;

    @Column(length = 500)
    private String observacoes;

    // ═══════════════════════════════════════════
    // CAMPOS DE TOTAIS (ADICIONADOS)
    // ═══════════════════════════════════════════
    
    @Column(name = "total_dinheiro", precision = 10, scale = 2)
    private BigDecimal totalDinheiro = BigDecimal.ZERO;

    @Column(name = "total_pix", precision = 10, scale = 2)
    private BigDecimal totalPix = BigDecimal.ZERO;

    @Column(name = "total_cartao_debito", precision = 10, scale = 2)
    private BigDecimal totalCartaoDebito = BigDecimal.ZERO;

    @Column(name = "total_cartao_credito", precision = 10, scale = 2)
    private BigDecimal totalCartaoCredito = BigDecimal.ZERO;

    @Column(name = "total_transferencia", precision = 10, scale = 2)
    private BigDecimal totalTransferencia = BigDecimal.ZERO;

    @Column(name = "total_faturado", precision = 10, scale = 2)
    private BigDecimal totalFaturado = BigDecimal.ZERO;

    @Column(name = "total_diarias", precision = 10, scale = 2)
    private BigDecimal totalDiarias = BigDecimal.ZERO;

    @Column(name = "total_produtos", precision = 10, scale = 2)
    private BigDecimal totalProdutos = BigDecimal.ZERO;

    @Column(name = "total_descontos", precision = 10, scale = 2)
    private BigDecimal totalDescontos = BigDecimal.ZERO;

    @Column(name = "total_estornos", precision = 10, scale = 2)
    private BigDecimal totalEstornos = BigDecimal.ZERO;

    @Column(name = "total_bruto", precision = 10, scale = 2)
    private BigDecimal totalBruto = BigDecimal.ZERO;

    @Column(name = "total_liquido", precision = 10, scale = 2)
    private BigDecimal totalLiquido = BigDecimal.ZERO;

    // ═══════════════════════════════════════════
    // ENUM DE STATUS
    // ═══════════════════════════════════════════
    
    public enum StatusCaixaEnum {
        ABERTO,
        FECHADO
    }

    // ═══════════════════════════════════════════
    // GETTERS E SETTERS
    // ═══════════════════════════════════════════
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
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

    public StatusCaixaEnum getStatus() {
        return status;
    }

    public void setStatus(StatusCaixaEnum status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    // GETTERS E SETTERS DOS TOTAIS
    
    public BigDecimal getTotalDinheiro() {
        return totalDinheiro;
    }

    public void setTotalDinheiro(BigDecimal totalDinheiro) {
        this.totalDinheiro = totalDinheiro;
    }

    public BigDecimal getTotalPix() {
        return totalPix;
    }

    public void setTotalPix(BigDecimal totalPix) {
        this.totalPix = totalPix;
    }

    public BigDecimal getTotalCartaoDebito() {
        return totalCartaoDebito;
    }

    public void setTotalCartaoDebito(BigDecimal totalCartaoDebito) {
        this.totalCartaoDebito = totalCartaoDebito;
    }

    public BigDecimal getTotalCartaoCredito() {
        return totalCartaoCredito;
    }

    public void setTotalCartaoCredito(BigDecimal totalCartaoCredito) {
        this.totalCartaoCredito = totalCartaoCredito;
    }

    public BigDecimal getTotalTransferencia() {
        return totalTransferencia;
    }

    public void setTotalTransferencia(BigDecimal totalTransferencia) {
        this.totalTransferencia = totalTransferencia;
    }

    public BigDecimal getTotalFaturado() {
        return totalFaturado;
    }

    public void setTotalFaturado(BigDecimal totalFaturado) {
        this.totalFaturado = totalFaturado;
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

    public BigDecimal getTotalDescontos() {
        return totalDescontos;
    }

    public void setTotalDescontos(BigDecimal totalDescontos) {
        this.totalDescontos = totalDescontos;
    }

    public BigDecimal getTotalEstornos() {
        return totalEstornos;
    }

    public void setTotalEstornos(BigDecimal totalEstornos) {
        this.totalEstornos = totalEstornos;
    }

    public BigDecimal getTotalBruto() {
        return totalBruto;
    }

    public void setTotalBruto(BigDecimal totalBruto) {
        this.totalBruto = totalBruto;
    }

    public BigDecimal getTotalLiquido() {
        return totalLiquido;
    }

    public void setTotalLiquido(BigDecimal totalLiquido) {
        this.totalLiquido = totalLiquido;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FechamentoCaixa other = (FechamentoCaixa) obj;
        return Objects.equals(id, other.id);
    }
}
