package com.divan.dto;

import com.divan.entity.Vale;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ValeResponse {
    private Long id;
    private Long clienteId;
    private String clienteNome;
    private String clienteCpf;
    private LocalDate dataConcessao;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private Vale.TipoVale tipoVale;
    private String tipoValeDescricao;
    private BigDecimal valor;
    private Vale.StatusVale status;
    private String statusDescricao;
    private String observacao;
    private String motivoCancelamento;
    private LocalDateTime dataCriacao;
    private String criadoPor;

    public static ValeResponse fromEntity(Vale vale) {
        ValeResponse response = new ValeResponse();
        response.setId(vale.getId());
        response.setClienteId(vale.getCliente().getId());
        response.setClienteNome(vale.getCliente().getNome());
        response.setClienteCpf(vale.getCliente().getCpf());
        response.setDataConcessao(vale.getDataConcessao());
        response.setDataVencimento(vale.getDataVencimento());
        response.setDataPagamento(vale.getDataPagamento());
        response.setTipoVale(vale.getTipoVale());
        response.setTipoValeDescricao(vale.getTipoVale().getDescricao());
        response.setValor(vale.getValor());
        response.setStatus(vale.getStatus());
        response.setStatusDescricao(vale.getStatus().getDescricao());
        response.setObservacao(vale.getObservacao());
        response.setMotivoCancelamento(vale.getMotivoCancelamento());
        response.setDataCriacao(vale.getDataCriacao());
        response.setCriadoPor(vale.getCriadoPor());
        return response;
        
        
    }

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

	public LocalDate getDataConcessao() {
		return dataConcessao;
	}

	public void setDataConcessao(LocalDate dataConcessao) {
		this.dataConcessao = dataConcessao;
	}

	public LocalDate getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(LocalDate dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public LocalDate getDataPagamento() {
		return dataPagamento;
	}

	public void setDataPagamento(LocalDate dataPagamento) {
		this.dataPagamento = dataPagamento;
	}

	public Vale.TipoVale getTipoVale() {
		return tipoVale;
	}

	public void setTipoVale(Vale.TipoVale tipoVale) {
		this.tipoVale = tipoVale;
	}

	public String getTipoValeDescricao() {
		return tipoValeDescricao;
	}

	public void setTipoValeDescricao(String tipoValeDescricao) {
		this.tipoValeDescricao = tipoValeDescricao;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public Vale.StatusVale getStatus() {
		return status;
	}

	public void setStatus(Vale.StatusVale status) {
		this.status = status;
	}

	public String getStatusDescricao() {
		return statusDescricao;
	}

	public void setStatusDescricao(String statusDescricao) {
		this.statusDescricao = statusDescricao;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public String getMotivoCancelamento() {
		return motivoCancelamento;
	}

	public void setMotivoCancelamento(String motivoCancelamento) {
		this.motivoCancelamento = motivoCancelamento;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(LocalDateTime dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public String getCriadoPor() {
		return criadoPor;
	}

	public void setCriadoPor(String criadoPor) {
		this.criadoPor = criadoPor;
	}
        
}
