package com.divan.dto;

//import com.divan.entity.ManutencaoApartamento.TipoServico;
//import com.divan.entity.ManutencaoApartamento.StatusManutencao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.divan.entity.StatusManutencao;
import com.divan.entity.TipoServico;


public class ManutencaoRequestDTO {

    @NotNull(message = "O apartamento é obrigatório")
    private Long apartamentoId;

    private LocalDate dataServico; // se nulo, assume hoje

    @NotNull(message = "O tipo de serviço é obrigatório")
    private TipoServico tipoServico;

    @NotBlank(message = "A descrição é obrigatória")
    private String descricao;

    private String responsavel;
    private BigDecimal custo;
    private StatusManutencao status;      // se nulo, assume PENDENTE
    private LocalDate dataConclusao;
    private String observacoes;

    // Getters e Setters
    public Long getApartamentoId() { return apartamentoId; }
    public void setApartamentoId(Long apartamentoId) { this.apartamentoId = apartamentoId; }

    public LocalDate getDataServico() { return dataServico; }
    public void setDataServico(LocalDate dataServico) { this.dataServico = dataServico; }

    public TipoServico getTipoServico() { return tipoServico; }
    public void setTipoServico(TipoServico tipoServico) { this.tipoServico = tipoServico; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public BigDecimal getCusto() { return custo; }
    public void setCusto(BigDecimal custo) { this.custo = custo; }

    public StatusManutencao getStatus() { return status; }
    public void setStatus(StatusManutencao status) { this.status = status; }

    public LocalDate getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDate dataConclusao) { this.dataConclusao = dataConclusao; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
