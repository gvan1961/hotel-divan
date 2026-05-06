package com.divan.controller;

import com.divan.entity.FechamentoCaixa;
import com.divan.entity.FechamentoCaixa.StatusCaixa;
import com.divan.entity.NotaVenda;
import com.divan.entity.Pagamento;
import com.divan.entity.Usuario;
import com.divan.repository.FechamentoCaixaRepository;
import com.divan.repository.NotaVendaRepository;
import com.divan.repository.PagamentoRepository;
import com.divan.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class FechamentoCaixaController {

    @Autowired private FechamentoCaixaRepository caixaRepository;
    @Autowired private PagamentoRepository pagamentoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private NotaVendaRepository notaVendaRepository;

    // ─── ABRIR CAIXA ──────────────────────────────────────
    @PostMapping("/api/fechamento-caixa/abrir")
    public ResponseEntity<?> abrirCaixa(@RequestBody Map<String, Object> body) {
        try {
            Long usuarioId = Long.parseLong(body.get("usuarioId").toString());

            caixaRepository.findByUsuarioIdAndStatus(usuarioId, StatusCaixa.ABERTO)
                .ifPresent(c -> { throw new RuntimeException("Já existe um caixa aberto para este usuário"); });

            Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            FechamentoCaixa caixa = new FechamentoCaixa();
            caixa.setUsuario(usuario);
            caixa.setDataHoraAbertura(LocalDateTime.now());
            caixa.setStatus(StatusCaixa.ABERTO);

            if (body.get("turno") != null)
                caixa.setTurno(body.get("turno").toString());
            if (body.get("observacoes") != null)
                caixa.setObservacoes(body.get("observacoes").toString());

            return ResponseEntity.ok(caixaRepository.save(caixa));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ─── BUSCAR CAIXA ABERTO (query param) ─────────────────
    @GetMapping("/api/fechamento-caixa/aberto")
    public ResponseEntity<?> buscarCaixaAberto(@RequestParam Long usuarioId) {
        return caixaRepository.findByUsuarioIdAndStatus(usuarioId, StatusCaixa.ABERTO)
            .map(c -> ResponseEntity.ok(montarDTO(c)))
            .orElse(ResponseEntity.ok(Map.of("aberto", false)));
    }

    // ─── BUSCAR CAIXA ABERTO (path - CaixaService) ─────────
    @GetMapping("/api/caixa/usuario/{usuarioId}/aberto")
    public ResponseEntity<?> buscarCaixaAbertoPorPath(@PathVariable Long usuarioId) {
        return caixaRepository.findByUsuarioIdAndStatus(usuarioId, StatusCaixa.ABERTO)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElse(ResponseEntity.ok(Collections.singletonMap("id", null)));
    }

    // ─── FECHAR CAIXA ──────────────────────────────────────
    @PostMapping("/api/fechamento-caixa/{id}/fechar")
    public ResponseEntity<?> fecharCaixa(@PathVariable Long id,
                                          @RequestBody Map<String, Object> body) {
        return caixaRepository.findById(id).map(caixa -> {
            caixa.setStatus(StatusCaixa.FECHADO);
            caixa.setDataHoraFechamento(LocalDateTime.now());
            if (body.get("observacoes") != null)
                caixa.setObservacoes(body.get("observacoes").toString());
            return ResponseEntity.ok(caixaRepository.save(caixa));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── BUSCAR POR ID ─────────────────────────────────────
    @GetMapping("/api/fechamento-caixa/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return caixaRepository.findById(id)
            .map(c -> ResponseEntity.ok(montarDTO(c)))
            .orElse(ResponseEntity.notFound().build());
    }

    // ─── LISTAR POR PERÍODO ────────────────────────────────
    @GetMapping("/api/fechamento-caixa/periodo")
    public ResponseEntity<?> listarPorPeriodo(@RequestParam String dataInicio,
                                               @RequestParam String dataFim) {
        try {
            LocalDateTime dtInicio = LocalDateTime.parse(dataInicio);
            LocalDateTime dtFim = LocalDateTime.parse(dataFim);
            List<FechamentoCaixa> caixas = caixaRepository
                .findByDataHoraAberturaBetween(dtInicio, dtFim);
            return ResponseEntity.ok(caixas.stream()
                .map(this::montarDTO)
                .collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ─── RELATÓRIO DETALHADO ───────────────────────────────
    @GetMapping("/api/fechamento-caixa/{id}/relatorio-detalhado")
    public ResponseEntity<?> relatorioDetalhado(@PathVariable Long id) {
        return caixaRepository.findById(id).map(caixa -> {

            LocalDateTime fim = caixa.getDataHoraFechamento() != null
                ? caixa.getDataHoraFechamento() : LocalDateTime.now();

            // === PAGAMENTOS DE RESERVAS ===
            List<Pagamento> pagamentos = pagamentoRepository
                .findByDataHoraPagamentoBetween(caixa.getDataHoraAbertura(), fim);

            // Agrupar por reserva
            Map<Long, Map<String, Object>> porReserva = new LinkedHashMap<>();
            for (Pagamento p : pagamentos) {
                if (p.getReserva() == null) continue;
                Long reservaId = p.getReserva().getId();
                Map<String, Object> rv = porReserva.computeIfAbsent(reservaId, k -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("reservaId", reservaId);
                    m.put("numeroApartamento",
                        p.getReserva().getApartamento() != null
                            ? p.getReserva().getApartamento().getNumeroApartamento() : "-");
                    m.put("clienteNome",
                        p.getReserva().getCliente() != null
                            ? p.getReserva().getCliente().getNome() : "-");
                    Map<String, BigDecimal> pags = new LinkedHashMap<>();
                    pags.put("dinheiro",      BigDecimal.ZERO);
                    pags.put("pix",           BigDecimal.ZERO);
                    pags.put("cartaoDebito",  BigDecimal.ZERO);
                    pags.put("cartaoCredito", BigDecimal.ZERO);
                    pags.put("transferencia", BigDecimal.ZERO);
                    pags.put("faturado",      BigDecimal.ZERO);
                    pags.put("linkPix",       BigDecimal.ZERO);  
                    pags.put("linkCartao",    BigDecimal.ZERO); 
                    m.put("pagamentos", pags);
                    m.put("total", BigDecimal.ZERO);
                    return m;
                });

                @SuppressWarnings("unchecked")
                Map<String, BigDecimal> pags = (Map<String, BigDecimal>) rv.get("pagamentos");
                String key = switch (p.getFormaPagamento()) {
                    case DINHEIRO             -> "dinheiro";
                    case PIX                  -> "pix";
                    case CARTAO_DEBITO        -> "cartaoDebito";
                    case CARTAO_CREDITO       -> "cartaoCredito";
                    case TRANSFERENCIA_BANCARIA -> "transferencia";
                    case FATURADO             -> "faturado";
                    case LINK_PIX    -> "linkPix";
                    case LINK_CARTAO -> "linkCartao";

                    default                   -> null;
                };
                if (key != null) pags.merge(key, p.getValor(), BigDecimal::add);
                rv.put("total", ((BigDecimal) rv.get("total")).add(p.getValor()));
            }

            List<Map<String, Object>> vendasReservas = new ArrayList<>(porReserva.values());

            // Subtotal reservas
            BigDecimal srDinheiro = BigDecimal.ZERO, srPix = BigDecimal.ZERO,
                       srDebito   = BigDecimal.ZERO, srCredito = BigDecimal.ZERO,
                       srTransf   = BigDecimal.ZERO, srFaturado = BigDecimal.ZERO;
            BigDecimal srLinkPix    = BigDecimal.ZERO;
            BigDecimal srLinkCartao = BigDecimal.ZERO;
            for (Pagamento p : pagamentos) {
                if (p.getFormaPagamento() == null) continue;
                switch (p.getFormaPagamento()) {
                    case DINHEIRO             -> srDinheiro  = srDinheiro.add(p.getValor());
                    case PIX                  -> srPix       = srPix.add(p.getValor());
                    case CARTAO_DEBITO        -> srDebito    = srDebito.add(p.getValor());
                    case CARTAO_CREDITO       -> srCredito   = srCredito.add(p.getValor());
                    case TRANSFERENCIA_BANCARIA -> srTransf  = srTransf.add(p.getValor());
                    case FATURADO             -> srFaturado  = srFaturado.add(p.getValor());
                    case LINK_PIX    -> srLinkPix    = srLinkPix.add(p.getValor());
                    case LINK_CARTAO -> srLinkCartao = srLinkCartao.add(p.getValor());
                    default -> {}
                }
            }
            BigDecimal srTotal = srDinheiro.add(srPix).add(srDebito)
            	    .add(srCredito).add(srTransf).add(srFaturado)
            	    .add(srLinkPix).add(srLinkCartao);
            
            Map<String, Object> subtotalReservas = new LinkedHashMap<>();
            subtotalReservas.put("dinheiro",      srDinheiro);
            subtotalReservas.put("pix",           srPix);
            subtotalReservas.put("cartaoDebito",  srDebito);
            subtotalReservas.put("cartaoCredito", srCredito);
            subtotalReservas.put("transferencia", srTransf);
            subtotalReservas.put("faturado",      srFaturado);
            subtotalReservas.put("linkPix",    srLinkPix);
            subtotalReservas.put("linkCartao", srLinkCartao);
            subtotalReservas.put("total",         srTotal);

            // === VENDAS AVULSAS PDV ===
            List<NotaVenda> vendasAvulsas = notaVendaRepository.findByTipoVendaInAndPeriodo(
                List.of(NotaVenda.TipoVendaEnum.VISTA, NotaVenda.TipoVendaEnum.FATURADO),
                caixa.getDataHoraAbertura(), fim
            );

            BigDecimal avDinheiro = BigDecimal.ZERO, avPix = BigDecimal.ZERO,
                       avDebito   = BigDecimal.ZERO, avCredito = BigDecimal.ZERO,
                       avTransf   = BigDecimal.ZERO, avFaturado = BigDecimal.ZERO;
            BigDecimal avLinkPix    = BigDecimal.ZERO;
            BigDecimal avLinkCartao = BigDecimal.ZERO;

            // Vendas faturadas com produtos (para listar no relatório)
            List<Map<String, Object>> vendasAvulsasFaturadas = new ArrayList<>();

            for (NotaVenda nv : vendasAvulsas) {
                if (nv.getTotal() == null || nv.getFormaPagamento() == null) continue;
                switch (nv.getFormaPagamento()) {
                    case DINHEIRO             -> avDinheiro = avDinheiro.add(nv.getTotal());
                    case PIX                  -> avPix      = avPix.add(nv.getTotal());
                    case CARTAO_DEBITO        -> avDebito   = avDebito.add(nv.getTotal());
                    case CARTAO_CREDITO       -> avCredito  = avCredito.add(nv.getTotal());
                    case TRANSFERENCIA_BANCARIA -> avTransf = avTransf.add(nv.getTotal());
                    case FATURADO             -> avFaturado = avFaturado.add(nv.getTotal());
                    case LINK_PIX    -> avLinkPix    = avLinkPix.add(nv.getTotal());
                    case LINK_CARTAO -> avLinkCartao = avLinkCartao.add(nv.getTotal());
                    default -> {}
                }

                if (nv.getFormaPagamento() == NotaVenda.FormaPagamentoEnum.FATURADO) {
                    List<Map<String, Object>> produtos = new ArrayList<>();
                    if (nv.getItens() != null) {
                        for (var item : nv.getItens()) {
                            Map<String, Object> pr = new LinkedHashMap<>();
                            pr.put("nomeProduto", item.getProduto() != null ? item.getProduto().getNomeProduto() : "-");
                            pr.put("quantidade",  item.getQuantidade());
                            pr.put("totalItem",   item.getTotalItem());
                            produtos.add(pr);
                        }
                    }
                    // Tentar extrair nome do cliente da observação
                    String clienteNome = "-";
                    if (nv.getObservacao() != null && nv.getObservacao().startsWith("PDV Faturado - ")) {
                        clienteNome = nv.getObservacao().replace("PDV Faturado - ", "").split(" - ")[0];
                    }
                    Map<String, Object> vf = new LinkedHashMap<>();
                    vf.put("notaId",      nv.getId());
                    vf.put("clienteNome", clienteNome);
                    vf.put("valor",       nv.getTotal());
                    vf.put("produtos",    produtos);
                    vendasAvulsasFaturadas.add(vf);
                }
            }

            BigDecimal avTotal = avDinheiro.add(avPix).add(avDebito)
                .add(avCredito).add(avTransf).add(avFaturado);

            Map<String, Object> vendasAvulsasMap = new LinkedHashMap<>();
            vendasAvulsasMap.put("dinheiro",      avDinheiro);
            vendasAvulsasMap.put("pix",           avPix);
            vendasAvulsasMap.put("cartaoDebito",  avDebito);
            vendasAvulsasMap.put("cartaoCredito", avCredito);
            vendasAvulsasMap.put("transferencia", avTransf);
            vendasAvulsasMap.put("faturado",      avFaturado);
            vendasAvulsasMap.put("total",         avTotal);

            // === TOTAL GERAL ===
            Map<String, Object> totalGeral = new LinkedHashMap<>();
            totalGeral.put("dinheiro",      srDinheiro.add(avDinheiro));
            totalGeral.put("pix",           srPix.add(avPix));
            totalGeral.put("cartaoDebito",  srDebito.add(avDebito));
            totalGeral.put("cartaoCredito", srCredito.add(avCredito));
            totalGeral.put("transferencia", srTransf.add(avTransf));
            totalGeral.put("faturado",      srFaturado.add(avFaturado));
            totalGeral.put("linkPix",    srLinkPix.add(avLinkPix));
            totalGeral.put("linkCartao", srLinkCartao.add(avLinkCartao));
            totalGeral.put("total",         srTotal.add(avTotal));

            // === MONTAR RESPOSTA ===
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("caixaId",                caixa.getId());
            resp.put("recepcionistaNome",      caixa.getUsuario().getNome());
            resp.put("turno",                  caixa.getTurno());
            resp.put("dataHoraAbertura",       caixa.getDataHoraAbertura());
            resp.put("dataHoraFechamento",     caixa.getDataHoraFechamento());
            resp.put("status",                 caixa.getStatus().name());
            resp.put("vendasReservas",         vendasReservas);
            resp.put("subtotalReservas",       subtotalReservas);
            resp.put("vendasAvulsasFaturadas", vendasAvulsasFaturadas);
            resp.put("vendasAvulsas",          vendasAvulsasMap);
            resp.put("totalGeral",             totalGeral);

            return ResponseEntity.ok(resp);

        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── RESUMO COMPLETO / VENDAS DETALHADAS / RELATÓRIO ───
    @GetMapping("/api/fechamento-caixa/{id}/resumo-completo")
    public ResponseEntity<?> resumoCompleto(@PathVariable Long id) {
        return relatorioDetalhado(id);
    }

    @GetMapping("/api/fechamento-caixa/{id}/vendas-detalhadas")
    public ResponseEntity<?> vendasDetalhadas(@PathVariable Long id) {
        return caixaRepository.findById(id).map(caixa -> {

            LocalDateTime fim = caixa.getDataHoraFechamento() != null
                ? caixa.getDataHoraFechamento() : LocalDateTime.now();

            // Buscar todas as notas de venda do período (VISTA, FATURADO, APARTAMENTO)
            List<NotaVenda> todasVendas = notaVendaRepository.findByTipoVendaInAndPeriodo(
                List.of(NotaVenda.TipoVendaEnum.VISTA,
                        NotaVenda.TipoVendaEnum.FATURADO,
                        NotaVenda.TipoVendaEnum.APARTAMENTO),
                caixa.getDataHoraAbertura(), fim
            );

            // Agrupar por forma de pagamento
            Map<String, List<Map<String, Object>>> vendasPorForma = new LinkedHashMap<>();
            Map<String, BigDecimal> totaisPorForma = new LinkedHashMap<>();
            Map<String, Integer> qtdVendasPorForma = new LinkedHashMap<>();
            Map<String, Integer> qtdProdutosPorForma = new LinkedHashMap<>();

            int totalVendas = 0;
            int totalProdutos = 0;
            BigDecimal totalGeral = BigDecimal.ZERO;

            for (NotaVenda nv : todasVendas) {
                if (nv.getItens() == null || nv.getItens().isEmpty()) continue;
                if (nv.getTotal() == null || nv.getTotal().compareTo(BigDecimal.ZERO) == 0) continue;

                String formaKey;
                if (nv.getTipoVenda() == NotaVenda.TipoVendaEnum.APARTAMENTO) {
                    formaKey = "APARTAMENTO";
                } else if (nv.getFormaPagamento() != null) {
                    formaKey = nv.getFormaPagamento().name();
                } else {
                    formaKey = "DINHEIRO";
                }

                // Montar produtos da venda
                List<Map<String, Object>> produtos = new ArrayList<>();
                int qtdProdutosVenda = 0;
                for (var item : nv.getItens()) {
                    Map<String, Object> prod = new LinkedHashMap<>();
                    prod.put("nomeProduto", item.getProduto() != null ? item.getProduto().getNomeProduto() : "-");
                    prod.put("quantidade", item.getQuantidade());
                    prod.put("valorUnitario", item.getValorUnitario());
                    prod.put("totalItem", item.getTotalItem());
                    produtos.add(prod);
                    qtdProdutosVenda += item.getQuantidade();
                }

             // Montar venda
                Map<String, Object> vendaMap = new LinkedHashMap<>();
                vendaMap.put("notaVendaId", nv.getId());
                vendaMap.put("dataHora", nv.getDataHoraVenda());
                vendaMap.put("tipoVenda", nv.getTipoVenda().name());
                vendaMap.put("valorTotal", nv.getTotal());
                vendaMap.put("produtos", produtos);

                // ✅ DADOS DA RESERVA
                if (nv.getReserva() != null) {
                    vendaMap.put("reservaId", nv.getReserva().getId());
                    if (nv.getReserva().getApartamento() != null) {
                        vendaMap.put("apartamento", nv.getReserva().getApartamento().getNumeroApartamento());
                    }
                    if (nv.getReserva().getCliente() != null) {
                        vendaMap.put("clienteNome", nv.getReserva().getCliente().getNome());
                    }
                }

                // Adicionar ao grupo
                vendasPorForma.computeIfAbsent(formaKey, k -> new ArrayList<>()).add(vendaMap);
                totaisPorForma.merge(formaKey, nv.getTotal(), BigDecimal::add);
                qtdVendasPorForma.merge(formaKey, 1, Integer::sum);
                qtdProdutosPorForma.merge(formaKey, qtdProdutosVenda, Integer::sum);

                totalVendas++;
                totalProdutos += qtdProdutosVenda;
                totalGeral = totalGeral.add(nv.getTotal());
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("totalVendas", totalVendas);
            resp.put("totalProdutos", totalProdutos);
            resp.put("totalGeral", totalGeral);
            resp.put("vendasPorFormaPagamento", vendasPorForma);
            resp.put("totaisPorFormaPagamento", totaisPorForma);
            resp.put("quantidadeVendasPorFormaPagamento", qtdVendasPorForma);
            resp.put("quantidadeProdutosPorFormaPagamento", qtdProdutosPorForma);

            return ResponseEntity.ok(resp);

        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/fechamento-caixa/{id}/relatorio")
    public ResponseEntity<?> relatorio(@PathVariable Long id) {
        return relatorioDetalhado(id);
    }

    // ─── MONTAR DTO COM TOTAIS (reservas + avulsas PDV) ────
    private Map<String, Object> montarDTO(FechamentoCaixa caixa) {
        LocalDateTime fim = caixa.getDataHoraFechamento() != null
            ? caixa.getDataHoraFechamento() : LocalDateTime.now();

        // === PAGAMENTOS DE RESERVAS ===
        List<Pagamento> pagamentos = pagamentoRepository
            .findByDataHoraPagamentoBetween(caixa.getDataHoraAbertura(), fim);

        BigDecimal totalDinheiro = somarPorForma(pagamentos, Pagamento.FormaPagamentoEnum.DINHEIRO);
        BigDecimal totalPix      = somarPorForma(pagamentos, Pagamento.FormaPagamentoEnum.PIX);
        BigDecimal totalDebito   = somarPorForma(pagamentos, Pagamento.FormaPagamentoEnum.CARTAO_DEBITO);
        BigDecimal totalCredito  = somarPorForma(pagamentos, Pagamento.FormaPagamentoEnum.CARTAO_CREDITO);
        BigDecimal totalTransf   = somarPorForma(pagamentos, Pagamento.FormaPagamentoEnum.TRANSFERENCIA_BANCARIA);
        BigDecimal totalFaturado = somarPorForma(pagamentos, Pagamento.FormaPagamentoEnum.FATURADO);
        BigDecimal totalLinkPix    = somarPorForma(pagamentos, Pagamento.FormaPagamentoEnum.LINK_PIX);
        BigDecimal totalLinkCartao = somarPorForma(pagamentos, Pagamento.FormaPagamentoEnum.LINK_CARTAO);
        // === VENDAS AVULSAS DO PDV (VISTA + FATURADO) ===
        List<NotaVenda> vendasAvulsas = notaVendaRepository.findByTipoVendaInAndPeriodo(
            List.of(NotaVenda.TipoVendaEnum.VISTA, NotaVenda.TipoVendaEnum.FATURADO),
            caixa.getDataHoraAbertura(), fim
        );

        BigDecimal avDinheiro      = BigDecimal.ZERO;
        BigDecimal avPix           = BigDecimal.ZERO;
        BigDecimal avDebito        = BigDecimal.ZERO;
        BigDecimal avCredito       = BigDecimal.ZERO;
        BigDecimal avTransferencia = BigDecimal.ZERO;
        BigDecimal avFaturado      = BigDecimal.ZERO;
        BigDecimal avLinkPix    = BigDecimal.ZERO;
        BigDecimal avLinkCartao = BigDecimal.ZERO;

        for (NotaVenda nv : vendasAvulsas) {
            if (nv.getTotal() == null || nv.getFormaPagamento() == null) continue;
            switch (nv.getFormaPagamento()) {
                case DINHEIRO             -> avDinheiro      = avDinheiro.add(nv.getTotal());
                case PIX                  -> avPix           = avPix.add(nv.getTotal());
                case CARTAO_DEBITO        -> avDebito        = avDebito.add(nv.getTotal());
                case CARTAO_CREDITO       -> avCredito       = avCredito.add(nv.getTotal());
                case TRANSFERENCIA_BANCARIA -> avTransferencia = avTransferencia.add(nv.getTotal());
                case FATURADO             -> avFaturado      = avFaturado.add(nv.getTotal());
                case LINK_PIX    -> avLinkPix    = avLinkPix.add(nv.getTotal());
                case LINK_CARTAO -> avLinkCartao = avLinkCartao.add(nv.getTotal());
                default -> {}
            }
        }

        BigDecimal avTotal = avDinheiro.add(avPix).add(avDebito)
            .add(avCredito).add(avTransferencia).add(avFaturado);

        // === TOTAIS GERAIS (reservas + avulsas) ===
        BigDecimal gtDinheiro = totalDinheiro.add(avDinheiro);
        BigDecimal gtPix      = totalPix.add(avPix);
        BigDecimal gtDebito   = totalDebito.add(avDebito);
        BigDecimal gtCredito  = totalCredito.add(avCredito);
        BigDecimal gtTransf   = totalTransf.add(avTransferencia);
        BigDecimal gtFaturado = totalFaturado.add(avFaturado);
        BigDecimal gtLinkPix    = totalLinkPix.add(avLinkPix);
        BigDecimal gtLinkCartao = totalLinkCartao.add(avLinkCartao);

        
        BigDecimal gtTotal    = gtDinheiro.add(gtPix).add(gtDebito)
            .add(gtCredito).add(gtTransf).add(gtFaturado)
            .add(gtLinkPix).add(gtLinkCartao);

        // === MAPA DE VENDAS AVULSAS (para o frontend) ===
        Map<String, Object> vendasAvulsasMap = new LinkedHashMap<>();
        vendasAvulsasMap.put("dinheiro",      avDinheiro);
        vendasAvulsasMap.put("pix",           avPix);
        vendasAvulsasMap.put("cartaoDebito",  avDebito);
        vendasAvulsasMap.put("cartaoCredito", avCredito);
        vendasAvulsasMap.put("transferencia", avTransferencia);
        vendasAvulsasMap.put("faturado",      avFaturado);
        vendasAvulsasMap.put("linkPix",    avLinkPix);
        vendasAvulsasMap.put("linkCartao", avLinkCartao);

        vendasAvulsasMap.put("total",         avTotal);

        Map<String, Object> dto = new HashMap<>();
        dto.put("id",                  caixa.getId());
        dto.put("usuarioId",           caixa.getUsuario().getId());
        dto.put("usuarioNome",         caixa.getUsuario().getNome());
        dto.put("dataHoraAbertura",    caixa.getDataHoraAbertura());
        dto.put("dataHoraFechamento",  caixa.getDataHoraFechamento());
        dto.put("status",              caixa.getStatus().name());
        dto.put("turno",               caixa.getTurno());
        dto.put("observacoes",         caixa.getObservacoes());

        // Totais gerais (reservas + PDV)
        dto.put("totalDinheiro",       gtDinheiro);
        dto.put("totalPix",            gtPix);
        dto.put("totalCartaoDebito",   gtDebito);
        dto.put("totalCartaoCredito",  gtCredito);
        dto.put("totalTransferencia",  gtTransf);
        dto.put("totalLinkPix",    gtLinkPix);
        dto.put("totalLinkCartao", gtLinkCartao);
        dto.put("totalFaturado",       gtFaturado);
        dto.put("totalLiquido",        gtTotal);
        dto.put("totalBruto",          gtTotal);
        dto.put("quantidadeVendas",    pagamentos.size() + vendasAvulsas.size());

        // Vendas avulsas separadas (para a seção 🛒 no frontend)
        dto.put("vendasAvulsas",       vendasAvulsasMap);

        return dto;
    }

    private BigDecimal somarPorForma(List<Pagamento> pagamentos,
                                      Pagamento.FormaPagamentoEnum forma) {
        return pagamentos.stream()
            .filter(p -> p.getFormaPagamento() == forma)
            .map(Pagamento::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}