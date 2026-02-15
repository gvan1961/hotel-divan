package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "clientes")

public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false, length = 100)
    private String nome;
    
    @CPF(message = "CPF inválido")
    @Column(unique = true, nullable = false)
    private String cpf;
    
    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message = "Celular deve estar no formato (XX) XXXXX-XXXX")
    @Column(nullable = false)
    private String celular;
    
    @Column(name = "credito_aprovado")
    private Boolean creditoAprovado = false;
    
    @Column(length = 200)
    private String endereco;
    
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP deve estar no formato XXXXX-XXX")
    private String cep;
    
    @Column(length = 50)
    private String cidade;
    
    @Column(length = 2)
    private String estado;
    
    @NotNull(message = "Data de nascimento é obrigatória")
    @Column(nullable = false)
    private LocalDate dataNascimento;
    
    @Column(name = "autorizado_jantar")
    private Boolean autorizadoJantar = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cliente", nullable = false, length = 20)
    private TipoCliente tipoCliente = TipoCliente.HOSPEDE;
    
    // IMPORTANTE: JsonIgnoreProperties evita o loop infinito
    @ManyToOne
    @JoinColumn(name = "empresa_id")
    @JsonIgnoreProperties("clientes")
    private Empresa empresa;
    
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @JsonIgnore  // ✅ MUDE PARA ISSO
    private List<Reserva> reservas;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getCelular() {
		return celular;
	}

	public void setCelular(String celular) {
		this.celular = celular;
	}

	public Boolean getCreditoAprovado() {
		return creditoAprovado;
	}

	public void setCreditoAprovado(Boolean creditoAprovado) {
		this.creditoAprovado = creditoAprovado;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
	}

	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public LocalDate getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(LocalDate dataNascimento) {
		this.dataNascimento = dataNascimento;
	}
	
	public Boolean getAutorizadoJantar() {
		return autorizadoJantar;
	}

	public void setAutorizadoJantar(Boolean autorizadoJantar) {
		this.autorizadoJantar = autorizadoJantar;
	}
	

	public TipoCliente getTipoCliente() {
		return tipoCliente;
	}

	public void setTipoCliente(TipoCliente tipoCliente) {
		this.tipoCliente = tipoCliente;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public List<Reserva> getReservas() {
		return reservas;
	}

	public void setReservas(List<Reserva> reservas) {
		this.reservas = reservas;
	}
	
	 @Transient
	    public boolean podeJantar() {
	        // REGRA 1: Se não tem empresa, pode jantar
	        if (this.empresa == null) {
	            return true;
	        }
	        
	        // REGRA 2: Se empresa autoriza todos, pode jantar
	        if (this.empresa.getAutorizaTodosJantar() != null && 
	            this.empresa.getAutorizaTodosJantar()) {
	            return true;
	        }
	        
	        // REGRA 3: Verificar autorização individual
	        return this.autorizadoJantar != null && this.autorizadoJantar;
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
		Cliente other = (Cliente) obj;
		return Objects.equals(id, other.id);
	}
	
	public enum TipoCliente {
	    HOSPEDE("Hóspede"),
	    FUNCIONARIO("Funcionário");
	    
	    private final String descricao;
	    
	    TipoCliente(String descricao) {
	        this.descricao = descricao;
	    }
	    
	    public String getDescricao() {
	        return descricao;
	    }
	}
    
    
}
