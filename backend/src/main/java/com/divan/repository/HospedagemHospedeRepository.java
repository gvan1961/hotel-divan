package com.divan.repository;

import com.divan.entity.HospedagemHospede;
import com.divan.entity.Reserva;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospedagemHospedeRepository extends JpaRepository<HospedagemHospede, Long> {
    
    List<HospedagemHospede> findByReservaId(Long reservaId);
    
    List<HospedagemHospede> findByReserva(Reserva reserva);
    
    long countByReservaId(Long reservaId);
    
    List<HospedagemHospede> findByClienteId(Long clienteId);
    
    @Modifying
    @Transactional
    void deleteByReserva(Reserva reserva);
        
}
