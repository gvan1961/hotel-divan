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

}