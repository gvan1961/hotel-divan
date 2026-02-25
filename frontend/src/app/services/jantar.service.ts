import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface HospedeJantar {
  id: number;
  hospedagemHospedeId: number;
  nomeCompleto: string;
  clienteId: number;
  nomeCliente: string;
  apartamentoId: number;
  numeroApartamento: string;
  titular: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class JantarService {
  private apiUrl = 'http://localhost:8080/api/jantar';

  constructor(private http: HttpClient) { }

 getApartamentosComHospedes(): Observable<any[]> {
  return this.http.get<any[]>(`${this.apiUrl}/apartamentos-autorizados`);
}

  getProdutosPorCategoria(categoriaId: number): Observable<any[]> {
  return this.http.get<any[]>(`http://localhost:8080/api/produtos/categoria/${categoriaId}`);
}

  salvarComanda(hospedagemId: number, itens: { produtoId: number, quantidade: number }[]): Observable<any> {
  const payload = {
    hospedagemHospedeId: hospedagemId,
    itens: itens.map(item => ({
      produtoId: item.produtoId,
      quantidade: item.quantidade
    }))
  };
  
  return this.http.post(`${this.apiUrl}/salvar-comanda`, payload);
}

buscarHospede(nome: string, numeroApartamento: string): Observable<any> {
  const payload = {
    nome: nome,
    numeroApartamento: numeroApartamento
  };
  
  return this.http.post(`${this.apiUrl}/buscar-hospede`, payload);
}

gerarRelatorioComandas(dataInicio: string, dataFim: string): Observable<any> {
  const payload = {
    dataInicio: dataInicio,
    dataFim: dataFim
  };
  
  return this.http.post(`${this.apiUrl}/relatorio-comandas`, payload);
}

gerarRelatorioFaturamento(dataInicio: string, dataFim: string): Observable<any> {
  const payload = {
    dataInicio: dataInicio,
    dataFim: dataFim
  };
  
  return this.http.post(`${this.apiUrl}/relatorio-faturamento`, payload);
}

cancelarComanda(notaId: number): Observable<any> {
  return this.http.post(`${this.apiUrl}/cancelar-comanda/${notaId}`, {});
}

}