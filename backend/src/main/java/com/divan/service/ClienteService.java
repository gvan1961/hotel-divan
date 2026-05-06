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
import java.util.ArrayList;
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

        // ✅ 1. LIMPAR CPF — remove pontos e traço
        if (cliente.getCpf() != null && !cliente.getCpf().isEmpty()) {
            cliente.setCpf(cliente.getCpf().replaceAll("[^0-9]", ""));
        }

        // ✅ 2. VALIDAR E VERIFICAR DUPLICATA
        if (cliente.getCpf() != null && !cliente.getCpf().isEmpty()) {
            if (!validarCPF(cliente.getCpf())) {
                throw new RuntimeException("CPF inválido: " + cliente.getCpf());
            }
            Optional<Cliente> existente = clienteRepository.findByCpf(cliente.getCpf());
            if (existente.isPresent() && !existente.get().getId().equals(cliente.getId())) {
                throw new RuntimeException("CPF já cadastrado para outro cliente");
            }
        }

        // ✅ 3. MONTA CELULAR COMPLETO: DDI + número sem formatação
        if (cliente.getDdi() != null && cliente.getCelular() != null) {
            String numeroLimpo = cliente.getCelular().replaceAll("\\D", "");
            cliente.setCelularCompleto(cliente.getDdi() + numeroLimpo);
        }

        if (cliente.getDdi2() != null && cliente.getCelular2() != null) {
            String numeroLimpo2 = cliente.getCelular2().replaceAll("\\D", "");
            cliente.setCelular2Completo(cliente.getDdi2() + numeroLimpo2);
        }

        // ✅ 4. VINCULAR EMPRESA E APROVAR CRÉDITO
        if (empresaId != null) {
            Optional<Empresa> empresaOpt = empresaRepository.findById(empresaId);
            if (empresaOpt.isEmpty()) {
                throw new RuntimeException("Empresa não encontrada");
            }
            cliente.setEmpresa(empresaOpt.get());
            cliente.setCreditoAprovado(true);
            System.out.println("✅ Crédito aprovado automaticamente — empresa: "
                + empresaOpt.get().getNomeEmpresa());
        }

        return clienteRepository.save(cliente);
    }
    
    public List<ClienteDTO> buscarPorTermo(String termo) {
        String termoLimpo = termo.trim();
        System.out.println("🔍 Buscando por: " + termoLimpo);

        try {
            List<Object[]> resultados;

            String apenasNumeros = termoLimpo.replaceAll("\\D", "");
            if (apenasNumeros.length() >= 2) {
                // Busca pelo número parcial no CPF sem formatação E pelo nome
                resultados = clienteRepository.buscarPorCpfParcialOuNome(
                    "%" + apenasNumeros + "%", termoLimpo + "*");
            } else {
                resultados = clienteRepository.buscarPorNomeFull(termoLimpo + "*");
            }

            System.out.println("✅ Resultados encontrados: " + resultados.size());

            return resultados.stream().map(row -> {
                ClienteDTO dto = new ClienteDTO();
                dto.setId(((Number) row[0]).longValue());
                dto.setNome((String) row[1]);
                dto.setCpf((String) row[2]);
                dto.setCelular((String) row[3]);
                if (row[4] != null) {
                    dto.setEmpresaNome((String) row[4]);
                }
                if (row.length > 5 && row[5] != null) {
                    dto.setTipoCliente((String) row[5]);
                }
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ ERRO na busca: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public ClienteDTO converterParaDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNome(cliente.getNome());
        dto.setCpf(cliente.getCpf());
        dto.setCelular(cliente.getCelular());
        dto.setDdi(cliente.getDdi() != null ? cliente.getDdi() : "+55");
        dto.setCelular2(cliente.getCelular2());
        dto.setDdi2(cliente.getDdi2() != null ? cliente.getDdi2() : "+55");
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
        clienteExistente.setDdi(clienteAtualizado.getDdi() != null ? clienteAtualizado.getDdi() : "55");
        clienteExistente.setCelular2(clienteAtualizado.getCelular2());
        clienteExistente.setDdi2(clienteAtualizado.getDdi2() != null ? clienteAtualizado.getDdi2() : "55");
        clienteExistente.setEndereco(clienteAtualizado.getEndereco());
        clienteExistente.setCep(clienteAtualizado.getCep());
        clienteExistente.setCidade(clienteAtualizado.getCidade());
        clienteExistente.setEstado(clienteAtualizado.getEstado());
        clienteExistente.setDataNascimento(clienteAtualizado.getDataNascimento());
        clienteExistente.setMenorDeIdade(clienteAtualizado.getMenorDeIdade() != null
            ? clienteAtualizado.getMenorDeIdade() : false);

        // Monta celular completo: DDI + número sem formatação
        if (clienteAtualizado.getDdi() != null && clienteAtualizado.getCelular() != null) {
            String numeroLimpo = clienteAtualizado.getCelular().replaceAll("\\D", "");
            clienteExistente.setCelularCompleto(clienteAtualizado.getDdi() + numeroLimpo);
        }

        if (clienteAtualizado.getDdi2() != null && clienteAtualizado.getCelular2() != null) {
            String numeroLimpo2 = clienteAtualizado.getCelular2().replaceAll("\\D", "");
            clienteExistente.setCelular2Completo(clienteAtualizado.getDdi2() + numeroLimpo2);
        }

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
    
    private boolean validarCPF(String cpf) {
        String numeros = cpf.replaceAll("\\D", "");
        
        if (numeros.length() != 11) return false;
        if (numeros.matches("(\\d)\\1{10}")) return false; // CPFs com todos dígitos iguais
        
        int soma = 0;
        for (int i = 0; i < 9; i++)
            soma += (numeros.charAt(i) - '0') * (10 - i);
        int primeiro = 11 - (soma % 11);
        if (primeiro >= 10) primeiro = 0;
        if (primeiro != (numeros.charAt(9) - '0')) return false;
        
        soma = 0;
        for (int i = 0; i < 10; i++)
            soma += (numeros.charAt(i) - '0') * (11 - i);
        int segundo = 11 - (soma % 11);
        if (segundo >= 10) segundo = 0;
        
        return segundo == (numeros.charAt(10) - '0');
    }
}
