import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { FornecedorService, Fornecedor } from '../../services/fornecedor.service';

@Component({
  selector: 'app-fornecedor-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>{{ isEdit ? '✏️ Editar Fornecedor' : '🏢 Novo Fornecedor' }}</h1>
        <button class="btn-back" (click)="router.navigate(['/fornecedores'])">← Voltar</button>
      </div>

      <div class="form-card">
        <div class="form-row">
          <div class="form-group">
            <label>Nome *</label>
            <input type="text" [(ngModel)]="fornecedor.nome" name="nome"
                   placeholder="Nome do fornecedor" required />
          </div>
          <div class="form-group">
            <label>CNPJ</label>
            <input type="text" [(ngModel)]="fornecedor.cnpj" name="cnpj"
                   placeholder="00.000.000/0001-00" (input)="formatarCnpj()" maxlength="18" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Telefone</label>
            <input type="text" [(ngModel)]="fornecedor.telefone" name="telefone"
                   placeholder="(00) 00000-0000" (input)="formatarTelefone()" maxlength="15" />
          </div>
          <div class="form-group">
            <label>Email</label>
            <input type="email" [(ngModel)]="fornecedor.email" name="email"
                   placeholder="email@fornecedor.com" />
          </div>
        </div>

        <div class="form-group">
          <label>Endereço</label>
          <input type="text" [(ngModel)]="fornecedor.endereco" name="endereco"
                 placeholder="Endereço completo" />
        </div>

        <div class="form-group">
          <label>Observação</label>
          <textarea [(ngModel)]="fornecedor.observacao" name="observacao" rows="3"
                    placeholder="Informações adicionais..."></textarea>
        </div>

        <div *ngIf="errorMessage" class="error-message">{{ errorMessage }}</div>

        <div class="form-actions">
          <button type="button" class="btn-cancel" (click)="router.navigate(['/fornecedores'])">Cancelar</button>
          <button type="button" class="btn-save" (click)="salvar()" [disabled]="loading">
            {{ loading ? 'Salvando...' : 'Salvar' }}
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 800px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h1 { color: #333; margin: 0; }
    .btn-back { background: #6c757d; color: white; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }
    .form-card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 16px; }
    .form-group { display: flex; flex-direction: column; margin-bottom: 16px; }
    .form-group label { margin-bottom: 6px; color: #555; font-weight: 500; }
    .form-group input, .form-group textarea { padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; }
    .form-group input:focus, .form-group textarea:focus { outline: none; border-color: #667eea; }
    .error-message { background: #fee; color: #c33; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
    .form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px; }
    .btn-cancel { background: #6c757d; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
    .btn-save { background: #667eea; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
    .btn-save:disabled { background: #aaa; cursor: not-allowed; }
    @media (max-width: 768px) { .form-row { grid-template-columns: 1fr; } }
  `]
})
export class FornecedorFormApp implements OnInit {
  router = inject(Router);
  private route = inject(ActivatedRoute);
  private service = inject(FornecedorService);

  isEdit = false;
  fornecedorId?: number;
  loading = false;
  errorMessage = '';

  fornecedor: Fornecedor = {
    nome: '',
    cnpj: '',
    telefone: '',
    email: '',
    endereco: '',
    observacao: ''
  };

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.fornecedorId = +params['id'];
        this.carregarFornecedor(this.fornecedorId);
      }
    });
  }

  carregarFornecedor(id: number): void {
    this.service.buscarPorId(id).subscribe({
      next: (data) => this.fornecedor = data,
      error: () => this.errorMessage = 'Erro ao carregar fornecedor'
    });
  }

  formatarCnpj(): void {
    if (!this.fornecedor.cnpj) return;
    let cnpj = this.fornecedor.cnpj.replace(/\D/g, '');
    if (cnpj.length > 2) cnpj = cnpj.substring(0, 2) + '.' + cnpj.substring(2);
    if (cnpj.length > 6) cnpj = cnpj.substring(0, 6) + '.' + cnpj.substring(6);
    if (cnpj.length > 10) cnpj = cnpj.substring(0, 10) + '/' + cnpj.substring(10);
    if (cnpj.length > 15) cnpj = cnpj.substring(0, 15) + '-' + cnpj.substring(15, 17);
    this.fornecedor.cnpj = cnpj;
  }

  formatarTelefone(): void {
    if (!this.fornecedor.telefone) return;
    let tel = this.fornecedor.telefone.replace(/\D/g, '');
    if (tel.length > 0) tel = '(' + tel;
    if (tel.length > 3) tel = tel.substring(0, 3) + ') ' + tel.substring(3);
    if (tel.length > 10) tel = tel.substring(0, 10) + '-' + tel.substring(10, 14);
    this.fornecedor.telefone = tel;
  }

  salvar(): void {
    if (!this.fornecedor.nome) {
      this.errorMessage = 'Nome é obrigatório';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const request = this.isEdit
      ? this.service.atualizar(this.fornecedorId!, this.fornecedor)
      : this.service.criar(this.fornecedor);

    request.subscribe({
      next: () => this.router.navigate(['/fornecedores']),
      error: (e) => {
        this.loading = false;
        this.errorMessage = e.error?.erro || 'Erro ao salvar';
      }
    });
  }
}