import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MovimentacaoEstoqueService } from '../../services/movimentacao-estoque.service';
import { FornecedorService, Fornecedor } from '../../services/fornecedor.service';

@Component({
  selector: 'app-movimentacao-estoque',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>📦 Movimentação de Estoque</h1>
        <button class="btn-back" (click)="router.navigate(['/produtos'])">← Voltar</button>
      </div>

      <!-- ABAS -->
      <div class="abas">
        <button [class.ativa]="aba === 'entrada'" (click)="aba = 'entrada'">📥 Entrada</button>
        <button [class.ativa]="aba === 'acerto'" (click)="aba = 'acerto'">🔧 Acerto de Estoque</button>
        <button [class.ativa]="aba === 'historico'" (click)="aba = 'historico'; carregarHistorico()">📋 Histórico</button>
      </div>

      <!-- ABA ENTRADA -->
      <div class="form-card" *ngIf="aba === 'entrada'">
        <h2>📥 Registrar Entrada de Produto</h2>

        <div class="form-group">
          <label>Produto *</label>
          <div class="busca-produto">
            <input type="text" [(ngModel)]="buscaProduto" (input)="filtrarProdutos()"
                   placeholder="Buscar produto por nome ou código de barras..." />
            <div class="lista-resultados" *ngIf="produtosFiltrados.length > 0 && !produtoSelecionado">
              <div class="resultado-item" *ngFor="let p of produtosFiltrados" (click)="selecionarProduto(p)">
                <strong>{{ p.nomeProduto }}</strong>
                <small>Estoque atual: {{ p.quantidade }}</small>
              </div>
            </div>
          </div>
          <div class="produto-selecionado" *ngIf="produtoSelecionado">
            ✅ {{ produtoSelecionado.nomeProduto }} — Estoque atual: <strong>{{ produtoSelecionado.quantidade }}</strong>
            <button type="button" class="btn-limpar" (click)="limparProduto()">✕</button>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Quantidade *</label>
            <input type="number" [(ngModel)]="entrada.quantidade" min="1" placeholder="0" />
          </div>
          <div class="form-group">
            <label>Valor Unitário (R$)</label>
            <input type="number" [(ngModel)]="entrada.valorUnitario" min="0" step="0.01" placeholder="0,00" />
          </div>
        </div>

        <div class="form-group">
          <label>Fornecedor</label>
          <select [(ngModel)]="entrada.fornecedorId">
            <option [ngValue]="null">Selecione o fornecedor...</option>
            <option *ngFor="let f of fornecedores" [ngValue]="f.id">{{ f.nome }}</option>
          </select>
        </div>

        <div class="form-group">
          <label>Motivo / Observação</label>
          <input type="text" [(ngModel)]="entrada.motivo" placeholder="Ex: Compra mensal, NF 1234..." />
        </div>

        <div *ngIf="produtoSelecionado && entrada.quantidade > 0" class="preview-box">
          <span>Estoque atual: <strong>{{ produtoSelecionado.quantidade }}</strong></span>
          <span>+ Entrada: <strong>{{ entrada.quantidade }}</strong></span>
          <span>= Novo estoque: <strong>{{ produtoSelecionado.quantidade + entrada.quantidade }}</strong></span>
        </div>

        <div *ngIf="errorMessage" class="error-message">{{ errorMessage }}</div>

        <div class="form-actions">
          <button class="btn-save" (click)="salvarEntrada()" [disabled]="loading || !produtoSelecionado || !entrada.quantidade">
            {{ loading ? 'Salvando...' : '✅ Registrar Entrada' }}
          </button>
        </div>
      </div>

      <!-- ABA ACERTO -->
      <div class="form-card" *ngIf="aba === 'acerto'">
        <h2>🔧 Acerto de Estoque</h2>

        <div class="form-group">
          <label>Produto *</label>
          <div class="busca-produto">
            <input type="text" [(ngModel)]="buscaProduto" (input)="filtrarProdutos()"
                   placeholder="Buscar produto por nome..." />
            <div class="lista-resultados" *ngIf="produtosFiltrados.length > 0 && !produtoSelecionado">
              <div class="resultado-item" *ngFor="let p of produtosFiltrados" (click)="selecionarProduto(p)">
                <strong>{{ p.nomeProduto }}</strong>
                <small>Estoque atual: {{ p.quantidade }}</small>
              </div>
            </div>
          </div>
          <div class="produto-selecionado" *ngIf="produtoSelecionado">
            ✅ {{ produtoSelecionado.nomeProduto }} — Estoque atual: <strong>{{ produtoSelecionado.quantidade }}</strong>
            <button type="button" class="btn-limpar" (click)="limparProduto()">✕</button>
          </div>
        </div>

        <div class="form-group">
          <label>Quantidade Real (contagem física) *</label>
          <input type="number" [(ngModel)]="acerto.quantidadeReal" min="0" placeholder="0" />
        </div>

        <div class="form-group">
          <label>Motivo *</label>
          <select [(ngModel)]="acerto.motivo">
            <option value="">Selecione o motivo...</option>
            <option value="Inventário periódico">Inventário periódico</option>
            <option value="Perda por avaria">Perda por avaria</option>
            <option value="Perda por vencimento">Perda por vencimento</option>
            <option value="Erro de contagem anterior">Erro de contagem anterior</option>
            <option value="Furto ou extravio">Furto ou extravio</option>
            <option value="Outro">Outro</option>
          </select>
        </div>

        <div *ngIf="produtoSelecionado && acerto.quantidadeReal !== null" class="preview-box"
             [class.preview-positivo]="acerto.quantidadeReal > produtoSelecionado.quantidade"
             [class.preview-negativo]="acerto.quantidadeReal < produtoSelecionado.quantidade">
          <span>Estoque no sistema: <strong>{{ produtoSelecionado.quantidade }}</strong></span>
          <span>Contagem física: <strong>{{ acerto.quantidadeReal }}</strong></span>
          <span>Diferença: <strong>{{ acerto.quantidadeReal - produtoSelecionado.quantidade }}</strong></span>
        </div>

        <div *ngIf="errorMessage" class="error-message">{{ errorMessage }}</div>

        <div class="form-actions">
          <button class="btn-save" (click)="salvarAcerto()"
                  [disabled]="loading || !produtoSelecionado || acerto.quantidadeReal === null || !acerto.motivo">
            {{ loading ? 'Salvando...' : '✅ Registrar Acerto' }}
          </button>
        </div>
      </div>

      <!-- ABA HISTÓRICO -->
      <div class="form-card" *ngIf="aba === 'historico'">
        <h2>📋 Histórico de Movimentações</h2>

        <div class="filtros">
          <select [(ngModel)]="filtroTipo" (change)="filtrarHistorico()">
            <option value="">Todos os tipos</option>
            <option value="ENTRADA">Entrada</option>
            <option value="SAIDA">Saída</option>
            <option value="ACERTO">Acerto</option>
          </select>
          <input type="text" [(ngModel)]="filtroProduto" (input)="filtrarHistorico()"
                 placeholder="Filtrar por produto..." />
        </div>

        <div *ngIf="loadingHistorico" class="loading">Carregando...</div>
        <div *ngIf="!loadingHistorico && movimentacoesFiltradas.length === 0" class="empty">
          Nenhuma movimentação encontrada
        </div>

        <div class="table-container" *ngIf="!loadingHistorico && movimentacoesFiltradas.length > 0">
          <table>
            <thead>
              <tr>
                <th>Data</th>
                <th>Produto</th>
                <th>Tipo</th>
                <th>Ant.</th>
                <th>Qtd</th>
                <th>Novo</th>
                <th>Valor Unit.</th>
                <th>Fornecedor</th>
                <th>Motivo</th>
                <th>Usuário</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let m of movimentacoesFiltradas"
                  [class.linha-entrada]="m.tipo === 'ENTRADA'"
                  [class.linha-saida]="m.tipo === 'SAIDA'"
                  [class.linha-acerto]="m.tipo === 'ACERTO'">
                <td>{{ formatarData(m.criadoEm) }}</td>
                <td>{{ m.produto?.nomeProduto }}</td>
                <td>
                  <span [class]="'badge badge-' + m.tipo.toLowerCase()">{{ m.tipo }}</span>
                </td>
                <td>{{ m.quantidadeAnterior }}</td>
                <td>{{ m.quantidadeMovimentada }}</td>
                <td>{{ m.quantidadeNova }}</td>
                <td>{{ m.valorUnitario ? 'R$ ' + formatarMoeda(m.valorUnitario) : '-' }}</td>
                <td>{{ m.fornecedor?.nome || '-' }}</td>
                <td>{{ m.motivo || '-' }}</td>
                <td>{{ m.usuario?.nome || '-' }}</td>
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

    .form-card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
    h2 { color: #2c3e50; margin: 0 0 24px; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
    .form-group { margin-bottom: 16px; }
    .form-group label { display: block; margin-bottom: 6px; font-weight: 600; color: #555; }
    .form-group input, .form-group select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; box-sizing: border-box; }

    .busca-produto { position: relative; }
    .lista-resultados { position: absolute; top: 100%; left: 0; right: 0; background: white; border: 1px solid #ddd; border-radius: 5px; max-height: 200px; overflow-y: auto; z-index: 100; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
    .resultado-item { padding: 10px 14px; cursor: pointer; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
    .resultado-item:hover { background: #f5f5f5; }
    .produto-selecionado { background: #e8f5e9; padding: 10px 14px; border-radius: 5px; border: 2px solid #27ae60; color: #1b5e20; font-size: 14px; display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
    .btn-limpar { background: none; border: none; color: #e53935; cursor: pointer; font-size: 16px; font-weight: bold; }

    .preview-box { background: #e3f2fd; padding: 15px; border-radius: 6px; border: 2px solid #1976d2; margin: 16px 0; display: flex; gap: 20px; align-items: center; font-size: 14px; }
    .preview-positivo { background: #e8f5e9; border-color: #27ae60; }
    .preview-negativo { background: #fff3e0; border-color: #e67e22; }

    .filtros { display: flex; gap: 10px; margin-bottom: 20px; }
    .filtros select, .filtros input { padding: 8px 12px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; }
    .filtros input { flex: 1; }

    .loading, .empty { text-align: center; padding: 40px; color: #666; }
    .table-container { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; font-size: 13px; }
    th { background: #f8f9fa; padding: 10px; text-align: left; font-weight: 600; border-bottom: 2px solid #dee2e6; }
    td { padding: 10px; border-bottom: 1px solid #dee2e6; }
    .linha-entrada { background: #f0fff4; }
    .linha-saida { background: #fff5f5; }
    .linha-acerto { background: #fffbf0; }

    .badge { padding: 3px 8px; border-radius: 12px; font-size: 11px; font-weight: 600; }
    .badge-entrada { background: #d4edda; color: #155724; }
    .badge-saida { background: #f8d7da; color: #721c24; }
    .badge-acerto { background: #fff3cd; color: #856404; }

    .error-message { background: #fee; color: #c33; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
    .form-actions { display: flex; justify-content: flex-end; margin-top: 24px; }
    .btn-save { background: #667eea; color: white; border: none; padding: 12px 24px; border-radius: 5px; cursor: pointer; font-weight: 600; font-size: 15px; }
    .btn-save:disabled { background: #aaa; cursor: not-allowed; }
    @media (max-width: 768px) { .form-row { grid-template-columns: 1fr; } }
  `]
})
export class MovimentacaoEstoqueApp implements OnInit {
  router = inject(Router);
  private http = inject(HttpClient);
  private service = inject(MovimentacaoEstoqueService);
  private fornecedorService = inject(FornecedorService);

  aba: 'entrada' | 'acerto' | 'historico' = 'entrada';
  loading = false;
  loadingHistorico = false;
  errorMessage = '';

  // Busca produto
  buscaProduto = '';
  produtos: any[] = [];
  produtosFiltrados: any[] = [];
  produtoSelecionado: any = null;

  // Fornecedores
  fornecedores: Fornecedor[] = [];

  // Entrada
  entrada = { quantidade: 0, valorUnitario: null as number | null, fornecedorId: null as number | null, motivo: '' };

  // Acerto
  acerto = { quantidadeReal: null as number | null, motivo: '' };

  // Histórico
  movimentacoes: any[] = [];
  movimentacoesFiltradas: any[] = [];
  filtroTipo = '';
  filtroProduto = '';

  ngOnInit(): void {
    this.carregarProdutos();
    this.fornecedorService.listarAtivos().subscribe({
      next: (data) => this.fornecedores = data,
      error: () => {}
    });
  }

  carregarProdutos(): void {
    this.http.get<any[]>('/api/produtos').subscribe({
      next: (data) => this.produtos = data.sort((a, b) => a.nomeProduto.localeCompare(b.nomeProduto, 'pt-BR')),
      error: () => {}
    });
  }

  filtrarProdutos(): void {
    if (this.buscaProduto.length < 2) { this.produtosFiltrados = []; return; }
    const termo = this.buscaProduto.toLowerCase();
    this.produtosFiltrados = this.produtos.filter(p =>
      p.nomeProduto.toLowerCase().includes(termo) ||
      (p.codigoBarras && p.codigoBarras.includes(termo))
    );
  }

  selecionarProduto(p: any): void {
    this.produtoSelecionado = p;
    this.buscaProduto = p.nomeProduto;
    this.produtosFiltrados = [];
  }

  limparProduto(): void {
    this.produtoSelecionado = null;
    this.buscaProduto = '';
    this.produtosFiltrados = [];
  }

  salvarEntrada(): void {
    if (!this.produtoSelecionado || !this.entrada.quantidade) return;
    this.loading = true;
    this.errorMessage = '';

    this.service.registrarEntrada({
      produtoId: this.produtoSelecionado.id,
      quantidade: this.entrada.quantidade,
      valorUnitario: this.entrada.valorUnitario || undefined,
      fornecedorId: this.entrada.fornecedorId || undefined,
      motivo: this.entrada.motivo || undefined
    }).subscribe({
      next: () => {
        this.loading = false;
        this.produtoSelecionado.quantidade += this.entrada.quantidade;
        this.entrada = { quantidade: 0, valorUnitario: null, fornecedorId: null, motivo: '' };
        alert('✅ Entrada registrada com sucesso!');
        this.limparProduto();
      },
      error: (e) => {
        this.loading = false;
        this.errorMessage = e.error?.erro || 'Erro ao registrar entrada';
      }
    });
  }

  salvarAcerto(): void {
    if (!this.produtoSelecionado || this.acerto.quantidadeReal === null || !this.acerto.motivo) return;
    this.loading = true;
    this.errorMessage = '';

    this.service.registrarAcerto({
      produtoId: this.produtoSelecionado.id,
      quantidadeReal: this.acerto.quantidadeReal,
      motivo: this.acerto.motivo
    }).subscribe({
      next: () => {
        this.loading = false;
        this.produtoSelecionado.quantidade = this.acerto.quantidadeReal;
        this.acerto = { quantidadeReal: null, motivo: '' };
        alert('✅ Acerto de estoque registrado!');
        this.limparProduto();
      },
      error: (e) => {
        this.loading = false;
        this.errorMessage = e.error?.erro || 'Erro ao registrar acerto';
      }
    });
  }

  carregarHistorico(): void {
    this.loadingHistorico = true;
    this.service.listarTodas().subscribe({
      next: (data) => {
        this.movimentacoes = data;
        this.movimentacoesFiltradas = [...data];
        this.loadingHistorico = false;
      },
      error: () => this.loadingHistorico = false
    });
  }

  filtrarHistorico(): void {
    this.movimentacoesFiltradas = this.movimentacoes.filter(m => {
      const matchTipo = !this.filtroTipo || m.tipo === this.filtroTipo;
      const matchProduto = !this.filtroProduto ||
        m.produto?.nomeProduto?.toLowerCase().includes(this.filtroProduto.toLowerCase());
      return matchTipo && matchProduto;
    });
  }

  formatarData(data: string): string {
    if (!data) return '-';
    const d = new Date(data);
    return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  formatarMoeda(valor: number): string {
    return (valor || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}