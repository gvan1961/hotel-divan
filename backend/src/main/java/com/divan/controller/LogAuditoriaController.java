package com.divan.controller;

import com.divan.entity.LogAuditoria;
import com.divan.service.LogAuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auditoria")
public class LogAuditoriaController {

    private final LogAuditoriaService service;

    public LogAuditoriaController(LogAuditoriaService service) {
        this.service = service;
    }

    private Map<String, Object> toMap(LogAuditoria log) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", log.getId());
        map.put("acao", log.getAcao());
        map.put("descricao", log.getDescricao());
        map.put("dataHora", log.getDataHora());
        map.put("ip", log.getIp());

        if (log.getUsuario() != null) {
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("id", log.getUsuario().getId());
            usuario.put("nome", log.getUsuario().getNome());
            usuario.put("username", log.getUsuario().getUsername());
            map.put("usuario", usuario);
        }

        if (log.getReserva() != null) {
            Map<String, Object> reserva = new HashMap<>();
            reserva.put("id", log.getReserva().getId());
            if (log.getReserva().getApartamento() != null) {
                reserva.put("apartamento", log.getReserva().getApartamento().getNumeroApartamento());
            }
            if (log.getReserva().getCliente() != null) {
                reserva.put("clienteNome", log.getReserva().getCliente().getNome());
                if (log.getReserva().getCliente().getEmpresa() != null) {
                    reserva.put("empresaNome", log.getReserva().getCliente().getEmpresa().getNomeEmpresa());
                }
            }
            map.put("reserva", reserva);
        }

        return map;
    }

    @GetMapping("/reserva/{id}")
    public ResponseEntity<List<Map<String, Object>>> porReserva(@PathVariable Long id) {
        List<Map<String, Object>> resultado = service.buscarPorReserva(id)
            .stream().map(this::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<Map<String, Object>>> porUsuario(@PathVariable Long id) {
        List<Map<String, Object>> resultado = service.buscarPorUsuario(id)
            .stream().map(this::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> todos() {
        List<Map<String, Object>> resultado = service.buscarTodos()
            .stream().map(this::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }
    
    @GetMapping("/filtrar")
    public ResponseEntity<List<Map<String, Object>>> filtrar(
            @RequestParam(required = false) String funcionario,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate dataInicio,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate dataFim,
            @RequestParam(required = false) String apartamento,
            @RequestParam(required = false) Long reservaId,
            @RequestParam(required = false) String hospede,
            @RequestParam(required = false) String empresa) {

        java.time.LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        java.time.LocalDateTime fim = dataFim != null ? dataFim.atTime(23, 59, 59) : null;

        List<Map<String, Object>> resultado = service.buscarComFiltros(funcionario, acao, inicio, fim, apartamento, reservaId, hospede, empresa)
            .stream().map(this::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }
}
