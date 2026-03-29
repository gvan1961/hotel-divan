package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "diarias", uniqueConstraints = @UniqueConstraint(columnNames = {"tipo_apartamento_id", "quantidade"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Diaria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Tipo de apartamento é obrigatório")
    @ManyToOne
    @JoinColumn(name = "tipo_apartamento_id", nullable = false)
    @JsonIgnoreProperties({"diarias", "apartamentos"})
    private TipoApartamento tipoApartamento;
    
    @Min(value = 1, message = "Quantidade deve ser no mínimo 1")
    @Column(nullable = false)
    private Integer quantidade;
    
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @OneToMany(mappedBy = "diaria", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("diaria")
    private List<Reserva> reservas;
}
