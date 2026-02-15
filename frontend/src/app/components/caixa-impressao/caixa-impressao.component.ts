import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CaixaConsultaService } from '../../services/caixa-consulta.service';

@Component({
  selector: 'app-caixa-impressao',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container-impressao">
      <!-- LOADING -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Gerando impressão...</p>
      </div>

      <!-- CONTEÚDO PARA IMPRESSÃO -->
      <div *ngIf="!loading && caixa" class="impressao">
        <!-- CABEÇALHO -->
        <div class="cabecalho">
          <h1>🏨 HOTEL DIVAN</h1>
          <h2>Fechamento de Caixa #{{ caixa.id }}</h2>
          <div class="status">
            Status: {{ caixa.status === 'ABERTO' ? '🔓 ABERTO' : '🔒 FECHADO' }}
          </div>
        </div>

        <!-- INFORMAÇÕES -->
        <div class="secao">
          <h3>📋 Informações</h3>
          <table class="tabela-info">
            <tr>
              <td><strong>Recepcionista:</strong></td>
              <td>{{ caixa.usuarioNome }}</td>
            </tr>
            <tr>
              <td><strong>Turno:</strong></td>
              <td>{{ caixa.turno }}</td>
            </tr>
            <tr>
              <td><strong>Abertura:</strong></td>
              <td>{{ formatarDataHora(caixa.dataHoraAbertura) }}</td>
            </tr>
            <tr>
              <td><strong>Fechamento:</strong></td>
              <td>{{ caixa.dataHoraFechamento ? formatarDataHora(caixa.dataHoraFechamento) : '-' }}</td>
            </tr>
          </table>
        </div>

        <!-- RESUMO FINANCEIRO -->
        <div class="secao">
          <h3>💰 Resumo Financeiro</h3>
          <table class="tabela-valores">
            <tr>
              <td>Diárias:</td>
              <td class="valor">R$ {{ caixa.totalDiarias | number:'1.2-2' }}</td>
            </tr>
            <tr>
              <td>Produtos:</td>
              <td class="valor">R$ {{ caixa.totalProdutos | number:'1.2-2' }}</td>
            </tr>
            <tr class="total">
              <td><strong>Total Bruto:</strong></td>
              <td class="valor"><strong>R$ {{ caixa.totalBruto | number:'1.2-2' }}</strong></td>
            </tr>
            <tr class="total-liquido">
              <td><strong>Total Líquido:</strong></td>
              <td class="valor"><strong>R$ {{ caixa.totalLiquido | number:'1.2-2' }}</strong></td>
            </tr>
          </table>
        </div>

        <!-- FORMAS DE PAGAMENTO -->
        <div class="secao">
          <h3>💳 Formas de Pagamento</h3>
          <table class="tabela-valores">
            <tr>
              <td>💵 Dinheiro:</td>
              <td class="valor">R$ {{ caixa.totalDinheiro | number:'1.2-2' }}</td>
            </tr>
            <tr>
              <td>📱 PIX:</td>
              <td class="valor">R$ {{ caixa.totalPix | number:'1.2-2' }}</td>
            </tr>
            <tr>
              <td>💳 Cartão Débito:</td>
              <td class="valor">R$ {{ caixa.totalCartaoDebito | number:'1.2-2' }}</td>
            </tr>
            <tr>
              <td>💳 Cartão Crédito:</td>
              <td class="valor">R$ {{ caixa.totalCartaoCredito | number:'1.2-2' }}</td>
            </tr>
            <tr>
              <td>🏦 Transferência:</td>
              <td class="valor">R$ {{ caixa.totalTransferencia | number:'1.2-2' }}</td>
            </tr>
            <tr>
              <td>📄 Faturado:</td>
              <td class="valor">R$ {{ caixa.totalFaturado | number:'1.2-2' }}</td>
            </tr>
          </table>
        </div>

        <!-- OBSERVAÇÕES -->
        <div *ngIf="caixa.observacoes" class="secao">
          <h3>📝 Observações</h3>
          <p class="observacoes">{{ caixa.observacoes }}</p>
        </div>

        <!-- RODAPÉ -->
        <div class="rodape">
          <p>Impresso em: {{ dataImpressao | date:'dd/MM/yyyy HH:mm' }}</p>
          <div class="assinatura">
            <div class="linha-assinatura"></div>
            <p>Assinatura do Responsável</p>
          </div>
        </div>
      </div>

      <!-- BOTÕES -->
      <div *ngIf="!loading" class="acoes no-print">
        <button class="btn-imprimir" (click)="imprimirPagina()">
          🖨️ Imprimir
        </button>
        <button class="btn-voltar" (click)="voltar()">
          ← Voltar
        </button>
      </div>
    </div>
  `,
  styles: [`
    /* TELA */
    .container-impressao {
      padding: 20px;
      max-width: 900px;
      margin: 0 auto;
    }

    .loading {
      text-align: center;
      padding: 60px;
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

    .impressao {
      background: white;
      padding: 40px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .cabecalho {
      text-align: center;
      border-bottom: 3px solid #2c3e50;
      padding-bottom: 20px;
      margin-bottom: 30px;
    }

    .cabecalho h1 {
      color: #2c3e50;
      margin: 0 0 10px 0;
    }

    .cabecalho h2 {
      color: #667eea;
      margin: 0 0 10px 0;
    }

    .status {
      font-weight: 600;
      font-size: 1.1em;
    }

    .secao {
      margin-bottom: 30px;
    }

    .secao h3 {
      color: #2c3e50;
      border-bottom: 2px solid #e0e0e0;
      padding-bottom: 10px;
      margin-bottom: 15px;
    }

    .tabela-info,
    .tabela-valores {
      width: 100%;
      border-collapse: collapse;
    }

    .tabela-info td {
      padding: 10px;
      border-bottom: 1px solid #e0e0e0;
    }

    .tabela-info td:first-child {
      width: 30%;
    }

    .tabela-valores td {
      padding: 12px;
      border-bottom: 1px solid #e0e0e0;
    }

    .tabela-valores .valor {
      text-align: right;
      font-weight: 600;
      color: #27ae60;
    }

    .tabela-valores .total td {
      border-top: 2px solid #2c3e50;
      padding-top: 15px;
      font-size: 1.1em;
    }

    .tabela-valores .total-liquido td {
      background: #f0f8ff;
      font-size: 1.2em;
      color: #27ae60;
    }

    .observacoes {
      padding: 15px;
      background: #f8f9fa;
      border-radius: 8px;
      border-left: 4px solid #667eea;
      white-space: pre-wrap;
    }

    .rodape {
      margin-top: 40px;
      padding-top: 20px;
      border-top: 2px solid #e0e0e0;
      text-align: center;
    }

    .assinatura {
      margin-top: 60px;
    }

    .linha-assinatura {
      width: 300px;
      height: 1px;
      background: #2c3e50;
      margin: 0 auto 10px;
    }

    .acoes {
      display: flex;
      gap: 15px;
      justify-content: center;
      margin-top: 20px;
    }

    .btn-imprimir,
    .btn-voltar {
      padding: 12px 30px;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn-imprimir {
      background: #27ae60;
      color: white;
    }

    .btn-imprimir:hover {
      background: #229954;
    }

    .btn-voltar {
      background: #95a5a6;
      color: white;
    }

    .btn-voltar:hover {
      background: #7f8c8d;
    }

    /* IMPRESSÃO */
    @media print {
      .no-print {
        display: none !important;
      }

      .container-impressao {
        padding: 0;
        max-width: 100%;
      }

      .impressao {
        box-shadow: none;
        padding: 20px;
      }
    }
  `]
})
export class CaixaImpressaoComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private caixaService = inject(CaixaConsultaService);

  caixa: any = null;
  loading = true;
  dataImpressao = new Date();

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.carregarDados(+id);
    }
  }

  carregarDados(id: number): void {
    this.loading = true;

    this.caixaService.buscarPorId(id).subscribe({
      next: (data) => {
        console.log('✅ Dados carregados para impressão:', data);
        this.caixa = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Erro ao carregar dados:', err);
        alert('Erro ao carregar dados para impressão');
        this.loading = false;
        this.voltar();
      }
    });
  }

  imprimirPagina(): void {
    window.print();
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