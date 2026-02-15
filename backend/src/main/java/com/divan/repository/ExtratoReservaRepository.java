package com.divan.repository;

import com.divan.entity.ExtratoReserva;
import com.divan.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExtratoReservaRepository extends JpaRepository<ExtratoReserva, Long> {
    
    List<ExtratoReserva> findByReserva(Reserva reserva);
    
    List<ExtratoReserva> findByReservaOrderByDataHoraLancamento(Reserva reserva);    
    
       
    List<ExtratoReserva> findByStatusLancamento(ExtratoReserva.StatusLancamentoEnum status);
    
    
    @Query("SELECT e FROM ExtratoReserva e WHERE e.dataHoraLancamento BETWEEN :inicio AND :fim")
    List<ExtratoReserva> findByPeriodo(LocalDateTime inicio, LocalDateTime fim);
    
    List<ExtratoReserva> findByReservaIdOrderByDataHoraLancamento(Reserva reserva);
          
 //   List<HistoricoHospede> findByReservaId(Long reservaId);  
    
    
 // ✅ MÉTODO CORRETO - Retorna List<ExtratoReserva>
    @Query("SELECT e FROM ExtratoReserva e WHERE e.reserva.id = :reservaId")
    List<ExtratoReserva> findByReservaId(@Param("reservaId") Long reservaId);   
   
    List<ExtratoReserva> findByDataHoraLancamentoBetween(LocalDateTime inicio, LocalDateTime fim);
    
    
}
