package com.divan.service;

import com.divan.dto.AlertaDTO;
import com.divan.dto.ConflitoPrReservaDTO;
import com.divan.entity.Apartamento;
import com.divan.entity.Reserva;
import com.divan.repository.ApartamentoRepository;
import com.divan.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConflitosService {
    
    private final ReservaRepository reservaRepository;
    private final ApartamentoRepository apartamentoRepository;
    
    public ConflitosService(ReservaRepository reservaRepository, 
                           ApartamentoRepository apartamentoRepository) {
        this.reservaRepository = reservaRepository;
        this.apartamentoRepository = apartamentoRepository;
    }
    
    @Transactional(readOnly = true)
    public List<ConflitoPrReservaDTO> detectarConflitos() {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔍 DETECTANDO CONFLITOS DE PRÉ-RESERVAS");
        
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioDia = hoje.atStartOfDay();
        LocalDateTime fimDia = hoje.atTime(23, 59, 59);
        
        List<ConflitoPrReservaDTO> conflitos = new ArrayList<>();
        
        List<Reserva> preReservasHoje = reservaRepository.findByStatusAndDataCheckinBetween(
            Reserva.StatusReservaEnum.PRE_RESERVA,
            inicioDia,
            fimDia
        );
        
        System.out.println("📅 Pré-reservas para hoje: " + preReservasHoje.size());
        
        for (Reserva preReserva : preReservasHoje) {
            
            if (preReserva.getApartamento() == null) {
                continue;
            }
            
            Long apartamentoId = preReserva.getApartamento().getId();
            
            List<Reserva> reservasAtivas = reservaRepository.findByApartamentoIdAndStatus(
                apartamentoId,
                Reserva.StatusReservaEnum.ATIVA
            );
            
            for (Reserva reservaAtiva : reservasAtivas) {
                
                LocalDateTime checkoutPrevisto = reservaAtiva.getDataCheckout();
                LocalDateTime agora = LocalDateTime.now();
                
                if (checkoutPrevisto.isBefore(agora)) {
                    
                    System.out.println("🚨 CONFLITO DETECTADO!");
                    
                    long horasAtraso = ChronoUnit.HOURS.between(checkoutPrevisto, agora);
                    
                    Long tipoApartamentoId = null;
                    if (preReserva.getApartamento().getTipoApartamento() != null) {
                        tipoApartamentoId = preReserva.getApartamento().getTipoApartamento().getId();
                    }
                    
                    List<ConflitoPrReservaDTO.ApartamentoDisponivelDTO> apartamentosDisponiveis = 
                        buscarApartamentosDisponiveis(tipoApartamentoId, hoje);
                    
                    ConflitoPrReservaDTO conflito = new ConflitoPrReservaDTO();
                    
                    conflito.setApartamentoId(apartamentoId);
                    conflito.setNumeroApartamento(preReserva.getApartamento().getNumeroApartamento());
                    
                    String tipoApto = "N/A";
                    if (preReserva.getApartamento().getTipoApartamento() != null) {                        
                    	tipoApto = preReserva.getApartamento().getTipoApartamento().getTipo().name();

                    }
                    conflito.setTipoApartamento(tipoApto);
                    
                    conflito.setReservaAtualId(reservaAtiva.getId());
                    
                    String nomeHospede = "N/A";
                    if (reservaAtiva.getCliente() != null) {
                        nomeHospede = reservaAtiva.getCliente().getNome();
                    }
                    conflito.setHospedeAtualNome(nomeHospede);
                    
                    conflito.setCheckoutPrevisto(checkoutPrevisto);
                    conflito.setHorasAtraso(horasAtraso);
                    
                    conflito.setPreReservaId(preReserva.getId());
                    
                    String nomeCliente = "N/A";
                    if (preReserva.getCliente() != null) {
                        nomeCliente = preReserva.getCliente().getNome();
                    }
                    conflito.setClientePreReservaNome(nomeCliente);
                    
                    conflito.setDataCheckinPreReserva(preReserva.getDataCheckin());
                    conflito.setApartamentosDisponiveis(apartamentosDisponiveis);
                    
                    if (apartamentosDisponiveis.isEmpty()) {
                        conflito.setNivelGravidade("CRITICO");
                        conflito.setRecomendacao("SEM APARTAMENTOS DISPONÍVEIS! Fazer checkout urgente ou cancelar pré-reserva.");
                    } else if (horasAtraso > 24) {
                        conflito.setNivelGravidade("ALTO");
                        conflito.setRecomendacao("Atraso de mais de 1 dia. Transferir pré-reserva ou fazer checkout imediato.");
                    } else {
                        conflito.setNivelGravidade("MEDIO");
                        conflito.setRecomendacao("Transferir pré-reserva para apartamento disponível.");
                    }
                    
                    conflitos.add(conflito);
                }
            }
        }
        
        System.out.println("✅ Total de conflitos detectados: " + conflitos.size());
        System.out.println("═══════════════════════════════════════════");
        
        return conflitos;
    }
    
    private List<ConflitoPrReservaDTO.ApartamentoDisponivelDTO> buscarApartamentosDisponiveis(
        Long tipoApartamentoId, 
        LocalDate data
    ) {
        
        List<ConflitoPrReservaDTO.ApartamentoDisponivelDTO> disponiveis = new ArrayList<>();
        
        List<Apartamento> todosApartamentos = apartamentoRepository.findAll();
        
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.atTime(23, 59, 59);
        
        for (Apartamento apto : todosApartamentos) {
            
            if (apto.getTipoApartamento() == null) {
                continue;
            }
            
            List<Reserva> reservasNoDia = reservaRepository.findByApartamentoIdAndDataCheckinBetween(
                apto.getId(),
                inicioDia,
                fimDia
            );
            
            long reservasConflitantes = reservasNoDia.stream()
                .filter(r -> r.getStatus() == Reserva.StatusReservaEnum.ATIVA 
                          || r.getStatus() == Reserva.StatusReservaEnum.PRE_RESERVA)
                .count();
            
            if (reservasConflitantes == 0) {
                
                ConflitoPrReservaDTO.ApartamentoDisponivelDTO dto = 
                    new ConflitoPrReservaDTO.ApartamentoDisponivelDTO();
                
                dto.setApartamentoId(apto.getId());
                dto.setNumeroApartamento(apto.getNumeroApartamento());
                
                String tipoAptoDisp = "N/A";
                if (apto.getTipoApartamento() != null) {
                	tipoAptoDisp = apto.getTipoApartamento().getTipo().name();
                }
                dto.setTipoApartamento(tipoAptoDisp);
                
                if (tipoApartamentoId != null 
                    && apto.getTipoApartamento() != null 
                    && apto.getTipoApartamento().getId().equals(tipoApartamentoId)) {
                    dto.setCategoria("MESMO_TIPO");
                    dto.setRecomendado(true);
                } else {
                    dto.setCategoria("OUTRO");
                    dto.setRecomendado(false);
                }
                
                disponiveis.add(dto);
            }
        }
        
        disponiveis.sort((a, b) -> {
            if (a.getRecomendado() != null && b.getRecomendado() != null) {
                if (a.getRecomendado() && !b.getRecomendado()) return -1;
                if (!a.getRecomendado() && b.getRecomendado()) return 1;
            }
            return a.getNumeroApartamento().compareTo(b.getNumeroApartamento());
        });
        
        return disponiveis;
    }
    
    @Transactional
    public void transferirPreReserva(Long preReservaId, Long novoApartamentoId, String motivo) {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔄 TRANSFERINDO PRÉ-RESERVA");
        
        Reserva preReserva = reservaRepository.findById(preReservaId)
            .orElseThrow(() -> new RuntimeException("Pré-reserva não encontrada"));
        
        Apartamento novoApartamento = apartamentoRepository.findById(novoApartamentoId)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        String apartamentoAntigo = "N/A";
        if (preReserva.getApartamento() != null) {
            apartamentoAntigo = preReserva.getApartamento().getNumeroApartamento();
        }
        
        String apartamentoNovo = novoApartamento.getNumeroApartamento();
        
        preReserva.setApartamento(novoApartamento);
        
        String mensagem = String.format(
            "Pré-reserva transferida do apartamento %s para %s. Motivo: %s",
            apartamentoAntigo,
            apartamentoNovo,
            motivo != null ? motivo : "Conflito de reserva"
        );
        
        System.out.println("✅ " + mensagem);
        
        reservaRepository.save(preReserva);
        
        System.out.println("═══════════════════════════════════════════");
    }
    
    /**
     * ⏰ DETECTAR CHECKOUTS VENCIDOS (após 12:01h)
     */
    @Transactional(readOnly = true)
    public List<AlertaDTO> detectarCheckoutsVencidos() {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("⏰ DETECTANDO CHECKOUTS VENCIDOS");
        System.out.println("═══════════════════════════════════════════");
        
        List<AlertaDTO> alertas = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now();
        
        // Buscar todas as reservas ativas
        List<Reserva> reservasAtivas = reservaRepository.findByStatus(Reserva.StatusReservaEnum.ATIVA);
        
        System.out.println("📊 Reservas ativas: " + reservasAtivas.size());
        
        for (Reserva reserva : reservasAtivas) {
            
            LocalDateTime checkoutPrevisto = reserva.getDataCheckout();
            
            // Se o checkout já passou = VENCIDO
            if (checkoutPrevisto.isBefore(agora)) {
                
                long horasAtraso = ChronoUnit.HOURS.between(checkoutPrevisto, agora);
                long minutosAtraso = ChronoUnit.MINUTES.between(checkoutPrevisto, agora);
                
                System.out.println("⏰ Checkout vencido: Apto " + 
                    (reserva.getApartamento() != null ? reserva.getApartamento().getNumeroApartamento() : "N/A") +
                    " - Atraso: " + horasAtraso + "h " + (minutosAtraso % 60) + "min");
                
                AlertaDTO alerta = new AlertaDTO();
                
                alerta.setTipoAlerta("CHECKOUT_VENCIDO");
                alerta.setDataHoraAlerta(agora);
                
                // Apartamento
                if (reserva.getApartamento() != null) {
                    alerta.setApartamentoId(reserva.getApartamento().getId());
                    alerta.setNumeroApartamento(reserva.getApartamento().getNumeroApartamento());
                    
                    if (reserva.getApartamento().getTipoApartamento() != null) {
                        alerta.setTipoApartamento(reserva.getApartamento().getTipoApartamento().getTipo().name());
                    }
                }
                
                // Reserva
                alerta.setReservaId(reserva.getId());
                alerta.setClienteNome(reserva.getCliente() != null ? reserva.getCliente().getNome() : "N/A");
                alerta.setStatusReserva(reserva.getStatus().name());
                alerta.setDataCheckout(checkoutPrevisto);
                
                // Atraso
                alerta.setHorasAtraso(horasAtraso);
                alerta.setMinutosAtraso(minutosAtraso);
                
                // Gravidade baseada no tempo de atraso
                if (horasAtraso >= 6) {
                    alerta.setNivelGravidade("CRITICO");
                    alerta.setTitulo("Checkout MUITO atrasado - Cobrar diária completa");
                    alerta.setRecomendacao("Cobrar diária completa adicional e contatar hóspede urgentemente.");
                    alerta.setAcoesDisponiveis(List.of("COBRAR_DIARIA_COMPLETA", "FAZER_CHECKOUT", "LIGAR_CLIENTE", "PRORROGAR"));
                } else if (horasAtraso >= 2) {
                    alerta.setNivelGravidade("ALTO");
                    alerta.setTitulo("Checkout atrasado - Cobrar meia diária");
                    alerta.setRecomendacao("Cobrar meia diária adicional ou contatar hóspede.");
                    alerta.setAcoesDisponiveis(List.of("COBRAR_MEIA_DIARIA", "FAZER_CHECKOUT", "LIGAR_CLIENTE", "PRORROGAR"));
                } else if (minutosAtraso > 30) {
                    alerta.setNivelGravidade("MEDIO");
                    alerta.setTitulo("Checkout levemente atrasado");
                    alerta.setRecomendacao("Contatar hóspede para confirmar saída.");
                    alerta.setAcoesDisponiveis(List.of("LIGAR_CLIENTE", "FAZER_CHECKOUT", "PRORROGAR"));
                } else {
                    alerta.setNivelGravidade("BAIXO");
                    alerta.setTitulo("Checkout acabou de vencer");
                    alerta.setRecomendacao("Monitorar. Tolerância até 30 minutos.");
                    alerta.setAcoesDisponiveis(List.of("AGUARDAR", "LIGAR_CLIENTE"));
                }
                
                alerta.setDescricao(String.format(
                    "Hóspede %s deveria ter feito checkout às %s. Atraso: %d hora(s) e %d minuto(s).",
                    alerta.getClienteNome(),
                    checkoutPrevisto.toLocalTime(),
                    horasAtraso,
                    minutosAtraso % 60
                ));
                
                alertas.add(alerta);
            }
        }
        
        System.out.println("✅ Checkouts vencidos detectados: " + alertas.size());
        System.out.println("═══════════════════════════════════════════");
        
        return alertas;
    }

    /**
     * 🔴 DETECTAR NO-SHOW (pré-reservas sem check-in após 18h)
     */
    @Transactional(readOnly = true)
    public List<AlertaDTO> detectarNoShows() {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔴 DETECTANDO NO-SHOWS (18h)");
        System.out.println("═══════════════════════════════════════════");
        
        List<AlertaDTO> alertas = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now();
        LocalDate hoje = LocalDate.now();
        
        // Só faz sentido alertar no-show após 18h
        if (agora.getHour() < 18) {
            System.out.println("⏰ Ainda não são 18h. No-show não será verificado.");
            System.out.println("═══════════════════════════════════════════");
            return alertas;
        }
        
        LocalDateTime inicioDia = hoje.atStartOfDay();
        LocalDateTime fimDia = hoje.atTime(23, 59, 59);
        
        // Buscar pré-reservas de hoje que ainda não viraram ATIVA
        List<Reserva> preReservasHoje = reservaRepository.findByStatusAndDataCheckinBetween(
            Reserva.StatusReservaEnum.PRE_RESERVA,
            inicioDia,
            fimDia
        );
        
        System.out.println("📅 Pré-reservas de hoje: " + preReservasHoje.size());
        
        for (Reserva reserva : preReservasHoje) {
            
            LocalDateTime checkinPrevisto = reserva.getDataCheckin();
            
            // Se o horário de check-in já passou e ainda está como PRE_RESERVA = NO-SHOW
            if (checkinPrevisto.isBefore(agora)) {
                
                System.out.println("🔴 Possível no-show: " + 
                    (reserva.getCliente() != null ? reserva.getCliente().getNome() : "N/A"));
                
                AlertaDTO alerta = new AlertaDTO();
                
                alerta.setTipoAlerta("NO_SHOW");
                alerta.setDataHoraAlerta(agora);
                
                // Apartamento
                if (reserva.getApartamento() != null) {
                    alerta.setApartamentoId(reserva.getApartamento().getId());
                    alerta.setNumeroApartamento(reserva.getApartamento().getNumeroApartamento());
                    
                    if (reserva.getApartamento().getTipoApartamento() != null) {
                        alerta.setTipoApartamento(reserva.getApartamento().getTipoApartamento().getTipo().name());
                    }
                }
                
                // Reserva
                alerta.setReservaId(reserva.getId());
                alerta.setClienteNome(reserva.getCliente() != null ? reserva.getCliente().getNome() : "N/A");
                alerta.setStatusReserva(reserva.getStatus().name());
                alerta.setDataCheckin(checkinPrevisto);
                
                // Calcular percentual pago
                double totalReserva = reserva.getTotalHospedagem() != null 
                    ? reserva.getTotalHospedagem().doubleValue() 
                    : 0.0;
                
                double totalPago = reserva.getTotalRecebido() != null 
                    ? reserva.getTotalRecebido().doubleValue() 
                    : 0.0;
                
                double percentualPago = totalReserva > 0 
                    ? (totalPago / totalReserva) * 100 
                    : 0.0;
                
                alerta.setTotalReserva(totalReserva);
                alerta.setTotalPago(totalPago);
                alerta.setPercentualPago(percentualPago);
                
                long horasAtraso = ChronoUnit.HOURS.between(checkinPrevisto, agora);
                alerta.setHorasAtraso(horasAtraso);
                
                // Gravidade baseada no pagamento
                if (percentualPago == 0) {
                    alerta.setNivelGravidade("CRITICO");
                    alerta.setTitulo("NO-SHOW sem pagamento - PREJUÍZO!");
                    alerta.setRecomendacao("Cliente não pagou nada e não compareceu. Cancelar reserva e tentar contato.");
                    alerta.setAcoesDisponiveis(List.of("MARCAR_NO_SHOW", "CANCELAR_RESERVA", "LIGAR_CLIENTE", "AGUARDAR_ATE_AMANHA"));
                } else if (percentualPago < 50) {
                    alerta.setNivelGravidade("ALTO");
                    alerta.setTitulo("NO-SHOW com pagamento parcial");
                    alerta.setRecomendacao("Cliente pagou " + String.format("%.0f%%", percentualPago) + ". Contatar urgentemente.");
                    alerta.setAcoesDisponiveis(List.of("MARCAR_NO_SHOW", "LIGAR_CLIENTE", "AGUARDAR_ATE_AMANHA"));
                } else {
                    alerta.setNivelGravidade("MEDIO");
                    alerta.setTitulo("Cliente com pagamento não compareceu");
                    alerta.setRecomendacao("Cliente pagou " + String.format("%.0f%%", percentualPago) + ". Pode estar atrasado.");
                    alerta.setAcoesDisponiveis(List.of("LIGAR_CLIENTE", "CONFIRMAR_CHEGADA", "AGUARDAR_ATE_AMANHA"));
                }
                
                alerta.setDescricao(String.format(
                    "Pré-reserva de %s para às %s. Cliente não compareceu. Pagamento: R$ %.2f (%.0f%%).",
                    alerta.getClienteNome(),
                    checkinPrevisto.toLocalTime(),
                    totalPago,
                    percentualPago
                ));
                
                alertas.add(alerta);
            }
        }
        
        System.out.println("✅ No-shows detectados: " + alertas.size());
        System.out.println("═══════════════════════════════════════════");
        
        return alertas;
    }

    /**
     * 📊 BUSCAR TODOS OS ALERTAS ATIVOS
     */
    @Transactional(readOnly = true)
    public Map<String, List<AlertaDTO>> buscarTodosAlertas() {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("📊 BUSCANDO TODOS OS ALERTAS ATIVOS");
        System.out.println("═══════════════════════════════════════════");
        
        Map<String, List<AlertaDTO>> todosAlertas = new java.util.HashMap<>();
        
        // 1. Conflitos de pré-reserva (8h)
        List<ConflitoPrReservaDTO> conflitos = detectarConflitos();
        List<AlertaDTO> alertasConflitos = converterConflitosParaAlertas(conflitos);
        todosAlertas.put("conflitos", alertasConflitos);
        
        // 2. Checkouts vencidos (12:01h)
        List<AlertaDTO> checkoutsVencidos = detectarCheckoutsVencidos();
        todosAlertas.put("checkoutsVencidos", checkoutsVencidos);
        
        // 3. No-shows (18h)
        List<AlertaDTO> noShows = detectarNoShows();
        todosAlertas.put("noShows", noShows);
        
        int totalAlertas = alertasConflitos.size() + checkoutsVencidos.size() + noShows.size();
        
        System.out.println("📊 RESUMO:");
        System.out.println("   Conflitos: " + alertasConflitos.size());
        System.out.println("   Checkouts vencidos: " + checkoutsVencidos.size());
        System.out.println("   No-shows: " + noShows.size());
        System.out.println("   TOTAL: " + totalAlertas);
        System.out.println("═══════════════════════════════════════════");
        
        return todosAlertas;
    }

    /**
     * 🔄 CONVERTER CONFLITOS PARA ALERTAS
     */
    private List<AlertaDTO> converterConflitosParaAlertas(List<ConflitoPrReservaDTO> conflitos) {
        
        List<AlertaDTO> alertas = new ArrayList<>();
        
        for (ConflitoPrReservaDTO conflito : conflitos) {
            
            AlertaDTO alerta = new AlertaDTO();
            
            alerta.setTipoAlerta("CONFLITO_PRE_RESERVA");
            alerta.setNivelGravidade(conflito.getNivelGravidade());
            alerta.setDataHoraAlerta(LocalDateTime.now());
            
            alerta.setApartamentoId(conflito.getApartamentoId());
            alerta.setNumeroApartamento(conflito.getNumeroApartamento());
            alerta.setTipoApartamento(conflito.getTipoApartamento());
            
            alerta.setReservaId(conflito.getPreReservaId());
            alerta.setClienteNome(conflito.getClientePreReservaNome());
            alerta.setDataCheckin(conflito.getDataCheckinPreReserva());
            alerta.setDataCheckout(conflito.getCheckoutPrevisto());
            
            alerta.setHorasAtraso(conflito.getHorasAtraso());
            
            alerta.setTitulo("Conflito de Pré-reserva - Apartamento Ocupado");
            alerta.setDescricao(String.format(
                "Apartamento %s está ocupado (checkout atrasado: %d horas). Pré-reserva de %s para hoje.",
                conflito.getNumeroApartamento(),
                conflito.getHorasAtraso(),
                conflito.getClientePreReservaNome()
            ));
            alerta.setRecomendacao(conflito.getRecomendacao());
            
            // Converter apartamentos disponíveis
            if (conflito.getApartamentosDisponiveis() != null && !conflito.getApartamentosDisponiveis().isEmpty()) {
                List<AlertaDTO.ApartamentoDisponivelDTO> aptosDisponiveis = new ArrayList<>();
                
                for (ConflitoPrReservaDTO.ApartamentoDisponivelDTO aptoConflito : conflito.getApartamentosDisponiveis()) {
                    AlertaDTO.ApartamentoDisponivelDTO aptoAlerta = new AlertaDTO.ApartamentoDisponivelDTO();
                    aptoAlerta.setApartamentoId(aptoConflito.getApartamentoId());
                    aptoAlerta.setNumeroApartamento(aptoConflito.getNumeroApartamento());
                    aptoAlerta.setTipoApartamento(aptoConflito.getTipoApartamento());
                    aptoAlerta.setCategoria(aptoConflito.getCategoria());
                    aptoAlerta.setRecomendado(aptoConflito.getRecomendado());
                    aptosDisponiveis.add(aptoAlerta);
                }
                
                alerta.setApartamentosDisponiveis(aptosDisponiveis);
                alerta.setAcoesDisponiveis(List.of("TRANSFERIR_PRE_RESERVA", "FAZER_CHECKOUT_HOSPEDE", "LIGAR_CLIENTE"));
            } else {
                alerta.setAcoesDisponiveis(List.of("FAZER_CHECKOUT_HOSPEDE", "CANCELAR_PRE_RESERVA", "LIGAR_CLIENTE"));
            }
            
            alertas.add(alerta);
        }
        
        return alertas;
    }
    
    /**
     * 🔄 PRORROGAR CHECKOUT
     */
    @Transactional
    public void prorrogarCheckout(Long reservaId, LocalDateTime novoCheckout, String motivo) {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔄 PRORROGANDO CHECKOUT");
        System.out.println("═══════════════════════════════════════════");
        
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        LocalDateTime checkoutAntigo = reserva.getDataCheckout();
        
        reserva.setDataCheckout(novoCheckout);
        
        String mensagem = String.format(
            "Checkout prorrogado de %s para %s. Motivo: %s",
            checkoutAntigo,
            novoCheckout,
            motivo != null ? motivo : "Solicitação do hóspede"
        );
        
        System.out.println("✅ " + mensagem);
        
        reservaRepository.save(reserva);
        
        System.out.println("═══════════════════════════════════════════");
    }

    /**
     * 💰 COBRAR DIÁRIA ADICIONAL (meia ou completa)
     */
    @Transactional
    public void cobrarDiariaAdicional(Long reservaId, String tipoDiaria, String motivo) {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("💰 COBRANDO DIÁRIA ADICIONAL");
        System.out.println("═══════════════════════════════════════════");
        
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        // Buscar valor da diária do tipo de apartamento
        // (Você precisará ajustar conforme sua lógica de diárias)
        java.math.BigDecimal valorDiaria = java.math.BigDecimal.ZERO;
        
        // Aqui você pode buscar o valor da diária do banco
        // Por enquanto, vou usar um valor fixo como exemplo
        valorDiaria = new java.math.BigDecimal("100.00");
        
        java.math.BigDecimal valorCobrar = valorDiaria;
        if ("MEIA".equals(tipoDiaria)) {
            valorCobrar = valorDiaria.divide(new java.math.BigDecimal("2"));
        }
        
        // Atualizar total da hospedagem
        java.math.BigDecimal totalAtual = reserva.getTotalHospedagem() != null 
            ? reserva.getTotalHospedagem() 
            : java.math.BigDecimal.ZERO;
        
        reserva.setTotalHospedagem(totalAtual.add(valorCobrar));
        
        String mensagem = String.format(
            "Cobrada %s adicional: R$ %.2f. Motivo: %s",
            tipoDiaria.equals("MEIA") ? "meia diária" : "diária completa",
            valorCobrar.doubleValue(),
            motivo != null ? motivo : "Checkout atrasado"
        );
        
        System.out.println("✅ " + mensagem);
        
        reservaRepository.save(reserva);
        
        System.out.println("═══════════════════════════════════════════");
    }

    /**
     * ❌ CANCELAR RESERVA
     */
    @Transactional
    public void cancelarReserva(Long reservaId, String motivo) {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("❌ CANCELANDO RESERVA");
        System.out.println("═══════════════════════════════════════════");
        
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        reserva.setStatus(Reserva.StatusReservaEnum.CANCELADA);
        
        String mensagem = String.format(
            "Reserva #%d cancelada. Cliente: %s. Motivo: %s",
            reserva.getId(),
            reserva.getCliente() != null ? reserva.getCliente().getNome() : "N/A",
            motivo != null ? motivo : "Não informado"
        );
        
        System.out.println("✅ " + mensagem);
        
        reservaRepository.save(reserva);
        
        System.out.println("═══════════════════════════════════════════");
    }

    /**
     * ✅ CONFIRMAR CHEGADA (PRE_RESERVA → ATIVA)
     */
    @Transactional
    public void confirmarChegada(Long reservaId, String observacao) {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("✅ CONFIRMANDO CHEGADA DO HÓSPEDE");
        System.out.println("═══════════════════════════════════════════");
        
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        if (reserva.getStatus() != Reserva.StatusReservaEnum.PRE_RESERVA) {
            throw new RuntimeException("Apenas pré-reservas podem ser confirmadas");
        }
        
        // Atualizar status para ATIVA
        reserva.setStatus(Reserva.StatusReservaEnum.ATIVA);
        
        // Registrar hora real de chegada (se tiver campo)
        // reserva.setDataHoraCheckinReal(LocalDateTime.now());
        
        String mensagem = String.format(
            "Check-in confirmado para %s. Apartamento: %s. Observação: %s",
            reserva.getCliente() != null ? reserva.getCliente().getNome() : "N/A",
            reserva.getApartamento() != null ? reserva.getApartamento().getNumeroApartamento() : "N/A",
            observacao != null ? observacao : "Nenhuma"
        );
        
        System.out.println("✅ " + mensagem);
        
        reservaRepository.save(reserva);
        
        System.out.println("═══════════════════════════════════════════");
    }

    /**
     * 🚪 FAZER CHECKOUT
     */
    /**
     * 🚪 FAZER CHECKOUT
     */
    @Transactional
    public Map<String, Object> fazerCheckout(Long reservaId, String observacao) {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🚪 FAZENDO CHECKOUT");
        System.out.println("═══════════════════════════════════════════");
        
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        if (reserva.getStatus() != Reserva.StatusReservaEnum.ATIVA) {
            throw new RuntimeException("Apenas reservas ativas podem fazer checkout");
        }
        
        // ✅ VERIFICAR SALDO
        java.math.BigDecimal totalHospedagem = reserva.getTotalHospedagem() != null 
            ? reserva.getTotalHospedagem() 
            : java.math.BigDecimal.ZERO;
        
        java.math.BigDecimal totalRecebido = reserva.getTotalRecebido() != null 
            ? reserva.getTotalRecebido() 
            : java.math.BigDecimal.ZERO;
        
        java.math.BigDecimal saldo = totalRecebido.subtract(totalHospedagem);
        
        System.out.println("💰 Total hospedagem: R$ " + totalHospedagem);
        System.out.println("💵 Total recebido: R$ " + totalRecebido);
        System.out.println("📊 Saldo: R$ " + saldo);
        
        // ✅ CRIAR RESPOSTA
        Map<String, Object> resultado = new java.util.HashMap<>();
        resultado.put("saldo", saldo.doubleValue());
        resultado.put("totalHospedagem", totalHospedagem.doubleValue());
        resultado.put("totalRecebido", totalRecebido.doubleValue());
        
        // ✅ SE TEM DÉBITO, NÃO PERMITE CHECKOUT
        if (saldo.compareTo(java.math.BigDecimal.ZERO) < 0) {
            System.out.println("❌ CHECKOUT BLOQUEADO - Saldo devedor: R$ " + saldo.abs());
            resultado.put("permitido", false);
            resultado.put("motivo", "SALDO_DEVEDOR");
            resultado.put("mensagem", String.format(
                "Checkout bloqueado! Cliente deve R$ %.2f",
                saldo.abs().doubleValue()
            ));
            return resultado;
        }
        
        // ✅ PERMITIDO - Fazer checkout
        reserva.setStatus(Reserva.StatusReservaEnum.FINALIZADA);
        
        String mensagem = String.format(
            "Checkout realizado para %s. Apartamento: %s liberado. Observação: %s",
            reserva.getCliente() != null ? reserva.getCliente().getNome() : "N/A",
            reserva.getApartamento() != null ? reserva.getApartamento().getNumeroApartamento() : "N/A",
            observacao != null ? observacao : "Nenhuma"
        );
        
        System.out.println("✅ " + mensagem);
        
        reservaRepository.save(reserva);
        
        resultado.put("permitido", true);
        resultado.put("motivo", saldo.compareTo(java.math.BigDecimal.ZERO) > 0 ? "SALDO_CREDOR" : "QUITADO");
        resultado.put("mensagem", String.format(
            "Checkout realizado com sucesso!%s",
            saldo.compareTo(java.math.BigDecimal.ZERO) > 0 
                ? String.format(" Devolver R$ %.2f ao cliente.", saldo.doubleValue())
                : ""
        ));
        
        System.out.println("═══════════════════════════════════════════");
        
        return resultado;
    }
    
}