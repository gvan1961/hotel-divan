package com.divan.dto;

import com.divan.entity.Reserva;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.divan.entity.ExtratoReserva;

public class ReservaDetalhesDTO {
    
    private Long id;
    
    // CLIENTE
    private ClienteSimples cliente;
    
    private List<HospedeSimples> hospedes;
    
    // APARTAMENTO
    private ApartamentoSimples apartamento;
    
    // DATAS E DIÁRIAS
    private Integer quantidadeHospede;
    private LocalDateTime dataCheckin;
    private LocalDateTime dataCheckout;
    private Integer quantidadeDiaria;
    private BigDecimal valorDiaria;
    
    // TOTAIS FINANCEIROS
    private BigDecimal totalDiaria;
    private BigDecimal totalHospedagem;
    private BigDecimal totalRecebido;
    private BigDecimal totalApagar;
    private BigDecimal totalProduto;
    private BigDecimal desconto;
    private List<DescontoSimples> descontos;
        
    // STATUS
    private Reserva.StatusReservaEnum status;
    
    private String observacoes;
   
    private List<ExtratoSimples> extratos;
    private List<HistoricoSimples> historicos;
    
    // CLASSES INTERNAS PARA DADOS SIMPLIFICADOS
    
    public static class DescontoSimples {
        private Long id;
        private BigDecimal valor;
        private String motivo;
        private LocalDateTime dataHoraDesconto;
        
        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public BigDecimal getValor() {
            return valor;
        }
        public void setValor(BigDecimal valor) {
            this.valor = valor;
        }
        public String getMotivo() {
            return motivo;
        }
        public void setMotivo(String motivo) {
            this.motivo = motivo;
        }
        public LocalDateTime getDataHoraDesconto() {
            return dataHoraDesconto;
        }
        public void setDataHoraDesconto(LocalDateTime dataHoraDesconto) {
            this.dataHoraDesconto = dataHoraDesconto;
        }
        
        
    }
    
    public static class HospedeSimples {
        private Long id;
        private Long clienteId;
        private String nomeCompleto;
        private String cpf;
        private String telefone;
        private Boolean titular;
        private String status; // HOSPEDADO, CHECKOUT_REALIZADO
        
        // Getters e Setters
        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public Long getClienteId() {
            return clienteId;
        }
        public void setClienteId(Long clienteId) {
            this.clienteId = clienteId;
        }
        public String getNomeCompleto() {
            return nomeCompleto;
        }
        public void setNomeCompleto(String nomeCompleto) {
            this.nomeCompleto = nomeCompleto;
        }
        public String getCpf() {
            return cpf;
        }
        public void setCpf(String cpf) {
            this.cpf = cpf;
        }
        public String getTelefone() {
            return telefone;
        }
        public void setTelefone(String telefone) {
            this.telefone = telefone;
        }
        public Boolean getTitular() {
            return titular;
        }
        public void setTitular(Boolean titular) {
            this.titular = titular;
        }
        public String getStatus() {
            return status;
        }
        public void setStatus(String status) {
            this.status = status;
        }
        
        
    }

    
    public static class ClienteSimples {
        private Long id;
        private String nome;
        private String cpf;
        private String telefone;
        
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
        public String getTelefone() {
            return telefone;
        }
        public void setTelefone(String telefone) {
            this.telefone = telefone;
        }
    }
    
    public static class ApartamentoSimples {
        private Long id;
        private String numeroApartamento;
        private Integer capacidade;
        private String tipoApartamentoNome;
        
        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public String getNumeroApartamento() {
            return numeroApartamento;
        }
        public void setNumeroApartamento(String numeroApartamento) {
            this.numeroApartamento = numeroApartamento;
        }
        public Integer getCapacidade() {
            return capacidade;
        }
        public void setCapacidade(Integer capacidade) {
            this.capacidade = capacidade;
        }
        public String getTipoApartamentoNome() {
            return tipoApartamentoNome;
        }
        public void setTipoApartamentoNome(String tipoApartamentoNome) {
            this.tipoApartamentoNome = tipoApartamentoNome;
        }
    }
    
    public static class ExtratoSimples {
        private Long id;
        private LocalDateTime dataHoraLancamento;
        private String descricao;
        private ExtratoReserva.StatusLancamentoEnum statusLancamento;
        private Integer quantidade;
        private BigDecimal valorUnitario;
        private BigDecimal totalLancamento;
        private Long notaVendaId;
        
        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public LocalDateTime getDataHoraLancamento() {
            return dataHoraLancamento;
        }
        public void setDataHoraLancamento(LocalDateTime dataHoraLancamento) {
            this.dataHoraLancamento = dataHoraLancamento;
        }
        public String getDescricao() {
            return descricao;
        }
        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }
        public ExtratoReserva.StatusLancamentoEnum getStatusLancamento() {
            return statusLancamento;
        }
        public void setStatusLancamento(ExtratoReserva.StatusLancamentoEnum statusLancamento) {
            this.statusLancamento = statusLancamento;
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
        public Long getNotaVendaId() {
            return notaVendaId;
        }
        public void setNotaVendaId(Long notaVendaId) {
            this.notaVendaId = notaVendaId;
        }
    }

    public static class HistoricoSimples {
        private Long id;
        private LocalDateTime dataHora;
        private String motivo;
        private Integer quantidadeAnterior;
        private Integer quantidadeNova;
        
        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public LocalDateTime getDataHora() {
            return dataHora;
        }
        public void setDataHora(LocalDateTime dataHora) {
            this.dataHora = dataHora;
        }
        public String getMotivo() {
            return motivo;
        }
        public void setMotivo(String motivo) {
            this.motivo = motivo;
        }
        public Integer getQuantidadeAnterior() {
            return quantidadeAnterior;
        }
        public void setQuantidadeAnterior(Integer quantidadeAnterior) {
            this.quantidadeAnterior = quantidadeAnterior;
        }
        public Integer getQuantidadeNova() {
            return quantidadeNova;
        }
        public void setQuantidadeNova(Integer quantidadeNova) {
            this.quantidadeNova = quantidadeNova;
        }
    }

    // GETTERS E SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClienteSimples getCliente() {
        return cliente;
    }

    public void setCliente(ClienteSimples cliente) {
        this.cliente = cliente;
    }

    public ApartamentoSimples getApartamento() {
        return apartamento;
    }

    public void setApartamento(ApartamentoSimples apartamento) {
        this.apartamento = apartamento;
    }

    public Integer getQuantidadeHospede() {
        return quantidadeHospede;
    }

    public void setQuantidadeHospede(Integer quantidadeHospede) {
        this.quantidadeHospede = quantidadeHospede;
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

    public Integer getQuantidadeDiaria() {
        return quantidadeDiaria;
    }

    public void setQuantidadeDiaria(Integer quantidadeDiaria) {
        this.quantidadeDiaria = quantidadeDiaria;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public void setValorDiaria(BigDecimal valorDiaria) {
        this.valorDiaria = valorDiaria;
    }

    public BigDecimal getTotalDiaria() {
        return totalDiaria;
    }

    public void setTotalDiaria(BigDecimal totalDiaria) {
        this.totalDiaria = totalDiaria;
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

    public BigDecimal getTotalApagar() {
        return totalApagar;
    }

    public void setTotalApagar(BigDecimal totalApagar) {
        this.totalApagar = totalApagar;
    }

    public BigDecimal getTotalProduto() {
        return totalProduto;
    }

    public void setTotalProduto(BigDecimal totalProduto) {
        this.totalProduto = totalProduto;
    }

    public Reserva.StatusReservaEnum getStatus() {
        return status;
    }

    public void setStatus(Reserva.StatusReservaEnum status) {
        this.status = status;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public List<ExtratoSimples> getExtratos() {
        return extratos;
    }

    public void setExtratos(List<ExtratoSimples> extratos) {
        this.extratos = extratos;
    }

    public List<HistoricoSimples> getHistoricos() {
        return historicos;
    }

    public void setHistoricos(List<HistoricoSimples> historicos) {
        this.historicos = historicos;
    }

	public List<DescontoSimples> getDescontos() {
		return descontos;
	}

	public void setDescontos(List<DescontoSimples> descontos) {
		this.descontos = descontos;
	}

	public List<HospedeSimples> getHospedes() {
		return hospedes;
	}

	public void setHospedes(List<HospedeSimples> hospedes) {
		this.hospedes = hospedes;
	}  
	
	
    
}
