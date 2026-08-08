package com.divan.dto;

import com.divan.entity.Vale.StatusVale;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ValeDTO {

    private Long id;
    private Long clienteId;
    private String clienteNome;
    private String clienteCpf;
    private BigDecimal valor;
    private String descricao;
    private LocalDateTime dataEmissao;
    private LocalDate dataVencimento;
    private LocalDateTime dataPagamento;
    private LocalDate dataConcessao;
    private String motivoCancelamento;
    private String observacao;
    private StatusVale status;
    private String tipoVale;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getClienteId() {
        return clienteId;
    }
    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
    public String getClienteNome() {
        return clienteNome;
    }
    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }
    public String getClienteCpf() {
        return clienteCpf;
    }
    public void setClienteCpf(String clienteCpf) {
        this.clienteCpf = clienteCpf;
    }
    public BigDecimal getValor() {
        return valor;
    }
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }
    public void setDataEmissao(LocalDateTime dataEmissao) {
        this.dataEmissao = dataEmissao;
    }
    public LocalDate getDataVencimento() {
        return dataVencimento;
    }
    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }
    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }
    public void setDataPagamento(LocalDateTime dataPagamento) {
        this.dataPagamento = dataPagamento;
    }
    public LocalDate getDataConcessao() {
        return dataConcessao;
    }
    public void setDataConcessao(LocalDate dataConcessao) {
        this.dataConcessao = dataConcessao;
    }
    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }
    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }
    public String getObservacao() {
        return observacao;
    }
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
    public StatusVale getStatus() {
        return status;
    }
    public void setStatus(StatusVale status) {
        this.status = status;
    }
    public String getTipoVale() {
        return tipoVale;
    }
    public void setTipoVale(String tipoVale) {
        this.tipoVale = tipoVale;
    }
}