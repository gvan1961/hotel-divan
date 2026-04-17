package com.divan.repository;

import com.divan.entity.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    List<Fornecedor> findByNomeContainingIgnoreCase(String nome);
    List<Fornecedor> findByAtivoTrue();
}
