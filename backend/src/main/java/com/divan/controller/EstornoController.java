package com.divan.controller;

import com.divan.dto.EstornoRequest;
import com.divan.service.EstornoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/estornos")
@CrossOrigin(origins = "*")
public class EstornoController {

    @Autowired
    private EstornoService estornoService;

    /**
     * Estornar consumo de apartamento (reserva)
     */
    @PostMapping("/consumo-apartamento")
    public ResponseEntity<?> estornarConsumoApartamento(@RequestBody EstornoRequest request) {
        try {
            estornoService.estornarConsumoApartamento(request);
            
            Map<String, String> response = new HashMap<>();
            response.put("mensagem", "Estorno realizado com sucesso");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erro no estorno: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(erro);
        }
    }

    /**
     * Estornar venda à vista
     */
    @PostMapping("/venda-vista")
    public ResponseEntity<?> estornarVendaAVista(@RequestBody EstornoRequest request) {
        try {
            estornoService.estornarVendaAVista(request);
            
            Map<String, String> response = new HashMap<>();
            response.put("mensagem", "Venda à vista estornada. ATENÇÃO: Devolva o valor ao cliente!");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erro no estorno: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(erro);
        }
    }

    /**
     * Estornar venda faturada
     */
    @PostMapping("/venda-faturada")
    public ResponseEntity<?> estornarVendaFaturada(@RequestBody EstornoRequest request) {
        try {
            estornoService.estornarVendaFaturada(request);
            
            Map<String, String> response = new HashMap<>();
            response.put("mensagem", "Venda faturada estornada com sucesso");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erro no estorno: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(erro);
        }
    }
}
