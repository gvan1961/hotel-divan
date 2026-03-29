import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Empresa, EmpresaRequest } from '../models/empresa.model';

@Injectable({
  providedIn: 'root'
})
export class EmpresaService {
  private http = inject(HttpClient);
  private apiUrl = '/api/empresas';

  getAll(): Observable<Empresa[]> {
    return this.http.get<Empresa[]>(this.apiUrl);
  }

  getById(id: number): Observable<Empresa> {
    return this.http.get<Empresa>(`${this.apiUrl}/${id}`);
  }

  create(empresa: EmpresaRequest): Observable<Empresa> {
    console.log('📤 Enviando empresa:', empresa);
    return this.http.post<Empresa>(this.apiUrl, empresa);
  }

  update(id: number, empresa: EmpresaRequest): Observable<Empresa> {
    console.log('📤 Atualizando empresa:', id, empresa);
    return this.http.put<Empresa>(`${this.apiUrl}/${id}`, empresa);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
