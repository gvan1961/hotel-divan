package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "produtos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome do produto é obrigatório")
    @Column(nullable = false, length = 100)
    private String nomeProduto;
    
    @Min(value = 0, message = "Quantidade não pode ser negativa")
    @Column(nullable = false)
    private Integer quantidade;
    
    @DecimalMin(value = "0.01", message = "Valor de venda deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorVenda;
    
    @DecimalMin(value = "0.01", message = "Valor de compra deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorCompra;
    
    private LocalDate dataUltimaCompra;
    
    @NotNull(message = "Categoria é obrigatória")
    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    @JsonIgnoreProperties("produtos")
    private Categoria categoria;
    
    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"produto", "notaVenda"})
    private List<ItemVenda> itensVenda;
}
