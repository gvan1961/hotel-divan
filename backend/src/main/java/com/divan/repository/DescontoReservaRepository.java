package com.divan.repository;

import com.divan.entity.DescontoReserva;
import com.divan.entity.Reserva;
import com.divan.entity.FechamentoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DescontoReservaRepository extends JpaRepository<DescontoReserva, Long> {
    
    List<DescontoReserva> findByReserva(Reserva reserva);
    
    List<DescontoReserva> findByCaixa(FechamentoCaixa caixa);
    
    @Query("SELECT COALESCE(SUM(d.valor), 0) FROM DescontoReserva d WHERE d.reserva = :reserva")
    BigDecimal somarDescontosPorReserva(@Param("reserva") Reserva reserva);
    
    @Query("SELECT COALESCE(SUM(d.valor), 0) FROM DescontoReserva d WHERE d.caixa = :caixa")
    BigDecimal somarDescontosPorCaixa(@Param("caixa") FechamentoCaixa caixa);
}
