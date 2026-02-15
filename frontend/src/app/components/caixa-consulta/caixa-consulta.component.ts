import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CaixaConsultaService, CaixaConsulta, FiltroCaixa } from '../../services/caixa-consulta.service';
import { UsuarioService } from '../../services/usuario.service';

@Component({
  selector: 'app-caixa-consulta',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <!-- CABEÇALHO -->
      <div class="header">
        <h1>🔍 Consulta de Caixas</h1>
        <button class="btn-voltar" (click)="voltar()">← Voltar</button>
      </div>

      <!-- FILTROS -->
      <div class="card-filtros">
        <h3>📊 Filtros de Busca</h3>
        
        <div class="filtros-grid">
          <!-- DATA INICIAL -->
          <div class="campo">
            <label>Data Inicial *</label>
            <input 
              type="date"
              [(ngModel)]="filtro.dataInicio"
              name="dataInicio"
              required>
          </div>

          <!-- DATA FINAL -->
          <div class="campo">
            <label>Data Final *</label>
            <input 
              type="date"
              [(ngModel)]="filtro.dataFim"
              name="dataFim"
              required>
          </div>

          <!-- RECEPCIONISTA -->
          <div class="campo">
            <label>Recepcionista</label>
            <select 
              [(ngModel)]="filtro.usuarioId"
              name="usuarioId">
              <option [ngValue]="undefined">📋 Todos</option>
              <option *ngFor="let user of usuarios" [ngValue]="user.id">
                {{ user.nome }}
              </option>
            </select>
          </div>

          <!-- STATUS -->
          <div class="campo">
            <label>Status</label>
            <select 
              [(ngModel)]="filtro.status"
              name="status">
              <option value="">📋 Todos</option>
              <option value="ABERTO">🔓 Abertos</option>
              <option value="FECHADO">🔒 Fechados</option>
            </select>
          </div>
        </div>

        <!-- BOTÕES -->
        <div class="filtros-acoes">
          <button 
            class="btn-buscar" 
            (click)="buscar()" 
            [disabled]="!filtroValido() || buscando">
            {{ buscando ? '🔄 Buscando...' : '🔍 Buscar Caixas' }}
          </button>
          <button class="btn-limpar" (click)="limparFiltros()">
            🗑️ Limpar
          </button>
        </div>
      </div>

      <!-- LOADING -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Buscando caixas...</p>
      </div>

      <!-- RESUMO -->
      <div *ngIf="!loading && caixas.length > 0" class="resumo">
        <div class="resumo-card">
          <div class="resumo-icone">📊</div>
          <div class="resumo-info">
            <div class="resumo-valor">{{ caixas.length }}</div>
            <div class="resumo-label">Caixas Encontrados</div>
          </div>
        </div>

        <div class="resumo-card">
          <div class="resumo-icone">💰</div>
          <div class="resumo-info">
            <div class="resumo-valor">R$ {{ totalGeral | number:'1.2-2' }}</div>
            <div class="resumo-label">Total Geral</div>
          </div>
        </div>

        <div class="resumo-card">
          <div class="resumo-icone">🏨</div>
          <div class="resumo-info">
            <div class="resumo-valor">R$ {{ totalDiarias | number:'1.2-2' }}</div>
            <div class="resumo-label">Total Diárias</div>
          </div>
        </div>

        <div class="resumo-card">
          <div class="resumo-icone">🛒</div>
          <div class="resumo-info">
            <div class="resumo-valor">R$ {{ totalProdutos | number:'1.2-2' }}</div>
            <div class="resumo-label">Total Produtos</div>
          </div>
        </div>
      </div>

      <!-- TABELA DE RESULTADOS -->
      <div *ngIf="!loading && caixas.length > 0" class="table-container">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Recepcionista</th>
              <th>Turno</th>
              <th>Abertura</th>
              <th>Fechamento</th>
              <th>Diárias</th>
              <th>Produtos</th>
              <th>Total</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let caixa of caixas" [class.aberto]="caixa.status === 'ABERTO'">
              <td><strong>#{{ caixa.id }}</strong></td>
              <td>{{ caixa.usuarioNome }}</td>
              <td>{{ caixa.turno }}</td>
              <td>{{ formatarDataHora(caixa.dataHoraAbertura) }}</td>
              <td>{{ caixa.dataHoraFechamento ? formatarDataHora(caixa.dataHoraFechamento) : '-' }}</td>
              <td class="valor-positivo">R$ {{ caixa.totalDiarias | number:'1.2-2' }}</td>
              <td class="valor-positivo">R$ {{ caixa.totalProdutos | number:'1.2-2' }}</td>
              <td class="valor-destaque">R$ {{ caixa.totalLiquido | number:'1.2-2' }}</td>
              <td>
                <span [class]="'badge badge-' + caixa.status.toLowerCase()">
                  {{ caixa.status === 'ABERTO' ? '🔓 Aberto' : '🔒 Fechado' }}
                </span>
              </td>
              <td class="acoes">
                <button 
                  class="btn-acao btn-visualizar" 
                  (click)="visualizar(caixa.id)"
                  title="Ver detalhes">
                  👁️
                </button>
                <button 
                  class="btn-acao btn-imprimir" 
                  (click)="imprimir(caixa.id)"
                  title="Imprimir">
                  🖨️
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- VAZIO -->
      <div *ngIf="!loading && caixas.length === 0 && pesquisaRealizada" class="vazio">
        <div class="icone-vazio">📭</div>
        <p>Nenhum caixa encontrado com os filtros selecionados</p>
        <button class="btn" (click)="limparFiltros()">🔄 Limpar Filtros</button>
      </div>
    </div>
  `,
  styles: [`
    /* ANIMAÇÕES */
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    @keyframes fadeIn {
      from {
        opacity: 0;
        transform: translateY(20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    /* CONTAINER */
    .container {
      padding: 20px;
      max-width: 1600px;
      margin: 0 auto;
    }

    /* CABEÇALHO */
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
      animation: fadeIn 0.5s ease;
    }

    .header h1 {
      color: #2c3e50;
      margin: 0;
    }

    .btn-voltar {
      padding: 10px 20px;
      background: #95a5a6;
      color: white;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn-voltar:hover {
      background: #7f8c8d;
      transform: translateY(-2px);
    }

    /* FILTROS */
    .card-filtros {
      background: white;
      padding: 25px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      margin-bottom: 25px;
      animation: fadeIn 0.5s ease 0.1s backwards;
    }

    .card-filtros h3 {
      color: #2c3e50;
      margin-bottom: 20px;
    }

    .filtros-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 15px;
      margin-bottom: 20px;
    }

    .campo label {
      display: block;
      margin-bottom: 8px;
      font-weight: 600;
      color: #2c3e50;
      font-size: 0.9em;
    }

    .campo input,
    .campo select {
      width: 100%;
      padding: 10px;
      border: 2px solid #e0e0e0;
      border-radius: 6px;
      font-size: 1em;
      transition: all 0.3s;
    }

    .campo input:focus,
    .campo select:focus {
      outline: none;
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }

    /* BOTÕES DE FILTRO */
    .filtros-acoes {
      display: flex;
      gap: 15px;
      justify-content: flex-end;
    }

    .btn-buscar,
    .btn-limpar {
      padding: 12px 30px;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn-buscar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }

    .btn-buscar:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }

    .btn-buscar:disabled {
      opacity: 0.6;
      cursor: not-allowed;
      transform: none;
    }

    .btn-limpar {
      background: #95a5a6;
      color: white;
    }

    .btn-limpar:hover {
      background: #7f8c8d;
      transform: translateY(-2px);
    }

    /* LOADING */
    .loading {
      text-align: center;
      padding: 60px;
      background: white;
      border-radius: 12px;
      animation: fadeIn 0.3s ease;
    }

    .spinner {
      border: 4px solid #f3f3f3;
      border-top: 4px solid #667eea;
      border-radius: 50%;
      width: 50px;
      height: 50px;
      animation: spin 1s linear infinite;
      margin: 0 auto 20px;
    }

    /* RESUMO */
    .resumo {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 20px;
      margin-bottom: 25px;
    }

    .resumo-card {
      background: white;
      padding: 25px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      gap: 15px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      border-left: 4px solid #667eea;
      transition: all 0.3s ease;
      cursor: pointer;
      animation: fadeIn 0.5s ease;
    }

    .resumo-card:nth-child(1) { 
      border-left-color: #3498db;
      animation-delay: 0.2s;
      animation-fill-mode: backwards;
    }
    
    .resumo-card:nth-child(2) { 
      border-left-color: #27ae60;
      animation-delay: 0.3s;
      animation-fill-mode: backwards;
    }
    
    .resumo-card:nth-child(3) { 
      border-left-color: #e74c3c;
      animation-delay: 0.4s;
      animation-fill-mode: backwards;
    }
    
    .resumo-card:nth-child(4) { 
      border-left-color: #f39c12;
      animation-delay: 0.5s;
      animation-fill-mode: backwards;
    }

    .resumo-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
      border-left-width: 6px;
    }

    .resumo-icone {
      font-size: 2.5em;
    }

    .resumo-valor {
      font-size: 1.5em;
      font-weight: 700;
      color: #2c3e50;
    }

    .resumo-label {
      font-size: 0.9em;
      color: #7f8c8d;
    }

    /* TABELA */
    .table-container {
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      overflow-x: auto;
      max-height: 600px;
      overflow-y: auto;
      scroll-behavior: smooth;
      animation: fadeIn 0.5s ease 0.6s backwards;
    }

    .table-container::-webkit-scrollbar {
      width: 8px;
      height: 8px;
    }

    .table-container::-webkit-scrollbar-track {
      background: #f1f1f1;
      border-radius: 4px;
    }

    .table-container::-webkit-scrollbar-thumb {
      background: #667eea;
      border-radius: 4px;
    }

    .table-container::-webkit-scrollbar-thumb:hover {
      background: #5568d3;
    }

    table {
      width: 100%;
      border-collapse: collapse;
    }

    th {
      background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
      color: white;
      padding: 15px;
      text-align: left;
      font-weight: 600;
      font-size: 0.9em;
      white-space: nowrap;
      position: sticky;
      top: 0;
      z-index: 10;
    }

    td {
      padding: 15px;
      border-bottom: 1px solid #e0e0e0;
    }

    tr {
      transition: all 0.2s;
    }

    tr:hover {
      background: #f8f9fa;
    }

    tr.aberto {
      background: linear-gradient(90deg, #fff3cd 0%, #ffffff 100%);
      border-left: 4px solid #ffc107;
    }

    tr.aberto:hover {
      background: linear-gradient(90deg, #ffe69c 0%, #ffffff 100%);
    }

    .valor-positivo {
      font-weight: 700;
      color: #27ae60;
    }

    .valor-destaque {
      font-weight: 700;
      color: #667eea;
      font-size: 1.1em;
    }

    /* BADGES */
    .badge {
      padding: 5px 12px;
      border-radius: 12px;
      font-size: 0.8em;
      font-weight: 600;
      display: inline-block;
      white-space: nowrap;
    }

    .badge-aberto {
      background: #fff3cd;
      color: #856404;
    }

    .badge-fechado {
      background: #d4edda;
      color: #155724;
    }

    /* AÇÕES */
    .acoes {
      display: flex;
      gap: 8px;
    }

    .btn-acao {
      padding: 8px 12px;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-size: 1.2em;
      transition: all 0.3s;
      background: white;
      border: 2px solid #e0e0e0;
      position: relative;
    }

    .btn-acao:hover {
      transform: scale(1.15) translateY(-2px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.2);
    }

    .btn-visualizar {
      color: #3498db;
    }

    .btn-visualizar:hover {
      background: #3498db;
      color: white;
      border-color: #3498db;
    }

    .btn-imprimir {
      color: #9b59b6;
    }

    .btn-imprimir:hover {
      background: #9b59b6;
      color: white;
      border-color: #9b59b6;
    }

    /* TOOLTIP */
    .btn-acao::after {
      content: attr(title);
      position: absolute;
      bottom: 100%;
      left: 50%;
      transform: translateX(-50%) translateY(-5px);
      background: #2c3e50;
      color: white;
      padding: 5px 10px;
      border-radius: 4px;
      font-size: 0.7em;
      white-space: nowrap;
      opacity: 0;
      pointer-events: none;
      transition: all 0.3s;
      z-index: 1000;
    }

    .btn-acao:hover::after {
      opacity: 1;
      transform: translateX(-50%) translateY(-10px);
    }

    /* VAZIO */
    .vazio {
      text-align: center;
      padding: 80px 40px;
      background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      animation: fadeIn 0.5s ease;
    }

    .icone-vazio {
      font-size: 5em;
      margin-bottom: 20px;
      opacity: 0.3;
    }

    .vazio p {
      font-size: 1.2em;
      color: #7f8c8d;
      margin-bottom: 20px;
    }

    .btn {
      padding: 12px 24px;
      background: #667eea;
      color: white;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn:hover {
      background: #5568d3;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }

    /* RESPONSIVE */
    @media (max-width: 768px) {
      .filtros-grid {
        grid-template-columns: 1fr;
      }

      .resumo {
        grid-template-columns: 1fr;
      }

      .filtros-acoes {
        flex-direction: column;
      }

      .btn-buscar,
      .btn-limpar {
        width: 100%;
      }

      table {
        font-size: 0.85em;
      }

      th, td {
        padding: 10px 8px;
      }

      .acoes {
        flex-direction: column;
      }
    }
  `]
})
export class CaixaConsultaComponent implements OnInit {
  private caixaService = inject(CaixaConsultaService);
  private usuarioService = inject(UsuarioService);
  private router = inject(Router);

  caixas: CaixaConsulta[] = [];
  usuarios: any[] = [];
  loading = false;
  buscando = false;
  pesquisaRealizada = false;

  // Filtros
  filtro: FiltroCaixa = {
    dataInicio: this.obterPrimeiroDiaMes(),
    dataFim: this.obterUltimoDiaMes()
  };

  // Totais
  totalGeral = 0;
  totalDiarias = 0;
  totalProdutos = 0;

  ngOnInit(): void {
    this.carregarUsuarios();
  }

  carregarUsuarios(): void {
    this.usuarioService.listarTodos().subscribe({
      next: (data) => {
        console.log('✅ Usuários carregados:', data);
        this.usuarios = data;
      },
      error: (err) => {
        console.error('❌ Erro ao carregar usuários:', err);
      }
    });
  }

  buscar(): void {
    if (!this.filtroValido()) {
      alert('⚠️ Preencha a data inicial e final');
      return;
    }

    this.buscando = true;
    this.loading = true;
    this.pesquisaRealizada = true;

    console.log('🔍 Buscando caixas com filtros:', this.filtro);

    this.caixaService.consultarCaixas(this.filtro).subscribe({
      next: (data) => {
        console.log('✅ Caixas recebidos:', data);
        this.caixas = data;
        this.calcularTotais();
        this.loading = false;
        this.buscando = false;
      },
      error: (err) => {
        console.error('❌ Erro ao buscar caixas:', err);
        alert('❌ Erro ao buscar caixas: ' + (err.error?.erro || err.message));
        this.loading = false;
        this.buscando = false;
      }
    });
  }

  calcularTotais(): void {
    this.totalDiarias = this.caixas.reduce((sum, c) => sum + (c.totalDiarias || 0), 0);
    this.totalProdutos = this.caixas.reduce((sum, c) => sum + (c.totalProdutos || 0), 0);
    this.totalGeral = this.caixas.reduce((sum, c) => sum + (c.totalLiquido || 0), 0);
  }

  visualizar(id: number): void {
    this.router.navigate(['/caixa/detalhes', id]);
  }

  imprimir(id: number): void {
    this.router.navigate(['/caixa/imprimir', id]);
  }

  limparFiltros(): void {
    this.filtro = {
      dataInicio: this.obterPrimeiroDiaMes(),
      dataFim: this.obterUltimoDiaMes()
    };
    this.caixas = [];
    this.pesquisaRealizada = false;
    this.totalGeral = 0;
    this.totalDiarias = 0;
    this.totalProdutos = 0;
  }

  filtroValido(): boolean {
    return !!this.filtro.dataInicio && !!this.filtro.dataFim;
  }

  formatarDataHora(data: string): string {
    if (!data) return '-';
    return new Date(data).toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  obterPrimeiroDiaMes(): string {
    const data = new Date();
    data.setDate(1);
    return data.toISOString().split('T')[0];
  }

  obterUltimoDiaMes(): string {
    const data = new Date();
    data.setMonth(data.getMonth() + 1);
    data.setDate(0);
    return data.toISOString().split('T')[0];
  }

  voltar(): void {
    this.router.navigate(['/dashboard']);
  }
}