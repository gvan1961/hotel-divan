package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LancamentoDTO {
    
    private Long id;
    private LocalDateTime dataHora;
    private String tipo; // DIARIA, PRODUTO, PAGAMENTO, ESTORNO
    private String descricao;
    private Integer quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal totalLancamento;
    private BigDecimal saldoAcumulado;
    private Long notaVendaId;
}
