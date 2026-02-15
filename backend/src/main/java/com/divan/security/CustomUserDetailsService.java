package com.divan.security;

import com.divan.entity.Usuario;
import com.divan.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UsuarioRepository usuarioRepository;
    
    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    
    
    
    @Transactional
    public UserDetails loadUserById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> 
                    new UsernameNotFoundException("Usuário não encontrado com id: " + id));
        
        return usuario;
    }
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 CustomUserDetailsService: Buscando usuário: " + username);
        
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("❌ Usuário não encontrado: " + username);
                    return new UsernameNotFoundException("Usuário não encontrado: " + username);
                });
        
        System.out.println("✅ Usuário encontrado: " + usuario.getUsername());
        System.out.println("📧 Email: " + usuario.getEmail());
        System.out.println("🔐 Hash senha (primeiros 30 chars): " + usuario.getPassword().substring(0, 30) + "...");
        System.out.println("✔️ Ativo (campo): " + usuario.getAtivo());
        System.out.println("🔓 isAccountNonLocked(): " + usuario.isAccountNonLocked());
        System.out.println("✅ isEnabled(): " + usuario.isEnabled());
        System.out.println("⏰ isCredentialsNonExpired(): " + usuario.isCredentialsNonExpired());
        System.out.println("🕐 isAccountNonExpired(): " + usuario.isAccountNonExpired());
        
        return usuario;
    }
}
