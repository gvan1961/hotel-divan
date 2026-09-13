import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CobrancaCartao } from '../models/cobranca-cartao.model';

@Injectable({ providedIn: 'root' })
export class CartaoService {
  private http = inject(HttpClient);
  private apiUrl = '/api/cartao';

  listarPendentes(): Observable<CobrancaCartao[]> {
    return this.http.get<CobrancaCartao[]>(`${this.apiUrl}/pendentes`);
  }

  consultarStatus(id: number): Observable<CobrancaCartao> {
    return this.http.get<CobrancaCartao>(`${this.apiUrl}/${id}/status`);
  }

  cancelar(id: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/cancelar`, {});
  }

  buscarHistorico(params: { dataInicio?: string; dataFim?: string; reservaId?: number; status?: string }): Observable<CobrancaCartao[]> {
  const query = new URLSearchParams();
  if (params.dataInicio) query.set('dataInicio', params.dataInicio);
  if (params.dataFim) query.set('dataFim', params.dataFim);
  if (params.reservaId) query.set('reservaId', params.reservaId.toString());
  if (params.status) query.set('status', params.status);
  return this.http.get<CobrancaCartao[]>(`${this.apiUrl}/historico?${query.toString()}`);
}

}