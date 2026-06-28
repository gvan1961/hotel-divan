package com.divan.service;

import com.divan.entity.AlertaAcesso;
import com.divan.entity.Cliente;
import com.divan.repository.AlertaAcessoRepository;
import com.divan.repository.ClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FaceRecognitionService {

    private static final double THRESHOLD = 0.5;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AlertaAcessoRepository alertaAcessoRepository;
    

    // Cadastra ou atualiza embedding e foto do cliente
    public void salvarEmbedding(Long clienteId, float[] descriptor, String fotoBase64) throws Exception {
        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        String descriptorJson = objectMapper.writeValueAsString(descriptor);

        cliente.setFaceDescriptor(descriptorJson);
        cliente.setFotoBase64(fotoBase64);
        cliente.setFaceCriadoEm(LocalDateTime.now());
        cliente.setFaceAtivo(true);

        clienteRepository.save(cliente);
    }

    // Compara descriptor recebido com todos os clientes cadastrados
    public Cliente verificarRosto(float[] descriptorEntrada) throws Exception {
        List<Cliente> clientes = clienteRepository.findByFaceAtivoTrue();

        for (Cliente cliente : clientes) {
            float[] descriptorSalvo = objectMapper
                .readValue(cliente.getFaceDescriptor(), float[].class);

            double distancia = calcularDistancia(descriptorEntrada, descriptorSalvo);

            if (distancia < THRESHOLD) {
                return cliente;
            }
        }
        return null;
    }

    private double calcularDistancia(float[] a, float[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }
    
    public AlertaAcesso registrarDesconhecido(String fotoBase64) {
        AlertaAcesso alerta = new AlertaAcesso();
        alerta.setFotoBase64(fotoBase64);
        alerta.setCriadoEm(LocalDateTime.now());
        alerta.setResolvido(false);
        return alertaAcessoRepository.save(alerta);
    }

    public List<AlertaAcesso> listarAlertas() {
        return alertaAcessoRepository.findByResolvidoFalseOrderByCriadoEmDesc();
    }

    public long contarAlertas() {
        return alertaAcessoRepository.countByResolvidoFalse();
    }

    public void resolverAlerta(Long id, String resolvidoPor, String observacao) {
        AlertaAcesso alerta = alertaAcessoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Alerta não encontrado"));
        alerta.setResolvido(true);
        alerta.setResolvidoEm(LocalDateTime.now());
        alerta.setResolvidoPor(resolvidoPor);
        alerta.setObservacao(observacao);
        alertaAcessoRepository.save(alerta);
    }
}