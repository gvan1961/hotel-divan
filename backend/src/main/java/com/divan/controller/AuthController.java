package com.divan.controller;

import com.divan.dto.LoginRequestDTO;
import com.divan.dto.LoginResponseDTO;
import com.divan.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            System.out.println("📨 ========== LOGIN REQUEST ==========");
            System.out.println("📨 Username recebido: " + loginRequest.getUsername());
            System.out.println("📨 Password recebido: " + (loginRequest.getPassword() != null ? "***" : "NULL"));
            
            LoginResponseDTO response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ ERRO NO CONTROLLER:");
            System.out.println("❌ Tipo: " + e.getClass().getName());
            System.out.println("❌ Mensagem: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body("Credenciais inválidas: " + e.getMessage());
        }
    }
    
    @GetMapping("/testar-senha")
    public ResponseEntity<String> testarSenha() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String senhaPlana = "123456";
        String hashDoBanco = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjJ8TT5XhGBjE5E5xJz0jQT5JqKp4S2";
        
        boolean match = encoder.matches(senhaPlana, hashDoBanco);
        
        String resultado = "Senha: " + senhaPlana + "\n" +
                          "Hash: " + hashDoBanco + "\n" +
                          "Match: " + match;
        
        System.out.println("🧪 TESTE BCRYPT:");
        System.out.println(resultado);
        
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/gerar-hash-novo")
    public ResponseEntity<String> gerarHashNovo() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String senhaPlana = "123456";
        String hashGerado = encoder.encode(senhaPlana);
        
        String resultado = "Senha: " + senhaPlana + "\n" +
                          "Hash gerado: " + hashGerado + "\n" +
                          "Tamanho: " + hashGerado.length() + "\n\n" +
                          "Execute no MySQL:\n" +
                          "UPDATE usuarios SET password = '" + hashGerado + "' WHERE username = 'admin';";
        
        System.out.println("🔑 NOVO HASH GERADO:");
        System.out.println(hashGerado);
        
        return ResponseEntity.ok(resultado);
    }
}
