package com.divan.repository;

import com.divan.entity.DepositoProvisorio;
import com.divan.entity.DepositoProvisorio.StatusDeposito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepositoProvisorioRepository extends JpaRepository<DepositoProvisorio, Long> {

	List<DepositoProvisorio> findByStatus(StatusDeposito status);
}