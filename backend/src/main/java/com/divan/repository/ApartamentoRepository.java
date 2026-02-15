package com.divan.repository;

import com.divan.entity.Apartamento;
import com.divan.entity.TipoApartamento;
import com.divan.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // QUERY CORRIGIDA - Buscar apartamentos disponíveis para reserva
    @Query("SELECT a FROM Apartamento a WHERE " +
    	       "a.status = 'DISPONIVEL' AND " +
    	       "a.id NOT IN (" +
    	       "  SELECT r.apartamento.id FROM Reserva r WHERE " +
    	       "  r.status IN ('ATIVA', 'PRE_RESERVA') AND " +
    	       "  (r.dataCheckin < :checkout AND r.dataCheckout > :checkin)" +
    	       ")")
    	List<Apartamento> findDisponiveisParaReserva(
    	    @Param("checkin") LocalDateTime checkin,
    	    @Param("checkout") LocalDateTime checkout
    	);
    
    /**
     * ✅ Listar apartamentos ordenados por número (crescente)
     */
    List<Apartamento> findAllByOrderByNumeroApartamentoAsc();
    
    
    
    // Buscar apartamentos por múltiplos status
    @Query("SELECT a FROM Apartamento a WHERE a.status IN :statusList")
    List<Apartamento> findByStatusIn(@Param("statusList") List<Apartamento.StatusEnum> statusList);
    
    boolean existsByNumeroApartamento(String numeroApartamento);
    long countByStatus(Apartamento.StatusEnum status);
}
