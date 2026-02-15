import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cliente, ClienteRequest } from '../models/cliente.model';

@Injectable({
  providedIn: 'root'
})
export class ClienteService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/clientes';

  getAll(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(this.apiUrl);
  }

  getById(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.apiUrl}/${id}`);
  }

  getByCpf(cpf: string): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.apiUrl}/cpf/${cpf}`);
  }

  create(cliente: ClienteRequest): Observable<Cliente> {
    console.log('📤 Criando cliente:', cliente);
    return this.http.post<Cliente>(this.apiUrl, cliente);
  }

  update(id: number, cliente: ClienteRequest): Observable<Cliente> {
    console.log('📤 Atualizando cliente:', id, cliente);
    return this.http.put<Cliente>(`${this.apiUrl}/${id}`, cliente);
  }

  buscarFuncionarios(termo: string): Observable<Cliente[]> {
  return this.http.get<Cliente[]>(`${this.apiUrl}/funcionarios/buscar?termo=${termo}`);
}

listarFuncionarios(): Observable<Cliente[]> {
  return this.http.get<Cliente[]>(`${this.apiUrl}/funcionarios`);
}

  delete(id: number): Observable<void> {
    console.log('🗑️ Deletando cliente:', id);
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  buscarPorId(id: number): Observable<Cliente> {
  return this.http.get<Cliente>(`${this.apiUrl}/${id}`);
}
}