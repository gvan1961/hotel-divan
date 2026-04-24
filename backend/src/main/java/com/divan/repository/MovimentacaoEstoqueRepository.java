package com.divan.repository;

import com.divan.entity.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByProdutoIdOrderByCriadoEmDesc(Long produtoId);

    List<MovimentacaoEstoque> findByTipoOrderByCriadoEmDesc(MovimentacaoEstoque.TipoMovimentacao tipo);

    @Query("SELECT m FROM MovimentacaoEstoque m WHERE m.criadoEm BETWEEN :inicio AND :fim ORDER BY m.criadoEm DESC")
    List<MovimentacaoEstoque> findByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT m FROM MovimentacaoEstoque m WHERE m.produto.id = :produtoId AND m.criadoEm BETWEEN :inicio AND :fim ORDER BY m.criadoEm DESC")
    List<MovimentacaoEstoque> findByProdutoIdAndPeriodo(@Param("produtoId") Long produtoId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    List<MovimentacaoEstoque> findAllByOrderByCriadoEmDesc();
}
