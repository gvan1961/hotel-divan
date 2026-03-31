package com.divan.config;

import com.divan.security.CustomUserDetailsService;
import com.divan.security.JwtAuthenticationEntryPoint;
import com.divan.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    
    public SecurityConfig(CustomUserDetailsService userDetailsService, 
                         JwtAuthenticationEntryPoint unauthorizedHandler) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeHttpRequests(auth -> auth
                // ========== ENDPOINTS PÚBLICOS ==========
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/alertas/**").authenticated()
                .requestMatchers("/error").permitAll() 
                // ✅ CRÍTICO: Permitir OPTIONS para CORS Preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Swagger / OpenAPI
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // ========== APARTAMENTOS ==========
                .requestMatchers(HttpMethod.GET, "/api/apartamentos/**").hasAnyAuthority("APARTAMENTO_READ", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/apartamentos").hasAnyAuthority("APARTAMENTO_CREATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/apartamentos/**").hasAnyAuthority("APARTAMENTO_UPDATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/apartamentos/**").hasAnyAuthority("APARTAMENTO_UPDATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/apartamentos/**").hasAnyAuthority("APARTAMENTO_DELETE", "ROLE_ADMIN")
                
                // ========== TIPOS DE APARTAMENTO ==========
                .requestMatchers("/api/tipos-apartamento/**").authenticated()
                
                .requestMatchers("/api/auditoria/**").hasAnyRole("ADMIN", "GERENTE")
                
                // ========== CLIENTES ==========
                .requestMatchers(HttpMethod.GET, "/api/clientes/**").hasAnyAuthority("CLIENTE_READ", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/clientes").hasAnyAuthority("CLIENTE_CREATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/clientes/**").hasAnyAuthority("CLIENTE_UPDATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasAnyAuthority("CLIENTE_DELETE", "ROLE_ADMIN")
                
                // ========== EMPRESAS ==========
                .requestMatchers(HttpMethod.GET, "/api/empresas/**").hasAnyAuthority("EMPRESA_READ", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/empresas").hasAnyAuthority("EMPRESA_CREATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/empresas/**").hasAnyAuthority("EMPRESA_UPDATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/empresas/**").hasAnyAuthority("EMPRESA_DELETE", "ROLE_ADMIN")
                
                // ========== CATEGORIAS ==========
                .requestMatchers(HttpMethod.GET, "/api/categorias/**").hasAnyAuthority("CATEGORIA_READ", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/categorias").hasAnyAuthority("CATEGORIA_CREATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasAnyAuthority("CATEGORIA_UPDATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasAnyAuthority("CATEGORIA_DELETE", "ROLE_ADMIN")
                
                // ========== RESERVAS ==========
                .requestMatchers(HttpMethod.GET, "/api/reservas/**").hasAnyAuthority("RESERVA_READ", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/reservas").hasAnyAuthority("RESERVA_CREATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/reservas/**").hasAnyAuthority("RESERVA_UPDATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/reservas/**").hasAnyAuthority("RESERVA_UPDATE", "ROLE_ADMIN")
                
                // ========== PRODUTOS ==========
                .requestMatchers(HttpMethod.GET, "/api/produtos/**").hasAnyAuthority("PRODUTO_READ", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/produtos").hasAnyAuthority("PRODUTO_CREATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/produtos/**").hasAnyAuthority("PRODUTO_UPDATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/produtos/**").hasAnyAuthority("PRODUTO_UPDATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/produtos/**").hasAnyAuthority("PRODUTO_DELETE", "ROLE_ADMIN")
                
                // ========== VENDAS ==========
                .requestMatchers(HttpMethod.GET, "/api/vendas/**").hasAnyAuthority("VENDA_READ", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/vendas/**").hasAnyAuthority("VENDA_CREATE", "ROLE_ADMIN")
                
                // ========== PAGAMENTOS ==========
                .requestMatchers(HttpMethod.GET, "/api/pagamentos/**").hasAnyAuthority("PAGAMENTO_READ", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/pagamentos").hasAnyAuthority("PAGAMENTO_CREATE", "ROLE_ADMIN")
                
                // ========== DIÁRIAS ==========
                .requestMatchers(HttpMethod.GET, "/api/diarias/**").hasAnyAuthority("DIARIA_READ", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/diarias").hasAnyAuthority("DIARIA_CREATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/diarias/**").hasAnyAuthority("DIARIA_UPDATE", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/diarias/**").hasAnyAuthority("DIARIA_DELETE", "ROLE_ADMIN")
                
                
                // ========== RELATÓRIOS ==========
                .requestMatchers("/api/relatorios/**").hasAnyAuthority("RELATORIO_READ", "ROLE_ADMIN", "ROLE_GERENTE")
                
                // ========== EXTRATOS ==========
                .requestMatchers("/api/extratos/**").hasAnyAuthority("EXTRATO_READ", "ROLE_ADMIN")
                
                // ========== GESTÃO DE USUÁRIOS ==========
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/api/perfis/**").hasRole("ADMIN")
                .requestMatchers("/api/permissoes/**").hasRole("ADMIN")
                
                .requestMatchers("/api/deposito/**").hasAnyRole("ADMIN", "GERENTE", "RECEPCIONISTA")
                
                
                // ========== QUALQUER OUTRA REQUISIÇÃO ==========
                .anyRequest().authenticated()
            );
        
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    // ❌ REMOVIDO O MÉTODO corsConfigurationSource() - JÁ EXISTE NO CorsConfig.java
}
