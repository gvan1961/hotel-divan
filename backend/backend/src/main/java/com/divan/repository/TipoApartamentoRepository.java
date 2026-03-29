package com.divan.repository;

import com.divan.entity.TipoApartamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoApartamentoRepository extends JpaRepository<TipoApartamento, Long> {
    
    Optional<TipoApartamento> findByTipo(TipoApartamento.TipoEnum tipo);
    
    boolean existsByTipo(TipoApartamento.TipoEnum tipo);
}
