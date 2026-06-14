package com.divan.repository;

import com.divan.entity.TipoServico;
import com.divan.entity.StatusManutencao;
import com.divan.entity.ManutencaoApartamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ManutencaoApartamentoRepository extends JpaRepository<ManutencaoApartamento, Long> {

    List<ManutencaoApartamento> findByApartamentoIdOrderByDataServicoDescIdDesc(Long apartamentoId);

    @Query("""
            SELECT m FROM ManutencaoApartamento m
            WHERE (:apartamentoId IS NULL OR m.apartamento.id = :apartamentoId)
              AND (:tipoServico   IS NULL OR m.tipoServico = :tipoServico)
              AND (:status        IS NULL OR m.status = :status)
              AND (:inicio        IS NULL OR m.dataServico >= :inicio)
              AND (:fim           IS NULL OR m.dataServico <= :fim)
            ORDER BY m.dataServico DESC, m.id DESC
            """)
    List<ManutencaoApartamento> buscarComFiltros(
            @Param("apartamentoId") Long apartamentoId,
            @Param("tipoServico")   TipoServico tipoServico,
            @Param("status")        StatusManutencao status,
            @Param("inicio")        LocalDate inicio,
            @Param("fim")           LocalDate fim
    );

    Optional<ManutencaoApartamento> findFirstByApartamentoIdAndTipoServicoOrderByDataServicoDesc(
            Long apartamentoId, TipoServico tipoServico);

    @Query("SELECT COALESCE(SUM(m.custo), 0) FROM ManutencaoApartamento m WHERE m.apartamento.id = :apartamentoId")
    java.math.BigDecimal totalGastoPorApartamento(@Param("apartamentoId") Long apartamentoId);
}
