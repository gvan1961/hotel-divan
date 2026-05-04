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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/estornos")
public class EstornoController {

    @Autowired
    private ExtratoReservaRepository extratoReservaRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private NotaVendaRepository notaVendaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @PostMapping("/consumo-apartamento")
    public ResponseEntity<?> estornarConsumoApartamento(@RequestBody Map<String, Object> body) {
        try {
            Long extratoId = Long.valueOf(body.get("extratoId").toString());
            String motivo = body.get("motivo").toString();

            // Busca o lançamento original
            ExtratoReserva extratoOriginal = extratoReservaRepository.findById(extratoId)
                .orElseThrow(() -> new RuntimeException("Lançamento não encontrado: " + extratoId));

            // Valida que é um lançamento de PRODUTO
            if (extratoOriginal.getStatusLancamento() != ExtratoReserva.StatusLancamentoEnum.PRODUTO) {
                return ResponseEntity.badRequest().body(Map.of(
                    "erro", "Apenas lançamentos de PRODUTO podem ser estornados. Tipo atual: "
                        + extratoOriginal.getStatusLancamento()
                ));
            }

            Reserva reserva = extratoOriginal.getReserva();

            // ✅ Cria lançamento de ESTORNO com valor NEGATIVO
            ExtratoReserva extratoEstorno = new ExtratoReserva();
            extratoEstorno.setReserva(reserva);
            extratoEstorno.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.ESTORNO);
            extratoEstorno.setDescricao("ESTORNO: " + extratoOriginal.getDescricao()
                + " [extratoId:" + extratoId + "] — Motivo: " + motivo);
            extratoEstorno.setQuantidade(extratoOriginal.getQuantidade());
            extratoEstorno.setValorUnitario(extratoOriginal.getValorUnitario());
            extratoEstorno.setTotalLancamento(extratoOriginal.getTotalLancamento().negate());
            extratoEstorno.setDataHoraLancamento(LocalDateTime.now());
            extratoEstorno.setNotaVendaId(null);

            extratoReservaRepository.save(extratoEstorno);

            // ✅ Atualiza totais da reserva
            BigDecimal valorEstorno = extratoOriginal.getTotalLancamento();
            reserva.setTotalHospedagem(reserva.getTotalHospedagem().subtract(valorEstorno));
            reserva.setTotalApagar(reserva.getTotalApagar().subtract(valorEstorno));
            reservaRepository.save(reserva);

            // ✅ Devolve estoque via NotaVenda
            if (extratoOriginal.getNotaVendaId() != null) {
                notaVendaRepository.findById(extratoOriginal.getNotaVendaId()).ifPresent(notaVenda -> {
                    if (notaVenda.getItens() != null) {
                        for (ItemVenda item : notaVenda.getItens()) {
                            Produto produto = item.getProduto();
                            if (produto != null) {
                                int quantidadeDevolver = item.getQuantidade();
                                produto.setQuantidade(produto.getQuantidade() + quantidadeDevolver);
                                produtoRepository.save(produto);
                                System.out.println("✅ Estoque devolvido — Produto: " + produto.getNomeProduto()
                                    + " — Qtd: +" + quantidadeDevolver
                                    + " — Novo estoque: " + produto.getQuantidade());
                            }
                        }
                    }
                });
            } else {
                System.out.println("⚠️ notaVendaId nulo — estoque não foi devolvido para extratoId: " + extratoId);
            }

            System.out.println("✅ Estorno realizado — Extrato #" + extratoId
                + " — Valor: R$ " + valorEstorno
                + " — Motivo: " + motivo);

            return ResponseEntity.ok(Map.of(
                "mensagem", "Estorno realizado com sucesso",
                "extratoEstornoId", extratoEstorno.getId(),
                "valorEstornado", valorEstorno
            ));

        } catch (Exception e) {
            System.err.println("❌ Erro no estorno: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
