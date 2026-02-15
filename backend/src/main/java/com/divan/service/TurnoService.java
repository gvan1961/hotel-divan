package com.divan.service;

import com.divan.dto.TurnoDTO;
import com.divan.entity.Turno;
import com.divan.repository.TurnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TurnoService {
    
    @Autowired
    private TurnoRepository turnoRepository;
    
    /**
     * ✅ LISTAR TODOS OS TURNOS ATIVOS
     */
    @Transactional(readOnly = true)
    public List<TurnoDTO> listarAtivos() {
        return turnoRepository.findByAtivoTrueOrderByOrdem()
            .stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * 📋 LISTAR TODOS OS TURNOS
     */
    @Transactional(readOnly = true)
    public List<TurnoDTO> listarTodos() {
        return turnoRepository.findAllByOrderByOrdem()
            .stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * 🔍 BUSCAR POR ID
     */
    @Transactional(readOnly = true)
    public TurnoDTO buscarPorId(Long id) {
        Turno turno = turnoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Turno não encontrado"));
        return converterParaDTO(turno);
    }
    
    /**
     * ➕ CRIAR TURNO
     */
    public TurnoDTO criar(TurnoDTO dto) {
        // Verificar se já existe turno com o mesmo nome
        if (turnoRepository.existsByNome(dto.getNome())) {
            throw new RuntimeException("Já existe um turno com este nome");
        }
        
        Turno turno = new Turno();
        turno.setNome(dto.getNome());
        turno.setHoraInicio(dto.getHoraInicio());
        turno.setHoraFim(dto.getHoraFim());
        turno.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
        turno.setOrdem(dto.getOrdem() != null ? dto.getOrdem() : 0);
        
        Turno salvo = turnoRepository.save(turno);
        
        System.out.println("✅ Turno criado: " + salvo.getNome());
        
        return converterParaDTO(salvo);
    }
    
    /**
     * ✏️ ATUALIZAR TURNO
     */
    public TurnoDTO atualizar(Long id, TurnoDTO dto) {
        Turno turno = turnoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Turno não encontrado"));
        
        // Verificar se o novo nome já existe em outro turno
        if (!turno.getNome().equals(dto.getNome()) && turnoRepository.existsByNome(dto.getNome())) {
            throw new RuntimeException("Já existe um turno com este nome");
        }
        
        turno.setNome(dto.getNome());
        turno.setHoraInicio(dto.getHoraInicio());
        turno.setHoraFim(dto.getHoraFim());
        turno.setAtivo(dto.getAtivo());
        turno.setOrdem(dto.getOrdem());
        
        Turno atualizado = turnoRepository.save(turno);
        
        System.out.println("✅ Turno atualizado: " + atualizado.getNome());
        
        return converterParaDTO(atualizado);
    }
    
    /**
     * 🗑️ DELETAR TURNO
     */
    public void deletar(Long id) {
        Turno turno = turnoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Turno não encontrado"));
        
        turnoRepository.delete(turno);
        
        System.out.println("✅ Turno deletado: " + turno.getNome());
    }
    
    /**
     * 🕐 SUGERIR TURNO BASEADO NA HORA ATUAL
     */
    @Transactional(readOnly = true)
    public TurnoDTO sugerirTurnoPorHorario(LocalTime horaAtual) {
        List<Turno> turnos = turnoRepository.findByAtivoTrueOrderByOrdem();
        
        for (Turno turno : turnos) {
            if (estaNoTurno(horaAtual, turno.getHoraInicio(), turno.getHoraFim())) {
                System.out.println("✅ Turno sugerido: " + turno.getNome() + " para hora " + horaAtual);
                return converterParaDTO(turno);
            }
        }
        
        // Se não encontrou, retorna o primeiro turno
        if (!turnos.isEmpty()) {
            return converterParaDTO(turnos.get(0));
        }
        
        throw new RuntimeException("Nenhum turno configurado no sistema");
    }
    
    /**
     * ⏰ VERIFICAR SE HORA ESTÁ DENTRO DO TURNO
     */
    private boolean estaNoTurno(LocalTime hora, LocalTime inicio, LocalTime fim) {
        // Turno normal (não passa da meia-noite)
        if (inicio.isBefore(fim)) {
            return !hora.isBefore(inicio) && hora.isBefore(fim);
        }
        // Turno que passa da meia-noite (ex: 23h às 8h)
        else {
            return !hora.isBefore(inicio) || hora.isBefore(fim);
        }
    }
    
    /**
     * 🔄 CONVERTER PARA DTO
     */
    private TurnoDTO converterParaDTO(Turno turno) {
        TurnoDTO dto = new TurnoDTO();
        dto.setId(turno.getId());
        dto.setNome(turno.getNome());
        dto.setHoraInicio(turno.getHoraInicio());
        dto.setHoraFim(turno.getHoraFim());
        dto.setAtivo(turno.getAtivo());
        dto.setOrdem(turno.getOrdem());
        return dto;
    }
}
