package com.divan.controller;

import com.divan.entity.Cliente;
import com.divan.entity.Vale;
import com.divan.entity.Vale.StatusVale;
import com.divan.repository.ClienteRepository;
import com.divan.repository.ValeRepository;
import com.divan.service.WhatsAppService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vales")
@CrossOrigin(origins = "*")
public class ValeController {

    @Autowired
    private ValeRepository valeRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private WhatsAppService whatsAppService;
    
    @GetMapping
    public ResponseEntity<List<Vale>> listarTodos() {
        return ResponseEntity.ok(valeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return valeRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Vale>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(valeRepository.findByClienteId(clienteId));
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<Vale>> listarPendentes() {
        return ResponseEntity.ok(valeRepository.findByStatus(StatusVale.PENDENTE));
    }

    @GetMapping("/vencidos")
    public ResponseEntity<List<Vale>> listarVencidos() {
        return ResponseEntity.ok(valeRepository.findVencidos());
    }

    @GetMapping("/cliente/{clienteId}/total-pendente")
    public ResponseEntity<Map<String, BigDecimal>> calcularTotalPendente(@PathVariable Long clienteId) {
        BigDecimal total = valeRepository.calcularTotalPendentePorCliente(clienteId);
        return ResponseEntity.ok(Map.of("totalPendente", total));
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body) {
        try {
            Vale vale = new Vale();
            preencherVale(vale, body);
            vale.setDataEmissao(LocalDateTime.now());
            vale.setStatus(StatusVale.PENDENTE);
            Vale valeSalvo = valeRepository.save(vale);
            notificarValeCreado(valeSalvo);
            return ResponseEntity.ok(valeSalvo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return valeRepository.findById(id).map(vale -> {
            try {
                preencherVale(vale, body);
                return ResponseEntity.ok(valeRepository.save(vale));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<?> marcarComoPago(@PathVariable Long id) {
        return valeRepository.findById(id).map(vale -> {
            vale.setStatus(StatusVale.PAGO);
            vale.setDataPagamento(LocalDateTime.now());
            Vale valeSalvo = valeRepository.save(vale);
            notificarValePago(valeSalvo);
            return ResponseEntity.ok(valeSalvo);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id,
                                       @RequestParam String motivo) {
        return valeRepository.findById(id).map(vale -> {
            vale.setStatus(StatusVale.CANCELADO);
            vale.setMotivoCancelamento(motivo);
            return ResponseEntity.ok(valeRepository.save(vale));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/assinar")
    public ResponseEntity<?> assinar(@PathVariable Long id,
                                      @RequestBody Map<String, Object> body) {
        return valeRepository.findById(id).map(vale -> {
            vale.setAssinaturaBase64(body.get("assinaturaBase64").toString());
            return ResponseEntity.ok(valeRepository.save(vale));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!valeRepository.existsById(id)) return ResponseEntity.notFound().build();
        valeRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/atualizar-vencidos")
    public ResponseEntity<Map<String, String>> atualizarVencidos() {
        List<Vale> vencidos = valeRepository.findVencidos();
        vencidos.forEach(v -> v.setStatus(StatusVale.VENCIDO));
        valeRepository.saveAll(vencidos);
        return ResponseEntity.ok(Map.of("mensagem",
            vencidos.size() + " vale(s) marcado(s) como vencido(s)"));
    }

    private void notificarValeCreado(Vale vale) {
        try {
            Cliente cliente = vale.getCliente();
            if (cliente == null || cliente.getCelular() == null || cliente.getCelular().isBlank()) return;

            String numero = whatsAppService.montarNumeroCompleto("55", cliente.getCelular());
            if (numero == null) return;

            String msg = String.format(
                "🏨 *Hotel Di Van*\n\n" +
                "Olá, *%s*!\n\n" +
                "Um vale foi registrado em seu nome:\n\n" +
                "💰 *Valor:* R$ %s\n" +
                "📋 *Tipo:* %s\n" +
                "📅 *Data:* %s\n" +
                "📆 *Vencimento:* %s\n\n" +
                "%s" +
                "Em caso de dúvidas, entre em contato com a recepção.",
                cliente.getNome(),
                vale.getValor().setScale(2),
                vale.getTipoVale() != null ? vale.getTipoVale() : "-",
                vale.getDataConcessao() != null ? vale.getDataConcessao().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-",
                vale.getDataVencimento() != null ? vale.getDataVencimento().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-",
                vale.getObservacao() != null && !vale.getObservacao().isBlank() ? "📝 *Obs:* " + vale.getObservacao() + "\n\n" : ""
            );

            whatsAppService.enviarTexto(numero, msg);
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao notificar vale: " + e.getMessage());
        }
    }
    
    private void preencherVale(Vale vale, Map<String, Object> body) {
        Long clienteId = Long.parseLong(body.get("clienteId").toString());
        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        vale.setCliente(cliente);
        vale.setValor(new BigDecimal(body.get("valor").toString()));

        if (body.get("tipoVale") != null)
            vale.setTipoVale(body.get("tipoVale").toString());

        if (body.get("descricao") != null)
            vale.setDescricao(body.get("descricao").toString());
        if (body.get("observacao") != null)
            vale.setObservacao(body.get("observacao").toString());
        if (body.get("dataVencimento") != null)
            vale.setDataVencimento(LocalDate.parse(
                body.get("dataVencimento").toString().substring(0, 10)));
        if (body.get("dataConcessao") != null)
            vale.setDataConcessao(LocalDate.parse(
                body.get("dataConcessao").toString().substring(0, 10)));        
        else
            vale.setDataConcessao(LocalDate.now());
        if (body.get("assinaturaBase64") != null)
            vale.setAssinaturaBase64(body.get("assinaturaBase64").toString());
    }
    
    private void notificarValePago(Vale vale) {
        try {
            Cliente cliente = vale.getCliente();
            if (cliente == null || cliente.getCelular() == null || cliente.getCelular().isBlank()) return;

            String numero = whatsAppService.montarNumeroCompleto("55", cliente.getCelular());
            if (numero == null) return;

            String msg = String.format(
                "🏨 *Hotel Di Van*\n\n" +
                "Olá, *%s*!\n\n" +
                "✅ O seu vale foi *quitado*:\n\n" +
                "💰 *Valor:* R$ %s\n" +
                "📋 *Tipo:* %s\n" +
                "📅 *Data Pagamento:* %s\n\n" +
                "Obrigado! 🙏",
                cliente.getNome(),
                vale.getValor().setScale(2),
                vale.getTipoVale() != null ? vale.getTipoVale() : "-",
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );

            whatsAppService.enviarTexto(numero, msg);
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao notificar pagamento de vale: " + e.getMessage());
        }
    }
}