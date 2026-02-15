package com.divan.repository;

import com.divan.entity.ControleDiaria;
import com.divan.entity.ControleDiaria.StatusDiariaEnum;
import com.divan.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ControleDiariaRepository extends JpaRepository<ControleDiaria, Long> {
    
    List<ControleDiaria> findByReserva(Reserva reserva);
    
    List<ControleDiaria> findByReservaId(Long reservaId);
    
    List<ControleDiaria> findByStatus(StatusDiariaEnum status);
    
    @Query("SELECT cd FROM ControleDiaria cd WHERE cd.status = :status AND cd.reserva.status = 'ATIVA'")
    List<ControleDiaria> findByStatusAndReservaAtiva(@Param("status") StatusDiariaEnum status);
    
    @Query("SELECT cd FROM ControleDiaria cd WHERE cd.reserva.id = :reservaId AND cd.status = :status")
    List<ControleDiaria> findByReservaIdAndStatus(@Param("reservaId") Long reservaId, @Param("status") StatusDiariaEnum status);
    
    @Query("SELECT cd FROM ControleDiaria cd WHERE cd.dataLancamento < :dataLimite AND cd.status = 'LANCADA' AND cd.reserva.status = 'ATIVA'")
    List<ControleDiaria> findDiariasParaFechar(@Param("dataLimite") LocalDateTime dataLimite);
}
