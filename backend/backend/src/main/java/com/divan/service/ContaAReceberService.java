package com.divan.service;

import com.divan.dto.ContaAReceberDTO;
import com.divan.dto.ContaAReceberRequestDTO;
import com.divan.dto.PagamentoContaReceberDTO;
import com.divan.entity.Cliente;
import com.divan.entity.ContaAReceber;
import com.divan.entity.ContaAReceber.StatusContaEnum;
import com.divan.entity.Empresa;
import com.divan.entity.Reserva;
import com.divan.repository.ContaAReceberRepository;
import com.divan.repository.EmpresaRepository;
import com.divan.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContaAReceberService {

    private final ContaAReceberRepository contaAReceberRepository;
    private final ReservaRepository reservaRepository;
    private final EmpresaRepository empresaRepository;

    // ========== LISTAR ==========
    
    public List<ContaAReceberDTO> listarTodas() {
        return contaAReceberRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<ContaAReceberDTO> listarPorStatus(StatusContaEnum status) {
        return contaAReceberRepository.findByStatus(status).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<ContaAReceberDTO> listarContasEmAberto() {
        return contaAReceberRepository.findContasEmAberto().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<ContaAReceberDTO> listarContasVencidas() {
        return contaAReceberRepository.findContasVencidas(LocalDate.now()).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<ContaAReceberDTO> listarPorCliente(Long clienteId) {
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        return contaAReceberRepository.findByCliente(cliente).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<ContaAReceberDTO> listarPorEmpresa(Long empresaId) {
        Empresa empresa = new Empresa();
        empresa.setId(empresaId);
        return contaAReceberRepository.findByEmpresa(empresa).stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public ContaAReceberDTO buscarPorId(Long id) {
        ContaAReceber conta = contaAReceberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta a receber não encontrada"));
        return converterParaDTO(conta);
    }

    // ========== CRIAR ==========
    
    @Transactional
    public ContaAReceberDTO criar(ContaAReceberRequestDTO dto) {
        System.out.println("🆕 Criando conta a receber para reserva: " + dto.getReservaId());

        // Buscar reserva
        Reserva reserva = reservaRepository.findById(dto.getReservaId())
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        // ✅ VERIFICAR SE JÁ EXISTE CONTA PARA ESTA RESERVA (CORRETO)
        Optional<ContaAReceber> contaExistente = contaAReceberRepository.findByReserva(reserva);
        if (contaExistente.isPresent()) {
            throw new RuntimeException("Já existe uma conta a receber para esta reserva");
        }

        ContaAReceber conta = new ContaAReceber();
        conta.setReserva(reserva);
        conta.setCliente(reserva.getCliente());
        
        // Se tiver empresa, buscar e setar
        if (dto.getEmpresaId() != null) {
            Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                    .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));
            conta.setEmpresa(empresa);
        }
        
        conta.setValor(dto.getValor());
        conta.setValorPago(BigDecimal.ZERO);
        conta.setSaldo(dto.getValor());
        conta.setDataVencimento(dto.getDataVencimento());
        conta.setStatus(StatusContaEnum.EM_ABERTO);
        conta.setDescricao(dto.getDescricao());

        conta = contaAReceberRepository.save(conta);
        
        System.out.println("✅ Conta a receber criada: " + conta.getId());
        return converterParaDTO(conta);
    }
    // ========== REGISTRAR PAGAMENTO ==========
    
    @Transactional
    public ContaAReceberDTO registrarPagamento(Long id, PagamentoContaReceberDTO dto) {
        System.out.println("💰 Registrando pagamento na conta: " + id);

        ContaAReceber conta = contaAReceberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta a receber não encontrada"));

        if (conta.getStatus() == StatusContaEnum.PAGA) {
            throw new RuntimeException("Esta conta já está paga");
        }

        // Validar valor do pagamento
        if (dto.getValorPago().compareTo(conta.getSaldo()) > 0) {
            throw new RuntimeException("Valor do pagamento não pode ser maior que o saldo");
        }

        // Atualizar valores
        BigDecimal novoValorPago = conta.getValorPago().add(dto.getValorPago());
        BigDecimal novoSaldo = conta.getValor().subtract(novoValorPago);

        conta.setValorPago(novoValorPago);
        conta.setSaldo(novoSaldo);
        conta.setDataPagamento(dto.getDataPagamento());

        // Se pagou tudo, marcar como PAGA
        if (novoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            conta.setStatus(StatusContaEnum.PAGA);
            System.out.println("✅ Conta totalmente paga!");
        } else {
            System.out.println("💵 Pagamento parcial registrado. Saldo: R$ " + novoSaldo);
        }

        conta = contaAReceberRepository.save(conta);
        return converterParaDTO(conta);
    }

    // ========== ATUALIZAR STATUS DE VENCIDAS ==========
    
    @Transactional
    public void atualizarStatusVencidas() {
        System.out.println("🔄 Atualizando status de contas vencidas...");
        
        List<ContaAReceber> contasVencidas = contaAReceberRepository.findContasVencidas(LocalDate.now());
        
        for (ContaAReceber conta : contasVencidas) {
            if (conta.getStatus() == StatusContaEnum.EM_ABERTO) {
                conta.setStatus(StatusContaEnum.VENCIDA);
                contaAReceberRepository.save(conta);
            }
        }
        
        System.out.println("✅ " + contasVencidas.size() + " conta(s) marcada(s) como vencida(s)");
    }

    // ========== EXCLUIR ==========
    
    @Transactional
    public void excluir(Long id) {
        System.out.println("🗑️ Excluindo conta a receber: " + id);
        
        ContaAReceber conta = contaAReceberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta a receber não encontrada"));

        if (conta.getStatus() != StatusContaEnum.PAGA) {
            throw new RuntimeException("Apenas contas PAGAS podem ser excluídas");
        }

        contaAReceberRepository.delete(conta);
        System.out.println("✅ Conta excluída com sucesso");
    }

    // ========== CONVERTER PARA DTO ==========
    
    private ContaAReceberDTO converterParaDTO(ContaAReceber conta) {
        ContaAReceberDTO dto = new ContaAReceberDTO();
        dto.setId(conta.getId());
        dto.setReservaId(conta.getReserva().getId());
        dto.setClienteNome(conta.getCliente().getNome());
        dto.setEmpresaNome(conta.getEmpresa() != null ? conta.getEmpresa().getNomeEmpresa() : null);
        dto.setValor(conta.getValor());
        dto.setValorPago(conta.getValorPago());
        dto.setSaldo(conta.getSaldo());
        dto.setDataVencimento(conta.getDataVencimento());
        dto.setDataPagamento(conta.getDataPagamento());
        dto.setStatus(conta.getStatus());
        dto.setDescricao(conta.getDescricao());
        
        // Calcular dias vencido
        if (conta.getStatus() == StatusContaEnum.VENCIDA || 
            (conta.getStatus() == StatusContaEnum.EM_ABERTO && conta.getDataVencimento().isBefore(LocalDate.now()))) {
            dto.setDiasVencido((int) ChronoUnit.DAYS.between(conta.getDataVencimento(), LocalDate.now()));
        } else {
            dto.setDiasVencido(0);
        }
        
        Reserva reserva = conta.getReserva();
        if (reserva != null) {
            dto.setNumeroApartamento(reserva.getApartamento() != null ? reserva.getApartamento().getNumeroApartamento() : null);
            dto.setQuantidadeHospede(reserva.getQuantidadeHospede());
            dto.setQuantidadeDiaria(reserva.getQuantidadeDiaria());
            dto.setTotalDiaria(reserva.getTotalDiaria());
            dto.setTotalConsumo(reserva.getTotalProduto());
            dto.setTotalHospedagem(reserva.getTotalHospedagem());
            dto.setTotalRecebido(reserva.getTotalRecebido());
            dto.setDesconto(reserva.getDesconto());
            dto.setTotalApagar(reserva.getTotalApagar());
        }
        
        return dto;
    }
}