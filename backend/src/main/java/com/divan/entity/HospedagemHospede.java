package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "hospedagem_hospede")
public class HospedagemHospede {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})  // ← ADICIONE
    private Reserva reserva;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "reservas"})  // ← ADICIONE
    private Cliente cliente;
    
    @Column(name = "nome_completo", length = 200)
    private String nomeCompleto;
    
    @Column(name = "data_entrada", nullable = false)
    private LocalDateTime dataEntrada;
    
    @Column(name = "data_saida")
    private LocalDateTime dataSaida;
    
    @Column(nullable = false)
    private Boolean titular = false;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusHospedeIndividual status = StatusHospedeIndividual.HOSPEDADO;
    
    public enum StatusHospedeIndividual {
        HOSPEDADO,
        SAIU,
        CHECKOUT_REALIZADO
    }
    
    // Getters e Setters (mantenha todos)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }
    
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    
    public LocalDateTime getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(LocalDateTime dataEntrada) { this.dataEntrada = dataEntrada; }
    
    public LocalDateTime getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDateTime dataSaida) { this.dataSaida = dataSaida; }
    
    public Boolean getTitular() { return titular; }
    public void setTitular(Boolean titular) { this.titular = titular; }
    
    public StatusHospedeIndividual getStatus() { return status; }
    public void setStatus(StatusHospedeIndividual status) { this.status = status; }
    
    
    
	public String getNomeCompleto() {
		return nomeCompleto;
	}
	public void setNomeCompleto(String nomeCompleto) {
		this.nomeCompleto = nomeCompleto;
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
		HospedagemHospede other = (HospedagemHospede) obj;
		return Objects.equals(id, other.id);
	}
    
    
}