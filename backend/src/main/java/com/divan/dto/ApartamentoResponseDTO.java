package com.divan.dto;

import com.divan.entity.Apartamento;
import java.time.LocalDateTime;

public class ApartamentoResponseDTO {

    private Long id;
    private String numeroApartamento;
    private Long tipoApartamentoId;
    private String tipoApartamentoNome;
    private String tipoApartamentoDescricao;
    private Integer capacidade;
    private String camasDoApartamento;
    private String tv;
    private Apartamento.StatusEnum status;
    private ReservaAtiva reservaAtiva;

    // ✅ NOVO
    private Boolean temCamaDeCasal;

    // Construtores
    public ApartamentoResponseDTO() {
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getTipoApartamentoNome() {
        return tipoApartamentoNome;
    }

    public void setTipoApartamentoNome(String tipoApartamentoNome) {
        this.tipoApartamentoNome = tipoApartamentoNome;
    }

    public String getTipoApartamentoDescricao() {
        return tipoApartamentoDescricao;
    }

    public void setTipoApartamentoDescricao(String tipoApartamentoDescricao) {
        this.tipoApartamentoDescricao = tipoApartamentoDescricao;
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

    public Apartamento.StatusEnum getStatus() {
        return status;
    }

    public void setStatus(Apartamento.StatusEnum status) {
        this.status = status;
    }

    public ReservaAtiva getReservaAtiva() {
        return reservaAtiva;
    }

    public void setReservaAtiva(ReservaAtiva reservaAtiva) {
        this.reservaAtiva = reservaAtiva;
    }

    public Boolean getTemCamaDeCasal() {
        return temCamaDeCasal;
    }

    public void setTemCamaDeCasal(Boolean temCamaDeCasal) {
        this.temCamaDeCasal = temCamaDeCasal;
    }

    // Classe interna ReservaAtiva
    public static class ReservaAtiva {
        private Long reservaId;
        private String nomeHospede;
        private Integer quantidadeHospede;
        private LocalDateTime dataCheckin;
        private LocalDateTime dataCheckout;

        public ReservaAtiva() {
        }

        public ReservaAtiva(Long reservaId, String nomeHospede, Integer quantidadeHospede,
                            LocalDateTime dataCheckin, LocalDateTime dataCheckout) {
            this.reservaId = reservaId;
            this.nomeHospede = nomeHospede;
            this.quantidadeHospede = quantidadeHospede;
            this.dataCheckin = dataCheckin;
            this.dataCheckout = dataCheckout;
        }

        public Long getReservaId() {
            return reservaId;
        }

        public void setReservaId(Long reservaId) {
            this.reservaId = reservaId;
        }

        public String getNomeHospede() {
            return nomeHospede;
        }

        public void setNomeHospede(String nomeHospede) {
            this.nomeHospede = nomeHospede;
        }

        public Integer getQuantidadeHospede() {
            return quantidadeHospede;
        }

        public void setQuantidadeHospede(Integer quantidadeHospede) {
            this.quantidadeHospede = quantidadeHospede;
        }

        public LocalDateTime getDataCheckin() {
            return dataCheckin;
        }

        public void setDataCheckin(LocalDateTime dataCheckin) {
            this.dataCheckin = dataCheckin;
        }

        public LocalDateTime getDataCheckout() {
            return dataCheckout;
        }

        public void setDataCheckout(LocalDateTime dataCheckout) {
            this.dataCheckout = dataCheckout;
        }
    }
}