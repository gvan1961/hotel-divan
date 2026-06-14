package com.divan.service;

import com.divan.dto.ContaAReceberDTO;
import com.divan.dto.ContaAReceberRequestDTO;
import com.divan.dto.PagamentoContaReceberDTO;
import com.divan.entity.Cliente;
import com.divan.entity.ContaAReceber;
import com.divan.entity.ContaAReceber.StatusContaEnum;
import com.divan.entity.Empresa;
import com.divan.entity.ExtratoReserva;
import com.divan.entity.Reserva;
import com.divan.entity.VwExtratoCompleto;
import com.divan.repository.ContaAReceberRepository;
import com.divan.repository.EmpresaRepository;
import com.divan.repository.ExtratoReservaRepository;
import com.divan.repository.HospedagemHospedeRepository;
import com.divan.repository.ReservaRepository;
import com.divan.repository.VwExtratoCompletoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContaAReceberService {

	@Autowired
	private VwExtratoCompletoRepository vwExtratoCompletoRepository;

    private final ContaAReceberRepository contaAReceberRepository;
    private final ReservaRepository reservaRepository;
    private final EmpresaRepository empresaRepository;
    private final HospedagemHospedeRepository hospedagemHospedeRepository;
    private final ExtratoReservaRepository extratoReservaRepository;
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

        Reserva reserva = reservaRepository.findById(dto.getReservaId())
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        Optional<ContaAReceber> contaExistente = contaAReceberRepository.findByReserva(reserva);
        if (contaExistente.isPresent()) {
            throw new RuntimeException("Já existe uma conta a receber para esta reserva");
        }

        ContaAReceber conta = new ContaAReceber();
        conta.setReserva(reserva);
        conta.setCliente(reserva.getCliente());
        
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

        if (dto.getValorPago().compareTo(conta.getSaldo()) > 0) {
            throw new RuntimeException("Valor do pagamento não pode ser maior que o saldo");
        }

        BigDecimal novoValorPago = conta.getValorPago().add(dto.getValorPago());
        BigDecimal novoSaldo = conta.getValor().subtract(novoValorPago);

        conta.setValorPago(novoValorPago);
        conta.setSaldo(novoSaldo);
        conta.setDataPagamento(dto.getDataPagamento());

        if (novoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            conta.setStatus(StatusContaEnum.PAGA);
            System.out.println("✅ Conta totalmente paga!");
        } else {
            System.out.println("💵 Pagamento parcial registrado. Saldo: R$ " + novoSaldo);
        }

        conta = contaAReceberRepository.save(conta);

        // ✅ LANÇAR PAGAMENTO NO EXTRATO DA RESERVA
        if (conta.getReserva() != null) {
            ExtratoReserva extrato = new ExtratoReserva();
            extrato.setReserva(conta.getReserva());
            extrato.setDataHoraLancamento(LocalDateTime.now());
            extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.PAGAMENTO);
            extrato.setDescricao("Pagamento empresa — " +
                (dto.getFormaPagamento() != null ? dto.getFormaPagamento().toString() : "Faturado"));
            extrato.setQuantidade(1);
            extrato.setValorUnitario(dto.getValorPago().negate());
            extrato.setTotalLancamento(dto.getValorPago().negate());
            extrato.setNotaVendaId(null);
            extratoReservaRepository.save(extrato);

            Reserva reservaDaConta = conta.getReserva();
            BigDecimal totalRecebidoAtual = reservaDaConta.getTotalRecebido() != null
                ? reservaDaConta.getTotalRecebido() : BigDecimal.ZERO;
            reservaDaConta.setTotalRecebido(totalRecebidoAtual.add(dto.getValorPago()));
            reservaRepository.save(reservaDaConta);
        }

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
    
    
 // ========== RELATÓRIO DETALHADO POR EMPRESA ==========
    public List<Map<String, Object>> relatorioDetalhadoEmpresa(Long empresaId) {
        List<VwExtratoCompleto> extratos = vwExtratoCompletoRepository
            .findByEmpresaIdOrderByReservaIdAscDataHoraLancamentoAsc(empresaId);

        // Agrupa extratos por reservaId
        Map<Long, List<VwExtratoCompleto>> porReserva = extratos.stream()
            .collect(Collectors.groupingBy(VwExtratoCompleto::getReservaId));

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Map.Entry<Long, List<VwExtratoCompleto>> entry : porReserva.entrySet()) {
            List<VwExtratoCompleto> extratosReserva = entry.getValue();
            VwExtratoCompleto primeiro = extratosReserva.get(0);

            Map<String, Object> item = new HashMap<>();
            item.put("reservaId", primeiro.getReservaId());
            item.put("clienteNome", primeiro.getClienteNome());
            item.put("numeroApartamento", primeiro.getNumeroApartamento());
            item.put("dataCheckin", primeiro.getDataCheckin());
            item.put("dataCheckout", primeiro.getDataCheckout());
            item.put("totalHospedagem", primeiro.getTotalHospedagem());
            item.put("totalDiaria", primeiro.getTotalDiaria());
            item.put("totalConsumo", primeiro.getTotalProduto());
            item.put("desconto", primeiro.getDesconto());
            item.put("totalRecebido", primeiro.getTotalRecebido());

            // Busca conta a receber para esta reserva
            ContaAReceber conta = contaAReceberRepository.findByReserva(
                reservaRepository.findById(primeiro.getReservaId()).orElse(null))
                .orElse(null);
            item.put("valor", conta != null ? conta.getValor() : BigDecimal.ZERO);
            item.put("valorPago", conta != null ? conta.getValorPago() : BigDecimal.ZERO);
            item.put("saldo", conta != null ? conta.getSaldo() : BigDecimal.ZERO);
            item.put("status", conta != null ? conta.getStatus() : "EM_ABERTO");
            item.put("quantidadeHospede", primeiro.getQuantidadeHospede());
            item.put("quantidadeDiaria", primeiro.getQuantidadeDiaria());

            // Calcula pago à vista (total recebido - débitos em conta)
            BigDecimal debitoEmConta = extratosReserva.stream()
                .filter(e -> e.getDescricao() != null && e.getDescricao().contains("DEBITO EM CONTA"))
                .map(VwExtratoCompleto::getTotalLancamento)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalRecebido = primeiro.getTotalRecebido() != null ? primeiro.getTotalRecebido() : BigDecimal.ZERO;
            BigDecimal pagoAVista = totalRecebido.subtract(debitoEmConta);
            if (pagoAVista.compareTo(BigDecimal.ZERO) < 0) pagoAVista = BigDecimal.ZERO;
            item.put("pagoAVista", pagoAVista);

            // Hóspedes
            String hospedes = hospedagemHospedeRepository
                .findByReservaId(primeiro.getReservaId())
                .stream()
                .map(h -> h.getCliente() != null ? h.getCliente().getNome() : h.getNomeCompleto())
                .filter(n -> n != null && !n.isBlank())
                .collect(Collectors.joining(", "));
            item.put("todosHospedes", hospedes.isBlank() ? primeiro.getClienteNome() : hospedes);

            // Extratos
            List<Map<String, Object>> extratosList = extratosReserva.stream().map(e -> {
                Map<String, Object> ext = new HashMap<>();
                ext.put("descricao", e.getDescricao());
                ext.put("status", e.getStatusLancamento());
                ext.put("quantidade", e.getQuantidade());
                ext.put("valorUnitario", e.getValorUnitario());
                ext.put("total", e.getTotalLancamento());
                ext.put("dataHora", e.getDataHoraLancamento());
                return ext;
            }).collect(Collectors.toList());

            item.put("extratos", extratosList);
            resultado.add(item);
        }
        return resultado;
    }
    

    // ========== CONVERTER PARA DTO ==========
    
    private ContaAReceberDTO converterParaDTO(ContaAReceber conta) {
        ContaAReceberDTO dto = new ContaAReceberDTO();
        dto.setId(conta.getId());
        dto.setReservaId(conta.getReserva().getId());
        dto.setClienteNome(conta.getCliente().getNome());      
        
        if (conta.getReserva() != null) {
            String todosHospedes = hospedagemHospedeRepository
                .findByReservaId(conta.getReserva().getId())
                .stream()
                .map(h -> h.getCliente() != null ? h.getCliente().getNome() : h.getNomeCompleto())
                .filter(n -> n != null && !n.isBlank())
                .collect(Collectors.joining(", "));
            dto.setTodosHospedes(todosHospedes.isBlank() ? conta.getCliente().getNome() : todosHospedes);
        }
        
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
            dto.setDesconto(reserva.getDesconto());

            // ✅ TOTAL RECEBIDO = pagamentos da reserva + pagamentos da conta a receber
            BigDecimal totalRecebidoReserva = reserva.getTotalRecebido() != null ? reserva.getTotalRecebido() : BigDecimal.ZERO;
            BigDecimal valorPagoContaReceber = conta.getValorPago() != null ? conta.getValorPago() : BigDecimal.ZERO;

            // ✅ CALCULAR PAGO À VISTA (exclui débito em conta)
            List<ExtratoReserva> extratos = extratoReservaRepository
            	    .findByReservaIdOrderByDataHoraLancamento(reserva.getId());
            System.out.println("📋 Extratos da reserva " + reserva.getId() + ": " + extratos.size());
            extratos.forEach(e -> System.out.println("  → " + e.getDescricao() + " = " + e.getTotalLancamento()));

            BigDecimal debitoEmConta = extratos.stream()
                .filter(e -> e.getDescricao() != null && e.getDescricao().contains("DEBITO EM CONTA"))
                .map(e -> e.getTotalLancamento().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            System.out.println("💳 Débito em conta: " + debitoEmConta);
            BigDecimal pagoAVista = totalRecebidoReserva.subtract(debitoEmConta);
            if (pagoAVista.compareTo(BigDecimal.ZERO) < 0) pagoAVista = BigDecimal.ZERO;

            dto.setTotalRecebido(pagoAVista);
            dto.setPagoAVista(pagoAVista);
            dto.setTotalRecebido(totalRecebidoReserva.add(valorPagoContaReceber));

            // ✅ SALDO REAL = totalHospedagem - desconto - totalRecebido
            BigDecimal desconto = reserva.getDesconto() != null ? reserva.getDesconto() : BigDecimal.ZERO;
            BigDecimal totalHospedagem = reserva.getTotalHospedagem() != null ? reserva.getTotalHospedagem() : BigDecimal.ZERO;
            dto.setTotalApagar(totalHospedagem.subtract(desconto).subtract(totalRecebidoReserva.add(valorPagoContaReceber)));
        }
        
        return dto;
    }
    
    public List<Map<String, Object>> relatórioDetalhadoEmpresa(Long empresaId) {
        Empresa empresa = new Empresa();
        empresa.setId(empresaId);
        List<ContaAReceber> contas = contaAReceberRepository.findByEmpresa(empresa);

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (ContaAReceber conta : contas) {
            Map<String, Object> item = new HashMap<>();
            item.put("contaId", conta.getId());
            item.put("reservaId", conta.getReserva().getId());
            item.put("clienteNome", conta.getCliente().getNome());
            item.put("numeroApartamento", conta.getReserva().getApartamento() != null
                ? conta.getReserva().getApartamento().getNumeroApartamento() : "-");
            item.put("dataCheckin", conta.getReserva().getDataCheckin());
            item.put("dataCheckout", conta.getReserva().getDataCheckout());
            item.put("quantidadeHospede", conta.getReserva().getQuantidadeHospede());
            item.put("quantidadeDiaria", conta.getReserva().getQuantidadeDiaria());
            item.put("totalHospedagem", conta.getReserva().getTotalHospedagem());
            item.put("desconto", conta.getReserva().getDesconto());
            item.put("totalRecebido", conta.getReserva().getTotalRecebido());
            item.put("valor", conta.getValor());
                       
            item.put("valorPago", conta.getReserva().getTotalRecebido() != null
            	    ? conta.getReserva().getTotalRecebido() : BigDecimal.ZERO);         
            
            
            item.put("saldo", conta.getSaldo());
            item.put("status", conta.getStatus());
            item.put("dataVencimento", conta.getDataVencimento());
           
            // Extratos da reserva
            List<ExtratoReserva> extratos = extratoReservaRepository
                .findByReservaIdOrderByDataHoraLancamento(conta.getReserva().getId());

            List<Map<String, Object>> extratosList = extratos.stream().map(e -> {
                Map<String, Object> ext = new HashMap<>();
                ext.put("descricao", e.getDescricao());
                ext.put("status", e.getStatusLancamento());
                ext.put("quantidade", e.getQuantidade());
                ext.put("valorUnitario", e.getValorUnitario());
                ext.put("total", e.getTotalLancamento());
                ext.put("dataHora", e.getDataHoraLancamento());
                return ext;
            }).collect(Collectors.toList());

            item.put("extratos", extratosList);
            resultado.add(item);
        }
        return resultado;
    }
    
    @Transactional
    public ContaAReceberDTO aplicarDesconto(Long id, BigDecimal valorDesconto, String motivo) {
        ContaAReceber conta = contaAReceberRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        if (valorDesconto.compareTo(conta.getSaldo()) > 0) {
            throw new RuntimeException("Desconto não pode ser maior que o saldo");
        }

        // Atualiza a conta
        BigDecimal novoSaldo = conta.getSaldo().subtract(valorDesconto);
        BigDecimal novoValor = conta.getValor().subtract(valorDesconto);
        conta.setSaldo(novoSaldo);
        conta.setValor(novoValor);
        if (novoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            conta.setStatus(StatusContaEnum.PAGA);
        }
        contaAReceberRepository.save(conta);

        // Lança desconto no extrato da reserva
        if (conta.getReserva() != null) {
            ExtratoReserva extrato = new ExtratoReserva();
            extrato.setReserva(conta.getReserva());
            extrato.setDataHoraLancamento(LocalDateTime.now());
            extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.ESTORNO);
            extrato.setDescricao("Desconto empresa — " + (motivo != null ? motivo : "Desconto negociado"));
            extrato.setQuantidade(1);
            extrato.setValorUnitario(valorDesconto.negate());
            extrato.setTotalLancamento(valorDesconto.negate());
            extrato.setNotaVendaId(null);
            extratoReservaRepository.save(extrato);

            // Atualiza desconto na reserva
            Reserva reserva = conta.getReserva();
            BigDecimal descontoAtual = reserva.getDesconto() != null ? reserva.getDesconto() : BigDecimal.ZERO;
            reserva.setDesconto(descontoAtual.add(valorDesconto));
            reservaRepository.save(reserva);
        }

        return converterParaDTO(conta);
    }
    
}