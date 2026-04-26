import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface RegistroPonto {
  id?: number;
  cliente?: any;
  tipo: string;
  dataHora?: string;
  observacao?: string;
  reconhecimentoFacial?: boolean;
  confiancaReconhecimento?: number;
}

@Injectable({ providedIn: 'root' })
export class PontoService {
  private http = inject(HttpClient);
  private apiUrl = '/api/ponto';

  listarHoje(): Observable<RegistroPonto[]> {
    return this.http.get<RegistroPonto[]>(`${this.apiUrl}/hoje`);
  }

  listarPorFuncionario(clienteId: number): Observable<RegistroPonto[]> {
    return this.http.get<RegistroPonto[]>(`${this.apiUrl}/funcionario/${clienteId}`);
  }

  listarPorPeriodo(inicio: string, fim: string): Observable<RegistroPonto[]> {
    return this.http.get<RegistroPonto[]>(`${this.apiUrl}/periodo?inicio=${inicio}&fim=${fim}`);
  }

  registrar(dados: {
    clienteId: number;
    tipo: string;
    reconhecimentoFacial?: boolean;
    confianca?: number;
    observacao?: string;
  }): Observable<any> {
    return this.http.post(`${this.apiUrl}/registrar`, dados);
  }

  buscarFotoFuncionario(clienteId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/fotos/funcionario/${clienteId}`);
  }

  cadastrarFoto(clienteId: number, fotoBase64: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/fotos/cadastrar`, { clienteId, fotoBase64 });
  }

  listarFotos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/fotos`);
  }
}