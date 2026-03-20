package com.divan.repository;

import com.divan.entity.HospedagemHospede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HospedagemHospedeRepository extends JpaRepository<HospedagemHospede, Long> {

    List<HospedagemHospede> findByReservaId(Long reservaId);

    List<HospedagemHospede> findByReservaIdAndStatus(Long reservaId, HospedagemHospede.StatusEnum status);

    List<HospedagemHospede> findByClienteId(Long clienteId);
    
    List<HospedagemHospede> findByStatus(HospedagemHospede.StatusEnum status);
    
    List<HospedagemHospede> findByClienteIdAndStatus(
    	    Long clienteId,
    	    HospedagemHospede.StatusEnum status
    	);
    
    List<HospedagemHospede> findByPlacaCarroIgnoreCaseAndStatus(
    	    String placaCarro, HospedagemHospede.StatusEnum status);
}
