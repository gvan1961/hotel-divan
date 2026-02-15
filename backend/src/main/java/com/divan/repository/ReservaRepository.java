package com.divan.repository;

import com.divan.entity.Reserva;
import com.divan.entity.Apartamento;
import com.divan.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
    // ═══════════════════════════════════════════
    // BUSCAR POR APARTAMENTO
    // ═══════════════════════════════════════════
    
    List<Reserva> findByApartamento(Apartamento apartamento);
    
    List<Reserva> findByApartamentoId(Long apartamentoId);
    
    List<Reserva> findByApartamentoOrderByDataCheckinDesc(Apartamento apartamento);
    
    List<Reserva> findByApartamentoAndStatus(Apartamento apartamento, Reserva.StatusReservaEnum status);
    
    @Query("SELECT r FROM Reserva r WHERE r.apartamento = :apartamento " +
           "AND r.status = :status ORDER BY r.dataCheckin DESC")
    List<Reserva> findByApartamentoAndStatusOrderByDataCheckinDesc(
        @Param("apartamento") Apartamento apartamento,
        @Param("status") Reserva.StatusReservaEnum status
    );
    
    // ═══════════════════════════════════════════
    // BUSCAR POR CLIENTE
    // ═══════════════════════════════════════════
    
    List<Reserva> findByCliente(Cliente cliente);
    
    List<Reserva> findByClienteId(Long clienteId);
    
    List<Reserva> findByClienteIdAndDataCheckinBetween(
    	    Long clienteId, 
    	    LocalDateTime inicioData, 
    	    LocalDateTime fimData
    	);
    
    // ═══════════════════════════════════════════
    // BUSCAR POR STATUS
    // ═══════════════════════════════════════════
    
    List<Reserva> findByStatus(Reserva.StatusReservaEnum status);
    
    List<Reserva> findByStatusIn(List<Reserva.StatusReservaEnum> statusList);
    
    @Query("SELECT r FROM Reserva r WHERE r.status IN ('ATIVA', 'PRE_RESERVA') " +
           "ORDER BY r.dataCheckin DESC")
    List<Reserva> findReservasAtivasEPreReservas();
    
    /**
     * ✅ Contar reservas por status
     */
    long countByStatus(Reserva.StatusReservaEnum status);
    
    // ═══════════════════════════════════════════
    // BUSCAR POR PERÍODO
    // ═══════════════════════════════════════════
    
    List<Reserva> findByDataCheckinBetween(LocalDateTime inicio, LocalDateTime fim);
    
    List<Reserva> findByDataCheckoutBetween(LocalDateTime inicio, LocalDateTime fim);
    
    List<Reserva> findByDataCheckinBetweenOrDataCheckoutBetween(
        LocalDateTime inicioCheckin, LocalDateTime fimCheckin,
        LocalDateTime inicioCheckout, LocalDateTime fimCheckout
    );
    
    @Query("SELECT r FROM Reserva r WHERE r.dataCheckin BETWEEN :inicio AND :fim")
    List<Reserva> findReservasCriadasNoPeriodo(
        @Param("inicio") LocalDateTime inicio, 
        @Param("fim") LocalDateTime fim
    );
    
    // ═══════════════════════════════════════════
    // VERIFICAR CONFLITOS
    // ═══════════════════════════════════════════
    
    @Query("SELECT r FROM Reserva r WHERE " +
           "r.apartamento.id = :apartamentoId AND " +
           "r.status IN ('ATIVA', 'PRE_RESERVA') AND " +
           "((r.dataCheckin < :checkout) AND (r.dataCheckout > :checkin))")
    List<Reserva> findConflitosReserva(
        @Param("apartamentoId") Long apartamentoId,
        @Param("checkin") LocalDateTime checkin,
        @Param("checkout") LocalDateTime checkout
    );
    
    @Query("SELECT COUNT(r) > 0 FROM Reserva r WHERE " +
           "r.apartamento.id = :apartamentoId AND " +
           "r.status IN ('ATIVA', 'PRE_RESERVA') AND " +
           "((r.dataCheckin < :checkout) AND (r.dataCheckout > :checkin))")
    boolean existeConflito(
        @Param("apartamentoId") Long apartamentoId,
        @Param("checkin") LocalDateTime checkin,
        @Param("checkout") LocalDateTime checkout
    );
    
    // ═══════════════════════════════════════════
    // AGREGAÇÕES E CÁLCULOS
    // ═══════════════════════════════════════════
    
    @Query("SELECT COALESCE(SUM(r.desconto), 0) FROM Reserva r " +
           "WHERE r.dataCheckin BETWEEN :inicio AND :fim")
    BigDecimal findSomaDescontosPorPeriodo(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
    
    // ═══════════════════════════════════════════
    // ✅ NOVOS MÉTODOS PARA RELATÓRIOS
    // ═══════════════════════════════════════════
    
    /**
     * ✅ Buscar reservas para check-in em uma data específica
     * Status válidos: CONFIRMADA, PRE_RESERVA
     */
    @Query("SELECT r FROM Reserva r WHERE DATE(r.dataCheckin) = DATE(:data) " +
           "AND r.status IN ('CONFIRMADA', 'PRE_RESERVA') " +
           "ORDER BY r.dataCheckin ASC")
    List<Reserva> findReservasParaCheckinNaData(@Param("data") LocalDateTime data);
    
    /**
     * ✅ Buscar reservas para check-out em uma data específica
     * Status válidos: ATIVA
     */
    @Query("SELECT r FROM Reserva r WHERE DATE(r.dataCheckout) = DATE(:data) " +
           "AND r.status = 'ATIVA' " +
           "ORDER BY r.dataCheckout ASC")
    List<Reserva> findReservasParaCheckoutNaData(@Param("data") LocalDateTime data);
    
    /**
     * ✅ Buscar reservas ativas (com hóspedes no hotel)
     * Status válido: ATIVA
     */
    @Query("SELECT r FROM Reserva r WHERE r.status = 'ATIVA' " +
           "ORDER BY r.dataCheckin DESC")
    List<Reserva> findReservasAtivas();
    
    /**
     * ✅ Contar reservas ativas
     */
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.status = 'ATIVA'")
    Long contarReservasAtivas();
    
    /**
     * ✅ Contar reservas confirmadas (aguardando check-in)
     */
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.status = 'CONFIRMADA'")
    Long contarReservasConfirmadas();
    
    /**
     * ✅ Buscar check-ins de hoje
     */
    @Query("SELECT r FROM Reserva r WHERE DATE(r.dataCheckin) = CURRENT_DATE " +
           "AND r.status IN ('CONFIRMADA', 'PRE_RESERVA') " +
           "ORDER BY r.dataCheckin ASC")
    List<Reserva> findCheckinsDodia();
    
    /**
     * ✅ Buscar check-outs de hoje
     */
    @Query("SELECT r FROM Reserva r WHERE DATE(r.dataCheckout) = CURRENT_DATE " +
           "AND r.status = 'ATIVA' " +
           "ORDER BY r.dataCheckout ASC")
    List<Reserva> findCheckoutsDodia();
    
    /**
     * ✅ Buscar reservas por período (check-in ou check-out dentro do período)
     */
    @Query("SELECT r FROM Reserva r WHERE " +
           "(r.dataCheckin BETWEEN :inicio AND :fim) OR " +
           "(r.dataCheckout BETWEEN :inicio AND :fim) OR " +
           "(r.dataCheckin <= :inicio AND r.dataCheckout >= :fim) " +
           "ORDER BY r.dataCheckin ASC")
    List<Reserva> findReservasPorPeriodo(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
    
 // ═══════════════════════════════════════════
 // ✅ MÉTODOS PARA DETECÇÃO DE CONFLITOS
 // ═══════════════════════════════════════════

 /**
  * ✅ Buscar reservas por status e data de checkin entre um período
  * Usado para encontrar pré-reservas de um dia específico
  */
 List<Reserva> findByStatusAndDataCheckinBetween(
     Reserva.StatusReservaEnum status, 
     LocalDateTime inicio, 
     LocalDateTime fim
 );

 /**
  * ✅ Buscar reservas por apartamento e status
  * Usado para verificar se apartamento tem reserva ativa
  */
 List<Reserva> findByApartamentoIdAndStatus(
     Long apartamentoId, 
     Reserva.StatusReservaEnum status
 );

 /**
  * ✅ Buscar reservas por apartamento e data de checkin entre um período
  * Usado para verificar disponibilidade de apartamento em uma data
  */
 List<Reserva> findByApartamentoIdAndDataCheckinBetween(
     Long apartamentoId,
     LocalDateTime inicio,
     LocalDateTime fim
 );
 
 List<Reserva> findByClienteAndStatus(Cliente cliente, Reserva.StatusReservaEnum status);
    
}
