import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PixService } from '../../services/pix.service';
import { CartaoService } from '../../services/cartao.service';

@Component({
  selector: 'app-historico-pagamentos-lista',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>📜 Histórico de Pagamentos (Pix / Cartão)</h1>
        <div class="header-botoes">
          <button class="btn-filtros" (click)="abrirModalFiltros()">🔍 Filtros</button>
          <button class="btn-voltar" (click)="voltar()">← Voltar</button>
        </div>
      </div>

      <div class="tabela-wrapper" *ngIf="jaBuscou">
        <table class="tabela">
          <thead>
            <tr>
              <th>Data/Hora</th>
              <th>Tipo</th>
              <th>Valor</th>
              <th>Status</th>
              <th>Reserva</th>
              <th>Apartamento</th>
              <th>Referência</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let item of resultados">
              <td>{{ formatarDataHora(item.dataCriacao) }}</td>
              <td>{{ tipoAtual === 'PIX' ? '📱 Pix' : (item.formaPagamento === 'credit_card' ? '💳 Crédito' : '💳 Débito') }}</td>
              <td>R$ {{ item.valor | number:'1.2-2' }}</td>
              <td>
                <span class="badge-status" [ngClass]="'status-' + item.status?.toLowerCase()">
                  {{ item.status }}
                </span>
              </td>
              <td>
                <span *ngIf="item.reservaId" class="link-reserva" (click)="irParaReserva(item.reservaId)">
                  #{{ item.reservaId }}
                </span>
                <span *ngIf="!item.reservaId">-</span>
              </td>
              <td>{{ item.numeroApartamento || '-' }}</td>
              <td class="correlation">{{ item.correlationId }}</td>
            </tr>
          </tbody>
        </table>
        <div class="vazio" *ngIf="resultados.length === 0">
          Nenhum registro encontrado com os filtros aplicados.
        </div>
      </div>

      <div class="vazio" *ngIf="!jaBuscou">
        <p>🔍 Use os filtros para buscar pagamentos no histórico.</p>
        <button class="btn" (click)="abrirModalFiltros()">Abrir Filtros</button>
      </div>

      <!-- MODAL FILTROS -->
      <div class="modal-overlay" *ngIf="modalFiltros" (click)="fecharModalFiltros()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>🔍 Filtros de Histórico</h2>

          <div class="campo">
            <label>Tipo de Pagamento *</label>
            <select [(ngModel)]="filtros.tipo">
              <option value="PIX">📱 Pix</option>
              <option value="CARTAO">💳 Cartão</option>
            </select>
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
            <label>Código da Reserva</label>
            <input type="number" [(ngModel)]="filtros.reservaId" placeholder="Ex: 1890" />
          </div>

          <div class="campo">
            <label>Status</label>
            <select [(ngModel)]="filtros.status">
              <option value="">Todos</option>
              <option *ngIf="filtros.tipo === 'PIX'" value="PENDENTE">Pendente</option>
              <option *ngIf="filtros.tipo === 'PIX'" value="PAGO">Pago</option>
              <option *ngIf="filtros.tipo === 'PIX'" value="CONFIRMADO">Confirmado</option>
              <option *ngIf="filtros.tipo === 'PIX'" value="CANCELADO">Cancelado</option>
              <option *ngIf="filtros.tipo === 'PIX'" value="EXPIRADO">Expirado</option>
              <option *ngIf="filtros.tipo === 'CARTAO'" value="PENDENTE">Pendente</option>
              <option *ngIf="filtros.tipo === 'CARTAO'" value="Pago">Pago</option>
              <option *ngIf="filtros.tipo === 'CARTAO'" value="CANCELADO">Cancelado</option>
            </select>
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
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .header-botoes { display: flex; gap: 10px; }
    h1 { margin: 0; color: #333; }
    .btn-voltar { background: #6c757d; color: #fff; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }
    .btn-filtros { background: #667eea; color: #fff; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }
    .tabela-wrapper { overflow-x: auto; }
    .tabela { width: 100%; border-collapse: collapse; font-size: 14px; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,.1); }
    .tabela th { background: #f0f4ff; padding: 12px; text-align: left; font-size: 13px; color: #333; }
    .tabela td { padding: 10px 12px; border-bottom: 1px solid #eee; font-size: 13px; }
    .tabela tr:hover td { background: #fafafa; }
    .badge-status { padding: 3px 10px; border-radius: 12px; font-size: 11px; font-weight: 700; text-transform: uppercase; }
    .status-pendente { background: #fff3cd; color: #856404; }
    .status-pago { background: #d4edda; color: #155724; }
    .status-confirmado { background: #d1e7dd; color: #0f5132; }
    .status-cancelado { background: #f8d7da; color: #842029; }
    .status-expirado { background: #e2e3e5; color: #41464b; }
    .link-reserva { color: #2980b9; cursor: pointer; font-weight: 600; text-decoration: underline; }
    .correlation { font-family: monospace; font-size: 11px; color: #888; }
    .vazio { text-align: center; padding: 40px; color: #aaa; }
    .modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .modal-content { background: #fff; padding: 24px; border-radius: 8px; width: 90%; max-width: 500px; max-height: 90vh; overflow-y: auto; }
    .campo { margin-bottom: 14px; }
    .campo label { display: block; margin-bottom: 4px; font-weight: 600; color: #555; font-size: 0.85rem; }
    .campo input, .campo select { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; }
    .campo-linha { display: flex; gap: 10px; }
    .campo-linha .campo { flex: 1; }
    .modal-footer { display: flex; justify-content: flex-end; gap: 10px; margin-top: 16px; }
    .btn-voltar-admin { background: #6c757d; color: #fff; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; margin-right: auto; }
    .btn-confirmar { background: #667eea; color: #fff; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }
  `]
})
export class HistoricoPagamentosListaApp implements OnInit {
  private pixService = inject(PixService);
  private cartaoService = inject(CartaoService);
  private router = inject(Router);

  resultados: any[] = [];
  jaBuscou = false;
  modalFiltros = false;
  tipoAtual = 'PIX';

  filtros = {
    tipo: 'PIX',
    dataInicio: '',
    dataFim: '',
    reservaId: null as number | null,
    status: ''
  };

  ngOnInit(): void {
    this.abrirModalFiltros();
  }

  abrirModalFiltros(): void {
    this.modalFiltros = true;
  }

  fecharModalFiltros(): void {
    this.modalFiltros = false;
  }

  voltarAoAdministrativo(): void {
    this.router.navigate(['/administrativo']);
  }

  aplicarFiltros(): void {
    this.tipoAtual = this.filtros.tipo;

    const params = {
      dataInicio: this.filtros.dataInicio || undefined,
      dataFim: this.filtros.dataFim || undefined,
      reservaId: this.filtros.reservaId || undefined,
      status: this.filtros.status || undefined
    };

  let busca: any;
if (this.filtros.tipo === 'PIX') {
  busca = this.pixService.buscarHistorico(params);
} else {
  busca = this.cartaoService.buscarHistorico(params);
}

busca.subscribe({
  next: (data: any) => {
    this.resultados = data;
    this.jaBuscou = true;
    this.modalFiltros = false;
  },
  error: (err: any) => {
    console.error('Erro ao buscar histórico:', err);
    alert('Erro ao buscar histórico');
  }
});
  }

  formatarDataHora(data: string): string {
    if (!data) return '';
    const d = new Date(data);
    return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  irParaReserva(reservaId: number): void {
    this.router.navigate(['/reservas', reservaId]);
  }

  voltar(): void {
    if (this.jaBuscou) {
      this.jaBuscou = false;
      this.resultados = [];
      this.filtros = { tipo: 'PIX', dataInicio: '', dataFim: '', reservaId: null, status: '' };
      this.abrirModalFiltros();
    } else {
      this.router.navigate(['/administrativo']);
    }
  }
}