import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface CheckoutInfo {
  reservaId: number;
  apartamentoNumero: string;
  clienteNome: string;
  dataCheckin: string;
  dataCheckout: string;
  quantidadeHospede: number;
  hospedes: string[];
  totalApagar: number;
}

@Component({
  selector: 'app-relatorio-checkouts',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container-relatorio">
      <!-- CABEÇALHO PARA TELA -->
      <div class="header-tela">
        <h1>📤 Relatório de Checkouts</h1>
        <div class="acoes">
          <button class="btn-voltar" (click)="voltar()">← Voltar</button>
          <button class="btn-imprimir" (click)="imprimir()" [disabled]="checkouts.length === 0">
            🖨️ Imprimir
          </button>
        </div>
      </div>

      <!-- FILTRO DE DATA -->
      <div class="filtro-data">
        <label>📅 Selecione a data:</label>
        <input type="date" [(ngModel)]="dataSelecionada" (change)="buscarCheckouts()">
        <button class="btn-hoje" (click)="selecionarHoje()">Hoje</button>
        <button class="btn-amanha" (click)="selecionarAmanha()">Amanhã</button>
      </div>

      <!-- LOADING -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Buscando checkouts...</p>
      </div>

      <!-- RELATÓRIO -->
      <div *ngIf="!loading" class="relatorio-conteudo">
        <!-- CABEÇALHO PARA IMPRESSÃO -->
        <div class="header-impressao">
          <h1>HOTEL DI VAN</h1>
          <h2>Checkouts Programados</h2>
          <p class="data-checkout">Data: {{ formatarDataCompleta(dataSelecionada) }}</p>
          <p class="data-geracao">Gerado em: {{ dataGeracao }}</p>
          <div class="totais-resumo">
            <span>Total de Checkouts: {{ checkouts.length }}</span>
            <span>Total de Hóspedes: {{ totalHospedes }}</span>
            <span>Total a Receber: R$ {{ formatarMoeda(totalAReceber) }}</span>
          </div>
        </div>

        <!-- LISTA DE CHECKOUTS -->
        <div class="lista-checkouts">
          <div class="checkout-item" *ngFor="let checkout of checkouts; let i = index">
            <!-- NÚMERO DO ITEM -->
            <div class="item-numero">{{ i + 1 }}</div>

            <!-- INFORMAÇÕES -->
            <div class="item-info">
              <div class="info-principal">
                <span class="apt-numero">Apto {{ checkout.apartamentoNumero }}</span>
                <span class="cliente-nome">{{ checkout.clienteNome }}</span>
                <span class="periodo">
                  {{ formatarData(checkout.dataCheckin) }} → {{ formatarData(checkout.dataCheckout) }}
                </span>
              </div>

              <!-- HÓSPEDES -->
              <div class="hospedes-lista" *ngIf="checkout.hospedes.length > 0">
                <strong>👥 Hóspedes ({{ checkout.quantidadeHospede }}):</strong>
                <span class="hospede-nome" *ngFor="let hospede of checkout.hospedes">
                  {{ hospede }}
                </span>
              </div>

              <!-- VALOR A PAGAR -->
              <div class="valor-info" [class.pago]="checkout.totalApagar === 0">
                <span class="label">Saldo:</span>
                <span class="valor">
                  R$ {{ formatarMoeda(checkout.totalApagar) }}
                  <span class="badge-status" *ngIf="checkout.totalApagar === 0">✅ PAGO</span>
                  <span class="badge-status alerta" *ngIf="checkout.totalApagar > 0">⚠️ DEVEDOR</span>
                </span>
              </div>
            </div>

            <!-- AÇÃO -->
            <div class="item-acao">
              <button class="btn-ver" (click)="verReserva(checkout.reservaId)">
                👁️ Ver
              </button>
            </div>
          </div>
        </div>

        <!-- MENSAGEM VAZIO -->
        <div *ngIf="checkouts.length === 0" class="vazio">
          <p>ℹ️ Nenhum checkout programado para esta data</p>
          <small>Selecione outra data para verificar</small>
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
      margin-bottom: 20px;
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

    .btn-imprimir:hover:not(:disabled) {
      background: #2980b9;
    }

    .btn-imprimir:disabled {
      background: #bdc3c7;
      cursor: not-allowed;
    }

    /* FILTRO DE DATA */
    .filtro-data {
      display: flex;
      gap: 15px;
      align-items: center;
      background: white;
      padding: 15px 20px;
      border-radius: 8px;
      margin-bottom: 20px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .filtro-data label {
      font-weight: 600;
      color: #2c3e50;
    }

    .filtro-data input[type="date"] {
      padding: 8px 12px;
      border: 2px solid #ddd;
      border-radius: 6px;
      font-size: 14px;
    }

    .btn-hoje, .btn-amanha {
      padding: 8px 16px;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.3s;
    }

    .btn-hoje {
      background: #27ae60;
      color: white;
    }

    .btn-hoje:hover {
      background: #229954;
    }

    .btn-amanha {
      background: #3498db;
      color: white;
    }

    .btn-amanha:hover {
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

    .data-checkout {
      margin: 10px 0;
      font-weight: 700;
      color: #3498db;
      font-size: 1.1em;
    }

    .data-geracao {
      margin: 5px 0;
      color: #95a5a6;
      font-size: 0.9em;
    }

    .totais-resumo {
      display: flex;
      justify-content: center;
      gap: 30px;
      margin-top: 15px;
      font-weight: 600;
      color: #2c3e50;
      flex-wrap: wrap;
    }

    /* LISTA DE CHECKOUTS */
    .lista-checkouts {
      display: flex;
      flex-direction: column;
      gap: 15px;
    }

    .checkout-item {
      display: flex;
      gap: 15px;
      border: 2px solid #e0e0e0;
      border-radius: 8px;
      padding: 15px;
      page-break-inside: avoid;
      transition: all 0.3s;
    }

    .checkout-item:hover {
      border-color: #3498db;
      box-shadow: 0 2px 8px rgba(52, 152, 219, 0.2);
    }

    .item-numero {
      display: flex;
      align-items: center;
      justify-content: center;
      min-width: 40px;
      height: 40px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 50%;
      font-weight: 700;
      font-size: 1.1em;
    }

    .item-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .info-principal {
      display: flex;
      gap: 15px;
      align-items: center;
      flex-wrap: wrap;
    }

    .apt-numero {
      background: #e3f2fd;
      color: #1976d2;
      padding: 4px 12px;
      border-radius: 6px;
      font-weight: 700;
      font-size: 0.95em;
    }

    .cliente-nome {
      font-weight: 600;
      color: #2c3e50;
      font-size: 1.05em;
    }

    .periodo {
      color: #7f8c8d;
      font-size: 0.9em;
    }

    .hospedes-lista {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      align-items: center;
      padding: 10px;
      background: #f8f9fa;
      border-radius: 6px;
      font-size: 0.9em;
    }

    .hospedes-lista strong {
      color: #2c3e50;
    }

    .hospede-nome {
      background: white;
      padding: 4px 10px;
      border-radius: 4px;
      color: #2c3e50;
      border: 1px solid #e0e0e0;
    }

    .valor-info {
      display: flex;
      gap: 10px;
      align-items: center;
      padding: 10px;
      background: #fff3cd;
      border-left: 4px solid #ffc107;
      border-radius: 4px;
    }

    .valor-info.pago {
      background: #d4edda;
      border-left-color: #28a745;
    }

    .valor-info .label {
      font-weight: 600;
      color: #856404;
    }

    .valor-info.pago .label {
      color: #155724;
    }

    .valor-info .valor {
      font-weight: 700;
      color: #856404;
      font-size: 1.1em;
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .valor-info.pago .valor {
      color: #155724;
    }

    .badge-status {
      background: #856404;
      color: white;
      padding: 3px 10px;
      border-radius: 12px;
      font-size: 0.75em;
      font-weight: 700;
    }

    .badge-status.alerta {
      background: #dc3545;
    }

    .valor-info.pago .badge-status {
      background: #28a745;
    }

    .item-acao {
      display: flex;
      align-items: center;
    }

    .btn-ver {
      background: #3498db;
      color: white;
      border: none;
      padding: 8px 16px;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.3s;
    }

    .btn-ver:hover {
      background: #2980b9;
      transform: translateY(-2px);
    }

    .vazio {
      text-align: center;
      padding: 80px 20px;
      color: #7f8c8d;
    }

    .vazio p {
      font-size: 1.2em;
      margin: 0 0 10px 0;
    }

    .vazio small {
      color: #95a5a6;
    }

    /* ESTILOS DE IMPRESSÃO */
    @media print {
      .container-relatorio {
        background: white;
        padding: 0;
      }

      .header-tela,
      .filtro-data {
        display: none !important;
      }

      .header-impressao {
        display: block !important;
      }

      .relatorio-conteudo {
        box-shadow: none;
        padding: 0;
      }

      .checkout-item {
        page-break-inside: avoid;
        margin-bottom: 20px;
      }

      .item-acao {
        display: none !important;
      }
    }

    @media (max-width: 768px) {
      .filtro-data {
        flex-direction: column;
        align-items: stretch;
      }

      .checkout-item {
        flex-direction: column;
      }

      .item-numero {
        width: 100%;
      }

      .totais-resumo {
        flex-direction: column;
        gap: 10px;
      }
    }
  `]
})
export class RelatorioCheckoutsComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);

  loading = false;
  dataSelecionada = '';
  checkouts: CheckoutInfo[] = [];
  dataGeracao = '';
  totalHospedes = 0;
  totalAReceber = 0;

  ngOnInit(): void {
    this.dataGeracao = new Date().toLocaleString('pt-BR');
    this.selecionarHoje();
  }

  selecionarHoje(): void {
    this.dataSelecionada = new Date().toISOString().split('T')[0];
    this.buscarCheckouts();
  }

  selecionarAmanha(): void {
    const amanha = new Date();
    amanha.setDate(amanha.getDate() + 1);
    this.dataSelecionada = amanha.toISOString().split('T')[0];
    this.buscarCheckouts();
  }

  buscarCheckouts(): void {
    if (!this.dataSelecionada) return;

    this.loading = true;
    this.checkouts = [];

    this.http.get<any[]>('http://localhost:8080/api/reservas').subscribe({
      next: (reservas) => {
        // Filtrar reservas que fazem checkout na data selecionada
        const reservasCheckout = reservas.filter(r => {
          if (r.status !== 'ATIVA') return false;
          const checkout = new Date(r.dataCheckout).toISOString().split('T')[0];
          return checkout === this.dataSelecionada;
        });

        // Para cada reserva, buscar os hóspedes
        const requests = reservasCheckout.map(reserva =>
          this.http.get<any[]>(`http://localhost:8080/api/hospedagem-hospedes/reserva/${reserva.id}`)
            .toPromise()
            .then(hospedes => ({
              reservaId: reserva.id,
              apartamentoNumero: reserva.apartamento.numeroApartamento,
              clienteNome: reserva.cliente.nome,
              dataCheckin: reserva.dataCheckin,
              dataCheckout: reserva.dataCheckout,
              quantidadeHospede: reserva.quantidadeHospede,
              hospedes: (hospedes || [])
                .filter(h => h.status === 'HOSPEDADO')
                .map(h => h.cliente?.nome || h.nomeCompleto),
              totalApagar: reserva.totalApagar || 0
            }))
        );

        Promise.all(requests).then(resultado => {
          // Ordenar por número de apartamento
          this.checkouts = resultado.sort((a, b) => {
            const numA = parseInt(a.apartamentoNumero) || 0;
            const numB = parseInt(b.apartamentoNumero) || 0;
            return numA - numB;
          });

          // Calcular totais
          this.totalHospedes = this.checkouts.reduce(
            (total, c) => total + c.quantidadeHospede,
            0
          );

          this.totalAReceber = this.checkouts.reduce(
            (total, c) => total + c.totalApagar,
            0
          );

          this.loading = false;
          console.log('✅ Checkouts carregados:', this.checkouts.length);
        });
      },
      error: (err) => {
        console.error('❌ Erro ao buscar checkouts:', err);
        this.loading = false;
        alert('Erro ao buscar checkouts');
      }
    });
  }

  formatarData(data: string): string {
    return new Date(data).toLocaleDateString('pt-BR');
  }

  formatarDataCompleta(data: string): string {
    const d = new Date(data + 'T00:00:00');
    return d.toLocaleDateString('pt-BR', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  formatarMoeda(valor: number): string {
    return valor.toFixed(2).replace('.', ',');
  }

  verReserva(id: number): void {
    this.router.navigate(['/reservas', id]);
  }

  imprimir(): void {
    window.print();
  }

  voltar(): void {
    this.router.navigate(['/reservas']);
  }
}