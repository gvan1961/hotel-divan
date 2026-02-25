import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { JantarService, HospedeJantar } from '../../services/jantar.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-jantar-lista',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './jantar-lista.component.html',
  styleUrls: ['./jantar-lista.component.css']
})
export class JantarListaComponent implements OnInit {
  apartamentos: { numeroApartamento: string, hospedes: HospedeJantar[] }[] = [];
  loading = false;
  erro = '';
  
  // Modal
  modalAberto = false;
  hospedeSelecionado: HospedeJantar | null = null;
  apartamentoSelecionado = '';
  
  // Produtos
  produtos: any[] = [];
  produtosCarregando = false;
  produtosSelecionados: { produto: any, quantidade: number }[] = [];
  
  nomeBusca = '';
  apartamentoBusca = '';
  resultadoBusca: any = null;

  constructor(
  private jantarService: JantarService,
  private http: HttpClient
) {}

  ngOnInit() {
    this.carregarApartamentos();
  }

  carregarApartamentos() {
  this.loading = true;
  this.erro = '';

  this.jantarService.getApartamentosComHospedes().subscribe({
    next: (data: any[]) => {
      console.log('✅ Dados do backend:', data);

      // ✅ CONVERTER ARRAY DO BACKEND PARA ESTRUTURA DO FRONTEND
      this.apartamentos = data.map(apto => ({
        numeroApartamento: apto.numeroApartamento,
        hospedes: apto.hospedes.map((h: any) => ({
  id: h.hospedagemHospedeId,  // ✅ CORRETO!
  hospedagemHospedeId: h.hospedagemHospedeId,  // ✅ ADICIONAR
  nomeCompleto: h.nomeCliente,
  clienteId: h.clienteId,
  nomeCliente: h.nomeCliente,
  apartamentoId: apto.reservaId,
  numeroApartamento: apto.numeroApartamento,
  titular: h.titular
}))
      }));

      console.log('✅ Apartamentos processados:', this.apartamentos);
      this.loading = false;
    },
    error: (error) => {
      console.error('❌ Erro:', error);
      this.erro = 'Erro ao carregar apartamentos';
      this.loading = false;
    }
  });
}

  abrirModalComanda(hospede: HospedeJantar, numeroApartamento: string) {
    console.log('🍽️ Abrindo modal para:', hospede.nomeCompleto, '- Apto:', numeroApartamento);
    this.hospedeSelecionado = hospede;
    this.apartamentoSelecionado = numeroApartamento;
    this.modalAberto = true;
    this.carregarProdutos();
  }

  carregarProdutos() {
  this.produtosCarregando = true;

  // ✅ USAR ENDPOINT QUE JÁ FILTRA ESTOQUE > 0
  this.http.get<any[]>('http://localhost:8080/api/jantar/produtos-restaurante').subscribe({
    next: (produtos) => {
      console.log('✅ Produtos carregados:', produtos);
      this.produtos = produtos;
      this.produtosCarregando = false;
    },
    error: (error) => {
      console.error('❌ Erro ao carregar produtos:', error);
      this.produtosCarregando = false;
    }
  });
}

  adicionarProduto(produto: any) {
    const existe = this.produtosSelecionados.find(p => p.produto.id === produto.id);
    
    if (existe) {
      existe.quantidade++;
    } else {
      this.produtosSelecionados.push({
        produto: produto,
        quantidade: 1
      });
    }
  }

  removerProduto(index: number) {
    this.produtosSelecionados.splice(index, 1);
  }

  alterarQuantidade(index: number, quantidade: number) {
    if (quantidade <= 0) {
      this.removerProduto(index);
    } else {
      this.produtosSelecionados[index].quantidade = quantidade;
    }
  }

  calcularTotal(): number {
    return this.produtosSelecionados.reduce((total, item) => {
      return total + (item.produto.valorVenda * item.quantidade);
    }, 0);
  }

  confirmarComanda() {

   console.log('🔍 DEBUG - hospedeSelecionado:', this.hospedeSelecionado);
  console.log('🔍 DEBUG - hospedeSelecionado.id:', this.hospedeSelecionado?.id);
  console.log('🔍 DEBUG - hospedeSelecionado.hospedagemHospedeId:', this.hospedeSelecionado?.hospedagemHospedeId);
  

  if (this.produtosSelecionados.length === 0) {
    alert('Selecione pelo menos um produto!');
    return;
  }
  if (!this.hospedeSelecionado) {
    alert('Erro: Hóspede não selecionado!');
    return;
  }
  console.log('📝 Enviando comanda:', {
    hospede: this.hospedeSelecionado,
    apartamento: this.apartamentoSelecionado,
    produtos: this.produtosSelecionados,
    total: this.calcularTotal()
  });
  const itens = this.produtosSelecionados.map(item => ({
    produtoId: item.produto.id,
    quantidade: item.quantidade
  }));

  // ✅ SALVAR DADOS ANTES DE ENVIAR (fecharModal limpa as variáveis)
  const dadosImpressao: any = {
    hospede: this.hospedeSelecionado,
    apartamento: this.apartamentoSelecionado,
    produtos: [...this.produtosSelecionados],
    total: this.calcularTotal()
  };

  this.jantarService.salvarComanda(this.hospedeSelecionado.hospedagemHospedeId, itens).subscribe({
    next: (response: any) => {
      console.log('✅ Comanda salva com sucesso:', response);

      // ✅ PEGAR notaId DA RESPOSTA
      dadosImpressao.notaId = response.notaId;

      this.fecharModal();
      this.carregarApartamentos();

      // ✅ IMPRIMIR APÓS FECHAR MODAL
      setTimeout(() => {
        this.imprimirComanda(dadosImpressao);
      }, 300);
    },
    error: (error) => {
      console.error('❌ Erro ao salvar comanda:', error);
      alert('❌ Erro ao salvar comanda:\n' + (error.error || error.message));
    }
  });
}

 buscar() {
  const temNome = this.nomeBusca.trim().length > 0;
  const temApartamento = this.apartamentoBusca.trim().length > 0;

  if (!temNome && !temApartamento) {
    alert('Preencha pelo menos o nome OU o número do apartamento!');
    return;
  }

  console.log('🔍 Buscando:', this.nomeBusca, '- Apto:', this.apartamentoBusca);

  this.jantarService.buscarHospede(this.nomeBusca.trim(), this.apartamentoBusca.trim()).subscribe({
    next: (resultado) => {
      console.log('✅ Resultado da busca:', resultado);
      this.resultadoBusca = resultado;
    },
    error: (error) => {
      console.error('❌ Erro na busca:', error);
      alert('Erro ao buscar hóspede!');
    }
  });
}

limparBusca() {
  this.nomeBusca = '';
  this.apartamentoBusca = '';
  this.resultadoBusca = null;
}

abrirModalComandaBusca(hospede: any) {
  console.log('🍽️ Abrindo modal para hóspede da busca:', hospede);
  
  // ✅ Remapear para o mesmo formato da listagem
  this.hospedeSelecionado = {
    id: hospede.hospedagemHospedeId || hospede.id,
    hospedagemHospedeId: hospede.hospedagemHospedeId || hospede.id,
    nomeCompleto: hospede.nomeCliente || hospede.nomeCompleto,
    clienteId: hospede.clienteId,
    nomeCliente: hospede.nomeCliente || hospede.nomeCompleto,
    apartamentoId: hospede.reservaId || hospede.apartamentoId,
    numeroApartamento: hospede.numeroApartamento,
    titular: hospede.titular
  };
  
  this.apartamentoSelecionado = hospede.numeroApartamento;
  this.modalAberto = true;
  this.carregarProdutos();
}

  fecharModal() {
    this.modalAberto = false;
    this.hospedeSelecionado = null;
    this.apartamentoSelecionado = '';
    this.produtos = [];
    this.produtosSelecionados = [];
  }

  imprimirComanda(dados: any): void {
  const agora = new Date();
  const dataHora = agora.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });

  // ✅ GERAR LINHAS DE PRODUTOS
  let linhasProdutos = '';
  dados.produtos.forEach((item: any) => {
    const nome = item.produto.nome || item.produto.descricao || 'Produto';
    const qty = item.quantidade;
    const valorUnit = item.produto.valorVenda.toFixed(2);
    const valorTotal = (item.produto.valorVenda * item.quantidade).toFixed(2);

    linhasProdutos += `
      <div class="linha-produto">
        <span class="produto-nome">${nome}</span>
        <span class="produto-qty">${qty}x</span>
        <span class="produto-unit">R$${valorUnit}</span>
        <span class="produto-valor">R$${valorTotal}</span>
      </div>
    `;
  });

  const html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <title>Comanda - Jantar</title>
      <style>
        * {
          font-family: 'Courier New', monospace !important;
          font-weight: 700 !important;
          color: #000 !important;
          -webkit-print-color-adjust: exact !important;
          print-color-adjust: exact !important;
        }

        @page {
          margin: 3mm;
          size: 80mm auto;
        }

        body {
          width: 80mm;
          margin: 0;
          padding: 2mm;
          font-size: 11pt !important;
        }

        .cabecalho {
          text-align: center;
          border-bottom: 2px solid #000;
          padding-bottom: 4mm;
          margin-bottom: 3mm;
        }

        .cabecalho h1 {
          margin: 0 0 1mm 0;
          font-size: 15pt !important;
          font-weight: 900 !important;
        }

        .cabecalho h2 {
          margin: 0;
          font-size: 11pt !important;
          font-weight: 700 !important;
        }

        .cabecalho .nota-id {
          font-size: 9pt !important;
          margin-top: 1mm;
        }

        .secao-info {
          margin-bottom: 3mm;
        }

        .linha-info {
          display: flex;
          justify-content: space-between;
          margin-bottom: 1mm;
          font-size: 10pt !important;
        }

        .linha-info .label {
          font-weight: 900 !important;
        }

        .divisor {
          border-top: 1px dashed #000;
          margin: 3mm 0;
        }

        .divisor-solido {
          border-top: 2px solid #000;
          margin: 3mm 0;
        }

        .titulo-secao {
          font-size: 11pt !important;
          font-weight: 900 !important;
          margin-bottom: 2mm;
          text-align: center;
          text-transform: uppercase;
        }

        .linha-produto {
          display: flex;
          justify-content: space-between;
          align-items: baseline;
          margin-bottom: 1.5mm;
          font-size: 10pt !important;
        }

        .produto-nome {
          flex: 1;
          margin-right: 2mm;
        }

        .produto-qty {
          min-width: 18px;
          text-align: right;
          margin-right: 2mm;
        }

        .produto-unit {
          min-width: 42px;
          text-align: right;
          margin-right: 2mm;
          font-size: 8pt !important;
        }

        .produto-valor {
          min-width: 48px;
          text-align: right;
        }

        .linha-total {
          display: flex;
          justify-content: space-between;
          font-size: 13pt !important;
          font-weight: 900 !important;
          margin-top: 1mm;
        }

        .secao-assinatura {
          margin-top: 6mm;
        }

        .linha-assinatura {
          border-bottom: 2px solid #000;
          width: 100%;
          height: 8mm;
          margin-bottom: 1mm;
        }

        .label-assinatura {
          font-size: 8pt !important;
          text-align: center;
        }

        .rodape {
          text-align: center;
          margin-top: 4mm;
          font-size: 8pt !important;
        }
      </style>
    </head>
    <body>

      <!-- CABEÇALHO -->
      <div class="cabecalho">
        <h1>🏨 HOTEL DI VAN</h1>
        <h2>COMANDA DE JANTAR</h2>
        ${dados.notaId ? `<div class="nota-id">Comanda #${dados.notaId}</div>` : ''}
      </div>

      <!-- INFO HOSPEDE -->
      <div class="secao-info">
        <div class="linha-info">
          <span class="label">Apartamento:</span>
          <span>${dados.apartamento}</span>
        </div>
        <div class="linha-info">
          <span class="label">Hospede:</span>
          <span>${dados.hospede?.nomeCompleto || dados.hospede?.nome || 'N/A'}</span>
        </div>
        <div class="linha-info">
          <span class="label">Data/Hora:</span>
          <span>${dataHora}</span>
        </div>
      </div>

      <div class="divisor-solido"></div>

      <!-- PRODUTOS -->
      <div class="titulo-secao">PRODUTOS</div>

      <div class="linha-produto" style="font-size: 8pt !important; border-bottom: 1px solid #000; padding-bottom: 1mm; margin-bottom: 2mm;">
        <span class="produto-nome">ITEM</span>
        <span class="produto-qty">QTD</span>
        <span class="produto-unit">UNIT</span>
        <span class="produto-valor">TOTAL</span>
      </div>

      ${linhasProdutos}

      <div class="divisor-solido"></div>

      <!-- TOTAL -->
      <div class="linha-total">
        <span>TOTAL:</span>
        <span>R$ ${dados.total.toFixed(2)}</span>
      </div>

      <div class="divisor-solido"></div>

      <!-- ASSINATURA -->
      <div class="secao-assinatura">
        <div class="linha-assinatura"></div>
        <div class="label-assinatura">Assinatura do Hospede</div>
      </div>

      <!-- RODAPE -->
      <div class="rodape">
        Hotel Di Van - Arapiraca/AL
      </div>

    </body>
    </html>
  `;

  const printWindow = window.open('', '_blank');
  if (printWindow) {
    printWindow.document.write(html);
    printWindow.document.close();
    setTimeout(() => {
      printWindow.print();
      setTimeout(() => printWindow.close(), 500);
    }, 300);
  }
}
}   