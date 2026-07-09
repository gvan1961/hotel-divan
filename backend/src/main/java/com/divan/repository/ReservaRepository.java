
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
    List<Reserva> findByStatusAndDataCheckinBefore(Reserva.StatusReservaEnum status, LocalDateTime dataHora);
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
    
    List<Reserva> findByStatusAndDataCheckinBetween(
    	    Reserva.StatusReservaEnum status,
    	    LocalDateTime inicio,
    	    LocalDateTime fim
    	);
    
    List<Reserva> findByStatusAndDataCheckinBefore(String status, LocalDateTime dataHora);
    
    @Query("""
    	    SELECT DISTINCT r.apartamento.id FROM Reserva r
    	    WHERE r.status NOT IN ('CANCELADA', 'FINALIZADA')
    	    AND r.dataCheckin < :checkout
    	    AND r.dataCheckout > :checkin
    	    """)
    	List<Long> findApartamentosComConflito(
    	    @Param("checkin") LocalDateTime checkin,
    	    @Param("checkout") LocalDateTime checkout
    	);
    
    @Query("""
    	    SELECT r FROM Reserva r
    	    WHERE r.apartamento.id = :apartamentoId
    	      AND r.status IN :status
    	      AND (:ignorarReservaId IS NULL OR r.id <> :ignorarReservaId)
    	      AND r.dataCheckin < :dataCheckout
    	      AND r.dataCheckout > :dataCheckin
    	    ORDER BY r.dataCheckin ASC
    	""")
    	List<Reserva> buscarConflitosApartamento(
    	    @Param("apartamentoId") Long apartamentoId,
    	    @Param("dataCheckin") LocalDateTime dataCheckin,
    	    @Param("dataCheckout") LocalDateTime dataCheckout,
    	    @Param("status") List<Reserva.StatusReservaEnum> status,
    	    @Param("ignorarReservaId") Long ignorarReservaId
    	);

    	@Query("""
    	    SELECT r FROM Reserva r
    	    WHERE r.apartamento.id = :apartamentoId
    	      AND r.status = :status
    	      AND (:ignorarReservaId IS NULL OR r.id <> :ignorarReservaId)
    	    ORDER BY r.dataCheckin DESC
    	""")
    	List<Reserva> buscarReservasAtivasDoApartamento(
    	    @Param("apartamentoId") Long apartamentoId,
    	    @Param("status") Reserva.StatusReservaEnum status,
    	    @Param("ignorarReservaId") Long ignorarReservaId
    	);
          
    	@Query("""
    		    SELECT r FROM Reserva r
    		    WHERE r.cliente.id = :clienteId
    		      AND r.status IN :statusReservas
    		      AND (:ignorarReservaId IS NULL OR r.id <> :ignorarReservaId)
    		      AND r.dataCheckin < :dataCheckout
    		      AND r.dataCheckout > :dataCheckin
    		    ORDER BY r.dataCheckin ASC
    		""")
    		List<Reserva> buscarConflitosClienteTitular(
    		    @Param("clienteId") Long clienteId,
    		    @Param("dataCheckin") LocalDateTime dataCheckin,
    		    @Param("dataCheckout") LocalDateTime dataCheckout,
    		    @Param("statusReservas") List<Reserva.StatusReservaEnum> statusReservas,
    		    @Param("ignorarReservaId") Long ignorarReservaId
    		);

    		@Query("""
    		    SELECT r FROM Reserva r
    		    WHERE r.cliente.id = :clienteId
    		      AND r.status = :statusReserva
    		      AND (:ignorarReservaId IS NULL OR r.id <> :ignorarReservaId)
    		    ORDER BY r.dataCheckin DESC
    		""")
    		List<Reserva> buscarReservasAtivasClienteTitular(
    		    @Param("clienteId") Long clienteId,
    		    @Param("statusReserva") Reserva.StatusReservaEnum statusReserva,
    		    @Param("ignorarReservaId") Long ignorarReservaId
    		);

        
}
