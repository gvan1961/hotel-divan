package com.divan.repository;

import com.divan.entity.AlertaAcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertaAcessoRepository extends JpaRepository<AlertaAcesso, Long> {

    // Busca alertas não resolvidos (para exibir na recepção)
    List<AlertaAcesso> findByResolvidoFalseOrderByCriadoEmDesc();

    // Conta alertas pendentes (para badge de notificação)
    long countByResolvidoFalse();   
   
    boolean existsByResolvidoFalseAndCriadoEmAfter(LocalDateTime dataLimite);

    Optional<AlertaAcesso> findFirstByResolvidoFalseOrderByCriadoEmDesc();
    
    void deleteByResolvidoFalseAndCriadoEmBefore(LocalDateTime dataLimite);
}