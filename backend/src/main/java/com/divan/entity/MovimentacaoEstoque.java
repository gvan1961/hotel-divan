package com.divan.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes_estoque")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    @Column(name = "quantidade_anterior")
    private Integer quantidadeAnterior;

    @Column(name = "quantidade_movimentada", nullable = false)
    private Integer quantidadeMovimentada;

    @Column(name = "quantidade_nova")
    private Integer quantidadeNova;

    @Column(name = "valor_unitario", precision = 10, scale = 2)
    private BigDecimal valorUnitario;

    @Column(length = 200)
    private String motivo;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    public enum TipoMovimentacao {
        ENTRADA, SAIDA, ACERTO
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public TipoMovimentacao getTipo() { return tipo; }
    public void setTipo(TipoMovimentacao tipo) { this.tipo = tipo; }
    public Integer getQuantidadeAnterior() { return quantidadeAnterior; }
    public void setQuantidadeAnterior(Integer quantidadeAnterior) { this.quantidadeAnterior = quantidadeAnterior; }
    public Integer getQuantidadeMovimentada() { return quantidadeMovimentada; }
    public void setQuantidadeMovimentada(Integer quantidadeMovimentada) { this.quantidadeMovimentada = quantidadeMovimentada; }
    public Integer getQuantidadeNova() { return quantidadeNova; }
    public void setQuantidadeNova(Integer quantidadeNova) { this.quantidadeNova = quantidadeNova; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(BigDecimal valorUnitario) { this.valorUnitario = valorUnitario; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Fornecedor getFornecedor() { return fornecedor; }
    public void setFornecedor(Fornecedor fornecedor) { this.fornecedor = fornecedor; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}