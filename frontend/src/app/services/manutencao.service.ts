import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Manutencao,
  ManutencaoRequest,
  TipoServico,
  StatusManutencao,
} from '../models/manutencao.model';

@Injectable({ providedIn: 'root' })
export class ManutencaoService {
  private http = inject(HttpClient);

  // 🔧 AJUSTE: use a MESMA base URL dos seus outros services (ex.: ApartamentoService).
  // Se eles usam environment.apiUrl, troque por:
  //   private apiUrl = `${environment.apiUrl}/manutencoes`;
  private apiUrl = '/api/manutencoes';

  /** Busca com filtros opcionais. */
  buscar(filtros: {
    apartamentoId?: number;
    tipoServico?: TipoServico | '';
    status?: StatusManutencao | '';
    inicio?: string;
    fim?: string;
  }): Observable<Manutencao[]> {
    let params = new HttpParams();
    if (filtros.apartamentoId) params = params.set('apartamentoId', filtros.apartamentoId);
    if (filtros.tipoServico)   params = params.set('tipoServico', filtros.tipoServico);
    if (filtros.status)        params = params.set('status', filtros.status);
    if (filtros.inicio)        params = params.set('inicio', filtros.inicio);
    if (filtros.fim)           params = params.set('fim', filtros.fim);
    return this.http.get<Manutencao[]>(this.apiUrl, { params });
  }

  /** Histórico completo de um apartamento. */
  getByApartamento(apartamentoId: number): Observable<Manutencao[]> {
    return this.http.get<Manutencao[]>(`${this.apiUrl}/apartamento/${apartamentoId}`);
  }

  getById(id: number): Observable<Manutencao> {
    return this.http.get<Manutencao>(`${this.apiUrl}/${id}`);
  }

  create(dto: ManutencaoRequest): Observable<Manutencao> {
    return this.http.post<Manutencao>(this.apiUrl, dto);
  }

  update(id: number, dto: ManutencaoRequest): Observable<Manutencao> {
    return this.http.put<Manutencao>(`${this.apiUrl}/${id}`, dto);
  }

  concluir(id: number): Observable<Manutencao> {
    return this.http.patch<Manutencao>(`${this.apiUrl}/${id}/concluir`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /** Alerta preventivo: filtro de ar vencido (>= 90 dias). */
  filtroArVencido(apartamentoId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/apartamento/${apartamentoId}/filtro-ar-vencido`);
  }
}