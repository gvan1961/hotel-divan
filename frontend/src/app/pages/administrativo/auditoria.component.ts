import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>🔍 Log de Auditoria</h1>
        <button class="btn-voltar" (click)="voltar()">← Voltar</button>
      </div>

      <!-- FILTROS -->
      <div class="filtros">
        <input
          type="text"
          placeholder="Filtrar por funcionário..."
          [(ngModel)]="filtroFuncionario"
          (input)="filtrar()"
          class="input-filtro" />
        <input
          type="text"
          placeholder="Filtrar por ação..."
          [(ngModel)]="filtroAcao"
          (input)="filtrar()"
          class="input-filtro" />
        <input
          type="date"
          [(ngModel)]="filtroData"
          (input)="filtrar()"
          class="input-filtro" />
        <button class="btn-limpar" (click)="limparFiltros()">✕ Limpar</button>
      </div>

      <!-- TABELA -->
      <div class="tabela-wrapper">
        <table class="tabela">
          <thead>
            <tr>
              <th>Data/Hora</th>
              <th>Funcionário</th>
              <th>Ação</th>
              <th>Descrição</th>
              <th>Reserva</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let log of logsFiltrados" [class]="getClassAcao(log.acao)">
              <td>{{ formatarDataHora(log.dataHora) }}</td>
              <td>{{ log.usuario?.nome || log.usuario?.username || 'Sistema' }}</td>
              <td>
                <span [class]="'badge-acao badge-' + log.acao.toLowerCase()">
                  {{ getLabelAcao(log.acao) }}
                </span>
              </td>
              <td>{{ log.descricao }}</td>
              <td>
                <span 
                  *ngIf="log.reserva?.id"
                  class="link-reserva"
                  (click)="irParaReserva(log.reserva.id)">
                  #{{ log.reserva.id }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="vazio" *ngIf="logsFiltrados.length === 0">
          Nenhum registro encontrado.
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 1200px; margin: 0 auto; }
    .header {
      display: flex; justify-content: space-between;
      align-items: center; margin-bottom: 20px;
    }
    h1 { margin: 0; color: #333; }
    .btn-voltar {
      background: #6c757d; color: #fff;
      border: none; padding: 8px 16px;
      border-radius: 5px; cursor: pointer;
    }
    .filtros {
      display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap;
    }
    .input-filtro {
      flex: 1; min-width: 180px; padding: 8px 12px;
      border: 1px solid #ddd; border-radius: 5px; font-size: 14px;
    }
    .btn-limpar {
      padding: 8px 14px; background: #e74c3c; color: #fff;
      border: none; border-radius: 5px; cursor: pointer;
    }
    .tabela-wrapper { overflow-x: auto; }
    .tabela {
      width: 100%; border-collapse: collapse; font-size: 14px;
      background: #fff; border-radius: 8px; overflow: hidden;
      box-shadow: 0 1px 4px rgba(0,0,0,.1);
    }
    .tabela th {
      background: #f0f4ff; padding: 12px; text-align: left;
      font-size: 13px; color: #333;
    }
    .tabela td {
      padding: 10px 12px; border-bottom: 1px solid #eee; font-size: 13px;
    }
    .tabela tr:hover td { background: #fafafa; }
    .badge-acao {
      padding: 3px 10px; border-radius: 12px;
      font-size: 11px; font-weight: 700;
    }
    .badge-checkin       { background: #d5f5e3; color: #1e8449; }
    .badge-pre_reserva   { background: #d6eaf8; color: #1a5276; }
    .badge-checkout_pago { background: #d5f5e3; color: #1e8449; }
    .badge-checkout_faturado { background: #fdebd0; color: #a04000; }
    .badge-pagamento     { background: #d6eaf8; color: #1a5276; }
    .badge-desconto      { background: #fef9e7; color: #b7770d; }
    .badge-cancelamento  { background: #fadbd8; color: #c0392b; }
    .row-checkout_pago   td { background: #f0fff4; }
    .row-checkout_faturado td { background: #fff8f0; }
    .row-cancelamento    td { background: #fff5f5; }
    .link-reserva {
      color: #2980b9; cursor: pointer; font-weight: 600;
      text-decoration: underline;
    }
    .vazio {
      text-align: center; padding: 40px; color: #aaa;
    }
  `]
})
export class AuditoriaComponent implements OnInit {

  logs: any[] = [];
  logsFiltrados: any[] = [];
  filtroFuncionario = '';
  filtroAcao = '';
  filtroData = '';

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.http.get<any[]>('/api/auditoria').subscribe({
      next: (data) => {
        this.logs = data;
        this.logsFiltrados = data;
      },
      error: (err) => console.error('Erro ao carregar auditoria:', err)
    });
  }

  filtrar(): void {
    this.logsFiltrados = this.logs.filter(log => {
      const okFuncionario = !this.filtroFuncionario ||
        (log.usuario?.nome?.toLowerCase().includes(this.filtroFuncionario.toLowerCase()) ||
         log.usuario?.username?.toLowerCase().includes(this.filtroFuncionario.toLowerCase()));
      const okAcao = !this.filtroAcao ||
        log.acao?.toLowerCase().includes(this.filtroAcao.toLowerCase()) ||
        this.getLabelAcao(log.acao).toLowerCase().includes(this.filtroAcao.toLowerCase());
      const okData = !this.filtroData ||
        log.dataHora?.startsWith(this.filtroData);
      return okFuncionario && okAcao && okData;
    });
  }

  limparFiltros(): void {
    this.filtroFuncionario = '';
    this.filtroAcao = '';
    this.filtroData = '';
    this.logsFiltrados = [...this.logs];
  }

  getLabelAcao(acao: string): string {
    const labels: any = {
      'CHECKIN': '🏠 Check-in',
      'PRE_RESERVA': '📅 Pré-Reserva',
      'CHECKOUT_PAGO': '💚 Checkout Pago',
      'CHECKOUT_FATURADO': '🧾 Checkout Faturado',
      'PAGAMENTO': '💳 Pagamento',
      'DESCONTO': '💰 Desconto',
      'CANCELAMENTO': '❌ Cancelamento',
      'ALTERACAO_CHECKOUT': '📅 Alteração Checkout',
      'ESTORNO': '↩ Estorno'
    };
    return labels[acao] || acao;
  }

  getClassAcao(acao: string): string {
    return 'row-' + acao.toLowerCase();
  }

  formatarDataHora(data: string): string {
    if (!data) return '';
    const d = new Date(data);
    return d.toLocaleDateString('pt-BR') + ' ' +
           d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  irParaReserva(id: number): void {
    this.router.navigate(['/reservas', id]);
  }

  voltar(): void {
    this.router.navigate(['/administrativo']);
  }
}