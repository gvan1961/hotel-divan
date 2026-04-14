package com.divan.repository;

import com.divan.entity.BilheteSorteio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BilheteSorteioRepository extends JpaRepository<BilheteSorteio, Long> {

    List<BilheteSorteio> findBySorteioId(Long sorteioId);

    List<BilheteSorteio> findByHospedagemHospedeId(Long hospedagemHospedeId);

    boolean existsByHospedagemHospedeIdAndSorteioId(Long hospedagemHospedeId, Long sorteioId);

    @Query("SELECT MAX(b.numeroBilhete) FROM BilheteSorteio b WHERE b.sorteio.id = :sorteioId")
    Optional<Integer> findMaxNumeroBilheteBySorteioId(@Param("sorteioId") Long sorteioId);

    @Query("SELECT b FROM BilheteSorteio b WHERE b.sorteio.id = :sorteioId ORDER BY b.numeroBilhete")
    List<BilheteSorteio> findBySorteioIdOrdenado(@Param("sorteioId") Long sorteioId);

    long countBySorteioId(Long sorteioId);
}
