import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { EmpresaService } from '../../services/empresa.service';
import { EmpresaRequest } from '../../models/empresa.model';
import { FaixaConsumoAguaService } from '../../services/faixa-consumo-agua.service';
import { FaixaConsumoAgua, FaixaConsumoAguaRequest } from '../../models/faixa-consumo-agua.model';

@Component({
  selector: 'app-empresa-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>{{ isEdit ? 'Editar Empresa' : 'Nova Empresa' }}</h1>
        <button class="btn-back" (click)="voltar()">← Voltar</button>
      </div>

      <div class="form-card">
        <form (ngSubmit)="salvar()">
          <div class="form-group">
            <label>Nome da Empresa *</label>
            <input type="text" [(ngModel)]="empresa.nomeEmpresa" name="nomeEmpresa" required />
          </div>

          <div class="form-group">
            <label>CNPJ *</label>
            <input type="text" [(ngModel)]="empresa.cnpj" name="cnpj" required 
                   (input)="formatarCnpj()" maxlength="18" 
                   placeholder="00.000.000/0000-00" />
          </div>

          <div class="form-group">
            <label>Contato *</label>
            <input type="text" [(ngModel)]="empresa.contato" name="contato" required />
          </div>

          <div class="form-group">
            <label>Celular *</label>
            <input type="text" [(ngModel)]="empresa.celular" name="celular" required 
                   (input)="formatarCelular()" maxlength="15" 
                   placeholder="(00) 00000-0000" />
          </div>

          <!-- ===== NOVO: Contato Financeiro ===== -->
          <fieldset class="contato-financeiro">
            <legend>📲 Contato Financeiro (Notificações de Checkout Faturado)</legend>
            <p class="hint">
              Quando uma reserva for finalizada como faturada, este contato receberá uma 
              mensagem no WhatsApp com o resumo da estadia.
            </p>

            <div class="form-group">
              <label>Nome do Contato</label>
              <input type="text" [(ngModel)]="empresa.contatoFinanceiroNome" 
                     name="contatoFinanceiroNome" maxlength="100"
                     placeholder="Ex: Maria Silva (Financeiro)" />
            </div>

            <div class="form-row">
              <div class="form-group ddi">
                <label>DDI</label>
                <input type="text" [(ngModel)]="empresa.contatoFinanceiroDdi" 
                       name="contatoFinanceiroDdi" maxlength="3"
                       placeholder="55" />
              </div>

              <div class="form-group celular-fin">
                <label>Celular WhatsApp</label>
                <input type="text" [(ngModel)]="empresa.contatoFinanceiroCelular" 
                       name="contatoFinanceiroCelular"
                       (input)="formatarCelularFinanceiro()" maxlength="15"
                       placeholder="(00) 00000-0000" />
              </div>
            </div>
          </fieldset>
          <!-- ===== FIM Contato Financeiro ===== -->

          <!-- ===== NOVO: Controle de Consumo de Água ===== -->
<fieldset class="controle-agua">
  <legend>💧 Controle de Consumo de Água (Convênio)</legend>

  <div *ngIf="!isEdit" class="hint">
    Salve a empresa primeiro para habilitar o controle de consumo de água.
  </div>

  <div *ngIf="isEdit">
    <p class="hint">
      Defina o limite diário de água mineral por quantidade de hóspedes na reserva.
      Se nenhuma faixa for cadastrada, não há controle de consumo para esta empresa.
    </p>

    <table class="tabela-faixas" *ngIf="faixas.length > 0">
      <thead>
        <tr>
          <th>Qtd. Hóspedes</th>
          <th>Valor Limite Diário</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let faixa of faixas">
          <td>{{ faixa.qtdHospedes }}</td>
          <td>{{ faixa.valorLimiteDiario | currency:'BRL' }}</td>
          <td>
            <button type="button" class="btn-remove-faixa" (click)="removerFaixa(faixa.id!)">Excluir</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="nova-faixa-row">
      <div class="form-group qtd">
        <label>Qtd. Hóspedes</label>
        <input type="number" [(ngModel)]="novaFaixa.qtdHospedes" name="novaFaixaQtd" min="1" />
      </div>
      <div class="form-group valor">
        <label>Valor Limite Diário (R$)</label>
        <input type="number" [(ngModel)]="novaFaixa.valorLimiteDiario" name="novaFaixaValor" min="0.01" step="0.01" />
      </div>
      <button type="button" class="btn-add-faixa" (click)="adicionarFaixa()">+ Adicionar Faixa</button>
    </div>

    <div *ngIf="faixaErrorMessage" class="error-message">{{ faixaErrorMessage }}</div>
  </div>
</fieldset>
<!-- ===== FIM Controle de Consumo de Água ===== -->

          <div *ngIf="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>

          <div class="form-actions">
            <button type="button" class="btn-cancel" (click)="voltar()">Cancelar</button>
            <button type="submit" class="btn-save" [disabled]="loading">
              {{ loading ? 'Salvando...' : 'Salvar' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .container {
      padding: 20px;
      max-width: 800px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    h1 {
      color: #333;
      margin: 0;
    }

    .btn-back {
      background: #6c757d;
      color: white;
      border: none;
      padding: 8px 16px;
      border-radius: 5px;
      cursor: pointer;
    }

    .btn-back:hover {
      background: #5a6268;
    }

    .form-card {
      background: white;
      padding: 30px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .form-group {
      margin-bottom: 20px;
    }

    label {
      display: block;
      margin-bottom: 5px;
      color: #555;
      font-weight: 500;
    }

    input {
      width: 100%;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 5px;
      font-size: 14px;
      box-sizing: border-box;
    }

    input:focus {
      outline: none;
      border-color: #667eea;
    }

    .error-message {
      background: #fee;
      color: #c33;
      padding: 10px;
      border-radius: 5px;
      margin-bottom: 15px;
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 30px;
    }

    .btn-cancel, .btn-save {
      padding: 10px 20px;
      border: none;
      border-radius: 5px;
      cursor: pointer;
      font-size: 14px;
    }

    .btn-cancel {
      background: #6c757d;
      color: white;
    }

    .btn-cancel:hover {
      background: #5a6268;
    }

    .btn-save {
      background: #667eea;
      color: white;
    }

    .btn-save:hover:not(:disabled) {
      background: #5568d3;
    }

    .btn-save:disabled {
      background: #ccc;
      cursor: not-allowed;
    }

    /* ===== Contato Financeiro ===== */
    .contato-financeiro {
      border: 1px solid #d0e1f9;
      background: #f7fbff;
      border-radius: 8px;
      padding: 20px;
      margin: 25px 0;
    }

    .contato-financeiro legend {
      padding: 0 10px;
      font-weight: 600;
      color: #175197;
      font-size: 15px;
    }

    .contato-financeiro .hint {
      font-size: 13px;
      color: #666;
      margin: 0 0 15px 0;
      line-height: 1.5;
    }

    .controle-agua { margin-top: 20px; padding: 15px; border: 1px solid #ddd; border-radius: 6px; }
.controle-agua legend { font-weight: 600; padding: 0 8px; }
.tabela-faixas { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
.tabela-faixas th { text-align: left; padding: 8px; background: #f5f5f5; font-size: 0.85rem; }
.tabela-faixas td { padding: 8px; border-top: 1px solid #eee; font-size: 0.9rem; }
.btn-remove-faixa { background: #dc3545; color: white; border: none; padding: 4px 10px; border-radius: 4px; cursor: pointer; font-size: 0.8rem; }
.nova-faixa-row { display: flex; gap: 10px; align-items: flex-end; }
.nova-faixa-row .form-group { margin-bottom: 0; flex: 1; }
.btn-add-faixa { background: #667eea; color: white; border: none; padding: 10px 16px; border-radius: 5px; cursor: pointer; white-space: nowrap; }

    .form-row {
      display: flex;
      gap: 15px;
    }

    .form-row .ddi {
      flex: 0 0 80px;
    }

    .form-row .celular-fin {
      flex: 1;
    }
  `]
})
export class EmpresaFormApp implements OnInit {
  private empresaService = inject(EmpresaService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  empresa: EmpresaRequest = {
    nomeEmpresa: '',
    cnpj: '',
    contato: '',
    celular: '',
    contatoFinanceiroNome: '',
    contatoFinanceiroDdi: '55',
    contatoFinanceiroCelular: ''
  };

  loading = false;
  errorMessage = '';
  isEdit = false;
  empresaId?: number;

  private faixaService = inject(FaixaConsumoAguaService);

faixas: FaixaConsumoAgua[] = [];
novaFaixa: { qtdHospedes?: number; valorLimiteDiario?: number } = {};
faixaErrorMessage = '';

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.empresaId = +params['id'];
        this.carregarEmpresa(this.empresaId);
      }
    });
  }

  carregarEmpresa(id: number): void {
  this.empresaService.getById(id).subscribe({
    next: (data) => {
      this.empresa = {
        nomeEmpresa: data.nomeEmpresa,
        cnpj: data.cnpj,
        contato: data.contato,
        celular: data.celular,
        contatoFinanceiroNome: data.contatoFinanceiroNome || '',
        contatoFinanceiroDdi: data.contatoFinanceiroDdi || '55',
        contatoFinanceiroCelular: data.contatoFinanceiroCelular || ''
      };
    },
    error: (err) => {
      console.error('Erro ao carregar empresa', err);
      this.errorMessage = 'Erro ao carregar empresa';
    }
  });
  this.carregarFaixas(id);
}

carregarFaixas(empresaId: number): void {
  this.faixaService.getByEmpresa(empresaId).subscribe({
    next: (data) => this.faixas = data,
    error: (err) => console.error('Erro ao carregar faixas', err)
  });
}

  salvar(): void {
  if (!this.validarFormulario()) {
    return;
  }

  this.loading = true;
  this.errorMessage = '';

  // ✅ Remover DDI do celular antes de enviar
  let celularLimpo = this.empresa.celular || '';
  celularLimpo = celularLimpo.replace(/^\+\d{1,3}\s?/, '').trim();

  const empresaRequest: EmpresaRequest = {
    nomeEmpresa: this.empresa.nomeEmpresa,
    cnpj: this.empresa.cnpj,
    contato: this.empresa.contato,
    celular: celularLimpo,  // ← usa o celular sem DDI
    contatoFinanceiroNome: this.empresa.contatoFinanceiroNome || undefined,
    contatoFinanceiroDdi: this.empresa.contatoFinanceiroDdi || undefined,
    contatoFinanceiroCelular: this.empresa.contatoFinanceiroCelular || undefined
  };

  console.log('📤 Enviando empresa:', empresaRequest);

  const request = this.isEdit
    ? this.empresaService.update(this.empresaId!, empresaRequest)
    : this.empresaService.create(empresaRequest);

  request.subscribe({
    next: () => {
      console.log('✅ Empresa salva com sucesso!');
      this.router.navigate(['/empresas']);
    },
    error: (err) => {
      this.loading = false;
      console.error('❌ ERRO COMPLETO:', err);
      console.error('   Status:', err.status);
      console.error('   Mensagem:', err.error);
      
      if (err.status === 401 || err.status === 403) {
        this.errorMessage = 'Acesso não autorizado. Verifique suas permissões.';
      } else {
        this.errorMessage = err.error?.message || 'Erro ao salvar empresa';
      }
    }
  });
}
  validarFormulario(): boolean {
    if (!this.empresa.nomeEmpresa || !this.empresa.cnpj || 
        !this.empresa.contato || !this.empresa.celular) {
      this.errorMessage = 'Preencha todos os campos obrigatórios';
      return false;
    }

    // Se preencheu celular financeiro, valida o formato
    if (this.empresa.contatoFinanceiroCelular) {
      const apenasDigitos = this.empresa.contatoFinanceiroCelular.replace(/\D/g, '');
      if (apenasDigitos.length < 10 || apenasDigitos.length > 11) {
        this.errorMessage = 'Celular do contato financeiro deve ter 10 ou 11 dígitos';
        return false;
      }
    }

    return true;
  }

  formatarCnpj(): void {
    if (!this.empresa.cnpj) return;

    let cnpj = this.empresa.cnpj.toUpperCase().replace(/[^A-Z0-9]/g, '');

    if (cnpj.length > 2) cnpj = cnpj.substring(0, 2) + '.' + cnpj.substring(2);
    if (cnpj.length > 6) cnpj = cnpj.substring(0, 6) + '.' + cnpj.substring(6);
    if (cnpj.length > 10) cnpj = cnpj.substring(0, 10) + '/' + cnpj.substring(10);
    if (cnpj.length > 15) cnpj = cnpj.substring(0, 15) + '-' + cnpj.substring(15, 17);

    this.empresa.cnpj = cnpj;
  }

  formatarCelular(): void {
  if (!this.empresa.celular) return;

  // ✅ Remove DDI antes de formatar
  let celular = this.empresa.celular.replace(/^\+\d{1,3}\s?/, '');
  celular = celular.replace(/\D/g, '');

  if (celular.length > 0) celular = '(' + celular;
  if (celular.length > 3) celular = celular.substring(0, 3) + ') ' + celular.substring(3);
  if (celular.length > 10) celular = celular.substring(0, 10) + '-' + celular.substring(10, 14);

  this.empresa.celular = celular;
}

  formatarCelularFinanceiro(): void {
    if (!this.empresa.contatoFinanceiroCelular) return;
    
    let celular = this.empresa.contatoFinanceiroCelular.replace(/\D/g, '');
    
    if (celular.length > 0) {
      celular = '(' + celular;
    }
    if (celular.length > 3) {
      celular = celular.substring(0, 3) + ') ' + celular.substring(3);
    }
    if (celular.length > 10) {
      celular = celular.substring(0, 10) + '-' + celular.substring(10, 14);
    }
    
    this.empresa.contatoFinanceiroCelular = celular;
  }

  adicionarFaixa(): void {
  this.faixaErrorMessage = '';

  if (!this.novaFaixa.qtdHospedes || this.novaFaixa.qtdHospedes < 1) {
    this.faixaErrorMessage = 'Informe a quantidade de hóspedes';
    return;
  }
  if (!this.novaFaixa.valorLimiteDiario || this.novaFaixa.valorLimiteDiario <= 0) {
    this.faixaErrorMessage = 'Informe o valor limite';
    return;
  }

  const request: FaixaConsumoAguaRequest = {
    empresaId: this.empresaId!,
    qtdHospedes: this.novaFaixa.qtdHospedes,
    valorLimiteDiario: this.novaFaixa.valorLimiteDiario
  };

  this.faixaService.create(request).subscribe({
    next: () => {
      this.novaFaixa = {};
      this.carregarFaixas(this.empresaId!);
    },
    error: (err) => {
      this.faixaErrorMessage = err.error?.message || err.error || 'Erro ao adicionar faixa';
    }
  });
}

removerFaixa(id: number): void {
  if (confirm('Deseja realmente excluir esta faixa?')) {
    this.faixaService.delete(id).subscribe({
      next: () => this.carregarFaixas(this.empresaId!),
      error: (err) => {
        console.error('Erro ao excluir faixa', err);
        alert(err.error?.message || err.error || 'Erro ao excluir faixa');
      }
    });
  }
}

  voltar(): void {
    this.router.navigate(['/empresas']);
  }
}
