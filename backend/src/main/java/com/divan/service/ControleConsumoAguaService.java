package com.divan.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.divan.dto.FaixaConsumoAguaResponseDTO;
import com.divan.entity.ControleConsumoAguaReserva;
import com.divan.entity.Empresa;
import com.divan.entity.ExtratoReserva;
import com.divan.entity.FaixaConsumoAguaEmpresa;
import com.divan.entity.Reserva;
import com.divan.repository.ControleConsumoAguaReservaRepository;
import com.divan.repository.EmpresaRepository;
import com.divan.repository.ExtratoReservaRepository;
import com.divan.repository.FaixaConsumoAguaEmpresaRepository;

import jakarta.transaction.Transactional;

import com.divan.dto.FaixaConsumoAguaRequestDTO;

@Service
public class ControleConsumoAguaService {

    @Autowired private FaixaConsumoAguaEmpresaRepository faixaRepository;
    @Autowired private ControleConsumoAguaReservaRepository controleRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    
    @Autowired
    private ExtratoReservaRepository extratoReservaRepository;
       

    public Optional<BigDecimal> getLimiteDiario(Reserva reserva) {
        if (reserva.getCliente() == null || reserva.getCliente().getEmpresa() == null) {
            return Optional.empty();
        }
        return faixaRepository.findByEmpresaIdAndQtdHospedes(
            reserva.getCliente().getEmpresa().getId(),
            reserva.getQuantidadeHospede()
        ).map(FaixaConsumoAguaEmpresa::getValorLimiteDiario);
    }

    public BigDecimal getConsumidoHoje(Long reservaId) {
        return controleRepository.findByReservaIdAndData(reservaId, LocalDate.now())
            .map(ControleConsumoAguaReserva::getValorConsumido)
            .orElse(BigDecimal.ZERO);
    }

    @Transactional
    public void registrarConsumo(Reserva reserva, BigDecimal valorVenda) {
        LocalDate hoje = LocalDate.now();
        ControleConsumoAguaReserva controle = controleRepository
            .findByReservaIdAndData(reserva.getId(), hoje)
            .orElseGet(() -> {
                ControleConsumoAguaReserva novo = new ControleConsumoAguaReserva();
                novo.setReserva(reserva);
                novo.setData(hoje);
                novo.setValorConsumido(BigDecimal.ZERO);
                return novo;
            });
        controle.setValorConsumido(controle.getValorConsumido().add(valorVenda));
        controleRepository.save(controle);
    }

    /** Retorna true se a venda pode prosseguir dentro do limite (ou se não há convênio). */
    public boolean validarLimite(Reserva reserva, BigDecimal valorVenda) {
        Optional<BigDecimal> limite = getLimiteDiario(reserva);
        if (limite.isEmpty()) return true; // sem convênio, sem restrição
        BigDecimal consumido = getConsumidoHoje(reserva.getId());
        return consumido.add(valorVenda).compareTo(limite.get()) <= 0;
    }
    
    public List<FaixaConsumoAguaResponseDTO> listarTodas() {
        return faixaRepository.findAll().stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }
    
    public List<FaixaConsumoAguaResponseDTO> listarPorEmpresa(Long empresaId) {
        return faixaRepository.findByEmpresaId(empresaId).stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public FaixaConsumoAguaResponseDTO criar(FaixaConsumoAguaRequestDTO dto) {
        if (faixaRepository.findByEmpresaIdAndQtdHospedes(dto.getEmpresaId(), dto.getQtdHospedes()).isPresent()) {
            throw new RuntimeException("Já existe uma faixa cadastrada para essa empresa com essa quantidade de hóspedes.");
        }
        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
            .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        FaixaConsumoAguaEmpresa faixa = new FaixaConsumoAguaEmpresa();
        faixa.setEmpresa(empresa);
        faixa.setQtdHospedes(dto.getQtdHospedes());
        faixa.setValorLimiteDiario(dto.getValorLimiteDiario());
        return toResponseDTO(faixaRepository.save(faixa));
    }
    
    @Transactional
    public FaixaConsumoAguaResponseDTO atualizar(Long id, FaixaConsumoAguaRequestDTO dto) {
        FaixaConsumoAguaEmpresa faixa = faixaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Faixa não encontrada"));
        faixa.setQtdHospedes(dto.getQtdHospedes());
        faixa.setValorLimiteDiario(dto.getValorLimiteDiario());
        return toResponseDTO(faixaRepository.save(faixa));
    }

    @Transactional
    public void excluir(Long id) {
        if (!faixaRepository.existsById(id)) {
            throw new RuntimeException("Faixa não encontrada");
        }
        faixaRepository.deleteById(id);
    }

    
    private FaixaConsumoAguaResponseDTO toResponseDTO(FaixaConsumoAguaEmpresa faixa) {
        FaixaConsumoAguaResponseDTO dto = new FaixaConsumoAguaResponseDTO();
        dto.setId(faixa.getId());
        dto.setEmpresaId(faixa.getEmpresa().getId());
        dto.setEmpresaNome(faixa.getEmpresa().getNomeEmpresa());
        dto.setQtdHospedes(faixa.getQtdHospedes());
        dto.setValorLimiteDiario(faixa.getValorLimiteDiario());
        return dto;
    }
    
    public List<Map<String, Object>> getItensAguaHoje(Reserva reserva) {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);

        return extratoReservaRepository.findByReservaIdAndStatusLancamentoAndDataHoraLancamentoBetween(
                reserva.getId(),
                ExtratoReserva.StatusLancamentoEnum.PRODUTO,
                inicioDia,
                fimDia
            ).stream()
            .filter(e -> e.getDescricao() != null && e.getDescricao().toUpperCase().contains("AGUA"))
            .map(e -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("descricao", e.getDescricao());
                item.put("quantidade", e.getQuantidade());
                item.put("horario", e.getDataHoraLancamento().toLocalTime().toString().substring(0, 5));
                return item;
            })
            .collect(Collectors.toList());
    }
    
    
}
