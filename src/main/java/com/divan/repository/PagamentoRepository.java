package com.divan.repository;

import com.divan.entity.Pagamento;
import com.divan.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    
    List<Pagamento> findByReserva(Reserva reserva);
    
    List<Pagamento> findByFormaPagamento(Pagamento.FormaPagamentoEnum formaPagamento);
    
    @Query("SELECT p FROM Pagamento p WHERE DATE(p.dataHoraPagamento) = DATE(:data)")
    List<Pagamento> findPagamentosDoDia(LocalDateTime data);
    
    @Query("SELECT p FROM Pagamento p WHERE p.dataHoraPagamento BETWEEN :inicio AND :fim")
    List<Pagamento> findPagamentosPorPeriodo(LocalDateTime inicio, LocalDateTime fim);
    
    @Query("SELECT p.formaPagamento, SUM(p.valor) FROM Pagamento p WHERE DATE(p.dataHoraPagamento) = DATE(:data) GROUP BY p.formaPagamento")
    List<Object[]> findTotalPorFormaPagamentoNoDia(LocalDateTime data);
    
    List<Pagamento> findByDataHoraPagamentoBetween(LocalDateTime inicio, LocalDateTime fim);
}
