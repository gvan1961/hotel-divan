package com.divan.service;

import com.divan.entity.CobrancaPix;
import com.divan.repository.CobrancaPixRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class PixService {

    private static final String WEBHOOK_URL = "https://hook.us1.make.com/fk7iycw05amiingu1sc82hdo35oow2n1";

    @Autowired
    private CobrancaPixRepository cobrancaPixRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public CobrancaPix gerarCobranca(BigDecimal valorReais, String comentario, Long reservaId) {
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

        ResponseEntity<Map> response = restTemplate.postForEntity(WEBHOOK_URL, request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Falha ao gerar cobrança Pix");
        }

        Map<String, Object> resp = response.getBody();

        CobrancaPix cobranca = new CobrancaPix();
        cobranca.setCorrelationId(correlationId);
        cobranca.setValor(valorReais);
        cobranca.setComentario(comentario);
        cobranca.setReservaId(reservaId);
        cobranca.setBrCode((String) resp.get("brCode"));
        cobranca.setQrCodeImage((String) resp.get("qrCodeImage"));
        cobranca.setStatus(CobrancaPix.StatusPixEnum.PENDENTE);
        cobranca.setDataCriacao(LocalDateTime.now());

        return cobrancaPixRepository.save(cobranca);
    }
}