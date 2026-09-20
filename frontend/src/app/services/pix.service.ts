import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CobrancaPix } from '../models/cobranca-pix.model';

@Injectable({
  providedIn: 'root'
})
export class PixService {
  private http = inject(HttpClient);
  private apiUrl = '/api/pix';

  listarPendentes(): Observable<CobrancaPix[]> {
    return this.http.get<CobrancaPix[]>(`${this.apiUrl}/pendentes`);
  }

  cancelar(id: number): Observable<any> {
  return this.http.patch(`${this.apiUrl}/${id}/cancelar`, {});
}

consultarStatus(id: number): Observable<CobrancaPix> {
  return this.http.get<CobrancaPix>(`${this.apiUrl}/${id}/status`);
}

buscarHistorico(params: { dataInicio?: string; dataFim?: string; reservaId?: number; status?: string }): Observable<CobrancaPix[]> {
  const query = new URLSearchParams();
  if (params.dataInicio) query.set('dataInicio', params.dataInicio);
  if (params.dataFim) query.set('dataFim', params.dataFim);
  if (params.reservaId) query.set('reservaId', params.reservaId.toString());
  if (params.status) query.set('status', params.status);
  return this.http.get<CobrancaPix[]>(`${this.apiUrl}/historico?${query.toString()}`);
}

confirmarCobranca(id: number): Observable<any> {
  return this.http.patch(`${this.apiUrl}/${id}/confirmar`, {});
}

}