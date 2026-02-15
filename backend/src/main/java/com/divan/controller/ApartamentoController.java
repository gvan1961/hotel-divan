package com.divan.controller;

import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import com.divan.dto.ApartamentoRequestDTO;
import com.divan.dto.ApartamentoResponseDTO;
import com.divan.entity.Apartamento;
import com.divan.service.ApartamentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.divan.entity.Reserva;
import com.divan.repository.ReservaRepository;
import com.divan.repository.ApartamentoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/apartamentos")
@CrossOrigin(origins = "*")
public class ApartamentoController {
    
    @Autowired
    private ApartamentoService apartamentoService;
    
    @Autowired
    private ApartamentoRepository apartamentoRepository;
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    // ✅ ATUALIZADO - Usar DTO
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody ApartamentoRequestDTO dto) {
        try {
            System.out.println("🔵 POST /api/apartamentos - DTO recebido: " + dto);
            ApartamentoResponseDTO apartamento = apartamentoService.criarComDTO(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(apartamento);
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar apartamento: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
  /*  
    // ✅ ATUALIZADO - Retornar DTOs
    @GetMapping
    public ResponseEntity<List<ApartamentoResponseDTO>> listarTodos() {
        List<ApartamentoResponseDTO> apartamentos = apartamentoService.listarTodosDTO();
        return ResponseEntity.ok(apartamentos);
    }
    
    // ✅ ATUALIZADO - Retornar DTO
    @GetMapping("/{id}")
    public ResponseEntity<ApartamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        try {
            ApartamentoResponseDTO apartamento = apartamentoService.buscarPorIdDTO(id);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    */
    
    @GetMapping
    public ResponseEntity<List<ApartamentoResponseDTO>> listarTodos() {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🏠 GET /api/apartamentos - CHEGOU NO CONTROLLER!");
        System.out.println("═══════════════════════════════════════════");
        
        try {
            List<ApartamentoResponseDTO> apartamentos = apartamentoService.listarTodosDTO();
            System.out.println("✅ Retornando " + apartamentos.size() + " apartamentos");
            return ResponseEntity.ok(apartamentos);
        } catch (Exception e) {
            System.err.println("❌ ERRO: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @GetMapping("/numero/{numero}")
    public ResponseEntity<Apartamento> buscarPorNumero(@PathVariable String numero) {
        Optional<Apartamento> apartamento = apartamentoService.buscarPorNumero(numero);
        return apartamento.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/disponiveis")
    public ResponseEntity<List<Apartamento>> buscarDisponiveis() {
        List<Apartamento> apartamentos = apartamentoService.buscarDisponiveis();
        return ResponseEntity.ok(apartamentos);
    }
           
        
    @GetMapping("/ocupados")
    public ResponseEntity<List<Apartamento>> buscarOcupados() {
        List<Apartamento> apartamentos = apartamentoService.buscarOcupados();
        return ResponseEntity.ok(apartamentos);
    }
    
    @GetMapping("/disponiveis-periodo")
    public ResponseEntity<List<Apartamento>> buscarDisponiveisParaPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkout) {
        List<Apartamento> apartamentos = apartamentoService.buscarDisponiveisParaPeriodo(checkin, checkout);
        return ResponseEntity.ok(apartamentos);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Apartamento>> buscarPorStatus(@PathVariable Apartamento.StatusEnum status) {
        List<Apartamento> apartamentos = apartamentoService.buscarPorStatus(status);
        return ResponseEntity.ok(apartamentos);
    }
    
    // ✅ ATUALIZADO - Usar DTO
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody ApartamentoRequestDTO dto) {
        try {
            System.out.println("🔵 PUT /api/apartamentos/" + id + " - DTO recebido: " + dto);
            ApartamentoResponseDTO apartamento = apartamentoService.atualizarComDTO(id, dto);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            System.err.println("❌ Erro ao atualizar apartamento: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Apartamento> atualizarStatus(@PathVariable Long id, @RequestParam Apartamento.StatusEnum status) {
        try {
            Apartamento apartamento = apartamentoService.atualizarStatus(id, status);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/liberar-limpeza")
    public ResponseEntity<?> liberarLimpeza(@PathVariable Long id) {
        try {
            Apartamento apartamento = apartamentoService.liberarLimpeza(id);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/manutencao")
    public ResponseEntity<?> colocarEmManutencao(
        @PathVariable Long id,
        @RequestParam(required = false) String motivo
    ) {
        try {
            Apartamento apartamento = apartamentoService.colocarEmManutencao(id, motivo);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/liberar-manutencao")
    public ResponseEntity<?> liberarManutencao(@PathVariable Long id) {
        try {
            Apartamento apartamento = apartamentoService.liberarManutencao(id);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/bloquear")
    public ResponseEntity<?> bloquear(
        @PathVariable Long id,
        @RequestParam(required = false) String motivo
    ) {
        try {
            Apartamento apartamento = apartamentoService.bloquear(id, motivo);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/desbloquear")
    public ResponseEntity<?> desbloquear(@PathVariable Long id) {
        try {
            Apartamento apartamento = apartamentoService.desbloquear(id);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/disponiveis-para-reserva")
    public ResponseEntity<?> buscarDisponiveisParaReserva(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataCheckin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataCheckout) {
        try {
            List<ApartamentoResponseDTO> disponiveis = apartamentoService
                .buscarApartamentosDisponiveisParaReservaDTO(dataCheckin, dataCheckout);
            return ResponseEntity.ok(disponiveis);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/verificar-checkout-vencido")
    public ResponseEntity<?> verificarCheckoutVencido(@PathVariable Long id) {

        System.out.println("🔍 Verificando checkout vencido - Apartamento ID: " + id);

        try {
            Apartamento apartamento = apartamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));

            // Buscar reservas ATIVAS no apartamento
            List<Reserva> reservasAtivas = reservaRepository.findByApartamentoAndStatus(
                apartamento,
                Reserva.StatusReservaEnum.ATIVA
            );

            if (reservasAtivas.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "temCheckoutVencido", false,
                    "disponivel", true
                ));
            }

            // Verificar se alguma tem checkout vencido
            LocalDateTime agora = LocalDateTime.now();

            for (Reserva reserva : reservasAtivas) {
                if (reserva.getDataCheckout().isBefore(agora)) {
                    long horasAtraso = ChronoUnit.HOURS.between(reserva.getDataCheckout(), agora);

                    System.out.println("⚠️ Checkout vencido detectado!");
                    System.out.println("   Reserva ID: " + reserva.getId());
                    System.out.println("   Hóspede: " + reserva.getCliente().getNome());
                    System.out.println("   Atraso: " + horasAtraso + " hora(s)");

                    return ResponseEntity.ok(Map.of(
                        "temCheckoutVencido", true,
                        "disponivel", false,
                        "reservaId", reserva.getId(),
                        "hospedeNome", reserva.getCliente().getNome(),
                        "checkoutPrevisto", reserva.getDataCheckout().toString(),
                        "horasAtraso", horasAtraso,
                        "mensagem", String.format(
                            "Apartamento %s está ocupado com checkout vencido há %d hora(s).\n\n" +
                            "Hóspede: %s\n" +
                            "Checkout previsto: %s\n\n" +
                            "É necessário fazer o checkout antes de criar nova reserva.",
                            apartamento.getNumeroApartamento(),
                            horasAtraso,
                            reserva.getCliente().getNome(),
                            reserva.getDataCheckout().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        )
                    ));
                }
            }

            // Tem reserva ativa mas não está vencida
            return ResponseEntity.ok(Map.of(
                "temCheckoutVencido", false,
                "disponivel", false,
                "mensagem", "Apartamento ocupado com checkout dentro do prazo"
            ));

        } catch (Exception e) {
            System.err.println("❌ Erro ao verificar checkout: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "erro", e.getMessage()
            ));
        }
    }

    // Verificar disponibilidade de um apartamento específico
    @GetMapping("/{id}/verificar-disponibilidade")
    public ResponseEntity<?> verificarDisponibilidade(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataCheckin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataCheckout) {
        try {
            boolean disponivel = apartamentoService.verificarDisponibilidade(id, dataCheckin, dataCheckout);
            return ResponseEntity.ok(Map.of(
                "disponivel", disponivel,
                "mensagem", disponivel ? "Apartamento disponível" : "Apartamento não disponível"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // Buscar apartamentos bloqueados
    @GetMapping("/bloqueados")
    public ResponseEntity<?> buscarBloqueados() {
        try {
            List<Apartamento> bloqueados = apartamentoService.buscarApartamentosBloqueados();
            return ResponseEntity.ok(bloqueados);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // Liberar apartamento (colocar como disponível)
    @PatchMapping("/{id}/liberar")
    public ResponseEntity<?> liberarApartamento(@PathVariable Long id) {
        try {
            Apartamento apartamento = apartamentoService.liberarApartamento(id);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
