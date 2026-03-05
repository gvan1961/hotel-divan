import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { JantarService } from '../../../services/jantar.service';

@Component({
  selector: 'app-relatorio-faturamento',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './relatorio-faturamento.component.html',
  styleUrls: ['./relatorio-faturamento.component.css']
})
export class RelatorioFaturamentoComponent implements OnInit {
  dataInicio = '';
  dataFim = '';
  relatorio: any = null;
  carregando = false;

  // ✅ CONTROLE DE VISUALIZAÇÃO
  viewAtiva: 'faturamento' | 'apartamentos' | 'produto' = 'faturamento';

  // ✅ PRODUTOS POR APARTAMENTO
  produtosApartamento: any[] = [];
  carregandoApartamentos = false;

  // ✅ QUANTIDADE POR PRODUTO
  listaProdutos: any[] = [];
  produtoSelecionadoId: number | null = null;
  resultadoProduto: any = null;
  carregandoProduto = false;

  constructor(
    private jantarService: JantarService,
    private http: HttpClient
  ) {
    const hoje = new Date().toISOString().split('T')[0];
    this.dataInicio = hoje;
    this.dataFim = hoje;
  }

  ngOnInit(): void {
  // ✅ Carregar apenas produtos da categoria RESTAURANTE (id=2)
  this.jantarService.getProdutosPorCategoria(2).subscribe({
    next: (data) => {
      this.listaProdutos = data.sort((a, b) =>
        a.nomeProduto.localeCompare(b.nomeProduto, 'pt-BR')
      );
    },
    error: (err) => console.error('Erro ao carregar produtos:', err)
  });
}

 
  // ✅ ALTERNAR VIEW
  mudarView(view: 'faturamento' | 'apartamentos' | 'produto'): void {
  this.viewAtiva = view;
  // ✅ Limpar resultados anteriores ao trocar de aba
  this.produtosApartamento = [];
  this.resultadoProduto = null;
}

  // ✅ FATURAMENTO GERAL (existente)
  gerarRelatorio(): void {
    if (!this.dataInicio || !this.dataFim) { alert('Preencha as datas!'); return; }
    this.carregando = true;
    this.jantarService.gerarRelatorioFaturamento(this.dataInicio, this.dataFim).subscribe({
      next: (resultado) => {
        console.log('✅ Relatório gerado:', resultado);
        this.relatorio = resultado;
        this.carregando = false;
      },
      error: (error) => {
        console.error('❌ Erro ao gerar relatório:', error);
        alert('Erro ao gerar relatório!');
        this.carregando = false;
      }
    });
  }

  // ✅ PRODUTOS POR APARTAMENTO
  buscarProdutosPorApartamento(): void {
    if (!this.dataInicio || !this.dataFim) { alert('Preencha as datas!'); return; }
    this.carregandoApartamentos = true;
    this.produtosApartamento = [];

    this.http.post<any[]>('http://localhost:8080/api/jantar/relatorio-produtos-apartamento', {
      dataInicio: this.dataInicio,
      dataFim: this.dataFim
    }).subscribe({
      next: (data) => {
        this.produtosApartamento = data;
        this.carregandoApartamentos = false;
      },
      error: () => {
        alert('Erro ao buscar produtos por apartamento!');
        this.carregandoApartamentos = false;
      }
    });
  }

  // ✅ QUANTIDADE DE UM PRODUTO NO PERÍODO
  buscarQuantidadeProduto(): void {
    if (!this.produtoSelecionadoId) { alert('Selecione um produto!'); return; }
    if (!this.dataInicio || !this.dataFim) { alert('Preencha as datas!'); return; }
    this.carregandoProduto = true;
    this.resultadoProduto = null;

    this.http.post<any>('http://localhost:8080/api/jantar/relatorio-quantidade-produto', {
      produtoId: this.produtoSelecionadoId,
      dataInicio: this.dataInicio,
      dataFim: this.dataFim
    }).subscribe({
      next: (data) => {
        this.resultadoProduto = data;
        this.carregandoProduto = false;
      },
      error: () => {
        alert('Erro ao buscar quantidade do produto!');
        this.carregandoProduto = false;
      }
    });
  }

  imprimir(): void {
    window.print();
  }
}