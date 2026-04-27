package com.divan.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "registros_ponto")
public class RegistroPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoPonto tipo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "observacao", length = 200)
    private String observacao;

    @Column(name = "reconhecimento_facial")
    private Boolean reconhecimentoFacial = false;

    @Column(name = "confianca_reconhecimento")
    private Double confiancaReconhecimento;
    
    @Column(name = "ajustado")
    private Boolean ajustado = false;

    @Column(name = "motivo_ajuste", length = 200)
    private String motivoAjuste;

    @Column(name = "ajustado_por", length = 100)
    private String ajustadoPor;

    public enum TipoPonto {
        ENTRADA,
        SAIDA_INTERVALO,
        RETORNO_INTERVALO,
        SAIDA
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public TipoPonto getTipo() { return tipo; }
    public void setTipo(TipoPonto tipo) { this.tipo = tipo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Boolean getReconhecimentoFacial() { return reconhecimentoFacial; }
    public void setReconhecimentoFacial(Boolean reconhecimentoFacial) { this.reconhecimentoFacial = reconhecimentoFacial; }
    public Double getConfiancaReconhecimento() { return confiancaReconhecimento; }
    public void setConfiancaReconhecimento(Double confiancaReconhecimento) { this.confiancaReconhecimento = confiancaReconhecimento; }
    public Boolean getAjustado() { return ajustado; }
    public void setAjustado(Boolean ajustado) { this.ajustado = ajustado; }
    public String getMotivoAjuste() { return motivoAjuste; }
    public void setMotivoAjuste(String motivoAjuste) { this.motivoAjuste = motivoAjuste; }
    public String getAjustadoPor() { return ajustadoPor; }
    public void setAjustadoPor(String ajustadoPor) { this.ajustadoPor = ajustadoPor; }
    
    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RegistroPonto other = (RegistroPonto) obj;
        return Objects.equals(id, other.id);
    }
}
