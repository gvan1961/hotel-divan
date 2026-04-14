import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SorteioService, Sorteio, BilheteSorteio, VencedorSorteio } from './sorteio.service';

@Component({
  selector: 'app-sorteio-lista',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <div class="header-left">
          <button class="btn-voltar" (click)="voltar()">← Voltar</button>
          <h1>🎟️ Sorteios</h1>
        </div>
        <button class="btn-primary" (click)="abrirModalNovo()">+ Novo Sorteio</button>
      </div>

      <!-- LISTA DE SORTEIOS -->
      <div *ngIf="loading" class="loading">Carregando...</div>

      <div class="sorteios-grid" *ngIf="!loading">
        <div *ngFor="let sorteio of sorteios" class="card-sorteio">
          <div class="card-header">
            <h2>{{ sorteio.nome }}</h2>
            <span [class]="'badge-status badge-' + sorteio.status?.toLowerCase()">
              {{ sorteio.status }}
            </span>
          </div>

          <div class="card-info">
            <div class="info-linha">
              <span>📅 Período:</span>
              <span>{{ formatarData(sorteio.dataInicio) }} — {{ formatarData(sorteio.dataFim) }}</span>
            </div>
            <div class="info-linha" *ngIf="sorteio.dataSorteio">
              <span>🎯 Data do Sorteio:</span>
              <span>{{ formatarData(sorteio.dataSorteio) }}</span>
            </div>
            <div class="info-linha">
              <span>🎟️ Total de Bilhetes:</span>
              <span class="destaque">{{ totalBilhetes[sorteio.id!] || 0 }}</span>
            </div>
          </div>

          <div class="card-acoes">
            <button class="btn-bilhetes" (click)="verBilhetes(sorteio)">
              🎟️ Ver Bilhetes
            </button>
            <button 
              class="btn-encerrar"
              *ngIf="sorteio.status === 'ATIVA'"
              (click)="encerrarSorteio(sorteio)">
              🔒 Encerrar
            </button>
            <button 
              class="btn-sortear"
              *ngIf="sorteio.status === 'ENCERRADA'"
              (click)="realizarSorteio(sorteio)">
              🎰 Realizar Sorteio
            </button>
          </div>
        </div>

        <div *ngIf="sorteios.length === 0" class="empty">
          Nenhum sorteio cadastrado
        </div>
      </div>

      <!-- MODAL NOVO SORTEIO -->
      <div class="modal-overlay" *ngIf="modalNovo" (click)="fecharModalNovo()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>🎟️ Novo Sorteio</h2>
          <div class="form-group">
            <label>Nome *</label>
            <input type="text" [(ngModel)]="novoSorteio.nome" placeholder="Ex: Sorteio Julho 2026" />
          </div>
          <div class="form-group">
            <label>Data Início *</label>
            <input type="date" [(ngModel)]="novoSorteio.dataInicio" />
          </div>
          <div class="form-group">
            <label>Data Fim *</label>
            <input type="date" [(ngModel)]="novoSorteio.dataFim" />
          </div>
          <div class="form-group">
            <label>Data do Sorteio</label>
            <input type="date" [(ngModel)]="novoSorteio.dataSorteio" />
          </div>
          <div class="modal-footer">
            <button class="btn-cancelar" (click)="fecharModalNovo()">Cancelar</button>
            <button class="btn-salvar" (click)="salvarSorteio()">Salvar</button>
          </div>
          <button class="btn-fechar-modal" (click)="fecharModalNovo()">✕</button>
        </div>
      </div>

      <!-- MODAL BILHETES -->
      <div class="modal-overlay" *ngIf="modalBilhetes" (click)="fecharModalBilhetes()">
        <div class="modal-content modal-grande" (click)="$event.stopPropagation()">
          <h2>🎟️ Bilhetes — {{ sorteioSelecionado?.nome }}</h2>
          
          <div class="resumo-bilhetes">
            <span>Total: <strong>{{ bilhetes.length }} bilhetes</strong></span>
          </div>

          <div class="tabela-container">
            <table class="tabela">
              <thead>
                <tr>
                  <th>Bilhete</th>
                  <th>Hóspede</th>
                  <th>CPF</th>
                  <th>Diárias</th>
                  <th>Emissão</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let b of bilhetes">
                  <td class="numero-bilhete">#{{ b.numeroBilhete }}</td>
                  <td>{{ b.hospedagemHospede?.cliente?.nome }}</td>
                  <td>{{ b.hospedagemHospede?.cliente?.cpf }}</td>
                  <td>{{ b.quantidadeDiarias }}</td>
                  <td>{{ formatarDataHora(b.dataEmissao) }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <button class="btn-fechar-modal" (click)="fecharModalBilhetes()">✕</button>
        </div>
      </div>

      <!-- MODAL VENCEDOR -->
      <div class="modal-overlay" *ngIf="modalVencedor" (click)="fecharModalVencedor()">
        <div class="modal-content modal-vencedor" (click)="$event.stopPropagation()">
          <div class="vencedor-header">
            <h2>🎉 SORTEIO REALIZADO!</h2>
          </div>
          <div class="vencedor-content" *ngIf="vencedor">
            <div class="numero-vencedor">#{{ vencedor.numeroBilhete }}</div>
            <div class="nome-vencedor">{{ vencedor.nomeHospede }}</div>
            <div class="info-vencedor">
              <p>📄 CPF: {{ vencedor.cpfHospede }}</p>
              <p>📞 Telefone: {{ vencedor.celularHospede }}</p>
              <p>🛏️ Diárias: {{ vencedor.quantidadeDiarias }}</p>
            </div>
            <button class="btn-imprimir" (click)="imprimirVencedor()">
              🖨️ Imprimir
            </button>
          </div>
          <button class="btn-fechar-modal" (click)="fecharModalVencedor()">✕</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container {
      padding: 20px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    .header-left {
      display: flex;
      align-items: center;
      gap: 15px;
    }

    h1 { margin: 0; color: #333; }

    .btn-voltar {
      background: #6c757d;
      color: white;
      border: none;
      padding: 10px 20px;
      border-radius: 5px;
      cursor: pointer;
      font-weight: 600;
    }

    .btn-primary {
      background: #667eea;
      color: white;
      border: none;
      padding: 10px 20px;
      border-radius: 5px;
      cursor: pointer;
      font-weight: 600;
    }

    .loading, .empty {
      text-align: center;
      padding: 40px;
      color: #666;
    }

    .sorteios-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
      gap: 20px;
    }

    .card-sorteio {
      background: white;
      border-radius: 12px;
      padding: 20px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      padding-bottom: 10px;
      border-bottom: 2px solid #f0f0f0;
    }

    .card-header h2 { margin: 0; font-size: 1.1em; color: #2c3e50; }

    .badge-status {
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 0.8em;
      font-weight: 600;
    }

    .badge-ativa { background: #d4edda; color: #155724; }
    .badge-encerrada { background: #fff3cd; color: #856404; }
    .badge-realizada { background: #cce5ff; color: #004085; }

    .card-info { margin-bottom: 15px; }

    .info-linha {
      display: flex;
      justify-content: space-between;
      padding: 6px 0;
      font-size: 0.9em;
      border-bottom: 1px solid #f5f5f5;
    }

    .destaque { font-weight: 700; color: #667eea; font-size: 1.1em; }

    .card-acoes {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    .btn-bilhetes, .btn-encerrar, .btn-sortear {
      flex: 1;
      padding: 8px;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      font-size: 0.85em;
    }

    .btn-bilhetes { background: #e3f2fd; color: #1565c0; }
    .btn-encerrar { background: #fff3cd; color: #856404; }
    .btn-sortear { background: linear-gradient(135deg, #667eea, #764ba2); color: white; }

    /* MODAL */
    .modal-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.6);
      display: flex;
      justify-content: center;
      align-items: center;
      z-index: 9999;
      padding: 20px;
    }

    .modal-content {
      background: white;
      border-radius: 12px;
      padding: 30px;
      max-width: 500px;
      width: 100%;
      max-height: 90vh;
      overflow-y: auto;
      position: relative;
    }

    .modal-grande { max-width: 900px; }

    .modal-content h2 { margin: 0 0 20px 0; color: #2c3e50; }

    .form-group { margin-bottom: 15px; }

    .form-group label {
      display: block;
      margin-bottom: 5px;
      font-weight: 600;
      color: #555;
    }

    .form-group input {
      width: 100%;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 5px;
      font-size: 14px;
      box-sizing: border-box;
    }

    .modal-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 20px;
    }

    .btn-cancelar, .btn-salvar {
      padding: 10px 20px;
      border: none;
      border-radius: 5px;
      cursor: pointer;
      font-weight: 600;
    }

    .btn-cancelar { background: #6c757d; color: white; }
    .btn-salvar { background: #667eea; color: white; }

    .btn-fechar-modal {
      position: absolute;
      top: 15px;
      right: 15px;
      background: #e74c3c;
      color: white;
      border: none;
      width: 30px;
      height: 30px;
      border-radius: 50%;
      cursor: pointer;
      font-size: 1em;
    }

    .resumo-bilhetes {
      background: #f8f9fa;
      padding: 10px 15px;
      border-radius: 6px;
      margin-bottom: 15px;
    }

    .tabela-container { overflow-x: auto; }

    .tabela {
      width: 100%;
      border-collapse: collapse;
    }

    .tabela th {
      background: #f8f9fa;
      padding: 12px;
      text-align: left;
      font-weight: 600;
      border-bottom: 2px solid #dee2e6;
    }

    .tabela td {
      padding: 10px 12px;
      border-bottom: 1px solid #f0f0f0;
    }

    .numero-bilhete {
      font-weight: 700;
      color: #667eea;
      font-size: 1.1em;
    }

    /* VENCEDOR */
    .modal-vencedor {
      text-align: center;
      max-width: 500px;
    }

    .vencedor-header {
      background: linear-gradient(135deg, #667eea, #764ba2);
      margin: -30px -30px 20px -30px;
      padding: 30px;
      border-radius: 12px 12px 0 0;
    }

    .vencedor-header h2 { color: white; margin: 0; font-size: 1.8em; }

    .numero-vencedor {
      font-size: 4em;
      font-weight: 900;
      color: #667eea;
      margin: 20px 0;
    }

    .nome-vencedor {
      font-size: 1.5em;
      font-weight: 700;
      color: #2c3e50;
      margin-bottom: 15px;
    }

    .info-vencedor p {
      margin: 8px 0;
      color: #555;
      font-size: 1em;
    }

    .btn-imprimir {
      background: #27ae60;
      color: white;
      border: none;
      padding: 12px 30px;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      font-size: 1em;
      margin-top: 20px;
    }
  `]
})
export class SorteioListaApp implements OnInit {
  private sorteioService = inject(SorteioService);
  private router = inject(Router);

  sorteios: Sorteio[] = [];
  loading = true;
  totalBilhetes: { [key: number]: number } = {};

  modalNovo = false;
  modalBilhetes = false;
  modalVencedor = false;

  sorteioSelecionado: Sorteio | null = null;
  bilhetes: BilheteSorteio[] = [];
  vencedor: VencedorSorteio | null = null;

  novoSorteio: Sorteio = {
    nome: '',
    dataInicio: '',
    dataFim: '',
    dataSorteio: ''
  };

  ngOnInit(): void {
    this.carregarSorteios();
  }

  carregarSorteios(): void {
    this.loading = true;
    this.sorteioService.listarTodos().subscribe({
      next: (data) => {
        this.sorteios = data;
        this.loading = false;
        data.forEach(s => {
          if (s.id) {
            this.sorteioService.contarBilhetes(s.id).subscribe({
              next: (res) => this.totalBilhetes[s.id!] = res.total,
              error: () => {}
            });
          }
        });
      },
      error: () => this.loading = false
    });
  }

  abrirModalNovo(): void {
    this.novoSorteio = { nome: '', dataInicio: '', dataFim: '', dataSorteio: '' };
    this.modalNovo = true;
  }

  fecharModalNovo(): void { this.modalNovo = false; }

  salvarSorteio(): void {
    if (!this.novoSorteio.nome || !this.novoSorteio.dataInicio || !this.novoSorteio.dataFim) {
      alert('⚠️ Preencha todos os campos obrigatórios!');
      return;
    }

    this.sorteioService.criar(this.novoSorteio).subscribe({
      next: () => {
        alert('✅ Sorteio criado com sucesso!');
        this.fecharModalNovo();
        this.carregarSorteios();
      },
      error: (err) => alert('❌ Erro: ' + (err.error?.erro || err.message))
    });
  }

  encerrarSorteio(sorteio: Sorteio): void {
    if (!confirm(`🔒 Encerrar o sorteio "${sorteio.nome}"?\n\nNovos bilhetes não serão mais gerados.`)) return;

    this.sorteioService.encerrar(sorteio.id!).subscribe({
      next: () => {
        alert('✅ Sorteio encerrado!');
        this.carregarSorteios();
      },
      error: (err) => alert('❌ Erro: ' + (err.error?.erro || err.message))
    });
  }

  realizarSorteio(sorteio: Sorteio): void {
    if (!confirm(`🎰 Realizar o sorteio "${sorteio.nome}" agora?\n\nEsta ação não pode ser desfeita!`)) return;

    this.sorteioService.realizar(sorteio.id!).subscribe({
      next: (vencedor) => {
        this.vencedor = vencedor;
        this.modalVencedor = true;
        this.carregarSorteios();
      },
      error: (err) => alert('❌ Erro: ' + (err.error?.erro || err.message))
    });
  }

  verBilhetes(sorteio: Sorteio): void {
    this.sorteioSelecionado = sorteio;
    this.sorteioService.listarBilhetes(sorteio.id!).subscribe({
      next: (data) => {
        this.bilhetes = data;
        this.modalBilhetes = true;
      },
      error: (err) => alert('❌ Erro: ' + (err.error?.erro || err.message))
    });
  }

  fecharModalBilhetes(): void { this.modalBilhetes = false; }
  fecharModalVencedor(): void { this.modalVencedor = false; }

  imprimirVencedor(): void {
    if (!this.vencedor) return;

    const html = `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
        <style>
          @page { size: 80mm auto; margin: 0; }
          body { font-family: 'Courier New', monospace; width: 72mm; margin: 0; padding: 2mm; font-size: 9pt; }
          .centro { text-align: center; }
          .separador { text-align: center; margin: 4px 0; }
          .numero { font-size: 28pt; font-weight: 900; text-align: center; margin: 10px 0; }
          .nome { font-size: 12pt; font-weight: 700; text-align: center; margin: 5px 0; }
          .info { font-size: 9pt; margin: 3px 0; }
        </style>
      </head>
      <body>
        <div class="centro"><strong>HOTEL DI VAN</strong></div>
        <div class="centro">CNPJ: 07.757.726/0001-12</div>
        <div class="separador">================================</div>
        <div class="centro"><strong>🎉 VENCEDOR DO SORTEIO</strong></div>
        <div class="separador">================================</div>
        <div class="numero">#${this.vencedor.numeroBilhete}</div>
        <div class="nome">${this.vencedor.nomeHospede}</div>
        <div class="separador">--------------------------------</div>
        <div class="info">CPF: ${this.vencedor.cpfHospede}</div>
        <div class="info">Tel: ${this.vencedor.celularHospede}</div>
        <div class="info">Diárias: ${this.vencedor.quantidadeDiarias}</div>
        <div class="separador">================================</div>
        <div class="centro">Parabéns!</div>
        <script>window.onload = () => { window.print(); window.onafterprint = () => window.close(); }</script>
      </body>
      </html>
    `;

    const w = window.open('', '_blank', 'width=800,height=600');
    if (w) { w.document.write(html); w.document.close(); }
  }

  formatarData(data: string): string {
    if (!data) return '-';
    return new Date(data + 'T00:00:00').toLocaleDateString('pt-BR');
  }

  formatarDataHora(data: string): string {
    if (!data) return '-';
    return new Date(data).toLocaleString('pt-BR');
  }

  voltar(): void {
    this.router.navigate(['/administrativo']);
  }
}