package com.divan.repository;
import com.divan.entity.NotaVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotaVendaRepository extends JpaRepository<NotaVenda, Long> {
    
    List<NotaVenda> findByReservaId(Long reservaId);
    
    List<NotaVenda> findByDataHoraVendaBetween(LocalDateTime inicio, LocalDateTime fim);
    
    List<NotaVenda> findByTipoVendaAndDataHoraVendaBetween(
        NotaVenda.TipoVendaEnum tipoVenda, 
        LocalDateTime inicio, 
        LocalDateTime fim
    );       
    
    @Query("SELECT n FROM NotaVenda n WHERE n.tipoVenda IN (:tipos) AND n.dataHoraVenda BETWEEN :inicio AND :fim")
    List<NotaVenda> findByTipoVendaInAndPeriodo(
        @Param("tipos") List<NotaVenda.TipoVendaEnum> tipos,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
}