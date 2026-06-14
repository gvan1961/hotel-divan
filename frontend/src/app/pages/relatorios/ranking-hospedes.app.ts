import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';

interface RankingHospede {
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
}

@Component({
  selector: 'app-ranking-hospedes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">

      <div class="header">
        <h1>🏆 Ranking de Hóspedes</h1>
        <button class="btn-back" (click)="voltar()">← Voltar</button>
      </div>

      <!-- FILTROS -->
      <div class="area-filtros">
        <div class="filtro-grupo">
          <label>Check-in a partir de:</label>
          <input type="date" [(ngModel)]="filtroInicio" />
        </div>
        <div class="filtro-grupo">
          <label>Check-in até:</label>
          <input type="date" [(ngModel)]="filtroFim" />
        </div>
        <button class="btn-buscar" (click)="buscar()">🔍 Buscar</button>
        <button class="btn-limpar" *ngIf="filtroInicio || filtroFim" (click)="limpar()">🗑️ Limpar</button>

        <div class="filtro-grupo">
          <label>Ordenar por:</label>
          <select [(ngModel)]="ordenacao" (change)="ordenar()">
            <option value="hospedagens">Quantidade de Hospedagens</option>
            <option value="diarias">Quantidade de Diárias</option>
            <option value="gasto">Total Gasto</option>
            <option value="dias">Dias sem hospedar</option>
          </select>
        </div>

      </div>

      <!-- RESUMO -->
      <div class="resumo-bar" *ngIf="!loading && ranking.length > 0">
        <span>📊 <strong>{{ ranking.length }}</strong> hóspedes encontrados</span>
        <span>🏨 <strong>{{ totalHospedagens() }}</strong> hospedagens no período</span>
        <span>💰 <strong>{{ totalGeral() | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong> em receita</span>
      </div>

      <!-- LOADING -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Carregando ranking...</p>
      </div>

      <!-- TABELA -->
      <div class="tabela-wrapper" *ngIf="!loading && ranking.length > 0">
        <table class="tabela">
          <thead>
            <tr>
              <th class="col-pos">#</th>
              <th>Hóspede</th>
              <th>CPF</th>
              <th>Celular</th>
              <th class="text-center">Hospedagens</th>
              <th class="text-center">Total Diárias</th>
              <th class="text-center">Média Diárias</th>
              <th>Primeira</th>
              <th>Última</th>
              <th class="text-center">Dias sem hospedar</th>
              <th>Total Gasto</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let h of ranking; let i = index"
                [class.top1]="i === 0"
                [class.top2]="i === 1"
                [class.top3]="i === 2">
              <td class="col-pos">
                <span class="medalha" *ngIf="i === 0">🥇</span>
                <span class="medalha" *ngIf="i === 1">🥈</span>
                <span class="medalha" *ngIf="i === 2">🥉</span>
                <span *ngIf="i > 2">{{ i + 1 }}</span>
              </td>
              <td><strong>{{ h.nomeCliente }}</strong></td>
              <td>{{ formatarCpf(h.cpf) || '—' }}</td>
              <td>{{ h.celular || '—' }}</td>
              <td class="text-center">
                <span class="badge-hospedagens">{{ h.totalHospedagens }}</span>
              </td>
              <td class="text-center">{{ h.totalDiasHospedado }}</td>
              <td class="text-center">{{ h.mediaEstadia | number:'1.1-1':'pt-BR' }}</td>
              <td>{{ formatarData(h.primeiraHospedagem) }}</td>
              <td>{{ formatarData(h.ultimaHospedagem) }}</td>
              <td class="text-center">
                <span [class]="classeDias(h.diasDesdeUltimaHospedagem)">
                  {{ labelDias(h.diasDesdeUltimaHospedagem) }}
                </span>
              </td>
              <td>{{ h.totalGasto | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</td>
              <td>
                <button class="btn-ver" (click)="verHistorico(h.clienteId)">📋 Histórico</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- VAZIO -->
      <div *ngIf="!loading && ranking.length === 0" class="vazio">
        <p>📭 Nenhum hóspede encontrado</p>
      </div>

    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 1600px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
    h1 { color: #2c3e50; margin: 0; }
    .btn-back { background: #6c757d; color: white; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
    .btn-back:hover { background: #5a6268; }

    .area-filtros {
      background: white; padding: 18px; border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,.1); margin-bottom: 20px;
      display: flex; gap: 15px; flex-wrap: wrap; align-items: flex-end;
    }
    .filtro-grupo { display: flex; flex-direction: column; gap: 6px; }
    .filtro-grupo label { font-weight: 600; color: #2c3e50; font-size: .85em; }
    .filtro-grupo input {
      padding: 9px; border: 2px solid #e0e0e0; border-radius: 6px; font-size: .95em;
    }
    .filtro-grupo input:focus { outline: none; border-color: #3498db; }
    .btn-buscar { background: #667eea; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: 600; }
    .btn-buscar:hover { background: #5568d3; }
    .btn-limpar { background: #e74c3c; color: white; border: none; padding: 10px 16px; border-radius: 6px; cursor: pointer; font-weight: 600; }
    .btn-limpar:hover { background: #c0392b; }

    .resumo-bar {
      background: white; padding: 14px 20px; border-radius: 10px;
      box-shadow: 0 2px 8px rgba(0,0,0,.1); margin-bottom: 20px;
      display: flex; gap: 30px; flex-wrap: wrap;
      font-size: .95em; color: #555;
    }

    .loading { text-align: center; padding: 60px; }
    .spinner {
      border: 4px solid #f3f3f3; border-top: 4px solid #3498db;
      border-radius: 50%; width: 50px; height: 50px;
      animation: spin 1s linear infinite; margin: 0 auto 20px;
    }
    @keyframes spin { 0% { transform: rotate(0); } 100% { transform: rotate(360deg); } }

    .tabela-wrapper { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,.1); overflow-x: auto; }
    .tabela { width: 100%; border-collapse: collapse; }
    .tabela th {
      background: #f8f9fa; padding: 12px; text-align: left;
      font-size: .8em; color: #2c3e50; text-transform: uppercase;
      border-bottom: 2px solid #e0e0e0; white-space: nowrap;
    }
    .tabela td { padding: 11px 12px; border-bottom: 1px solid #f0f0f0; font-size: .9em; color: #555; }
    .tabela tr:hover { background: #f8f9fa; }
    .text-center { text-align: center; }
    .col-pos { width: 50px; text-align: center; }

    .top1 { background: #fffde7 !important; }
    .top2 { background: #f5f5f5 !important; }
    .top3 { background: #fff8f0 !important; }
    .medalha { font-size: 1.4em; }

    .badge-hospedagens {
      background: #667eea; color: white; padding: 3px 10px;
      border-radius: 12px; font-weight: 700; font-size: .85em;
    }

    .dias-ok      { color: #27ae60; font-weight: 600; }
    .dias-alerta  { color: #e67e22; font-weight: 600; }
    .dias-critico { color: #e74c3c; font-weight: 600; }

    .btn-ver { background: #3498db; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; font-size: .8em; white-space: nowrap; }
    .btn-ver:hover { background: #2980b9; }

    .vazio { text-align: center; padding: 60px; color: #7f8c8d; }
    .vazio p { font-size: 1.2em; }

    @media (max-width: 768px) {
      .area-filtros { flex-direction: column; }
      .resumo-bar { flex-direction: column; gap: 8px; }
      .dias-hospedado { color: #27ae60; font-weight: 700; }
    }
  `]
})
export class RankingHospedesApp implements OnInit {
  private http   = inject(HttpClient);
  private router = inject(Router);

  ranking: RankingHospede[] = [];
  loading = false;
  filtroInicio = '';
  filtroFim    = '';
  ordenacao    = 'hospedagens';

  ngOnInit(): void {
    this.buscar();
  }

  buscar(): void {
    this.loading = true;
    let params = new HttpParams();
    if (this.filtroInicio) params = params.set('inicio', this.filtroInicio);
    if (this.filtroFim)    params = params.set('fim',    this.filtroFim);

    this.http.get<RankingHospede[]>('/api/clientes/ranking', { params }).subscribe({
      next:  (data) => {
           this.ranking = data;
           this.ordenar();
           this.loading = false; },
      error: (err)  => { console.error('Erro ao carregar ranking', err); this.loading = false; }
    });
  }

  limpar(): void {
    this.filtroInicio = '';
    this.filtroFim    = '';
    this.buscar();
  }

  ordenar(): void {
  switch (this.ordenacao) {
    case 'hospedagens':
      this.ranking.sort((a, b) => b.totalHospedagens - a.totalHospedagens);
      break;
    case 'diarias':
      this.ranking.sort((a, b) => b.totalDiasHospedado - a.totalDiasHospedado);
      break;
    case 'gasto':
      this.ranking.sort((a, b) => b.totalGasto - a.totalGasto);
      break;
    case 'dias':
      this.ranking.sort((a, b) => b.diasDesdeUltimaHospedagem - a.diasDesdeUltimaHospedagem);
      break;
  }
}

  totalHospedagens(): number {
    return this.ranking.reduce((acc, h) => acc + h.totalHospedagens, 0);
  }

  totalGeral(): number {
    return this.ranking.reduce((acc, h) => acc + (h.totalGasto || 0), 0);
  }

  classeDias(dias: number): string {
  if (dias <= 0)   return 'dias-hospedado';
  if (dias <= 30)  return 'dias-ok';
  if (dias <= 90)  return 'dias-alerta';
  return 'dias-critico';
}

labelDias(dias: number): string {
  if (dias <= 0) return '🏨 Hospedado';
  return `${dias} dias`;
}

  formatarCpf(cpf: string): string {
    if (!cpf) return '';
    const n = cpf.replace(/\D/g, '');
    if (n.length !== 11) return cpf;
    return `${n.slice(0,3)}.${n.slice(3,6)}.${n.slice(6,9)}-${n.slice(9)}`;
  }

  formatarData(data: string): string {
    if (!data) return '—';
    return new Date(data).toLocaleDateString('pt-BR', {
      day: '2-digit', month: '2-digit', year: 'numeric'
    });
  }

  verHistorico(id: number): void {
    this.router.navigate(['/clientes', id, 'historico']);
  }

  voltar(): void {
    this.router.navigate(['/clientes']);
  }
}