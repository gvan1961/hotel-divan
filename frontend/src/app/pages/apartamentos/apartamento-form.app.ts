import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ApartamentoService } from '../../services/apartamento.service';
import { TipoApartamentoService } from '../../services/tipo-apartamento.service';
import { ApartamentoRequest } from '../../models/apartamento.model';
import { TipoApartamento } from '../../models/tipo-apartamento.model';

@Component({
  selector: 'app-apartamento-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>{{ isEdit ? 'Editar Apartamento' : 'Novo Apartamento' }}</h1>
        <button class="btn-back" (click)="voltar()">← Voltar</button>
      </div>

      <button type="submit" class="btn-save" [disabled]="loading || apartamentoOcupado">
         {{ loading ? 'Salvando...' : (apartamentoOcupado ? 'Bloqueado' : 'Salvar') }}
      </button>

      <!-- ✅ AVISO SE OCUPADO -->
    <div *ngIf="apartamentoOcupado" class="warning-ocupado">
      <div class="warning-icon">🔴</div>
      <div class="warning-content">
        <strong>Apartamento OCUPADO</strong>
        <p>Este apartamento não pode ser editado enquanto estiver ocupado. Finalize a reserva primeiro.</p>
      </div>
    </div>

      <div class="form-card">
        <form (ngSubmit)="salvar()">
          <div class="form-row">
            <div class="form-group">
              <label>Número do Apartamento *</label>
              <input type="text" [(ngModel)]="apartamento.numeroApartamento" 
                     name="numeroApartamento" required 
                     placeholder="Ex: 101, 202, 305" />
            </div>

            <div class="form-group">
              <label>Tipo de Apartamento *</label>
              <select [(ngModel)]="apartamento.tipoApartamentoId" 
                      name="tipoApartamentoId" required>
                <option [ngValue]="0">Selecione o tipo</option>
                <option *ngFor="let tipo of tiposApartamento" [ngValue]="tipo.id">
                  {{ tipo.tipo }} - {{ tipo.descricao }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label>Capacidade de Hóspedes *</label>
            <input type="number" [(ngModel)]="apartamento.capacidade" 
                   name="capacidade" required min="1" 
                   placeholder="Ex: 2, 3, 4" />
            <small class="field-help">Quantidade máxima de pessoas no apartamento</small>
          </div>

          <div class="form-group">
            <label>Camas do Apartamento *</label>
            <textarea [(ngModel)]="apartamento.camasDoApartamento" 
                      name="camasDoApartamento" required rows="2"
                      placeholder="Ex: 1 cama de casal + 1 de solteiro, 2 camas de solteiro, 1 cama king size"></textarea>
            <small class="field-help">Descreva as camas (quantidade e tipo)</small>
          </div>

          <div class="form-group">
            <label>TV</label>
            <input type="text" [(ngModel)]="apartamento.tv" 
                   name="tv" 
                   placeholder="Ex: Smart TV LG 50 polegadas, TV Samsung 42 pol" />
            <small class="field-help">Especificações da TV (marca, tamanho, se é smart, etc.) - Opcional</small>
          </div>

          <div class="form-group cama-casal-campo">
  <label class="checkbox-label">
    <input type="checkbox" 
           [(ngModel)]="apartamento.temCamaDeCasal" 
           name="temCamaDeCasal" />
    <span>🛏️ Possui cama de casal</span>
  </label>
  <small class="field-help">
    Quando marcado, o preço da diária para 1 pessoa será cobrado pela tabela "1 pessoa CASAL".
    Para 2 ou mais pessoas, o preço é o mesmo (não depende da cama).
  </small>
</div>

          <div class="info-box">
            <strong>💡 Exemplos de descrição de camas:</strong>
            <ul>
              <li>1 cama de casal</li>
              <li>2 camas de solteiro</li>
              <li>1 cama de casal + 1 de solteiro</li>
              <li>3 camas de solteiro</li>
              <li>1 cama king size</li>
              <li>1 cama de casal + 2 beliches</li>
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

    input, select, textarea {
      width: 100%;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 5px;
      font-size: 14px;
      box-sizing: border-box;
      font-family: inherit;
    }

    textarea {
      resize: vertical;
    }

    input:focus, select:focus, textarea:focus {
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
      padding: 12px 16px;
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
      margin: 0;
      padding-left: 20px;
    }

    .info-box li {
      margin: 4px 0;
      color: #555;
    }

    /* AVISO DE APARTAMENTO OCUPADO */
.warning-ocupado {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.02); }
}

.warning-icon {
  font-size: 3em;
  animation: blink 1.5s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.warning-content {
  flex: 1;
}

.warning-content strong {
  display: block;
  font-size: 1.2em;
  margin-bottom: 5px;
}

.warning-content p {
  margin: 0;
  opacity: 0.95;
  font-size: 0.95em;
}

/* CAMPOS DESABILITADOS */
input:disabled,
select:disabled,
textarea:disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
  border-color: #e0e0e0;
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

    .cama-casal-campo {
  background: #fff8e1;
  border-left: 4px solid #ffc107;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
}

.checkbox-label {
  display: flex !important;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-weight: 600;
  color: #333;
  margin-bottom: 0 !important;
}

.checkbox-label input[type="checkbox"] {
  width: 20px;
  height: 20px;
  cursor: pointer;
  margin: 0;
}

.checkbox-label span {
  user-select: none;
}

    @media (max-width: 768px) {
      .form-row {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class ApartamentoFormApp implements OnInit {
  private apartamentoService = inject(ApartamentoService);
  private tipoApartamentoService = inject(TipoApartamentoService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  apartamento: ApartamentoRequest = {
  numeroApartamento: '',
  tipoApartamentoId: 0,
  capacidade: 1,
  camasDoApartamento: '',
  tv: '',
  temCamaDeCasal: false
};


  tiposApartamento: TipoApartamento[] = [];
  loading = false;
  errorMessage = '';
  isEdit = false;
  apartamentoId?: number;
  statusApartamento?: string;  
  apartamentoOcupado = false;  

  ngOnInit(): void {
    console.log('🔵 Inicializando ApartamentoForm');
    this.carregarTiposApartamento();
    
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.apartamentoId = +params['id'];
        console.log('✏️ Modo edição - ID:', this.apartamentoId);
        this.carregarApartamento(this.apartamentoId);
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

  carregarApartamento(id: number): void {
  console.log('📦 Carregando apartamento ID:', id);
  this.apartamentoService.getById(id).subscribe({
    next: (data) => {
      console.log('📥 Dados recebidos do backend:', data);
      
      // ✅ VERIFICAR SE ESTÁ OCUPADO
      this.statusApartamento = data.status;
      this.apartamentoOcupado = data.status === 'OCUPADO';
      
      if (this.apartamentoOcupado) {
        console.log('🔴 Apartamento OCUPADO - bloqueando edição');
        this.errorMessage = '⚠️ Este apartamento está OCUPADO e não pode ser editado. Finalize a reserva primeiro.';
      }
      
      const tipoId = data.tipoApartamento?.id || data.tipoApartamentoId;
      
      this.apartamento = {
  numeroApartamento: data.numeroApartamento,
  tipoApartamentoId: Number(tipoId),
  capacidade: Number(data.capacidade),
  camasDoApartamento: data.camasDoApartamento || '',
  tv: data.tv || '',
  temCamaDeCasal: data.temCamaDeCasal || false
};
      
      console.log('✅ Apartamento carregado no formulário:', this.apartamento);
    },
    error: (err) => {
      console.error('❌ Erro ao carregar apartamento:', err);
      this.errorMessage = 'Erro ao carregar apartamento';
    }
  });
}

  salvar(): void {
    console.log('💾 Iniciando salvamento...');
    console.log('📝 Estado atual do formulário:', this.apartamento);

    // ✅ BLOQUEAR SALVAMENTO SE OCUPADO
  if (this.apartamentoOcupado) {
    this.errorMessage = '⚠️ Não é possível editar apartamento OCUPADO!';
    alert('⚠️ Não é possível editar apartamento OCUPADO! Finalize a reserva primeiro.');
    return;
  }
    
    if (!this.validarFormulario()) {
      console.log('⚠️ Validação falhou');
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const apartamentoRequest: ApartamentoRequest = {
  numeroApartamento: this.apartamento.numeroApartamento,
  tipoApartamentoId: Number(this.apartamento.tipoApartamentoId),
  capacidade: Number(this.apartamento.capacidade),
  camasDoApartamento: this.apartamento.camasDoApartamento,
  tv: this.apartamento.tv || undefined,
  temCamaDeCasal: this.apartamento.temCamaDeCasal || false
};

    console.log('📤 Request montado:', apartamentoRequest);

    const request = this.isEdit
      ? this.apartamentoService.update(this.apartamentoId!, apartamentoRequest)
      : this.apartamentoService.create(apartamentoRequest);

    request.subscribe({
      next: (response) => {
        console.log('✅ Salvo com sucesso:', response);
        this.router.navigate(['/apartamentos']);
      },
      error: (err) => {
        console.error('❌ Erro ao salvar:', err);
        console.error('❌ Detalhes:', err.error);
        this.loading = false;
        this.errorMessage = err.error?.message || err.error || 'Erro ao salvar apartamento';
      }
    });
  }

  validarFormulario(): boolean {
    console.log('🔍 Validando formulário...');
    
    if (!this.apartamento.numeroApartamento) {
      this.errorMessage = 'Número do apartamento é obrigatório';
      return false;
    }
    
    if (!this.apartamento.tipoApartamentoId || this.apartamento.tipoApartamentoId === 0) {
      this.errorMessage = 'Selecione o tipo de apartamento';
      return false;
    }
    
    if (this.apartamento.capacidade < 1) {
      this.errorMessage = 'Capacidade deve ser no mínimo 1 pessoa';
      return false;
    }
    
    if (!this.apartamento.camasDoApartamento || this.apartamento.camasDoApartamento.trim() === '') {
      this.errorMessage = 'Descrição das camas é obrigatória';
      return false;
    }
    
    console.log('✅ Formulário válido');
    return true;
  }

  voltar(): void {
    this.router.navigate(['/apartamentos']);
  }
}


