package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "reservas")

public class Reserva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Apartamento é obrigatório")
    @ManyToOne
    @JoinColumn(name = "apartamento_id", nullable = false)
    @JsonIgnoreProperties({"reservas", "tipoApartamento"})
    private Apartamento apartamento;
    
    @NotNull(message = "Cliente é obrigatório")
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({"reservas", "empresa"})
    private Cliente cliente;
    
    @Min(value = 1, message = "Quantidade de hóspedes deve ser no mínimo 1")
    @Column(nullable = false)
    private Integer quantidadeHospede;
    
    @NotNull(message = "Diária é obrigatória")
    @ManyToOne
    @JoinColumn(name = "diaria_id", nullable = false)
    @JsonIgnoreProperties({"reservas", "tipoApartamento"})
    private Diaria diaria;
    
    @NotNull(message = "Data de check-in é obrigatória")
    @Column(nullable = false)
    private LocalDateTime dataCheckin;
    
    @NotNull(message = "Data de check-out é obrigatória")
    @Column(nullable = false)
    private LocalDateTime dataCheckout;
    
    private LocalDateTime dataCheckoutReal;
    
    @Min(value = 1, message = "Quantidade de diárias deve ser no mínimo 1")
    @Column(nullable = false)
    private Integer quantidadeDiaria;
    
       
    @DecimalMin(value = "0.0", message = "Total de diária não pode ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDiaria = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Total de produto não pode ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalProduto = BigDecimal.ZERO;
    
    @Column(length = 500)
    private String observacoes;
    
    @DecimalMin(value = "0.0", message = "Total de hospedagem não pode ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalHospedagem = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Total recebido não pode ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalRecebido = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Desconto não pode ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal desconto = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalApagar = BigDecimal.ZERO;    
       
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusReservaEnum status;
    
    
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("reserva")
    private List<ExtratoReserva> extratos;
    
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("reserva")
    private List<HistoricoHospede> historicos;
    
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("reserva")
    private List<NotaVenda> notasVenda;     
         
    public enum StatusReservaEnum {
        ATIVA, CANCELADA, FINALIZADA, PRE_RESERVA
    }

    
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Apartamento getApartamento() {
		return apartamento;
	}

	public void setApartamento(Apartamento apartamento) {
		this.apartamento = apartamento;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Integer getQuantidadeHospede() {
		return quantidadeHospede;
	}

	public void setQuantidadeHospede(Integer quantidadeHospede) {
		this.quantidadeHospede = quantidadeHospede;
	}

	public Diaria getDiaria() {
		return diaria;
	}

	public void setDiaria(Diaria diaria) {
		this.diaria = diaria;
	}

	public LocalDateTime getDataCheckin() {
		return dataCheckin;
	}

	public void setDataCheckin(LocalDateTime dataCheckin) {
		this.dataCheckin = dataCheckin;
	}

	public LocalDateTime getDataCheckout() {
		return dataCheckout;
	}

	public void setDataCheckout(LocalDateTime dataCheckout) {
		this.dataCheckout = dataCheckout;
	}

	public LocalDateTime getDataCheckoutReal() {
		return dataCheckoutReal;
	}

	public void setDataCheckoutReal(LocalDateTime dataCheckoutReal) {
		this.dataCheckoutReal = dataCheckoutReal;
	}

	public Integer getQuantidadeDiaria() {
		return quantidadeDiaria;
	}

	public void setQuantidadeDiaria(Integer quantidadeDiaria) {
		this.quantidadeDiaria = quantidadeDiaria;
	}

	public BigDecimal getTotalDiaria() {
		return totalDiaria;
	}

	public void setTotalDiaria(BigDecimal totalDiaria) {
		this.totalDiaria = totalDiaria;
	}

	public BigDecimal getTotalProduto() {
		return totalProduto;
	}

	public void setTotalProduto(BigDecimal totalProduto) {
		this.totalProduto = totalProduto;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}

	public BigDecimal getTotalHospedagem() {
		return totalHospedagem;
	}

	public void setTotalHospedagem(BigDecimal totalHospedagem) {
		this.totalHospedagem = totalHospedagem;
	}

	public BigDecimal getTotalRecebido() {
		return totalRecebido;
	}

	public void setTotalRecebido(BigDecimal totalRecebido) {
		this.totalRecebido = totalRecebido;
	}

	public BigDecimal getDesconto() {
		return desconto;
	}

	public void setDesconto(BigDecimal desconto) {
		this.desconto = desconto;
	}

	public BigDecimal getTotalApagar() {
		return totalApagar;
	}

	public void setTotalApagar(BigDecimal totalApagar) {
		this.totalApagar = totalApagar;
	}

	public StatusReservaEnum getStatus() {
		return status;
	}

	public void setStatus(StatusReservaEnum status) {
		this.status = status;
	}

	public List<ExtratoReserva> getExtratos() {
		return extratos;
	}

	public void setExtratos(List<ExtratoReserva> extratos) {
		this.extratos = extratos;
	}

	public List<HistoricoHospede> getHistoricos() {
		return historicos;
	}

	public void setHistoricos(List<HistoricoHospede> historicos) {
		this.historicos = historicos;
	}

	public List<NotaVenda> getNotasVenda() {
		return notasVenda;
	}

	public void setNotasVenda(List<NotaVenda> notasVenda) {
		this.notasVenda = notasVenda;
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
		Reserva other = (Reserva) obj;
		return Objects.equals(id, other.id);
	}
    
	
    
}


//@OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
//private List<NotaVenda> notasVenda;
