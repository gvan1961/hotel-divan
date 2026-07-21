import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Vale, StatusVale, TipoVale, STATUS_VALE_LABELS, TIPO_VALE_LABELS } from '../../models/vale.model';
import { ClienteService } from '../../services/cliente.service';
import { Cliente } from '../../models/cliente.model';

interface ValeAgrupado {
  funcionario: string;
  clienteId: number;
  vales: Vale[];
  total: number;
}

@Component({
  selector: 'app-vale-relatorio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <button class="btn-voltar" (click)="voltar()">← Voltar</button>
        <h1>📊 Relatório de Vales</h1>
        <button class="btn-imprimir" (click)="imprimir()" [disabled]="valesAgrupados.length === 0">
          🖨️ Imprimir
        </button>
      </div>

      <!-- FILTROS -->
      <div class="card-filtros">
        <h3>🔍 Filtros</h3>
        <div class="filtros-grid">

          <div class="campo">
            <label>Status</label>
            <select [(ngModel)]="filtros.status" (change)="buscar()">
              <option value="">Todos</option>
              <option value="PENDENTE">⏳ Pendente</option>
              <option value="VENCIDO">⚠️ Vencido</option>
              <option value="PAGO">✅ Pago</option>
              <option value="CANCELADO">❌ Cancelado</option>
            </select>
          </div>

          <div class="campo">
            <label>Funcionário</label>
            <div class="busca-funcionario">
              <input
                type="text"
                [(ngModel)]="termoBusca"
                (input)="buscarFuncionario()"
                (blur)="fecharListaComDelay()"
                placeholder="🔍 Nome do funcionário..."
                autocomplete="off"
                class="input-busca">
              <div class="lista-resultados" *ngIf="mostrarLista && funcionarios.length > 0">
                <button
                  type="button"
                  class="item-resultado"
                  *ngFor="let func of funcionarios"
                  (click)="selecionarFuncionario(func)">
                  <strong>{{ func.nome }}</strong>
                  <small>CPF: {{ func.cpf }}</small>
                </button>
              </div>
            </div>
            <div class="funcionario-selecionado" *ngIf="funcionarioSelecionado">
              <span>{{ funcionarioSelecionado.nome }}</span>
              <button type="button" (click)="limparFuncionario()">❌</button>
            </div>
          </div>

          <div class="campo">
            <label>Data Início</label>
            <input type="date" [(ngModel)]="filtros.dataInicio" (change)="buscar()">
          </div>

          <div class="campo">
            <label>Data Fim</label>
            <input type="date" [(ngModel)]="filtros.dataFim" (change)="buscar()">
          </div>

        </div>

        <div class="filtros-acoes">
          <button class="btn-buscar" (click)="buscar()">🔍 Buscar</button>
          <button class="btn-limpar" (click)="limparFiltros()">🧹 Limpar</button>
        </div>
      </div>

      <!-- LOADING -->
      <div class="loading" *ngIf="carregando">
        <div class="spinner"></div>
        <p>Carregando vales...</p>
      </div>

      <!-- RESUMO GERAL -->
      <div class="card-resumo" *ngIf="!carregando && valesAgrupados.length > 0">
        <h3>📈 Resumo Geral</h3>
        <div class="resumo-grid">
          <div class="resumo-item pendente">
            <span class="resumo-label">⏳ Pendentes</span>
            <span class="resumo-valor">{{ totalPorStatus('PENDENTE') | currency:'BRL' }}</span>
            <span class="resumo-qtd">{{ qtdPorStatus('PENDENTE') }} vale(s)</span>
          </div>
          <div class="resumo-item vencido">
            <span class="resumo-label">⚠️ Vencidos</span>
            <span class="resumo-valor">{{ totalPorStatus('VENCIDO') | currency:'BRL' }}</span>
            <span class="resumo-qtd">{{ qtdPorStatus('VENCIDO') }} vale(s)</span>
          </div>
          <div class="resumo-item pago">
            <span class="resumo-label">✅ Pagos</span>
            <span class="resumo-valor">{{ totalPorStatus('PAGO') | currency:'BRL' }}</span>
            <span class="resumo-qtd">{{ qtdPorStatus('PAGO') }} vale(s)</span>
          </div>
          <div class="resumo-item total">
            <span class="resumo-label">💰 Total Geral</span>
            <span class="resumo-valor">{{ totalGeral | currency:'BRL' }}</span>
            <span class="resumo-qtd">{{ totalVales }} vale(s)</span>
          </div>
        </div>
      </div>

      <!-- RESULTADOS POR FUNCIONÁRIO -->
      <div *ngIf="!carregando && valesAgrupados.length > 0">
        <div class="card-funcionario" *ngFor="let grupo of valesAgrupados">
          <div class="funcionario-header">
            <h3>👤 {{ grupo.funcionario }}</h3>
            <span class="total-funcionario">Total: {{ grupo.total | currency:'BRL' }}</span>
          </div>
          <table>
            <thead>
              <tr>
                <th>Data</th>
                <th>Tipo</th>
                <th>Vencimento</th>
                <th>Valor</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let vale of grupo.vales" [class]="'row-' + vale.status?.toLowerCase()">
                <td>{{ formatarData(vale.dataConcessao) }}</td>
                <td>{{ vale.tipoValeDescricao || vale.tipoVale }}</td>
                <td>{{ formatarData(vale.dataVencimento) }}</td>
                <td>{{ vale.valor | currency:'BRL' }}</td>
                <td>
                  <span class="badge" [class]="'badge-' + vale.status.toLowerCase()">
                    {{ vale.statusDescricao || vale.status }}
                  </span>
                </td>
              </tr>
            </tbody>
            <tfoot>
              <tr>
                <td colspan="3"><strong>Total {{ grupo.funcionario }}</strong></td>
                <td><strong>{{ grupo.total | currency:'BRL' }}</strong></td>
                <td></td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

      <!-- VAZIO -->
      <div class="empty" *ngIf="!carregando && valesAgrupados.length === 0 && buscaRealizada">
        <p>📭 Nenhum vale encontrado com os filtros selecionados.</p>
      </div>

    </div>
  `,
  styles: [`
    .container {
      max-width: 1000px;
      margin: 0 auto;
      padding: 20px;
    }
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 25px;
    }
    .header h1 { margin: 0; color: #2c3e50; }
    .btn-voltar {
      padding: 10px 20px;
      background: #95a5a6;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
    }
    .btn-imprimir {
      padding: 10px 20px;
      background: #2980b9;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
    }
    .btn-imprimir:disabled { opacity: 0.5; cursor: not-allowed; }
    .card-filtros, .card-resumo, .card-funcionario {
      background: white;
      border-radius: 10px;
      padding: 20px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      margin-bottom: 20px;
    }
    .card-filtros h3, .card-resumo h3 { margin: 0 0 15px 0; color: #2c3e50; }
    .filtros-grid {
      display: grid;
      grid-template-columns: 1fr 2fr 1fr 1fr;
      gap: 15px;
      margin-bottom: 15px;
    }
    .campo label {
      display: block;
      margin-bottom: 6px;
      font-weight: 600;
      color: #2c3e50;
      font-size: 0.9em;
    }
    .campo input, .campo select {
      width: 100%;
      padding: 10px;
      border: 2px solid #e0e0e0;
      border-radius: 6px;
      font-size: 0.95em;
      box-sizing: border-box;
    }
    .filtros-acoes {
      display: flex;
      gap: 10px;
    }
    .btn-buscar {
      padding: 10px 25px;
      background: #667eea;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
    }
    .btn-limpar {
      padding: 10px 25px;
      background: #95a5a6;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
    }
    /* BUSCA FUNCIONÁRIO */
    .busca-funcionario { position: relative; }
    .input-busca { width: 100%; }
    .lista-resultados {
      position: absolute;
      top: 100%; left: 0; right: 0;
      background: white;
      border: 2px solid #667eea;
      border-top: none;
      border-radius: 0 0 6px 6px;
      max-height: 200px;
      overflow-y: auto;
      z-index: 100;
      box-shadow: 0 4px 8px rgba(0,0,0,0.15);
    }
    .item-resultado {
      display: block;
      width: 100%;
      padding: 10px;
      border: none;
      border-bottom: 1px solid #f0f0f0;
      background: white;
      text-align: left;
      cursor: pointer;
    }
    .item-resultado:hover { background: #f0f4ff; }
    .item-resultado strong { display: block; color: #2c3e50; }
    .item-resultado small { color: #7f8c8d; }
    .funcionario-selecionado {
      margin-top: 6px;
      padding: 6px 10px;
      background: #f0f4ff;
      border-radius: 4px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 0.9em;
    }
    .funcionario-selecionado button {
      background: none;
      border: none;
      cursor: pointer;
      font-size: 0.85em;
    }
    /* RESUMO */
    .resumo-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 15px;
    }
    .resumo-item {
      padding: 15px;
      border-radius: 8px;
      text-align: center;
      display: flex;
      flex-direction: column;
      gap: 5px;
    }
    .resumo-item.pendente { background: #fff3cd; border-left: 4px solid #ffc107; }
    .resumo-item.vencido { background: #fde8e8; border-left: 4px solid #e74c3c; }
    .resumo-item.pago { background: #d4edda; border-left: 4px solid #27ae60; }
    .resumo-item.total { background: #e8f4fd; border-left: 4px solid #2980b9; }
    .resumo-label { font-weight: 600; font-size: 0.9em; }
    .resumo-valor { font-size: 1.2em; font-weight: 700; color: #2c3e50; }
    .resumo-qtd { font-size: 0.8em; color: #7f8c8d; }
    /* TABELA */
    .funcionario-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
    }
    .funcionario-header h3 { margin: 0; color: #2c3e50; }
    .total-funcionario {
      font-weight: 700;
      color: #2980b9;
      font-size: 1.1em;
    }
    table { width: 100%; border-collapse: collapse; font-size: 0.9em; }
    th {
      background: #f8f9fa;
      padding: 10px;
      text-align: left;
      font-weight: 600;
      color: #2c3e50;
      border-bottom: 2px solid #e0e0e0;
    }
    td { padding: 10px; border-bottom: 1px solid #f0f0f0; }
    tfoot td { border-top: 2px solid #e0e0e0; background: #f8f9fa; }
    .row-vencido { background: #fff8f8; }
    .row-pago { background: #f8fff8; }
    .badge {
      padding: 3px 10px;
      border-radius: 12px;
      font-size: 0.85em;
      font-weight: 600;
    }
    .badge-pendente { background: #fff3cd; color: #856404; }
    .badge-vencido { background: #fde8e8; color: #c0392b; }
    .badge-pago { background: #d4edda; color: #155724; }
    .badge-cancelado { background: #e2e3e5; color: #495057; }
    /* LOADING */
    .loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 40px;
      gap: 15px;
    }
    .spinner {
      width: 40px; height: 40px;
      border: 4px solid #f3f3f3;
      border-top: 4px solid #667eea;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
    @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
    .empty { text-align: center; padding: 40px; color: #7f8c8d; font-size: 1.1em; }
    @media (max-width: 768px) {
      .filtros-grid { grid-template-columns: 1fr; }
      .resumo-grid { grid-template-columns: 1fr 1fr; }
    }
    @media print {
      .header button, .card-filtros, .filtros-acoes { display: none !important; }
      .card-funcionario { box-shadow: none; border: 1px solid #ddd; }
    }
  `]
})
export class ValeRelatorioComponent implements OnInit {
  private router = inject(Router);
  private http = inject(HttpClient);
  private clienteService = inject(ClienteService);

  carregando = false;
  buscaRealizada = false;

  filtros = {
    status: '',
    clienteId: null as number | null,
    dataInicio: '',
    dataFim: ''
  };

  termoBusca = '';
  funcionarios: Cliente[] = [];
  funcionarioSelecionado: Cliente | null = null;
  mostrarLista = false;

  vales: Vale[] = [];
  valesAgrupados: ValeAgrupado[] = [];
  totalGeral = 0;
  totalVales = 0;

  ngOnInit(): void {
    // Inicia com mês atual
    const hoje = new Date();
    const inicio = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
    this.filtros.dataInicio = inicio.toISOString().split('T')[0];
    this.filtros.dataFim = hoje.toISOString().split('T')[0];
  }

  buscar(): void {
    this.carregando = true;
    this.buscaRealizada = true;

    const params: any = {};
    if (this.filtros.status) params.status = this.filtros.status;
    if (this.filtros.clienteId) params.clienteId = this.filtros.clienteId;
    if (this.filtros.dataInicio) params.dataInicio = this.filtros.dataInicio;
    if (this.filtros.dataFim) params.dataFim = this.filtros.dataFim;

    this.http.get<Vale[]>('/api/vales/relatorio', { params }).subscribe({
      next: (data) => {
        this.vales = data;
        this.agrupar();
        this.carregando = false;
      },
      error: (err) => {
        console.error('Erro ao buscar vales:', err);
        this.carregando = false;
      }
    });
  }

  agrupar(): void {
    const mapa = new Map<string, ValeAgrupado>();

    this.vales.forEach(vale => {
      const nome = vale.clienteNome || 'Sem nome';
      const id = (vale as any).clienteId || 0;
      const chave = `${id}-${nome}`;

      if (!mapa.has(chave)) {
        mapa.set(chave, { funcionario: nome, clienteId: id, vales: [], total: 0 });
      }
      const grupo = mapa.get(chave)!;
      grupo.vales.push(vale);
      grupo.total += Number(vale.valor) || 0;
    });

    this.valesAgrupados = Array.from(mapa.values())
      .sort((a, b) => a.funcionario.localeCompare(b.funcionario));

    this.totalGeral = this.vales.reduce((sum, v) => sum + (Number(v.valor) || 0), 0);
    this.totalVales = this.vales.length;
  }

  totalPorStatus(status: string): number {
    return this.vales
      .filter(v => v.status === status)
      .reduce((sum, v) => sum + (Number(v.valor) || 0), 0);
  }

  qtdPorStatus(status: string): number {
    return this.vales.filter(v => v.status === status).length;
  }

  // BUSCA FUNCIONÁRIO
  buscarFuncionario(): void {
    if (this.termoBusca.length < 2) {
      this.funcionarios = [];
      this.mostrarLista = false;
      return;
    }
    this.clienteService.buscarFuncionarios(this.termoBusca).subscribe({
      next: (data) => {
        this.funcionarios = data;
        this.mostrarLista = data.length > 0;
      }
    });
  }

  selecionarFuncionario(func: any): void {
    this.funcionarioSelecionado = func;
    this.termoBusca = func.nome;
    this.filtros.clienteId = func.id ?? func.clienteId ?? null;
    this.funcionarios = [];
    this.mostrarLista = false;
    this.buscar();
  }

  fecharListaComDelay(): void {
    setTimeout(() => { this.mostrarLista = false; }, 300);
  }

  limparFuncionario(): void {
    this.funcionarioSelecionado = null;
    this.termoBusca = '';
    this.filtros.clienteId = null;
    this.buscar();
  }

  limparFiltros(): void {
    this.filtros = { status: '', clienteId: null, dataInicio: '', dataFim: '' };
    this.funcionarioSelecionado = null;
    this.termoBusca = '';
    this.valesAgrupados = [];
    this.vales = [];
    this.buscaRealizada = false;
  }

  formatarData(data: string): string {
    if (!data) return '-';
    return new Date(data + 'T00:00:00').toLocaleDateString('pt-BR');
  }

  imprimir(): void {
    const agora = new Date().toLocaleString('pt-BR');
    const statusLabel = this.filtros.status || 'Todos';
    const periodoLabel = this.filtros.dataInicio && this.filtros.dataFim
      ? `${this.formatarData(this.filtros.dataInicio)} a ${this.formatarData(this.filtros.dataFim)}`
      : 'Todos os períodos';

    let html = `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
        <title>Relatório de Vales</title>
        <style>
          @page { size: A4; margin: 15mm; }
          body { font-family: Arial, sans-serif; font-size: 10pt; color: #000; }
          h1 { text-align: center; font-size: 14pt; margin: 0 0 5px 0; }
          .subtitulo { text-align: center; font-size: 9pt; color: #555; margin-bottom: 15px; }
          .linha { border-top: 1px solid #000; margin: 8px 0; }
          .linha-dupla { border-top: 3px double #000; margin: 8px 0; }
          .funcionario-titulo {
            font-size: 11pt;
            font-weight: bold;
            margin: 15px 0 5px 0;
            padding: 5px;
            background: #f0f0f0;
          }
          table { width: 100%; border-collapse: collapse; margin-bottom: 5px; }
          th { font-size: 9pt; font-weight: bold; border-bottom: 1px solid #000; padding: 4px; text-align: left; }
          td { font-size: 9pt; padding: 4px; border-bottom: 1px dotted #ccc; }
          .text-right { text-align: right; }
          .total-funcionario { font-weight: bold; font-size: 9pt; text-align: right; margin: 3px 0 10px 0; }
          .resumo-geral { margin-top: 15px; border-top: 2px solid #000; padding-top: 10px; }
          .resumo-linha { display: flex; justify-content: space-between; font-size: 10pt; margin: 3px 0; }
          .resumo-linha.total { font-weight: bold; font-size: 11pt; border-top: 1px solid #000; padding-top: 5px; margin-top: 5px; }
          .badge-pendente { color: #856404; }
          .badge-vencido { color: #c0392b; font-weight: bold; }
          .badge-pago { color: #155724; }
          .badge-cancelado { color: #495057; }
        </style>
      </head>
      <body>
        <h1>HOTEL DI VAN — RELATÓRIO DE VALES</h1>
        <div class="subtitulo">
          Status: ${statusLabel} | Período: ${periodoLabel}<br>
          Emitido em: ${agora}
          ${this.funcionarioSelecionado ? ' | Funcionário: ' + this.funcionarioSelecionado.nome : ''}
        </div>
        <div class="linha-dupla"></div>
    `;

    this.valesAgrupados.forEach(grupo => {
      html += `
        <div class="funcionario-titulo">👤 ${grupo.funcionario}</div>
        <table>
          <thead>
            <tr>
              <th>Data</th>
              <th>Tipo</th>
              <th>Vencimento</th>
              <th>Status</th>
              <th class="text-right">Valor</th>
            </tr>
          </thead>
          <tbody>
      `;
      grupo.vales.forEach(vale => {
        const statusClass = 'badge-' + (vale.status?.toLowerCase() || '');
        html += `
          <tr>
            <td>${this.formatarData(vale.dataConcessao)}</td>
            <td>${vale.tipoValeDescricao || vale.tipoVale}</td>
            <td>${this.formatarData(vale.dataVencimento)}</td>
            <td class="${statusClass}">${vale.statusDescricao || vale.status}</td>
            <td class="text-right">R$ ${Number(vale.valor).toLocaleString('pt-BR', {minimumFractionDigits:2})}</td>
          </tr>
        `;
      });
      html += `
          </tbody>
        </table>
        <div class="total-funcionario">Total: R$ ${grupo.total.toLocaleString('pt-BR', {minimumFractionDigits:2})}</div>
        <div class="linha"></div>
      `;
    });

    html += `
        <div class="resumo-geral">
          <div class="resumo-linha"><span>⏳ Pendentes (${this.qtdPorStatus('PENDENTE')} vales)</span><span>R$ ${this.totalPorStatus('PENDENTE').toLocaleString('pt-BR', {minimumFractionDigits:2})}</span></div>
          <div class="resumo-linha"><span>⚠️ Vencidos (${this.qtdPorStatus('VENCIDO')} vales)</span><span>R$ ${this.totalPorStatus('VENCIDO').toLocaleString('pt-BR', {minimumFractionDigits:2})}</span></div>
          <div class="resumo-linha"><span>✅ Pagos (${this.qtdPorStatus('PAGO')} vales)</span><span>R$ ${this.totalPorStatus('PAGO').toLocaleString('pt-BR', {minimumFractionDigits:2})}</span></div>
          <div class="resumo-linha"><span>❌ Cancelados (${this.qtdPorStatus('CANCELADO')} vales)</span><span>R$ ${this.totalPorStatus('CANCELADO').toLocaleString('pt-BR', {minimumFractionDigits:2})}</span></div>
          <div class="resumo-linha total"><span>💰 TOTAL GERAL (${this.totalVales} vales)</span><span>R$ ${this.totalGeral.toLocaleString('pt-BR', {minimumFractionDigits:2})}</span></div>
        </div>
        <script>window.onload = function() { window.print(); window.onafterprint = function() { window.close(); }; };</script>
      </body>
      </html>
    `;

    const janela = window.open('', '_blank', 'width=900,height=700');
    if (janela) {
      janela.document.write(html);
      janela.document.close();
    }
  }

  voltar(): void {
    this.router.navigate(['/vales']);
  }
}