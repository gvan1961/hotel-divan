package com.divan.repository;

import com.divan.entity.FotoFuncionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FotoFuncionarioRepository extends JpaRepository<FotoFuncionario, Long> {

    Optional<FotoFuncionario> findByClienteIdAndAtivaTrue(Long clienteId);

    List<FotoFuncionario> findByClienteId(Long clienteId);

    List<FotoFuncionario> findByAtivaTrue();
}
