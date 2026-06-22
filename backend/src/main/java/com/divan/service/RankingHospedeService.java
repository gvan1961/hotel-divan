package com.divan.service;


import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


import com.divan.dto.RankingHospedeDTO;
import com.divan.entity.HospedagemHospede;
import com.divan.repository.HistoricoClienteRepository;

import java.math.BigDecimal;

import java.math.RoundingMode;

import java.time.LocalDate;

import java.time.LocalDateTime;

import java.time.temporal.ChronoUnit;

import java.util.Comparator;

import java.util.List;

import java.util.Map;

import java.util.Objects;

import java.util.stream.Collectors;


@Service

public class RankingHospedeService {


    private final HistoricoClienteRepository repository;


    public RankingHospedeService(HistoricoClienteRepository repository) {

        this.repository = repository;

    }


    @Transactional(readOnly = true)

    public List<RankingHospedeDTO> ranking(LocalDate dataInicio, LocalDate dataFim, String ordenarPor) {


        LocalDateTime dtInicio = dataInicio != null ? dataInicio.atStartOfDay() : null;

        LocalDateTime dtFim    = dataFim    != null ? dataFim.atTime(23, 59, 59) : null;


        List<HospedagemHospede> hospedagens =

                repository.findTodosParaRanking(dtInicio, dtFim);


        // Agrupa por clienteId

        Map<Long, List<HospedagemHospede>> porCliente = hospedagens.stream()

                .collect(Collectors.groupingBy(h -> h.getCliente().getId()));


        List<RankingHospedeDTO> ranking = porCliente.entrySet().stream()

                .map(entry -> montarDTO(entry.getKey(), entry.getValue()))

                .sorted(criarComparator(ordenarPor)) // ✅ agora respeita o ordenarPor

                .toList();


        return ranking;

    }


    /**

     * Define o critério de ordenação do ranking.

     * Valores aceitos: "hospedagens" (padrão), "diarias", "gasto", "diasSemHospedar".

     */

    private Comparator<RankingHospedeDTO> criarComparator(String ordenarPor) {

        String criterio = (ordenarPor == null || ordenarPor.isBlank()) ? "hospedagens" : ordenarPor;

        return switch (criterio) {

            case "diarias"         -> Comparator.comparingInt(RankingHospedeDTO::getTotalDiasHospedado).reversed();

            case "gasto"           -> Comparator.comparing(RankingHospedeDTO::getTotalGasto).reversed();

            case "diasSemHospedar" -> Comparator.comparingLong(RankingHospedeDTO::getDiasDesdeUltimaHospedagem).reversed();

            default                -> Comparator.comparingInt(RankingHospedeDTO::getTotalHospedagens).reversed();

        };

    }


    private RankingHospedeDTO montarDTO(Long clienteId, List<HospedagemHospede> hospedagens) {

        RankingHospedeDTO dto = new RankingHospedeDTO();

        var cliente = hospedagens.get(0).getCliente();


        dto.setClienteId(clienteId);

        dto.setNomeCliente(cliente.getNome());

        dto.setCpf(cliente.getCpf());

        dto.setCelular(cliente.getCelularCompleto() != null

                ? cliente.getCelularCompleto() : cliente.getCelular());


        // ✅✅✅ NOVO — preenche o nome da empresa (vem do ManyToOne da entity Cliente)

        dto.setEmpresa(

                cliente.getEmpresa() != null && cliente.getEmpresa().getNomeEmpresa() != null

                        ? cliente.getEmpresa().getNomeEmpresa()

                        : null

        );


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
