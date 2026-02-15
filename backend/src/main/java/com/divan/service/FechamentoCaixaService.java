package com.divan.service;

import com.divan.dto.FechamentoCaixaDTO;
import com.divan.entity.FechamentoCaixa;
import com.divan.entity.Pagamento;
import com.divan.entity.Usuario;
import com.divan.repository.FechamentoCaixaRepository;
import com.divan.repository.FechamentoCaixaDetalheRepository;
import com.divan.repository.PagamentoRepository;
import com.divan.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.divan.dto.ProdutoVendidoDTO;
import com.divan.dto.VendaDetalhadaDTO;
import com.divan.dto.RelatorioVendasCaixaDTO;
import com.divan.entity.NotaVenda;
import com.divan.entity.ItemVenda;
import com.divan.repository.NotaVendaRepository;
import com.divan.repository.ItemVendaRepository;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class FechamentoCaixaService {
    
    @Autowired
    private FechamentoCaixaRepository fechamentoCaixaRepository;
    
    @Autowired
    private FechamentoCaixaDetalheRepository fechamentoCaixaDetalheRepository;
    
    @Autowired
    private PagamentoRepository pagamentoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private NotaVendaRepository notaVendaRepository;

    @Autowired
    private ItemVendaRepository itemVendaRepository;
    
    /**
     * ✅ ABRIR CAIXA
     */
    @Transactional
    public FechamentoCaixaDTO abrirCaixa(Long usuarioId, String turno, String observacoes) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔓 SERVICE - ABRINDO CAIXA");
        System.out.println("   Usuário ID: " + usuarioId);
        System.out.println("   Turno: " + turno);
        System.out.println("═══════════════════════════════════════════");
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + usuarioId));
        
        System.out.println("✅ Usuário encontrado: " + usuario.getNome());
        
        Optional<FechamentoCaixa> caixaAbertoOpt = fechamentoCaixaRepository
            .findByUsuarioAndStatus(usuario, FechamentoCaixa.StatusCaixaEnum.ABERTO);
        
        if (caixaAbertoOpt.isPresent()) {
            FechamentoCaixa caixaExistente = caixaAbertoOpt.get();
            System.err.println("❌ Já existe um caixa aberto!");
            System.err.println("   ID: " + caixaExistente.getId());
            System.err.println("   Turno: " + caixaExistente.getTurno());
            System.err.println("   Aberto em: " + caixaExistente.getDataHoraAbertura());
            
            throw new RuntimeException(
                "Você já possui um caixa aberto! " +
                "Caixa #" + caixaExistente.getId() + 
                " - Turno: " + caixaExistente.getTurno()
            );
        }
        
        System.out.println("✅ Nenhum caixa aberto encontrado. Criando novo...");
        
        FechamentoCaixa caixa = new FechamentoCaixa();
        caixa.setUsuario(usuario);
        caixa.setTurno(turno);
        caixa.setDataHoraAbertura(LocalDateTime.now());
        caixa.setStatus(FechamentoCaixa.StatusCaixaEnum.ABERTO);
        caixa.setObservacoes(observacoes);
        
        caixa.setTotalDinheiro(BigDecimal.ZERO);
        caixa.setTotalPix(BigDecimal.ZERO);
        caixa.setTotalCartaoDebito(BigDecimal.ZERO);
        caixa.setTotalCartaoCredito(BigDecimal.ZERO);
        caixa.setTotalTransferencia(BigDecimal.ZERO);
        caixa.setTotalFaturado(BigDecimal.ZERO);
        caixa.setTotalDiarias(BigDecimal.ZERO);
        caixa.setTotalProdutos(BigDecimal.ZERO);
        caixa.setTotalBruto(BigDecimal.ZERO);
        caixa.setTotalLiquido(BigDecimal.ZERO);
        caixa.setTotalDescontos(BigDecimal.ZERO);
        caixa.setTotalEstornos(BigDecimal.ZERO);
        
        FechamentoCaixa caixaSalvo = fechamentoCaixaRepository.save(caixa);
        
        System.out.println("✅ Caixa criado com sucesso!");
        System.out.println("   ID: " + caixaSalvo.getId());
        System.out.println("   Turno: " + caixaSalvo.getTurno());
        System.out.println("   Data/Hora: " + caixaSalvo.getDataHoraAbertura());
        System.out.println("═══════════════════════════════════════════");
        
        return converterParaDTO(caixaSalvo);
    }
    
    /**
     * ✅ BUSCAR CAIXA ABERTO
     */
    @Transactional(readOnly = true)
    public FechamentoCaixaDTO buscarCaixaAberto(Long usuarioId) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔍 SERVICE - BUSCANDO CAIXA ABERTO");
        System.out.println("   Usuário ID: " + usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + usuarioId));
        
        System.out.println("✅ Usuário encontrado: " + usuario.getNome());
        
        Optional<FechamentoCaixa> caixaOpt = fechamentoCaixaRepository
            .findByUsuarioAndStatus(usuario, FechamentoCaixa.StatusCaixaEnum.ABERTO);
        
        if (caixaOpt.isPresent()) {
            FechamentoCaixa caixa = caixaOpt.get();
            System.out.println("✅ Caixa aberto encontrado!");
            System.out.println("   ID: " + caixa.getId());
            System.out.println("   Turno: " + caixa.getTurno());
            System.out.println("═══════════════════════════════════════════");
            
            return converterParaDTO(caixa);
        } else {
            System.out.println("📭 Nenhum caixa aberto encontrado");
            System.out.println("═══════════════════════════════════════════");
            
            return null;
        }
    }
    
    /**
     * 🔒 FECHAR CAIXA
     */
    @Transactional
    public FechamentoCaixaDTO fecharCaixa(Long caixaId, String observacoesFechamento) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔒 SERVICE - FECHANDO CAIXA");
        System.out.println("   Caixa ID: " + caixaId);
        
        FechamentoCaixa caixa = fechamentoCaixaRepository.findById(caixaId)
            .orElseThrow(() -> new RuntimeException("Caixa não encontrado com ID: " + caixaId));
        
        if (caixa.getStatus() == FechamentoCaixa.StatusCaixaEnum.FECHADO) {
            throw new RuntimeException("Caixa já está fechado!");
        }
        
        recalcularTotais(caixa);
        
        caixa.setDataHoraFechamento(LocalDateTime.now());
        caixa.setStatus(FechamentoCaixa.StatusCaixaEnum.FECHADO);
        
        if (observacoesFechamento != null && !observacoesFechamento.isEmpty()) {
            String obsAtual = caixa.getObservacoes() != null ? caixa.getObservacoes() : "";
            caixa.setObservacoes(obsAtual + "\n\nFECHAMENTO: " + observacoesFechamento);
        }
        
        FechamentoCaixa caixaFechado = fechamentoCaixaRepository.save(caixa);
        
        System.out.println("✅ Caixa fechado com sucesso!");
        System.out.println("   ID: " + caixaFechado.getId());
        System.out.println("   Total Líquido: R$ " + caixaFechado.getTotalLiquido());
        System.out.println("═══════════════════════════════════════════");
        
        return converterParaDTO(caixaFechado);
    }
    
    /**
     * 🔄 RECALCULAR TOTAIS DO CAIXA
     */
    private void recalcularTotais(FechamentoCaixa caixa) {
        System.out.println("🔄 Recalculando totais do caixa #" + caixa.getId());
        
        List<Pagamento> pagamentos = pagamentoRepository.findByCaixa(caixa);
        
        System.out.println("📊 Total de pagamentos encontrados: " + pagamentos.size());
        
        BigDecimal totalDinheiro = BigDecimal.ZERO;
        BigDecimal totalPix = BigDecimal.ZERO;
        BigDecimal totalCartaoDebito = BigDecimal.ZERO;
        BigDecimal totalCartaoCredito = BigDecimal.ZERO;
        BigDecimal totalTransferencia = BigDecimal.ZERO;
        BigDecimal totalFaturado = BigDecimal.ZERO;
        BigDecimal totalDiarias = BigDecimal.ZERO;
        BigDecimal totalProdutos = BigDecimal.ZERO;
        
        for (Pagamento pagamento : pagamentos) {
            BigDecimal valor = pagamento.getValor() != null ? pagamento.getValor() : BigDecimal.ZERO;
            
            if (pagamento.getFormaPagamento() != null) {
                String formaPagamento = pagamento.getFormaPagamento().toString();
                
                switch (formaPagamento) {
                    case "DINHEIRO":
                        totalDinheiro = totalDinheiro.add(valor);
                        break;
                    case "PIX":
                        totalPix = totalPix.add(valor);
                        break;
                    case "CARTAO_DEBITO":
                        totalCartaoDebito = totalCartaoDebito.add(valor);
                        break;
                    case "CARTAO_CREDITO":
                        totalCartaoCredito = totalCartaoCredito.add(valor);
                        break;
                    case "TRANSFERENCIA":
                        totalTransferencia = totalTransferencia.add(valor);
                        break;
                    case "FATURADO":
                        totalFaturado = totalFaturado.add(valor);
                        break;
                }
            }
            
            if (pagamento.getTipo() != null) {
                String tipo = pagamento.getTipo();
                
                if ("DIARIA".equals(tipo)) {
                    totalDiarias = totalDiarias.add(valor);
                } else if ("PRODUTO".equals(tipo)) {
                    totalProdutos = totalProdutos.add(valor);
                }
            }
        }
        
        caixa.setTotalDinheiro(totalDinheiro);
        caixa.setTotalPix(totalPix);
        caixa.setTotalCartaoDebito(totalCartaoDebito);
        caixa.setTotalCartaoCredito(totalCartaoCredito);
        caixa.setTotalTransferencia(totalTransferencia);
        caixa.setTotalFaturado(totalFaturado);
        caixa.setTotalDiarias(totalDiarias);
        caixa.setTotalProdutos(totalProdutos);
        
        BigDecimal totalBruto = totalDinheiro
            .add(totalPix)
            .add(totalCartaoDebito)
            .add(totalCartaoCredito)
            .add(totalTransferencia)
            .add(totalFaturado);
        
        caixa.setTotalBruto(totalBruto);
        
        BigDecimal totalLiquido = totalDinheiro
            .add(totalPix)
            .add(totalCartaoDebito)
            .add(totalCartaoCredito)
            .add(totalTransferencia);
        
        caixa.setTotalLiquido(totalLiquido);
        
        System.out.println("💰 Totais recalculados:");
        System.out.println("   Diárias: R$ " + totalDiarias);
        System.out.println("   Produtos: R$ " + totalProdutos);
        System.out.println("   Total Bruto: R$ " + totalBruto);
        System.out.println("   Total Líquido: R$ " + totalLiquido);
    }
    
    /**
     * ✅ LISTAR TODOS
     */
    @Transactional(readOnly = true)
    public List<FechamentoCaixaDTO> listarTodos() {
        List<FechamentoCaixa> caixas = fechamentoCaixaRepository.findAll();
        return caixas.stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * ✅ LISTAR POR PERÍODO (COM FILTROS)
     */
    @Transactional(readOnly = true)
    public List<FechamentoCaixaDTO> listarPorPeriodo(
        LocalDateTime dataInicio, 
        LocalDateTime dataFim,
        Long usuarioId,
        String status
    ) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("📅 SERVICE - LISTAR POR PERÍODO COM FILTROS");
        System.out.println("   Período: " + dataInicio + " até " + dataFim);
        System.out.println("   Usuário ID: " + (usuarioId != null ? usuarioId : "TODOS"));
        System.out.println("   Status: " + (status != null ? status : "TODOS"));

        List<FechamentoCaixa> caixas;

        if (usuarioId != null && status != null && !status.isEmpty()) {
            System.out.println("🔍 Filtrando por: Usuário + Status + Período");
            caixas = fechamentoCaixaRepository.buscarPorUsuarioStatusEPeriodo(
                usuarioId,
                FechamentoCaixa.StatusCaixaEnum.valueOf(status),
                dataInicio,
                dataFim
            );

        } else if (usuarioId != null) {
            System.out.println("🔍 Filtrando por: Usuário + Período");
            caixas = fechamentoCaixaRepository.buscarPorUsuarioEPeriodo(
                usuarioId,
                dataInicio,
                dataFim
            );

        } else if (status != null && !status.isEmpty()) {
            System.out.println("🔍 Filtrando por: Status + Período");
            caixas = fechamentoCaixaRepository.buscarPorStatusEPeriodo(
                FechamentoCaixa.StatusCaixaEnum.valueOf(status),
                dataInicio,
                dataFim
            );

        } else {
            System.out.println("🔍 Filtrando por: Período");
            caixas = fechamentoCaixaRepository.findByDataHoraAberturaBetween(
                dataInicio,
                dataFim
            );
        }

        System.out.println("✅ Encontrados: " + caixas.size() + " caixa(s)");
        System.out.println("═══════════════════════════════════════════");

        return caixas.stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * 🛒 BUSCAR VENDAS DETALHADAS DO CAIXA
     */
    @Transactional(readOnly = true)
    public RelatorioVendasCaixaDTO buscarVendasDetalhadas(Long caixaId) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🛒 BUSCANDO VENDAS DETALHADAS DO CAIXA #" + caixaId);
        
        FechamentoCaixa caixa = fechamentoCaixaRepository.findById(caixaId)
            .orElseThrow(() -> new RuntimeException("Caixa não encontrado"));
        
        RelatorioVendasCaixaDTO relatorio = new RelatorioVendasCaixaDTO(caixaId);
        
        List<Pagamento> todosPagamentos = pagamentoRepository.findByCaixa(caixa);
        
        System.out.println("📊 Total de pagamentos no caixa: " + todosPagamentos.size());
        
        List<Pagamento> pagamentos = todosPagamentos.stream()
            .filter(p -> p.getTipo() != null && 
                        (p.getTipo().equalsIgnoreCase("PRODUTO") || 
                         p.getTipo().equalsIgnoreCase("VENDA") ||
                         p.getTipo().equalsIgnoreCase("VENDA_AVULSA_FATURADA") ||
                         p.getTipo().equalsIgnoreCase("CONSUMO")))
            .collect(Collectors.toList());
        
        System.out.println("📊 Total de pagamentos de produtos/vendas: " + pagamentos.size());
        
        for (Pagamento pagamento : pagamentos) {
            if (pagamento.getReserva() != null) {
                List<NotaVenda> notas = notaVendaRepository.findByReserva(pagamento.getReserva());
                for (NotaVenda nota : notas) {
                    processarNotaVenda(relatorio, nota, pagamento);
                }
            }
        }
        
        calcularTotaisGerais(relatorio);
        
        System.out.println("✅ Relatório gerado com sucesso!");
        System.out.println("   Total de vendas: " + relatorio.getTotalVendas());
        System.out.println("   Total geral: R$ " + relatorio.getTotalGeral());
        System.out.println("═══════════════════════════════════════════");
        
        return relatorio;
    }
    
    /**
     * 🔄 PROCESSAR NOTA DE VENDA
     */
    private void processarNotaVenda(RelatorioVendasCaixaDTO relatorio, NotaVenda nota, Pagamento pagamento) {
        List<ItemVenda> itens = itemVendaRepository.findByNotaVendaIdWithProduto(nota.getId());
        
        if (!itens.isEmpty()) {
            VendaDetalhadaDTO vendaDTO = new VendaDetalhadaDTO();
            vendaDTO.setNotaVendaId(nota.getId());
            vendaDTO.setDataHora(nota.getDataHoraVenda());
            vendaDTO.setValorTotal(nota.getTotal());
            vendaDTO.setTipoVenda(nota.getTipoVenda().toString());
            
            List<ProdutoVendidoDTO> produtosDTO = new ArrayList<>();
            for (ItemVenda item : itens) {
                ProdutoVendidoDTO produtoDTO = new ProdutoVendidoDTO();
                produtoDTO.setProdutoId(item.getProduto().getId());
                produtoDTO.setNomeProduto(item.getProduto().getNomeProduto());
                produtoDTO.setQuantidade(item.getQuantidade());
                produtoDTO.setValorUnitario(item.getValorUnitario());
                produtoDTO.setTotalItem(item.getTotalItem());
                produtosDTO.add(produtoDTO);
            }
            vendaDTO.setProdutos(produtosDTO);
            
            String formaPagamento = pagamento.getFormaPagamento().toString();
            relatorio.getVendasPorFormaPagamento().get(formaPagamento).add(vendaDTO);
            
            BigDecimal totalAtual = relatorio.getTotaisPorFormaPagamento().get(formaPagamento);
            relatorio.getTotaisPorFormaPagamento().put(formaPagamento, totalAtual.add(vendaDTO.getValorTotal()));
            
            Integer qtdVendas = relatorio.getQuantidadeVendasPorFormaPagamento().get(formaPagamento);
            relatorio.getQuantidadeVendasPorFormaPagamento().put(formaPagamento, qtdVendas + 1);
            
            Integer qtdProdutos = relatorio.getQuantidadeProdutosPorFormaPagamento().get(formaPagamento);
            int totalProdutosVenda = produtosDTO.stream().mapToInt(ProdutoVendidoDTO::getQuantidade).sum();
            relatorio.getQuantidadeProdutosPorFormaPagamento().put(formaPagamento, qtdProdutos + totalProdutosVenda);
        }
    }
    
    /**
     * 🔄 CALCULAR TOTAIS GERAIS
     */
    private void calcularTotaisGerais(RelatorioVendasCaixaDTO relatorio) {
        BigDecimal totalGeral = BigDecimal.ZERO;
        int totalVendas = 0;
        int totalProdutos = 0;
        
        for (BigDecimal total : relatorio.getTotaisPorFormaPagamento().values()) {
            totalGeral = totalGeral.add(total);
        }
        
        for (Integer qtd : relatorio.getQuantidadeVendasPorFormaPagamento().values()) {
            totalVendas += qtd;
        }
        
        for (Integer qtd : relatorio.getQuantidadeProdutosPorFormaPagamento().values()) {
            totalProdutos += qtd;
        }
        
        relatorio.setTotalGeral(totalGeral);
        relatorio.setTotalVendas(totalVendas);
        relatorio.setTotalProdutos(totalProdutos);
    }
    
    /**
     * 🔄 CONVERTER ENTIDADE PARA DTO
     */
    private FechamentoCaixaDTO converterParaDTO(FechamentoCaixa caixa) {
        FechamentoCaixaDTO dto = new FechamentoCaixaDTO();
        
        dto.setId(caixa.getId());
        dto.setUsuarioId(caixa.getUsuario() != null ? caixa.getUsuario().getId() : null);
        dto.setUsuarioNome(caixa.getUsuario() != null ? caixa.getUsuario().getNome() : "N/A");
        dto.setDataHoraAbertura(caixa.getDataHoraAbertura());
        dto.setDataHoraFechamento(caixa.getDataHoraFechamento());
        dto.setStatus(caixa.getStatus() != null ? caixa.getStatus().name() : "ABERTO");
        dto.setTurno(caixa.getTurno());
        
        dto.setTotalDiarias(caixa.getTotalDiarias() != null ? caixa.getTotalDiarias() : BigDecimal.ZERO);
        dto.setTotalProdutos(caixa.getTotalProdutos() != null ? caixa.getTotalProdutos() : BigDecimal.ZERO);
        dto.setTotalDescontos(caixa.getTotalDescontos() != null ? caixa.getTotalDescontos() : BigDecimal.ZERO);
        dto.setTotalEstornos(caixa.getTotalEstornos() != null ? caixa.getTotalEstornos() : BigDecimal.ZERO);
        dto.setTotalBruto(caixa.getTotalBruto() != null ? caixa.getTotalBruto() : BigDecimal.ZERO);
        dto.setTotalLiquido(caixa.getTotalLiquido() != null ? caixa.getTotalLiquido() : BigDecimal.ZERO);
        
        dto.setTotalDinheiro(caixa.getTotalDinheiro() != null ? caixa.getTotalDinheiro() : BigDecimal.ZERO);
        dto.setTotalPix(caixa.getTotalPix() != null ? caixa.getTotalPix() : BigDecimal.ZERO);
        dto.setTotalCartaoDebito(caixa.getTotalCartaoDebito() != null ? caixa.getTotalCartaoDebito() : BigDecimal.ZERO);
        dto.setTotalCartaoCredito(caixa.getTotalCartaoCredito() != null ? caixa.getTotalCartaoCredito() : BigDecimal.ZERO);
        dto.setTotalTransferencia(caixa.getTotalTransferencia() != null ? caixa.getTotalTransferencia() : BigDecimal.ZERO);
        dto.setTotalFaturado(caixa.getTotalFaturado() != null ? caixa.getTotalFaturado() : BigDecimal.ZERO);
        
        dto.setObservacoes(caixa.getObservacoes());
        
        return dto;
    }
}