package com.divan.repository;

import com.divan.entity.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, Long> {

    /**
     * Busca subscriptions ativas para um evento específico
     * (usado para disparar webhooks)
     */
    List<WebhookSubscription> findByEventoAndAtivoTrue(String evento);

    /**
     * Busca todas as subscriptions ativas
     */
    List<WebhookSubscription> findByAtivoTrue();

    /**
     * Busca subscriptions inativas
     */
    List<WebhookSubscription> findByAtivoFalse();

    /**
     * Busca subscription por nome
     */
    Optional<WebhookSubscription> findByNome(String nome);

    /**
     * Busca subscriptions de um evento específico (ativas ou inativas)
     */
    List<WebhookSubscription> findByEvento(String evento);

    /**
     * Busca subscriptions por URL
     */
    List<WebhookSubscription> findByUrl(String url);

    /**
     * Busca subscriptions que apontam para uma URL específica e estão ativas
     */
    List<WebhookSubscription> findByUrlAndAtivoTrue(String url);

    /**
     * Conta subscriptions ativas
     */
    long countByAtivoTrue();

    /**
     * Conta subscriptions de um evento
     */
    long countByEvento(String evento);

    /**
     * Verifica se existe subscription ativa para um evento
     */
    boolean existsByEventoAndAtivoTrue(String evento);

    /**
     * Busca subscriptions com timeout maior que X segundos
     */
    @Query("SELECT s FROM WebhookSubscription s WHERE s.timeoutSegundos > :segundos")
    List<WebhookSubscription> findComTimeoutMaiorQue(@Param("segundos") Integer segundos);

    /**
     * Busca subscriptions com muitas tentativas configuradas
     */
    @Query("SELECT s FROM WebhookSubscription s WHERE s.maxTentativas > :tentativas")
    List<WebhookSubscription> findComMuitasTentativas(@Param("tentativas") Integer tentativas);

    /**
     * Busca subscriptions ordenadas por nome
     */
    List<WebhookSubscription> findAllByOrderByNomeAsc();

    /**
     * Busca subscriptions criadas recentemente
     */
    @Query("SELECT s FROM WebhookSubscription s ORDER BY s.criadoEm DESC")
    List<WebhookSubscription> findRecentes();

    /**
     * Busca subscriptions que usam um método HTTP específico
     */
    List<WebhookSubscription> findByMetodoHttp(String metodoHttp);

    /**
     * Busca subscriptions por parte do nome (LIKE)
     */
    @Query("SELECT s FROM WebhookSubscription s WHERE LOWER(s.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<WebhookSubscription> findByNomeContendo(@Param("nome") String nome);

    /**
     * Busca subscriptions por parte da URL (LIKE)
     */
    @Query("SELECT s FROM WebhookSubscription s WHERE LOWER(s.url) LIKE LOWER(CONCAT('%', :url, '%'))")
    List<WebhookSubscription> findByUrlContendo(@Param("url") String url);

    /**
     * Busca subscriptions com secret configurado
     */
    @Query("SELECT s FROM WebhookSubscription s WHERE s.secret IS NOT NULL AND s.secret != ''")
    List<WebhookSubscription> findComSecret();

    /**
     * Busca subscriptions sem secret (inseguras)
     */
    @Query("SELECT s FROM WebhookSubscription s WHERE s.secret IS NULL OR s.secret = ''")
    List<WebhookSubscription> findSemSecret();

    /**
     * Estatísticas de subscriptions por evento
     */
    @Query("SELECT s.evento, COUNT(s) FROM WebhookSubscription s GROUP BY s.evento ORDER BY COUNT(s) DESC")
    List<Object[]> estatisticasPorEvento();

    /**
     * Verifica se já existe subscription com mesmo nome
     */
    boolean existsByNome(String nome);
}