package com.divan.dto;

import com.divan.entity.Diaria;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class DiariaRequestDTO {

    @NotNull(message = "Tipo de apartamento é obrigatório")
    private Long tipoApartamentoId;

    @Min(value = 1, message = "Quantidade deve ser no mínimo 1")
    @NotNull(message = "Quantidade de hóspedes é obrigatória")
    private Integer quantidade;

    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @NotNull(message = "Valor é obrigatório")
    private BigDecimal valor;

    // ✅ NOVO — só obrigatório quando quantidade = 1 (validação no service)
    private Diaria.ModalidadeEnum modalidade;

    // Construtores
    public DiariaRequestDTO() {
    }

    public DiariaRequestDTO(Long tipoApartamentoId, Integer quantidade, BigDecimal valor, Diaria.ModalidadeEnum modalidade) {
        this.tipoApartamentoId = tipoApartamentoId;
        this.quantidade = quantidade;
        this.valor = valor;
        this.modalidade = modalidade;
    }

    // Getters e Setters
    public Long getTipoApartamentoId() {
        return tipoApartamentoId;
    }

    public void setTipoApartamentoId(Long tipoApartamentoId) {
        this.tipoApartamentoId = tipoApartamentoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Diaria.ModalidadeEnum getModalidade() {
        return modalidade;
    }

    public void setModalidade(Diaria.ModalidadeEnum modalidade) {
        this.modalidade = modalidade;
    }    
    
}
