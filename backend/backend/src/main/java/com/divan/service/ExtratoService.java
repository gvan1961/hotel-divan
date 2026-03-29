package com.divan.service;

import com.divan.dto.*;
import com.divan.entity.*;
import com.divan.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ExtratoService {
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private ExtratoReservaRepository extratoReservaRepository;
    
    @Autowired
    private HistoricoHospedeRepository historicoHospedeRepository;
    
    @Autowired
    private PagamentoRepository pagamentoRepository;
    
    public ExtratoDetalhadoDTO gerarExtratoDetalhado(Long reservaId) {
        // Buscar reserva
        Optional<Reserva> reservaOpt = reservaRepository.findById(reservaId);
        if (reservaOpt.isEmpty()) {
            throw new RuntimeException("Reserva não encontrada");
        }
        
        Reserva reserva = reservaOpt.get();
        
        ExtratoDetalhadoDTO extrato = new ExtratoDetalhadoDTO();
        
        // Informações da Reserva
        extrato.setReservaId(reserva.getId());
        extrato.setNumeroApartamento(reserva.getApartamento().getNumeroApartamento());
        extrato.setTipoApartamento(reserva.getApartamento().getTipoApartamento().getTipo().name());
        extrato.setClienteNome(reserva.getCliente().getNome());
        extrato.setClienteCpf(reserva.getCliente().getCpf());
        extrato.setQuantidadeHospede(reserva.getQuantidadeHospede());
        extrato.setDataCheckin(reserva.getDataCheckin());
        extrato.setDataCheckout(reserva.getDataCheckout());
        extrato.setQuantidadeDiarias(reserva.getQuantidadeDiaria());
        extrato.setStatusReserva(reserva.getStatus().name());
        
        // Histórico de Hóspedes
        List<HistoricoHospede> historicos = historicoHospedeRepository.findByReservaOrderByDataHora(reserva);
        extrato.setHistoricoHospedes(historicos.stream()
            .map(this::converterHistorico)
            .collect(Collectors.toList()));
        
        // ✅ BUSCAR APENAS OS EXTRATOS (que já incluem pagamentos e produtos)
        List<ExtratoReserva> extratos = extratoReservaRepository.findByReservaOrderByDataHoraLancamento(reserva);
        
        List<LancamentoDTO> lancamentos = new ArrayList<>();
        BigDecimal saldo = BigDecimal.ZERO;
        
        for (ExtratoReserva e : extratos) {
            LancamentoDTO lanc = new LancamentoDTO();
            lanc.setId(e.getId());
            lanc.setDataHora(e.getDataHoraLancamento());
            lanc.setTipo(e.getStatusLancamento().name());
            lanc.setDescricao(e.getDescricao());
            lanc.setQuantidade(e.getQuantidade());
            lanc.setValorUnitario(e.getValorUnitario());
            lanc.setTotalLancamento(e.getTotalLancamento());
            lanc.setNotaVendaId(e.getNotaVendaId());
            
            // Calcular saldo acumulado
            saldo = saldo.add(e.getTotalLancamento());
            lanc.setSaldoAcumulado(saldo);
            
            lancamentos.add(lanc);
        }
        
        // ✅ REMOVER A PARTE QUE BUSCAVA PAGAMENTOS SEPARADAMENTE
        // (Pagamentos já estão nos extratos)
        
        extrato.setLancamentos(lancamentos);
        
        // Totais
        extrato.setTotalDiarias(reserva.getTotalDiaria());
        extrato.setTotalProdutos(reserva.getTotalProduto());
        extrato.setTotalHospedagem(reserva.getTotalHospedagem());
        extrato.setTotalRecebido(reserva.getTotalRecebido());
        extrato.setDesconto(reserva.getDesconto());
        extrato.setSaldoDevedor(reserva.getTotalApagar());
        
        return extrato;
    }
    
    public List<ExtratoReserva> buscarExtratosPorReserva(Long reservaId) {
        Optional<Reserva> reserva = reservaRepository.findById(reservaId);
        if (reserva.isEmpty()) {
            throw new RuntimeException("Reserva não encontrada");
        }
        return extratoReservaRepository.findByReservaOrderByDataHoraLancamento(reserva.get());
    }
    
    public List<HistoricoHospede> buscarHistoricoPorReserva(Long reservaId) {
        Optional<Reserva> reserva = reservaRepository.findById(reservaId);
        if (reserva.isEmpty()) {
            throw new RuntimeException("Reserva não encontrada");
        }
        return historicoHospedeRepository.findByReservaOrderByDataHora(reserva.get());
    }
    
    public List<Reserva> buscarReservasPorApartamento(String numeroApartamento) {
        return reservaRepository.findAll().stream()
            .filter(r -> r.getApartamento().getNumeroApartamento().equals(numeroApartamento))
            .collect(Collectors.toList());
    }
    
    private HistoricoHospedeDTO converterHistorico(HistoricoHospede h) {
        HistoricoHospedeDTO dto = new HistoricoHospedeDTO();
        dto.setId(h.getId());
        dto.setDataHora(h.getDataHora());
        dto.setQuantidadeAnterior(h.getQuantidadeAnterior());
        dto.setQuantidadeNova(h.getQuantidadeNova());
        dto.setMotivo(h.getMotivo());
        return dto;
    }
}
