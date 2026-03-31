package com.divan.service;

import com.divan.entity.Produto;
import com.divan.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.divan.dto.ProdutoRequestDTO;
import com.divan.repository.CategoriaRepository;
import com.divan.repository.ItemVendaRepository;
import com.divan.entity.Categoria;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProdutoService {
	
	@Autowired
	private ItemVendaRepository itemVendaRepository;
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }
    
    public Produto atualizar(Long id, Produto produto) {
        Optional<Produto> produtoExistente = produtoRepository.findById(id);
        if (produtoExistente.isEmpty()) {
            throw new RuntimeException("Produto não encontrado");
        }
        
        produto.setId(id);
        return produtoRepository.save(produto);
    }
    
    public List<Produto> listarDisponiveis() {
        return produtoRepository.findProdutosDisponiveis();
    }
    
    public Produto atualizarEstoque(Long id, Integer novaQuantidade) {
        Optional<Produto> produtoOpt = produtoRepository.findById(id);
        if (produtoOpt.isEmpty()) {
            throw new RuntimeException("Produto não encontrado");
        }
        
        Produto produto = produtoOpt.get();
        produto.setQuantidade(novaQuantidade);
        return produtoRepository.save(produto);
    }
    
    public void baixarEstoque(Long produtoId, Integer quantidade) {
        Optional<Produto> produtoOpt = produtoRepository.findById(produtoId);
        if (produtoOpt.isEmpty()) {
            throw new RuntimeException("Produto não encontrado");
        }
        
        Produto produto = produtoOpt.get();
        if (produto.getQuantidade() < quantidade) {
            throw new RuntimeException("Estoque insuficiente");
        }
        
        produto.setQuantidade(produto.getQuantidade() - quantidade);
        produtoRepository.save(produto);
    }
    
    @Transactional(readOnly = true)
    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepository.findAllOrderByNome();
    }
    
    @Transactional(readOnly = true)
    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeProdutoContainingIgnoreCase(nome);
    }
    
    @Transactional(readOnly = true)
    public List<Produto> buscarComEstoqueBaixo() {
        return produtoRepository.findProdutosComEstoqueBaixo();
    }
    
    @Transactional(readOnly = true)
    public List<Produto> buscarSemEstoque() {
        return produtoRepository.findProdutosSemEstoque();
    }
    
    public Produto salvarComDTO(ProdutoRequestDTO dto) {
        System.out.println("════════════════════════════════════");
        System.out.println("💾 SALVANDO PRODUTO COM DTO");
        System.out.println("   Nome: " + dto.getNomeProduto());
        System.out.println("   Categoria ID: " + dto.getCategoriaId());
        System.out.println("════════════════════════════════════");
        
        Produto produto = new Produto();
        produto.setNomeProduto(dto.getNomeProduto());
        produto.setQuantidade(dto.getQuantidade());
        produto.setValorVenda(dto.getValorVenda());
        produto.setValorCompra(dto.getValorCompra());
        produto.setCodigoBarras(dto.getCodigoBarras());
        
        
        // ✅ BUSCAR E SETAR A CATEGORIA
        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
            produto.setCategoria(categoria);
        }
        
        Produto salvo = produtoRepository.save(produto);
        System.out.println("✅ Produto salvo com ID: " + salvo.getId());
        
        return salvo;
    }

    public Produto atualizarComDTO(Long id, ProdutoRequestDTO dto) {
        System.out.println("════════════════════════════════════");
        System.out.println("🔄 ATUALIZANDO PRODUTO COM DTO");
        System.out.println("   ID: " + id);
        System.out.println("   Nome: " + dto.getNomeProduto());
        System.out.println("   Categoria ID: " + dto.getCategoriaId());
        System.out.println("════════════════════════════════════");
        
        Produto produto = produtoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        
        produto.setNomeProduto(dto.getNomeProduto());
        produto.setQuantidade(dto.getQuantidade());
        produto.setValorVenda(dto.getValorVenda());
        produto.setValorCompra(dto.getValorCompra());
        produto.setCodigoBarras(dto.getCodigoBarras());
        
        // ✅ BUSCAR E SETAR A CATEGORIA
        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
            produto.setCategoria(categoria);
        } else {
            produto.setCategoria(null);
        }
        
        Produto atualizado = produtoRepository.save(produto);
        System.out.println("✅ Produto atualizado com sucesso");
        
        return atualizado;
    }
    
    public List<Produto> buscarPorCodigoBarras(String codigo) {
        return produtoRepository.findByCodigoBarrasContaining(codigo);
    }
    
    public void deletar(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new RuntimeException("Produto não encontrado");
        }
        
        // Verificar se tem itens de venda vinculados
        if (itemVendaRepository.existsByProdutoId(id)) {
            throw new RuntimeException("Produto possui vendas registradas e não pode ser excluído");
        }
        
        produtoRepository.deleteById(id);
    }
}
