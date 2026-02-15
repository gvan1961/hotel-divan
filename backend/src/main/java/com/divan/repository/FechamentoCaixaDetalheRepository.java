package com.divan.repository;

import com.divan.entity.FechamentoCaixa;
import com.divan.entity.FechamentoCaixaDetalhe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FechamentoCaixaDetalheRepository extends JpaRepository<FechamentoCaixaDetalhe, Long> {
    
    List<FechamentoCaixaDetalhe> findByFechamentoCaixaIdOrderByDataHora(Long fechamentoCaixaId);
    
    List<FechamentoCaixaDetalhe> findByFechamentoCaixaIdAndTipo(Long fechamentoCaixaId, String tipo);
    
    List<FechamentoCaixaDetalhe> findByFechamentoCaixaIdOrderByDataHoraDesc(Long fechamentoCaixaId);
    
    /**
     * ✅ BUSCAR DETALHES POR CAIXA
     */
    List<FechamentoCaixaDetalhe> findByFechamentoCaixa(FechamentoCaixa fechamentoCaixa);

    /**
     * ✅ BUSCAR DETALHES POR CAIXA ORDENADOS POR DATA/HORA DECRESCENTE
     */
    List<FechamentoCaixaDetalhe> findByFechamentoCaixaOrderByDataHoraDesc(FechamentoCaixa fechamentoCaixa);
        
    
}
