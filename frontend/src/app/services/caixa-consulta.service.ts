import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CaixaConsulta {
  id: number;
  usuarioId: number;
  usuarioNome: string;
  dataHoraAbertura: string;
  dataHoraFechamento?: string;
  totalDiarias: number;
  totalProdutos: number;
  totalBruto: number;
  totalLiquido: number;
  totalDinheiro: number;
  totalPix: number;
  totalCartaoDebito: number;
  totalCartaoCredito: number;
  totalTransferencia: number;
  totalFaturado: number;
  status: string;
  turno: string;
  observacoes?: string;
}

export interface FiltroCaixa {
  dataInicio: string;
  dataFim: string;
  usuarioId?: number;
  status?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CaixaConsultaService {
  private apiUrl = 'http://localhost:8080/api/fechamento-caixa';

  constructor(private http: HttpClient) {}

  consultarCaixas(filtro: FiltroCaixa): Observable<CaixaConsulta[]> {
    let params = new HttpParams()
      .set('dataInicio', filtro.dataInicio + 'T00:00:00')
      .set('dataFim', filtro.dataFim + 'T23:59:59');

    if (filtro.usuarioId) {
      params = params.set('usuarioId', filtro.usuarioId.toString());
    }

    if (filtro.status) {
      params = params.set('status', filtro.status);
    }

    return this.http.get<CaixaConsulta[]>(`${this.apiUrl}/periodo`, { params });
  }

  buscarPorId(id: number): Observable<CaixaConsulta> {
    return this.http.get<CaixaConsulta>(`${this.apiUrl}/${id}`);
  }
  
}