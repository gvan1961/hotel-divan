package com.divan.repository;

import com.divan.entity.Vale;
import com.divan.entity.Vale.StatusVale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;

public interface ValeRepository extends JpaRepository<Vale, Long> {
    List<Vale> findByClienteId(Long clienteId);
    List<Vale> findByStatus(StatusVale status);
    
    @Query("SELECT v FROM Vale v WHERE v.status = 'PENDENTE' " +
           "AND v.dataVencimento < CURRENT_DATE")
    List<Vale> findVencidos();
    
    @Query("SELECT COALESCE(SUM(v.valor), 0) FROM Vale v " +
           "WHERE v.cliente.id = :clienteId AND v.status = 'PENDENTE'")
    BigDecimal calcularTotalPendentePorCliente(Long clienteId);
}
