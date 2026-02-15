package com.divan.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "vales")
public class Vale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDate dataConcessao;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoVale tipoVale;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVale status = StatusVale.PENDENTE;

    @Column(length = 500)
    private String observacao;

    @Column(length = 500)
    private String motivoCancelamento;

    private LocalDateTime dataCriacao;

    @Column(length = 100)
    private String criadoPor;
    
    @Column(columnDefinition = "LONGTEXT")
    private String assinaturaBase64;

    private LocalDateTime dataAssinatura;

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
    }
    
    

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public LocalDate getDataConcessao() {
		return dataConcessao;
	}

	public void setDataConcessao(LocalDate dataConcessao) {
		this.dataConcessao = dataConcessao;
	}

	public LocalDate getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(LocalDate dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public LocalDate getDataPagamento() {
		return dataPagamento;
	}

	public void setDataPagamento(LocalDate dataPagamento) {
		this.dataPagamento = dataPagamento;
	}

	public TipoVale getTipoVale() {
		return tipoVale;
	}

	public void setTipoVale(TipoVale tipoVale) {
		this.tipoVale = tipoVale;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public StatusVale getStatus() {
		return status;
	}

	public void setStatus(StatusVale status) {
		this.status = status;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public String getMotivoCancelamento() {
		return motivoCancelamento;
	}

	public void setMotivoCancelamento(String motivoCancelamento) {
		this.motivoCancelamento = motivoCancelamento;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(LocalDateTime dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public String getCriadoPor() {
		return criadoPor;
	}

	public void setCriadoPor(String criadoPor) {
		this.criadoPor = criadoPor;
	}
	
	
    
	
	public String getAssinaturaBase64() {
		return assinaturaBase64;
	}



	public void setAssinaturaBase64(String assinaturaBase64) {
		this.assinaturaBase64 = assinaturaBase64;
	}



	public LocalDateTime getDataAssinatura() {
		return dataAssinatura;
	}



	public void setDataAssinatura(LocalDateTime dataAssinatura) {
		this.dataAssinatura = dataAssinatura;
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
		Vale other = (Vale) obj;
		return Objects.equals(id, other.id);
	}




	public enum TipoVale {
        ADIANTAMENTO("Adiantamento Salarial"),
        EMPRESTIMO("Empréstimo"),
        OUTROS("Outros");

        private final String descricao;

        TipoVale(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
                
    }

    public enum StatusVale {
        PENDENTE("Pendente"),
        PAGO("Pago"),
        CANCELADO("Cancelado"),
        VENCIDO("Vencido");

        private final String descricao;

        StatusVale(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }        
        
    }
        
}
