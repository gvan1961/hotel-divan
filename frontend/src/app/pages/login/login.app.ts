import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-container">
      <div class="login-card" *ngIf="mostrarCard" [class.card-saindo]="saindo">
        <div class="logo">
          <div class="logo-icon">🏨</div>
          <h1>Hotel Di Van</h1>
          <p class="subtitulo">Sistema de Gestão</p>
        </div>

        <form (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label for="username">Usuário</label>
            <input
              type="text"
              id="username"
              [(ngModel)]="username"
              name="username"
              required
              placeholder="Digite seu usuário"
              autofocus
            />
          </div>

          <div class="form-group">
            <label for="password">Senha</label>
            <input
              type="password"
              id="password"
              [(ngModel)]="password"
              name="password"
              required
              placeholder="Digite sua senha"
            />
          </div>

          <div *ngIf="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>

          <button type="submit" [disabled]="loading">
            {{ loading ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>
      </div>
    </div>

    <!-- TELA DE BOAS-VINDAS -->
    <div class="boas-vindas" *ngIf="mostrarBoasVindas">
      <div class="bv-icone">{{ boasVindasEmoji }}</div>
      <p class="bv-saudacao">{{ saudacao }},</p>
      <p class="bv-nome">{{ nomeUsuario }}</p>
      <p class="bv-msg">{{ boasVindasMsg }}</p>
      <div class="bv-barra">
        <div class="bv-barra-fill" [class.bv-animar]="animarBarra"></div>
      </div>
    </div>
  `,
  styles: [`
    /* ===== ANIMAÇÕES ===== */
    @keyframes fadeSlideIn {
      from { opacity: 0; transform: translateY(32px) scale(0.97); }
      to   { opacity: 1; transform: translateY(0) scale(1); }
    }
    @keyframes pulse {
      0%, 100% { box-shadow: 0 0 0 0 rgba(102,126,234,0.4); }
      50%       { box-shadow: 0 0 0 10px rgba(102,126,234,0); }
    }
    @keyframes fadeIn {
      from { opacity: 0; }
      to   { opacity: 1; }
    }
    @keyframes slideDown {
      from { opacity: 0; transform: translateY(-14px); }
      to   { opacity: 1; transform: translateY(0); }
    }
    @keyframes cardSaindo {
      from { opacity: 1; transform: scale(1); }
      to   { opacity: 0; transform: scale(0.95); }
    }

    /* ===== CONTAINER ===== */
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    /* ===== CARD ===== */
    .login-card {
      background: white;
      padding: 2.5rem;
      border-radius: 16px;
      box-shadow: 0 20px 60px rgba(0,0,0,0.25);
      width: 100%;
      max-width: 400px;
      animation: fadeSlideIn 0.55s cubic-bezier(.22,1,.36,1) both;
    }

    .card-saindo {
      animation: cardSaindo 0.4s ease forwards;
    }

    /* ===== LOGO ===== */
    .logo {
      text-align: center;
      margin-bottom: 2rem;
    }

    .logo-icon {
      width: 64px;
      height: 64px;
      border-radius: 16px;
      background: linear-gradient(135deg, #667eea, #764ba2);
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-size: 32px;
      margin-bottom: 0.75rem;
      animation: pulse 2.5s ease-in-out infinite;
    }

    .logo h1 {
      font-size: 22px;
      font-weight: 700;
      color: #1a1a2e;
      margin: 0 0 4px 0;
    }

    .subtitulo {
      font-size: 13px;
      color: #888;
      margin: 0;
    }

    /* ===== FORMULÁRIO ===== */
    .form-group {
      margin-bottom: 1.2rem;
    }

    label {
      display: block;
      margin-bottom: 6px;
      color: #555;
      font-size: 13px;
      font-weight: 500;
    }

    input {
      width: 100%;
      padding: 11px 14px;
      border: 1.5px solid #e0e0e0;
      border-radius: 8px;
      font-size: 14px;
      box-sizing: border-box;
      transition: border-color 0.2s, box-shadow 0.2s;
      outline: none;
      color: #333;
    }

    input:focus {
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102,126,234,0.15);
    }

    button {
      width: 100%;
      padding: 12px;
      background: linear-gradient(135deg, #667eea, #764ba2);
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 15px;
      font-weight: 600;
      cursor: pointer;
      transition: opacity 0.2s, transform 0.15s;
      letter-spacing: 0.3px;
      margin-top: 0.5rem;
    }

    button:hover:not(:disabled) {
      opacity: 0.9;
      transform: translateY(-1px);
    }

    button:active:not(:disabled) {
      transform: scale(0.98);
    }

    button:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .error-message {
      background: #fee;
      color: #c33;
      padding: 10px;
      border-radius: 8px;
      margin-bottom: 15px;
      text-align: center;
      font-size: 13px;
    }

    /* ===== BOAS-VINDAS ===== */
    .boas-vindas {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: white;
      text-align: center;
      z-index: 9999;
      animation: fadeIn 0.4s ease;
    }

    .bv-icone {
      font-size: 64px;
      margin-bottom: 0.5rem;
      animation: slideDown 0.5s 0.1s both;
    }

    .bv-saudacao {
      font-size: 18px;
      opacity: 0.85;
      margin: 0;
      animation: slideDown 0.5s 0.2s both;
    }

    .bv-nome {
      font-size: 32px;
      font-weight: 700;
      margin: 0.4rem 0;
      animation: slideDown 0.5s 0.3s both;
    }

    .bv-msg {
      font-size: 16px;
      opacity: 0.85;
      margin: 0;
      animation: slideDown 0.5s 0.4s both;
    }

    .bv-barra {
      margin-top: 2rem;
      width: 200px;
      height: 4px;
      border-radius: 2px;
      background: rgba(255,255,255,0.25);
      overflow: hidden;
      animation: slideDown 0.5s 0.5s both;
    }

    .bv-barra-fill {
      height: 100%;
      background: white;
      border-radius: 2px;
      width: 0%;
      transition: none;
    }

    .bv-animar {
      width: 100% !important;
      transition: width 2.5s linear !important;
    }
  `]
})
export class LoginApp {
  private authService = inject(AuthService);
  private router = inject(Router);

  username = '';
  password = '';
  loading = false;
  errorMessage = '';
  saindo = false;

  mostrarBoasVindas = false;
  animarBarra = false;
  saudacao = '';
  nomeUsuario = '';
  boasVindasMsg = '';
  boasVindasEmoji = '';

  mostrarCard = false;

  private obterSaudacao(): void {
    const h = new Date().getHours();
    if (h < 12) {
      this.saudacao = 'Bom dia';
      this.boasVindasEmoji = '☀️';
      this.boasVindasMsg = 'Que seja um dia produtivo e cheio de energia!';
    } else if (h < 18) {
      this.saudacao = 'Boa tarde';
      this.boasVindasEmoji = '🌤️';
      this.boasVindasMsg = 'Continue com o ótimo trabalho!';
    } else {
      this.saudacao = 'Boa noite';
      this.boasVindasEmoji = '🌙';
      this.boasVindasMsg = 'Obrigado pela dedicação, bom turno!';
    }
  }

  ngOnInit(): void {
  setTimeout(() => {
    this.mostrarCard = true;
  }, 100);
}

  onSubmit(): void {
    if (!this.username || !this.password) {
      this.errorMessage = 'Preencha todos os campos';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login({ username: this.username, password: this.password })
      .subscribe({
        next: () => {
          const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
          const perfis: string[] = usuario.perfis || [];

          // ✅ Prepara boas-vindas
          this.obterSaudacao();
          this.nomeUsuario = usuario.nome || this.username;

          // ✅ Anima o card saindo
          this.saindo = true;

          setTimeout(() => {
            // ✅ Mostra tela de boas-vindas
            this.mostrarBoasVindas = true;

            setTimeout(() => {
              this.animarBarra = true;
            }, 100);

            // ✅ Navega após 3 segundos
            setTimeout(() => {
              if (perfis.includes('RESTAURANTE') || perfis.includes('COZINHA')) {
                this.router.navigate(['/jantar']);
              } else {
                this.router.navigate(['/painel-recepcao']);
              }
            }, 3000);
          }, 400);
        },
        error: (err) => {
          this.loading = false;
          this.saindo = false;
          this.errorMessage = err.error?.message || 'Usuário ou senha inválidos';
        }
      });
  }
}
