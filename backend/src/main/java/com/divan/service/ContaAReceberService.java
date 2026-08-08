package com.divan.service;

import com.divan.dto.ContaAReceberDTO;
import com.divan.dto.ContaAReceberRequestDTO;
import com.divan.dto.PagamentoContaReceberDTO;
import com.divan.entity.Cliente;
import com.divan.entity.ContaAReceber;
import com.divan.entity.ContaAReceber.StatusContaEnum;
import com.divan.entity.Empresa;
import com.divan.entity.ExtratoReserva;
import com.divan.entity.HospedagemHospede;
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
import java.util.Collections;
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
        return converterListaParaDTO(contaAReceberRepository.findAll());
    }

    public List<ContaAReceberDTO> listarPorStatus(StatusContaEnum status) {
        return converterListaParaDTO(contaAReceberRepository.findByStatus(status));
    }

    public List<ContaAReceberDTO> listarContasEmAberto() {
        return converterListaParaDTO(contaAReceberRepository.findContasEmAberto());
    }

    public List<ContaAReceberDTO> listarContasVencidas() {
        return converterListaParaDTO(contaAReceberRepository.findContasVencidas(LocalDate.now()));
    }

    public List<ContaAReceberDTO> listarPorCliente(Long clienteId) {
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        return converterListaParaDTO(contaAReceberRepository.findByCliente(cliente));
    }

    public List<ContaAReceberDTO> listarPorEmpresa(Long empresaId) {
        Empresa empresa = new Empresa();
        empresa.setId(empresaId);
        return converterListaParaDTO(contaAReceberRepository.findByEmpresa(empresa));
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
    

    // ========== CONVERTER PARA DTO (LISTAS - SEM N+1) ==========

    /**
     * Converte uma lista de contas para DTO buscando hóspedes e extratos
     * EM LOTE (2 queries no total), em vez de 2 queries por conta.
     */
    private List<ContaAReceberDTO> converterListaParaDTO(List<ContaAReceber> contas) {
        List<Long> reservaIds = contas.stream()
                .map(ContaAReceber::getReserva)
                .filter(r -> r != null)
                .map(Reserva::getId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, List<HospedagemHospede>> hospedesPorReserva;
        Map<Long, List<ExtratoReserva>> extratosPorReserva;

        if (reservaIds.isEmpty()) {
            hospedesPorReserva = Collections.emptyMap();
            extratosPorReserva = Collections.emptyMap();
        } else {
            hospedesPorReserva = hospedagemHospedeRepository.findByReservaIdIn(reservaIds).stream()
                    .collect(Collectors.groupingBy(h -> h.getReserva().getId()));

            extratosPorReserva = extratoReservaRepository.findByReservaIdInOrderByDataHoraLancamento(reservaIds).stream()
                    .collect(Collectors.groupingBy(e -> e.getReserva().getId()));
        }

        return contas.stream()
                .map(conta -> converterParaDTO(conta, hospedesPorReserva, extratosPorReserva))
                .collect(Collectors.toList());
    }

    // ========== CONVERTER PARA DTO (REGISTRO ÚNICO) ==========

    private ContaAReceberDTO converterParaDTO(ContaAReceber conta) {
        Long reservaId = conta.getReserva() != null ? conta.getReserva().getId() : null;

        Map<Long, List<HospedagemHospede>> hospedesPorReserva = reservaId == null
                ? Collections.emptyMap()
                : Map.of(reservaId, hospedagemHospedeRepository.findByReservaId(reservaId));

        Map<Long, List<ExtratoReserva>> extratosPorReserva = reservaId == null
                ? Collections.emptyMap()
                : Map.of(reservaId, extratoReservaRepository.findByReservaIdOrderByDataHoraLancamento(reservaId));

        return converterParaDTO(conta, hospedesPorReserva, extratosPorReserva);
    }

    // ========== CONVERTER PARA DTO (NÚCLEO - RECEBE OS DADOS JÁ CARREGADOS) ==========

    private ContaAReceberDTO converterParaDTO(ContaAReceber conta,
                                               Map<Long, List<HospedagemHospede>> hospedesPorReserva,
                                               Map<Long, List<ExtratoReserva>> extratosPorReserva) {
        ContaAReceberDTO dto = new ContaAReceberDTO();
        dto.setId(conta.getId());
        dto.setReservaId(conta.getReserva().getId());
        dto.setClienteNome(conta.getCliente().getNome());

        Long reservaId = conta.getReserva() != null ? conta.getReserva().getId() : null;

        if (reservaId != null) {
            List<HospedagemHospede> hospedes = hospedesPorReserva.getOrDefault(reservaId, Collections.emptyList());
            String todosHospedes = hospedes.stream()
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
            dto.setDataCheckin(reserva.getDataCheckin() != null ? reserva.getDataCheckin().toLocalDate() : null);
            dto.setDataCheckout(reserva.getDataCheckout() != null ? reserva.getDataCheckout().toLocalDate() : null);
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
            List<ExtratoReserva> extratos = reservaId != null
                    ? extratosPorReserva.getOrDefault(reservaId, Collections.emptyList())
                    : Collections.emptyList();

            BigDecimal debitoEmConta = extratos.stream()
                .filter(e -> e.getDescricao() != null && e.getDescricao().contains("DEBITO EM CONTA"))
                .map(e -> e.getTotalLancamento().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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
            conta.setValorPago(conta.getValor());
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