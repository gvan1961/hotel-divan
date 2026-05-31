package com.divan.controller;

import com.divan.entity.ExtratoReserva;
import com.divan.entity.ItemVenda;
import com.divan.entity.NotaVenda;
import com.divan.entity.Produto;
import com.divan.entity.Reserva;
import com.divan.repository.ExtratoReservaRepository;
import com.divan.repository.NotaVendaRepository;
import com.divan.repository.ProdutoRepository;
import com.divan.repository.ReservaRepository;
import com.divan.service.ReservaService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/estornos")
public class EstornoController {

    @Autowired private ExtratoReservaRepository extratoReservaRepository;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private NotaVendaRepository notaVendaRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private ReservaService reservaService;

    @Transactional
    @PostMapping("/consumo-apartamento")
    public ResponseEntity<?> estornarConsumoApartamento(@RequestBody Map<String, Object> body) {
        try {
            Long extratoId = Long.valueOf(body.get("extratoId").toString());
            String motivo = body.get("motivo").toString();

            ExtratoReserva extratoOriginal = extratoReservaRepository.findById(extratoId)
                .orElseThrow(() -> new RuntimeException("Lançamento não encontrado: " + extratoId));

            if (extratoOriginal.getStatusLancamento() != ExtratoReserva.StatusLancamentoEnum.PRODUTO) {
                return ResponseEntity.badRequest().body(Map.of(
                    "erro", "Apenas lançamentos de PRODUTO podem ser estornados. Tipo atual: "
                        + extratoOriginal.getStatusLancamento()
                ));
            }

            Reserva reserva = extratoOriginal.getReserva();
            BigDecimal valorEstorno = extratoOriginal.getTotalLancamento();

            // ✅ Cria lançamento de ESTORNO com valor NEGATIVO
            ExtratoReserva extratoEstorno = new ExtratoReserva();
            extratoEstorno.setReserva(reserva);
            extratoEstorno.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.ESTORNO);
            extratoEstorno.setDescricao("ESTORNO: " + extratoOriginal.getDescricao()
                + " [extratoId:" + extratoId + "] – Motivo: " + motivo);
            extratoEstorno.setQuantidade(extratoOriginal.getQuantidade());
            extratoEstorno.setValorUnitario(extratoOriginal.getValorUnitario());
            extratoEstorno.setTotalLancamento(valorEstorno.negate());
            extratoEstorno.setDataHoraLancamento(LocalDateTime.now());
            extratoEstorno.setNotaVendaId(null);
            extratoReservaRepository.save(extratoEstorno);

            // ✅ Recalcula todos os totais corretamente a partir do extrato
            reservaService.recalcularTotaisReserva(reserva);
            reservaRepository.save(reserva);

            // ✅ Devolve estoque via NotaVenda (@Transactional garante lazy loading)
            if (extratoOriginal.getNotaVendaId() != null) {
                notaVendaRepository.findById(extratoOriginal.getNotaVendaId()).ifPresent(notaVenda -> {
                    if (notaVenda.getItens() != null) {
                        for (ItemVenda item : notaVenda.getItens()) {
                            Produto produto = item.getProduto();
                            if (produto != null) {
                                int qtd = item.getQuantidade();
                                produto.setQuantidade(produto.getQuantidade() + qtd);
                                produtoRepository.save(produto);
                                System.out.println("✅ Estoque devolvido – "
                                    + produto.getNomeProduto() + " +" + qtd
                                    + " → novo total: " + produto.getQuantidade());
                            }
                        }
                    }
                });
            } else {
                System.out.println("⚠️ notaVendaId nulo – estoque não devolvido para extratoId: " + extratoId);
            }

            System.out.println("✅ Estorno OK – Extrato #" + extratoId
                + " – R$ " + valorEstorno + " – " + motivo);

            return ResponseEntity.ok(Map.of(
                "mensagem", "Estorno realizado com sucesso",
                "extratoEstornoId", extratoEstorno.getId(),
                "valorEstornado", valorEstorno
            ));

        } catch (Exception e) {
            System.err.println("❌ Erro no estorno: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
