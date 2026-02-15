package com.divan.service;

import com.divan.dto.DescontoRequestDTO;
import com.divan.entity.*;
import com.divan.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DescontoService {
    
    @Autowired
    private DescontoReservaRepository descontoRepository;
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ExtratoReservaRepository extratoReservaRepository;
    
    @Autowired
    private CaixaValidacaoService caixaValidacaoService;
    
    @Autowired
    private LogMovimentacaoCaixaRepository logRepository;
    
    /**
     * ✅ APLICAR DESCONTO NA RESERVA
     */
    @Transactional
    public DescontoReserva aplicarDesconto(DescontoRequestDTO request) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("💸 APLICANDO DESCONTO");
        System.out.println("═══════════════════════════════════════════");
        
        // ✅ 1. VALIDAR SE CAIXA ESTÁ ABERTO
        FechamentoCaixa caixa = caixaValidacaoService.validarCaixaAberto(request.getUsuarioId());
        
        // ✅ 2. BUSCAR RESERVA
        Reserva reserva = reservaRepository.findById(request.getReservaId())
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        // ✅ 3. BUSCAR USUÁRIO
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        System.out.println("📊 ANTES DO DESCONTO:");
        System.out.println("   Reserva: #" + reserva.getId());
        System.out.println("   Total Hospedagem: R$ " + reserva.getTotalHospedagem());
        System.out.println("   Total Descontos Atual: R$ " + calcularTotalDescontos(reserva));
        System.out.println("   Total A Pagar: R$ " + reserva.getTotalApagar());
        System.out.println("   Novo Desconto: R$ " + request.getValor());
        
        // ✅ 4. VALIDAÇÕES
        if (request.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valor do desconto deve ser maior que zero");
        }
        
        BigDecimal totalDescontosAtual = calcularTotalDescontos(reserva);
        BigDecimal novoTotalDescontos = totalDescontosAtual.add(request.getValor());
        
        if (novoTotalDescontos.compareTo(reserva.getTotalHospedagem()) > 0) {
            throw new RuntimeException(
                "Total de descontos (R$ " + novoTotalDescontos + ") não pode ser maior que o total da hospedagem (R$ " + reserva.getTotalHospedagem() + ")"
            );
        }
        
        // ✅ 5. CRIAR REGISTRO DE DESCONTO
        DescontoReserva desconto = new DescontoReserva();
        desconto.setReserva(reserva);
        desconto.setValor(request.getValor());
        desconto.setMotivo(request.getMotivo());
        desconto.setDataHoraDesconto(LocalDateTime.now());
        desconto.setUsuario(usuario);
        desconto.setCaixa(caixa);
        
        DescontoReserva descontoSalvo = descontoRepository.save(desconto);
        
        // ✅ 6. ATUALIZAR TOTAL DE DESCONTOS NA RESERVA
        reserva.setDesconto(novoTotalDescontos);
        
        // Recalcular total a pagar
        BigDecimal novoTotalAPagar = reserva.getTotalHospedagem()
            .subtract(reserva.getTotalRecebido())
            .subtract(novoTotalDescontos);
        
        reserva.setTotalApagar(novoTotalAPagar);
        reservaRepository.save(reserva);
        
        // ✅ 7. CRIAR LANÇAMENTO NO EXTRATO
        ExtratoReserva extratoDesconto = new ExtratoReserva();
        extratoDesconto.setReserva(reserva);
        extratoDesconto.setDataHoraLancamento(LocalDateTime.now());
        extratoDesconto.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.ESTORNO);
        extratoDesconto.setDescricao("Desconto aplicado" + 
            (request.getMotivo() != null && !request.getMotivo().isEmpty() ? " - " + request.getMotivo() : ""));
        extratoDesconto.setQuantidade(1);
        extratoDesconto.setValorUnitario(request.getValor().negate());
        extratoDesconto.setTotalLancamento(request.getValor().negate());
        extratoDesconto.setNotaVendaId(null);
        
        extratoReservaRepository.save(extratoDesconto);
        
        // ✅ 8. REGISTRAR LOG
        LogMovimentacaoCaixa log = new LogMovimentacaoCaixa();
        log.setCaixa(caixa);
        log.setUsuario(usuario);
        log.setTipoOperacao("DESCONTO");
        log.setDescricao("Desconto de R$ " + request.getValor() + " aplicado - " + request.getMotivo());
        log.setDataHora(LocalDateTime.now());
        log.setReservaId(reserva.getId());
        
        logRepository.save(log);
        
        System.out.println("✅ Desconto aplicado e lançado no extrato!");
        System.out.println("📊 APÓS O DESCONTO:");
        System.out.println("   Total Descontos: R$ " + novoTotalDescontos);
        System.out.println("   Total A Pagar: R$ " + novoTotalAPagar);
        System.out.println("═══════════════════════════════════════════");
        
        return descontoSalvo;
    }
    
    /**
     * ✅ LISTAR DESCONTOS DE UMA RESERVA
     */
    @Transactional(readOnly = true)
    public List<DescontoReserva> listarDescontosPorReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        return descontoRepository.findByReserva(reserva);
    }
    
    /**
     * ✅ CALCULAR TOTAL DE DESCONTOS DE UMA RESERVA
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalDescontos(Reserva reserva) {
        return descontoRepository.somarDescontosPorReserva(reserva);
    }
    
    /**
     * ✅ REMOVER DESCONTO (SE NECESSÁRIO)
     */
    /**
     * ✅ REMOVER DESCONTO
     */
    @Transactional
    public void removerDesconto(Long descontoId, Long usuarioId) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🗑️ REMOVENDO DESCONTO");
        System.out.println("═══════════════════════════════════════════");
        
        // ✅ VALIDAR CAIXA ABERTO
        FechamentoCaixa caixa = caixaValidacaoService.validarCaixaAberto(usuarioId);
        
        DescontoReserva desconto = descontoRepository.findById(descontoId)
            .orElseThrow(() -> new RuntimeException("Desconto não encontrado"));
        
        Reserva reserva = desconto.getReserva();
        BigDecimal valorDesconto = desconto.getValor();
        String motivoDesconto = desconto.getMotivo();
        
        System.out.println("📊 Desconto a remover:");
        System.out.println("   ID: " + descontoId);
        System.out.println("   Valor: R$ " + valorDesconto);
        System.out.println("   Motivo: " + motivoDesconto);
        
        // ✅ 1. CRIAR LANÇAMENTO DE ESTORNO NO EXTRATO (POSITIVO - CANCELA O DESCONTO)
        ExtratoReserva extratoEstorno = new ExtratoReserva();
        extratoEstorno.setReserva(reserva);
        extratoEstorno.setDataHoraLancamento(LocalDateTime.now());
        extratoEstorno.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.ESTORNO);
        extratoEstorno.setDescricao("Estorno de desconto" + 
            (motivoDesconto != null && !motivoDesconto.isEmpty() ? " - " + motivoDesconto : ""));
        extratoEstorno.setQuantidade(1);
        extratoEstorno.setValorUnitario(valorDesconto); // ✅ POSITIVO (cancela o desconto)
        extratoEstorno.setTotalLancamento(valorDesconto); // ✅ POSITIVO
        extratoEstorno.setNotaVendaId(null);
        
        extratoReservaRepository.save(extratoEstorno);
        System.out.println("✅ Lançamento de estorno criado no extrato: +R$ " + valorDesconto);
        
        // ✅ 2. REMOVER O DESCONTO DO BANCO
        descontoRepository.delete(desconto);
        System.out.println("✅ Desconto removido da tabela desconto_reserva");
        
        // ✅ 3. RECALCULAR TOTAL DE DESCONTOS
        BigDecimal novoTotalDescontos = calcularTotalDescontos(reserva);
        reserva.setDesconto(novoTotalDescontos);
        
        // ✅ 4. RECALCULAR TOTAL A PAGAR
        BigDecimal novoTotalAPagar = reserva.getTotalHospedagem()
            .subtract(reserva.getTotalRecebido())
            .subtract(novoTotalDescontos);
        
        reserva.setTotalApagar(novoTotalAPagar);
        reservaRepository.save(reserva);
        
        // ✅ 5. REGISTRAR LOG
        LogMovimentacaoCaixa log = new LogMovimentacaoCaixa();
        log.setCaixa(caixa);
        log.setUsuario(usuarioRepository.findById(usuarioId).orElse(null));
        log.setTipoOperacao("ESTORNO_DESCONTO");
        log.setDescricao("Desconto de R$ " + valorDesconto + " removido - " + motivoDesconto);
        log.setDataHora(LocalDateTime.now());
        log.setReservaId(reserva.getId());
        
        logRepository.save(log);
        
        System.out.println("✅ Desconto removido com sucesso!");
        System.out.println("📊 APÓS REMOÇÃO:");
        System.out.println("   Novo total de descontos: R$ " + novoTotalDescontos);
        System.out.println("   Novo total a pagar: R$ " + novoTotalAPagar);
        System.out.println("═══════════════════════════════════════════");
    }
}
