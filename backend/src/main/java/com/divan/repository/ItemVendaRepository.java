package com.divan.repository;

import com.divan.entity.ItemVenda;
import com.divan.entity.NotaVenda;
import com.divan.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {
    
    List<ItemVenda> findByNotaVenda(NotaVenda notaVenda);
    
    List<ItemVenda> findByProduto(Produto produto);
    
    @Query("SELECT i FROM ItemVenda i WHERE i.notaVenda.dataHoraVenda BETWEEN :inicio AND :fim")
    List<ItemVenda> findItensPorPeriodo(LocalDateTime inicio, LocalDateTime fim);
    
    @Query("SELECT i.produto, SUM(i.quantidade) FROM ItemVenda i WHERE i.notaVenda.dataHoraVenda BETWEEN :inicio AND :fim GROUP BY i.produto ORDER BY SUM(i.quantidade) DESC")
    List<Object[]> findProdutosMaisVendidosPorPeriodo(LocalDateTime inicio, LocalDateTime fim);
    
 // ✅ NOVO MÉTODO - Buscar itens com produto (JOIN FETCH)
    @Query("SELECT iv FROM ItemVenda iv JOIN FETCH iv.produto WHERE iv.notaVenda.id = :notaVendaId")
    List<ItemVenda> findByNotaVendaIdWithProduto(@Param("notaVendaId") Long notaVendaId);

}
