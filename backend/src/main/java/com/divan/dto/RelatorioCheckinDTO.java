package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioCheckinDTO {
    
    private Long codigoReserva;
    private LocalDateTime dataHoraCheckin;
    private LocalDateTime dataCheckout;
    private String apartamento;
    private Integer quantidadeHospede;
    private BigDecimal valorDiaria;
    private String hospedePrincipal;
    private String hospedesSecundarios;
    private String avisos;
}
