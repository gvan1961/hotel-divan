package com.divan.dto;

import com.divan.entity.TipoApartamento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiariaResponseDTO {
    
    private Long id;
    private Long tipoApartamentoId;
    private TipoApartamento.TipoEnum tipoApartamento;
    private String descricaoTipoApartamento;
    private Integer quantidade;
    private BigDecimal valor;
}
