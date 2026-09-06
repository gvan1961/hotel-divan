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
    <div class="header-botoes">
      <button class="btn-filtros" (click)="abrirModalFiltros()">🔍 Filtros</button>
      <button class="btn-voltar" (click)="voltar()">← Voltar</button>
    </div>
  </div>

  <!-- TABELA -->
  <div class="tabela-wrapper" *ngIf="jaBuscou">
    <table class="tabela">
      <thead>
        <tr>
          <th>Data/Hora</th>
          <th>Funcionário</th>
          <th>Ação</th>
          <th>Descrição</th>
          <th>Reserva</th>
          <th>Apartamento</th>
          <th>Hóspede</th>
          <th>Empresa</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let log of logs" [class]="getClassAcao(log.acao)">
          <td>{{ formatarDataHora(log.dataHora) }}</td>
          <td>{{ log.usuario?.nome || log.usuario?.username || 'Sistema' }}</td>
          <td>
            <span [class]="'badge-acao badge-' + log.acao.toLowerCase()">
              {{ getLabelAcao(log.acao) }}
            </span>
          </td>
          <td>{{ log.descricao }}</td>
          <td>
            <span *ngIf="log.reserva?.id" class="link-reserva" (click)="irParaReserva(log.reserva.id)">
              #{{ log.reserva.id }}
            </span>
          </td>
          <td>{{ log.reserva?.apartamento || '-' }}</td>
          <td>{{ log.reserva?.clienteNome || '-' }}</td>
          <td>{{ log.reserva?.empresaNome || '-' }}</td>
        </tr>
      </tbody>
    </table>
    <div class="vazio" *ngIf="logs.length === 0">
      Nenhum registro encontrado com os filtros aplicados.
    </div>
  </div>

  <div class="vazio" *ngIf="!jaBuscou">
    <p>🔍 Use os filtros acima para buscar registros de auditoria.</p>
    <button class="btn" (click)="abrirModalFiltros()">Abrir Filtros</button>
  </div>

  <!-- MODAL FILTROS -->
  <div class="modal-overlay" *ngIf="modalFiltros" (click)="fecharModalFiltros()">
    <div class="modal-content" (click)="$event.stopPropagation()">
      <h2>🔍 Filtros de Auditoria</h2>

      <div class="campo">
        <label>Funcionário</label>
        <input type="text" [(ngModel)]="filtros.funcionario" placeholder="Nome do funcionário..." />
      </div>
      <div class="campo">
        <label>Ação</label>
        <input type="text" [(ngModel)]="filtros.acao" placeholder="Ex: CHECKIN, PAGAMENTO..." />
      </div>
      <div class="campo-linha">
        <div class="campo">
          <label>Data Início</label>
          <input type="date" [(ngModel)]="filtros.dataInicio" />
        </div>
        <div class="campo">
          <label>Data Fim</label>
          <input type="date" [(ngModel)]="filtros.dataFim" />
        </div>
      </div>
      <div class="campo">
        <label>Apartamento</label>
        <input type="text" [(ngModel)]="filtros.apartamento" placeholder="Número do apartamento..." />
      </div>
      <div class="campo">
        <label>Código da Reserva</label>
        <input type="number" [(ngModel)]="filtros.reservaId" placeholder="Ex: 1890" />
      </div>
      <div class="campo">
        <label>Nome do Hóspede</label>
        <input type="text" [(ngModel)]="filtros.hospede" placeholder="Nome do hóspede..." />
      </div>
      <div class="campo">
        <label>Empresa</label>
        <input type="text" [(ngModel)]="filtros.empresa" placeholder="Nome da empresa..." />
      </div>

      <div class="modal-footer">
        <button class="btn-voltar-admin" (click)="voltarAoAdministrativo()">← Administrativo</button>
        <button class="btn-confirmar" (click)="aplicarFiltros()">🔍 Buscar</button>
      </div>
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

    .header-botoes { display: flex; gap: 10px; }
.btn-filtros { background: #667eea; color: #fff; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background: #fff; padding: 24px; border-radius: 8px; width: 90%; max-width: 500px; max-height: 90vh; overflow-y: auto; }
.campo { margin-bottom: 14px; }
.campo label { display: block; margin-bottom: 4px; font-weight: 600; color: #555; font-size: 0.85rem; }
.campo input { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; }
.campo-linha { display: flex; gap: 10px; }
.campo-linha .campo { flex: 1; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; margin-top: 16px; }
.btn-cancelar { background: #6c757d; color: #fff; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }
.btn-confirmar { background: #667eea; color: #fff; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }

  `]
})
export class AuditoriaComponent implements OnInit {

  logs: any[] = [];
  jaBuscou = false;
  modalFiltros = false;

  filtros = {
    funcionario: '',
    acao: '',
    dataInicio: '',
    dataFim: '',
    apartamento: '',
    reservaId: null as number | null,
    hospede: '',
    empresa: ''
  };

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.abrirModalFiltros();
  }
  abrirModalFiltros(): void {
    this.modalFiltros = true;
  }

  fecharModalFiltros(): void {
    this.modalFiltros = false;
  }

  aplicarFiltros(): void {
    const params = new URLSearchParams();
    if (this.filtros.funcionario) params.set('funcionario', this.filtros.funcionario);
    if (this.filtros.acao) params.set('acao', this.filtros.acao);
    if (this.filtros.dataInicio) params.set('dataInicio', this.filtros.dataInicio);
    if (this.filtros.dataFim) params.set('dataFim', this.filtros.dataFim);
    if (this.filtros.apartamento) params.set('apartamento', this.filtros.apartamento);
    if (this.filtros.reservaId) params.set('reservaId', this.filtros.reservaId.toString());
    if (this.filtros.hospede) params.set('hospede', this.filtros.hospede);
    if (this.filtros.empresa) params.set('empresa', this.filtros.empresa);

    this.http.get<any[]>(`/api/auditoria/filtrar?${params.toString()}`).subscribe({
      next: (data) => {
        this.logs = data;
        this.jaBuscou = true;
        this.modalFiltros = false;
      },
      error: (err) => {
        console.error('Erro ao buscar auditoria:', err);
        alert('Erro ao buscar registros');
      }
    });
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
  if (this.jaBuscou) {
    // Já tem busca feita — reseta e já abre o modal de novo, pronto pra nova busca
    this.jaBuscou = false;
    this.logs = [];
    this.filtros = {
      funcionario: '',
      acao: '',
      dataInicio: '',
      dataFim: '',
      apartamento: '',
      reservaId: null,
      hospede: '',
      empresa: ''
    };
    this.abrirModalFiltros();
  } else {
    // Ainda não buscou nada — sai pro Administrativo mesmo
    this.router.navigate(['/administrativo']);
  }
}

   voltarAoAdministrativo(): void {
  this.router.navigate(['/administrativo']);
}

}