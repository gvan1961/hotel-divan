package com.divan.repository;

import com.divan.entity.ContaAPagar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContaAPagarRepository extends JpaRepository<ContaAPagar, Long> {

    List<ContaAPagar> findByStatus(ContaAPagar.StatusContaEnum status);

    List<ContaAPagar> findByFornecedor(String fornecedor);

    List<ContaAPagar> findByFornecedorObjId(Long fornecedorId);

    List<ContaAPagar> findByCategoria(String categoria);

    @Query("SELECT c FROM ContaAPagar c WHERE c.status = 'EM_ABERTO'")
    List<ContaAPagar> findContasEmAberto();

    @Query("SELECT c FROM ContaAPagar c WHERE c.dataVencimento < :data AND c.status = 'EM_ABERTO'")
    List<ContaAPagar> findContasVencidas(@Param("data") LocalDate data);

    @Query("SELECT c FROM ContaAPagar c WHERE c.dataVencimento BETWEEN :dataInicio AND :dataFim")
    List<ContaAPagar> findContasPorVencimento(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
}
