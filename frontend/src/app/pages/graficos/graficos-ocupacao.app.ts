import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-graficos-ocupacao',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>📊 Gráficos de Ocupação</h1>
      </div>

      <!-- FILTROS -->
      <div class="filtros">
        <div class="filtro-grupo">
          <label>Data Início</label>
          <input type="date" [(ngModel)]="dataInicio" />
        </div>
        <div class="filtro-grupo">
          <label>Data Fim</label>
          <input type="date" [(ngModel)]="dataFim" />
        </div>
        <div class="filtro-grupo">
          <label>Tipo</label>
          <select [(ngModel)]="filtroTipo">
            <option value="">Todos</option>
            <option value="A">Tipo A</option>
            <option value="B">Tipo B</option>
            <option value="C">Tipo C</option>
            <option value="L">Tipo L</option>
          </select>
        </div>
        <button class="btn-buscar" (click)="carregar()">🔍 Buscar</button>
      </div>

      <div *ngIf="loading" class="loading">Carregando...</div>

      <div *ngIf="!loading && dados.length > 0">

        <!-- RESUMO -->
        <div class="resumo-cards">
          <div class="resumo-card">
            <span class="resumo-label">Apartamentos</span>
            <span class="resumo-valor">{{ dadosFiltrados.length }}</span>
          </div>
          <div class="resumo-card">
            <span class="resumo-label">Total Reservas</span>
            <span class="resumo-valor">{{ totalReservas }}</span>
          </div>
          <div class="resumo-card">
            <span class="resumo-label">Média Ocupação</span>
            <span class="resumo-valor">{{ mediaOcupacao }}%</span>
          </div>
          <div class="resumo-card">
            <span class="resumo-label">Receita Total</span>
            <span class="resumo-valor">R$ {{ formatarMoeda(receitaTotal) }}</span>
          </div>
        </div>

        <!-- GRÁFICO DE BARRAS — DIAS OCUPADOS -->
        <div class="card-grafico">
          <h2>🏨 Dias Ocupados por Apartamento</h2>
          <div class="grafico-barras">
            <div class="barra-item" *ngFor="let item of dadosFiltrados">
              <div class="barra-label">{{ item.numeroApartamento }}</div>
              <div class="barra-container">
                <div class="barra"
                     [style.width.%]="item.taxaOcupacao > 100 ? 100 : item.taxaOcupacao"
                     [style.background]="getCorBarra(item.taxaOcupacao)"
                     [title]="item.diasOcupados + ' dias — ' + item.taxaOcupacao.toFixed(1) + '%'">
                </div>
                <span class="barra-valor">{{ item.diasOcupados }}d ({{ item.taxaOcupacao.toFixed(0) }}%)</span>
              </div>
              <div class="barra-tipo">{{ item.tipoApartamento }}</div>
            </div>
          </div>
        </div>

        <!-- TABELA -->
        <div class="card-grafico">
          <h2>📋 Detalhamento</h2>
          <table class="tabela">
            <thead>
              <tr>
                <th>Apartamento</th>
                <th>Tipo</th>
                <th>Reservas</th>
                <th>Dias Ocupados</th>
                <th>Taxa Ocupação</th>
                <th>Receita Total</th>
                <th>Receita Média</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of dadosFiltrados"
                  [class.linha-alta]="item.taxaOcupacao >= 70"
                  [class.linha-media]="item.taxaOcupacao >= 30 && item.taxaOcupacao < 70"
                  [class.linha-baixa]="item.taxaOcupacao < 30">
                <td><strong>{{ item.numeroApartamento }}</strong></td>
                <td><span class="badge-tipo badge-tipo-{{ item.tipoApartamento }}">{{ item.tipoApartamento }}</span></td>
                <td>{{ item.quantidadeReservas }}</td>
                <td>{{ item.diasOcupados }} dias</td>
                <td>
                  <div class="taxa-bar">
                    <div class="taxa-fill"
                         [style.width.%]="item.taxaOcupacao > 100 ? 100 : item.taxaOcupacao"
                         [style.background]="getCorBarra(item.taxaOcupacao)">
                    </div>
                    <span>{{ item.taxaOcupacao.toFixed(1) }}%</span>
                  </div>
                </td>
                <td>R$ {{ formatarMoeda(item.receitaTotal) }}</td>
                <td>R$ {{ item.quantidadeReservas > 0 ? formatarMoeda(item.receitaTotal / item.quantidadeReservas) : '0,00' }}</td>
              </tr>
            </tbody>
          </table>
        </div>

      </div>

      <div *ngIf="!loading && dados.length === 0" class="empty">
        Nenhum dado encontrado para o período selecionado.
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 1400px; margin: 0 auto; }
    .header { margin-bottom: 20px; }
    h1 { color: #333; margin: 0; }

    .filtros { display: flex; gap: 15px; align-items: flex-end; margin-bottom: 20px; flex-wrap: wrap; }
    .filtro-grupo { display: flex; flex-direction: column; gap: 5px; }
    .filtro-grupo label { font-size: 13px; font-weight: 600; color: #555; }
    .filtro-grupo input, .filtro-grupo select { padding: 8px 12px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; }
    .btn-buscar { background: #667eea; color: white; border: none; padding: 8px 20px; border-radius: 5px; cursor: pointer; font-weight: 600; }

    .loading { text-align: center; padding: 40px; color: #666; }
    .empty { text-align: center; padding: 40px; color: #666; }

    .resumo-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; margin-bottom: 20px; }
    .resumo-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); display: flex; flex-direction: column; align-items: center; }
    .resumo-label { font-size: 13px; color: #666; margin-bottom: 8px; }
    .resumo-valor { font-size: 22px; font-weight: bold; color: #2c3e50; }

    .card-grafico { background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); padding: 20px; margin-bottom: 20px; }
    .card-grafico h2 { margin: 0 0 20px; color: #333; font-size: 18px; }

    .grafico-barras { display: flex; flex-direction: column; gap: 8px; }
    .barra-item { display: flex; align-items: center; gap: 10px; }
    .barra-label { width: 50px; font-size: 12px; font-weight: 600; text-align: right; }
    .barra-container { flex: 1; display: flex; align-items: center; gap: 8px; }
    .barra { height: 24px; border-radius: 4px; min-width: 2px; transition: width 0.3s; }
    .barra-valor { font-size: 12px; color: #555; white-space: nowrap; }
    .barra-tipo { width: 25px; font-size: 11px; color: #888; }

    .tabela { width: 100%; border-collapse: collapse; }
    .tabela th { background: #f8f9fa; padding: 12px; text-align: left; font-weight: 600; border-bottom: 2px solid #dee2e6; font-size: 13px; }
    .tabela td { padding: 10px 12px; border-bottom: 1px solid #dee2e6; font-size: 13px; }
    .tabela tr:hover { background: #f8f9fa; }
    .linha-alta { background: #f0fff4; }
    .linha-media { background: #fffbf0; }
    .linha-baixa { background: #fff5f5; }

    .badge-tipo { padding: 3px 8px; border-radius: 12px; font-size: 12px; font-weight: 600; }
    .badge-tipo-A { background: #dbeafe; color: #1d4ed8; }
    .badge-tipo-B { background: #dcfce7; color: #15803d; }
    .badge-tipo-C { background: #fef3c7; color: #b45309; }
    .badge-tipo-L { background: #f3e8ff; color: #7e22ce; }

    .taxa-bar { position: relative; height: 20px; background: #f0f0f0; border-radius: 10px; overflow: hidden; display: flex; align-items: center; min-width: 120px; }
    .taxa-fill { position: absolute; left: 0; top: 0; height: 100%; border-radius: 10px; }
    .taxa-bar span { position: relative; z-index: 1; font-size: 12px; font-weight: 600; padding: 0 8px; }
  `]
})
export class GraficosOcupacaoApp implements OnInit {
  dados: any[] = [];
  dadosFiltrados: any[] = [];
  loading = false;
  filtroTipo = '';

  dataInicio = new Date(new Date().getFullYear(), new Date().getMonth(), 1)
    .toISOString().split('T')[0];
  dataFim = new Date().toISOString().split('T')[0];

  totalReservas = 0;
  mediaOcupacao = 0;
  receitaTotal = 0;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading = true;
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });

    this.http.get<any[]>(
      `/api/relatorios/apartamentos?dataInicio=${this.dataInicio}&dataFim=${this.dataFim}`,
      { headers }
    ).subscribe({
      next: (data) => {
        this.dados = data;
        this.aplicarFiltro();
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  aplicarFiltro(): void {
    this.dadosFiltrados = this.filtroTipo
      ? this.dados.filter(d => d.tipoApartamento === this.filtroTipo)
      : [...this.dados];

    this.dadosFiltrados.sort((a, b) => b.diasOcupados - a.diasOcupados);

    this.totalReservas = this.dadosFiltrados.reduce((s, d) => s + d.quantidadeReservas, 0);
    this.receitaTotal = this.dadosFiltrados.reduce((s, d) => s + (d.receitaTotal || 0), 0);
    const media = this.dadosFiltrados.length > 0
      ? this.dadosFiltrados.reduce((s, d) => s + Math.min(d.taxaOcupacao, 100), 0) / this.dadosFiltrados.length
      : 0;
    this.mediaOcupacao = parseFloat(media.toFixed(1));
  }

  getCorBarra(taxa: number): string {
    if (taxa >= 70) return '#27ae60';
    if (taxa >= 40) return '#f39c12';
    return '#e74c3c';
  }

  formatarMoeda(valor: number): string {
    return (valor || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}