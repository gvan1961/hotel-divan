package com.divan.service;

import com.divan.entity.CobrancaPix;
import com.divan.repository.CobrancaPixRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PixService {

    private static final String WEBHOOK_URL = "https://hook.us1.make.com/fk7iycw05amiingu1sc82hdo35oow2n1";

    @Autowired
    private CobrancaPixRepository cobrancaPixRepository;
    
    @Autowired
    private com.divan.service.PagamentoService pagamentoService;

    private final RestTemplate restTemplate = criarRestTemplateComTimeout();

    private static RestTemplate criarRestTemplateComTimeout() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 segundos pra conectar
        factory.setReadTimeout(15000);    // 15 segundos esperando resposta
        return new RestTemplate(factory);
    }

    public CobrancaPix gerarCobranca(BigDecimal valorReais, String comentario, Long reservaId, Object itens) {
        String correlationId = "HSP-" + (reservaId != null ? reservaId : "VENDA") + "-" + UUID.randomUUID().toString().substring(0, 8);
        int valorCentavos = valorReais.multiply(BigDecimal.valueOf(100)).intValue();

        Map<String, Object> body = Map.of(
            "value", valorCentavos,
            "comment", comentario != null ? comentario : "",
            "correlationID", correlationId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(WEBHOOK_URL, request, String.class);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            throw new RuntimeException(
                "A Woovi/Make está demorando demais ou instável no momento. " +
                "Não tente gerar novamente agora — aguarde alguns minutos e verifique se a cobrança " +
                "não foi criada mesmo assim antes de tentar de novo."
            );
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Falha ao gerar cobrança Pix");
        }

        Map<String, Object> resp;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapperResposta = new com.fasterxml.jackson.databind.ObjectMapper();
            resp = mapperResposta.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao interpretar resposta do Make: " + e.getMessage()
                + " | Resposta recebida: " + response.getBody());
        }

        CobrancaPix cobranca = new CobrancaPix();
        cobranca.setCorrelationId(correlationId);
        cobranca.setValor(valorReais);
        cobranca.setComentario(comentario);
        cobranca.setReservaId(reservaId);
        cobranca.setBrCode((String) resp.get("brCode"));
        cobranca.setQrCodeImage((String) resp.get("qrCodeImage"));
        cobranca.setStatus(CobrancaPix.StatusPixEnum.PENDENTE);
        cobranca.setDataCriacao(LocalDateTime.now());

        if (itens != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                cobranca.setItensJson(mapper.writeValueAsString(itens));
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao serializar itens do carrinho: " + e.getMessage());
            }
        }

        return cobrancaPixRepository.save(cobranca);
    }

    public List<CobrancaPix> listarPendentes() {
        return cobrancaPixRepository.findByStatusAndReservaIdIsNullOrderByDataCriacaoDesc(
            CobrancaPix.StatusPixEnum.PENDENTE);
    }
    
    public void cancelarCobranca(Long id) {
        CobrancaPix cobranca = cobrancaPixRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cobrança não encontrada"));
        cobranca.setStatus(CobrancaPix.StatusPixEnum.CANCELADO);
        cobrancaPixRepository.save(cobranca);
    }
    
       
    @Transactional
    public void confirmarPagamento(String correlationId) {
        CobrancaPix cobranca = cobrancaPixRepository.findByCorrelationId(correlationId)
            .orElseThrow(() -> new RuntimeException("Cobrança não encontrada para correlationID: " + correlationId));

        if (cobranca.getStatus() == CobrancaPix.StatusPixEnum.PAGO) {
            return; // já processado — evita duplicar
        }

        cobranca.setStatus(CobrancaPix.StatusPixEnum.PAGO);
        cobrancaPixRepository.save(cobranca);
    }
    
    public void marcarExpirado(String correlationId) {
        CobrancaPix cobranca = cobrancaPixRepository.findByCorrelationId(correlationId)
            .orElseThrow(() -> new RuntimeException("Cobrança não encontrada para correlationID: " + correlationId));

        if (cobranca.getStatus() == CobrancaPix.StatusPixEnum.PAGO
            || cobranca.getStatus() == CobrancaPix.StatusPixEnum.CONFIRMADO) {
            return; // já foi pago, nunca sobrescreve com expirado
        }

        cobranca.setStatus(CobrancaPix.StatusPixEnum.EXPIRADO);
        cobrancaPixRepository.save(cobranca);
    }
    
    public List<CobrancaPix> listarAtivas() {
        return cobrancaPixRepository.findByStatusInOrderByDataCriacaoDesc(
            List.of(CobrancaPix.StatusPixEnum.PENDENTE, CobrancaPix.StatusPixEnum.PAGO));
    }

    public void marcarConfirmado(Long id) {
        CobrancaPix cobranca = cobrancaPixRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cobrança não encontrada"));
        cobranca.setStatus(CobrancaPix.StatusPixEnum.CONFIRMADO);
        cobrancaPixRepository.save(cobranca);
    }
    
    public CobrancaPix buscarPorId(Long id) {
        return cobrancaPixRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cobrança não encontrada"));
    }
    
    public CobrancaPix buscarAtivaPorReserva(Long reservaId) {
        return cobrancaPixRepository.findFirstByReservaIdAndStatusInOrderByDataCriacaoDesc(
            reservaId, List.of(CobrancaPix.StatusPixEnum.PENDENTE, CobrancaPix.StatusPixEnum.PAGO))
            .orElse(null);
    }
    
    public List<CobrancaPix> buscarHistorico(LocalDateTime dataInicio, LocalDateTime dataFim, Long reservaId, CobrancaPix.StatusPixEnum status) {
        return cobrancaPixRepository.buscarHistorico(dataInicio, dataFim, reservaId, status);
    }
    
    
}