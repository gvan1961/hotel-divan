package com.divan.service;

import com.divan.entity.FechamentoCaixa;
import com.divan.repository.FechamentoCaixaRepository;
import com.divan.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Centraliza a busca do caixa aberto do usuário logado no momento.
 * Usado na criação de Pagamento e NotaVenda, pra vincular cada registro
 * ao caixa correto (em vez de inferir isso depois por intervalo de data,
 * que causa duplicação em relatório quando há caixas com período
 * sobreposto).
 */
@Service
public class CaixaContextoService {

    private final FechamentoCaixaRepository caixaRepository;
    private final UsuarioRepository usuarioRepository;

    public CaixaContextoService(FechamentoCaixaRepository caixaRepository, UsuarioRepository usuarioRepository) {
        this.caixaRepository = caixaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Retorna o caixa aberto do usuário autenticado no momento, ou null
     * se não houver nenhum caixa aberto (não deveria acontecer nos pontos
     * onde isso é chamado, já que todos já validam caixaAberto antes —
     * mas retornamos null em vez de lançar exceção aqui, pra não duplicar
     * a validação que já existe em cada endpoint).
     */
    public FechamentoCaixa buscarCaixaAbertoDoUsuarioAtual() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(login)
            .flatMap(usuario -> caixaRepository.findByUsuarioIdAndStatus(
                usuario.getId(), FechamentoCaixa.StatusCaixa.ABERTO))
            .orElse(null);
    }
}
