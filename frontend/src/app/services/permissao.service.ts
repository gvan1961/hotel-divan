import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Permissao {
  id: number;
  nome: string;
  descricao: string;
  categoria: string;
}

@Injectable({
  providedIn: 'root'
})
export class PermissaoService {
  private apiUrl = 'http://localhost:8080/api/permissoes';

  constructor(private http: HttpClient) {}

  listarTodas(): Observable<Permissao[]> {
    return this.http.get<Permissao[]>(this.apiUrl);
  }
}