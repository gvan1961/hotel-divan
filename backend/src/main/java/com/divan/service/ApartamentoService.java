package com.divan.service;

import com.divan.dto.ApartamentoRequestDTO;
import com.divan.dto.ApartamentoResponseDTO;
import com.divan.entity.Apartamento;
import com.divan.entity.TipoApartamento;
import com.divan.entity.Reserva;
import com.divan.repository.ApartamentoRepository;
import com.divan.repository.ReservaRepository;
import com.divan.repository.TipoApartamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
        return apartamentoRepository.findAllByOrderByNumeroApartamentoAsc().stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    // =============== MÉTODO PRINCIPAL - BUSCAR DISPONÍVEIS ===============
    
    public List<Apartamento> buscarApartamentosDisponiveisParaReserva(
            LocalDateTime dataCheckin, LocalDateTime dataCheckout) {
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔍 BUSCANDO APARTAMENTOS DISPONÍVEIS");
        System.out.println("═══════════════════════════════════════════");
        System.out.println("📅 Check-in: " + dataCheckin);
        System.out.println("📅 Check-out: " + dataCheckout);
        
        // Validar datas
        if (dataCheckout.isBefore(dataCheckin) || dataCheckout.isEqual(dataCheckin)) {
            throw new RuntimeException("Data de check-out deve ser posterior ao check-in");
        }
        
        // ✅ BUSCAR TODOS OS APARTAMENTOS (menos INDISPONÍVEL e MANUTENCAO)
        List<Apartamento> todosApartamentos = apartamentoRepository.findAll().stream()
            .filter(apt -> apt.getStatus() != Apartamento.StatusEnum.INDISPONIVEL && 
                           apt.getStatus() != Apartamento.StatusEnum.MANUTENCAO)
            .collect(Collectors.toList());
        
        System.out.println("📊 Total de apartamentos ativos: " + todosApartamentos.size());
        
        // ✅ VALIDAÇÃO - Verificar conflitos de RESERVA (não de status!)
        List<Apartamento> disponiveis = new ArrayList<>();
        
        for (Apartamento apt : todosApartamentos) {
            // ✅ VERIFICAR APENAS CONFLITO DE DATAS - NÃO O STATUS DO APARTAMENTO!
            boolean temConflito = reservaRepository.existeConflito(
                apt.getId(), dataCheckin, dataCheckout
            );
            
            if (!temConflito) {
                disponiveis.add(apt);
                System.out.println("✅ Apto " + apt.getNumeroApartamento() + 
                                 " - " + apt.getTipoApartamento().getTipo() + 
                                 " (Capacidade: " + apt.getCapacidade() + 
                                 ") - Status atual: " + apt.getStatus() + 
                                 " - DISPONÍVEL para o período");
            } else {
                System.out.println("❌ Apto " + apt.getNumeroApartamento() + 
                                 " - TEM CONFLITO DE RESERVA no período");
            }
        }
        
        System.out.println("📊 Total de apartamentos disponíveis: " + disponiveis.size());
        System.out.println("═══════════════════════════════════════════");
        
        return disponiveis;
    }
    
    public List<ApartamentoResponseDTO> buscarApartamentosDisponiveisParaReservaDTO(
            LocalDateTime dataCheckin, LocalDateTime dataCheckout) {
        
        List<Apartamento> disponiveis = buscarApartamentosDisponiveisParaReserva(dataCheckin, dataCheckout);
        
        return disponiveis.stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    // =============== VERIFICAÇÃO DE DISPONIBILIDADE ===============
    
    public boolean verificarDisponibilidade(Long apartamentoId, 
                                           LocalDateTime dataCheckin, 
                                           LocalDateTime dataCheckout) {
        
        Apartamento apartamento = apartamentoRepository.findById(apartamentoId)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        System.out.println("🔍 Verificando disponibilidade do Apto " + 
                         apartamento.getNumeroApartamento());
        
        // 1. Verificar status
        if (apartamento.getStatus() != Apartamento.StatusEnum.DISPONIVEL) {
            System.out.println("❌ Status não disponível: " + apartamento.getStatus());
            return false;
        }
        
        // 2. Verificar conflitos usando o método do repository
        boolean temConflito = reservaRepository.existeConflito(
            apartamentoId, dataCheckin, dataCheckout
        );
        
        if (temConflito) {
            // Buscar detalhes dos conflitos para log
            List<Reserva> conflitos = reservaRepository.findConflitosReserva(
                apartamentoId, dataCheckin, dataCheckout
            );
            
            System.out.println("❌ Encontrados " + conflitos.size() + " conflitos:");
            for (Reserva r : conflitos) {
                System.out.println("   - Reserva #" + r.getId() + 
                                 " (" + r.getDataCheckin().toLocalDate() + 
                                 " a " + r.getDataCheckout().toLocalDate() + 
                                 ") - Status: " + r.getStatus());
            }
            return false;
        }
        
        System.out.println("✅ Apartamento disponível!");
        return true;
    }
    
    // =============== GERENCIAMENTO DE STATUS ===============
    
    public List<Apartamento> buscarApartamentosBloqueados() {
        List<Apartamento.StatusEnum> statusBloqueados = Arrays.asList(
            Apartamento.StatusEnum.MANUTENCAO,
            Apartamento.StatusEnum.LIMPEZA,
            Apartamento.StatusEnum.INDISPONIVEL,
            Apartamento.StatusEnum.OCUPADO
        );
        
        return apartamentoRepository.findByStatusIn(statusBloqueados);
    }
    
    @Transactional
    public Apartamento liberarApartamento(Long apartamentoId) {
        Apartamento apartamento = apartamentoRepository.findById(apartamentoId)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        // Verificar se está ocupado com reserva ativa
        if (apartamento.getStatus() == Apartamento.StatusEnum.OCUPADO) {
            List<Reserva> reservasAtivas = reservaRepository.findByApartamentoAndStatus(
                apartamento, Reserva.StatusReservaEnum.ATIVA
            );
            
            if (!reservasAtivas.isEmpty()) {
                throw new RuntimeException("Apartamento está ocupado com reserva ativa #" + 
                                         reservasAtivas.get(0).getId() + 
                                         ". Finalize a reserva primeiro.");
            }
        }
        
        Apartamento.StatusEnum statusAnterior = apartamento.getStatus();
        apartamento.setStatus(Apartamento.StatusEnum.DISPONIVEL);
        
        Apartamento salvo = apartamentoRepository.save(apartamento);
        
        System.out.println("✅ Apartamento " + apartamento.getNumeroApartamento() + 
                         " liberado. Status anterior: " + statusAnterior);
        
        return salvo;
    }
    
    public Apartamento liberarLimpeza(Long id) {
        Apartamento apartamento = apartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        if (!apartamento.getStatus().equals(Apartamento.StatusEnum.LIMPEZA)) {
            throw new RuntimeException("Apartamento não está em limpeza. Status atual: " + 
                                     apartamento.getStatus());
        }
        
        apartamento.setStatus(Apartamento.StatusEnum.DISPONIVEL);
        System.out.println("✅ Apartamento " + apartamento.getNumeroApartamento() + 
                         " liberado da limpeza");
        
        return apartamentoRepository.save(apartamento);
    }
    
    public Apartamento colocarEmManutencao(Long id, String motivo) {
        Apartamento apartamento = apartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        if (apartamento.getStatus().equals(Apartamento.StatusEnum.OCUPADO)) {
            throw new RuntimeException("Não é possível colocar apartamento ocupado em manutenção");
        }
        
        apartamento.setStatus(Apartamento.StatusEnum.MANUTENCAO);
        System.out.println("🔧 Apartamento " + apartamento.getNumeroApartamento() + 
                         " em manutenção. Motivo: " + motivo);
        
        return apartamentoRepository.save(apartamento);
    }
    
    public Apartamento liberarManutencao(Long id) {
        Apartamento apartamento = apartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        if (!apartamento.getStatus().equals(Apartamento.StatusEnum.MANUTENCAO)) {
            throw new RuntimeException("Apartamento não está em manutenção");
        }
        
        apartamento.setStatus(Apartamento.StatusEnum.DISPONIVEL);
        System.out.println("✅ Apartamento " + apartamento.getNumeroApartamento() + 
                         " liberado da manutenção");
        
        return apartamentoRepository.save(apartamento);
    }
    
    public Apartamento bloquear(Long id, String motivo) {
        Apartamento apartamento = apartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        if (apartamento.getStatus().equals(Apartamento.StatusEnum.OCUPADO)) {
            throw new RuntimeException("Não é possível bloquear apartamento ocupado");
        }
        
        apartamento.setStatus(Apartamento.StatusEnum.INDISPONIVEL);
        System.out.println("🚫 Apartamento " + apartamento.getNumeroApartamento() + 
                         " bloqueado. Motivo: " + motivo);
        
        return apartamentoRepository.save(apartamento);
    }
    
    public Apartamento desbloquear(Long id) {
        Apartamento apartamento = apartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        if (!apartamento.getStatus().equals(Apartamento.StatusEnum.INDISPONIVEL)) {
            throw new RuntimeException("Apartamento não está bloqueado");
        }
        
        apartamento.setStatus(Apartamento.StatusEnum.DISPONIVEL);
        System.out.println("✅ Apartamento " + apartamento.getNumeroApartamento() + 
                         " desbloqueado");
        
        return apartamentoRepository.save(apartamento);
    }
    
    public Apartamento atualizarStatus(Long id, Apartamento.StatusEnum status) {
        Apartamento apartamento = apartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        apartamento.setStatus(status);
        return apartamentoRepository.save(apartamento);
    }
    
    // =============== MÉTODO AUXILIAR - CONVERTER PARA DTO ===============
    
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

        // ✅ 1️⃣ BUSCAR RESERVA ATIVA (somente se OCUPADO)
        if (apartamento.getStatus() == Apartamento.StatusEnum.OCUPADO) {
            List<Reserva> reservasAtivas = reservaRepository
                .findByApartamentoAndStatusOrderByDataCheckinDesc(
                    apartamento, 
                    Reserva.StatusReservaEnum.ATIVA
                );
            
            if (!reservasAtivas.isEmpty()) {
                Reserva reserva = reservasAtivas.get(0);
                
                ApartamentoResponseDTO.ReservaAtiva dadosReserva = 
                    new ApartamentoResponseDTO.ReservaAtiva();
                dadosReserva.setReservaId(reserva.getId());
                dadosReserva.setNomeHospede(reserva.getCliente().getNome());
                dadosReserva.setQuantidadeHospede(reserva.getQuantidadeHospede());
                dadosReserva.setDataCheckin(reserva.getDataCheckin());
                dadosReserva.setDataCheckout(reserva.getDataCheckout());
                dadosReserva.setStatus(reserva.getStatus().name());
                
                dto.setReservaAtiva(dadosReserva);
                
                System.out.println("📋 Reserva ATIVA encontrada - Apt " + 
                                 apartamento.getNumeroApartamento() + 
                                 " - Hóspede: " + reserva.getCliente().getNome());
            }
        }
        
        // ✅ 2️⃣ BUSCAR PRÉ-RESERVAS (independente do status do apartamento)
        List<Reserva> preReservas = reservaRepository
            .findByApartamentoAndStatusOrderByDataCheckinDesc(
                apartamento, 
                Reserva.StatusReservaEnum.PRE_RESERVA
            );
        
        if (!preReservas.isEmpty()) {
            Reserva preReserva = preReservas.get(0);
            
            ApartamentoResponseDTO.ReservaAtiva dadosPreReserva = 
                new ApartamentoResponseDTO.ReservaAtiva();
            dadosPreReserva.setReservaId(preReserva.getId());
            dadosPreReserva.setNomeHospede(preReserva.getCliente().getNome());
            dadosPreReserva.setQuantidadeHospede(preReserva.getQuantidadeHospede());
            dadosPreReserva.setDataCheckin(preReserva.getDataCheckin());
            dadosPreReserva.setDataCheckout(preReserva.getDataCheckout());
            dadosPreReserva.setStatus(preReserva.getStatus().name());
            
            // ✅ DECISÃO: Onde colocar a pré-reserva?
            if (apartamento.getStatus() == Apartamento.StatusEnum.PRE_RESERVA) {
                // Se apartamento está em PRE_RESERVA, colocar em reservaAtiva
                dto.setReservaAtiva(dadosPreReserva);
                
                System.out.println("🔶 PRÉ-RESERVA (como ativa) - Apt " + 
                                 apartamento.getNumeroApartamento() + 
                                 " - Hóspede: " + preReserva.getCliente().getNome());
            } else {
                // Se apartamento está DISPONIVEL ou OCUPADO, colocar em preReservaFutura
                dto.setPreReservaFutura(dadosPreReserva);
                
                System.out.println("🔶 PRÉ-RESERVA FUTURA - Apt " + 
                                 apartamento.getNumeroApartamento() + 
                                 " (Status: " + apartamento.getStatus() + ")" +
                                 " - Hóspede: " + preReserva.getCliente().getNome() +
                                 " - Check-in: " + preReserva.getDataCheckin());
            }
        }

        return dto;
    }
    
    // =============== MÉTODOS BÁSICOS (SEM ALTERAÇÃO) ===============
    
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
    
    public List<Apartamento> buscarPorStatus(Apartamento.StatusEnum status) {
        return apartamentoRepository.findByStatus(status);
    }
    
    public List<Apartamento> buscarDisponiveisParaPeriodo(LocalDateTime checkin, LocalDateTime checkout) {
        // Redirecionar para o método correto
        return buscarApartamentosDisponiveisParaReserva(checkin, checkout);
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
}