import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  template: `  
    <header class="app-header no-print" *ngIf="isLogado">
      <div class="header-content">
        <div class="logo">
          <h1>🏨 Hotel Di Van</h1>
        </div>
        
        <div class="user-info">
  <span class="user-greeting">
    @if (fotoUsuario) {
      <img [src]="fotoUsuario" class="foto-usuario" alt="Foto do usuário" />
    } @else {
      👤
    }
    Olá, <strong>{{ nomeUsuario }}</strong>
  </span>
  <a href="/ponto" class="btn-ponto" title="Registrar Ponto">
    ⏱️ Ponto
  </a>
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
    .btn-logout:active { transform: translateY(0); }
    @media (max-width: 768px) {
      .app-header { padding: 12px 15px; }
      .header-content { flex-direction: column; gap: 10px; align-items: stretch; }
      .logo h1 { font-size: 20px; text-align: center; }
      .user-info { justify-content: space-between; }
      .user-greeting { font-size: 13px; padding: 6px 12px; }
      .btn-logout { font-size: 12px; padding: 6px 15px; }
    }
      .btn-ponto {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s;
}
.btn-ponto:hover {
  background: rgba(255, 255, 255, 0.35);
  transform: translateY(-2px);
}

.foto-usuario {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  vertical-align: middle;
  margin-right: 4px;
  border: 1px solid rgba(255,255,255,0.5);
}

  `]
})

export class HeaderComponent implements OnInit, OnDestroy {
  nomeUsuario: string = '';
  fotoUsuario: string | null = null;
  isLogado: boolean = false;
  private sub: Subscription = new Subscription();

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.atualizarHeader();

    // ✅ Atualiza ao navegar entre telas (cobre login/logout/troca de perfil)
    // em vez de um setInterval de 1s que forçava re-render da aplicação inteira.
    this.sub.add(
      this.router.events
        .pipe(filter(event => event instanceof NavigationEnd))
        .subscribe(() => this.atualizarHeader())
    );

    // ✅ Atualiza se o localStorage mudar (ex: foto de perfil trocada em outra aba)
    const onStorage = (event: StorageEvent) => {
      if (event.key === 'usuario' || event.key === 'user' || event.key === null) {
        this.atualizarHeader();
      }
    };
    window.addEventListener('storage', onStorage);
    this.sub.add({ unsubscribe: () => window.removeEventListener('storage', onStorage) });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  atualizarHeader(): void {
    this.isLogado = this.authService.isAuthenticated();

    if (this.isLogado) {
      const usuarioStr = localStorage.getItem('usuario') || localStorage.getItem('user');
      if (usuarioStr) {
        try {
          const usuario = JSON.parse(usuarioStr);
          this.nomeUsuario = usuario.nome || usuario.username || 'Usuário';
          this.fotoUsuario = usuario.fotoBase64 || null;
        } catch (e) {
          this.nomeUsuario = 'Usuário';
          this.fotoUsuario = null;
        }
      } else {
        this.nomeUsuario = this.authService.getUsuarioNome();
        this.fotoUsuario = null;
      }
    }
  }

  sair(): void {
    if (confirm('🚪 Deseja realmente sair do sistema?')) {
      this.authService.logout();
    }
  }
}