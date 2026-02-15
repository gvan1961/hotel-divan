import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Vale, ValeRequest, StatusVale } from '../models/vale.model';

@Injectable({
  providedIn: 'root'
})
export class ValeService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/vales';

  listarTodos(): Observable<Vale[]> {
    return this.http.get<Vale[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Vale> {
    return this.http.get<Vale>(`${this.apiUrl}/${id}`);
  }

  listarPorCliente(clienteId: number): Observable<Vale[]> {
    return this.http.get<Vale[]>(`${this.apiUrl}/cliente/${clienteId}`);
  }

  listarPendentes(): Observable<Vale[]> {
    return this.http.get<Vale[]>(`${this.apiUrl}/pendentes`);
  }

  listarVencidos(): Observable<Vale[]> {
    return this.http.get<Vale[]>(`${this.apiUrl}/vencidos`);
  }

  calcularTotalPendente(clienteId: number): Observable<{ totalPendente: number }> {
    return this.http.get<{ totalPendente: number }>(`${this.apiUrl}/cliente/${clienteId}/total-pendente`);
  }

  criar(request: ValeRequest): Observable<Vale> {
    return this.http.post<Vale>(this.apiUrl, request);
  }

  atualizar(id: number, request: ValeRequest): Observable<Vale> {
    return this.http.put<Vale>(`${this.apiUrl}/${id}`, request);
  }

  marcarComoPago(id: number): Observable<Vale> {
    return this.http.patch<Vale>(`${this.apiUrl}/${id}/pagar`, {});
  }

  cancelar(id: number, motivo: string): Observable<Vale> {
    return this.http.patch<Vale>(`${this.apiUrl}/${id}/cancelar?motivo=${encodeURIComponent(motivo)}`, {});
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  atualizarVencidos(): Observable<{ mensagem: string }> {
    return this.http.post<{ mensagem: string }>(`${this.apiUrl}/atualizar-vencidos`, {});
  }

  assinarVale(id: number, assinaturaBase64: string): Observable<Vale> {
  return this.http.patch<Vale>(`${this.apiUrl}/${id}/assinar`, {
    assinaturaBase64: assinaturaBase64
  });
}
}   