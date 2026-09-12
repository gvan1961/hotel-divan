import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CartaoService } from '../../services/cartao.service';
import { CobrancaCartao } from '../../models/cobranca-cartao.model';

@Component({
  selector: 'app-cartao-pendentes-lista',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <div class="header">
        <button class="btn-voltar" (click)="voltar()">← Voltar</button>
        <h1>💳 Cartões Pendentes</h1>
      </div>

      <div *ngIf="loading" class="loading">Carregando...</div>
      <div *ngIf="!loading && cobrancas.length === 0" class="empty">
        Nenhuma cobrança de cartão pendente no momento.
      </div>

      <div class="lista" *ngIf="!loading && cobrancas.length > 0">
      
      <div class="card-pendente" *ngFor="let cobranca of cobrancas" [class.pago]="isPago(cobranca.status)">
  <div class="card-header">
    <span class="valor">R$ {{ cobranca.valor | number:'1.2-2' }}</span>
    <span class="badge-status" [class.badge-pago]="isPago(cobranca.status)" [class.badge-pendente]="!isPago(cobranca.status)">
      {{ isPago(cobranca.status) ? '✅ Pago' : '⏳ Aguardando' }}
    </span>
  </div>
  <div class="card-sub">
    <span class="data">{{ cobranca.dataCriacao | date:'dd/MM/yyyy HH:mm' }}</span>
    <span class="tipo" *ngIf="cobranca.reservaId">🏨 Reserva #{{ cobranca.reservaId }} — Apt {{ cobranca.numeroApartamento }}</span>
           
           
            <span class="tipo">{{ cobranca.formaPagamento === 'credit_card' ? '💳 Crédito' : '💳 Débito' }}</span>
          </div>

          <button class="btn-ir-reserva" *ngIf="cobranca.reservaId" (click)="irParaReserva(cobranca)">✅ Ir para Reserva</button>
          <button class="btn-cancelar-pix" (click)="cancelar(cobranca)">❌ Cancelar</button>
        </div>
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
    .card-pendente.pago { border-color: #28a745; background: #f4fdf6; }
    .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
    .valor { font-weight: 700; font-size: 1.1rem; color: #ff9800; }
    .data { font-size: 0.8rem; color: #888; }
    .badge-status { font-size: 0.75rem; padding: 3px 10px; border-radius: 12px; font-weight: 600; }
    .badge-pendente { background: #fff3cd; color: #856404; }
    .badge-pago { background: #d4edda; color: #155724; }
    .card-sub { display: flex; gap: 10px; margin-bottom: 8px; font-size: 0.8rem; color: #888; }
    .btn-ir-reserva { display: block; width: 100%; padding: 8px; margin-top: 8px; border: none; border-radius: 5px; cursor: pointer; background: #667eea; color: white; font-weight: 600; }
    .btn-cancelar-pix { width: 100%; padding: 8px; margin-top: 6px; background: #dc3545; color: white; border: none; border-radius: 5px; cursor: pointer; }
  `]
})
export class CartaoPendentesListaApp implements OnInit {
  private cartaoService = inject(CartaoService);
  private router = inject(Router);

  cobrancas: CobrancaCartao[] = [];
  loading = true;

  ngOnInit(): void {
    this.carregarPendentes();
  }

  carregarPendentes(): void {
    this.cartaoService.listarPendentes().subscribe({
      next: (data) => {
        this.cobrancas = data.sort((a, b) =>
          new Date(b.dataCriacao).getTime() - new Date(a.dataCriacao).getTime()
        );
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar pendentes', err);
        this.loading = false;
      }
    });
  }

  irParaReserva(cobranca: CobrancaCartao): void {
    this.router.navigate(['/reservas', cobranca.reservaId]);
  }

  isPago(status: string): boolean {
    return status?.toUpperCase() === 'PAGO';
  }

  cancelar(cobranca: CobrancaCartao): void {
  if (!confirm('Deseja realmente cancelar esta cobrança de cartão pendente?')) return;

  this.cartaoService.cancelar(cobranca.id).subscribe({
    next: () => {
      alert('✅ Cobrança cancelada no sistema.\n\n⚠️ IMPORTANTE: Cancele também manualmente na maquininha física, pois o cancelamento automático nela ainda está em teste.');
      this.carregarPendentes();
    },
    error: (err) => {
      console.error('Erro ao cancelar', err);
      alert('❌ Erro ao cancelar cobrança');
    }
  });
}

  voltar(): void {
    this.router.navigate(['/administrativo']);
  }
}