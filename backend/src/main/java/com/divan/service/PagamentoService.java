package com.divan.service;

import com.divan.dto.ResumoPagamentosDTO;
import com.divan.entity.Apartamento;
import com.divan.entity.Apartamento.StatusEnum;
import com.divan.entity.ContaAReceber;
import com.divan.entity.ExtratoReserva;
import com.divan.entity.ExtratoReserva.StatusLancamentoEnum;
import com.divan.entity.FechamentoCaixa;
import com.divan.entity.LogMovimentacaoCaixa;
import com.divan.entity.Pagamento;
import com.divan.entity.Reserva;
import com.divan.entity.Reserva.StatusReservaEnum;
import com.divan.entity.Usuario;
import com.divan.enums.FormaPagamento;  // ✅ IMPORT CORRETO
import com.divan.repository.ExtratoReservaRepository;
import com.divan.repository.LogMovimentacaoCaixaRepository;
import com.divan.repository.PagamentoRepository;
import com.divan.repository.ReservaRepository;
import com.divan.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.divan.repository.ContaAReceberRepository;
import java.time.LocalDate;
import com.divan.repository.ApartamentoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PagamentoService {
    
    @Autowired
    private CaixaValidacaoService caixaValidacaoService;

    @Autowired
    private LogMovimentacaoCaixaRepository logRepository;
    
    @Autowired
    private PagamentoRepository pagamentoRepository;
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ExtratoReservaRepository extratoRepository;  
    
    @Autowired
    private ApartamentoRepository apartamentoRepository;

    @Autowired
    private ContaAReceberRepository contaAReceberRepository;
    
    /**
     * ✅ PROCESSAR PAGAMENTO (SEM FINALIZAR RESERVA)
     * - Registra o pagamento
     * - Atualiza totais
     * - Cria extrato
     * - MANTÉM reserva ATIVA (mesmo se zerar saldo)
     */
    public Pagamento processarPagamento(Pagamento pagamento, Long usuarioId) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("💰 PROCESSANDO PAGAMENTO");
        System.out.println("═══════════════════════════════════════════");
        
        // ✅ VALIDAR SE CAIXA ESTÁ ABERTO
        FechamentoCaixa caixa = caixaValidacaoService.validarCaixaAberto(usuarioId);
        
        // Buscar reserva
        Optional<Reserva> reservaOpt = reservaRepository.findById(pagamento.getReserva().getId());
        if (reservaOpt.isEmpty()) {
            throw new RuntimeException("Reserva não encontrada");
        }
        
        Reserva reserva = reservaOpt.get();
        
        System.out.println("📊 VALORES ANTES DO PAGAMENTO:");
        System.out.println("   Total Hospedagem: R$ " + reserva.getTotalHospedagem());
        System.out.println("   Total Recebido: R$ " + reserva.getTotalRecebido());
        System.out.println("   Total A Pagar: R$ " + reserva.getTotalApagar());
        System.out.println("   Valor do Pagamento: R$ " + pagamento.getValor());
        
        // Validar se reserva está ativa ou pré-reserva
        if (reserva.getStatus() != Reserva.StatusReservaEnum.ATIVA && 
            reserva.getStatus() != Reserva.StatusReservaEnum.PRE_RESERVA) {
            throw new RuntimeException("Não é possível adicionar pagamento a uma reserva FINALIZADA ou CANCELADA");
        }
        
        // Se for PRÉ-RESERVA, ativar automaticamente
        if (reserva.getStatus() == Reserva.StatusReservaEnum.PRE_RESERVA) {
            System.out.println("⚠️ Reserva estava em PRE_RESERVA, ativando automaticamente...");
            reserva.setStatus(Reserva.StatusReservaEnum.ATIVA);
            reservaRepository.save(reserva);
            
            // Atualizar apartamento para OCUPADO
            if (reserva.getApartamento() != null) {
                Apartamento apartamento = reserva.getApartamento();
                apartamento.setStatus(Apartamento.StatusEnum.OCUPADO);
                apartamentoRepository.save(apartamento);
                System.out.println("✅ Apartamento atualizado para OCUPADO");
            }
        }
        
        // Validar se valor não excede saldo devedor
        if (pagamento.getValor().compareTo(reserva.getTotalApagar()) > 0) {
            throw new RuntimeException("Valor do pagamento excede o saldo devedor de R$ " + reserva.getTotalApagar());
        }
        
        // ✅ Salvar pagamento com dataHora
        pagamento.setDataHora(LocalDateTime.now());
        pagamento.setReserva(reserva);
        pagamento.setCaixa(caixa);  // ✅ Associar ao caixa
        Pagamento pagamentoSalvo = pagamentoRepository.save(pagamento);
        
        System.out.println("✅ Pagamento registrado: R$ " + pagamento.getValor());
        
        // Atualizar totais da reserva
        atualizarTotalRecebidoReserva(reserva.getId(), pagamento.getValor());
        
        // Criar extrato de pagamento
        criarExtratoPagamento(pagamento);
        
        // ✅ REGISTRAR LOG
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        LogMovimentacaoCaixa log = new LogMovimentacaoCaixa();
        log.setCaixa(caixa);
        log.setUsuario(usuario);
        log.setTipoOperacao("PAGAMENTO");
        log.setDescricao("Pagamento " + pagamento.getFormaPagamento() + " - R$ " + pagamento.getValor());
        log.setDataHora(LocalDateTime.now());
        log.setReservaId(reserva.getId());
        log.setPagamentoId(pagamentoSalvo.getId());
        
        logRepository.save(log);
        
        // Buscar reserva atualizada
        reserva = reservaRepository.findById(reserva.getId()).get();
        
        System.out.println("📊 VALORES APÓS O PAGAMENTO:");
        System.out.println("   Total Recebido: R$ " + reserva.getTotalRecebido());
        System.out.println("   Total A Pagar: R$ " + reserva.getTotalApagar());
        
        if (reserva.getTotalApagar().compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("═══════════════════════════════════════════");
            System.out.println("💚 PAGAMENTO TOTAL EFETUADO!");
            System.out.println("✅ Reserva continua ATIVA");
            System.out.println("💡 Use 'Finalizar Paga' para fazer checkout");
            System.out.println("═══════════════════════════════════════════");
        } else {
            System.out.println("⚠️ Pagamento PARCIAL - Saldo restante: R$ " + reserva.getTotalApagar());
            System.out.println("═══════════════════════════════════════════");
        }
        
        return pagamentoSalvo;
    }
    
    
    /**
     * ✅ ATUALIZAR TOTAL RECEBIDO DA RESERVA
     */
    private void atualizarTotalRecebidoReserva(Long reservaId, BigDecimal valorPago) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(reservaId);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            reserva.setTotalRecebido(reserva.getTotalRecebido().add(valorPago));
            reserva.setTotalApagar(reserva.getTotalHospedagem()
                .subtract(reserva.getTotalRecebido())
                .subtract(reserva.getDesconto() != null ? reserva.getDesconto() : BigDecimal.ZERO));
            
            reservaRepository.save(reserva);
        }
    }
    
    /**
     * ✅ CRIAR EXTRATO DE PAGAMENTO
     */
    private void criarExtratoPagamento(Pagamento pagamento) {
        ExtratoReserva extrato = new ExtratoReserva();
        extrato.setReserva(pagamento.getReserva());
        extrato.setDataHoraLancamento(pagamento.getDataHora());
        extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.PAGAMENTO);
        extrato.setTotalLancamento(pagamento.getValor().negate()); // Negativo = crédito
        extrato.setDescricao("Pagamento " + pagamento.getFormaPagamento().name().replace("_", " "));
        extrato.setValorUnitario(pagamento.getValor());
        
        extratoRepository.save(extrato);
    }
    
    /**
     * 💳 FINALIZAR RESERVA PAGA (NOVO MÉTODO)
     * - Verifica se está totalmente pago
     * - Finaliza reserva
     * - Libera apartamento para LIMPEZA
     * - Cria registro em Contas a Receber como PAGA
     */
    @Transactional
    public void finalizarReservaPaga(Long reservaId) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("💳 FINALIZANDO RESERVA PAGA");
        System.out.println("═══════════════════════════════════════════");
        
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        System.out.println("📊 Reserva #" + reserva.getId());
        System.out.println("   Status: " + reserva.getStatus());
        System.out.println("   Total Hospedagem: R$ " + reserva.getTotalHospedagem());
        System.out.println("   Total Recebido: R$ " + reserva.getTotalRecebido());
        System.out.println("   Total A Pagar: R$ " + reserva.getTotalApagar());
        
        // ✅ VALIDAÇÃO: Só pode finalizar se estiver ATIVA
        if (reserva.getStatus() != StatusReservaEnum.ATIVA) {
            throw new RuntimeException("Apenas reservas ATIVAS podem ser finalizadas");
        }
        
        // ✅ VALIDAÇÃO: Deve estar totalmente paga
        if (reserva.getTotalApagar().compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException(
                "❌ NÃO É POSSÍVEL FINALIZAR COMO PAGA!\n\n" +
                "Ainda há saldo devedor de R$ " + reserva.getTotalApagar() + "\n\n" +
                "Use 'Finalizar Faturada' para enviar o saldo para Contas a Receber."
            );
        }
        
        System.out.println("✅ Validação OK - Reserva totalmente paga");
        
        // 1️⃣ Finalizar reserva
        reserva.setStatus(StatusReservaEnum.FINALIZADA);
        reservaRepository.save(reserva);
        System.out.println("✅ Reserva finalizada");
        
        // 2️⃣ Liberar apartamento para LIMPEZA
        Apartamento apartamento = reserva.getApartamento();
        apartamento.setStatus(StatusEnum.LIMPEZA);
        apartamentoRepository.save(apartamento);
        System.out.println("🧹 Apartamento " + apartamento.getNumeroApartamento() + " → LIMPEZA");
        
        // 3️⃣ Criar registro em Contas a Receber (PAGA)
        criarContaAReceberPaga(reserva);
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("✅ FINALIZAÇÃO PAGA CONCLUÍDA!");
        System.out.println("═══════════════════════════════════════════");
    }
    
    /**
     * ✅ CRIAR CONTA A RECEBER (PAGA)
     */
    private void criarContaAReceberPaga(Reserva reserva) {
        try {
            ContaAReceber conta = new ContaAReceber();
            
            // Dados da reserva
            conta.setReserva(reserva);
            conta.setCliente(reserva.getCliente());
            
            // Valores
            conta.setValor(reserva.getTotalHospedagem()); // Valor total original
            conta.setValorPago(reserva.getTotalRecebido()); // Valor recebido
            conta.setSaldo(BigDecimal.ZERO); // Sem saldo devedor
            
            // Status e datas
            conta.setStatus(ContaAReceber.StatusContaEnum.PAGA);
            conta.setDataPagamento(LocalDate.now());
            conta.setDataVencimento(LocalDate.now());
            conta.setDataCriacao(LocalDateTime.now());
            
            // Descrição
            conta.setDescricao("Reserva PAGA #" + reserva.getId() + 
                              " - Apt " + reserva.getApartamento().getNumeroApartamento());
            conta.setObservacao("Pagamento à vista no checkout");
            
            // Empresa (se existir)
            if (reserva.getCliente().getEmpresa() != null) {
                conta.setEmpresa(reserva.getCliente().getEmpresa());
            }
            
            contaAReceberRepository.save(conta);
            
            System.out.println("💚 Registro PAGO criado em Contas a Receber!");
            System.out.println("   Valor: R$ " + conta.getValor());
            System.out.println("   Valor Pago: R$ " + conta.getValorPago());
            System.out.println("   Saldo: R$ " + conta.getSaldo());
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar Conta a Receber: " + e.getMessage());
            throw new RuntimeException("Erro ao criar registro financeiro: " + e.getMessage());
        }
    }
    
    /**
     * ✅ PROCESSAR PAGAMENTO DE PRÉ-RESERVA
     */
    @Transactional
    public Pagamento processarPagamentoPreReserva(Pagamento pagamento) {
        System.out.println("🔹 PagamentoService.processarPagamentoPreReserva()");
        
        Reserva reserva = pagamento.getReserva();
        
        // 1️⃣ Salvar pagamento
        System.out.println("   1️⃣ Salvando pagamento...");
        pagamento.setDataHora(LocalDateTime.now());
        Pagamento pagamentoSalvo = pagamentoRepository.save(pagamento);
        System.out.println("   ✅ Pagamento salvo: ID=" + pagamentoSalvo.getId());
        
        // 2️⃣ Adicionar ao extrato
        System.out.println("   2️⃣ Adicionando ao extrato...");
        ExtratoReserva extrato = new ExtratoReserva();
        extrato.setReserva(reserva);
        extrato.setDescricao("Pagamento - " + pagamento.getFormaPagamento().name());
        extrato.setQuantidade(1);
        extrato.setValorUnitario(pagamento.getValor().negate());
        extrato.setTotalLancamento(pagamento.getValor().negate());
        extrato.setStatusLancamento(StatusLancamentoEnum.PAGAMENTO);
        extrato.setDataHoraLancamento(LocalDateTime.now());
        
        if (pagamento.getDescricao() != null && !pagamento.getDescricao().isEmpty()) {
            extrato.setDescricao(extrato.getDescricao() + " - " + pagamento.getDescricao());
        }
        
        extratoRepository.save(extrato);
        System.out.println("   ✅ Extrato adicionado");
        
        // 3️⃣ Recalcular totais
        System.out.println("   3️⃣ Recalculando totais...");
        recalcularValores(reserva);
        System.out.println("   ✅ Totais recalculados");
        
        // 4️⃣ ATIVAR A RESERVA
        System.out.println("   4️⃣ Ativando reserva...");
        reserva.setStatus(StatusReservaEnum.ATIVA);
        reservaRepository.save(reserva);
        System.out.println("   ✅ Reserva ativada: Status=" + reserva.getStatus());
        
        // 5️⃣ ATUALIZAR STATUS DO APARTAMENTO PARA OCUPADO
        if (reserva.getApartamento() != null) {
            System.out.println("   5️⃣ Atualizando apartamento para OCUPADO...");
            Apartamento apartamento = reserva.getApartamento();
            apartamento.setStatus(StatusEnum.OCUPADO);
            apartamentoRepository.save(apartamento);
            System.out.println("   ✅ Apartamento atualizado: Status=" + apartamento.getStatus());
        } else {
            System.err.println("   ⚠️ Apartamento não encontrado na reserva!");
        }
        
        System.out.println("🔹 Processo concluído com sucesso!");
        
        return pagamentoSalvo;
    }
    
    /**
     * ✅ RECALCULAR VALORES DA RESERVA
     */
    private void recalcularValores(Reserva reserva) {
        System.out.println("   🔹 Recalculando valores da reserva...");
        
        List<ExtratoReserva> extratos = extratoRepository.findByReservaOrderByDataHoraLancamento(reserva);
        
        BigDecimal totalHospedagem = BigDecimal.ZERO;
        BigDecimal totalConsumo = BigDecimal.ZERO;
        BigDecimal totalRecebido = BigDecimal.ZERO;
        
        for (ExtratoReserva extrato : extratos) {
            BigDecimal total = extrato.getTotalLancamento();
            
            if (extrato.getStatusLancamento() == StatusLancamentoEnum.DIARIA) {
                totalHospedagem = totalHospedagem.add(total);
            } else if (extrato.getStatusLancamento() == StatusLancamentoEnum.PRODUTO) {
                totalConsumo = totalConsumo.add(total);
            } else if (extrato.getStatusLancamento() == StatusLancamentoEnum.PAGAMENTO) {
                totalRecebido = totalRecebido.add(total.abs());
            }
        }
        
        reserva.setTotalHospedagem(totalHospedagem);
        reserva.setTotalProduto(totalConsumo);
        reserva.setTotalRecebido(totalRecebido);
        
        BigDecimal desconto = reserva.getDesconto() != null ? reserva.getDesconto() : BigDecimal.ZERO;
        BigDecimal totalAPagar = totalHospedagem.add(totalConsumo).subtract(totalRecebido).subtract(desconto);
        reserva.setTotalApagar(totalAPagar);
        
        reservaRepository.save(reserva);
        
        System.out.println("   ✅ Valores recalculados:");
        System.out.println("      Total Hospedagem: R$ " + totalHospedagem);
        System.out.println("      Total Consumo: R$ " + totalConsumo);
        System.out.println("      Total Recebido: R$ " + totalRecebido);
        System.out.println("      Total a Pagar: R$ " + totalAPagar);
    }
    
    // ============================================
    // ✅ MÉTODOS DE CONSULTA
    // ============================================
    
    @Transactional(readOnly = true)
    public List<Pagamento> buscarPorReserva(Long reservaId) {
        return pagamentoRepository.findByReservaId(reservaId);
    }
    
    @Transactional(readOnly = true)
    public List<Pagamento> buscarPagamentosDoDia(LocalDate data) {
        return pagamentoRepository.findPagamentosDoDia(data);
    }
    
    @Transactional(readOnly = true)
    public List<Pagamento> buscarPagamentosPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return pagamentoRepository.findPagamentosPorPeriodo(inicio, fim);
    }
    
    @Transactional(readOnly = true)
    public ResumoPagamentosDTO gerarResumoDoDia(LocalDate data) {
        List<Pagamento> pagamentos = pagamentoRepository.findPagamentosDoDia(data);
        
        // ✅ Usar o enum correto
        Map<FormaPagamento, BigDecimal> totaisPorForma = new HashMap<>();
        for (FormaPagamento forma : FormaPagamento.values()) {
            totaisPorForma.put(forma, BigDecimal.ZERO);
        }
        
        BigDecimal totalGeral = BigDecimal.ZERO;
        
        for (Pagamento p : pagamentos) {
            BigDecimal atual = totaisPorForma.get(p.getFormaPagamento());
            totaisPorForma.put(p.getFormaPagamento(), atual.add(p.getValor()));
            totalGeral = totalGeral.add(p.getValor());
        }
        
        ResumoPagamentosDTO resumo = new ResumoPagamentosDTO();
        resumo.setTotalDinheiro(totaisPorForma.get(FormaPagamento.DINHEIRO));
        resumo.setTotalPix(totaisPorForma.get(FormaPagamento.PIX));
        resumo.setTotalCartaoDebito(totaisPorForma.get(FormaPagamento.CARTAO_DEBITO));
        resumo.setTotalCartaoCredito(totaisPorForma.get(FormaPagamento.CARTAO_CREDITO));
        resumo.setTotalTransferencia(totaisPorForma.get(FormaPagamento.TRANSFERENCIA));
        resumo.setTotalGeral(totalGeral);
        resumo.setQuantidadePagamentos(pagamentos.size());
        
        return resumo;
    }
    
    /**
     * ✅ APLICAR DESCONTO NA RESERVA
     */
    @Transactional
    public void aplicarDesconto(Long reservaId, BigDecimal valorDesconto, String motivo) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("💸 APLICANDO DESCONTO");
        System.out.println("═══════════════════════════════════════════");
        
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        System.out.println("📊 ANTES DO DESCONTO:");
        System.out.println("   Total Hospedagem: R$ " + reserva.getTotalHospedagem());
        System.out.println("   Desconto Atual: R$ " + (reserva.getDesconto() != null ? reserva.getDesconto() : BigDecimal.ZERO));
        System.out.println("   Total A Pagar: R$ " + reserva.getTotalApagar());
        
        // Validações
        if (valorDesconto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valor do desconto deve ser maior que zero");
        }
        
        if (valorDesconto.compareTo(reserva.getTotalHospedagem()) > 0) {
            throw new RuntimeException("Desconto não pode ser maior que o total da hospedagem");
        }
        
        // Aplicar desconto
        BigDecimal descontoAnterior = reserva.getDesconto() != null ? reserva.getDesconto() : BigDecimal.ZERO;
        BigDecimal novoDesconto = descontoAnterior.add(valorDesconto);
        
        reserva.setDesconto(novoDesconto);
        
        BigDecimal novoTotalAPagar = reserva.getTotalHospedagem()
            .subtract(reserva.getTotalRecebido())
            .subtract(novoDesconto);
        
        reserva.setTotalApagar(novoTotalAPagar);
        reservaRepository.save(reserva);
        
        // ✅ CRIAR LANÇAMENTO NO EXTRATO
        ExtratoReserva extratoDesconto = new ExtratoReserva();
        extratoDesconto.setReserva(reserva);
        extratoDesconto.setDataHoraLancamento(LocalDateTime.now());
        extratoDesconto.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.ESTORNO);
        extratoDesconto.setDescricao("Desconto aplicado" + (motivo != null && !motivo.isEmpty() ? " - " + motivo : ""));
        extratoDesconto.setQuantidade(1);
        extratoDesconto.setValorUnitario(valorDesconto.negate());
        extratoDesconto.setTotalLancamento(valorDesconto.negate());
        extratoDesconto.setNotaVendaId(null);
        
        extratoRepository.save(extratoDesconto);
        
        System.out.println("✅ Desconto aplicado e lançado no extrato!");
        System.out.println("📊 APÓS O DESCONTO:");
        System.out.println("   Desconto Total: R$ " + novoDesconto);
        System.out.println("   Total A Pagar: R$ " + novoTotalAPagar);
        System.out.println("═══════════════════════════════════════════");
    }
}
