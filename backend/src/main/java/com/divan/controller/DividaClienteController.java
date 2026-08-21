package com.divan.controller;

import com.divan.entity.Cliente;
import com.divan.entity.DividaCliente;
import com.divan.repository.ClienteRepository;
import com.divan.repository.DividaClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dividas-cliente")
@CrossOrigin(origins = "*")
public class DividaClienteController {

    @Autowired private DividaClienteRepository dividaClienteRepository;
    @Autowired private ClienteRepository clienteRepository;

    /**
     * Lista todas as dívidas pendentes — usado pelo alerta no Painel de Recepção.
     */
    @GetMapping("/pendentes")
    public ResponseEntity<List<Map<String, Object>>> listarPendentes() {
        List<Map<String, Object>> resultado = dividaClienteRepository
            .findByStatus(DividaCliente.StatusDivida.PENDENTE).stream()
            .map(d -> {
                Map<String, Object> map = new java.util.LinkedHashMap<>();
                map.put("id", d.getId());
                map.put("clienteId", d.getCliente().getId());
                map.put("clienteNome", d.getCliente().getNome());
                map.put("clienteCpf", d.getCliente().getCpf());
                map.put("valor", d.getValor());
                map.put("motivo", d.getMotivo());
                map.put("reservaOrigemId", d.getReservaOrigemId());
                map.put("dataRegistro", d.getDataRegistro());
                return map;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }

    /**
     * Verifica se um cliente específico tem dívida pendente — útil pra checar
     * na hora de criar uma nova reserva.
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<DividaCliente>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(dividaClienteRepository
            .findByClienteIdAndStatus(clienteId, DividaCliente.StatusDivida.PENDENTE));
    }

    /**
     * Registra uma dívida pendente manualmente (endpoint genérico — o fluxo
     * principal de criação é via finalizarComDivida() no ReservaController,
     * que já cria isso automaticamente ao liberar um checkout sem pagamento).
     */
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body) {
        try {
            Long clienteId = Long.parseLong(body.get("clienteId").toString());
            Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            DividaCliente divida = new DividaCliente();
            divida.setCliente(cliente);
            divida.setValor(new BigDecimal(body.get("valor").toString()));
            divida.setMotivo(body.get("motivo") != null ? body.get("motivo").toString() : null);
            if (body.get("reservaOrigemId") != null) {
                divida.setReservaOrigemId(Long.parseLong(body.get("reservaOrigemId").toString()));
            }
            divida.setDataRegistro(LocalDateTime.now());
            divida.setStatus(DividaCliente.StatusDivida.PENDENTE);

            String usuario = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
            divida.setRegistradoPor(usuario);

            DividaCliente salva = dividaClienteRepository.save(divida);
            return ResponseEntity.ok(salva);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    /**
     * Marca a dívida como quitada — chamar quando o hóspede efetivamente
     * pagar na próxima visita.
     */
    @PatchMapping("/{id}/quitar")
    public ResponseEntity<?> quitar(@PathVariable Long id) {
        return dividaClienteRepository.findById(id).map(divida -> {
            divida.setStatus(DividaCliente.StatusDivida.QUITADA);
            divida.setDataQuitacao(LocalDateTime.now());
            dividaClienteRepository.save(divida);
            return ResponseEntity.ok(divida);
        }).orElse(ResponseEntity.notFound().build());
    }
}