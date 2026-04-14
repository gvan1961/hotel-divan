package com.divan.dto;

import java.time.LocalDate;

public class ClienteRequestDTO {

    private String nome;
    private String cpf;
    private String celular;
    private String ddi = "+55";
    private String celular2;
    private String ddi2 = "+55";
    private String endereco;
    private String cep;
    private String cidade;
    private String estado;
    private LocalDate dataNascimento;
    private Long empresaId;
    private Boolean creditoAprovado;
    private String tipoCliente;
    private Boolean autorizadoJantar;
    private Boolean menorDeIdade;
    private Long responsavelId;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
    public String getDdi() { return ddi; }
    public void setDdi(String ddi) { this.ddi = ddi; }
    public String getCelular2() { return celular2; }
    public void setCelular2(String celular2) { this.celular2 = celular2; }
    public String getDdi2() { return ddi2; }
    public void setDdi2(String ddi2) { this.ddi2 = ddi2; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public Boolean getCreditoAprovado() { return creditoAprovado; }
    public void setCreditoAprovado(Boolean creditoAprovado) { this.creditoAprovado = creditoAprovado; }
    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    public Boolean getAutorizadoJantar() { return autorizadoJantar; }
    public void setAutorizadoJantar(Boolean autorizadoJantar) { this.autorizadoJantar = autorizadoJantar; }
    public Boolean getMenorDeIdade() { return menorDeIdade; }
    public void setMenorDeIdade(Boolean menorDeIdade) { this.menorDeIdade = menorDeIdade; }
    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }
}
