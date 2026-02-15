package com.divan.controller;

import com.divan.dto.ComandaConsumoRequest;
import com.divan.dto.ComandaConsumoResponse;
import com.divan.dto.ItemVendaRequestDTO;
import com.divan.dto.ValeResponse;
import com.divan.dto.VendaBalcaoRequest;
import com.divan.dto.VendaBalcaoResponse;
import com.divan.dto.VendaReservaRequestDTO;
import com.divan.entity.ItemVenda;
import com.divan.entity.NotaVenda;
import com.divan.entity.Produto;
import com.divan.entity.Vale;
import com.divan.service.VendaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendas")
@CrossOrigin(origins = "*")
public class VendaController {

    @Autowired
    private VendaService vendaService;

    @GetMapping("/teste")
    public ResponseEntity<String> teste() {
        return ResponseEntity.ok("VendaController funcionando!");
    }

    @PostMapping("/reserva")
    public ResponseEntity<?> adicionarVendaParaReserva(@Valid @RequestBody VendaReservaRequestDTO dto) {
        try {
            List<ItemVenda> itens = new ArrayList<>();

            for (ItemVendaRequestDTO itemDto : dto.getItens()) {
                ItemVenda item = new ItemVenda();
                Produto produto = new Produto();
                produto.setId(itemDto.getProdutoId());
                item.setProduto(produto);
                item.setQuantidade(itemDto.getQuantidade());
                itens.add(item);
            }

            NotaVenda vendaProcessada = vendaService.adicionarVendaParaReserva(dto.getReservaId(), itens);
            return ResponseEntity.status(HttpStatus.CREATED).body(vendaProcessada);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 💵 Realizar venda à vista (balcão)
     */
    @PostMapping("/a-vista")
    public ResponseEntity<?> vendaAVista(@Valid @RequestBody VendaBalcaoRequest request) {
        try {
            System.out.println("═══════════════════════════════════════════");
            System.out.println("🔵 API - Recebida requisição de venda à vista");
            System.out.println("   Itens: " + request.getItens().size());
            System.out.println("   Forma pagamento: " + request.getFormaPagamento());
            System.out.println("═══════════════════════════════════════════");
            
            // ✅ BUSCAR USUÁRIO DO REQUEST OU USAR PADRÃO
            Long usuarioId = request.getUsuarioId() != null ? request.getUsuarioId() : 1L;
            
            VendaBalcaoResponse response = vendaService.realizarVendaAVista(request, usuarioId);
            
            System.out.println("✅ Venda à vista processada com sucesso!");
            System.out.println("   Nota #" + response.getNotaVendaId());
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erro na venda à vista: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(erro);
        }
    }
    
    /**
     * 💳 Realizar venda faturada (a prazo)
     */
    @PostMapping("/faturada")
    public ResponseEntity<?> vendaFaturada(
            @Valid @RequestBody VendaBalcaoRequest request,
            @RequestParam Long usuarioId  // ✅ ADICIONAR ESTE PARÂMETRO
    ) {
        try {
            System.out.println("═══════════════════════════════════════════");
            System.out.println("🔵 API - Recebida requisição de venda faturada");
            System.out.println("   Itens: " + request.getItens().size());
            System.out.println("   Cliente ID: " + request.getClienteId());
            System.out.println("   Usuário ID: " + usuarioId);  // ✅ ADICIONAR LOG
            System.out.println("═══════════════════════════════════════════");

            VendaBalcaoResponse response = vendaService.realizarVendaFaturada(request, usuarioId);  // ✅ PASSAR O USUÁRIO

            System.out.println("✅ Venda faturada processada com sucesso!");
            System.out.println("   Nota #" + response.getNotaVendaId());
            System.out.println("   Cliente: " + response.getClienteNome());
            System.out.println("═══════════════════════════════════════════");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Erro na venda faturada: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(erro);
        }
    }
    
    @PostMapping("/comanda-consumo")
    public ResponseEntity<?> adicionarComandaConsumo(@RequestBody ComandaConsumoRequest request) {
        try {
            System.out.println("═══════════════════════════════════════════");
            System.out.println("🔵 API - Recebida comanda de consumo");
            System.out.println("   Reserva ID: " + request.getReservaId());
            System.out.println("   Itens: " + request.getItens().size());
            System.out.println("═══════════════════════════════════════════");
            
            ComandaConsumoResponse response = vendaService.adicionarComandaConsumo(request);
            
            System.out.println("✅ Comanda processada com sucesso!");
            System.out.println("   Nota #" + response.getNotaVendaId());
            System.out.println("   Apartamento: " + response.getNumeroApartamento());
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Erro na comanda de consumo: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(erro);
        }
    }
    
}
