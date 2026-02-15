package com.divan.dto;

import com.divan.entity.Cliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public class ClienteRequestDTO {
    
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    
    @CPF(message = "CPF inválido")
    private String cpf;
    
    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message = "Celular deve estar no formato (XX) XXXXX-XXXX")
    private String celular;
    
    private String endereco;
    
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP deve estar no formato XXXXX-XXX")
    private String cep;
    
    private String cidade;
    private String estado;
    
    @NotNull(message = "Data de nascimento é obrigatória")
    private LocalDate dataNascimento;
    
    private Long empresaId;
    private Boolean creditoAprovado;
    private Boolean autorizadoJantar;
    
    // ⭐ NOVO CAMPO - Tipo de Cliente
    private Cliente.TipoCliente tipoCliente;
    
    // GETTERS E SETTERS
    
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
    
    // ⭐ GETTER E SETTER DO TIPO CLIENTE
    public Cliente.TipoCliente getTipoCliente() {
        return tipoCliente;
    }
    
    public void setTipoCliente(Cliente.TipoCliente tipoCliente) {
        this.tipoCliente = tipoCliente;
    }
}