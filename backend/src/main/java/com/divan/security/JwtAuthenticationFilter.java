package com.divan.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    
    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        System.out.println("🔍 Requisição: " + method + " " + path);
        
        // ✅ VERIFICAR SE É ENDPOINT PÚBLICO (PULAR AUTENTICAÇÃO)
        if (isPublicEndpoint(path, method)) {
            System.out.println("🌐 Endpoint público - pulando autenticação");
            filterChain.doFilter(request, response);
            return;
        }
        
        String jwt = getJwtFromRequest(request);
        System.out.println("🎫 JWT recebido: " + (jwt != null ? "SIM (" + jwt.substring(0, 20) + "...)" : "NÃO"));
        
        if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
            String username = jwtUtil.getUsernameFromToken(jwt);
            System.out.println("👤 Usuário do token: " + username);
            
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            System.out.println("🔑 Authorities: " + userDetails.getAuthorities());
            
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("✅ Autenticação configurada");
        } else {
            System.out.println("❌ Token inválido ou ausente");
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * ✅ VERIFICA SE O ENDPOINT É PÚBLICO (NÃO PRECISA DE JWT)
     */
    private boolean isPublicEndpoint(String path, String method) {
        // Auth
        if (path.startsWith("/api/auth/")) return true;
        if (path.startsWith("/api/public/")) return true;
        
        // Hospedagem
        if (path.startsWith("/api/hospedagem-hospedes/")) return true;
        
        // ✅ RELATÓRIOS DE CAIXA (GET apenas)
        if ("GET".equals(method) && path.matches("/api/fechamento-caixa/\\d+/relatorio")) return true;
        if ("GET".equals(method) && path.matches("/api/fechamento-caixa/\\d+/imprimir")) return true;
        
        // Jantar
        if (path.equals("/api/jantar/relatorio-impressao")) return true;
        
        // Checkout parcial
        if ("POST".equals(method) && path.matches("/api/reservas/\\d+/checkout-parcial")) return true;
        
        // Outros
        if (path.equals("/favicon.ico")) return true;
        if ("OPTIONS".equals(method)) return true;
        
        return false;
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        System.out.println("📋 Header Authorization: " + bearerToken);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}