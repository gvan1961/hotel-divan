package com.divan.repository;

import com.divan.entity.Apartamento;
import com.divan.entity.HospedagemHospede;
import com.divan.entity.TipoApartamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApartamentoRepository extends JpaRepository<Apartamento, Long> {
    
    Optional<Apartamento> findByNumeroApartamento(String numeroApartamento);
    
    List<Apartamento> findByStatus(Apartamento.StatusEnum status);
    
    List<Apartamento> findByTipoApartamento(TipoApartamento tipoApartamento);
    
    @Query("SELECT a FROM Apartamento a WHERE a.status = 'DISPONIVEL'")
    List<Apartamento> findDisponiveis();
    
    @Query("SELECT a FROM Apartamento a WHERE a.status = 'OCUPADO'")
    List<Apartamento> findOcupados();
    
    @Query("SELECT a FROM Apartamento a WHERE a.id NOT IN " +
           "(SELECT r.apartamento.id FROM Reserva r WHERE " +
           "r.status = 'ATIVA' AND " +
           "((r.dataCheckin <= :checkout AND r.dataCheckout >= :checkin)))")
    List<Apartamento> findDisponiveisParaPeriodo(LocalDateTime checkin, LocalDateTime checkout);
    
    boolean existsByNumeroApartamento(String numeroApartamento);  
    
    List<HospedagemHospede> findByStatus(HospedagemHospede.StatusEnum status);

    
}
