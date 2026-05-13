package com.divan.controller;

import com.divan.entity.DepositoProvisorio;
import com.divan.entity.DepositoProvisorioItem;
import com.divan.dto.*;
import com.divan.service.DepositoProvisorioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deposito")
public class DepositoProvisorioController {

    private final DepositoProvisorioService service;

    public DepositoProvisorioController(DepositoProvisorioService service) {
        this.service = service;
    }

    @GetMapping("/atual")
    public ResponseEntity<DepositoProvisorio> getAtual() {
        DepositoProvisorio deposito = service.getDepositoAtual();
        if (deposito == null) return ResponseEntity.ok().build();
        return ResponseEntity.ok(deposito);
    }

    @PostMapping("/abrir")
    public ResponseEntity<DepositoProvisorio> abrir() {
        return ResponseEntity.ok(service.abrirOuRetornarDeposito());
    }

    @PostMapping("/item")
    public ResponseEntity<DepositoProvisorioItem> adicionarItem(
            @RequestBody DepositoItemRequest request) {
        return ResponseEntity.ok(service.adicionarItem(request));
    }

    @PostMapping("/distribuir")
    public ResponseEntity<Void> distribuir(
            @RequestBody DepositoDistribuirRequest request) {
        service.distribuirItem(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/item/{id}")
    public ResponseEntity<Void> removerItem(@PathVariable Long id) {
        service.removerItem(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/pagar-avista")
    public ResponseEntity<Void> pagarAVista(
            @RequestBody DepositoPagarAVistaRequest request) {
        service.pagarAVista(request);
        return ResponseEntity.ok().build();
    }
    
}