package com.divan.dto;

public class ClienteResumoDTO {
    private Long id;
    private String nome;
    private String cpf;
    private String celular;
    private String ddi;
    private String celular2;
    private String ddi2;
    private String celularCompleto;
    private String celular2Completo;
    private String empresaNome;
    private Long empresaId;
    private String tipoCliente;
    private String classificacao;
    private Boolean fumante;
    private Boolean faceAtivo;
    private Boolean creditoAprovado;
    private Boolean autorizadoJantar;
    private Boolean menorDeIdade;
    private String responsavelNome;

    public ClienteResumoDTO() {}

    public ClienteResumoDTO(Long id, String nome, String cpf, String celular,
                             String ddi, String celular2, String ddi2,
                             String celularCompleto, String celular2Completo,
                             String empresaNome, Long empresaId, String tipoCliente,
                             String classificacao, Boolean fumante, Boolean faceAtivo,
                             Boolean creditoAprovado, Boolean autorizadoJantar,
                             Boolean menorDeIdade, String responsavelNome) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.celular = celular;
        this.ddi = ddi;
        this.celular2 = celular2;
        this.ddi2 = ddi2;
        this.celularCompleto = celularCompleto;
        this.celular2Completo = celular2Completo;
        this.empresaNome = empresaNome;
        this.empresaId = empresaId;
        this.tipoCliente = tipoCliente;
        this.classificacao = classificacao;
        this.fumante = fumante;
        this.faceAtivo = faceAtivo;
        this.creditoAprovado = creditoAprovado;
        this.autorizadoJantar = autorizadoJantar;
        this.menorDeIdade = menorDeIdade;
        this.responsavelNome = responsavelNome;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getCelularCompleto() { return celularCompleto; }
    public void setCelularCompleto(String celularCompleto) { this.celularCompleto = celularCompleto; }

    public String getCelular2Completo() { return celular2Completo; }
    public void setCelular2Completo(String celular2Completo) { this.celular2Completo = celular2Completo; }

    public String getEmpresaNome() { return empresaNome; }
    public void setEmpresaNome(String empresaNome) { this.empresaNome = empresaNome; }

    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }

    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }

    public String getClassificacao() { return classificacao; }
    public void setClassificacao(String classificacao) { this.classificacao = classificacao; }

    public Boolean getFumante() { return fumante; }
    public void setFumante(Boolean fumante) { this.fumante = fumante; }

    public Boolean getFaceAtivo() { return faceAtivo; }
    public void setFaceAtivo(Boolean faceAtivo) { this.faceAtivo = faceAtivo; }

    public Boolean getCreditoAprovado() { return creditoAprovado; }
    public void setCreditoAprovado(Boolean creditoAprovado) { this.creditoAprovado = creditoAprovado; }

    public Boolean getAutorizadoJantar() { return autorizadoJantar; }
    public void setAutorizadoJantar(Boolean autorizadoJantar) { this.autorizadoJantar = autorizadoJantar; }

    public Boolean getMenorDeIdade() { return menorDeIdade; }
    public void setMenorDeIdade(Boolean menorDeIdade) { this.menorDeIdade = menorDeIdade; }

    public String getResponsavelNome() { return responsavelNome; }
    public void setResponsavelNome(String responsavelNome) { this.responsavelNome = responsavelNome; }
}