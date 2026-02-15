import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ClienteService } from '../../services/cliente.service';
import { EmpresaService } from '../../services/empresa.service';
import { ClienteRequest, TipoCliente, TIPO_CLIENTE_LABELS } from '../../models/cliente.model';
import { Empresa } from '../../models/empresa.model';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-cliente-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>{{ isEdit ? 'Editar Cliente' : 'Novo Cliente' }}</h1>
        <button class="btn-back" (click)="voltar()">← Voltar</button>
      </div>

      <div class="form-card">
        <form (ngSubmit)="salvar()">
          
          <!-- ⭐ NOVO: TIPO DE CLIENTE -->
          <div class="form-group tipo-cliente-group">
            <label>Tipo de Cliente *</label>
            <select [(ngModel)]="cliente.tipoCliente" name="tipoCliente" required>
              <option [ngValue]="TipoCliente.HOSPEDE">🏨 Hóspede</option>
              <option [ngValue]="TipoCliente.FUNCIONARIO">👤 Funcionário</option>
            </select>
            <small class="field-help">
              {{ cliente.tipoCliente === TipoCliente.FUNCIONARIO ? 
                'Funcionários podem receber vales e acessar o sistema' : 
                'Hóspedes podem fazer reservas e hospedar-se no hotel' }}
            </small>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Nome *</label>
              <input type="text" [(ngModel)]="cliente.nome" name="nome" required />
            </div>

            <div class="form-group">
              <label>CPF *</label>
              <input type="text" [(ngModel)]="cliente.cpf" name="cpf" required 
                     (input)="formatarCpf()" maxlength="14" 
                     placeholder="000.000.000-00" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Celular *</label>
              <input type="text" [(ngModel)]="cliente.celular" name="celular" required 
                     (input)="formatarCelular()" maxlength="15" 
                     placeholder="(00) 00000-0000" />
            </div>

            <div class="form-group">
              <label>Data de Nascimento *</label>
              <input type="date" [(ngModel)]="dataNascimento" name="dataNascimento" required />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Endereço</label>
              <input type="text" [(ngModel)]="cliente.endereco" name="endereco" />
            </div>

            <div class="form-group">
              <label>CEP</label>
              <input type="text" [(ngModel)]="cliente.cep" name="cep" 
                     (input)="formatarCep()" maxlength="9" 
                     placeholder="00000-000" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Cidade</label>
              <input type="text" [(ngModel)]="cliente.cidade" name="cidade" />
            </div>

            <div class="form-group">
              <label>Estado</label>
              <input type="text" [(ngModel)]="cliente.estado" name="estado" maxlength="2" />
            </div>
          </div>

         <!-- ⭐ EMPRESA - SÓ PARA HÓSPEDES -->
         <div class="form-group" *ngIf="cliente.tipoCliente === TipoCliente.HOSPEDE">
              <label>Empresa</label>
              <select [(ngModel)]="selectedEmpresaId" name="empresaId" (change)="onEmpresaChange()">
              <option [value]="null">Selecione uma empresa</option>
              <option *ngFor="let empresa of empresas" [value]="empresa.id">
               {{ empresa.nomeEmpresa }}
           </option>
           </select>
          </div>

          <!-- CHECKBOX DE CRÉDITO - SÓ PARA HÓSPEDES -->
          <div class="form-group checkbox-group" *ngIf="cliente.tipoCliente === TipoCliente.HOSPEDE">
            <label class="checkbox-label">
              <input type="checkbox" 
                     [(ngModel)]="cliente.creditoAprovado" 
                     name="creditoAprovado">
              <span class="checkbox-text">✅ Aprovar Crédito (permite reservas faturadas)</span>
            </label>
            <small class="field-help">
              Cliente poderá fazer check-out sem pagar (gera conta a receber)
            </small>
          </div>

          <!-- CHECKBOX DE JANTAR - SÓ PARA HÓSPEDES -->
          <div class="form-group checkbox-group checkbox-jantar" *ngIf="cliente.tipoCliente === TipoCliente.HOSPEDE">
            <label class="checkbox-label">
              <input type="checkbox" 
                     [(ngModel)]="cliente.autorizadoJantar" 
                     name="autorizadoJantar">
              <span class="checkbox-text">🍽️ Autorizado para Jantar</span>
            </label>
            <small class="field-help">
              Cliente poderá jantar no hotel
            </small>
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
      max-width: 1000px;
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
      display: flex;
      flex-direction: column;
    }

    /* ⭐ ESTILO ESPECIAL PARA TIPO DE CLIENTE */
    .tipo-cliente-group {
      background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
      padding: 20px;
      border-radius: 8px;
      border: 2px solid #2196f3;
      margin-bottom: 25px;
    }

    .tipo-cliente-group label {
      font-size: 16px;
      font-weight: 600;
      color: #1976d2;
      margin-bottom: 10px;
    }

    .tipo-cliente-group select {
      background: white;
      font-weight: 600;
      font-size: 15px;
      padding: 12px;
      border: 2px solid #2196f3;
    }

    label {
      margin-bottom: 5px;
      color: #555;
      font-weight: 500;
    }

    input, select {
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 5px;
      font-size: 14px;
    }

    input:focus, select:focus {
      outline: none;
      border-color: #667eea;
    }

    /* ESTILOS PARA O CHECKBOX */
    .checkbox-group {
      background: #f8f9fa;
      padding: 15px;
      border-radius: 8px;
      border: 2px solid #e0e0e0;
      margin-bottom: 20px;
    }

    /* ⭐ ESTILO ESPECIAL PARA CHECKBOX DE JANTAR */
    .checkbox-jantar {
      background: linear-gradient(135deg, #fff5e6 0%, #ffe8cc 100%);
      border: 2px solid #ff9800;
    }

    .checkbox-label {
      display: flex;
      align-items: center;
      cursor: pointer;
      margin-bottom: 8px;
      font-weight: 500;
    }

    .checkbox-label input[type="checkbox"] {
      width: 20px;
      height: 20px;
      margin-right: 10px;
      cursor: pointer;
    }

    .checkbox-text {
      color: #2c3e50;
      font-size: 14px;
    }

    .field-help {
      display: block;
      font-size: 12px;
      color: #666;
      margin-top: 5px;
      margin-left: 30px;
      font-style: italic;
    }

    .tipo-cliente-group .field-help {
      margin-left: 0;
      margin-top: 8px;
      color: #1565c0;
      font-weight: 500;
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

    @media (max-width: 768px) {
      .form-row {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class ClienteFormApp implements OnInit {
  private clienteService = inject(ClienteService);
  private empresaService = inject(EmpresaService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);

  // ⭐ EXPOR O ENUM PARA O TEMPLATE
  TipoCliente = TipoCliente;

  selectedEmpresaId: any = null;

  cliente: ClienteRequest = {
    nome: '',
    cpf: '',
    celular: '',
    dataNascimento: '',
    creditoAprovado: false,
    autorizadoJantar: true,
    tipoCliente: TipoCliente.HOSPEDE  // ⭐ PADRÃO = HÓSPEDE
  };

  dataNascimento = '';
  empresas: Empresa[] = [];
  loading = false;
  errorMessage = '';
  isEdit = false;
  clienteId?: number;

  ngOnInit(): void {
    this.carregarEmpresas();
    
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.clienteId = +params['id'];
        this.carregarCliente(this.clienteId);
      }
    });
  }

  carregarEmpresas(): void {
    this.empresaService.getAll().subscribe({
      next: (data) => {
        this.empresas = data;
      },
      error: (err) => {
        console.error('Erro ao carregar empresas', err);
      }
    });
  }

  carregarCliente(id: number): void {
  this.clienteService.getById(id).subscribe({
    next: (data) => {
      console.log('==========================================');
      console.log('📥 DADOS RECEBIDOS DO BACKEND:');
      console.log('📥 Objeto completo:', data);
      console.log('📥 tipoCliente =', data.tipoCliente, '(tipo:', typeof data.tipoCliente, ')');
      console.log('==========================================');
      
      this.cliente = {
        nome: data.nome,
        cpf: data.cpf,
        celular: data.celular,
        endereco: data.endereco,
        cep: data.cep,
        cidade: data.cidade,
        estado: data.estado,
        dataNascimento: data.dataNascimento,
        empresaId: data.empresaId,
        creditoAprovado: data.creditoAprovado ?? false,
        autorizadoJantar: data.autorizadoJantar ?? false,
        // ⭐ CORREÇÃO: Converter string para enum corretamente
        tipoCliente: data.tipoCliente === 'FUNCIONARIO' ? TipoCliente.FUNCIONARIO : TipoCliente.HOSPEDE
      };
      
      console.log('==========================================');
      console.log('📝 APÓS POPULAR:');
      console.log('📝 cliente.tipoCliente =', this.cliente.tipoCliente);
      console.log('📝 É FUNCIONARIO?', this.cliente.tipoCliente === TipoCliente.FUNCIONARIO);
      console.log('==========================================');
      
      if (data.empresa && data.empresa.id) {
        this.selectedEmpresaId = data.empresa.id;
      } else if (data.empresaId) {
        this.selectedEmpresaId = data.empresaId;
      } else {
        this.selectedEmpresaId = null;
      }
      
      this.dataNascimento = data.dataNascimento.split('T')[0];
    },
    error: (err) => {
      console.error('Erro ao carregar cliente', err);
      this.errorMessage = 'Erro ao carregar cliente';
    }
  });
}

  salvar(): void {
    if (!this.validarFormulario()) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const dataISO = new Date(this.dataNascimento + 'T00:00:00').toISOString();
    
    const clienteRequest: ClienteRequest = {
      nome: this.cliente.nome,
      cpf: this.cliente.cpf,
      celular: this.cliente.celular,
      dataNascimento: dataISO,
      endereco: this.cliente.endereco || undefined,
      cep: this.cliente.cep || undefined,
      cidade: this.cliente.cidade || undefined,
      estado: this.cliente.estado || undefined,
      empresaId: this.selectedEmpresaId ? Number(this.selectedEmpresaId) : undefined,
      creditoAprovado: this.cliente.creditoAprovado || false,
      autorizadoJantar: this.cliente.autorizadoJantar !== undefined ? this.cliente.autorizadoJantar : true,
      tipoCliente: this.cliente.tipoCliente  // ⭐ ENVIAR TIPO
    };

    console.log('==========================================');
    console.log('🔵 MODO:', this.isEdit ? 'EDITAR' : 'CRIAR');
    console.log('👤 Tipo Cliente:', clienteRequest.tipoCliente);
    console.log('✅ Crédito Aprovado:', clienteRequest.creditoAprovado);
    console.log('🍽️ Autorizado Jantar:', clienteRequest.autorizadoJantar);
    console.log('🔵 PAYLOAD COMPLETO:', JSON.stringify(clienteRequest, null, 2));
    console.log('==========================================');

    const request = this.isEdit
      ? this.clienteService.update(this.clienteId!, clienteRequest)
      : this.clienteService.create(clienteRequest);

    request.subscribe({
      next: () => {
        console.log('✅ Cliente salvo com sucesso!');
        this.router.navigate(['/clientes']);
      },
      error: (err) => {
        this.loading = false;
        console.error('❌ ERRO AO SALVAR:', err);
        
        if (err.status === 401 || err.status === 403) {
          this.errorMessage = 'Acesso não autorizado. Verifique suas permissões.';
        } else if (err.error?.message) {
          this.errorMessage = err.error.message;
        } else {
          this.errorMessage = 'Erro ao salvar cliente. Verifique os dados e tente novamente.';
        }
      }
    });
  }

  validarFormulario(): boolean {
    if (!this.cliente.nome || !this.cliente.cpf || !this.cliente.celular || !this.dataNascimento) {
      this.errorMessage = 'Preencha todos os campos obrigatórios';
      return false;
    }
    return true;
  }

  formatarCep(): void {
    if (!this.cliente.cep) return;
    let cep = this.cliente.cep.replace(/\D/g, '');
    if (cep.length > 5) {
      cep = cep.substring(0, 5) + '-' + cep.substring(5, 8);
    }
    this.cliente.cep = cep;
  }

  formatarCpf(): void {
    if (!this.cliente.cpf) return;
    let cpf = this.cliente.cpf.replace(/\D/g, '');
    if (cpf.length > 3) {
      cpf = cpf.substring(0, 3) + '.' + cpf.substring(3);
    }
    if (cpf.length > 7) {
      cpf = cpf.substring(0, 7) + '.' + cpf.substring(7);
    }
    if (cpf.length > 11) {
      cpf = cpf.substring(0, 11) + '-' + cpf.substring(11, 13);
    }
    this.cliente.cpf = cpf;
  }

  formatarCelular(): void {
    if (!this.cliente.celular) return;
    let celular = this.cliente.celular.replace(/\D/g, '');
    if (celular.length > 0) {
      celular = '(' + celular;
    }
    if (celular.length > 3) {
      celular = celular.substring(0, 3) + ') ' + celular.substring(3);
    }
    if (celular.length > 10) {
      celular = celular.substring(0, 10) + '-' + celular.substring(10, 14);
    }
    this.cliente.celular = celular;
  }

  onEmpresaChange(): void {
    if (this.selectedEmpresaId === null || this.selectedEmpresaId === '' || !this.selectedEmpresaId) {
      this.cliente.empresaId = undefined;
    } else {
      this.cliente.empresaId = Number(this.selectedEmpresaId);
    }
  }

  voltar(): void {
    this.router.navigate(['/clientes']);
  }
}