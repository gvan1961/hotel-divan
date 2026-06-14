package com.divan.controller;

import com.divan.entity.Cliente;
import com.divan.entity.Reserva;
import com.divan.repository.ClienteRepository;
import com.divan.repository.ReservaRepository;
import com.divan.service.WhatsAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/publico/consulta-cliente")
@CrossOrigin(origins = "*")
public class ClienteConsultaController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private WhatsAppService whatsAppService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @PostMapping
    public ResponseEntity<?> consultarCliente(@RequestBody Map<String, Object> body) {
        try {
            String cpf = body.get("cpf") != null ? body.get("cpf").toString().replaceAll("[^0-9]", "") : null;
            String numeroWhatsApp = body.get("numeroWhatsApp") != null ? body.get("numeroWhatsApp").toString() : null;

            if (cpf == null || cpf.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "CPF não informado"));
            }
            if (numeroWhatsApp == null || numeroWhatsApp.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Número WhatsApp não informado"));
            }

            Optional<Cliente> clienteOpt = clienteRepository.findByCpf(cpf);

            String mensagem;

            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();

                // Busca reservas do cliente
                List<Reserva> reservas = reservaRepository.findByCliente(cliente);
                long totalHospedagens = reservas.stream()
                    .filter(r -> r.getStatus() == Reserva.StatusReservaEnum.FINALIZADA)
                    .count();

                Reserva ultimaReserva = reservas.stream()
                    .filter(r -> r.getStatus() == Reserva.StatusReservaEnum.FINALIZADA)
                    .max((a, b) -> a.getDataCheckout().compareTo(b.getDataCheckout()))
                    .orElse(null);

                StringBuilder sb = new StringBuilder();
                sb.append("✅ *Cliente encontrado!*\n\n");
                sb.append("👤 *Nome:* ").append(cliente.getNome()).append("\n");
                sb.append("🪪 *CPF:* ").append(formatarCpf(cpf)).append("\n");
                if (cliente.getCelular() != null) sb.append("📱 *Celular:* ").append(cliente.getCelular()).append("\n");
                if (cliente.getEmpresa() != null) sb.append("🏢 *Empresa:* ").append(cliente.getEmpresa().getNomeEmpresa()).append("\n");
                sb.append("🏨 *Total hospedagens:* ").append(totalHospedagens).append("\n");
                if (ultimaReserva != null) {
                    sb.append("📅 *Última hospedagem:* ")
                      .append(ultimaReserva.getDataCheckin().format(FMT))
                      .append(" — Apt ").append(ultimaReserva.getApartamento().getNumeroApartamento()).append("\n");
                }
                mensagem = sb.toString();
            } else {
                mensagem = "❌ *Cliente não cadastrado no sistema.*\n\n" +
                           "🪪 *CPF:* " + formatarCpf(cpf) + "\n\n" +
                           "Cliente não possui histórico no Hotel Di Van.";
            }

            // Envia via WhatsApp
            String numero = whatsAppService.montarNumeroCompleto("55", numeroWhatsApp);
            whatsAppService.enviarTexto(numero, mensagem);

            return ResponseEntity.ok(Map.of("mensagem", "Consulta realizada e enviada via WhatsApp"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    private String formatarCpf(String cpf) {
        if (cpf.length() == 11) {
            return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." +
                   cpf.substring(6, 9) + "-" + cpf.substring(9);
        }
        return cpf;
    }
}
