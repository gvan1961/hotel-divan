package com.divan.repository;

import com.divan.entity.LogMovimentacaoCaixa;
import com.divan.entity.FechamentoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogMovimentacaoCaixaRepository extends JpaRepository<LogMovimentacaoCaixa, Long> {
    
    List<LogMovimentacaoCaixa> findByCaixaOrderByDataHoraDesc(FechamentoCaixa caixa);
}
