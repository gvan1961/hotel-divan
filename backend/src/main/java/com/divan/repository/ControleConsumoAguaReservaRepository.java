package com.divan.repository;

import org.springframework.stereotype.Repository;

import com.divan.entity.ControleConsumoAguaReserva;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


@Repository
public interface ControleConsumoAguaReservaRepository extends JpaRepository<ControleConsumoAguaReserva, Long> {
    Optional<ControleConsumoAguaReserva> findByReservaIdAndData(Long reservaId, LocalDate data);
}
