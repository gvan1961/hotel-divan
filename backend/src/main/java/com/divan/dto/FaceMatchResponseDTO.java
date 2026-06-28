package com.divan.dto;

public class FaceMatchResponseDTO {
    private boolean reconhecido;
    private Long clienteId;
    private String nomeCliente;
    private String classificacao;
    private String fotoBase64;
    private String mensagem;
    private Long alertaId;
    private Boolean hospedadoAtualmente;
    
    // Getters e Setters
    public boolean isReconhecido() { return reconhecido; }
    public void setReconhecido(boolean reconhecido) { this.reconhecido = reconhecido; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
    public String getClassificacao() { return classificacao; }
    public void setClassificacao(String classificacao) { this.classificacao = classificacao; }
    public String getFotoBase64() { return fotoBase64; }
    public void setFotoBase64(String fotoBase64) { this.fotoBase64 = fotoBase64; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public Long getAlertaId() { return alertaId; }
    public void setAlertaId(Long alertaId) { this.alertaId = alertaId; }
    public Boolean getHospedadoAtualmente() { return hospedadoAtualmente; }
    public void setHospedadoAtualmente(Boolean hospedadoAtualmente) { 
        this.hospedadoAtualmente = hospedadoAtualmente; 
    }
}
