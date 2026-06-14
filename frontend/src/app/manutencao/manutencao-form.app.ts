import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

// 🔧 AJUSTE os caminhos conforme sua estrutura de pastas
import { ManutencaoService } from '../services/manutencao.service';
import { ApartamentoService } from '../services/apartamento.service';
import {
  ManutencaoRequest,
  StatusManutencao,
  TIPOS_SERVICO,
  STATUS_MANUTENCAO,
} from '../models/manutencao.model';

@Component({
  selector: 'app-manutencao-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>{{ isEdit ? '✏️ Editar Manutenção' : '➕ Nova Manutenção' }}</h1>
        <button class="btn-back" (click)="voltar()">← Voltar</button>
      </div>

      <div class="form-card">
        <form (ngSubmit)="salvar()">
          <div class="form-row">
            <div class="form-group">
              <label>Apartamento *</label>
              <select [(ngModel)]="manutencao.apartamentoId" name="apartamentoId" required>
                <option [ngValue]="0">Selecione o apartamento</option>
                <option *ngFor="let apt of apartamentos" [ngValue]="apt.id">
                  {{ apt.numeroApartamento }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>Data do Serviço *</label>
              <input type="date" [(ngModel)]="manutencao.dataServico" name="dataServico" required />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Tipo de Serviço *</label>
              <select [(ngModel)]="manutencao.tipoServico" name="tipoServico" required>
                <option value="">Selecione o tipo</option>
                <option *ngFor="let t of tipos" [value]="t.valor">{{ t.icone }} {{ t.descricao }}</option>
              </select>
            </div>

            <div class="form-group">
              <label>Status</label>
              <select [(ngModel)]="manutencao.status" name="status">
                <option *ngFor="let s of statusList" [value]="s.valor">{{ s.descricao }}</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label>Descrição *</label>
            <textarea [(ngModel)]="manutencao.descricao" name="descricao" required rows="3"
                      placeholder="Ex: Troca da resistência do chuveiro; ar não gelava, recarga de gás..."></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Responsável / Técnico</label>
              <input type="text" [(ngModel)]="manutencao.responsavel" name="responsavel"
                     placeholder="Ex: João (manutenção), Refrigeração Silva" />
            </div>

            <div class="form-group">
              <label>Custo (R$)</label>
              <input type="number" [(ngModel)]="manutencao.custo" name="custo" min="0" step="0.01"
                     placeholder="Ex: 150.00" />
            </div>
          </div>

          <div class="form-group" *ngIf="manutencao.status === 'CONCLUIDO'">
            <label>Data de Conclusão</label>
            <input type="date" [(ngModel)]="manutencao.dataConclusao" name="dataConclusao" />
          </div>

          <div class="form-group">
            <label>Observações</label>
            <textarea [(ngModel)]="manutencao.observacoes" name="observacoes" rows="2"
                      placeholder="Informações adicionais (opcional)"></textarea>
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
    .btn-back:hover { background: #5a6268; }
    .form-card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,.1); }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
    .form-group { margin-bottom: 20px; }
    label { display: block; margin-bottom: 5px; color: #555; font-weight: 500; }
    input, select, textarea {
      width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px;
      font-size: 14px; box-sizing: border-box; font-family: inherit;
    }
    textarea { resize: vertical; }
    input:focus, select:focus, textarea:focus { outline: none; border-color: #667eea; }
    .error-message { background: #fee; color: #c33; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
    .form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 30px; }
    .btn-cancel, .btn-save { padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; font-size: 14px; }
    .btn-cancel { background: #6c757d; color: white; }
    .btn-cancel:hover { background: #5a6268; }
    .btn-save { background: #667eea; color: white; }
    .btn-save:hover:not(:disabled) { background: #5568d3; }
    .btn-save:disabled { background: #ccc; cursor: not-allowed; }
    @media (max-width: 768px) { .form-row { grid-template-columns: 1fr; } }
  `]
})
export class ManutencaoFormApp implements OnInit {
  private manutencaoService = inject(ManutencaoService);
  private apartamentoService = inject(ApartamentoService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  manutencao: ManutencaoRequest = {
    apartamentoId: 0,
    dataServico: new Date().toISOString().substring(0, 10),
    tipoServico: '' as any,
    descricao: '',
    responsavel: '',
    custo: undefined,
    status: 'PENDENTE',
    dataConclusao: undefined,
    observacoes: '',
  };

  apartamentos: any[] = [];
  tipos = TIPOS_SERVICO;
  statusList = STATUS_MANUTENCAO;

  isEdit = false;
  manutencaoId?: number;
  loading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.apartamentoService.getAll().subscribe({
      next: (data: any[]) => {
        this.apartamentos = [...data].sort((a, b) =>
          a.numeroApartamento.localeCompare(b.numeroApartamento, 'pt-BR', { numeric: true })
        );
      },
      error: (err) => console.error('Erro ao carregar apartamentos', err),
    });

    // Pré-seleção via queryParam (ex.: vindo do card do apartamento)
    const aptQuery = this.route.snapshot.queryParamMap.get('apartamentoId');
    if (aptQuery) this.manutencao.apartamentoId = +aptQuery;

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.manutencaoId = +id;
      this.carregar(this.manutencaoId);
    }
  }

  carregar(id: number): void {
    this.manutencaoService.getById(id).subscribe({
      next: (data) => {
        this.manutencao = {
          apartamentoId: data.apartamentoId,
          dataServico: data.dataServico,
          tipoServico: data.tipoServico,
          descricao: data.descricao,
          responsavel: data.responsavel || '',
          custo: data.custo,
          status: data.status,
          dataConclusao: data.dataConclusao,
          observacoes: data.observacoes || '',
        };
      },
      error: (err) => { console.error('Erro ao carregar manutenção', err); this.errorMessage = 'Erro ao carregar manutenção'; },
    });
  }

  validar(): boolean {
    if (!this.manutencao.apartamentoId) { this.errorMessage = 'Selecione o apartamento'; return false; }
    if (!this.manutencao.tipoServico) { this.errorMessage = 'Selecione o tipo de serviço'; return false; }
    if (!this.manutencao.descricao?.trim()) { this.errorMessage = 'A descrição é obrigatória'; return false; }
    return true;
  }

  salvar(): void {
    if (!this.validar()) return;
    this.loading = true;
    this.errorMessage = '';

    const req = this.isEdit
      ? this.manutencaoService.update(this.manutencaoId!, this.manutencao)
      : this.manutencaoService.create(this.manutencao);

    req.subscribe({
      next: () => this.router.navigate(['/manutencoes']),
      error: (err) => {
        console.error('Erro ao salvar', err);
        this.loading = false;
        this.errorMessage = err.error?.message || err.error || 'Erro ao salvar manutenção';
      },
    });
  }

  voltar(): void { this.router.navigate(['/manutencoes']); }
}