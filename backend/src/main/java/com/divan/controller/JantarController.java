package com.divan.controller;

import com.divan.dto.ApartamentoJantarDTO;
import com.divan.dto.ConsumoJantarDTO;
import com.divan.dto.HospedeJantarDTO;
import com.divan.entity.HospedagemHospede;
import com.divan.entity.NotaVenda;
import com.divan.repository.HospedagemHospedeRepository;
import com.divan.repository.NotaVendaRepository;
import com.divan.service.ConsumoJantarService;
import com.divan.service.JantarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.RoundingMode;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.divan.entity.ExtratoReserva;
import com.divan.repository.ExtratoReservaRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.divan.entity.ItemVenda;


@RestController
@RequestMapping("/api/jantar")
@CrossOrigin(origins = "*")
public class JantarController {
	
	@Autowired
	private NotaVendaRepository notaVendaRepository;

    @Autowired
    private JantarService jantarService;
    
    @Autowired
    private ConsumoJantarService consumoJantarService;

    @Autowired
    private HospedagemHospedeRepository hospedagemHospedeRepository;

    @Autowired
    private ExtratoReservaRepository extratoReservaRepository;
    
    // ═══════════════════════════════════════════════════════════
    // HEALTH CHECK
    // ═══════════════════════════════════════════════════════════
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        System.out.println("\n🏥 GET /api/jantar/health");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "JantarService");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════
    // APARTAMENTOS AGRUPADOS - APENAS AUTORIZADOS
    // ═══════════════════════════════════════════════════════════
    
    @GetMapping("/apartamentos-autorizados")
    public ResponseEntity<?> listarApartamentosAutorizados() {
        try {
            System.out.println("\n📞 GET /api/jantar/apartamentos-autorizados");
            
            List<ApartamentoJantarDTO> apartamentos = jantarService.listarApartamentosComHospedesAutorizados();
            
            System.out.println("✅ Retornando " + apartamentos.size() + " apartamentos\n");
            
            return ResponseEntity.ok(apartamentos);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao listar apartamentos autorizados: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao listar apartamentos autorizados");
            error.put("mensagem", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // APARTAMENTOS AGRUPADOS - TODOS (INCLUINDO NÃO AUTORIZADOS)
    // ═══════════════════════════════════════════════════════════
    
    @GetMapping("/todos-apartamentos")
    public ResponseEntity<?> listarTodosApartamentos() {
        try {
            System.out.println("\n📞 GET /api/jantar/todos-apartamentos");
            
            List<ApartamentoJantarDTO> apartamentos = jantarService.listarTodosApartamentosComHospedes();
            
            System.out.println("✅ Retornando " + apartamentos.size() + " apartamentos (incluindo não autorizados)\n");
            
            return ResponseEntity.ok(apartamentos);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao listar todos os apartamentos: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao listar todos os apartamentos");
            error.put("mensagem", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LISTA INDIVIDUAL - APENAS AUTORIZADOS
    // ═══════════════════════════════════════════════════════════
    
    @GetMapping("/hospedes-autorizados")
    public ResponseEntity<?> listarHospedesAutorizados() {
        try {
            System.out.println("\n📞 GET /api/jantar/hospedes-autorizados");
            
            List<HospedeJantarDTO> hospedes = jantarService.listarAutorizados();
            
            System.out.println("✅ Retornando " + hospedes.size() + " hóspedes autorizados\n");
            
            return ResponseEntity.ok(hospedes);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao listar hóspedes autorizados: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao listar hóspedes autorizados");
            error.put("mensagem", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // LISTA INDIVIDUAL - TODOS (INCLUINDO NÃO AUTORIZADOS)
    // ═══════════════════════════════════════════════════════════
    
    @GetMapping("/todos-hospedes")
    public ResponseEntity<?> listarTodosHospedes() {
        try {
            System.out.println("\n📞 GET /api/jantar/todos-hospedes");
            
            List<HospedeJantarDTO> hospedes = jantarService.listarTodos();
            
            System.out.println("✅ Retornando " + hospedes.size() + " hóspedes (incluindo não autorizados)\n");
            
            return ResponseEntity.ok(hospedes);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao listar todos os hóspedes: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao listar todos os hóspedes");
            error.put("mensagem", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // VERIFICAR AUTORIZAÇÃO DE CLIENTE ESPECÍFICO
    // ═══════════════════════════════════════════════════════════
    
    @GetMapping("/verificar/{clienteId}")
    public ResponseEntity<?> verificarAutorizacao(@PathVariable Long clienteId) {
        try {
            System.out.println("\n📞 GET /api/jantar/verificar/" + clienteId);
            
            Map<String, Object> resultado = jantarService.verificarAutorizacao(clienteId);
            
            if (Boolean.FALSE.equals(resultado.get("encontrado"))) {
                System.out.println("⚠️ Cliente não encontrado\n");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultado);
            }
            
            System.out.println("✅ Cliente verificado: " + resultado.get("nomeCliente") + " - Pode jantar: " + resultado.get("podeJantar") + "\n");
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao verificar autorização: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao verificar autorização");
            error.put("mensagem", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ESTATÍSTICAS
    // ═══════════════════════════════════════════════════════════
    
    @GetMapping("/estatisticas")
    public ResponseEntity<?> getEstatisticas() {
        try {
            System.out.println("\n📞 GET /api/jantar/estatisticas");
            
            Map<String, Object> estatisticas = jantarService.getEstatisticas();
            
            System.out.println("✅ Estatísticas geradas:");
            System.out.println("   - Total de hóspedes: " + estatisticas.get("totalHospedes"));
            System.out.println("   - Total autorizados: " + estatisticas.get("totalAutorizados"));
            System.out.println("   - Percentual: " + String.format("%.2f", estatisticas.get("percentualAutorizados")) + "%\n");
            
            return ResponseEntity.ok(estatisticas);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao gerar estatísticas: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Erro ao gerar estatísticas");
            error.put("mensagem", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // RELATÓRIO HTML PARA IMPRESSÃO
    // ═══════════════════════════════════════════════════════════
    
    @GetMapping(value = "/relatorio-impressao", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<?> gerarRelatorioHtml() {
        try {
            System.out.println("\n📞 GET /api/jantar/relatorio-impressao");
            
            String html = jantarService.gerarHtmlRelatorio();
            
            System.out.println("✅ Relatório HTML gerado com sucesso (" + html.length() + " caracteres)\n");
            
            return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao gerar relatório HTML: " + e.getMessage());
            e.printStackTrace();
            
            String errorHtml = "<html><body><h1>Erro ao gerar relatório</h1><p>" + e.getMessage() + "</p></body></html>";
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_HTML)
                .body(errorHtml);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ENDPOINT DE TESTE (OPCIONAL - PODE REMOVER EM PRODUÇÃO)
    // ═══════════════════════════════════════════════════════════
    
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        System.out.println("\n📞 GET /api/jantar/info");
        
        Map<String, Object> info = new HashMap<>();
        info.put("servico", "Sistema de Autorização de Jantar");
        info.put("versao", "1.0.0");
        info.put("endpoints", new String[] {
            "GET /api/jantar/health",
            "GET /api/jantar/apartamentos-autorizados",
            "GET /api/jantar/todos-apartamentos",
            "GET /api/jantar/hospedes-autorizados",
            "GET /api/jantar/todos-hospedes",
            "GET /api/jantar/verificar/{clienteId}",
            "GET /api/jantar/estatisticas",
            "GET /api/jantar/relatorio-impressao"
        });
        
        return ResponseEntity.ok(info);
    }
    
 // ═══════════════════════════════════════════════════════════
 // SALVAR COMANDA (LANÇAR CONSUMO)
 // ═══════════════════════════════════════════════════════════

 @PostMapping("/salvar-comanda")
 public ResponseEntity<?> salvarComanda(@RequestBody Map<String, Object> payload) {
     try {
         System.out.println("\n📝 POST /api/jantar/salvar-comanda");
         System.out.println("📦 Payload: " + payload);
         
         Long hospedagemHospedeId = Long.valueOf(payload.get("hospedagemHospedeId").toString());
         
         System.out.println("🔍 HospedagemHospede ID: " + hospedagemHospedeId);
         
         // Buscar hospedagem para pegar a reserva
         HospedagemHospede hospedagem = hospedagemHospedeRepository.findById(hospedagemHospedeId)
             .orElseThrow(() -> new RuntimeException("Hospedagem não encontrada"));
         
         Long reservaId = hospedagem.getReserva().getId();
         
         System.out.println("📋 Reserva ID: " + reservaId);
         
         // Montar DTO para o ConsumoJantarService
         ConsumoJantarDTO dto = new ConsumoJantarDTO();
         dto.setReservaId(reservaId);
         
      // Processar itens
         @SuppressWarnings("unchecked")
         List<Map<String, Object>> itensPayload = (List<Map<String, Object>>) payload.get("itens");

         List<ConsumoJantarDTO.ItemConsumo> itens = new ArrayList<>();
         for (Map<String, Object> item : itensPayload) {
             ConsumoJantarDTO.ItemConsumo itemDTO = new ConsumoJantarDTO.ItemConsumo();
             itemDTO.setProdutoId(Long.valueOf(item.get("produtoId").toString()));
             itemDTO.setQuantidade(Integer.valueOf(item.get("quantidade").toString()));
             itens.add(itemDTO);
         }
         
         dto.setItens(itens);
         
         System.out.println("📦 Lançando " + itens.size() + " itens no consumo");
         
         // Lançar consumo
         consumoJantarService.lancarConsumo(dto);
         
         Map<String, Object> response = new HashMap<>();
         response.put("sucesso", true);
         response.put("mensagem", "Comanda salva com sucesso!");
         response.put("notaId", reservaId); // Usando reservaId como referência
         
         System.out.println("✅ Comanda salva com sucesso!\n");
         
         return ResponseEntity.ok(response);
         
     } catch (Exception e) {
         System.err.println("❌ Erro ao salvar comanda: " + e.getMessage());
         e.printStackTrace();
         
         Map<String, String> erro = new HashMap<>();
         erro.put("erro", e.getMessage());
         
         return ResponseEntity.badRequest().body(erro);
     }
 }  
 
 @PostMapping("/relatorio-faturamento")
 public ResponseEntity<?> gerarRelatorioFaturamento(@RequestBody Map<String, String> payload) {
     try {
         LocalDate dataInicio = LocalDate.parse(payload.get("dataInicio"));
         LocalDate dataFim = LocalDate.parse(payload.get("dataFim"));

         LocalDateTime inicio = dataInicio.atStartOfDay();
         LocalDateTime fim = dataFim.atTime(23, 59, 59);

         // Buscar extratos de PRODUTO no período
         List<ExtratoReserva> extratos = extratoReservaRepository
             .findByStatusLancamentoAndDataHoraLancamentoBetween(
                 ExtratoReserva.StatusLancamentoEnum.PRODUTO, inicio, fim
             );

         // Agrupar por dia
         Map<LocalDate, List<ExtratoReserva>> porDia = extratos.stream()
             .collect(Collectors.groupingBy(e -> e.getDataHoraLancamento().toLocalDate()));

         List<Map<String, Object>> faturamentoDiario = new ArrayList<>();
         BigDecimal totalGeral = BigDecimal.ZERO;
         int totalComandas = 0;

         for (LocalDate dia = dataInicio; !dia.isAfter(dataFim); dia = dia.plusDays(1)) {
             List<ExtratoReserva> extratosDia = porDia.getOrDefault(dia, new ArrayList<>());
             
             BigDecimal totalDia = extratosDia.stream()
                 .map(ExtratoReserva::getTotalLancamento)
                 .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                 .reduce(BigDecimal.ZERO, BigDecimal::add);

             if (!extratosDia.isEmpty()) {
                 Map<String, Object> diaMap = new HashMap<>();
                 diaMap.put("data", dia.toString());
                 diaMap.put("totalComandas", extratosDia.size());
                 diaMap.put("totalVendas", totalDia);
                 faturamentoDiario.add(diaMap);

                 totalGeral = totalGeral.add(totalDia);
                 totalComandas += extratosDia.size();
             }
         }

         BigDecimal ticketMedio = totalComandas > 0
             ? totalGeral.divide(BigDecimal.valueOf(totalComandas), 2, RoundingMode.HALF_UP)
             : BigDecimal.ZERO;

         Map<String, Object> periodo = new HashMap<>();
         periodo.put("inicio", dataInicio.toString());
         periodo.put("fim", dataFim.toString());

         Map<String, Object> response = new HashMap<>();
         response.put("totalGeral", totalGeral);
         response.put("totalComandas", totalComandas);
         response.put("ticketMedio", ticketMedio);
         response.put("periodo", periodo);
         response.put("faturamentoDiario", faturamentoDiario);

         return ResponseEntity.ok(response);

     } catch (Exception e) {
         return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
     }
 }
 
 @PostMapping("/relatorio-comandas")
 public ResponseEntity<?> gerarRelatorioComandas(@RequestBody Map<String, String> payload) {
     try {
         LocalDate dataInicio = LocalDate.parse(payload.get("dataInicio"));
         LocalDate dataFim = LocalDate.parse(payload.get("dataFim"));

         LocalDateTime inicio = dataInicio.atStartOfDay();
         LocalDateTime fim = dataFim.atTime(23, 59, 59);

         List<NotaVenda> notas = notaVendaRepository.findByDataHoraVendaBetween(inicio, fim);
         

         List<Map<String, Object>> comandas = new ArrayList<>();
         BigDecimal totalGeral = BigDecimal.ZERO;

         for (NotaVenda nota : notas) {
             Map<String, Object> comanda = new HashMap<>();
             comanda.put("notaId", nota.getId());
             comanda.put("dataHora", nota.getDataHoraVenda());
             comanda.put("observacao", nota.getObservacao());
             comanda.put("status", nota.getStatus() != null ? nota.getStatus().name() : "ABERTA");

             // Apartamento e cliente via reserva
             if (nota.getReserva() != null) {
            	    comanda.put("apartamento", nota.getReserva().getApartamento().getNumeroApartamento());
            	    comanda.put("cliente", nota.getReserva().getCliente().getNome());
            	    comanda.put("reservaStatus", nota.getReserva().getStatus().name()); // ✅ ADICIONAR
            	} else {
            	    comanda.put("apartamento", "-");
            	    comanda.put("cliente", "-");
            	    comanda.put("reservaStatus", "-"); // ✅ ADICIONAR
            	}

             // Itens
             List<Map<String, Object>> itens = new ArrayList<>();
             BigDecimal totalNota = BigDecimal.ZERO;

             if (nota.getItens() != null) {
                 for (ItemVenda item : nota.getItens()) {
                     Map<String, Object> itemMap = new HashMap<>();
                     itemMap.put("produto", item.getProduto().getNomeProduto());
                     itemMap.put("quantidade", item.getQuantidade());
                     itemMap.put("valorUnitario", item.getValorUnitario());
                     BigDecimal totalItem = item.getValorUnitario()
                         .multiply(BigDecimal.valueOf(item.getQuantidade()));
                     itemMap.put("total", totalItem);
                     itens.add(itemMap);
                     totalNota = totalNota.add(totalItem);
                 }
             }

             comanda.put("itens", itens);
             comanda.put("total", totalNota);
             totalGeral = totalGeral.add(totalNota);
             comandas.add(comanda);
         }

         Map<String, Object> periodo = new HashMap<>();
         periodo.put("inicio", dataInicio.toString());
         periodo.put("fim", dataFim.toString());

         Map<String, Object> response = new HashMap<>();
         response.put("periodo", periodo);
         response.put("totalComandas", comandas.size());
         response.put("totalGeral", totalGeral);
         response.put("comandas", comandas);

         return ResponseEntity.ok(response);

     } catch (Exception e) {
         return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
     }
 }
 
    
}
