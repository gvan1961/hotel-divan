import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FaixaConsumoAguaService } from '../../services/faixa-consumo-agua.service';
import { FaixaConsumoAgua } from '../../models/faixa-consumo-agua.model';

@Component({
  selector: 'app-faixa-consumo-agua-lista',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <div class="header-left">
          <button class="btn-voltar" (click)="voltar()">← Voltar</button>
          <h1>Faixas de Consumo de Água</h1>
        </div>
        <button class="btn-primary" (click)="novo()">+ Nova Faixa</button>
      </div>

      <div class="search-box">
        <input
          type="text"
          placeholder="Buscar por empresa..."
          [(ngModel)]="filtro"
          (input)="filtrar()"
        />
      </div>

      <div *ngIf="loading" class="loading">Carregando...</div>
      <div *ngIf="!loading && faixasFiltradas.length === 0" class="empty">
        Nenhuma faixa cadastrada
      </div>

      <div class="table-container" *ngIf="!loading && faixasFiltradas.length > 0">
        <table>
          <thead>
            <tr>
              <th>Empresa</th>
              <th>Qtd. Hóspedes</th>
              <th>Valor Limite Diário</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let faixa of faixasFiltradas">
              <td>{{ faixa.empresaNome }}</td>
              <td>{{ faixa.qtdHospedes }}</td>
              <td>{{ faixa.valorLimiteDiario | currency:'BRL' }}</td>
              <td>
                <button class="btn-edit" (click)="editar(faixa.id!)">Editar</button>
                <button class="btn-delete" (click)="excluir(faixa.id!)">Excluir</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 1000px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .header-left { display: flex; align-items: center; gap: 15px; }
    h1 { color: #333; margin: 0; }
    .btn-voltar { background: #6c757d; color: white; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }
    .btn-primary { background: #667eea; color: white; border: none; padding: 10px 18px; border-radius: 5px; cursor: pointer; }
    .search-box { margin-bottom: 15px; }
    .search-box input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; box-sizing: border-box; }
    .loading, .empty { text-align: center; padding: 40px; color: #888; }
    .table-container { background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
    table { width: 100%; border-collapse: collapse; }
    th { text-align: left; background: #f5f5f5; padding: 12px 16px; font-weight: 600; color: #555; }
    td { padding: 12px 16px; border-top: 1px solid #eee; }
    .btn-edit, .btn-delete { padding: 6px 12px; border: none; border-radius: 4px; cursor: pointer; margin-right: 6px; font-size: 0.85rem; }
    .btn-edit { background: #ffc107; color: #333; }
    .btn-delete { background: #dc3545; color: white; }
  `]
})
export class FaixaConsumoAguaListaApp implements OnInit {
  private faixaService = inject(FaixaConsumoAguaService);
  private router = inject(Router);

  faixas: FaixaConsumoAgua[] = [];
  faixasFiltradas: FaixaConsumoAgua[] = [];
  filtro = '';
  loading = true;

  ngOnInit(): void {
    this.carregarFaixas();
  }

  carregarFaixas(): void {
    this.faixaService.getAll().subscribe({
      next: (data) => {
        this.faixas = data;
        this.faixasFiltradas = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar faixas', err);
        this.loading = false;
      }
    });
  }

  filtrar(): void {
    const termo = this.filtro.toLowerCase();
    this.faixasFiltradas = this.faixas.filter(f =>
      (f.empresaNome || '').toLowerCase().includes(termo)
    );
  }

  novo(): void {
    this.router.navigate(['/faixas-consumo-agua/novo']);
  }

  editar(id: number): void {
    this.router.navigate(['/faixas-consumo-agua/editar', id]);
  }

  voltar(): void {
    this.router.navigate(['/cadastros']);
  }

  excluir(id: number): void {
    if (confirm('Deseja realmente excluir esta faixa?')) {
      this.faixaService.delete(id).subscribe({
        next: () => this.carregarFaixas(),
        error: (err) => {
          console.error('Erro ao excluir faixa', err);
          alert(err.error?.message || err.error || 'Erro ao excluir faixa');
        }
      });
    }
  }
}