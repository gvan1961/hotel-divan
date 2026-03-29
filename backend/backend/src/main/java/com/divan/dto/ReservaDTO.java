package com.divan.dto;

import com.divan.entity.Reserva;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {
    
    private Long id;
    
    @NotNull(message = "Apartamento é obrigatório")
    private Long apartamentoId;
    
    private String apartamentoNumero;
    
    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;
    
    private String clienteNome;
    
    @Min(value = 1, message = "Quantidade de hóspedes deve ser no mínimo 1")
    private Integer quantidadeHospede;
    
    private Long diariaId;
    
    @NotNull(message = "Data de check-in é obrigatória")
    private LocalDateTime dataCheckin;
    
    @NotNull(message = "Data de check-out é obrigatória")
    private LocalDateTime dataCheckout;
    
    private Integer quantidadeDiaria;
    
    @DecimalMin(value = "0.0", message = "Total de diária não pode ser negativo")
    private BigDecimal totalDiaria;
    
    @DecimalMin(value = "0.0", message = "Total de produto não pode ser negativo")
    private BigDecimal totalProduto;
    
    @DecimalMin(value = "0.0", message = "Total de hospedagem não pode ser negativo")
    private BigDecimal totalHospedagem;
    
    @DecimalMin(value = "0.0", message = "Total recebido não pode ser negativo")
    private BigDecimal totalRecebido;
    
    @DecimalMin(value = "0.0", message = "Desconto não pode ser negativo")
    private BigDecimal desconto;
    
    private BigDecimal totalApagar;
    private Reserva.StatusReservaEnum status;
}
