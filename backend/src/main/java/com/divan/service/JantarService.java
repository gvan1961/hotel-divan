package com.divan.service;

import com.divan.dto.ApartamentoJantarDTO;
import com.divan.dto.HospedeJantarDTO;
import com.divan.entity.Apartamento;
import com.divan.entity.Cliente;
import com.divan.entity.Empresa;
import com.divan.entity.HospedagemHospede;
import com.divan.entity.Reserva;
import com.divan.entity.HospedagemHospede.StatusHospedeIndividual;
import com.divan.repository.HospedagemHospedeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JantarService {

    @Autowired
    private HospedagemHospedeRepository hospedagemHospedeRepository;

    // ═══════════════════════════════════════════════════════════
    // LISTAR APARTAMENTOS COM HÓSPEDES AUTORIZADOS
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<ApartamentoJantarDTO> listarApartamentosComHospedesAutorizados() {
        System.out.println("\n🍽️ ═══════════════════════════════════════════════════════");
        System.out.println("   BUSCANDO APARTAMENTOS COM HÓSPEDES AUTORIZADOS");
        System.out.println("═══════════════════════════════════════════════════════\n");
        
        return processarApartamentosComHospedes(true);
    }

    // ═══════════════════════════════════════════════════════════
    // LISTAR TODOS OS APARTAMENTOS
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<ApartamentoJantarDTO> listarTodosApartamentosComHospedes() {
        System.out.println("\n🍽️ ═══════════════════════════════════════════════════════");
        System.out.println("   BUSCANDO TODOS OS APARTAMENTOS");
        System.out.println("═══════════════════════════════════════════════════════\n");
        
        return processarApartamentosComHospedes(false);
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTODO PRINCIPAL DE PROCESSAMENTO
    // ═══════════════════════════════════════════════════════════
    private List<ApartamentoJantarDTO> processarApartamentosComHospedes(boolean apenasAutorizados) {
        
        List<HospedagemHospede> todosHospedes = hospedagemHospedeRepository.findAll()
            .stream()
            .filter(h -> h.getStatus() == StatusHospedeIndividual.HOSPEDADO)
            .collect(Collectors.toList());
        
        System.out.println("👥 Total de registros HOSPEDADOS: " + todosHospedes.size());
        
        // Deduplicar por cliente (pegar o mais recente)
        Map<Long, HospedagemHospede> clienteUnico = new HashMap<>();
        
        for (HospedagemHospede hospede : todosHospedes) {
            Cliente cliente = hospede.getCliente();
            if (cliente == null) continue;
            
            Long clienteId = cliente.getId();
            
            if (clienteUnico.containsKey(clienteId)) {
                HospedagemHospede existente = clienteUnico.get(clienteId);
                
                if (hospede.getDataEntrada() != null && existente.getDataEntrada() != null) {
                    if (hospede.getDataEntrada().isAfter(existente.getDataEntrada())) {
                        clienteUnico.put(clienteId, hospede);
                        System.out.println("🔄 Cliente " + cliente.getNome() + " → Usando registro mais recente");
                    }
                } else if (existente.getDataEntrada() == null) {
                    clienteUnico.put(clienteId, hospede);
                }
            } else {
                clienteUnico.put(clienteId, hospede);
            }
        }
        
        System.out.println("\n✅ Total de clientes únicos: " + clienteUnico.size());
        
        // Processar apartamentos
        Map<String, ApartamentoJantarDTO> apartamentosMap = new LinkedHashMap<>();
        
        for (HospedagemHospede hospede : clienteUnico.values()) {
            Reserva reserva = hospede.getReserva();
            if (reserva == null) continue;
            
            Apartamento apartamento = reserva.getApartamento();
            if (apartamento == null) continue;
            
            String numeroApartamento = apartamento.getNumeroApartamento();
            Cliente cliente = hospede.getCliente();
            if (cliente == null) continue;
            
            boolean podeJantar = verificarSeClientePodeJantar(cliente);
            
            if (apenasAutorizados && !podeJantar) {
                System.out.println("❌ Apt " + numeroApartamento + " → " + cliente.getNome() + " (NÃO AUTORIZADO)");
                continue;
            }
            
            String emoji = podeJantar ? "✅" : "⚠️";
            System.out.println(emoji + " Apt " + numeroApartamento + " → " + cliente.getNome());
            
            ApartamentoJantarDTO apartamentoDTO = apartamentosMap.get(numeroApartamento);
            
            if (apartamentoDTO == null) {
                apartamentoDTO = new ApartamentoJantarDTO();
                apartamentoDTO.setNumeroApartamento(numeroApartamento);
                apartamentoDTO.setReservaId(reserva.getId());
                apartamentosMap.put(numeroApartamento, apartamentoDTO);
            }
            
            String empresaNome = null;
            if (cliente.getEmpresa() != null) {
                empresaNome = cliente.getEmpresa().getNomeEmpresa();
            }
            
            Boolean titular = hospede.getTitular() != null ? hospede.getTitular() : false;
            
            apartamentoDTO.adicionarHospede(
                cliente.getId(),
                cliente.getNome(),
                empresaNome,
                titular
            );
        }
        
        List<ApartamentoJantarDTO> resultado = new ArrayList<>(apartamentosMap.values());
        
        resultado.sort((a, b) -> {
            try {
                int numA = Integer.parseInt(a.getNumeroApartamento().replaceAll("[^0-9]", ""));
                int numB = Integer.parseInt(b.getNumeroApartamento().replaceAll("[^0-9]", ""));
                return Integer.compare(numA, numB);
            } catch (NumberFormatException e) {
                return a.getNumeroApartamento().compareTo(b.getNumeroApartamento());
            }
        });
        
        System.out.println("\n📊 RESULTADO:");
        System.out.println("   🏢 Total de apartamentos: " + resultado.size());
        
        int totalHospedes = resultado.stream()
            .mapToInt(ApartamentoJantarDTO::getTotalHospedes)
            .sum();
        System.out.println("   👥 Total de hóspedes: " + totalHospedes);
        
        System.out.println("\n📋 LISTA RESUMIDA:");
        for (ApartamentoJantarDTO apt : resultado) {
            String nomes = apt.getHospedes().stream()
                .map(ApartamentoJantarDTO.HospedeJantarInfoDTO::getNomeCliente)
                .collect(Collectors.joining(", "));
            System.out.println("   Apt " + apt.getNumeroApartamento() + " → " + nomes + " (" + apt.getTotalHospedes() + " hóspedes)");
        }
        
        System.out.println("═══════════════════════════════════════════════════════\n");
        
        return resultado;
    }

    // ═══════════════════════════════════════════════════════════
    // LISTAR HÓSPEDES AUTORIZADOS (LISTA INDIVIDUAL)
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<HospedeJantarDTO> listarAutorizados() {
        return listarHospedesIndividual(true);
    }

    @Transactional(readOnly = true)
    public List<HospedeJantarDTO> listarTodos() {
        return listarHospedesIndividual(false);
    }

    private List<HospedeJantarDTO> listarHospedesIndividual(boolean apenasAutorizados) {
        
        List<HospedagemHospede> hospedes = hospedagemHospedeRepository.findAll()
            .stream()
            .filter(h -> h.getStatus() == StatusHospedeIndividual.HOSPEDADO)
            .collect(Collectors.toList());
        
        Map<Long, HospedagemHospede> clienteUnico = new HashMap<>();
        for (HospedagemHospede hospede : hospedes) {
            Cliente cliente = hospede.getCliente();
            if (cliente == null) continue;
            
            Long clienteId = cliente.getId();
            if (!clienteUnico.containsKey(clienteId)) {
                clienteUnico.put(clienteId, hospede);
            } else {
                HospedagemHospede existente = clienteUnico.get(clienteId);
                if (hospede.getDataEntrada() != null && existente.getDataEntrada() != null) {
                    if (hospede.getDataEntrada().isAfter(existente.getDataEntrada())) {
                        clienteUnico.put(clienteId, hospede);
                    }
                }
            }
        }
        
        List<HospedeJantarDTO> resultado = new ArrayList<>();
        
        for (HospedagemHospede hospede : clienteUnico.values()) {
            Cliente cliente = hospede.getCliente();
            if (cliente == null) continue;
            
            boolean podeJantar = verificarSeClientePodeJantar(cliente);
            
            if (apenasAutorizados && !podeJantar) {
                continue;
            }
            
            HospedeJantarDTO dto = criarDTO(hospede);
            if (dto != null) {
                resultado.add(dto);
            }
        }
        
        return resultado;
    }

    // ═══════════════════════════════════════════════════════════
    // VERIFICAR AUTORIZAÇÃO
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public Map<String, Object> verificarAutorizacao(Long clienteId) {
        
        Map<String, Object> resultado = new HashMap<>();
        
        List<HospedagemHospede> hospedes = hospedagemHospedeRepository.findAll()
            .stream()
            .filter(h -> h.getStatus() == StatusHospedeIndividual.HOSPEDADO)
            .filter(h -> h.getCliente() != null && h.getCliente().getId().equals(clienteId))
            .collect(Collectors.toList());
        
        if (hospedes.isEmpty()) {
            resultado.put("encontrado", false);
            resultado.put("mensagem", "Cliente não encontrado");
            return resultado;
        }
        
        HospedagemHospede hospede = hospedes.get(0);
        if (hospedes.size() > 1) {
            // Pegar o mais recente
            for (HospedagemHospede h : hospedes) {
                if (h.getDataEntrada() != null && hospede.getDataEntrada() != null) {
                    if (h.getDataEntrada().isAfter(hospede.getDataEntrada())) {
                        hospede = h;
                    }
                }
            }
        }
        
        Cliente cliente = hospede.getCliente();
        boolean podeJantar = verificarSeClientePodeJantar(cliente);
        
        resultado.put("encontrado", true);
        resultado.put("clienteId", cliente.getId());
        resultado.put("nomeCliente", cliente.getNome());
        resultado.put("podeJantar", podeJantar);
        
        if (hospede.getReserva() != null && hospede.getReserva().getApartamento() != null) {
            resultado.put("numeroApartamento", hospede.getReserva().getApartamento().getNumeroApartamento());
        }
        
        if (cliente.getEmpresa() != null) {
            resultado.put("empresaNome", cliente.getEmpresa().getNomeEmpresa());
            resultado.put("empresaAutorizaTodos", cliente.getEmpresa().getAutorizaTodosJantar());
        }
        
        resultado.put("clienteAutorizado", cliente.getAutorizadoJantar());
        
        return resultado;
    }

    // ═══════════════════════════════════════════════════════════
    // ESTATÍSTICAS
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public Map<String, Object> getEstatisticas() {
        
        List<HospedagemHospede> hospedes = hospedagemHospedeRepository.findAll()
            .stream()
            .filter(h -> h.getStatus() == StatusHospedeIndividual.HOSPEDADO)
            .collect(Collectors.toList());
        
        Map<Long, HospedagemHospede> clienteUnico = new HashMap<>();
        for (HospedagemHospede hospede : hospedes) {
            Cliente cliente = hospede.getCliente();
            if (cliente == null) continue;
            
            Long clienteId = cliente.getId();
            if (!clienteUnico.containsKey(clienteId)) {
                clienteUnico.put(clienteId, hospede);
            } else {
                HospedagemHospede existente = clienteUnico.get(clienteId);
                if (hospede.getDataEntrada() != null && existente.getDataEntrada() != null) {
                    if (hospede.getDataEntrada().isAfter(existente.getDataEntrada())) {
                        clienteUnico.put(clienteId, hospede);
                    }
                }
            }
        }
        
        int totalHospedes = clienteUnico.size();
        int totalAutorizados = 0;
        int semEmpresa = 0;
        int empresaAutorizaTodos = 0;
        int empresaSeletiva = 0;
        
        for (HospedagemHospede hospede : clienteUnico.values()) {
            Cliente cliente = hospede.getCliente();
            if (cliente == null) continue;
            
            if (verificarSeClientePodeJantar(cliente)) {
                totalAutorizados++;
            }
            
            if (cliente.getEmpresa() == null) {
                semEmpresa++;
            } else if (Boolean.TRUE.equals(cliente.getEmpresa().getAutorizaTodosJantar())) {
                empresaAutorizaTodos++;
            } else {
                empresaSeletiva++;
            }
        }
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalHospedes", totalHospedes);
        resultado.put("totalAutorizados", totalAutorizados);
        resultado.put("totalNaoAutorizados", totalHospedes - totalAutorizados);
        resultado.put("percentualAutorizados", totalHospedes > 0 ? (totalAutorizados * 100.0 / totalHospedes) : 0);
        resultado.put("semEmpresa", semEmpresa);
        resultado.put("empresaAutorizaTodos", empresaAutorizaTodos);
        resultado.put("empresaSeletiva", empresaSeletiva);
        
        return resultado;
    }

    // ═══════════════════════════════════════════════════════════
    // GERAR HTML PARA IMPRESSÃO
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public String gerarHtmlRelatorio() {

        List<ApartamentoJantarDTO> apartamentos = listarApartamentosComHospedesAutorizados();

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='pt-BR'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Relatório de Jantar</title>");
        html.append("<style>");
        html.append("* { margin: 0; padding: 0; box-sizing: border-box; }");
        html.append("body { font-family: Arial, sans-serif; padding: 20px; padding-top: 80px; }");
        html.append(".btn-voltar { position: fixed; top: 20px; right: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none; padding: 12px 24px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.3); transition: all 0.3s; z-index: 1000; }");
        html.append(".btn-voltar:hover { transform: translateX(-5px); box-shadow: 0 6px 16px rgba(0,0,0,0.4); }");
        html.append(".btn-imprimir { position: fixed; top: 20px; left: 20px; background: #27ae60; color: white; border: none; padding: 12px 24px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.3); transition: all 0.3s; z-index: 1000; }");
        html.append(".btn-imprimir:hover { background: #229954; transform: translateY(-2px); }");
        html.append(".header { text-align: center; margin-bottom: 30px; border-bottom: 3px solid #333; padding-bottom: 15px; }");
        html.append(".header h1 { font-size: 28px; }");
        html.append(".header h2 { font-size: 20px; color: #555; }");
        html.append(".apartamento { margin-bottom: 25px; page-break-inside: avoid; }");
        html.append(".apt-numero { font-size: 20px; font-weight: bold; background: #f0f0f0; padding: 10px; border-left: 5px solid #333; }");
        html.append(".hospede { padding-left: 25px; line-height: 2; font-size: 16px; }");
        html.append(".rodape { margin-top: 30px; text-align: center; border-top: 2px solid #333; padding-top: 15px; font-weight: bold; }");
        html.append("@media print { .btn-voltar, .btn-imprimir { display: none; } body { padding-top: 20px; } }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        // ✅ BOTÕES DE AÇÃO
        html.append("<button class='btn-voltar' onclick='window.close(); if(!window.closed) history.back();'>← Voltar</button>");
        html.append("<button class='btn-imprimir' onclick='window.print();'>🖨️ Imprimir</button>");

        html.append("<div class='header'>");
        html.append("<h1>HOTEL DI VAN</h1>");
        html.append("<h2>Autorizados para Jantar</h2>");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String dataAtual = LocalDate.now().format(formatter);
        html.append("<div>").append(dataAtual).append("</div>");
        html.append("</div>");

        int totalHospedes = 0;

        for (ApartamentoJantarDTO apartamento : apartamentos) {
            html.append("<div class='apartamento'>");
            html.append("<div class='apt-numero'>Apartamento ").append(apartamento.getNumeroApartamento()).append("</div>");

            for (ApartamentoJantarDTO.HospedeJantarInfoDTO hospede : apartamento.getHospedes()) {
                html.append("<div class='hospede'>").append(hospede.getNomeCliente()).append("</div>");
                totalHospedes++;
            }

            html.append("</div>");
        }

        html.append("<div class='rodape'>");
        html.append("Total de apartamentos: ").append(apartamentos.size());
        html.append(" | Total de hóspedes: ").append(totalHospedes);
        html.append("</div>");

        html.append("</body></html>");

        return html.toString();
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ═══════════════════════════════════════════════════════════
    
    private boolean verificarSeClientePodeJantar(Cliente cliente) {
        if (cliente == null) {
            System.out.println("⚠️ Cliente NULL");
            return false;
        }
        
        Empresa empresa = cliente.getEmpresa();
        
        // ✅ ÚNICA EXCEÇÃO: Empresa autoriza TODOS os funcionários
        if (empresa != null && Boolean.TRUE.equals(empresa.getAutorizaTodosJantar())) {
            System.out.println("✅ " + cliente.getNome() + " → EMPRESA AUTORIZA TODOS (" + empresa.getNomeEmpresa() + ")");
            return true;
        }
        
        // ✅ PARA TODOS OS OUTROS CASOS: Verificar campo individual
        // (Com empresa OU sem empresa → sempre verifica autorizadoJantar)
        boolean autorizado = Boolean.TRUE.equals(cliente.getAutorizadoJantar());
        
        String detalhe = empresa != null 
            ? " (Empresa: " + empresa.getNomeEmpresa() + " - não autoriza todos)" 
            : " (SEM EMPRESA)";
        
        System.out.println((autorizado ? "✅ " : "❌ ") + cliente.getNome() + 
                          " → Autorizado individual: " + autorizado + detalhe);
        
        return autorizado;
    }
    
    private HospedeJantarDTO criarDTO(HospedagemHospede hospede) {
        Cliente cliente = hospede.getCliente();
        if (cliente == null) return null;
        
        Reserva reserva = hospede.getReserva();
        if (reserva == null) return null;
        
        Apartamento apartamento = reserva.getApartamento();
        if (apartamento == null) return null;
        
        HospedeJantarDTO dto = new HospedeJantarDTO();
        dto.setClienteId(cliente.getId());
        dto.setNomeCliente(cliente.getNome());
        dto.setNumeroApartamento(apartamento.getNumeroApartamento());
        dto.setReservaId(reserva.getId());
        dto.setHospedeId(hospede.getId());
        
        if (cliente.getEmpresa() != null) {
            dto.setEmpresaNome(cliente.getEmpresa().getNomeEmpresa());
            dto.setEmpresaAutorizaTodos(cliente.getEmpresa().getAutorizaTodosJantar());
        }
        
        dto.setClienteAutorizado(cliente.getAutorizadoJantar());
        
        return dto;
    }
}