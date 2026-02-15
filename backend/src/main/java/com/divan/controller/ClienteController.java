package com.divan.controller;

import com.divan.dto.ClienteDTO;
import com.divan.repository.ClienteRepository;
import com.divan.dto.ClienteRequestDTO;
import com.divan.entity.Cliente;
import com.divan.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {
    
    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private ClienteRepository clienteRepository;
    
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
            cliente.setCreditoAprovado(dto.getCreditoAprovado() != null ? dto.getCreditoAprovado() : false);
            cliente.setAutorizadoJantar(dto.getAutorizadoJantar() != null ? dto.getAutorizadoJantar() : true);
            
            // ⭐ ADICIONAR TIPO CLIENTE
            cliente.setTipoCliente(dto.getTipoCliente() != null ? dto.getTipoCliente() : Cliente.TipoCliente.HOSPEDE);
            
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
    
    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteDTO>> buscar(@RequestParam String termo) {
        List<ClienteDTO> clientes = clienteService.buscarPorTermo(termo);
        return ResponseEntity.ok(clientes);
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
            cliente.setCreditoAprovado(dto.getCreditoAprovado() != null ? dto.getCreditoAprovado() : false);
            cliente.setAutorizadoJantar(dto.getAutorizadoJantar() != null ? dto.getAutorizadoJantar() : true);
            
            // ⭐ ADICIONAR TIPO CLIENTE
            cliente.setTipoCliente(dto.getTipoCliente() != null ? dto.getTipoCliente() : Cliente.TipoCliente.HOSPEDE);
            
            Cliente clienteAtualizado = clienteService.atualizar(id, cliente, dto.getEmpresaId());
            return ResponseEntity.ok(clienteAtualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        try {
            clienteService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ⭐ NOVOS ENDPOINTS PARA FUNCIONÁRIOS
    @GetMapping("/funcionarios")
    public ResponseEntity<List<Cliente>> listarFuncionarios() {
        List<Cliente> funcionarios = clienteRepository.findByTipoCliente(Cliente.TipoCliente.FUNCIONARIO);
        return ResponseEntity.ok(funcionarios);
    }

    @GetMapping("/funcionarios/buscar")
    public ResponseEntity<List<Cliente>> buscarFuncionarios(@RequestParam String termo) {
        List<Cliente> funcionarios = clienteRepository.buscarPorTipoETermo(
            Cliente.TipoCliente.FUNCIONARIO, 
            termo
        );
        return ResponseEntity.ok(funcionarios);
    }
}
