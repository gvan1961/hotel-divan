package com.divan.repository;

import com.divan.entity.Webhook;
import com.divan.entity.Webhook.StatusWebhookEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook, Long> {

    /**
     * Busca webhooks por status
     */
    List<Webhook> findByStatus(StatusWebhookEnum status);

    /**
     * Busca webhooks por evento
     */
    List<Webhook> findByEvento(String evento);

    /**
     * Busca webhooks por entidade
     * ✅ CORRIGIDO - Usando @Query explícita
     */
    @Query("SELECT w FROM Webhook w WHERE w.entidadeTipo = :tipo AND w.entidadeId = :id")
    List<Webhook> findByEntidadaTipoAndEntidadeId(@Param("tipo") String entidadeTipo, @Param("id") Long entidadeId);

    /**
     * Busca webhooks pendentes que precisam ser reenviados
     */
    @Query("SELECT w FROM Webhook w WHERE " +
           "(w.status = 'PENDING' OR w.status = 'SENDING') " +
           "AND w.tentativas < w.maxTentativas " +
           "AND (w.proximaTentativa IS NULL OR w.proximaTentativa <= :agora)")
    List<Webhook> findPendentesParaReenvio(@Param("agora") LocalDateTime agora);

    /**
     * Busca webhooks falhados
     */
    @Query("SELECT w FROM Webhook w WHERE " +
           "w.status = 'FAILED' " +
           "AND w.tentativas >= w.maxTentativas")
    List<Webhook> findFalhados();

    /**
     * Busca webhooks de uma subscription específica
     */
    List<Webhook> findBySubscriptionId(Long subscriptionId);

    /**
     * Busca webhooks criados em um período
     */
    List<Webhook> findByCriadoEmBetween(LocalDateTime inicio, LocalDateTime fim);

    /**
     * Busca webhooks com sucesso de uma entidade específica
     */
    @Query("SELECT w FROM Webhook w WHERE " +
           "w.entidadeTipo = :tipo " +
           "AND w.entidadeId = :id " +
           "AND w.status = 'SUCCESS' " +
           "ORDER BY w.criadoEm DESC")
    List<Webhook> findSucessosPorEntidade(@Param("tipo") String tipo, @Param("id") Long id);

    /**
     * Conta webhooks pendentes
     */
    long countByStatus(StatusWebhookEnum status);

    /**
     * Conta webhooks de um evento específico
     */
    long countByEvento(String evento);

    /**
     * Busca últimos webhooks
     */
    List<Webhook> findTop10ByOrderByCriadoEmDesc();

    /**
     * Busca webhooks com erro específico
     */
    @Query("SELECT w FROM Webhook w WHERE " +
           "w.status = 'FAILED' " +
           "AND w.erroMensagem LIKE %:erro%")
    List<Webhook> findByErroContendo(@Param("erro") String erro);

    /**
     * Busca webhooks lentos
     */
    @Query("SELECT w FROM Webhook w WHERE " +
           "w.tempoRespostaMs > :tempoMs " +
           "ORDER BY w.tempoRespostaMs DESC")
    List<Webhook> findLentos(@Param("tempoMs") Long tempoMs);

    /**
     * Estatísticas por status em um período
     */
    @Query("SELECT w.status, COUNT(w) FROM Webhook w WHERE " +
           "w.criadoEm BETWEEN :inicio AND :fim " +
           "GROUP BY w.status")
    List<Object[]> estatisticasPorStatus(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    /**
     * Estatísticas por evento em um período
     */
    @Query("SELECT w.evento, COUNT(w), AVG(w.tempoRespostaMs) FROM Webhook w WHERE " +
           "w.criadoEm BETWEEN :inicio AND :fim " +
           "GROUP BY w.evento " +
           "ORDER BY COUNT(w) DESC")
    List<Object[]> estatisticasPorEvento(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    /**
     * Remove webhooks antigos
     */
    void deleteByCriadoEmBefore(LocalDateTime dataLimite);

    /**
     * Busca webhooks que precisam ser cancelados
     */
    @Query("SELECT w FROM Webhook w WHERE " +
           "w.status IN ('PENDING', 'SENDING') " +
           "AND w.tentativas >= w.maxTentativas")
    List<Webhook> findParaCancelar();
}