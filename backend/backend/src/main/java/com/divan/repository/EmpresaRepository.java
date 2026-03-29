package com.divan.repository;

import com.divan.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    
    Optional<Empresa> findByCnpj(String cnpj);
    
    List<Empresa> findByNomeEmpresaContainingIgnoreCase(String nome);
    
    @Query("SELECT e FROM Empresa e WHERE e.nomeEmpresa LIKE %:nome%")
    List<Empresa> buscarPorNome(String nome);
    
    boolean existsByCnpj(String cnpj);
}
