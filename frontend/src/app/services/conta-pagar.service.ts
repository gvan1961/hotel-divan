import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ContaPagar {
  id?: number;
  descricao: string;
  valor: number;
  valorPago?: number;
  saldo?: number;
  dataVencimento: string;
  dataPagamento?: string;
  status?: string;
  categoria?: string;
  codigoBarras?: string;
  formaPagamento?: string;
  observacao?: string;
  fornecedor?: string;
  fornecedorId?: number;
  fornecedorObj?: any;
  usuario?: any;
  criadoEm?: string;
}

@Injectable({ providedIn: 'root' })
export class ContaPagarService {
  private http = inject(HttpClient);
  private apiUrl = '/api/contas-pagar';

  listarTodas(): Observable<ContaPagar[]> {
    return this.http.get<ContaPagar[]>(this.apiUrl);
  }

  listarEmAberto(): Observable<ContaPagar[]> {
    return this.http.get<ContaPagar[]>(`${this.apiUrl}/em-aberto`);
  }

  listarVencidas(): Observable<ContaPagar[]> {
    return this.http.get<ContaPagar[]>(`${this.apiUrl}/vencidas`);
  }

  buscarPorId(id: number): Observable<ContaPagar> {
    return this.http.get<ContaPagar>(`${this.apiUrl}/${id}`);
  }

  criar(conta: any): Observable<ContaPagar> {
    return this.http.post<ContaPagar>(this.apiUrl, conta);
  }

  atualizar(id: number, conta: any): Observable<ContaPagar> {
    return this.http.put<ContaPagar>(`${this.apiUrl}/${id}`, conta);
  }

  registrarPagamento(id: number, valorPago: number, formaPagamento: string): Observable<ContaPagar> {
    return this.http.post<ContaPagar>(`${this.apiUrl}/${id}/pagar`, { valorPago, formaPagamento });
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}