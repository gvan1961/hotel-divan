package com.divan.controller;

import com.divan.dto.ContaAReceberDTO;
import com.divan.dto.ContaAReceberEdicaoDTO;

import com.divan.dto.ContaAReceberRequestDTO;
import com.divan.dto.PagamentoContaReceberDTO;
import com.divan.entity.ContaAReceber.StatusContaEnum;
import com.divan.repository.ContaAReceberRepository;
import com.divan.repository.FechamentoCaixaRepository;
import com.divan.service.ContaAReceberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.divan.entity.FechamentoCaixa;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.divan.entity.LogAuditoria;

import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contas-receber")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContaAReceberController {

	private final ContaAReceberService contaAReceberService;

	@Autowired
	private ContaAReceberRepository contaAReceberRepository;

	@Autowired
	private com.divan.repository.UsuarioRepository usuarioRepository;

	@Autowired
	private com.divan.repository.LogAuditoriaRepository logAuditoriaRepository;

	@Autowired
	private FechamentoCaixaRepository caixaRepository;
    // ========== LISTAR ==========
    
    @GetMapping
    public ResponseEntity<List<ContaAReceberDTO>> listarTodas() {
        return ResponseEntity.ok(contaAReceberService.listarTodas());
    }

    @GetMapping("/em-aberto")
    public ResponseEntity<List<ContaAReceberDTO>> listarEmAberto() {
        return ResponseEntity.ok(contaAReceberService.listarContasEmAberto());
    }

    @GetMapping("/vencidas")
    public ResponseEntity<List<ContaAReceberDTO>> listarVencidas() {
        return ResponseEntity.ok(contaAReceberService.listarContasVencidas());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ContaAReceberDTO>> listarPorStatus(@PathVariable StatusContaEnum status) {
        return ResponseEntity.ok(contaAReceberService.listarPorStatus(status));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ContaAReceberDTO>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(contaAReceberService.listarPorCliente(clienteId));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<ContaAReceberDTO>> listarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(contaAReceberService.listarPorEmpresa(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaAReceberDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contaAReceberService.buscarPorId(id));
    }

    // ========== CRIAR ==========
    
    @PostMapping
    public ResponseEntity<ContaAReceberDTO> criar(@Valid @RequestBody ContaAReceberRequestDTO dto) {
        ContaAReceberDTO conta = contaAReceberService.criar(dto);
        return ResponseEntity.ok(conta);
    }

    // ========== REGISTRAR PAGAMENTO ==========
    
    @PostMapping("/{id}/pagamento")
    public ResponseEntity<ContaAReceberDTO> registrarPagamento(
            @PathVariable Long id,
            @Valid @RequestBody PagamentoContaReceberDTO dto) {
        ContaAReceberDTO conta = contaAReceberService.registrarPagamento(id, dto);
        return ResponseEntity.ok(conta);
    }

    // ========== ATUALIZAR STATUS ==========
    
    @PostMapping("/atualizar-vencidas")
    public ResponseEntity<Void> atualizarStatusVencidas() {
        contaAReceberService.atualizarStatusVencidas();
        return ResponseEntity.ok().build();
    }

    // ========== EXCLUIR ==========
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        contaAReceberService.excluir(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/empresa/{empresaId}/relatorio-detalhado")
    public ResponseEntity<List<Map<String, Object>>> relatorioDetalhado(@PathVariable Long empresaId) {
        return ResponseEntity.ok(contaAReceberService.relatorioDetalhadoEmpresa(empresaId));
    }
    
    @PatchMapping("/{id}/desconto")
    public ResponseEntity<?> aplicarDesconto(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            BigDecimal valorDesconto = new BigDecimal(body.get("valorDesconto").toString());
            String motivo = body.get("motivo") != null ? body.get("motivo").toString() : null;
            ContaAReceberDTO conta = contaAReceberService.aplicarDesconto(id, valorDesconto, motivo);
            return ResponseEntity.ok(conta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/reabrir")
    public ResponseEntity<?> reabrirConta(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            String motivo = body != null && body.get("motivo") != null ? body.get("motivo").toString() : null;
            ContaAReceberDTO conta = contaAReceberService.reabrirConta(id, motivo);

            // ✅ LOG AUDITORIA
            try {
                String username = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();
                LogAuditoria log = new LogAuditoria();
                log.setAcao("REABRIR_CONTA_RECEBER");
                log.setDescricao("Conta a Receber reaberta — ID " + id
                    + (motivo != null && !motivo.isBlank() ? " — Motivo: " + motivo : ""));
                log.setDataHora(LocalDateTime.now());
                usuarioRepository.findByUsername(username).ifPresent(log::setUsuario);
                logAuditoriaRepository.save(log);
            } catch (Exception logEx) {
                System.err.println("⚠️ Erro ao salvar log: " + logEx.getMessage());
            }

            return ResponseEntity.ok(conta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/editar")
    public ResponseEntity<?> editarConta(
            @PathVariable Long id,
            @RequestBody ContaAReceberEdicaoDTO dto) {
        try {
            ContaAReceberDTO conta = contaAReceberService.editarConta(id, dto);

            // ✅ LOG AUDITORIA
            try {
                String username = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();
                LogAuditoria log = new LogAuditoria();
                log.setAcao("EDITAR_CONTA_RECEBER");
                log.setDescricao("Conta a Receber editada — ID " + id
                    + (dto.getMotivo() != null && !dto.getMotivo().isBlank() ? " — Motivo: " + dto.getMotivo() : ""));
                log.setDataHora(LocalDateTime.now());
                usuarioRepository.findByUsername(username).ifPresent(log::setUsuario);
                logAuditoriaRepository.save(log);
            } catch (Exception logEx) {
                System.err.println("⚠️ Erro ao salvar log: " + logEx.getMessage());
            }

            return ResponseEntity.ok(conta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
        
    @GetMapping("/pendencias-pessoais")
    public ResponseEntity<List<Map<String, Object>>> listarPendenciasPessoais() {
        List<Map<String, Object>> resultado = contaAReceberRepository.findPendenciasPessoais().stream()
            .map(c -> {
                Map<String, Object> map = new java.util.LinkedHashMap<>();
                map.put("id", c.getId());
                map.put("clienteId", c.getCliente().getId());
                map.put("clienteNome", c.getCliente().getNome());
                map.put("clienteCpf", c.getCliente().getCpf());
                map.put("valor", c.getSaldo());
                map.put("descricao", c.getDescricao());
                map.put("observacao", c.getObservacao());
                map.put("dataCriacao", c.getDataCriacao());
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(resultado);
    }
    
    @GetMapping("/pendencias-pessoais/cliente/{clienteId}")
    public ResponseEntity<List<Map<String, Object>>> listarPendenciasPessoaisPorCliente(@PathVariable Long clienteId) {
        List<Map<String, Object>> resultado = contaAReceberRepository
            .findPendenciasPessoaisPorCliente(clienteId).stream()
            .map(c -> {
                Map<String, Object> map = new java.util.LinkedHashMap<>();
                map.put("id", c.getId());
                map.put("valor", c.getSaldo());
                map.put("descricao", c.getDescricao());
                map.put("dataCriacao", c.getDataCriacao());
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(resultado);
    }
    
    @PostMapping("/corrigir-para-faturado")
    public ResponseEntity<?> corrigirParaFaturado(@RequestBody Map<String, Object> body) {
        try {
            Long reservaId = Long.parseLong(body.get("reservaId").toString());
            BigDecimal valor = new BigDecimal(body.get("valor").toString());
            Long empresaId = Long.parseLong(body.get("empresaId").toString());
            LocalDate dataVencimento = LocalDate.parse(body.get("dataVencimento").toString());
            String descricao = body.get("descricao") != null ? body.get("descricao").toString() : null;
            String motivo = body.get("motivo") != null
                ? body.get("motivo").toString()
                : "Correção: forma de pagamento incorreta";

            String login = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
            var usuarioOpt = usuarioRepository.findByUsername(login);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Usuário não encontrado"));
            }
            boolean caixaAberto = caixaRepository
                .findByUsuarioIdAndStatus(usuarioOpt.get().getId(), FechamentoCaixa.StatusCaixa.ABERTO)
                .isPresent();
            if (!caixaAberto) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Caixa não aberto. Abra o caixa antes de registrar essa correção."));
            }

            ContaAReceberDTO conta = contaAReceberService.corrigirParaFaturado(
                reservaId, valor, empresaId, dataVencimento, descricao, motivo);

            // ✅ LOG AUDITORIA
            try {
                LogAuditoria log = new LogAuditoria();
                log.setAcao("CORRIGIR_PARA_FATURADO");
                log.setDescricao("Reserva " + reservaId + " — pagamento revertido e conta a receber criada — Motivo: " + motivo);
                log.setDataHora(LocalDateTime.now());
                usuarioOpt.ifPresent(log::setUsuario);
                logAuditoriaRepository.save(log);
            } catch (Exception logEx) {
                System.err.println("⚠️ Erro ao salvar log: " + logEx.getMessage());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(conta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
}