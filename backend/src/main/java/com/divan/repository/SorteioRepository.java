package com.divan.repository;

import com.divan.entity.Sorteio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SorteioRepository extends JpaRepository<Sorteio, Long> {
    List<Sorteio> findByStatusOrderByDataCriacaoDesc(Sorteio.StatusEnum status);
    Optional<Sorteio> findFirstByStatusOrderByDataCriacaoDesc(Sorteio.StatusEnum status);
    List<Sorteio> findAllByOrderByDataCriacaoDesc();
}
