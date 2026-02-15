import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Perfil {
  id?: number;
  nome: string;
  descricao: string;
  permissoes?: Permissao[];
}

export interface Permissao {
  id: number;
  nome: string;
  descricao: string;
  categoria: string;
}

export interface PerfilRequest {
  nome: string;
  descricao: string;
  permissaoIds: number[];
}

@Injectable({
  providedIn: 'root'
})
export class PerfilService {
  private apiUrl = 'http://localhost:8080/api/perfis';

  constructor(private http: HttpClient) {}

  listarTodos(): Observable<Perfil[]> {
    return this.http.get<Perfil[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Perfil> {
    return this.http.get<Perfil>(`${this.apiUrl}/${id}`);
  }

  criar(perfil: PerfilRequest): Observable<Perfil> {
    return this.http.post<Perfil>(this.apiUrl, perfil);
  }

  atualizar(id: number, perfil: PerfilRequest): Observable<Perfil> {
    return this.http.put<Perfil>(`${this.apiUrl}/${id}`, perfil);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}