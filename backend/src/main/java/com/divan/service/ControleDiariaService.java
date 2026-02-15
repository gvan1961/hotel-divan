package com.divan.service;

import com.divan.entity.*;
import com.divan.entity.ControleDiaria.StatusDiariaEnum;
import com.divan.entity.ExtratoReserva.StatusLancamentoEnum;
import com.divan.repository.*;
import com.divan.util.DataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ControleDiariaService {
    
    @Autowired
    private ControleDiariaRepository controleDiariaRepository;
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private ExtratoReservaRepository extratoReservaRepository;
    
    @Autowired
    private DiariaRepository diariaRepository;
    
    /**
     * ✅ LANÇAR DIÁRIA (no check-in ou às 12h01 automaticamente)
     */
    public ControleDiaria lancarDiaria(Reserva reserva) {
        System.out.println("════════════════════════════════════");
        System.out.println("📝 LANÇANDO NOVA DIÁRIA");
        System.out.println("════════════════════════════════════");
        System.out.println("📋 Reserva #" + reserva.getId());
        System.out.println("🏨 Apartamento: " + reserva.getApartamento().getNumeroApartamento());
        System.out.println("👥 Hóspedes: " + reserva.getQuantidadeHospede());
        
        // Buscar diária aplicável
        TipoApartamento tipoApartamento = reserva.getApartamento().getTipoApartamento();
        Optional<Diaria> diariaOpt = diariaRepository.findByTipoApartamentoAndQuantidade(
            tipoApartamento, 
            reserva.getQuantidadeHospede()
        );
        
        if (diariaOpt.isEmpty()) {
            throw new RuntimeException("Diária não encontrada para tipo " + 
                tipoApartamento.getTipo() + " com " + reserva.getQuantidadeHospede() + " hóspede(s)");
        }
        
        Diaria diaria = diariaOpt.get();
        BigDecimal valorDiaria = diaria.getValor();
        
        System.out.println("💰 Valor da diária: R$ " + valorDiaria);
        
        // Criar controle de diária
        ControleDiaria controle = new ControleDiaria();
        controle.setReserva(reserva);
        controle.setDataLancamento(LocalDateTime.now());
        controle.setValor(valorDiaria);
        controle.setStatus(StatusDiariaEnum.LANCADA);
        controle.setQuantidadeHospedes(reserva.getQuantidadeHospede());
        
        controle = controleDiariaRepository.save(controle);
        
        System.out.println("✅ Diária LANÇADA com sucesso!");
        System.out.println("🆔 Controle ID: " + controle.getId());
        System.out.println("📅 Data lançamento: " + DataUtil.formatarDataHora(controle.getDataLancamento()));
        System.out.println("🔒 Será fechada no próximo dia às 12h01");
        System.out.println("════════════════════════════════════");
        
        return controle;
    }
    
    /**
     * 🔒 FECHAR DIÁRIA (às 12h01 automaticamente)
     */
    public void fecharDiaria(ControleDiaria controle) {
        System.out.println("════════════════════════════════════");
        System.out.println("🔒 FECHANDO DIÁRIA");
        System.out.println("════════════════════════════════════");
        System.out.println("🆔 Controle ID: " + controle.getId());
        System.out.println("📋 Reserva #" + controle.getReserva().getId());
        
        Reserva reserva = controle.getReserva();
        
        // Lançar no extrato
        ExtratoReserva extrato = new ExtratoReserva();
        extrato.setReserva(reserva);
        extrato.setDataHoraLancamento(LocalDateTime.now());
        extrato.setStatusLancamento(StatusLancamentoEnum.DIARIA);
        extrato.setDescricao(String.format(
            "Diária %s - %d hóspede(s)",
            DataUtil.formatarData(controle.getDataLancamento()),
            controle.getQuantidadeHospedes()
        ));
        extrato.setQuantidade(1);
        extrato.setValorUnitario(controle.getValor());
        extrato.setTotalLancamento(controle.getValor());
        
        extrato = extratoReservaRepository.save(extrato);
        
        System.out.println("📊 Extrato criado ID: " + extrato.getId());
        
        // Atualizar controle
        controle.setStatus(StatusDiariaEnum.FECHADA);
        controle.setDataFechamento(LocalDateTime.now());
        controle.setExtratoId(extrato.getId());
        controleDiariaRepository.save(controle);
        
        // Atualizar totais da reserva
        BigDecimal totalDiariaAtual = reserva.getTotalDiaria() != null ? 
            reserva.getTotalDiaria() : BigDecimal.ZERO;
        
        BigDecimal novoTotalDiaria = totalDiariaAtual.add(controle.getValor());
        reserva.setTotalDiaria(novoTotalDiaria);
        
        // Recalcular total da hospedagem
        BigDecimal totalProduto = reserva.getTotalProduto() != null ? 
            reserva.getTotalProduto() : BigDecimal.ZERO;
        BigDecimal desconto = reserva.getDesconto() != null ? 
            reserva.getDesconto() : BigDecimal.ZERO;
        
        BigDecimal totalHospedagem = novoTotalDiaria.add(totalProduto).subtract(desconto);
        reserva.setTotalHospedagem(totalHospedagem);
        
        // Recalcular saldo
        BigDecimal totalRecebido = reserva.getTotalRecebido() != null ? 
            reserva.getTotalRecebido() : BigDecimal.ZERO;
        reserva.setTotalApagar(totalHospedagem.subtract(totalRecebido));
        
        reservaRepository.save(reserva);
        
        System.out.println("💰 Total diárias: R$ " + novoTotalDiaria);
        System.out.println("💳 Total hospedagem: R$ " + totalHospedagem);
        System.out.println("📊 Saldo devedor: R$ " + reserva.getTotalApagar());
        System.out.println("✅ Diária FECHADA com sucesso!");
        System.out.println("════════════════════════════════════");
    }
    
    /**
     * ❌ CANCELAR DIÁRIA (quando hóspede faz checkout antes de fechar)
     */
    public void cancelarDiaria(ControleDiaria controle, String motivo) {
        System.out.println("════════════════════════════════════");
        System.out.println("❌ CANCELANDO DIÁRIA");
        System.out.println("════════════════════════════════════");
        System.out.println("🆔 Controle ID: " + controle.getId());
        System.out.println("📋 Motivo: " + motivo);
        
        controle.setStatus(StatusDiariaEnum.CANCELADA);
        controle.setDataFechamento(LocalDateTime.now());
        controleDiariaRepository.save(controle);
        
        System.out.println("✅ Diária cancelada!");
        System.out.println("════════════════════════════════════");
    }
    
    /**
     * 📋 LISTAR DIÁRIAS DE UMA RESERVA
     */
    public List<ControleDiaria> listarDiariasPorReserva(Long reservaId) {
        return controleDiariaRepository.findByReservaId(reservaId);
    }
    
    /**
     * 🔍 BUSCAR DIÁRIAS PARA FECHAR (chamado pelo job às 12h01)
     */
    public List<ControleDiaria> buscarDiariasParaFechar() {
        LocalDateTime agora = LocalDateTime.now();
        return controleDiariaRepository.findDiariasParaFechar(agora);
    }
    
    /**
     * Busca diárias LANCADAS de uma reserva específica
     */
    public List<ControleDiaria> buscarDiariasLancadasPorReserva(Long reservaId) {
        System.out.println("🔍 Buscando diárias LANCADAS da reserva #" + reservaId);
        
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        List<ControleDiaria> diarias = controleDiariaRepository.findByReserva(reserva).stream()
            .filter(d -> d.getStatus() == StatusDiariaEnum.LANCADA)
            .collect(Collectors.toList());
        
        System.out.println("📊 Encontradas " + diarias.size() + " diária(s) LANCADA(s)");
        
        return diarias;
    }
}
