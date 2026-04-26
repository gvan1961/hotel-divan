package com.divan.repository;

import com.divan.entity.RegistroPonto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, Long> {

    List<RegistroPonto> findByClienteIdOrderByDataHoraDesc(Long clienteId);

    @Query("SELECT r FROM RegistroPonto r WHERE r.dataHora BETWEEN :inicio AND :fim ORDER BY r.dataHora DESC")
    List<RegistroPonto> findByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT r FROM RegistroPonto r WHERE r.cliente.id = :clienteId AND r.dataHora BETWEEN :inicio AND :fim ORDER BY r.dataHora ASC")
    List<RegistroPonto> findByClienteIdAndPeriodo(@Param("clienteId") Long clienteId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT r FROM RegistroPonto r WHERE r.cliente.id = :clienteId ORDER BY r.dataHora DESC")
    Optional<RegistroPonto> findUltimoRegistro(@Param("clienteId") Long clienteId);

    @Query("SELECT r FROM RegistroPonto r WHERE DATE(r.dataHora) = DATE(:data) ORDER BY r.dataHora DESC")
    List<RegistroPonto> findByData(@Param("data") LocalDateTime data);
}
