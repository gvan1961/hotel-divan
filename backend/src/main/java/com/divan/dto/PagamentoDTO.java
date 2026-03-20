package com.divan.dto;

import com.divan.entity.Pagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoDTO {
    
    private Long id;
    
    @NotNull(message = "Reserva é obrigatória")
    private Long reservaId;
    
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;
    
    private Pagamento.FormaPagamentoEnum formaPagamento;
    private LocalDateTime dataHoraPagamento;
    private String observacao;
}
