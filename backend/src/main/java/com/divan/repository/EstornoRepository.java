package com.divan.repository;

import com.divan.entity.Estorno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EstornoRepository extends JpaRepository<Estorno, Long> {
    List<Estorno> findByReservaId(Long reservaId);
    List<Estorno> findByNotaVendaId(Long notaVendaId);
    List<Estorno> findByDataHoraEstornoBetween(LocalDateTime inicio, LocalDateTime fim);
}
