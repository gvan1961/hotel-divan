package com.divan.repository;

import com.divan.entity.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {
    
    // Buscar turnos ativos ordenados
    List<Turno> findByAtivoTrueOrderByOrdem();
    
    // Buscar todos ordenados
    List<Turno> findAllByOrderByOrdem();
    
    // Buscar por nome
    Optional<Turno> findByNome(String nome);
    
    // Verificar se existe turno com o nome
    boolean existsByNome(String nome);
}
