package com.divan.repository;

import com.divan.entity.FechamentoCaixa;
import com.divan.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FechamentoCaixaRepository extends JpaRepository<FechamentoCaixa, Long> {
    
    /**
     * ✅ BUSCAR CAIXA ABERTO DO USUÁRIO
     */
    Optional<FechamentoCaixa> findByUsuarioAndStatus(
        Usuario usuario, 
        FechamentoCaixa.StatusCaixaEnum status
    );
    
    /**
     * ✅ BUSCAR CAIXAS POR PERÍODO
     */
    List<FechamentoCaixa> findByDataHoraAberturaBetween(
        LocalDateTime dataInicio, 
        LocalDateTime dataFim
    );
    
    /**
     * ✅ BUSCAR CAIXAS POR USUÁRIO
     */
    List<FechamentoCaixa> findByUsuarioOrderByDataHoraAberturaDesc(Usuario usuario);
 
    /**
     * ✅ BUSCAR CAIXAS POR STATUS
     */
    List<FechamentoCaixa> findByStatusOrderByDataHoraAberturaDesc(
        FechamentoCaixa.StatusCaixaEnum status
    );
    
    Optional<FechamentoCaixa> findFirstByStatusOrderByDataHoraAberturaDesc(
        FechamentoCaixa.StatusCaixaEnum status
    );

    // ═══════════════════════════════════════════════════════════
    // ⭐ MÉTODOS COM @Query PERSONALIZADA
    // ═══════════════════════════════════════════════════════════

    /**
     * 🔍 BUSCAR POR USUÁRIO E PERÍODO
     */
    @Query("SELECT f FROM FechamentoCaixa f WHERE f.usuario.id = :usuarioId " +
           "AND f.dataHoraAbertura BETWEEN :inicio AND :fim " +
           "ORDER BY f.dataHoraAbertura DESC")
    List<FechamentoCaixa> buscarPorUsuarioEPeriodo(
        @Param("usuarioId") Long usuarioId,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    /**
     * 🔍 BUSCAR POR STATUS E PERÍODO
     */
    @Query("SELECT f FROM FechamentoCaixa f WHERE f.status = :status " +
           "AND f.dataHoraAbertura BETWEEN :inicio AND :fim " +
           "ORDER BY f.dataHoraAbertura DESC")
    List<FechamentoCaixa> buscarPorStatusEPeriodo(
        @Param("status") FechamentoCaixa.StatusCaixaEnum status,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    /**
     * 🔍 BUSCAR POR USUÁRIO, STATUS E PERÍODO
     */
    @Query("SELECT f FROM FechamentoCaixa f WHERE f.usuario.id = :usuarioId " +
           "AND f.status = :status " +
           "AND f.dataHoraAbertura BETWEEN :inicio AND :fim " +
           "ORDER BY f.dataHoraAbertura DESC")
    List<FechamentoCaixa> buscarPorUsuarioStatusEPeriodo(
        @Param("usuarioId") Long usuarioId,
        @Param("status") FechamentoCaixa.StatusCaixaEnum status,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
}