package com.divan.dto;

public class AdicionarHospedeRequestDTO {
    private Long clienteId;
    private String nome;
    private String cpf;
    private String celular;
    private Boolean cadastrarNovo;

    // Getters e Setters
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
    
    public Boolean getCadastrarNovo() { return cadastrarNovo; }
    public void setCadastrarNovo(Boolean cadastrarNovo) { this.cadastrarNovo = cadastrarNovo; }
}
