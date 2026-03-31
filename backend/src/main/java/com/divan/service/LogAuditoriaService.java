package com.divan.service;

import com.divan.entity.*;
import com.divan.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogAuditoriaService {

    private final LogAuditoriaRepository logRepository;
    private final UsuarioRepository usuarioRepository;

    public LogAuditoriaService(LogAuditoriaRepository logRepository,
                               UsuarioRepository usuarioRepository) {
        this.logRepository = logRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public void registrar(String acao, String descricao, Reserva reserva) {
        try {
            String username = SecurityContextHolder.getContext()
                    .getAuthentication().getName();
            Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

            LogAuditoria log = new LogAuditoria();
            log.setAcao(acao);
            log.setDescricao(descricao);
            log.setReserva(reserva);
            log.setUsuario(usuario);
            log.setDataHora(LocalDateTime.now());
            logRepository.save(log);
        } catch (Exception e) {
            System.err.println("Erro ao registrar auditoria: " + e.getMessage());
        }
    }

    public List<LogAuditoria> buscarPorReserva(Long reservaId) {
        return logRepository.findByReservaIdOrderByDataHoraDesc(reservaId);
    }

    public List<LogAuditoria> buscarPorUsuario(Long usuarioId) {
        return logRepository.findByUsuarioIdOrderByDataHoraDesc(usuarioId);
    }

    public List<LogAuditoria> buscarTodos() {
        return logRepository.findAllByOrderByDataHoraDesc();
    }
}
