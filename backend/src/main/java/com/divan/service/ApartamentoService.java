package com.divan.service;   

import com.divan.dto.ApartamentoRequestDTO;
import com.divan.dto.ApartamentoResponseDTO;
import com.divan.entity.Apartamento;
import com.divan.entity.TipoApartamento;
import com.divan.repository.ApartamentoRepository;
import com.divan.repository.ReservaRepository;
import com.divan.repository.TipoApartamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.divan.entity.Reserva;
import com.divan.repository.ReservaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
public class ApartamentoService {
    
    @Autowired
    private ApartamentoRepository apartamentoRepository;
    
    @Autowired
    private TipoApartamentoRepository tipoApartamentoRepository;
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    // =============== MÉTODOS COM DTO ===============
    
    public ApartamentoResponseDTO criarComDTO(ApartamentoRequestDTO dto) {
        System.out.println("📥 Criando apartamento com DTO: " + dto);
        
        TipoApartamento tipoApartamento = tipoApartamentoRepository.findById(dto.getTipoApartamentoId())
            .orElseThrow(() -> new RuntimeException("Tipo de apartamento não encontrado"));
        
        if (apartamentoRepository.existsByNumeroApartamento(dto.getNumeroApartamento())) {
            throw new RuntimeException("Já existe um apartamento com este número");
        }
        
        Apartamento apartamento = new Apartamento();
        apartamento.setNumeroApartamento(dto.getNumeroApartamento());
        apartamento.setTipoApartamento(tipoApartamento);
        apartamento.setCapacidade(dto.getCapacidade());
        apartamento.setCamasDoApartamento(dto.getCamasDoApartamento());
        apartamento.setTv(dto.getTv());
        apartamento.setTemCamaDeCasal(dto.getTemCamaDeCasal() != null ? dto.getTemCamaDeCasal() : false);  // ✅ NOVO
        apartamento.setStatus(Apartamento.StatusEnum.DISPONIVEL);
        
        Apartamento salvo = apartamentoRepository.save(apartamento);
        System.out.println("✅ Apartamento criado: " + salvo.getId());
        
        return converterParaDTO(salvo);
    }
    
    public ApartamentoResponseDTO atualizarComDTO(Long id, ApartamentoRequestDTO dto) {
        System.out.println("📥 Atualizando apartamento " + id + " com DTO: " + dto);
        
        Apartamento apartamento = apartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        if (apartamento.getStatus() == Apartamento.StatusEnum.OCUPADO) {
            throw new RuntimeException("Não é possível editar apartamento OCUPADO. Finalize a reserva primeiro.");
        }
        
        TipoApartamento tipoApartamento = tipoApartamentoRepository.findById(dto.getTipoApartamentoId())
            .orElseThrow(() -> new RuntimeException("Tipo de apartamento não encontrado"));
        
        if (!apartamento.getNumeroApartamento().equals(dto.getNumeroApartamento()) &&
            apartamentoRepository.existsByNumeroApartamento(dto.getNumeroApartamento())) {
            throw new RuntimeException("Já existe outro apartamento com este número");
        }
        
        apartamento.setNumeroApartamento(dto.getNumeroApartamento());
        apartamento.setTipoApartamento(tipoApartamento);
        apartamento.setCapacidade(dto.getCapacidade());
        apartamento.setCamasDoApartamento(dto.getCamasDoApartamento());
        apartamento.setTv(dto.getTv());
        apartamento.setTemCamaDeCasal(dto.getTemCamaDeCasal() != null ? dto.getTemCamaDeCasal() : false);
        
        Apartamento atualizado = apartamentoRepository.save(apartamento);
        System.out.println("✅ Apartamento atualizado: " + atualizado.getId());
        
        return converterParaDTO(atualizado);
    }
    
    public ApartamentoResponseDTO buscarPorIdDTO(Long id) {
        Apartamento apartamento = apartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        return converterParaDTO(apartamento);
    }
    
    public List<ApartamentoResponseDTO> listarTodosDTO() {
        return apartamentoRepository.findAll().stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    private ApartamentoResponseDTO converterParaDTO(Apartamento apartamento) {
        ApartamentoResponseDTO dto = new ApartamentoResponseDTO();
        dto.setId(apartamento.getId());
        dto.setNumeroApartamento(apartamento.getNumeroApartamento());
        dto.setTipoApartamentoId(apartamento.getTipoApartamento().getId());
        dto.setTipoApartamentoNome(apartamento.getTipoApartamento().getTipo().toString());
        dto.setTipoApartamentoDescricao(apartamento.getTipoApartamento().getDescricao());
        dto.setCapacidade(apartamento.getCapacidade());
        dto.setCamasDoApartamento(apartamento.getCamasDoApartamento());
        dto.setTv(apartamento.getTv());
        dto.setStatus(apartamento.getStatus());
        dto.setTemCamaDeCasal(apartamento.getTemCamaDeCasal());
        
        // ✅ SE OCUPADO, BUSCAR RESERVA ATIVA
        if (apartamento.getStatus() == Apartamento.StatusEnum.OCUPADO) {
        	Optional<Reserva> reservaAtiva = reservaRepository
        		    .findFirstByApartamentoAndStatusOrderByDataCheckinDesc(
        		        apartamento, Reserva.StatusReservaEnum.ATIVA);
            
            if (reservaAtiva.isPresent()) {
                Reserva reserva = reservaAtiva.get();
                
                ApartamentoResponseDTO.ReservaAtiva dadosReserva = new ApartamentoResponseDTO.ReservaAtiva();
                dadosReserva.setReservaId(reserva.getId());
                dadosReserva.setNomeHospede(reserva.getCliente().getNome());
                dadosReserva.setQuantidadeHospede(reserva.getQuantidadeHospede());
                dadosReserva.setDataCheckin(reserva.getDataCheckin());
                dadosReserva.setDataCheckout(reserva.getDataCheckout());
                
                dto.setReservaAtiva(dadosReserva);
                
                System.out.println("📋 Reserva ativa encontrada no apartamento " + apartamento.getNumeroApartamento() + 
                                 ": " + reserva.getCliente().getNome());
            }
        }
        
        return dto;
    }
    
    // =============== MÉTODOS ADICIONAIS (RETORNAM ENTIDADE) ===============
    
    public Optional<Apartamento> buscarPorId(Long id) {
        return apartamentoRepository.findById(id);
    }
    
    public List<Apartamento> listarTodos() {
        return apartamentoRepository.findAll();
    }
    
    public Optional<Apartamento> buscarPorNumero(String numero) {
        return apartamentoRepository.findByNumeroApartamento(numero);
    }
    
    public List<Apartamento> buscarDisponiveis() {
        return apartamentoRepository.findByStatus(Apartamento.StatusEnum.DISPONIVEL);
    }
    
    public List<Apartamento> buscarOcupados() {
        return apartamentoRepository.findByStatus(Apartamento.StatusEnum.OCUPADO);
    }
    
    public List<Apartamento> buscarDisponiveisParaPeriodo(LocalDateTime checkin, LocalDateTime checkout) {
        List<Apartamento> todos = apartamentoRepository.findAll();

        return todos.stream().filter(apt -> {
            // Bloqueia apenas INDISPONIVEL
            if (apt.getStatus() == Apartamento.StatusEnum.INDISPONIVEL) {
                return false;
            }

            List<Reserva> reservas = reservaRepository.findByApartamentoId(apt.getId());

            for (Reserva r : reservas) {
                if (r.getStatus() == Reserva.StatusReservaEnum.CANCELADA ||
                    r.getStatus() == Reserva.StatusReservaEnum.FINALIZADA) {
                    continue;
                }

                // Conflito de período
                boolean semConflito =
                    !checkin.toLocalDate().isBefore(r.getDataCheckout().toLocalDate()) ||
                    !checkout.toLocalDate().isAfter(r.getDataCheckin().toLocalDate());

                if (!semConflito) return false;
            }

            return true;
        }).collect(Collectors.toList());
    }
    
    public List<Apartamento> buscarPorStatus(Apartamento.StatusEnum status) {
        return apartamentoRepository.findByStatus(status);
    }
    
    public Apartamento atualizarStatus(Long id, Apartamento.StatusEnum status) {
        Apartamento apartamento = apartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        apartamento.setStatus(status);
        return apartamentoRepository.save(apartamento);
    }
    
    public Apartamento salvar(Apartamento apartamento) {
        return apartamentoRepository.save(apartamento);
    }
    
    public Apartamento atualizar(Long id, Apartamento apartamentoAtualizado) {
        Apartamento apartamento = apartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        apartamento.setNumeroApartamento(apartamentoAtualizado.getNumeroApartamento());
        apartamento.setTipoApartamento(apartamentoAtualizado.getTipoApartamento());
        apartamento.setCapacidade(apartamentoAtualizado.getCapacidade());
        apartamento.setCamasDoApartamento(apartamentoAtualizado.getCamasDoApartamento());
        apartamento.setTv(apartamentoAtualizado.getTv());
        apartamento.setStatus(apartamentoAtualizado.getStatus());
        
        return apartamentoRepository.save(apartamento);
    }
    
    public Apartamento liberarLimpeza(Long id) {
        Apartamento apartamento = apartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        if (!apartamento.getStatus().equals(Apartamento.StatusEnum.LIMPEZA)) {
            throw new RuntimeException("Apartamento não está em limpeza. Status atual: " + apartamento.getStatus());
        }
        
        apartamento.setStatus(Apartamento.StatusEnum.DISPONIVEL);
        System.out.println("✅ Apartamento " + apartamento.getNumeroApartamento() + " liberado da limpeza");
        
        return apartamentoRepository.save(apartamento);
    }

    public Apartamento colocarEmManutencao(Long id, String motivo) {
        Apartamento apartamento = apartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        if (apartamento.getStatus().equals(Apartamento.StatusEnum.OCUPADO)) {
            throw new RuntimeException("Não é possível colocar apartamento ocupado em manutenção");
        }
        
        apartamento.setStatus(Apartamento.StatusEnum.MANUTENCAO);
        System.out.println("🔧 Apartamento " + apartamento.getNumeroApartamento() + " em manutenção. Motivo: " + motivo);
        
        return apartamentoRepository.save(apartamento);
    }

    public Apartamento liberarManutencao(Long id) {
        Apartamento apartamento = apartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        if (!apartamento.getStatus().equals(Apartamento.StatusEnum.MANUTENCAO)) {
            throw new RuntimeException("Apartamento não está em manutenção. Status atual: " + apartamento.getStatus());
        }
        
        apartamento.setStatus(Apartamento.StatusEnum.DISPONIVEL);
        System.out.println("✅ Apartamento " + apartamento.getNumeroApartamento() + " liberado da manutenção");
        
        return apartamentoRepository.save(apartamento);
    }

    public Apartamento bloquear(Long id, String motivo) {
        Apartamento apartamento = apartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        if (apartamento.getStatus().equals(Apartamento.StatusEnum.OCUPADO)) {
            throw new RuntimeException("Não é possível bloquear apartamento ocupado");
        }
        
        apartamento.setStatus(Apartamento.StatusEnum.INDISPONIVEL);
        System.out.println("🚫 Apartamento " + apartamento.getNumeroApartamento() + " bloqueado. Motivo: " + motivo);
        
        return apartamentoRepository.save(apartamento);
    }

    public Apartamento desbloquear(Long id) {
        Apartamento apartamento = apartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        if (!apartamento.getStatus().equals(Apartamento.StatusEnum.INDISPONIVEL)) {
            throw new RuntimeException("Apartamento não está bloqueado. Status atual: " + apartamento.getStatus());
        }
        
        apartamento.setStatus(Apartamento.StatusEnum.DISPONIVEL);
        System.out.println("✅ Apartamento " + apartamento.getNumeroApartamento() + " desbloqueado");
        
        return apartamentoRepository.save(apartamento);
    }
}