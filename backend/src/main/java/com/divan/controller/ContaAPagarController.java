package com.divan.controller;

import com.divan.entity.ContaAPagar;
import com.divan.service.ContaAPagarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contas-pagar")
@CrossOrigin(origins = "*")
public class ContaAPagarController {

    private final ContaAPagarService service;

    public ContaAPagarController(ContaAPagarService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ContaAPagar>> listarTodas() {
        service.atualizarVencidas();
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/em-aberto")
    public ResponseEntity<List<ContaAPagar>> listarEmAberto() {
        return ResponseEntity.ok(service.listarEmAberto());
    }

    @GetMapping("/vencidas")
    public ResponseEntity<List<ContaAPagar>> listarVencidas() {
        return ResponseEntity.ok(service.listarVencidas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body) {
        try {
            ContaAPagar conta = new ContaAPagar();
            conta.setDescricao((String) body.get("descricao"));
            conta.setValor(new BigDecimal(body.get("valor").toString()));
            conta.setValorPago(BigDecimal.ZERO);
            conta.setDataVencimento(java.time.LocalDate.parse(body.get("dataVencimento").toString()));
            
            if (body.get("dataCompra") != null) {
                conta.setDataCompra(java.time.LocalDate.parse(body.get("dataCompra").toString()));
            }
            
            conta.setCategoria((String) body.get("categoria"));
            conta.setCodigoBarras((String) body.get("codigoBarras"));
            conta.setObservacao((String) body.get("observacao"));
            conta.setFornecedor((String) body.get("fornecedor"));

            Long fornecedorId = body.get("fornecedorId") != null
                    ? Long.parseLong(body.get("fornecedorId").toString()) : null;

            ContaAPagar salva = service.salvar(conta, fornecedorId);
            return ResponseEntity.status(HttpStatus.CREATED).body(salva);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            ContaAPagar conta = new ContaAPagar();
            conta.setDescricao((String) body.get("descricao"));
            conta.setValor(new BigDecimal(body.get("valor").toString()));
            conta.setDataVencimento(java.time.LocalDate.parse(body.get("dataVencimento").toString()));
            if (body.get("dataCompra") != null) {
                conta.setDataCompra(java.time.LocalDate.parse(body.get("dataCompra").toString()));
            }
            conta.setCategoria((String) body.get("categoria"));
            conta.setCodigoBarras((String) body.get("codigoBarras"));
            conta.setObservacao((String) body.get("observacao"));
            conta.setFornecedor((String) body.get("fornecedor"));
            Long fornecedorId = body.get("fornecedorId") != null
                    ? Long.parseLong(body.get("fornecedorId").toString()) : null;

            return ResponseEntity.ok(service.atualizar(id, conta, fornecedorId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<?> registrarPagamento(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            BigDecimal valorPago = new BigDecimal(body.get("valorPago").toString());
            String formaPagamento = (String) body.get("formaPagamento");
            return ResponseEntity.ok(service.registrarPagamento(id, valorPago, formaPagamento));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
