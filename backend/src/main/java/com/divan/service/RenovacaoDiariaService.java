package com.divan.service;

import com.divan.entity.Apartamento;
import com.divan.entity.Reserva;
import com.divan.entity.Diaria;
import com.divan.entity.ItemReserva;
import com.divan.repository.ReservaRepository;
import com.divan.repository.ItemReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RenovacaoDiariaService {
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private ItemReservaRepository itemReservaRepository;
    
    // ✅ Horário de tolerância para checkout (em horas após o checkout previsto)
    private static final int HORAS_TOLERANCIA = 2;
    
    /**
     * Executa a cada 1 minuto (TESTE)
     * Para PRODUÇÃO: "0 0 * * * *" (a cada 1 hora)
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void verificarRenovacaoAutomatica() {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔄 VERIFICAÇÃO DE RENOVAÇÃO AUTOMÁTICA");
        System.out.println("   Data/Hora: " + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        ));
        System.out.println("═══════════════════════════════════════════");
        
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limiteTolerancia = agora.minusHours(HORAS_TOLERANCIA);
        
        // ✅ 1. Buscar todas as reservas ATIVAS
        List<Reserva> reservasAtivas = reservaRepository
            .findByStatus(Reserva.StatusReservaEnum.ATIVA);
        
        System.out.println("📊 Total de reservas ativas: " + reservasAtivas.size());
        
        int renovadas = 0;
        
        for (Reserva reserva : reservasAtivas) {
            // ✅ 2. Verificar se checkout está vencido (passou + tolerância)
            if (reserva.getDataCheckout().isBefore(limiteTolerancia)) {
                
                System.out.println("\n⚠️ CHECKOUT VENCIDO DETECTADO:");
                System.out.println("   Reserva #" + reserva.getId());
                System.out.println("   Apartamento: " + reserva.getApartamento().getNumeroApartamento());
                System.out.println("   Hóspede: " + reserva.getCliente().getNome());
                System.out.println("   Checkout previsto: " + reserva.getDataCheckout().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                ));
                
                long horasAtraso = java.time.temporal.ChronoUnit.HOURS.between(
                    reserva.getDataCheckout(), agora
                );
                System.out.println("   ⏰ Atraso: " + horasAtraso + " hora(s)");
                
                // ✅ 3. Verificar se há pré-reserva para o dia seguinte
                LocalDateTime inicioDiaSeguinte = reserva.getDataCheckout()
                    .toLocalDate()
                    .plusDays(1)
                    .atStartOfDay();
                
                LocalDateTime fimDiaSeguinte = inicioDiaSeguinte.plusDays(1).minusSeconds(1);
                
                boolean temPreReserva = reservaRepository.existeConflito(
                    reserva.getApartamento().getId(),
                    inicioDiaSeguinte,
                    fimDiaSeguinte
                );
                
                if (temPreReserva) {
                    System.out.println("   ❌ Há pré-reserva para o dia seguinte - NÃO RENOVAR");
                    System.out.println("   ⚠️ ATENÇÃO: Hóspede deve fazer checkout urgente!");
                    continue;
                }
                
                // ✅ 4. NÃO há pré-reserva - RENOVAR AUTOMATICAMENTE
                System.out.println("   ✅ Sem pré-reserva para o dia seguinte");
                System.out.println("   🔄 RENOVANDO DIÁRIA AUTOMATICAMENTE...");
                
                // Calcular nova data de checkout (+1 dia)
                LocalDateTime novoCheckout = reserva.getDataCheckout().plusDays(1);
                
                // ✅ Buscar valor da diária da reserva
                Diaria diaria = reserva.getDiaria();
                BigDecimal valorDiaria = diaria.getValor();
                
                // ✅ Valor da diária adicional (mesmo valor da diária original)
                BigDecimal valorAdicional = valorDiaria;
                
                System.out.println("   💰 Valor da diária: R$ " + valorDiaria);
                
                // ✅ 5. LANÇAR NO EXTRATO DA RESERVA
                ItemReserva itemDiaria = new ItemReserva();
                itemDiaria.setReserva(reserva);
                itemDiaria.setDescricao("Diária adicional - Renovação automática");
                itemDiaria.setQuantidade(1);
                itemDiaria.setValorUnitario(valorAdicional);
                itemDiaria.setValorTotal(valorAdicional);
                itemDiaria.setDataHora(agora);
                itemDiaria.setTipo("DIARIA");
                
                itemReservaRepository.save(itemDiaria);
                
                System.out.println("   ✅ Lançamento criado no extrato:");
                System.out.println("      Descrição: Diária adicional - Renovação automática");
                System.out.println("      Valor: R$ " + valorAdicional);
                
                // ✅ 6. ATUALIZAR RESERVA
                LocalDateTime checkoutAnterior = reserva.getDataCheckout();
                BigDecimal totalDiariaAnterior = reserva.getTotalDiaria();
                BigDecimal totalHospedagemAnterior = reserva.getTotalHospedagem();
                
                // Atualizar checkout
                reserva.setDataCheckout(novoCheckout);
                
                // Atualizar quantidade de diárias
                reserva.setQuantidadeDiaria(reserva.getQuantidadeDiaria() + 1);
                
                // Atualizar totais
                reserva.setTotalDiaria(totalDiariaAnterior.add(valorAdicional));
                reserva.setTotalHospedagem(totalHospedagemAnterior.add(valorAdicional));
                
                // Recalcular totalApagar (totalHospedagem - totalRecebido + totalProduto - desconto)
                BigDecimal novoTotalApagar = reserva.getTotalHospedagem()
                    .add(reserva.getTotalProduto())
                    .subtract(reserva.getTotalRecebido())
                    .subtract(reserva.getDesconto());
                
                reserva.setTotalApagar(novoTotalApagar);
                
                reservaRepository.save(reserva);
                
                System.out.println("   ✅ RENOVAÇÃO CONCLUÍDA:");
                System.out.println("      Checkout anterior: " + checkoutAnterior.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                ));
                System.out.println("      Novo checkout: " + novoCheckout.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                ));
                System.out.println("      Qtd diárias: " + (reserva.getQuantidadeDiaria() - 1) + 
                                 " → " + reserva.getQuantidadeDiaria());
                System.out.println("      Total diária anterior: R$ " + totalDiariaAnterior);
                System.out.println("      Diária adicional: R$ " + valorAdicional);
                System.out.println("      Novo total diária: R$ " + reserva.getTotalDiaria());
                System.out.println("      Total hospedagem: R$ " + reserva.getTotalHospedagem());
                System.out.println("      Total a pagar: R$ " + reserva.getTotalApagar());
                
                renovadas++;
                
                // TODO: Enviar notificação (WhatsApp/Email) sobre renovação
            }
        }
        
        System.out.println("\n📊 RESUMO:"); 
        System.out.println("   Total verificadas: " + reservasAtivas.size());
        System.out.println("   Renovações automáticas: " + renovadas);
        System.out.println("═══════════════════════════════════════════\n");
    }
}
