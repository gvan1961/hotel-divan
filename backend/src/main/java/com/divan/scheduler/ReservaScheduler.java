package com.divan.scheduler;

import com.divan.entity.Apartamento;
import com.divan.entity.ExtratoReserva;
import com.divan.entity.Reserva;
import com.divan.repository.ApartamentoRepository;
import com.divan.repository.ExtratoReservaRepository;
import com.divan.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReservaScheduler {

    @Autowired
    private ExtratoReservaRepository extratoReservaRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ApartamentoRepository apartamentoRepository;

    @Autowired
    private org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void ativarPreReservas() {
        ativarPreReservasInterno();
    }

    private void ativarPreReservasInterno() {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔄 VERIFICANDO PRÉ-RESERVAS PARA ATIVAR");
        System.out.println("   Data/Hora: " + LocalDateTime.now());
        System.out.println("═══════════════════════════════════════════");

        LocalDateTime agora = LocalDateTime.now();

        List<Reserva> preReservas = reservaRepository.findByStatus(Reserva.StatusReservaEnum.PRE_RESERVA);

        System.out.println("📋 Total de pré-reservas: " + preReservas.size());

        int ativadas = 0;

        for (Reserva reserva : preReservas) {
            LocalDateTime dataCheckin = reserva.getDataCheckin();

            if (!dataCheckin.isAfter(agora)) {
                System.out.println("───────────────────────────────────────────");
                System.out.println("✅ Ativando Reserva #" + reserva.getId());
                System.out.println("   Apartamento: " + reserva.getApartamento().getNumeroApartamento());
                System.out.println("   Cliente: " + reserva.getCliente().getNome());
                System.out.println("   Check-in: " + dataCheckin.toLocalDate());

                reserva.setStatus(Reserva.StatusReservaEnum.ATIVA);
                reservaRepository.save(reserva);

                Apartamento apartamento = reserva.getApartamento();
                apartamento.setStatus(Apartamento.StatusEnum.OCUPADO);
                apartamentoRepository.save(apartamento);

                System.out.println("   ✅ Reserva ativada!");
                System.out.println("   🏨 Apartamento " + apartamento.getNumeroApartamento() + " → OCUPADO");

                ativadas++;
            } else {
                System.out.println("⏭️ Reserva #" + reserva.getId() + " ainda é futura (check-in: " + dataCheckin.toLocalDate() + ")");
            }
        }

        System.out.println("═══════════════════════════════════════════");
        System.out.println("✅ VERIFICAÇÃO CONCLUÍDA");
        System.out.println("   Pré-reservas ativadas: " + ativadas);
        System.out.println("═══════════════════════════════════════════");
    }

    @Scheduled(cron = "0 1 12 * * *")
    @Transactional
    public void cobrarDiariaHospedesAtrasados() {
        LocalDateTime agora = LocalDateTime.now();

        System.out.println("═══════════════════════════════════════════");
        System.out.println("⏰ SCHEDULER 12:01 — VERIFICANDO CHECKOUTS VENCIDOS");
        System.out.println("   Data/Hora: " + agora);
        System.out.println("═══════════════════════════════════════════");

     //   List<Reserva> reservasVencidas = reservaRepository
     //       .findByStatusAndDataCheckoutBefore(Reserva.StatusReservaEnum.ATIVA, agora);
        
        List<Reserva> reservasVencidas = reservaRepository
        	    .findByStatusAndDataCheckoutBefore(
        	        Reserva.StatusReservaEnum.ATIVA, 
        	        LocalDateTime.now().toLocalDate().atStartOfDay() // ✅ apenas dias anteriores
        	    );
        
        

        System.out.println("🔍 Reservas com checkout vencido: " + reservasVencidas.size());

        int processadas = 0;

        for (Reserva reserva : reservasVencidas) {
            try {
                System.out.println("───────────────────────────────────────────");
                System.out.println("📋 Processando reserva #" + reserva.getId());
                System.out.println("   Apt: " + reserva.getApartamento().getNumeroApartamento());
                System.out.println("   Cliente: " + reserva.getCliente().getNome());
                System.out.println("   Checkout vencido: " + reserva.getDataCheckout());

                BigDecimal valorDiaria = reserva.getDiaria().getValor();

                ExtratoReserva extrato = new ExtratoReserva();
                extrato.setReserva(reserva);
                extrato.setDataHoraLancamento(agora.toLocalDate().atTime(12, 1));
                extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.DIARIA);
                extrato.setDescricao(String.format("Diária extra — permanência após 12:01 de %02d/%02d/%d",
                    agora.getDayOfMonth(),
                    agora.getMonthValue(),
                    agora.getYear()));
                extrato.setQuantidade(1);
                extrato.setValorUnitario(valorDiaria);
                extrato.setTotalLancamento(valorDiaria);
                extrato.setNotaVendaId(null);

                extratoReservaRepository.save(extrato);

                LocalDateTime novoCheckout = reserva.getDataCheckout().plusDays(1);
                reserva.setDataCheckout(novoCheckout);
                reserva.setQuantidadeDiaria(reserva.getQuantidadeDiaria() + 1);

                reserva.setTotalDiaria(reserva.getTotalDiaria().add(valorDiaria));
                reserva.setTotalHospedagem(reserva.getTotalHospedagem().add(valorDiaria));
                reserva.setTotalApagar(reserva.getTotalApagar().add(valorDiaria));

                reservaRepository.save(reserva);

                System.out.println("✅ Diária extra lançada: R$ " + valorDiaria);
                System.out.println("📅 Novo checkout: " + novoCheckout.toLocalDate());

                processadas++;

            } catch (Exception e) {
                System.err.println("❌ Erro na reserva #" + reserva.getId() + ": " + e.getMessage());
            }
        }

        System.out.println("═══════════════════════════════════════════");
        System.out.println("✅ SCHEDULER 12:01 CONCLUÍDO");
        System.out.println("   Processadas: " + processadas);
        System.out.println("═══════════════════════════════════════════");
    }

    @PostConstruct
    public void verificarAoIniciar() {
        System.out.println("🚀 Verificando pré-reservas ao iniciar aplicação...");
        transactionTemplate.execute(status -> {
            ativarPreReservasInterno();
            return null;
        });
    }
}