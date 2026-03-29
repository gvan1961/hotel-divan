package com.divan.repository;

import com.divan.entity.DepositoProvisorioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DepositoProvisorioItemRepository extends JpaRepository<DepositoProvisorioItem, Long> {

    List<DepositoProvisorioItem> findByDepositoId(Long depositoId);
}
