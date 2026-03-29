package com.divan.entity;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;


@Entity
@Table(name = "deposito_provisorio_item")
public class DepositoProvisorioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "deposito_id", nullable = false)
    private DepositoProvisorio deposito;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    @JsonIgnoreProperties({"itensVenda", "categoria"})
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false)
    private Integer quantidadeDistribuida;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DepositoProvisorio getDeposito() {
		return deposito;
	}

	public void setDeposito(DepositoProvisorio deposito) {
		this.deposito = deposito;
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

	public Integer getQuantidadeDistribuida() {
		return quantidadeDistribuida;
	}

	public void setQuantidadeDistribuida(Integer quantidadeDistribuida) {
		this.quantidadeDistribuida = quantidadeDistribuida;
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
		DepositoProvisorioItem other = (DepositoProvisorioItem) obj;
		return Objects.equals(id, other.id);
	}
    
    
}
