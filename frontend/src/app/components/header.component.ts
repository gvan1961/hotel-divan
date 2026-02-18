import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  template: `
    <header class="app-header" *ngIf="isLogado">
      <div class="header-content">
        <div class="logo">
          <h1>🏨 Hotel Divan</h1>
        </div>
        
        <div class="user-info">
          <span class="user-greeting">
            👤 Olá, <strong>{{ nomeUsuario }}</strong>
          </span>
          <button class="btn-logout" (click)="sair()" title="Sair do sistema">
            🚪 Sair
          </button>
        </div>
      </div>
    </header>
  `,
  styles: [`
    .app-header {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 15px 30px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.15);
      position: sticky;
      top: 0;
      z-index: 1000;
    }

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      max-width: 1600px;
      margin: 0 auto;
    }

    .logo h1 {
      margin: 0;
      font-size: 24px;
      font-weight: 700;
      cursor: default;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 15px;
    }

    .user-greeting {
      font-size: 14px;
      background: rgba(255, 255, 255, 0.2);
      padding: 8px 15px;
      border-radius: 20px;
      backdrop-filter: blur(10px);
    }

    .user-greeting strong {
      font-weight: 700;
      font-size: 15px;
    }

    .btn-logout {
      background: rgba(255, 255, 255, 0.9);
      color: #667eea;
      border: none;
      padding: 8px 20px;
      border-radius: 20px;
      font-weight: 600;
      font-size: 13px;
      cursor: pointer;
      transition: all 0.3s;
      display: flex;
      align-items: center;
      gap: 5px;
    }

    .btn-logout:hover {
      background: white;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.2);
    }

    .btn-logout:active {
      transform: translateY(0);
    }

    @media (max-width: 768px) {
      .app-header {
        padding: 12px 15px;
      }

      .header-content {
        flex-direction: column;
        gap: 10px;
        align-items: stretch;
      }
      
      .logo h1 {
        font-size: 20px;
        text-align: center;
      }

      .user-info {
        justify-content: space-between;
      }

      .user-greeting {
        font-size: 13px;
        padding: 6px 12px;
      }

      .btn-logout {
        font-size: 12px;
        padding: 6px 15px;
      }
    }
  `]
})
export class HeaderComponent implements OnInit {
  nomeUsuario: string = '';
  isLogado: boolean = false;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.isLogado = this.authService.isAuthenticated();
    
    if (this.isLogado) {
      // ✅ PEGAR DIRETO DO LOCALSTORAGE (MAIS CONFIÁVEL)
      const usuarioStr = localStorage.getItem('usuario') || localStorage.getItem('user');
      
      if (usuarioStr) {
        try {
          const usuario = JSON.parse(usuarioStr);
          this.nomeUsuario = usuario.nome || usuario.username || 'Usuário';
          console.log('👤 Header - Usuário logado:', this.nomeUsuario);
        } catch (e) {
          console.error('❌ Erro ao parsear usuário:', e);
          this.nomeUsuario = 'Usuário';
        }
      } else {
        // FALLBACK: Tentar pelo service
        this.nomeUsuario = this.authService.getUsuarioNome();
      }
    }
  }

  sair(): void {
    if (confirm('🚪 Deseja realmente sair do sistema?')) {
      this.authService.logout();
    }
  }
}