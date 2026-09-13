package com.divan.repository;

import com.divan.entity.CobrancaPix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CobrancaPixRepository extends JpaRepository<CobrancaPix, Long> {
    Optional<CobrancaPix> findByCorrelationId(String correlationId);

    List<CobrancaPix> findByStatusAndReservaIdIsNullOrderByDataCriacaoDesc(CobrancaPix.StatusPixEnum status);

    List<CobrancaPix> findByStatusInOrderByDataCriacaoDesc(List<CobrancaPix.StatusPixEnum> status);

    Optional<CobrancaPix> findFirstByReservaIdAndStatusInOrderByDataCriacaoDesc(
        Long reservaId, List<CobrancaPix.StatusPixEnum> status);
    
    @Query("SELECT c FROM CobrancaPix c WHERE " +
    	       "(:dataInicio IS NULL OR c.dataCriacao >= :dataInicio) AND " +
    	       "(:dataFim IS NULL OR c.dataCriacao <= :dataFim) AND " +
    	       "(:reservaId IS NULL OR c.reservaId = :reservaId) AND " +
    	       "(:status IS NULL OR c.status = :status) " +
    	       "ORDER BY c.dataCriacao DESC")
    	List<CobrancaPix> buscarHistorico(
    	    @Param("dataInicio") java.time.LocalDateTime dataInicio,
    	    @Param("dataFim") java.time.LocalDateTime dataFim,
    	    @Param("reservaId") Long reservaId,
    	    @Param("status") CobrancaPix.StatusPixEnum status);
}
