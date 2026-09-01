package com.divan.dto;

import java.math.BigDecimal;

public class FaixaConsumoAguaResponseDTO {

    private Long id;
    private Long empresaId;
    private String empresaNome;
    private Integer qtdHospedes;
    private BigDecimal valorLimiteDiario;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public String getEmpresaNome() { return empresaNome; }
    public void setEmpresaNome(String empresaNome) { this.empresaNome = empresaNome; }
    public Integer getQtdHospedes() { return qtdHospedes; }
    public void setQtdHospedes(Integer qtdHospedes) { this.qtdHospedes = qtdHospedes; }
    public BigDecimal getValorLimiteDiario() { return valorLimiteDiario; }
    public void setValorLimiteDiario(BigDecimal valorLimiteDiario) { this.valorLimiteDiario = valorLimiteDiario; }
}
