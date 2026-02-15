import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { FechamentoCaixaService, FechamentoCaixaDTO } from '../../services/fechamento-caixa.service';
import { CaixaStateService } from '../../services/caixa-state.service'; 
import { HasPermissionDirective } from '../../directives/has-permission.directive';


@Component({
  selector: 'app-fechamento-caixa',
  standalone: true,
  imports: [CommonModule, FormsModule, HasPermissionDirective],
  templateUrl: './fechamento-caixa.component.html',
  styleUrls: ['./fechamento-caixa.component.css']
})

export class FechamentoCaixaComponent implements OnInit {
  
  caixaId!: number;
  caixa?: FechamentoCaixaDTO;
  carregando: boolean = true;
  fechando: boolean = false;
  
  observacoesFechamento: string = '';
  mostrarModalFechamento: boolean = false;

  // ✅ PRODUTOS VENDIDOS
vendasDetalhadas: any = null;
carregandoVendas = false;
formasPagamentoVendas = [
  { key: 'DINHEIRO', nome: 'Dinheiro', icone: '💵' },
  { key: 'PIX', nome: 'PIX', icone: '📱' },
  { key: 'CARTAO_DEBITO', nome: 'Cartão de Débito', icone: '💳' },
  { key: 'CARTAO_CREDITO', nome: 'Cartão de Crédito', icone: '💳' },
  { key: 'TRANSFERENCIA', nome: 'Transferência', icone: '🏦' },
  { key: 'FATURADO', nome: 'Faturado', icone: '📄' }
];
  
  // Abas
  abaAtiva: string = 'resumo'; // resumo, movimentacao, apartamentos, detalhes
 // resumoCompleto: any = null;
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fechamentoCaixaService: FechamentoCaixaService,
    private caixaStateService: CaixaStateService
  ) {}
  
  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.caixaId = +params['id'];
      this.carregarCaixa();
    });
  }

  verRelatorioDetalhado(caixaId: number): void {
  this.router.navigate(['/fechamento-caixa', caixaId, 'relatorio']);
}
  
  carregarCaixa(): void {
  console.log('🔵 Carregando caixa ID:', this.caixaId);
  this.carregando = true;
  
  this.fechamentoCaixaService.buscarPorId(this.caixaId).subscribe({
    next: (caixa) => {
      console.log('✅ Caixa carregado:', caixa);
      this.caixa = caixa;
      
      // ✅ IMPORTANTE: Desativar o loading
      this.carregando = false;
      
      console.log('✅ Caixa pronto para exibição');
    },
    error: (error) => {
      console.error('❌ Erro ao carregar caixa:', error);
      alert('Erro ao carregar caixa: ' + (error.error?.erro || error.message));
      this.carregando = false;
      this.router.navigate(['/dashboard']);
    }
  });
}


//carregarResumoCompleto(): void {
//  this.fechamentoCaixaService.buscarResumoCompleto(this.caixaId).subscribe({
//    next: (resumo) => {
//      console.log('✅ Resumo completo:', resumo);
//      this.resumoCompleto = resumo;
//      this.carregando = false;
//    },
//    error: (error) => {
//      console.error('❌ Erro ao carregar resumo:', error);
//      this.carregando = false;
//    }
//  });
//}
  
  abrirModalFechamento(): void {
    if (confirm('Deseja realmente fechar o caixa?\n\nApós o fechamento não será possível fazer alterações.')) {
      this.mostrarModalFechamento = true;
    }
  }
  
  fecharCaixa(): void {
    this.fechando = true;
    
    this.fechamentoCaixaService.fecharCaixa(this.caixaId, this.observacoesFechamento).subscribe({
      next: (response) => {
        console.log('✅ Caixa fechado:', response);
        alert('✅ Caixa fechado com sucesso!');
        this.mostrarModalFechamento = false;
        this.fechando = false;

        // ✅ NOTIFICAR O SIDEBAR
        this.caixaStateService.notificarAtualizacao();

        this.carregarCaixa(); // Recarregar para mostrar dados atualizados
      },
      error: (error) => {
        console.error('❌ Erro ao fechar caixa:', error);
        alert('❌ Erro ao fechar caixa: ' + (error.error?.erro || error.message));
        this.fechando = false;

      }
    });
  }
  
  imprimirRelatorio(): void {
  if (!this.caixa) return;

  // Buscar dados do relatório
  this.fechamentoCaixaService.buscarRelatorioImpressao(this.caixaId).subscribe({
    next: (relatorio: any) => {
      this.gerarHTMLImpressao(relatorio);
    },
    error: (error: any) => {
      console.error('Erro ao gerar relatório:', error);
      alert('Erro ao gerar relatório de impressão');
    }
  });
}

  gerarHTMLImpressao(relatorio: any): void {
  const htmlImpressao = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>Fechamento Caixa #${relatorio.caixaId}</title>
      <style>
        @page { size: 80mm auto; margin: 0; }
        body { 
          font-family: 'Courier New', monospace; 
          font-size: 10px; 
          width: 80mm; 
          margin: 0; 
          padding: 5mm;
          line-height: 1.3;
        }
        .titulo { 
          font-size: 14px; 
          font-weight: bold; 
          text-align: center; 
          margin-bottom: 5px; 
        }
        .info { 
          font-size: 9px; 
          margin: 2px 0; 
        }
        .separador { 
          text-align: center; 
          margin: 5px 0; 
          font-size: 10px; 
        }
        .linha { 
          display: flex; 
          justify-content: space-between; 
          margin: 2px 0; 
          font-size: 10px; 
        }
        .linha.total { 
          font-weight: bold; 
          font-size: 12px; 
          margin: 5px 0; 
          border-top: 1px dashed #000;
          padding-top: 5px;
        }
        .destaque {
          background: #f0f0f0;
          padding: 5px;
          margin: 5px 0;
          font-weight: bold;
        }
      </style>
    </head>
    <body>
      <div class="titulo">HOTEL DI VAN</div>
      <div class="titulo">FECHAMENTO DE CAIXA</div>
      
      <div class="separador">================================</div>
      
      <div class="info">Caixa: #${relatorio.caixaId}</div>
      <div class="info">Responsável: ${relatorio.recepcionistaNome}</div>
      <div class="info">Turno: ${relatorio.turno}</div>
      <div class="info">Abertura: ${this.formatarDataHoraSimples(relatorio.dataHoraAbertura)}</div>
      <div class="info">Fechamento: ${this.formatarDataHoraSimples(relatorio.dataHoraFechamento)}</div>
      
      <div class="separador">================================</div>
      <div class="separador"><strong>RESUMO POR FORMA DE PAGAMENTO</strong></div>
      <div class="separador">================================</div>
      
      ${relatorio.totalGeral.dinheiro > 0 ? `<div class="linha"><span>💵 Dinheiro:</span><span>R$ ${this.formatarValorImpressao(relatorio.totalGeral.dinheiro)}</span></div>` : ''}
      ${relatorio.totalGeral.pix > 0 ? `<div class="linha"><span>📱 PIX:</span><span>R$ ${this.formatarValorImpressao(relatorio.totalGeral.pix)}</span></div>` : ''}
      ${relatorio.totalGeral.cartaoDebito > 0 ? `<div class="linha"><span>💳 C. Débito:</span><span>R$ ${this.formatarValorImpressao(relatorio.totalGeral.cartaoDebito)}</span></div>` : ''}
      ${relatorio.totalGeral.cartaoCredito > 0 ? `<div class="linha"><span>💳 C. Crédito:</span><span>R$ ${this.formatarValorImpressao(relatorio.totalGeral.cartaoCredito)}</span></div>` : ''}
      ${relatorio.totalGeral.transferencia > 0 ? `<div class="linha"><span>🏦 Transferência:</span><span>R$ ${this.formatarValorImpressao(relatorio.totalGeral.transferencia)}</span></div>` : ''}
      ${relatorio.totalGeral.faturado > 0 ? `<div class="linha"><span>📄 Faturado:</span><span>R$ ${this.formatarValorImpressao(relatorio.totalGeral.faturado)}</span></div>` : ''}
      
      <div class="separador">================================</div>
      
      <div class="linha total">
        <span>TOTAL GERAL:</span>
        <span>R$ ${this.formatarValorImpressao(relatorio.totalGeral.total)}</span>
      </div>
      
      <div class="separador">================================</div>
      
      <div class="destaque">
        <div class="linha">
          <span>💰 DINHEIRO A ENTREGAR:</span>
          <span>R$ ${this.formatarValorImpressao(relatorio.totalGeral.dinheiro)}</span>
        </div>
      </div>
      
      <div class="separador">================================</div>
      
      <div style="text-align: center; font-size: 9px; margin-top: 10px;">
        Impresso em: ${new Date().toLocaleString('pt-BR')}
      </div>

      <script>
        window.onload = function() {
          window.print();
          window.onafterprint = function() {
            window.close();
          };
        };
      </script>
    </body>
    </html>
  `;

  const janelaImpressao = window.open('', '_blank', 'width=400,height=600');
  if (janelaImpressao) {
    janelaImpressao.document.write(htmlImpressao);
    janelaImpressao.document.close();
  }
}

formatarValorImpressao(valor: number): string {
  if (!valor) return '0,00';
  return valor.toFixed(2).replace('.', ',');
}

formatarDataHoraSimples(data: string): string {
  if (!data) return '-';
  const d = new Date(data);
  return d.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

  
  // ✅ MÉTODOS DE FORMATAÇÃO
  
  formatarMoeda(valor: number | undefined | null): string {
    if (!valor && valor !== 0) return '0,00';
    
    return new Intl.NumberFormat('pt-BR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(valor);
  }
  
  formatarDataHora(dataHora: string | undefined): string {
    if (!dataHora) return '-';
    
    return new Date(dataHora).toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }
  
  getCorStatus(status: string): string {
    return status === 'ABERTO' ? '#4caf50' : '#2196f3';
  }
  
  // ✅ MÉTODOS DE CÁLCULO DE TOTAIS
  
  calcularTotalFormasPagamento(): number {
    if (!this.caixa) return 0;
    
    return (
      (this.caixa.totalDinheiro || 0) +
      (this.caixa.totalPix || 0) +
      (this.caixa.totalCartaoDebito || 0) +
      (this.caixa.totalCartaoCredito || 0) +
      (this.caixa.totalTransferencia || 0) +
      (this.caixa.totalFaturado || 0)
    );
  }

  calcularTotalRecebidoAVista(): number {
  if (!this.caixa) return 0;
  
  return (
    (this.caixa.totalDinheiro || 0) +
    (this.caixa.totalPix || 0) +
    (this.caixa.totalCartaoDebito || 0) +
    (this.caixa.totalCartaoCredito || 0) +
    (this.caixa.totalTransferencia || 0)
    // ❌ NÃO somar totalFaturado aqui
  );
}
  
  calcularTotalPorApartamento(): number {
    if (!this.caixa || !this.caixa.resumoApartamentos) return 0;
    
    return this.caixa.resumoApartamentos.reduce(
      (total, apto) => total + (apto.totalPagamentos || 0), 
      0
    );
  }
  
  calcularTotalDetalhes(): number {
  if (!this.caixa || !this.caixa.detalhes) return 0;
  
  return this.caixa.detalhes.reduce(
    (total, detalhe) => total + (detalhe.valor || 0), 
    0
  );
}

/**
 * ✅ CARREGAR VENDAS DETALHADAS (PRODUTOS)
 */
carregarVendasDetalhadas(): void {
  if (!this.caixaId || this.vendasDetalhadas) {
    return; // Já carregou ou não tem caixa
  }
  
  this.carregandoVendas = true;
  console.log('🛒 Carregando vendas detalhadas do caixa #' + this.caixaId);
  
  this.fechamentoCaixaService.buscarVendasDetalhadas(this.caixaId).subscribe({
    next: (response) => {
      console.log('✅ Vendas detalhadas carregadas:', response);
      this.vendasDetalhadas = response;
      this.carregandoVendas = false;
    },
    error: (error) => {
      console.error('❌ Erro ao carregar vendas detalhadas:', error);
      this.carregandoVendas = false;
    }
  });
}

/**
 * ✅ VERIFICAR SE TEM VENDAS NA FORMA DE PAGAMENTO
 */
temVendasNaForma(formaPagamento: string): boolean {
  if (!this.vendasDetalhadas?.vendasPorFormaPagamento) {
    return false;
  }
  const vendas = this.vendasDetalhadas.vendasPorFormaPagamento[formaPagamento];
  return vendas && vendas.length > 0;
}

/**
 * ✅ OBTER VENDAS POR FORMA DE PAGAMENTO
 */
getVendasPorForma(formaPagamento: string): any[] {
  if (!this.vendasDetalhadas?.vendasPorFormaPagamento) {
    return [];
  }
  return this.vendasDetalhadas.vendasPorFormaPagamento[formaPagamento] || [];
}

/**
 * ✅ OBTER TOTAL POR FORMA DE PAGAMENTO
 */
getTotalPorForma(formaPagamento: string): number {
  if (!this.vendasDetalhadas?.totaisPorFormaPagamento) {
    return 0;
  }
  return this.vendasDetalhadas.totaisPorFormaPagamento[formaPagamento] || 0;
}

/**
 * ✅ OBTER QUANTIDADE DE VENDAS POR FORMA
 */
getQtdVendasPorForma(formaPagamento: string): number {
  if (!this.vendasDetalhadas?.quantidadeVendasPorFormaPagamento) {
    return 0;
  }
  return this.vendasDetalhadas.quantidadeVendasPorFormaPagamento[formaPagamento] || 0;
}

/**
 * ✅ OBTER QUANTIDADE DE PRODUTOS POR FORMA
 */
getQtdProdutosPorForma(formaPagamento: string): number {
  if (!this.vendasDetalhadas?.quantidadeProdutosPorFormaPagamento) {
    return 0;
  }
  return this.vendasDetalhadas.quantidadeProdutosPorFormaPagamento[formaPagamento] || 0;
}
  
  // ✅ NAVEGAÇÃO
  
  voltar(): void {
    this.router.navigate(['/dashboard']);
  }
}