package com.divan.repository;

import com.divan.entity.Cliente;
import com.divan.entity.Empresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCpf(String cpf);

    List<Cliente> findByNomeContainingIgnoreCase(String nome);

    List<Cliente> findByEmpresa(Empresa empresa);

    List<Cliente> findByEmpresaId(Long empresaId);

    @Query("SELECT c FROM Cliente c WHERE c.nome LIKE %:nome% OR c.cpf LIKE %:cpf%")
    List<Cliente> buscarPorNomeOuCpf(String nome, String cpf);

    boolean existsByCpf(String cpf);

    @Query("SELECT c FROM Cliente c JOIN c.reservas r WHERE r.apartamento.numeroApartamento = :numeroApartamento")
    List<Cliente> findByApartamento(String numeroApartamento);

    @Query(value = """
    	    SELECT c.id, c.nome, c.cpf, c.celular, e.nome_empresa
    	    FROM clientes c
    	    LEFT JOIN empresas e ON c.empresa_id = e.id
    	    WHERE MATCH(c.nome) AGAINST(:termo IN BOOLEAN MODE)
    	    LIMIT 20
    	    """, nativeQuery = true)
    	List<Object[]> buscarPorNomeFull(@Param("termo") String termo);
    	
    @EntityGraph(attributePaths = {"empresa", "responsavel"})
    Page<Cliente> findByNomeContainingIgnoreCaseOrCpfContaining(
        String nome, String cpf, Pageable pageable);
    
    @Query(value = """
    	    SELECT c.id, c.nome, c.cpf, c.celular, e.nome_empresa
    	    FROM clientes c
    	    LEFT JOIN empresas e ON c.empresa_id = e.id
    	    WHERE c.cpf LIKE :cpf
    	       OR MATCH(c.nome) AGAINST(:nome IN BOOLEAN MODE)
    	    LIMIT 20
    	    """, nativeQuery = true)
    	List<Object[]> buscarPorCpfOuNome(@Param("cpf") String cpf, @Param("nome") String nome);
    	
    	@Query(value = """
    		    SELECT c.id, c.nome, c.cpf, c.celular, e.nome_empresa, c.tipo_cliente
    		    FROM clientes c
    		    LEFT JOIN empresas e ON c.empresa_id = e.id
    		    WHERE REPLACE(REPLACE(REPLACE(c.cpf, '.', ''), '-', ''), '/', '') LIKE :cpf
    		    OR MATCH(c.nome) AGAINST (:nome IN BOOLEAN MODE)
    		    LIMIT 50
    		    """, nativeQuery = true)
    		List<Object[]> buscarPorCpfParcialOuNome(@Param("cpf") String cpf, @Param("nome") String nome);
}


