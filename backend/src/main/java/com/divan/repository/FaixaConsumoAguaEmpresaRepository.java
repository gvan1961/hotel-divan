package com.divan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.divan.entity.FaixaConsumoAguaEmpresa;

@Repository
public interface FaixaConsumoAguaEmpresaRepository extends JpaRepository<FaixaConsumoAguaEmpresa, Long> {
    Optional<FaixaConsumoAguaEmpresa> findByEmpresaIdAndQtdHospedes(Long empresaId, Integer qtdHospedes);
    
    List<FaixaConsumoAguaEmpresa> findByEmpresaId(Long empresaId);
}
