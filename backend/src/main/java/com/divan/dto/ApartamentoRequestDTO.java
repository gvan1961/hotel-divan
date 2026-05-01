package com.divan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ApartamentoRequestDTO {

    @NotBlank(message = "Número do apartamento é obrigatório")
    private String numeroApartamento;

    @NotNull(message = "Tipo de apartamento é obrigatório")
    private Long tipoApartamentoId;

    @NotNull(message = "Capacidade é obrigatória")
    @Min(value = 1, message = "Capacidade deve ser no mínimo 1")
    private Integer capacidade;

    @NotBlank(message = "Descrição das camas é obrigatória")
    private String camasDoApartamento;

    private String tv;

    // ✅ NOVO
    private Boolean temCamaDeCasal;

    // Construtores
    public ApartamentoRequestDTO() {
    }

    public ApartamentoRequestDTO(String numeroApartamento, Long tipoApartamentoId, Integer capacidade,
                                  String camasDoApartamento, String tv, Boolean temCamaDeCasal) {
        this.numeroApartamento = numeroApartamento;
        this.tipoApartamentoId = tipoApartamentoId;
        this.capacidade = capacidade;
        this.camasDoApartamento = camasDoApartamento;
        this.tv = tv;
        this.temCamaDeCasal = temCamaDeCasal;
    }

    // Getters e Setters
    public String getNumeroApartamento() {
        return numeroApartamento;
    }

    public void setNumeroApartamento(String numeroApartamento) {
        this.numeroApartamento = numeroApartamento;
    }

    public Long getTipoApartamentoId() {
        return tipoApartamentoId;
    }

    public void setTipoApartamentoId(Long tipoApartamentoId) {
        this.tipoApartamentoId = tipoApartamentoId;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
    }

    public String getCamasDoApartamento() {
        return camasDoApartamento;
    }

    public void setCamasDoApartamento(String camasDoApartamento) {
        this.camasDoApartamento = camasDoApartamento;
    }

    public String getTv() {
        return tv;
    }

    public void setTv(String tv) {
        this.tv = tv;
    }

    public Boolean getTemCamaDeCasal() {
        return temCamaDeCasal;
    }

    public void setTemCamaDeCasal(Boolean temCamaDeCasal) {
        this.temCamaDeCasal = temCamaDeCasal;
    }

    @Override
    public String toString() {
        return "ApartamentoRequestDTO{" +
                "numeroApartamento='" + numeroApartamento + '\'' +
                ", tipoApartamentoId=" + tipoApartamentoId +
                ", capacidade=" + capacidade +
                ", camasDoApartamento='" + camasDoApartamento + '\'' +
                ", tv='" + tv + '\'' +
                ", temCamaDeCasal=" + temCamaDeCasal +
                '}';
    }
}
