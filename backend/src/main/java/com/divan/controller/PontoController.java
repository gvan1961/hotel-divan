package com.divan.controller;

import com.divan.entity.Cliente;
import com.divan.entity.FotoFuncionario;
import com.divan.entity.RegistroPonto;
import com.divan.repository.ClienteRepository;
import com.divan.repository.FotoFuncionarioRepository;
import com.divan.repository.RegistroPontoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ponto")
@CrossOrigin(origins = "*")
public class PontoController {

    @Autowired
    private RegistroPontoRepository registroPontoRepository;

    @Autowired
    private FotoFuncionarioRepository fotoFuncionarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // ============================================
    // REGISTROS DE PONTO
    // ============================================

    @GetMapping
    public ResponseEntity<List<RegistroPonto>> listarTodos() {
        return ResponseEntity.ok(registroPontoRepository.findAll());
    }

    @GetMapping("/hoje")
    public ResponseEntity<List<RegistroPonto>> listarHoje() {
        return ResponseEntity.ok(registroPontoRepository.findByData(LocalDateTime.now()));
    }

    @GetMapping("/funcionario/{clienteId}")
    public ResponseEntity<List<RegistroPonto>> listarPorFuncionario(@PathVariable Long clienteId) {
        return ResponseEntity.ok(registroPontoRepository.findByClienteIdOrderByDataHoraDesc(clienteId));
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<RegistroPonto>> listarPorPeriodo(
            @RequestParam String inicio,
            @RequestParam String fim) {
        LocalDateTime dtInicio = LocalDate.parse(inicio).atStartOfDay();
        LocalDateTime dtFim = LocalDate.parse(fim).atTime(23, 59, 59);
        return ResponseEntity.ok(registroPontoRepository.findByPeriodo(dtInicio, dtFim));
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody Map<String, Object> body) {
        try {
            Long clienteId = Long.parseLong(body.get("clienteId").toString());
            String tipo = body.get("tipo").toString();
            Boolean reconhecimentoFacial = body.get("reconhecimentoFacial") != null
                    && Boolean.parseBoolean(body.get("reconhecimentoFacial").toString());
            Double confianca = body.get("confianca") != null
                    ? Double.parseDouble(body.get("confianca").toString()) : null;
            String observacao = body.get("observacao") != null
                    ? body.get("observacao").toString() : null;

            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

            RegistroPonto registro = new RegistroPonto();
            registro.setCliente(cliente);
            registro.setTipo(RegistroPonto.TipoPonto.valueOf(tipo));
            registro.setDataHora(LocalDateTime.now());
            registro.setReconhecimentoFacial(reconhecimentoFacial);
            registro.setConfiancaReconhecimento(confianca);
            registro.setObservacao(observacao);

            RegistroPonto salvo = registroPontoRepository.save(registro);

            return ResponseEntity.ok(Map.of(
                "id", salvo.getId(),
                "funcionario", cliente.getNome(),
                "tipo", salvo.getTipo(),
                "dataHora", salvo.getDataHora().toString(),
                "mensagem", "Ponto registrado com sucesso!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!registroPontoRepository.existsById(id)) return ResponseEntity.notFound().build();
        registroPontoRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ============================================
    // FOTOS DE FUNCIONÁRIOS
    // ============================================

    @GetMapping("/fotos")
    public ResponseEntity<List<FotoFuncionario>> listarFotos() {
        return ResponseEntity.ok(fotoFuncionarioRepository.findByAtivaTrue());
    }

    @GetMapping("/fotos/funcionario/{clienteId}")
    public ResponseEntity<?> buscarFotoPorFuncionario(@PathVariable Long clienteId) {
        return fotoFuncionarioRepository.findByClienteIdAndAtivaTrue(clienteId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/fotos/cadastrar")
    public ResponseEntity<?> cadastrarFoto(@RequestBody Map<String, Object> body) {
        try {
            Long clienteId = Long.parseLong(body.get("clienteId").toString());
            String fotoBase64 = body.get("fotoBase64").toString();

            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

            // Desativar foto anterior
            fotoFuncionarioRepository.findByClienteIdAndAtivaTrue(clienteId)
                    .ifPresent(foto -> {
                        foto.setAtiva(false);
                        fotoFuncionarioRepository.save(foto);
                    });

            // Salvar nova foto
            FotoFuncionario foto = new FotoFuncionario();
            foto.setCliente(cliente);
            foto.setFotoBase64(fotoBase64);
            foto.setAtiva(true);
            foto.setCriadoEm(LocalDateTime.now());

            FotoFuncionario salva = fotoFuncionarioRepository.save(foto);

            return ResponseEntity.ok(Map.of(
                "id", salva.getId(),
                "funcionario", cliente.getNome(),
                "mensagem", "Foto cadastrada com sucesso!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/fotos/{id}")
    public ResponseEntity<?> excluirFoto(@PathVariable Long id) {
        return fotoFuncionarioRepository.findById(id).map(foto -> {
            foto.setAtiva(false);
            fotoFuncionarioRepository.save(foto);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/relatorio-horas")
    public ResponseEntity<?> relatorioPorHoras(
            @RequestParam String inicio,
            @RequestParam String fim,
            @RequestParam(required = false) Long clienteId) {
        try {
            LocalDateTime dtInicio = LocalDate.parse(inicio).atStartOfDay();
            LocalDateTime dtFim = LocalDate.parse(fim).atTime(23, 59, 59);

            List<RegistroPonto> registros = clienteId != null
                ? registroPontoRepository.findByClienteIdAndPeriodo(clienteId, dtInicio, dtFim)
                : registroPontoRepository.findByPeriodo(dtInicio, dtFim);

            // Agrupar por funcionário e data
            Map<String, Map<LocalDate, List<RegistroPonto>>> agrupado = new LinkedHashMap<>();

            for (RegistroPonto r : registros) {
                String nomeFuncionario = r.getCliente().getNome();
                LocalDate data = r.getDataHora().toLocalDate();

                agrupado
                    .computeIfAbsent(nomeFuncionario, k -> new LinkedHashMap<>())
                    .computeIfAbsent(data, k -> new ArrayList<>())
                    .add(r);
            }

            // Calcular horas por funcionário por dia
            List<Map<String, Object>> resultado = new ArrayList<>();

            for (Map.Entry<String, Map<LocalDate, List<RegistroPonto>>> funcEntry : agrupado.entrySet()) {
                String nome = funcEntry.getKey();
                long totalMinutosFuncionario = 0;

                for (Map.Entry<LocalDate, List<RegistroPonto>> diaEntry : funcEntry.getValue().entrySet()) {
                    LocalDate data = diaEntry.getKey();
                    List<RegistroPonto> regsDia = diaEntry.getValue();

                    // Ordenar por hora
                    regsDia.sort(Comparator.comparing(RegistroPonto::getDataHora));

                    long totalMinutosDia = 0;
                    LocalDateTime entrada = null;

                    for (RegistroPonto r : regsDia) {
                        switch (r.getTipo()) {
                            case ENTRADA, RETORNO_INTERVALO -> entrada = r.getDataHora();
                            case SAIDA_INTERVALO, SAIDA -> {
                                if (entrada != null) {
                                    totalMinutosDia += java.time.Duration.between(entrada, r.getDataHora()).toMinutes();
                                    entrada = null;
                                }
                            }
                        }
                    }

                    totalMinutosFuncionario += totalMinutosDia;

                    Map<String, Object> linha = new LinkedHashMap<>();
                    linha.put("funcionario", nome);
                    linha.put("data", data.toString());
                    linha.put("registros", regsDia.stream().map(r -> Map.of(
                        "tipo", r.getTipo(),
                        "hora", r.getDataHora().toLocalTime().toString()
                    )).collect(java.util.stream.Collectors.toList()));
                    linha.put("totalMinutos", totalMinutosDia);
                    linha.put("totalHoras", String.format("%dh%02dmin",
                        totalMinutosDia / 60, totalMinutosDia % 60));
                    resultado.add(linha);
                }

                // Linha de total do funcionário
                Map<String, Object> totalFunc = new LinkedHashMap<>();
                totalFunc.put("funcionario", nome);
                totalFunc.put("data", "TOTAL");
                totalFunc.put("registros", List.of());
                totalFunc.put("totalMinutos", totalMinutosFuncionario);
                totalFunc.put("totalHoras", String.format("%dh%02dmin",
                    totalMinutosFuncionario / 60, totalMinutosFuncionario % 60));
                resultado.add(totalFunc);
            }

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
