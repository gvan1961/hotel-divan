import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface HospedeResumo {
  id: number;
  nomeCompleto: string;
  cpf?: string;
  telefone?: string;
  titular: boolean;
  empresaNome?: string;
}

interface ReservaOcupacao {
  id: number;
  apartamentoNumero: string;
  clienteNome: string;
  dataCheckin: string;
  dataCheckout: string;
  quantidadeHospede: number;
  hospedes: HospedeResumo[];
}

@Component({
  selector: 'app-relatorio-ocupacao',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container-relatorio">
      <!-- CABEÇALHO PARA TELA -->
      <div class="header-tela">
        <h1>🏨 Relatório de Apartamentos Ocupados</h1>
        <div class="acoes">
          <button class="btn-voltar" (click)="voltar()">← Voltar</button>
          <button class="btn-imprimir" (click)="imprimir()">🖨️ Imprimir</button>
        </div>
      </div>

      <!-- LOADING -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Carregando relatório...</p>
      </div>

      <!-- RELATÓRIO -->
      <div *ngIf="!loading" class="relatorio-conteudo">
        <!-- CABEÇALHO PARA IMPRESSÃO -->
        <div class="header-impressao">
          <h1>HOTEL DI VAN</h1>
          <h2>Relatório de Apartamentos Ocupados</h2>
          <p class="data-geracao">Gerado em: {{ dataGeracao }}</p>
          <div class="totais-resumo">
            <span>Total de Apartamentos: {{ reservas.length }}</span>
            <span>Total de Hóspedes: {{ totalHospedes }}</span>
          </div>
        </div>

        <!-- LISTA DE APARTAMENTOS -->
        <div class="lista-apartamentos">
          <div class="apartamento-item" *ngFor="let reserva of reservas">
            <!-- CABEÇALHO DO APARTAMENTO -->
            <div class="apt-header">
  <div class="apt-numero">
    <span class="numero">{{ reserva.apartamentoNumero }}</span>
  </div>
  <div class="apt-info">
    <div class="info-linha">
      <strong>Check-in:</strong> {{ formatarData(reserva.dataCheckin) }}
      <strong>Check-out:</strong> {{ formatarData(reserva.dataCheckout) }}
    </div>
    <div class="info-linha">
      <strong>Total de Hóspedes:</strong> {{ reserva.quantidadeHospede }}
    </div>
  </div>
</div>

            <!-- LISTA DE HÓSPEDES -->
            <div class="lista-hospedes">
              <h4>👥 Hóspedes:</h4>
              <div class="hospede-row" *ngFor="let hospede of reserva.hospedes; let i = index">
  <span class="hospede-numero">{{ i + 1 }}</span>
  <span class="hospede-nome">
    {{ hospede.nomeCompleto }}
    <span class="badge-titular" *ngIf="hospede.titular">★ TITULAR</span>
  </span>
  <span class="hospede-telefone" *ngIf="hospede.telefone">
    {{ hospede.telefone }}
  </span>
  <span class="hospede-empresa" *ngIf="hospede.empresaNome">
    🏢 {{ hospede.empresaNome }}
  </span>
</div>
            </div>
          </div>
        </div>


        <!-- TOTAIS FINAIS (SÓ NA IMPRESSÃO) -->
        <div class="totais-finais-impressao">
          <div class="total-linha">
            <span>Total de Apartamentos:</span>
            <strong>{{ reservas.length }}</strong>
          </div>
          <div class="total-linha">
            <span>Total de Hóspedes:</span>
            <strong>{{ totalHospedes }}</strong>
          </div>
        </div>

        <!-- MENSAGEM VAZIO -->
        <div *ngIf="reservas.length === 0" class="vazio">
          ℹ️ Nenhum apartamento ocupado no momento
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container-relatorio {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
      background: #f5f7fa;
      min-height: 100vh;
    }

    .header-tela {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
      background: white;
      padding: 20px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .header-tela h1 {
      margin: 0;
      color: #2c3e50;
    }

    .acoes {
      display: flex;
      gap: 10px;
    }

    .btn-voltar, .btn-imprimir {
      padding: 10px 20px;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
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
      background: #3498db;
      color: white;
    }

    .btn-imprimir:hover {
      background: #2980b9;
    }

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

    .relatorio-conteudo {
      background: white;
      border-radius: 12px;
      padding: 30px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    /* CABEÇALHO IMPRESSÃO */
    .header-impressao {
      display: none;
      text-align: center;
      margin-bottom: 30px;
      padding-bottom: 20px;
      border-bottom: 3px solid #2c3e50;
    }

    .header-impressao h1 {
      margin: 0 0 10px 0;
      color: #2c3e50;
      font-size: 2em;
    }

    .header-impressao h2 {
      margin: 0 0 10px 0;
      color: #7f8c8d;
      font-size: 1.3em;
    }

    .data-geracao {
      margin: 10px 0;
      color: #95a5a6;
      font-size: 0.9em;
    }

    .totais-resumo {
      display: flex;
      justify-content: center;
      gap: 40px;
      margin-top: 15px;
      font-weight: 600;
      color: #2c3e50;
    }

    /* LISTA DE APARTAMENTOS */
    .lista-apartamentos {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .apartamento-item {
      border: 2px solid #e0e0e0;
      border-radius: 8px;
      padding: 20px;
      page-break-inside: avoid;
    }

    .apt-header {
      display: flex;
      gap: 20px;
      margin-bottom: 15px;
      padding-bottom: 15px;
      border-bottom: 2px solid #e0e0e0;
    }

    .apt-numero {
      display: flex;
      align-items: center;
      justify-content: center;
      min-width: 80px;
      height: 80px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 8px;
      box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
    }

    .apt-numero .numero {
      font-size: 2em;
      font-weight: 700;
      color: white;
    }

    .apt-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .info-linha {
      display: flex;
      gap: 15px;
      font-size: 0.95em;
      color: #2c3e50;
    }

    .info-linha strong {
      color: #7f8c8d;
      min-width: 100px;
    }

    /* LISTA DE HÓSPEDES */
    .lista-hospedes {
      background: #f8f9fa;
      padding: 15px;
      border-radius: 6px;
    }

    .lista-hospedes h4 {
      margin: 0 0 15px 0;
      color: #2c3e50;
    }

    .hospede-row {
      display: flex;
      align-items: center;
      gap: 15px;
      padding: 10px;
      background: white;
      border-radius: 4px;
      margin-bottom: 8px;
      border-left: 3px solid #667eea;
    }

    .hospede-row:last-child {
      margin-bottom: 0;
    }

    .hospede-numero {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 30px;
      height: 30px;
      background: #667eea;
      color: white;
      border-radius: 50%;
      font-weight: 700;
      font-size: 0.9em;
    }

    .hospede-nome {
      flex: 1;
      font-weight: 600;
      color: #2c3e50;
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .badge-titular {
      background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
      color: white;
      padding: 3px 10px;
      border-radius: 12px;
      font-size: 0.75em;
      font-weight: 700;
    }

    .hospede-cpf {
      color: #7f8c8d;
      font-size: 0.9em;
    }

    .hospede-empresa {
      background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
      color: #1565c0;
      padding: 4px 10px;
      border-radius: 6px;
      font-size: 0.85em;
      font-weight: 600;
    }

    .vazio {
      text-align: center;
      padding: 60px;
      color: #7f8c8d;
      font-size: 1.2em;
    }

    /* TOTAIS FINAIS - SÓ APARECEM NA IMPRESSÃO */
     .totais-finais-impressao {
      display: none;
      }

      /* TELEFONE DO HÓSPEDE */
.hospede-telefone {
  color: #7f8c8d;
  font-size: 0.85em;
  white-space: nowrap;
}

    /* ESTILOS DE IMPRESSÃO */
    @media print {
    @page {
    size: A4;
    margin: 10mm;
  }

  body {
    font-size: 8pt;
    line-height: 1.1;
  }

  .container-relatorio {
    background: white;
    padding: 0;
  }

  /* OCULTAR ELEMENTOS DA TELA */
  .header-tela {
    display: none !important;
  }

  /* CABEÇALHO DE IMPRESSÃO COMPACTO */
  .header-impressao {
    display: block !important;
    margin-bottom: 8px;
    padding-bottom: 6px;
  }

  .header-impressao h1 {
    font-size: 14pt;
    margin: 0 0 2px 0;
  }

  .header-impressao h2 {
    font-size: 10pt;
    margin: 0 0 3px 0;
  }

  .data-geracao {
    font-size: 7pt;
    margin: 0;
  }

  .totais-resumo {
    font-size: 8pt;
    gap: 12px;
    margin-top: 5px;
  }

  .relatorio-conteudo {
    box-shadow: none;
    padding: 0;
  }

  /* APARTAMENTOS ULTRA COMPACTOS */
  .lista-apartamentos {
    gap: 4px;
  }

  .apartamento-item {
    page-break-inside: avoid;
    margin-bottom: 4px;
    padding: 4px;
    border: 1px solid #333;
  }

  /* CABEÇALHO APARTAMENTO REDUZIDO */
  .apt-header {
    gap: 5px;
    margin-bottom: 3px;
    padding-bottom: 3px;
    border-bottom: 1px solid #666;
  }

  .apt-numero {
    min-width: 32px !important;
    max-width: 32px !important;
    height: 32px !important;
    border-radius: 4px;
  }

  .apt-numero .numero {
    font-size: 12pt !important;
  }

  .apt-info {
    gap: 1px;
    flex: 1;
  }

  .info-linha {
    font-size: 7pt;
    gap: 5px;
    padding: 1px 0;
  }

  .info-linha strong {
    min-width: 45px;
    font-size: 7pt;
  }

  /* HÓSPEDES ULTRA COMPACTOS */
  .lista-hospedes {
    padding: 3px 5px;
    background: white !important;
    margin: 0;
  }

  .lista-hospedes h4 {
    font-size: 8pt;
    margin: 0 0 3px 0;
  }

  .hospede-row {
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 2px 3px;
    margin-bottom: 1px;
    background: white !important;
    border-left: 2px solid #666;
    font-size: 7pt;
  }

  .hospede-numero {
    width: 14px !important;
    height: 14px !important;
    min-width: 14px;
    font-size: 6pt !important;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .hospede-nome {
    flex: 1;
    font-size: 7pt;
    gap: 3px;
    font-weight: 600;
  }

  .badge-titular {
    padding: 1px 4px;
    font-size: 5pt;
    border-radius: 3px;
  }

  .hospede-telefone {
    font-size: 6pt;
    color: #666;
    white-space: nowrap;
  }

  .hospede-cpf {
    font-size: 6pt;
    color: #666;
  }

  .hospede-empresa {
    padding: 1px 4px;
    font-size: 6pt;
    border-radius: 3px;
  }

  /* GARANTIR CORES */
  .apt-numero,
  .hospede-numero,
  .badge-titular,
  .hospede-empresa {
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }

  /* TOTAIS FINAIS */
  .totais-finais-impressao {
    display: block !important;
    margin-top: 10px;
    padding: 8px 12px;
    border: 2px solid #000;
    page-break-inside: avoid;
  }

  .total-linha {
    display: flex;
    justify-content: space-between;
    padding: 4px 0;
    font-size: 9pt;
    border-bottom: 1px solid #ccc;
  }

  .total-linha:last-child {
    border-bottom: none;
  }

  .total-linha strong {
    font-size: 11pt;
    font-weight: 700;
  }
    }

    @media (max-width: 768px) {
      .apt-header {
        flex-direction: column;
      }

      .apt-numero {
        width: 100%;
      }

      .hospede-row {
        flex-wrap: wrap;
      }

      .totais-resumo {
        flex-direction: column;
        gap: 10px;
      }
    }
  `]
})
export class RelatorioOcupacaoComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);

  loading = false;
  reservas: ReservaOcupacao[] = [];
  dataGeracao = '';
  totalHospedes = 0;

  ngOnInit(): void {
    this.dataGeracao = new Date().toLocaleString('pt-BR');
    this.carregarReservas();
  }

  carregarReservas(): void {
    this.loading = true;

    this.http.get<any[]>('http://localhost:8080/api/reservas').subscribe({
      next: (reservas) => {
        // Filtrar apenas reservas ATIVAS
        const reservasAtivas = reservas.filter(r => r.status === 'ATIVA');

        // Para cada reserva, buscar os hóspedes
        const requests = reservasAtivas.map(reserva => 
          this.http.get<any[]>(`http://localhost:8080/api/hospedagem-hospedes/reserva/${reserva.id}`)
            .toPromise()
            .then(hospedes => ({
              id: reserva.id,
              apartamentoNumero: reserva.apartamento.numeroApartamento,
              clienteNome: reserva.cliente.nome,
              dataCheckin: reserva.dataCheckin,
              dataCheckout: reserva.dataCheckout,
              quantidadeHospede: reserva.quantidadeHospede,
              hospedes: (hospedes || [])
                .filter(h => h.status === 'HOSPEDADO')
                .map(h => ({
                  id: h.id,
                  nomeCompleto: h.cliente?.nome || h.nomeCompleto,
                  cpf: h.cliente?.cpf,
                  telefone: h.cliente?.celular || h.cliente?.telefone,
                  titular: h.titular,
                  empresaNome: h.cliente?.empresa?.nomeEmpresa
                }))
            }))
        );

        Promise.all(requests).then(resultado => {
          // Ordenar por número de apartamento
          this.reservas = resultado.sort((a, b) => {
            const numA = parseInt(a.apartamentoNumero) || 0;
            const numB = parseInt(b.apartamentoNumero) || 0;
            return numA - numB;
          });

          // Calcular total de hóspedes
          this.totalHospedes = this.reservas.reduce(
            (total, r) => total + r.hospedes.length, 
            0
          );

          this.loading = false;
          console.log('✅ Relatório carregado:', this.reservas.length, 'apartamentos');
        });
      },
      error: (err) => {
        console.error('❌ Erro ao carregar reservas:', err);
        this.loading = false;
        alert('Erro ao carregar relatório');
      }
    });
  }

  formatarData(data: string): string {
    return new Date(data).toLocaleDateString('pt-BR');
  }

  formatarCPF(cpf: string): string {
    if (!cpf) return '';
    return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }

  imprimir(): void {
    window.print();
  }

  voltar(): void {
    this.router.navigate(['/reservas']);
  }
}