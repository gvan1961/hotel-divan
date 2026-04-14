package com.divan.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "bilhetes_sorteio",
    uniqueConstraints = @UniqueConstraint(columnNames = {"sorteio_id", "numero_bilhete"}))
public class BilheteSorteio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sorteio_id", nullable = false)
    private Sorteio sorteio;

    @ManyToOne
    @JoinColumn(name = "hospedagem_hospede_id", nullable = false)
    private HospedagemHospede hospedagemHospede;

    @Column(nullable = false)
    private Integer numeroBilhete;

    @Column(nullable = false)
    private LocalDateTime dataEmissao = LocalDateTime.now();

    private Integer quantidadeDiarias;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Sorteio getSorteio() { return sorteio; }
    public void setSorteio(Sorteio sorteio) { this.sorteio = sorteio; }
    public HospedagemHospede getHospedagemHospede() { return hospedagemHospede; }
    public void setHospedagemHospede(HospedagemHospede hospedagemHospede) { this.hospedagemHospede = hospedagemHospede; }
    public Integer getNumeroBilhete() { return numeroBilhete; }
    public void setNumeroBilhete(Integer numeroBilhete) { this.numeroBilhete = numeroBilhete; }
    public LocalDateTime getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDateTime dataEmissao) { this.dataEmissao = dataEmissao; }
    public Integer getQuantidadeDiarias() { return quantidadeDiarias; }
    public void setQuantidadeDiarias(Integer quantidadeDiarias) { this.quantidadeDiarias = quantidadeDiarias;
        
    
    }
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BilheteSorteio other = (BilheteSorteio) obj;
		return Objects.equals(id, other.id);
	}
}