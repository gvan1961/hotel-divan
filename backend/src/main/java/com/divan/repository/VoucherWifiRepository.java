package com.divan.repository;

import com.divan.entity.VoucherWifi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoucherWifiRepository extends JpaRepository<VoucherWifi, Long> {

    List<VoucherWifi> findByReservaId(Long reservaId);

    List<VoucherWifi> findByReservaIdAndCanceladoFalse(Long reservaId);
    
    boolean existsByUsernameAndCanceladoFalse(String username);
    
}