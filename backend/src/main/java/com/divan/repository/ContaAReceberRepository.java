package com.divan.repository;

import com.divan.entity.Cliente;
import com.divan.entity.ContaAReceber;
import com.divan.entity.Empresa;
import com.divan.entity.Reserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContaAReceberRepository extends JpaRepository<ContaAReceber, Long> {
    
    List<ContaAReceber> findByCliente(Cliente cliente);
    
    List<ContaAReceber> findByEmpresa(Empresa empresa);
    
    List<ContaAReceber> findByStatus(ContaAReceber.StatusContaEnum status);
    
    @Query("SELECT c FROM ContaAReceber c WHERE c.status = 'EM_ABERTO'")
    List<ContaAReceber> findContasEmAberto();
    
    @Query("SELECT c FROM ContaAReceber c WHERE c.dataVencimento < :data AND c.status = 'EM_ABERTO'")
    List<ContaAReceber> findContasVencidas(LocalDate data);
    
    @Query("SELECT c FROM ContaAReceber c WHERE c.dataVencimento BETWEEN :dataInicio AND :dataFim")
    List<ContaAReceber> findContasPorVencimento(LocalDate dataInicio, LocalDate dataFim);
    
    List<ContaAReceber> findByClienteAndStatus(Cliente cliente, ContaAReceber.StatusContaEnum status);
    
    List<ContaAReceber> findByEmpresaAndStatus(Empresa empresa, ContaAReceber.StatusContaEnum status);
    
    Optional<ContaAReceber> findByReserva(Reserva reserva);
}
