import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PixService } from '../../services/pix.service';
import { CobrancaPix, ItemPixPendente } from '../../models/cobranca-pix.model';

@Component({
  selector: 'app-pix-pendentes-lista',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <div class="header">
        <button class="btn-voltar" (click)="voltar()">← Voltar ao PDV</button>
        <h1>🔔 Vendas Pix Pendentes</h1>
      </div>

      <div *ngIf="loading" class="loading">Carregando...</div>
      <div *ngIf="!loading && cobrancas.length === 0" class="empty">
        Nenhuma venda Pix pendente no momento.
      </div>


      <div class="lista" *ngIf="!loading && cobrancas.length > 0">
        <div class="card-pendente" *ngFor="let cobranca of cobrancas" [class.pago]="cobranca.status === 'PAGO'">
  <div class="card-header">
    <span class="valor">R$ {{ cobranca.valor | number:'1.2-2' }}</span>
    <span class="badge-status" [class.badge-pago]="cobranca.status === 'PAGO'" [class.badge-pendente]="cobranca.status === 'PENDENTE'">
      {{ cobranca.status === 'PAGO' ? '✅ Pago' : '⏳ Aguardando' }}
    </span>
  </div>
  <div class="card-sub">
    <span class="data">{{ cobranca.dataCriacao | date:'dd/MM/yyyy HH:mm' }}</span>
    <span class="tipo" *ngIf="cobranca.reservaId">🏨 Reserva #{{ cobranca.reservaId }}</span>
    <span class="tipo" *ngIf="!cobranca.reservaId">💳 Venda PDV</span>
  </div>
  <div class="itens" *ngIf="getItens(cobranca) as itens">
    <div class="item-linha" *ngFor="let item of itens">
      {{ item.quantidade }}x {{ item.nome }} — R$ {{ item.valorUnitario | number:'1.2-2' }}
    </div>
  </div>
  
  <button class="btn-retomar" *ngIf="!cobranca.reservaId" (click)="retomar(cobranca)">🔄 Retomar Venda</button>
<button class="btn-ir-reserva" *ngIf="cobranca.reservaId" (click)="irParaReserva(cobranca)">✅ Ir para Reserva</button>
  <button class="btn-cancelar-pix" (click)="cancelar(cobranca)">❌ Cancelar</button>
</div>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 700px; margin: 0 auto; }
    .header { display: flex; align-items: center; gap: 15px; margin-bottom: 20px; }
    h1 { margin: 0; color: #333; }
    .btn-voltar { background: #6c757d; color: white; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }
    .loading, .empty { text-align: center; padding: 40px; color: #888; }
    .lista { display: flex; flex-direction: column; gap: 12px; }
    .card-pendente { background: white; border: 1px solid #ddd; border-radius: 8px; padding: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
    .card-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
    .valor { font-weight: 700; font-size: 1.1rem; color: #ff9800; }
    .data { font-size: 0.8rem; color: #888; }
    .itens { margin-bottom: 10px; }
    .item-linha { font-size: 0.85rem; color: #555; }
    .btn-retomar { width: 100%; padding: 8px; background: #667eea; color: white; border: none; border-radius: 5px; cursor: pointer; }
    .btn-cancelar-pix { width: 100%; padding: 8px; margin-top: 6px; background: #dc3545; color: white; border: none; border-radius: 5px; cursor: pointer; }
    .card-pendente.pago { border-color: #28a745; background: #f4fdf6; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.badge-status { font-size: 0.75rem; padding: 3px 10px; border-radius: 12px; font-weight: 600; }
.badge-pendente { background: #fff3cd; color: #856404; }
.badge-pago { background: #d4edda; color: #155724; }
.card-sub { display: flex; gap: 10px; margin-bottom: 8px; font-size: 0.8rem; color: #888; }
.btn-ir-reserva { display: block; width: 100%; padding: 8px; margin-top: 8px; border: none; border-radius: 5px; cursor: pointer; background: #667eea; color: white; font-weight: 600; }
    `]
})
export class PixPendentesListaApp implements OnInit {
  private pixService = inject(PixService);
  private router = inject(Router);

  cobrancas: CobrancaPix[] = [];
  loading = true;

  ngOnInit(): void {
    this.carregarPendentes();
  }

  carregarPendentes(): void {
  this.pixService.listarPendentes().subscribe({
    next: (data) => {
      this.cobrancas = data.sort((a, b) => {
        if (a.status === 'PAGO' && b.status !== 'PAGO') return -1;
        if (a.status !== 'PAGO' && b.status === 'PAGO') return 1;
        return new Date(b.dataCriacao).getTime() - new Date(a.dataCriacao).getTime();
      });
      this.loading = false;
    },
    error: (err) => {
      console.error('Erro ao carregar pendentes', err);
      this.loading = false;
    }
  });
}

  getItens(cobranca: CobrancaPix): ItemPixPendente[] {
    if (!cobranca.itensJson) return [];
    try {
      return JSON.parse(cobranca.itensJson);
    } catch {
      return [];
    }
  }

  retomar(cobranca: CobrancaPix): void {
    // Guarda a cobrança selecionada e navega de volta pro PDV
    sessionStorage.setItem('pixPendenteRetomar', JSON.stringify(cobranca));
    this.router.navigate(['/pdv']);
  }

  irParaReserva(cobranca: CobrancaPix): void {
  this.router.navigate(['/reservas', cobranca.reservaId]);
}

  cancelar(cobranca: CobrancaPix): void {
  if (!confirm('Deseja realmente cancelar esta cobrança Pix pendente?')) return;

  this.pixService.cancelar(cobranca.id).subscribe({
    next: () => {
      alert('✅ Cobrança cancelada!');
      this.carregarPendentes();
    },
    error: (err) => {
      console.error('Erro ao cancelar', err);
      alert('❌ Erro ao cancelar cobrança');
    }
  });
}

  voltar(): void {
    this.router.navigate(['/pdv']);
  }
}