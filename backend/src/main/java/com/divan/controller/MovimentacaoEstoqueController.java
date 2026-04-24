package com.divan.controller;

import com.divan.entity.MovimentacaoEstoque;
import com.divan.service.MovimentacaoEstoqueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estoque/movimentacoes")
@CrossOrigin(origins = "*")
public class MovimentacaoEstoqueController {

    private final MovimentacaoEstoqueService service;

    public MovimentacaoEstoqueController(MovimentacaoEstoqueService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<MovimentacaoEstoque>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<List<MovimentacaoEstoque>> listarPorProduto(@PathVariable Long produtoId) {
        return ResponseEntity.ok(service.listarPorProduto(produtoId));
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<MovimentacaoEstoque>> listarPorPeriodo(
            @RequestParam String inicio,
            @RequestParam String fim) {
        LocalDateTime dtInicio = LocalDateTime.parse(inicio);
        LocalDateTime dtFim = LocalDateTime.parse(fim);
        return ResponseEntity.ok(service.listarPorPeriodo(dtInicio, dtFim));
    }

    @PostMapping("/entrada")
    public ResponseEntity<?> registrarEntrada(@RequestBody Map<String, Object> body) {
        try {
            Long produtoId = Long.parseLong(body.get("produtoId").toString());
            Integer quantidade = Integer.parseInt(body.get("quantidade").toString());
            BigDecimal valorUnitario = body.get("valorUnitario") != null
                    ? new BigDecimal(body.get("valorUnitario").toString()) : null;
            Long fornecedorId = body.get("fornecedorId") != null
                    ? Long.parseLong(body.get("fornecedorId").toString()) : null;
            String motivo = (String) body.get("motivo");

            MovimentacaoEstoque mov = service.registrarEntrada(
                    produtoId, quantidade, valorUnitario, fornecedorId, motivo);
            return ResponseEntity.ok(mov);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/acerto")
    public ResponseEntity<?> registrarAcerto(@RequestBody Map<String, Object> body) {
        try {
            Long produtoId = Long.parseLong(body.get("produtoId").toString());
            Integer quantidadeReal = Integer.parseInt(body.get("quantidadeReal").toString());
            String motivo = (String) body.get("motivo");

            MovimentacaoEstoque mov = service.registrarAcerto(produtoId, quantidadeReal, motivo);
            return ResponseEntity.ok(mov);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
