import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ClienteService } from '../../services/cliente.service';
import { Cliente } from '../../models/cliente.model';

@Component({

  selector: 'app-cliente-lista',

  standalone: true,

  imports: [CommonModule, FormsModule],

  template: `

    <div class="container">

      <div class="header">

        <h1>Clientes</h1>

        <div class="header-actions">

          <button class="btn-ranking" (click)="verRanking()">🏆 Ranking</button>

          <button class="btn-primary" (click)="novo()">+ Novo Cliente</button>

        </div>

      </div>


      <!-- BARRA DE PESQUISA: 3 CAMPOS SEPARADOS -->

      <div class="search-box">

        <div class="search-field">

          <label>🔍 Nome</label>

          <input

            type="text"

            placeholder="Buscar por nome..."

            [(ngModel)]="filtroNome"

            (input)="filtrar()"

            (keyup.enter)="filtrar()"

            class="input-search"

          />

        </div>


        <div class="search-field">

          <label>🔍 CPF</label>

          <input

            type="text"

            placeholder="000.000.000-00"

            [(ngModel)]="filtroCpf"

            (input)="onCpfInput()"

            (keyup.enter)="filtrar()"

            maxlength="14"

            class="input-search"

          />

        </div>


        <div class="search-field">

          <label>🔍 Empresa</label>

          <input

            type="text"

            placeholder="Buscar por empresa..."

            [(ngModel)]="filtroEmpresa"

            (input)="filtrar()"

            (keyup.enter)="filtrar()"

            class="input-search"

          />

        </div>


        <button

          class="btn-clear"

          (click)="limparFiltros()"

          *ngIf="temFiltroAtivo()"

          type="button">

          ✕ Limpar

        </button>


        <span class="total-info">

          {{ clientesFiltrados.length }} / {{ totalElementos || clientes.length }} cliente(s)

          <span *ngIf="temFiltroAtivo()" class="filter-tag">filtros ativos</span>

        </span>

      </div>


      <div *ngIf="loading" class="loading">Carregando...</div>


      <div *ngIf="!loading && clientesFiltrados.length === 0" class="empty">

        <div class="empty-icon">🔍</div>

        <p *ngIf="temFiltroAtivo()">Nenhum cliente encontrado com esses filtros.</p>

        <p *ngIf="!temFiltroAtivo()">Nenhum cliente cadastrado.</p>

        <button

          *ngIf="temFiltroAtivo()"

          class="link-button"

          (click)="limparFiltros()">

          Limpar filtros

        </button>

      </div>


      <div class="table-container" *ngIf="!loading && clientesFiltrados.length > 0">

        <table>

          <thead>

            <tr>

              <th>Nome</th>

              <th>CPF</th>

              <th>Celular</th>

              <th>Celular 2</th>

              <th>Empresa</th>

              <th>Ações</th>

            </tr>

          </thead>

          <tbody>

            <tr *ngFor="let cliente of clientesFiltrados">

              <td>

                <strong>{{ cliente.nome }}</strong>

                <small *ngIf="cliente.menorDeIdade" class="badge-menor" title="Menor de idade"> 👶</small>

              </td>

              <td class="col-cpf">{{ cliente.cpf || '-' }}</td>

              <td>{{ cliente.ddi || '55' }} {{ cliente.celular || '-' }}</td>

              <td *ngIf="cliente.celular2">{{ cliente.ddi2 || '55' }} {{ cliente.celular2 }}</td>

              <td *ngIf="!cliente.celular2">-</td>

              <td>{{ cliente.empresa?.nomeEmpresa || '-' }}</td>
              <td>

                <button class="btn-historico" (click)="verHistorico(cliente.id!)">📋 Histórico</button>

                <button class="btn-edit" (click)="editar(cliente.id!)">Editar</button>

                <button class="btn-delete" (click)="excluir(cliente.id!)">Excluir</button>

              </td>

            </tr>

          </tbody>

        </table>

      </div>


      <!-- PAGINAÇÃO: oculta quando há filtros ativos -->

      <div class="paginacao" *ngIf="!loading && !temFiltroAtivo() && totalPaginas > 1">

        <button

          class="btn-pag"

          (click)="irParaPagina(paginaAtual - 1)"

          [disabled]="paginaAtual === 0">

          ← Anterior

        </button>

        <span class="pag-info">

          Página {{ paginaAtual + 1 }} de {{ totalPaginas }}

        </span>

        <button

          class="btn-pag"

          (click)="irParaPagina(paginaAtual + 1)"

          [disabled]="paginaAtual >= totalPaginas - 1">

          Próxima →

        </button>

      </div>

    </div>

  `,

  styles: [`

    .container { padding: 20px; max-width: 1400px; margin: 0 auto; }

    .header {

      display: flex;

      justify-content: space-between;

      align-items: center;

      margin-bottom: 20px;

      flex-wrap: wrap;

      gap: 10px;

    }

    h1 { color: #333; margin: 0; }


    .header-actions { display: flex; gap: 10px; }

    .btn-primary { background: #667eea; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

    .btn-primary:hover { background: #5568d3; }

    .btn-ranking { background: #f39c12; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }

    .btn-ranking:hover { background: #e67e22; }


    /* SEARCH BOX */

    .search-box {

      display: grid;

      grid-template-columns: 1fr 1fr 1fr auto auto;

      gap: 12px;

      align-items: end;

      margin-bottom: 20px;

      background: white;

      padding: 15px;

      border-radius: 8px;

      box-shadow: 0 2px 4px rgba(0,0,0,0.08);

      border-left: 4px solid #667eea;

    }

    .search-field { display: flex; flex-direction: column; min-width: 0; }

    .search-field label {

      font-size: 12px;

      color: #666;

      margin-bottom: 4px;

      font-weight: 500;

    }

    .input-search {

      padding: 9px 12px;

      border: 1px solid #ddd;

      border-radius: 5px;

      font-size: 14px;

      transition: all 0.15s;

    }

    .input-search:focus {

      outline: none;

      border-color: #667eea;

      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);

    }

    .btn-clear {

      background: #dc3545;

      color: white;

      border: none;

      padding: 9px 14px;

      border-radius: 5px;

      cursor: pointer;

      font-size: 14px;

      white-space: nowrap;

      height: 38px;

    }

    .btn-clear:hover { background: #c82333; }

    .total-info {

      color: #666;

      font-size: 13px;

      white-space: nowrap;

      padding-bottom: 8px;

    }

    .filter-tag {

      background: #fff3cd;

      color: #856404;

      padding: 2px 8px;

      border-radius: 10px;

      font-size: 11px;

      margin-left: 6px;

    }


    .loading, .empty { text-align: center; padding: 40px; color: #666; }

    .empty-icon { font-size: 48px; margin-bottom: 12px; }

    .link-button {

      background: none;

      border: none;

      color: #667eea;

      cursor: pointer;

      text-decoration: underline;

      padding: 6px 12px;

      font-size: 14px;

    }

    .link-button:hover { color: #5568d3; }


    .table-container {

      background: white;

      border-radius: 8px;

      box-shadow: 0 2px 4px rgba(0,0,0,0.1);

      overflow-x: auto;

    }

    table { width: 100%; border-collapse: collapse; }

    th {

      background: #f8f9fa;

      padding: 12px;

      text-align: left;

      font-weight: 600;

      border-bottom: 2px solid #dee2e6;

      font-size: 13px;

    }

    td { padding: 12px; border-bottom: 1px solid #dee2e6; font-size: 14px; }

    tr:hover { background: #f8f9fa; }

    .col-cpf { font-family: 'Courier New', monospace; }

    .badge-menor { color: #c2185b; }


    .btn-edit, .btn-delete, .btn-historico {

      padding: 6px 12px;

      border: none;

      border-radius: 4px;

      cursor: pointer;

      font-size: 12px;

      margin-right: 5px;

    }

    .btn-edit { background: #28a745; color: white; }

    .btn-edit:hover { background: #218838; }

    .btn-delete { background: #dc3545; color: white; }

    .btn-delete:hover { background: #c82333; }

    .btn-historico { background: #3498db; color: white; }

    .btn-historico:hover { background: #2980b9; }


    .paginacao {

      display: flex;

      justify-content: center;

      align-items: center;

      gap: 20px;

      margin-top: 20px;

      padding: 10px;

    }

    .btn-pag {

      background: #667eea;

      color: white;

      border: none;

      padding: 8px 20px;

      border-radius: 5px;

      cursor: pointer;

      font-size: 14px;

    }

    .btn-pag:disabled { background: #ccc; cursor: not-allowed; }

    .btn-pag:hover:not(:disabled) { background: #5568d3; }

    .pag-info { color: #555; font-weight: 600; }


    @media (max-width: 900px) {

      .search-box { grid-template-columns: 1fr; }

    }

  `]

})

export class ClienteListaApp implements OnInit {

  private clienteService = inject(ClienteService);

  private router = inject(Router);

  private http = inject(HttpClient);


  clientes: Cliente[] = [];

  clientesFiltrados: Cliente[] = [];

  loading = false;


  // Paginação

  paginaAtual = 0;

  totalPaginas = 0;

  totalElementos = 0;

  tamanhoPagina = 50;


  // Filtros de pesquisa

  filtroNome = '';

  filtroCpf = '';

  filtroEmpresa = '';


  ngOnInit(): void {

    this.carregarPagina(0);

  }


  carregarPagina(pagina: number): void {

    this.loading = true;

    this.http.get<any>(`/api/clientes?page=${pagina}&size=${this.tamanhoPagina}`).subscribe({

      next: (data) => {

        this.clientes = data.clientes

          .sort((a: Cliente, b: Cliente) => a.nome.localeCompare(b.nome, 'pt-BR'))

          .map((c: any) => ({ ...c, cpf: this.formatarCpf(c.cpf) }));

        this.clientesFiltrados = [...this.clientes];

        this.paginaAtual = data.paginaAtual;

        this.totalPaginas = data.totalPaginas;

        this.totalElementos = data.totalElementos;

        this.loading = false;


        // Reaplica filtros se houver algum ativo

        if (this.temFiltroAtivo()) this.filtrar();

      },

      error: (err) => {

        console.error('Erro ao carregar clientes', err);

        this.loading = false;

      }

    });

  }


  irParaPagina(pagina: number): void {

    if (pagina >= 0 && pagina < this.totalPaginas) {

      this.carregarPagina(pagina);

    }

  }


  /**

   * Filtra a lista carregada por nome, CPF e/ou empresa.

   * Combinação: TODOS os critérios precisam casar (AND).

   * - Nome e empresa: case-insensitive, parcial

   * - CPF: ignora máscara, busca parcial nos números

   */

  filtrar(): void {

    const nome = this.filtroNome.toLowerCase().trim();

    const cpfFiltro = this.filtroCpf.replace(/\D/g, '');

    const empresa = this.filtroEmpresa.toLowerCase().trim();


    // Sem filtros: mostra a página inteira

    if (!nome && !cpfFiltro && !empresa) {

      this.clientesFiltrados = [...this.clientes];

      return;

    }


    this.clientesFiltrados = this.clientes.filter((c) => {

      const matchNome = !nome || (c.nome || '').toLowerCase().includes(nome);


      const cpfCliente = (c.cpf || '').replace(/\D/g, '');

      const matchCpf = !cpfFiltro || cpfCliente.includes(cpfFiltro);


      const matchEmpresa =

  !empresa ||

  (c.empresa?.nomeEmpresa || '')

    .toLowerCase()

    .includes(empresa);


      return matchNome && matchCpf && matchEmpresa;

    });

  }


  /**

   * Aplica a máscara 000.000.000-00 no campo de CPF do filtro.

   */

  onCpfInput(): void {

    if (!this.filtroCpf) {

      this.filtrar();

      return;

    }

    let numeros = this.filtroCpf.replace(/\D/g, '');

    if (numeros.length > 11) numeros = numeros.substring(0, 11);


    let cpf = numeros;

    if (numeros.length > 9) {

      cpf =

        numeros.substring(0, 3) + '.' +

        numeros.substring(3, 6) + '.' +

        numeros.substring(6, 9) + '-' +

        numeros.substring(9);

    } else if (numeros.length > 6) {

      cpf =

        numeros.substring(0, 3) + '.' +

        numeros.substring(3, 6) + '.' +

        numeros.substring(6);

    } else if (numeros.length > 3) {

      cpf = numeros.substring(0, 3) + '.' + numeros.substring(3);

    }

    this.filtroCpf = cpf;

    this.filtrar();

  }


  temFiltroAtivo(): boolean {

    return !!(this.filtroNome || this.filtroCpf || this.filtroEmpresa);

  }


  limparFiltros(): void {

    this.filtroNome = '';

    this.filtroCpf = '';

    this.filtroEmpresa = '';

    this.clientesFiltrados = [...this.clientes];

  }


  novo(): void { this.router.navigate(['/clientes/novo']); }

  editar(id: number): void { this.router.navigate(['/clientes/editar', id]); }


  excluir(id: number): void {

    if (confirm('Deseja realmente excluir este cliente?')) {

      this.clienteService.delete(id).subscribe({

        next: () => this.carregarPagina(this.paginaAtual),

        error: (err) => alert(err.error?.erro || 'Erro ao excluir cliente')

      });

    }

  }


  formatarCpf(cpf: string | null | undefined): string {

    if (!cpf) return '';

    const numeros = cpf.replace(/\D/g, '');

    if (numeros.length !== 11) return cpf;

    return numeros.substring(0, 3) + '.' +

           numeros.substring(3, 6) + '.' +

           numeros.substring(6, 9) + '-' +

           numeros.substring(9);

  }


  verHistorico(id: number): void {

    this.router.navigate(['/clientes', id, 'historico']);

  }


  verRanking(): void {

    this.router.navigate(['/clientes/ranking']);

  }

}

