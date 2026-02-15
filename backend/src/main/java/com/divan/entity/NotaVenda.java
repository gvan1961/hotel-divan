package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "notas_venda")

public class NotaVenda {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Data/hora da venda é obrigatória")
    @Column(nullable = false)
    private LocalDateTime dataHoraVenda;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVendaEnum tipoVenda;
    
    @Column(name = "observacao", length = 500)
    private String observacao;
    
    @ManyToOne
    @JoinColumn(name = "reserva_id")
    @JsonIgnore
    private Reserva reserva;
    
    // CORREÇÃO: Mudar de 0.01 para 0.0 para permitir zero temporariamente
    @DecimalMin(value = "0.0", message = "Total não pode ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    
    @OneToMany(mappedBy = "notaVenda", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("notaVenda")
    private List<ItemVenda> itens;
    
    public enum TipoVendaEnum {
        VISTA, APARTAMENTO, FATURADO, CONSUMO
    }
    
    public enum Status {
        ABERTA, FECHADA, CANCELADA,ESTORNADA
    }
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getDataHoraVenda() {
		return dataHoraVenda;
	}

	public void setDataHoraVenda(LocalDateTime dataHoraVenda) {
		this.dataHoraVenda = dataHoraVenda;
	}

	public TipoVendaEnum getTipoVenda() {
		return tipoVenda;
	}

	public void setTipoVenda(TipoVendaEnum tipoVenda) {
		this.tipoVenda = tipoVenda;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public Reserva getReserva() {
		return reserva;
	}

	public void setReserva(Reserva reserva) {
		this.reserva = reserva;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public List<ItemVenda> getItens() {
		return itens;
	}

	public void setItens(List<ItemVenda> itens) {
		this.itens = itens;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
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
		NotaVenda other = (NotaVenda) obj;
		return Objects.equals(id, other.id);
	}
    
    
}
