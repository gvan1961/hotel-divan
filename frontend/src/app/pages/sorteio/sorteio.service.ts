import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Sorteio {
  id?: number;
  nome: string;
  dataInicio: string;
  dataFim: string;
  dataSorteio?: string;
  status?: 'ATIVA' | 'ENCERRADA' | 'REALIZADA';
  dataCriacao?: string;
}

export interface BilheteSorteio {
  id?: number;
  numeroBilhete: number;
  quantidadeDiarias: number;
  dataEmissao: string;
  hospedagemHospede?: {
    id: number;
    cliente?: {
      nome: string;
      cpf?: string;
      celular?: string;
    };
  };
}

export interface VencedorSorteio {
  numeroBilhete: number;
  nomeHospede: string;
  cpfHospede: string;
  celularHospede: string;
  quantidadeDiarias: number;
  dataEmissao: string;
}

@Injectable({ providedIn: 'root' })
export class SorteioService {
  private http = inject(HttpClient);
  private apiUrl = '/api/sorteios';

  listarTodos(): Observable<Sorteio[]> {
    return this.http.get<Sorteio[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Sorteio> {
    return this.http.get<Sorteio>(`${this.apiUrl}/${id}`);
  }

  criar(sorteio: Sorteio): Observable<Sorteio> {
    return this.http.post<Sorteio>(this.apiUrl, sorteio);
  }

  encerrar(id: number): Observable<Sorteio> {
    return this.http.patch<Sorteio>(`${this.apiUrl}/${id}/encerrar`, {});
  }

  realizar(id: number): Observable<VencedorSorteio> {
    return this.http.post<VencedorSorteio>(`${this.apiUrl}/${id}/realizar`, {});
  }

  listarBilhetes(id: number): Observable<BilheteSorteio[]> {
    return this.http.get<BilheteSorteio[]>(`${this.apiUrl}/${id}/bilhetes`);
  }

  contarBilhetes(id: number): Observable<{total: number}> {
    return this.http.get<{total: number}>(`${this.apiUrl}/${id}/bilhetes/count`);
  }
}