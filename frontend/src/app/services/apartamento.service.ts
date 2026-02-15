import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Apartamento, ApartamentoRequest } from '../models/apartamento.model';

@Injectable({
  providedIn: 'root'
})
export class ApartamentoService {
  private apiUrl = 'http://localhost:8080/api/apartamentos';

  constructor(private http: HttpClient) {}

  // =======================================
  // ✅ MÉTODOS CRUD BÁSICOS
  // =======================================

  getAll(): Observable<Apartamento[]> {
    return this.http.get<Apartamento[]>(this.apiUrl);
  }

  getById(id: number): Observable<Apartamento> {
    return this.http.get<Apartamento>(`${this.apiUrl}/${id}`);
  }

  create(apartamento: ApartamentoRequest): Observable<Apartamento> {
    return this.http.post<Apartamento>(this.apiUrl, apartamento);
  }

  update(id: number, apartamento: ApartamentoRequest): Observable<Apartamento> {
    return this.http.put<Apartamento>(`${this.apiUrl}/${id}`, apartamento);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // =======================================
  // ✅ MÉTODOS ESPECÍFICOS
  // =======================================

  listar(): Observable<Apartamento[]> {
    return this.http.get<Apartamento[]>(this.apiUrl);
  }

  getDisponiveis(): Observable<Apartamento[]> {
    return this.http.get<Apartamento[]>(`${this.apiUrl}/disponiveis`);
  }

  buscarPorNumero(numero: string): Observable<Apartamento> {
    return this.http.get<Apartamento>(`${this.apiUrl}/numero/${numero}`);
  }

  buscarPorStatus(status: string): Observable<Apartamento[]> {
    return this.http.get<Apartamento[]>(`${this.apiUrl}/status/${status}`);
  }

  // =======================================
  // ✅ GESTÃO DE STATUS
  // =======================================

  liberarLimpeza(id: number): Observable<Apartamento> {
    return this.http.patch<Apartamento>(`${this.apiUrl}/${id}/liberar-limpeza`, null);
  }

  colocarEmManutencao(id: number, motivo: string): Observable<Apartamento> {
    return this.http.patch<Apartamento>(
      `${this.apiUrl}/${id}/manutencao`,
      null,
      { params: { motivo } }
    );
  }

  liberarManutencao(id: number): Observable<Apartamento> {
    return this.http.patch<Apartamento>(`${this.apiUrl}/${id}/liberar-manutencao`, null);
  }

  bloquear(id: number, motivo: string): Observable<Apartamento> {
    return this.http.patch<Apartamento>(
      `${this.apiUrl}/${id}/bloquear`,
      null,
      { params: { motivo } }
    );
  }

  desbloquear(id: number): Observable<Apartamento> {
    return this.http.patch<Apartamento>(`${this.apiUrl}/${id}/desbloquear`, null);
  }

  atualizarStatus(id: number, status: string): Observable<Apartamento> {
    return this.http.patch<Apartamento>(
      `${this.apiUrl}/${id}/status`,
      null,
      { params: { status } }
    );
  }

  listarDisponiveisParaTransferencia(): Observable<Apartamento[]> {
  return this.http.get<Apartamento[]>(`${this.apiUrl}/disponiveis`);
}

  buscarDisponiveisParaReserva(dataCheckin: string, dataCheckout: string): Observable<any[]> {
    const url = `${this.apiUrl}/disponiveis-para-reserva?dataCheckin=${dataCheckin}&dataCheckout=${dataCheckout}`;
    console.log('🔍 Buscando apartamentos disponíveis:', url);
    return this.http.get<any[]>(url);
  }

  listarTodos(): Observable<Apartamento[]> {
    return this.http.get<Apartamento[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Apartamento> {
    return this.http.get<Apartamento>(`${this.apiUrl}/${id}`);
  }

  criar(apartamento: Apartamento): Observable<Apartamento> {
    return this.http.post<Apartamento>(this.apiUrl, apartamento);
  }

  atualizar(id: number, apartamento: Apartamento): Observable<Apartamento> {
    return this.http.put<Apartamento>(`${this.apiUrl}/${id}`, apartamento);
  }

  /**
 * Verificar se apartamento tem checkout vencido
 */
verificarCheckoutVencido(apartamentoId: number): Observable<any> {
  return this.http.get(`${this.apiUrl}/${apartamentoId}/verificar-checkout-vencido`);
}

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

}