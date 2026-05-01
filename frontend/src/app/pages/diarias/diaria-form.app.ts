import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { DiariaService } from '../../services/diaria.service';
import { TipoApartamentoService } from '../../services/tipo-apartamento.service';
import { DiariaRequest } from '../../models/diaria.model';
import { TipoApartamento } from '../../models/tipo-apartamento.model';

@Component({
  selector: 'app-diaria-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>{{ isEdit ? '✏️ Editar Diária' : '➕ Nova Diária' }}</h1>
        <button class="btn-back" (click)="voltar()">← Voltar</button>
      </div>

      <div class="form-card">
        <form (ngSubmit)="salvar()">
          <div class="form-group">
            <label>Tipo de Apartamento *</label>
            <select [(ngModel)]="diaria.tipoApartamentoId" 
                    name="tipoApartamentoId" required>
              <option [ngValue]="0">Selecione o tipo</option>
              <option *ngFor="let tipo of tiposApartamento" [ngValue]="tipo.id">
                {{ tipo.tipo }} - {{ tipo.descricao }}
              </option>
            </select>
          </div>

          <div class="form-row">
  <div class="form-group">
    <label>Quantidade de Hóspedes *</label>
    <input type="number" [(ngModel)]="diaria.quantidade" 
           name="quantidade" required min="1" max="10"
           (ngModelChange)="onQuantidadeChange()"
           placeholder="Ex: 1, 2, 3..." />
    <small class="field-help">Número de pessoas que ocuparão o apartamento</small>
  </div>

  <div class="form-group">
    <label>Valor da Diária (R$) *</label>
    <input type="number" [(ngModel)]="diaria.valor" 
           name="valor" required min="0.01" step="0.01"
           placeholder="Ex: 150.00" />
    <small class="field-help">Valor cobrado por dia</small>
  </div>
</div>

<!-- ✅ Modalidade — só aparece quando quantidade = 1 -->
<div class="form-group modalidade-campo" *ngIf="diaria.quantidade == 1">
  <label>Modalidade da Cama *</label>
  <select [(ngModel)]="diaria.modalidade" name="modalidade" required>
    <option [ngValue]="null">Selecione a modalidade</option>
    <option value="SOLTEIRO">🛏️ Cama de Solteiro</option>
    <option value="CASAL">👫 Cama de Casal</option>
  </select>
  <small class="field-help">
    Para 1 pessoa, o preço varia conforme o tipo de cama.
    Apartamento com cama de casal cobra "1 pessoa CASAL", apto sem cama de casal cobra "1 pessoa SOLTEIRO".
  </small>
</div>

          <div class="info-box">
  <strong>💡 Dica:</strong>
  <p>Cada combinação de Tipo + Quantidade (+ Modalidade quando 1 pessoa) é única:</p>
  <ul>
    <li>Tipo A + 1 hóspede + SOLTEIRO = R$ 115,00</li>
    <li>Tipo A + 1 hóspede + CASAL = R$ 130,00</li>
    <li>Tipo A + 2 hóspedes = R$ 155,00</li>
    <li>Tipo A + 3 hóspedes = R$ 200,00</li>
  </ul>
</div>

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

    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
      margin-bottom: 20px;
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

    input, select {
      width: 100%;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 5px;
      font-size: 14px;
      box-sizing: border-box;
    }

    input:focus, select:focus {
      outline: none;
      border-color: #667eea;
    }

    .field-help {
      display: block;
      font-size: 12px;
      color: #666;
      margin-top: 4px;
      font-style: italic;
    }

    .info-box {
      background: #e3f2fd;
      border-left: 4px solid #2196f3;
      padding: 15px;
      margin-bottom: 20px;
      border-radius: 4px;
      font-size: 13px;
    }

    .info-box strong {
      color: #1976d2;
      display: block;
      margin-bottom: 8px;
    }

    .info-box ul {
      margin: 10px 0 0 20px;
      padding: 0;
    }

    .info-box li {
      margin: 5px 0;
      color: #555;
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
      background: #28a745;
      color: white;
    }

    .btn-save:hover:not(:disabled) {
      background: #218838;
    }

    .btn-save:disabled {
      background: #ccc;
      cursor: not-allowed;
    }

    @media (max-width: 768px) {
      .form-row {
        grid-template-columns: 1fr;
      }
    }

    .modalidade-campo {
  background: #fff8e1;
  border-left: 4px solid #ffc107;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
}

.modalidade-campo label {
  color: #f57c00;
  font-weight: 700;
}
  `]
})  
export class DiariaFormApp implements OnInit {
  private diariaService = inject(DiariaService);
  private tipoApartamentoService = inject(TipoApartamentoService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  diaria: DiariaRequest = {
  tipoApartamentoId: 0,
  quantidade: 1,
  valor: 0,
  modalidade: 'SOLTEIRO'  // padrão para 1 hóspede
};

  tiposApartamento: TipoApartamento[] = [];
  loading = false;
  errorMessage = '';
  isEdit = false;
  diariaId?: number;

  ngOnInit(): void {
    console.log('🔵 Inicializando DiariaForm');
    this.carregarTiposApartamento();
    
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.diariaId = +params['id'];
        console.log('✏️ Modo edição - ID:', this.diariaId);
        this.carregarDiaria(this.diariaId);
      } else {
        console.log('➕ Modo criação');
      }
    });
  }

  carregarTiposApartamento(): void {
    console.log('📋 Carregando tipos de apartamento...');
    this.tipoApartamentoService.getAll().subscribe({
      next: (data) => {
        this.tiposApartamento = data;
        console.log('✅ Tipos carregados:', data);
      },
      error: (err) => {
        console.error('❌ Erro ao carregar tipos:', err);
      }
    });
  }

  carregarDiaria(id: number): void {
    console.log('📦 Carregando diária ID:', id);
    this.diariaService.getById(id).subscribe({
      next: (data) => {
        console.log('📥 Dados recebidos do backend:', data);
        
        this.diaria = {
    tipoApartamentoId: data.tipoApartamentoId,
    quantidade: data.quantidade,
    valor: data.valor,
    modalidade: data.modalidade || null
  };  
        
        console.log('✅ Diária carregada no formulário:', this.diaria);
      },
      error: (err) => {
        console.error('❌ Erro ao carregar diária:', err);
        this.errorMessage = 'Erro ao carregar diária';
      }
    });
  }

  salvar(): void {
    console.log('💾 Iniciando salvamento...');
    console.log('📝 Estado atual do formulário:', this.diaria);
    
    if (!this.validarFormulario()) {
      console.log('⚠️ Validação falhou');
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const diariaRequest: DiariaRequest = {
  tipoApartamentoId: Number(this.diaria.tipoApartamentoId),
  quantidade: Number(this.diaria.quantidade),
  valor: Number(this.diaria.valor),
  modalidade: Number(this.diaria.quantidade) === 1 ? this.diaria.modalidade : null
};

    console.log('📤 Request montado:', diariaRequest);

    const request = this.isEdit
      ? this.diariaService.update(this.diariaId!, diariaRequest)
      : this.diariaService.create(diariaRequest);

    request.subscribe({
      next: (response) => {
        console.log('✅ Salvo com sucesso:', response);
        this.router.navigate(['/diarias']);
      },
      error: (err) => {
        console.error('❌ Erro ao salvar:', err);
        console.error('❌ Detalhes:', err.error);
        this.loading = false;
        this.errorMessage = err.error?.message || err.error || 'Erro ao salvar diária';
      }
    });
  }

  validarFormulario(): boolean {
    console.log('🔍 Validando formulário...');
    
    if (!this.diaria.tipoApartamentoId || this.diaria.tipoApartamentoId === 0) {
      this.errorMessage = 'Selecione o tipo de apartamento';
      return false;
    }
    
    if (this.diaria.quantidade < 1) {
      this.errorMessage = 'Quantidade deve ser no mínimo 1';
      return false;
    }
    
    if (this.diaria.valor <= 0) {
      this.errorMessage = 'Valor deve ser maior que zero';
      return false;
    }

    // ✅ NOVO — Para 1 pessoa, modalidade é obrigatória
  if (Number(this.diaria.quantidade) === 1 && !this.diaria.modalidade) {
    this.errorMessage = 'Para 1 hóspede, selecione a modalidade (Solteiro ou Casal)';
    return false;
  }
    
    console.log('✅ Formulário válido');
    return true;
  }

  onQuantidadeChange(): void {
  // Para 2+ hóspedes, modalidade não se aplica → limpa
  if (Number(this.diaria.quantidade) > 1) {
    this.diaria.modalidade = null;
  } else if (Number(this.diaria.quantidade) === 1 && !this.diaria.modalidade) {
    // Para 1 hóspede, sugere SOLTEIRO como padrão se ainda não tiver escolhido
    this.diaria.modalidade = 'SOLTEIRO';
  }
}

  voltar(): void {
    this.router.navigate(['/diarias']);
  }
}
