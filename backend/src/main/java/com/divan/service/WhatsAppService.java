package com.divan.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${evolution.api.url}")
    private String apiUrl;

    @Value("${evolution.api.key}")
    private String apiKey;

    @Value("${evolution.api.instancia}")
    private String instancia;

    @Value("${evolution.api.timeout-segundos:10}")
    private int timeoutSegundos;

    /**
     * Resultado de envio com sucesso/erro e payload de resposta.
     */
    public static class ResultadoEnvio {
        private final boolean sucesso;
        private final String responseBody;
        private final String erro;

        public ResultadoEnvio(boolean sucesso, String responseBody, String erro) {
            this.sucesso = sucesso;
            this.responseBody = responseBody;
            this.erro = erro;
        }

        public static ResultadoEnvio sucesso(String body) {
            return new ResultadoEnvio(true, body, null);
        }

        public static ResultadoEnvio erro(String mensagemErro) {
            return new ResultadoEnvio(false, null, mensagemErro);
        }

        public boolean isSucesso() { return sucesso; }
        public String getResponseBody() { return responseBody; }
        public String getErro() { return erro; }
    }

    /**
     * Envia mensagem de texto simples para um número.
     *
     * @param numero  número completo com DDI (ex: 5582996082903)
     * @param texto   conteúdo da mensagem
     * @return resultado do envio
     */
    public ResultadoEnvio enviarTexto(String numero, String texto) {
        try {
            String numeroLimpo = limparNumero(numero);
            if (numeroLimpo == null) {
                return ResultadoEnvio.erro("Número inválido: " + numero);
            }

            Map<String, Object> body = new HashMap<>();
            body.put("number", numeroLimpo);
            body.put("text", texto);

            RestClient client = RestClient.builder()
                    .baseUrl(apiUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader("apikey", apiKey)
                    .build();

            String response = client.post()
                    .uri("/message/sendText/{instancia}", instancia)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            log.info("✅ WhatsApp enviado para {}: {}", numeroLimpo, response);
            return ResultadoEnvio.sucesso(response);

        } catch (Exception e) {
            String erro = "Erro ao enviar WhatsApp para " + numero + ": " + e.getMessage();
            log.error("❌ {}", erro, e);
            return ResultadoEnvio.erro(erro);
        }
    }

    /**
     * Monta o número no formato exigido pelo WhatsApp (apenas dígitos com DDI).
     * Aceita: "82996082903", "(82) 99608-2903", "+55 82 99608-2903"
     * Retorna: "5582996082903"
     */
    public String limparNumero(String numero) {
        if (numero == null) return null;
        String somenteDigitos = numero.replaceAll("\\D", "");
        if (somenteDigitos.isEmpty()) return null;

        if (somenteDigitos.length() == 12 || somenteDigitos.length() == 13) {
            return somenteDigitos;
        }
        if (somenteDigitos.length() == 10 || somenteDigitos.length() == 11) {
            return "55" + somenteDigitos;
        }
        return null;
    }

    /**
     * Combina DDI + celular em um número limpo.
     */
    public String montarNumeroCompleto(String ddi, String celular) {
        if (celular == null || celular.isBlank()) return null;
        String ddiLimpo = (ddi == null || ddi.isBlank()) ? "55" : ddi.replaceAll("\\D", "");
        String celularLimpo = celular.replaceAll("\\D", "");
        if (celularLimpo.isEmpty()) return null;
        return ddiLimpo + celularLimpo;
    }
}
