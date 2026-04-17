import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FornecedorService, Fornecedor } from '../../services/fornecedor.service';

@Component({
  selector: 'app-fornecedor-lista',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>🏢 Fornecedores</h1>
        <div class="header-acoes">
          <button class="btn-voltar" (click)="router.navigate(['/contas-pagar'])">← Contas a Pagar</button>
          <button class="btn-novo" (click)="router.navigate(['/fornecedores/novo'])">+ Novo Fornecedor</button>
        </div>
      </div>

      <div class="search-box">
        <input type="text" placeholder="Buscar fornecedor..."
               [(ngModel)]="filtro" (input)="filtrar()" />
      </div>

      <div *ngIf="loading" class="loading">Carregando...</div>
      <div *ngIf="!loading && fornecedoresFiltrados.length === 0" class="empty">
        Nenhum fornecedor encontrado
      </div>

      <div class="table-container" *ngIf="!loading && fornecedoresFiltrados.length > 0">
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>CNPJ</th>
              <th>Telefone</th>
              <th>Email</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let f of fornecedoresFiltrados">
              <td>{{ f.nome }}</td>
              <td>{{ f.cnpj || '-' }}</td>
              <td>{{ f.telefone || '-' }}</td>
              <td>{{ f.email || '-' }}</td>
              <td>
                <span [class]="f.ativo ? 'badge-ativo' : 'badge-inativo'">
                  {{ f.ativo ? 'Ativo' : 'Inativo' }}
                </span>
              </td>
              <td>
                <button class="btn-edit" (click)="router.navigate(['/fornecedores/editar', f.id])">✏️ Editar</button>
                <button class="btn-inativar" (click)="inativar(f)" *ngIf="f.ativo">🚫 Inativar</button>
                <button class="btn-delete" (click)="excluir(f.id!)">🗑️</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 1200px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h1 { color: #333; margin: 0; }
    .header-acoes { display: flex; gap: 10px; }
    .btn-novo { background: #667eea; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
    .btn-voltar { background: #6c757d; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
    .search-box { margin-bottom: 20px; }
    .search-box input { width: 400px; padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; }
    .loading, .empty { text-align: center; padding: 40px; color: #666; }
    .table-container { background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; }
    th { background: #f8f9fa; padding: 12px; text-align: left; font-weight: 600; border-bottom: 2px solid #dee2e6; }
    td { padding: 12px; border-bottom: 1px solid #dee2e6; }
    tr:hover { background: #f8f9fa; }
    .badge-ativo { background: #d4edda; color: #155724; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
    .badge-inativo { background: #f8d7da; color: #721c24; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
    .btn-edit { background: #3498db; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; font-size: 12px; margin-right: 4px; }
    .btn-inativar { background: #e67e22; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; font-size: 12px; margin-right: 4px; }
    .btn-delete { background: #e74c3c; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; font-size: 12px; }
  `]
})
export class FornecedorListaApp implements OnInit {
  router = inject(Router);
  private service = inject(FornecedorService);

  fornecedores: Fornecedor[] = [];
  fornecedoresFiltrados: Fornecedor[] = [];
  filtro = '';
  loading = false;

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading = true;
    this.service.listarTodos().subscribe({
      next: (data) => {
        this.fornecedores = data.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
        this.fornecedoresFiltrados = [...this.fornecedores];
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  filtrar(): void {
    const termo = this.filtro.toLowerCase();
    this.fornecedoresFiltrados = this.fornecedores.filter(f =>
      f.nome.toLowerCase().includes(termo) ||
      (f.cnpj || '').includes(termo)
    );
  }

  inativar(f: Fornecedor): void {
    if (!confirm(`Inativar fornecedor ${f.nome}?`)) return;
    this.service.inativar(f.id!).subscribe({
      next: () => this.carregar(),
      error: (e) => alert('Erro: ' + e.error?.erro)
    });
  }

  excluir(id: number): void {
    if (!confirm('Excluir este fornecedor?')) return;
    this.service.deletar(id).subscribe({
      next: () => this.carregar(),
      error: (e) => alert('Erro: ' + e.error?.erro)
    });
  }
}