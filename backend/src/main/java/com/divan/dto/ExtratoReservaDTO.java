package com.divan.dto;

import com.divan.entity.ExtratoReserva;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtratoReservaDTO {
    
    private Long id;
    private Long reservaId;
    private LocalDateTime dataHoraLancamento;
    private ExtratoReserva.StatusLancamentoEnum statusLancamento;
    private Integer quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal totalLancamento;
    private String descricao;
    private Long notaVendaId;
}
