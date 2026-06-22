import { Component, OnInit, inject } from '@angular/core';

import { CommonModule } from '@angular/common';

import { FormsModule } from '@angular/forms';

import { Router } from '@angular/router';

import { HttpClient, HttpParams } from '@angular/common/http';


interface HospedeRanking {

  clienteId: number;

  nomeCliente: string;

  cpf: string;

  celular: string;

  empresa?: string;            // ← nova info, se o backend retornar

  totalHospedagens: number;

  totalDiasHospedado: number;

  mediaEstadia: number;

  primeiraHospedagem: string;

  ultimaHospedagem: string;

  diasDesdeUltimaHospedagem?: number | null;

  totalGasto: number;

  hospedado: boolean;

}


@Component({

  selector: 'app-cliente-ranking',

  standalone: true,

  imports: [CommonModule, FormsModule],

  template: `

    <div class="container">


      <!-- HEADER -->

      <div class="header">

        <h1>🏆 Ranking de Hóspedes</h1>

        <button class="btn-back" (click)="voltar()">← Voltar</button>

      </div>


      <!-- FILTROS -->

      <div class="filtros-card">


        <!-- LINHA 1: PERÍODO + BUSCAR -->

        <div class="filtros-row">

          <div class="filtro-field">

            <label>Check-in a partir de</label>

            <input type="date" [(ngModel)]="dataInicio" class="input-filtro" />

          </div>

          <div class="filtro-field">

            <label>Check-in até</label>

            <input type="date" [(ngModel)]="dataFim" class="input-filtro" />

          </div>

          <button class="btn-buscar" (click)="buscar()">🔍 Buscar</button>

        </div>


        <!-- LINHA 2: ORDENAR + 3 PESQUISAS -->

        <div class="filtros-row">

          <div class="filtro-field">

            <label>Ordenar por</label>

            <select [(ngModel)]="ordenarPor" (change)="buscar()" class="input-filtro">

              <option value="hospedagens">Quantidade de Hospedagens</option>

              <option value="diarias">Quantidade de Diárias</option>

              <option value="gasto">Total Gasto</option>

              <option value="diasSemHospedar">Dias sem hospedar</option>

            </select>

          </div>


          <div class="filtro-field">

            <label>🔍 Hóspede (nome)</label>

            <input

              type="text"

              [(ngModel)]="filtroNome"

              (input)="aplicarFiltros()"

              (keyup.enter)="aplicarFiltros()"

              placeholder="Buscar por nome..."

              class="input-filtro" />

          </div>


          <div class="filtro-field">

            <label>🔍 CPF</label>

            <input

              type="text"

              [(ngModel)]="filtroCpf"

              (input)="onCpfInput()"

              (keyup.enter)="aplicarFiltros()"

              placeholder="000.000.000-00"

              maxlength="14"

              class="input-filtro" />

          </div>


          <div class="filtro-field">

            <label>🔍 Empresa</label>

            <input

              type="text"

              [(ngModel)]="filtroEmpresa"

              (input)="aplicarFiltros()"

              (keyup.enter)="aplicarFiltros()"

              placeholder="Buscar por empresa..."

              class="input-filtro" />

          </div>


          <button

            class="btn-clear"

            (click)="limparFiltros()"

            *ngIf="temFiltroAtivo()"

            type="button">

            ✕ Limpar

          </button>

        </div>

      </div>


      <!-- STATS -->

      <div class="stats-row" *ngIf="!loading">

        <div class="stat-card">

          <div class="stat-icon">📊</div>

          <div class="stat-valor">{{ totalHospedes }}</div>

          <div class="stat-label">hóspedes encontrados</div>

        </div>

        <div class="stat-card">

          <div class="stat-icon">🏨</div>

          <div class="stat-valor">{{ totalHospedagens }}</div>

          <div class="stat-label">hospedagens no período</div>

        </div>

        <div class="stat-card">

          <div class="stat-icon">💰</div>

          <div class="stat-valor">{{ formatarMoeda(receitaTotal) }}</div>

          <div class="stat-label">em receita</div>

        </div>

      </div>


      <!-- LOADING -->

      <div *ngIf="loading" class="loading">

        <div class="spinner"></div>

        <span>Carregando ranking...</span>

      </div>


      <!-- TABELA -->

      <div class="table-card" *ngIf="!loading">


        <div *ngIf="hospedesFiltrados.length === 0" class="empty">

          <div class="empty-icon">🔍</div>

          <h3 *ngIf="temFiltroAtivo()">Nenhum hóspede encontrado com esses filtros</h3>

          <h3 *ngIf="!temFiltroAtivo()">Nenhum hóspede no período</h3>

          <button *ngIf="temFiltroAtivo()" class="link-button" (click)="limparFiltros()">

            Limpar filtros

          </button>

        </div>


        <div class="table-wrapper" *ngIf="hospedesFiltrados.length > 0">

          <table>

            <thead>

              <tr>

                <th class="col-pos">#</th>

                <th>Hóspede</th>

                <th>CPF</th>

                <th>Celular</th>

                <th>Empresa</th>

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

              <tr *ngFor="let h of hospedesFiltrados; let i = index" [class.top-3]="i < 3">

                <td class="col-pos">

                  <span *ngIf="i < 3" class="medal">{{ medalha(i) }}</span>

                  <span *ngIf="i >= 3" class="pos-numero">{{ i + 1 }}</span>

                </td>

                <td>

                  <div class="cliente-cell">

                    <strong>{{ h.nomeCliente }}</strong>

                  </div>

                </td>

                <td class="col-cpf">{{ formatarCpf(h.cpf) }}</td>

                <td>{{ h.celular || '—' }}</td>

                <td>{{ h.empresa || '—' }}</td>

                <td class="text-center"><strong>{{ h.totalHospedagens }}</strong></td>

                <td class="text-center">{{ h.totalDiasHospedado }}</td>

                <td class="text-center">{{ h.mediaEstadia | number:'1.1-1':'pt-BR' }}</td>

                <td>{{ formatarData(h.primeiraHospedagem) }}</td>

                <td>{{ formatarData(h.ultimaHospedagem) }}</td>

                <td class="text-center">

                  <span *ngIf="h.hospedado" class="badge-hospedado">🏨 Hospedado</span>

                  <span *ngIf="!h.hospedado && h.diasDesdeUltimaHospedagem != null">

                    {{ h.diasDesdeUltimaHospedagem }} dias

                  </span>

                  <span *ngIf="!h.hospedado && h.diasDesdeUltimaHospedagem == null">—</span>

                </td>

                <td class="col-valor">{{ formatarMoeda(h.totalGasto) }}</td>

                <td>

                  <button class="btn-historico" (click)="verHistorico(h.clienteId)" title="Ver histórico">

                    📋 Histórico

                  </button>

                </td>

              </tr>

            </tbody>

          </table>

        </div>

      </div>


    </div>

  `,

  styles: [`

    .container { padding: 20px; max-width: 1700px; margin: 0 auto; }

    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }

    h1 { color: #2c3e50; margin: 0; font-size: 24px; }

    .btn-back { background: #6c757d; color: white; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; font-size: 14px; }

    .btn-back:hover { background: #5a6268; }


    /* FILTROS */

    .filtros-card {

      background: white;

      padding: 20px;

      border-radius: 8px;

      box-shadow: 0 2px 4px rgba(0,0,0,0.1);

      margin-bottom: 20px;

      border-left: 4px solid #667eea;

    }

    .filtros-row {

      display: grid;

      gap: 12px;

      align-items: end;

      margin-bottom: 12px;

    }

    .filtros-row:last-child { margin-bottom: 0; }

    .filtros-row:nth-child(1) { grid-template-columns: 1fr 1fr auto; }

    .filtros-row:nth-child(2) { grid-template-columns: 1fr 1fr 1fr 1fr auto; }


    .filtro-field { display: flex; flex-direction: column; min-width: 0; }

    .filtro-field label {

      font-size: 13px;

      color: #666;

      margin-bottom: 5px;

      font-weight: 500;

    }

    .input-filtro {

      padding: 9px 12px;

      border: 1px solid #ddd;

      border-radius: 5px;

      font-size: 14px;

      background: white;

      transition: all 0.15s;

    }

    .input-filtro:focus {

      outline: none;

      border-color: #667eea;

      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);

    }

    .btn-buscar {

      background: #667eea;

      color: white;

      border: none;

      padding: 9px 18px;

      border-radius: 5px;

      cursor: pointer;

      font-size: 14px;

      white-space: nowrap;

      height: 38px;

      font-weight: 500;

    }

    .btn-buscar:hover { background: #5568d3; }

    .btn-clear {

      background: #dc3545;

      color: white;

      border: none;

      padding: 9px 14px;

      border-radius: 5px;

      cursor: pointer;

      font-size: 14px;

      white-space: nowrap;

      height: 38px;

      font-weight: 500;

    }

    .btn-clear:hover { background: #c82333; }


    /* STATS */

    .stats-row {

      display: grid;

      grid-template-columns: repeat(3, 1fr);

      gap: 15px;

      margin-bottom: 20px;

    }

    .stat-card {

      background: white;

      padding: 22px;

      border-radius: 8px;

      box-shadow: 0 2px 4px rgba(0,0,0,0.1);

      text-align: center;

      border-top: 4px solid #667eea;

    }

    .stat-card:nth-child(1) { border-top-color: #3498db; }

    .stat-card:nth-child(2) { border-top-color: #27ae60; }

    .stat-card:nth-child(3) { border-top-color: #f39c12; }

    .stat-icon { font-size: 2em; margin-bottom: 8px; }

    .stat-valor { font-size: 1.8em; font-weight: 700; color: #2c3e50; line-height: 1; margin-bottom: 6px; }

    .stat-label { font-size: 0.85em; color: #7f8c8d; }


    /* LOADING */

    .loading { text-align: center; padding: 60px; color: #666; display: flex; flex-direction: column; align-items: center; gap: 15px; }

    .spinner {

      width: 40px;

      height: 40px;

      border: 4px solid #f3f3f3;

      border-top: 4px solid #667eea;

      border-radius: 50%;

      animation: spin 0.8s linear infinite;

    }

    @keyframes spin { to { transform: rotate(360deg); } }


    /* TABELA */

    .table-card {

      background: white;

      border-radius: 8px;

      box-shadow: 0 2px 4px rgba(0,0,0,0.1);

      overflow: hidden;

    }

    .table-wrapper { overflow-x: auto; }

    table { width: 100%; border-collapse: collapse; min-width: 1400px; }

    th {

      background: #f8f9fa;

      padding: 12px;

      text-align: left;

      font-size: 12px;

      color: #555;

      text-transform: uppercase;

      font-weight: 600;

      white-space: nowrap;

      border-bottom: 2px solid #dee2e6;

    }

    td {

      padding: 12px;

      border-bottom: 1px solid #eee;

      font-size: 14px;

    }

    tbody tr { transition: background 0.15s; }

    tbody tr:hover { background: #f8f9fa; }

    tbody tr.top-3 { background: linear-gradient(90deg, rgba(255, 215, 0, 0.05), transparent); }

    tbody tr.top-3:hover { background: linear-gradient(90deg, rgba(255, 215, 0, 0.1), #f8f9fa); }


    .col-pos { width: 60px; text-align: center; }

    .col-cpf { font-family: 'Courier New', monospace; white-space: nowrap; }

    .col-valor { font-weight: 700; color: #27ae60; white-space: nowrap; }

    .text-center { text-align: center; }


    .medal { font-size: 1.6em; }

    .pos-numero { color: #666; font-weight: 600; }

    .cliente-cell strong { color: #2c3e50; }


    .badge-hospedado {

      background: #d4edda;

      color: #155724;

      padding: 4px 10px;

      border-radius: 12px;

      font-size: 11px;

      font-weight: 600;

      white-space: nowrap;

      display: inline-block;

    }


    .btn-historico {

      background: #3498db;

      color: white;

      border: none;

      padding: 6px 12px;

      border-radius: 4px;

      cursor: pointer;

      font-size: 12px;

      white-space: nowrap;

    }

    .btn-historico:hover { background: #2980b9; }


    /* ESTADO VAZIO */

    .empty { text-align: center; padding: 80px 20px; color: #999; }

    .empty-icon { font-size: 72px; margin-bottom: 15px; }

    .empty h3 { color: #666; font-weight: 500; margin-bottom: 15px; }

    .link-button {

      background: none;

      border: none;

      color: #667eea;

      cursor: pointer;

      text-decoration: underline;

      padding: 6px 12px;

      font-size: 14px;

    }

    .link-button:hover { color: #5568d3; }


    @media (max-width: 900px) {

      .filtros-row:nth-child(1),

      .filtros-row:nth-child(2) { grid-template-columns: 1fr; }

      .stats-row { grid-template-columns: 1fr; }

    }

  `]

})

export class ClienteRankingComponent implements OnInit {

  private http = inject(HttpClient);

  private router = inject(Router);


  loading = false;

  hospedes: HospedeRanking[] = [];

  hospedesFiltrados: HospedeRanking[] = [];


  // Período

  dataInicio = '';

  dataFim = '';


  // Ordenação

  ordenarPor: 'hospedagens' | 'diarias' | 'gasto' | 'diasSemHospedar' = 'hospedagens';


  // Filtros de pesquisa local

  filtroNome = '';

  filtroCpf = '';

  filtroEmpresa = '';


  // Stats (vindas do backend)

  totalHospedes = 0;

  totalHospedagens = 0;

  receitaTotal = 0;


  ngOnInit(): void {

    // Padrão: últimos 12 meses

    const hoje = new Date();

    const umAnoAtras = new Date();

    umAnoAtras.setFullYear(hoje.getFullYear() - 1);

    this.dataFim = this.toIsoDate(hoje);

    this.dataInicio = this.toIsoDate(umAnoAtras);

    this.buscar();

  }


  /**

   * Busca o ranking no backend com os filtros de período e ordenação.

   * A pesquisa por nome/CPF/empresa é feita localmente no front.

   */

  buscar(): void {

    this.loading = true;

    const params = new HttpParams()

      .set('dataInicio', this.dataInicio)

      .set('dataFim', this.dataFim)

      .set('ordenarPor', this.ordenarPor);


    this.http.get<any>(`/api/clientes/ranking`, { params }).subscribe({

      next: (data) => {

        this.hospedes = data.hospedes || data || [];

        this.totalHospedes = data.totalHospedes ?? this.hospedes.length;

        this.totalHospedagens = data.totalHospedagens ?? 0;

        this.receitaTotal = data.receitaTotal ?? 0;

        this.aplicarFiltros();

        this.loading = false;

      },

      error: (err) => {

        console.error('Erro ao carregar ranking', err);

        this.loading = false;

        this.hospedes = [];

        this.hospedesFiltrados = [];

      }

    });

  }


  /**

   * Filtra a lista carregada por nome, CPF e/ou empresa.

   * Combinação: TODOS os critérios precisam casar (AND).

   */

  aplicarFiltros(): void {

    const nome = this.filtroNome.toLowerCase().trim();

    const cpfFiltro = this.filtroCpf.replace(/\D/g, '');

    const empresa = this.filtroEmpresa.toLowerCase().trim();


    if (!nome && !cpfFiltro && !empresa) {

      this.hospedesFiltrados = [...this.hospedes];

      return;

    }


    this.hospedesFiltrados = this.hospedes.filter((h) => {

      const matchNome = !nome || (h.nomeCliente || '').toLowerCase().includes(nome);


      const cpfHospede = (h.cpf || '').replace(/\D/g, '');

      const matchCpf = !cpfFiltro || cpfHospede.includes(cpfFiltro);


      const matchEmpresa =

        !empresa || (h.empresa || '').toLowerCase().includes(empresa);


      return matchNome && matchCpf && matchEmpresa;

    });

  }


  onCpfInput(): void {

    if (!this.filtroCpf) {

      this.aplicarFiltros();

      return;

    }

    let numeros = this.filtroCpf.replace(/\D/g, '');

    if (numeros.length > 11) numeros = numeros.substring(0, 11);


    let cpf = numeros;

    if (numeros.length > 9) {

      cpf =

        numeros.substring(0, 3) + '.' +

        numeros.substring(3, 6) + '.' +

        numeros.substring(6, 9) + '-' +

        numeros.substring(9);

    } else if (numeros.length > 6) {

      cpf =

        numeros.substring(0, 3) + '.' +

        numeros.substring(3, 6) + '.' +

        numeros.substring(6);

    } else if (numeros.length > 3) {

      cpf = numeros.substring(0, 3) + '.' + numeros.substring(3);

    }

    this.filtroCpf = cpf;

    this.aplicarFiltros();

  }


  temFiltroAtivo(): boolean {

    return !!(this.filtroNome || this.filtroCpf || this.filtroEmpresa);

  }


  limparFiltros(): void {

    this.filtroNome = '';

    this.filtroCpf = '';

    this.filtroEmpresa = '';

    this.hospedesFiltrados = [...this.hospedes];

  }


  // ================== HELPERS ==================


  formatarCpf(cpf: string): string {

    if (!cpf) return '—';

    const n = cpf.replace(/\D/g, '');

    if (n.length !== 11) return cpf;

    return n.substring(0, 3) + '.' +

           n.substring(3, 6) + '.' +

           n.substring(6, 9) + '-' +

           n.substring(9);

  }


  formatarData(data: string): string {

    if (!data) return '—';

    return new Date(data).toLocaleDateString('pt-BR');

  }


  formatarMoeda(valor: number): string {

    return (valor || 0).toLocaleString('pt-BR', {

      style: 'currency',

      currency: 'BRL'

    });

  }


  medalha(posicao: number): string {

    if (posicao === 0) return '🥇';

    if (posicao === 1) return '🥈';

    if (posicao === 2) return '🥉';

    return '';

  }


  toIsoDate(d: Date): string {

    const yyyy = d.getFullYear();

    const mm = String(d.getMonth() + 1).padStart(2, '0');

    const dd = String(d.getDate()).padStart(2, '0');

    return `${yyyy}-${mm}-${dd}`;

  }


  verHistorico(clienteId: number): void {

    this.router.navigate(['/clientes', clienteId, 'historico']);

  }


  voltar(): void {

    this.router.navigate(['/clientes']);

  }

}