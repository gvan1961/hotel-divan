package com.divan.dto;

import com.divan.entity.TipoApartamento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoApartamentoResponseDTO {
    
    private Long id;
    private TipoApartamento.TipoEnum tipo;
    private String descricao;
    private Integer totalApartamentos; // Opcional: quantos apartamentos deste tipo existem
}
