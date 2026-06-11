package com.divan.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Immutable
@Table(name = "vw_extrato_completo")
public class VwExtratoCompleto {

    @Id
    private Long id;

    @Column(name = "reserva_id")
    private Long reservaId;

    @Column(name = "data_hora_lancamento")
    private LocalDateTime dataHoraLancamento;

    @Column(name = "status_lancamento")
    private String statusLancamento;

    @Column(name = "descricao")
    private String descricao;

    private Integer quantidade;

    @Column(name = "valor_unitario")
    private BigDecimal valorUnitario;

    @Column(name = "total_lancamento")
    private BigDecimal totalLancamento;

    @Column(name = "numero_apartamento")
    private String numeroApartamento;

    @Column(name = "data_checkin")
    private LocalDateTime dataCheckin;

    @Column(name = "data_checkout")
    private LocalDateTime dataCheckout;

    @Column(name = "total_hospedagem")
    private BigDecimal totalHospedagem;

    @Column(name = "total_recebido")
    private BigDecimal totalRecebido;

    @Column(name = "total_diaria")
    private BigDecimal totalDiaria;

    @Column(name = "total_produto")
    private BigDecimal totalProduto;

    @Column(name = "desconto")
    private BigDecimal desconto;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "cliente_nome")
    private String clienteNome;

    @Column(name = "cliente_cpf")
    private String clienteCpf;

    @Column(name = "empresa_id")
    private Long empresaId;

    @Column(name = "nome_empresa")
    private String nomeEmpresa;
    
    @Column(name = "quantidade_hospede")
    private Integer quantidadeHospede;

    @Column(name = "quantidade_diaria")
    private Integer quantidadeDiaria;

    public Integer getQuantidadeHospede() { return quantidadeHospede; }
    public Integer getQuantidadeDiaria() { return quantidadeDiaria; }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getReservaId() {
		return reservaId;
	}

	public void setReservaId(Long reservaId) {
		this.reservaId = reservaId;
	}

	public LocalDateTime getDataHoraLancamento() {
		return dataHoraLancamento;
	}

	public void setDataHoraLancamento(LocalDateTime dataHoraLancamento) {
		this.dataHoraLancamento = dataHoraLancamento;
	}

	public String getStatusLancamento() {
		return statusLancamento;
	}

	public void setStatusLancamento(String statusLancamento) {
		this.statusLancamento = statusLancamento;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public BigDecimal getValorUnitario() {
		return valorUnitario;
	}

	public void setValorUnitario(BigDecimal valorUnitario) {
		this.valorUnitario = valorUnitario;
	}

	public BigDecimal getTotalLancamento() {
		return totalLancamento;
	}

	public void setTotalLancamento(BigDecimal totalLancamento) {
		this.totalLancamento = totalLancamento;
	}

	public String getNumeroApartamento() {
		return numeroApartamento;
	}

	public void setNumeroApartamento(String numeroApartamento) {
		this.numeroApartamento = numeroApartamento;
	}

	public LocalDateTime getDataCheckin() {
		return dataCheckin;
	}

	public void setDataCheckin(LocalDateTime dataCheckin) {
		this.dataCheckin = dataCheckin;
	}

	public LocalDateTime getDataCheckout() {
		return dataCheckout;
	}

	public void setDataCheckout(LocalDateTime dataCheckout) {
		this.dataCheckout = dataCheckout;
	}

	public BigDecimal getTotalHospedagem() {
		return totalHospedagem;
	}

	public void setTotalHospedagem(BigDecimal totalHospedagem) {
		this.totalHospedagem = totalHospedagem;
	}

	public BigDecimal getTotalRecebido() {
		return totalRecebido;
	}

	public void setTotalRecebido(BigDecimal totalRecebido) {
		this.totalRecebido = totalRecebido;
	}

	public BigDecimal getTotalDiaria() {
		return totalDiaria;
	}

	public void setTotalDiaria(BigDecimal totalDiaria) {
		this.totalDiaria = totalDiaria;
	}

	public BigDecimal getTotalProduto() {
		return totalProduto;
	}

	public void setTotalProduto(BigDecimal totalProduto) {
		this.totalProduto = totalProduto;
	}

	public BigDecimal getDesconto() {
		return desconto;
	}

	public void setDesconto(BigDecimal desconto) {
		this.desconto = desconto;
	}

	public Long getClienteId() {
		return clienteId;
	}

	public void setClienteId(Long clienteId) {
		this.clienteId = clienteId;
	}

	public String getClienteNome() {
		return clienteNome;
	}

	public void setClienteNome(String clienteNome) {
		this.clienteNome = clienteNome;
	}

	public String getClienteCpf() {
		return clienteCpf;
	}

	public void setClienteCpf(String clienteCpf) {
		this.clienteCpf = clienteCpf;
	}

	public Long getEmpresaId() {
		return empresaId;
	}

	public void setEmpresaId(Long empresaId) {
		this.empresaId = empresaId;
	}

	public String getNomeEmpresa() {
		return nomeEmpresa;
	}

	public void setNomeEmpresa(String nomeEmpresa) {
		this.nomeEmpresa = nomeEmpresa;
	}	
	

	public void setQuantidadeHospede(Integer quantidadeHospede) {
		this.quantidadeHospede = quantidadeHospede;
	}
	public void setQuantidadeDiaria(Integer quantidadeDiaria) {
		this.quantidadeDiaria = quantidadeDiaria;
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
		VwExtratoCompleto other = (VwExtratoCompleto) obj;
		return Objects.equals(id, other.id);
	}

    
}
