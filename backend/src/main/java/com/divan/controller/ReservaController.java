package com.divan.controller;

import com.divan.service.DiariaService;
import com.divan.repository.ApartamentoRepository;
import com.divan.repository.BilheteSorteioRepository;
import com.divan.repository.ClienteRepository;
import com.divan.repository.HospedagemHospedeRepository;
import com.divan.repository.LogAuditoriaRepository;
import com.divan.entity.HospedagemHospede;
import com.divan.entity.Cliente;
import com.divan.entity.ExtratoReserva;
import com.divan.entity.Diaria;
import com.divan.entity.TipoApartamento;
import com.divan.repository.DiariaRepository;
import com.divan.repository.ExtratoReservaRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import com.divan.dto.ItemVendaRequestDTO;
import com.divan.dto.LancamentoRapidoRequest;
import com.divan.dto.ReservaDetalhesDTO;
import com.divan.dto.ReservaRequestDTO;
import com.divan.dto.ReservaResponseDTO;
import com.divan.dto.TransferenciaApartamentoDTO;
import com.divan.entity.Apartamento;
import com.divan.entity.BilheteSorteio;
import com.divan.entity.ItemVenda;
import com.divan.entity.LogAuditoria;
import com.divan.entity.NotaVenda;
import com.divan.entity.Reserva;
import com.divan.repository.ReservaRepository;
import com.divan.repository.UsuarioRepository;
import com.divan.service.ApartamentoService;
import com.divan.service.ClienteService;
import com.divan.service.ReservaService;
import com.divan.service.SorteioService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "*")
public class ReservaController {
	
	@Autowired
	private LogAuditoriaRepository logAuditoriaRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private ApartamentoRepository apartamentoRepository;
			
	@Autowired
	private DiariaRepository diariaRepository;
		
	@Autowired
	private ExtratoReservaRepository extratoReservaRepository;
	
	@Autowired
	private ClienteRepository clienteRepository;
	
	@Autowired
	private HospedagemHospedeRepository hospedagemHospedeRepository;
	
	@Autowired
	private ReservaRepository reservaRepository;
    
    @Autowired
    private ReservaService reservaService;
    
    @Autowired
    private ApartamentoService apartamentoService;
    
    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private SorteioService sorteioService;
    
    @Autowired
    private DiariaService diariaService;
    
    @Autowired
    private BilheteSorteioRepository bilheteSorteioRepository;
    
    @PostMapping
    public ResponseEntity<?> criarReserva(@Valid @RequestBody ReservaRequestDTO dto) {
        try {
            if (dto.getDataCheckout().isBefore(dto.getDataCheckin()) || 
                dto.getDataCheckout().isEqual(dto.getDataCheckin())) {
                return ResponseEntity.badRequest().body("Data de check-out deve ser posterior ao check-in");
            }
            
            Optional<Apartamento> apartamentoOpt = apartamentoService.buscarPorId(dto.getApartamentoId());
            if (apartamentoOpt.isEmpty()) return ResponseEntity.badRequest().body("Apartamento não encontrado");
            
            Optional<Cliente> clienteOpt = clienteService.buscarPorId(dto.getClienteId());
            if (clienteOpt.isEmpty()) return ResponseEntity.badRequest().body("Cliente não encontrado");
            
            Apartamento apartamento = apartamentoOpt.get();
            if (dto.getQuantidadeHospede() > apartamento.getCapacidade()) {
                return ResponseEntity.badRequest()
                    .body("Quantidade de hóspedes excede a capacidade do apartamento");
            }
            
            Reserva reserva = new Reserva();
            reserva.setApartamento(apartamento);
            reserva.setCliente(clienteOpt.get());
            reserva.setQuantidadeHospede(dto.getQuantidadeHospede());
            reserva.setDataCheckin(dto.getDataCheckin());
            reserva.setDataCheckout(dto.getDataCheckout());
            
            Reserva reservaCriada = reservaService.criarReserva(reserva);
            
            if (dto.getHospedesAdicionaisIds() != null && !dto.getHospedesAdicionaisIds().isEmpty()) {
                for (Long clienteId : dto.getHospedesAdicionaisIds()) {
                    clienteRepository.findById(clienteId).ifPresent(cliente -> {
                        HospedagemHospede hospedeExtra = new HospedagemHospede();
                        hospedeExtra.setReserva(reservaCriada);
                        hospedeExtra.setCliente(cliente);
                        hospedeExtra.setTitular(false);
                        hospedeExtra.setStatus(HospedagemHospede.StatusEnum.HOSPEDADO);
                        hospedeExtra.setDataHoraEntrada(LocalDateTime.now());
                        hospedagemHospedeRepository.save(hospedeExtra);
                        System.out.println("✅ Hóspede adicional salvo: " + cliente.getNome());
                    });
                }
            }
            
            // ✅ Retorna só os dados essenciais, sem lazy loading
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", reservaCriada.getId(),
                "status", reservaCriada.getStatus().name(),
                "mensagem", "Reserva criada com sucesso"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
       
    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> listarTodas() {
        List<ReservaResponseDTO> reservas = reservaService.listarTodasDTO();
        return ResponseEntity.ok(reservas);
    }
        
    @GetMapping("/mapa")
    public ResponseEntity<?> getReservasMapa() {
        try {
            List<ReservaResponseDTO> reservas = reservaService.listarPorStatusDTO(Reserva.StatusReservaEnum.ATIVA);
            List<ReservaResponseDTO> preReservas = reservaService.listarPorStatusDTO(Reserva.StatusReservaEnum.PRE_RESERVA);
            
            List<ReservaResponseDTO> todas = new ArrayList<>();
            todas.addAll(reservas);
            todas.addAll(preReservas);
            
            return ResponseEntity.ok(todas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/pesquisar-cliente")
    public ResponseEntity<?> pesquisarCliente(@RequestParam String nome) {
        try {
            List<Cliente> clientes = clienteRepository.findByNomeContainingIgnoreCase(nome);
            
            if (clientes.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "sucesso", false,
                    "mensagem", "Nenhum cliente encontrado com o nome: " + nome
                ));
            }

            for (Cliente cliente : clientes) {
                // ✅ BUSCAR COMO TITULAR
                List<Reserva> reservasAtivas = reservaRepository.findByClienteIdAndStatusIn(
                    cliente.getId(),
                    List.of(Reserva.StatusReservaEnum.ATIVA, Reserva.StatusReservaEnum.PRE_RESERVA)
                );

                if (!reservasAtivas.isEmpty()) {
                    Reserva reserva = reservasAtivas.get(0);
                    return ResponseEntity.ok(Map.of(
                        "sucesso", true,
                        "mensagem", "Cliente encontrado",
                        "reserva", Map.of(
                            "id", reserva.getId(),
                            "cliente", cliente.getNome(),
                            "apartamento", reserva.getApartamento().getNumeroApartamento(),
                            "dataCheckin", reserva.getDataCheckin(),
                            "dataCheckout", reserva.getDataCheckout(),
                            "status", reserva.getStatus(),
                            "quantidadeHospede", reserva.getQuantidadeHospede(),
                            "totalHospedagem", reserva.getTotalHospedagem()
                        )
                    ));
                }

                // ✅ BUSCAR COMO HÓSPEDE ADICIONAL
                List<HospedagemHospede> hospedesAtivos = hospedagemHospedeRepository
                    .findByClienteIdAndStatus(cliente.getId(), HospedagemHospede.StatusEnum.HOSPEDADO);

                if (!hospedesAtivos.isEmpty()) {
                    HospedagemHospede hospede = hospedesAtivos.get(0);
                    Reserva reserva = hospede.getReserva();
                    if (reserva != null && reserva.getStatus() == Reserva.StatusReservaEnum.ATIVA) {
                        return ResponseEntity.ok(Map.of(
                            "sucesso", true,
                            "mensagem", "Cliente encontrado como hóspede adicional",
                            "reserva", Map.of(
                                "id", reserva.getId(),
                                "cliente", cliente.getNome(),
                                "apartamento", reserva.getApartamento().getNumeroApartamento(),
                                "dataCheckin", reserva.getDataCheckin(),
                                "dataCheckout", reserva.getDataCheckout(),
                                "status", reserva.getStatus(),
                                "quantidadeHospede", reserva.getQuantidadeHospede(),
                                "totalHospedagem", reserva.getTotalHospedagem()
                            )
                        ));
                    }
                }
            }

            return ResponseEntity.ok(Map.of(
                "sucesso", false,
                "mensagem", "Cliente encontrado mas sem reserva ativa"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("sucesso", false, "mensagem", e.getMessage()));
        }
    }
    
    @GetMapping("/pesquisar-empresa")
    public ResponseEntity<?> pesquisarEmpresa(@RequestParam String nomeEmpresa) {
        try {
            // ✅ BUSCAR HÓSPEDES DA EMPRESA COM RESERVAS ATIVAS
            List<HospedagemHospede> hospedes = hospedagemHospedeRepository
                .findByStatus(HospedagemHospede.StatusEnum.HOSPEDADO);

            List<Map<String, Object>> hospedesEmpresa = hospedes.stream()
                .filter(h -> h.getCliente() != null
                    && h.getCliente().getEmpresa() != null
                    && h.getCliente().getEmpresa().getNomeEmpresa()
                        .toLowerCase().contains(nomeEmpresa.toLowerCase())
                    && h.getReserva() != null
                    && h.getReserva().getStatus() == Reserva.StatusReservaEnum.ATIVA)
                .map(h -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("nomeCliente", h.getCliente().getNome());
                    map.put("apartamento", h.getReserva().getApartamento().getNumeroApartamento());
                    map.put("reservaId", h.getReserva().getId());
                    map.put("titular", h.isTitular());
                    return map;
                })
                .collect(Collectors.toList());

            if (hospedesEmpresa.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "sucesso", false,
                    "mensagem", "Nenhum hóspede encontrado para a empresa: " + nomeEmpresa
                ));
            }

            Set<Object> apartamentos = hospedesEmpresa.stream()
                .map(h -> h.get("apartamento"))
                .collect(Collectors.toSet());

            String nomeEmpresaEncontrada = hospedes.stream()
                .filter(h -> h.getCliente() != null && h.getCliente().getEmpresa() != null
                    && h.getCliente().getEmpresa().getNomeEmpresa()
                        .toLowerCase().contains(nomeEmpresa.toLowerCase()))
                .map(h -> h.getCliente().getEmpresa().getNomeEmpresa())
                .findFirst().orElse(nomeEmpresa);

            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Empresa encontrada",
                "nomeEmpresa", nomeEmpresaEncontrada,
                "hospedes", hospedesEmpresa,
                "totalHospedes", hospedesEmpresa.size(),
                "totalApartamentos", apartamentos.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "mensagem", e.getMessage()
            ));
        }
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
    
    @GetMapping("/estatisticas")
    public ResponseEntity<?> getEstatisticas() {
        try {
            long total       = reservaRepository.count();
            long ativas      = reservaRepository.countByStatus(Reserva.StatusReservaEnum.ATIVA);
            long preReservas = reservaRepository.countByStatus(Reserva.StatusReservaEnum.PRE_RESERVA);
            long finalizadas = reservaRepository.countByStatus(Reserva.StatusReservaEnum.FINALIZADA);

            return ResponseEntity.ok(Map.of(
                "total",       total,
                "ativas",      ativas,
                "preReservas", preReservas,
                "finalizadas", finalizadas
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
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
    
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizarReserva(@PathVariable Long id) {
        try {
            Reserva reserva = reservaService.finalizarReserva(id);
            return ResponseEntity.ok(reserva);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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

            // ✅ LOG AUDITORIA
            try {
                String username = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();
                LogAuditoria log = new LogAuditoria();
                log.setAcao("COMANDA_CONSUMO");
                log.setDescricao("Comanda de Consumo — Apt " + reserva.getApartamento().getNumeroApartamento()
                    + " — Cliente: " + reserva.getCliente().getNome());
                log.setDataHora(LocalDateTime.now());
                log.setReserva(reserva);
                usuarioRepository.findByUsername(username).ifPresent(log::setUsuario);
                logAuditoriaRepository.save(log);
            } catch (Exception logEx) {
                System.err.println("⚠️ Erro ao salvar log: " + logEx.getMessage());
            }

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
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReservaResponseDTO>> listarPorStatus(@PathVariable Reserva.StatusReservaEnum status) {
        List<ReservaResponseDTO> reservas = reservaService.listarPorStatusDTO(status);
        return ResponseEntity.ok(reservas);
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
    
    @PostMapping("/comandas-rapidas")
    public ResponseEntity<Map<String, Object>> processarComandasRapidas(@RequestBody LancamentoRapidoRequest request) {
        System.out.println("🍽️ Recebendo comandas rápidas");
        Map<String, Object> resultado = reservaService.processarComandasRapidas(request);
        return ResponseEntity.ok(resultado);
    }
    
    @PostMapping("/validar-hospede")
    public ResponseEntity<?> validarHospede(@RequestBody Map<String, Object> body) {
        try {
            Object clienteIdObj = body.get("clienteId");
            if (clienteIdObj == null) {
                return ResponseEntity.badRequest().body("clienteId é obrigatório");
            }

            Long clienteId = Long.parseLong(clienteIdObj.toString());
            LocalDateTime dataCheckin = LocalDateTime.parse(body.get("dataCheckin").toString().substring(0, 19));
            LocalDateTime dataCheckout = LocalDateTime.parse(body.get("dataCheckout").toString().substring(0, 19));

            // ✅ VERIFICAR SE CLIENTE JÁ ESTÁ HOSPEDADO (titular OU adicional)
            List<HospedagemHospede> hospedesAtivos = hospedagemHospedeRepository
            	    .findByClienteIdAndStatus(clienteId, HospedagemHospede.StatusEnum.HOSPEDADO);

            	for (HospedagemHospede hAtivo : hospedesAtivos) {
            	    Reserva rAtiva = hAtivo.getReserva();
            	    if (rAtiva == null) continue;
            	    if (rAtiva.getStatus() != Reserva.StatusReservaEnum.ATIVA) continue;

            	    // ✅ SÓ BLOQUEIA SE AS DATAS CONFLITAM OU SE O CHECKOUT JÁ VENCEU
            	    LocalDateTime agora = LocalDateTime.now();
            	    boolean checkoutVencido = rAtiva.getDataCheckout().isBefore(agora);
            	    boolean conflitaDatas = dataCheckin.isBefore(rAtiva.getDataCheckout()) &&
            	                            dataCheckout.isAfter(rAtiva.getDataCheckin());

            	    if (checkoutVencido || conflitaDatas) {
            	        return ResponseEntity.ok(Map.of(
            	            "disponivel", false,
            	            "mensagem", String.format(
            	                "Cliente está HOSPEDADO no apartamento %s (Reserva #%d) até %s. " +
            	                "Faça o checkout antes de criar nova reserva.",
            	                rAtiva.getApartamento().getNumeroApartamento(),
            	                rAtiva.getId(),
            	                rAtiva.getDataCheckout().toLocalDate()
            	            )
            	        ));
            	    }
            	}

            // ✅ VERIFICAR PRÉ-RESERVAS COMO TITULAR
            List<Reserva> preReservas = reservaRepository.findByClienteIdAndStatusIn(
                clienteId,
                List.of(Reserva.StatusReservaEnum.PRE_RESERVA)
            );

            for (Reserva r : preReservas) {
                boolean conflito =
                    dataCheckin.isBefore(r.getDataCheckout()) &&
                    dataCheckout.isAfter(r.getDataCheckin());

                if (conflito) {
                    return ResponseEntity.ok(Map.of(
                        "disponivel", false,
                        "mensagem", String.format(
                            "Cliente já possui pré-reserva no apartamento %s (Reserva #%d) de %s a %s.",
                            r.getApartamento().getNumeroApartamento(),
                            r.getId(),
                            r.getDataCheckin().toLocalDate(),
                            r.getDataCheckout().toLocalDate()
                        )
                    ));
                }
            }

            return ResponseEntity.ok(Map.of(
                "disponivel", true,
                "mensagem", "Cliente disponível"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "disponivel", false,
                "mensagem", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/{id}/hospedes")
    public ResponseEntity<?> adicionarHospede(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

            // ✅ CRIAR HÓSPEDE
            boolean cadastrarNovo = Boolean.TRUE.equals(body.get("cadastrarNovo"));

            HospedagemHospede hospede = new HospedagemHospede();
            hospede.setReserva(reserva);
            hospede.setStatus(HospedagemHospede.StatusEnum.HOSPEDADO);
            hospede.setTitular(false);
            hospede.setDataHoraEntrada(LocalDateTime.now());

            if (cadastrarNovo) {
                Cliente novoCliente = new Cliente();
                novoCliente.setNome(body.get("nome").toString());
                novoCliente.setCpf(body.containsKey("cpf") && body.get("cpf") != null
                    ? body.get("cpf").toString() : null);
                novoCliente.setCelular(body.containsKey("celular") && body.get("celular") != null
                    ? body.get("celular").toString() : null);
                novoCliente = clienteRepository.save(novoCliente);
                hospede.setCliente(novoCliente);
            } else {
                Long clienteId = Long.parseLong(body.get("clienteId").toString());
                Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
                hospede.setCliente(cliente);
            }

            if (body.containsKey("placaCarro") && body.get("placaCarro") != null
                    && !body.get("placaCarro").toString().isBlank()) {
                hospede.setPlacaCarro(body.get("placaCarro").toString().toUpperCase());               
                }
            
         // ✅ VERIFICAR SE HÓSPEDE JÁ ESTÁ HOSPEDADO EM OUTRO APARTAMENTO
            List<HospedagemHospede> hospedesAtivos = hospedagemHospedeRepository
                .findByClienteIdAndStatus(hospede.getCliente().getId(),
                    HospedagemHospede.StatusEnum.HOSPEDADO);

            for (HospedagemHospede hAtivo : hospedesAtivos) {
                Reserva rAtiva = hAtivo.getReserva();
                if (rAtiva == null) continue;
                if (rAtiva.getStatus() != Reserva.StatusReservaEnum.ATIVA) continue;
              //  if (rAtiva.getId().equals(reserva.getId())) continue; // mesma reserva

             // ✅ Só bloqueia se checkin da nova reserva for ANTES do checkout da ativa
                boolean conflito = reserva.getDataCheckin()
                    .isBefore(rAtiva.getDataCheckout());

                if (conflito) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "erro", String.format(
                            "%s já está hospedado no apartamento %s (Reserva #%d) de %s a %s.",
                            hospede.getCliente().getNome(),
                            rAtiva.getApartamento().getNumeroApartamento(),
                            rAtiva.getId(),
                            rAtiva.getDataCheckin().toLocalDate(),
                            rAtiva.getDataCheckout().toLocalDate()
                        )
                    ));
                }
            }
            
            
            

            hospedagemHospedeRepository.save(hospede);

            // ✅ NOVA QUANTIDADE DE HÓSPEDES
            int novaQuantidade = reserva.getQuantidadeHospede() + 1;

            // ✅ BUSCAR DIÁRIA PARA A NOVA QUANTIDADE
            Apartamento apartamento = reserva.getApartamento();
            TipoApartamento tipoApartamento = apartamento.getTipoApartamento();

            Optional<Diaria> novaDiariaOpt = diariaService.buscarDiariaPara(apartamento, novaQuantidade);
            // ✅ CALCULAR DIFERENÇA DE VALOR
            BigDecimal valorDiariaAtual = reserva.getDiaria().getValor();
            BigDecimal valorDiariaNova;

            if (novaDiariaOpt.isPresent()) {
                valorDiariaNova = novaDiariaOpt.get().getValor();
                reserva.setDiaria(novaDiariaOpt.get());
                System.out.println("✅ Diária atualizada para " + novaQuantidade + 
                    " hóspedes: R$ " + valorDiariaNova);
            } else {
                valorDiariaNova = valorDiariaAtual;
                System.out.println("⚠️ Sem diária específica para " + novaQuantidade + 
                    " hóspedes — mantendo R$ " + valorDiariaAtual);
            }

            BigDecimal diferencaPorDia = valorDiariaNova.subtract(valorDiariaAtual);

            // ✅ CALCULAR DIAS RESTANTES
            long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDateTime.now().toLocalDate(),
                reserva.getDataCheckout().toLocalDate()
            );
            if (diasRestantes < 1) diasRestantes = 1;

            BigDecimal valorExtra = diferencaPorDia.multiply(BigDecimal.valueOf(diasRestantes));

            System.out.println("💰 Diferença por dia: R$ " + diferencaPorDia);
            System.out.println("📅 Dias restantes: " + diasRestantes);
            System.out.println("💵 Valor extra total: R$ " + valorExtra);

            // ✅ SÓ LANÇA SE TIVER DIFERENÇA
            if (valorExtra.compareTo(BigDecimal.ZERO) > 0) {
                ExtratoReserva lancamento = new ExtratoReserva();
                lancamento.setReserva(reserva);
                lancamento.setDescricao("Acréscimo de hóspede: " + novaQuantidade +
                    " hóspedes × " + diasRestantes + " diária(s)" +
                    " (diferença R$ " + diferencaPorDia.setScale(2) + "/dia)");
                lancamento.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.DIARIA);
                lancamento.setQuantidade((int) diasRestantes);
                lancamento.setValorUnitario(diferencaPorDia);
                lancamento.setTotalLancamento(valorExtra);
                lancamento.setDataHoraLancamento(LocalDateTime.now());
                extratoReservaRepository.save(lancamento);

                // ✅ ATUALIZAR TOTAIS DA RESERVA
                reserva.setTotalDiaria(reserva.getTotalDiaria().add(valorExtra));
                reserva.setTotalHospedagem(reserva.getTotalHospedagem().add(valorExtra));
                reserva.setTotalApagar(reserva.getTotalApagar().add(valorExtra));

                System.out.println("✅ Extrato lançado e totais atualizados");
            } else {
                System.out.println("ℹ️ Sem diferença de valor — nenhum lançamento gerado");
            }

            // ✅ ATUALIZAR QUANTIDADE E SALVAR
            reserva.setQuantidadeHospede(novaQuantidade);
            reservaRepository.save(reserva);

            System.out.println("✅ Hóspede adicionado na reserva #" + id +
                " | Qtd: " + novaQuantidade + " | +R$ " + valorExtra);

            return ResponseEntity.ok(Map.of(
                "mensagem", "Hóspede adicionado com sucesso",
                "novaQuantidadeHospedes", novaQuantidade,
                "diasCobrados", diasRestantes,
                "valorCobrado", valorExtra
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/hospedes")
    public ResponseEntity<?> listarHospedes(@PathVariable Long id) {
        try {
            List<HospedagemHospede> hospedes = hospedagemHospedeRepository.findByReservaId(id);
            return ResponseEntity.ok(hospedes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
     
    @PatchMapping("/hospedes/{hospedeId}/atualizar-placa")
    public ResponseEntity<?> atualizarPlacaHospede(
            @PathVariable Long hospedeId,
            @RequestParam String placa) {
        try {
            HospedagemHospede hospede = hospedagemHospedeRepository.findById(hospedeId)
                .orElseThrow(() -> new RuntimeException("Hóspede não encontrado"));
            
            hospede.setPlacaCarro(placa.toUpperCase());
            hospedagemHospedeRepository.save(hospede);
            
            return ResponseEntity.ok(Map.of("mensagem", "Placa atualizada", "placa", placa.toUpperCase()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/checkout-parcial")
    public ResponseEntity<?> checkoutParcial(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Long hospedagemHospedeId = Long.parseLong(body.get("hospedagemHospedeId").toString());
            String motivo = body.containsKey("motivo") ? body.get("motivo").toString() : "Checkout parcial";

            HospedagemHospede hospede = hospedagemHospedeRepository.findById(hospedagemHospedeId)
                .orElseThrow(() -> new RuntimeException("Hóspede não encontrado"));

            Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

            LocalDateTime agora = LocalDateTime.now();
            int horaAtual = agora.getHour();

            // ✅ MARCAR CHECKOUT DO HÓSPEDE
            hospede.setStatus(HospedagemHospede.StatusEnum.CHECKOUT_REALIZADO);
            hospede.setDataHoraSaida(agora);
            hospedagemHospedeRepository.save(hospede);

            // ✅ GERAR BILHETES DO SORTEIO
            List<BilheteSorteio> bilhetes = sorteioService.gerarBilhetesCheckout(hospede);
            System.out.println("🎟️ Bilhetes gerados: " + bilhetes.size());

            // ✅ NOVA QUANTIDADE
            int novaQuantidade = reserva.getQuantidadeHospede() - 1;
            if (novaQuantidade < 1) novaQuantidade = 1;

            // ✅ PROMOVER PRÓXIMO TITULAR SE NECESSÁRIO
            if (hospede.isTitular()) {
                List<HospedagemHospede> hospedes = hospedagemHospedeRepository.findByReservaId(id);
                HospedagemHospede proximo = hospedes.stream()
                    .filter(h -> !h.getId().equals(hospedagemHospedeId))
                    .filter(h -> h.getStatus() == HospedagemHospede.StatusEnum.HOSPEDADO)
                    .findFirst()
                    .orElse(null);

                if (proximo != null) {
                    proximo.setTitular(true);
                    hospedagemHospedeRepository.save(proximo);
                    System.out.println("✅ Novo titular: " + proximo.getCliente().getNome());
                }
            }

            // ✅ BUSCAR DIÁRIA PARA A NOVA QUANTIDADE
            Apartamento apartamento = reserva.getApartamento();
            TipoApartamento tipoApartamento = apartamento.getTipoApartamento();
            BigDecimal valorDiariaAtual = reserva.getDiaria().getValor();
            BigDecimal valorDiariaNova;

            Optional<Diaria> novaDiariaOpt = diariaService.buscarDiariaPara(apartamento, novaQuantidade);

            if (novaDiariaOpt.isPresent()) {
                valorDiariaNova = novaDiariaOpt.get().getValor();
                reserva.setDiaria(novaDiariaOpt.get());
                System.out.println("✅ Nova diária para " + novaQuantidade + 
                    " hóspede(s): R$ " + valorDiariaNova);
            } else {
                valorDiariaNova = valorDiariaAtual;
                System.out.println("⚠️ Sem diária específica para " + novaQuantidade + 
                    " hóspede(s) — mantendo R$ " + valorDiariaAtual);
            }

            BigDecimal diferencaPorDia = valorDiariaNova.subtract(valorDiariaAtual);
            // diferencaPorDia será NEGATIVA quando nova diária é menor (desconto)

            // ✅ CALCULAR DIAS RESTANTES
            // Antes das 12h: o scheduler ainda não lançou hoje → inclui hoje no recálculo
            // Após as 12h: hoje já foi cobrado com a diária antiga → recalcula a partir de amanhã
            java.time.LocalDate inicioDiferenca;
            if (horaAtual < 12) {
                inicioDiferenca = agora.toLocalDate(); // inclui hoje
                System.out.println("⏰ Saída antes das 12h — recalcula a partir de HOJE");
            } else {
                inicioDiferenca = agora.toLocalDate().plusDays(1); // só a partir de amanhã
                System.out.println("⏰ Saída após as 12h — recalcula a partir de AMANHÃ");
            }

            long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(
                inicioDiferenca, reserva.getDataCheckout().toLocalDate()
            );

            System.out.println("📅 Dias a recalcular: " + diasRestantes);
            System.out.println("💰 Diferença por dia: R$ " + diferencaPorDia);

            // ✅ SÓ LANÇA SE HOUVER DIFERENÇA E DIAS RESTANTES
            if (diasRestantes > 0 && diferencaPorDia.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal valorAjuste = diferencaPorDia.multiply(BigDecimal.valueOf(diasRestantes));

                ExtratoReserva lancamento = new ExtratoReserva();
                lancamento.setReserva(reserva);
                lancamento.setDataHoraLancamento(agora);
                lancamento.setQuantidade((int) diasRestantes);
                lancamento.setValorUnitario(diferencaPorDia);
                lancamento.setTotalLancamento(valorAjuste);

                if (valorAjuste.compareTo(BigDecimal.ZERO) < 0) {
                    // ✅ DESCONTO — nova diária é mais barata
                    lancamento.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.ESTORNO);
                    lancamento.setDescricao("Ajuste checkout parcial: " + novaQuantidade +
                        " hóspede(s) × " + diasRestantes + " dia(s)" +
                        " (R$ " + diferencaPorDia.setScale(2) + "/dia)");
                } else {
                    // ✅ ACRÉSCIMO — nova diária é mais cara (raro, mas possível)
                    lancamento.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.DIARIA);
                    lancamento.setDescricao("Ajuste checkout parcial: " + novaQuantidade +
                        " hóspede(s) × " + diasRestantes + " dia(s)" +
                        " (R$ " + diferencaPorDia.setScale(2) + "/dia)");
                }

                extratoReservaRepository.save(lancamento);

                // ✅ ATUALIZAR TOTAIS DA RESERVA
                reserva.setTotalDiaria(reserva.getTotalDiaria().add(valorAjuste));
                reserva.setTotalHospedagem(reserva.getTotalHospedagem().add(valorAjuste));
                reserva.setTotalApagar(reserva.getTotalApagar().add(valorAjuste));

                System.out.println("✅ Ajuste lançado: R$ " + valorAjuste);
            } else {
                System.out.println("ℹ️ Sem ajuste necessário" + 
                    (diasRestantes == 0 ? " (último dia)" : " (mesma diária)"));
            }

            // ✅ ATUALIZAR QUANTIDADE E SALVAR
            reserva.setQuantidadeHospede(novaQuantidade);
            reservaRepository.save(reserva);

            System.out.println("✅ Checkout parcial concluído | Reserva #" + id +
                " | Saiu: " + hospede.getCliente().getNome() +
                " | Nova qtd: " + novaQuantidade +
                " | Hora: " + horaAtual + "h");

            return ResponseEntity.ok(Map.of(
            	    "mensagem", "Checkout parcial realizado com sucesso",
            	    "novaQuantidadeHospedes", novaQuantidade,
            	    "diasRecalculados", diasRestantes,
            	    "diferencaPorDia", diferencaPorDia,
            	    "bilhetesGerados", bilhetes.stream()
            	        .map(b -> Map.of(
            	            "numeroBilhete", b.getNumeroBilhete(),
            	            "nomeHospede", b.getHospedagemHospede().getCliente().getNome()
            	        ))
            	        .collect(java.util.stream.Collectors.toList())
            	));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/finalizar-paga")
    public ResponseEntity<?> finalizarReservaPaga(@PathVariable Long id) {
        try {
            Reserva reserva = reservaService.finalizarReservaPaga(id);
            return ResponseEntity.ok(Map.of("mensagem", "Reserva finalizada com sucesso"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
 // SALVAR
    @PostMapping("/{id}/assinatura")
    public ResponseEntity<?> salvarAssinatura(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Reserva reserva = reservaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        reserva.setAssinaturaBase64(body.get("assinatura"));
        reservaRepository.save(reserva);
        return ResponseEntity.ok().build();
    }

    // BUSCAR
    @GetMapping("/{id}/assinatura")
    public ResponseEntity<?> buscarAssinatura(@PathVariable Long id) {
        Reserva reserva = reservaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        if (reserva.getAssinaturaBase64() == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(Map.of("assinatura", reserva.getAssinaturaBase64()));
    }
    
    @GetMapping("/apartamentos-disponiveis-para-transferencia")
    public ResponseEntity<?> buscarApartamentosDisponiveisParaTransferencia(
            @RequestParam Long apartamentoOrigemId) {
        try {
            List<Apartamento> todos = apartamentoRepository.findAll();
            List<Map<String, Object>> resultado = new ArrayList<>();

            for (Apartamento apt : todos) {
                // Excluir o apartamento de origem
                if (apt.getId().equals(apartamentoOrigemId)) continue;

                // Buscar reserva ativa neste apartamento
                List<Reserva> reservasAtivas = reservaRepository
                    .findByApartamentoIdAndStatus(apt.getId(), Reserva.StatusReservaEnum.ATIVA);

                Map<String, Object> item = new HashMap<>();
                item.put("id", apt.getId());
                item.put("numeroApartamento", apt.getNumeroApartamento());
                item.put("tipoApartamentoNome", apt.getTipoApartamento() != null
                	    ? apt.getTipoApartamento().getTipo().name() : "");
                item.put("capacidade", apt.getCapacidade());

                if (reservasAtivas.isEmpty()) {
                    item.put("status", "DISPONIVEL");
                    item.put("observacao", "Disponível - " + apt.getCapacidade() + " vagas");
                } else {
                    Reserva reservaAtiva = reservasAtivas.get(0);
                    int hospedes = reservaAtiva.getQuantidadeHospede();
                    int vagas = apt.getCapacidade() - hospedes;
                    if (vagas <= 0) continue; // apartamento lotado, pular
                    item.put("status", "OCUPADO");
                    item.put("observacao", "Ocupado - " + hospedes + "/" + apt.getCapacidade()
                        + " hóspedes - " + vagas + " vaga(s)");
                }

                resultado.add(item);
            }

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/transferir-hospede")
    public ResponseEntity<?> transferirHospede(@RequestBody Map<String, Object> body) {
        try {
            Long hospedagemHospedeId = Long.parseLong(body.get("hospedagemHospedeId").toString());
            Long novoApartamentoId   = Long.parseLong(body.get("novoApartamentoId").toString());
            String motivo            = body.get("motivo").toString();

            Map<String, Object> resultado = reservaService.transferirHospede(
                hospedagemHospedeId, novoApartamentoId, motivo);

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/ativar-pre-reserva")
    public ResponseEntity<?> ativarPreReserva(@PathVariable Long id) {
        try {
            Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

            if (reserva.getStatus() != Reserva.StatusReservaEnum.PRE_RESERVA) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Reserva não está como PRÉ-RESERVA. Status atual: " + reserva.getStatus()));
            }

            // ✅ VERIFICAR SE APARTAMENTO ESTÁ EM LIMPEZA
            if (reserva.getApartamento().getStatus() == Apartamento.StatusEnum.LIMPEZA) {
                return ResponseEntity.badRequest().body(Map.of(
                    "erro", String.format(
                        "Apartamento %s está em LIMPEZA. Libere o apartamento antes de ativar a reserva.",
                        reserva.getApartamento().getNumeroApartamento()
                    )
                ));
            }

            // ✅ VERIFICAR SE JÁ EXISTE RESERVA ATIVA NO MESMO APARTAMENTO COM CONFLITO DE DATAS
            List<Reserva> ativas = reservaRepository.findByApartamentoIdAndStatusIn(
                reserva.getApartamento().getId(),
                List.of(Reserva.StatusReservaEnum.ATIVA)
            );

            for (Reserva ativa : ativas) {
                boolean semConflito = !reserva.getDataCheckin().toLocalDate()
                                        .isBefore(ativa.getDataCheckout().toLocalDate())
                                   || !reserva.getDataCheckout().toLocalDate()
                                        .isAfter(ativa.getDataCheckin().toLocalDate());

                if (!semConflito) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "erro", String.format(
                            "Apartamento %s já possui reserva ATIVA #%d (%s) no período %s a %s.",
                            reserva.getApartamento().getNumeroApartamento(),
                            ativa.getId(),
                            ativa.getCliente().getNome(),
                            ativa.getDataCheckin().toLocalDate(),
                            ativa.getDataCheckout().toLocalDate()
                        )
                    ));
                }
            }

            // ✅ VERIFICAR SE CLIENTE JÁ ESTÁ HOSPEDADO EM OUTRO APARTAMENTO
            List<HospedagemHospede> hospedesAtivos = hospedagemHospedeRepository
                .findByClienteIdAndStatus(reserva.getCliente().getId(),
                    HospedagemHospede.StatusEnum.HOSPEDADO);

            for (HospedagemHospede hAtivo : hospedesAtivos) {
                Reserva rAtiva = hAtivo.getReserva();
                if (rAtiva == null) continue;
                if (rAtiva.getStatus() != Reserva.StatusReservaEnum.ATIVA) continue;

                boolean conflito =
                    reserva.getDataCheckin().isBefore(rAtiva.getDataCheckout())
                    && reserva.getDataCheckout().isAfter(rAtiva.getDataCheckin());

                if (conflito) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "erro", String.format(
                            "%s já está hospedado no apartamento %s (Reserva #%d) de %s a %s.",
                            reserva.getCliente().getNome(),
                            rAtiva.getApartamento().getNumeroApartamento(),
                            rAtiva.getId(),
                            rAtiva.getDataCheckin().toLocalDate(),
                            rAtiva.getDataCheckout().toLocalDate()
                        )
                    ));
                }
            }

            // ✅ ATIVAR RESERVA
            reserva.setStatus(Reserva.StatusReservaEnum.ATIVA);
            reserva.setDataCheckin(reserva.getDataCheckin().toLocalDate()
            	    .atTime(LocalDateTime.now().toLocalTime()));
            LocalDateTime checkoutPadronizado = reserva.getDataCheckout().toLocalDate().atTime(12, 0);
            reserva.setDataCheckout(checkoutPadronizado);
            reservaRepository.save(reserva);

            // ✅ ATUALIZAR APARTAMENTO PARA OCUPADO
            Apartamento apartamento = reserva.getApartamento();
            apartamento.setStatus(Apartamento.StatusEnum.OCUPADO);
            apartamentoRepository.save(apartamento);

            // ✅ ADICIONAR TITULAR EM HOSPEDAGEM_HOSPEDES AO ATIVAR
            HospedagemHospede hospedeTitular = new HospedagemHospede();
            hospedeTitular.setReserva(reserva);
            hospedeTitular.setCliente(reserva.getCliente());
            hospedeTitular.setTitular(true);
            hospedeTitular.setStatus(HospedagemHospede.StatusEnum.HOSPEDADO);
            hospedeTitular.setDataHoraEntrada(LocalDateTime.now());
            hospedagemHospedeRepository.save(hospedeTitular);
            System.out.println("✅ Titular adicionado ao ativar pré-reserva: " + reserva.getCliente().getNome());

            return ResponseEntity.ok(Map.of(
                "mensagem", "Pré-reserva #" + id + " ativada com sucesso!",
                "status", "ATIVA"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/responsavel-pagamento")
    public ResponseEntity<?> definirResponsavelPagamento(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

            if (body.get("responsavelId") != null) {
                Long responsavelId = Long.parseLong(body.get("responsavelId").toString());
                Cliente responsavel = clienteRepository.findById(responsavelId)
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
                reserva.setResponsavelPagamento(responsavel);
            } else {
                reserva.setResponsavelPagamento(null);
            }

            if (body.get("numeroApartamento") != null) {
                reserva.setNumeroApartamentoResponsavel(body.get("numeroApartamento").toString());
            } else {
                reserva.setNumeroApartamentoResponsavel(null);
            }

            reservaRepository.save(reserva);

            return ResponseEntity.ok(Map.of(
                "mensagem", "Responsável pelo pagamento definido com sucesso"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/buscar-por-placa/{placa}")
    public ResponseEntity<?> buscarPorPlaca(@PathVariable String placa) {
        try {
            List<HospedagemHospede> hospedes = hospedagemHospedeRepository
                .findByPlacaCarroIgnoreCaseAndStatus(placa, HospedagemHospede.StatusEnum.HOSPEDADO);

            if (hospedes.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "encontrado", false,
                    "mensagem", "Nenhum hóspede encontrado com a placa: " + placa
                ));
            }

            List<Map<String, Object>> hospedagens = hospedes.stream().map(h -> {
                Map<String, Object> map = new HashMap<>();
                map.put("hospedeId", h.getId());
                map.put("hospedeNome", h.getCliente() != null ? h.getCliente().getNome() : h.getNomeCompleto());
                map.put("placaCarro", h.getPlacaCarro());
                map.put("reservaId", h.getReserva().getId());
                map.put("apartamento", h.getReserva().getApartamento().getNumeroApartamento());
                map.put("dataCheckin", h.getReserva().getDataCheckin());
                map.put("dataCheckout", h.getReserva().getDataCheckout());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "encontrado", true,
                "quantidade", hospedagens.size(),
                "hospedagens", hospedagens
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/ativas/buscar")
    public ResponseEntity<List<Reserva>> buscarAtivasPorTermo(@RequestParam String termo) {
        List<Reserva> reservas = reservaService.buscarAtivas().stream()
            .filter(r -> r.getStatus() == Reserva.StatusReservaEnum.ATIVA)
            .filter(r -> r.getCliente().getNome().toLowerCase().contains(termo.toLowerCase())
                    || r.getApartamento().getNumeroApartamento().toLowerCase().contains(termo.toLowerCase()))
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(reservas);
    }
    
    @PatchMapping("/{id}/observacao")
    public ResponseEntity<?> salvarObservacao(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
            reserva.setObservacoes(body.get("observacoes"));
            reservaRepository.save(reserva);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/bilhetes-sorteio")
    public ResponseEntity<?> getBilhetesSorteio(@PathVariable Long id) {
        try {
            Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
            
            List<HospedagemHospede> hospedes = hospedagemHospedeRepository.findByReservaId(id);
            List<Map<String, Object>> bilhetes = new ArrayList<>();
            
            for (HospedagemHospede h : hospedes) {
                List<BilheteSorteio> bs = bilheteSorteioRepository.findByHospedagemHospedeId(h.getId());
                for (BilheteSorteio b : bs) {
                    bilhetes.add(Map.of(
                        "numeroBilhete", b.getNumeroBilhete(),
                        "nomeHospede", h.getCliente().getNome(),
                        "apartamento", reserva.getApartamento().getNumeroApartamento(),
                        "dataCheckin", reserva.getDataCheckin().toString(),
                        "dataCheckout", reserva.getDataCheckout().toString(),
                        "dataEmissao", b.getDataEmissao().toString()
                    ));
                }
            }
            return ResponseEntity.ok(bilhetes);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @PostMapping("/{id}/registrar-recibo")
    public ResponseEntity<?> registrarRecibo(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

            BigDecimal valorRecibo = new BigDecimal(body.get("valorRecibo").toString());
            BigDecimal totalReciboEmitido = reserva.getTotalReciboEmitido() != null 
                ? reserva.getTotalReciboEmitido() : BigDecimal.ZERO;
            BigDecimal totalRecebido = reserva.getTotalRecebido() != null 
                ? reserva.getTotalRecebido() : BigDecimal.ZERO;

            BigDecimal saldoDisponivel = totalRecebido.subtract(totalReciboEmitido);

            if (valorRecibo.compareTo(saldoDisponivel) > 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "erro", "Valor do recibo excede o saldo disponível para recibo. " +
                            "Já recibado: R$ " + totalReciboEmitido + 
                            " | Disponível: R$ " + saldoDisponivel
                ));
            }

            reserva.setTotalReciboEmitido(totalReciboEmitido.add(valorRecibo));
            reservaRepository.save(reserva);

            return ResponseEntity.ok(Map.of(
                "mensagem", "Recibo registrado",
                "totalReciboEmitido", reserva.getTotalReciboEmitido(),
                "saldoDisponivel", totalRecebido.subtract(reserva.getTotalReciboEmitido())
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/devolver-troco")
    public ResponseEntity<?> devolverTroco(@PathVariable Long id) {
        try {
            Reserva reserva = reservaService.devolverTroco(id);
            return ResponseEntity.ok(Map.of(
                "mensagem", "Crédito devolvido com sucesso",
                "reservaId", reserva.getId(),
                "totalApagar", reserva.getTotalApagar()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
        
}
