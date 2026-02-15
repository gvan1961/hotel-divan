package com.divan.controller;

import com.divan.dto.ValeRequest;
import com.divan.dto.ValeResponse;
import com.divan.entity.Vale;
import com.divan.repository.ValeRepository;
import com.divan.service.ValeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ValeController {

    private final ValeService valeService;
    private final ValeRepository valeRepository;  // ⭐ ADICIONAR ESTA LINHA

    @GetMapping
    public ResponseEntity<List<ValeResponse>> listarTodas() {
        return ResponseEntity.ok(valeService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ValeResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(valeService.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ValeResponse>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(valeService.listarPorCliente(clienteId));
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<ValeResponse>> listarPendentes() {
        return ResponseEntity.ok(valeService.listarValesPendentes());
    }

    @GetMapping("/vencidos")
    public ResponseEntity<List<ValeResponse>> listarVencidos() {
        return ResponseEntity.ok(valeService.listarValesVencidos());
    }

    @GetMapping("/cliente/{clienteId}/total-pendente")
    public ResponseEntity<Map<String, BigDecimal>> calcularTotalPendente(@PathVariable Long clienteId) {
        BigDecimal total = valeService.calcularTotalPendentePorCliente(clienteId);
        return ResponseEntity.ok(Map.of("totalPendente", total));
    }

    @PostMapping
    public ResponseEntity<ValeResponse> criar(@RequestBody ValeRequest request) {
        ValeResponse response = valeService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ValeResponse> atualizar(@PathVariable Long id, @RequestBody ValeRequest request) {
        return ResponseEntity.ok(valeService.atualizar(id, request));
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<ValeResponse> marcarComoPago(@PathVariable Long id) {
        return ResponseEntity.ok(valeService.marcarComoPago(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ValeResponse> cancelar(@PathVariable Long id, @RequestParam String motivo) {
        return ResponseEntity.ok(valeService.cancelar(id, motivo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        valeService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/atualizar-vencidos")
    public ResponseEntity<Map<String, String>> atualizarVencidos() {
        valeService.atualizarValesVencidos();
        return ResponseEntity.ok(Map.of("mensagem", "Vales vencidos atualizados com sucesso"));
    }

    // ⭐ ADICIONAR ESTE MÉTODO
    @PatchMapping("/{id}/assinar")
    public ResponseEntity<ValeResponse> assinar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String assinaturaBase64 = body.get("assinaturaBase64");
        
        if (assinaturaBase64 == null || assinaturaBase64.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Vale vale = valeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Vale não encontrado"));
        
        vale.setAssinaturaBase64(assinaturaBase64);
        vale.setDataAssinatura(LocalDateTime.now());
        
        Vale atualizado = valeRepository.save(vale);
        return ResponseEntity.ok(ValeResponse.fromEntity(atualizado));
    }
}
