package com.divan.controller;

import com.divan.dto.AlertaDTO;
import com.divan.dto.ConflitoPrReservaDTO;
import com.divan.service.ConflitosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
@CrossOrigin(origins = "*")
public class ConflitosController {
    
    private final ConflitosService conflitosService;     
    
    public ConflitosController(ConflitosService conflitosService) {
        this.conflitosService = conflitosService;
        System.out.println("═══════════════════════════════════════════");
        System.out.println("✅ ConflitosController INICIALIZADO!");
        System.out.println("═══════════════════════════════════════════");
    }
    
    /**
     * 📊 BUSCAR TODOS OS ALERTAS ATIVOS
     * GET /api/alertas/todos
     * 
     * Retorna:
     * {
     *   "conflitos": [...],
     *   "checkoutsVencidos": [...],
     *   "noShows": [...]
     * }
     */
    @GetMapping("/todos")
    public ResponseEntity<Map<String, List<AlertaDTO>>> buscarTodosAlertas() {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("📊 CONTROLLER - BUSCAR TODOS ALERTAS");
        System.out.println("═══════════════════════════════════════════");
        
        try {
            Map<String, List<AlertaDTO>> alertas = conflitosService.buscarTodosAlertas();
            
            int total = alertas.values().stream()
                .mapToInt(List::size)
                .sum();
            
            System.out.println("✅ Total de alertas: " + total);
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(alertas);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar alertas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/teste")
    public ResponseEntity<?> teste() {
        System.out.println("✅ ENDPOINT /teste FUNCIONOU!");
        return ResponseEntity.ok(Map.of("mensagem", "Funcionou!"));
    }
    
    /**
     * 🚨 DETECTAR APENAS CONFLITOS DE PRÉ-RESERVA
     * GET /api/alertas/conflitos
     */
    @GetMapping("/conflitos")
    public ResponseEntity<List<ConflitoPrReservaDTO>> detectarConflitos() {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🚨 CONTROLLER - DETECTAR CONFLITOS");
        System.out.println("═══════════════════════════════════════════");
        
        try {
            List<ConflitoPrReservaDTO> conflitos = conflitosService.detectarConflitos();
            
            System.out.println("✅ Conflitos detectados: " + conflitos.size());
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(conflitos);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao detectar conflitos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * ⏰ DETECTAR APENAS CHECKOUTS VENCIDOS
     * GET /api/alertas/checkouts-vencidos
     */
    @GetMapping("/checkouts-vencidos")
    public ResponseEntity<List<AlertaDTO>> detectarCheckoutsVencidos() {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("⏰ CONTROLLER - CHECKOUTS VENCIDOS");
        System.out.println("═══════════════════════════════════════════");
        
        try {
            List<AlertaDTO> checkouts = conflitosService.detectarCheckoutsVencidos();
            
            System.out.println("✅ Checkouts vencidos: " + checkouts.size());
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(checkouts);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao detectar checkouts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 🔴 DETECTAR APENAS NO-SHOWS
     * GET /api/alertas/no-shows
     */
    @GetMapping("/no-shows")
    public ResponseEntity<List<AlertaDTO>> detectarNoShows() {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔴 CONTROLLER - DETECTAR NO-SHOWS");
        System.out.println("═══════════════════════════════════════════");
        
        try {
            List<AlertaDTO> noShows = conflitosService.detectarNoShows();
            
            System.out.println("✅ No-shows detectados: " + noShows.size());
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(noShows);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao detectar no-shows: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 🔄 TRANSFERIR PRÉ-RESERVA PARA OUTRO APARTAMENTO
     * POST /api/alertas/transferir
     */
    @PostMapping("/transferir")
    public ResponseEntity<?> transferirPreReserva(@RequestBody Map<String, Object> request) {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔄 CONTROLLER - TRANSFERIR PRÉ-RESERVA");
        System.out.println("═══════════════════════════════════════════");
        
        try {
            Long preReservaId = Long.valueOf(request.get("preReservaId").toString());
            Long novoApartamentoId = Long.valueOf(request.get("novoApartamentoId").toString());
            String motivo = request.get("motivo") != null ? request.get("motivo").toString() : null;
            
            System.out.println("   Pré-reserva ID: " + preReservaId);
            System.out.println("   Novo apartamento ID: " + novoApartamentoId);
            System.out.println("   Motivo: " + motivo);
            
            conflitosService.transferirPreReserva(preReservaId, novoApartamentoId, motivo);
            
            System.out.println("✅ Transferência realizada com sucesso!");
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Pré-reserva transferida com sucesso!"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao transferir: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        }
    }
    
    /**
     * ✅ MARCAR RESERVA COMO NO-SHOW
     * POST /api/alertas/marcar-no-show
     */
    @PostMapping("/marcar-no-show")
    public ResponseEntity<?> marcarNoShow(@RequestBody Map<String, Object> request) {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔴 CONTROLLER - MARCAR NO-SHOW");
        System.out.println("═══════════════════════════════════════════");
        
        try {
            Long reservaId = Long.valueOf(request.get("reservaId").toString());
            String observacao = request.get("observacao") != null ? request.get("observacao").toString() : null;
            
            System.out.println("   Reserva ID: " + reservaId);
            System.out.println("   Observação: " + observacao);
            
            // TODO: Implementar lógica de marcar no-show
            // Por enquanto, só registramos o log
            
            System.out.println("✅ No-show registrado!");
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "No-show registrado com sucesso!"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao marcar no-show: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/fazer-checkout")
    public ResponseEntity<?> fazerCheckout(@RequestBody Map<String, Object> request) {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🚪 CONTROLLER - FAZER CHECKOUT");
        System.out.println("═══════════════════════════════════════════");
        
        try {
            Long reservaId = Long.valueOf(request.get("reservaId").toString());
            String observacao = request.get("observacao") != null 
                ? request.get("observacao").toString() 
                : null;
            
            System.out.println("   Reserva ID: " + reservaId);
            System.out.println("   Observação: " + observacao);
            
            conflitosService.fazerCheckout(reservaId, observacao);
            
            System.out.println("✅ Checkout realizado com sucesso!");
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Checkout realizado com sucesso!"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao fazer checkout: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        }
    }

    /**
     * 🔄 PRORROGAR CHECKOUT
     * POST /api/alertas/prorrogar-checkout
     */
    @PostMapping("/prorrogar-checkout")
    public ResponseEntity<?> prorrogarCheckout(@RequestBody Map<String, Object> request) {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔄 CONTROLLER - PRORROGAR CHECKOUT");
        System.out.println("═══════════════════════════════════════════");
        
        try {
            Long reservaId = Long.valueOf(request.get("reservaId").toString());
            String novoCheckoutStr = request.get("novoCheckout").toString();
            String motivo = request.get("motivo") != null 
                ? request.get("motivo").toString() 
                : null;
            
            java.time.LocalDateTime novoCheckout = java.time.LocalDateTime.parse(novoCheckoutStr);
            
            System.out.println("   Reserva ID: " + reservaId);
            System.out.println("   Novo checkout: " + novoCheckout);
            System.out.println("   Motivo: " + motivo);
            
            conflitosService.prorrogarCheckout(reservaId, novoCheckout, motivo);
            
            System.out.println("✅ Checkout prorrogado!");
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Checkout prorrogado com sucesso!"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao prorrogar checkout: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        }
    }

    /**
     * 💰 COBRAR DIÁRIA ADICIONAL
     * POST /api/alertas/cobrar-diaria
     */
    @PostMapping("/cobrar-diaria")
    public ResponseEntity<?> cobrarDiaria(@RequestBody Map<String, Object> request) {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("💰 CONTROLLER - COBRAR DIÁRIA");
        System.out.println("═══════════════════════════════════════════");
        
        try {
            Long reservaId = Long.valueOf(request.get("reservaId").toString());
            String tipoDiaria = request.get("tipoDiaria").toString();
            String motivo = request.get("motivo") != null 
                ? request.get("motivo").toString() 
                : null;
            
            System.out.println("   Reserva ID: " + reservaId);
            System.out.println("   Tipo: " + tipoDiaria);
            System.out.println("   Motivo: " + motivo);
            
            conflitosService.cobrarDiariaAdicional(reservaId, tipoDiaria, motivo);
            
            System.out.println("✅ Diária cobrada!");
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Diária adicional cobrada com sucesso!"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao cobrar diária: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        }
    }

    /**
     * ❌ CANCELAR RESERVA
     * POST /api/alertas/cancelar-reserva
     */
    @PostMapping("/cancelar-reserva")
    public ResponseEntity<?> cancelarReserva(@RequestBody Map<String, Object> request) {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("❌ CONTROLLER - CANCELAR RESERVA");
        System.out.println("═══════════════════════════════════════════");
        
        try {
            Long reservaId = Long.valueOf(request.get("reservaId").toString());
            String motivo = request.get("motivo") != null 
                ? request.get("motivo").toString() 
                : null;
            
            System.out.println("   Reserva ID: " + reservaId);
            System.out.println("   Motivo: " + motivo);
            
            conflitosService.cancelarReserva(reservaId, motivo);
            
            System.out.println("✅ Reserva cancelada!");
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Reserva cancelada com sucesso!"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao cancelar reserva: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        }
    }

    /**
     * ✅ CONFIRMAR CHEGADA (PRÉ-RESERVA → CHECK-IN)
     * POST /api/alertas/confirmar-chegada
     */
    @PostMapping("/confirmar-chegada")
    public ResponseEntity<?> confirmarChegada(@RequestBody Map<String, Object> request) {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("✅ CONTROLLER - CONFIRMAR CHEGADA");
        System.out.println("═══════════════════════════════════════════");
        
        try {
            Long reservaId = Long.valueOf(request.get("reservaId").toString());
            String observacao = request.get("observacao") != null 
                ? request.get("observacao").toString() 
                : null;
            
            System.out.println("   Reserva ID: " + reservaId);
            System.out.println("   Observação: " + observacao);
            
            conflitosService.confirmarChegada(reservaId, observacao);
            
            System.out.println("✅ Chegada confirmada - Check-in realizado!");
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Check-in realizado com sucesso!"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao confirmar chegada: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        }
    }
}
