package com.divan.dto;

import com.divan.entity.Pagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoContaReceberDTO {
    
    @NotNull(message = "Valor pago é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valorPago;
    
    @NotNull(message = "Data do pagamento é obrigatória")
    private LocalDate dataPagamento;
    
    @NotNull(message = "Forma de pagamento é obrigatória")
    private Pagamento.FormaPagamentoEnum formaPagamento;
    
    private String observacao;
}
