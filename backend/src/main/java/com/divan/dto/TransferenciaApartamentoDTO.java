package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaApartamentoDTO {
    
    private Long reservaId;
    private Long novoApartamentoId;
    private LocalDateTime dataTransferencia; // null = imediato
    private String motivo;
}
