package com.divan.service;

import com.divan.entity.Apartamento;
import com.divan.entity.Reserva;
import com.divan.repository.ApartamentoRepository;
import com.divan.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendadorService {

    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private ApartamentoRepository apartamentoRepository;

    /**
     * ✅ ATIVAR PRÉ-RESERVAS AUTOMATICAMENTE
     * Executa todos os dias às 00:05
     */
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void ativarPreReservasAutomaticamente() {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔄 ATIVANDO PRÉ-RESERVAS AUTOMATICAMENTE");
        System.out.println("═══════════════════════════════════════════");
        
        LocalDate hoje = LocalDate.now();
        
        // Buscar pré-reservas cujo check-in é HOJE
        List<Reserva> preReservasHoje = reservaRepository.findAll().stream()
            .filter(r -> r.getStatus() == Reserva.StatusReservaEnum.PRE_RESERVA)
            .filter(r -> r.getDataCheckin().toLocalDate().equals(hoje))
            .toList();
        
        System.out.println("📋 Pré-reservas encontradas para hoje: " + preReservasHoje.size());
        
        for (Reserva reserva : preReservasHoje) {
            try {
                // Ativar reserva
                reserva.setStatus(Reserva.StatusReservaEnum.ATIVA);
                
                // Ocupar apartamento
                Apartamento apartamento = reserva.getApartamento();
                apartamento.setStatus(Apartamento.StatusEnum.OCUPADO);
                
                apartamentoRepository.save(apartamento);
                reservaRepository.save(reserva);
                
                System.out.println("✅ Reserva #" + reserva.getId() + 
                                 " ativada - Apt " + apartamento.getNumeroApartamento() + 
                                 " - Cliente: " + reserva.getCliente().getNome());
                
            } catch (Exception e) {
                System.err.println("❌ Erro ao ativar reserva #" + reserva.getId() + ": " + e.getMessage());
            }
        }
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("✅ ATIVAÇÃO AUTOMÁTICA CONCLUÍDA");
        System.out.println("   Total ativadas: " + preReservasHoje.size());
        System.out.println("═══════════════════════════════════════════");
    }
}