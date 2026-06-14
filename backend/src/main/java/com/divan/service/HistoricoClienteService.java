package com.divan.service;

import com.divan.dto.HistoricoClienteDTO;
import com.divan.dto.ResumoClienteDTO;
import com.divan.entity.Cliente;
import com.divan.entity.HospedagemHospede;
import com.divan.entity.Reserva;
import com.divan.repository.ClienteRepository;
import com.divan.repository.HistoricoClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class HistoricoClienteService {

    private final HistoricoClienteRepository historicoRepository;
    private final ClienteRepository clienteRepository;

    public HistoricoClienteService(HistoricoClienteRepository historicoRepository,
                                   ClienteRepository clienteRepository) {
        this.historicoRepository = historicoRepository;
        this.clienteRepository   = clienteRepository;
    }

    @Transactional(readOnly = true)
    public ResumoClienteDTO buscarHistorico(Long clienteId) {

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + clienteId));

        List<HospedagemHospede> hospedagens = historicoRepository.findByClienteId(clienteId);

        return montarResumo(cliente, hospedagens);
    }

    private ResumoClienteDTO montarResumo(Cliente cliente, List<HospedagemHospede> hospedagens) {

        ResumoClienteDTO resumo = new ResumoClienteDTO();
        resumo.setClienteId(cliente.getId());
        resumo.setNomeCliente(cliente.getNome());
        resumo.setCpf(cliente.getCpf());
        resumo.setCelular(cliente.getCelularCompleto() != null
                ? cliente.getCelularCompleto() : cliente.getCelular());

        // Monta lista de hospedagens
        List<HistoricoClienteDTO> lista = hospedagens.stream()
                .map(this::toDTO)
                .toList();

        resumo.setHospedagens(lista);
        resumo.setTotalHospedagens(lista.size());

        // Total de dias hospedado
        int totalDias = lista.stream()
                .mapToInt(h -> h.getQuantidadeDiarias() != null ? h.getQuantidadeDiarias() : 0)
                .sum();
        resumo.setTotalDiasHospedado(totalDias);

        // Total gasto
        BigDecimal totalGasto = lista.stream()
                .map(h -> h.getTotalHospedagem() != null ? h.getTotalHospedagem() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        resumo.setTotalGasto(totalGasto);

        // Primeira e última hospedagem
        lista.stream()
                .map(HistoricoClienteDTO::getDataCheckin)
                .min(LocalDateTime::compareTo)
                .ifPresent(resumo::setPrimeiraHospedagem);

        lista.stream()
                .map(HistoricoClienteDTO::getDataCheckin)
                .max(LocalDateTime::compareTo)
                .ifPresent(resumo::setUltimaHospedagem);

        // Dias desde última hospedagem (usa checkout real se disponível)
        hospedagens.stream()
                .map(h -> h.getReserva().getDataCheckoutReal() != null
                        ? h.getReserva().getDataCheckoutReal()
                        : h.getReserva().getDataCheckout())
                .max(LocalDateTime::compareTo)
                .ifPresent(ultimo -> resumo.setDiasDesdeUltimaHospedagem(
                        ChronoUnit.DAYS.between(ultimo, LocalDateTime.now())));

        // Média de diárias por hospedagem
        if (!lista.isEmpty()) {
            double media = (double) totalDias / lista.size();
            resumo.setMediaEstadia(
                    BigDecimal.valueOf(media).setScale(1, RoundingMode.HALF_UP).doubleValue());
        }

        return resumo;
    }

    private HistoricoClienteDTO toDTO(HospedagemHospede h) {
        HistoricoClienteDTO dto = new HistoricoClienteDTO();
        Reserva r = h.getReserva();

        dto.setReservaId(r.getId());
        dto.setDataCheckin(r.getDataCheckin());
        dto.setDataCheckout(r.getDataCheckout());
        dto.setDataCheckoutReal(r.getDataCheckoutReal());
        dto.setQuantidadeDiarias(r.getQuantidadeDiaria());
        dto.setQuantidadeHospedes(r.getQuantidadeHospede());
        dto.setTotalHospedagem(r.getTotalHospedagem());
        dto.setTotalRecebido(r.getTotalRecebido());
        dto.setStatus(r.getStatus() != null ? r.getStatus().name() : null);
        dto.setTitular(h.isTitular());

        if (r.getApartamento() != null) {
            dto.setNumeroApartamento(r.getApartamento().getNumeroApartamento());
            if (r.getApartamento().getTipoApartamento() != null) {
            	dto.setTipoApartamento(r.getApartamento().getTipoApartamento().getTipo().name());
            }
        }

        return dto;
    }
}
