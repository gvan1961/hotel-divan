package com.divan.repository;

import com.divan.entity.HospedagemHospede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoricoClienteRepository extends JpaRepository<HospedagemHospede, Long> {

    /**
     * Todas as hospedagens de um cliente ordenadas da mais recente
     * para a mais antiga. Inclui reservas onde aparece como
     * hóspede (não só como titular).
     */
    @Query("""
            SELECT h FROM HospedagemHospede h
            JOIN FETCH h.reserva r
            JOIN FETCH r.apartamento a
            WHERE h.cliente.id = :clienteId
              AND r.status IN ('ATIVA', 'FINALIZADA')
            ORDER BY r.dataCheckin DESC
            """)
    List<HospedagemHospede> findByClienteId(@Param("clienteId") Long clienteId);

    /**
     * Busca por CPF (útil quando o cliente não tem id cadastrado
     * mas tem cpf na hospedagem_hospedes).
     */
    @Query("""
            SELECT h FROM HospedagemHospede h
            JOIN FETCH h.reserva r
            JOIN FETCH r.apartamento a
            WHERE h.cpf = :cpf
              AND r.status IN ('ATIVA', 'FINALIZADA')
            ORDER BY r.dataCheckin DESC
            """)
    List<HospedagemHospede> findByCpf(@Param("cpf") String cpf);
    
    @Query("""
            SELECT h FROM HospedagemHospede h
            JOIN FETCH h.cliente c
            JOIN FETCH h.reserva r
            JOIN FETCH r.apartamento a
            WHERE h.cliente IS NOT NULL
              AND r.status IN ('ATIVA', 'FINALIZADA')
              AND (:inicio IS NULL OR r.dataCheckin >= :inicio)
              AND (:fim    IS NULL OR r.dataCheckin <= :fim)
            ORDER BY c.nome ASC
            """)
    List<HospedagemHospede> findTodosParaRanking(
            @Param("inicio") java.time.LocalDateTime inicio,
            @Param("fim")    java.time.LocalDateTime fim
    );
}
