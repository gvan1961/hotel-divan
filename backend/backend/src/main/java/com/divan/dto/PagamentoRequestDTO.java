package com.divan.dto;

import com.divan.entity.Pagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoRequestDTO {
    
    @NotNull(message = "Reserva é obrigatória")
    private Long reservaId;
    
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @NotNull(message = "Valor é obrigatório")
    private BigDecimal valor;
    
    @NotNull(message = "Forma de pagamento é obrigatória")
    private Pagamento.FormaPagamentoEnum formaPagamento;
    
    private String observacao;
}
