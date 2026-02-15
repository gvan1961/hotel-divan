package com.divan.dto;

import jakarta.validation.constraints.NotBlank;

public class HospedeReservaDTO {
    
    private Long clienteId;  // Se já existe no banco
    
    @NotBlank(message = "Nome é obrigatório")
    private String nomeCompleto;
    
    private String cpf;
    private String telefone;
    private Boolean titular = false;
    private Boolean cadastrarNovo = false;  // Se deve criar novo cliente
    
    // Getters e Setters
    public Long getClienteId() {
        return clienteId;
    }
    
    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
    
    public String getNomeCompleto() {
        return nomeCompleto;
    }
    
    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }
    
    public String getCpf() {
        return cpf;
    }
    
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    
    public String getTelefone() {
        return telefone;
    }
    
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    
    public Boolean getTitular() {
        return titular;
    }
    
    public void setTitular(Boolean titular) {
        this.titular = titular;
    }
    
    public Boolean getCadastrarNovo() {
        return cadastrarNovo;
    }
    
    public void setCadastrarNovo(Boolean cadastrarNovo) {
        this.cadastrarNovo = cadastrarNovo;
    }
}
