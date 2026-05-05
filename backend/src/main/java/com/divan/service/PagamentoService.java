package com.divan.service;

import com.divan.dto.ResumoPagamentosDTO;
import com.divan.entity.Apartamento;
import com.divan.entity.ContaAReceber;
import com.divan.entity.ExtratoReserva;
import com.divan.entity.Pagamento;
import com.divan.entity.Reserva;
import com.divan.repository.ExtratoReservaRepository;
import com.divan.repository.PagamentoRepository;
import com.divan.repository.ReservaRepository;
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
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ExtratoReservaRepository extratoRepository;

    @Autowired
    private ApartamentoRepository apartamentoRepository;

    @Autowired
    private ContaAReceberRepository contaAReceberRepository;


    public Pagamento processarPagamento(Pagamento pagamento) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("💰 PROCESSANDO PAGAMENTO");
        System.out.println("═══════════════════════════════════════════");

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

        if (reserva.getStatus() != Reserva.StatusReservaEnum.ATIVA) {
            throw new RuntimeException("Não é possível adicionar pagamento a uma reserva não ativa");
        }

        if (pagamento.getValor().compareTo(reserva.getTotalApagar()) > 0) {
            throw new RuntimeException("Valor do pagamento excede o saldo devedor de R$ " + reserva.getTotalApagar());
        }

        // ✅ Marcar como PAGAMENTO normal
        pagamento.setTipo(Pagamento.TipoPagamentoEnum.PAGAMENTO);
        pagamento.setDataHoraPagamento(LocalDateTime.now());
        pagamento.setReserva(reserva);
        Pagamento pagamentoSalvo = pagamentoRepository.save(pagamento);

        System.out.println("✅ Pagamento registrado: R$ " + pagamento.getValor());

        atualizarTotalRecebidoReserva(reserva.getId(), pagamento.getValor());
        criarExtratoPagamento(pagamento);

        reserva = reservaRepository.findById(reserva.getId()).get();

        System.out.println("📊 VALORES APÓS O PAGAMENTO:");
        System.out.println("   Total Recebido: R$ " + reserva.getTotalRecebido());
        System.out.println("   Total A Pagar: R$ " + reserva.getTotalApagar());
        System.out.println("═══════════════════════════════════════════");

        return pagamentoSalvo;
    }

    /**
     * ✅ NOVO — Processa um adiantamento (crédito do hóspede para consumos futuros).
     * Só permitido quando a reserva está com saldo zerado.
     */
    /**
     * ✅ Processa um adiantamento (mesma lógica de pagamento, mas SEM validar saldo devedor).
     * Permite que o totalApagar fique negativo, indicando crédito do cliente.
     */
    public Pagamento processarAdiantamento(Pagamento pagamento) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("💵 PROCESSANDO ADIANTAMENTO");
        System.out.println("═══════════════════════════════════════════");

        Optional<Reserva> reservaOpt = reservaRepository.findById(pagamento.getReserva().getId());
        if (reservaOpt.isEmpty()) {
            throw new RuntimeException("Reserva não encontrada");
        }

        Reserva reserva = reservaOpt.get();

        if (reserva.getStatus() != Reserva.StatusReservaEnum.ATIVA) {
            throw new RuntimeException("Adiantamento só pode ser registrado em reservas ATIVAS");
        }

        if (reserva.getTotalApagar().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException(
                "Reserva ainda possui saldo devedor de R$ " + reserva.getTotalApagar() +
                ". Quite o saldo antes de registrar adiantamento."
            );
        }

        if (pagamento.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valor do adiantamento deve ser maior que zero");
        }

        System.out.println("📊 ANTES:");
        System.out.println("   Total Recebido: R$ " + reserva.getTotalRecebido());
        System.out.println("   Total A Pagar: R$ " + reserva.getTotalApagar());
        System.out.println("   Valor do Adiantamento: R$ " + pagamento.getValor());

        // Marcar como ADIANTAMENTO (mas trata como pagamento)
        pagamento.setTipo(Pagamento.TipoPagamentoEnum.ADIANTAMENTO);
        pagamento.setDataHoraPagamento(LocalDateTime.now());
        pagamento.setReserva(reserva);
        Pagamento pagamentoSalvo = pagamentoRepository.save(pagamento);

        // ✅ Soma ao totalRecebido (igual pagamento normal)
        reserva.setTotalRecebido(reserva.getTotalRecebido().add(pagamento.getValor()));
        reserva.setTotalApagar(reserva.getTotalHospedagem()
            .subtract(reserva.getTotalRecebido())
            .subtract(reserva.getDesconto()));
        reservaRepository.save(reserva);

        // Criar extrato como PAGAMENTO (afeta saldo, valor negativo)
        ExtratoReserva extrato = new ExtratoReserva();
        extrato.setReserva(reserva);
        extrato.setDataHoraLancamento(pagamento.getDataHoraPagamento());
        extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.ADIANTAMENTO);
        extrato.setTotalLancamento(pagamento.getValor().negate()); // ✅ Negativo: reduz saldo
        extrato.setDescricao("Adiantamento - " + pagamento.getFormaPagamento().name().replace("_", " "));
        extrato.setValorUnitario(pagamento.getValor());
        extrato.setQuantidade(1);
        extratoRepository.save(extrato);

        System.out.println("📊 DEPOIS:");
        System.out.println("   Total Recebido: R$ " + reserva.getTotalRecebido());
        System.out.println("   Total A Pagar: R$ " + reserva.getTotalApagar() + " (negativo = crédito do cliente)");
        System.out.println("✅ Adiantamento registrado!");
        System.out.println("═══════════════════════════════════════════");

        return pagamentoSalvo;
    }
    /**
     * ✅ NOVO — Usa parte do crédito de adiantamento para abater um consumo.
     * Retorna o valor efetivamente abatido (pode ser menor que o solicitado se não houver saldo suficiente).
     */
    
    private void atualizarTotalRecebidoReserva(Long reservaId, BigDecimal valorPago) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(reservaId);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            reserva.setTotalRecebido(reserva.getTotalRecebido().add(valorPago));
            reserva.setTotalApagar(reserva.getTotalHospedagem()
                .subtract(reserva.getTotalRecebido())
                .subtract(reserva.getDesconto()));

            reservaRepository.save(reserva);
        }
    }

    private void criarExtratoPagamento(Pagamento pagamento) {
        ExtratoReserva extrato = new ExtratoReserva();
        extrato.setReserva(pagamento.getReserva());
        extrato.setDataHoraLancamento(pagamento.getDataHoraPagamento());
        extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.PAGAMENTO);
        extrato.setTotalLancamento(pagamento.getValor().negate());
        extrato.setDescricao("Pagamento " + pagamento.getFormaPagamento().name().replace("_", " "));
        extrato.setValorUnitario(pagamento.getValor());

        extratoRepository.save(extrato);
    }

    @Transactional(readOnly = true)
    public List<Pagamento> buscarPorReserva(Long reservaId) {
        Optional<Reserva> reserva = reservaRepository.findById(reservaId);
        return reserva.map(pagamentoRepository::findByReserva).orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<Pagamento> buscarPagamentosDoDia(LocalDateTime data) {
        return pagamentoRepository.findPagamentosDoDia(data);
    }

    @Transactional(readOnly = true)
    public List<Pagamento> buscarPagamentosPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return pagamentoRepository.findPagamentosPorPeriodo(inicio, fim);
    }

    @Transactional(readOnly = true)
    public ResumoPagamentosDTO gerarResumoDoDia(LocalDateTime data) {
        List<Pagamento> pagamentos = pagamentoRepository.findPagamentosDoDia(data);

        Map<Pagamento.FormaPagamentoEnum, BigDecimal> totaisPorForma = new HashMap<>();
        for (Pagamento.FormaPagamentoEnum forma : Pagamento.FormaPagamentoEnum.values()) {
            totaisPorForma.put(forma, BigDecimal.ZERO);
        }

        BigDecimal totalGeral = BigDecimal.ZERO;

        for (Pagamento p : pagamentos) {
            BigDecimal atual = totaisPorForma.get(p.getFormaPagamento());
            totaisPorForma.put(p.getFormaPagamento(), atual.add(p.getValor()));
            totalGeral = totalGeral.add(p.getValor());
        }

        ResumoPagamentosDTO resumo = new ResumoPagamentosDTO();
        resumo.setTotalDinheiro(totaisPorForma.get(Pagamento.FormaPagamentoEnum.DINHEIRO));
        resumo.setTotalPix(totaisPorForma.get(Pagamento.FormaPagamentoEnum.PIX));
        resumo.setTotalCartaoDebito(totaisPorForma.get(Pagamento.FormaPagamentoEnum.CARTAO_DEBITO));
        resumo.setTotalCartaoCredito(totaisPorForma.get(Pagamento.FormaPagamentoEnum.CARTAO_CREDITO));
        resumo.setTotalTransferencia(totaisPorForma.get(Pagamento.FormaPagamentoEnum.TRANSFERENCIA_BANCARIA));
        resumo.setTotalLinkPix(totaisPorForma.get(Pagamento.FormaPagamentoEnum.LINK_PIX));       // ← adicionar
        resumo.setTotalLinkCartao(totaisPorForma.get(Pagamento.FormaPagamentoEnum.LINK_CARTAO)); // ← adicionar
        resumo.setTotalGeral(totalGeral);
        resumo.setQuantidadePagamentos(pagamentos.size());

        return resumo;
    }
}
