package com.divan.repository;

import com.divan.entity.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissaoRepository extends JpaRepository<Permissao, Long> {
    
    Optional<Permissao> findByNome(String nome);
    
    List<Permissao> findByCategoria(String categoria);
    
    boolean existsByNome(String nome);
}
