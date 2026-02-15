package com.divan.dto;

import java.time.LocalDateTime;

public class CheckoutParcialRequestDTO {
    
    private Long hospedagemHospedeId; // ID do registro em HospedagemHospede
    private LocalDateTime dataHoraSaida; // Opcional - se null, usa LocalDateTime.now()
    private String motivo; // Ex: "Viagem de negócios encerrada"
    
    // Getters e Setters
    
    public Long getHospedagemHospedeId() {
        return hospedagemHospedeId;
    }
    
    public void setHospedagemHospedeId(Long hospedagemHospedeId) {
        this.hospedagemHospedeId = hospedagemHospedeId;
    }
    
    public LocalDateTime getDataHoraSaida() {
        return dataHoraSaida;
    }
    
    public void setDataHoraSaida(LocalDateTime dataHoraSaida) {
        this.dataHoraSaida = dataHoraSaida;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
