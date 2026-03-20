import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, interval } from 'rxjs';
import { switchMap, startWith } from 'rxjs/operators';

export interface AlertaDTO {
  tipoAlerta: string; // CONFLITO_PRE_RESERVA, CHECKOUT_VENCIDO, NO_SHOW
  nivelGravidade: string; // CRITICO, ALTO, MEDIO, BAIXO
  titulo: string;
  descricao: string;
  recomendacao: string;
  
  apartamentoId?: number;
  numeroApartamento?: string;
  tipoApartamento?: string;
  
  reservaId?: number;
  clienteNome?: string;
  statusReserva?: string;
  
  dataCheckin?: string;
  dataCheckout?: string;
  dataHoraAlerta?: string;
  
  horasAtraso?: number;
  minutosAtraso?: number;
  totalPago?: number;
  totalReserva?: number;
  percentualPago?: number;
  
  acoesDisponiveis?: string[];
  apartamentosDisponiveis?: ApartamentoDisponivel[];
}

export interface ApartamentoDisponivel {
  apartamentoId: number;
  numeroApartamento: string;
  tipoApartamento: string;
  categoria: string;
  recomendado: boolean;
}

export interface TodosAlertasResponse {
  conflitos: AlertaDTO[];
  checkoutsVencidos: AlertaDTO[];
  noShows: AlertaDTO[];
  preReservasEmRisco: AlertaDTO[]; // ✅ NOVO
}

@Injectable({
  providedIn: 'root'
})
export class AlertasService {
  
  private apiUrl = '/api/alertas';

  constructor(private http: HttpClient) {}

  /**
   * 📊 BUSCAR TODOS OS ALERTAS
   */
 
buscarTodosAlertas(): Observable<TodosAlertasResponse> {
  return this.http.get<TodosAlertasResponse>(
    `${this.apiUrl}/todos-alertas` // ✅ CORRIGIDO: SEM duplicar /alertas
  );
}


  /**
   * 🔄 BUSCAR ALERTAS COM AUTO-REFRESH (a cada 5 minutos)
   */
  buscarAlertasComRefresh(): Observable<TodosAlertasResponse> {
    return interval(5 * 60 * 1000) // 5 minutos
      .pipe(
        startWith(0),
        switchMap(() => this.buscarTodosAlertas())
      );
  }

  /**
   * 🚨 BUSCAR APENAS CONFLITOS
   */
  buscarConflitos(): Observable<AlertaDTO[]> {
    return this.http.get<AlertaDTO[]>(`${this.apiUrl}/conflitos`);
  }

  /**
   * ⏰ BUSCAR APENAS CHECKOUTS VENCIDOS
   */
  buscarCheckoutsVencidos(): Observable<AlertaDTO[]> {
    return this.http.get<AlertaDTO[]>(`${this.apiUrl}/checkouts-vencidos`);
  }

  /**
   * 🔴 BUSCAR APENAS NO-SHOWS
   */
  buscarNoShows(): Observable<AlertaDTO[]> {
    return this.http.get<AlertaDTO[]>(`${this.apiUrl}/no-shows`);
  }

  /**
   * 🔄 TRANSFERIR PRÉ-RESERVA
   */
  transferirPreReserva(preReservaId: number, novoApartamentoId: number, motivo?: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/transferir`, {
      preReservaId,
      novoApartamentoId,
      motivo
    });
  }

  /**
   * ❌ MARCAR COMO NO-SHOW
   */
  marcarNoShow(reservaId: number, observacao?: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/marcar-no-show`, {
      reservaId,
      observacao
    });
  }

  /**
   * 📊 CALCULAR TOTAL DE ALERTAS
   */
  calcularTotalAlertas(alertas: TodosAlertasResponse): number {
  return (alertas.conflitos?.length || 0) +
         (alertas.checkoutsVencidos?.length || 0) +
         (alertas.noShows?.length || 0) +
         (alertas.preReservasEmRisco?.length || 0); // ✅ NOVO
}

  /**
   * 🎨 OBTER COR POR GRAVIDADE
   */
  obterCorGravidade(gravidade: string): string {
    switch (gravidade) {
      case 'CRITICO': return '#dc3545';
      case 'ALTO': return '#fd7e14';
      case 'MEDIO': return '#ffc107';
      case 'BAIXO': return '#17a2b8';
      default: return '#6c757d';
    }
  }

  /**
   * 📝 OBTER ÍCONE POR TIPO DE ALERTA
   */
  obterIconeTipoAlerta(tipo: string): string {
    switch (tipo) {
      case 'CONFLITO_PRE_RESERVA': return '🚨';
      case 'CHECKOUT_VENCIDO': return '⏰';
      case 'NO_SHOW': return '🔴';
      default: return '⚠️';
    }
  }

  /**
   * 📝 OBTER NOME LEGÍVEL DO TIPO
   */
  obterNomeTipoAlerta(tipo: string): string {
    switch (tipo) {
      case 'CONFLITO_PRE_RESERVA': return 'Conflito de Reserva';
      case 'CHECKOUT_VENCIDO': return 'Checkout Vencido';
      case 'NO_SHOW': return 'Não Compareceu (No-Show)';
      default: return 'Alerta';
    }
  }

  /**
 * 🚪 FAZER CHECKOUT
 */
fazerCheckout(reservaId: number, observacao?: string): Observable<any> {
  return this.http.post(`${this.apiUrl}/fazer-checkout`, {
    reservaId,
    observacao
  });
}

/**
 * 🔄 PRORROGAR CHECKOUT
 */
prorrogarCheckout(reservaId: number, novoCheckout: string, motivo?: string): Observable<any> {
  return this.http.post(`${this.apiUrl}/prorrogar-checkout`, {
    reservaId,
    novoCheckout,
    motivo
  });
}

/**
 * 💰 COBRAR DIÁRIA ADICIONAL
 */
cobrarDiaria(reservaId: number, tipoDiaria: string, motivo?: string): Observable<any> {
  return this.http.post(`${this.apiUrl}/cobrar-diaria`, {
    reservaId,
    tipoDiaria,
    motivo
  });
}

/**
 * ❌ CANCELAR RESERVA
 */
cancelarReserva(reservaId: number, motivo?: string): Observable<any> {
  return this.http.post(`${this.apiUrl}/cancelar-reserva`, {
    reservaId,
    motivo
  });
}

/**
 * ✅ CONFIRMAR CHEGADA (PRÉ-RESERVA → CHECK-IN)
 */
confirmarChegada(reservaId: number, observacao?: string): Observable<any> {
  return this.http.post(`${this.apiUrl}/confirmar-chegada`, {
    reservaId,
    observacao
  });
}

}
