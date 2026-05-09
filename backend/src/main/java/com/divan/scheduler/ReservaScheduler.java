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
        System.out.println("🔄 VERIFICANDO PRÉ-RESERVAS PARA ALERTAR");
        System.out.println("   Data/Hora: " + LocalDateTime.now());
        System.out.println("═══════════════════════════════════════════");

        LocalDateTime agora = LocalDateTime.now();

        List<Reserva> preReservas = reservaRepository.findByStatus(Reserva.StatusReservaEnum.PRE_RESERVA);

        System.out.println("📋 Total de pré-reservas: " + preReservas.size());

        int alertas = 0;

        for (Reserva reserva : preReservas) {
            LocalDateTime dataCheckin = reserva.getDataCheckin();

            if (!dataCheckin.isAfter(agora)) {
                System.out.println("───────────────────────────────────────────");
                System.out.println("🔍 Pré-reserva #" + reserva.getId() + " com check-in no passado");
                System.out.println("   Apartamento: " + reserva.getApartamento().getNumeroApartamento());
                System.out.println("   Cliente: " + reserva.getCliente().getNome());
                System.out.println("   Check-in previsto: " + dataCheckin);

                Apartamento apartamento = reserva.getApartamento();

                if (apartamento.getStatus() == Apartamento.StatusEnum.OCUPADO) {
                    System.out.println("⚠️ ALERTA — Apt " + apartamento.getNumeroApartamento()
                        + " está OCUPADO — recepção deve tomar decisão!");
                } else if (apartamento.getStatus() == Apartamento.StatusEnum.LIMPEZA) {
                    System.out.println("⚠️ ALERTA — Apt " + apartamento.getNumeroApartamento()
                        + " está em LIMPEZA — aguardando liberação para ativar!");
                } else {
                    System.out.println("ℹ️ Apt " + apartamento.getNumeroApartamento()
                        + " está DISPONÍVEL — aguardando check-in manual pelo recepcionista!");
                }

                alertas++;
            } else {
                System.out.println("⏭️ Reserva #" + reserva.getId()
                    + " ainda é futura (check-in: " + dataCheckin.toLocalDate() + ")");
            }
        }

        System.out.println("═══════════════════════════════════════════");
        System.out.println("✅ VERIFICAÇÃO CONCLUÍDA");
        System.out.println("   Pré-reservas pendentes de check-in: " + alertas);
        System.out.println("   ⚠️ Ativação automática DESATIVADA");
        System.out.println("   ✅ Ativação manual pelo recepcionista no Painel");
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

        List<Reserva> reservasVencidas = reservaRepository
            .findByStatusAndDataCheckoutBefore(
                Reserva.StatusReservaEnum.ATIVA,
                LocalDateTime.now().toLocalDate().atStartOfDay()
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

                // ✅ VERIFICAR SE JÁ EXISTE OUTRA RESERVA NO NOVO PERÍODO
                LocalDateTime novoCheckoutVerif = reserva.getDataCheckout().plusDays(1);
                List<Reserva> conflitos = reservaRepository.findByApartamentoIdAndStatusIn(
                    reserva.getApartamento().getId(),
                    List.of(Reserva.StatusReservaEnum.ATIVA, Reserva.StatusReservaEnum.PRE_RESERVA)
                );

                boolean temConflito = conflitos.stream()
                    .filter(r -> !r.getId().equals(reserva.getId()))
                    .anyMatch(r -> reserva.getDataCheckout().isBefore(r.getDataCheckout())
                               && novoCheckoutVerif.isAfter(r.getDataCheckin()));

                if (temConflito) {
                    System.out.println("⚠️ Reserva #" + reserva.getId()
                        + " NÃO renovada — conflito com outra reserva no novo período");
                    continue;
                }

                BigDecimal valorDiaria = reserva.getDiaria().getValor();

                ExtratoReserva extrato = new ExtratoReserva();
                extrato.setReserva(reserva);
                extrato.setDataHoraLancamento(agora.toLocalDate().atTime(12, 1));
                extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.DIARIA);
                extrato.setDescricao(String.format("Diária extra — permanência após 12:01 de %02d/%02d/%d",
                    agora.getDayOfMonth(), agora.getMonthValue(), agora.getYear()));
                extrato.setQuantidade(1);
                extrato.setValorUnitario(valorDiaria);
                extrato.setTotalLancamento(valorDiaria);
                extrato.setNotaVendaId(null);

                extratoReservaRepository.saveAndFlush(extrato);

                Reserva reservaAtualizada = reservaRepository.findById(reserva.getId()).orElse(reserva);

                LocalDateTime novoCheckout = reservaAtualizada.getDataCheckout().plusDays(1);
                reservaAtualizada.setDataCheckout(novoCheckout);
                reservaAtualizada.setQuantidadeDiaria(reservaAtualizada.getQuantidadeDiaria() + 1);
                reservaAtualizada.setTotalDiaria(reservaAtualizada.getTotalDiaria().add(valorDiaria));
                reservaAtualizada.setTotalHospedagem(reservaAtualizada.getTotalHospedagem().add(valorDiaria));
                reservaAtualizada.setTotalApagar(reservaAtualizada.getTotalApagar().add(valorDiaria));
                reservaAtualizada.setRenovacaoAutomatica(true);
                reservaRepository.saveAndFlush(reservaAtualizada);

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
    
    /**
     * 🕐 Renova automaticamente diárias de reservas com checkout
     * vencido HOJE (após 2h de tolerância).
     * 
     * Roda todo dia às 14h05.
     * - 14h00 = checkout 12h00 + 2h de tolerância
     * - 14h05 = margem para garantir
     * 
     * Para checkouts de DIAS ANTERIORES, ver o job das 12h01.
     */
    @Scheduled(cron = "0 5 14 * * *")
    @Transactional
    public void renovarDiariaPorTolerancia() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioHoje = agora.toLocalDate().atStartOfDay();
        LocalDateTime fimHoje = agora.toLocalDate().atTime(23, 59, 59);

        System.out.println("═══════════════════════════════════════════");
        System.out.println("⏰ SCHEDULER 14:05 — RENOVAÇÃO POR TOLERÂNCIA");
        System.out.println("   Data/Hora: " + agora);
        System.out.println("═══════════════════════════════════════════");

        List<Reserva> reservasHoje = reservaRepository
            .findByStatusAndDataCheckoutBetween(
                Reserva.StatusReservaEnum.ATIVA,
                inicioHoje,
                fimHoje
            );

        System.out.println("🔍 Reservas com checkout HOJE: " + reservasHoje.size());

        int processadas = 0;
        int ignoradas = 0;

        for (Reserva reserva : reservasHoje) {
            try {
                if (reserva.getDataCheckout().isAfter(agora)) {
                    System.out.println("⏭️ Reserva #" + reserva.getId()
                        + " — checkout ainda futuro (" + reserva.getDataCheckout() + ")");
                    ignoradas++;
                    continue;
                }

                // ✅ VERIFICAR SE JÁ EXISTE OUTRA RESERVA NO NOVO PERÍODO
                LocalDateTime novoCheckoutVerif = reserva.getDataCheckout().plusDays(1);
                List<Reserva> conflitos = reservaRepository.findByApartamentoIdAndStatusIn(
                    reserva.getApartamento().getId(),
                    List.of(Reserva.StatusReservaEnum.ATIVA, Reserva.StatusReservaEnum.PRE_RESERVA)
                );

                boolean temConflito = conflitos.stream()
                    .filter(r -> !r.getId().equals(reserva.getId()))
                    .anyMatch(r -> reserva.getDataCheckout().isBefore(r.getDataCheckout())
                               && novoCheckoutVerif.isAfter(r.getDataCheckin()));

                if (temConflito) {
                    System.out.println("⚠️ Reserva #" + reserva.getId()
                        + " NÃO renovada — conflito com outra reserva no novo período");
                    ignoradas++;
                    continue;
                }

                System.out.println("───────────────────────────────────────────");
                System.out.println("📋 Renovando reserva #" + reserva.getId());
                System.out.println("   Apt: " + reserva.getApartamento().getNumeroApartamento());
                System.out.println("   Cliente: " + reserva.getCliente().getNome());
                System.out.println("   Checkout previsto: " + reserva.getDataCheckout());

                BigDecimal valorDiaria = reserva.getDiaria().getValor();

                ExtratoReserva extrato = new ExtratoReserva();
                extrato.setReserva(reserva);
                extrato.setDataHoraLancamento(agora);
                extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.DIARIA);
                extrato.setDescricao(String.format(
                    "Diária extra — renovação automática (tolerância 2h excedida) %02d/%02d/%d",
                    agora.getDayOfMonth(), agora.getMonthValue(), agora.getYear()));
                extrato.setQuantidade(1);
                extrato.setValorUnitario(valorDiaria);
                extrato.setTotalLancamento(valorDiaria);
                extrato.setNotaVendaId(null);

                extratoReservaRepository.saveAndFlush(extrato);

                Reserva reservaAtualizada = reservaRepository.findById(reserva.getId()).orElse(reserva);

                LocalDateTime novoCheckout = reservaAtualizada.getDataCheckout().plusDays(1);
                reservaAtualizada.setDataCheckout(novoCheckout);
                reservaAtualizada.setQuantidadeDiaria(reservaAtualizada.getQuantidadeDiaria() + 1);
                reservaAtualizada.setTotalDiaria(reservaAtualizada.getTotalDiaria().add(valorDiaria));
                reservaAtualizada.setTotalHospedagem(reservaAtualizada.getTotalHospedagem().add(valorDiaria));
                reservaAtualizada.setTotalApagar(reservaAtualizada.getTotalApagar().add(valorDiaria));
                reservaAtualizada.setRenovacaoAutomatica(true);
                reservaRepository.saveAndFlush(reservaAtualizada);

                System.out.println("✅ Diária extra lançada: R$ " + valorDiaria);
                System.out.println("📅 Novo checkout: " + novoCheckout.toLocalDate());

                processadas++;

            } catch (Exception e) {
                System.err.println("❌ Erro na reserva #" + reserva.getId() + ": " + e.getMessage());
            }
        }

        System.out.println("═══════════════════════════════════════════");
        System.out.println("✅ SCHEDULER 14:05 CONCLUÍDO");
        System.out.println("   Renovadas: " + processadas);
        System.out.println("   Ignoradas: " + ignoradas);
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