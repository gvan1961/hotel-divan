import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ContaPagarService, ContaPagar } from '../../services/conta-pagar.service';
import { FornecedorService, Fornecedor } from '../../services/fornecedor.service';

@Component({
  selector: 'app-contas-pagar-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>{{ isEdit ? '✏️ Editar Conta' : '💰 Nova Conta a Pagar' }}</h1>
        <button class="btn-back" (click)="router.navigate(['/contas-pagar'])">← Voltar</button>
      </div>

      <div class="form-card">

        <!-- LEITOR DE CÓDIGO DE BARRAS -->
        <div class="barcode-section">
          <label>📷 Código de Barras do Boleto</label>
          <div class="barcode-input">
            <input type="text"
                   [(ngModel)]="codigoBarras"
                   name="codigoBarras"
                   placeholder="Digite ou escaneie o código de barras..."
                   (input)="onCodigoBarrasInput()"
                   #inputCodigo
                   autofocus />
            <button class="btn-limpar-codigo" (click)="limparCodigo()" *ngIf="codigoBarras">✕</button>
          </div>
          <small class="hint">O sistema preencherá automaticamente valor e vencimento</small>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Descrição *</label>
            <input type="text" [(ngModel)]="conta.descricao" name="descricao" required
                   placeholder="Ex: Conta de luz, Fornecedor X..." />
          </div>
          <div class="form-group">
            <label>Categoria *</label>
            <select [(ngModel)]="conta.categoria" name="categoria">
              <option value="">Selecione...</option>
              <option value="PRODUTO">Produto</option>
              <option value="DESPESA_FIXA">Despesa Fixa</option>
              <option value="FORNECEDOR">Fornecedor</option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Valor (R$) *</label>
            <input type="number" [(ngModel)]="conta.valor" name="valor"
                   min="0.01" step="0.01" placeholder="0,00" />
          </div>
          <div class="form-group">
            <label>Data de Vencimento *</label>
            <input type="date" [(ngModel)]="conta.dataVencimento" name="dataVencimento" />
          </div>
        </div>

        <!-- FORNECEDOR -->
        <div class="form-group">
          <label>Fornecedor</label>
          <div class="fornecedor-busca">
            <input type="text"
                   [(ngModel)]="termoBuscaFornecedor"
                   name="termoBuscaFornecedor"
                   placeholder="Buscar fornecedor por nome..."
                   (input)="buscarFornecedor()"
                   [class.campo-preenchido]="fornecedorSelecionado" />
            <div class="fornecedor-selecionado" *ngIf="fornecedorSelecionado">
              ✅ {{ fornecedorSelecionado.nome }}
              <button type="button" class="btn-limpar" (click)="limparFornecedor()">✕</button>
            </div>
          </div>
          <div class="lista-resultados" *ngIf="fornecedoresEncontrados.length > 0 && !fornecedorSelecionado">
            <div class="resultado-item"
                 *ngFor="let f of fornecedoresEncontrados"
                 (click)="selecionarFornecedor(f)">
              {{ f.nome }} {{ f.cnpj ? '— CNPJ: ' + f.cnpj : '' }}
            </div>
          </div>
        </div>

        <div class="form-group">
          <label>Observação</label>
          <textarea [(ngModel)]="conta.observacao" name="observacao" rows="3"
                    placeholder="Informações adicionais..."></textarea>
        </div>

        <div *ngIf="errorMessage" class="error-message">{{ errorMessage }}</div>

        <div class="form-actions">
          <button type="button" class="btn-cancel" (click)="router.navigate(['/contas-pagar'])">Cancelar</button>
          <button type="button" class="btn-save" (click)="salvar()" [disabled]="loading">
            {{ loading ? 'Salvando...' : 'Salvar' }}
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 900px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h1 { color: #333; margin: 0; }
    .btn-back { background: #6c757d; color: white; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }
    .form-card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }

    .barcode-section { background: #e8f5e9; padding: 20px; border-radius: 8px; border: 2px solid #27ae60; margin-bottom: 24px; }
    .barcode-section label { font-size: 15px; font-weight: 600; color: #1b5e20; display: block; margin-bottom: 10px; }
    .barcode-input { display: flex; gap: 8px; align-items: center; }
    .barcode-input input { flex: 1; padding: 12px; border: 2px solid #27ae60; border-radius: 6px; font-size: 15px; }
    .barcode-input input:focus { outline: none; border-color: #1b5e20; }
    .btn-limpar-codigo { background: #e53935; color: white; border: none; padding: 12px 16px; border-radius: 6px; cursor: pointer; font-size: 16px; }
    .hint { color: #2e7d32; font-size: 12px; margin-top: 6px; display: block; }

    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; }
    .form-group { display: flex; flex-direction: column; margin-bottom: 16px; }
    .form-group label { margin-bottom: 6px; color: #555; font-weight: 500; }
    .form-group input, .form-group select, .form-group textarea {
      padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px;
    }
    .form-group input:focus, .form-group select:focus { outline: none; border-color: #667eea; }
    .campo-preenchido { border-color: #27ae60 !important; }

    .fornecedor-busca { display: flex; flex-direction: column; gap: 8px; }
    .fornecedor-selecionado { background: #e8f5e9; padding: 10px; border-radius: 6px; border: 2px solid #27ae60; color: #1b5e20; font-weight: 500; display: flex; justify-content: space-between; align-items: center; }
    .btn-limpar { background: none; border: none; color: #e53935; cursor: pointer; font-size: 16px; font-weight: bold; }
    .lista-resultados { background: white; border: 1px solid #ddd; border-radius: 6px; max-height: 200px; overflow-y: auto; }
    .resultado-item { padding: 10px 14px; cursor: pointer; font-size: 14px; border-bottom: 1px solid #eee; }
    .resultado-item:hover { background: #e8f5e9; }

    .error-message { background: #fee; color: #c33; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
    .form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px; }
    .btn-cancel { background: #6c757d; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
    .btn-save { background: #667eea; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
    .btn-save:disabled { background: #aaa; cursor: not-allowed; }
    @media (max-width: 768px) { .form-row { grid-template-columns: 1fr; } }
  `]
})
export class ContasPagarFormApp implements OnInit {
  router = inject(Router);
  private route = inject(ActivatedRoute);
  private service = inject(ContaPagarService);
  private fornecedorService = inject(FornecedorService);

  isEdit = false;
  contaId?: number;
  loading = false;
  errorMessage = '';

  codigoBarras = '';
  termoBuscaFornecedor = '';
  fornecedoresEncontrados: Fornecedor[] = [];
  fornecedorSelecionado: Fornecedor | null = null;

  conta: ContaPagar = {
    descricao: '',
    valor: 0,
    dataVencimento: '',
    categoria: '',
    observacao: ''
  };

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.contaId = +params['id'];
        this.carregarConta(this.contaId);
      }
    });
  }

  carregarConta(id: number): void {
    this.service.buscarPorId(id).subscribe({
      next: (data) => {
        this.conta = data;
        this.codigoBarras = data.codigoBarras || '';
        if (data.fornecedorObj) {
          this.fornecedorSelecionado = data.fornecedorObj;
          this.termoBuscaFornecedor = data.fornecedorObj.nome;
        }
      },
      error: () => this.errorMessage = 'Erro ao carregar conta'
    });
  }

  onCodigoBarrasInput(): void {
    const codigo = this.codigoBarras.replace(/\D/g, '');
    if (codigo.length >= 44) {
      this.extrairDadosBoleto(codigo);
    }
  }

  extrairDadosBoleto(codigo: string): void {
    try {
      // Extrai vencimento (posição 33-36 = AAAAMM, depois DD)
      // Fator de vencimento: posições 33-36
      const fatorVencimento = parseInt(codigo.substring(33, 37));
      if (fatorVencimento > 0) {
        const dataBase = new Date('1997-10-07');
        dataBase.setDate(dataBase.getDate() + fatorVencimento);
        const ano = dataBase.getFullYear();
        const mes = String(dataBase.getMonth() + 1).padStart(2, '0');
        const dia = String(dataBase.getDate()).padStart(2, '0');
        this.conta.dataVencimento = `${ano}-${mes}-${dia}`;
      }

      // Extrai valor: posições 37-46 (últimos 10 dígitos antes do fim)
      const valorStr = codigo.substring(37, 47);
      const valor = parseInt(valorStr) / 100;
      if (valor > 0) {
        this.conta.valor = valor;
      }

      this.conta.codigoBarras = this.codigoBarras;
    } catch (e) {
      console.error('Erro ao extrair dados do boleto', e);
    }
  }

  limparCodigo(): void {
    this.codigoBarras = '';
    this.conta.codigoBarras = '';
  }

  buscarFornecedor(): void {
    if (this.termoBuscaFornecedor.length < 2) {
      this.fornecedoresEncontrados = [];
      return;
    }
    this.fornecedorService.buscarPorNome(this.termoBuscaFornecedor).subscribe({
      next: (data) => this.fornecedoresEncontrados = data,
      error: () => this.fornecedoresEncontrados = []
    });
  }

  selecionarFornecedor(f: Fornecedor): void {
    this.fornecedorSelecionado = f;
    this.termoBuscaFornecedor = f.nome;
    this.fornecedoresEncontrados = [];
  }

  limparFornecedor(): void {
    this.fornecedorSelecionado = null;
    this.termoBuscaFornecedor = '';
    this.fornecedoresEncontrados = [];
  }

  salvar(): void {
    if (!this.conta.descricao) { this.errorMessage = 'Descrição é obrigatória'; return; }
    if (!this.conta.valor || this.conta.valor <= 0) { this.errorMessage = 'Valor é obrigatório'; return; }
    if (!this.conta.dataVencimento) { this.errorMessage = 'Data de vencimento é obrigatória'; return; }

    this.loading = true;
    this.errorMessage = '';

    const payload = {
      ...this.conta,
      codigoBarras: this.codigoBarras || null,
      fornecedorId: this.fornecedorSelecionado?.id || null
    };

    const request = this.isEdit
      ? this.service.atualizar(this.contaId!, payload)
      : this.service.criar(payload);

    request.subscribe({
      next: () => this.router.navigate(['/contas-pagar']),
      error: (e) => {
        this.loading = false;
        this.errorMessage = e.error?.erro || 'Erro ao salvar';
      }
    });
  }
}