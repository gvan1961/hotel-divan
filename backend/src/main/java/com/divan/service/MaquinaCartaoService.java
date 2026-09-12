package com.divan.service;

import com.divan.entity.CobrancaCartao;
import com.divan.repository.CobrancaCartaoRepository;

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
public class MaquinaCartaoService {

    private static final String WEBHOOK_URL = "https://hook.us1.make.com/clq4c7odvbuhmu1sa6uqvglu2b2u91sq";

    @Autowired
    private CobrancaCartaoRepository cobrancaCartaoRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public CobrancaCartao gerarCobranca(BigDecimal valor, String formaPagamento, Long reservaId, Object itens) {
        if (!formaPagamento.equals("credit_card") && !formaPagamento.equals("debit_card")) {
            throw new RuntimeException("Forma de pagamento inválida: " + formaPagamento);
        }

        // ✅ Cancela qualquer cobrança pendente anterior da MESMA reserva
        if (reservaId != null) {
            cobrancaCartaoRepository.findFirstByReservaIdAndStatusInOrderByDataCriacaoDesc(
                reservaId, List.of("PENDENTE")
            ).ifPresent(anterior -> {
                anterior.setStatus("CANCELADO");
                cobrancaCartaoRepository.save(anterior);
                System.out.println("⚠️ Cobrança de cartão anterior (id=" + anterior.getId() + ") cancelada — substituída por nova cobrança");
            });
        }

        String correlationId = "CARD-" + (reservaId != null ? reservaId : "VENDA") + "-" + UUID.randomUUID().toString().substring(0, 8);

        Map<String, Object> body = Map.of(
            "acao", "criar",
            "valor", valor.setScale(2).toString(),
            "referencia", correlationId,
            "forma_pagamento", formaPagamento
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // ✅ Só confirma que o Make recebeu — o código da ordem vem depois,
        // gravado direto no banco pelo próprio Make (não vem na resposta)
     
        
        @SuppressWarnings("rawtypes")
        
        
        ResponseEntity<String> response = restTemplate.postForEntity(WEBHOOK_URL, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Falha ao acionar a maquininha de cartão");
        }

        Object ordemRecebida;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> resp = mapper.readValue(response.getBody(), Map.class);
            ordemRecebida = resp.get("ordem");
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao interpretar resposta do Make: " + e.getMessage() + " | Resposta recebida: " + response.getBody());
            ordemRecebida = null;
        }
        

        CobrancaCartao cobranca = new CobrancaCartao();
        cobranca.setCorrelationId(correlationId);
        cobranca.setValor(valor);
        cobranca.setFormaPagamento(formaPagamento);
        cobranca.setReservaId(reservaId);
        cobranca.setStatus("PENDENTE");
        cobranca.setDataCriacao(LocalDateTime.now());
        cobranca.setOrdemMercadoPago(ordemRecebida != null ? ordemRecebida.toString() : null);
        
        
        // ordemMercadoPago fica null por enquanto — o Make preenche depois

        if (itens != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                cobranca.setItensJson(mapper.writeValueAsString(itens));
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao serializar itens: " + e.getMessage());
            }
        }

        return cobrancaCartaoRepository.save(cobranca);
    }

    public void cancelarCobranca(Long id) {
        CobrancaCartao cobranca = cobrancaCartaoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cobrança não encontrada"));

        Map<String, Object> body = Map.of(
            "acao", "cancelar",
            "Ordem", cobranca.getOrdemMercadoPago()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

       restTemplate.postForEntity(WEBHOOK_URL, request, String.class);

        cobranca.setStatus("CANCELADO");
        cobrancaCartaoRepository.save(cobranca);
    }

    public void confirmarPagamento(String correlationId) {
        CobrancaCartao cobranca = cobrancaCartaoRepository.findByCorrelationId(correlationId)
            .orElseThrow(() -> new RuntimeException("Cobrança não encontrada para referência: " + correlationId));

        if ("PAGO".equalsIgnoreCase(cobranca.getStatus())) {
            return; // já processado
        }

        cobranca.setStatus("PAGO");
        cobrancaCartaoRepository.save(cobranca);
    }
    
    public CobrancaCartao buscarPorId(Long id) {
        return cobrancaCartaoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cobrança não encontrada"));
    }
    
    public List<CobrancaCartao> listarAtivas() {
        return cobrancaCartaoRepository.findByStatusInOrderByDataCriacaoDesc(List.of("PENDENTE", "PAGO"));
    }
}
