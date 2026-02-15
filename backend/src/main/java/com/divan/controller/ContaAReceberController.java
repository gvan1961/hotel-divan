package com.divan.controller;

import com.divan.dto.ContaAReceberDTO;
import com.divan.dto.ContaAReceberRequestDTO;
import com.divan.dto.PagamentoContaReceberDTO;
import com.divan.entity.ContaAReceber.StatusContaEnum;
import com.divan.service.ContaAReceberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contas-receber")
@CrossOrigin(origins = "*")
public class ContaAReceberController {
    
    @Autowired  // ✅ SUBSTITUIR @RequiredArgsConstructor por @Autowired
    private ContaAReceberService contaAReceberService;  // ✅ REMOVER final
    
    // ========== LISTAR ==========   
      
    @GetMapping
    public ResponseEntity<List<ContaAReceberDTO>> listarTodas() {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("💰 GET /api/contas-receber - CHEGOU NO CONTROLLER!");
        System.out.println("═══════════════════════════════════════════");
        
        try {
            List<ContaAReceberDTO> contas = contaAReceberService.listarTodas();
            System.out.println("✅ Retornando " + contas.size() + " contas");
            return ResponseEntity.ok(contas);
        } catch (Exception e) {
            System.err.println("❌ ERRO: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    
    @GetMapping("/em-aberto")
    public ResponseEntity<List<ContaAReceberDTO>> listarEmAberto() {
        return ResponseEntity.ok(contaAReceberService.listarContasEmAberto());
    }
    
    @GetMapping("/vencidas")
    public ResponseEntity<List<ContaAReceberDTO>> listarVencidas() {
        return ResponseEntity.ok(contaAReceberService.listarContasVencidas());
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ContaAReceberDTO>> listarPorStatus(@PathVariable StatusContaEnum status) {
        return ResponseEntity.ok(contaAReceberService.listarPorStatus(status));
    }
    
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ContaAReceberDTO>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(contaAReceberService.listarPorCliente(clienteId));
    }
    
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<ContaAReceberDTO>> listarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(contaAReceberService.listarPorEmpresa(empresaId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ContaAReceberDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contaAReceberService.buscarPorId(id));
    }
    
    // ========== CRIAR ==========
    
    @PostMapping
    public ResponseEntity<ContaAReceberDTO> criar(@Valid @RequestBody ContaAReceberRequestDTO dto) {
        ContaAReceberDTO conta = contaAReceberService.criar(dto);
        return ResponseEntity.ok(conta);
    }
    
    // ========== REGISTRAR PAGAMENTO ==========
    
    @PostMapping("/{id}/pagamento")
    public ResponseEntity<ContaAReceberDTO> registrarPagamento(
            @PathVariable Long id,
            @Valid @RequestBody PagamentoContaReceberDTO dto) {
        ContaAReceberDTO conta = contaAReceberService.registrarPagamento(id, dto);
        return ResponseEntity.ok(conta);
    }
    
    // ========== ATUALIZAR STATUS ==========
    
    @PostMapping("/atualizar-vencidas")
    public ResponseEntity<Void> atualizarStatusVencidas() {
        contaAReceberService.atualizarStatusVencidas();
        return ResponseEntity.ok().build();
    }
    
    // ========== EXCLUIR ==========
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        contaAReceberService.excluir(id);
        return ResponseEntity.ok().build();
    }
}