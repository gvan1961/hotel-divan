import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface HistoricoItem {
  reservaId: number;
  numeroApartamento: string;
  tipoApartamento: string;
  dataCheckin: string;
  dataCheckout: string;
  dataCheckoutReal?: string;
  quantidadeDiarias: number;
  quantidadeHospedes: number;
  totalHospedagem: number;
  totalRecebido: number;
  status: string;
  titular: boolean;
}

interface ResumoCliente {
  clienteId: number;
  nomeCliente: string;
  cpf: string;
  celular: string;
  totalHospedagens: number;
  totalDiasHospedado: number;
  totalGasto: number;
  primeiraHospedagem: string;
  ultimaHospedagem: string;
  diasDesdeUltimaHospedagem: number;
  mediaEstadia: number;
  hospedagens: HistoricoItem[];
}

@Component({
  selector: 'app-cliente-historico',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">

      <!-- HEADER -->
      <div class="header">
        <div class="header-left">
          <button class="btn-back" (click)="voltar()">← Voltar</button>
          <h1>📋 Histórico de Hospedagens</h1>
        </div>
      </div>

      <!-- LOADING -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Carregando histórico...</p>
      </div>

      <ng-container *ngIf="!loading && resumo">

        <!-- DADOS DO CLIENTE -->
        <div class="cliente-card">
          <div class="cliente-avatar">{{ iniciais(resumo.nomeCliente) }}</div>
          <div class="cliente-info">
            <h2>{{ resumo.nomeCliente }}</h2>
            <div class="cliente-detalhes">
              <span *ngIf="resumo.cpf">🪪 {{ formatarCpf(resumo.cpf) }}</span>
              <span *ngIf="resumo.celular">📱 {{ resumo.celular }}</span>
            </div>
          </div>
        </div>

        <!-- CARDS DE ESTATÍSTICAS -->
        <div class="stats-grid">
          <div class="stat-card azul">
            <div class="stat-icon">🏨</div>
            <div class="stat-valor">{{ resumo.totalHospedagens }}</div>
            <div class="stat-label">Hospedagens</div>
          </div>

          <div class="stat-card verde">
            <div class="stat-icon">🛏️</div>
            <div class="stat-valor">{{ resumo.totalDiasHospedado }}</div>
            <div class="stat-label">Total de Diárias</div>
          </div>

          <div class="stat-card roxo">
            <div class="stat-icon">📊</div>
            <div class="stat-valor">{{ resumo.mediaEstadia | number:'1.1-1':'pt-BR' }}</div>
            <div class="stat-label">Média de Diárias</div>
          </div>

          <div class="stat-card laranja">
            <div class="stat-icon">💰</div>
            <div class="stat-valor">{{ resumo.totalGasto | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</div>
            <div class="stat-label">Total Gasto</div>
          </div>

          <div class="stat-card cinza">
            <div class="stat-icon">📅</div>
            <div class="stat-valor">{{ resumo.diasDesdeUltimaHospedagem }}</div>
            <div class="stat-label">Dias sem hospedar</div>
          </div>

          <div class="stat-card teal">
            <div class="stat-icon">🕐</div>
            <div class="stat-valor">{{ formatarDataCurta(resumo.ultimaHospedagem) }}</div>
            <div class="stat-label">Última Hospedagem</div>
          </div>
        </div>

        <!-- LINHA DO TEMPO / TABELA -->
        <div class="historico-section">
          <h3>📅 Histórico Detalhado</h3>

          <div *ngIf="resumo.hospedagens.length === 0" class="vazio">
            <p>📭 Nenhuma hospedagem encontrada</p>
          </div>

          <div class="tabela-wrapper" *ngIf="resumo.hospedagens.length > 0">
            <table class="tabela">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Apto</th>
                  <th>Tipo</th>
                  <th>Check-in</th>
                  <th>Check-out</th>
                  <th>Diárias</th>
                  <th>Hóspedes</th>
                  <th>Total</th>
                  <th>Status</th>
                  <th>Titular</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let h of resumo.hospedagens">
                  <td><a class="link-reserva" (click)="irParaReserva(h.reservaId)">#{{ h.reservaId }}</a></td>
                  <td><strong>{{ h.numeroApartamento }}</strong></td>
                  <td>{{ h.tipoApartamento || '—' }}</td>
                  <td>{{ formatarDataHora(h.dataCheckin) }}</td>
                  <td>{{ formatarDataHora(h.dataCheckoutReal || h.dataCheckout) }}</td>
                  <td class="text-center">{{ h.quantidadeDiarias }}</td>
                  <td class="text-center">{{ h.quantidadeHospedes }}</td>
                  <td>{{ h.totalHospedagem | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</td>
                  <td>
                    <span [class]="'badge badge-' + h.status.toLowerCase()">
                      {{ labelStatus(h.status) }}
                    </span>
                  </td>
                  <td class="text-center">{{ h.titular ? '✅' : '—' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

      </ng-container>

      <!-- ERRO -->
      <div *ngIf="!loading && !resumo" class="vazio">
        <p>❌ Erro ao carregar histórico do cliente.</p>
        <button class="btn-back" (click)="voltar()">← Voltar</button>
      </div>

    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 1400px; margin: 0 auto; }

    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
    .header-left { display: flex; align-items: center; gap: 15px; }
    h1 { color: #2c3e50; margin: 0; font-size: 1.6em; }

    .btn-back { background: #6c757d; color: white; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-size: 14px; }
    .btn-back:hover { background: #5a6268; }

    /* CLIENTE CARD */
    .cliente-card {
      background: white; border-radius: 12px; padding: 20px;
      box-shadow: 0 2px 8px rgba(0,0,0,.1); margin-bottom: 25px;
      display: flex; align-items: center; gap: 20px;
    }
    .cliente-avatar {
      width: 60px; height: 60px; border-radius: 50%;
      background: linear-gradient(135deg, #667eea, #764ba2);
      color: white; font-size: 1.5em; font-weight: 700;
      display: flex; align-items: center; justify-content: center;
      flex-shrink: 0;
    }
    .cliente-info h2 { margin: 0 0 8px; color: #2c3e50; }
    .cliente-detalhes { display: flex; gap: 20px; color: #7f8c8d; font-size: .95em; }

    /* STATS */
    .stats-grid {
      display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
      gap: 15px; margin-bottom: 25px;
    }
    .stat-card {
      background: white; border-radius: 12px; padding: 20px; text-align: center;
      box-shadow: 0 2px 8px rgba(0,0,0,.1); border-top: 4px solid;
    }
    .stat-card.azul    { border-top-color: #3498db; }
    .stat-card.verde   { border-top-color: #27ae60; }
    .stat-card.roxo    { border-top-color: #9b59b6; }
    .stat-card.laranja { border-top-color: #e67e22; }
    .stat-card.cinza   { border-top-color: #95a5a6; }
    .stat-card.teal    { border-top-color: #1abc9c; }
    .stat-icon { font-size: 2em; margin-bottom: 8px; }
    .stat-valor { font-size: 1.6em; font-weight: 700; color: #2c3e50; line-height: 1; margin-bottom: 6px; }
    .stat-label { font-size: .8em; color: #7f8c8d; text-transform: uppercase; letter-spacing: .5px; }

    /* HISTÓRICO */
    .historico-section { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,.1); }
    .historico-section h3 { margin: 0 0 20px; color: #2c3e50; }

    .tabela-wrapper { overflow-x: auto; }
    .tabela { width: 100%; border-collapse: collapse; }
    .tabela th {
      background: #f8f9fa; padding: 12px; text-align: left;
      font-size: .82em; color: #2c3e50; text-transform: uppercase;
      border-bottom: 2px solid #e0e0e0;
    }
    .tabela td { padding: 11px 12px; border-bottom: 1px solid #f0f0f0; font-size: .9em; color: #555; }
    .tabela tr:hover { background: #f8f9fa; }
    .text-center { text-align: center; }

    .link-reserva { color: #3498db; cursor: pointer; font-weight: 700; text-decoration: underline; }
    .link-reserva:hover { color: #2980b9; }

    .badge { padding: 4px 10px; border-radius: 12px; font-size: .75em; font-weight: 600; text-transform: uppercase; }
    .badge-ativa      { background: #d4edda; color: #155724; }
    .badge-finalizada { background: #e3f2fd; color: #1565c0; }
    .badge-cancelada  { background: #f8d7da; color: #721c24; }

    /* LOADING */
    .loading { text-align: center; padding: 60px; }
    .spinner {
      border: 4px solid #f3f3f3; border-top: 4px solid #3498db;
      border-radius: 50%; width: 50px; height: 50px;
      animation: spin 1s linear infinite; margin: 0 auto 20px;
    }
    @keyframes spin { 0% { transform: rotate(0); } 100% { transform: rotate(360deg); } }

    .vazio { text-align: center; padding: 60px; color: #7f8c8d; }
    .vazio p { font-size: 1.2em; }

    @media (max-width: 768px) {
      .stats-grid { grid-template-columns: repeat(2, 1fr); }
      .cliente-detalhes { flex-direction: column; gap: 5px; }
    }
  `]
})
export class ClienteHistoricoApp implements OnInit {
  private http   = inject(HttpClient);
  private router = inject(Router);
  private route  = inject(ActivatedRoute);

  resumo: ResumoCliente | null = null;
  loading = true;
  clienteId!: number;

  ngOnInit(): void {
    this.clienteId = +this.route.snapshot.paramMap.get('id')!;
    this.carregar();
  }

  carregar(): void {
    this.loading = true;
    this.http.get<ResumoCliente>(`/api/clientes/${this.clienteId}/historico`).subscribe({
      next: (data) => { this.resumo = data; this.loading = false; },
      error: (err) => { console.error('Erro ao carregar histórico', err); this.loading = false; }
    });
  }

  iniciais(nome: string): string {
    if (!nome) return '?';
    return nome.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
  }

  formatarCpf(cpf: string): string {
    if (!cpf) return '';
    const n = cpf.replace(/\D/g, '');
    if (n.length !== 11) return cpf;
    return `${n.slice(0,3)}.${n.slice(3,6)}.${n.slice(6,9)}-${n.slice(9)}`;
  }

  formatarDataHora(data: string): string {
    if (!data) return '—';
    return new Date(data).toLocaleString('pt-BR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  formatarDataCurta(data: string): string {
    if (!data) return '—';
    return new Date(data).toLocaleDateString('pt-BR', {
      day: '2-digit', month: '2-digit', year: 'numeric'
    });
  }

  labelStatus(status: string): string {
    const map: any = {
      ATIVA: 'Ativa', FINALIZADA: 'Finalizada', CANCELADA: 'Cancelada',
      PRE_RESERVA: 'Pré-Reserva'
    };
    return map[status] || status;
  }

  irParaReserva(id: number): void {
    this.router.navigate(['/reservas', id]);
  }

  voltar(): void {
    this.router.navigate(['/clientes']);
  }
}
