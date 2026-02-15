import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CaixaConsultaService } from '../../services/caixa-consulta.service';

@Component({
  selector: 'app-caixa-detalhes',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <!-- LOADING -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Carregando detalhes do caixa...</p>
      </div>

      <!-- DETALHES DO CAIXA -->
      <div *ngIf="!loading && caixa" class="detalhes-caixa">
        <!-- CABEÇALHO -->
        <div class="header">
          <div>
            <h1>📊 Detalhes do Caixa #{{ caixa.id }}</h1>
            <span [class]="'badge badge-' + caixa.status.toLowerCase()">
              {{ caixa.status === 'ABERTO' ? '🔓 Aberto' : '🔒 Fechado' }}
            </span>
          </div>
          <div class="acoes-header">
            <button class="btn-imprimir" (click)="imprimir()">
              🖨️ Imprimir
            </button>
            <button class="btn-voltar" (click)="voltar()">
              ← Voltar
            </button>
          </div>
        </div>

        <!-- INFORMAÇÕES BÁSICAS -->
        <div class="card">
          <h3>ℹ️ Informações Básicas</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">Recepcionista:</span>
              <span class="valor">{{ caixa.usuarioNome }}</span>
            </div>
            <div class="info-item">
              <span class="label">Turno:</span>
              <span class="valor">{{ caixa.turno }}</span>
            </div>
            <div class="info-item">
              <span class="label">Abertura:</span>
              <span class="valor">{{ formatarDataHora(caixa.dataHoraAbertura) }}</span>
            </div>
            <div class="info-item">
              <span class="label">Fechamento:</span>
              <span class="valor">{{ caixa.dataHoraFechamento ? formatarDataHora(caixa.dataHoraFechamento) : '-' }}</span>
            </div>
          </div>
          <div *ngIf="caixa.observacoes" class="observacoes">
            <strong>📝 Observações:</strong>
            <p>{{ caixa.observacoes }}</p>
          </div>
        </div>

        <!-- RESUMO FINANCEIRO -->
        <div class="resumo-financeiro">
          <div class="resumo-card">
            <div class="resumo-icone">🏨</div>
            <div class="resumo-info">
              <div class="resumo-label">Diárias</div>
              <div class="resumo-valor">R$ {{ caixa.totalDiarias | number:'1.2-2' }}</div>
            </div>
          </div>

          <div class="resumo-card">
            <div class="resumo-icone">🛒</div>
            <div class="resumo-info">
              <div class="resumo-label">Produtos</div>
              <div class="resumo-valor">R$ {{ caixa.totalProdutos | number:'1.2-2' }}</div>
            </div>
          </div>

          <div class="resumo-card">
            <div class="resumo-icone">📊</div>
            <div class="resumo-info">
              <div class="resumo-label">Total Bruto</div>
              <div class="resumo-valor">R$ {{ caixa.totalBruto | number:'1.2-2' }}</div>
            </div>
          </div>

          <div class="resumo-card destaque">
            <div class="resumo-icone">💰</div>
            <div class="resumo-info">
              <div class="resumo-label">Total Líquido</div>
              <div class="resumo-valor">R$ {{ caixa.totalLiquido | number:'1.2-2' }}</div>
            </div>
          </div>
        </div>

        <!-- FORMAS DE PAGAMENTO -->
        <div class="card">
          <h3>💳 Formas de Pagamento</h3>
          <div class="formas-pagamento">
            <div class="forma-item">
              <span class="forma-label">💵 Dinheiro:</span>
              <span class="forma-valor">R$ {{ caixa.totalDinheiro | number:'1.2-2' }}</span>
            </div>
            <div class="forma-item">
              <span class="forma-label">📱 PIX:</span>
              <span class="forma-valor">R$ {{ caixa.totalPix | number:'1.2-2' }}</span>
            </div>
            <div class="forma-item">
              <span class="forma-label">💳 Débito:</span>
              <span class="forma-valor">R$ {{ caixa.totalCartaoDebito | number:'1.2-2' }}</span>
            </div>
            <div class="forma-item">
              <span class="forma-label">💳 Crédito:</span>
              <span class="forma-valor">R$ {{ caixa.totalCartaoCredito | number:'1.2-2' }}</span>
            </div>
            <div class="forma-item">
              <span class="forma-label">🏦 Transferência:</span>
              <span class="forma-valor">R$ {{ caixa.totalTransferencia | number:'1.2-2' }}</span>
            </div>
            <div class="forma-item">
              <span class="forma-label">📄 Faturado:</span>
              <span class="forma-valor">R$ {{ caixa.totalFaturado | number:'1.2-2' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    /* LOADING */
    .loading {
      text-align: center;
      padding: 60px;
      background: white;
      border-radius: 12px;
    }

    .spinner {
      border: 4px solid #f3f3f3;
      border-top: 4px solid #667eea;
      border-radius: 50%;
      width: 50px;
      height: 50px;
      animation: spin 1s linear infinite;
      margin: 0 auto 20px;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    /* HEADER */
    .header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 30px;
    }

    .header h1 {
      color: #2c3e50;
      margin: 0 0 10px 0;
    }

    .badge {
      padding: 5px 15px;
      border-radius: 12px;
      font-size: 0.9em;
      font-weight: 600;
      display: inline-block;
    }

    .badge-aberto {
      background: #fff3cd;
      color: #856404;
    }

    .badge-fechado {
      background: #d4edda;
      color: #155724;
    }

    .acoes-header {
      display: flex;
      gap: 10px;
    }

    .btn-imprimir,
    .btn-voltar {
      padding: 10px 20px;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn-imprimir {
      background: #9b59b6;
      color: white;
    }

    .btn-imprimir:hover {
      background: #8e44ad;
      transform: translateY(-2px);
    }

    .btn-voltar {
      background: #95a5a6;
      color: white;
    }

    .btn-voltar:hover {
      background: #7f8c8d;
    }

    /* CARD */
    .card {
      background: white;
      padding: 25px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      margin-bottom: 20px;
    }

    .card h3 {
      color: #2c3e50;
      margin-bottom: 20px;
    }

    /* INFO GRID */
    .info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 15px;
      margin-bottom: 20px;
    }

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 5px;
    }

    .label {
      font-size: 0.9em;
      color: #7f8c8d;
      font-weight: 600;
    }

    .valor {
      font-size: 1.1em;
      color: #2c3e50;
      font-weight: 600;
    }

    .observacoes {
      padding: 15px;
      background: #f8f9fa;
      border-radius: 8px;
      border-left: 4px solid #667eea;
    }

    .observacoes p {
      margin: 10px 0 0 0;
      color: #2c3e50;
      white-space: pre-wrap;
    }

    /* RESUMO FINANCEIRO */
    .resumo-financeiro {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 20px;
      margin-bottom: 20px;
    }

    .resumo-card {
      background: white;
      padding: 20px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      gap: 15px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      border-left: 4px solid #667eea;
    }

    .resumo-card.destaque {
      border-left-color: #27ae60;
      background: linear-gradient(135deg, #f8fff8 0%, #ffffff 100%);
    }

    .resumo-icone {
      font-size: 2.5em;
    }

    .resumo-label {
      font-size: 0.9em;
      color: #7f8c8d;
      margin-bottom: 5px;
    }

    .resumo-valor {
      font-size: 1.5em;
      font-weight: 700;
      color: #2c3e50;
    }

    .resumo-card.destaque .resumo-valor {
      color: #27ae60;
    }

    /* FORMAS DE PAGAMENTO */
    .formas-pagamento {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 15px;
    }

    .forma-item {
      display: flex;
      justify-content: space-between;
      padding: 15px;
      background: #f8f9fa;
      border-radius: 8px;
      border-left: 3px solid #667eea;
    }

    .forma-label {
      font-weight: 600;
      color: #2c3e50;
    }

    .forma-valor {
      font-weight: 700;
      color: #667eea;
    }

    /* RESPONSIVE */
    @media (max-width: 768px) {
      .header {
        flex-direction: column;
        gap: 15px;
      }

      .acoes-header {
        width: 100%;
      }

      .btn-imprimir,
      .btn-voltar {
        flex: 1;
      }

      .info-grid,
      .resumo-financeiro,
      .formas-pagamento {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class CaixaDetalhesComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private caixaService = inject(CaixaConsultaService);

  caixa: any = null;
  loading = true;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.carregarDetalhes(+id);
    }
  }

  carregarDetalhes(id: number): void {
    this.loading = true;

    this.caixaService.buscarPorId(id).subscribe({
      next: (data) => {
        console.log('✅ Detalhes do caixa carregados:', data);
        this.caixa = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Erro ao carregar detalhes:', err);
        alert('Erro ao carregar detalhes do caixa');
        this.loading = false;
        this.voltar();
      }
    });
  }

  imprimir(): void {
    this.router.navigate(['/caixa/imprimir', this.caixa.id]);
  }

  voltar(): void {
    this.router.navigate(['/caixa/consulta']);
  }

  formatarDataHora(data: string): string {
    if (!data) return '-';
    return new Date(data).toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}