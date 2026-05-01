package com.divan.service;

import com.divan.entity.Apartamento;
import com.divan.entity.Diaria;
import com.divan.entity.TipoApartamento;
import com.divan.repository.DiariaRepository;
import com.divan.repository.TipoApartamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DiariaService {

    @Autowired
    private DiariaRepository diariaRepository;

    @Autowired
    private TipoApartamentoRepository tipoApartamentoRepository;

    public Diaria salvar(Diaria diaria, Long tipoApartamentoId) {
        // Buscar tipo de apartamento
        Optional<TipoApartamento> tipoOpt = tipoApartamentoRepository.findById(tipoApartamentoId);
        if (tipoOpt.isEmpty()) {
            throw new RuntimeException("Tipo de apartamento não encontrado");
        }

        diaria.setTipoApartamento(tipoOpt.get());

        // ✅ Para 1 hóspede, modalidade é obrigatória
        if (diaria.getQuantidade() != null && diaria.getQuantidade() == 1 && diaria.getModalidade() == null) {
            throw new RuntimeException("Para 1 hóspede, é obrigatório informar a modalidade (SOLTEIRO ou CASAL)");
        }

        // ✅ Para 2+ hóspedes, modalidade não se aplica (zera)
        if (diaria.getQuantidade() != null && diaria.getQuantidade() > 1) {
            diaria.setModalidade(null);
        }

        // Verificar se já existe (considerando modalidade para 1 hóspede)
        if (diaria.getId() == null) {
            boolean jaExiste;
            if (diaria.getQuantidade() == 1 && diaria.getModalidade() != null) {
                jaExiste = diariaRepository.existsByTipoApartamentoAndQuantidadeAndModalidade(
                    diaria.getTipoApartamento(), diaria.getQuantidade(), diaria.getModalidade());
            } else {
                jaExiste = diariaRepository.existsByTipoApartamentoAndQuantidade(
                    diaria.getTipoApartamento(), diaria.getQuantidade());
            }
            if (jaExiste) {
                throw new RuntimeException("Já existe diária para este tipo, quantidade e modalidade");
            }
        }

        return diariaRepository.save(diaria);
    }

    public Diaria atualizar(Long id, Diaria diaria, Long tipoApartamentoId) {
        Optional<Diaria> diariaExistente = diariaRepository.findById(id);
        if (diariaExistente.isEmpty()) {
            throw new RuntimeException("Diária não encontrada");
        }

        Optional<TipoApartamento> tipoOpt = tipoApartamentoRepository.findById(tipoApartamentoId);
        if (tipoOpt.isEmpty()) {
            throw new RuntimeException("Tipo de apartamento não encontrado");
        }

        diaria.setId(id);
        diaria.setTipoApartamento(tipoOpt.get());

        // ✅ Mesmas regras de modalidade no update
        if (diaria.getQuantidade() != null && diaria.getQuantidade() == 1 && diaria.getModalidade() == null) {
            throw new RuntimeException("Para 1 hóspede, é obrigatório informar a modalidade (SOLTEIRO ou CASAL)");
        }
        if (diaria.getQuantidade() != null && diaria.getQuantidade() > 1) {
            diaria.setModalidade(null);
        }

        return diariaRepository.save(diaria);
    }

    public void deletar(Long id) {
        if (!diariaRepository.existsById(id)) {
            throw new RuntimeException("Diária não encontrada");
        }
        diariaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Diaria> buscarPorId(Long id) {
        return diariaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Diaria> buscarPorTipoEQuantidade(Long tipoApartamentoId, Integer quantidade) {
        Optional<TipoApartamento> tipoOpt = tipoApartamentoRepository.findById(tipoApartamentoId);
        if (tipoOpt.isEmpty()) {
            throw new RuntimeException("Tipo de apartamento não encontrado");
        }
        return diariaRepository.findByTipoApartamentoAndQuantidade(tipoOpt.get(), quantidade);
    }

    @Transactional(readOnly = true)
    public List<Diaria> listarTodas() {
        return diariaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Diaria> buscarPorTipo(Long tipoApartamentoId) {
        Optional<TipoApartamento> tipoOpt = tipoApartamentoRepository.findById(tipoApartamentoId);
        if (tipoOpt.isEmpty()) {
            throw new RuntimeException("Tipo de apartamento não encontrado");
        }
        return diariaRepository.findByTipoApartamentoOrderByQuantidade(tipoOpt.get());
    }

    /**
     * ✅ NOVO — Busca a diária correta considerando o apartamento (cama de casal) e a quantidade de hóspedes.
     * Para 1 hóspede, considera se o apartamento tem cama de casal.
     * Para 2+ hóspedes, busca pela quantidade ignorando modalidade.
     * Se não encontrar a diária com modalidade específica, faz fallback para a sem modalidade.
     */
    @Transactional(readOnly = true)
    public Optional<Diaria> buscarDiariaPara(Apartamento apartamento, Integer quantidadeHospedes) {
        if (apartamento == null || quantidadeHospedes == null || quantidadeHospedes < 1) {
            return Optional.empty();
        }
        TipoApartamento tipo = apartamento.getTipoApartamento();
        if (tipo == null) {
            return Optional.empty();
        }

        // Para 1 hóspede, considera modalidade (casal/solteiro)
        if (quantidadeHospedes == 1) {
            Diaria.ModalidadeEnum modalidade = (apartamento.getTemCamaDeCasal() != null && apartamento.getTemCamaDeCasal())
                ? Diaria.ModalidadeEnum.CASAL
                : Diaria.ModalidadeEnum.SOLTEIRO;

            // Busca diária específica pela modalidade — sem fallback para 1 pessoa
            return diariaRepository.findByTipoApartamentoAndQuantidadeAndModalidade(tipo, 1, modalidade);
        }

        // Para 2+ hóspedes — busca por tipo e quantidade (modalidade NULL)
        return diariaRepository.findByTipoApartamentoAndQuantidade(tipo, quantidadeHospedes);
    }
}
