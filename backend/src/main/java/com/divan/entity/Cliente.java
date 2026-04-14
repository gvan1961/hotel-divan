package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @Column(unique = true, nullable = true)
    private String cpf;

    @Column(nullable = true)
    private String celular;
    
    @Column(length = 5)
    private String ddi = "+55";

    @Column(name = "celular2", nullable = true)
    private String celular2;

    @Column(length = 5)
    private String ddi2 = "+55";

    @Column(name = "credito_aprovado")
    private Boolean creditoAprovado = false;

    @Column(length = 200)
    private String endereco;

    private String cep;

    @Column(length = 50)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(nullable = true)
    private LocalDate dataNascimento;

    @Column(name = "menor_de_idade")
    private Boolean menorDeIdade = false;

    @ManyToOne
    @JoinColumn(name = "responsavel_id")
    @JsonIgnoreProperties({"clientes", "reservas", "responsaveis"})
    private Cliente responsavel;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    @JsonIgnoreProperties("clientes")
    private Empresa empresa;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("cliente")
    private List<Reserva> reservas;

    @Column(name = "tipo_cliente", length = 20)
    private String tipoCliente;

    @Column(name = "autorizado_jantar")
    private Boolean autorizadoJantar = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
            
    public String getDdi() {
		return ddi;
	}
	public void setDdi(String ddi) {
		this.ddi = ddi;
	}
	public String getCelular2() {
		return celular2;
	}
	public void setCelular2(String celular2) {
		this.celular2 = celular2;
	}
	public String getDdi2() {
		return ddi2;
	}
	public void setDdi2(String ddi2) {
		this.ddi2 = ddi2;
	}
	public Boolean getCreditoAprovado() { return creditoAprovado; }
    public void setCreditoAprovado(Boolean creditoAprovado) { this.creditoAprovado = creditoAprovado; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public Boolean getMenorDeIdade() { return menorDeIdade; }
    public void setMenorDeIdade(Boolean menorDeIdade) { this.menorDeIdade = menorDeIdade; }
    public Cliente getResponsavel() { return responsavel; }
    public void setResponsavel(Cliente responsavel) { this.responsavel = responsavel; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public List<Reserva> getReservas() { return reservas; }
    public void setReservas(List<Reserva> reservas) { this.reservas = reservas; }
    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    public Boolean getAutorizadoJantar() { return autorizadoJantar; }
    public void setAutorizadoJantar(Boolean autorizadoJantar) { this.autorizadoJantar = autorizadoJantar; }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Cliente other = (Cliente) obj;
        return Objects.equals(id, other.id);
    }
}