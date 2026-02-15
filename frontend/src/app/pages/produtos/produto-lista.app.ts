import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ProdutoService } from '../../services/produto.service';
import { Produto } from '../../models/produto.model';
import { HasPermissionDirective } from '../../directives/has-permission.directive';

interface Categoria {
  id: number;
  nome: string;
  selecionada?: boolean;
}

@Component({
  selector: 'app-produto-lista',
  standalone: true,
  imports: [CommonModule, FormsModule, HasPermissionDirective],
  template: `
    <div class="container">
      <div class="header">
        <div class="header-left">
          <button class="btn-voltar" (click)="voltar()">← Voltar</button>
          <h1>Produtos</h1>
        </div>
        <div class="header-right">
          <button class="btn-imprimir" (click)="imprimirConferencia()">
            🖨️ Imprimir Conferência
          </button>
          <button *hasPermission="'PRODUTO_CRIAR'" 
        class="btn-primary" 
        (click)="novo()">+ Novo Produto</button>
        </div>
      </div>

      <!-- FILTRO POR CATEGORIAS -->
      <div class="filtro-categorias">
        <h3>📂 Filtrar por Categorias:</h3>
        <div class="categorias-grid">
          <label *ngFor="let cat of categorias" class="categoria-checkbox">
            <input 
              type="checkbox" 
              [(ngModel)]="cat.selecionada"
              (change)="filtrarPorCategorias()"
            />
            <span>{{ cat.nome }}</span>
          </label>
        </div>
        <div class="filtro-acoes">
          <button class="btn-limpar" (click)="limparFiltros()">Limpar Filtros</button>
          <button class="btn-todas" (click)="selecionarTodas()">Selecionar Todas</button>
        </div>
      </div>

      <div class="search-box">
        <input 
          type="text" 
          placeholder="Buscar produto por nome..."
          [(ngModel)]="filtro"
          (input)="filtrar()"
        />
      </div>

      <div class="resumo" *ngIf="!loading">
        <span>📦 Total de produtos exibidos: <strong>{{ produtosFiltrados.length }}</strong></span>
      </div>

      <div *ngIf="loading" class="loading">Carregando...</div>

      <div *ngIf="!loading && produtosFiltrados.length === 0" class="empty">
        Nenhum produto encontrado
      </div>

      <div class="table-container" *ngIf="!loading && produtosFiltrados.length > 0">
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>Categoria</th>
              <th>Quantidade</th>
              <th>Valor Venda</th>
              <th>Valor Compra</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let produto of produtosFiltrados">
              <td>{{ produto.nomeProduto }}</td>
              <td>{{ produto.categoria?.nome || '-' }}</td>
              <td [class.estoque-baixo]="produto.quantidade < 10">{{ produto.quantidade }}</td>
              <td>{{ produto.valorVenda | currency:'BRL' }}</td>
              <td>{{ produto.valorCompra | currency:'BRL' }}</td>
              <td>
                <button *hasPermission="'PRODUTO_EDITAR'" 
                class="btn-edit" 
                (click)="editar(produto.id!)">Editar</button>
                <button *hasPermission="'PRODUTO_EXCLUIR'" 
                class="btn-delete" 
                (click)="excluir(produto.id!)">Excluir</button>
              </td>
            </tr>
          </tbody>
        </table>
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

    .header-right {
      display: flex;
      gap: 10px;
    }

    h1 {
      color: #333;
      margin: 0;
    }

    .btn-primary, .btn-imprimir, .btn-voltar {
      border: none;
      padding: 10px 20px;
      border-radius: 5px;
      cursor: pointer;
      font-size: 14px;
      font-weight: 600;
      transition: all 0.3s;
    }

    .btn-primary {
      background: #667eea;
      color: white;
    }

    .btn-primary:hover {
      background: #5568d3;
    }

    .btn-imprimir {
      background: #28a745;
      color: white;
    }

    .btn-imprimir:hover {
      background: #218838;
    }

    .btn-voltar {
      background: #6c757d;
      color: white;
    }

    .btn-voltar:hover {
      background: #5a6268;
      transform: translateX(-3px);
    }

    /* FILTRO DE CATEGORIAS */
    .filtro-categorias {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px;
      border-radius: 10px;
      margin-bottom: 20px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }

    .filtro-categorias h3 {
      color: white;
      margin: 0 0 15px 0;
      font-size: 1.1em;
    }

    .categorias-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 12px;
      margin-bottom: 15px;
    }

    .categoria-checkbox {
      background: white;
      padding: 12px 15px;
      border-radius: 6px;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 10px;
      transition: all 0.3s;
      font-weight: 500;
    }

    .categoria-checkbox:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(0,0,0,0.2);
    }

    .categoria-checkbox input[type="checkbox"] {
      width: 18px;
      height: 18px;
      cursor: pointer;
    }

    .filtro-acoes {
      display: flex;
      gap: 10px;
    }

    .btn-limpar, .btn-todas {
      background: white;
      color: #667eea;
      border: none;
      padding: 8px 16px;
      border-radius: 5px;
      cursor: pointer;
      font-weight: 600;
      font-size: 13px;
      transition: all 0.3s;
    }

    .btn-limpar:hover, .btn-todas:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(0,0,0,0.2);
    }

    .search-box {
      margin-bottom: 15px;
    }

    .search-box input {
      width: 100%;
      max-width: 400px;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 5px;
      font-size: 14px;
    }

    .resumo {
      margin-bottom: 15px;
      padding: 10px;
      background: #e7f3ff;
      border-left: 4px solid #667eea;
      border-radius: 4px;
      font-size: 14px;
    }

    .loading, .empty {
      text-align: center;
      padding: 40px;
      color: #666;
    }

    .table-container {
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      overflow-x: auto;
    }

    table {
      width: 100%;
      border-collapse: collapse;
    }

    th {
      background: #f8f9fa;
      padding: 12px;
      text-align: left;
      font-weight: 600;
      color: #333;
      border-bottom: 2px solid #dee2e6;
    }

    td {
      padding: 12px;
      border-bottom: 1px solid #dee2e6;
    }

    tr:hover {
      background: #f8f9fa;
    }

    .estoque-baixo {
      color: #dc3545;
      font-weight: bold;
    }

    .btn-edit, .btn-delete {
      padding: 6px 12px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 12px;
      margin-right: 5px;
    }

    .btn-edit {
      background: #28a745;
      color: white;
    }

    .btn-edit:hover {
      background: #218838;
    }

    .btn-delete {
      background: #dc3545;
      color: white;
    }

    .btn-delete:hover {
      background: #c82333;
    }
  `]
})
export class ProdutoListaApp implements OnInit {
  private produtoService = inject(ProdutoService);
  private router = inject(Router);
  private http = inject(HttpClient);

  produtos: Produto[] = [];
  produtosFiltrados: Produto[] = [];
  categorias: Categoria[] = [];
  filtro = '';
  loading = true;

  ngOnInit(): void {
    this.carregarCategorias();
    this.carregarProdutos();
  }

  carregarCategorias(): void {
    this.http.get<any[]>('http://localhost:8080/api/categorias').subscribe({
      next: (data) => {
        this.categorias = data.map(c => ({
          id: c.id,
          nome: c.nome,
          selecionada: false
        }));
        console.log('✅ Categorias carregadas:', this.categorias.length);
      },
      error: (err) => {
        console.error('❌ Erro ao carregar categorias:', err);
      }
    });
  }

  carregarProdutos(): void {
    this.produtoService.getAll().subscribe({
      next: (data) => {
        this.produtos = data;
        this.produtosFiltrados = data;
        this.loading = false;
        console.log('✅ Produtos carregados:', data.length);
      },
      error: (err) => {
        console.error('❌ Erro ao carregar produtos', err);
        this.loading = false;
      }
    });
  }

  filtrarPorCategorias(): void {
    const categoriasSelecionadas = this.categorias
      .filter(c => c.selecionada)
      .map(c => c.id);

    if (categoriasSelecionadas.length === 0) {
      // Nenhuma categoria selecionada = mostrar todos
      this.produtosFiltrados = this.produtos;
    } else {
      // Filtrar pelos IDs das categorias selecionadas
      this.produtosFiltrados = this.produtos.filter(p =>
        p.categoria && categoriasSelecionadas.includes(p.categoria.id)
      );
    }

    // Aplicar filtro de texto também
    if (this.filtro.trim() !== '') {
      this.filtrar();
    }

    console.log('🔍 Produtos filtrados:', this.produtosFiltrados.length);
  }

  filtrar(): void {
    const termo = this.filtro.toLowerCase();
    
    const categoriasSelecionadas = this.categorias
      .filter(c => c.selecionada)
      .map(c => c.id);

    let base = this.produtos;

    // Aplicar filtro de categoria primeiro
    if (categoriasSelecionadas.length > 0) {
      base = this.produtos.filter(p =>
        p.categoria && categoriasSelecionadas.includes(p.categoria.id)
      );
    }

    // Depois aplicar filtro de texto
    this.produtosFiltrados = base.filter(p =>
      p.nomeProduto.toLowerCase().includes(termo)
    );
  }

  limparFiltros(): void {
    this.categorias.forEach(c => c.selecionada = false);
    this.filtro = '';
    this.produtosFiltrados = this.produtos;
  }

  selecionarTodas(): void {
    const todasSelecionadas = this.categorias.every(c => c.selecionada);
    
    if (todasSelecionadas) {
      // Se todas estão selecionadas, desmarcar todas
      this.categorias.forEach(c => c.selecionada = false);
    } else {
      // Caso contrário, marcar todas
      this.categorias.forEach(c => c.selecionada = true);
    }
    
    this.filtrarPorCategorias();
  }

  imprimirConferencia(): void {
    const categoriasSelecionadas = this.categorias.filter(c => c.selecionada);
    
    if (this.produtosFiltrados.length === 0) {
      alert('⚠️ Nenhum produto para imprimir!');
      return;
    }

    const agora = new Date();
    const dataHora = agora.toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });

    let html = `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
        <title>Conferência de Estoque</title>
        <style>
          @page { 
            size: 80mm auto; 
            margin: 0; 
          }
          
          body { 
            font-family: 'Courier New', monospace; 
            font-size: 11px;
            width: 80mm; 
            margin: 0; 
            padding: 5mm;
            line-height: 1.4;
          }
          
          .cabecalho {
            text-align: center;
            margin-bottom: 10px;
            border-bottom: 2px dashed #000;
            padding-bottom: 8px;
          }
          
          .titulo {
            font-size: 14px;
            font-weight: bold;
            margin: 5px 0;
            letter-spacing: 1px;
          }
          
          .data {
            font-size: 10px;
            margin: 3px 0;
          }
          
          .categorias-info {
            font-size: 10px;
            margin: 5px 0;
            font-weight: bold;
          }
          
          .separador {
            text-align: center;
            margin: 8px 0;
            font-size: 10px;
          }
          
          .produto-item {
            margin: 8px 0;
            padding: 5px 0;
            border-bottom: 1px dotted #666;
          }
          
          .produto-nome {
            font-weight: bold;
            font-size: 12px;
            margin-bottom: 3px;
          }
          
          .produto-qtd {
            font-size: 11px;
            display: flex;
            justify-content: space-between;
          }
          
          .conferencia {
            display: flex;
            justify-content: space-between;
            margin-top: 3px;
            font-size: 10px;
          }
          
          .linha-conferencia {
            border-bottom: 1px solid #000;
            width: 50px;
          }
          
          .rodape {
            margin-top: 15px;
            text-align: center;
            font-size: 10px;
            border-top: 2px dashed #000;
            padding-top: 8px;
          }
          
          .total {
            font-weight: bold;
            font-size: 12px;
            text-align: center;
            margin: 10px 0;
          }
        </style>
      </head>
      <body>
        <div class="cabecalho">
          <div class="titulo">CONFERÊNCIA DE ESTOQUE</div>
          <div class="data">${dataHora}</div>
          ${categoriasSelecionadas.length > 0 ? `
            <div class="categorias-info">
              Categorias: ${categoriasSelecionadas.map(c => c.nome).join(', ')}
            </div>
          ` : ''}
        </div>
        
        <div class="separador">================================</div>
    `;

    // Agrupar por categoria
    const produtosPorCategoria = this.agruparPorCategoria(this.produtosFiltrados);

    for (const [categoriaNome, produtos] of Object.entries(produtosPorCategoria)) {
      html += `
        <div style="font-weight: bold; margin: 10px 0; font-size: 12px; text-decoration: underline;">
          ${categoriaNome}
        </div>
      `;

      produtos.forEach((produto: Produto) => {
        html += `
          <div class="produto-item">
            <div class="produto-nome">${produto.nomeProduto}</div>
            <div class="produto-qtd">
              <span>Estoque: <strong>${produto.quantidade}</strong></span>
              <span>Conferido: <span class="linha-conferencia">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></span>
            </div>
          </div>
        `;
      });
    }

    html += `
        <div class="total">
          Total de Produtos: ${this.produtosFiltrados.length}
        </div>
        
        <div class="rodape">
          <p>_____________________________</p>
          <p>Assinatura do Responsável</p>
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

    const janelaImpressao = window.open('', '_blank', 'width=800,height=600');
    if (janelaImpressao) {
      janelaImpressao.document.write(html);
      janelaImpressao.document.close();
    }
  }

  private agruparPorCategoria(produtos: Produto[]): { [key: string]: Produto[] } {
    const agrupado: { [key: string]: Produto[] } = {};

    produtos.forEach(produto => {
      const categoriaNome = produto.categoria?.nome || 'Sem Categoria';
      
      if (!agrupado[categoriaNome]) {
        agrupado[categoriaNome] = [];
      }
      
      agrupado[categoriaNome].push(produto);
    });

    return agrupado;
  }

  novo(): void {
    this.router.navigate(['/produtos/novo']);
  }

  editar(id: number): void {
    this.router.navigate(['/produtos/editar', id]);
  }

  voltar(): void {
    this.router.navigate(['/cadastros']);
  }

  excluir(id: number): void {
    if (confirm('Deseja realmente excluir este produto?')) {
      this.produtoService.delete(id).subscribe({
        next: () => {
          this.carregarProdutos();
        },
        error: (err) => {
          console.error('Erro ao excluir produto', err);
          alert('Erro ao excluir produto');
        }
      });
    }
  }
}