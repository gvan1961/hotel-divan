package com.divan.service;

import com.divan.dto.*;
import com.divan.entity.*;
import com.divan.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RelatorioFechamentoCaixaService {
    
    @Autowired
    private FechamentoCaixaRepository fechamentoCaixaRepository;
    
    @Autowired
    private PagamentoRepository pagamentoRepository;
    
    @Autowired
    private DescontoReservaRepository descontoReservaRepository;
    
    @Autowired
    private NotaVendaRepository notaVendaRepository;
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    /**
     * 📊 GERAR RELATÓRIO COMPLETO DO FECHAMENTO DE CAIXA
     */
    /**
     * 📊 GERAR RELATÓRIO COMPLETO DO FECHAMENTO DE CAIXA
     */
    public RelatorioFechamentoCaixaDTO gerarRelatorioCompleto(Long caixaId) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("📊 GERANDO RELATÓRIO COMPLETO DO CAIXA #" + caixaId);
        System.out.println("═══════════════════════════════════════════");
        
        FechamentoCaixa caixa = fechamentoCaixaRepository.findById(caixaId)
            .orElseThrow(() -> new RuntimeException("Caixa não encontrado"));
        
        RelatorioFechamentoCaixaDTO relatorio = new RelatorioFechamentoCaixaDTO();
        
        // ✅ DADOS DO CAIXA
        relatorio.setCaixaId(caixa.getId());
        relatorio.setRecepcionistaNome(caixa.getUsuario().getNome());
        relatorio.setDataHoraAbertura(caixa.getDataHoraAbertura());
        relatorio.setDataHoraFechamento(caixa.getDataHoraFechamento());
        relatorio.setTurno(caixa.getTurno());
        
        LocalDateTime dataInicio = caixa.getDataHoraAbertura();
        LocalDateTime dataFim = caixa.getDataHoraFechamento() != null ? 
            caixa.getDataHoraFechamento() : LocalDateTime.now();
        
        System.out.println("📅 Período: " + dataInicio + " até " + dataFim);
        
        // ✅ 1. BUSCAR TODOS OS PAGAMENTOS DO PERÍODO
        List<Pagamento> todosPagamentos = pagamentoRepository.findPagamentosPorPeriodo(dataInicio, dataFim);
        System.out.println("💰 Total de pagamentos: " + todosPagamentos.size());
        
        // ✅ 2. BUSCAR TODOS OS DESCONTOS DO CAIXA
        List<DescontoReserva> todosDescontos = descontoReservaRepository.findByCaixa(caixa);
        System.out.println("💸 Total de descontos: " + todosDescontos.size());
        
        // ✅ 3. SEPARAR PAGAMENTOS POR RESERVA E AVULSOS
        Map<Long, List<Pagamento>> pagamentosPorReserva = new HashMap<>();
        List<Pagamento> pagamentosAvulsos = new ArrayList<>();
        
        for (Pagamento pag : todosPagamentos) {
            if (pag.getReserva() != null) {
                Long reservaId = pag.getReserva().getId();
                pagamentosPorReserva.computeIfAbsent(reservaId, k -> new ArrayList<>()).add(pag);
            } else {
                pagamentosAvulsos.add(pag);
            }
        }
        
        // ✅ 4. SEPARAR DESCONTOS POR RESERVA
        Map<Long, List<DescontoReserva>> descontosPorReserva = todosDescontos.stream()
            .collect(Collectors.groupingBy(d -> d.getReserva().getId()));
        
        // ✅ 5. PROCESSAR MOVIMENTAÇÕES POR APARTAMENTO
        List<MovimentacaoApartamentoDTO> movimentacoes = processarMovimentacoesPorApartamento(
            pagamentosPorReserva, 
            descontosPorReserva
        );
        relatorio.setMovimentacoesReservas(movimentacoes);
        
        // ✅ 6. PROCESSAR VENDAS AVULSAS (RESUMO)
        RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento resumoAvulsas = 
            processarVendasAvulsas(dataInicio, dataFim);
        relatorio.setResumoAvulsas(resumoAvulsas);
        
        // ✅ 7. PROCESSAR VENDAS AVULSAS FATURADAS (PARA IMPRESSÃO COM NOME DO CLIENTE)
        List<VendaAvulsaDTO> vendasAvulsasFaturadas = processarVendasAvulsasFaturadas(dataInicio, dataFim);
        relatorio.setVendasAvulsasFaturadas(vendasAvulsasFaturadas);
        
        // ✅ 8. CALCULAR RESUMOS
        RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento resumoReservas = 
            calcularResumoReservas(pagamentosPorReserva, todosDescontos);
        relatorio.setResumoReservas(resumoReservas);
        
        RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento resumoGeral = 
            calcularResumoGeral(resumoReservas, resumoAvulsas);
        relatorio.setResumoGeral(resumoGeral);
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("✅ RELATÓRIO GERADO COM SUCESSO!");
        System.out.println("   Apartamentos: " + movimentacoes.size());
        System.out.println("   Vendas Avulsas Faturadas: " + vendasAvulsasFaturadas.size());
        System.out.println("   💰 TOTAL GERAL: R$ " + resumoGeral.getTotal());
        System.out.println("   💵 DINHEIRO: R$ " + resumoGeral.getDinheiro());
        System.out.println("   📄 FATURADO: R$ " + resumoGeral.getFaturado());
        System.out.println("═══════════════════════════════════════════");
        
        return relatorio;
    }
    
    /**
     * 🏨 PROCESSAR MOVIMENTAÇÕES POR APARTAMENTO
     */
    private List<MovimentacaoApartamentoDTO> processarMovimentacoesPorApartamento(
            Map<Long, List<Pagamento>> pagamentosPorReserva,
            Map<Long, List<DescontoReserva>> descontosPorReserva
    ) {
        List<MovimentacaoApartamentoDTO> movimentacoes = new ArrayList<>();
        
        for (Map.Entry<Long, List<Pagamento>> entry : pagamentosPorReserva.entrySet()) {
            Long reservaId = entry.getKey();
            List<Pagamento> pagamentos = entry.getValue();
            
            // Buscar dados da reserva
            Optional<Reserva> reservaOpt = reservaRepository.findById(reservaId);
            if (reservaOpt.isEmpty()) continue;
            
            Reserva reserva = reservaOpt.get();
            
            MovimentacaoApartamentoDTO mov = new MovimentacaoApartamentoDTO();
            mov.setReservaId(reservaId);
            mov.setNumeroApartamento(reserva.getApartamento().getNumeroApartamento());
            mov.setClienteNome(reserva.getCliente().getNome());
            mov.setQuantidadeHospedes(reserva.getQuantidadeHospede());
            
            // ✅ PROCESSAR PAGAMENTOS
            List<MovimentacaoApartamentoDTO.PagamentoDetalhado> pagamentosDetalhados = new ArrayList<>();
            BigDecimal totalPagamentos = BigDecimal.ZERO;
            
            for (Pagamento pag : pagamentos) {
                MovimentacaoApartamentoDTO.PagamentoDetalhado pagDTO = 
                    new MovimentacaoApartamentoDTO.PagamentoDetalhado();
                pagDTO.setId(pag.getId());
                pagDTO.setFormaPagamento(pag.getFormaPagamento().name());
                pagDTO.setValor(pag.getValor());
                pagDTO.setDataHora(pag.getDataHora());
                
                pagamentosDetalhados.add(pagDTO);
                totalPagamentos = totalPagamentos.add(pag.getValor());
            }
            
            mov.setPagamentos(pagamentosDetalhados);
            mov.setTotalPagamentos(totalPagamentos);
            
            // ✅ PROCESSAR DESCONTOS
            List<MovimentacaoApartamentoDTO.DescontoDetalhado> descontosDetalhados = new ArrayList<>();
            BigDecimal totalDescontos = BigDecimal.ZERO;
            
            List<DescontoReserva> descontos = descontosPorReserva.getOrDefault(reservaId, new ArrayList<>());
            for (DescontoReserva desc : descontos) {
                MovimentacaoApartamentoDTO.DescontoDetalhado descDTO = 
                    new MovimentacaoApartamentoDTO.DescontoDetalhado();
                descDTO.setId(desc.getId());
                descDTO.setValor(desc.getValor());
                descDTO.setMotivo(desc.getMotivo());
                descDTO.setDataHora(desc.getDataHoraDesconto());
                
                descontosDetalhados.add(descDTO);
                totalDescontos = totalDescontos.add(desc.getValor());
            }
            
            mov.setDescontos(descontosDetalhados);
            mov.setTotalDescontos(totalDescontos);
            
            // Total final = pagamentos - descontos
            mov.setTotalFinal(totalPagamentos.subtract(totalDescontos));
            
            movimentacoes.add(mov);
        }
        
        // Ordenar por número do apartamento
        movimentacoes.sort(Comparator.comparing(MovimentacaoApartamentoDTO::getNumeroApartamento));
        
        return movimentacoes;
    }
    
    /**
     * 🛒 PROCESSAR VENDAS AVULSAS - RETORNA RESUMO POR FORMA DE PAGAMENTO
     */
    private RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento processarVendasAvulsas(
            LocalDateTime dataInicio, 
            LocalDateTime dataFim
    ) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🛒 PROCESSANDO VENDAS AVULSAS");
        System.out.println("═══════════════════════════════════════════");
        
        RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento resumo = 
            new RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento();
        
        // ✅ BUSCAR PAGAMENTOS SEM RESERVA (vendas avulsas)
        List<Pagamento> pagamentosAvulsos = pagamentoRepository
            .findByReservaIsNullAndDataHoraBetween(dataInicio, dataFim);
        
        System.out.println("💰 Total de pagamentos avulsos encontrados: " + pagamentosAvulsos.size());
        
        // ✅ SOMAR CADA PAGAMENTO AVULSO NO RESUMO
        for (Pagamento pag : pagamentosAvulsos) {
            System.out.println("   📝 Pagamento: " + pag.getFormaPagamento() + 
                              " - R$ " + pag.getValor() + 
                              " - Tipo: " + pag.getTipo());
            somarPagamento(resumo, pag);
        }
        
        System.out.println("✅ Resumo de vendas avulsas:");
        System.out.println("   💵 Dinheiro: R$ " + resumo.getDinheiro());
        System.out.println("   📱 PIX: R$ " + resumo.getPix());
        System.out.println("   💳 Cartão Débito: R$ " + resumo.getCartaoDebito());
        System.out.println("   💳 Cartão Crédito: R$ " + resumo.getCartaoCredito());
        System.out.println("   🏦 Transferência: R$ " + resumo.getTransferencia());
        System.out.println("   📄 Faturado: R$ " + resumo.getFaturado());
        System.out.println("   💰 TOTAL: R$ " + resumo.getTotal());
        System.out.println("═══════════════════════════════════════════");
        
        return resumo;
    }
    
    /**
     * 🧮 CALCULAR RESUMO DE RESERVAS
     */
    private RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento calcularResumoReservas(
            Map<Long, List<Pagamento>> pagamentosPorReserva,
            List<DescontoReserva> todosDescontos
    ) {
        RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento resumo = 
            new RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento();
        
        // Somar todos os pagamentos de reservas
        for (List<Pagamento> pagamentos : pagamentosPorReserva.values()) {
            for (Pagamento pag : pagamentos) {
                somarPagamento(resumo, pag);
            }
        }
        
        // Somar descontos
        BigDecimal totalDescontos = todosDescontos.stream()
            .map(DescontoReserva::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        resumo.setTotalDescontos(totalDescontos);
        
        return resumo;
    }
    
    /**
     * 🧮 CALCULAR RESUMO DE VENDAS AVULSAS
     */
    private RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento calcularResumoAvulsas(
            List<Pagamento> pagamentosAvulsos
    ) {
        RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento resumo = 
            new RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento();
        
        for (Pagamento pag : pagamentosAvulsos) {
            somarPagamento(resumo, pag);
        }
        
        return resumo;
    }
    
    /**
     * 🧮 CALCULAR RESUMO GERAL
     */
    private RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento calcularResumoGeral(
            RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento resumoReservas,
            RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento resumoAvulsas
    ) {
        RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento resumoGeral = 
            new RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento();
        
        resumoGeral.setDinheiro(resumoReservas.getDinheiro().add(resumoAvulsas.getDinheiro()));
        resumoGeral.setPix(resumoReservas.getPix().add(resumoAvulsas.getPix()));
        resumoGeral.setCartaoDebito(resumoReservas.getCartaoDebito().add(resumoAvulsas.getCartaoDebito()));
        resumoGeral.setCartaoCredito(resumoReservas.getCartaoCredito().add(resumoAvulsas.getCartaoCredito()));
        resumoGeral.setTransferencia(resumoReservas.getTransferencia().add(resumoAvulsas.getTransferencia()));
        resumoGeral.setFaturado(resumoReservas.getFaturado().add(resumoAvulsas.getFaturado()));
        resumoGeral.setTotalDescontos(resumoReservas.getTotalDescontos()); // Descontos só em reservas
        
        BigDecimal total = resumoGeral.getDinheiro()
            .add(resumoGeral.getPix())
            .add(resumoGeral.getCartaoDebito())
            .add(resumoGeral.getCartaoCredito())
            .add(resumoGeral.getTransferencia())
            .add(resumoGeral.getFaturado());
        
        resumoGeral.setTotal(total);
        
        return resumoGeral;
    }
    
    /**
     * 📄 PROCESSAR VENDAS AVULSAS FATURADAS (COM NOME DO CLIENTE)
     */
    private List<VendaAvulsaDTO> processarVendasAvulsasFaturadas(
            LocalDateTime dataInicio, 
            LocalDateTime dataFim
    ) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("📄 PROCESSANDO VENDAS AVULSAS FATURADAS");
        System.out.println("═══════════════════════════════════════════");
        
        List<VendaAvulsaDTO> vendasFaturadas = new ArrayList<>();
        
        // ✅ BUSCAR PAGAMENTOS FATURADOS SEM RESERVA
        List<Pagamento> pagamentosFaturados = pagamentoRepository
            .findByTipoAndDataHoraBetween("VENDA_AVULSA_FATURADA", dataInicio, dataFim);
        
        System.out.println("💰 Total de pagamentos faturados avulsos: " + pagamentosFaturados.size());
        
        for (Pagamento pag : pagamentosFaturados) {
            VendaAvulsaDTO venda = new VendaAvulsaDTO();
            venda.setNotaVendaId(pag.getId());
            venda.setDataHora(pag.getDataHora());
            venda.setFormaPagamento("FATURADO");
            venda.setValor(pag.getValor());
            
            // ✅ EXTRAIR NOME DO CLIENTE DA DESCRIÇÃO
            // Formato da descrição: "Venda balcão faturada #123 - João Silva"
            String nomeCliente = "Cliente Avulso";
            if (pag.getDescricao() != null && pag.getDescricao().contains(" - ")) {
                String[] partes = pag.getDescricao().split(" - ");
                if (partes.length > 1) {
                    nomeCliente = partes[1];
                }
            }
            venda.setClienteNome(nomeCliente);
            venda.setDescricao(pag.getDescricao());
            
            System.out.println("   📝 Venda #" + pag.getId() + 
                             " - Cliente: " + nomeCliente + 
                             " - Valor: R$ " + pag.getValor());
            
            vendasFaturadas.add(venda);
        }
        
        System.out.println("✅ Vendas avulsas faturadas processadas: " + vendasFaturadas.size());
        System.out.println("═══════════════════════════════════════════");
        
        return vendasFaturadas;
    }
    
    /**
     * 🧮 SOMAR PAGAMENTO NO RESUMO
     */
    private void somarPagamento(
            RelatorioFechamentoCaixaDTO.ResumoPorFormaPagamento resumo,
            Pagamento pagamento
    ) {
        BigDecimal valor = pagamento.getValor();
        
        switch (pagamento.getFormaPagamento()) {
            case DINHEIRO:
                resumo.setDinheiro(resumo.getDinheiro().add(valor));
                break;
            case PIX:
                resumo.setPix(resumo.getPix().add(valor));
                break;
            case CARTAO_DEBITO:
                resumo.setCartaoDebito(resumo.getCartaoDebito().add(valor));
                break;
            case CARTAO_CREDITO:
                resumo.setCartaoCredito(resumo.getCartaoCredito().add(valor));
                break;
            case TRANSFERENCIA:
                resumo.setTransferencia(resumo.getTransferencia().add(valor));
                break;
            case FATURADO:
                resumo.setFaturado(resumo.getFaturado().add(valor));
                break;
        }
        
        resumo.setTotal(resumo.getTotal().add(valor));
    }
}
