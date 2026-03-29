package com.divan.config;

import com.divan.entity.Perfil;
import com.divan.entity.Permissao;
import com.divan.entity.Usuario;
import com.divan.repository.PerfilRepository;
import com.divan.repository.PermissaoRepository;
import com.divan.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private PermissaoRepository permissaoRepository;
    
    @Autowired
    private PerfilRepository perfilRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        criarPermissoes();
        criarPerfis();
        criarUsuarioAdmin();
    }
    
    private void criarPermissoes() {
        String[][] permissoes = {
            // APARTAMENTOS
            {"APARTAMENTO_CREATE", "Criar apartamentos", "APARTAMENTOS"},
            {"APARTAMENTO_READ", "Visualizar apartamentos", "APARTAMENTOS"},
            {"APARTAMENTO_UPDATE", "Atualizar apartamentos", "APARTAMENTOS"},
            {"APARTAMENTO_DELETE", "Deletar apartamentos", "APARTAMENTOS"},
            
            // CLIENTES
            {"CLIENTE_CREATE", "Criar clientes", "CLIENTES"},
            {"CLIENTE_READ", "Visualizar clientes", "CLIENTES"},
            {"CLIENTE_UPDATE", "Atualizar clientes", "CLIENTES"},
            {"CLIENTE_DELETE", "Deletar clientes", "CLIENTES"},
            
            // EMPRESAS
            {"EMPRESA_CREATE", "Criar empresas", "EMPRESAS"},
            {"EMPRESA_READ", "Visualizar empresas", "EMPRESAS"},
            {"EMPRESA_UPDATE", "Atualizar empresas", "EMPRESAS"},
            {"EMPRESA_DELETE", "Deletar empresas", "EMPRESAS"},
            
            // RESERVAS
            {"RESERVA_CREATE", "Criar reservas", "RESERVAS"},
            {"RESERVA_READ", "Visualizar reservas", "RESERVAS"},
            {"RESERVA_UPDATE", "Atualizar reservas", "RESERVAS"},
            {"RESERVA_CANCEL", "Cancelar reservas", "RESERVAS"},
            {"RESERVA_FINALIZE", "Finalizar reservas", "RESERVAS"},
            
            // PRODUTOS
            {"PRODUTO_CREATE", "Criar produtos", "PRODUTOS"},
            {"PRODUTO_READ", "Visualizar produtos", "PRODUTOS"},
            {"PRODUTO_UPDATE", "Atualizar produtos", "PRODUTOS"},
            {"PRODUTO_DELETE", "Deletar produtos", "PRODUTOS"},
            
            // VENDAS
            {"VENDA_CREATE", "Registrar vendas", "VENDAS"},
            {"VENDA_READ", "Visualizar vendas", "VENDAS"},
            
            // PAGAMENTOS
            {"PAGAMENTO_CREATE", "Registrar pagamentos", "PAGAMENTOS"},
            {"PAGAMENTO_READ", "Visualizar pagamentos", "PAGAMENTOS"},
            
            // DIÁRIAS
            {"DIARIA_CREATE", "Criar diárias", "DIARIAS"},
            {"DIARIA_READ", "Visualizar diárias", "DIARIAS"},
            {"DIARIA_UPDATE", "Atualizar diárias", "DIARIAS"},
            {"DIARIA_DELETE", "Deletar diárias", "DIARIAS"},
            
            // RELATÓRIOS
            {"RELATORIO_READ", "Visualizar relatórios", "RELATORIOS"},
            {"RELATORIO_DASHBOARD", "Visualizar dashboard", "RELATORIOS"},
            
            // EXTRATOS
            {"EXTRATO_READ", "Visualizar extratos", "EXTRATOS"},
        };
        
        for (String[] perm : permissoes) {
            if (!permissaoRepository.existsByNome(perm[0])) {
                Permissao permissao = new Permissao();
                permissao.setNome(perm[0]);
                permissao.setDescricao(perm[1]);
                permissao.setCategoria(perm[2]);
                permissaoRepository.save(permissao);
            }
        }
        
        System.out.println("✓ Permissões inicializadas");
    }
    
    private void criarPerfis() {
        // ========== PERFIL ADMIN ==========
        if (!perfilRepository.existsByNome("ADMIN")) {
            Perfil admin = new Perfil();
            admin.setNome("ADMIN");
            admin.setDescricao("Administrador do sistema com todas as permissões");
            
            // ✅ Converter List para HashSet
            admin.setPermissoes(new HashSet<>(permissaoRepository.findAll()));
            
            perfilRepository.save(admin);
            System.out.println("✓ Perfil ADMIN criado");
        }
        
        // ========== PERFIL GERENTE ==========
        if (!perfilRepository.existsByNome("GERENTE")) {
            Perfil gerente = new Perfil();
            gerente.setNome("GERENTE");
            gerente.setDescricao("Gerente com acesso a relatórios e visualização completa");
            
            Set<Permissao> permissoesGerente = new HashSet<>();
            permissoesGerente.addAll(permissaoRepository.findByCategoria("RELATORIOS"));
            permissoesGerente.addAll(permissaoRepository.findByCategoria("EXTRATOS"));
            
            // Adicionar permissões específicas
            adicionarPermissao(permissoesGerente, "APARTAMENTO_READ");
            adicionarPermissao(permissoesGerente, "CLIENTE_READ");
            adicionarPermissao(permissoesGerente, "RESERVA_READ");
            adicionarPermissao(permissoesGerente, "PRODUTO_READ");
            adicionarPermissao(permissoesGerente, "VENDA_READ");
            adicionarPermissao(permissoesGerente, "PAGAMENTO_READ");
            
            gerente.setPermissoes(permissoesGerente);
            perfilRepository.save(gerente);
            System.out.println("✓ Perfil GERENTE criado");
        }
        
        // ========== PERFIL RECEPCIONISTA ==========
        if (!perfilRepository.existsByNome("RECEPCIONISTA")) {
            Perfil recepcionista = new Perfil();
            recepcionista.setNome("RECEPCIONISTA");
            recepcionista.setDescricao("Recepcionista com permissões para reservas e clientes");
            
            Set<Permissao> permissoesRecepcionista = new HashSet<>();
            
            adicionarPermissao(permissoesRecepcionista, "CLIENTE_CREATE");
            adicionarPermissao(permissoesRecepcionista, "CLIENTE_READ");
            adicionarPermissao(permissoesRecepcionista, "CLIENTE_UPDATE");
            adicionarPermissao(permissoesRecepcionista, "RESERVA_CREATE");
            adicionarPermissao(permissoesRecepcionista, "RESERVA_READ");
            adicionarPermissao(permissoesRecepcionista, "RESERVA_UPDATE");
            adicionarPermissao(permissoesRecepcionista, "APARTAMENTO_READ");
            adicionarPermissao(permissoesRecepcionista, "PAGAMENTO_CREATE");
            adicionarPermissao(permissoesRecepcionista, "PAGAMENTO_READ");
            adicionarPermissao(permissoesRecepcionista, "EXTRATO_READ");
            
            recepcionista.setPermissoes(permissoesRecepcionista);
            perfilRepository.save(recepcionista);
            System.out.println("✓ Perfil RECEPCIONISTA criado");
        }
        
        // ========== PERFIL VENDEDOR ==========
        if (!perfilRepository.existsByNome("VENDEDOR")) {
            Perfil vendedor = new Perfil();
            vendedor.setNome("VENDEDOR");
            vendedor.setDescricao("Vendedor com permissões para produtos e vendas");
            
            Set<Permissao> permissoesVendedor = new HashSet<>();
            
            adicionarPermissao(permissoesVendedor, "PRODUTO_READ");
            adicionarPermissao(permissoesVendedor, "VENDA_CREATE");
            adicionarPermissao(permissoesVendedor, "VENDA_READ");
            adicionarPermissao(permissoesVendedor, "RESERVA_READ");
            
            vendedor.setPermissoes(permissoesVendedor);
            perfilRepository.save(vendedor);
            System.out.println("✓ Perfil VENDEDOR criado");
        }
    }
    
    /**
     * Método auxiliar para adicionar permissão ao Set
     */
    private void adicionarPermissao(Set<Permissao> set, String nomePermissao) {
        permissaoRepository.findByNome(nomePermissao).ifPresent(set::add);
    }
    
    private void criarUsuarioAdmin() {
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setUsername("admin");
            admin.setEmail("admin@divan.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setAtivo(true);
            
            Perfil perfilAdmin = perfilRepository.findByNome("ADMIN").orElse(null);
            if (perfilAdmin != null) {
                // ✅ Converter List para HashSet se Usuario.perfis for Set
                // Se for List, manter Arrays.asList            
                admin.setPerfis(new HashSet<>(Arrays.asList(perfilAdmin)));

            }
            
            usuarioRepository.save(admin);
            System.out.println("✓ Usuário admin criado - Username: admin | Senha: admin123");
        }
    }
}