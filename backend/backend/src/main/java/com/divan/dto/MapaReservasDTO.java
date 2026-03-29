package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapaReservasDTO {
    
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private List<ApartamentoReservaDTO> apartamentos;
}
