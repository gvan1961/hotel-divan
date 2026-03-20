package com.divan.dto;

import com.divan.entity.NotaVenda;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaDTO {
    
    private Long id;
    private LocalDateTime dataHoraVenda;
    private NotaVenda.TipoVendaEnum tipoVenda;
    private Long reservaId;
    private String apartamentoNumero;
    private BigDecimal total;
    private List<ItemVendaDTO> itens;
}
