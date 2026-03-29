package com.divan.controller;

import com.divan.dto.AlertaDTO;
import com.divan.entity.Reserva;
import com.divan.entity.Reserva.StatusReservaEnum;
import com.divan.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/alertas")
public class AlertasController {

    @Autowired
    private ReservaRepository reservaRepository;

    @GetMapping("/todos-alertas")
    public ResponseEntity<Map<String, Object>> getTodosAlertas() {
        LocalDateTime agora = LocalDateTime.now();

        // Checkouts vencidos
        List<Reserva> reservasAtrasadas = reservaRepository
            .findByStatusAndDataCheckoutBefore(StatusReservaEnum.ATIVA, agora);

        // No-shows
        List<Reserva> noShowReservas = reservaRepository
            .findByStatusAndDataCheckinBefore(StatusReservaEnum.PRE_RESERVA, agora);

        // Converte para AlertaDTO
        List<AlertaDTO> checkoutsVencidos = new ArrayList<>();
        for (Reserva r : reservasAtrasadas) {
            AlertaDTO dto = new AlertaDTO();
            dto.setTipoAlerta("CHECKOUT_VENCIDO");
            dto.setNivelGravidade("ALTO");
            dto.setTitulo("Checkout Vencido");
            dto.setDescricao("Hóspede ainda no apartamento após data de checkout.");
            dto.setRecomendacao("Entre em contato com o hóspede para regularizar.");
            dto.setReservaId(r.getId());
            dto.setClienteNome(r.getCliente() != null ? r.getCliente().getNome() : "N/A");
            dto.setStatusReserva(r.getStatus().name());
            dto.setDataCheckin(r.getDataCheckin());
            dto.setDataCheckout(r.getDataCheckout());
            dto.setHorasAtraso(ChronoUnit.HOURS.between(r.getDataCheckout(), agora));
            if (r.getApartamento() != null) {
                dto.setApartamentoId(r.getApartamento().getId());
                dto.setNumeroApartamento(r.getApartamento().getNumeroApartamento());
                dto.setTipoApartamento(
                    Optional.ofNullable(r.getApartamento().getTipoApartamento())
                        .map(t -> t.getDescricao())
                        .orElse("")
                );
            }
            checkoutsVencidos.add(dto);
        }

        List<AlertaDTO> noShows = new ArrayList<>();
        for (Reserva r : noShowReservas) {
            AlertaDTO dto = new AlertaDTO();
            dto.setTipoAlerta("NO_SHOW");
            dto.setNivelGravidade("MEDIO");
            dto.setTitulo("No-Show");
            dto.setDescricao("Hóspede não realizou check-in na data prevista.");
            dto.setRecomendacao("Confirme com o cliente ou cancele a reserva.");
            dto.setReservaId(r.getId());
            dto.setClienteNome(r.getCliente() != null ? r.getCliente().getNome() : "N/A");
            dto.setStatusReserva(r.getStatus().name());
            dto.setDataCheckin(r.getDataCheckin());
            dto.setDataCheckout(r.getDataCheckout());
            if (r.getApartamento() != null) {
                dto.setApartamentoId(r.getApartamento().getId());
                dto.setNumeroApartamento(r.getApartamento().getNumeroApartamento());
                if (r.getApartamento().getTipoApartamento() != null) {
                    dto.setTipoApartamento(r.getApartamento().getTipoApartamento().getDescricao());
                }
            }
            
            noShows.add(dto);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("conflitos", new ArrayList<>());
        response.put("checkoutsVencidos", checkoutsVencidos);
        response.put("noShows", noShows);
        response.put("preReservasEmRisco", new ArrayList<>());

        return ResponseEntity.ok(response);
    }
}