package com.divan.repository;

import com.divan.entity.CobrancaCartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CobrancaCartaoRepository extends JpaRepository<CobrancaCartao, Long> {
    Optional<CobrancaCartao> findByCorrelationId(String correlationId);

    Optional<CobrancaCartao> findByOrdemMercadoPago(String ordemMercadoPago);

    List<CobrancaCartao> findByStatusInOrderByDataCriacaoDesc(List<String> status);

    Optional<CobrancaCartao> findFirstByReservaIdAndStatusInOrderByDataCriacaoDesc(
        Long reservaId, List<String> status);
    
    @Query("SELECT c FROM CobrancaCartao c WHERE " +
    	       "(:dataInicio IS NULL OR c.dataCriacao >= :dataInicio) AND " +
    	       "(:dataFim IS NULL OR c.dataCriacao <= :dataFim) AND " +
    	       "(:reservaId IS NULL OR c.reservaId = :reservaId) AND " +
    	       "(:status IS NULL OR c.status = :status) " +
    	       "ORDER BY c.dataCriacao DESC")
    	List<CobrancaCartao> buscarHistorico(
    	    @Param("dataInicio") java.time.LocalDateTime dataInicio,
    	    @Param("dataFim") java.time.LocalDateTime dataFim,
    	    @Param("reservaId") Long reservaId,
    	    @Param("status") String status);
}
