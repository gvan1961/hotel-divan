package com.divan.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.divan.entity.ItemReserva;

import java.util.List;

@Repository
public interface ItemReservaRepository extends JpaRepository<ItemReserva, Long> {
    
    // Buscar itens por reserva
    List<ItemReserva> findByReservaId(Long reservaId);
    
    // Buscar itens por caixa
    List<ItemReserva> findByCaixaId(Long caixaId);
    
    // Buscar itens por reserva ordenados por data
    List<ItemReserva> findByReservaIdOrderByDataHoraDesc(Long reservaId);
    
    // Buscar itens de uma reserva por tipo
    List<ItemReserva> findByReservaIdAndTipo(Long reservaId, String tipo);
}
