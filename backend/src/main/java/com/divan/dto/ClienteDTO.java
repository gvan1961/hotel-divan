package com.divan.dto;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CPF;



import java.time.LocalDate;

public class ClienteDTO {
    
    private Long id;
    
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    
    @CPF(message = "CPF inválido")
    private String cpf;
    
    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message = "Celular deve estar no formato (XX) XXXXX-XXXX")
    private String celular;
    
    private String ddi = "55";
    private String celular2;
    private String ddi2 = "55";
    
    private String endereco;
    
    private Boolean menorDeIdade;
    private Long responsavelId;
    private String responsavelNome;
    private String responsavelCpf;
    
   // @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP deve estar no formato XXXXX-XXX")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP deve ter 8 dígitos")
    private String cep;
    
    @PrePersist
    @PreUpdate
    private void normalizarCep() {
        if (cep != null) {
            // Remove tudo que não é número
            cep = cep.replaceAll("\\D", "");
            // Adiciona o traço: 57316-175
            if (cep.length() == 8) {
                cep = cep.substring(0, 5) + "-" + cep.substring(5);
            }
        }
    }
    
    
    
    private String cidade;
    private String estado;
    
    private Boolean creditoAprovado;
    private Boolean autorizadoJantar;
    private String tipoCliente;
    
    @NotNull(message = "Data de nascimento é obrigatória")
    private LocalDate dataNascimento;
    
    private Long empresaId;
    private String empresaNome;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getCpf() {
		return cpf;
	}
	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
	public String getCelular() {
		return celular;
	}
	public void setCelular(String celular) {
		this.celular = celular;
	}		
	
	public String getDdi() {
		return ddi;
	}
	public void setDdi(String ddi) {
		this.ddi = ddi;
	}
	public String getCelular2() {
		return celular2;
	}
	public void setCelular2(String celular2) {
		this.celular2 = celular2;
	}
	public String getDdi2() {
		return ddi2;
	}
	public void setDdi2(String ddi2) {
		this.ddi2 = ddi2;
	}
	public String getEndereco() {
		return endereco;
	}
	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}
	public String getCep() {
		return cep;
	}
	public void setCep(String cep) {
		this.cep = cep;
	}
	public String getCidade() {
		return cidade;
	}
	public void setCidade(String cidade) {
		this.cidade = cidade;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public LocalDate getDataNascimento() {
		return dataNascimento;
	}
	public void setDataNascimento(LocalDate dataNascimento) {
		this.dataNascimento = dataNascimento;
	}
	public Long getEmpresaId() {
		return empresaId;
	}
	public void setEmpresaId(Long empresaId) {
		this.empresaId = empresaId;
	}
	public String getEmpresaNome() {
		return empresaNome;
	}
	public void setEmpresaNome(String empresaNome) {
		this.empresaNome = empresaNome;
	}
	public Boolean getMenorDeIdade() {
		return menorDeIdade;
	}
	public void setMenorDeIdade(Boolean menorDeIdade) {
		this.menorDeIdade = menorDeIdade;
	}
	public Long getResponsavelId() {
		return responsavelId;
	}
	public void setResponsavelId(Long responsavelId) {
		this.responsavelId = responsavelId;
	}
	public String getResponsavelNome() {
		return responsavelNome;
	}
	public void setResponsavelNome(String responsavelNome) {
		this.responsavelNome = responsavelNome;
	}
	public String getResponsavelCpf() {
		return responsavelCpf;
	}
	public void setResponsavelCpf(String responsavelCpf) {
		this.responsavelCpf = responsavelCpf;
	}
	public Boolean getCreditoAprovado() {
		return creditoAprovado;
	}
	public void setCreditoAprovado(Boolean creditoAprovado) {
		this.creditoAprovado = creditoAprovado;
	}
	public Boolean getAutorizadoJantar() {
		return autorizadoJantar;
	}
	public void setAutorizadoJantar(Boolean autorizadoJantar) {
		this.autorizadoJantar = autorizadoJantar;
	}
	public String getTipoCliente() {
		return tipoCliente;
	}
	public void setTipoCliente(String tipoCliente) {
		this.tipoCliente = tipoCliente;
	}   
			        
}
