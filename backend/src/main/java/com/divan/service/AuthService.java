package com.divan.service;

import com.divan.dto.LoginRequestDTO;
import com.divan.dto.LoginResponseDTO;
import com.divan.entity.Permissao;
import com.divan.entity.Perfil;
import com.divan.entity.Usuario;
import com.divan.repository.UsuarioRepository;
import com.divan.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        System.out.println("\n🔵 ========== INÍCIO DO LOGIN ==========");
        System.out.println("📝 Username recebido: " + loginRequest.getUsername());
        System.out.println("📝 Password recebido: " + (loginRequest.getPassword() != null ? "****" : "NULL"));
        
        try {
            // Autenticar
            System.out.println("🔐 Tentando autenticar...");
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            System.out.println("✅ Autenticação realizada com sucesso");

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Gerar token JWT
            String jwt = jwtUtil.generateToken(authentication);
            System.out.println("🎫 Token JWT gerado: " + jwt.substring(0, 50) + "...");

            // Buscar usuário completo do banco
            Usuario usuario = usuarioRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            System.out.println("👤 Usuário carregado do banco: " + usuario.getUsername());

            // ==================== DEBUG DETALHADO ====================
            System.out.println("\n📊 ========== ANÁLISE DE PERMISSÕES ==========");
            System.out.println("📋 Total de perfis do usuário: " + usuario.getPerfis().size());
            
            int totalPermissoesPerfis = 0;
            for (Perfil perfil : usuario.getPerfis()) {
                System.out.println("\n  🎭 PERFIL: " + perfil.getNome());
                System.out.println("  📝 Descrição: " + perfil.getDescricao());
                System.out.println("  🔑 Permissões neste perfil: " + perfil.getPermissoes().size());
                
                if (perfil.getPermissoes().isEmpty()) {
                    System.out.println("  ⚠️  ATENÇÃO: Este perfil NÃO tem permissões!");
                } else {
                    for (Permissao perm : perfil.getPermissoes()) {
                        System.out.println("    ✓ " + perm.getNome() + " (" + perm.getCategoria() + ")");
                        totalPermissoesPerfis++;
                    }
                }
            }
            
            System.out.println("\n🔐 Permissões diretas do usuário: " + usuario.getPermissoes().size());
            if (!usuario.getPermissoes().isEmpty()) {
                for (Permissao perm : usuario.getPermissoes()) {
                    System.out.println("  ✓ " + perm.getNome() + " (" + perm.getCategoria() + ")");
                }
            }
            
            System.out.println("\n📈 RESUMO:");
            System.out.println("  • Permissões dos perfis: " + totalPermissoesPerfis);
            System.out.println("  • Permissões diretas: " + usuario.getPermissoes().size());
            System.out.println("  • TOTAL ESPERADO: " + (totalPermissoesPerfis + usuario.getPermissoes().size()));
            
            // Obter authorities do Spring Security
            Collection<? extends GrantedAuthority> authorities = usuario.getAuthorities();
            System.out.println("\n⚡ Authorities carregadas pelo Spring Security: " + authorities.size());
            System.out.println("📜 Lista completa de Authorities:");
            for (GrantedAuthority auth : authorities) {
                System.out.println("  → " + auth.getAuthority());
            }
            
            // ==================== MONTAR RESPONSE ====================
            
            // Coletar perfis
            List<String> perfis = usuario.getPerfis().stream()
                .map(Perfil::getNome)
                .collect(Collectors.toList());
            System.out.println("\n📋 Perfis para o response: " + perfis);

            // Coletar permissões (removendo prefixo ROLE_)
            List<String> permissoes = usuario.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(auth -> !auth.startsWith("ROLE_"))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

            System.out.println("\n📤 ========== DADOS DO RESPONSE ==========");
            System.out.println("🆔 ID: " + usuario.getId());
            System.out.println("👤 Username: " + usuario.getUsername());
            System.out.println("📧 Email: " + usuario.getEmail());
            System.out.println("🎭 Perfis: " + perfis);
            System.out.println("🔑 Permissões enviadas: " + permissoes.size());
            System.out.println("📋 Lista de permissões:");
            for (String perm : permissoes) {
                System.out.println("  ✓ " + perm);
            }

            // Atualizar último acesso
            usuario.setUltimoAcesso(LocalDateTime.now());
            usuarioRepository.save(usuario);
            System.out.println("⏰ Último acesso atualizado");

            // Montar response
            LoginResponseDTO response = new LoginResponseDTO();
            response.setToken(jwt);
            response.setId(usuario.getId());
            response.setUsername(usuario.getUsername());
            response.setNome(usuario.getNome());
            response.setEmail(usuario.getEmail());
            response.setPerfis(perfis);
            response.setPermissoes(permissoes);

            System.out.println("\n✅ ========== LOGIN FINALIZADO COM SUCESSO ==========\n");

            return response;
            
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            System.out.println("❌ ERRO: SENHA INCORRETA!");
            System.out.println("❌ Mensagem: " + e.getMessage());
            throw new RuntimeException("Usuário ou senha incorretos");
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            System.out.println("❌ ERRO: USUÁRIO NÃO ENCONTRADO!");
            System.out.println("❌ Mensagem: " + e.getMessage());
            throw new RuntimeException("Usuário não encontrado");
        } catch (Exception e) {
            System.out.println("❌ ERRO INESPERADO:");
            System.out.println("❌ Tipo: " + e.getClass().getName());
            System.out.println("❌ Mensagem: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao processar login: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado"));
    }
}