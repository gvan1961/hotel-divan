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
          <small class="hint">
  ⚠️ Digite a <strong>linha digitável</strong> (47 dígitos com pontos e espaços), 
  não escaneie o código de barras. Ex: 03399.06349 68700.000000 20023.901018 1 14450000072697
</small>
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
           <input type="text"
           [(ngModel)]="valorFormatado"
           name="valor"
           (input)="onValorInput($event)"
           placeholder="0,00" />
  </div>
  <div class="form-group">
    <label>Data da Compra *</label>
    <input type="date" [(ngModel)]="conta.dataCompra" name="dataCompra"
       (change)="onDataCompraChange()"
       min="2020-01-01" max="2099-12-31" />
  </div>
</div>
<div class="form-row">
  <div class="form-group">
    <label>Data de Vencimento *</label>
    <input type="date" [(ngModel)]="conta.dataVencimento" name="dataVencimento"
       [min]="conta.dataCompra || '2020-01-01'" max="2099-12-31" />
  </div>
</div>

        <!-- FORNECEDOR -->
         <!-- FORNECEDOR -->
      <div class="form-group">
        <label>Fornecedor</label>
        <div class="fornecedor-busca">
          <div *ngIf="!fornecedorSelecionado">
            <input type="text"
                   [(ngModel)]="termoBuscaFornecedor"
                   name="termoBuscaFornecedor"
                   placeholder="Buscar fornecedor por nome..."
                   (input)="buscarFornecedor()"
                   autocomplete="off" />
            <div class="lista-resultados" *ngIf="fornecedoresEncontrados.length > 0">
              <div class="resultado-item"
                   *ngFor="let f of fornecedoresEncontrados"
                   (click)="selecionarFornecedor(f)">
                <strong>{{ f.nome }}</strong>
                <small *ngIf="f.cnpj">CNPJ: {{ f.cnpj }}</small>
              </div>
            </div>
            <div class="sem-resultado" *ngIf="termoBuscaFornecedor.length >= 2 && fornecedoresEncontrados.length === 0">
              ❌ Nenhum fornecedor encontrado
            </div>
          </div>
          <div class="fornecedor-selecionado" *ngIf="fornecedorSelecionado">
            ✅ {{ fornecedorSelecionado.nome }}
            <button type="button" class="btn-limpar" (click)="limparFornecedor()">✕</button>
          </div>
        </div>
      </div>

      <div *ngIf="errorMessage" class="error-message">{{ errorMessage }}</div>

      <div class="form-actions">
        <button type="button" class="btn-cancel" (click)="router.navigate(['/contas-pagar'])">Cancelar</button>
        <button type="button" class="btn-save" (click)="salvar()" [disabled]="loading">
          {{ loading ? 'Salvando...' : '💾 Salvar Conta' }}
        </button>
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

    .fornecedor-busca { position: relative; }
.lista-resultados { position: absolute; top: 100%; left: 0; right: 0; background: white; border: 1px solid #ddd; border-radius: 5px; max-height: 200px; overflow-y: auto; z-index: 100; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
.resultado-item { padding: 10px 14px; cursor: pointer; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.resultado-item:hover { background: #f5f5f5; }
.sem-resultado { padding: 10px 14px; color: #e74c3c; font-size: 13px; }

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

  valorFormatado = '0,00'; 

  conta: ContaPagar = {
    descricao: '',
    valor: 0,
    dataVencimento: '',
    dataCompra: '',
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
        this.valorFormatado = (data.valor || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
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
      const limpo = codigo.replace(/\D/g, '');
      console.log('Código limpo:', limpo, '| Tamanho:', limpo.length);

      if (limpo.length === 44) {
        // CÓDIGO DE BARRAS (44 dígitos)
        const fatorVencimento = parseInt(limpo.substring(5, 9));
        if (fatorVencimento > 0) {
          const dataBase = new Date('1997-10-07');
          dataBase.setDate(dataBase.getDate() + fatorVencimento);
          this.conta.dataVencimento = `${dataBase.getFullYear()}-${String(dataBase.getMonth() + 1).padStart(2, '0')}-${String(dataBase.getDate()).padStart(2, '0')}`;
        }
        const valor = parseInt(limpo.substring(9, 19)) / 100;
        if (valor > 0) this.conta.valor = valor;

      } else if (limpo.length === 47) {
        // LINHA DIGITÁVEL (47 dígitos)
        const banco = limpo.substring(0, 3);
        const moeda = limpo.substring(3, 4);
        const campo1 = limpo.substring(4, 9);
        const campo2 = limpo.substring(10, 20);
        const campo3 = limpo.substring(21, 31);
        const digVerificador = limpo.substring(32, 33);
        const fatorVencStr = limpo.substring(32, 37);
        const fatorVencimento = parseInt(fatorVencStr) - 1000;
        const valorStr = limpo.substring(37, 47);

        console.log('Fator vencimento str:', fatorVencStr);
        console.log('Fator vencimento calc:', fatorVencimento);

        if (fatorVencimento > 0) {
          const dataBase = new Date('1997-10-07');
          dataBase.setDate(dataBase.getDate() + fatorVencimento);
          this.conta.dataVencimento = `${dataBase.getFullYear()}-${String(dataBase.getMonth() + 1).padStart(2, '0')}-${String(dataBase.getDate()).padStart(2, '0')}`;
          console.log('Vencimento calculado:', this.conta.dataVencimento);
        }

        const valorNum = parseInt(valorStr) / 100;
        if (valorNum > 0) this.conta.valor = valorNum;
      }

      this.conta.codigoBarras = this.codigoBarras;
      console.log('Vencimento:', this.conta.dataVencimento, '| Valor:', this.conta.valor);

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

  onDataCompraChange(): void {
  if (this.conta.dataVencimento && this.conta.dataVencimento < (this.conta.dataCompra || '')) {
    this.conta.dataVencimento = '';
  }
}

  onValorInput(event: any): void {
  let digits = event.target.value.replace(/\D/g, '');
  if (!digits) digits = '0';
  const num = parseInt(digits) / 100;
  this.conta.valor = num;
  this.valorFormatado = num.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  event.target.value = this.valorFormatado;
}  

  salvar(): void {
    if (!this.conta.descricao) { this.errorMessage = 'Descrição é obrigatória'; return; }
    if (!this.conta.valor || this.conta.valor <= 0) { this.errorMessage = 'Valor é obrigatório'; return; }
    if (!this.conta.dataVencimento) { this.errorMessage = 'Data de vencimento é obrigatória'; return; }

    // ✅ Validar formato das datas (ano com 4 dígitos)
const regexData = /^\d{4}-\d{2}-\d{2}$/;
if (this.conta.dataCompra && !regexData.test(this.conta.dataCompra)) {
  this.errorMessage = 'Data da compra inválida. Use o formato AAAA-MM-DD com ano de 4 dígitos.';
  return;
}
if (this.conta.dataVencimento && !regexData.test(this.conta.dataVencimento)) {
  this.errorMessage = 'Data de vencimento inválida. Use o formato AAAA-MM-DD com ano de 4 dígitos.';
  return;
}

    if (this.conta.dataVencimento < (this.conta.dataCompra || '')) {
      this.errorMessage = 'Data de vencimento não pode ser menor que a data da compra';
      return;
    }

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