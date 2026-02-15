package com.divan.controller;

import java.util.stream.Collectors;
import com.divan.dto.FechamentoCaixaDTO;
import com.divan.dto.FechamentoCaixaDetalheDTO;
import com.divan.entity.FechamentoCaixa;
import com.divan.entity.FechamentoCaixaDetalhe;
import com.divan.entity.Pagamento;
import com.divan.entity.Reserva;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import com.divan.repository.FechamentoCaixaRepository;
import com.divan.repository.FechamentoCaixaDetalheRepository;
import com.divan.repository.PagamentoRepository;
import com.divan.service.FechamentoCaixaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import com.divan.dto.RelatorioVendasCaixaDTO;
import java.math.BigDecimal;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fechamento-caixa")
@CrossOrigin(origins = "*")
public class FechamentoCaixaController {
    
    @Autowired
    private FechamentoCaixaService fechamentoCaixaService;
    
    @Autowired
    private FechamentoCaixaRepository fechamentoCaixaRepository;
    
    @Autowired
    private FechamentoCaixaDetalheRepository fechamentoCaixaDetalheRepository;
    
    @Autowired
    private PagamentoRepository pagamentoRepository;
    
    /**
     * 🔓 ABRIR CAIXA
     */
    @PostMapping("/abrir")
    public ResponseEntity<?> abrirCaixa(@RequestBody Map<String, Object> request) {
        try {
            Long usuarioId = Long.valueOf(request.get("usuarioId").toString());
            String turno = request.get("turno").toString();
            String observacoes = request.get("observacoes") != null ? 
                request.get("observacoes").toString() : null;
            
            System.out.println("═══════════════════════════════════════════");
            System.out.println("🔓 ABERTURA DE CAIXA");
            System.out.println("═══════════════════════════════════════════");
            System.out.println("   Usuário ID: " + usuarioId);
            System.out.println("   Turno: " + turno);
            System.out.println("   Observações: " + observacoes);
            
            FechamentoCaixaDTO caixa = fechamentoCaixaService.abrirCaixa(usuarioId, turno, observacoes);
            
            System.out.println("✅ Caixa aberto com sucesso!");
            System.out.println("   ID: " + caixa.getId());
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "sucesso", true,
                "mensagem", "Caixa aberto com sucesso!",
                "caixa", caixa
            ));
            
        } catch (RuntimeException e) {
            System.err.println("❌ Erro ao abrir caixa: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "sucesso", false,
                "erro", "Erro interno ao abrir caixa: " + e.getMessage()
            ));
        }
    }    
        
    /**
     * 🔍 BUSCAR CAIXA ABERTO DO USUÁRIO
     */
    @GetMapping("/aberto")
    public ResponseEntity<?> buscarCaixaAberto(@RequestParam Long usuarioId) {
        try {
            System.out.println("═══════════════════════════════════════════");
            System.out.println("🔍 BUSCANDO CAIXA ABERTO");
            System.out.println("   Usuário ID: " + usuarioId);
            
            FechamentoCaixaDTO caixa = fechamentoCaixaService.buscarCaixaAberto(usuarioId);
            
            if (caixa != null) {
                System.out.println("✅ Caixa aberto encontrado!");
                System.out.println("   ID: " + caixa.getId());
                System.out.println("   Turno: " + caixa.getTurno());
                System.out.println("═══════════════════════════════════════════");
                
                return ResponseEntity.ok(caixa);
            } else {
                System.out.println("📭 Nenhum caixa aberto encontrado");
                System.out.println("═══════════════════════════════════════════");
                
                return ResponseEntity.ok(Map.of("caixaAberto", false));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    
    
    
    /**
     * 🔒 FECHAR CAIXA
     */
    @PostMapping("/{id}/fechar")
    public ResponseEntity<?> fecharCaixa(
        @PathVariable Long id,
        @RequestBody(required = false) Map<String, Object> dados
    ) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔒 CONTROLLER - FECHAR CAIXA");
        System.out.println("   ID: " + id);
        System.out.println("   Dados: " + dados);
        
        // ✅ EXTRAIR OBSERVAÇÕES DO MAP
        String observacoes = "";
        if (dados != null && dados.containsKey("observacoes")) {
            observacoes = (String) dados.get("observacoes");
        }
        
        System.out.println("   Observações: " + observacoes);
        System.out.println("═══════════════════════════════════════════");
        
        // ✅ CHAMAR O SERVICE COM STRING
        FechamentoCaixaDTO caixaFechado = fechamentoCaixaService.fecharCaixa(id, observacoes);
        
        return ResponseEntity.ok(caixaFechado);
    }
    
    
    
    /**
     * 📦 BUSCAR CAIXA POR ID (COM RECÁLCULO DE TOTAIS)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            System.out.println("═══════════════════════════════════════════");
            System.out.println("📦 BUSCANDO CAIXA ID: " + id);
            
            // Buscar o caixa
            FechamentoCaixa caixa = fechamentoCaixaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caixa não encontrado"));
            
            // Recalcular totais
            caixa = recalcularTotaisCaixa(caixa);
            
            // Montar DTO
            FechamentoCaixaDTO dto = montarDTO(caixa);
            
            // Buscar detalhes
            List<FechamentoCaixaDetalhe> detalhes = 
                fechamentoCaixaDetalheRepository.findByFechamentoCaixaIdOrderByDataHoraDesc(id);
            dto.setDetalhes(converterDetalhesParaDTO(detalhes));
            
            System.out.println("✅ Caixa encontrado!");
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erro ao buscar caixa: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * 🖨️ IMPRIMIR FECHAMENTO DE CAIXA
     */
    @GetMapping("/{id}/imprimir")
    public ResponseEntity<?> imprimirFechamento(@PathVariable Long id) {
        try {
            System.out.println("═══════════════════════════════════════════");
            System.out.println("🖨️ GERANDO IMPRESSÃO DO CAIXA #" + id);
            System.out.println("═══════════════════════════════════════════");
            
            FechamentoCaixa caixa = fechamentoCaixaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caixa não encontrado"));
            
            // Recalcular totais
            caixa = recalcularTotaisCaixa(caixa);
            
            // Montar DTO
            FechamentoCaixaDTO dto = montarDTO(caixa);
            
            // Buscar detalhes
            List<FechamentoCaixaDetalhe> detalhes = 
                fechamentoCaixaDetalheRepository.findByFechamentoCaixaIdOrderByDataHoraDesc(id);
            dto.setDetalhes(converterDetalhesParaDTO(detalhes));
            
            System.out.println("✅ Impressão gerada com sucesso!");
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erro ao gerar impressão: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * 📊 GERAR RELATÓRIO (HTML PARA VISUALIZAÇÃO)
     */
    @GetMapping("/{id}/relatorio")
    public ResponseEntity<?> gerarRelatorio(@PathVariable Long id) {
        try {
            System.out.println("═══════════════════════════════════════════");
            System.out.println("📊 GERANDO RELATÓRIO HTML DO CAIXA #" + id);
            System.out.println("═══════════════════════════════════════════");
            
            // Buscar o caixa
            FechamentoCaixa caixa = fechamentoCaixaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caixa não encontrado"));
            
            System.out.println("✅ Caixa encontrado!");
            
            // Buscar todos os pagamentos do caixa
            List<Pagamento> todosPagamentos = pagamentoRepository.findByCaixaId(caixa.getId());
            
            System.out.println("📊 Total de pagamentos: " + todosPagamentos.size());
            
            // Separar pagamentos de RESERVAS e AVULSOS
            List<Pagamento> pagamentosReservas = todosPagamentos.stream()
                .filter(p -> p.getReserva() != null)
                .collect(Collectors.toList());
            
            List<Pagamento> pagamentosAvulsos = todosPagamentos.stream()
                .filter(p -> p.getReserva() == null)
                .collect(Collectors.toList());
            
            // Montar resposta
            Map<String, Object> relatorio = new java.util.HashMap<>();
            
            // Informações básicas
            relatorio.put("caixaId", caixa.getId());
            relatorio.put("recepcionistaNome", caixa.getUsuario() != null ? caixa.getUsuario().getNome() : "N/A");
            relatorio.put("dataHoraAbertura", caixa.getDataHoraAbertura());
            relatorio.put("dataHoraFechamento", caixa.getDataHoraFechamento());
            relatorio.put("status", caixa.getStatus() != null ? caixa.getStatus().name() : "ABERTO");
            relatorio.put("turno", caixa.getTurno());
            
            // Calcular subtotal reservas
            Map<String, BigDecimal> subtotalReservas = calcularTotaisPorFormaPagamento(pagamentosReservas);
            relatorio.put("subtotalReservas", subtotalReservas);
            
            // Vendas por apartamento
            relatorio.put("vendasReservas", agruparVendasPorApartamento(pagamentosReservas));
            
            // Vendas avulsas
            Map<String, BigDecimal> vendasAvulsas = calcularTotaisPorFormaPagamento(pagamentosAvulsos);
            relatorio.put("vendasAvulsas", vendasAvulsas);
            
            // Total geral
            Map<String, BigDecimal> totalGeral = calcularTotaisPorFormaPagamento(todosPagamentos);
            relatorio.put("totalGeral", totalGeral);
            
            System.out.println("✅ Relatório gerado com sucesso!");
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(relatorio);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao gerar relatório: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * 🔄 RECALCULAR TOTAIS DO CAIXA
     */
    private FechamentoCaixa recalcularTotaisCaixa(FechamentoCaixa caixa) {
        System.out.println("🔄 Recalculando totais do caixa #" + caixa.getId());
        
        // Buscar todos os pagamentos do caixa
        List<Pagamento> pagamentos = pagamentoRepository.findByCaixaId(caixa.getId());
        
        System.out.println("📊 Total de pagamentos encontrados: " + pagamentos.size());
        
        // Resetar totais
        caixa.setTotalDinheiro(BigDecimal.ZERO);
        caixa.setTotalPix(BigDecimal.ZERO);
        caixa.setTotalCartaoDebito(BigDecimal.ZERO);
        caixa.setTotalCartaoCredito(BigDecimal.ZERO);
        caixa.setTotalTransferencia(BigDecimal.ZERO);
        caixa.setTotalFaturado(BigDecimal.ZERO);
        caixa.setTotalDiarias(BigDecimal.ZERO);
        caixa.setTotalProdutos(BigDecimal.ZERO);
        
        // Calcular por forma de pagamento e tipo
        for (Pagamento pag : pagamentos) {
            BigDecimal valor = pag.getValor();
            
            System.out.println("💰 Pagamento: " + pag.getFormaPagamento() + 
                " | " + pag.getTipo() + " = R$ " + valor);
            
            // Por forma de pagamento
            if (pag.getFormaPagamento() != null) {
                switch (pag.getFormaPagamento()) {
                    case DINHEIRO:
                        caixa.setTotalDinheiro(caixa.getTotalDinheiro().add(valor));
                        break;
                    case PIX:
                        caixa.setTotalPix(caixa.getTotalPix().add(valor));
                        break;
                    case CARTAO_DEBITO:
                        caixa.setTotalCartaoDebito(caixa.getTotalCartaoDebito().add(valor));
                        break;
                    case CARTAO_CREDITO:
                        caixa.setTotalCartaoCredito(caixa.getTotalCartaoCredito().add(valor));
                        break;
                    case TRANSFERENCIA:
                        caixa.setTotalTransferencia(caixa.getTotalTransferencia().add(valor));
                        break;
                    case FATURADO:
                        caixa.setTotalFaturado(caixa.getTotalFaturado().add(valor));
                        break;
                }
            }
            
            // Por tipo
            if (pag.getTipo() != null) {
                String tipo = pag.getTipo().toUpperCase();
                if (tipo.contains("DIARIA") || tipo.equals("HOSPEDAGEM")) {
                    caixa.setTotalDiarias(caixa.getTotalDiarias().add(valor));
                } else if (tipo.contains("PRODUTO") || tipo.contains("CONSUMO") || tipo.equals("VENDA")) {
                    caixa.setTotalProdutos(caixa.getTotalProdutos().add(valor));
                }
            }
        }
        
        // Calcular totais gerais
        BigDecimal totalBruto = caixa.getTotalDiarias().add(caixa.getTotalProdutos());
        caixa.setTotalBruto(totalBruto);
        
        BigDecimal descontos = caixa.getTotalDescontos() != null ? 
            caixa.getTotalDescontos() : BigDecimal.ZERO;
        caixa.setTotalLiquido(totalBruto.subtract(descontos));
        
        System.out.println("💰 Totais recalculados:");
        System.out.println("   Diárias: R$ " + caixa.getTotalDiarias());
        System.out.println("   Produtos: R$ " + caixa.getTotalProdutos());
        System.out.println("   Total Bruto: R$ " + caixa.getTotalBruto());
        System.out.println("   Total Líquido: R$ " + caixa.getTotalLiquido());
        
        return fechamentoCaixaRepository.save(caixa);
    }
    
    /**
     * 📋 MONTAR DTO
     */
    private FechamentoCaixaDTO montarDTO(FechamentoCaixa caixa) {
        FechamentoCaixaDTO dto = new FechamentoCaixaDTO();
        dto.setId(caixa.getId());
        dto.setUsuarioId(caixa.getUsuario() != null ? caixa.getUsuario().getId() : null);
        dto.setUsuarioNome(caixa.getUsuario() != null ? caixa.getUsuario().getNome() : "N/A");
        
        // Datas - passar LocalDateTime direto
        dto.setDataHoraAbertura(caixa.getDataHoraAbertura());
        dto.setDataHoraFechamento(caixa.getDataHoraFechamento());
        
        // Status - converter enum para String
        dto.setStatus(caixa.getStatus() != null ? caixa.getStatus().name() : "ABERTO");
        dto.setTurno(caixa.getTurno());
        
        // Totais
        dto.setTotalDiarias(caixa.getTotalDiarias() != null ? caixa.getTotalDiarias() : BigDecimal.ZERO);
        dto.setTotalProdutos(caixa.getTotalProdutos() != null ? caixa.getTotalProdutos() : BigDecimal.ZERO);
        dto.setTotalDescontos(caixa.getTotalDescontos() != null ? caixa.getTotalDescontos() : BigDecimal.ZERO);
        dto.setTotalEstornos(caixa.getTotalEstornos() != null ? caixa.getTotalEstornos() : BigDecimal.ZERO);
        dto.setTotalBruto(caixa.getTotalBruto() != null ? caixa.getTotalBruto() : BigDecimal.ZERO);
        dto.setTotalLiquido(caixa.getTotalLiquido() != null ? caixa.getTotalLiquido() : BigDecimal.ZERO);
        
        // Formas de pagamento
        dto.setTotalDinheiro(caixa.getTotalDinheiro() != null ? caixa.getTotalDinheiro() : BigDecimal.ZERO);
        dto.setTotalPix(caixa.getTotalPix() != null ? caixa.getTotalPix() : BigDecimal.ZERO);
        dto.setTotalCartaoDebito(caixa.getTotalCartaoDebito() != null ? caixa.getTotalCartaoDebito() : BigDecimal.ZERO);
        dto.setTotalCartaoCredito(caixa.getTotalCartaoCredito() != null ? caixa.getTotalCartaoCredito() : BigDecimal.ZERO);
        dto.setTotalTransferencia(caixa.getTotalTransferencia() != null ? caixa.getTotalTransferencia() : BigDecimal.ZERO);
        dto.setTotalFaturado(caixa.getTotalFaturado() != null ? caixa.getTotalFaturado() : BigDecimal.ZERO);
        
        dto.setObservacoes(caixa.getObservacoes());
        
        return dto;
    }
    
    /**
     * 📋 CONVERTER DETALHES PARA DTO
     */
    private List<FechamentoCaixaDetalheDTO> converterDetalhesParaDTO(
        List<FechamentoCaixaDetalhe> detalhes) {
        
        return detalhes.stream().map(d -> {
            FechamentoCaixaDetalheDTO dto = new FechamentoCaixaDetalheDTO();
            dto.setId(d.getId());
            dto.setTipo(d.getTipo());
            dto.setDescricao(d.getDescricao());
            dto.setApartamentoNumero(d.getApartamentoNumero());
            dto.setReservaId(d.getReservaId());
            dto.setValor(d.getValor());
            dto.setFormaPagamento(d.getFormaPagamento());
            
            // Passar LocalDateTime direto
            dto.setDataHora(d.getDataHora());
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * 📋 LISTAR TODOS OS CAIXAS
     */
    @GetMapping
    public ResponseEntity<?> listarTodos() {
        try {
            List<FechamentoCaixaDTO> caixas = fechamentoCaixaService.listarTodos();
            return ResponseEntity.ok(caixas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
        
    /**
     * 📅 LISTAR CAIXAS POR PERÍODO (COM FILTROS OPCIONAIS)
     */     
    @GetMapping("/periodo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FechamentoCaixaDTO>> listarPorPeriodo(
        @RequestParam String dataInicio,
        @RequestParam String dataFim,
        @RequestParam(required = false) Long usuarioId,
        @RequestParam(required = false) String status
    ) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🎯 CONTROLLER - ENDPOINT /periodo CHAMADO!");
        System.out.println("   Data Início: " + dataInicio);
        System.out.println("   Data Fim: " + dataFim);
        System.out.println("   Usuário ID: " + (usuarioId != null ? usuarioId : "TODOS"));
        System.out.println("   Status: " + (status != null ? status : "TODOS"));
        System.out.println("═══════════════════════════════════════════");

        try {
            LocalDateTime dataInicioConvertida = LocalDateTime.parse(dataInicio);
            LocalDateTime dataFimConvertida = LocalDateTime.parse(dataFim);

            List<FechamentoCaixaDTO> caixas = fechamentoCaixaService.listarPorPeriodo(
                dataInicioConvertida,
                dataFimConvertida,
                usuarioId,
                status
            );

            System.out.println("✅ Total retornado: " + caixas.size());

            return ResponseEntity.ok(caixas);

        } catch (DateTimeParseException e) {
            System.err.println("❌ Erro ao converter datas: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ Erro geral: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
        
    
    /**
     * 📊 GERAR RELATÓRIO DETALHADO
     */
    @GetMapping("/{id}/relatorio-detalhado")
    public ResponseEntity<?> buscarRelatorioDetalhado(@PathVariable Long id) {
        try {
            System.out.println("═══════════════════════════════════════════");
            System.out.println("📊 GERANDO RELATÓRIO DETALHADO DO CAIXA #" + id);
            System.out.println("═══════════════════════════════════════════");
            
            // Buscar o caixa
            FechamentoCaixa caixa = fechamentoCaixaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caixa não encontrado"));
            
            // Buscar todos os pagamentos do caixa
            List<Pagamento> todosPagamentos = pagamentoRepository.findByCaixaId(caixa.getId());
            
            System.out.println("📊 Total de pagamentos: " + todosPagamentos.size());
            
            // Separar pagamentos de RESERVAS e AVULSOS
            List<Pagamento> pagamentosReservas = todosPagamentos.stream()
                .filter(p -> p.getReserva() != null)
                .collect(Collectors.toList());
            
            List<Pagamento> pagamentosAvulsos = todosPagamentos.stream()
                .filter(p -> p.getReserva() == null)
                .collect(Collectors.toList());
            
            System.out.println("🏨 Pagamentos de reservas: " + pagamentosReservas.size());
            System.out.println("🛒 Pagamentos avulsos: " + pagamentosAvulsos.size());
            
            // Montar resposta
            Map<String, Object> relatorio = new java.util.HashMap<>();
            
            // Informações básicas
            relatorio.put("caixaId", caixa.getId());
            relatorio.put("recepcionistaNome", caixa.getUsuario() != null ? caixa.getUsuario().getNome() : "N/A");
            relatorio.put("dataHoraAbertura", caixa.getDataHoraAbertura());
            relatorio.put("dataHoraFechamento", caixa.getDataHoraFechamento());
            relatorio.put("status", caixa.getStatus() != null ? caixa.getStatus().name() : "ABERTO");
            relatorio.put("turno", caixa.getTurno());
            
            // ═══════════════════════════════════════════
            // CALCULAR SUBTOTAL RESERVAS
            // ═══════════════════════════════════════════
            Map<String, BigDecimal> subtotalReservas = calcularTotaisPorFormaPagamento(pagamentosReservas);
            relatorio.put("subtotalReservas", subtotalReservas);
            
            // Vendas por apartamento (agrupado)
            relatorio.put("vendasReservas", agruparVendasPorApartamento(pagamentosReservas));
            
            // ═══════════════════════════════════════════
            // CALCULAR VENDAS AVULSAS
            // ═══════════════════════════════════════════
            Map<String, BigDecimal> vendasAvulsas = calcularTotaisPorFormaPagamento(pagamentosAvulsos);
            relatorio.put("vendasAvulsas", vendasAvulsas);
            
            // ═══════════════════════════════════════════
            // CALCULAR TOTAL GERAL
            // ═══════════════════════════════════════════
            Map<String, BigDecimal> totalGeral = calcularTotaisPorFormaPagamento(todosPagamentos);
            relatorio.put("totalGeral", totalGeral);
            
            System.out.println("✅ Relatório detalhado gerado!");
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(relatorio);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * 🛒 BUSCAR VENDAS DETALHADAS DO CAIXA (PRODUTOS POR FORMA DE PAGAMENTO)
     */
    @GetMapping("/{id}/vendas-detalhadas")
    public ResponseEntity<?> buscarVendasDetalhadas(@PathVariable Long id) {
        try {
            System.out.println("═══════════════════════════════════════════");
            System.out.println("🛒 CONTROLLER - BUSCAR VENDAS DETALHADAS");
            System.out.println("   Caixa ID: " + id);
            
            RelatorioVendasCaixaDTO relatorio = fechamentoCaixaService.buscarVendasDetalhadas(id);
            
            System.out.println("✅ Relatório gerado com sucesso!");
            System.out.println("   Total vendas: " + relatorio.getTotalVendas());
            System.out.println("   Total produtos: " + relatorio.getTotalProdutos());
            System.out.println("   Total geral: R$ " + relatorio.getTotalGeral());
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(relatorio);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar vendas detalhadas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * 📊 CALCULAR TOTAIS POR FORMA DE PAGAMENTO
     */
    private Map<String, BigDecimal> calcularTotaisPorFormaPagamento(List<Pagamento> pagamentos) {
        Map<String, BigDecimal> totais = new java.util.HashMap<>();
        
        BigDecimal dinheiro = BigDecimal.ZERO;
        BigDecimal pix = BigDecimal.ZERO;
        BigDecimal cartaoDebito = BigDecimal.ZERO;
        BigDecimal cartaoCredito = BigDecimal.ZERO;
        BigDecimal transferencia = BigDecimal.ZERO;
        BigDecimal faturado = BigDecimal.ZERO;
        
        for (Pagamento pag : pagamentos) {
            BigDecimal valor = pag.getValor();
            
            if (pag.getFormaPagamento() != null) {
                switch (pag.getFormaPagamento()) {
                    case DINHEIRO:
                        dinheiro = dinheiro.add(valor);
                        break;
                    case PIX:
                        pix = pix.add(valor);
                        break;
                    case CARTAO_DEBITO:
                        cartaoDebito = cartaoDebito.add(valor);
                        break;
                    case CARTAO_CREDITO:
                        cartaoCredito = cartaoCredito.add(valor);
                        break;
                    case TRANSFERENCIA:
                        transferencia = transferencia.add(valor);
                        break;
                    case FATURADO:
                        faturado = faturado.add(valor);
                        break;
                }
            }
        }
        
        totais.put("dinheiro", dinheiro);
        totais.put("pix", pix);
        totais.put("cartaoDebito", cartaoDebito);
        totais.put("cartaoCredito", cartaoCredito);
        totais.put("transferencia", transferencia);
        totais.put("faturado", faturado);
        totais.put("total", dinheiro.add(pix).add(cartaoDebito).add(cartaoCredito).add(transferencia).add(faturado));
        
        return totais;
    }

    /**
     * 🏨 AGRUPAR VENDAS POR APARTAMENTO
     */
    private List<Map<String, Object>> agruparVendasPorApartamento(List<Pagamento> pagamentos) {
        // Agrupar por reserva
        Map<Long, List<Pagamento>> porReserva = pagamentos.stream()
            .filter(p -> p.getReserva() != null)
            .collect(Collectors.groupingBy(p -> p.getReserva().getId()));
        
        List<Map<String, Object>> vendas = new java.util.ArrayList<>();
        
        for (Map.Entry<Long, List<Pagamento>> entry : porReserva.entrySet()) {
            List<Pagamento> pagamentosReserva = entry.getValue();
            Pagamento primeiro = pagamentosReserva.get(0);
            Reserva reserva = primeiro.getReserva();
            
            Map<String, Object> venda = new java.util.HashMap<>();
            venda.put("reservaId", reserva.getId());
            venda.put("numeroApartamento", reserva.getApartamento() != null ? 
                reserva.getApartamento().getNumeroApartamento() : "N/A");
            venda.put("clienteNome", reserva.getCliente() != null ? 
                reserva.getCliente().getNome() : "N/A");
            
            // Calcular totais por forma de pagamento
            Map<String, BigDecimal> pagamentosTotais = calcularTotaisPorFormaPagamento(pagamentosReserva);
            venda.put("pagamentos", pagamentosTotais);
            venda.put("total", pagamentosTotais.get("total"));
            
            vendas.add(venda);
        }
        
        return vendas;
    }
}
