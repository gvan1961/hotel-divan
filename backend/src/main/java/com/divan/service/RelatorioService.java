package com.divan.service;

import com.divan.dto.*;
import com.divan.entity.*;
import com.divan.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import com.divan.enums.FormaPagamento;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RelatorioService {
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private ApartamentoRepository apartamentoRepository;
    
    @Autowired
    private PagamentoRepository pagamentoRepository;
    
    @Autowired
    private NotaVendaRepository notaVendaRepository;
    
    @Autowired
    private ItemVendaRepository itemVendaRepository;
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private ContaAReceberRepository contaReceberRepository;
    
    @Autowired
    private ExtratoReservaRepository extratoRepository;
    
    // ========================================
    // RELATÓRIO DE OCUPAÇÃO
    // ========================================
    
    public RelatorioOcupacaoDTO gerarRelatorioOcupacao(LocalDate data) {
        RelatorioOcupacaoDTO relatorio = new RelatorioOcupacaoDTO();
        relatorio.setData(data);
        
        List<Apartamento> apartamentos = apartamentoRepository.findAll();
        relatorio.setTotalApartamentos(apartamentos.size());
        
        Map<Apartamento.StatusEnum, Long> statusCount = apartamentos.stream()
            .collect(Collectors.groupingBy(Apartamento::getStatus, Collectors.counting()));
        
        relatorio.setApartamentosOcupados(statusCount.getOrDefault(Apartamento.StatusEnum.OCUPADO, 0L).intValue());
        relatorio.setApartamentosDisponiveis(statusCount.getOrDefault(Apartamento.StatusEnum.DISPONIVEL, 0L).intValue());
        relatorio.setApartamentosLimpeza(statusCount.getOrDefault(Apartamento.StatusEnum.LIMPEZA, 0L).intValue());
        relatorio.setApartamentosManutencao(statusCount.getOrDefault(Apartamento.StatusEnum.MANUTENCAO, 0L).intValue());
        
        double taxaOcupacao = apartamentos.size() > 0 
            ? (relatorio.getApartamentosOcupados() * 100.0) / apartamentos.size() 
            : 0.0;
        relatorio.setTaxaOcupacao(Math.round(taxaOcupacao * 100.0) / 100.0);
        
        // Total de hóspedes
        List<Reserva> reservasAtivas = reservaRepository.findReservasAtivas();
        int totalHospedes = reservasAtivas.stream()
            .mapToInt(Reserva::getQuantidadeHospede)
            .sum();
        relatorio.setTotalHospedes(totalHospedes);
        
        // Receita do dia
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.atTime(LocalTime.MAX);
        List<Pagamento> pagamentos = pagamentoRepository.findPagamentosPorPeriodo(inicioDia, fimDia);
        BigDecimal receitaDia = pagamentos.stream()
            .map(Pagamento::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        relatorio.setReceitaDiaria(receitaDia);
        
        return relatorio;
    }
    
    // ========================================
    // RELATÓRIO DE FATURAMENTO
    // ========================================
    
    public RelatorioFaturamentoDTO gerarRelatorioFaturamento(LocalDate dataInicio, LocalDate dataFim) {
        RelatorioFaturamentoDTO relatorio = new RelatorioFaturamentoDTO();
        relatorio.setDataInicio(dataInicio);
        relatorio.setDataFim(dataFim);
        
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(LocalTime.MAX);
        
        // Buscar reservas do período
        List<Reserva> reservas = reservaRepository.findReservasPorPeriodo(inicio, fim);
        relatorio.setQuantidadeReservas(reservas.size());
        
        // Calcular receitas
        BigDecimal receitaDiarias = reservas.stream()
            .map(Reserva::getTotalDiaria)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        relatorio.setReceitaDiarias(receitaDiarias);
        
        BigDecimal receitaProdutos = reservas.stream()
            .map(Reserva::getTotalProduto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        relatorio.setReceitaProdutos(receitaProdutos);
        
        relatorio.setReceitaTotal(receitaDiarias.add(receitaProdutos));
        
        // Pagamentos por forma
        List<Pagamento> pagamentos = pagamentoRepository.findPagamentosPorPeriodo(inicio, fim);
        Map<FormaPagamento, BigDecimal> pagamentosPorForma = pagamentos.stream()
            .collect(Collectors.groupingBy(
                Pagamento::getFormaPagamento,
                Collectors.reducing(BigDecimal.ZERO, Pagamento::getValor, BigDecimal::add)
            ));

        relatorio.setPagamentoDinheiro(pagamentosPorForma.getOrDefault(FormaPagamento.DINHEIRO, BigDecimal.ZERO));
        relatorio.setPagamentoPix(pagamentosPorForma.getOrDefault(FormaPagamento.PIX, BigDecimal.ZERO));
        relatorio.setPagamentoCartaoDebito(pagamentosPorForma.getOrDefault(FormaPagamento.CARTAO_DEBITO, BigDecimal.ZERO));
        relatorio.setPagamentoCartaoCredito(pagamentosPorForma.getOrDefault(FormaPagamento.CARTAO_CREDITO, BigDecimal.ZERO));
        relatorio.setPagamentoTransferencia(pagamentosPorForma.getOrDefault(FormaPagamento.TRANSFERENCIA, BigDecimal.ZERO));
        relatorio.setPagamentoFaturado(pagamentosPorForma.getOrDefault(FormaPagamento.FATURADO, BigDecimal.ZERO));
        
        // Estatísticas
        long checkIns = reservas.stream()
            .filter(r -> !r.getDataCheckin().toLocalDate().isBefore(dataInicio) && 
                         !r.getDataCheckin().toLocalDate().isAfter(dataFim))
            .count();
        relatorio.setQuantidadeCheckIns((int) checkIns);
        
        long checkOuts = reservas.stream()
            .filter(r -> !r.getDataCheckout().toLocalDate().isBefore(dataInicio) && 
                         !r.getDataCheckout().toLocalDate().isAfter(dataFim))
            .count();
        relatorio.setQuantidadeCheckOuts((int) checkOuts);
        
        long cancelamentos = reservas.stream()
            .filter(r -> r.getStatus() == Reserva.StatusReservaEnum.CANCELADA)
            .count();
        relatorio.setQuantidadeCancelamentos((int) cancelamentos);
        
        // Ticket Médio
        if (reservas.size() > 0) {
            relatorio.setTicketMedio(relatorio.getReceitaTotal()
                .divide(BigDecimal.valueOf(reservas.size()), 2, RoundingMode.HALF_UP));
            
            double mediaHospedes = reservas.stream()
                .mapToInt(Reserva::getQuantidadeHospede)
                .average()
                .orElse(0.0);
            relatorio.setMediaHospedes(BigDecimal.valueOf(mediaHospedes).setScale(2, RoundingMode.HALF_UP));
            
            double mediaDiarias = reservas.stream()
                .mapToInt(Reserva::getQuantidadeDiaria)
                .average()
                .orElse(0.0);
            relatorio.setMediaDiarias(BigDecimal.valueOf(mediaDiarias).setScale(2, RoundingMode.HALF_UP));
        } else {
            relatorio.setTicketMedio(BigDecimal.ZERO);
            relatorio.setMediaHospedes(BigDecimal.ZERO);
            relatorio.setMediaDiarias(BigDecimal.ZERO);
        }
        
        return relatorio;
    }
    
    // ========================================
    // RELATÓRIO DE PRODUTOS
    // ========================================
    
    public List<RelatorioProdutoDTO> gerarRelatorioProdutos(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(LocalTime.MAX);
        
        List<ItemVenda> itens = itemVendaRepository.findItensPorPeriodo(inicio, fim);
        
        Map<Long, List<ItemVenda>> itensPorProduto = itens.stream()
            .collect(Collectors.groupingBy(item -> item.getProduto().getId()));
        
        return itensPorProduto.entrySet().stream()
            .map(entry -> {
                Long produtoId = entry.getKey();
                List<ItemVenda> itensDoProduct = entry.getValue();
                
                Produto produto = itensDoProduct.get(0).getProduto();
                
                int quantidadeVendida = itensDoProduct.stream()
                    .mapToInt(ItemVenda::getQuantidade)
                    .sum();
                
                BigDecimal valorTotal = itensDoProduct.stream()
                    .map(ItemVenda::getTotalItem)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal valorMedio = valorTotal.divide(
                    BigDecimal.valueOf(quantidadeVendida), 
                    2, 
                    RoundingMode.HALF_UP
                );
                
                RelatorioProdutoDTO dto = new RelatorioProdutoDTO();
                dto.setProdutoId(produtoId);
                dto.setNomeProduto(produto.getNomeProduto());
                dto.setCategoria(produto.getCategoria().getNome());
                dto.setQuantidadeVendida(quantidadeVendida);
                dto.setValorTotal(valorTotal);
                dto.setValorMedio(valorMedio);
                dto.setQuantidadeVendas(itensDoProduct.size());
                
                return dto;
            })
            .sorted((a, b) -> b.getQuantidadeVendida().compareTo(a.getQuantidadeVendida()))
            .collect(Collectors.toList());
    }
    
    // ========================================
    // RELATÓRIO DE APARTAMENTOS
    // ========================================
    
    public List<RelatorioApartamentoDTO> gerarRelatorioApartamentos(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(LocalTime.MAX);
        
        List<Apartamento> apartamentos = apartamentoRepository.findAll();
        List<Reserva> todasReservas = reservaRepository.findReservasPorPeriodo(inicio, fim);
        
        return apartamentos.stream()
            .map(apto -> {
                List<Reserva> reservasDoApto = todasReservas.stream()
                    .filter(r -> r.getApartamento().getId().equals(apto.getId()))
                    .collect(Collectors.toList());
                
                int diasOcupados = reservasDoApto.stream()
                    .mapToInt(Reserva::getQuantidadeDiaria)
                    .sum();
                
                long diasPeriodo = dataInicio.datesUntil(dataFim.plusDays(1)).count();
                double taxaOcupacao = diasPeriodo > 0 
                    ? (diasOcupados * 100.0) / diasPeriodo 
                    : 0.0;
                
                BigDecimal receitaTotal = reservasDoApto.stream()
                    .map(Reserva::getTotalHospedagem)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal receitaMedia = reservasDoApto.size() > 0
                    ? receitaTotal.divide(BigDecimal.valueOf(reservasDoApto.size()), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
                
                RelatorioApartamentoDTO dto = new RelatorioApartamentoDTO();
                dto.setNumeroApartamento(apto.getNumeroApartamento());
                dto.setTipoApartamento(apto.getTipoApartamento().getTipo().name());
                dto.setQuantidadeReservas(reservasDoApto.size());
                dto.setDiasOcupados(diasOcupados);
                dto.setTaxaOcupacao(Math.round(taxaOcupacao * 100.0) / 100.0);
                dto.setReceitaTotal(receitaTotal);
                dto.setReceitaMedia(receitaMedia);
                
                return dto;
            })
            .sorted((a, b) -> b.getReceitaTotal().compareTo(a.getReceitaTotal()))
            .collect(Collectors.toList());
    }
    
    // ========================================
    // DASHBOARD
    // ========================================
    
    public DashboardDTO gerarDashboard() {
        LocalDate hoje = LocalDate.now();
        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setData(hoje);
        
        // Ocupação Hoje
        RelatorioOcupacaoDTO ocupacao = gerarRelatorioOcupacao(hoje);
        dashboard.setApartamentosOcupados(ocupacao.getApartamentosOcupados());
        dashboard.setApartamentosDisponiveis(ocupacao.getApartamentosDisponiveis());
        dashboard.setTaxaOcupacao(ocupacao.getTaxaOcupacao());
        dashboard.setTotalHospedes(ocupacao.getTotalHospedes());
        
        // Financeiro
        dashboard.setReceitaDia(ocupacao.getReceitaDiaria());
        
        LocalDate primeiroDiaMes = hoje.withDayOfMonth(1);
        RelatorioFaturamentoDTO faturamentoMes = gerarRelatorioFaturamento(primeiroDiaMes, hoje);
        dashboard.setReceitaMes(faturamentoMes.getReceitaTotal());
        
        List<Pagamento> pagamentosHoje = pagamentoRepository.findPagamentosDoDia(hoje);
        BigDecimal pagamentosRecebidos = pagamentosHoje.stream()
            .map(Pagamento::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.setPagamentosRecebidos(pagamentosRecebidos);
        
        List<ContaAReceber> contasAReceber = contaReceberRepository.findContasEmAberto();
        BigDecimal totalContasReceber = contasAReceber.stream()
            .map(ContaAReceber::getSaldo)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.setContasReceber(totalContasReceber);
        
        // Movimentação
        List<Reserva> checkInsHoje = reservaRepository.findReservasParaCheckinNaData(hoje.atStartOfDay());
        dashboard.setCheckInsHoje(checkInsHoje.size());
        
        List<Reserva> checkOutsHoje = reservaRepository.findReservasParaCheckoutNaData(hoje.atStartOfDay());
        dashboard.setCheckOutsHoje(checkOutsHoje.size());
        
        List<Reserva> reservasAtivas = reservaRepository.findReservasAtivas();
        dashboard.setReservasAtivas(reservasAtivas.size());
        
        // Produtos
        List<Produto> semEstoque = produtoRepository.findProdutosSemEstoque();
        dashboard.setProdutosSemEstoque(semEstoque.size());
        
        List<Produto> estoqueBaixo = produtoRepository.findProdutosComEstoqueBaixo();
        dashboard.setProdutosEstoqueBaixo(estoqueBaixo.size());
        
        // Gráficos - Últimos 7 dias
        List<RelatorioOcupacaoDTO> ocupacao7Dias = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate data = hoje.minusDays(i);
            ocupacao7Dias.add(gerarRelatorioOcupacao(data));
        }
        dashboard.setOcupacaoUltimos7Dias(ocupacao7Dias);
        
        // Top 5 Produtos
        List<RelatorioProdutoDTO> todosProdutos = gerarRelatorioProdutos(hoje.minusDays(30), hoje);
        dashboard.setTop5ProdutosMaisVendidos(
            todosProdutos.stream().limit(5).collect(Collectors.toList())
        );
        
        return dashboard;
    }
}
