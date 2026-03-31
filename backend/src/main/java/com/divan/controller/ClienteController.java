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
    public ResponseEntity<Cliente> criar(@Valid @RequestBody ClienteRequestDTO dto) {
        try {
            // Converter DTO para Entity
            Cliente cliente = new Cliente();
            cliente.setNome(dto.getNome());
            cliente.setCpf(dto.getCpf());
            cliente.setCelular(dto.getCelular());
            cliente.setEndereco(dto.getEndereco());
            cliente.setCep(dto.getCep());
            cliente.setCidade(dto.getCidade());
            cliente.setEstado(dto.getEstado());
            cliente.setDataNascimento(dto.getDataNascimento());
            
            Cliente clienteSalvo = clienteService.salvar(cliente, dto.getEmpresaId());
            return ResponseEntity.status(HttpStatus.CREATED).body(clienteSalvo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        List<Cliente> clientes = clienteService.listarTodos();
        return ResponseEntity.ok(clientes);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id) {
        Optional<Cliente> cliente = clienteService.buscarPorId(id);
        return cliente.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
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
    public ResponseEntity<Cliente> atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO dto) {
        try {
            Cliente cliente = new Cliente();
            cliente.setNome(dto.getNome());
            cliente.setCpf(dto.getCpf());
            cliente.setCelular(dto.getCelular());
            cliente.setEndereco(dto.getEndereco());
            cliente.setCep(dto.getCep());
            cliente.setCidade(dto.getCidade());
            cliente.setEstado(dto.getEstado());
            cliente.setDataNascimento(dto.getDataNascimento());

            // ✅ CAMPOS QUE FALTAVAM
            if (dto.getCreditoAprovado() != null) {
                cliente.setCreditoAprovado(dto.getCreditoAprovado());
            }
            if (dto.getTipoCliente() != null) {
                cliente.setTipoCliente(dto.getTipoCliente());
            }
            if (dto.getAutorizadoJantar() != null) {
                cliente.setAutorizadoJantar(dto.getAutorizadoJantar());
            }

            Cliente clienteAtualizado = clienteService.atualizar(id, cliente, dto.getEmpresaId());
            return ResponseEntity.ok(clienteAtualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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
}
