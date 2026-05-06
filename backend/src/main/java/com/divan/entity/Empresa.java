package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
    
    @NotBlank(message = "CNPJ é obrigatório")
    @Column(unique = true, nullable = false)
    private String cnpj;
    
    @NotBlank(message = "Contato é obrigatório")
    @Column(nullable = false, length = 100)
    private String contato;
    
    @Pattern(regexp = "(\\+\\d{1,3}\\s?)?\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", 
            message = "Celular deve estar no formato (XX) XXXXX-XXXX")
   private String celular;
    
    // IMPORTANTE: JsonIgnoreProperties evita o loop infinito
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("empresa")
    private List<Cliente> clientes;
    
    @Column(name = "contato_financeiro_nome", length = 100)
    @Size(max = 100, message = "Nome do contato deve ter no máximo 100 caracteres")
    private String contatoFinanceiroNome;

    @Column(name = "contato_financeiro_celular", length = 20)
    @Pattern(
            regexp = "^$|^\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}$|^\\d{10,11}$",
            message = "Celular do contato deve estar no formato (XX) XXXXX-XXXX ou conter 10/11 dígitos"
        )
        private String contatoFinanceiroCelular;

    @Column(name = "contato_financeiro_ddi", length = 5)
    private String contatoFinanceiroDdi = "55";

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

	public List<Cliente> getClientes() {
		return clientes;
	}

	public void setClientes(List<Cliente> clientes) {
		this.clientes = clientes;
	}
	
	

	public String getContatoFinanceiroNome() {
		return contatoFinanceiroNome;
	}

	public void setContatoFinanceiroNome(String contatoFinanceiroNome) {
		this.contatoFinanceiroNome = contatoFinanceiroNome;
	}

	public String getContatoFinanceiroCelular() {
		return contatoFinanceiroCelular;
	}

	public void setContatoFinanceiroCelular(String contatoFinanceiroCelular) {
		this.contatoFinanceiroCelular = contatoFinanceiroCelular;
	}

	public String getContatoFinanceiroDdi() {
		return contatoFinanceiroDdi;
	}

	public void setContatoFinanceiroDdi(String contatoFinanceiroDdi) {
		this.contatoFinanceiroDdi = contatoFinanceiroDdi;
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