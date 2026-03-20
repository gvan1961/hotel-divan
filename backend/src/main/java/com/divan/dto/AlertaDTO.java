package com.divan.dto;

import java.time.LocalDateTime;

public class AlertaDTO {
    private String tipoAlerta;
    private String nivelGravidade;
    private String titulo;
    private String descricao;
    private String recomendacao;
    private Long apartamentoId;
    private String numeroApartamento;
    private String tipoApartamento;
    private Long reservaId;
    private String clienteNome;
    private String statusReserva;
    private LocalDateTime dataCheckin;
    private LocalDateTime dataCheckout;
    private Long horasAtraso;
    private Double totalPago;
    private Double totalReserva;
    private Double percentualPago;

    // Getters e Setters
    public String getTipoAlerta() { return tipoAlerta; }
    public void setTipoAlerta(String tipoAlerta) { this.tipoAlerta = tipoAlerta; }
    public String getNivelGravidade() { return nivelGravidade; }
    public void setNivelGravidade(String nivelGravidade) { this.nivelGravidade = nivelGravidade; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getRecomendacao() { return recomendacao; }
    public void setRecomendacao(String recomendacao) { this.recomendacao = recomendacao; }
    public Long getApartamentoId() { return apartamentoId; }
    public void setApartamentoId(Long apartamentoId) { this.apartamentoId = apartamentoId; }
    public String getNumeroApartamento() { return numeroApartamento; }
    public void setNumeroApartamento(String numeroApartamento) { this.numeroApartamento = numeroApartamento; }
    public String getTipoApartamento() { return tipoApartamento; }
    public void setTipoApartamento(String tipoApartamento) { this.tipoApartamento = tipoApartamento; }
    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long reservaId) { this.reservaId = reservaId; }
    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }
    public String getStatusReserva() { return statusReserva; }
    public void setStatusReserva(String statusReserva) { this.statusReserva = statusReserva; }
    public LocalDateTime getDataCheckin() { return dataCheckin; }
    public void setDataCheckin(LocalDateTime dataCheckin) { this.dataCheckin = dataCheckin; }
    public LocalDateTime getDataCheckout() { return dataCheckout; }
    public void setDataCheckout(LocalDateTime dataCheckout) { this.dataCheckout = dataCheckout; }
    public Long getHorasAtraso() { return horasAtraso; }
    public void setHorasAtraso(Long horasAtraso) { this.horasAtraso = horasAtraso; }
    public Double getTotalPago() { return totalPago; }
    public void setTotalPago(Double totalPago) { this.totalPago = totalPago; }
    public Double getTotalReserva() { return totalReserva; }
    public void setTotalReserva(Double totalReserva) { this.totalReserva = totalReserva; }
    public Double getPercentualPago() { return percentualPago; }
    public void setPercentualPago(Double percentualPago) { this.percentualPago = percentualPago; }
}