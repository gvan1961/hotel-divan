import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FaixaConsumoAgua, FaixaConsumoAguaRequest } from '../models/faixa-consumo-agua.model';

@Injectable({
  providedIn: 'root'
})
export class FaixaConsumoAguaService {
  private http = inject(HttpClient);
  private apiUrl = '/api/faixas-consumo-agua';

  getAll(): Observable<FaixaConsumoAgua[]> {
    return this.http.get<FaixaConsumoAgua[]>(this.apiUrl);
  }
  getByEmpresa(empresaId: number): Observable<FaixaConsumoAgua[]> {
    return this.http.get<FaixaConsumoAgua[]>(`${this.apiUrl}/empresa/${empresaId}`);
  }
  create(faixa: FaixaConsumoAguaRequest): Observable<FaixaConsumoAgua> {
    return this.http.post<FaixaConsumoAgua>(this.apiUrl, faixa);
  }
  update(id: number, faixa: FaixaConsumoAguaRequest): Observable<FaixaConsumoAgua> {
    return this.http.put<FaixaConsumoAgua>(`${this.apiUrl}/${id}`, faixa);
  }
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}