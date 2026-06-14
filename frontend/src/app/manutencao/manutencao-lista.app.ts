import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

// 🔧 AJUSTE os caminhos conforme sua estrutura de pastas
import { ManutencaoService } from '../services/manutencao.service';
import { ApartamentoService } from '../services/apartamento.service';
import {
  Manutencao,
  TipoServico,
  StatusManutencao,
  TIPOS_SERVICO,
  STATUS_MANUTENCAO,
} from '../models/manutencao.model';

@Component({
  selector: 'app-manutencao-lista',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>🔧 Histórico de Manutenção</h1>
        <button class="btn-novo" (click)="nova()">➕ Nova Manutenção</button>
      </div>

      <!-- RESUMO -->
      <div class="resumo-cards">
        <div class="card-status pendente">
          <div class="card-icon">⏳</div>
          <div class="card-info">
            <span class="card-numero">{{ contar('PENDENTE') }}</span>
            <span class="card-label">Pendentes</span>
          </div>
        </div>
        <div class="card-status andamento">
          <div class="card-icon">🔄</div>
          <div class="card-info">
            <span class="card-numero">{{ contar('EM_ANDAMENTO') }}</span>
            <span class="card-label">Em Andamento</span>
          </div>
        </div>
        <div class="card-status concluido">
          <div class="card-icon">✅</div>
          <div class="card-info">
            <span class="card-numero">{{ contar('CONCLUIDO') }}</span>
            <span class="card-label">Concluídas</span>
          </div>
        </div>
        <div class="card-status custo">
          <div class="card-icon">💰</div>
          <div class="card-info">
            <span class="card-numero">{{ totalGasto() | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</span>
            <span class="card-label">Total Gasto</span>
          </div>
        </div>
      </div>

      <!-- FILTROS -->
      <div class="area-filtros">
        <div class="filtro-grupo">
          <label>Apartamento:</label>
          <select [(ngModel)]="filtroApartamentoId" (change)="buscar()">
            <option [ngValue]="null">Todos</option>
            <option *ngFor="let apt of apartamentos" [ngValue]="apt.id">
              {{ apt.numeroApartamento }}
            </option>
          </select>
        </div>

        <div class="filtro-grupo">
          <label>Tipo de Serviço:</label>
          <select [(ngModel)]="filtroTipo" (change)="buscar()">
            <option value="">Todos</option>
            <option *ngFor="let t of tipos" [value]="t.valor">{{ t.icone }} {{ t.descricao }}</option>
          </select>
        </div>

        <div class="filtro-grupo">
          <label>Status:</label>
          <select [(ngModel)]="filtroStatus" (change)="buscar()">
            <option value="">Todos</option>
            <option *ngFor="let s of statusList" [value]="s.valor">{{ s.descricao }}</option>
          </select>
        </div>

        <div class="filtro-grupo">
          <label>De:</label>
          <input type="date" [(ngModel)]="filtroInicio" (change)="buscar()" />
        </div>

        <div class="filtro-grupo">
          <label>Até:</label>
          <input type="date" [(ngModel)]="filtroFim" (change)="buscar()" />
        </div>

        <button class="btn-limpar" *ngIf="temFiltros()" (click)="limpar()">🗑️ Limpar</button>
      </div>

      <!-- LOADING -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Carregando...</p>
      </div>

      <!-- TABELA -->
      <div *ngIf="!loading && manutencoes.length > 0" class="tabela-wrapper">
        <table class="tabela">
          <thead>
            <tr>
              <th>Data</th>
              <th>Apto</th>
              <th>Tipo</th>
              <th>Descrição</th>
              <th>Responsável</th>
              <th>Custo</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let m of manutencoes">
              <td>{{ formatarData(m.dataServico) }}</td>
              <td><strong>{{ m.apartamentoNumero }}</strong></td>
              <td>{{ iconeTipo(m.tipoServico) }} {{ m.tipoServicoDescricao }}</td>
              <td class="col-desc">{{ m.descricao }}</td>
              <td>{{ m.responsavel || '—' }}</td>
              <td>{{ m.custo != null ? (m.custo | currency:'BRL':'symbol':'1.2-2':'pt-BR') : '—' }}</td>
              <td>
                <span [class]="'badge badge-' + m.status.toLowerCase()">
                  {{ m.statusDescricao }}
                </span>
              </td>
              <td class="col-acoes">
                <button *ngIf="m.status !== 'CONCLUIDO' && m.status !== 'CANCELADO'"
                        class="btn-mini btn-ok" title="Concluir" (click)="concluir(m)">✅</button>
                <button class="btn-mini btn-edit" title="Editar" (click)="editar(m.id)">✏️</button>
                <button class="btn-mini btn-del" title="Excluir" (click)="excluir(m)">🗑️</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- VAZIO -->
      <div *ngIf="!loading && manutencoes.length === 0" class="vazio">
        <p>📭 Nenhuma manutenção encontrada</p>
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 1600px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
    h1 { color: #2c3e50; margin: 0; }
    .btn-novo {
      background: #667eea; color: white; border: none; padding: 10px 20px;
      border-radius: 6px; cursor: pointer; font-size: 14px; font-weight: 600; transition: all .3s;
    }
    .btn-novo:hover { background: #5568d3; transform: translateY(-2px); }

    .resumo-cards {
      display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 15px; margin-bottom: 25px;
    }
    .card-status {
      background: white; border-radius: 12px; padding: 18px; display: flex; align-items: center;
      gap: 15px; box-shadow: 0 2px 8px rgba(0,0,0,.1); border-left: 4px solid;
    }
    .card-status.pendente  { border-left-color: #f39c12; }
    .card-status.andamento { border-left-color: #3498db; }
    .card-status.concluido { border-left-color: #27ae60; }
    .card-status.custo     { border-left-color: #9b59b6; }
    .card-icon { font-size: 2.2em; }
    .card-numero { font-size: 1.8em; font-weight: 700; color: #2c3e50; line-height: 1; }
    .card-label { font-size: .85em; color: #7f8c8d; margin-top: 4px; display: block; }

    .area-filtros {
      background: white; padding: 18px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,.1);
      margin-bottom: 25px; display: flex; gap: 12px; flex-wrap: wrap; align-items: flex-end;
    }
    .filtro-grupo { flex: 1; min-width: 150px; }
    .filtro-grupo label { display: block; margin-bottom: 6px; font-weight: 600; color: #2c3e50; font-size: .85em; }
    .filtro-grupo select, .filtro-grupo input {
      width: 100%; padding: 9px; border: 2px solid #e0e0e0; border-radius: 6px; font-size: .95em;
    }
    .filtro-grupo select:focus, .filtro-grupo input:focus { outline: none; border-color: #3498db; }
    .btn-limpar {
      padding: 9px 16px; background: #e74c3c; color: white; border: none; border-radius: 6px;
      cursor: pointer; font-weight: 600; white-space: nowrap;
    }
    .btn-limpar:hover { background: #c0392b; }

    .loading { text-align: center; padding: 60px; }
    .spinner {
      border: 4px solid #f3f3f3; border-top: 4px solid #3498db; border-radius: 50%;
      width: 50px; height: 50px; animation: spin 1s linear infinite; margin: 0 auto 20px;
    }
    @keyframes spin { 0% { transform: rotate(0); } 100% { transform: rotate(360deg); } }

    .tabela-wrapper { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,.1); overflow-x: auto; }
    .tabela { width: 100%; border-collapse: collapse; }
    .tabela th {
      background: #f8f9fa; padding: 12px; text-align: left; font-size: .85em;
      color: #2c3e50; text-transform: uppercase; border-bottom: 2px solid #e0e0e0;
    }
    .tabela td { padding: 12px; border-bottom: 1px solid #f0f0f0; font-size: .9em; color: #555; }
    .tabela tr:hover { background: #f8f9fa; }
    .col-desc { max-width: 320px; }
    .col-acoes { white-space: nowrap; }

    .badge { padding: 4px 10px; border-radius: 12px; font-size: .75em; font-weight: 600; text-transform: uppercase; }
    .badge-pendente     { background: #fff3cd; color: #856404; }
    .badge-em_andamento { background: #e3f2fd; color: #1565c0; }
    .badge-concluido    { background: #d4edda; color: #155724; }
    .badge-cancelado    { background: #f8d7da; color: #721c24; }

    .btn-mini {
      border: none; border-radius: 5px; padding: 6px 9px; cursor: pointer;
      font-size: .9em; margin-right: 4px; transition: all .2s;
    }
    .btn-mini:hover { transform: translateY(-2px); }
    .btn-ok   { background: #d4edda; }
    .btn-edit { background: #fff3cd; }
    .btn-del  { background: #f8d7da; }

    .vazio { text-align: center; padding: 60px; color: #7f8c8d; }
    .vazio p { font-size: 1.2em; }

    @media (max-width: 768px) {
      .resumo-cards { grid-template-columns: repeat(2, 1fr); }
      .area-filtros { flex-direction: column; }
      .filtro-grupo { width: 100%; }
    }
  `]
})
export class ManutencaoListaApp implements OnInit {
  private manutencaoService = inject(ManutencaoService);
  private apartamentoService = inject(ApartamentoService);
  private router = inject(Router);

  manutencoes: Manutencao[] = [];
  apartamentos: any[] = [];

  tipos = TIPOS_SERVICO;
  statusList = STATUS_MANUTENCAO;

  filtroApartamentoId: number | null = null;
  filtroTipo: TipoServico | '' = '';
  filtroStatus: StatusManutencao | '' = '';
  filtroInicio = '';
  filtroFim = '';

  loading = true;

  ngOnInit(): void {
    this.apartamentoService.getAll().subscribe({
      next: (data: any[]) => {
        this.apartamentos = [...data].sort((a, b) =>
          a.numeroApartamento.localeCompare(b.numeroApartamento, 'pt-BR', { numeric: true })
        );
      },
      error: (err) => console.error('Erro ao carregar apartamentos', err),
    });
    this.buscar();
  }

  buscar(): void {
    this.loading = true;
    this.manutencaoService.buscar({
      apartamentoId: this.filtroApartamentoId ?? undefined,
      tipoServico: this.filtroTipo,
      status: this.filtroStatus,
      inicio: this.filtroInicio || undefined,
      fim: this.filtroFim || undefined,
    }).subscribe({
      next: (data) => { this.manutencoes = data; this.loading = false; },
      error: (err) => { console.error('Erro ao buscar manutenções', err); this.loading = false; },
    });
  }

  temFiltros(): boolean {
    return !!(this.filtroApartamentoId || this.filtroTipo || this.filtroStatus || this.filtroInicio || this.filtroFim);
  }

  limpar(): void {
    this.filtroApartamentoId = null;
    this.filtroTipo = '';
    this.filtroStatus = '';
    this.filtroInicio = '';
    this.filtroFim = '';
    this.buscar();
  }

  contar(status: StatusManutencao): number {
    return this.manutencoes.filter(m => m.status === status).length;
  }

  totalGasto(): number {
    return this.manutencoes.reduce((acc, m) => acc + (m.custo || 0), 0);
  }

  iconeTipo(tipo: TipoServico): string {
    return this.tipos.find(t => t.valor === tipo)?.icone || '🔧';
  }

  formatarData(data: string): string {
    if (!data) return '—';
    // dataServico vem como yyyy-MM-dd; evita problema de fuso somando T12:00
    return new Date(data + 'T12:00:00').toLocaleDateString('pt-BR');
  }

  concluir(m: Manutencao): void {
    if (!confirm(`Concluir a manutenção do apartamento ${m.apartamentoNumero}?`)) return;
    this.manutencaoService.concluir(m.id).subscribe({
      next: () => this.buscar(),
      error: (err) => { console.error('Erro ao concluir', err); alert('Erro ao concluir manutenção'); },
    });
  }

  excluir(m: Manutencao): void {
    if (!confirm(`Excluir este registro de manutenção (Apto ${m.apartamentoNumero})?`)) return;
    this.manutencaoService.delete(m.id).subscribe({
      next: () => this.buscar(),
      error: (err) => { console.error('Erro ao excluir', err); alert('Erro ao excluir manutenção'); },
    });
  }

  nova(): void { this.router.navigate(['/manutencoes/novo']); }
  editar(id: number): void { this.router.navigate(['/manutencoes/editar', id]); }
}