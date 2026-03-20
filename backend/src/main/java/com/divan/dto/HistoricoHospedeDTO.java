package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoHospedeDTO {
    
    private Long id;
    private LocalDateTime dataHora;
    private Integer quantidadeAnterior;
    private Integer quantidadeNova;
    private String motivo;
}
