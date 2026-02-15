package com.divan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "item_reserva")
public class ItemReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caixa_id")
    private FechamentoCaixa caixa;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false)
    private Integer quantidade = 1;

    @Column(name = "valor_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorUnitario;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(length = 50)
    private String tipo = "PRODUTO"; // PRODUTO, SERVICO, OUTRO
    
    

    public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
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
		ItemReserva other = (ItemReserva) obj;
		return Objects.equals(id, other.id);
	}



	public Reserva getReserva() {
		return reserva;
	}



	public void setReserva(Reserva reserva) {
		this.reserva = reserva;
	}



	public Produto getProduto() {
		return produto;
	}



	public void setProduto(Produto produto) {
		this.produto = produto;
	}



	public FechamentoCaixa getCaixa() {
		return caixa;
	}



	public void setCaixa(FechamentoCaixa caixa) {
		this.caixa = caixa;
	}



	public String getDescricao() {
		return descricao;
	}



	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}



	public Integer getQuantidade() {
		return quantidade;
	}



	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}



	public BigDecimal getValorUnitario() {
		return valorUnitario;
	}



	public void setValorUnitario(BigDecimal valorUnitario) {
		this.valorUnitario = valorUnitario;
	}



	public BigDecimal getValorTotal() {
		return valorTotal;
	}



	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}



	public LocalDateTime getDataHora() {
		return dataHora;
	}



	public void setDataHora(LocalDateTime dataHora) {
		this.dataHora = dataHora;
	}



	public Long getUsuarioId() {
		return usuarioId;
	}



	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}



	public String getTipo() {
		return tipo;
	}



	public void setTipo(String tipo) {
		this.tipo = tipo;
	}



	// ✅ Método auxiliar para calcular valor total
    @PrePersist
    @PreUpdate
    public void calcularValorTotal() {
        if (this.quantidade != null && this.valorUnitario != null) {
            this.valorTotal = this.valorUnitario.multiply(
                BigDecimal.valueOf(this.quantidade)
            );
        }
    }
}
