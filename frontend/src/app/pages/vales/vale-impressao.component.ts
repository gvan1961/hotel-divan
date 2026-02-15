import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ValeService } from '../../services/vale.service';
import { Vale, TIPO_VALE_LABELS, STATUS_VALE_LABELS } from '../../models/vale.model';

@Component({
  selector: 'app-vale-impressao',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <!-- BOTÕES (não imprime) -->
      <div class="acoes no-print">
        <button class="btn btn-voltar" (click)="voltar()">← Voltar</button>
        <button class="btn btn-imprimir" (click)="imprimir()">🖨️ Imprimir</button>
      </div>

      <!-- LOADING -->
      <div *ngIf="loading" class="loading no-print">
        <div class="spinner"></div>
        <p>Carregando vale...</p>
      </div>

      <!-- VALE PARA IMPRESSÃO -->
      <div *ngIf="!loading && vale" class="vale-documento">
        <!-- CABEÇALHO -->
        <div class="cabecalho">
          <h1>🏨 HOTEL DI VAN</h1>
          <h2>COMPROVANTE DE VALE</h2>
          <p class="numero-vale">Nº {{ vale.id }}</p>
        </div>

        <!-- DADOS DO VALE -->
        <div class="secao">
          <h3>📋 DADOS DO VALE</h3>
          <div class="linha-info">
            <span class="label">Tipo:</span>
            <span class="valor">{{ obterLabelTipo(vale.tipoVale) }}</span>
          </div>
          <div class="linha-info">
            <span class="label">Valor:</span>
            <span class="valor destaque">R$ {{ vale.valor | number:'1.2-2' }}</span>
          </div>
          <div class="linha-info">
            <span class="label">Data de Concessão:</span>
            <span class="valor">{{ formatarData(vale.dataConcessao) }}</span>
          </div>
          <div class="linha-info">
            <span class="label">Data de Vencimento:</span>
            <span class="valor">{{ formatarData(vale.dataVencimento) }}</span>
          </div>
          <div class="linha-info" *ngIf="vale.observacao">
            <span class="label">Observação:</span>
            <span class="valor">{{ vale.observacao }}</span>
          </div>
        </div>

        <!-- DADOS DO FUNCIONÁRIO -->
        <div class="secao">
          <h3>👤 DADOS DO FUNCIONÁRIO</h3>
          <div class="linha-info">
            <span class="label">Nome:</span>
            <span class="valor">{{ vale.clienteNome }}</span>
          </div>
          <div class="linha-info">
            <span class="label">CPF:</span>
            <span class="valor">{{ vale.clienteCpf }}</span>
          </div>
        </div>

        <!-- ASSINATURA -->
        <div class="secao-assinatura">
          <p class="texto-declaracao">
            Declaro que recebi o valor acima especificado e me comprometo a quitar este vale conforme as condições estabelecidas.
          </p>
          
          <div class="area-assinatura">
            <div class="linha-assinatura"></div>
            <p class="label-assinatura">Assinatura do Funcionário</p>
            <p class="data-assinatura">{{ obterDataAtual() }}</p>
          </div>
        </div>

        <!-- RODAPÉ -->
        <div class="rodape">
          <p>Este documento é um comprovante de vale concedido.</p>
          <p>Hotel Di Van - Documento gerado em {{ obterDataHoraAtual() }}</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }

    .container {
      padding: 20px;
      max-width: 800px;
      margin: 0 auto;
      background: white;
    }

    /* AÇÕES */
    .acoes {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;
      justify-content: center;
    }

    .btn {
      padding: 12px 24px;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn-voltar {
      background: #95a5a6;
      color: white;
    }

    .btn-voltar:hover {
      background: #7f8c8d;
    }

    .btn-imprimir {
      background: #27ae60;
      color: white;
    }

    .btn-imprimir:hover {
      background: #229954;
    }

    /* LOADING */
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

    /* DOCUMENTO DO VALE */
    .vale-documento {
      background: white;
      border: 2px solid #2c3e50;
      padding: 40px;
      min-height: 900px;
    }

    .cabecalho {
      text-align: center;
      margin-bottom: 40px;
      padding-bottom: 20px;
      border-bottom: 3px double #2c3e50;
    }

    .cabecalho h1 {
      font-size: 2em;
      color: #2c3e50;
      margin-bottom: 10px;
    }

    .cabecalho h2 {
      font-size: 1.5em;
      color: #7f8c8d;
      margin-bottom: 15px;
    }

    .numero-vale {
      font-size: 1.2em;
      font-weight: 700;
      color: #e74c3c;
    }

    /* SEÇÕES */
    .secao {
      margin-bottom: 30px;
      padding: 20px;
      background: #f8f9fa;
      border-left: 4px solid #3498db;
      border-radius: 4px;
    }

    .secao h3 {
      font-size: 1.2em;
      color: #2c3e50;
      margin-bottom: 15px;
      padding-bottom: 10px;
      border-bottom: 2px solid #e0e0e0;
    }

    .linha-info {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #e0e0e0;
    }

    .linha-info:last-child {
      border-bottom: none;
    }

    .linha-info .label {
      font-weight: 600;
      color: #7f8c8d;
    }

    .linha-info .valor {
      color: #2c3e50;
      font-weight: 500;
    }

    .linha-info .valor.destaque {
      color: #27ae60;
      font-size: 1.3em;
      font-weight: 700;
    }

    /* SEÇÃO DE ASSINATURA */
    .secao-assinatura {
      margin-top: 50px;
      padding: 30px;
      background: #fff9e6;
      border: 2px dashed #f39c12;
      border-radius: 8px;
    }

    .texto-declaracao {
      text-align: justify;
      line-height: 1.6;
      margin-bottom: 40px;
      color: #2c3e50;
      font-size: 0.95em;
    }

    .area-assinatura {
      text-align: center;
      margin-top: 50px;
    }

    .linha-assinatura {
      width: 400px;
      height: 2px;
      background: #2c3e50;
      margin: 0 auto 10px;
    }

    .label-assinatura {
      font-weight: 600;
      color: #2c3e50;
      margin-bottom: 5px;
    }

    .data-assinatura {
      font-size: 0.9em;
      color: #7f8c8d;
    }

    /* RODAPÉ */
    .rodape {
      margin-top: 60px;
      padding-top: 20px;
      border-top: 2px solid #e0e0e0;
      text-align: center;
      color: #95a5a6;
      font-size: 0.85em;
    }

    .rodape p {
      margin: 5px 0;
    }

    /* IMPRESSÃO */
    @media print {
      body {
        background: white;
      }

      .container {
        padding: 0;
        max-width: 100%;
      }

      .no-print {
        display: none !important;
      }

      .vale-documento {
        border: 2px solid #2c3e50;
        min-height: auto;
      }

      * {
        print-color-adjust: exact !important;
        -webkit-print-color-adjust: exact !important;
      }

      @page {
        margin: 20mm;
        size: A4 portrait;
      }
    }
  `]
})
export class ValeImpressaoComponent implements OnInit {
  private valeService = inject(ValeService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  vale: Vale | null = null;
  loading = false;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.carregarVale(+id);
    }
  }

  carregarVale(id: number): void {
    this.loading = true;
    this.valeService.buscarPorId(id).subscribe({
      next: (data) => {
        this.vale = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar vale:', err);
        alert('Erro ao carregar vale');
        this.voltar();
      }
    });
  }

  obterLabelTipo(tipo: string): string {
    return TIPO_VALE_LABELS[tipo as keyof typeof TIPO_VALE_LABELS] || tipo;
  }

  formatarData(data: any): string {
    if (!data) return '-';
    return new Date(data).toLocaleDateString('pt-BR');
  }

  obterDataAtual(): string {
    return new Date().toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  obterDataHoraAtual(): string {
    return new Date().toLocaleString('pt-BR');
  }

  imprimir(): void {
    window.print();
  }

  voltar(): void {
    this.router.navigate(['/vales']);
  }
}