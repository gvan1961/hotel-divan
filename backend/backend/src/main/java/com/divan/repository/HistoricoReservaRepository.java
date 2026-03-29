package com.divan.repository;


import com.divan.entity.HistoricoReserva;
import com.divan.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoReservaRepository extends JpaRepository<HistoricoReserva, Long> {
    
    // Buscar históricos de uma reserva ordenados por data (mais recente primeiro)
    List<HistoricoReserva> findByReservaOrderByDataHoraDesc(Reserva reserva);
    
    // Buscar históricos por ID da reserva
    List<HistoricoReserva> findByReservaIdOrderByDataHoraDesc(Long reservaId);
}