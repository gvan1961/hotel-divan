package com.divan.repository;

import com.divan.entity.Diaria;
import com.divan.entity.TipoApartamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiariaRepository extends JpaRepository<Diaria, Long> {
    
    List<Diaria> findByTipoApartamento(TipoApartamento tipoApartamento);
           
    List<Diaria> findByTipoApartamentoOrderByQuantidade(TipoApartamento tipoApartamento);
    
    boolean existsByTipoApartamentoAndQuantidade(TipoApartamento tipoApartamento, Integer quantidade);
        
    List<Diaria> findByTipoApartamentoOrderByQuantidadeAsc(TipoApartamento tipoApartamento);
    
    Optional<Diaria> findByTipoApartamentoAndQuantidade(TipoApartamento tipoApartamento, Integer quantidade);
    
}
