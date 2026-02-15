package com.divan.repository;

import com.divan.entity.Categoria;
import com.divan.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    
    List<Produto> findByNomeProdutoContainingIgnoreCase(String nome);
    
    List<Produto> findByCategoria(Categoria categoria);
    
    @Query("SELECT p FROM Produto p WHERE p.quantidade <= 10")
    List<Produto> findProdutosComEstoqueBaixo();
    
    @Query("SELECT p FROM Produto p WHERE p.quantidade = 0")
    List<Produto> findProdutosSemEstoque();
    
    @Query("SELECT p FROM Produto p ORDER BY p.nomeProduto")
    List<Produto> findAllOrderByNome();
    
    @Query("SELECT p FROM Produto p WHERE p.quantidade > 0 ORDER BY p.nomeProduto")
    List<Produto> findProdutosDisponiveis();
}
