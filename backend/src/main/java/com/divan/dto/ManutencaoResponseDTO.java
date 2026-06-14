package com.divan.dto;

import com.divan.entity.ManutencaoApartamento;
import com.divan.entity.TipoServico;
import com.divan.entity.StatusManutencao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ManutencaoResponseDTO {

    private Long id;
    private Long apartamentoId;
    private String apartamentoNumero;
    private LocalDate dataServico;
    private String tipoServico;
    private String tipoServicoDescricao;
    private String descricao;
    private String responsavel;
    private BigDecimal custo;
    private String status;
    private String statusDescricao;
    private LocalDate dataConclusao;
    private String observacoes;
    private LocalDateTime createdAt;

    public static ManutencaoResponseDTO fromEntity(ManutencaoApartamento m) {
        ManutencaoResponseDTO dto = new ManutencaoResponseDTO();
        dto.id = m.getId();

        if (m.getApartamento() != null) {
            dto.apartamentoId     = m.getApartamento().getId();
            dto.apartamentoNumero = m.getApartamento().getNumeroApartamento();
        }

        dto.dataServico = m.getDataServico();

        TipoServico tipo = m.getTipoServico();
        if (tipo != null) {
            dto.tipoServico          = tipo.name();
            dto.tipoServicoDescricao = tipo.getDescricao();
        }

        dto.descricao   = m.getDescricao();
        dto.responsavel = m.getResponsavel();
        dto.custo       = m.getCusto();

        StatusManutencao st = m.getStatus();
        if (st != null) {
            dto.status          = st.name();
            dto.statusDescricao = st.getDescricao();
        }

        dto.dataConclusao = m.getDataConclusao();
        dto.observacoes   = m.getObservacoes();
        dto.createdAt     = m.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public Long getApartamentoId() { return apartamentoId; }
    public String getApartamentoNumero() { return apartamentoNumero; }
    public LocalDate getDataServico() { return dataServico; }
    public String getTipoServico() { return tipoServico; }
    public String getTipoServicoDescricao() { return tipoServicoDescricao; }
    public String getDescricao() { return descricao; }
    public String getResponsavel() { return responsavel; }
    public BigDecimal getCusto() { return custo; }
    public String getStatus() { return status; }
    public String getStatusDescricao() { return statusDescricao; }
    public LocalDate getDataConclusao() { return dataConclusao; }
    public String getObservacoes() { return observacoes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}