import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { FaixaConsumoAguaService } from '../../services/faixa-consumo-agua.service';
import { FaixaConsumoAguaRequest } from '../../models/faixa-consumo-agua.model';
import { EmpresaService } from '../../services/empresa.service';
import { Empresa } from '../../models/empresa.model';

@Component({
  selector: 'app-faixa-consumo-agua-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>{{ isEdit ? 'Editar Faixa' : 'Nova Faixa de Consumo de Água' }}</h1>
        <button class="btn-back" (click)="voltar()">← Voltar</button>
      </div>

      <div class="form-card">
        <form (ngSubmit)="salvar()">
          <div class="form-group">
            <label>Empresa *</label>
            <select [(ngModel)]="faixa.empresaId" name="empresaId" required [disabled]="isEdit">
              <option [ngValue]="undefined">Selecione a empresa</option>
              <option *ngFor="let emp of empresas" [ngValue]="emp.id">{{ emp.nomeEmpresa }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>Quantidade de Hóspedes *</label>
            <input type="number" [(ngModel)]="faixa.qtdHospedes" name="qtdHospedes"
                   required min="1" [disabled]="isEdit" placeholder="Ex: 1, 2, 3, 4..." />
          </div>

          <div class="form-group">
            <label>Valor Limite Diário (R$) *</label>
            <input type="number" [(ngModel)]="faixa.valorLimiteDiario" name="valorLimiteDiario"
                   required min="0.01" step="0.01" placeholder="Ex: 8.00" />
          </div>

          <div *ngIf="errorMessage" class="error-message">{{ errorMessage }}</div>

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
    .container { padding: 20px; max-width: 800px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h1 { color: #333; margin: 0; }
    .btn-back { background: #6c757d; color: white; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }
    .form-card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
    .form-group { margin-bottom: 20px; }
    label { display: block; margin-bottom: 5px; color: #555; font-weight: 500; }
    input, select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; box-sizing: border-box; font-family: inherit; }
    input:focus, select:focus { outline: none; border-color: #667eea; }
    input:disabled, select:disabled { background: #f0f0f0; color: #888; }
    .error-message { background: #fee; color: #c33; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
    .form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 30px; }
    .btn-cancel, .btn-save { padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; font-size: 14px; }
    .btn-cancel { background: #6c757d; color: white; }
    .btn-save { background: #667eea; color: white; }
    .btn-save:disabled { background: #ccc; cursor: not-allowed; }
  `]
})
export class FaixaConsumoAguaFormApp implements OnInit {
  private faixaService = inject(FaixaConsumoAguaService);
  private empresaService = inject(EmpresaService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  faixa: FaixaConsumoAguaRequest = {
    empresaId: undefined as any,
    qtdHospedes: undefined as any,
    valorLimiteDiario: undefined as any
  };

  empresas: Empresa[] = [];
  loading = false;
  errorMessage = '';
  isEdit = false;
  faixaId?: number;

  ngOnInit(): void {
    this.empresaService.getAll().subscribe({
      next: (data) => this.empresas = data,
      error: (err) => console.error('Erro ao carregar empresas', err)
    });

    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.faixaId = +params['id'];
        this.carregarFaixa(this.faixaId);
      }
    });
  }

  carregarFaixa(id: number): void {
    this.faixaService.getAll().subscribe({
      next: (lista) => {
        const encontrada = lista.find(f => f.id === id);
        if (encontrada) {
          this.faixa = {
            empresaId: encontrada.empresaId,
            qtdHospedes: encontrada.qtdHospedes,
            valorLimiteDiario: encontrada.valorLimiteDiario
          };
        }
      },
      error: (err) => {
        console.error('Erro ao carregar faixa', err);
        this.errorMessage = 'Erro ao carregar faixa';
      }
    });
  }

  salvar(): void {
    if (!this.validarFormulario()) return;

    this.loading = true;
    this.errorMessage = '';

    const request = this.isEdit
      ? this.faixaService.update(this.faixaId!, this.faixa)
      : this.faixaService.create(this.faixa);

    request.subscribe({
      next: () => this.router.navigate(['/faixas-consumo-agua']),
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || err.error || 'Erro ao salvar faixa';
      }
    });
  }

  validarFormulario(): boolean {
    if (!this.faixa.empresaId) { this.errorMessage = 'Selecione a empresa'; return false; }
    if (!this.faixa.qtdHospedes || this.faixa.qtdHospedes < 1) { this.errorMessage = 'Informe a quantidade de hóspedes'; return false; }
    if (!this.faixa.valorLimiteDiario || this.faixa.valorLimiteDiario <= 0) { this.errorMessage = 'Informe o valor limite'; return false; }
    return true;
  }

  voltar(): void {
    this.router.navigate(['/faixas-consumo-agua']);
  }
}