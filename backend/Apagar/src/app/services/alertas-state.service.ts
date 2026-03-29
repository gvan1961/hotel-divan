import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

/**
 * 🔔 SERVIÇO DE ESTADO DOS ALERTAS
 * 
 * Gerencia notificações em tempo real quando os alertas são atualizados.
 * Funciona como um canal de comunicação entre componentes.
 */
@Injectable({
  providedIn: 'root'
})
export class AlertasStateService {
  
  // Subject que emite true quando os alertas foram atualizados
  private alertasAtualizadosSubject = new BehaviorSubject<boolean>(false);
  
  // Observable que os componentes podem se inscrever
  public alertasAtualizados$ = this.alertasAtualizadosSubject.asObservable();
  
  constructor() {
    console.log('🔔 AlertasStateService inicializado');
  }
  
  /**
   * 🔔 NOTIFICAR QUE OS ALERTAS FORAM ATUALIZADOS
   * 
   * Chame este método após:
   * - Fazer checkout
   * - Criar/cancelar reserva
   * - Qualquer ação que afete alertas
   */
  notificarAlertasAtualizados(): void {
    console.log('🔔 Notificando atualização de alertas');
    this.alertasAtualizadosSubject.next(true);
  }
  
  /**
   * 🔄 RESETAR NOTIFICAÇÃO
   * 
   * Chame após processar a notificação
   */
  resetarNotificacao(): void {
    this.alertasAtualizadosSubject.next(false);
  }
}
