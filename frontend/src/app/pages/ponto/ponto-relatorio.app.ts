import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { PontoService } from '../../services/ponto.service';

@Component({
  selector: 'app-ponto-relatorio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>📊 Relatório de Ponto</h1>
        <button class="btn-back" (click)="router.navigate(['/ponto'])">← Voltar</button>
      </div>

      <!-- ABAS -->
      <div class="abas">
        <button [class.ativa]="aba === 'registros'" (click)="aba = 'registros'; buscar()">📋 Registros</button>
        <button [class.ativa]="aba === 'horas'" (click)="aba = 'horas'; buscarHoras()">⏱️ Horas Trabalhadas</button>
      </div>

      <!-- FILTROS -->
      <div class="filtros-card">
        <div class="filtros">
          <div class="filtro-grupo">
            <label>Data Início</label>
            <input type="date" [(ngModel)]="dataInicio" />
          </div>
          <div class="filtro-grupo">
            <label>Data Fim</label>
            <input type="date" [(ngModel)]="dataFim" />
          </div>
          <div class="filtro-grupo" *ngIf="aba === 'horas'">
            <label>Funcionário</label>
            <div style="position:relative">
              <input type="text" [(ngModel)]="buscaFuncionario" (input)="filtrarFuncionarios()"
                     placeholder="Todos os funcionários..." style="width:200px" />
              <div class="lista-func" *ngIf="funcionariosFiltrados.length > 0">
                <div *ngFor="let f of funcionariosFiltrados" (click)="selecionarFuncionario(f)">
                  {{ f.nome }}
                </div>
                <div (click)="limparFuncionario()" style="color:#e74c3c">Todos</div>
              </div>
            </div>
            <small *ngIf="funcionarioSelecionado" style="color:#27ae60">
              ✅ {{ funcionarioSelecionado.nome }}
              <span style="cursor:pointer; color:#e74c3c" (click)="limparFuncionario()"> ✕</span>
            </small>
          </div>
          <button class="btn-buscar" (click)="aba === 'horas' ? buscarHoras() : buscar()">🔍 Buscar</button>
          <button class="btn-hoje" (click)="filtrarHoje()">📅 Hoje</button>
          <button class="btn-imprimir" (click)="imprimir()">🖨️ Imprimir</button>
        </div>
      </div>

      <!-- ABA REGISTROS -->
      <div class="relatorio-card" *ngIf="aba === 'registros'">
        <div *ngIf="loading" class="loading">Carregando...</div>
        <div *ngIf="!loading && registros.length === 0" class="empty">
          Nenhum registro encontrado para o período
        </div>
        <div class="tabela-wrapper" *ngIf="!loading && registros.length > 0">
          <table>
            <thead>
              <tr>
                <th>Data</th>
                <th>Hora</th>
                <th>Funcionário</th>
                <th>Tipo</th>
                <th>Reconh.</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let r of registros" [class]="'linha-' + r.tipo.toLowerCase()">
                <td>{{ formatarData(r.dataHora) }}</td>
                <td>{{ formatarHora(r.dataHora) }}</td>
                <td>{{ r.cliente?.nome }}</td>
                <td>
                  <span [class]="'badge badge-' + r.tipo.toLowerCase()">
                    {{ formatarTipo(r.tipo) }}
                  </span>
                </td>
                <td>{{ r.reconhecimentoFacial ? '🤖 Facial' : '👆 Manual' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="totais" *ngIf="registros.length > 0">
          <span>Total: <strong>{{ registros.length }}</strong></span>
          <span>Entradas: <strong>{{ contarTipo('ENTRADA') }}</strong></span>
          <span>Saídas: <strong>{{ contarTipo('SAIDA') }}</strong></span>
        </div>
      </div>

      <!-- ABA HORAS -->
      <div class="relatorio-card" *ngIf="aba === 'horas'">
        <div *ngIf="loading" class="loading">Carregando...</div>
        <div *ngIf="!loading && relatorioHoras.length === 0" class="empty">
          Nenhum registro encontrado para o período
        </div>
        <div class="tabela-wrapper" *ngIf="!loading && relatorioHoras.length > 0">
          <table>
            <thead>
              <tr>
                <th>Funcionário</th>
                <th>Data</th>
                <th>Registros do Dia</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let r of relatorioHoras"
                  [class.linha-total]="r.data === 'TOTAL'"
                  [class.linha-dia]="r.data !== 'TOTAL'">
                <td>{{ r.funcionario }}</td>
                <td>{{ r.data === 'TOTAL' ? '📊 TOTAL GERAL' : formatarDataStr(r.data) }}</td>
                <td>
                  <span *ngIf="r.data !== 'TOTAL'" class="registros-dia">
                    <span *ngFor="let reg of r.registros" [class]="'tag-' + reg.tipo.toLowerCase()">
                      {{ formatarTipo(reg.tipo) }} {{ reg.hora.substring(0,5) }}
                    </span>
                    <span *ngIf="r.registros.length === 0" class="sem-reg">Sem registros</span>
                  </span>
                </td>
                <td class="total-horas">{{ r.totalHoras }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 1200px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h1 { color: #333; margin: 0; }
    .btn-back { background: #6c757d; color: white; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }

    .abas { display: flex; gap: 10px; margin-bottom: 20px; }
    .abas button { padding: 10px 20px; border: 2px solid #ddd; background: white; border-radius: 5px; cursor: pointer; font-weight: 600; color: #666; }
    .abas button.ativa { background: #667eea; color: white; border-color: #667eea; }

    .filtros-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }
    .filtros { display: flex; gap: 15px; align-items: flex-end; flex-wrap: wrap; }
    .filtro-grupo { display: flex; flex-direction: column; gap: 5px; }
    .filtro-grupo label { font-weight: 600; color: #555; font-size: 14px; }
    .filtro-grupo input { padding: 8px 12px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; }
    .btn-buscar { background: #667eea; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; font-weight: 600; }
    .btn-hoje { background: #27ae60; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; font-weight: 600; }
    .btn-imprimir { background: #e67e22; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; font-weight: 600; }

    .lista-func { position: absolute; top: 100%; left: 0; right: 0; background: white; border: 1px solid #ddd; border-radius: 5px; max-height: 200px; overflow-y: auto; z-index: 100; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
    .lista-func div { padding: 8px 12px; cursor: pointer; }
    .lista-func div:hover { background: #f5f5f5; }

    .relatorio-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
    .tabela-wrapper { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; font-size: 14px; }
    th { background: #f8f9fa; padding: 10px; text-align: left; font-weight: 600; border-bottom: 2px solid #dee2e6; }
    td { padding: 10px; border-bottom: 1px solid #dee2e6; vertical-align: middle; }

    .linha-total { background: #e8f5e9 !important; font-weight: 700; border-top: 2px solid #27ae60; }
    .linha-dia:hover { background: #f8f9fa; }
    .total-horas { font-weight: 700; color: #27ae60; font-size: 15px; }

    .registros-dia { display: flex; flex-wrap: wrap; gap: 5px; }
    .tag-entrada { background: #d4edda; color: #155724; padding: 2px 8px; border-radius: 12px; font-size: 12px; }
    .tag-saida_intervalo { background: #fff3cd; color: #856404; padding: 2px 8px; border-radius: 12px; font-size: 12px; }
    .tag-retorno_intervalo { background: #d1ecf1; color: #0c5460; padding: 2px 8px; border-radius: 12px; font-size: 12px; }
    .tag-saida { background: #f8d7da; color: #721c24; padding: 2px 8px; border-radius: 12px; font-size: 12px; }
    .sem-reg { color: #999; font-size: 12px; }

    .linha-entrada { background: #f0fff4; }
    .linha-saida { background: #fff5f5; }
    .linha-saida_intervalo { background: #fffbf0; }
    .linha-retorno_intervalo { background: #f0f8ff; }

    .badge { padding: 3px 8px; border-radius: 12px; font-size: 12px; font-weight: 600; }
    .badge-entrada { background: #d4edda; color: #155724; }
    .badge-saida { background: #f8d7da; color: #721c24; }
    .badge-saida_intervalo { background: #fff3cd; color: #856404; }
    .badge-retorno_intervalo { background: #d1ecf1; color: #0c5460; }

    .totais { display: flex; gap: 20px; margin-top: 15px; padding-top: 15px; border-top: 2px solid #dee2e6; }
    .totais span { font-size: 14px; color: #555; }
    .loading, .empty { text-align: center; padding: 40px; color: #666; }

    @media print {
      .btn-back, .filtros-card, .btn-imprimir, .abas { display: none; }
      .container { padding: 0; }
    }
  `]
})
export class PontoRelatorioApp implements OnInit {
  router = inject(Router);
  private http = inject(HttpClient);
  private pontoService = inject(PontoService);

  aba: 'registros' | 'horas' = 'registros';
  dataInicio = '';
  dataFim = '';
  registros: any[] = [];
  relatorioHoras: any[] = [];
  loading = false;

  buscaFuncionario = '';
  funcionarios: any[] = [];
  funcionariosFiltrados: any[] = [];
  funcionarioSelecionado: any = null;

  ngOnInit(): void {
    this.filtrarHoje();
    this.carregarFuncionarios();
  }

  carregarFuncionarios(): void {
    this.http.get<any[]>('/api/clientes/funcionarios/buscar?termo=a').subscribe({
      next: (data) => this.funcionarios = data,
      error: () => {}
    });
  }

  filtrarFuncionarios(): void {
    if (this.buscaFuncionario.length < 2) { this.funcionariosFiltrados = []; return; }
    const termo = this.buscaFuncionario.toLowerCase();
    this.funcionariosFiltrados = this.funcionarios.filter(f =>
      f.nome.toLowerCase().includes(termo)
    );
  }

  selecionarFuncionario(f: any): void {
    this.funcionarioSelecionado = f;
    this.buscaFuncionario = f.nome;
    this.funcionariosFiltrados = [];
  }

  limparFuncionario(): void {
    this.funcionarioSelecionado = null;
    this.buscaFuncionario = '';
    this.funcionariosFiltrados = [];
  }

  filtrarHoje(): void {
    const hoje = new Date().toISOString().split('T')[0];
    this.dataInicio = hoje;
    this.dataFim = hoje;
    this.buscar();
  }

  buscar(): void {
    if (!this.dataInicio || !this.dataFim) return;
    this.loading = true;
    this.pontoService.listarPorPeriodo(this.dataInicio, this.dataFim).subscribe({
      next: (data) => { this.registros = data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  buscarHoras(): void {
    if (!this.dataInicio || !this.dataFim) return;
    this.loading = true;
    let url = `/api/ponto/relatorio-horas?inicio=${this.dataInicio}&fim=${this.dataFim}`;
    if (this.funcionarioSelecionado) url += `&clienteId=${this.funcionarioSelecionado.id}`;
    this.http.get<any[]>(url).subscribe({
      next: (data) => { this.relatorioHoras = data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  contarTipo(tipo: string): number {
    return this.registros.filter(r => r.tipo === tipo).length;
  }

  formatarData(dataHora: string): string {
    if (!dataHora) return '-';
    return new Date(dataHora).toLocaleDateString('pt-BR');
  }

  formatarDataStr(data: string): string {
    if (!data) return '-';
    const [ano, mes, dia] = data.split('-');
    return `${dia}/${mes}/${ano}`;
  }

  formatarHora(dataHora: string): string {
    if (!dataHora) return '-';
    return new Date(dataHora).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  formatarTipo(tipo: string): string {
    const labels: any = {
      ENTRADA: '🟢 Entrada',
      SAIDA_INTERVALO: '🟡 S.Int',
      RETORNO_INTERVALO: '🔵 R.Int',
      SAIDA: '🔴 Saída'
    };
    return labels[tipo] || tipo;
  }

  imprimir(): void {
    window.print();
  }
}