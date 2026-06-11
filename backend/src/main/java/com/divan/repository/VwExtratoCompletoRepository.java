package com.divan.repository;

import com.divan.entity.VwExtratoCompleto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VwExtratoCompletoRepository extends JpaRepository<VwExtratoCompleto, Long> {
    List<VwExtratoCompleto> findByEmpresaId(Long empresaId);
    List<VwExtratoCompleto> findByReservaId(Long reservaId);
    List<VwExtratoCompleto> findByEmpresaIdAndDataCheckinBetween(
        Long empresaId, 
        java.time.LocalDateTime inicio, 
        java.time.LocalDateTime fim
    );
    
    List<VwExtratoCompleto> findByEmpresaIdOrderByReservaIdAscDataHoraLancamentoAsc(Long empresaId);
    
}