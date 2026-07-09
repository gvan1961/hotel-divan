package com.divan.controller;

import com.divan.dto.ClienteDTO;
import com.divan.repository.ClienteRepository;
import com.divan.repository.HospedagemHospedeRepository;
import com.divan.dto.ClienteRequestDTO;
import com.divan.entity.Cliente;
import com.divan.entity.HospedagemHospede;
import com.divan.entity.Reserva;
import com.divan.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.divan.dto.ClienteResumoDTO;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {
    
    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private HospedagemHospedeRepository hospedagemHospedeRepository;
    
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody ClienteRequestDTO dto) {
        try {
        	System.out.println("📨 CPF recebido no controller: " + dto.getCpf());
            Cliente cliente = new Cliente();
            cliente.setNome(dto.getNome());
            cliente.setCpf(dto.getCpf());
            cliente.setCelular(dto.getCelular());
            cliente.setDdi(dto.getDdi() != null ? dto.getDdi() : "+55");
            cliente.setCelular2(dto.getCelular2());
            cliente.setDdi2(dto.getDdi2() != null ? dto.getDdi2() : "+55");
            cliente.setEndereco(dto.getEndereco());
            cliente.setCep(dto.getCep());
            cliente.setCidade(dto.getCidade());
            cliente.setEstado(dto.getEstado());
            cliente.setDataNascimento(dto.getDataNascimento());
            
            
            cliente.setMenorDeIdade(dto.getMenorDeIdade() != null ? dto.getMenorDeIdade() : false);
            cliente.setCreditoAprovado(dto.getCreditoAprovado() != null ? dto.getCreditoAprovado() : false);
            cliente.setAutorizadoJantar(dto.getAutorizadoJantar() != null ? dto.getAutorizadoJantar() : false);
            cliente.setClassificacao(dto.getClassificacao());
            cliente.setFumante(dto.getFumante() != null ? dto.getFumante() : false);
            System.out.println("🍽️ autorizadoJantar antes de salvar: " + cliente.getAutorizadoJantar());
            Cliente clienteSalvo = clienteService.salvar(cliente, dto.getEmpresaId(), dto.getResponsavelId());
            System.out.println("🍽️ autorizadoJantar após salvar: " + clienteSalvo.getAutorizadoJantar());

            return ResponseEntity.status(HttpStatus.CREATED).body(clienteSalvo);
            
            
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String empresa) {

        // Com filtros — busca no banco
        if ((nome != null && !nome.isBlank()) ||
            (cpf  != null && !cpf.isBlank())  ||
            (empresa != null && !empresa.isBlank())) {
            List<com.divan.entity.Cliente> todos = clienteRepository.findAll(
                org.springframework.data.domain.Sort.by("nome"));
            String nomeLower    = nome    != null ? nome.toLowerCase().trim()    : "";
            String cpfLimpo     = cpf     != null ? cpf.replaceAll("\\D", "")    : "";
            String empresaLower = empresa != null ? empresa.toLowerCase().trim()  : "";
            var filtrados = todos.stream()
                .filter(c -> nomeLower.isEmpty()    || (c.getNome()    != null && c.getNome().toLowerCase().contains(nomeLower)))
                .filter(c -> cpfLimpo.isEmpty()     || (c.getCpf()     != null && c.getCpf().replaceAll("\\D","").contains(cpfLimpo)))
                .filter(c -> empresaLower.isEmpty() || (c.getEmpresa() != null && c.getEmpresa().getNomeEmpresa() != null
                             && c.getEmpresa().getNomeEmpresa().toLowerCase().contains(empresaLower)))
                .map(this::converterParaResumo) // ← converte para DTO leve
                .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(Map.of(
                "clientes",       filtrados,
                "totalPaginas",   1,
                "totalElementos", filtrados.size(),
                "paginaAtual",    0
            ));
        }

        // Sem filtros — paginação normal
        var pagina = clienteRepository.findAll(
            org.springframework.data.domain.PageRequest.of(
                page, size,
                org.springframework.data.domain.Sort.by("nome")));
        var clientes = pagina.getContent().stream()
            .map(this::converterParaResumo) // ← converte para DTO leve
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(Map.of(
            "clientes",       clientes,
            "totalPaginas",   pagina.getTotalPages(),
            "totalElementos", pagina.getTotalElements(),
            "paginaAtual",    pagina.getNumber()
        ));
    }

    // ✅ Método de conversão para DTO leve
    private ClienteResumoDTO converterParaResumo(com.divan.entity.Cliente c) {
        return new ClienteResumoDTO(
            c.getId(),
            c.getNome(),
            c.getCpf(),
            c.getCelular(),
            c.getDdi(),
            c.getCelular2(),
            c.getDdi2(),
            c.getCelularCompleto(),
            c.getCelular2Completo(),
            c.getEmpresa() != null ? c.getEmpresa().getNomeEmpresa() : null,
            c.getEmpresa() != null ? c.getEmpresa().getId() : null,
            c.getTipoCliente() != null ? c.getTipoCliente() : null,
            c.getClassificacao(),
            c.getFumante(),
            c.getFaceAtivo(),
            c.getCreditoAprovado(),
            c.getAutorizadoJantar(),
            c.getMenorDeIdade(),
            c.getResponsavel() != null ? c.getResponsavel().getNome() : null
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> buscarPorId(@PathVariable Long id) {
        Optional<Cliente> cliente = clienteService.buscarPorId(id);
        return cliente.map(c -> ResponseEntity.ok(clienteService.converterParaDTO(c)))
                      .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/cpf/verificar")
    public ResponseEntity<?> verificarCpfDuplicado(
            @RequestParam String cpf,
            @RequestParam(required = false) Long id) {

        Optional<Cliente> existente = clienteRepository.findByCpf(cpf);

        boolean duplicado = existente.isPresent() &&
                            !existente.get().getId().equals(id);

        return ResponseEntity.ok(Map.of("duplicado", duplicado));
    }
    
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Cliente> buscarPorCpf(@PathVariable String cpf) {
        Optional<Cliente> cliente = clienteService.buscarPorCpf(cpf);
        return cliente.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }   
  
    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteResumoDTO>> buscar(@RequestParam String termo) {
        List<ClienteResumoDTO> clientes = clienteService.buscarPorTermo(termo);
        return ResponseEntity.ok(clientes);
    }
    
    @GetMapping("/hospedados/buscar")
    public ResponseEntity<List<Map<String, Object>>> buscarHospedados(@RequestParam String termo) {
        List<HospedagemHospede> hospedados = hospedagemHospedeRepository
            .findByStatus(HospedagemHospede.StatusEnum.HOSPEDADO);

        List<Map<String, Object>> resultado = hospedados.stream()
            .filter(h -> h.getCliente() != null &&
                    h.getReserva() != null &&
                    Reserva.StatusReservaEnum.ATIVA.equals(h.getReserva().getStatus()) &&
                    (h.getCliente().getNome().toLowerCase().contains(termo.toLowerCase()) ||
                    (h.getCliente().getCpf() != null && h.getCliente().getCpf().contains(termo))))
            .map(h -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", h.getCliente().getId());
                map.put("nome", h.getCliente().getNome());
                map.put("cpf", h.getCliente().getCpf());
                map.put("celular", h.getCliente().getCelular());
                map.put("reservaId", h.getReserva().getId());
                map.put("numeroApartamento", h.getReserva().getApartamento().getNumeroApartamento());
                return map;
            })
            .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(resultado);
    }
    
    @PatchMapping("/{id}/aprovar-credito")
    public ResponseEntity<Void> aprovarCredito(@PathVariable Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        cliente.setCreditoAprovado(true);
        clienteRepository.save(cliente);
        
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/revogar-credito")
    public ResponseEntity<Void> revogarCredito(@PathVariable Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        cliente.setCreditoAprovado(false);
        clienteRepository.save(cliente);
        
        return ResponseEntity.ok().build();
    }
    
    
    
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<Cliente>> buscarPorEmpresa(@PathVariable Long empresaId) {
        List<Cliente> clientes = clienteService.buscarPorEmpresa(empresaId);
        return ResponseEntity.ok(clientes);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO dto) {
        try {
            Cliente cliente = new Cliente();
            cliente.setNome(dto.getNome());
            cliente.setCpf(dto.getCpf());
            cliente.setCelular(dto.getCelular());
            cliente.setDdi(dto.getDdi() != null ? dto.getDdi() : "+55");
            cliente.setCelular2(dto.getCelular2());
            cliente.setDdi2(dto.getDdi2() != null ? dto.getDdi2() : "+55");
            cliente.setEndereco(dto.getEndereco());
            cliente.setCep(dto.getCep());
            cliente.setCidade(dto.getCidade());
            cliente.setEstado(dto.getEstado());
            cliente.setDataNascimento(dto.getDataNascimento());
            cliente.setMenorDeIdade(dto.getMenorDeIdade() != null ? dto.getMenorDeIdade() : false);

            if (dto.getCreditoAprovado() != null) {
                cliente.setCreditoAprovado(dto.getCreditoAprovado());
            }
            if (dto.getTipoCliente() != null) {
                cliente.setTipoCliente(dto.getTipoCliente());
            }
            if (dto.getAutorizadoJantar() != null) {
                cliente.setAutorizadoJantar(dto.getAutorizadoJantar());
            }
            cliente.setClassificacao(dto.getClassificacao());
            cliente.setFumante(dto.getFumante() != null ? dto.getFumante() : false);
            Cliente clienteAtualizado = clienteService.atualizar(id, cliente, dto.getEmpresaId(), dto.getResponsavelId());
            return ResponseEntity.ok(clienteAtualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            clienteService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
            
    @GetMapping("/funcionarios/buscar")
    public ResponseEntity<List<ClienteResumoDTO>> buscarFuncionarios(@RequestParam String termo) {
        List<ClienteResumoDTO> clientes = clienteService.buscarPorTermo(termo)
            .stream()
            .filter(c -> "FUNCIONARIO".equals(c.getTipoCliente()))
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(clientes);
    }
    
    @GetMapping("/funcionarios")
    public ResponseEntity<List<Cliente>> listarFuncionarios() {
        return ResponseEntity.ok(clienteRepository.findByTipoCliente("FUNCIONARIO"));
    }
    
    @PatchMapping("/{id}/autorizar-jantar")
    public ResponseEntity<?> autorizarJantar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return clienteRepository.findById(id).map(cliente -> {
            cliente.setAutorizadoJantar((Boolean) body.get("autorizadoJantar"));
            clienteRepository.save(cliente);
            return ResponseEntity.ok(Map.of("autorizadoJantar", cliente.getAutorizadoJantar()));
        }).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/foto")
    public ResponseEntity<?> buscarFoto(@PathVariable Long id) {
        Optional<Cliente> cliente = clienteService.buscarPorId(id);
        return cliente
            .map(c -> ResponseEntity.ok(Map.of(
                "fotoBase64", c.getFotoBase64() != null ? c.getFotoBase64() : ""
            )))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PatchMapping("/{id}/face")
    public ResponseEntity<?> cadastrarFace(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String faceDescriptor = body.get("faceDescriptor");
            String fotoBase64 = body.get("fotoBase64");

            if (faceDescriptor == null || faceDescriptor.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Descritor facial é obrigatório"));
            }

            Cliente cliente = clienteService.cadastrarFace(id, faceDescriptor, fotoBase64);

            return ResponseEntity.ok(Map.of(
                "id", cliente.getId(),
                "nome", cliente.getNome(),
                "faceAtivo", cliente.getFaceAtivo(),
                "faceCriadoEm", cliente.getFaceCriadoEm()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/face/desativar")
    public ResponseEntity<?> desativarFace(@PathVariable Long id) {
        try {
            Cliente cliente = clienteService.desativarFace(id);

            return ResponseEntity.ok(Map.of(
                "id", cliente.getId(),
                "nome", cliente.getNome(),
                "faceAtivo", cliente.getFaceAtivo()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }


}
