package com.divan.service;

import com.divan.dto.ValeRequest;
import com.divan.dto.ValeResponse;
import com.divan.entity.Cliente;
import com.divan.entity.Vale;


import com.divan.repository.ClienteRepository;
import com.divan.repository.UsuarioRepository;
import com.divan.repository.ValeRepository;

import org.springframework.beans.factory.annotation.Autowired;
//import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ValeService {
	
	
	
	@Autowired
    private ValeRepository valeRepository;
	
	@Autowired
    private ClienteRepository clienteRepository;

    public List<ValeResponse> listarTodas() {
        return valeRepository.findAll().stream()
                .map(ValeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ValeResponse buscarPorId(Long id) {
        Vale vale = valeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vale não encontrado"));
        return ValeResponse.fromEntity(vale);
    }

    public List<ValeResponse> listarPorCliente(Long clienteId) {
        return valeRepository.findByClienteIdOrderByDataConcessaoDesc(clienteId).stream()
                .map(ValeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ValeResponse> listarPorStatus(Vale.StatusVale status) {
        return valeRepository.findByStatusOrderByDataVencimentoAsc(status).stream()
                .map(ValeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ValeResponse> listarValesPendentes() {
        return listarPorStatus(Vale.StatusVale.PENDENTE);
    }

    public List<ValeResponse> listarValesVencidos() {
        LocalDate hoje = LocalDate.now();
        return valeRepository.findValesVencidos(hoje).stream()
                .map(ValeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public BigDecimal calcularTotalPendentePorCliente(Long clienteId) {
        return valeRepository.calcularTotalPendentePorCliente(clienteId);
    }

    @Transactional
    public ValeResponse criar(ValeRequest request) {
        // Validar cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Validar datas
        if (request.getDataVencimento().isBefore(request.getDataConcessao())) {
            throw new RuntimeException("Data de vencimento não pode ser anterior à data de concessão");
        }

        // Criar vale
        Vale vale = new Vale();
        vale.setCliente(cliente);
        vale.setDataConcessao(request.getDataConcessao());
        vale.setDataVencimento(request.getDataVencimento());
        vale.setTipoVale(request.getTipoVale());
        vale.setValor(request.getValor());
        vale.setObservacao(request.getObservacao());
        vale.setStatus(Vale.StatusVale.PENDENTE);

        Vale salvo = valeRepository.save(vale);
        return ValeResponse.fromEntity(salvo);
    }

    @Transactional
    public ValeResponse atualizar(Long id, ValeRequest request) {
        Vale vale = valeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vale não encontrado"));

        // Só permite editar se estiver PENDENTE
        if (vale.getStatus() != Vale.StatusVale.PENDENTE) {
            throw new RuntimeException("Só é possível editar vales com status PENDENTE");
        }

        // Validar cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Validar datas
        if (request.getDataVencimento().isBefore(request.getDataConcessao())) {
            throw new RuntimeException("Data de vencimento não pode ser anterior à data de concessão");
        }

        vale.setCliente(cliente);
        vale.setDataConcessao(request.getDataConcessao());
        vale.setDataVencimento(request.getDataVencimento());
        vale.setTipoVale(request.getTipoVale());
        vale.setValor(request.getValor());
        vale.setObservacao(request.getObservacao());

        Vale atualizado = valeRepository.save(vale);
        return ValeResponse.fromEntity(atualizado);
    }

    @Transactional
    public ValeResponse marcarComoPago(Long id) {
        Vale vale = valeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vale não encontrado"));

        if (vale.getStatus() == Vale.StatusVale.PAGO) {
            throw new RuntimeException("Vale já está marcado como pago");
        }

        if (vale.getStatus() == Vale.StatusVale.CANCELADO) {
            throw new RuntimeException("Não é possível pagar um vale cancelado");
        }

        vale.setStatus(Vale.StatusVale.PAGO);
        vale.setDataPagamento(LocalDate.now());

        Vale atualizado = valeRepository.save(vale);
        return ValeResponse.fromEntity(atualizado);
    }

    @Transactional
    public ValeResponse cancelar(Long id, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new RuntimeException("É necessário informar o motivo do cancelamento");
        }

        Vale vale = valeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vale não encontrado"));

        if (vale.getStatus() == Vale.StatusVale.PAGO) {
            throw new RuntimeException("Não é possível cancelar um vale já pago");
        }

        if (vale.getStatus() == Vale.StatusVale.CANCELADO) {
            throw new RuntimeException("Vale já está cancelado");
        }

        vale.setStatus(Vale.StatusVale.CANCELADO);
        vale.setMotivoCancelamento(motivo);

        Vale atualizado = valeRepository.save(vale);
        return ValeResponse.fromEntity(atualizado);
    }

    @Transactional
    public void excluir(Long id) {
        Vale vale = valeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vale não encontrado"));

        // Só permite excluir se estiver CANCELADO
        if (vale.getStatus() != Vale.StatusVale.CANCELADO) {
            throw new RuntimeException("Só é possível excluir vales cancelados");
        }

        valeRepository.delete(vale);
    }

    @Transactional
    public void atualizarValesVencidos() {
        LocalDate hoje = LocalDate.now();
        List<Vale> valesVencidos = valeRepository.findValesVencidos(hoje);

        for (Vale vale : valesVencidos) {
            vale.setStatus(Vale.StatusVale.VENCIDO);
        }

        valeRepository.saveAll(valesVencidos);
    }
}
