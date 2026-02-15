package com.divan.service;

import com.divan.dto.ClienteDTO;
import com.divan.entity.Cliente;
import com.divan.entity.Empresa;
import com.divan.repository.ClienteRepository;
import com.divan.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClienteService {
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private EmpresaRepository empresaRepository;
    
    public Cliente salvar(Cliente cliente, Long empresaId) {
        if (clienteRepository.existsByCpf(cliente.getCpf()) && cliente.getId() == null) {
            throw new RuntimeException("CPF já cadastrado");
        }
        
        // Se foi informado ID da empresa, buscar e vincular
        if (empresaId != null) {
            Optional<Empresa> empresaOpt = empresaRepository.findById(empresaId);
            if (empresaOpt.isEmpty()) {
                throw new RuntimeException("Empresa não encontrada");
            }
            cliente.setEmpresa(empresaOpt.get());
        }
        
        return clienteRepository.save(cliente);
    }
    
    public List<ClienteDTO> buscarPorTermo(String termo) {
        List<Cliente> clientes = clienteRepository.findByNomeContainingIgnoreCaseOrCpfContaining(termo, termo);
        return clientes.stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    private ClienteDTO converterParaDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNome(cliente.getNome());
        dto.setCpf(cliente.getCpf());
        dto.setCelular(cliente.getCelular());
        
        // ✅ ADICIONE OUTROS CAMPOS SE EXISTIREM NO DTO
        // dto.setEmail(cliente.getEmail());
        // dto.setEndereco(cliente.getEndereco());
        
        if (cliente.getEmpresa() != null) {
            dto.setEmpresaId(cliente.getEmpresa().getId());
            dto.setEmpresaNome(cliente.getEmpresa().getNomeEmpresa());
        }
        
        return dto;
    }
    
    public Cliente atualizar(Long id, Cliente cliente, Long empresaId) {
        Optional<Cliente> clienteExistente = clienteRepository.findById(id);
        if (clienteExistente.isEmpty()) {
            throw new RuntimeException("Cliente não encontrado");
        }
        
        cliente.setId(id);
        
        // Se foi informado ID da empresa, buscar e vincular
        if (empresaId != null) {
            Optional<Empresa> empresaOpt = empresaRepository.findById(empresaId);
            if (empresaOpt.isEmpty()) {
                throw new RuntimeException("Empresa não encontrada");
            }
            cliente.setEmpresa(empresaOpt.get());
        } else {
            cliente.setEmpresa(null);
        }
        
        return clienteRepository.save(cliente);
    }
    
    public boolean isAniversarianteDoMes(Long clienteId) {
        Optional<Cliente> cliente = clienteRepository.findById(clienteId);
        if (cliente.isEmpty() || cliente.get().getDataNascimento() == null) {
            return false;
        }
        
        LocalDate hoje = LocalDate.now();
        LocalDate dataNascimento = cliente.get().getDataNascimento();
        
        return dataNascimento.getMonth() == hoje.getMonth();
    }
    
    public void deletar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente não encontrado");
        }
        clienteRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorCpf(String cpf) {
        return clienteRepository.findByCpf(cpf);
    }
    
    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorNome(String nome) {
        return clienteRepository.findByNomeContainingIgnoreCase(nome);
    }
    
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorEmpresa(Long empresaId) {
        return clienteRepository.findByEmpresaId(empresaId);
    }
}
