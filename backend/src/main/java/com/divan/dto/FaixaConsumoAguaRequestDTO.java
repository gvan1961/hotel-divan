package com.divan.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class FaixaConsumoAguaRequestDTO {

    @NotNull(message = "Empresa é obrigatória")
    private Long empresaId;

    @NotNull(message = "Quantidade de hóspedes é obrigatória")
    @Positive(message = "Quantidade de hóspedes deve ser maior que zero")
    private Integer qtdHospedes;

    @NotNull(message = "Valor limite é obrigatório")
    @Positive(message = "Valor limite deve ser maior que zero")
    private BigDecimal valorLimiteDiario;

    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public Integer getQtdHospedes() { return qtdHospedes; }
    public void setQtdHospedes(Integer qtdHospedes) { this.qtdHospedes = qtdHospedes; }
    public BigDecimal getValorLimiteDiario() { return valorLimiteDiario; }
    public void setValorLimiteDiario(BigDecimal valorLimiteDiario) { this.valorLimiteDiario = valorLimiteDiario; }
}