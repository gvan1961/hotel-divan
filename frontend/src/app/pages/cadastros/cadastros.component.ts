import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

interface CardCadastro {
  titulo: string;
  icone: string;
  descricao: string;
  rota: string;
  cor: string;
}

@Component({
  selector: 'app-cadastros',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container-cadastros">
      
      <!-- HEADER -->
      <div class="header">
        <div class="header-content">
          <h1>📋 Cadastros do Sistema</h1>
          <p class="subtitulo">Gerencie todos os cadastros em um só lugar</p>
        </div>
        <button class="btn-voltar" (click)="voltar()">
          ← Voltar ao Dashboard
        </button>
      </div>

      <!-- GRID DE CARDS -->
      <div class="grid-cadastros">
        <div 
          *ngFor="let card of cadastros" 
          class="card-cadastro"
          [style.border-left-color]="card.cor"
          (click)="navegar(card.rota)">
          
          <div class="card-icone" [style.background]="card.cor">
            {{ card.icone }}
          </div>
          
          <div class="card-conteudo">
            <h3 class="card-titulo">{{ card.titulo }}</h3>
            <p class="card-descricao">{{ card.descricao }}</p>
          </div>
          
          <div class="card-seta">→</div>
        </div>
      </div>

      <!-- RODAPÉ INFORMATIVO -->
      <div class="rodape-info">
        <div class="info-box">
          <span class="info-icone">💡</span>
          <span class="info-texto">
            Clique em qualquer card para acessar o cadastro correspondente
          </span>
        </div>
      </div>

    </div>
  `,
  styles: [`
    .container-cadastros {
      max-width: 1400px;
      margin: 0 auto;
      padding: 30px;
      min-height: 100vh;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    }

    /* ========================================== */
    /* HEADER */
    /* ========================================== */
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 40px;
      padding: 30px;
      background: white;
      border-radius: 15px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.1);
    }

    .header-content h1 {
      margin: 0 0 10px 0;
      font-size: 2.5em;
      color: #2c3e50;
      font-weight: 800;
    }

    .subtitulo {
      margin: 0;
      color: #7f8c8d;
      font-size: 1.1em;
    }

    .btn-voltar {
      padding: 12px 24px;
      background: linear-gradient(135deg, #6c757d 0%, #495057 100%);
      color: white;
      border: none;
      border-radius: 8px;
      font-weight: 700;
      cursor: pointer;
      transition: all 0.3s;
      box-shadow: 0 4px 15px rgba(108, 117, 125, 0.3);
    }

    .btn-voltar:hover {
      transform: translateY(-3px);
      box-shadow: 0 6px 20px rgba(108, 117, 125, 0.4);
    }

    /* ========================================== */
    /* GRID DE CARDS */
    /* ========================================== */
    .grid-cadastros {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
      gap: 25px;
      margin-bottom: 40px;
    }

    .card-cadastro {
      display: flex;
      align-items: center;
      gap: 20px;
      padding: 30px;
      background: white;
      border-radius: 15px;
      border-left: 6px solid;
      cursor: pointer;
      transition: all 0.3s ease;
      box-shadow: 0 4px 15px rgba(0,0,0,0.08);
      position: relative;
      overflow: hidden;
    }

    .card-cadastro::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 0;
      height: 100%;
      background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.3) 100%);
      transition: width 0.4s ease;
    }

    .card-cadastro:hover {
      transform: translateY(-8px) translateX(5px);
      box-shadow: 0 12px 35px rgba(0,0,0,0.15);
    }

    .card-cadastro:hover::before {
      width: 100%;
    }

    .card-icone {
      width: 80px;
      height: 80px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 40px;
      border-radius: 15px;
      flex-shrink: 0;
      box-shadow: 0 4px 15px rgba(0,0,0,0.15);
      transition: all 0.3s;
    }

    .card-cadastro:hover .card-icone {
      transform: scale(1.1) rotate(5deg);
    }

    .card-conteudo {
      flex: 1;
    }

    .card-titulo {
      margin: 0 0 8px 0;
      font-size: 1.5em;
      font-weight: 700;
      color: #2c3e50;
    }

    .card-descricao {
      margin: 0;
      color: #7f8c8d;
      font-size: 1em;
      line-height: 1.5;
    }

    .card-seta {
      font-size: 2em;
      color: #bdc3c7;
      transition: all 0.3s;
    }

    .card-cadastro:hover .card-seta {
      color: #3498db;
      transform: translateX(10px);
    }

    /* ========================================== */
    /* RODAPÉ INFORMATIVO */
    /* ========================================== */
    .rodape-info {
      margin-top: 40px;
    }

    .info-box {
      display: flex;
      align-items: center;
      gap: 15px;
      padding: 20px 30px;
      background: white;
      border-radius: 12px;
      border-left: 4px solid #3498db;
      box-shadow: 0 2px 10px rgba(0,0,0,0.08);
    }

    .info-icone {
      font-size: 2em;
    }

    .info-texto {
      color: #7f8c8d;
      font-size: 1em;
    }

    /* ========================================== */
    /* RESPONSIVO */
    /* ========================================== */
    @media (max-width: 768px) {
      .container-cadastros {
        padding: 15px;
      }

      .header {
        flex-direction: column;
        gap: 20px;
        padding: 20px;
      }

      .header-content h1 {
        font-size: 1.8em;
      }

      .grid-cadastros {
        grid-template-columns: 1fr;
        gap: 15px;
      }

      .card-cadastro {
        padding: 20px;
      }

      .card-icone {
        width: 60px;
        height: 60px;
        font-size: 30px;
      }

      .card-titulo {
        font-size: 1.2em;
      }

      .card-descricao {
        font-size: 0.9em;
      }
    }
  `]
})
export class CadastrosComponent {
  
  cadastros: CardCadastro[] = [
    {
      titulo: 'Categorias',
      icone: '📦',
      descricao: 'Gerencie as categorias de produtos do sistema',
      rota: '/categorias',
      cor: '#667eea'
    },
    {
      titulo: 'Produtos',
      icone: '🛒',
      descricao: 'Cadastre e controle o estoque de produtos',
      rota: '/produtos',
      cor: '#28a745'
    },
    {
      titulo: 'Tipos de Apartamento',
      icone: '🏷️',
      descricao: 'Configure os tipos de acomodações disponíveis',
      rota: '/tipos-apartamento',
      cor: '#ffc107'
    },
    {
      titulo: 'Diárias',
      icone: '💵',
      descricao: 'Defina valores e regras das diárias',
      rota: '/diarias',
      cor: '#17a2b8'
    },
    
    {
      titulo: 'Empresas',
      icone: '🏢',
      descricao: 'Cadastre empresas parceiras e corporativas',
      rota: '/empresas',
      cor: '#6f42c1'
    }
  ];

  constructor(private router: Router) {}

  navegar(rota: string): void {
    this.router.navigate([rota]);
  }

  voltar(): void {
    this.router.navigate(['/dashboard']);
  }
}