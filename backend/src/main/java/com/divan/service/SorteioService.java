package com.divan.service;

import com.divan.entity.*;
import com.divan.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SorteioService {

    @Autowired
    private SorteioRepository sorteioRepository;

    @Autowired
    private BilheteSorteioRepository bilheteSorteioRepository;

    @Autowired
    private HospedagemHospedeRepository hospedagemHospedeRepository;

    // ============================================
    // ✅ CRUD SORTEIO
    // ============================================

    public Sorteio criarSorteio(Sorteio sorteio) {
        sorteio.setStatus(Sorteio.StatusEnum.ATIVA);
        sorteio.setDataCriacao(LocalDateTime.now());
        return sorteioRepository.save(sorteio);
    }

    public List<Sorteio> listarTodos() {
        return sorteioRepository.findAllByOrderByDataCriacaoDesc();
    }

    public Optional<Sorteio> buscarPorId(Long id) {
        return sorteioRepository.findById(id);
    }

    public Sorteio encerrarSorteio(Long id) {
        Sorteio sorteio = sorteioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sorteio não encontrado"));
        sorteio.setStatus(Sorteio.StatusEnum.ENCERRADA);
        return sorteioRepository.save(sorteio);
    }

    // ============================================
    // ✅ GERAR BILHETES NO CHECKOUT
    // ============================================

    @Transactional
    public List<BilheteSorteio> gerarBilhetesCheckout(HospedagemHospede hospedagemHospede) {
        List<BilheteSorteio> bilhetesGerados = new ArrayList<>();

        // Buscar sorteio ativo
        Optional<Sorteio> sorteioOpt = sorteioRepository
            .findFirstByStatusOrderByDataCriacaoDesc(Sorteio.StatusEnum.ATIVA);

        if (sorteioOpt.isEmpty()) {
            System.out.println("ℹ️ Nenhum sorteio ativo — bilhetes não gerados");
            return bilhetesGerados;
        }

        Sorteio sorteio = sorteioOpt.get();

        // Verificar se o checkout está dentro do período do sorteio
        LocalDate dataCheckout = hospedagemHospede.getDataHoraSaida() != null
            ? hospedagemHospede.getDataHoraSaida().toLocalDate()
            : LocalDate.now();

        if (dataCheckout.isBefore(sorteio.getDataInicio()) ||
            dataCheckout.isAfter(sorteio.getDataFim())) {
            System.out.println("ℹ️ Checkout fora do período do sorteio");
            return bilhetesGerados;
        }

        // Verificar se já foram gerados bilhetes para este hóspede neste sorteio
        if (bilheteSorteioRepository.existsByHospedagemHospedeIdAndSorteioId(
                hospedagemHospede.getId(), sorteio.getId())) {
            System.out.println("ℹ️ Bilhetes já gerados para este hóspede neste sorteio");
            return bilheteSorteioRepository.findByHospedagemHospedeId(hospedagemHospede.getId());
        }

        // Calcular quantidade de diárias do hóspede
        int quantidadeDiarias = calcularDiariasHospede(hospedagemHospede);

        if (quantidadeDiarias <= 0) {
            System.out.println("ℹ️ Quantidade de diárias inválida");
            return bilhetesGerados;
        }

        // Buscar próximo número de bilhete
        int proximoNumero = bilheteSorteioRepository
            .findMaxNumeroBilheteBySorteioId(sorteio.getId())
            .map(max -> max + 1)
            .orElse(1);

        // Gerar um bilhete por diária
        for (int i = 0; i < quantidadeDiarias; i++) {
            BilheteSorteio bilhete = new BilheteSorteio();
            bilhete.setSorteio(sorteio);
            bilhete.setHospedagemHospede(hospedagemHospede);
            bilhete.setNumeroBilhete(proximoNumero + i);
            bilhete.setQuantidadeDiarias(quantidadeDiarias);
            bilhete.setDataEmissao(LocalDateTime.now());

            bilhetesGerados.add(bilheteSorteioRepository.save(bilhete));
        }

        System.out.println("✅ " + quantidadeDiarias + " bilhete(s) gerado(s) para: " +
            hospedagemHospede.getCliente().getNome());

        return bilhetesGerados;
    }

    // ============================================
    // ✅ REALIZAR SORTEIO
    // ============================================

    @Transactional
    public BilheteSorteio realizarSorteio(Long sorteioId) {
        Sorteio sorteio = sorteioRepository.findById(sorteioId)
            .orElseThrow(() -> new RuntimeException("Sorteio não encontrado"));

        if (sorteio.getStatus() == Sorteio.StatusEnum.REALIZADA) {
            throw new RuntimeException("Sorteio já foi realizado!");
        }

        List<BilheteSorteio> todos = bilheteSorteioRepository
            .findBySorteioIdOrdenado(sorteioId);

        if (todos.isEmpty()) {
            throw new RuntimeException("Nenhum bilhete cadastrado para este sorteio");
        }

        // Sortear aleatoriamente
        int indice = (int) (Math.random() * todos.size());
        BilheteSorteio vencedor = todos.get(indice);

        // Marcar sorteio como realizado
        sorteio.setStatus(Sorteio.StatusEnum.REALIZADA);
        sorteioRepository.save(sorteio);

        System.out.println("🎉 SORTEIO REALIZADO!");
        System.out.println("   Bilhete vencedor: #" + vencedor.getNumeroBilhete());
        System.out.println("   Hóspede: " + vencedor.getHospedagemHospede().getCliente().getNome());

        return vencedor;
    }

    // ============================================
    // ✅ MÉTODOS AUXILIARES
    // ============================================

    private int calcularDiariasHospede(HospedagemHospede hospedagemHospede) {
        LocalDateTime entrada = hospedagemHospede.getDataHoraEntrada();
        LocalDateTime saida = hospedagemHospede.getDataHoraSaida() != null
            ? hospedagemHospede.getDataHoraSaida()
            : LocalDateTime.now();

        if (entrada == null) return 0;

        // ✅ SE FEZ CHECKOUT NO MESMO DIA OU MENOS DE 1 DIA — USA DIÁRIAS DA RESERVA
        long dias = ChronoUnit.DAYS.between(entrada.toLocalDate(), saida.toLocalDate());
        
        if (dias <= 0) {
            Reserva reserva = hospedagemHospede.getReserva();
            if (reserva != null && reserva.getQuantidadeDiaria() > 0) {
                System.out.println("📅 Checkout no mesmo dia — usando diárias da reserva: " + reserva.getQuantidadeDiaria());
                return reserva.getQuantidadeDiaria();
            }
            return 1;
        }

        return (int) dias;
    }

    public List<BilheteSorteio> listarBilhetesPorSorteio(Long sorteioId) {
        return bilheteSorteioRepository.findBySorteioIdOrdenado(sorteioId);
    }

    public long contarBilhetesPorSorteio(Long sorteioId) {
        return bilheteSorteioRepository.countBySorteioId(sorteioId);
    }
}
