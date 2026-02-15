package com.divan.repository;

import com.divan.entity.FechamentoCaixa;
import com.divan.entity.Pagamento;
import com.divan.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    
    // ═══════════════════════════════════════════
    // BUSCAR POR CAIXA
    // ═══════════════════════════════════════════
    
    /**
     * ✅ Buscar pagamentos por ID do caixa
     */
    List<Pagamento> findByCaixaId(Long caixaId);
    
    /**
     * ✅ Buscar pagamentos por objeto FechamentoCaixa
     * IMPORTANTE: Este método é usado pelo FechamentoCaixaService
     */
    List<Pagamento> findByCaixa(FechamentoCaixa caixa);
    
    /**
     * ✅ Buscar pagamentos por caixa e tipo
     */
    List<Pagamento> findByCaixaAndTipo(FechamentoCaixa caixa, String tipo);
    
    /**
     * ✅ Buscar pagamentos por caixa ordenados por data
     */
    List<Pagamento> findByCaixaOrderByDataHoraDesc(FechamentoCaixa caixa);
    
    // ═══════════════════════════════════════════
    // BUSCAR POR RESERVA
    // ═══════════════════════════════════════════
    
    /**
     * ✅ Buscar pagamentos por ID da reserva
     */
    List<Pagamento> findByReservaId(Long reservaId);
    
    /**
     * ✅ Buscar pagamentos por objeto Reserva
     */
    List<Pagamento> findByReserva(Reserva reserva);
    
    /**
     * ✅ Buscar pagamentos de reservas em um período
     */
    List<Pagamento> findByReservaIsNotNullAndDataHoraBetween(
        LocalDateTime inicio, 
        LocalDateTime fim
    );
    
    /**
     * ✅ Buscar pagamentos SEM reserva (vendas avulsas) em um período
     */
    List<Pagamento> findByReservaIsNullAndDataHoraBetween(
        LocalDateTime inicio, 
        LocalDateTime fim
    );
    
    // ═══════════════════════════════════════════
    // BUSCAR POR PERÍODO
    // ═══════════════════════════════════════════
    
    /**
     * ✅ Buscar pagamentos por período
     */
    List<Pagamento> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
    
    /**
     * ✅ Buscar pagamentos por tipo e período
     */
    List<Pagamento> findByTipoAndDataHoraBetween(
        String tipo, 
        LocalDateTime inicio, 
        LocalDateTime fim
    );
    
    /**
     * ✅ Buscar pagamentos do dia usando query
     */
    @Query("SELECT p FROM Pagamento p WHERE DATE(p.dataHora) = :data")
    List<Pagamento> findPagamentosDoDia(@Param("data") LocalDate data);
    
    /**
     * ✅ Buscar pagamentos por período usando query
     */
    @Query("SELECT p FROM Pagamento p WHERE p.dataHora BETWEEN :inicio AND :fim ORDER BY p.dataHora DESC")
    List<Pagamento> findPagamentosPorPeriodo(
        @Param("inicio") LocalDateTime inicio, 
        @Param("fim") LocalDateTime fim
    );
    
    /**
     * ✅ Buscar pagamentos por período e caixa
     */
    @Query("SELECT p FROM Pagamento p WHERE p.caixa.id = :caixaId AND p.dataHora BETWEEN :inicio AND :fim ORDER BY p.dataHora DESC")
    List<Pagamento> findPagamentosPorPeriodoECaixa(
        @Param("caixaId") Long caixaId, 
        @Param("inicio") LocalDateTime inicio, 
        @Param("fim") LocalDateTime fim
    );
    
    // ═══════════════════════════════════════════
    // QUERIES CUSTOMIZADAS COM AGREGAÇÕES
    // ═══════════════════════════════════════════
    
    /**
     * ✅ Somar total de pagamentos por caixa
     */
    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.caixa.id = :caixaId")
    java.math.BigDecimal somarTotalPorCaixa(@Param("caixaId") Long caixaId);
    
    /**
     * ✅ Somar total de pagamentos por reserva
     */
    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.reserva.id = :reservaId")
    java.math.BigDecimal somarTotalPorReserva(@Param("reservaId") Long reservaId);
    
    /**
     * ✅ Somar total de pagamentos por forma de pagamento e período
     */
    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.formaPagamento = :formaPagamento AND p.dataHora BETWEEN :inicio AND :fim")
    java.math.BigDecimal somarTotalPorFormaPagamentoEPeriodo(
        @Param("formaPagamento") com.divan.enums.FormaPagamento formaPagamento,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
    
    /**
     * ✅ Contar pagamentos por caixa
     */
    @Query("SELECT COUNT(p) FROM Pagamento p WHERE p.caixa.id = :caixaId")
    Long contarPagamentosPorCaixa(@Param("caixaId") Long caixaId);
    
    /**
     * ✅ Buscar últimos pagamentos do caixa
     */
    @Query("SELECT p FROM Pagamento p WHERE p.caixa.id = :caixaId ORDER BY p.dataHora DESC")
    List<Pagamento> findUltimosPagamentosDoCaixa(@Param("caixaId") Long caixaId);
}