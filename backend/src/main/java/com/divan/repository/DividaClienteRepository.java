package com.divan.repository;
 
import com.divan.entity.DividaCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.List;
 
public interface DividaClienteRepository extends JpaRepository<DividaCliente, Long> {
 
    @Query("SELECT d FROM DividaCliente d JOIN FETCH d.cliente WHERE d.status = :status ORDER BY d.dataRegistro")
    List<DividaCliente> findByStatus(@Param("status") DividaCliente.StatusDivida status);
 
    @Query("SELECT d FROM DividaCliente d WHERE d.cliente.id = :clienteId AND d.status = :status")
    List<DividaCliente> findByClienteIdAndStatus(@Param("clienteId") Long clienteId,
                                                  @Param("status") DividaCliente.StatusDivida status);
}
 
