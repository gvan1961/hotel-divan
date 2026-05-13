import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface DepositoProvisorio {
  id: number;
  usuario: any;
  criadoEm: string;
  status: string;
  itens: DepositoProvisorioItem[];
}

export interface DepositoProvisorioItem {
  id: number;
  produto: any;
  quantidade: number;
  quantidadeDistribuida: number;
}

@Injectable({ providedIn: 'root' })
export class DepositoProvisorioService {

  private api = `${environment.apiUrl}/deposito`;

  constructor(private http: HttpClient) {}

  getAtual(): Observable<DepositoProvisorio> {
    return this.http.get<DepositoProvisorio>(`${this.api}/atual`);
  }

  abrir(): Observable<DepositoProvisorio> {
    return this.http.post<DepositoProvisorio>(`${this.api}/abrir`, {});
  }

  adicionarItem(produtoId: number, quantidade: number): Observable<DepositoProvisorioItem> {
    return this.http.post<DepositoProvisorioItem>(`${this.api}/item`, { produtoId, quantidade });
  }

  distribuirItem(itemId: number, reservaId: number, quantidade: number): Observable<void> {
    return this.http.post<void>(`${this.api}/distribuir`, { itemId, reservaId, quantidade });
  }

  removerItem(itemId: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/item/${itemId}`);
  }


pagarAVista(itemId: number, quantidade: number, formaPagamento: string): Observable<void> {
  return this.http.post<void>(`${this.api}/pagar-avista`, { itemId, quantidade, formaPagamento });
}

}