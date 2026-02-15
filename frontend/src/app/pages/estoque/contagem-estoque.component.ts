import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProdutoService } from '../../services/produto.service';
import { CategoriaService } from '../../services/categoria.service';

interface Categoria {
  id: number;
  nome: string;  // ← TROCAR aqui
  selecionada?: boolean;
}

interface Produto {
  id: number;
  nomeProduto: string;
  quantidade: number;
  categoriaId: number;
  categoriaNome: string;
}

@Component({
  selector: 'app-contagem-estoque',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>📋 Contagem de Estoque</h1>
        <button class="btn-voltar" (click)="voltar()">← Voltar</button>
      </div>

      <!-- LOADING -->
      <div *ngIf="carregando" class="loading">
        <div class="spinner"></div>
        <p>Carregando categorias...</p>
      </div>

      <!-- SELEÇÃO DE CATEGORIAS -->
      <div *ngIf="!carregando" class="card-categorias">
        <h2>📦 Selecione as Categorias</h2>
        <p class="subtitulo">Marque as categorias que deseja incluir na contagem</p>

        <div class="acoes-rapidas">
          <button class="btn-secundario" (click)="selecionarTodas()">
            ✅ Selecionar Todas
          </button>
          <button class="btn-secundario" (click)="desmarcarTodas()">
            ❌ Desmarcar Todas
          </button>
        </div>

        <div class="lista-categorias">
          <label class="checkbox-categoria" *ngFor="let categoria of categorias">
            <input 
              type="checkbox" 
              [(ngModel)]="categoria.selecionada"
              (change)="atualizarContador()">
            <span class="checkbox-label">{{ categoria.nome }}</span>
          </label>
        </div>

        <div class="info-selecao" *ngIf="categoriasSelecioadas.length > 0">
          ✅ {{ categoriasSelecioadas.length }} categoria(s) selecionada(s)
        </div>

        <div class="aviso-selecao" *ngIf="categoriasSelecioadas.length === 0">
          ⚠️ Selecione pelo menos uma categoria para gerar a contagem
        </div>
      </div>

      <!-- BOTÃO IMPRIMIR -->
      <div class="card-acoes" *ngIf="!carregando">
        <button 
          class="btn-imprimir"
          [disabled]="categoriasSelecioadas.length === 0 || imprimindo"
          (click)="imprimirContagem()">
          {{ imprimindo ? '⏳ Gerando...' : '🖨️ Imprimir Contagem' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .container {
      max-width: 900px;
      margin: 0 auto;
      padding: 20px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
      padding-bottom: 20px;
      border-bottom: 3px solid #007bff;
    }

    .header h1 {
      margin: 0;
      font-size: 28px;
      color: #333;
    }

    .btn-voltar {
      padding: 10px 20px;
      background: #6c757d;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.3s;
    }

    .btn-voltar:hover {
      background: #5a6268;
      transform: translateX(-5px);
    }

    .loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60px;
      gap: 20px;
    }

    .spinner {
      width: 50px;
      height: 50px;
      border: 5px solid #f3f3f3;
      border-top: 5px solid #007bff;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    .card-categorias,
    .card-acoes {
      background: white;
      padding: 30px;
      border-radius: 12px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
      margin-bottom: 20px;
    }

    .card-categorias h2 {
      margin: 0 0 10px 0;
      font-size: 22px;
      color: #333;
    }

    .subtitulo {
      color: #666;
      margin: 0 0 20px 0;
      font-size: 14px;
    }

    .acoes-rapidas {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;
    }

    .btn-secundario {
      padding: 8px 16px;
      background: #f8f9fa;
      border: 2px solid #dee2e6;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.3s;
    }

    .btn-secundario:hover {
      background: #e9ecef;
      border-color: #adb5bd;
    }

    .lista-categorias {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: 15px;
      margin: 20px 0;
    }

    .checkbox-categoria {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 15px;
      background: #f8f9fa;
      border: 2px solid #e9ecef;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;
    }

    .checkbox-categoria:hover {
      background: #e9ecef;
      border-color: #007bff;
    }

    .checkbox-categoria input[type="checkbox"] {
      width: 20px;
      height: 20px;
      cursor: pointer;
    }

    .checkbox-label {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      cursor: pointer;
    }

    .info-selecao {
      background: #d4edda;
      border: 2px solid #28a745;
      border-radius: 8px;
      padding: 15px;
      color: #155724;
      font-weight: 600;
      text-align: center;
    }

    .aviso-selecao {
      background: #fff3cd;
      border: 2px solid #ffc107;
      border-radius: 8px;
      padding: 15px;
      color: #856404;
      font-weight: 600;
      text-align: center;
    }

    .card-acoes {
      text-align: center;
    }

    .btn-imprimir {
      padding: 15px 40px;
      background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 18px;
      font-weight: 700;
      cursor: pointer;
      transition: all 0.3s;
      box-shadow: 0 4px 15px rgba(40, 167, 69, 0.3);
    }

    .btn-imprimir:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(40, 167, 69, 0.4);
    }

    .btn-imprimir:disabled {
      background: #6c757d;
      cursor: not-allowed;
      box-shadow: none;
    }
  `]
})
export class ContagemEstoqueComponent implements OnInit {
  
  categorias: Categoria[] = [];
  categoriasSelecioadas: Categoria[] = [];
  carregando = true;
  imprimindo = false;

  constructor(
    private produtoService: ProdutoService,
    private categoriaService: CategoriaService
  ) {}

  ngOnInit(): void {
    this.carregarCategorias();
  }

  carregarCategorias(): void {
  this.carregando = true;
  
  this.categoriaService.getAll().subscribe({
    next: (categorias: any[]) => {
      this.categorias = categorias.map((cat: any) => {
        return {
          id: cat.id,
          nome: cat.nome,
          selecionada: false
        };
      });
      
      this.carregando = false;
      console.log('✅ Categorias carregadas:', this.categorias.length);
    },
    error: (error: any) => {
      console.error('❌ Erro ao carregar categorias:', error);
      alert('Erro ao carregar categorias');
      this.carregando = false;
    }
  });
}

  selecionarTodas(): void {
    this.categorias.forEach(cat => cat.selecionada = true);
    this.atualizarContador();
  }

  desmarcarTodas(): void {
    this.categorias.forEach(cat => cat.selecionada = false);
    this.atualizarContador();
  }

  atualizarContador(): void {
    this.categoriasSelecioadas = this.categorias.filter(cat => cat.selecionada);
  }

  imprimirContagem(): void {
  if (this.categoriasSelecioadas.length === 0) {
    alert('⚠️ Selecione pelo menos uma categoria');
    return;
  }

  this.imprimindo = true;

  // Buscar produtos das categorias selecionadas
  const idsCategoria = this.categoriasSelecioadas.map(cat => cat.id);
  
  console.log('🔍 IDs das categorias selecionadas:', idsCategoria);
  
  this.produtoService.listarTodos().subscribe({
    next: (produtos: any[]) => {
      console.log('📦 Total de produtos retornados:', produtos.length);
      console.log('📦 Primeiro produto (exemplo):', produtos[0]);
      
      // Filtrar por categorias selecionadas
      const produtosFiltrados = produtos
        .filter(p => {
          console.log(`Produto: ${p.nomeProduto} | categoriaId: ${p.categoriaId} | categoria.id: ${p.categoria?.id}`);
          return idsCategoria.includes(p.categoriaId) || idsCategoria.includes(p.categoria?.id);
        })
        .sort((a, b) => a.nomeProduto.localeCompare(b.nomeProduto));

      console.log('📦 Produtos filtrados:', produtosFiltrados.length);

      if (produtosFiltrados.length === 0) {
        alert('⚠️ Nenhum produto encontrado nas categorias selecionadas');
        this.imprimindo = false;
        return;
      }

      this.gerarImpressao(produtosFiltrados);
      this.imprimindo = false;
    },
    error: (error: any) => {
      console.error('❌ Erro ao buscar produtos:', error);
      alert('Erro ao buscar produtos');
      this.imprimindo = false;
    }
  });
}

  gerarImpressao(produtos: any[]): void {
    const agora = new Date();
    const dataFormatada = agora.toLocaleDateString('pt-BR');
    const horaFormatada = agora.toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit'
    });

    let linhasProdutos = '';
    
    produtos.forEach((produto) => {
      linhasProdutos += `
        <div class="produto-linha">
          <div class="produto-nome">${produto.nomeProduto}</div>
           <div class="produto-qtd">${produto.quantidade || 0}</div>
        </div>
      `;
    });

    const htmlImpressao = `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
        <title>Contagem de Estoque - Hotel Di Van</title>
        <style>
          @page { 
            size: 80mm auto; 
            margin: 0; 
          }
          
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
            margin-bottom: 3px;
            text-transform: uppercase;
          }
          
          .subtitulo {
            font-size: 11px;
            text-align: center;
            margin-bottom: 2px;
          }
          
          .info { 
            font-size: 9px; 
            margin: 2px 0;
            text-align: center;
          }
          
          .separador { 
            text-align: center; 
            margin: 5px 0; 
            font-size: 10px; 
          }
          
          .secao-titulo {
            font-size: 11px;
            font-weight: bold;
            margin: 8px 0 5px 0;
            padding-bottom: 2px;
            border-bottom: 1px dashed #000;
            text-align: center;
          }
          
          .produto-linha {
            display: flex;
            justify-content: space-between;
            padding: 4px 0;
            border-bottom: 1px dotted #ccc;
            font-size: 10px;
          }
          
          .produto-nome {
            flex: 1;
            font-weight: 600;
            padding-right: 10px;
          }
          
          .produto-qtd {
            min-width: 40px;
            text-align: right;
            font-weight: bold;
            border-bottom: 1px solid #000;
          }
          
          .rodape {
            margin-top: 10px;
            text-align: center;
            font-size: 8px;
            color: #666;
          }
          
          .total-produtos {
            text-align: center;
            font-size: 10px;
            margin: 8px 0;
            font-weight: bold;
          }
        </style>
      </head>
      <body>
        <div class="titulo">HOTEL DI VAN</div>
        <div class="subtitulo">Contagem de Estoque</div>
        <div class="subtitulo">para Conferência</div>
        
        <div class="separador">================================</div>
        
        <div class="info">Data: ${dataFormatada}</div>
        <div class="info">Hora: ${horaFormatada}</div>
        
        <div class="separador">================================</div>
        
        <div class="secao-titulo">PRODUTOS</div>
        
        ${linhasProdutos}
        
        <div class="separador">================================</div>
        
        <div class="total-produtos">
          Total de itens: ${produtos.length}
        </div>
        
        <div class="separador">================================</div>
        
        <div class="rodape">
          Conferido por: ___________________<br>
          Assinatura: ______________________
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

  voltar(): void {
    window.history.back();
  }
}