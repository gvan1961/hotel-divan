package com.divan.service;

import com.divan.dto.ClienteDTO;
import com.divan.entity.Cliente;
import com.divan.entity.Empresa;
import com.divan.repository.ClienteRepository;
import com.divan.repository.EmpresaRepository;
import com.divan.repository.HospedagemHospedeRepository;
import com.divan.repository.ReservaRepository;

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
    
    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private HospedagemHospedeRepository hospedagemHospedeRepository;
    
    public Cliente salvar(Cliente cliente, Long empresaId, Long responsavelId) {
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
            // ✅ CRÉDITO APROVADO AUTOMATICAMENTE POR VÍNCULO COM EMPRESA
            cliente.setCreditoAprovado(true);
            System.out.println("✅ Crédito aprovado automaticamente — empresa: " 
                + empresaOpt.get().getNomeEmpresa());
        }
        
        return clienteRepository.save(cliente);
    }
    
    public List<ClienteDTO> buscarPorTermo(String termo) {
        List<Cliente> clientes = clienteRepository.findByNomeContainingIgnoreCaseOrCpfContaining(termo, termo);
        return clientes.stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    public ClienteDTO converterParaDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNome(cliente.getNome());
        dto.setCpf(cliente.getCpf());
        dto.setCelular(cliente.getCelular());
        dto.setCreditoAprovado(cliente.getCreditoAprovado());
        dto.setAutorizadoJantar(cliente.getAutorizadoJantar());
        dto.setTipoCliente(cliente.getTipoCliente());
        
        // ✅ ADICIONE OUTROS CAMPOS SE EXISTIREM NO DTO
        // dto.setEmail(cliente.getEmail());
        // dto.setEndereco(cliente.getEndereco());
        
        if (cliente.getEmpresa() != null) {
            dto.setEmpresaId(cliente.getEmpresa().getId());
            dto.setEmpresaNome(cliente.getEmpresa().getNomeEmpresa());
        }
        
        dto.setMenorDeIdade(cliente.getMenorDeIdade());
        if (cliente.getResponsavel() != null) {
            dto.setResponsavelId(cliente.getResponsavel().getId());
            dto.setResponsavelNome(cliente.getResponsavel().getNome());
            dto.setResponsavelCpf(cliente.getResponsavel().getCpf());
        }
        
        return dto;
    }
    
    public Cliente atualizar(Long id, Cliente clienteAtualizado, Long empresaId, Long responsavelId) {
        Cliente clienteExistente = clienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        clienteExistente.setNome(clienteAtualizado.getNome());
        clienteExistente.setCpf(clienteAtualizado.getCpf());
        clienteExistente.setCelular(clienteAtualizado.getCelular());
        clienteExistente.setEndereco(clienteAtualizado.getEndereco());
        clienteExistente.setCep(clienteAtualizado.getCep());
        clienteExistente.setCidade(clienteAtualizado.getCidade());
        clienteExistente.setEstado(clienteAtualizado.getEstado());
        clienteExistente.setDataNascimento(clienteAtualizado.getDataNascimento());
        clienteExistente.setMenorDeIdade(clienteAtualizado.getMenorDeIdade() != null 
            ? clienteAtualizado.getMenorDeIdade() : false);

        if (clienteAtualizado.getCreditoAprovado() != null) {
            clienteExistente.setCreditoAprovado(clienteAtualizado.getCreditoAprovado());
        }
        if (clienteAtualizado.getTipoCliente() != null) {
            clienteExistente.setTipoCliente(clienteAtualizado.getTipoCliente());
        }
        if (clienteAtualizado.getAutorizadoJantar() != null) {
            clienteExistente.setAutorizadoJantar(clienteAtualizado.getAutorizadoJantar());
        }

        // ✅ RESPONSÁVEL (menor de idade)
        if (responsavelId != null) {
            Cliente responsavel = clienteRepository.findById(responsavelId)
                .orElseThrow(() -> new RuntimeException("Responsável não encontrado"));
            clienteExistente.setResponsavel(responsavel);
        } else {
            clienteExistente.setResponsavel(null);
        }

        // ✅ EMPRESA
        if (empresaId != null) {
            Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));
            clienteExistente.setEmpresa(empresa);
            clienteExistente.setCreditoAprovado(true);
            System.out.println("✅ Crédito aprovado automaticamente — empresa: "
                + empresa.getNomeEmpresa());
        } else {
            clienteExistente.setEmpresa(null);
        }

        return clienteRepository.save(clienteExistente);
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

        boolean temReservas = reservaRepository.existsByClienteId(id);
        if (temReservas) {
            throw new RuntimeException("Cliente possui reservas e não pode ser excluído");
        }

        boolean temHospedagens = hospedagemHospedeRepository.existsByClienteId(id);
        if (temHospedagens) {
            throw new RuntimeException("Cliente possui hospedagens e não pode ser excluído");
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
