package com.divan.repository;

import com.divan.entity.FechamentoCaixa;
import com.divan.entity.FechamentoCaixa.StatusCaixa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FechamentoCaixaRepository extends JpaRepository<FechamentoCaixa, Long> {

    Optional<FechamentoCaixa> findByUsuarioIdAndStatus(Long usuarioId, StatusCaixa status);

    List<FechamentoCaixa> findByDataHoraAberturaBetween(LocalDateTime inicio, LocalDateTime fim);

    Optional<FechamentoCaixa> findTopByUsuarioIdOrderByDataHoraAberturaDesc(Long usuarioId);
    
    List<FechamentoCaixa> findByStatus(StatusCaixa status);
}
