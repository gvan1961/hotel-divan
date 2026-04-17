import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ContaPagarService, ContaPagar } from '../../services/conta-pagar.service';

@Component({
  selector: 'app-contas-pagar-lista',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>💰 Contas a Pagar</h1>
        <div class="header-acoes">
          <button class="btn-fornecedores" (click)="router.navigate(['/fornecedores'])">🏢 Fornecedores</button>
          <button class="btn-novo" (click)="router.navigate(['/contas-pagar/nova'])">+ Nova Conta</button>
          <button class="btn-imprimir" (click)="imprimirRelatorio()">🖨️ Imprimir</button>
        </div>
      </div>

      <!-- RESUMO -->
      <div class="resumo-cards">
        <div class="resumo-card card-total">
          <span class="resumo-label">Total em Aberto</span>
          <span class="resumo-valor">R$ {{ formatarMoeda(totalEmAberto) }}</span>
        </div>
        <div class="resumo-card card-vencido">
          <span class="resumo-label">Vencidas</span>
          <span class="resumo-valor">{{ totalVencidas }}</span>
        </div>
        <div class="resumo-card card-hoje">
          <span class="resumo-label">Vencem Hoje</span>
          <span class="resumo-valor">{{ totalHoje }}</span>
        </div>
      </div>

      <!-- FILTROS -->
      <div class="filtros">
        <select [(ngModel)]="filtroStatus" (change)="filtrar()">
          <option value="">Todos os status</option>
          <option value="EM_ABERTO">Em Aberto</option>
          <option value="PAGA">Paga</option>
          <option value="VENCIDA">Vencida</option>
        </select>
        <select [(ngModel)]="filtroCategoria" (change)="filtrar()">
          <option value="">Todas as categorias</option>
          <option value="PRODUTO">Produto</option>
          <option value="DESPESA_FIXA">Despesa Fixa</option>
          <option value="FORNECEDOR">Fornecedor</option>
        </select>
        <input type="text" [(ngModel)]="filtroDescricao" (input)="filtrar()"
               placeholder="Buscar por descrição..." />
      </div>

      <div *ngIf="loading" class="loading">Carregando...</div>

      <div *ngIf="!loading && contasFiltradas.length === 0" class="empty">
        Nenhuma conta encontrada
      </div>

      <div class="table-container" *ngIf="!loading && contasFiltradas.length > 0">
        <table>
          <thead>
            <tr>
              <th>Descrição</th>
              <th>Fornecedor</th>
              <th>Categoria</th>
              <th>Vencimento</th>
              <th>Valor</th>
              <th>Saldo</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let conta of contasFiltradas"
                [class.linha-vencida]="conta.status === 'VENCIDA'"
                [class.linha-paga]="conta.status === 'PAGA'">
              <td>{{ conta.descricao }}</td>
              <td>{{ conta.fornecedorObj?.nome || conta.fornecedor || '-' }}</td>
              <td>{{ conta.categoria || '-' }}</td>
              <td [class.vencido]="isVencida(conta)">{{ formatarData(conta.dataVencimento) }}</td>
              <td>R$ {{ formatarMoeda(conta.valor) }}</td>
              <td>R$ {{ formatarMoeda(conta.saldo || 0) }}</td>
              <td>
                <span [class]="'badge badge-' + (conta.status || '').toLowerCase()">
                  {{ conta.status }}
                </span>
              </td>
              <td>
                <button class="btn-pagar" (click)="abrirModalPagamento(conta)"
                        *ngIf="conta.status !== 'PAGA'">
                  💳 Pagar
                </button>
                <button class="btn-edit" (click)="router.navigate(['/contas-pagar/editar', conta.id])">
                  ✏️
                </button>
                <button class="btn-delete" (click)="excluir(conta.id!)">🗑️</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- MODAL PAGAMENTO -->
    <div class="modal-overlay" *ngIf="modalPagamento" (click)="fecharModalPagamento()">
      <div class="modal" (click)="$event.stopPropagation()">
        <h2>💳 Registrar Pagamento</h2>
        <div class="info-box">
          <p><strong>Conta:</strong> {{ contaPagando?.descricao }}</p>
          <p><strong>Saldo:</strong> R$ {{ formatarMoeda(contaPagando?.saldo || 0) }}</p>
        </div>
        <div class="campo">
          <label>Valor Pago *</label>
          <input type="number" [(ngModel)]="valorPagamento" min="0.01"
            [max]="contaPagando?.saldo ?? 0" step="0.01" />
        </div>
        <div class="campo">
          <label>Forma de Pagamento *</label>
          <select [(ngModel)]="formaPagamento">
            <option value="">Selecione...</option>
            <option value="DINHEIRO">Dinheiro</option>
            <option value="PIX">PIX</option>
            <option value="CARTAO_DEBITO">Cartão Débito</option>
            <option value="CARTAO_CREDITO">Cartão Crédito</option>
            <option value="TRANSFERENCIA">Transferência</option>
          </select>
        </div>
        <div class="modal-footer">
          <button class="btn-cancelar" (click)="fecharModalPagamento()">Cancelar</button>
          <button class="btn-confirmar" (click)="confirmarPagamento()"
                  [disabled]="!valorPagamento || !formaPagamento">
            ✅ Confirmar
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 1400px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h1 { color: #333; margin: 0; }
    .header-acoes { display: flex; gap: 10px; }
    .btn-novo { background: #667eea; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
    .btn-fornecedores { background: #2980b9; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

    .resumo-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-bottom: 20px; }
    .resumo-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); display: flex; flex-direction: column; align-items: center; }
    .card-total { border-left: 4px solid #e74c3c; }
    .card-vencido { border-left: 4px solid #e67e22; }
    .card-hoje { border-left: 4px solid #f39c12; }
    .resumo-label { font-size: 13px; color: #666; margin-bottom: 8px; }
    .resumo-valor { font-size: 22px; font-weight: bold; color: #2c3e50; }

    .filtros { display: flex; gap: 10px; margin-bottom: 20px; }
    .filtros select, .filtros input { padding: 8px 12px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; }
    .filtros input { flex: 1; }

    .loading, .empty { text-align: center; padding: 40px; color: #666; }
    .table-container { background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; }
    th { background: #f8f9fa; padding: 12px; text-align: left; font-weight: 600; border-bottom: 2px solid #dee2e6; }
    td { padding: 12px; border-bottom: 1px solid #dee2e6; }
    tr:hover { background: #f8f9fa; }
    .linha-vencida { background: #fff5f5; }
    .linha-paga { background: #f0fff4; opacity: 0.8; }
    .vencido { color: #e74c3c; font-weight: bold; }

    .badge { padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
    .badge-em_aberto { background: #fff3cd; color: #856404; }
    .badge-paga { background: #d4edda; color: #155724; }
    .badge-vencida { background: #f8d7da; color: #721c24; }

    .btn-pagar { background: #27ae60; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; font-size: 12px; margin-right: 4px; }
    .btn-edit { background: #3498db; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; font-size: 12px; margin-right: 4px; }
    .btn-delete { background: #e74c3c; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; font-size: 12px; }

    .modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 1100; display: flex; align-items: center; justify-content: center; }
    .modal { background: white; border-radius: 8px; padding: 30px; width: 450px; }
    .modal h2 { margin: 0 0 20px; }
    .info-box { background: #e3f2fd; padding: 12px; border-radius: 6px; margin-bottom: 16px; }
    .info-box p { margin: 4px 0; color: #1976d2; }
    .campo { margin-bottom: 16px; }
    .campo label { display: block; margin-bottom: 6px; font-weight: 600; color: #333; }
    .campo input, .campo select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; box-sizing: border-box; }
    .modal-footer { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
    .btn-cancelar { background: #95a5a6; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
    .btn-confirmar { background: #27ae60; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
    .btn-confirmar:disabled { background: #aaa; cursor: not-allowed; }
    .btn-imprimir { background: #7f8c8d; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
  `]
})
export class ContasPagarListaApp implements OnInit {
  router = inject(Router);
  private service = inject(ContaPagarService);

  contas: ContaPagar[] = [];
  contasFiltradas: ContaPagar[] = [];
  loading = false;

  filtroStatus = '';
  filtroCategoria = '';
  filtroDescricao = '';

  totalEmAberto = 0;
  totalVencidas = 0;
  totalHoje = 0;

  modalPagamento = false;
  contaPagando: ContaPagar | null = null;
  valorPagamento: number | null = null;
  formaPagamento = '';

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading = true;
    this.service.listarTodas().subscribe({
      next: (data) => {
        this.contas = data.sort((a, b) =>
          new Date(a.dataVencimento).getTime() - new Date(b.dataVencimento).getTime());
        this.contasFiltradas = [...this.contas];
        this.calcularResumo();
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  calcularResumo(): void {
    const hoje = new Date().toISOString().split('T')[0];
    this.totalEmAberto = this.contas
      .filter(c => c.status === 'EM_ABERTO' || c.status === 'VENCIDA')
      .reduce((sum, c) => sum + (c.saldo || 0), 0);
    this.totalVencidas = this.contas.filter(c => c.status === 'VENCIDA').length;
    this.totalHoje = this.contas.filter(c => c.dataVencimento === hoje && c.status === 'EM_ABERTO').length;
  }

  filtrar(): void {
    this.contasFiltradas = this.contas.filter(c => {
      const matchStatus = !this.filtroStatus || c.status === this.filtroStatus;
      const matchCategoria = !this.filtroCategoria || c.categoria === this.filtroCategoria;
      const matchDescricao = !this.filtroDescricao ||
        c.descricao.toLowerCase().includes(this.filtroDescricao.toLowerCase());
      return matchStatus && matchCategoria && matchDescricao;
    });
  }

  isVencida(conta: ContaPagar): boolean {
    const hoje = new Date().toISOString().split('T')[0];
    return conta.dataVencimento < hoje && conta.status !== 'PAGA';
  }

  abrirModalPagamento(conta: ContaPagar): void {
    this.contaPagando = conta;
    this.valorPagamento = conta.saldo || 0;
    this.modalPagamento = true;
  }

  fecharModalPagamento(): void {
    this.modalPagamento = false;
    this.contaPagando = null;
    this.valorPagamento = null;
    this.formaPagamento = '';
  }

  confirmarPagamento(): void {
    if (!this.contaPagando || !this.valorPagamento || !this.formaPagamento) return;
    this.service.registrarPagamento(
      this.contaPagando.id!,
      this.valorPagamento,
      this.formaPagamento
    ).subscribe({
      next: () => {
        this.fecharModalPagamento();
        this.carregar();
      },
      error: (e) => alert('Erro: ' + e.error?.erro)
    });
  }

  excluir(id: number): void {
    if (!confirm('Excluir esta conta?')) return;
    this.service.deletar(id).subscribe({
      next: () => this.carregar(),
      error: (e) => alert('Erro: ' + e.error?.erro)
    });
  }

  formatarMoeda(valor: number): string {
    return (valor || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  formatarData(data: string): string {
    if (!data) return '-';
    const [ano, mes, dia] = data.split('-');
    return `${dia}/${mes}/${ano}`;
  }

  imprimirRelatorio(): void {
  const hoje = new Date().toLocaleDateString('pt-BR');
  const titulo = this.filtroStatus === 'EM_ABERTO' ? 'Em Aberto'
    : this.filtroStatus === 'PAGA' ? 'Pagas'
    : this.filtroStatus === 'VENCIDA' ? 'Vencidas'
    : 'Todas';

  const totalValor = this.contasFiltradas.reduce((s, c) => s + (c.valor || 0), 0);
  const totalSaldo = this.contasFiltradas.reduce((s, c) => s + (c.saldo || 0), 0);

  const linhas = this.contasFiltradas.map(c => `
    <tr>
      <td>${c.descricao}</td>
      <td>${c.fornecedorObj?.nome || c.fornecedor || '-'}</td>
      <td>${c.categoria || '-'}</td>
      <td>${this.formatarData(c.dataVencimento)}</td>
      <td>R$ ${this.formatarMoeda(c.valor)}</td>
      <td>R$ ${this.formatarMoeda(c.valorPago || 0)}</td>
      <td>R$ ${this.formatarMoeda(c.saldo || 0)}</td>
      <td>${c.status}</td>
    </tr>
  `).join('');

  const html = `
    <!DOCTYPE html>
    <html lang="pt-BR">
    <head>
      <meta charset="UTF-8">
      <title>Contas a Pagar — ${titulo}</title>
      <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; font-size: 11pt; padding: 30px; color: #000; }
        .cabecalho { text-align: center; border-bottom: 2px solid #000; padding-bottom: 12px; margin-bottom: 20px; }
        .cabecalho h1 { font-size: 16pt; font-weight: bold; }
        .cabecalho p { font-size: 10pt; margin-top: 4px; }
        .titulo-relatorio { font-size: 13pt; font-weight: bold; margin-bottom: 16px; }
        .info { font-size: 10pt; color: #555; margin-bottom: 16px; }
        table { width: 100%; border-collapse: collapse; font-size: 10pt; }
        th { background: #f0f0f0; padding: 8px; text-align: left; border: 1px solid #ccc; font-weight: bold; }
        td { padding: 7px 8px; border: 1px solid #ddd; }
        tr:nth-child(even) { background: #fafafa; }
        .rodape { margin-top: 20px; display: flex; justify-content: flex-end; gap: 40px; font-size: 11pt; }
        .rodape div { text-align: right; }
        .rodape strong { font-size: 12pt; }
        .status-em_aberto { color: #856404; font-weight: bold; }
        .status-paga { color: #155724; font-weight: bold; }
        .status-vencida { color: #721c24; font-weight: bold; }
        @media print { body { padding: 15px; } }
      </style>
    </head>
    <body>
      <div class="cabecalho">
        <h1>HOTEL DI VAN</h1>
        <p>SANTOS E CORREIA LTDA &nbsp;|&nbsp; CNPJ: 07.757.726/0001-12</p>
        <p>Av. João Crisóstomo Ramalho, Nr 406 C, Jardim Tropical, CEP 57.316-110 – Arapiraca/AL</p>
      </div>

      <div class="titulo-relatorio">📋 Relatório de Contas a Pagar — ${titulo}</div>
      <div class="info">
        Data de emissão: ${hoje} &nbsp;|&nbsp;
        Total de registros: ${this.contasFiltradas.length}
        ${this.filtroCategoria ? ' &nbsp;|&nbsp; Categoria: ' + this.filtroCategoria : ''}
        ${this.filtroDescricao ? ' &nbsp;|&nbsp; Busca: ' + this.filtroDescricao : ''}
      </div>

      <table>
        <thead>
          <tr>
            <th>Descrição</th>
            <th>Fornecedor</th>
            <th>Categoria</th>
            <th>Vencimento</th>
            <th>Valor</th>
            <th>Pago</th>
            <th>Saldo</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          ${linhas}
        </tbody>
      </table>

      <div class="rodape">
        <div>
          <span>Total Valor: </span>
          <strong>R$ ${this.formatarMoeda(totalValor)}</strong>
        </div>
        <div>
          <span>Total Saldo: </span>
          <strong>R$ ${this.formatarMoeda(totalSaldo)}</strong>
        </div>
      </div>

      <script>window.onload = function() { window.print(); }</script>
    </body>
    </html>
  `;

  const win = window.open('', '_blank');
  if (win) {
    win.document.write(html);
    win.document.close();
  }
}

}