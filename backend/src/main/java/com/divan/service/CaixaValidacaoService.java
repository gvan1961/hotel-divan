package com.divan.service;

import com.divan.entity.FechamentoCaixa;
import com.divan.entity.Usuario;
import com.divan.repository.FechamentoCaixaRepository;
import com.divan.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CaixaValidacaoService {
    
    @Autowired
    private FechamentoCaixaRepository fechamentoCaixaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * ✅ VALIDAR SE O USUÁRIO TEM CAIXA ABERTO
     */
    @Transactional(readOnly = true)
    public FechamentoCaixa validarCaixaAberto(Long usuarioId) {
        System.out.println("🔍 Validando caixa aberto para usuário: " + usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        Optional<FechamentoCaixa> caixaOpt = fechamentoCaixaRepository
            .findByUsuarioAndStatus(usuario, FechamentoCaixa.StatusCaixaEnum.ABERTO);
        
        if (caixaOpt.isEmpty()) {
            System.err.println("❌ CAIXA FECHADO! Operação bloqueada.");
            throw new RuntimeException("❌ CAIXA FECHADO! Você precisa abrir o caixa antes de fazer lançamentos.");
        }
        
        FechamentoCaixa caixa = caixaOpt.get();
        System.out.println("✅ Caixa aberto encontrado: #" + caixa.getId());
        
        return caixa;
    }
    
    /**
     * ✅ VERIFICAR SE EXISTE CAIXA ABERTO (SEM LANÇAR ERRO)
     */
    @Transactional(readOnly = true)
    public boolean existeCaixaAberto(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        return fechamentoCaixaRepository
            .findByUsuarioAndStatus(usuario, FechamentoCaixa.StatusCaixaEnum.ABERTO)
            .isPresent();
    }
}
