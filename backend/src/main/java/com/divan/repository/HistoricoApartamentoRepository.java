package com.divan.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.divan.entity.HistoricoApartamento;

@Repository
public interface HistoricoApartamentoRepository extends JpaRepository<HistoricoApartamento, Long> {
    List<HistoricoApartamento> findByApartamentoIdOrderByDataHoraDesc(Long apartamentoId);
    
    List<HistoricoApartamento> findByAcaoAndDataHoraBetweenOrderByDataHoraDesc(
    	    String acao, LocalDateTime inicio, LocalDateTime fim);
}