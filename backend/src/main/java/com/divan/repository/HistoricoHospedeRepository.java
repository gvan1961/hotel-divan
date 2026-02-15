package com.divan.repository;

import com.divan.entity.HistoricoHospede;
import com.divan.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoHospedeRepository extends JpaRepository<HistoricoHospede, Long> {
    
    List<HistoricoHospede> findByReserva(Reserva reserva);
    
    List<HistoricoHospede> findByReservaOrderByDataHora(Reserva reserva);
    
    List<HistoricoHospede> findByReservaIdOrderByDataHoraDesc(Long reservaId);
    
    List<HistoricoHospede> findByReservaId(Long reservaId);         
  
    List<HistoricoHospede> findByReservaOrderByDataHoraDesc(Reserva reserva);
}
