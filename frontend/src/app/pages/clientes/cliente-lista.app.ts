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
        <button class="btn-primary" (click)="novo()">+ Novo Cliente</button>
      </div>

     <div class="search-box">
  <input 
    type="text" 
    placeholder="Buscar cliente por nome ou CPF..."
    [(ngModel)]="filtro"
    (input)="onFiltrar()"
  />
  <select [(ngModel)]="filtroEmpresa" (change)="filtrarPorEmpresa()" class="filtro-select">
    <option value="">Todas as empresas</option>
    <option value="SEM_EMPRESA">Sem empresa</option>
    <option *ngFor="let emp of empresas" [value]="emp.id">{{ emp.nomeEmpresa }}</option>
  </select>
  <span class="total-info">
    Total: {{ totalElementos }} clientes
  </span>
</div>

      <div *ngIf="loading" class="loading">Carregando...</div>

      <div *ngIf="!loading && clientesFiltrados.length === 0" class="empty">
        Nenhum cliente encontrado
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
    <td>{{ cliente.nome }}</td>
    <td>{{ cliente.cpf || '-' }}</td>
    <td>{{ cliente.ddi || '55' }} {{ cliente.celular || '-' }}</td>
    <td *ngIf="cliente.celular2">{{ cliente.ddi2 || '55' }} {{ cliente.celular2 }}</td>
    <td *ngIf="!cliente.celular2">-</td>
    <td>{{ cliente.empresaNome || cliente.empresa?.nomeEmpresa || '-' }}</td>
    <td>
      <button class="btn-edit" (click)="editar(cliente.id!)">Editar</button>
      <button class="btn-delete" (click)="excluir(cliente.id!)">Excluir</button>
    </td>
  </tr>
</tbody>
        </table>
      </div>

      <!-- PAGINAÇÃO -->
      <div class="paginacao" *ngIf="!loading && !filtro && totalPaginas > 1">
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
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h1 { color: #333; margin: 0; }

    .btn-primary { background: #667eea; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
    .btn-primary:hover { background: #5568d3; }

    .search-box { 
      display: flex; 
      align-items: center; 
      gap: 15px;
      margin-bottom: 20px; 
    }
    .search-box input { 
      width: 400px;
      padding: 10px; 
      border: 1px solid #ddd; 
      border-radius: 5px; 
      font-size: 14px; 
    }

    .filtro-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  min-width: 200px;
}

    .total-info { color: #666; font-size: 0.9em; }

    .loading, .empty { text-align: center; padding: 40px; color: #666; }

    .table-container { 
      background: white; 
      border-radius: 8px; 
      box-shadow: 0 2px 4px rgba(0,0,0,0.1); 
      overflow-x: auto; 
    }
    table { width: 100%; border-collapse: collapse; }
    th { background: #f8f9fa; padding: 12px; text-align: left; font-weight: 600; border-bottom: 2px solid #dee2e6; }
    td { padding: 12px; border-bottom: 1px solid #dee2e6; }
    tr:hover { background: #f8f9fa; }

    .btn-edit, .btn-delete { padding: 6px 12px; border: none; border-radius: 4px; cursor: pointer; font-size: 12px; margin-right: 5px; }
    .btn-edit { background: #28a745; color: white; }
    .btn-edit:hover { background: #218838; }
    .btn-delete { background: #dc3545; color: white; }
    .btn-delete:hover { background: #c82333; }

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
  `]
})
export class ClienteListaApp implements OnInit {
  private clienteService = inject(ClienteService);
  private router = inject(Router);
  private http = inject(HttpClient);

  clientes: Cliente[] = [];
  clientesFiltrados: Cliente[] = [];
  filtro = '';
  loading = false;

  paginaAtual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  tamanhoPagina = 50;

  filtroEmpresa = '';
  empresas: any[] = [];

  ngOnInit(): void {
  this.carregarPagina(0);
  this.http.get<any[]>('/api/empresas').subscribe({
    next: (data) => this.empresas = data.sort((a, b) => a.nomeEmpresa.localeCompare(b.nomeEmpresa, 'pt-BR')),
    error: () => {}
  });
}

  carregarPagina(pagina: number): void {
    this.loading = true;
    this.http.get<any>(`/api/clientes?page=${pagina}&size=${this.tamanhoPagina}`).subscribe({
      next: (data) => {
        this.clientes = data.clientes.sort((a: Cliente, b: Cliente) => 
          a.nome.localeCompare(b.nome, 'pt-BR'));
        this.clientesFiltrados = [...this.clientes];
        this.paginaAtual = data.paginaAtual;
        this.totalPaginas = data.totalPaginas;
        this.totalElementos = data.totalElementos;
        this.loading = false;
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

  onFiltrar(): void {
  const termo = this.filtro.toLowerCase().trim();
  
  if (!termo) {
    this.clientesFiltrados = [...this.clientes];
    return;
  }

  if (termo.length < 2) return;

  // Remove formatação do CPF para busca
  const termoBusca = termo.replace(/[.\-\/]/g, '');

  this.loading = true;
  this.http.get<any[]>(`/api/clientes/buscar?termo=${termoBusca}`).subscribe({
    next: (data) => {
      this.clientesFiltrados = data;
      this.loading = false;
    },
    error: () => this.loading = false
  });
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

 filtrarPorEmpresa(): void {
  if (!this.filtroEmpresa) {
    this.carregarPagina(0);
    return;
  }
  if (this.filtroEmpresa === 'SEM_EMPRESA') {
    this.http.get<any[]>('/api/clientes/buscar?termo=*').subscribe({
      next: (data) => {
        this.clientesFiltrados = data.filter(c => !c.empresaNome);
        this.loading = false;
      },
      error: () => this.loading = false
    });
    return;
  }
  // ✅ AQUI — substitua o subscribe existente por este:
  this.http.get<any[]>(`/api/clientes/empresa/${this.filtroEmpresa}`).subscribe({
    next: (data) => {
      this.clientesFiltrados = data.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
      this.loading = false;
    },
    error: () => this.loading = false
  });
}
}