package com.divan.controller;

import com.divan.entity.ExtratoReserva;
import com.divan.entity.Produto;
import com.divan.repository.ExtratoReservaRepository;
import com.divan.repository.ProdutoRepository;
import com.divan.service.JantarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jantar")
@CrossOrigin(origins = "*")
public class JantarController {

    @Autowired
    private JantarService jantarService;
    
    @Autowired
    private ExtratoReservaRepository extratoReservaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    // ✅ LISTAR APARTAMENTOS COM HÓSPEDES AUTORIZADOS
    @GetMapping("/apartamentos-autorizados")
    public ResponseEntity<?> getApartamentosAutorizados() {
        try {
            List<Map<String, Object>> resultado = jantarService.getApartamentosAutorizados();
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ✅ BUSCAR HÓSPEDE
    @PostMapping("/buscar-hospede")
    public ResponseEntity<?> buscarHospede(@RequestBody Map<String, Object> body) {
        try {
            String nome = body.containsKey("nome") ? body.get("nome").toString() : "";
            String numeroApartamento = body.containsKey("numeroApartamento") ?
                body.get("numeroApartamento").toString() : "";

            Map<String, Object> resultado = jantarService.buscarHospedeFormatado(nome, numeroApartamento);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ✅ PRODUTOS POR CATEGORIA (RESTAURANTE)
    @GetMapping("/produtos/categoria/{categoriaId}")
    public ResponseEntity<?> getProdutosPorCategoria(@PathVariable Long categoriaId) {
        try {
            List<Produto> produtos = produtoRepository.findByCategoriaId(categoriaId);
            return ResponseEntity.ok(produtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ✅ SALVAR COMANDA
    @SuppressWarnings("unchecked")
    @PostMapping("/salvar-comanda")
    public ResponseEntity<?> salvarComanda(@RequestBody Map<String, Object> body) {
        try {
            // ✅ Aceita tanto hospedagemHospedeId quanto hospedagemId
            Object idObj = body.get("hospedagemHospedeId") != null 
                ? body.get("hospedagemHospedeId") 
                : body.get("hospedagemId");
            
            if (idObj == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "hospedagemHospedeId é obrigatório"));
            }
            
            Long hospedagemHospedeId = Long.parseLong(idObj.toString());
            List<Map<String, Object>> itens = (List<Map<String, Object>>) body.get("itens");

            Map<String, Object> resultado = jantarService.salvarComanda(hospedagemHospedeId, itens);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    // ✅ CANCELAR COMANDA
    @PostMapping("/cancelar-comanda/{notaId}")
    public ResponseEntity<?> cancelarComanda(@PathVariable Long notaId) {
        try {
            Map<String, Object> resultado = jantarService.cancelarComanda(notaId);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
 // ✅ PRODUTOS DO RESTAURANTE (com estoque > 0)
    @GetMapping("/produtos-restaurante")
    public ResponseEntity<?> getProdutosRestaurante() {
        try {
            List<Map<String, Object>> resultado = jantarService.getProdutosRestaurante();
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ✅ RELATÓRIO DE COMANDAS
    @PostMapping("/relatorio-comandas")
    public ResponseEntity<?> relatorioComandas(@RequestBody Map<String, Object> body) {
        try {
            String dataInicio = body.get("dataInicio").toString();
            String dataFim = body.get("dataFim").toString();
            Map<String, Object> resultado = jantarService.gerarRelatorioComandas(dataInicio, dataFim);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ✅ RELATÓRIO DE FATURAMENTO
    @PostMapping("/relatorio-faturamento")
    public ResponseEntity<?> relatorioFaturamento(@RequestBody Map<String, Object> body) {
        try {
            String dataInicio = body.get("dataInicio").toString();
            String dataFim = body.get("dataFim").toString();
            Map<String, Object> resultado = jantarService.gerarRelatorioFaturamento(dataInicio, dataFim);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ✅ RELATÓRIO PRODUTOS POR APARTAMENTO
    @PostMapping("/relatorio-produtos-apartamento")
    public ResponseEntity<?> relatorioProdutosApartamento(@RequestBody Map<String, Object> body) {
        try {
            String dataInicio = body.get("dataInicio").toString();
            String dataFim = body.get("dataFim").toString();
            List<Map<String, Object>> resultado = jantarService.relatorioProdutosPorApartamento(dataInicio, dataFim);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ✅ RELATÓRIO QUANTIDADE POR PRODUTO
    @PostMapping("/relatorio-quantidade-produto")
    public ResponseEntity<?> relatorioQuantidadeProduto(@RequestBody Map<String, Object> body) {
        try {
            Long produtoId = Long.parseLong(body.get("produtoId").toString());
            String dataInicio = body.get("dataInicio").toString();
            String dataFim = body.get("dataFim").toString();
            Map<String, Object> resultado = jantarService.relatorioQuantidadeProduto(produtoId, dataInicio, dataFim);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/consumo-hoje/{reservaId}")
    public ResponseEntity<?> getConsumoDhoje(@PathVariable Long reservaId) {
        try {
            LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
            LocalDateTime fimDia = LocalDate.now().atTime(23, 59, 59);

            List<ExtratoReserva> extratos = extratoReservaRepository
                .findByReservaIdAndStatusLancamentoAndDataHoraLancamentoBetween(
                    reservaId,
                    ExtratoReserva.StatusLancamentoEnum.PRODUTO,
                    inicioDia,
                    fimDia
                );

            List<Map<String, Object>> resultado = extratos.stream()
                .map(e -> Map.of(
                    "descricao",  (Object) e.getDescricao(),
                    "quantidade", (Object) e.getQuantidade(),
                    "valorUnitario", (Object) e.getValorUnitario(),
                    "total",      (Object) e.getTotalLancamento(),
                    "hora",       (Object) e.getDataHoraLancamento()
                ))
                .collect(Collectors.toList());

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}