import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MovimentacaoEstoque {
  id?: number;
  produto?: any;
  tipo: string;
  quantidadeAnterior?: number;
  quantidadeMovimentada: number;
  quantidadeNova?: number;
  valorUnitario?: number;
  motivo?: string;
  fornecedor?: any;
  usuario?: any;
  criadoEm?: string;
}

@Injectable({ providedIn: 'root' })
export class MovimentacaoEstoqueService {
  private http = inject(HttpClient);
  private apiUrl = '/api/estoque/movimentacoes';

  listarTodas(): Observable<MovimentacaoEstoque[]> {
    return this.http.get<MovimentacaoEstoque[]>(this.apiUrl);
  }

  listarPorProduto(produtoId: number): Observable<MovimentacaoEstoque[]> {
    return this.http.get<MovimentacaoEstoque[]>(`${this.apiUrl}/produto/${produtoId}`);
  }

  registrarEntrada(dados: {
    produtoId: number;
    quantidade: number;
    valorUnitario?: number;
    fornecedorId?: number;
    motivo?: string;
  }): Observable<MovimentacaoEstoque> {
    return this.http.post<MovimentacaoEstoque>(`${this.apiUrl}/entrada`, dados);
  }

  registrarAcerto(dados: {
    produtoId: number;
    quantidadeReal: number;
    motivo: string;
  }): Observable<MovimentacaoEstoque> {
    return this.http.post<MovimentacaoEstoque>(`${this.apiUrl}/acerto`, dados);
  }
}