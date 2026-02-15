package com.divan.controller;

import com.divan.dto.ApartamentoJantarDTO;
import com.divan.dto.HospedeJantarDTO;
import com.divan.service.JantarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jantar")
@CrossOrigin(origins = "*")
public class JantarController {

    @Autowired
    private JantarService jantarService;

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
}
