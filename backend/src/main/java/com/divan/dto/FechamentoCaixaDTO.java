package com.divan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class FechamentoCaixaDTO {

    private Long id;
    private Long usuarioId;
    private String usuarioNome;
    private LocalDateTime dataHoraAbertura;
    private LocalDateTime dataHoraFechamento;
    private String status;
    private String turno;
    private String observacoes;
    
    // TOTAIS GERAIS
    private BigDecimal totalDiarias;
    private BigDecimal totalProdutos;
    private BigDecimal totalDescontos;
    private BigDecimal totalEstornos;
    private BigDecimal totalBruto;
    private BigDecimal totalLiquido;
    
    // FORMAS DE PAGAMENTO
    private BigDecimal totalDinheiro;
    private BigDecimal totalPix;
    private BigDecimal totalCartaoDebito;
    private BigDecimal totalCartaoCredito;
    private BigDecimal totalTransferencia;
    private BigDecimal totalFaturado;
    
    // QUANTIDADES
    private Integer quantidadeCheckins;
    private Integer quantidadeCheckouts;
    private Integer quantidadeVendas;
    private Integer quantidadeReservas;
    
    // DETALHES
    private List<FechamentoCaixaDetalheDTO> detalhes;
    
    // RESUMO APARTAMENTOS
    private List<ResumoApartamentoDTO> resumoApartamentos;

    // GETTERS E SETTERS
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }

    public LocalDateTime getDataHoraAbertura() {
        return dataHoraAbertura;
    }

    public void setDataHoraAbertura(LocalDateTime dataHoraAbertura) {
        this.dataHoraAbertura = dataHoraAbertura;
    }

    public LocalDateTime getDataHoraFechamento() {
        return dataHoraFechamento;
    }

    public void setDataHoraFechamento(LocalDateTime dataHoraFechamento) {
        this.dataHoraFechamento = dataHoraFechamento;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public BigDecimal getTotalDiarias() {
        return totalDiarias;
    }

    public void setTotalDiarias(BigDecimal totalDiarias) {
        this.totalDiarias = totalDiarias;
    }

    public BigDecimal getTotalProdutos() {
        return totalProdutos;
    }

    public void setTotalProdutos(BigDecimal totalProdutos) {
        this.totalProdutos = totalProdutos;
    }

    public BigDecimal getTotalDescontos() {
        return totalDescontos;
    }

    public void setTotalDescontos(BigDecimal totalDescontos) {
        this.totalDescontos = totalDescontos;
    }

    public BigDecimal getTotalEstornos() {
        return totalEstornos;
    }

    public void setTotalEstornos(BigDecimal totalEstornos) {
        this.totalEstornos = totalEstornos;
    }

    public BigDecimal getTotalBruto() {
        return totalBruto;
    }

    public void setTotalBruto(BigDecimal totalBruto) {
        this.totalBruto = totalBruto;
    }

    public BigDecimal getTotalLiquido() {
        return totalLiquido;
    }

    public void setTotalLiquido(BigDecimal totalLiquido) {
        this.totalLiquido = totalLiquido;
    }

    public BigDecimal getTotalDinheiro() {
        return totalDinheiro;
    }

    public void setTotalDinheiro(BigDecimal totalDinheiro) {
        this.totalDinheiro = totalDinheiro;
    }

    public BigDecimal getTotalPix() {
        return totalPix;
    }

    public void setTotalPix(BigDecimal totalPix) {
        this.totalPix = totalPix;
    }

    public BigDecimal getTotalCartaoDebito() {
        return totalCartaoDebito;
    }

    public void setTotalCartaoDebito(BigDecimal totalCartaoDebito) {
        this.totalCartaoDebito = totalCartaoDebito;
    }

    public BigDecimal getTotalCartaoCredito() {
        return totalCartaoCredito;
    }

    public void setTotalCartaoCredito(BigDecimal totalCartaoCredito) {
        this.totalCartaoCredito = totalCartaoCredito;
    }

    public BigDecimal getTotalTransferencia() {
        return totalTransferencia;
    }

    public void setTotalTransferencia(BigDecimal totalTransferencia) {
        this.totalTransferencia = totalTransferencia;
    }

    public BigDecimal getTotalFaturado() {
        return totalFaturado;
    }

    public void setTotalFaturado(BigDecimal totalFaturado) {
        this.totalFaturado = totalFaturado;
    }

    public Integer getQuantidadeCheckins() {
        return quantidadeCheckins;
    }

    public void setQuantidadeCheckins(Integer quantidadeCheckins) {
        this.quantidadeCheckins = quantidadeCheckins;
    }

    public Integer getQuantidadeCheckouts() {
        return quantidadeCheckouts;
    }

    public void setQuantidadeCheckouts(Integer quantidadeCheckouts) {
        this.quantidadeCheckouts = quantidadeCheckouts;
    }

    public Integer getQuantidadeVendas() {
        return quantidadeVendas;
    }

    public void setQuantidadeVendas(Integer quantidadeVendas) {
        this.quantidadeVendas = quantidadeVendas;
    }

    public Integer getQuantidadeReservas() {
        return quantidadeReservas;
    }

    public void setQuantidadeReservas(Integer quantidadeReservas) {
        this.quantidadeReservas = quantidadeReservas;
    }

    public List<FechamentoCaixaDetalheDTO> getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(List<FechamentoCaixaDetalheDTO> detalhes) {
        this.detalhes = detalhes;
    }

    public List<ResumoApartamentoDTO> getResumoApartamentos() {
        return resumoApartamentos;
    }

    public void setResumoApartamentos(List<ResumoApartamentoDTO> resumoApartamentos) {
        this.resumoApartamentos = resumoApartamentos;
    }
}