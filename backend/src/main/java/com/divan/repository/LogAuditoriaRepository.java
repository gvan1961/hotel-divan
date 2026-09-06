package com.divan.repository;

import com.divan.entity.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
    List<LogAuditoria> findByReservaIdOrderByDataHoraDesc(Long reservaId);
    List<LogAuditoria> findByUsuarioIdOrderByDataHoraDesc(Long usuarioId);
    List<LogAuditoria> findAllByOrderByDataHoraDesc();
    boolean existsByReservaIdAndAcaoAndDataHoraAfter(Long reservaId, String acao, LocalDateTime dataHora);
    
    @Query("SELECT l FROM LogAuditoria l " +
    	       "LEFT JOIN l.usuario u " +
    	       "LEFT JOIN l.reserva r " +
    	       "LEFT JOIN r.apartamento a " +
    	       "LEFT JOIN r.cliente c " +
    	       "LEFT JOIN c.empresa e " +
    	       "WHERE (:funcionario IS NULL OR :funcionario = '' OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :funcionario, '%'))) " +
    	       "AND (:acao IS NULL OR :acao = '' OR l.acao = :acao) " +
    	       "AND (:dataInicio IS NULL OR l.dataHora >= :dataInicio) " +
    	       "AND (:dataFim IS NULL OR l.dataHora <= :dataFim) " +
    	       "AND (:apartamento IS NULL OR :apartamento = '' OR a.numeroApartamento LIKE CONCAT('%', :apartamento, '%')) " +
    	       "AND (:reservaId IS NULL OR r.id = :reservaId) " +
    	       "AND (:hospede IS NULL OR :hospede = '' OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :hospede, '%'))) " +
    	       "AND (:empresa IS NULL OR :empresa = '' OR LOWER(e.nomeEmpresa) LIKE LOWER(CONCAT('%', :empresa, '%'))) " +
    	       "ORDER BY l.dataHora DESC")
    	List<LogAuditoria> buscarComFiltros(
    	        @Param("funcionario") String funcionario,
    	        @Param("acao") String acao,
    	        @Param("dataInicio") LocalDateTime dataInicio,
    	        @Param("dataFim") LocalDateTime dataFim,
    	        @Param("apartamento") String apartamento,
    	        @Param("reservaId") Long reservaId,
    	        @Param("hospede") String hospede,
    	        @Param("empresa") String empresa);
}