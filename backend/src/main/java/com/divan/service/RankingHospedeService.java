package com.divan.service;

import com.divan.dto.RankingHospedeDTO;
import com.divan.entity.HospedagemHospede;
import com.divan.entity.Reserva;
import com.divan.repository.HistoricoClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankingHospedeService {

    private final HistoricoClienteRepository repository;

    public RankingHospedeService(HistoricoClienteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RankingHospedeDTO> ranking(LocalDate inicio, LocalDate fim) {

        LocalDateTime dtInicio = inicio != null ? inicio.atStartOfDay()         : null;
        LocalDateTime dtFim    = fim    != null ? fim.atTime(23, 59, 59) : null;

        List<HospedagemHospede> hospedagens =
                repository.findTodosParaRanking(dtInicio, dtFim);

        // Agrupa por clienteId
        Map<Long, List<HospedagemHospede>> porCliente = hospedagens.stream()
                .collect(Collectors.groupingBy(h -> h.getCliente().getId()));

        List<RankingHospedeDTO> ranking = porCliente.entrySet().stream()
                .map(entry -> montarDTO(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(RankingHospedeDTO::getTotalHospedagens).reversed())
                .toList();

        return ranking;
    }

    private RankingHospedeDTO montarDTO(Long clienteId, List<HospedagemHospede> hospedagens) {
        RankingHospedeDTO dto = new RankingHospedeDTO();
        var cliente = hospedagens.get(0).getCliente();

        dto.setClienteId(clienteId);
        dto.setNomeCliente(cliente.getNome());
        dto.setCpf(cliente.getCpf());
        dto.setCelular(cliente.getCelularCompleto() != null
                ? cliente.getCelularCompleto() : cliente.getCelular());
        dto.setTotalHospedagens(hospedagens.size());

        // Total de diárias
        int totalDias = hospedagens.stream()
                .mapToInt(h -> h.getReserva().getQuantidadeDiaria() != null
                        ? h.getReserva().getQuantidadeDiaria() : 0)
                .sum();
        dto.setTotalDiasHospedado(totalDias);

        // Total gasto
        BigDecimal totalGasto = hospedagens.stream()
                .map(h -> h.getReserva().getTotalHospedagem() != null
                        ? h.getReserva().getTotalHospedagem() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalGasto(totalGasto);

        // Primeira hospedagem
        hospedagens.stream()
                .map(h -> h.getReserva().getDataCheckin())
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .ifPresent(dto::setPrimeiraHospedagem);

        // Última hospedagem
        hospedagens.stream()
                .map(h -> h.getReserva().getDataCheckin())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .ifPresent(dto::setUltimaHospedagem);

        // Dias desde última hospedagem
        hospedagens.stream()
                .map(h -> h.getReserva().getDataCheckoutReal() != null
                        ? h.getReserva().getDataCheckoutReal()
                        : h.getReserva().getDataCheckout())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .ifPresent(ultimo -> dto.setDiasDesdeUltimaHospedagem(
                        ChronoUnit.DAYS.between(ultimo, LocalDateTime.now())));

        // Média de diárias
        if (!hospedagens.isEmpty()) {
            double media = (double) totalDias / hospedagens.size();
            dto.setMediaEstadia(
                    BigDecimal.valueOf(media).setScale(1, RoundingMode.HALF_UP).doubleValue());
        }

        return dto;
    }
}
