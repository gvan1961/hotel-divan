package com.divan.repository;

import com.divan.entity.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
    List<LogAuditoria> findByReservaIdOrderByDataHoraDesc(Long reservaId);
    List<LogAuditoria> findByUsuarioIdOrderByDataHoraDesc(Long usuarioId);
    List<LogAuditoria> findAllByOrderByDataHoraDesc();
}
