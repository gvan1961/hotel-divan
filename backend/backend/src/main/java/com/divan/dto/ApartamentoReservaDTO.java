package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApartamentoReservaDTO {
    
    private String numeroApartamento;
    private String tipoApartamento;
    private String status;
    private List<ReservaMapaDTO> reservas;
}
