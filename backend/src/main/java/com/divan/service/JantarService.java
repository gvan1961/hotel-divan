package com.divan.service;

import com.divan.entity.*;
import com.divan.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JantarService {
	
	@Autowired
	private CategoriaRepository categoriaRepository;

    @Autowired
    private HospedagemHospedeRepository hospedagemHospedeRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private NotaVendaRepository notaVendaRepository;

    @Autowired
    private ItemVendaRepository itemVendaRepository;

    @Autowired
    private ExtratoReservaRepository extratoReservaRepository;
    
    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ✅ LISTAR APARTAMENTOS COM HÓSPEDES AUTORIZADOS PARA JANTAR
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getApartamentosAutorizados() {
        // Busca todos os hóspedes HOSPEDADOS
        List<HospedagemHospede> hospedes = hospedagemHospedeRepository
            .findByStatus(HospedagemHospede.StatusEnum.HOSPEDADO);

        // Filtra apenas os autorizados para jantar
        List<HospedagemHospede> autorizados = hospedes.stream()
        	    .filter(h -> h.getCliente() != null &&
        	                 Boolean.TRUE.equals(h.getCliente().getAutorizadoJantar()) &&
        	                 h.getReserva() != null &&
        	                 h.getReserva().getStatus() == Reserva.StatusReservaEnum.ATIVA)
        	    .collect(Collectors.toList());

        // Agrupa por apartamento
        Map<Long, List<HospedagemHospede>> porApartamento = autorizados.stream()
            .collect(Collectors.groupingBy(h -> h.getReserva().getApartamento().getId()));

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Map.Entry<Long, List<HospedagemHospede>> entry : porApartamento.entrySet()) {
            List<HospedagemHospede> hospedesApt = entry.getValue();
            Apartamento apt = hospedesApt.get(0).getReserva().getApartamento();
            Reserva reserva = hospedesApt.get(0).getReserva();

            List<Map<String, Object>> hospedesInfo = hospedesApt.stream().map(h -> {
                Map<String, Object> info = new HashMap<>();
                info.put("id", h.getId());
                info.put("clienteId", h.getCliente().getId());
                info.put("nomeCliente", h.getCliente().getNome());
                info.put("titular", h.isTitular());
                info.put("autorizadoJantar", h.getCliente().getAutorizadoJantar());
                return info;
            }).collect(Collectors.toList());

            Map<String, Object> aptInfo = new HashMap<>();
            aptInfo.put("apartamentoId", apt.getId());
            aptInfo.put("numeroApartamento", apt.getNumeroApartamento());
            aptInfo.put("reservaId", reserva.getId());
            aptInfo.put("hospedes", hospedesInfo);

            resultado.add(aptInfo);
        }

        // Ordena por número do apartamento
        resultado.sort(Comparator.comparing(m -> m.get("numeroApartamento").toString()));

        System.out.println("🍽️ Apartamentos com jantar autorizado: " + resultado.size());
        return resultado;
    }

    // ✅ BUSCAR HÓSPEDE POR NOME OU APARTAMENTO
    @Transactional(readOnly = true)
    public List<Map<String, Object>> buscarHospede(String nome, String numeroApartamento) {
        List<HospedagemHospede> hospedes = hospedagemHospedeRepository
            .findByStatus(HospedagemHospede.StatusEnum.HOSPEDADO);

        return hospedes.stream()
            .filter(h -> h.getCliente() != null &&
                         Boolean.TRUE.equals(h.getCliente().getAutorizadoJantar()))
            .filter(h -> {
                boolean matchNome = nome == null || nome.isBlank() ||
                    h.getCliente().getNome().toLowerCase().contains(nome.toLowerCase());
                boolean matchApt = numeroApartamento == null || numeroApartamento.isBlank() ||
                    h.getReserva().getApartamento().getNumeroApartamento()
                        .contains(numeroApartamento);
                return matchNome && matchApt;
            })
            .map(h -> {
                Map<String, Object> info = new HashMap<>();
                info.put("id", h.getId());
                info.put("clienteId", h.getCliente().getId());
                info.put("nomeCliente", h.getCliente().getNome());
                info.put("apartamentoId", h.getReserva().getApartamento().getId());
                info.put("numeroApartamento", h.getReserva().getApartamento().getNumeroApartamento());
                info.put("reservaId", h.getReserva().getId());
                info.put("titular", h.isTitular());
                return info;
            })
            .collect(Collectors.toList());
    }

    // ✅ SALVAR COMANDA — LANÇA NO EXTRATO DA RESERVA
    @Transactional
    public Map<String, Object> salvarComanda(Long hospedagemHospedeId,
            List<Map<String, Object>> itens) {

        HospedagemHospede hospede = hospedagemHospedeRepository.findById(hospedagemHospedeId)
            .orElseThrow(() -> new RuntimeException("Hóspede não encontrado"));

        if (!Boolean.TRUE.equals(hospede.getCliente().getAutorizadoJantar())) {
            throw new RuntimeException("Hóspede não autorizado para jantar");
        }

        Reserva reserva = hospede.getReserva();
        BigDecimal totalComanda = BigDecimal.ZERO;

        // ✅ CRIAR NOTA DE VENDA
        NotaVenda nota = new NotaVenda();
        nota.setReserva(reserva);
        nota.setDataHoraVenda(LocalDateTime.now());
        nota.setTipoVenda(NotaVenda.TipoVendaEnum.APARTAMENTO);
        nota.setStatus(NotaVenda.Status.FECHADA);
        nota.setObservacao("Jantar - " + hospede.getCliente().getNome());
        nota.setItens(new ArrayList<>());
        nota.setTotal(BigDecimal.ZERO);
        notaVendaRepository.save(nota);

        // ✅ PROCESSAR ITENS
        for (Map<String, Object> itemMap : itens) {
            Long produtoId = Long.parseLong(itemMap.get("produtoId").toString());
            int quantidade = Integer.parseInt(itemMap.get("quantidade").toString());

            Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + produtoId));

            if (produto.getQuantidade() <= 0) {
                throw new RuntimeException(
                    "❌ Produto '" + produto.getNomeProduto() + "' está com ESTOQUE ZERO."
                );
            }
            if (produto.getQuantidade() < quantidade) {
                throw new RuntimeException(
                    "❌ Estoque insuficiente para '" + produto.getNomeProduto() + 
                    "'. Disponível: " + produto.getQuantidade() + " | Solicitado: " + quantidade
                );
            }

            BigDecimal valorUnitario = produto.getValorVenda();
            BigDecimal totalItem = valorUnitario.multiply(BigDecimal.valueOf(quantidade));

            // ✅ CRIAR ITEM DE VENDA
            ItemVenda itemVenda = new ItemVenda();
            itemVenda.setNotaVenda(nota);
            itemVenda.setProduto(produto);
            itemVenda.setQuantidade(quantidade);
            itemVenda.setValorUnitario(valorUnitario);
            itemVenda.setTotalItem(totalItem);
            itemVendaRepository.save(itemVenda);

            // ✅ BAIXAR ESTOQUE
            produto.setQuantidade(produto.getQuantidade() - quantidade);
            produtoRepository.save(produto);

            totalComanda = totalComanda.add(totalItem);

            // ✅ LANÇAR NO EXTRATO DA RESERVA
            ExtratoReserva extrato = new ExtratoReserva();
            extrato.setReserva(reserva);
            extrato.setDataHoraLancamento(LocalDateTime.now());
            extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.PRODUTO);
            extrato.setDescricao("Jantar: " + produto.getNomeProduto() +
                " (" + hospede.getCliente().getNome() + ")");
            extrato.setQuantidade(quantidade);
            extrato.setValorUnitario(valorUnitario);
            extrato.setTotalLancamento(totalItem);
            extrato.setNotaVendaId(nota.getId());
            extratoReservaRepository.save(extrato);
        }

        // ✅ ATUALIZAR TOTAL DA NOTA
        nota.setTotal(totalComanda);
        notaVendaRepository.save(nota);

        // ✅ ATUALIZAR TOTAIS DA RESERVA
        reserva.setTotalProduto(reserva.getTotalProduto().add(totalComanda));
        reserva.setTotalHospedagem(reserva.getTotalHospedagem().add(totalComanda));
        reserva.setTotalApagar(reserva.getTotalApagar().add(totalComanda));
        reservaRepository.save(reserva);

        // ✅ LOG AUDITORIA
        try {
            String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
            LogAuditoria log = new LogAuditoria();
            log.setAcao("JANTAR_COMANDA");
            log.setDescricao("Jantar — Apt " + reserva.getApartamento().getNumeroApartamento()
                + " — Cliente: " + hospede.getCliente().getNome()
                + " — Total: R$ " + totalComanda);
            log.setDataHora(LocalDateTime.now());
            log.setReserva(reserva);
            usuarioRepository.findByUsername(username).ifPresent(log::setUsuario);
            logAuditoriaRepository.save(log);
        } catch (Exception logEx) {
            System.err.println("⚠️ Erro ao salvar log: " + logEx.getMessage());
        }

        System.out.println("🍽️ Comanda salva | Reserva #" + reserva.getId() +
            " | Hóspede: " + hospede.getCliente().getNome() +
            " | Total: R$ " + totalComanda);

        return Map.of(
            "mensagem", "Comanda salva com sucesso",
            "notaId", nota.getId(),
            "total", totalComanda,
            "reservaId", reserva.getId()
        );
    }

    // ✅ CANCELAR COMANDA
    @Transactional
    public Map<String, Object> cancelarComanda(Long notaId) {
        NotaVenda nota = notaVendaRepository.findById(notaId)
            .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        Reserva reserva = nota.getReserva();
        BigDecimal totalNota = nota.getTotal();

        // ✅ DEVOLVER ESTOQUE
        for (ItemVenda item : nota.getItens()) {
            Produto produto = item.getProduto();
            produto.setQuantidade(produto.getQuantidade() + item.getQuantidade());
            produtoRepository.save(produto);
        }

        // ✅ LANÇAR ESTORNO NO EXTRATO
        ExtratoReserva estorno = new ExtratoReserva();
        estorno.setReserva(reserva);
        estorno.setDataHoraLancamento(LocalDateTime.now());
        estorno.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.ESTORNO);
        estorno.setDescricao("Cancelamento comanda jantar #" + notaId);
        estorno.setQuantidade(1);
        estorno.setValorUnitario(totalNota.negate());
        estorno.setTotalLancamento(totalNota.negate());
        estorno.setNotaVendaId(notaId);
        extratoReservaRepository.save(estorno);

        // ✅ ATUALIZAR TOTAIS DA RESERVA
        reserva.setTotalProduto(reserva.getTotalProduto().subtract(totalNota));
        reserva.setTotalHospedagem(reserva.getTotalHospedagem().subtract(totalNota));
        reserva.setTotalApagar(reserva.getTotalApagar().subtract(totalNota));
        reservaRepository.save(reserva);

        // ✅ CANCELAR NOTA
        nota.setStatus(NotaVenda.Status.CANCELADA);
        notaVendaRepository.save(nota);

        System.out.println("❌ Comanda cancelada #" + notaId +
            " | Estorno: R$ " + totalNota);

        return Map.of(
            "mensagem", "Comanda cancelada com sucesso",
            "estorno", totalNota
        );
    }
    
 // ✅ PRODUTOS DO RESTAURANTE COM ESTOQUE > 0
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProdutosRestaurante() {
        // Busca todos os produtos e filtra pela categoria RESTAURANTE
        List<Produto> todos = produtoRepository.findAll();
        
        return todos.stream()
            .filter(p -> p.getCategoria() != null &&
                         p.getCategoria().getNome().toUpperCase().contains("RESTAURANTE") &&
                         p.getQuantidade() > 0)
            .map(p -> {
                Map<String, Object> info = new HashMap<>();
                info.put("id", p.getId());
                info.put("nomeProduto", p.getNomeProduto());
                info.put("valorVenda", p.getValorVenda());
                info.put("quantidade", p.getQuantidade());
                return info;
            })
            .sorted(Comparator.comparing(m -> m.get("nomeProduto").toString()))
            .collect(Collectors.toList());
    }

    // ✅ BUSCAR HÓSPEDE — retorno no formato que o frontend espera
    @Transactional(readOnly = true)
    public Map<String, Object> buscarHospedeFormatado(String nome, String numeroApartamento) {
        List<HospedagemHospede> hospedes = hospedagemHospedeRepository
            .findByStatus(HospedagemHospede.StatusEnum.HOSPEDADO);

        List<Map<String, Object>> encontrados = hospedes.stream()
        	    .filter(h -> h.getCliente() != null)
        	    .filter(h -> h.getReserva() != null &&
        	                 h.getReserva().getStatus() == Reserva.StatusReservaEnum.ATIVA)
        	    .filter(h -> {
                boolean matchNome = nome == null || nome.isBlank() ||
                    h.getCliente().getNome().toLowerCase().contains(nome.toLowerCase());
                boolean matchApt = numeroApartamento == null || numeroApartamento.isBlank() ||
                    h.getReserva().getApartamento().getNumeroApartamento().contains(numeroApartamento);
                return matchNome && matchApt;
            })
            .map(h -> {
                Map<String, Object> info = new HashMap<>();
                info.put("id", h.getId());
                info.put("hospedagemHospedeId", h.getId());
                info.put("clienteId", h.getCliente().getId());
                info.put("nomeCliente", h.getCliente().getNome());
                info.put("nomeCompleto", h.getCliente().getNome());
                info.put("apartamentoId", h.getReserva().getApartamento().getId());
                info.put("numeroApartamento", h.getReserva().getApartamento().getNumeroApartamento());
                info.put("reservaId", h.getReserva().getId());
                info.put("titular", h.isTitular());
                info.put("autorizadoJantar", Boolean.TRUE.equals(h.getCliente().getAutorizadoJantar()));
                return info;
            })
            .collect(Collectors.toList());

        if (encontrados.isEmpty()) {
            return Map.of(
                "encontrado", false,
                "mensagem", "Nenhum hóspede encontrado com os dados informados"
            );
        }

        return Map.of(
            "encontrado", true,
            "hospedes", encontrados
        );
    }

    // ✅ RELATÓRIO DE COMANDAS
    @Transactional(readOnly = true)
    public Map<String, Object> gerarRelatorioComandas(String dataInicio, String dataFim) {
        LocalDateTime inicio = LocalDate.parse(dataInicio).atStartOfDay();
        LocalDateTime fim = LocalDate.parse(dataFim).atTime(23, 59, 59);

        List<NotaVenda> notas = notaVendaRepository
            .findByTipoVendaAndDataHoraVendaBetween(NotaVenda.TipoVendaEnum.APARTAMENTO, inicio, fim)
            .stream()
            .filter(n -> n.getObservacao() != null && n.getObservacao().startsWith("Jantar"))
            .filter(n -> n.getStatus() != NotaVenda.Status.CANCELADA)
            .collect(Collectors.toList());

        BigDecimal totalGeral = notas.stream()
            .map(NotaVenda::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> comandas = notas.stream().map(n -> {
            Map<String, Object> c = new HashMap<>();
            c.put("notaId", n.getId());
            c.put("dataHora", n.getDataHoraVenda());
            c.put("apartamento", n.getReserva().getApartamento().getNumeroApartamento());
            c.put("cliente", n.getReserva().getCliente().getNome());
            c.put("total", n.getTotal());
            c.put("observacao", n.getObservacao());

            List<Map<String, Object>> itens = n.getItens().stream().map(i -> {
                Map<String, Object> item = new HashMap<>();
                item.put("produto", i.getProduto().getNomeProduto());
                item.put("quantidade", i.getQuantidade());
                item.put("valorUnitario", i.getValorUnitario());
                item.put("total", i.getTotalItem());
                return item;
            }).collect(Collectors.toList());

            c.put("itens", itens);
            return c;
        }).collect(Collectors.toList());

        return Map.of(
            "totalComandas", notas.size(),
            "totalGeral", totalGeral,
            "comandas", comandas,
            "periodo", Map.of("inicio", dataInicio, "fim", dataFim)
        );
    }

    // ✅ RELATÓRIO DE FATURAMENTO
    @Transactional(readOnly = true)
    public Map<String, Object> gerarRelatorioFaturamento(String dataInicio, String dataFim) {
        LocalDateTime inicio = LocalDate.parse(dataInicio).atStartOfDay();
        LocalDateTime fim = LocalDate.parse(dataFim).atTime(23, 59, 59);

        List<NotaVenda> notas = notaVendaRepository
            .findByTipoVendaAndDataHoraVendaBetween(NotaVenda.TipoVendaEnum.APARTAMENTO, inicio, fim)
            .stream()
            .filter(n -> n.getObservacao() != null && n.getObservacao().startsWith("Jantar"))
            .filter(n -> n.getStatus() != NotaVenda.Status.CANCELADA)
            .collect(Collectors.toList());

        BigDecimal totalGeral = notas.stream()
            .map(NotaVenda::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ticketMedio = notas.isEmpty() ? BigDecimal.ZERO :
            totalGeral.divide(BigDecimal.valueOf(notas.size()), 2, java.math.RoundingMode.HALF_UP);

        // Agrupar por dia
        Map<LocalDate, List<NotaVenda>> porDia = notas.stream()
            .collect(Collectors.groupingBy(n -> n.getDataHoraVenda().toLocalDate()));

        List<Map<String, Object>> faturamentoDiario = porDia.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> {
                BigDecimal totalDia = e.getValue().stream()
                    .map(NotaVenda::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
                Map<String, Object> dia = new HashMap<>();
                dia.put("data", e.getKey().toString());
                dia.put("totalComandas", e.getValue().size());
                dia.put("totalVendas", totalDia);
                return dia;
            }).collect(Collectors.toList());

        return Map.of(
            "totalComandas", notas.size(),
            "totalGeral", totalGeral,
            "ticketMedio", ticketMedio,
            "faturamentoDiario", faturamentoDiario,
            "periodo", Map.of("inicio", dataInicio, "fim", dataFim)
        );
    }

    // ✅ RELATÓRIO PRODUTOS POR APARTAMENTO
    @Transactional(readOnly = true)
    public List<Map<String, Object>> relatorioProdutosPorApartamento(String dataInicio, String dataFim) {
        LocalDateTime inicio = LocalDate.parse(dataInicio).atStartOfDay();
        LocalDateTime fim = LocalDate.parse(dataFim).atTime(23, 59, 59);

        List<ExtratoReserva> extratos = extratoReservaRepository
            .findByStatusLancamentoAndDataHoraLancamentoBetween(
                ExtratoReserva.StatusLancamentoEnum.PRODUTO, inicio, fim)
            .stream()
            .filter(e -> e.getDescricao() != null && e.getDescricao().startsWith("Jantar:"))
            .collect(Collectors.toList());

        Map<Long, List<ExtratoReserva>> porReserva = extratos.stream()
            .collect(Collectors.groupingBy(e -> e.getReserva().getId()));

        return porReserva.entrySet().stream().map(entry -> {
            List<ExtratoReserva> itensReserva = entry.getValue();
            Reserva reserva = itensReserva.get(0).getReserva();
            BigDecimal totalGasto = itensReserva.stream()
                .map(ExtratoReserva::getTotalLancamento)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<Map<String, Object>> itens = itensReserva.stream().map(e -> {
                Map<String, Object> item = new HashMap<>();
                item.put("nomeProduto", e.getDescricao().replace("Jantar: ", "").split(" \\(")[0]);
                item.put("dataHora", e.getDataHoraLancamento());
                item.put("quantidade", e.getQuantidade());
                item.put("valorUnitario", e.getValorUnitario());
                item.put("totalItem", e.getTotalLancamento());
                return item;
            }).collect(Collectors.toList());

            Map<String, Object> apto = new HashMap<>();
            apto.put("numeroApartamento", reserva.getApartamento().getNumeroApartamento());
            apto.put("nomeHospede", reserva.getCliente().getNome());
            apto.put("totalGasto", totalGasto);
            apto.put("itens", itens);
            return apto;
        })
        .sorted(Comparator.comparing(m -> m.get("numeroApartamento").toString()))
        .collect(Collectors.toList());
    }

    // ✅ RELATÓRIO QUANTIDADE POR PRODUTO
    @Transactional(readOnly = true)
    public Map<String, Object> relatorioQuantidadeProduto(Long produtoId, String dataInicio, String dataFim) {
        LocalDateTime inicio = LocalDate.parse(dataInicio).atStartOfDay();
        LocalDateTime fim = LocalDate.parse(dataFim).atTime(23, 59, 59);

        Produto produto = produtoRepository.findById(produtoId)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        List<ItemVenda> itens = itemVendaRepository.findByProdutoId(produtoId)
            .stream()
            .filter(i -> i.getNotaVenda() != null &&
                         i.getNotaVenda().getDataHoraVenda() != null &&
                         !i.getNotaVenda().getDataHoraVenda().isBefore(inicio) &&
                         !i.getNotaVenda().getDataHoraVenda().isAfter(fim) &&
                         i.getNotaVenda().getObservacao() != null &&
                         i.getNotaVenda().getObservacao().startsWith("Jantar") &&
                         i.getNotaVenda().getStatus() != NotaVenda.Status.CANCELADA)
            .collect(Collectors.toList());

        int quantidadeTotal = itens.stream().mapToInt(ItemVenda::getQuantidade).sum();
        BigDecimal totalFaturado = itens.stream()
            .map(ItemVenda::getTotalItem).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> porApartamento = itens.stream().map(i -> {
            Map<String, Object> info = new HashMap<>();
            info.put("numeroApartamento", i.getNotaVenda().getReserva().getApartamento().getNumeroApartamento());
            info.put("nomeHospede", i.getNotaVenda().getReserva().getCliente().getNome());
            info.put("dataHora", i.getNotaVenda().getDataHoraVenda());
            info.put("quantidade", i.getQuantidade());
            info.put("total", i.getTotalItem());
            return info;
        }).collect(Collectors.toList());

        return Map.of(
            "nomeProduto", produto.getNomeProduto(),
            "quantidadeTotal", quantidadeTotal,
            "totalFaturado", totalFaturado,
            "porApartamento", porApartamento
        );
    }
}
