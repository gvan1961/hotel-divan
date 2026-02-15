import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ValeService } from '../../services/vale.service';
import { ClienteService } from '../../services/cliente.service';
import { Vale, ValeRequest, TipoVale, TIPO_VALE_LABELS } from '../../models/vale.model';
import { Cliente } from '../../models/cliente.model';

@Component({
  selector: 'app-vale-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container-form">
      <div class="header">
        <h1>{{ modoEdicao ? '✏️ Editar Vale' : '➕ Novo Vale' }}</h1>
        <button class="btn-voltar" (click)="voltar()">
          ← Voltar
        </button>
      </div>

      <div class="card-form">
        <form (ngSubmit)="salvar()">
          
          <!-- FUNCIONÁRIO -->
          <div class="campo">
            <label>Funcionário *</label>
            <div class="busca-funcionario">
              <input 
                type="text"
                [(ngModel)]="termoBuscaFuncionario"
                name="termoBuscaFuncionario"
                (input)="buscarFuncionarios()"
                placeholder="🔍 Digite o nome ou CPF do funcionário..."
                [disabled]="modoEdicao"
                class="input-busca">
              
              <div class="lista-resultados" *ngIf="funcionarios.length > 0 && !funcionarioSelecionado">
                <div 
                  class="item-resultado"
                  *ngFor="let func of funcionarios"
                  (click)="selecionarFuncionario(func)">
                  <strong>{{ func.nome }}</strong>
                  <small>CPF: {{ func.cpf }}</small>
                </div>
              </div>
            </div>

            <div class="funcionario-selecionado" *ngIf="funcionarioSelecionado">
              <div class="info-funcionario">
                <div>
                  <strong>{{ funcionarioSelecionado.nome }}</strong>
                  <br>
                  <small>CPF: {{ funcionarioSelecionado.cpf }}</small>
                </div>
                <button 
                  type="button"
                  class="btn-limpar"
                  (click)="limparSelecao()"
                  *ngIf="!modoEdicao">
                  ❌
                </button>
              </div>

              <!-- TOTAL PENDENTE -->
              <div class="alerta-pendente" *ngIf="totalPendente > 0">
                ⚠️ Este funcionário possui <strong>R$ {{ totalPendente | number:'1.2-2' }}</strong> em vales pendentes
              </div>
            </div>
          </div>

          <!-- TIPO DE VALE -->
          <div class="campo">
            <label>Tipo de Vale *</label>
            <select 
              [(ngModel)]="form.tipoVale"
              name="tipoVale"
              required>
              <option [ngValue]="null">Selecione o tipo</option>
              <option *ngFor="let tipo of tiposVale" [ngValue]="tipo.valor">
                {{ tipo.label }}
              </option>
            </select>
          </div>

          <!-- ⭐ VALOR (CORRIGIDO) -->
          <div class="campo">
            <label>Valor *</label>
            <div class="input-group-valor">
              <span class="prefixo">R$</span>
              <input 
                type="text" 
                [(ngModel)]="valorExibicao"
                name="valorExibicao"
                (keypress)="onKeyPress($event)"
                (focus)="onFocus()"
                placeholder="0,00"
                required
                class="input-valor"
              />
            </div>
            <small class="field-help">Digite o valor do vale em reais</small>
          </div>

          <!-- DATAS -->
          <div class="campos-linha">
            <div class="campo">
              <label>Data de Concessão *</label>
              <input 
                type="date"
                [(ngModel)]="form.dataConcessao"
                name="dataConcessao"
                required>
            </div>

            <div class="campo">
              <label>Data de Vencimento *</label>
              <input 
                type="date"
                [(ngModel)]="form.dataVencimento"
                name="dataVencimento"
                [min]="form.dataConcessao"
                required>
            </div>
          </div>

          <!-- OBSERVAÇÃO -->
          <div class="campo">
            <label>Observação</label>
            <textarea 
              [(ngModel)]="form.observacao"
              name="observacao"
              rows="4"
              placeholder="Informações adicionais sobre o vale...">
            </textarea>
          </div>

          <!-- BOTÕES -->
          <div class="form-footer">
            <button type="button" class="btn-cancelar" (click)="voltar()">
              Cancelar
            </button>
            <button 
              type="submit" 
              class="btn-salvar"
              [disabled]="!formValido()">
              {{ modoEdicao ? '💾 Salvar Alterações' : '✅ Criar Vale' }}
            </button>
          </div>
        </form>
      </div>

      <!-- LOADING -->
      <div class="loading-overlay" *ngIf="loading">
        <div class="spinner"></div>
        <p>{{ modoEdicao ? 'Salvando alterações...' : 'Criando vale...' }}</p>
      </div>
    </div>
  `,
  styles: [`
    .container-form {
      padding: 20px;
      max-width: 800px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
    }

    .header h1 {
      margin: 0;
      color: #2c3e50;
    }

    .btn-voltar {
      padding: 10px 20px;
      background: #95a5a6;
      color: white;
      border: none;
      border-radius: 6px;
      font-size: 1em;
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .btn-voltar:hover {
      background: #7f8c8d;
    }

    .card-form {
      background: white;
      border-radius: 12px;
      padding: 30px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .campo {
      margin-bottom: 25px;
    }

    .campo label {
      display: block;
      margin-bottom: 8px;
      font-weight: 600;
      color: #2c3e50;
    }

    .campo input,
    .campo select,
    .campo textarea {
      width: 100%;
      padding: 12px;
      border: 2px solid #e0e0e0;
      border-radius: 6px;
      font-size: 1em;
      transition: border-color 0.3s ease;
      box-sizing: border-box;
    }

    .campo input:focus,
    .campo select:focus,
    .campo textarea:focus {
      outline: none;
      border-color: #667eea;
    }

    .campo textarea {
      resize: vertical;
      font-family: inherit;
    }

    /* ⭐ CAMPO DE VALOR ESTILIZADO */
    .input-group-valor {
      display: flex;
      align-items: center;
      border: 2px solid #e0e0e0;
      border-radius: 6px;
      overflow: hidden;
      transition: border-color 0.3s ease;
      background: white;
    }

    .input-group-valor:focus-within {
      border-color: #667eea;
    }

    .prefixo {
      padding: 12px 15px;
      background: #f8f9fa;
      color: #27ae60;
      font-weight: 700;
      font-size: 1.1em;
      border-right: 2px solid #e0e0e0;
    }

    .input-valor {
      flex: 1;
      border: none !important;
      padding: 12px 15px;
      font-size: 1.1em;
      font-weight: 600;
      color: #27ae60;
      text-align: right;
      letter-spacing: 1px;
    }

    .input-valor:focus {
      outline: none;
    }

    .field-help {
      display: block;
      margin-top: 5px;
      color: #7f8c8d;
      font-size: 0.85em;
    }

    .campos-linha {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
    }

    /* BUSCA FUNCIONÁRIO */
    .busca-funcionario {
      position: relative;
    }

    .input-busca {
      width: 100%;
    }

    .lista-resultados {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      background: white;
      border: 2px solid #667eea;
      border-top: none;
      border-radius: 0 0 6px 6px;
      max-height: 300px;
      overflow-y: auto;
      z-index: 10;
      box-shadow: 0 4px 8px rgba(0,0,0,0.1);
    }

    .item-resultado {
      padding: 12px;
      cursor: pointer;
      border-bottom: 1px solid #f0f0f0;
      transition: background 0.2s ease;
    }

    .item-resultado:hover {
      background: #f0f4ff;
    }

    .item-resultado:last-child {
      border-bottom: none;
    }

    .item-resultado strong {
      display: block;
      color: #2c3e50;
      margin-bottom: 4px;
    }

    .item-resultado small {
      color: #7f8c8d;
    }

    .funcionario-selecionado {
      margin-top: 10px;
      padding: 15px;
      background: #f0f4ff;
      border-radius: 6px;
      border-left: 4px solid #667eea;
    }

    .info-funcionario {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .info-funcionario strong {
      color: #2c3e50;
      font-size: 1.1em;
    }

    .info-funcionario small {
      color: #7f8c8d;
    }

    .btn-limpar {
      padding: 6px 12px;
      background: #e74c3c;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.9em;
    }

    .btn-limpar:hover {
      background: #c0392b;
    }

    .alerta-pendente {
      margin-top: 10px;
      padding: 10px;
      background: #fff3cd;
      border-left: 4px solid #ff9800;
      border-radius: 4px;
      color: #856404;
    }

    .alerta-pendente strong {
      color: #e65100;
    }

    /* BOTÕES */
    .form-footer {
      display: flex;
      gap: 15px;
      justify-content: flex-end;
      margin-top: 30px;
      padding-top: 20px;
      border-top: 2px solid #f0f0f0;
    }

    .btn-cancelar,
    .btn-salvar {
      padding: 12px 30px;
      border: none;
      border-radius: 6px;
      font-size: 1em;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .btn-cancelar {
      background: #95a5a6;
      color: white;
    }

    .btn-cancelar:hover {
      background: #7f8c8d;
    }

    .btn-salvar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }

    .btn-salvar:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }

    .btn-salvar:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    /* LOADING */
    .loading-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.7);
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      z-index: 1000;
      color: white;
    }

    .spinner {
      border: 4px solid rgba(255,255,255,0.3);
      border-top: 4px solid white;
      border-radius: 50%;
      width: 50px;
      height: 50px;
      animation: spin 1s linear infinite;
      margin-bottom: 20px;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    .loading-overlay p {
      font-size: 1.2em;
      font-weight: 600;
    }

    @media (max-width: 768px) {
      .campos-linha {
        grid-template-columns: 1fr;
      }

      .form-footer {
        flex-direction: column;
      }

      .btn-cancelar,
      .btn-salvar {
        width: 100%;
      }
    }
  `]
})
export class ValeFormComponent implements OnInit {
  private valeService = inject(ValeService);
  private clienteService = inject(ClienteService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  modoEdicao = false;
  valeId?: number;
  loading = false;

  // Formulário
  form: ValeRequest = {
    clienteId: 0,
    dataConcessao: this.obterDataHoje(),
    dataVencimento: this.obterDataDaqui30Dias(),
    tipoVale: null as any,
    valor: 0,
    observacao: ''
  };

  // ⭐ NOVO: Valor para exibição formatada
  valorExibicao = '0,00';
  valorCentavos = 0;

  // Busca de funcionário
  termoBuscaFuncionario = '';
  funcionarios: Cliente[] = [];
  funcionarioSelecionado: Cliente | null = null;
  totalPendente = 0;

  // Tipos de vale
  tiposVale = [
    { valor: TipoVale.ADIANTAMENTO, label: TIPO_VALE_LABELS.ADIANTAMENTO },
    { valor: TipoVale.EMPRESTIMO, label: TIPO_VALE_LABELS.EMPRESTIMO },
    { valor: TipoVale.OUTROS, label: TIPO_VALE_LABELS.OUTROS }
  ];

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    
    if (id) {
      this.modoEdicao = true;
      this.valeId = +id;
      this.carregarVale();
    }
  }

  // ⭐ MANIPULAÇÃO DO VALOR (IGUAL À TELA DE PAGAMENTO)
  
  onKeyPress(event: KeyboardEvent): void {
    // Permitir apenas números
    const charCode = event.which ? event.which : event.keyCode;
    if (charCode < 48 || charCode > 57) {
      event.preventDefault();
      return;
    }

    const digito = String.fromCharCode(charCode);
    this.valorCentavos = this.valorCentavos * 10 + parseInt(digito);
    
    // Atualizar valor
    this.form.valor = this.valorCentavos / 100;
    this.valorExibicao = this.formatarValorBR(this.form.valor);
    
    event.preventDefault();
  }

  onFocus(): void {
    // Limpar valor ao focar (opcional)
    if (this.valorCentavos === 0) {
      this.valorExibicao = '';
    }
  }

  formatarValorBR(valor: number): string {
    return valor.toLocaleString('pt-BR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }

  carregarVale(): void {
    this.loading = true;
    
    this.valeService.buscarPorId(this.valeId!).subscribe({
      next: (vale: Vale) => {
        this.form = {
          clienteId: vale.clienteId,
          dataConcessao: vale.dataConcessao,
          dataVencimento: vale.dataVencimento,
          tipoVale: vale.tipoVale,
          valor: vale.valor,
          observacao: vale.observacao || ''
        };

        // ⭐ CONVERTER VALOR PARA CENTAVOS
        this.valorCentavos = Math.round(vale.valor * 100);
        this.valorExibicao = this.formatarValorBR(vale.valor);

        // Carregar funcionário
        this.clienteService.buscarPorId(vale.clienteId).subscribe({
          next: (cliente: Cliente) => {
            this.funcionarioSelecionado = cliente;
            this.termoBuscaFuncionario = cliente.nome;
            this.carregarTotalPendente(cliente.id!);
            this.loading = false;
          },
          error: (err: any) => {
            console.error('Erro ao carregar funcionário:', err);
            this.loading = false;
          }
        });
      },
      error: (err: any) => {
        console.error('Erro ao carregar vale:', err);
        alert('Erro ao carregar vale');
        this.voltar();
      }
    });
  }

  buscarFuncionarios(): void {
    if (this.termoBuscaFuncionario.length < 2) {
      this.funcionarios = [];
      return;
    }

    this.clienteService.buscarFuncionarios(this.termoBuscaFuncionario).subscribe({
      next: (data: Cliente[]) => {
        this.funcionarios = data;
      },
      error: (err: any) => {
        console.error('Erro ao buscar funcionários:', err);
      }
    });
  }

  selecionarFuncionario(funcionario: Cliente): void {
    this.funcionarioSelecionado = funcionario;
    this.termoBuscaFuncionario = funcionario.nome;
    this.funcionarios = [];
    this.form.clienteId = funcionario.id!;

    // Carregar total pendente
    this.carregarTotalPendente(funcionario.id!);
  }

  carregarTotalPendente(clienteId: number): void {
    this.valeService.calcularTotalPendente(clienteId).subscribe({
      next: (data: { totalPendente: number }) => {
        this.totalPendente = data.totalPendente;
      },
      error: (err: any) => {
        console.error('Erro ao carregar total pendente:', err);
      }
    });
  }

  limparSelecao(): void {
    this.funcionarioSelecionado = null;
    this.termoBuscaFuncionario = '';
    this.form.clienteId = 0;
    this.totalPendente = 0;
  }

 formValido(): boolean {
  console.log('🔍 Validando formulário:', {
    clienteId: this.form.clienteId,
    clienteIdValido: this.form.clienteId > 0,
    tipoVale: this.form.tipoVale,
    tipoValeValido: this.form.tipoVale !== null,
    valor: this.form.valor,
    valorValido: this.form.valor > 0,
    valorCentavos: this.valorCentavos,
    dataConcessao: this.form.dataConcessao,
    dataConcessaoValida: this.form.dataConcessao !== '',
    dataVencimento: this.form.dataVencimento,
    dataVencimentoValida: this.form.dataVencimento !== ''
  });

  return this.form.clienteId > 0 &&
         this.form.tipoVale !== null &&
         this.form.valor > 0 &&
         this.form.dataConcessao !== '' &&
         this.form.dataVencimento !== '';
}

  salvar(): void {
    if (!this.formValido()) {
      alert('⚠️ Preencha todos os campos obrigatórios');
      return;
    }

    this.loading = true;

    const operacao = this.modoEdicao
      ? this.valeService.atualizar(this.valeId!, this.form)
      : this.valeService.criar(this.form);

    operacao.subscribe({
      next: () => {
        alert(this.modoEdicao ? '✅ Vale atualizado com sucesso!' : '✅ Vale criado com sucesso!');
        this.voltar();
      },
      error: (err: any) => {
        console.error('Erro ao salvar vale:', err);
        this.loading = false;
        
        if (err.error?.mensagem) {
          alert('❌ ' + err.error.mensagem);
        } else {
          alert('❌ Erro ao salvar vale');
        }
      }
    });
  }

  voltar(): void {
    this.router.navigate(['/vales']);
  }

  obterDataHoje(): string {
    return new Date().toISOString().split('T')[0];
  }

  obterDataDaqui30Dias(): string {
    const data = new Date();
    data.setDate(data.getDate() + 30);
    return data.toISOString().split('T')[0];
  }
}