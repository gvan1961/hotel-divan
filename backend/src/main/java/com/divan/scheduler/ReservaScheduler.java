package com.divan.scheduler;

import com.divan.entity.Apartamento;
import com.divan.entity.ControleDiaria;
import com.divan.entity.Reserva;
import com.divan.repository.ApartamentoRepository;
import com.divan.repository.ReservaRepository;
import com.divan.service.ControleDiariaService;
import com.divan.util.DataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReservaScheduler {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ApartamentoRepository apartamentoRepository;
    
    @Autowired
    private ControleDiariaService controleDiariaService;

    /**
     * 🕐 VERSÃO 1: Roda a cada 1 hora (mais eficiente)
     * Ativa pré-reservas quando chega a hora do check-in
     */
    @Scheduled(cron = "0 0 * * * *")  // ✅ A cada hora (00:00, 01:00, 02:00...)
    @Transactional
    public void ativarPreReservasHorario() {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🕐 VERIFICANDO PRÉ-RESERVAS (HORÁRIO)");
        System.out.println("   Data/Hora: " + DataUtil.formatarDataHoraCompleto(LocalDateTime.now()));
        System.out.println("═══════════════════════════════════════════");

        LocalDateTime agora = LocalDateTime.now();

        // ✅ Buscar PRE_RESERVAS cujo check-in já chegou (data E hora)
        List<Reserva> preReservas = reservaRepository.findByStatus(Reserva.StatusReservaEnum.PRE_RESERVA);

        if (preReservas.isEmpty()) {
            System.out.println("✅ Nenhuma pré-reserva para processar");
            System.out.println("═══════════════════════════════════════════");
            return;
        }

        System.out.println("📋 Total de pré-reservas encontradas: " + preReservas.size());

        int ativadas = 0;

        for (Reserva reserva : preReservas) {
            LocalDateTime dataCheckin = reserva.getDataCheckin();

            // ✅ CORRIGIDO: Ativa apenas se a data/hora do check-in JÁ PASSOU ou É AGORA
            if (dataCheckin.isBefore(agora) || dataCheckin.isEqual(agora)) {
                System.out.println("───────────────────────────────────────────");
                System.out.println("✅ Ativando Reserva #" + reserva.getId());
                System.out.println("   Apartamento: " + reserva.getApartamento().getNumeroApartamento());
                System.out.println("   Cliente: " + reserva.getCliente().getNome());
                System.out.println("   Check-in previsto: " + DataUtil.formatarDataHora(dataCheckin));
                System.out.println("   Horário atual: " + DataUtil.formatarDataHora(agora));

                try {
                    // Mudar status da reserva para ATIVA
                    reserva.setStatus(Reserva.StatusReservaEnum.ATIVA);
                    reservaRepository.save(reserva);

                    // Mudar status do apartamento para OCUPADO
                    Apartamento apartamento = reserva.getApartamento();
                    apartamento.setStatus(Apartamento.StatusEnum.OCUPADO);
                    apartamentoRepository.save(apartamento);

                    System.out.println("   ✅ Reserva ativada com sucesso!");
                    System.out.println("   🏨 Apartamento " + apartamento.getNumeroApartamento() + " → OCUPADO");
                    
                    // 🆕 LANÇAR PRIMEIRA DIÁRIA
                    try {
                        controleDiariaService.lancarDiaria(reserva);
                        System.out.println("   💰 Primeira diária lançada!");
                    } catch (Exception e) {
                        System.err.println("   ⚠️ Erro ao lançar diária: " + e.getMessage());
                    }

                    ativadas++;
                    
                } catch (Exception e) {
                    System.err.println("   ❌ Erro ao ativar reserva: " + e.getMessage());
                }
            } else {
                // Reserva ainda é futura
                long horasRestantes = java.time.Duration.between(agora, dataCheckin).toHours();
                System.out.println("⏭️ Reserva #" + reserva.getId() + " ainda é futura");
                System.out.println("   Check-in em: " + horasRestantes + "h (" + DataUtil.formatarDataHora(dataCheckin) + ")");
            }
        }

        System.out.println("═══════════════════════════════════════════");
        System.out.println("✅ VERIFICAÇÃO CONCLUÍDA");
        System.out.println("   Pré-reservas ativadas: " + ativadas);
        System.out.println("   Pré-reservas futuras: " + (preReservas.size() - ativadas));
        System.out.println("═══════════════════════════════════════════");
    }

    /**
     * 🕐 VERSÃO 2: Roda apenas à meia-noite (backup)
     * Ativa todas as reservas do dia, independente da hora
     */
    @Scheduled(cron = "0 1 0 * * *")  // ✅ 00:01 da manhã
    @Transactional
    public void ativarPreReservasDoDia() {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🌙 ATIVAÇÃO NOTURNA - PRÉ-RESERVAS DO DIA");
        System.out.println("   Data: " + DataUtil.formatarData(LocalDate.now()));
        System.out.println("═══════════════════════════════════════════");

        LocalDate hoje = LocalDate.now();

        // Buscar PRE_RESERVAS cujo check-in é HOJE
        List<Reserva> preReservas = reservaRepository.findByStatus(Reserva.StatusReservaEnum.PRE_RESERVA);

        if (preReservas.isEmpty()) {
            System.out.println("✅ Nenhuma pré-reserva para processar");
            System.out.println("═══════════════════════════════════════════");
            return;
        }

        int ativadas = 0;

        for (Reserva reserva : preReservas) {
            LocalDate dataCheckin = reserva.getDataCheckin().toLocalDate();

            // Se o check-in é HOJE ou JÁ PASSOU
            if (dataCheckin.isBefore(hoje) || dataCheckin.isEqual(hoje)) {
                System.out.println("───────────────────────────────────────────");
                System.out.println("✅ Ativando Reserva #" + reserva.getId());
                System.out.println("   Apartamento: " + reserva.getApartamento().getNumeroApartamento());
                System.out.println("   Cliente: " + reserva.getCliente().getNome());

                try {
                    reserva.setStatus(Reserva.StatusReservaEnum.ATIVA);
                    reservaRepository.save(reserva);

                    Apartamento apartamento = reserva.getApartamento();
                    apartamento.setStatus(Apartamento.StatusEnum.OCUPADO);
                    apartamentoRepository.save(apartamento);

                    System.out.println("   ✅ Ativada!");
                    
                    // 🆕 LANÇAR PRIMEIRA DIÁRIA
                    try {
                        controleDiariaService.lancarDiaria(reserva);
                        System.out.println("   💰 Primeira diária lançada!");
                    } catch (Exception e) {
                        System.err.println("   ⚠️ Erro ao lançar diária: " + e.getMessage());
                    }
                    
                    ativadas++;
                    
                } catch (Exception e) {
                    System.err.println("   ❌ Erro: " + e.getMessage());
                }
            }
        }

        System.out.println("═══════════════════════════════════════════");
        System.out.println("✅ ATIVAÇÃO NOTURNA CONCLUÍDA");
        System.out.println("   Total ativadas: " + ativadas);
        System.out.println("═══════════════════════════════════════════");
    }

    /**
     * 💰 PROCESSAR DIÁRIAS ÀS 12h01
     * Fecha diárias lançadas e lança novas diárias
     */
    @Scheduled(cron = "0 1 12 * * *")  // Todo dia às 12h01
    @Transactional
    public void processarDiariasAoMeioDia() {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("💰 PROCESSAMENTO DE DIÁRIAS - 12h01");
        System.out.println("   Data/Hora: " + DataUtil.formatarDataHoraCompleto(LocalDateTime.now()));
        System.out.println("═══════════════════════════════════════════");
        
        // 1️⃣ FECHAR DIÁRIAS LANÇADAS
        List<ControleDiaria> diariasParaFechar = controleDiariaService.buscarDiariasParaFechar();
        
        System.out.println("🔍 Diárias para fechar: " + diariasParaFechar.size());
        
        int fechadas = 0;
        for (ControleDiaria controle : diariasParaFechar) {
            try {
                controleDiariaService.fecharDiaria(controle);
                fechadas++;
            } catch (Exception e) {
                System.err.println("❌ Erro ao fechar diária ID " + controle.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("✅ Diárias fechadas: " + fechadas);
        System.out.println("───────────────────────────────────────────");
        
        // 2️⃣ LANÇAR NOVAS DIÁRIAS PARA RESERVAS ATIVAS
        List<Reserva> reservasAtivas = reservaRepository.findByStatus(Reserva.StatusReservaEnum.ATIVA);
        
        System.out.println("🏨 Reservas ativas: " + reservasAtivas.size());
        
        int lancadas = 0;
        for (Reserva reserva : reservasAtivas) {
            try {
                controleDiariaService.lancarDiaria(reserva);
                lancadas++;
            } catch (Exception e) {
                System.err.println("❌ Erro ao lançar diária para reserva #" + reserva.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("✅ Novas diárias lançadas: " + lancadas);
        System.out.println("═══════════════════════════════════════════");
        System.out.println("📊 RESUMO:");
        System.out.println("   Diárias fechadas: " + fechadas);
        System.out.println("   Diárias lançadas: " + lancadas);
        System.out.println("═══════════════════════════════════════════");
    }

    /**
     * ⚠️ Verifica check-outs vencidos
     * Roda a cada 2 horas
     */
    @Scheduled(cron = "0 0 */2 * * *")  // ✅ A cada 2 horas
    @Transactional(readOnly = true)
    public void verificarCheckoutsVencidos() {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("⚠️ VERIFICANDO CHECK-OUTS VENCIDOS");
        System.out.println("   Data/Hora: " + DataUtil.formatarDataHoraCompleto(LocalDateTime.now()));
        System.out.println("═══════════════════════════════════════════");

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limiteAtraso = agora.minusHours(2); // 2h de tolerância

        List<Reserva> reservasAtivas = reservaRepository.findByStatus(Reserva.StatusReservaEnum.ATIVA);

        int atrasadas = 0;

        for (Reserva reserva : reservasAtivas) {
            LocalDateTime dataCheckout = reserva.getDataCheckout();

            // Se o checkout já passou há mais de 2 horas
            if (dataCheckout.isBefore(limiteAtraso)) {
                long horasAtraso = java.time.Duration.between(dataCheckout, agora).toHours();

                System.out.println("───────────────────────────────────────────");
                System.out.println("⚠️ CHECKOUT VENCIDO - Reserva #" + reserva.getId());
                System.out.println("   Apartamento: " + reserva.getApartamento().getNumeroApartamento());
                System.out.println("   Cliente: " + reserva.getCliente().getNome());
                System.out.println("   Check-out previsto: " + DataUtil.formatarDataHora(dataCheckout));
                System.out.println("   Atraso: " + horasAtraso + " hora(s)");
                System.out.println("   ⚠️ AÇÃO NECESSÁRIA: Realizar checkout!");

                atrasadas++;
            }
        }

        if (atrasadas == 0) {
            System.out.println("✅ Nenhuma reserva com checkout atrasado");
        }

        System.out.println("═══════════════════════════════════════════");
        System.out.println("📊 RESUMO:");
        System.out.println("   Reservas ativas: " + reservasAtivas.size());
        System.out.println("   Check-outs atrasados: " + atrasadas);
        System.out.println("═══════════════════════════════════════════");
    }

    /**
     * 📊 Relatório diário às 08:00
     */
    @Scheduled(cron = "0 0 8 * * *")  // ✅ Às 08:00 da manhã
    @Transactional(readOnly = true)
    public void relatorioDiario() {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("📊 RELATÓRIO DIÁRIO DE RESERVAS");
        System.out.println("   Data: " + DataUtil.formatarDataComDiaSemana(LocalDate.now()));
        System.out.println("═══════════════════════════════════════════");

        LocalDate hoje = LocalDate.now();

        // Pré-reservas
        List<Reserva> preReservas = reservaRepository.findByStatus(Reserva.StatusReservaEnum.PRE_RESERVA);
        long preReservasHoje = preReservas.stream()
            .filter(r -> r.getDataCheckin().toLocalDate().isEqual(hoje))
            .count();

        // Ativas
        List<Reserva> ativas = reservaRepository.findByStatus(Reserva.StatusReservaEnum.ATIVA);

        // Checkouts hoje
        long checkoutsHoje = ativas.stream()
            .filter(r -> r.getDataCheckout().toLocalDate().isEqual(hoje))
            .count();

        System.out.println("📋 PRÉ-RESERVAS:");
        System.out.println("   Total: " + preReservas.size());
        System.out.println("   Check-ins hoje: " + preReservasHoje);
        System.out.println("");
        System.out.println("🏨 RESERVAS ATIVAS:");
        System.out.println("   Total: " + ativas.size());
        System.out.println("   Check-outs hoje: " + checkoutsHoje);
        System.out.println("");
        System.out.println("📊 OCUPAÇÃO:");
        
        long totalApartamentos = apartamentoRepository.count();
        long apartamentosOcupados = apartamentoRepository.countByStatus(Apartamento.StatusEnum.OCUPADO);
        double taxaOcupacao = totalApartamentos > 0 ? (apartamentosOcupados * 100.0 / totalApartamentos) : 0;
        
        System.out.println("   Total apartamentos: " + totalApartamentos);
        System.out.println("   Ocupados: " + apartamentosOcupados);
        System.out.println("   Taxa de ocupação: " + String.format("%.1f%%", taxaOcupacao));
        System.out.println("═══════════════════════════════════════════");
    }
}