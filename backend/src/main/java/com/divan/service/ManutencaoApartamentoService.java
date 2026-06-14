package com.divan.service;

import com.divan.dto.ManutencaoRequestDTO;
import com.divan.dto.ManutencaoResponseDTO;
import com.divan.entity.Apartamento;
import com.divan.entity.ManutencaoApartamento;
import com.divan.entity.TipoServico;
import com.divan.entity.StatusManutencao;
import com.divan.repository.ApartamentoRepository;
import com.divan.repository.ManutencaoApartamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ManutencaoApartamentoService {

    private static final int INTERVALO_LIMPEZA_FILTRO_DIAS = 90;

    private final ManutencaoApartamentoRepository repository;
    private final ApartamentoRepository apartamentoRepository;

    public ManutencaoApartamentoService(ManutencaoApartamentoRepository repository,
                                        ApartamentoRepository apartamentoRepository) {
        this.repository = repository;
        this.apartamentoRepository = apartamentoRepository;
    }

    @Transactional
    public ManutencaoResponseDTO criar(ManutencaoRequestDTO dto) {
        Apartamento apartamento = apartamentoRepository.findById(dto.getApartamentoId())
                .orElseThrow(() -> new IllegalArgumentException("Apartamento não encontrado: " + dto.getApartamentoId()));

        ManutencaoApartamento m = new ManutencaoApartamento();
        m.setApartamento(apartamento);
        aplicarDados(m, dto);
        return ManutencaoResponseDTO.fromEntity(repository.save(m));
    }

    @Transactional
    public ManutencaoResponseDTO atualizar(Long id, ManutencaoRequestDTO dto) {
        ManutencaoApartamento m = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Manutenção não encontrada: " + id));

        if (dto.getApartamentoId() != null && !dto.getApartamentoId().equals(m.getApartamento().getId())) {
            Apartamento apartamento = apartamentoRepository.findById(dto.getApartamentoId())
                    .orElseThrow(() -> new IllegalArgumentException("Apartamento não encontrado: " + dto.getApartamentoId()));
            m.setApartamento(apartamento);
        }
        aplicarDados(m, dto);
        return ManutencaoResponseDTO.fromEntity(repository.save(m));
    }

    private void aplicarDados(ManutencaoApartamento m, ManutencaoRequestDTO dto) {
        m.setDataServico(dto.getDataServico() != null ? dto.getDataServico() : LocalDate.now());
        m.setTipoServico(dto.getTipoServico());
        m.setDescricao(dto.getDescricao());
        m.setResponsavel(dto.getResponsavel());
        m.setCusto(dto.getCusto());
        m.setObservacoes(dto.getObservacoes());

        StatusManutencao status = dto.getStatus() != null ? dto.getStatus() : StatusManutencao.PENDENTE;
        m.setStatus(status);

        if (status == StatusManutencao.CONCLUIDO) {
            m.setDataConclusao(dto.getDataConclusao() != null ? dto.getDataConclusao() : LocalDate.now());
        } else {
            m.setDataConclusao(dto.getDataConclusao());
        }
    }

    @Transactional
    public ManutencaoResponseDTO concluir(Long id) {
        ManutencaoApartamento m = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Manutenção não encontrada: " + id));
        m.setStatus(StatusManutencao.CONCLUIDO);
        if (m.getDataConclusao() == null) m.setDataConclusao(LocalDate.now());
        return ManutencaoResponseDTO.fromEntity(repository.save(m));
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) throw new IllegalArgumentException("Manutenção não encontrada: " + id);
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ManutencaoResponseDTO buscarPorId(Long id) {
        return repository.findById(id)
                .map(ManutencaoResponseDTO::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Manutenção não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<ManutencaoResponseDTO> historicoDoApartamento(Long apartamentoId) {
        return repository.findByApartamentoIdOrderByDataServicoDescIdDesc(apartamentoId)
                .stream().map(ManutencaoResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<ManutencaoResponseDTO> buscarComFiltros(Long apartamentoId, TipoServico tipoServico,
                                                        StatusManutencao status,
                                                        LocalDate inicio, LocalDate fim) {
        return repository.buscarComFiltros(apartamentoId, tipoServico, status, inicio, fim)
                .stream().map(ManutencaoResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public boolean filtroArVencido(Long apartamentoId) {
        Optional<ManutencaoApartamento> ultima =
                repository.findFirstByApartamentoIdAndTipoServicoOrderByDataServicoDesc(
                        apartamentoId, TipoServico.LIMPEZA_FILTRO_AR);
        if (ultima.isEmpty()) return true;
        return ChronoUnit.DAYS.between(ultima.get().getDataServico(), LocalDate.now()) >= INTERVALO_LIMPEZA_FILTRO_DIAS;
    }
}