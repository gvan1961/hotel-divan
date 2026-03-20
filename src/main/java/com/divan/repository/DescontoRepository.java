package com.divan.repository;

import com.divan.entity.Desconto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DescontoRepository extends JpaRepository<Desconto, Long> {
    List<Desconto> findByReservaId(Long reservaId);
}
