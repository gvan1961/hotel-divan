
package com.divan.repository;

import com.divan.entity.Apartamento;
import com.divan.entity.Cliente;
import com.divan.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
	
	long countByStatus(Reserva.StatusReservaEnum status);
	
	List<Reserva> findByApartamentoId(Long apartamentoId);
    
    List<Reserva> findByCliente(Cliente cliente);
    
    List<Reserva> findByApartamento(Apartamento apartamento);
    
    boolean existsByClienteId(Long clienteId);      
        
    List<Reserva> findByStatus(Reserva.StatusReservaEnum status);
    
    @Query("SELECT r FROM Reserva r WHERE r.status = 'ATIVA'")
    List<Reserva> findReservasAtivas();
    
    @Query("SELECT r FROM Reserva r WHERE DATE(r.dataCheckin) = DATE(:data)")
    List<Reserva> findReservasParaCheckinNaData(@Param("data") LocalDateTime data);
    
    @Query("SELECT r FROM Reserva r WHERE DATE(r.dataCheckout) = DATE(:data)")
    List<Reserva> findReservasParaCheckoutNaData(@Param("data") LocalDateTime data);
    
    @Query("SELECT r FROM Reserva r WHERE r.dataCheckin BETWEEN :inicio AND :fim")
    List<Reserva> findReservasPorPeriodo(@Param("inicio") LocalDateTime inicio, 
                                        @Param("fim") LocalDateTime fim);
    
    @Query("SELECT r FROM Reserva r WHERE r.apartamento.id = :apartamentoId AND " +
           "r.status = 'ATIVA' AND " +
           "((r.dataCheckin <= :checkout AND r.dataCheckout >= :checkin))")
    List<Reserva> findReservasConflitantes(@Param("apartamentoId") Long apartamentoId,
                                          @Param("checkin") LocalDateTime checkin,
                                          @Param("checkout") LocalDateTime checkout);
    
    @Query("SELECT r FROM Reserva r WHERE r.apartamento = :apartamento AND r.status = 'ATIVA'")
    Optional<Reserva> findReservaAtivaDoApartamento(@Param("apartamento") Apartamento apartamento);
    
    @Query("SELECT r FROM Reserva r WHERE r.totalApagar > 0")
    List<Reserva> findReservasComSaldoDevedor();
      
    List<Reserva> findByDataCheckinBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Reserva> findByDataCheckoutBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Reserva> findByDataCheckinBetweenOrDataCheckoutBetween(
        LocalDateTime inicio1, LocalDateTime fim1, 
        LocalDateTime inicio2, LocalDateTime fim2
    );
    
    Optional<Reserva> findByApartamentoAndStatus(Apartamento apartamento, Reserva.StatusReservaEnum status);
    
    List<Reserva> findByStatusAndDataCheckoutBefore(Reserva.StatusReservaEnum status, LocalDateTime dataCheckout);
    List<Reserva> findByStatusAndDataCheckinBefore(Reserva.StatusReservaEnum status, LocalDateTime dataCheckin);
    @Query("SELECT r.apartamento.id FROM Reserva r WHERE r.status = :status " +
           "GROUP BY r.apartamento.id HAVING COUNT(r) > 1")
    List<Long> findApartamentosComConflito(@Param("status") Reserva.StatusReservaEnum status);
    
    List<Reserva> findByClienteIdAndStatusIn(Long clienteId, List<Reserva.StatusReservaEnum> status);
    
    List<Reserva> findByApartamentoIdAndStatus(Long apartamentoId, Reserva.StatusReservaEnum status);
    
    List<Reserva> findByApartamentoIdAndStatusIn(
    	    Long apartamentoId,
    	    List<Reserva.StatusReservaEnum> statuses
    	);  
    
    Optional<Reserva> findFirstByApartamentoAndStatusOrderByDataCheckinDesc(
    	    Apartamento apartamento, Reserva.StatusReservaEnum status);
    
    List<Reserva> findByStatusAndDataCheckoutBetween(
            Reserva.StatusReservaEnum status, 
            LocalDateTime inicio, 
            LocalDateTime fim
        );
    
    List<Reserva> findAllByRenovacaoAutomaticaTrueAndStatus(Reserva.StatusReservaEnum status);
        
}
