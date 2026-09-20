package com.divan.controller;

import com.divan.entity.HospedagemHospede;
import com.divan.entity.Reserva;
import com.divan.repository.HospedagemHospedeRepository;
import com.divan.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/relatorios")
@CrossOrigin(origins = "*")
public class RelatorioApartamentosController {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private HospedagemHospedeRepository hospedagemHospedeRepository;

    @GetMapping("/apartamentos-hospedes")
    public ResponseEntity<?> apartamentosComHospedes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataCheckin,
            @RequestParam(required = false) Long empresaId) {

        LocalDateTime dataCheckinDateTime = dataCheckin != null ? dataCheckin.atStartOfDay() : null;

        List<Reserva> reservas = reservaRepository.buscarRelatorioApartamentosHospedes(dataCheckinDateTime, empresaId);

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Reserva r : reservas) {
            Map<String, Object> item = new HashMap<>();
            item.put("reservaId", r.getId());
            item.put("status", r.getStatus());
            item.put("numeroApartamento", r.getApartamento() != null ? r.getApartamento().getNumeroApartamento() : "-");
            item.put("tipoApartamento", r.getApartamento() != null && r.getApartamento().getTipoApartamento() != null
                ? r.getApartamento().getTipoApartamento().getDescricao() : "-");
            item.put("dataCheckin", r.getDataCheckin());
            item.put("dataCheckout", r.getDataCheckout());
            item.put("quantidadeHospede", r.getQuantidadeHospede());

            if (r.getCliente() != null) {
                item.put("clienteResponsavel", r.getCliente().getNome());
                if (r.getCliente().getEmpresa() != null) {
                    item.put("empresaNome", r.getCliente().getEmpresa().getNomeEmpresa());
                }
            }

            List<HospedagemHospede> hospedes = hospedagemHospedeRepository.findByReservaId(r.getId());
            List<Map<String, Object>> hospedesLista = hospedes.stream()
                .filter(h -> h.getStatus() == HospedagemHospede.StatusEnum.HOSPEDADO)
                .map(h -> {
                    Map<String, Object> hMap = new HashMap<>();
                    hMap.put("nome", h.getCliente() != null ? h.getCliente().getNome() : "-");
                    hMap.put("titular", h.isTitular());
                    return hMap;
                })
                .collect(Collectors.toList());

            item.put("hospedes", hospedesLista);

            resultado.add(item);
        }

        return ResponseEntity.ok(resultado);
    }
}
