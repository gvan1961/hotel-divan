package com.divan.controller;

import com.divan.dto.ProdutoRequestDTO;
import com.divan.entity.Produto;
import com.divan.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {
    
    @Autowired
    private ProdutoService produtoService;
    
    @PostMapping
    public ResponseEntity<Produto> criar(@Valid @RequestBody ProdutoRequestDTO dto) {
        System.out.println("📥 Recebendo requisição para criar produto");
        System.out.println("📦 DTO recebido: " + dto);
        
        try {
            Produto produtoSalvo = produtoService.salvarComDTO(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(produtoSalvo);
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar produto: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
       public ResponseEntity<List<Produto>> listarTodos() {
        List<Produto> produtos = produtoService.listarTodos();
       return ResponseEntity.ok(produtos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id) {
        Optional<Produto> produto = produtoService.buscarPorId(id);
        return produto.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/buscar")
    public ResponseEntity<List<Produto>> buscarPorNome(@RequestParam String nome) {
        List<Produto> produtos = produtoService.buscarPorNome(nome);
        return ResponseEntity.ok(produtos);
    }
    
    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<Produto>> buscarComEstoqueBaixo() {
        List<Produto> produtos = produtoService.buscarComEstoqueBaixo();
        return ResponseEntity.ok(produtos);
    }
    
    @GetMapping("/sem-estoque")
    public ResponseEntity<List<Produto>> buscarSemEstoque() {
        List<Produto> produtos = produtoService.buscarSemEstoque();
        return ResponseEntity.ok(produtos);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequestDTO dto) {
        System.out.println("📥 Recebendo requisição para atualizar produto: " + id);
        System.out.println("📦 DTO recebido: " + dto);
        
        try {
            Produto produtoAtualizado = produtoService.atualizarComDTO(id, dto);
            return ResponseEntity.ok(produtoAtualizado);
        } catch (Exception e) {
            System.err.println("❌ Erro ao atualizar produto: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/estoque")
    public ResponseEntity<Produto> atualizarEstoque(@PathVariable Long id, @RequestParam Integer quantidade) {
        try {
            Produto produto = produtoService.atualizarEstoque(id, quantidade);
            return ResponseEntity.ok(produto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
  /*
    @GetMapping("/disponiveis")
    public ResponseEntity<List<Produto>> listarDisponiveis() {
        List<Produto> produtos = produtoService.listarTodos()
            .stream()
            .filter(p -> p.getQuantidade() > 0)
            .sorted((a, b) -> a.getNomeProduto().compareTo(b.getNomeProduto()))
            .toList();
        return ResponseEntity.ok(produtos);
    }
    */
    
    @GetMapping("/disponiveis")
    public ResponseEntity<List<Produto>> listarDisponiveis() {
        List<Produto> produtos = produtoService.listarDisponiveis();
        return ResponseEntity.ok(produtos);
    }
}
