package com.divan.repository;

import com.divan.entity.CobrancaCartao;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
