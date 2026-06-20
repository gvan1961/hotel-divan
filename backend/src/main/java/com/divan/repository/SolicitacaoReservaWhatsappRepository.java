package com.divan.repository;

import com.divan.entity.SolicitacaoReservaWhatsapp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitacaoReservaWhatsappRepository extends JpaRepository<SolicitacaoReservaWhatsapp, Long> {

    List<SolicitacaoReservaWhatsapp> findByStatusOrderByDataSolicitacaoDesc(
        SolicitacaoReservaWhatsapp.StatusSolicitacao status);

    long countByStatus(SolicitacaoReservaWhatsapp.StatusSolicitacao status);
}