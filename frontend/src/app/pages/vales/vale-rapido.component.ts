import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ValeService } from '../../services/vale.service';
import { ClienteService } from '../../services/cliente.service';
import { Vale, ValeRequest, TipoVale } from '../../models/vale.model';
import { Cliente } from '../../models/cliente.model';

interface FuncionarioOpcao {
  id: number;
  nome: string;
  cpf?: string;
}

@Component({
  selector: 'app-vale-rapido',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="overlay">
      <div class="modal-rapido">
        <div class="header">
          <h2>⚡ Vale Rápido</h2>
          <button class="btn-fechar" (click)="cancelar()">✕</button>
        </div>

        <div class="body">
          <!-- FUNCIONÁRIO -->
          <div class="campo">
            <label>Funcionário *</label>
            <input
              type="text"
              placeholder="Digite o nome do funcionário..."
              [(ngModel)]="termoBuscaFuncionario"
              (input)="filtrarFuncionarios()"
              (focus)="mostrarLista = true"
              autocomplete="off"
            />
            <div class="lista-sugestoes" *ngIf="mostrarLista && funcionariosFiltrados.length > 0">
              <div
                class="sugestao"
                *ngFor="let f of funcionariosFiltrados"
                (click)="selecionarFuncionario(f)"
              >
                <strong>{{ f.nome }}</strong>
                <small *ngIf="f.cpf">{{ f.cpf }}</small>
              </div>
            </div>
            <div class="funcionario-selecionado" *ngIf="funcionarioSelecionado">
              ✅ {{ funcionarioSelecionado.nome }}
            </div>
          </div>

          <!-- VALOR -->
          <div class="campo">
            <label>Valor *</label>
            <div class="input-valor-wrapper">
              <span class="prefixo-moeda">R$</span>
              <input
                type="text"
                inputmode="numeric"
                [value]="valorDisplay"
                (input)="onValorInput($event)"
              />
            </div>
          </div>

          <!-- DATA DE CONCESSÃO (editável, default hoje) -->
          <div class="campo">
            <label>Data de Concessão *</label>
            <input type="date" [(ngModel)]="dataConcessao" (change)="recalcularVencimento()" />
          </div>

          <!-- VENCIMENTO (calculado automaticamente, somente leitura) -->
          <div class="campo">
            <label>Vencimento (5º dia útil do próximo mês)</label>
            <input type="date" [ngModel]="dataVencimento" disabled />
          </div>

          <!-- OBSERVAÇÃO opcional -->
          <div class="campo">
            <label>Observação (opcional)</label>
            <textarea
              [(ngModel)]="observacao"
              rows="2"
              placeholder="Ex: adiantamento solicitado na recepção"
            ></textarea>
          </div>

          <p class="aviso" *ngIf="erro">⚠️ {{ erro }}</p>
        </div>

        <div class="footer">
          <button class="btn-cancelar" (click)="cancelar()">Cancelar</button>
          <button class="btn-confirmar" [disabled]="salvando" (click)="criarESeguirParaAssinatura()">
            {{ salvando ? 'Salvando...' : '✍️ Criar e Assinar' }}
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .overlay {
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.6);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 2000;
    }
    .modal-rapido {
      background: white;
      border-radius: 12px;
      width: 90%;
      max-width: 420px;
      max-height: 90vh;
      overflow-y: auto;
    }
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 18px 20px;
      border-bottom: 2px solid #eee;
    }
    .header h2 { margin: 0; color: #2c3e50; }
    .btn-fechar {
      background: #e74c3c;
      color: white;
      border: none;
      width: 30px;
      height: 30px;
      border-radius: 50%;
      cursor: pointer;
    }
    .body { padding: 18px 20px; display: flex; flex-direction: column; gap: 14px; }
    .campo { position: relative; display: flex; flex-direction: column; gap: 6px; }
    .campo label { font-weight: 600; color: #2c3e50; font-size: 0.9em; }
    .campo input, .campo textarea {
      padding: 10px;
      border: 2px solid #e0e0e0;
      border-radius: 6px;
      font-size: 1em;
    }
    .input-valor-wrapper {
      display: flex;
      align-items: stretch;
      border: 2px solid #e0e0e0;
      border-radius: 6px;
      overflow: hidden;
      transition: border-color 0.2s;
    }
    .input-valor-wrapper:focus-within {
      border-color: #667eea;
    }
    .prefixo-moeda {
      display: flex;
      align-items: center;
      padding: 0 10px;
      background: #f4f4f4;
      color: #555;
      font-weight: 700;
      border-right: 2px solid #e0e0e0;
    }
    .input-valor-wrapper input {
      border: none !important;
      border-radius: 0 !important;
      flex: 1;
    }
    .input-valor-wrapper input:focus {
      outline: none;
    }
    .campo input:focus, .campo textarea:focus {
      outline: none;
      border-color: #667eea;
    }
    .campo input:disabled {
      background: #f4f4f4;
      color: #555;
    }
    .lista-sugestoes {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      background: white;
      border: 1px solid #ddd;
      border-radius: 6px;
      max-height: 180px;
      overflow-y: auto;
      z-index: 10;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }
    .sugestao {
      padding: 10px;
      cursor: pointer;
      display: flex;
      flex-direction: column;
    }
    .sugestao:hover { background: #f0f2ff; }
    .sugestao small { color: #888; }
    .funcionario-selecionado {
      margin-top: 4px;
      color: #27ae60;
      font-weight: 600;
      font-size: 0.9em;
    }
    .aviso { color: #e74c3c; font-weight: 600; margin: 0; }
    .footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      padding: 16px 20px;
      border-top: 2px solid #eee;
    }
    .btn-cancelar {
      padding: 10px 18px;
      background: #f0f0f0;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
    }
    .btn-confirmar {
      padding: 10px 18px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
    }
    .btn-confirmar:disabled { opacity: 0.6; cursor: not-allowed; }
  `]
})
export class ValeRapidoComponent implements OnInit {
  private valeService = inject(ValeService);
  private clienteService = inject(ClienteService);
  private router = inject(Router);

  funcionarios: FuncionarioOpcao[] = [];
  funcionariosFiltrados: FuncionarioOpcao[] = [];
  funcionarioSelecionado: FuncionarioOpcao | null = null;
  termoBuscaFuncionario = '';
  mostrarLista = false;

  valor: number | null = null;
  valorDisplay = '0,00'; // texto mostrado no campo, formatado tipo caixa eletrônico
  dataConcessao: string = this.formatarParaInput(new Date());
  dataVencimento: string = '';
  observacao = '';

  salvando = false;
  erro = '';

  ngOnInit(): void {
    this.clienteService.listarFuncionarios().subscribe({
      next: (clientes: Cliente[]) => {
        this.funcionarios = clientes.map(c => ({
          id: c.id!,
          nome: c.nome,
          cpf: c.cpf
        }));
      },
      error: (err: any) => console.error('Erro ao carregar funcionários:', err)
    });

    this.recalcularVencimento();
  }

  /**
   * Máscara de valor monetário estilo "caixa eletrônico": os dígitos digitados
   * entram sempre pela direita, como centavos. Ex: digitar 2, 5, 0, 0 mostra,
   * em sequência: 0,02 → 0,25 → 2,50 → 25,00.
   */
  onValorInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const somenteDigitos = input.value.replace(/\D/g, '');
    const centavos = somenteDigitos === '' ? 0 : parseInt(somenteDigitos, 10);

    this.valor = centavos / 100;
    this.valorDisplay = this.valor.toLocaleString('pt-BR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });

    // Reposiciona o valor exibido no próprio input (o [value] do template
    // não força atualização sozinho em todos os navegadores)
    input.value = this.valorDisplay;
  }

  filtrarFuncionarios(): void {
    const termo = this.termoBuscaFuncionario.toLowerCase().trim();
    this.funcionarioSelecionado = null;
    if (!termo) {
      this.funcionariosFiltrados = [];
      return;
    }
    this.funcionariosFiltrados = this.funcionarios
      .filter(f => f.nome.toLowerCase().includes(termo) || f.cpf?.includes(termo))
      .slice(0, 8);
  }

  selecionarFuncionario(f: FuncionarioOpcao): void {
    this.funcionarioSelecionado = f;
    this.termoBuscaFuncionario = f.nome;
    this.mostrarLista = false;
    this.funcionariosFiltrados = [];
  }

  /**
   * Calcula o 5º dia útil do mês SEGUINTE ao mês da data de concessão.
   * Considera sábado e domingo como não úteis.
   * Se sua empresa tiver calendário de feriados, dá pra injetar aqui.
   */
  recalcularVencimento(): void {
    const base = this.dataConcessao ? new Date(this.dataConcessao + 'T00:00:00') : new Date();

    let mes = base.getMonth() + 1; // mês seguinte (0-indexed +1 já avança 1 mês)
    let ano = base.getFullYear();
    if (mes > 11) {
      mes = 0;
      ano++;
    }

    const quintoUtil = this.quintoDiaUtil(ano, mes);
    this.dataVencimento = this.formatarParaInput(quintoUtil);
  }

  private quintoDiaUtil(ano: number, mesIndexZero: number): Date {
    const data = new Date(ano, mesIndexZero, 1);
    let diasUteis = 0;
    while (true) {
      const diaSemana = data.getDay(); // 0 = domingo, 6 = sábado
      if (diaSemana !== 0 && diaSemana !== 6) {
        diasUteis++;
        if (diasUteis === 5) break;
      }
      data.setDate(data.getDate() + 1);
    }
    return data;
  }

  private formatarParaInput(data: Date): string {
    const ano = data.getFullYear();
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const dia = String(data.getDate()).padStart(2, '0');
    return `${ano}-${mes}-${dia}`;
  }

  criarESeguirParaAssinatura(): void {
    this.erro = '';

    if (!this.funcionarioSelecionado) {
      this.erro = 'Selecione o funcionário.';
      return;
    }
    if (!this.valor || this.valor <= 0) {
      this.erro = 'Informe um valor válido.';
      return;
    }

    this.salvando = true;

    // Vale rápido = adiantamento solicitado na recepção fora do horário/disponibilidade do ADM
    const body: ValeRequest = {
      clienteId: this.funcionarioSelecionado.id,
      valor: this.valor,
      tipoVale: TipoVale.ADIANTAMENTO,
      dataConcessao: this.dataConcessao,
      dataVencimento: this.dataVencimento,
      observacao: this.observacao || undefined
    };

    this.valeService.criar(body).subscribe({
      next: (valeCriado: Vale) => {
        this.salvando = false;
        // Vai direto pra tela de assinatura na mesa digitalizadora
        this.router.navigate(['/vales/assinar', valeCriado.id]);
      },
      error: (err: any) => {
        this.salvando = false;
        this.erro = 'Erro ao criar vale. Tente novamente.';
        console.error(err);
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['..']); // ajuste conforme a rota "pai" desejada
  }
}