package com.divan.repository;

import com.divan.entity.HospedagemHospede;
import com.divan.entity.Reserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


@Repository
public interface HospedagemHospedeRepository extends JpaRepository<HospedagemHospede, Long> {

    List<HospedagemHospede> findByReservaId(Long reservaId);

    List<HospedagemHospede> findByReservaIdAndStatus(Long reservaId, HospedagemHospede.StatusEnum status);

    List<HospedagemHospede> findByClienteId(Long clienteId);
    
    List<HospedagemHospede> findByStatus(HospedagemHospede.StatusEnum status);
    
    List<HospedagemHospede> findByClienteIdAndStatus(
    	    Long clienteId,
    	    HospedagemHospede.StatusEnum status
    	);
    
    List<HospedagemHospede> findByPlacaCarroIgnoreCaseAndStatus(
    	    String placaCarro, HospedagemHospede.StatusEnum status);
    
    boolean existsByClienteId(Long clienteId);
    
    @Query("""
    	    SELECT h FROM HospedagemHospede h
    	    JOIN h.reserva r
    	    WHERE h.cliente.id = :clienteId
    	      AND h.status = :statusHospede
    	      AND r.status IN :statusReservas
    	      AND (:ignorarReservaId IS NULL OR r.id <> :ignorarReservaId)
    	      AND r.dataCheckin < :dataCheckout
    	      AND r.dataCheckout > :dataCheckin
    	    ORDER BY r.dataCheckin ASC
    	""")
    	List<HospedagemHospede> buscarConflitosHospedePeriodo(
    	    @Param("clienteId") Long clienteId,
    	    @Param("dataCheckin") LocalDateTime dataCheckin,
    	    @Param("dataCheckout") LocalDateTime dataCheckout,
    	    @Param("statusHospede") HospedagemHospede.StatusEnum statusHospede,
    	    @Param("statusReservas") List<Reserva.StatusReservaEnum> statusReservas,
    	    @Param("ignorarReservaId") Long ignorarReservaId
    	);

    	@Query("""
    	    SELECT h FROM HospedagemHospede h
    	    JOIN h.reserva r
    	    WHERE h.cliente.id = :clienteId
    	      AND h.status = :statusHospede
    	      AND r.status = :statusReserva
    	      AND (:ignorarReservaId IS NULL OR r.id <> :ignorarReservaId)
    	    ORDER BY r.dataCheckin DESC
    	""")
    	List<HospedagemHospede> buscarHospedagensAtivasCliente(
    	    @Param("clienteId") Long clienteId,
    	    @Param("statusHospede") HospedagemHospede.StatusEnum statusHospede,
    	    @Param("statusReserva") Reserva.StatusReservaEnum statusReserva,
    	    @Param("ignorarReservaId") Long ignorarReservaId
    	);

    	@Query("SELECT h FROM HospedagemHospede h WHERE h.reserva.id = :reservaId ORDER BY h.titular DESC, h.id ASC")
    	List<HospedagemHospede> findByReservaIdOrdenado(@Param("reservaId") Long reservaId);
    
}
