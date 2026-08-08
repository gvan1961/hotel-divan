package com.divan.repository;

import com.divan.entity.Vale;
import com.divan.entity.Vale.StatusVale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ValeRepository extends JpaRepository<Vale, Long> {

    // ===== Consultas de LISTAGEM (com JOIN FETCH para evitar N+1 no cliente) =====

    @Query("SELECT v FROM Vale v JOIN FETCH v.cliente ORDER BY v.cliente.nome")
    List<Vale> findAllComCliente();

    @Query("SELECT v FROM Vale v JOIN FETCH v.cliente WHERE v.cliente.id = :clienteId ORDER BY v.dataEmissao DESC")
    List<Vale> findByClienteIdComCliente(@Param("clienteId") Long clienteId);

    @Query("SELECT v FROM Vale v JOIN FETCH v.cliente WHERE v.status = :status ORDER BY v.dataVencimento")
    List<Vale> findByStatusComCliente(@Param("status") StatusVale status);

    @Query("SELECT v FROM Vale v JOIN FETCH v.cliente WHERE v.status = 'PENDENTE' " +
           "AND v.dataVencimento < CURRENT_DATE ORDER BY v.dataVencimento")
    List<Vale> findVencidos();

    // ===== Relatório: filtro feito no banco em vez de findAll() + stream em memória =====

    @Query("SELECT v FROM Vale v JOIN FETCH v.cliente WHERE " +
           "(:status IS NULL OR v.status = :status) AND " +
           "(:clienteId IS NULL OR v.cliente.id = :clienteId) AND " +
           "(:dataInicio IS NULL OR v.dataEmissao >= :dataInicio) AND " +
           "(:dataFim IS NULL OR v.dataEmissao <= :dataFim) " +
           "ORDER BY v.cliente.nome")
    List<Vale> buscarRelatorio(@Param("status") StatusVale status,
                                @Param("clienteId") Long clienteId,
                                @Param("dataInicio") LocalDateTime dataInicio,
                                @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT COALESCE(SUM(v.valor), 0) FROM Vale v " +
           "WHERE v.cliente.id = :clienteId AND v.status = 'PENDENTE'")
    BigDecimal calcularTotalPendentePorCliente(Long clienteId);
}