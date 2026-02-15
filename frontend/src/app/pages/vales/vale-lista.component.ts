import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ValeService } from '../../services/vale.service';
import { Vale, TipoVale, StatusVale, TIPO_VALE_LABELS, STATUS_VALE_LABELS } from '../../models/vale.model';

@Component({
  selector: 'app-vale-lista',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <!-- CABEÇALHO -->
      <div class="header">
        <h1>💵 Vales</h1>
        <button class="btn-novo" (click)="novo()">
          ➕ Novo Vale
        </button>
      </div>

      <!-- CARDS DE ESTATÍSTICAS -->
      <div class="cards-stats">
        <div class="card-stat pendentes">
          <div class="card-icon">⏳</div>
          <div class="card-info">
            <div class="card-valor">R$ {{ totalPendente | number:'1.2-2' }}</div>
            <div class="card-label">Pendentes ({{ quantidadePendente }})</div>
          </div>
        </div>

        <div class="card-stat vencidos">
          <div class="card-icon">⚠️</div>
          <div class="card-info">
            <div class="card-valor">{{ quantidadeVencido }}</div>
            <div class="card-label">Vencidos</div>
          </div>
        </div>

        <div class="card-stat pagos">
          <div class="card-icon">✅</div>
          <div class="card-info">
            <div class="card-valor">{{ quantidadePagoMes }}</div>
            <div class="card-label">Pagos no Mês</div>
          </div>
        </div>
      </div>

      <!-- FILTROS -->
      <div class="filtros">
        <div class="filtro-grupo">
          <label>Status:</label>
          <select [(ngModel)]="filtroStatus" (change)="aplicarFiltros()">
            <option value="">Todos</option>
            <option value="PENDENTE">⏳ Pendentes</option>
            <option value="VENCIDO">⚠️ Vencidos</option>
            <option value="PAGO">✅ Pagos</option>
            <option value="CANCELADO">❌ Cancelados</option>
          </select>
        </div>

        <div class="filtro-grupo">
          <label>Buscar Funcionário:</label>
          <input 
            type="text" 
            placeholder="Nome ou CPF do funcionário..."
            [(ngModel)]="termoBusca"
            (input)="aplicarFiltros()"
          />
        </div>

        <button 
          class="btn-limpar" 
          *ngIf="filtroStatus || termoBusca"
          (click)="limparFiltros()">
          🗑️ Limpar Filtros
        </button>
      </div>

      <!-- LOADING -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Carregando vales...</p>
      </div>

      <!-- TABELA -->
      <div *ngIf="!loading && valesFiltrados.length > 0" class="table-container">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Funcionário</th>
              <th>Tipo</th>
              <th>Valor</th>
              <th>Concessão</th>
              <th>Vencimento</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let vale of valesFiltrados" 
                [class.vencido]="vale.status === 'VENCIDO'">
              <td>{{ vale.id }}</td>
              <td>
                <div class="funcionario-info">
                  <strong>{{ vale.clienteNome }}</strong>
                  <small>{{ vale.clienteCpf }}</small>
                </div>
              </td>
              <td>{{ obterLabelTipo(vale.tipoVale) }}</td>
              <td class="valor">R$ {{ vale.valor | number:'1.2-2' }}</td>
              <td>{{ formatarData(vale.dataConcessao) }}</td>
              <td>{{ formatarData(vale.dataVencimento) }}</td>
              <td>
                <span [class]="'badge badge-' + vale.status.toLowerCase()">
                  {{ obterLabelStatus(vale.status) }}
                </span>
              </td>
              <td class="acoes">
                <!-- VISUALIZAR -->
                <button class="btn-acao btn-visualizar" 
                        (click)="visualizar(vale)" 
                        title="Visualizar detalhes">
                  👁️
                </button>

                <!-- ⭐ IMPRIMIR -->
                <button class="btn-acao btn-imprimir" 
                        (click)="imprimirVale(vale)" 
                        title="Imprimir vale">
                  🖨️
                </button>

                <!-- ⭐ ASSINAR (só PENDENTE) -->
                <button *ngIf="vale.status === 'PENDENTE'" 
                        class="btn-acao btn-assinar" 
                        (click)="assinarVale(vale)" 
                        title="Assinar digitalmente">
                  ✍️
                </button>

                <!-- PAGAR -->
                <button *ngIf="vale.status === 'PENDENTE' || vale.status === 'VENCIDO'" 
                        class="btn-acao btn-pagar" 
                        (click)="marcarComoPago(vale)" 
                        title="Marcar como pago">
                  💰
                </button>

                <!-- EDITAR (só PENDENTE) -->
                <button *ngIf="vale.status === 'PENDENTE'" 
                        class="btn-acao btn-editar" 
                        (click)="editar(vale.id!)" 
                        title="Editar vale">
                  ✏️
                </button>

                <!-- CANCELAR -->
                <button *ngIf="vale.status === 'PENDENTE' || vale.status === 'VENCIDO'" 
                        class="btn-acao btn-cancelar" 
                        (click)="cancelar(vale)" 
                        title="Cancelar vale">
                  ❌
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- VAZIO -->
      <div *ngIf="!loading && valesFiltrados.length === 0" class="vazio">
        <p>📭 Nenhum vale encontrado</p>
        <button class="btn" (click)="limparFiltros()">🔍 Limpar Filtros</button>
      </div>

      <!-- MODAL DE DETALHES -->
      <div class="modal" *ngIf="valeDetalhes" (click)="fecharModal()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>📋 Detalhes do Vale #{{ valeDetalhes.id }}</h2>
            <button class="btn-fechar" (click)="fecharModal()">✕</button>
          </div>

          <div class="modal-body">
            <div class="detalhe-grupo">
              <h3>👤 Funcionário</h3>
              <p><strong>Nome:</strong> {{ valeDetalhes.clienteNome }}</p>
              <p><strong>CPF:</strong> {{ valeDetalhes.clienteCpf }}</p>
            </div>

            <div class="detalhe-grupo">
              <h3>💵 Informações do Vale</h3>
              <p><strong>Tipo:</strong> {{ obterLabelTipo(valeDetalhes.tipoVale) }}</p>
              <p><strong>Valor:</strong> R$ {{ valeDetalhes.valor | number:'1.2-2' }}</p>
              <p><strong>Status:</strong> 
                <span [class]="'badge badge-' + valeDetalhes.status.toLowerCase()">
                  {{ obterLabelStatus(valeDetalhes.status) }}
                </span>
              </p>
            </div>

            <div class="detalhe-grupo">
              <h3>📅 Datas</h3>
              <p><strong>Concessão:</strong> {{ formatarData(valeDetalhes.dataConcessao) }}</p>
              <p><strong>Vencimento:</strong> {{ formatarData(valeDetalhes.dataVencimento) }}</p>
              <p *ngIf="valeDetalhes.dataPagamento">
                <strong>Pagamento:</strong> {{ formatarData(valeDetalhes.dataPagamento) }}
              </p>
            </div>

            <div class="detalhe-grupo" *ngIf="valeDetalhes.observacao">
              <h3>📝 Observação</h3>
              <p>{{ valeDetalhes.observacao }}</p>
            </div>

            <div class="detalhe-grupo" *ngIf="valeDetalhes.motivoCancelamento">
              <h3>❌ Motivo do Cancelamento</h3>
              <p>{{ valeDetalhes.motivoCancelamento }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
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
    }

    h1 {
      color: #2c3e50;
      margin: 0;
    }

    .btn-novo {
      padding: 12px 24px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn-novo:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }

    /* CARDS DE ESTATÍSTICAS */
    .cards-stats {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 20px;
      margin-bottom: 30px;
    }

    .card-stat {
      background: white;
      padding: 20px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      gap: 15px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      border-left: 4px solid;
    }

    .card-stat.pendentes {
      border-left-color: #f39c12;
    }

    .card-stat.vencidos {
      border-left-color: #e74c3c;
    }

    .card-stat.pagos {
      border-left-color: #27ae60;
    }

    .card-icon {
      font-size: 2.5em;
    }

    .card-valor {
      font-size: 1.8em;
      font-weight: 700;
      color: #2c3e50;
    }

    .card-label {
      font-size: 0.9em;
      color: #7f8c8d;
    }

    /* FILTROS */
    .filtros {
      background: white;
      padding: 20px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      margin-bottom: 25px;
      display: flex;
      gap: 15px;
      align-items: flex-end;
      flex-wrap: wrap;
    }

    .filtro-grupo {
      flex: 1;
      min-width: 200px;
    }

    .filtro-grupo label {
      display: block;
      margin-bottom: 8px;
      font-weight: 600;
      color: #2c3e50;
      font-size: 0.9em;
    }

    .filtro-grupo select,
    .filtro-grupo input {
      width: 100%;
      padding: 10px;
      border: 2px solid #e0e0e0;
      border-radius: 6px;
      font-size: 1em;
      transition: all 0.3s;
    }

    .filtro-grupo select:focus,
    .filtro-grupo input:focus {
      outline: none;
      border-color: #667eea;
    }

    .btn-limpar {
      padding: 10px 20px;
      background: #e74c3c;
      color: white;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
      white-space: nowrap;
    }

    .btn-limpar:hover {
      background: #c0392b;
    }

    /* LOADING */
    .loading {
      text-align: center;
      padding: 60px;
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

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    /* TABELA */
    .table-container {
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      overflow: hidden;
    }

    table {
      width: 100%;
      border-collapse: collapse;
    }

    th {
      background: #2c3e50;
      color: white;
      padding: 15px;
      text-align: left;
      font-weight: 600;
      font-size: 0.9em;
    }

    td {
      padding: 15px;
      border-bottom: 1px solid #e0e0e0;
    }

    tr:hover {
      background: #f8f9fa;
    }

    tr.vencido {
      background: #ffebee;
    }

    tr.vencido:hover {
      background: #ffcdd2;
    }

    .funcionario-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .funcionario-info strong {
      color: #2c3e50;
    }

    .funcionario-info small {
      color: #7f8c8d;
      font-size: 0.85em;
    }

    .valor {
      font-weight: 700;
      color: #27ae60;
      font-size: 1.1em;
    }

    /* BADGES */
    .badge {
      padding: 5px 12px;
      border-radius: 12px;
      font-size: 0.8em;
      font-weight: 600;
      text-transform: uppercase;
      display: inline-block;
    }

    .badge-pendente {
      background: #fff3cd;
      color: #856404;
    }

    .badge-pago {
      background: #d4edda;
      color: #155724;
    }

    .badge-cancelado {
      background: #f8d7da;
      color: #721c24;
    }

    .badge-vencido {
      background: #f8d7da;
      color: #721c24;
      animation: blink 2s infinite;
    }

    @keyframes blink {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.7; }
    }

    /* AÇÕES */
    .acoes {
      display: flex;
      gap: 5px;
      justify-content: center;
      flex-wrap: wrap;
    }

    .btn-acao {
      padding: 6px 10px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 1.2em;
      transition: all 0.3s;
      background: white;
      border: 1px solid #ddd;
    }

    .btn-acao:hover {
      transform: translateY(-2px);
      box-shadow: 0 2px 8px rgba(0,0,0,0.2);
    }

    .btn-visualizar:hover {
      background: #3498db;
      border-color: #3498db;
    }

    .btn-imprimir {
      background: #f8f9fa;
    }

    .btn-imprimir:hover {
      background: #9b59b6;
      border-color: #9b59b6;
      transform: translateY(-2px);
    }

    .btn-assinar {
      background: #fff9e6;
      animation: pulseGlow 2s infinite;
    }

    .btn-assinar:hover {
      background: #f39c12;
      border-color: #f39c12;
      transform: translateY(-2px) scale(1.05);
    }

    @keyframes pulseGlow {
      0%, 100% { box-shadow: 0 0 0 rgba(243, 156, 18, 0.4); }
      50% { box-shadow: 0 0 10px rgba(243, 156, 18, 0.6); }
    }

    .btn-pagar:hover {
      background: #27ae60;
      border-color: #27ae60;
    }

    .btn-editar:hover {
      background: #f39c12;
      border-color: #f39c12;
    }

    .btn-cancelar:hover {
      background: #e74c3c;
      border-color: #e74c3c;
    }

    /* VAZIO */
    .vazio {
      text-align: center;
      padding: 60px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .vazio p {
      font-size: 1.2em;
      color: #7f8c8d;
      margin-bottom: 20px;
    }

    .btn {
      padding: 10px 20px;
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
    }

    /* MODAL */
    .modal {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.7);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
      animation: fadeIn 0.3s ease;
    }

    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }

    .modal-content {
      background: white;
      border-radius: 12px;
      max-width: 600px;
      width: 90%;
      max-height: 90vh;
      overflow-y: auto;
      animation: slideUp 0.3s ease;
    }

    @keyframes slideUp {
      from { transform: translateY(50px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      border-bottom: 2px solid #e0e0e0;
    }

    .modal-header h2 {
      margin: 0;
      color: #2c3e50;
    }

    .btn-fechar {
      background: #e74c3c;
      color: white;
      border: none;
      width: 35px;
      height: 35px;
      border-radius: 50%;
      cursor: pointer;
      font-size: 1.2em;
      transition: all 0.3s;
    }

    .btn-fechar:hover {
      background: #c0392b;
      transform: rotate(90deg);
    }

    .modal-body {
      padding: 20px;
    }

    .detalhe-grupo {
      margin-bottom: 20px;
      padding: 15px;
      background: #f8f9fa;
      border-radius: 8px;
      border-left: 4px solid #667eea;
    }

    .detalhe-grupo h3 {
      color: #2c3e50;
      margin-bottom: 10px;
      font-size: 1.1em;
    }

    .detalhe-grupo p {
      margin: 8px 0;
      color: #555;
    }

    .detalhe-grupo strong {
      color: #2c3e50;
    }

    /* RESPONSIVE */
    @media (max-width: 768px) {
      .cards-stats {
        grid-template-columns: 1fr;
      }

      .filtros {
        flex-direction: column;
      }

      .filtro-grupo {
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

      .btn-acao {
        width: 100%;
      }
    }
  `]
})
export class ValeListaComponent implements OnInit {
  private valeService = inject(ValeService);
  private router = inject(Router);

  vales: Vale[] = [];
  valesFiltrados: Vale[] = [];
  valeDetalhes: Vale | null = null;
  
  loading = false;
  
  // Estatísticas
  totalPendente = 0;
  quantidadePendente = 0;
  quantidadeVencido = 0;
  quantidadePagoMes = 0;
  
  // Filtros
  filtroStatus = '';
  termoBusca = '';

  ngOnInit(): void {
    this.carregarVales();
    this.atualizarValesVencidos();
  }

  carregarVales(): void {
    this.loading = true;
    this.valeService.listarTodos().subscribe({
      next: (data) => {
        this.vales = data;
        this.valesFiltrados = data;
        this.calcularEstatisticas();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar vales:', err);
        this.loading = false;
        alert('Erro ao carregar vales');
      }
    });
  }

  atualizarValesVencidos(): void {
    this.valeService.atualizarVencidos().subscribe({
      next: () => {
        console.log('✅ Vales vencidos atualizados');
        this.carregarVales();
      },
      error: (err) => {
        console.error('Erro ao atualizar vencidos:', err);
      }
    });
  }

  calcularEstatisticas(): void {
    this.quantidadePendente = this.vales.filter(v => v.status === 'PENDENTE').length;
    this.quantidadeVencido = this.vales.filter(v => v.status === 'VENCIDO').length;
    
    const hoje = new Date();
    const mesAtual = hoje.getMonth();
    const anoAtual = hoje.getFullYear();
    
    this.quantidadePagoMes = this.vales.filter(v => {
      if (v.status !== 'PAGO' || !v.dataPagamento) return false;
      const dataPagamento = new Date(v.dataPagamento);
      return dataPagamento.getMonth() === mesAtual && 
             dataPagamento.getFullYear() === anoAtual;
    }).length;
    
    this.totalPendente = this.vales
      .filter(v => v.status === 'PENDENTE' || v.status === 'VENCIDO')
      .reduce((total, v) => total + Number(v.valor), 0);
  }

  calcularEstatisticasFiltradas(): void {
  // Se há filtros ativos, calcular apenas dos filtrados
  const valesParaCalcular = (this.filtroStatus || this.termoBusca) 
    ? this.valesFiltrados 
    : this.vales;
  
  this.quantidadePendente = valesParaCalcular.filter(v => v.status === 'PENDENTE').length;
  this.quantidadeVencido = valesParaCalcular.filter(v => v.status === 'VENCIDO').length;
  
  const hoje = new Date();
  const mesAtual = hoje.getMonth();
  const anoAtual = hoje.getFullYear();
  
  this.quantidadePagoMes = valesParaCalcular.filter(v => {
    if (v.status !== 'PAGO' || !v.dataPagamento) return false;
    const dataPagamento = new Date(v.dataPagamento);
    return dataPagamento.getMonth() === mesAtual && 
           dataPagamento.getFullYear() === anoAtual;
  }).length;
  
  this.totalPendente = valesParaCalcular
    .filter(v => v.status === 'PENDENTE' || v.status === 'VENCIDO')
    .reduce((total, v) => total + Number(v.valor), 0);
}

 aplicarFiltros(): void {
  let resultado = [...this.vales];

  // Filtro por status
  if (this.filtroStatus) {
    resultado = resultado.filter(v => v.status === this.filtroStatus);
  }

  // Filtro por busca
  if (this.termoBusca) {
    const termo = this.termoBusca.toLowerCase();
    resultado = resultado.filter(v => 
      (v.clienteNome?.toLowerCase().includes(termo) || false) ||
      (v.clienteCpf?.includes(termo) || false)
    );
  }

  this.valesFiltrados = resultado;
  
  // ⭐ RECALCULAR ESTATÍSTICAS COM BASE NOS FILTRADOS
  this.calcularEstatisticasFiltradas();
}

 limparFiltros(): void {
  this.filtroStatus = '';
  this.termoBusca = '';
  this.valesFiltrados = this.vales;
  
  // ⭐ RECALCULAR ESTATÍSTICAS GERAIS (TODOS)
  this.calcularEstatisticas();
  
  console.log('🗑️ Filtros limpos - mostrando total geral');
}

  

  visualizar(vale: Vale): void {
    this.valeDetalhes = vale;
  }

  fecharModal(): void {
    this.valeDetalhes = null;
  }

  // ⭐ IMPRIMIR VALE
  imprimirVale(vale: Vale): void {
    console.log('🖨️ Imprimir vale:', vale.id);
    this.router.navigate(['/vales/imprimir', vale.id]);
  }

  // ⭐ ASSINAR VALE
  assinarVale(vale: Vale): void {
    console.log('✍️ Assinar vale:', vale.id);
    this.router.navigate(['/vales/assinar', vale.id]);
  }

  marcarComoPago(vale: Vale): void {
    const confirmacao = confirm(
      `💰 Confirmar pagamento do vale?\n\n` +
      `Funcionário: ${vale.clienteNome}\n` +
      `Valor: R$ ${vale.valor.toFixed(2)}`
    );

    if (!confirmacao) return;

    this.valeService.marcarComoPago(vale.id!).subscribe({
      next: () => {
        alert('✅ Vale marcado como pago!');
        this.carregarVales();
      },
      error: (err) => {
        console.error('Erro ao marcar como pago:', err);
        alert('❌ Erro ao marcar como pago');
      }
    });
  }

  editar(id: number): void {
    this.router.navigate(['/vales/editar', id]);
  }

  cancelar(vale: Vale): void {
    const motivo = prompt('❌ Motivo do cancelamento:');
    if (!motivo) return;

    this.valeService.cancelar(vale.id!, motivo).subscribe({
      next: () => {
        alert('✅ Vale cancelado!');
        this.carregarVales();
      },
      error: (err) => {
        console.error('Erro ao cancelar:', err);
        alert('❌ Erro ao cancelar vale');
      }
    });
  }

  novo(): void {
    this.router.navigate(['/vales/novo']);
  }

  obterLabelTipo(tipo: string): string {
    return TIPO_VALE_LABELS[tipo as keyof typeof TIPO_VALE_LABELS] || tipo;
  }

  obterLabelStatus(status: string): string {
    return STATUS_VALE_LABELS[status as keyof typeof STATUS_VALE_LABELS] || status;
  }

  formatarData(data: any): string {
    if (!data) return '-';
    return new Date(data).toLocaleDateString('pt-BR');
  }
}