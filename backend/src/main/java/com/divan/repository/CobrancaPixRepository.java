package com.divan.repository;

import com.divan.entity.CobrancaPix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CobrancaPixRepository extends JpaRepository<CobrancaPix, Long> {
    Optional<CobrancaPix> findByCorrelationId(String correlationId);
}
