package com.divan.controller;

import com.divan.dto.ConsumoJantarDTO;
import com.divan.entity.Produto;
import com.divan.service.ConsumoJantarService;
import com.divan.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jantar")
@CrossOrigin(origins = "*")
public class ConsumoJantarController {
    
    @Autowired
    private ConsumoJantarService consumoJantarService;
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
    /**
     * Buscar produtos da categoria RESTAURANTE (ID = 2)
     */
    @GetMapping("/produtos-restaurante")
    public ResponseEntity<List<Produto>> listarProdutosRestaurante() {
        try {
            // Filtrar apenas categoria RESTAURANTE (ID = 2) com estoque > 0
            List<Produto> produtos = produtoRepository.findAll().stream()
                .filter(p -> p.getCategoria() != null && p.getCategoria().getId() == 2L)
                .filter(p -> p.getQuantidade() != null && p.getQuantidade() > 0)
                .toList();
            
            System.out.println("📦 Produtos RESTAURANTE encontrados: " + produtos.size());
            return ResponseEntity.ok(produtos);
        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar produtos: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lançar consumo no extrato da reserva
     */
    @PostMapping("/consumo")
    public ResponseEntity<?> lancarConsumo(@RequestBody ConsumoJantarDTO dto) {
        try {
            System.out.println("🍽️ Lançando consumo na reserva #" + dto.getReservaId());
            System.out.println("   Itens: " + dto.getItens().size());
            
            consumoJantarService.lancarConsumo(dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("sucesso", true);
            response.put("mensagem", "Consumo lançado com sucesso!");
            response.put("reservaId", dto.getReservaId());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            System.err.println("❌ Erro ao lançar consumo: " + e.getMessage());
            
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            
            return ResponseEntity.badRequest().body(erro);
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", "Erro ao processar consumo");
            
            return ResponseEntity.internalServerError().body(erro);
        }
    }
}
