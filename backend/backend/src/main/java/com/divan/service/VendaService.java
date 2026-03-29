package com.divan.service;

import com.divan.entity.NotaVenda;
import com.divan.entity.Reserva;
import com.divan.repository.NotaVendaRepository;
import com.divan.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VendaService {
    
    @Autowired
    private NotaVendaRepository notaVendaRepository;
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    public List<NotaVenda> listarNotasVendaPorReserva(Long reservaId) {
        Optional<Reserva> reserva = reservaRepository.findById(reservaId);
        
        // ✅ CORRIGIDO
        if (reserva.isPresent()) {
            return notaVendaRepository.findByReservaId(reservaId);
        }
        return List.of();
    }
    
    public List<NotaVenda> listarVendasDoDia(LocalDateTime data) {
        LocalDateTime inicioDia = data.toLocalDate().atStartOfDay();
        LocalDateTime fimDia = inicioDia.plusDays(1);
        
        // ✅ CORRIGIDO
        return notaVendaRepository.findByDataHoraVendaBetween(inicioDia, fimDia);
    }
    
    public List<NotaVenda> listarVendasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        // ✅ CORRIGIDO
        return notaVendaRepository.findByDataHoraVendaBetween(inicio, fim);
    }
    
    public List<NotaVenda> listarVendasVistaDelDia(LocalDateTime data) {
        LocalDateTime inicioDia = data.toLocalDate().atStartOfDay();
        LocalDateTime fimDia = inicioDia.plusDays(1);
        
        // ✅ CORRIGIDO
        return notaVendaRepository.findByTipoVendaAndDataHoraVendaBetween(
            NotaVenda.TipoVendaEnum.VISTA, 
            inicioDia, 
            fimDia
        );
    }
}
