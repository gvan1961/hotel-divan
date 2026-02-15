package com.divan.controller;

import com.divan.entity.HistoricoHospede;
import com.divan.repository.HistoricoHospedeRepository;
import com.divan.repository.DiariaRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import com.divan.util.DataUtil;

import com.divan.dto.ValidarHospedeDTO;
import java.util.Map;

import com.divan.dto.CheckoutParcialRequestDTO;
import com.divan.entity.ExtratoReserva;
import com.divan.repository.ExtratoReservaRepository;
import com.divan.dto.AdicionarHospedeRequestDTO;
import com.divan.dto.ApartamentoResponseDTO;
import com.divan.dto.ClienteResponseDTO;
import com.divan.dto.ItemVendaRequestDTO;
import com.divan.dto.LancamentoRapidoRequestDTO;
import com.divan.dto.ReservaDetalhesDTO;
import com.divan.dto.ReservaRequestDTO;
import com.divan.dto.ReservaResponseDTO;
import com.divan.dto.TransferenciaApartamentoDTO;
import com.divan.dto.TransferenciaHospedeDTO;
import com.divan.dto.ValidarHospedeDTO;
import com.divan.entity.Apartamento;
import com.divan.entity.Cliente;
import com.divan.entity.Diaria;
import com.divan.entity.ExtratoReserva.StatusLancamentoEnum;
import com.divan.entity.HistoricoReserva;
import com.divan.entity.HospedagemHospede;
import com.divan.entity.ItemVenda;
import com.divan.entity.NotaVenda;
import com.divan.entity.Reserva;
import com.divan.entity.Reserva.StatusReservaEnum;
import com.divan.entity.TipoApartamento;
import com.divan.repository.ApartamentoRepository;
import com.divan.repository.ClienteRepository;
import com.divan.repository.HistoricoReservaRepository;
import com.divan.repository.HospedagemHospedeRepository;
import com.divan.repository.ReservaRepository;
import com.divan.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "*")
public class ReservaController {
    
    @Autowired
    private ReservaService reservaService;         
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private ApartamentoRepository apartamentoRepository;
    
    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private HistoricoReservaRepository historicoReservaRepository;
    
    @Autowired
    private HistoricoHospedeRepository historicoHospedeRepository;
    
    @Autowired
    private HospedagemHospedeRepository hospedagemHospedeRepository;
    
    @Autowired
    private DiariaRepository diariaRepository;
    
    @Autowired
    private ExtratoReservaRepository extratoReservaRepository;      
       
    @PostMapping
    public ResponseEntity<?> criarReserva(@Valid @RequestBody ReservaRequestDTO dto) {
        try {
            // Log de debug
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("========== CRIAR RESERVA ==========");
            System.out.println("👤 Usuário: " + (auth != null ? auth.getName() : "NÃO AUTENTICADO"));
            System.out.println("🔑 Authorities: " + (auth != null ? auth.getAuthorities() : "NENHUMA"));
            System.out.println("📝 DTO recebido: " + dto);
            System.out.println("👥 Quantidade de hóspedes informados: " + 
                (dto.getHospedes() != null ? dto.getHospedes().size() : 0));
            
            // Validar datas
            if (dto.getDataCheckout().isBefore(dto.getDataCheckin()) || 
                dto.getDataCheckout().isEqual(dto.getDataCheckin())) {
                return ResponseEntity.badRequest()
                    .body("Data de checkout deve ser posterior ao checkin");
            }
            
            // Buscar cliente
            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
            
            // Buscar apartamento
            Apartamento apartamento = apartamentoRepository.findById(dto.getApartamentoId())
                .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
            
            // Validar capacidade do apartamento
            if (dto.getQuantidadeHospede() > apartamento.getCapacidade()) {
                return ResponseEntity.badRequest()
                    .body("Quantidade de hóspedes (" + dto.getQuantidadeHospede() + 
                          ") excede a capacidade do apartamento (" + apartamento.getCapacidade() + ")");
            }
            
            // Validar lista de hóspedes (se fornecida)
            if (dto.getHospedes() != null && !dto.getHospedes().isEmpty()) {
                if (dto.getHospedes().size() > dto.getQuantidadeHospede()) {
                    return ResponseEntity.badRequest()
                        .body("Quantidade de hóspedes na lista (" + dto.getHospedes().size() + 
                              ") não pode exceder a quantidade total (" + dto.getQuantidadeHospede() + ")");
                }
                
                if (dto.getHospedes().size() > apartamento.getCapacidade()) {
                    return ResponseEntity.badRequest()
                        .body("Quantidade de hóspedes na lista (" + dto.getHospedes().size() + 
                              ") excede a capacidade do apartamento (" + apartamento.getCapacidade() + ")");
                }
            }
            
            // ═══════════════════════════════════════════
            // CRIAR A RESERVA
            // ═══════════════════════════════════════════
            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setApartamento(apartamento);
            reserva.setQuantidadeHospede(dto.getQuantidadeHospede());
            reserva.setDataCheckin(dto.getDataCheckin());
            reserva.setDataCheckout(dto.getDataCheckout());
            
            // Chamar o service
            Reserva reservaCriada = reservaService.criarReserva(reserva);
            
            // Processar hóspedes individuais (se fornecidos)
            if (dto.getHospedes() != null && !dto.getHospedes().isEmpty()) {
                reservaService.processarHospedes(reservaCriada, dto.getHospedes());
            }
            
            // Converter para DTO de resposta
            ReservaResponseDTO response = converterParaDTO(reservaCriada);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            System.out.println("❌ Erro ao criar reserva: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // ============================================
    // LISTAGENS E CONSULTAS
    // ============================================
    
    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> listarTodas() {
        List<ReservaResponseDTO> reservas = reservaService.listarTodasDTO();
        return ResponseEntity.ok(reservas);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ReservaDetalhesDTO> buscarPorId(@PathVariable Long id) {
        System.out.println("📋 Requisição para buscar reserva: " + id);
        
        ReservaDetalhesDTO reserva = reservaService.buscarDetalhes(id);
        
        System.out.println("📤 Retornando reserva com:");
        System.out.println("  Total Diária: R$ " + reserva.getTotalDiaria());
        System.out.println("  Total Produto: R$ " + reserva.getTotalProduto());
        System.out.println("  Total Hospedagem: R$ " + reserva.getTotalHospedagem());
        
        return ResponseEntity.ok(reserva);
    }
    
    @GetMapping("/ativas")
    public ResponseEntity<List<Reserva>> buscarAtivas() {
        List<Reserva> reservas = reservaService.buscarAtivas();
        return ResponseEntity.ok(reservas);
    }
    
    @GetMapping("/checkins-do-dia")
    public ResponseEntity<List<Reserva>> buscarCheckinsDoDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime data) {
        List<Reserva> reservas = reservaService.buscarCheckinsDoDia(data);
        return ResponseEntity.ok(reservas);
    }
    
    @GetMapping("/checkouts-do-dia")
    public ResponseEntity<List<Reserva>> buscarCheckoutsDoDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime data) {
        List<Reserva> reservas = reservaService.buscarCheckoutsDoDia(data);
        return ResponseEntity.ok(reservas);
    }
    
    @GetMapping("/periodo")
    public ResponseEntity<List<Reserva>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<Reserva> reservas = reservaService.buscarPorPeriodo(inicio, fim);
        return ResponseEntity.ok(reservas);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReservaResponseDTO>> listarPorStatus(@PathVariable Reserva.StatusReservaEnum status) {
        List<ReservaResponseDTO> reservas = reservaService.listarPorStatusDTO(status);
        return ResponseEntity.ok(reservas);
    }
    
    // ============================================
    // ALTERAÇÕES EM RESERVA
    // ============================================
    
    @PatchMapping("/{id}/alterar-hospedes")
    public ResponseEntity<?> alterarQuantidadeHospedes(
            @PathVariable Long id, 
            @RequestParam Integer quantidade,
            @RequestParam(required = false) String motivo) {
        try {
            Reserva reserva = reservaService.alterarQuantidadeHospedes(id, quantidade, motivo);
            return ResponseEntity.ok(reserva);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/alterar-checkout")
    public ResponseEntity<?> alterarDataCheckout(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime novaDataCheckout,
            @RequestParam(required = false) String motivo) {
        try {
            Reserva reserva = reservaService.alterarDataCheckout(id, novaDataCheckout, motivo);
            return ResponseEntity.ok(reserva);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/transferir-apartamento")
    public ResponseEntity<?> transferirApartamento(@RequestBody TransferenciaApartamentoDTO dto) {
        try {
            Reserva reserva = reservaService.transferirApartamento(dto);
            return ResponseEntity.ok(reserva);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    // ============================================
    // FINALIZAÇÃO E CANCELAMENTO
    // ============================================
    
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizarReserva(@PathVariable Long id) {
        try {
            System.out.println("═══════════════════════════════════════");
            System.out.println("📝 ENDPOINT: FINALIZAR RESERVA");
            System.out.println("   Reserva ID: " + id);
            System.out.println("═══════════════════════════════════════");
            
            Reserva reserva = reservaService.finalizarReserva(id);
            
            System.out.println("✅ Reserva finalizada com sucesso!");
            
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Reserva finalizada com sucesso!",
                "reserva", reserva
            ));
            
        } catch (RuntimeException e) {
            System.err.println("❌ ERRO ao finalizar reserva:");
            System.err.println("   Mensagem: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("❌ ERRO INESPERADO:");
            System.err.println("   Mensagem: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "sucesso", false,
                "erro", "Erro inesperado ao finalizar reserva: " + e.getMessage()
            ));
        }
    }
    
    @PatchMapping("/{id}/finalizar-paga")
    public ResponseEntity<?> finalizarReservaPaga(@PathVariable Long id) {
        try {
            System.out.println("═══════════════════════════════════════");
            System.out.println("💳 ENDPOINT: FINALIZAR RESERVA PAGA");
            System.out.println("   Reserva ID: " + id);
            System.out.println("═══════════════════════════════════════");
            
            Optional<Reserva> reservaOpt = reservaService.buscarPorId(id);
            if (reservaOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "sucesso", false,
                    "erro", "Reserva não encontrada"
                ));
            }
            
            Reserva reserva = reservaOpt.get();
            
            if (reserva.getTotalApagar().compareTo(BigDecimal.ZERO) != 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "sucesso", false,
                    "erro", "Ainda há saldo devedor de R$ " + reserva.getTotalApagar() + 
                           ". Use 'Finalizar Faturada' para enviar para Contas a Receber."
                ));
            }
            
            reservaService.finalizarReservaPaga(id);
            
            System.out.println("✅ Reserva finalizada como PAGA com sucesso!");
            System.out.println("═══════════════════════════════════════");
            
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Reserva finalizada! Recibo disponível para impressão."
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        }
    }
    
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id, @RequestParam String motivo) {
        try {
            Reserva reserva = reservaService.cancelarReserva(id, motivo);
            return ResponseEntity.ok(reserva);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // ============================================
    // CONSUMO E NOTAS DE VENDA
    // ============================================
    
    @PostMapping("/{id}/consumo")
    public ResponseEntity<?> adicionarProdutoAoConsumo(
            @PathVariable Long id,
            @RequestBody ItemVendaRequestDTO request) {
        try {
            Reserva reserva = reservaService.adicionarProdutoAoConsumo(
                id, 
                request.getProdutoId(), 
                request.getQuantidade(), 
                request.getObservacao()
            );
            return ResponseEntity.ok(reserva);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/{id}/consumo")
    public ResponseEntity<?> listarConsumo(@PathVariable Long id) {
        try {
            List<ItemVenda> itens = reservaService.listarConsumoPorReserva(id);
            return ResponseEntity.ok(itens);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/notas-venda")
    public ResponseEntity<?> listarNotasVenda(@PathVariable Long id) {
        try {
            List<NotaVenda> notas = reservaService.listarNotasVendaPorReserva(id);
            return ResponseEntity.ok(notas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @PostMapping("/comandas-rapidas")
    public ResponseEntity<Map<String, Object>> processarComandasRapidas(@RequestBody LancamentoRapidoRequestDTO request) {
        System.out.println("🍽️ Recebendo comandas rápidas");
        Map<String, Object> resultado = reservaService.processarComandasRapidas(request);
        return ResponseEntity.ok(resultado);
    }
    
    // ============================================
    // DESCONTOS
    // ============================================
    
    @PatchMapping("/{id}/aplicar-desconto")
    public ResponseEntity<?> aplicarDesconto(
            @PathVariable Long id,
            @RequestParam BigDecimal valorDesconto,
            @RequestParam(required = false) String motivo) {
        
        try {
            Reserva reserva = reservaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
            
            if (!reserva.getStatus().equals(StatusReservaEnum.ATIVA)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Só é possível aplicar desconto em reservas ativas"));
            }
            
            if (valorDesconto.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Valor do desconto deve ser maior que zero"));
            }
            
            if (valorDesconto.compareTo(reserva.getTotalHospedagem()) > 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Desconto não pode ser maior que o total da hospedagem"));
            }
            
            reserva.setDesconto(valorDesconto);
            
            BigDecimal novoTotal = reserva.getTotalHospedagem().subtract(valorDesconto);
            BigDecimal novoSaldo = novoTotal.subtract(reserva.getTotalRecebido());
            reserva.setTotalApagar(novoSaldo);
            
            HistoricoReserva historico = new HistoricoReserva();
            historico.setReserva(reserva);
            historico.setDataHora(LocalDateTime.now());
            historico.setTipo("DESCONTO_APLICADO");
            historico.setDescricao("Desconto aplicado no valor de R$ " + valorDesconto);
            historico.setDetalhes(
                "Valor do desconto: R$ " + valorDesconto + "\n" +
                "Total anterior: R$ " + reserva.getTotalHospedagem().add(valorDesconto) + "\n" +
                "Total com desconto: R$ " + novoTotal + "\n" +
                "Novo saldo: R$ " + novoSaldo +
                (motivo != null && !motivo.trim().isEmpty() ? "\nMotivo: " + motivo : "")
            );
            historicoReservaRepository.save(historico);
            
            reservaRepository.save(reserva);
            
            return ResponseEntity.ok(Map.of(
                "mensagem", "Desconto aplicado com sucesso",
                "desconto", valorDesconto,
                "novoTotal", novoTotal,
                "novoSaldo", novoSaldo
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}/remover-desconto")
    public ResponseEntity<?> removerDesconto(@PathVariable Long id) {
        try {
            Reserva reserva = reservaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
            
            if (!reserva.getStatus().equals(StatusReservaEnum.ATIVA)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Só é possível remover desconto em reservas ativas"));
            }
            
            if (reserva.getDesconto() == null || reserva.getDesconto().compareTo(BigDecimal.ZERO) == 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Não há desconto aplicado nesta reserva"));
            }
            
            BigDecimal descontoRemovido = reserva.getDesconto();
            BigDecimal totalAnterior = reserva.getTotalHospedagem().subtract(descontoRemovido);
            
            reserva.setDesconto(BigDecimal.ZERO);
            
            BigDecimal novoSaldo = reserva.getTotalHospedagem().subtract(reserva.getTotalRecebido());
            reserva.setTotalApagar(novoSaldo);
            
            HistoricoReserva historico = new HistoricoReserva();
            historico.setReserva(reserva);
            historico.setDataHora(LocalDateTime.now());
            historico.setTipo("DESCONTO_REMOVIDO");
            historico.setDescricao("Desconto removido no valor de R$ " + descontoRemovido);
            historico.setDetalhes(
                "Valor do desconto removido: R$ " + descontoRemovido + "\n" +
                "Total anterior (com desconto): R$ " + totalAnterior + "\n" +
                "Total atual (sem desconto): R$ " + reserva.getTotalHospedagem() + "\n" +
                "Novo saldo: R$ " + novoSaldo
            );
            historicoReservaRepository.save(historico);
            
            reservaRepository.save(reserva);
            
            return ResponseEntity.ok(Map.of(
                "mensagem", "Desconto removido com sucesso",
                "descontoRemovido", descontoRemovido,
                "novoSaldo", novoSaldo
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }
    
    // ============================================
    // PRÉ-RESERVAS
    // ============================================
    
    @PatchMapping("/{id}/editar-pre-reserva")
    public ResponseEntity<?> editarPreReserva(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            
            String dataCheckinStr = (String) updates.get("dataCheckin");
            String dataCheckoutStr = (String) updates.get("dataCheckout");
            
            dataCheckinStr = dataCheckinStr.replaceAll("\\.\\d{3}Z?$", "");
            dataCheckoutStr = dataCheckoutStr.replaceAll("\\.\\d{3}Z?$", "");
            
            LocalDateTime dataCheckin = LocalDateTime.parse(dataCheckinStr, formatter);
            LocalDateTime dataCheckout = LocalDateTime.parse(dataCheckoutStr, formatter);
            
            Reserva reserva = reservaService.editarPreReserva(
                id,
                ((Number) updates.get("apartamentoId")).longValue(),
                (Integer) updates.get("quantidadeHospede"),
                dataCheckin,
                dataCheckout
            );
            
            return ResponseEntity.ok(reserva);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}/pre-reserva")
    public ResponseEntity<?> excluirPreReserva(@PathVariable Long id) {
        try {
            reservaService.excluirPreReserva(id);
            return ResponseEntity.ok(Map.of("message", "Pré-reserva excluída com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    // ============================================
    // GESTÃO DE HÓSPEDES EM RESERVA EXISTENTE
    // ============================================
    
 // ============================================
 // GESTÃO DE HÓSPEDES EM RESERVA EXISTENTE
 // ============================================

    @PostMapping("/{reservaId}/hospedes")
    public ResponseEntity<?> adicionarHospede(
            @PathVariable Long reservaId,
            @RequestBody AdicionarHospedeRequestDTO request) {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("👤 ADICIONANDO HÓSPEDE À RESERVA #" + reservaId);
        System.out.println("═══════════════════════════════════════════");
        
        try {
            // ========================================
            // 1️⃣ BUSCAR RESERVA
            // ========================================
            Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
            
            System.out.println("📋 Reserva #" + reserva.getId());
            System.out.println("   Status: " + reserva.getStatus());
            System.out.println("   Apartamento: " + reserva.getApartamento().getNumeroApartamento());
            
            // ========================================
            // 2️⃣ VALIDAR STATUS DA RESERVA
            // ========================================
            if (!reserva.getStatus().equals(StatusReservaEnum.ATIVA)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Só é possível adicionar hóspedes em reservas ATIVAS"));
            }
            
            // ========================================
            // 3️⃣ VERIFICAR CAPACIDADE DO APARTAMENTO
            // ========================================
            List<HospedagemHospede> hospedesAtuais = hospedagemHospedeRepository
                .findByReservaId(reservaId);
            
        //    int quantidadeAtual = hospedesAtuais.size();
            int quantidadeAtual = (int) hospedesAtuais.stream()
            	    .filter(h -> h.getStatus() == HospedagemHospede.StatusHospedeIndividual.HOSPEDADO)
            	    .count();
            int capacidadeApartamento = reserva.getApartamento().getCapacidade();
            
            System.out.println("📊 Hóspedes atuais: " + quantidadeAtual);
            System.out.println("📊 Capacidade: " + capacidadeApartamento);
            
            if (quantidadeAtual >= capacidadeApartamento) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Capacidade máxima do apartamento já atingida: " + 
                        capacidadeApartamento + " hóspede(s)"));
            }
            
            // ========================================
            // 4️⃣ GUARDAR VALORES ANTES DE ADICIONAR
            // ========================================
            BigDecimal totalDiariaAnterior = reserva.getTotalDiaria() != null ? 
                reserva.getTotalDiaria() : BigDecimal.ZERO;
            
            System.out.println("💰 Total de diárias ANTES: R$ " + totalDiariaAnterior);
            
            // ========================================
            // 5️⃣ PROCESSAR CLIENTE (NOVO OU EXISTENTE)
            // ========================================
            Cliente cliente = null;
            
            if (Boolean.TRUE.equals(request.getCadastrarNovo())) {
                // ✅ CADASTRAR NOVO CLIENTE
                System.out.println("➕ Cadastrando novo cliente...");
                
                if (request.getNome() == null || request.getNome().trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Nome é obrigatório para cadastrar novo cliente"));
                }
                
                cliente = new Cliente();
                cliente.setNome(request.getNome());
                cliente.setCpf(request.getCpf());
                cliente.setCelular(request.getCelular());
                cliente.setDataNascimento(LocalDate.now());
                cliente.setEndereco("");
                cliente.setCidade("");
                cliente.setEstado("");
                cliente.setCep("");
                cliente.setCreditoAprovado(false);
                
                cliente = clienteRepository.save(cliente);
                
                System.out.println("✅ Cliente criado: " + cliente.getNome() + " (ID: " + cliente.getId() + ")");
                
            } else {
                // ✅ BUSCAR CLIENTE EXISTENTE
                System.out.println("🔍 Buscando cliente existente...");
                
                if (request.getClienteId() == null) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("erro", "ClienteId é obrigatório quando não está cadastrando novo"));
                }
                
                cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
                
                System.out.println("✅ Cliente encontrado: " + cliente.getNome());
                
                // ✅ VALIDAR SE CLIENTE JÁ ESTÁ HOSPEDADO EM OUTRO APARTAMENTO
                List<HospedagemHospede> todasHospedagens = hospedagemHospedeRepository.findAll();
                
                for (HospedagemHospede h : todasHospedagens) {
                    if (h.getCliente() != null && h.getCliente().getId().equals(cliente.getId())) {
                        Reserva reservaExistente = h.getReserva();
                        
                        // Verificar se está em OUTRA reserva ATIVA
                        if (reservaExistente != null && 
                            !reservaExistente.getId().equals(reservaId) &&
                            reservaExistente.getStatus().equals(StatusReservaEnum.ATIVA) &&
                            h.getStatus() == HospedagemHospede.StatusHospedeIndividual.HOSPEDADO) {
                            
                            return ResponseEntity.badRequest().body(Map.of(
                                "erro", "Cliente já está hospedado em outro apartamento",
                                "apartamento", reservaExistente.getApartamento().getNumeroApartamento(),
                                "reservaId", reservaExistente.getId()
                            ));
                        }
                    }
                }
            }
            
            // ========================================
            // 6️⃣ CRIAR REGISTRO DE HOSPEDAGEM
            // ========================================
            HospedagemHospede hospedagem = new HospedagemHospede();
            hospedagem.setCliente(cliente);
            hospedagem.setReserva(reserva);
            hospedagem.setDataEntrada(LocalDateTime.now());
            hospedagem.setDataSaida(null);
            hospedagem.setTitular(false); // Novos hóspedes não são titulares
            hospedagem.setStatus(HospedagemHospede.StatusHospedeIndividual.HOSPEDADO);
            
            hospedagem = hospedagemHospedeRepository.save(hospedagem);
            
            System.out.println("✅ Hospedagem criada: ID " + hospedagem.getId());
            
            // ========================================
            // 7️⃣ ATUALIZAR QUANTIDADE DE HÓSPEDES
            // ========================================
            int novaQuantidade = (int) hospedagemHospedeRepository.findByReservaId(reservaId).stream()
            	    .filter(h -> h.getStatus() == HospedagemHospede.StatusHospedeIndividual.HOSPEDADO)
            	    .count();
            
            reserva.setQuantidadeHospede(novaQuantidade);
            
            System.out.println("👥 Quantidade: " + quantidadeAtual + " → " + novaQuantidade);
            
            // ========================================
            // 8️⃣ RECALCULAR VALORES E LANÇAR DIFERENÇA
            // ========================================
            BigDecimal diferenca = BigDecimal.ZERO;
            
            try {
                TipoApartamento tipoApartamento = reserva.getApartamento().getTipoApartamento();
                
                // Buscar diária para nova quantidade
                Optional<Diaria> diariaOpt = diariaRepository.findByTipoApartamentoAndQuantidade(
                    tipoApartamento, 
                    novaQuantidade
                );
                
                if (diariaOpt.isPresent()) {
                    Diaria diariaAplicavel = diariaOpt.get();
                    
                    System.out.println("💰 Diária aplicável: R$ " + diariaAplicavel.getValor() + 
                        " (para " + novaQuantidade + " hóspede(s))");
                    
                    // Calcular quantidade de dias
                    long dias = ChronoUnit.DAYS.between(
                        reserva.getDataCheckin().toLocalDate(),
                        reserva.getDataCheckout().toLocalDate()
                    );
                    
                    // Calcular novo valor total
                    BigDecimal novoValorTotal = diariaAplicavel.getValor()
                        .multiply(BigDecimal.valueOf(dias));
                    
                    // Calcular diferença
                    diferenca = novoValorTotal.subtract(totalDiariaAnterior);
                    
                    System.out.println("💰 Novo total de diárias: R$ " + novoValorTotal);
                    System.out.println("💰 Diferença: R$ " + diferenca);
                    
                    // ✅ SE HÁ DIFERENÇA, LANÇAR NO EXTRATO
                    if (diferenca.compareTo(BigDecimal.ZERO) > 0) {
                        ExtratoReserva extratoAcrescimo = new ExtratoReserva();
                        extratoAcrescimo.setReserva(reserva);
                        extratoAcrescimo.setDescricao(String.format(
                            "Acréscimo - Hóspede adicional (%d → %d hóspedes)",
                            quantidadeAtual, novaQuantidade
                        ));
                        extratoAcrescimo.setQuantidade(1);
                        extratoAcrescimo.setValorUnitario(diferenca);
                        extratoAcrescimo.setTotalLancamento(diferenca);
                        extratoAcrescimo.setStatusLancamento(StatusLancamentoEnum.DIARIA);
                        extratoAcrescimo.setDataHoraLancamento(LocalDateTime.now());
                        
                        extratoReservaRepository.save(extratoAcrescimo);
                        
                        System.out.println("✅ Diferença lançada no extrato: R$ " + diferenca);
                    }
                    
                    // Atualizar diária e totais da reserva
                    reserva.setDiaria(diariaAplicavel);
                    reserva.setTotalDiaria(novoValorTotal);
                    
                    // Recalcular total da hospedagem (diárias + consumo)
                    BigDecimal totalProduto = reserva.getTotalProduto() != null ? 
                        reserva.getTotalProduto() : BigDecimal.ZERO;
                    reserva.setTotalHospedagem(novoValorTotal.add(totalProduto));
                    
                    // Recalcular saldo
                    BigDecimal totalRecebido = reserva.getTotalRecebido() != null ? 
                        reserva.getTotalRecebido() : BigDecimal.ZERO;
                    reserva.setTotalApagar(reserva.getTotalHospedagem().subtract(totalRecebido));
                    
                } else {
                    System.out.println("⚠️ Diária não encontrada para " + novaQuantidade + " hóspede(s)");
                }
                
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao recalcular valores: " + e.getMessage());
                e.printStackTrace();
            }
            
            // ========================================
            // 9️⃣ SALVAR RESERVA
            // ========================================
            reservaRepository.save(reserva);
            
            // ========================================
            // 🔟 CRIAR HISTÓRICO (IMPORTANTE!)
            // ========================================
            HistoricoHospede historico = new HistoricoHospede();
            historico.setReserva(reserva);
            historico.setDataHora(LocalDateTime.now());
            historico.setQuantidadeAnterior(quantidadeAtual);
            historico.setQuantidadeNova(novaQuantidade);

            String motivoHistorico = String.format(
                "Hóspede adicionado: %s - Quantidade: %d → %d hóspede(s)",
                cliente.getNome(),
                quantidadeAtual,
                novaQuantidade
            );

            if (diferenca.compareTo(BigDecimal.ZERO) > 0) {
                motivoHistorico += String.format(" - Acréscimo: R$ %.2f", diferenca);
            }

            historico.setMotivo(motivoHistorico);

            historicoHospedeRepository.save(historico);
            
            System.out.println("📝 Histórico criado: " + motivoHistorico);
            
            // ========================================
            // 1️⃣1️⃣ MONTAR RESPOSTA
            // ========================================
            Map<String, Object> hospedeResponse = new HashMap<>();
            hospedeResponse.put("id", hospedagem.getId());
            hospedeResponse.put("nomeCompleto", cliente.getNome());
            hospedeResponse.put("cpf", cliente.getCpf());
            hospedeResponse.put("telefone", cliente.getCelular());
            hospedeResponse.put("titular", hospedagem.getTitular());
            hospedeResponse.put("status", hospedagem.getStatus());
            
            System.out.println("═══════════════════════════════════════════");
            System.out.println("✅ HÓSPEDE ADICIONADO COM SUCESSO!");
            System.out.println("   Nome: " + cliente.getNome());
            System.out.println("   Nova quantidade: " + novaQuantidade);
            System.out.println("   Novo total: R$ " + reserva.getTotalHospedagem());
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(Map.of(
                "mensagem", "Hóspede adicionado com sucesso",
                "hospede", hospedeResponse,
                "novaQuantidade", novaQuantidade,
                "novoValorTotal", reserva.getTotalHospedagem(),
                "diferenca", diferenca
            ));
            
        } catch (Exception e) {
            System.err.println("═══════════════════════════════════════════");
            System.err.println("❌ ERRO AO ADICIONAR HÓSPEDE");
            System.err.println("═══════════════════════════════════════════");
            e.printStackTrace();
            System.err.println("═══════════════════════════════════════════");
            
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/{reservaId}/hospedes/{hospedeId}")
    public ResponseEntity<?> removerHospede(
            @PathVariable Long reservaId,
            @PathVariable Long hospedeId) {
        
        try {
            System.out.println("═══════════════════════════════════════════");
            System.out.println("🗑️ REMOVENDO HÓSPEDE");
            System.out.println("═══════════════════════════════════════════");
            
            // ========================================
            // 1️⃣ BUSCAR RESERVA
            // ========================================
            Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
            
            System.out.println("📋 Reserva #" + reservaId);
            
            // ========================================
            // 2️⃣ VALIDAR SE RESERVA ESTÁ ATIVA
            // ========================================
            if (!reserva.getStatus().equals(StatusReservaEnum.ATIVA)) {
                System.out.println("❌ Reserva não está ATIVA");
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Só é possível remover hóspedes de reservas ATIVAS"));
            }
            
            // ========================================
            // 3️⃣ BUSCAR HOSPEDAGEM
            // ========================================
            HospedagemHospede hospedagem = hospedagemHospedeRepository.findById(hospedeId)
                .orElseThrow(() -> new RuntimeException("Hóspede não encontrado"));
            
            String nomeHospede = hospedagem.getCliente().getNome();
            boolean ehTitular = hospedagem.getTitular();
            
            System.out.println("👤 Hóspede: " + nomeHospede);
            System.out.println("⭐ Titular: " + (ehTitular ? "SIM" : "NÃO"));
            
            // ========================================
            // 4️⃣ VERIFICAR QUANTIDADE MÍNIMA (ANTES DE REMOVER)
            // ========================================
            int quantidadeAtual = (int) hospedagemHospedeRepository.findByReservaId(reservaId).stream()
            	    .filter(h -> h.getStatus() == HospedagemHospede.StatusHospedeIndividual.HOSPEDADO)
            	    .count();
            
            System.out.println("📊 Hóspedes atuais: " + quantidadeAtual);
            
            if (quantidadeAtual <= 1) {
                System.out.println("❌ Não pode remover - mínimo 1 hóspede");
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Reserva deve ter pelo menos 1 hóspede"));
            }
            
            // ========================================
            // 5️⃣ SE FOR TITULAR, PROMOVER OUTRO HÓSPEDE
            // ========================================
            if (ehTitular) {
                System.out.println("⚠️ Removendo TITULAR - Promovendo próximo hóspede...");
                
                List<HospedagemHospede> hospedesAtuais = hospedagemHospedeRepository.findByReservaId(reservaId);
                
                // Encontrar o próximo hóspede (que não seja o atual)
                HospedagemHospede novoTitular = hospedesAtuais.stream()
                    .filter(h -> !h.getId().equals(hospedeId))
                    .findFirst()
                    .orElse(null);
                
                if (novoTitular != null) {
                    novoTitular.setTitular(true);
                    hospedagemHospedeRepository.save(novoTitular);
                    
                    System.out.println("✅ Novo titular: " + novoTitular.getCliente().getNome());
                    
                    // CRÍTICO: Atualizar o cliente titular na reserva
                    reserva.setCliente(novoTitular.getCliente());
                } else {
                    System.out.println("❌ ERRO: Não foi possível encontrar novo titular!");
                }
            }
            
            // ========================================
            // 6️⃣ REMOVER HOSPEDAGEM
            // ========================================
            hospedagemHospedeRepository.delete(hospedagem);
            System.out.println("🗑️ Hóspede removido: " + nomeHospede);
            
            // ========================================
            // 7️⃣ ATUALIZAR QUANTIDADE
            // ========================================
                       
            int novaQuantidade = (int) hospedagemHospedeRepository.findByReservaId(reservaId).stream()
            	    .filter(h -> h.getStatus() == HospedagemHospede.StatusHospedeIndividual.HOSPEDADO)
            	    .count();
            
            
            reserva.setQuantidadeHospede(novaQuantidade);
            
            System.out.println("📊 Nova quantidade: " + novaQuantidade);
            
            // ========================================
            // 8️⃣ RECALCULAR VALORES
            // ========================================
            try {
                TipoApartamento tipoApartamento = reserva.getApartamento().getTipoApartamento();
                
                Optional<Diaria> diariaOpt = diariaRepository.findByTipoApartamentoAndQuantidade(
                    tipoApartamento,
                    reserva.getQuantidadeHospede()
                );
                
                if (diariaOpt.isPresent()) {
                    Diaria diariaAplicavel = diariaOpt.get();
                    
                    long dias = ChronoUnit.DAYS.between(
                        reserva.getDataCheckin().toLocalDate(),
                        reserva.getDataCheckout().toLocalDate()
                    );
                    
                    BigDecimal novoValorTotal = diariaAplicavel.getValor()
                        .multiply(BigDecimal.valueOf(dias));
                    
                    reserva.setDiaria(diariaAplicavel);
                    reserva.setTotalDiaria(novoValorTotal);
                    reserva.setTotalHospedagem(novoValorTotal);
                    
                    BigDecimal novoSaldo = novoValorTotal.subtract(
                        reserva.getTotalRecebido() != null ? reserva.getTotalRecebido() : BigDecimal.ZERO
                    );
                    reserva.setTotalApagar(novoSaldo);
                    
                    System.out.println("💰 Novo valor total: R$ " + novoValorTotal);
                    System.out.println("💳 Novo saldo: R$ " + novoSaldo);
                } else {
                    System.out.println("⚠️ Diária não encontrada para recálculo");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao recalcular valores: " + e.getMessage());
            }
            
            // ========================================
            // 9️⃣ SALVAR RESERVA
            // ========================================
            reservaRepository.save(reserva);
            
            System.out.println("✅ HÓSPEDE REMOVIDO COM SUCESSO!");
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(Map.of(
                "mensagem", "Hóspede removido com sucesso",
                "hospedeRemovido", nomeHospede,
                "eraTitular", ehTitular,
                "novaQuantidade", novaQuantidade,
                "novoValorTotal", reserva.getTotalHospedagem()
            ));
            
        } catch (Exception e) {
            System.err.println("═══════════════════════════════════════════");
            System.err.println("❌ ERRO AO REMOVER HÓSPEDE");
            System.err.println("═══════════════════════════════════════════");
            e.printStackTrace();
            System.err.println("═══════════════════════════════════════════");
            
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        }
    }
    // ============================================
    // MÉTODO AUXILIAR
    // ============================================
    
    private ReservaResponseDTO converterParaDTO(Reserva reserva) {
        ReservaResponseDTO dto = new ReservaResponseDTO();
        
        dto.setId(reserva.getId());
        dto.setQuantidadeHospede(reserva.getQuantidadeHospede());
        dto.setDataCheckin(reserva.getDataCheckin());
        dto.setDataCheckout(reserva.getDataCheckout());
        dto.setQuantidadeDiaria(reserva.getQuantidadeDiaria());
        dto.setStatus(reserva.getStatus());
        dto.setObservacoes(reserva.getObservacoes() != null ? reserva.getObservacoes() : "");
        
        dto.setValorDiaria(reserva.getDiaria() != null ? reserva.getDiaria().getValor() : BigDecimal.ZERO);
        dto.setTotalDiaria(reserva.getTotalDiaria());
        dto.setTotalHospedagem(reserva.getTotalHospedagem());
        dto.setTotalRecebido(reserva.getTotalRecebido());
        dto.setTotalApagar(reserva.getTotalApagar());
        
        if (reserva.getCliente() != null) {
            ClienteResponseDTO clienteDTO = new ClienteResponseDTO();
            clienteDTO.setId(reserva.getCliente().getId());
            clienteDTO.setNome(reserva.getCliente().getNome());
            clienteDTO.setCpf(reserva.getCliente().getCpf());
            clienteDTO.setTelefone(reserva.getCliente().getCelular());
            clienteDTO.setEndereco(reserva.getCliente().getEndereco());
            clienteDTO.setCidade(reserva.getCliente().getCidade());
            clienteDTO.setEstado(reserva.getCliente().getEstado());
            clienteDTO.setCep(reserva.getCliente().getCep());
            dto.setCliente(clienteDTO);
        }
        
        if (reserva.getApartamento() != null) {
            ApartamentoResponseDTO apartamentoDTO = new ApartamentoResponseDTO();
            apartamentoDTO.setId(reserva.getApartamento().getId());
            apartamentoDTO.setNumeroApartamento(reserva.getApartamento().getNumeroApartamento());
            apartamentoDTO.setCapacidade(reserva.getApartamento().getCapacidade());
            apartamentoDTO.setCamasDoApartamento(reserva.getApartamento().getCamasDoApartamento());
            apartamentoDTO.setTv(reserva.getApartamento().getTv());
            apartamentoDTO.setStatus(reserva.getApartamento().getStatus());
            
            if (reserva.getApartamento().getTipoApartamento() != null) {
                apartamentoDTO.setTipoApartamentoId(reserva.getApartamento().getTipoApartamento().getId());
                apartamentoDTO.setTipoApartamentoNome(reserva.getApartamento().getTipoApartamento().getTipo().toString());
                apartamentoDTO.setTipoApartamentoDescricao(reserva.getApartamento().getTipoApartamento().getDescricao());
            }
            
            dto.setApartamento(apartamentoDTO);
        }
        
        return dto;
    }
        /**
         * Checkout parcial - Um hóspede sai antes do checkout geral
         */
        @PostMapping("/{id}/checkout-parcial") 
        public ResponseEntity<?> checkoutParcial(
                @PathVariable Long id,
                @RequestBody CheckoutParcialRequestDTO dto) {
            
            System.out.println("═══════════════════════════════════════════");
            System.out.println("🔔 ENDPOINT: Checkout Parcial");
            System.out.println("═══════════════════════════════════════════");
            System.out.println("📊 Reserva ID: " + id);
            System.out.println("👤 Hospedagem ID: " + dto.getHospedagemHospedeId());
            
            try {
                Reserva reserva = reservaService.checkoutParcial(id, dto);
                
                Map<String, Object> resposta = new HashMap<>();
                resposta.put("sucesso", true);
                resposta.put("mensagem", "Checkout parcial realizado com sucesso");
                resposta.put("reserva", reserva);
                resposta.put("novaQuantidadeHospedes", reserva.getQuantidadeHospede());
                
                System.out.println("✅ Checkout parcial concluído!");
                System.out.println("═══════════════════════════════════════════");
                
                return ResponseEntity.ok(resposta);
                
            } catch (RuntimeException e) {
                System.err.println("❌ Erro no checkout parcial: " + e.getMessage());
                System.out.println("═══════════════════════════════════════════");
                
                Map<String, Object> erro = new HashMap<>();
                erro.put("sucesso", false);
                erro.put("mensagem", e.getMessage());
                
                return ResponseEntity.badRequest().body(erro);
                
            } catch (Exception e) {
                System.err.println("❌ Erro inesperado: " + e.getMessage());
                e.printStackTrace();
                System.out.println("═══════════════════════════════════════════");
                
                Map<String, Object> erro = new HashMap<>();
                erro.put("sucesso", false);
                erro.put("mensagem", "Erro interno ao processar checkout parcial: " + e.getMessage());
                
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
            }
        }
        
        @PostMapping("/transferir-hospede")
        public ResponseEntity<?> transferirHospede(@Valid @RequestBody TransferenciaHospedeDTO dto) {
            try {
                System.out.println("═══════════════════════════════════════════");
                System.out.println("🔄 INICIANDO TRANSFERÊNCIA DE HÓSPEDE");
                System.out.println("═══════════════════════════════════════════");
                System.out.println("📋 Hóspede ID: " + dto.getHospedeId());
                System.out.println("🏨 Apartamento destino ID: " + dto.getApartamentoDestinoId());
                System.out.println("📅 Check-in novo: " + dto.getDataCheckinNovo());
                System.out.println("📅 Check-out novo: " + dto.getDataCheckoutNovo());
                System.out.println("💰 Pagar despesas antes: " + dto.getPagarDespesasAntes());
                
                // Chamar o service
                Reserva novaReserva = reservaService.transferirHospede(dto);
                
                // Converter para DTO de resposta
                ReservaResponseDTO response = converterParaDTO(novaReserva);
                
                System.out.println("✅ Transferência concluída com sucesso!");
                System.out.println("═══════════════════════════════════════════");
                
                return ResponseEntity.ok(response);
                
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Erro de validação: " + e.getMessage());
                return ResponseEntity.badRequest().body(e.getMessage());
                
            } catch (Exception e) {
                System.out.println("❌ Erro ao transferir hóspede: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.badRequest().body("Erro ao transferir hóspede: " + e.getMessage());
            }
        }
        
        /**
         * Pesquisar em qual apartamento um cliente está hospedado
         */
        @GetMapping("/pesquisar-cliente")
        public ResponseEntity<?> pesquisarCliente(@RequestParam String nome) {
            try {
                System.out.println("═══════════════════════════════════════════");
                System.out.println("🔍 PESQUISANDO CLIENTE");
                System.out.println("═══════════════════════════════════════════");
                System.out.println("📝 Nome: " + nome);
                
                // Buscar reservas ATIVAS
                List<Reserva> reservasAtivas = reservaRepository.findByStatus(StatusReservaEnum.ATIVA);
                
                System.out.println("📊 Total de reservas ativas: " + reservasAtivas.size());
                
                // Procurar cliente titular
                for (Reserva reserva : reservasAtivas) {
                    if (reserva.getCliente() != null && 
                        reserva.getCliente().getNome().toLowerCase().contains(nome.toLowerCase())) {
                        
                        System.out.println("✅ Cliente encontrado como titular!");
                        System.out.println("   Apartamento: " + reserva.getApartamento().getNumeroApartamento());
                        
                        Map<String, Object> resultado = new HashMap<>();
                        resultado.put("sucesso", true);
                        resultado.put("mensagem", "Cliente encontrado!");
                        
                        Map<String, Object> dadosReserva = new HashMap<>();
                        dadosReserva.put("id", reserva.getId());
                        dadosReserva.put("cliente", reserva.getCliente().getNome());
                        dadosReserva.put("apartamento", reserva.getApartamento().getNumeroApartamento());
                        dadosReserva.put("dataCheckin", reserva.getDataCheckin());
                        dadosReserva.put("dataCheckout", reserva.getDataCheckout());
                        dadosReserva.put("status", reserva.getStatus().toString());
                        
                        resultado.put("reserva", dadosReserva);
                        
                        return ResponseEntity.ok(resultado);
                    }
                }
                
                // Procurar nos hóspedes adicionais
                for (Reserva reserva : reservasAtivas) {
                    List<HospedagemHospede> hospedes = hospedagemHospedeRepository.findByReservaId(reserva.getId());
                    
                    for (HospedagemHospede hospede : hospedes) {
                        if (hospede.getCliente() != null && 
                            hospede.getCliente().getNome().toLowerCase().contains(nome.toLowerCase()) &&
                            hospede.getStatus() == HospedagemHospede.StatusHospedeIndividual.HOSPEDADO) {
                            
                            System.out.println("✅ Cliente encontrado como hóspede adicional!");
                            System.out.println("   Apartamento: " + reserva.getApartamento().getNumeroApartamento());
                            
                            Map<String, Object> resultado = new HashMap<>();
                            resultado.put("sucesso", true);
                            resultado.put("mensagem", "Cliente encontrado como hóspede adicional!");
                            
                            Map<String, Object> dadosReserva = new HashMap<>();
                            dadosReserva.put("id", reserva.getId());
                            dadosReserva.put("cliente", hospede.getCliente().getNome());
                            dadosReserva.put("apartamento", reserva.getApartamento().getNumeroApartamento());
                            dadosReserva.put("dataCheckin", reserva.getDataCheckin());
                            dadosReserva.put("dataCheckout", reserva.getDataCheckout());
                            dadosReserva.put("status", reserva.getStatus().toString());
                            
                            resultado.put("reserva", dadosReserva);
                            
                            return ResponseEntity.ok(resultado);
                        }
                    }
                }
                
                // Não encontrado
                System.out.println("❌ Cliente não encontrado em nenhuma reserva ativa");
                System.out.println("═══════════════════════════════════════════");
                
                Map<String, Object> resultado = new HashMap<>();
                resultado.put("sucesso", false);
                resultado.put("mensagem", "Cliente '" + nome + "' não está hospedado no momento");
                
                return ResponseEntity.ok(resultado);
                
            } catch (Exception e) {
                System.err.println("❌ Erro na pesquisa: " + e.getMessage());
                e.printStackTrace();
                
                Map<String, Object> erro = new HashMap<>();
                erro.put("sucesso", false);
                erro.put("mensagem", "Erro ao pesquisar cliente: " + e.getMessage());
                
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
            }
        }

        /**
         * Pesquisar todos os hóspedes de uma empresa
         */
        @GetMapping("/pesquisar-empresa")
        public ResponseEntity<?> pesquisarEmpresa(@RequestParam String nomeEmpresa) {
            List<Reserva> reservasAtivas = reservaRepository.findByStatus(StatusReservaEnum.ATIVA);
            List<Map<String, Object>> hospedesdaEmpresa = new ArrayList<>();
            Set<String> apartamentosUnicos = new HashSet<>(); // ✅ NOVO: Para contar apartamentos únicos

            // Percorrer todas as reservas ativas
            for (Reserva reserva : reservasAtivas) {
                List<HospedagemHospede> hospedes = hospedagemHospedeRepository.findByReservaId(reserva.getId());

                for (HospedagemHospede hospede : hospedes) {
                    if (hospede.getCliente().getEmpresa() != null &&
                        hospede.getCliente().getEmpresa().getNomeEmpresa().toLowerCase()
                            .contains(nomeEmpresa.toLowerCase()) &&
                        hospede.getStatus() == HospedagemHospede.StatusHospedeIndividual.HOSPEDADO) {

                        Map<String, Object> hospedeMap = new HashMap<>();
                        hospedeMap.put("nomeCliente", hospede.getCliente().getNome());
                        hospedeMap.put("apartamento", reserva.getApartamento().getNumeroApartamento());
                        hospedeMap.put("reservaId", reserva.getId());
                        hospedeMap.put("titular", hospede.getTitular());
                        hospedeMap.put("nomeEmpresa", hospede.getCliente().getEmpresa().getNomeEmpresa());

                        hospedesdaEmpresa.add(hospedeMap);
                        
                        // ✅ NOVO: Adicionar apartamento ao Set (não permite duplicados)
                        apartamentosUnicos.add(reserva.getApartamento().getNumeroApartamento());
                    }
                }
            }

            if (hospedesdaEmpresa.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "sucesso", false,
                    "mensagem", "Nenhum hóspede da empresa '" + nomeEmpresa + "' está hospedado no momento"
                ));
            }

            // Ordenar por apartamento
            hospedesdaEmpresa.sort((a, b) -> {
                String aptA = (String) a.get("apartamento");
                String aptB = (String) b.get("apartamento");
                return aptA.compareTo(aptB);
            });

            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Encontrados " + hospedesdaEmpresa.size() + " hóspede(s) da empresa",
                "nomeEmpresa", hospedesdaEmpresa.get(0).get("nomeEmpresa"),
                "totalHospedes", hospedesdaEmpresa.size(),
                "totalApartamentos", apartamentosUnicos.size(), // ✅ NOVO
                "hospedes", hospedesdaEmpresa
            ));
        }
        
        @PostMapping("/validar-hospede")
        public ResponseEntity<?> validarDisponibilidadeHospede(@RequestBody ValidarHospedeDTO dto) {
            try {
                System.out.println("\n═══════════════════════════════════════════");
                System.out.println("🔍 VALIDANDO DISPONIBILIDADE DO HÓSPEDE");
                System.out.println("═══════════════════════════════════════════");
                System.out.println("📋 DTO recebido: " + dto);
                System.out.println("👤 Cliente ID: " + dto.getClienteId());
                System.out.println("📅 Check-in:  " + DataUtil.formatarDataHora(dto.getDataCheckin()));
                System.out.println("📅 Check-out: " + DataUtil.formatarDataHora(dto.getDataCheckout()));
                
                Long clienteId = dto.getClienteId();
                LocalDateTime checkinNovo = dto.getDataCheckin();
                LocalDateTime checkoutNovo = dto.getDataCheckout();
                
                // Buscar cliente
                Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
                
                System.out.println("✅ Cliente encontrado: " + cliente.getNome());
                
                // ═══════════════════════════════════════════
                // 1️⃣ VERIFICAR SE É TITULAR DE ALGUMA RESERVA ATIVA
                // ═══════════════════════════════════════════
                List<Reserva> reservasComoTitular = reservaRepository.findAll().stream()
                    .filter(r -> r.getStatus() == StatusReservaEnum.ATIVA)
                    .filter(r -> r.getCliente() != null && r.getCliente().getId().equals(clienteId))
                    .collect(Collectors.toList());
                
                System.out.println("📊 Reservas como TITULAR: " + reservasComoTitular.size());
                
                for (Reserva reserva : reservasComoTitular) {
                    System.out.println("\n🔎 Verificando reserva #" + reserva.getId());
                    System.out.println("   Apartamento: " + reserva.getApartamento().getNumeroApartamento());
                    System.out.println("   Período: " + DataUtil.formatarPeriodo(reserva.getDataCheckin(), reserva.getDataCheckout()));
                    
                    // Verificar conflito de datas
                    boolean checkinConflita = !checkinNovo.isBefore(reserva.getDataCheckin()) && 
                                             checkinNovo.isBefore(reserva.getDataCheckout());
                    
                    boolean checkoutConflita = checkoutNovo.isAfter(reserva.getDataCheckin()) && 
                                              !checkoutNovo.isAfter(reserva.getDataCheckout());
                    
                    boolean envolveTudo = !checkinNovo.isAfter(reserva.getDataCheckin()) && 
                                         !checkoutNovo.isBefore(reserva.getDataCheckout());
                    
                    if (checkinConflita || checkoutConflita || envolveTudo) {
                        System.out.println("   ❌ CONFLITO DETECTADO!");
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("disponivel", false);
                        response.put("mensagem", 
                            cliente.getNome() + " já possui reserva no período de " + 
                            DataUtil.formatarPeriodo(reserva.getDataCheckin(), reserva.getDataCheckout()) +
                            " no apartamento " + reserva.getApartamento().getNumeroApartamento()
                        );
                        return ResponseEntity.ok(response);
                    }
                    
                    System.out.println("   ✅ Sem conflito com esta reserva");
                }
                
                // ═══════════════════════════════════════════
                // 2️⃣ VERIFICAR SE É HÓSPEDE EM ALGUMA HOSPEDAGEM ATIVA
                // ═══════════════════════════════════════════
                List<HospedagemHospede> hospedagensAtivas = hospedagemHospedeRepository.findAll().stream()
                    .filter(h -> h.getStatus() == HospedagemHospede.StatusHospedeIndividual.HOSPEDADO)
                    .filter(h -> h.getReserva() != null && 
                                 h.getReserva().getStatus() == StatusReservaEnum.ATIVA)
                    .filter(h -> h.getCliente() != null && 
                                 h.getCliente().getId().equals(clienteId))
                    .collect(Collectors.toList());
                
                System.out.println("📊 Hospedagens como HÓSPEDE: " + hospedagensAtivas.size());
                
                for (HospedagemHospede hospedagem : hospedagensAtivas) {
                    Reserva reserva = hospedagem.getReserva();
                    
                    System.out.println("\n🔎 Verificando hospedagem - Reserva #" + reserva.getId());
                    System.out.println("   Apartamento: " + reserva.getApartamento().getNumeroApartamento());
                    System.out.println("   Período: " + DataUtil.formatarPeriodo(reserva.getDataCheckin(), reserva.getDataCheckout()));
                    
                    // Verificar conflito de datas
                    boolean checkinConflita = !checkinNovo.isBefore(reserva.getDataCheckin()) && 
                                             checkinNovo.isBefore(reserva.getDataCheckout());
                    
                    boolean checkoutConflita = checkoutNovo.isAfter(reserva.getDataCheckin()) && 
                                              !checkoutNovo.isAfter(reserva.getDataCheckout());
                    
                    boolean envolveTudo = !checkinNovo.isAfter(reserva.getDataCheckin()) && 
                                         !checkoutNovo.isBefore(reserva.getDataCheckout());
                    
                    if (checkinConflita || checkoutConflita || envolveTudo) {
                        System.out.println("   ❌ CONFLITO DETECTADO!");
                        
                        Map<String, Object> response = new HashMap<>();
                        response.put("disponivel", false);
                        response.put("mensagem", 
                            cliente.getNome() + " já está hospedado no período de " + 
                            DataUtil.formatarPeriodo(reserva.getDataCheckin(), reserva.getDataCheckout()) +
                            " no apartamento " + reserva.getApartamento().getNumeroApartamento()
                        );
                        return ResponseEntity.ok(response);
                    }
                    
                    System.out.println("   ✅ Sem conflito com esta hospedagem");
                }
                
                System.out.println("\n✅ Cliente disponível!");
                System.out.println("═══════════════════════════════════════════\n");
                
                Map<String, Object> response = new HashMap<>();
                response.put("disponivel", true);
                response.put("mensagem", "Cliente disponível para o período solicitado");
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                System.err.println("❌ ERRO ao validar disponibilidade: " + e.getMessage());
                e.printStackTrace();
                
                Map<String, Object> response = new HashMap<>();
                response.put("disponivel", false);
                response.put("mensagem", "Erro ao validar: " + e.getMessage());
                return ResponseEntity.status(500).body(response);
            }
        }
        
        /**
         * 📊 ESTATÍSTICAS DE RESERVAS
         */
        @GetMapping("/estatisticas")
        public ResponseEntity<?> obterEstatisticas() {
            try {
                long ativas = reservaRepository.countByStatus(StatusReservaEnum.ATIVA);
                long preReservas = reservaRepository.countByStatus(StatusReservaEnum.PRE_RESERVA);
                long canceladas = reservaRepository.countByStatus(StatusReservaEnum.CANCELADA);
                long finalizadas = reservaRepository.countByStatus(StatusReservaEnum.FINALIZADA);
                
                System.out.println("📊 Estatísticas de reservas:");
                System.out.println("   Ativas: " + ativas);
                System.out.println("   Pré-reservas: " + preReservas);
                System.out.println("   Canceladas: " + canceladas);
                System.out.println("   Finalizadas: " + finalizadas);
                
                return ResponseEntity.ok(Map.of(
                    "ativas", ativas,
                    "preReservas", preReservas,
                    "canceladas", canceladas,
                    "finalizadas", finalizadas
                ));
            } catch (Exception e) {
                System.err.println("❌ Erro ao obter estatísticas: " + e.getMessage());
                return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
            }
        }
        
        /**
         * 🚪 REALIZAR CHECK-OUT
         */
        @PostMapping("/{id}/checkout")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<?> realizarCheckOut(@PathVariable Long id) {
            try {
                System.out.println("🚪 Requisição de checkout para reserva #" + id);
                
                Reserva reserva = reservaService.realizarCheckOut(id);
                
                return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Check-out realizado com sucesso!",
                    "reserva", reserva
                ));
                
            } catch (Exception e) {
                System.err.println("❌ Erro no checkout: " + e.getMessage());
                return ResponseEntity.badRequest().body(Map.of(
                    "sucesso", false,
                    "erro", e.getMessage()
                ));
            }
        
        }}

