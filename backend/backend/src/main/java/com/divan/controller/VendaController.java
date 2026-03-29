package com.divan.controller;

import com.divan.entity.*;
import com.divan.repository.*;
import com.divan.service.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendas")
@CrossOrigin(origins = "*")
public class VendaController {

    @Autowired private ReservaRepository reservaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private NotaVendaRepository notaVendaRepository;
    @Autowired private ItemVendaRepository itemVendaRepository;
    @Autowired private ExtratoReservaRepository extratoReservaRepository;
    @Autowired private VendaService vendaService;

    @GetMapping("/teste")
    public ResponseEntity<String> teste() {
        return ResponseEntity.ok("VendaController funcionando!");
    }

    // ─── COMANDA APARTAMENTO ───────────────────────────────
    @SuppressWarnings("unchecked")
    @PostMapping("/comanda-consumo")
    public ResponseEntity<?> comandaConsumo(@RequestBody Map<String, Object> body) {
        try {
            Long reservaId = Long.parseLong(body.get("reservaId").toString());
            String observacao = body.containsKey("observacao") && body.get("observacao") != null
                ? body.get("observacao").toString() : "";
            List<Map<String, Object>> itens = (List<Map<String, Object>>) body.get("itens");

            Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

            if (reserva.getStatus() != Reserva.StatusReservaEnum.ATIVA)
                throw new RuntimeException("Reserva não está ativa");

            BigDecimal totalNota = BigDecimal.ZERO;

            NotaVenda nota = new NotaVenda();
            nota.setReserva(reserva);
            nota.setDataHoraVenda(LocalDateTime.now());
            nota.setTipoVenda(NotaVenda.TipoVendaEnum.APARTAMENTO);
            nota.setStatus(NotaVenda.Status.FECHADA);
            nota.setObservacao("PDV - " + (observacao.isBlank() ? "Consumo apartamento" : observacao));
            nota.setTotal(BigDecimal.ZERO);
            nota.setItens(new ArrayList<>());
            notaVendaRepository.save(nota);

            for (Map<String, Object> itemMap : itens) {
                Long produtoId = Long.parseLong(itemMap.get("produtoId").toString());
                int quantidade = Integer.parseInt(itemMap.get("quantidade").toString());
                BigDecimal valorUnitario = new BigDecimal(itemMap.get("valorUnitario").toString());

                Produto produto = produtoRepository.findById(produtoId)
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + produtoId));

                if (produto.getQuantidade() < quantidade)
                    throw new RuntimeException("Estoque insuficiente para: " + produto.getNomeProduto());

                BigDecimal totalItem = valorUnitario.multiply(BigDecimal.valueOf(quantidade));

                ItemVenda itemVenda = new ItemVenda();
                itemVenda.setNotaVenda(nota);
                itemVenda.setProduto(produto);
                itemVenda.setQuantidade(quantidade);
                itemVenda.setValorUnitario(valorUnitario);
                itemVenda.setTotalItem(totalItem);
                itemVendaRepository.save(itemVenda);

                produto.setQuantidade(produto.getQuantidade() - quantidade);
                produtoRepository.save(produto);

                totalNota = totalNota.add(totalItem);

                ExtratoReserva extrato = new ExtratoReserva();
                extrato.setReserva(reserva);
                extrato.setDataHoraLancamento(LocalDateTime.now());
                extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.PRODUTO);
                extrato.setDescricao("Consumo: " + produto.getNomeProduto());
                extrato.setQuantidade(quantidade);
                extrato.setValorUnitario(valorUnitario);
                extrato.setTotalLancamento(totalItem);
                extrato.setNotaVendaId(nota.getId());
                extratoReservaRepository.save(extrato);
            }

            nota.setTotal(totalNota);
            notaVendaRepository.save(nota);

            reserva.setTotalProduto(reserva.getTotalProduto().add(totalNota));
            reserva.setTotalHospedagem(reserva.getTotalHospedagem().add(totalNota));
            reserva.setTotalApagar(reserva.getTotalApagar().add(totalNota));
            reservaRepository.save(reserva);

            return ResponseEntity.ok(Map.of(
                "notaVendaId", nota.getId(),
                "total", totalNota,
                "mensagem", "Comanda registrada com sucesso"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ─── VENDA À VISTA (PDV BALCÃO) ────────────────────────
    @SuppressWarnings("unchecked")
    @PostMapping("/a-vista")
    public ResponseEntity<?> vendaAVista(@RequestBody Map<String, Object> body) {
        try {
            String formaPagamentoStr = body.get("formaPagamento").toString();
            String observacao = body.containsKey("observacao") && body.get("observacao") != null
                ? body.get("observacao").toString() : "";
            List<Map<String, Object>> itens = (List<Map<String, Object>>) body.get("itens");

            // ✅ Converter string para enum
            NotaVenda.FormaPagamentoEnum formaPagamento =
                NotaVenda.FormaPagamentoEnum.valueOf(formaPagamentoStr);

            BigDecimal totalNota = BigDecimal.ZERO;

            NotaVenda nota = new NotaVenda();
            nota.setDataHoraVenda(LocalDateTime.now());
            nota.setTipoVenda(NotaVenda.TipoVendaEnum.VISTA);
            nota.setFormaPagamento(formaPagamento); // ✅ SALVAR FORMA DE PAGAMENTO
            nota.setStatus(NotaVenda.Status.FECHADA);
            nota.setObservacao("PDV À Vista - " + formaPagamentoStr +
                (observacao.isBlank() ? "" : " - " + observacao));
            nota.setTotal(BigDecimal.ZERO);
            nota.setItens(new ArrayList<>());
            notaVendaRepository.save(nota);

            for (Map<String, Object> itemMap : itens) {
                Long produtoId = Long.parseLong(itemMap.get("produtoId").toString());
                int quantidade = Integer.parseInt(itemMap.get("quantidade").toString());
                BigDecimal valorUnitario = new BigDecimal(itemMap.get("valorUnitario").toString());

                Produto produto = produtoRepository.findById(produtoId)
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + produtoId));

                if (produto.getQuantidade() < quantidade)
                    throw new RuntimeException("Estoque insuficiente para: " + produto.getNomeProduto());

                BigDecimal totalItem = valorUnitario.multiply(BigDecimal.valueOf(quantidade));

                ItemVenda itemVenda = new ItemVenda();
                itemVenda.setNotaVenda(nota);
                itemVenda.setProduto(produto);
                itemVenda.setQuantidade(quantidade);
                itemVenda.setValorUnitario(valorUnitario);
                itemVenda.setTotalItem(totalItem);
                itemVendaRepository.save(itemVenda);

                produto.setQuantidade(produto.getQuantidade() - quantidade);
                produtoRepository.save(produto);

                totalNota = totalNota.add(totalItem);
            }

            nota.setTotal(totalNota);
            notaVendaRepository.save(nota);

            return ResponseEntity.ok(Map.of(
                "notaVendaId", nota.getId(),
                "total", totalNota,
                "mensagem", "Venda à vista realizada com sucesso"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ─── VENDA FATURADA (PDV CRÉDITO) ──────────────────────
    @SuppressWarnings("unchecked")
    @PostMapping("/faturada")
    public ResponseEntity<?> vendaFaturada(@RequestBody Map<String, Object> body,
                                            @RequestParam(required = false) Long usuarioId) {
        try {
            Long clienteId = Long.parseLong(body.get("clienteId").toString());
            String observacao = body.containsKey("observacao") && body.get("observacao") != null
                ? body.get("observacao").toString() : "";
            List<Map<String, Object>> itens = (List<Map<String, Object>>) body.get("itens");

            Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            BigDecimal totalNota = BigDecimal.ZERO;

            NotaVenda nota = new NotaVenda();
            nota.setDataHoraVenda(LocalDateTime.now());
            nota.setTipoVenda(NotaVenda.TipoVendaEnum.FATURADO); // ✅ CORRIGIDO (era VISTA)
            nota.setFormaPagamento(NotaVenda.FormaPagamentoEnum.FATURADO); // ✅ SALVAR FORMA
            nota.setStatus(NotaVenda.Status.FECHADA);
            nota.setObservacao("PDV Faturado - " + cliente.getNome() +
                (observacao.isBlank() ? "" : " - " + observacao));
            nota.setTotal(BigDecimal.ZERO);
            nota.setItens(new ArrayList<>());
            notaVendaRepository.save(nota);

            for (Map<String, Object> itemMap : itens) {
                Long produtoId = Long.parseLong(itemMap.get("produtoId").toString());
                int quantidade = Integer.parseInt(itemMap.get("quantidade").toString());
                BigDecimal valorUnitario = new BigDecimal(itemMap.get("valorUnitario").toString());

                Produto produto = produtoRepository.findById(produtoId)
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + produtoId));

                if (produto.getQuantidade() < quantidade)
                    throw new RuntimeException("Estoque insuficiente para: " + produto.getNomeProduto());

                BigDecimal totalItem = valorUnitario.multiply(BigDecimal.valueOf(quantidade));

                ItemVenda itemVenda = new ItemVenda();
                itemVenda.setNotaVenda(nota);
                itemVenda.setProduto(produto);
                itemVenda.setQuantidade(quantidade);
                itemVenda.setValorUnitario(valorUnitario);
                itemVenda.setTotalItem(totalItem);
                itemVendaRepository.save(itemVenda);

                produto.setQuantidade(produto.getQuantidade() - quantidade);
                produtoRepository.save(produto);

                totalNota = totalNota.add(totalItem);
            }

            nota.setTotal(totalNota);
            notaVendaRepository.save(nota);

            return ResponseEntity.ok(Map.of(
                "notaVendaId", nota.getId(),
                "total", totalNota,
                "clienteNome", cliente.getNome(),
                "mensagem", "Venda faturada registrada com sucesso"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
