import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Reserva, ReservaRequest, ReservaResponse } from '../models/reserva.model';

@Injectable({
  providedIn: 'root'
})
export class ReservaService {
  private http = inject(HttpClient);
  private apiUrl = '/api/reservas';

  getAll(): Observable<ReservaResponse[]> {
    return this.http.get<ReservaResponse[]>(this.apiUrl);
  }

  getById(id: number): Observable<ReservaResponse> {
    return this.http.get<ReservaResponse>(`${this.apiUrl}/${id}`);
  }

  getAtivas(): Observable<ReservaResponse[]> {
    return this.http.get<ReservaResponse[]>(`${this.apiUrl}/ativas`);
  }

  getCheckinsDoDia(data: string): Observable<ReservaResponse[]> {
    const params = new HttpParams().set('data', data);
    return this.http.get<ReservaResponse[]>(`${this.apiUrl}/checkins-do-dia`, { params });
  }

  getCheckoutsDoDia(data: string): Observable<ReservaResponse[]> {
    const params = new HttpParams().set('data', data);
    return this.http.get<ReservaResponse[]>(`${this.apiUrl}/checkouts-do-dia`, { params });
  }

  getPorPeriodo(inicio: string, fim: string): Observable<ReservaResponse[]> {
    const params = new HttpParams()
      .set('inicio', inicio)
      .set('fim', fim);
    return this.http.get<ReservaResponse[]>(`${this.apiUrl}/periodo`, { params });
  }

  create(reserva: ReservaRequest): Observable<ReservaResponse> {
    console.log('📤 Criando reserva:', reserva);
    return this.http.post<ReservaResponse>(this.apiUrl, reserva);
  }

  alterarQuantidadeHospedes(id: number, quantidade: number, motivo?: string): Observable<ReservaResponse> {
    console.log('🔄 Alterando quantidade de hóspedes:', id, quantidade);
    let params = new HttpParams().set('quantidade', quantidade.toString());
    if (motivo) {
      params = params.set('motivo', motivo);
    }
    return this.http.patch<ReservaResponse>(`${this.apiUrl}/${id}/alterar-hospedes`, null, { params });
  }

  alterarCheckout(id: number, novaDataCheckout: string, motivo?: string): Observable<ReservaResponse> {
    console.log('🔄 Alterando checkout:', id, novaDataCheckout);
    let params = new HttpParams().set('novaDataCheckout', novaDataCheckout);
    if (motivo) {
      params = params.set('motivo', motivo);
    }
    return this.http.patch<ReservaResponse>(`${this.apiUrl}/${id}/alterar-checkout`, null, { params });
  }

  finalizar(id: number): Observable<ReservaResponse> {
    console.log('✅ Finalizando reserva:', id);
    return this.http.patch<ReservaResponse>(`${this.apiUrl}/${id}/finalizar`, null);
  }

  cancelar(id: number, motivo: string): Observable<ReservaResponse> {
    console.log('❌ Cancelando reserva:', id, motivo);
    const params = new HttpParams().set('motivo', motivo);
    return this.http.patch<ReservaResponse>(`${this.apiUrl}/${id}/cancelar`, null, { params });
  }

  // Método NOVO (com payload genérico)
  adicionarProdutoAoConsumo(reservaId: number, payload: any): Observable<any> {
    console.log(`📤 SERVICE - POST /api/reservas/${reservaId}/consumo`, payload);
    return this.http.post(`${this.apiUrl}/${reservaId}/consumo`, payload);
  }

  // Método ANTIGO (mantido para compatibilidade)
  adicionarConsumo(reservaId: number, produtoId: number, quantidade: number, observacao?: string): Observable<ReservaResponse> {
    const payload = {
      produtoId,
      quantidade,
      observacao
    };
    
    // Redireciona para o método novo
    return this.adicionarProdutoAoConsumo(reservaId, payload);
  }

  listarConsumo(reservaId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${reservaId}/consumo`);
  }

  listarNotasVenda(reservaId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${reservaId}/notas-venda`);
  }

  transferirApartamento(dto: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/transferir-apartamento`, dto);
  }

  // ═══════════════════════════════════════════════════════════════
  // ✅ NOVOS MÉTODOS - CHECKOUT PARCIAL
  // ═══════════════════════════════════════════════════════════════

  /**
   * Realiza checkout parcial de um hóspede específico
   * @param reservaId ID da reserva
   * @param dto Dados do checkout (hospedagemHospedeId, dataHoraSaida, motivo)
   * @returns Observable com resposta do backend
   */
  checkoutParcial(reservaId: number, dto: any): Observable<any> {
    console.log('🚪 Realizando checkout parcial:', reservaId, dto);
    return this.http.post(`${this.apiUrl}/${reservaId}/checkout-parcial`, dto);
  }

  /**
   * Lista todos os hóspedes de uma reserva
   * @param reservaId ID da reserva
   * @returns Observable com array de hóspedes
   */
  listarHospedes(reservaId: number): Observable<any[]> {
    return this.http.get<any[]>(`/api/hospedagem-hospedes/reserva/${reservaId}`);
  }

  // ═══════════════════════════════════════════════════════════════
  // MÉTODOS EXISTENTES (mantidos)
  // ═══════════════════════════════════════════════════════════════

  buscarPorId(id: number): Observable<Reserva> {
    return this.http.get<Reserva>(`${this.apiUrl}/${id}`);
  }

  listarTodas(): Observable<ReservaResponse[]> {
    return this.http.get<ReservaResponse[]>(this.apiUrl);
  }

  criar(reserva: any): Observable<Reserva> {
    return this.http.post<Reserva>(this.apiUrl, reserva);
  }

  atualizar(id: number, reserva: any): Observable<Reserva> {
    return this.http.put<Reserva>(`${this.apiUrl}/${id}`, reserva);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  verificarHospedagem(clienteId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/verificar-hospedagem/${clienteId}`);
  }
  
  obterEstatisticas(): Observable<any> {
    return this.http.get(`${this.apiUrl}/estatisticas`);
  }

  // ═══════════════════════════════════════════════════════════════
  // ✅ TRANSFERÊNCIA DE HÓSPEDE
  // ═══════════════════════════════════════════════════════════════

  /**
   * ✅ TRANSFERIR HÓSPEDE INDIVIDUAL
   */
  transferirHospede(dto: {
    hospedagemHospedeId: number;
    novoApartamentoId: number;
    motivo: string;
  }): Observable<any> {
    return this.http.post(`${this.apiUrl}/transferir-hospede`, dto);
    // ✅ URL CORRETA: /api/reservas/transferir-hospede
  }

  /**
   * ✅ BUSCAR APARTAMENTOS DISPONÍVEIS PARA TRANSFERÊNCIA
   */
  buscarApartamentosDisponiveisParaTransferencia(apartamentoOrigemId: number): Observable<any> {
    return this.http.get(
      `${this.apiUrl}/apartamentos-disponiveis-para-transferencia?apartamentoOrigemId=${apartamentoOrigemId}`
      // ✅ URL CORRETA: /api/reservas/apartamentos-disponiveis-para-transferencia
    );
  }

  prorrogarCheckout(reservaId: number, novoCheckout: string, motivo: string): Observable<any> {
  const params: any = {
    novaDataCheckout: novoCheckout
  };
  
  if (motivo && motivo.trim()) {
    params.motivo = motivo;
  }
  
  return this.http.patch(
    `${this.apiUrl}/${reservaId}/alterar-checkout`, 
    null, 
    { params }
  );
}

 /**
 * ✅ CANCELAR PRÉ-RESERVA (muda status para CANCELADA)
 */
cancelarPreReserva(id: number, motivo?: string): Observable<any> {
  let params = new HttpParams();
  if (motivo) params = params.set('motivo', motivo);
  return this.http.patch(`${this.apiUrl}/${id}/cancelar`, null, { params });
}

/**
 * ✅ EXCLUIR PRÉ-RESERVA (remove do banco)
 */
excluirPreReserva(id: number): Observable<any> {
  console.log('🗑️ Excluindo pré-reserva:', id);
  return this.http.delete(`${this.apiUrl}/${id}/pre-reserva`);
}

/**
 * ✅ BUSCAR RESERVAS PARA O MAPA (apenas ATIVAS e PRÉ-RESERVAS)
 */
buscarParaMapa(): Observable<ReservaResponse[]> {
  return this.http.get<ReservaResponse[]>(`${this.apiUrl}/mapa`);
}

}



