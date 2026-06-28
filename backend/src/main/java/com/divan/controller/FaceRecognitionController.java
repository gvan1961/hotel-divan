package com.divan.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.divan.dto.FaceEmbeddingRequestDTO;
import com.divan.dto.FaceMatchResponseDTO;
import com.divan.entity.AlertaAcesso;
import com.divan.entity.Cliente;
import com.divan.repository.ClienteRepository;
import com.divan.service.FaceRecognitionService;


@RestController
@RequestMapping("/api/face")
public class FaceRecognitionController {
	

    @Autowired
    private FaceRecognitionService faceRecognitionService;

    @Autowired
    private ClienteRepository clienteRepository;
    
    // Cadastra/atualiza embedding e foto do cliente
    @PostMapping("/cadastrar")
    public ResponseEntity<String> cadastrar(@RequestBody FaceEmbeddingRequestDTO dto) {
        try {
            faceRecognitionService.salvarEmbedding(
                dto.getClienteId(),
                dto.getDescriptor(),
                dto.getFotoBase64()
            );
            return ResponseEntity.ok("Embedding cadastrado com sucesso");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    // Verifica rosto recebido da câmera de entrada
    @PostMapping("/verificar")
    public ResponseEntity<FaceMatchResponseDTO> verificar(@RequestBody float[] descriptor) {
        FaceMatchResponseDTO response = new FaceMatchResponseDTO();
        try {
            Cliente cliente = faceRecognitionService.verificarRosto(descriptor);

            if (cliente != null) {
                response.setReconhecido(true);
                response.setClienteId(cliente.getId());
                response.setNomeCliente(cliente.getNome());
                response.setClassificacao(
                    cliente.getClassificacao() != null 
                    ? cliente.getClassificacao().toString() 
                    : null
                );
                response.setFotoBase64(cliente.getFotoBase64());
                response.setMensagem("Hóspede reconhecido");
            } else {
                response.setReconhecido(false);
                response.setMensagem("Pessoa não identificada");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setReconhecido(false);
            response.setMensagem("Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
 // Verifica e registra desconhecido automaticamente
    @PostMapping("/verificar-entrada")
    public ResponseEntity<FaceMatchResponseDTO> verificarEntrada(
            @RequestBody Map<String, Object> body) {
        FaceMatchResponseDTO response = new FaceMatchResponseDTO();
        try {
            List<Double> descriptorList = (List<Double>) body.get("descriptor");
            String fotoBase64 = (String) body.get("fotoBase64");

            float[] descriptor = new float[descriptorList.size()];
            for (int i = 0; i < descriptorList.size(); i++) {
                descriptor[i] = descriptorList.get(i).floatValue();
            }

            Cliente cliente = faceRecognitionService.verificarRosto(descriptor);

            if (cliente != null) {
                response.setReconhecido(true);
                response.setClienteId(cliente.getId());
                response.setNomeCliente(cliente.getNome());
                response.setClassificacao(
                    cliente.getClassificacao() != null
                    ? cliente.getClassificacao().toString()
                    : null
                );
                response.setFotoBase64(cliente.getFotoBase64());
                response.setMensagem("Hóspede reconhecido");
                
                // ✅ Verifica se está hospedado agora
                boolean hospedado = clienteRepository
                    .existeHospedagemAtiva(cliente.getId());
                response.setHospedadoAtualmente(hospedado);
                
            } else {
                if (fotoBase64 != null) {
                    AlertaAcesso alerta = faceRecognitionService
                        .registrarDesconhecido(fotoBase64);
                    response.setAlertaId(alerta.getId());
                }
                response.setReconhecido(false);
                response.setMensagem("Pessoa não identificada");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setReconhecido(false);
            response.setMensagem("Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Lista alertas pendentes
    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaAcesso>> listarAlertas() {
        return ResponseEntity.ok(faceRecognitionService.listarAlertas());
    }

    // Conta alertas pendentes
    @GetMapping("/alertas/count")
    public ResponseEntity<Map<String, Long>> contarAlertas() {
        return ResponseEntity.ok(Map.of("total", faceRecognitionService.contarAlertas()));
    }

    // Resolve alerta
    @PatchMapping("/alertas/{id}/resolver")
    public ResponseEntity<String> resolverAlerta(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            faceRecognitionService.resolverAlerta(
                id,
                body.get("resolvidoPor"),
                body.get("observacao")
            );
            return ResponseEntity.ok("Alerta resolvido");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
}
