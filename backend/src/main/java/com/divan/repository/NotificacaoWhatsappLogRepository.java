package com.divan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.divan.entity.NotificacaoWhatsappLog;

import java.util.List;

@Repository
public interface NotificacaoWhatsappLogRepository extends JpaRepository<NotificacaoWhatsappLog, Long> {

    List<NotificacaoWhatsappLog> findByReservaIdOrderByDataEnvioDesc(Long reservaId);

    List<NotificacaoWhatsappLog> findByEmpresaIdOrderByDataEnvioDesc(Long empresaId);

    List<NotificacaoWhatsappLog> findByTipoAndReservaIdAndSucessoTrue(String tipo, Long reservaId);
}
