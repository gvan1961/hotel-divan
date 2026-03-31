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
}
