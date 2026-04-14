package com.divan.controller;

import com.divan.entity.*;
import com.divan.service.SorteioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sorteios")
@CrossOrigin(origins = "*")
public class SorteioController {

    @Autowired
    private SorteioService sorteioService;

    // ✅ LISTAR TODOS
    @GetMapping
    public ResponseEntity<List<Sorteio>> listarTodos() {
        return ResponseEntity.ok(sorteioService.listarTodos());
    }

    // ✅ BUSCAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return sorteioService.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // ✅ CRIAR SORTEIO
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Sorteio sorteio) {
        try {
            Sorteio criado = sorteioService.criarSorteio(sorteio);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ✅ ENCERRAR SORTEIO
    @PatchMapping("/{id}/encerrar")
    public ResponseEntity<?> encerrar(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(sorteioService.encerrarSorteio(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ✅ REALIZAR SORTEIO
    @PostMapping("/{id}/realizar")
    public ResponseEntity<?> realizar(@PathVariable Long id) {
        try {
            BilheteSorteio vencedor = sorteioService.realizarSorteio(id);
            return ResponseEntity.ok(Map.of(
                "numeroBilhete", vencedor.getNumeroBilhete(),
                "nomeHospede", vencedor.getHospedagemHospede().getCliente().getNome(),
                "cpfHospede", vencedor.getHospedagemHospede().getCliente().getCpf(),
                "celularHospede", vencedor.getHospedagemHospede().getCliente().getCelular(),
                "quantidadeDiarias", vencedor.getQuantidadeDiarias(),
                "dataEmissao", vencedor.getDataEmissao().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ✅ LISTAR BILHETES DO SORTEIO
    @GetMapping("/{id}/bilhetes")
    public ResponseEntity<?> listarBilhetes(@PathVariable Long id) {
        try {
            List<BilheteSorteio> bilhetes = sorteioService.listarBilhetesPorSorteio(id);
            return ResponseEntity.ok(bilhetes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ✅ CONTAR BILHETES DO SORTEIO
    @GetMapping("/{id}/bilhetes/count")
    public ResponseEntity<?> contarBilhetes(@PathVariable Long id) {
        try {
            long total = sorteioService.contarBilhetesPorSorteio(id);
            return ResponseEntity.ok(Map.of("total", total));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
