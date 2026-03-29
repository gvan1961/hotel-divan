package com.divan.repository;

import com.divan.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    @EntityGraph(attributePaths = {"perfis", "perfis.permissoes", "permissoes"})
    Optional<Usuario> findByUsername(String username);
    
    @EntityGraph(attributePaths = {"perfis", "perfis.permissoes", "permissoes"})
    Optional<Usuario> findById(Long id);
    
    // Métodos adicionais necessários
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    Optional<Usuario> findByEmail(String email);
}