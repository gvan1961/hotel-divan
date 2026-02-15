package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CNPJ;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "empresas")

public class Empresa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome da empresa é obrigatório")
    @Column(nullable = false, length = 100)
    private String nomeEmpresa;
    
    @CNPJ(message = "CNPJ inválido")
    @Column(unique = true, nullable = false)
    private String cnpj;
    
    @NotBlank(message = "Contato é obrigatório")
    @Column(nullable = false, length = 100)
    private String contato;
    
    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message = "Celular deve estar no formato (XX) XXXXX-XXXX")
    @Column(nullable = false)
    private String celular;

    @Column(name = "autoriza_todos_jantar")
    private Boolean autorizaTodosJantar = false;
    // IMPORTANTE: JsonIgnoreProperties evita o loop infinito
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("empresa")
    private List<Cliente> clientes;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNomeEmpresa() {
		return nomeEmpresa;
	}

	public void setNomeEmpresa(String nomeEmpresa) {
		this.nomeEmpresa = nomeEmpresa;
	}

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	public String getContato() {
		return contato;
	}

	public void setContato(String contato) {
		this.contato = contato;
	}

	public String getCelular() {
		return celular;
	}

	public void setCelular(String celular) {
		this.celular = celular;
	}
	
	

	public Boolean getAutorizaTodosJantar() {
		return autorizaTodosJantar;
	}

	public void setAutorizaTodosJantar(Boolean autorizaTodosJantar) {
		this.autorizaTodosJantar = autorizaTodosJantar;
	}

	public List<Cliente> getClientes() {
		return clientes;
	}

	public void setClientes(List<Cliente> clientes) {
		this.clientes = clientes;
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
		Empresa other = (Empresa) obj;
		return Objects.equals(id, other.id);
	}   
    
}