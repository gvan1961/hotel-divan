package com.divan.service;

import com.divan.dto.AlertaDTO;
import com.divan.entity.Reserva;
import com.divan.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlertaService {

    @Autowired
    private ReservaRepository reservaRepository;

    /**
     * 📊 BUSCAR TODOS OS ALERTAS AGRUPADOS
     */
    public Map<String, List<AlertaDTO>> buscarTodosAlertas() {
        Map<String, List<AlertaDTO>> alertas = new HashMap<>();
        
        alertas.put("conflitos", buscarConflitos());
        alertas.put("checkoutsVencidos", buscarCheckoutsVencidos());
        alertas.put("noShows", buscarNoShows());
        alertas.put("preReservasEmRisco", buscarPreReservasEmRisco());
        
        return alertas;
    }

    /**
     * 🚨 BUSCAR CONFLITOS DE PRÉ-RESERVAS
     */
    public List<AlertaDTO> buscarConflitos() {
        // TODO: Implementar lógica de conflitos
        // Por enquanto retorna lista vazia
        return new ArrayList<>();
    }

    /**
     * ⏰ BUSCAR CHECKOUTS VENCIDOS
     */
    public List<AlertaDTO> buscarCheckoutsVencidos() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite = agora.minusHours(2); // 2 horas de tolerância
        
        List<Reserva> reservasAtivas = reservaRepository.findByStatus(
            Reserva.StatusReservaEnum.ATIVA
        );
        
        return reservasAtivas.stream()
            .filter(r -> r.getDataCheckout() != null && 
                        r.getDataCheckout().isBefore(limite))
            .map(this::criarAlertaCheckoutVencido)
            .collect(Collectors.toList());
    }

    /**
     * 🔴 BUSCAR NO-SHOWS (não compareceram)
     */
    public List<AlertaDTO> buscarNoShows() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite = agora.minusHours(24); // 24h após checkin previsto
        
        List<Reserva> preReservas = reservaRepository.findByStatus(
            Reserva.StatusReservaEnum.PRE_RESERVA
        );
        
        return preReservas.stream()
            .filter(r -> r.getDataCheckin() != null && 
                        r.getDataCheckin().isBefore(limite))
            .map(this::criarAlertaNoShow)
            .collect(Collectors.toList());
    }

    /**
     * ⚠️ BUSCAR PRÉ-RESERVAS EM RISCO
     */
    public List<AlertaDTO> buscarPreReservasEmRisco() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime proximasHoras = agora.plusHours(6); // Próximas 6 horas
        
        List<Reserva> preReservas = reservaRepository.findByStatus(
            Reserva.StatusReservaEnum.PRE_RESERVA
        );
        
        return preReservas.stream()
            .filter(r -> r.getDataCheckin() != null && 
                        r.getDataCheckin().isBefore(proximasHoras) &&
                        r.getDataCheckin().isAfter(agora))
            .map(this::criarAlertaPreReservaEmRisco)
            .collect(Collectors.toList());
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================

    private AlertaDTO criarAlertaCheckoutVencido(Reserva reserva) {
        AlertaDTO alerta = new AlertaDTO();
        alerta.setTipoAlerta("CHECKOUT_VENCIDO");
        alerta.setNivelGravidade("ALTO");
        alerta.setTitulo("Checkout Vencido");
        
        LocalDateTime agora = LocalDateTime.now();
        long horas = ChronoUnit.HOURS.between(reserva.getDataCheckout(), agora);
        long minutos = ChronoUnit.MINUTES.between(reserva.getDataCheckout(), agora) % 60;
        
        alerta.setDescricao(String.format(
            "Hóspede %s deveria ter feito checkout há %d horas e %d minutos",
            reserva.getCliente().getNome(),
            horas,
            minutos
        ));
        
        alerta.setRecomendacao("Entre em contato com o hóspede e verifique a situação");
        
        alerta.setReservaId(reserva.getId());
        alerta.setClienteNome(reserva.getCliente().getNome());
        alerta.setApartamentoId(reserva.getApartamento().getId());
        alerta.setNumeroApartamento(reserva.getApartamento().getNumeroApartamento());
        alerta.setDataCheckin(reserva.getDataCheckin());
        alerta.setDataCheckout(reserva.getDataCheckout());
        alerta.setDataHoraAlerta(agora);
        alerta.setHorasAtraso(horas);
        alerta.setMinutosAtraso(minutos);
        
        return alerta;
    }

    private AlertaDTO criarAlertaNoShow(Reserva reserva) {
        AlertaDTO alerta = new AlertaDTO();
        alerta.setTipoAlerta("NO_SHOW");
        alerta.setNivelGravidade("CRITICO");
        alerta.setTitulo("Cliente Não Compareceu");
        
        LocalDateTime agora = LocalDateTime.now();
        long horas = ChronoUnit.HOURS.between(reserva.getDataCheckin(), agora);
        
        alerta.setDescricao(String.format(
            "Cliente %s não compareceu. Checkin previsto há %d horas",
            reserva.getCliente().getNome(),
            horas
        ));
        
        alerta.setRecomendacao("Considere cancelar a reserva e liberar o apartamento");
        
        alerta.setReservaId(reserva.getId());
        alerta.setClienteNome(reserva.getCliente().getNome());
        alerta.setApartamentoId(reserva.getApartamento().getId());
        alerta.setNumeroApartamento(reserva.getApartamento().getNumeroApartamento());
        alerta.setDataCheckin(reserva.getDataCheckin());
        alerta.setDataCheckout(reserva.getDataCheckout());
        alerta.setDataHoraAlerta(agora);
        alerta.setHorasAtraso(horas);
        
        return alerta;
    }

    private AlertaDTO criarAlertaPreReservaEmRisco(Reserva reserva) {
        AlertaDTO alerta = new AlertaDTO();
        alerta.setTipoAlerta("PRE_RESERVA_EM_RISCO");
        alerta.setNivelGravidade("MEDIO");
        alerta.setTitulo("Pré-Reserva Próxima do Checkin");
        
        LocalDateTime agora = LocalDateTime.now();
        long horas = ChronoUnit.HOURS.between(agora, reserva.getDataCheckin());
        
        alerta.setDescricao(String.format(
            "Pré-reserva de %s tem checkin em %d horas e ainda não foi confirmada",
            reserva.getCliente().getNome(),
            horas
        ));
        
        alerta.setRecomendacao("Confirme o pagamento e ative a reserva");
        
        alerta.setReservaId(reserva.getId());
        alerta.setClienteNome(reserva.getCliente().getNome());
        alerta.setApartamentoId(reserva.getApartamento().getId());
        alerta.setNumeroApartamento(reserva.getApartamento().getNumeroApartamento());
        alerta.setDataCheckin(reserva.getDataCheckin());
        alerta.setDataCheckout(reserva.getDataCheckout());
        alerta.setDataHoraAlerta(agora);
        
        return alerta;
    }
}
