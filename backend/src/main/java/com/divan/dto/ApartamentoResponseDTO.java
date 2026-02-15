package com.divan.dto;

import com.divan.entity.Apartamento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApartamentoResponseDTO {
    private Long id;
    private String numeroApartamento;
    private Long tipoApartamentoId;
    private String tipoApartamentoNome; // Ex: "A", "B", "C"
    private String tipoApartamentoDescricao; // Ex: "Luxo"
    private Integer capacidade;
    private String camasDoApartamento;
    private String tv;
    private Apartamento.StatusEnum status;
    private ReservaAtiva preReservaFutura;
    private ReservaAtiva reservaAtiva;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservaAtiva {
        private Long reservaId;
        private String nomeHospede;
        private Integer quantidadeHospede;
        private LocalDateTime dataCheckin;
        private LocalDateTime dataCheckout;
        private String status;  // ← ADICIONADO
    }
}