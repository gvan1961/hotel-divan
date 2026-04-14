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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

            Cliente clienteSalvo = clienteService.salvar(cliente, dto.getEmpresaId(), dto.getResponsavelId());
            return ResponseEntity.status(HttpStatus.CREATED).body(clienteSalvo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        var pagina = clienteRepository.findAll(
            org.springframework.data.domain.PageRequest.of(
                page, size, 
                org.springframework.data.domain.Sort.by("nome")));
        
        return ResponseEntity.ok(Map.of(
            "clientes", pagina.getContent(),
            "totalPaginas", pagina.getTotalPages(),
            "totalElementos", pagina.getTotalElements(),
            "paginaAtual", pagina.getNumber()
        ));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> buscarPorId(@PathVariable Long id) {
        Optional<Cliente> cliente = clienteService.buscarPorId(id);
        return cliente.map(c -> ResponseEntity.ok(clienteService.converterParaDTO(c)))
                      .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Cliente> buscarPorCpf(@PathVariable String cpf) {
        Optional<Cliente> cliente = clienteService.buscarPorCpf(cpf);
        return cliente.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
   // @GetMapping("/buscar")
   // public ResponseEntity<List<Cliente>> buscarPorNome(@RequestParam String nome) {
   //     List<Cliente> clientes = clienteService.buscarPorNome(nome);
   //     return ResponseEntity.ok(clientes);
   // }
    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteDTO>> buscar(@RequestParam String termo) {
        List<ClienteDTO> clientes = clienteService.buscarPorTermo(termo);
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
    
    /*
    @GetMapping("/funcionarios/buscar")
    public ResponseEntity<List<ClienteDTO>> buscarFuncionarios(@RequestParam String termo) {
        List<ClienteDTO> clientes = clienteService.buscarPorTermo(termo)
            .stream()
            .filter(c -> "FUNCIONARIO".equals(c.getTipoCliente()))
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(clientes);
    }
    */
    
    @GetMapping("/funcionarios/buscar")
    public ResponseEntity<List<Cliente>> buscarFuncionarios(@RequestParam String termo) {
        List<Cliente> clientes = clienteService.buscarPorNome(termo);
        return ResponseEntity.ok(clientes);
    }
    
}
