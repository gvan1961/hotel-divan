package com.divan.controller;

import com.divan.dto.ApartamentoRequestDTO;
import com.divan.entity.HistoricoApartamento;
import com.divan.entity.HospedagemHospede;
import com.divan.entity.LogAuditoria;
import com.divan.dto.ApartamentoResponseDTO;
import com.divan.entity.Apartamento;
import com.divan.util.DataUtil;
import java.util.stream.Collectors;
import com.divan.entity.Reserva;
import com.divan.repository.HistoricoApartamentoRepository;
import com.divan.repository.HospedagemHospedeRepository;
import com.divan.repository.LogAuditoriaRepository;
import com.divan.repository.ReservaRepository;
import com.divan.repository.UsuarioRepository;
import com.divan.service.ApartamentoService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/apartamentos")
@CrossOrigin(origins = "*")
public class ApartamentoController {
    
    @Autowired
    private ApartamentoService apartamentoService;
    
    @Autowired
    private ReservaRepository reservaRepository;    
        
    @Autowired
    private HistoricoApartamentoRepository historicoApartamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private HospedagemHospedeRepository hospedagemHospedeRepository;
    
    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;
    
    // ✅ ATUALIZADO - Usar DTO
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody ApartamentoRequestDTO dto) {
        try {
            System.out.println("🔵 POST /api/apartamentos - DTO recebido: " + dto);
            ApartamentoResponseDTO apartamento = apartamentoService.criarComDTO(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(apartamento);
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar apartamento: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // ✅ ATUALIZADO - Retornar DTOs
    @GetMapping
    public ResponseEntity<List<ApartamentoResponseDTO>> listarTodos() {
        List<ApartamentoResponseDTO> apartamentos = apartamentoService.listarTodosDTO();
        return ResponseEntity.ok(apartamentos);
    }
    
    // ✅ ATUALIZADO - Retornar DTO
    @GetMapping("/{id}")
    public ResponseEntity<ApartamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        try {
            ApartamentoResponseDTO apartamento = apartamentoService.buscarPorIdDTO(id);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/numero/{numero}")
    public ResponseEntity<Apartamento> buscarPorNumero(@PathVariable String numero) {
        Optional<Apartamento> apartamento = apartamentoService.buscarPorNumero(numero);
        return apartamento.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/disponiveis")
    public ResponseEntity<List<Apartamento>> buscarDisponiveis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {

        if (dataInicio != null && dataFim != null) {
            List<Apartamento> apartamentos = apartamentoService.buscarDisponiveisParaPeriodo(dataInicio, dataFim);
            return ResponseEntity.ok(apartamentos);
        }

        List<Apartamento> apartamentos = apartamentoService.buscarDisponiveis();
        return ResponseEntity.ok(apartamentos);
    }
           
        
    @GetMapping("/ocupados")
    public ResponseEntity<List<Apartamento>> buscarOcupados() {
        List<Apartamento> apartamentos = apartamentoService.buscarOcupados();
        return ResponseEntity.ok(apartamentos);
    }
    
    @GetMapping("/disponiveis-periodo")
    public ResponseEntity<List<Apartamento>> buscarDisponiveisParaPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkout) {
        List<Apartamento> apartamentos = apartamentoService.buscarDisponiveisParaPeriodo(checkin, checkout);
        return ResponseEntity.ok(apartamentos);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Apartamento>> buscarPorStatus(@PathVariable Apartamento.StatusEnum status) {
        List<Apartamento> apartamentos = apartamentoService.buscarPorStatus(status);
        return ResponseEntity.ok(apartamentos);
    }
    
    // ✅ ATUALIZADO - Usar DTO
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody ApartamentoRequestDTO dto) {
        try {
            System.out.println("🔵 PUT /api/apartamentos/" + id + " - DTO recebido: " + dto);
            ApartamentoResponseDTO apartamento = apartamentoService.atualizarComDTO(id, dto);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            System.err.println("❌ Erro ao atualizar apartamento: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Apartamento> atualizarStatus(@PathVariable Long id, @RequestParam Apartamento.StatusEnum status) {
        try {
            Apartamento apartamento = apartamentoService.atualizarStatus(id, status);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}/liberar-limpeza")
    public ResponseEntity<?> liberarLimpeza(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            Apartamento apartamento = apartamentoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));

            if (apartamento.getStatus() != Apartamento.StatusEnum.LIMPEZA) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Apartamento não está em limpeza. Status atual: "
                        + apartamento.getStatus()));
            }
            
         // ✅ VERIFICAR SE EXISTE RESERVA ATIVA NO APARTAMENTO
            List<Reserva> reservasAtivas = reservaRepository.findByApartamentoIdAndStatusIn(
                apartamento.getId(),
                List.of(Reserva.StatusReservaEnum.ATIVA)
            );

            if (!reservasAtivas.isEmpty()) {
                Reserva reservaAtiva = reservasAtivas.get(0);
                return ResponseEntity.badRequest().body(Map.of(
                    "erro", String.format(
                        "Apartamento %s possui reserva ATIVA #%d (%s). Faça o checkout antes de liberar.",
                        apartamento.getNumeroApartamento(),
                        reservaAtiva.getId(),
                        reservaAtiva.getCliente().getNome()
                    )
                ));
            }

            String statusAnterior = apartamento.getStatus().name();

            // ✅ LIBERAR
            apartamento.setStatus(Apartamento.StatusEnum.DISPONIVEL);
            apartamentoService.salvar(apartamento);

            // ✅ REGISTRAR HISTÓRICO
            HistoricoApartamento historico = new HistoricoApartamento();
            historico.setApartamento(apartamento);
            historico.setAcao("LIBERADO_LIMPEZA");
            historico.setStatusAnterior(statusAnterior);
            historico.setStatusNovo("DISPONIVEL");
            historico.setDataHora(LocalDateTime.now());

            if (body != null && body.get("usuarioId") != null) {
                Long usuarioId = Long.parseLong(body.get("usuarioId").toString());
                usuarioRepository.findById(usuarioId)
                    .ifPresent(historico::setUsuario);
            }

            if (body != null && body.get("motivo") != null) {
                historico.setMotivo(body.get("motivo").toString());
            } else {
                historico.setMotivo("Liberação via Painel de Recepção");
            }

            historicoApartamentoRepository.save(historico);
            
         // ✅ REGISTRAR LOG DE AUDITORIA
            LogAuditoria log = new LogAuditoria();
            log.setAcao("LIBERAR_LIMPEZA");
            log.setDescricao("Apartamento " + apartamento.getNumeroApartamento() + " liberado da limpeza");
            log.setDataHora(LocalDateTime.now());

            if (historico.getUsuario() != null) {
                log.setUsuario(historico.getUsuario());
            }

            logAuditoriaRepository.save(log);
            

            System.out.println("✅ Apt " + apartamento.getNumeroApartamento()
                + " liberado da limpeza por usuário "
                + (historico.getUsuario() != null ? historico.getUsuario().getNome() : "desconhecido"));

            return ResponseEntity.ok(Map.of(
                "mensagem", "Apartamento liberado com sucesso",
                "apartamento", apartamento.getNumeroApartamento()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/manutencao")
    public ResponseEntity<?> colocarEmManutencao(
        @PathVariable Long id,
        @RequestParam(required = false) String motivo
    ) {
        try {
            Apartamento apartamento = apartamentoService.colocarEmManutencao(id, motivo);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/liberar-manutencao")
    public ResponseEntity<?> liberarManutencao(@PathVariable Long id) {
        try {
            Apartamento apartamento = apartamentoService.liberarManutencao(id);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/bloquear")
    public ResponseEntity<?> bloquear(
        @PathVariable Long id,
        @RequestParam(required = false) String motivo
    ) {
        try {
            Apartamento apartamento = apartamentoService.bloquear(id, motivo);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/desbloquear")
    public ResponseEntity<?> desbloquear(@PathVariable Long id) {
        try {
            Apartamento apartamento = apartamentoService.desbloquear(id);
            return ResponseEntity.ok(apartamento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/painel")
    public ResponseEntity<?> getPainelApartamentos(@RequestParam(required = false) String camas,
            @RequestParam(required = false) String tv) {
        try {
            List<Apartamento> apartamentos = apartamentoService.listarTodos();
            apartamentos.sort(Comparator.comparing(Apartamento::getNumeroApartamento));

            if (camas != null && !camas.isBlank()) {
                apartamentos = apartamentos.stream()
                    .filter(a -> a.getCamasDoApartamento() != null &&
                        a.getCamasDoApartamento().toLowerCase().contains(camas.toLowerCase()))
                    .collect(Collectors.toList());
            }

            if (tv != null && !tv.isBlank()) {
                apartamentos = apartamentos.stream()
                    .filter(a -> a.getTv() != null &&
                        a.getTv().toLowerCase().contains(tv.toLowerCase()))
                    .collect(Collectors.toList());
            }

            LocalDate hoje = DataUtil.hoje();
            List<Map<String, Object>> cards = new ArrayList<>();

            int cntDisponivel = 0, cntOcupado = 0, cntLimpeza = 0,
            	    cntBloqueado = 0, cntManutencao = 0, cntPreReserva = 0,
            	    cntEntraHoje = 0, cntSaiHoje = 0, cntAtrasado = 0;
            int cntHospedesOcupados = 0;

            for (Apartamento apt : apartamentos) {
                Map<String, Object> card = new LinkedHashMap<>();
                card.put("id",        apt.getId());
                card.put("numero",    apt.getNumeroApartamento());
                card.put("capacidade",apt.getCapacidade());
                card.put("statusApt", apt.getStatus() != null ? apt.getStatus().name() : "DISPONIVEL");
                card.put("descricao", apt.getTipoApartamento() != null
                    ? apt.getTipoApartamento().getDescricao() : "");
                card.put("tipo",      apt.getTipoApartamento() != null
                    ? apt.getTipoApartamento().getTipo().name() : "");
                card.put("camas",     apt.getCamasDoApartamento());
                card.put("tv",        apt.getTv());
                
             // ✅ SE APARTAMENTO ESTÁ EM LIMPEZA, MANUTENÇÃO OU BLOQUEADO
             // NÃO mostrar reserva — status físico tem prioridade
             String statusFisico = apt.getStatus() != null ? apt.getStatus().name() : "DISPONIVEL";
             if (statusFisico.equals("LIMPEZA") || 
                 statusFisico.equals("MANUTENCAO") || 
                 statusFisico.equals("INDISPONIVEL")) {
                 card.put("reserva", null);
                 switch (statusFisico) {
                 case "LIMPEZA"      -> cntLimpeza++;
                 case "MANUTENCAO"   -> cntManutencao++;
                 case "INDISPONIVEL" -> cntBloqueado++;
             }
                 cards.add(card);
                 continue; // ✅ PULAR busca de reservas
             }

                
                
                // ✅ TODAS as pré-reservas (para contar)
                List<Reserva> todasPreReservas = reservaRepository
                    .findByApartamentoIdAndStatusIn(apt.getId(),
                        List.of(Reserva.StatusReservaEnum.PRE_RESERVA));
                cntPreReserva += todasPreReservas.size();
                card.put("temPreReservaFutura", !todasPreReservas.isEmpty());

                // ✅ Apenas para exibir no grid (ATIVA + PRE_RESERVA de hoje)
                List<Reserva> reservasAtivas = reservaRepository
                    .findByApartamentoIdAndStatusIn(apt.getId(),
                        List.of(Reserva.StatusReservaEnum.ATIVA,
                                Reserva.StatusReservaEnum.PRE_RESERVA))
                    .stream()
                    .filter(r -> {
                        if (r.getStatus() == Reserva.StatusReservaEnum.ATIVA) return true;
                        return !r.getDataCheckin().toLocalDate().isAfter(hoje);
                    })
                    .collect(Collectors.toList());

                if (!reservasAtivas.isEmpty()) {
                    Reserva r = reservasAtivas.get(0);
                    LocalDate checkin  = r.getDataCheckin().toLocalDate();
                    LocalDate checkout = r.getDataCheckout().toLocalDate();
                    boolean saiHoje   = checkout.isEqual(hoje);
                    boolean entraHoje = checkin.isEqual(hoje) 
                    	    && r.getStatus() == Reserva.StatusReservaEnum.PRE_RESERVA;
                    boolean atrasado  = checkout.isBefore(hoje)
                                        && r.getStatus() == Reserva.StatusReservaEnum.ATIVA;

                    Map<String, Object> res = new LinkedHashMap<>();
                    res.put("id",               r.getId());
                    res.put("status",           r.getStatus().name());
                    res.put("clienteNome",      r.getCliente() != null ? r.getCliente().getNome() : "-");
                    res.put("dataCheckin",      checkin.toString());
                    res.put("dataCheckout",     checkout.toString());
                    res.put("quantidadeHospedes", r.getQuantidadeHospede());
                    res.put("saiHoje",          saiHoje);
                    res.put("entraHoje",        entraHoje);
                    res.put("atrasado",         atrasado);
                    res.put("renovacaoAutomatica", r.getRenovacaoAutomatica() != null && r.getRenovacaoAutomatica());
                  
                    // ✅ PRÓXIMA RESERVA (se ATIVA e tem pré-reserva futura)
                    if (r.getStatus() == Reserva.StatusReservaEnum.ATIVA
                            && !todasPreReservas.isEmpty()) {
                        Reserva proxima = todasPreReservas.stream()
                            .filter(p -> p.getDataCheckin().toLocalDate().isAfter(hoje))
                            .min(Comparator.comparing(Reserva::getDataCheckin))
                            .orElse(null);

                        if (proxima != null) {
                            Map<String, Object> proximaMap = new LinkedHashMap<>();
                            proximaMap.put("id",          proxima.getId());
                            proximaMap.put("clienteNome", proxima.getCliente() != null
                                ? proxima.getCliente().getNome() : "-");
                            proximaMap.put("dataCheckin",
                                proxima.getDataCheckin().toLocalDate().toString());
                            proximaMap.put("dataCheckout",
                                proxima.getDataCheckout().toLocalDate().toString());
                            res.put("proximaReserva", proximaMap);
                        }
                    }

                    card.put("reserva", res);

                    if (entraHoje) cntEntraHoje++;
                    if (saiHoje)   cntSaiHoje++;
                    if (atrasado)  cntAtrasado++;
                    if (r.getStatus() != Reserva.StatusReservaEnum.PRE_RESERVA) {
                        cntOcupado++;
                        cntHospedesOcupados += r.getQuantidadeHospede();
                    }
                } else {
                    card.put("reserva", null);
                    String statusApt = apt.getStatus() != null
                        ? apt.getStatus().name() : "DISPONIVEL";
                    switch (statusApt) {
                    case "LIMPEZA"      -> cntLimpeza++;
                    case "MANUTENCAO"   -> cntManutencao++;
                    case "INDISPONIVEL" -> cntBloqueado++;
                    default             -> cntDisponivel++;
                }
                }
                cards.add(card);
            }

            Map<String, Object> contadores = new LinkedHashMap<>();
            contadores.put("total",            apartamentos.size());
            contadores.put("disponivel",       cntDisponivel);
            contadores.put("ocupado",          cntOcupado);
            contadores.put("hospedesOcupados", cntHospedesOcupados);
            contadores.put("limpeza",          cntLimpeza);
            contadores.put("bloqueado",        cntBloqueado);
            contadores.put("manutencao",       cntManutencao);
            contadores.put("preReserva",       cntPreReserva);
            contadores.put("entraHoje",        cntEntraHoje);
            contadores.put("saiHoje",          cntSaiHoje);
            contadores.put("atrasado",         cntAtrasado);

            return ResponseEntity.ok(Map.of("apartamentos", cards, "contadores", contadores));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/painel/buscar")
    public ResponseEntity<?> buscarNoPainel(
            @RequestParam(required = false) String hospede,
            @RequestParam(required = false) String placa) {
        try {
            if ((hospede == null || hospede.isBlank()) &&
                (placa == null || placa.isBlank())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Informe o nome do hóspede ou placa"));
            }

            List<HospedagemHospede> hospedes = hospedagemHospedeRepository
                .findByStatus(HospedagemHospede.StatusEnum.HOSPEDADO);

            List<Map<String, Object>> resultados = new ArrayList<>();

            for (HospedagemHospede h : hospedes) {
                boolean matchHospede = hospede != null && !hospede.isBlank()
                    && h.getCliente() != null
                    && h.getCliente().getNome().toLowerCase()
                        .contains(hospede.toLowerCase());

                boolean matchPlaca = placa != null && !placa.isBlank()
                    && h.getPlacaCarro() != null
                    && h.getPlacaCarro().toLowerCase()
                        .contains(placa.toLowerCase());

                if (matchHospede || matchPlaca) {
                    Reserva r = h.getReserva();
                    if (r == null || r.getStatus() != Reserva.StatusReservaEnum.ATIVA) continue;

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("hospedeNome",    h.getCliente() != null ? h.getCliente().getNome() : "-");
                    item.put("placaCarro",     h.getPlacaCarro());
                    item.put("titular",        h.isTitular());
                    item.put("reservaId",      r.getId());
                    item.put("dataCheckin",    r.getDataCheckin().toLocalDate().toString());
                    item.put("dataCheckout",   r.getDataCheckout().toLocalDate().toString());
                    item.put("apartamento",    r.getApartamento() != null
                        ? r.getApartamento().getNumeroApartamento() : "-");
                    item.put("apartamentoId",  r.getApartamento() != null
                        ? r.getApartamento().getId() : null);
                    resultados.add(item);
                }
            }

            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/verificar-checkout-vencido")
    public ResponseEntity<?> verificarCheckoutVencido(@PathVariable Long id) {
        try {
            List<Reserva> reservasAtivas = reservaRepository
                .findByApartamentoIdAndStatus(id, Reserva.StatusReservaEnum.ATIVA);
            
            LocalDateTime agora = LocalDateTime.now();
            
            for (Reserva r : reservasAtivas) {
                if (r.getDataCheckout().isBefore(agora)) {
                    return ResponseEntity.ok(Map.of(
                        "temCheckoutVencido", true,
                        "hospedeNome", r.getCliente().getNome(),
                        "checkoutPrevisto", r.getDataCheckout().toString(),
                        "horasAtraso", java.time.Duration.between(r.getDataCheckout(), agora).toHours()
                    ));
                }
            }
            
            return ResponseEntity.ok(Map.of("temCheckoutVencido", false));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
