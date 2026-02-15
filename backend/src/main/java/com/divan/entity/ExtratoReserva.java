package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "extratos_reserva")

public class ExtratoReserva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    
          
    @NotNull(message = "Reserva é obrigatória")
    @ManyToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    @JsonIgnore  // ✅ SUBSTITUA POR ISTO
    private Reserva reserva;   
    
    
    @NotNull(message = "Data/hora do lançamento é obrigatória")
    @Column(nullable = false)
    private LocalDateTime dataHoraLancamento;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLancamentoEnum statusLancamento;
    
    @Min(value = 0, message = "Quantidade não pode ser negativa")
    private Integer quantidade;
    
    // CORREÇÃO: Remover validação @DecimalMin para permitir valores negativos
    // Pagamentos precisam ser negativos para diminuir o saldo
    @Column(precision = 10, scale = 2)
    private BigDecimal valorUnitario;
    
    // CORREÇÃO: Remover validação @DecimalMin para permitir valores negativos
    // Pagamentos e estornos são negativos
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalLancamento;
    
    @Column(length = 200)
    private String descricao;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    private Produto produto;
    
    @Column(name = "nota_venda_id")
    private Long notaVendaId;
    
    public enum StatusLancamentoEnum {
        PRODUTO, DIARIA, PAGAMENTO, ESTORNO, PENDENTE
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Reserva getReserva() {
		return reserva;
	}

	public void setReserva(Reserva reserva) {
		this.reserva = reserva;
	}

	public LocalDateTime getDataHoraLancamento() {
		return dataHoraLancamento;
	}

	public void setDataHoraLancamento(LocalDateTime dataHoraLancamento) {
		this.dataHoraLancamento = dataHoraLancamento;
	}

	public StatusLancamentoEnum getStatusLancamento() {
		return statusLancamento;
	}

	public void setStatusLancamento(StatusLancamentoEnum statusLancamento) {
		this.statusLancamento = statusLancamento;
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

	public BigDecimal getTotalLancamento() {
		return totalLancamento;
	}

	public void setTotalLancamento(BigDecimal totalLancamento) {
		this.totalLancamento = totalLancamento;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Long getNotaVendaId() {
		return notaVendaId;
	}

	public void setNotaVendaId(Long notaVendaId) {
		this.notaVendaId = notaVendaId;
	}	

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
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
		ExtratoReserva other = (ExtratoReserva) obj;
		return Objects.equals(id, other.id);
	}
    
    
}
