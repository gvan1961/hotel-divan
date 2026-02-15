package com.divan.repository;

import com.divan.entity.Vale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ValeRepository extends JpaRepository<Vale, Long> {

    List<Vale> findByClienteIdOrderByDataConcessaoDesc(Long clienteId);

    List<Vale> findByStatusOrderByDataVencimentoAsc(Vale.StatusVale status);

    List<Vale> findByClienteIdAndStatus(Long clienteId, Vale.StatusVale status);

    @Query("SELECT v FROM Vale v WHERE v.dataConcessao BETWEEN :inicio AND :fim ORDER BY v.dataConcessao DESC")
    List<Vale> findByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT v FROM Vale v WHERE v.status = 'PENDENTE' AND v.dataVencimento < :hoje")
    List<Vale> findValesVencidos(@Param("hoje") LocalDate hoje);

    @Query("SELECT COALESCE(SUM(v.valor), 0) FROM Vale v WHERE v.cliente.id = :clienteId AND (v.status = 'PENDENTE' OR v.status = 'VENCIDO')")
    BigDecimal calcularTotalPendentePorCliente(@Param("clienteId") Long clienteId);
}
