package com.divan.controller;

import com.divan.entity.HospedagemHospede;
import com.divan.repository.HospedagemHospedeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospedagem-hospedes")
@CrossOrigin(origins = "*")
public class HospedagemHospedeController {
    
    @Autowired
    private HospedagemHospedeRepository hospedagemHospedeRepository;
    
    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<List<HospedagemHospede>> listarPorReserva(@PathVariable Long reservaId) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("📋 BUSCANDO HÓSPEDES DA RESERVA: " + reservaId);
        
        List<HospedagemHospede> hospedes = hospedagemHospedeRepository.findByReservaId(reservaId);
        
        System.out.println("✅ Encontrados: " + hospedes.size() + " hóspede(s)");
        
        for (HospedagemHospede h : hospedes) {
            System.out.println("   👤 " + h.getCliente().getNome() + 
                             " | Titular: " + h.getTitular() +
                             " | Status: " + h.getStatus());
        }
        
        System.out.println("═══════════════════════════════════════");
        
        return ResponseEntity.ok(hospedes);
    }
}
