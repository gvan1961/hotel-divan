
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ValeService } from '../../services/vale.service';
import { ClienteService } from '../../services/cliente.service';
import { Vale, ValeRequest, TipoVale, TIPO_VALE_LABELS } from '../../models/vale.model';
import { Cliente } from '../../models/cliente.model';
import { CurrencyInputDirective } from '../../directives/currency-input.directive';
import { Component, OnInit, inject, ChangeDetectorRef, ViewChild } from '@angular/core';
import { SignaturePadComponent } from '../../components/signature-pad/signature-pad.component';

@Component({
  selector: 'app-vale-form',
  standalone: true,
  imports: [CommonModule, FormsModule, CurrencyInputDirective, SignaturePadComponent],
  template: `
    <div class="container-form">
      <div class="header">
        <h1>{{ modoEdicao ? '✏️ Editar Vale' : '➕ Novo Vale' }}</h1>
        <button class="btn-voltar" (click)="voltar()">← Voltar</button>
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
                (blur)="fecharListaComDelay()"
                placeholder="🔍 Digite o nome ou CPF do funcionário..."
                [disabled]="modoEdicao"
                class="input-busca"
                autocomplete="off">

              <div class="lista-resultados" *ngIf="mostrarLista && funcionarios.length > 0">
                <button
                  type="button"
                  class="item-resultado"
                  *ngFor="let func of funcionarios"
                  (click)="selecionarFuncionario(func)">
                  <strong>{{ func.nome }}</strong>
                  <small>CPF: {{ func.cpf }}</small>
                </button>
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

          <!-- VALOR -->
          <div class="campo">
            <label>Valor *</label>
            <input
              type="text"
              [(ngModel)]="valorExibicao"
              name="valor"
              (input)="onValorInput($event)"
              (focus)="focarValor()"
              placeholder="0,00"
              style="width:100%; padding:10px; border:1px solid #ddd; border-radius:5px; font-size:14px; box-sizing:border-box; background:white;"
            />
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
            <button type="button" class="btn-cancelar" (click)="voltar()">Cancelar</button>
            <button type="button" class="btn-remoto" (click)="solicitarRemoto()" [disabled]="!formValido()">
              📱 Solicitar Remotamente
            </button>
            <button type="button" class="btn-salvar" (click)="abrirAssinatura()" [disabled]="!formValido()">
              ✅ Criar Vale
            </button>
          </div>
        </form>
      </div>

      <!-- MODAL ASSINATURA -->
      <div class="modal-overlay" *ngIf="modalAssinatura">
        <div class="modal-content modal-assinatura" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3>✏️ Assinatura do Funcionário</h3>
            <button class="btn-fechar-modal" (click)="modalAssinatura = false">&times;</button>
          </div>
          <div class="modal-body">
            <p><strong>Funcionário:</strong> {{ funcionarioSelecionado?.nome }}</p>
            <p><strong>Valor:</strong> R$ {{ form.valor | number:'1.2-2' }}</p>
            <app-signature-pad #signaturePad></app-signature-pad>
          </div>
          <div class="modal-footer">
            <button class="btn-cancelar" (click)="modalAssinatura = false">❌ Cancelar</button>
            <button class="btn-confirmar" (click)="confirmarAssinatura()">✅ Confirmar e Salvar</button>
          </div>
        </div>
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
      z-index: 100;
      box-shadow: 0 4px 8px rgba(0,0,0,0.15);
    }

    .item-resultado {
      display: block;
      width: 100%;
      padding: 12px;
      cursor: pointer;
      border: none;
      border-bottom: 1px solid #f0f0f0;
      background: white;
      text-align: left;
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
    .btn-salvar,
    .btn-remoto {
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

    .btn-remoto {
      background: #f39c12;
      color: white;
    }

    .btn-remoto:hover:not(:disabled) {
      background: #e67e22;
    }

    .btn-remoto:disabled {
      opacity: 0.5;
      cursor: not-allowed;
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

    /* MODAL */
    .modal-overlay {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.6);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }

    .modal-content {
      background: white;
      border-radius: 12px;
      padding: 0;
      max-width: 600px;
      width: 90%;
      box-shadow: 0 10px 30px rgba(0,0,0,0.3);
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 25px;
      border-bottom: 1px solid #eee;
    }

    .modal-header h3 {
      margin: 0;
      color: #2c3e50;
    }

    .btn-fechar-modal {
      background: none;
      border: none;
      font-size: 1.5em;
      cursor: pointer;
      color: #7f8c8d;
    }

    .modal-body {
      padding: 25px;
    }

    .modal-footer {
      display: flex;
      gap: 10px;
      justify-content: flex-end;
      padding: 15px 25px;
      border-top: 1px solid #eee;
    }

    .btn-confirmar {
      padding: 10px 25px;
      background: #27ae60;
      color: white;
      border: none;
      border-radius: 6px;
      font-size: 1em;
      font-weight: 600;
      cursor: pointer;
    }

    .btn-confirmar:hover {
      background: #219a52;
    }

    /* LOADING */
    .loading-overlay {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
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
      .btn-salvar,
      .btn-remoto {
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
  private cdr = inject(ChangeDetectorRef);

  modoEdicao = false;
  valeId?: number;
  loading = false;

  form: ValeRequest = {
    clienteId: 0,
    dataConcessao: this.obterDataHoje(),
    dataVencimento: this.obterDataDaqui30Dias(),
    tipoVale: null as any,
    valor: 0,
    observacao: ''
  };

  valorExibicao = '';

  termoBuscaFuncionario = '';
  funcionarios: Cliente[] = [];
  funcionarioSelecionado: Cliente | null = null;
  totalPendente = 0;
  mostrarLista = false;

  modalAssinatura = false;
  assinaturaBase64: string | null = null;
  @ViewChild('signaturePad') signaturePad!: SignaturePadComponent;

  tiposVale = [
    { valor: TipoVale.ADIANTAMENTO,    label: TIPO_VALE_LABELS.ADIANTAMENTO },
    { valor: TipoVale.EMPRESTIMO,      label: TIPO_VALE_LABELS.EMPRESTIMO },
    { valor: TipoVale.DESCONTO_FOLHA,  label: TIPO_VALE_LABELS.DESCONTO_FOLHA },
    { valor: TipoVale.OUTROS,          label: TIPO_VALE_LABELS.OUTROS }
  ];

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.modoEdicao = true;
      this.valeId = +id;
      this.carregarVale();
    }
  }

  // ── BUSCA DE FUNCIONÁRIO ─────────────────────────────────────────────────

  buscarFuncionarios(): void {
    // Se já selecionou e está editando o nome, limpa a seleção
    if (this.funcionarioSelecionado && this.termoBuscaFuncionario !== this.funcionarioSelecionado.nome) {
      this.funcionarioSelecionado = null;
      this.form.clienteId = 0;
      this.totalPendente = 0;
    }

    if (this.termoBuscaFuncionario.length < 2) {
      this.funcionarios = [];
      this.mostrarLista = false;
      return;
    }

    this.clienteService.buscarFuncionarios(this.termoBuscaFuncionario).subscribe({
      next: (data: Cliente[]) => {
        this.funcionarios = data;
        this.mostrarLista = data.length > 0;
      },
      error: (err: any) => {
        console.error('Erro ao buscar funcionários:', err);
      }
    });
  }

  selecionarFuncionario(funcionario: any): void {
  const id = funcionario.id ?? funcionario.clienteId ?? funcionario.idCliente ?? 0;

  if (!id || id <= 0) {
    alert('⚠️ Funcionário sem ID válido. Contate o suporte.');
    console.error('Objeto sem ID:', funcionario);
    return;
  }

  this.mostrarLista = false;
  this.funcionarios = [];
  this.funcionarioSelecionado = funcionario;
  this.termoBuscaFuncionario = funcionario.nome;
  this.form.clienteId = id;
  this.cdr.detectChanges();

  this.carregarTotalPendente(id);
}

fecharListaComDelay(): void {
  setTimeout(() => {
    this.mostrarLista = false;
  }, 300);
}

  limparSelecao(): void {
    this.funcionarioSelecionado = null;
    this.termoBuscaFuncionario = '';
    this.funcionarios = [];
    this.mostrarLista = false;
    this.form.clienteId = 0;
    this.totalPendente = 0;
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

  // ── VALOR ────────────────────────────────────────────────────────────────

  onValorInput(event: any): void {
    let numeros = event.target.value.replace(/\D/g, '');
    if (!numeros) numeros = '0';
    const centavos = parseInt(numeros, 10);
    const reais = centavos / 100;
    this.form.valor = reais;
    this.valorExibicao = reais.toLocaleString('pt-BR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }

  focarValor(): void {
    // Não zera o valor — apenas limpa a exibição se for zero
    if (!this.form.valor || this.form.valor === 0) {
      this.valorExibicao = '';
    }
  }

  formatarValorBR(valor: number): string {
    return valor.toLocaleString('pt-BR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }

  // ── VALIDAÇÃO ────────────────────────────────────────────────────────────

  formValido(): boolean {
    return this.form.clienteId > 0 &&
           this.form.tipoVale !== null &&
           this.form.valor > 0 &&
           this.form.dataConcessao !== '' &&
           this.form.dataVencimento !== '';
  }

  // ── CARREGAR (MODO EDIÇÃO) ───────────────────────────────────────────────

  carregarVale(): void {
    this.loading = true;

    this.valeService.buscarPorId(this.valeId!).subscribe({
      next: (vale: Vale) => {
        const clienteId = vale.clienteId || (vale as any).cliente?.id;

        this.form = {
          clienteId: clienteId,
          dataConcessao: vale.dataConcessao,
          dataVencimento: vale.dataVencimento,
          tipoVale: vale.tipoVale,
          valor: vale.valor,
          observacao: vale.observacao || ''
        };

        this.valorExibicao = this.formatarValorBR(vale.valor);

        if (clienteId) {
          this.clienteService.buscarPorId(clienteId).subscribe({
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
        } else {
          this.loading = false;
        }
      },
      error: (err: any) => {
        console.error('Erro ao carregar vale:', err);
        alert('Erro ao carregar vale');
        this.voltar();
      }
    });
  }

  // ── SALVAR ───────────────────────────────────────────────────────────────

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

  abrirAssinatura(): void {
    if (!this.formValido()) return;
    this.modalAssinatura = true;
  }

  confirmarAssinatura(): void {
    const assinatura = this.signaturePad?.obterAssinatura();
    if (!assinatura) {
      alert('⚠️ Por favor, assine antes de confirmar.');
      return;
    }
    this.form.assinaturaBase64 = assinatura;
    this.modalAssinatura = false;
    this.salvar();
  }

  solicitarRemoto(): void {
    if (!this.formValido()) return;
    const agora = new Date().toLocaleString('pt-BR');
    this.form.observacao = (this.form.observacao ? this.form.observacao + '\n' : '') +
      `Solicitado remotamente em ${agora}`;
    this.salvar();
  }

  // ── UTILITÁRIOS ──────────────────────────────────────────────────────────

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
