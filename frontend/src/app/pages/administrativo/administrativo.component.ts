import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HasPermissionDirective } from '../../directives/has-permission.directive';

@Component({
  selector: 'app-administrativo',
  standalone: true,
  imports: [CommonModule, HasPermissionDirective],
  template: `
    <div class="container">
      <div class="header">
        <h1>⚙️ Área Administrativa</h1>
        <p class="subtitle">Acesso restrito — funções de gestão do sistema</p>
      </div>

      <div class="grid">

        <div *hasPermission="'USUARIO_VISUALIZAR'"
             class="card" (click)="ir('/usuarios')">
          <div class="card-icon">👤</div>
          <div class="card-info">
            <h3>Usuários</h3>
            <p>Gerenciar usuários do sistema</p>
          </div>
          <span class="seta">›</span>
        </div>

        <div *hasPermission="'PERFIL_GERENCIAR'"
             class="card" (click)="ir('/perfis')">
          <div class="card-icon">🔐</div>
          <div class="card-info">
            <h3>Perfis</h3>
            <p>Permissões e perfis de acesso</p>
          </div>
          <span class="seta">›</span>
        </div>

        <div *hasPermission="'CONTA_RECEBER_VISUALIZAR'"
             class="card" (click)="ir('/contas-receber')">
          <div class="card-icon">💰</div>
          <div class="card-info">
            <h3>Contas a Receber</h3>
            <p>Controle financeiro de contas</p>
          </div>
          <span class="seta">›</span>
        </div>

        <div *hasPermission="'CAIXA_VISUALIZAR'"
             class="card" (click)="ir('/caixa/consulta')">
          <div class="card-icon">🔍</div>
          <div class="card-info">
            <h3>Consulta de Caixas</h3>
            <p>Histórico e relatórios de caixa</p>
          </div>
          <span class="seta">›</span>
        </div>

        <div *hasPermission="'CAIXA_VISUALIZAR'"
             class="card" (click)="ir('/vales')">
          <div class="card-icon">💵</div>
          <div class="card-info">
            <h3>Vales</h3>
            <p>Vales e descontos de funcionários</p>
          </div>
          <span class="seta">›</span>
        </div>

        <div *hasPermission="'ROLE_ADMIN'"
             class="card" (click)="ir('/auditoria')">
          <div class="card-icon">📋</div>
          <div class="card-info">
            <h3>Log de Auditoria</h3>
            <p>Registro de ações dos funcionários</p>
          </div>
          <span class="seta">›</span>
        </div>

      </div>
    </div>
  `,
  styles: [`
    .container {
      padding: 30px;
      max-width: 900px;
      margin: 0 auto;
    }

    .header {
      margin-bottom: 35px;
    }

    .header h1 {
      margin: 0 0 8px 0;
      color: #2c3e50;
      font-size: 1.8em;
    }

    .subtitle {
      margin: 0;
      color: #7f8c8d;
      font-size: 1em;
    }

    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
      gap: 20px;
    }

    .card {
      display: flex;
      align-items: center;
      gap: 20px;
      background: white;
      border-radius: 12px;
      padding: 25px;
      cursor: pointer;
      box-shadow: 0 2px 8px rgba(0,0,0,0.08);
      border-left: 5px solid #ffc107;
      transition: all 0.25s ease;
    }

    .card:hover {
      transform: translateX(6px);
      box-shadow: 0 6px 20px rgba(0,0,0,0.12);
      border-left-color: #e0a800;
    }

    .card-icon {
      font-size: 2.5em;
      width: 55px;
      text-align: center;
      flex-shrink: 0;
    }

    .card-info {
      flex: 1;
    }

    .card-info h3 {
      margin: 0 0 5px 0;
      color: #2c3e50;
      font-size: 1.1em;
    }

    .card-info p {
      margin: 0;
      color: #7f8c8d;
      font-size: 0.9em;
    }

    .seta {
      font-size: 1.8em;
      color: #bdc3c7;
      font-weight: 300;
    }

    .card:hover .seta {
      color: #ffc107;
    }

    @media (max-width: 768px) {
      .grid { grid-template-columns: 1fr; }
    }
  `]
})
export class AdministrativoComponent {
  constructor(private router: Router) {}

  ir(rota: string): void {
    this.router.navigate([rota]);
  }
}
