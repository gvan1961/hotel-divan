package com.divan.entity;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "faixa_consumo_agua_empresa")
public class FaixaConsumoAguaEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "qtd_hospedes", nullable = false)
    private Integer qtdHospedes;

    @Column(name = "valor_limite_diario", nullable = false)
    private BigDecimal valorLimiteDiario;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Integer getQtdHospedes() {
		return qtdHospedes;
	}

	public void setQtdHospedes(Integer qtdHospedes) {
		this.qtdHospedes = qtdHospedes;
	}

	public BigDecimal getValorLimiteDiario() {
		return valorLimiteDiario;
	}

	public void setValorLimiteDiario(BigDecimal valorLimiteDiario) {
		this.valorLimiteDiario = valorLimiteDiario;
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
		FaixaConsumoAguaEmpresa other = (FaixaConsumoAguaEmpresa) obj;
		return Objects.equals(id, other.id);
	}	
	
    
}
