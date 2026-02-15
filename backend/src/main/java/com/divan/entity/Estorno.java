package com.divan.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "estornos")

public class Estorno {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;
    
    @ManyToOne
    @JoinColumn(name = "nota_venda_id")
    private NotaVenda notaVenda;
    
    @ManyToOne
    @JoinColumn(name = "extrato_id")
    private ExtratoReserva extratoOriginal;
    
        
    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = true) // ✅ PERMITE NULL
    private Produto produto;
       
    
    @Column(nullable = false)
    private Integer quantidade;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorUnitario;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;
    
    @Column(nullable = false, length = 500)
    private String motivo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEstornoEnum tipoEstorno;
    
    @Column(nullable = false)
    private LocalDateTime dataHoraEstorno;
    
    private String usuario; // Quem fez o estorno
    
    public enum TipoEstornoEnum {
        RESERVA_APARTAMENTO,  // Estorno em consumo de apartamento
        VENDA_VISTA,          // Estorno em venda à vista
        VENDA_FATURADA        // Estorno em venda faturada
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

	public NotaVenda getNotaVenda() {
		return notaVenda;
	}

	public void setNotaVenda(NotaVenda notaVenda) {
		this.notaVenda = notaVenda;
	}

	public ExtratoReserva getExtratoOriginal() {
		return extratoOriginal;
	}

	public void setExtratoOriginal(ExtratoReserva extratoOriginal) {
		this.extratoOriginal = extratoOriginal;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
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

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public TipoEstornoEnum getTipoEstorno() {
		return tipoEstorno;
	}

	public void setTipoEstorno(TipoEstornoEnum tipoEstorno) {
		this.tipoEstorno = tipoEstorno;
	}

	public LocalDateTime getDataHoraEstorno() {
		return dataHoraEstorno;
	}

	public void setDataHoraEstorno(LocalDateTime dataHoraEstorno) {
		this.dataHoraEstorno = dataHoraEstorno;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
    
    
}
