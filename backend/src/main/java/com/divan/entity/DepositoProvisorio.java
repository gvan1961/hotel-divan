package com.divan.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "deposito_provisorio")
public class DepositoProvisorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"perfis", "permissoes", "password"})
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDeposito status;

    @OneToMany(mappedBy = "deposito", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("deposito")
    private List<DepositoProvisorioItem> itens;

    public enum StatusDeposito {
        ABERTO, FECHADO
    }

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

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}

	public StatusDeposito getStatus() {
		return status;
	}

	public void setStatus(StatusDeposito status) {
		this.status = status;
	}

	public List<DepositoProvisorioItem> getItens() {
		return itens;
	}

	public void setItens(List<DepositoProvisorioItem> itens) {
		this.itens = itens;
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
		DepositoProvisorio other = (DepositoProvisorio) obj;
		return Objects.equals(id, other.id);
	}
	    
  }