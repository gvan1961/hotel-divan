package com.divan.repository;
import com.divan.entity.NotaVenda;
import com.divan.entity.Reserva;

import org.springframework.data.jpa.repository.JpaRepository;
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
    
    List<NotaVenda> findByReserva(Reserva reserva); 
    
    
}