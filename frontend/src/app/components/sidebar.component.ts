import { Component, OnInit, OnDestroy, inject, ChangeDetectorRef, NgZone, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FechamentoCaixaService } from '../services/fechamento-caixa.service';
import { AuthService } from '../services/auth.service';
import { CaixaStateService } from '../services/caixa-state.service';
import { AlertasService, AlertaDTO } from '../services/alertas.service';
import { AlertasStateService } from '../services/alertas-state.service'; // ✅ NOVO IMPORT
import { Subscription } from 'rxjs';
import { HasPermissionDirective } from '../directives/has-permission.directive';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, HasPermissionDirective ],
  
  template: `
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2>🏨 Di Van</h2>
      </div>

      <nav class="sidebar-nav">

        <!-- PAINEL DE RECEPÇÃO  -->
          <a *hasPermission="'RESERVA_VISUALIZAR'"
            routerLink="/painel-recepcao" routerLinkActive="active" class="nav-item">
            <span class="icon">🏨</span>
            <span class="label">Painel Recepção</span>
          </a>
     
        <!-- ALERTAS -->
        <a routerLink="/alertas" routerLinkActive="active" class="nav-item nav-item-alertas">
          <span class="icon">🚨</span>
          <span class="label">Alertas</span>
          <span *ngIf="totalAlertas > 0" class="badge-alertas">{{ totalAlertas }}</span>
        </a>

        <!-- RESERVAS -->
        <a *hasPermission="'RESERVA_VISUALIZAR'"
           routerLink="/reservas" routerLinkActive="active" class="nav-item">
          <span class="icon">📋</span>
          <span class="label">Reservas</span>
        </a>

        <!-- MAPA DE RESERVAS -->
        <a *hasPermission="'RESERVA_VISUALIZAR'"
           routerLink="/reservas/mapa" routerLinkActive="active" class="nav-item">
          <span class="icon">📅</span>
          <span class="label">Mapa de Reservas</span>
        </a>

         <!-- AVISO CAIXA DE OUTRO USUÁRIO -->
        <div class="aviso-caixa-outro" *ngIf="avisoOutrosCaixas">
          {{ avisoOutrosCaixas }}
        </div>

        
        <div class="nav-divider"></div>

        <!-- CAIXA -->
        <ng-container *hasPermission="'CAIXA_FECHAMENTO'">
          <div class="nav-item caixa-status" *ngIf="caixaAberto">
            <span class="icon">✅</span>
            <span class="label">Caixa Aberto</span>
          </div>
          <a *ngIf="!caixaAberto"
             routerLink="/abertura-caixa" class="nav-item nav-caixa abrir">
            <span class="icon">🔓</span>
            <span class="label">Abrir Caixa</span>
          </a>
          <a *ngIf="caixaAberto && caixaAberto.id"
             [routerLink]="['/fechamento-caixa', caixaAberto.id]"
             class="nav-item nav-caixa visualizar">
            <span class="icon">💰</span>
            <span class="label">Meu Caixa</span>
          </a>
        </ng-container>

        <div class="nav-divider"></div>

        <!-- CLIENTES -->
        <a *hasPermission="'HOSPEDE_VISUALIZAR'"
           routerLink="/clientes" routerLinkActive="active" class="nav-item">
          <span class="icon">👥</span>
          <span class="label">Clientes</span>
        </a>

        <!-- APARTAMENTOS -->
        <a *hasPermission="'RESERVA_VISUALIZAR'"
           routerLink="/apartamentos" routerLinkActive="active" class="nav-item">
          <span class="icon">🏨</span>
          <span class="label">Apartamentos</span>
        </a>

        <!-- MANUTENÇÕES -->
           <a *hasPermission="'RESERVA_VISUALIZAR'"
            routerLink="/manutencoes" routerLinkActive="active" class="nav-item">
            <span class="icon">🔧</span>
            <span class="label">Manutenções</span>
           </a>

        <!-- CADASTROS -->
        <a *hasPermission="'USUARIO_VISUALIZAR'"
           routerLink="/cadastros" routerLinkActive="active" class="nav-item nav-item-cadastros">
          <span class="icon">📋</span>
          <span class="label">Cadastros</span>
        </a>

        <div class="nav-divider"></div>

        <!-- LIMPEZA -->
        <a *hasPermission="'RESERVA_VISUALIZAR'"
           routerLink="/apartamentos/limpeza" routerLinkActive="active" class="nav-item">
          <span class="icon">🧹</span>
          <span class="label">Limpeza</span>
        </a>

        <!-- CONTAGEM ESTOQUE -->
        <a *hasPermission="'ESTOQUE_CONTAGEM'"
           routerLink="/contagem-estoque" routerLinkActive="active" class="nav-item nav-item-destaque">
          <span class="icon">📋</span>
          <span class="label">Contagem Estoque</span>
        </a>

        <!-- ⭐ VALE RÁPIDO -->
        <!-- Nova permissão granular (VALE_CRIAR) para não depender de ROLE_ADMIN.
             Atribua essa permissão para ADMIN, GERENTE e RECEPCIONISTA na tela
             de Cadastros/Permissões. -->
        <a *hasPermission="'VALE_CRIAR'"
           routerLink="/vales/rapido" routerLinkActive="active" class="nav-item nav-item-vale-rapido">
          <span class="icon">⚡</span>
          <span class="label">Vale Rápido</span>
        </a>

        <!-- PDV -->
        <a *hasPermission="'PRODUTO_VISUALIZAR'"
           routerLink="/pdv" routerLinkActive="active" class="nav-item">
          <span class="icon">💳</span>
          <span class="label">PDV - Vendas</span>
        </a>

        <!-- JANTAR -->
        <a *hasPermission="'JANTAR_VISUALIZAR'"
           routerLink="/jantar" routerLinkActive="active" class="nav-item">
          <span class="icon">🍽️</span>
          <span class="label">Jantar</span>
        </a>

        <!-- COMANDAS RÁPIDAS -->
        <a *hasPermission="'JANTAR_COMANDO'"
           routerLink="/comandas-rapidas" routerLinkActive="active" class="nav-item"
           style="color: #ffc107 !important; font-weight: 700;">
          <span class="icon">🍽️</span>
          <span class="label">Comandas Rápidas</span>
        </a>

        <!-- GESTÃO DE COMANDAS -->
        <a *hasPermission="'JANTAR_COMANDO'"
           routerLink="/gestao-comandas" routerLinkActive="active" class="nav-item">
          <span class="icon">🗂️</span>
          <span class="label">Gestão de Comandas</span>
        </a>

        <!-- RELATÓRIO COMANDAS -->
        <a *hasPermission="'RELATORIO_VISUALIZAR'"
           routerLink="/relatorio-comandas" routerLinkActive="active" class="nav-item">
          <span class="icon">📊</span>
          <span class="label">Relatório Comandas</span>
        </a>

        <!-- FATURAMENTO -->
        <a *hasPermission="'RELATORIO_VISUALIZAR'"
           routerLink="/relatorio-faturamento" routerLinkActive="active" class="nav-item">
          <span class="icon">💰</span>
          <span class="label">Faturamento</span>
        </a>

     <!-- GRÁFICOS -->
<a *hasPermission="'ROLE_ADMIN'"
   routerLink="/graficos" routerLinkActive="active" class="nav-item">
  <span class="icon">📈</span>
  <span class="label">Gráficos</span>
</a>

        <div class="nav-divider"></div>

        <!-- ══════════════════════════════════════ -->
        <!-- BOTÃO ÁREA ADMINISTRATIVA             -->
        <!-- ══════════════════════════════════════ -->
        <a routerLink="/administrativo" routerLinkActive="active" class="nav-item btn-admin-toggle">
  <span class="icon">⚙️</span>
  <span class="label">Administrativo</span>
</a>       
  
      </nav>

      <div class="sidebar-footer">
        <button class="logout-btn" (click)="logout()">
          <span class="icon">🚪</span>
          <span class="label">Sair</span>
        </button>
      </div>
    </aside>

    <!-- ✅ POP-UP DE CHECKOUT VENCIDO (som + destaque na tela) -->
    <div *ngIf="popupCheckoutVencido"
         class="popup-checkout-vencido-overlay"
         (click)="fecharPopupCheckoutVencido()">
      <div class="popup-checkout-vencido-card" (click)="$event.stopPropagation()">
        <div class="popup-checkout-vencido-header">
          <span class="popup-icone">⏰</span>
          <strong>Checkout vencido!</strong>
          <span *ngIf="filaPopupsCheckoutVencido.length > 0" class="popup-contador-fila">
            +{{ filaPopupsCheckoutVencido.length }}
          </span>
        </div>
        <div class="popup-checkout-vencido-body">
          <p class="popup-apto">Apto {{ popupCheckoutVencido.numeroApartamento || '?' }}</p>
          <p>{{ popupCheckoutVencido.clienteNome || 'Cliente não identificado' }}</p>
          <p class="popup-detalhe">
            Checkout previsto: {{ formatarDataHoraPopup(popupCheckoutVencido.dataCheckout) }}
          </p>
        </div>
        <div class="popup-checkout-vencido-acoes">
          <button class="popup-btn-ver" (click)="irParaReservaPopup(popupCheckoutVencido.reservaId)">
            Ver reserva
          </button>
          <button class="popup-btn-fechar" (click)="fecharPopupCheckoutVencido()">
            Fechar
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    /* ✅ POP-UP DE CHECKOUT VENCIDO */
    .popup-checkout-vencido-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.45);
      display: flex;
      align-items: flex-start;
      justify-content: flex-end;
      padding: 24px;
      z-index: 9999;
    }

    .popup-checkout-vencido-card {
      background: #fff;
      border-left: 6px solid #dc3545;
      border-radius: 10px;
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
      width: 320px;
      max-width: 90vw;
      overflow: hidden;
      animation: popup-entrar 0.25s ease-out;
    }

    @keyframes popup-entrar {
      from { transform: translateY(-16px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }

    .popup-checkout-vencido-header {
      display: flex;
      align-items: center;
      gap: 8px;
      background: #dc3545;
      color: #fff;
      padding: 12px 16px;
      font-size: 15px;
    }

    .popup-icone {
      font-size: 18px;
      animation: popup-balancar 1s ease-in-out infinite;
    }

    @keyframes popup-balancar {
      0%, 100% { transform: rotate(0deg); }
      25% { transform: rotate(-15deg); }
      75% { transform: rotate(15deg); }
    }

    .popup-contador-fila {
      margin-left: auto;
      background: rgba(255, 255, 255, 0.25);
      border-radius: 12px;
      padding: 2px 8px;
      font-size: 12px;
    }

    .popup-checkout-vencido-body {
      padding: 14px 16px;
    }

    .popup-checkout-vencido-body p {
      margin: 0 0 6px;
      font-size: 14px;
      color: #333;
    }

    .popup-apto {
      font-weight: 700;
      font-size: 16px !important;
      color: #dc3545 !important;
    }

    .popup-detalhe {
      color: #777 !important;
      font-size: 12px !important;
    }

    .popup-checkout-vencido-acoes {
      display: flex;
      gap: 8px;
      padding: 0 16px 14px;
    }

    .popup-checkout-vencido-acoes button {
      flex: 1;
      padding: 8px 10px;
      border-radius: 6px;
      border: none;
      cursor: pointer;
      font-size: 13px;
      font-weight: 600;
    }

    .popup-btn-ver {
      background: #dc3545;
      color: #fff;
    }

    .popup-btn-ver:hover {
      background: #b02a37;
    }

    .popup-btn-fechar {
      background: #f1f1f1;
      color: #333;
    }

    .popup-btn-fechar:hover {
      background: #e2e2e2;
    }

    .sidebar {
      position: fixed;
      left: 0;
      top: 0;
      bottom: 0;
      width: 200px;
      background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
      color: white;
      display: flex;
      flex-direction: column;
      box-shadow: 2px 0 8px rgba(0,0,0,0.1);
      z-index: 100;
    }

    .sidebar-header {
      padding: 20px;
      border-bottom: 1px solid rgba(255,255,255,0.1);
      text-align: center;
    }

    .sidebar-header h2 {
      margin: 0;
      font-size: 1.5em;
      font-weight: 700;
    }

    .sidebar-nav {
      flex: 1;
      overflow-y: auto;
      padding: 10px 0;
    }

    .sidebar-nav::-webkit-scrollbar {
      width: 4px;
    }

    .sidebar-nav::-webkit-scrollbar-thumb {
      background: rgba(255,255,255,0.2);
      border-radius: 2px;
    }

    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 20px;
      color: rgba(255,255,255,0.8);
      text-decoration: none;
      transition: all 0.2s ease;
      cursor: pointer;
      border: none;
      background: none;
      width: 100%;
      text-align: left;
    }

    .nav-item:hover {
      background: rgba(255,255,255,0.1);
      color: white;
    }

    .nav-item.active {
      background: rgba(52, 152, 219, 0.3);
      color: white;
      border-left: 3px solid #3498db;
    }

    .caixa-status {
      background: rgba(46, 204, 113, 0.2);
      color: #2ecc71;
      font-weight: 700;
      cursor: default;
    }

    .caixa-status:hover {
      background: rgba(46, 204, 113, 0.2);
      color: #2ecc71;
    }

    .nav-caixa {
      font-weight: 700;
    }

    .nav-caixa.abrir {
      background: rgba(46, 204, 113, 0.2);
      color: #2ecc71;
    }

    .nav-caixa.abrir:hover {
      background: rgba(46, 204, 113, 0.3);
      color: #27ae60;
    }

    /* ✅ ESTILO PARA ALERTAS */
    .nav-item-alertas {
      position: relative;
      background: rgba(220, 53, 69, 0.1);
      font-weight: 700;
    }

    .nav-item-alertas:hover {
      background: rgba(220, 53, 69, 0.2);
    }

    .nav-item-alertas.active {
      background: rgba(220, 53, 69, 0.3);
      border-left: 3px solid #dc3545;
    }

    .badge-alertas {
      position: absolute;
      top: 8px;
      right: 15px;
      background: #dc3545;
      color: white;
      font-size: 0.75em;
      font-weight: 700;
      padding: 3px 8px;
      border-radius: 12px;
      min-width: 20px;
      text-align: center;
      box-shadow: 0 2px 5px rgba(220, 53, 69, 0.3);
      animation: pulse-badge 2s ease-in-out infinite;
    }

    @keyframes pulse-badge {
      0%, 100% {
        transform: scale(1);
        box-shadow: 0 2px 5px rgba(220, 53, 69, 0.3);
      }
      50% {
        transform: scale(1.1);
        box-shadow: 0 4px 10px rgba(220, 53, 69, 0.5);
      }
    }

    .nav-caixa.visualizar {
      background: rgba(52, 152, 219, 0.2);
      color: #3498db;
    }

    .nav-caixa.visualizar:hover {
      background: rgba(52, 152, 219, 0.3);
      color: #2980b9;
    }

    .icon {
      font-size: 1.3em;
      width: 24px;
      text-align: center;
    }

    .label {
      font-size: 0.95em;
      font-weight: 500;
    }

    .nav-divider {
      height: 1px;
      background: rgba(255,255,255,0.1);
      margin: 10px 20px;
    }

    .sidebar-footer {
      padding: 15px;
      border-top: 1px solid rgba(255,255,255,0.1);
    }

    .logout-btn {
      display: flex;
      align-items: center;
      gap: 12px;
      width: 100%;
      padding: 12px;
      background: rgba(231, 76, 60, 0.2);
      border: 1px solid rgba(231, 76, 60, 0.3);
      color: white;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .logout-btn:hover {
      background: rgba(231, 76, 60, 0.4);
    }

    .nav-item-cadastros {
      background: rgba(102, 126, 234, 0.1);
      font-weight: 700;
    }

    .nav-item-cadastros:hover {
      background: rgba(102, 126, 234, 0.2);
    }

    .nav-item-cadastros.active {
      background: rgba(102, 126, 234, 0.3);
      border-left: 3px solid #667eea;
    }

    /* ✅ ESTILO ESPECIAL PARA CONTAGEM DE ESTOQUE */
    .nav-item-destaque {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white !important;
      font-weight: 700;
      margin: 5px 10px;
      border-radius: 8px;
      box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
    }

    .nav-item-destaque:hover {
      background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
      transform: translateX(5px);
      box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
    }

    .nav-item-destaque.active {
      background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
      border-left: 3px solid #fff;
    }

    /* ⭐ ESTILO PARA VALE RÁPIDO */
    .nav-item-vale-rapido {
      background: rgba(255, 193, 7, 0.15);
      font-weight: 700;
      margin: 5px 10px;
      border-radius: 8px;
      border-left: 3px solid #ffc107;
    }

    .nav-item-vale-rapido:hover {
      background: rgba(255, 193, 7, 0.3);
      color: #ffc107;
      transform: translateX(5px);
    }

    .nav-item-vale-rapido.active {
      background: rgba(255, 193, 7, 0.3);
      color: #ffc107;
      border-left: 3px solid #ffc107;
    }

    @media (max-width: 768px) {
      .sidebar {
        width: 70px;
      }

      .label {
        display: none;
      }

      .sidebar-header h2 {
        font-size: 1.2em;
      }
    }

    @media print {
      .sidebar {
        display: none !important;
      }
    }

     /* ── BOTÃO ADMINISTRATIVO ─────────────────── */
    .btn-admin-toggle {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 20px;
      width: 100%;
      background: rgba(255, 193, 7, 0.15);
      border: none;
      border-left: 3px solid #ffc107;
      color: rgba(255,255,255,0.9);
      cursor: pointer;
      font-size: 0.95em;
      font-weight: 700;
      transition: all 0.2s ease;
      text-align: left;
    }

    .btn-admin-toggle:hover {
      background: rgba(255, 193, 7, 0.25);
      color: #ffc107;
    }

    .btn-admin-toggle .seta {
      margin-left: auto;
      font-size: 0.75em;
      color: #ffc107;
    }

    /* ── MENU ADMINISTRATIVO COLAPSÁVEL ──────── */
    .admin-menu {
      background: rgba(0,0,0,0.2);
    }
   
    .nav-item-admin {
      padding-left: 30px;
      font-size: 0.9em;
      border-left: 2px solid rgba(255, 193, 7, 0.3);
    }

    .nav-item-admin:hover {
      border-left-color: #ffc107;
      color: #ffc107;
    }

    .nav-item-admin.active {
      background: rgba(255, 193, 7, 0.15);
      border-left: 2px solid #ffc107;
      color: #ffc107;
    }

    .aviso-caixa-outro {
  background: #fff3cd;
  color: #856404;
  padding: 6px 12px;
  border-left: 4px solid #ffc107;
  font-size: 0.82rem;
  font-weight: 600;
  margin: 4px 8px;
  border-radius: 4px;
}

  `]
})
export class SidebarComponent implements OnInit, OnDestroy {
  private router = inject(Router);
  private fechamentoCaixaService = inject(FechamentoCaixaService);
  private authService = inject(AuthService);
  private caixaStateService = inject(CaixaStateService);
  private alertasService = inject(AlertasService);
  private alertasStateService = inject(AlertasStateService); // ✅ NOVO INJECT
  private http = inject(HttpClient);
  caixaAberto: any = null;
  usuarioId: number = 1;
  totalAlertas = 0;
  avisoOutrosCaixas = '';
  adminAberto = false;

  // ✅ POP-UP DE CHECKOUT VENCIDO (som + destaque)
  popupCheckoutVencido: AlertaDTO | null = null;
  filaPopupsCheckoutVencido: AlertaDTO[] = [];
  private checkoutVencidoNotificados = new Set<number>(); // reservaIds já avisados nesta sessão
   
  private verificandoCaixa = false;
  private subscription?: Subscription;
  private caixaAtualizadoSubscription?: Subscription;
  private alertasAtualizadosSubscription?: Subscription; // ✅ NOVA SUBSCRIPTION

  ngOnInit(): void {
    console.log('🔄 Sidebar inicializado - COM EVENTOS');
    
    this.usuarioId = this.authService.getUsuarioId();

    // ✅ Prepara o desbloqueio de áudio no primeiro clique/tecla do usuário
    this.prepararDesbloqueioDeAudio();
    
    // ✅ BUSCAR ALERTAS IMEDIATAMENTE
    this.buscarTotalAlertas();

    // ✅ ATUALIZAR ALERTAS A CADA 30 SEGUNDOS (BACKUP)
    setInterval(() => {
      this.buscarTotalAlertas();
    }, 30000); // 30 segundos (era 5 minutos)
    
    // ✅ VERIFICAR CAIXA APENAS UMA VEZ
    setTimeout(() => {
      this.verificarCaixaAberto();
    }, 1000);

    // ✅ ESCUTAR NOTIFICAÇÕES DE ATUALIZAÇÃO DO CAIXA
    this.caixaAtualizadoSubscription = this.caixaStateService.caixaAtualizado$.subscribe(
      (atualizado) => {
        if (atualizado) {
          console.log('🔔 Recebida notificação de atualização do caixa');
          this.verificarCaixaAberto();
          this.caixaStateService.resetarNotificacao();
        }
      }
    );

    // ✅ ESCUTAR NOTIFICAÇÕES DE ATUALIZAÇÃO DOS ALERTAS
    this.alertasAtualizadosSubscription = this.alertasStateService.alertasAtualizados$.subscribe(
      (atualizado) => {
        if (atualizado) {
          console.log('🔔 Recebida notificação de atualização de alertas');
          this.buscarTotalAlertas();
          this.alertasStateService.resetarNotificacao();
        }
      }
    );
  }

  ngOnDestroy(): void {
    console.log('🛑 Sidebar destruído');
    
    if (this.subscription) {
      this.subscription.unsubscribe();
    }

    if (this.caixaAtualizadoSubscription) {
      this.caixaAtualizadoSubscription.unsubscribe();
    }

    // ✅ UNSUBSCRIBE DOS ALERTAS
    if (this.alertasAtualizadosSubscription) {
      this.alertasAtualizadosSubscription.unsubscribe();
    }
  }

  buscarTotalAlertas(): void {
    console.log('🔍 Buscando alertas...');
    this.alertasService.buscarTodosAlertas().subscribe({
      next: (alertas) => {
        const total = this.alertasService.calcularTotalAlertas(alertas);
        console.log('✅ Total de alertas:', total);
        this.totalAlertas = total;

        // ✅ NOVO: detecta checkouts vencidos ainda não avisados nesta sessão
        // e enfileira um pop-up + som pra cada um.
        this.detectarNovosCheckoutsVencidos(alertas.checkoutsVencidos || []);
      },
      error: (err) => {
        console.error('❌ Erro ao buscar alertas:', err);
        this.totalAlertas = 0;
      }
    });
  }

  // ✅ POP-UP DE CHECKOUT VENCIDO ─────────────────────────────

  private detectarNovosCheckoutsVencidos(checkoutsVencidos: AlertaDTO[]): void {
    const novos = checkoutsVencidos.filter(
      a => a.reservaId != null && !this.checkoutVencidoNotificados.has(a.reservaId)
    );

    if (novos.length === 0) {
      return;
    }

    novos.forEach(a => {
      if (a.reservaId != null) {
        this.checkoutVencidoNotificados.add(a.reservaId);
      }
    });

    this.filaPopupsCheckoutVencido.push(...novos);
    this.tocarSomAlerta();

    if (!this.popupCheckoutVencido) {
      this.mostrarProximoPopupCheckoutVencido();
    }
  }

  private mostrarProximoPopupCheckoutVencido(): void {
    this.popupCheckoutVencido = this.filaPopupsCheckoutVencido.shift() || null;
  }

  fecharPopupCheckoutVencido(): void {
    this.mostrarProximoPopupCheckoutVencido();
  }

  irParaReservaPopup(reservaId?: number): void {
    if (reservaId == null) return;
    this.fecharPopupCheckoutVencido();
    this.router.navigate(['/reservas', reservaId]);
  }

  formatarDataHoraPopup(data?: string): string {
    if (!data) return '-';
    const d = new Date(data);
    return d.toLocaleString('pt-BR', {
      day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit'
    });
  }

  private tocarSomAlerta(): void {
    try {
      const ctx = this.obterAudioContext();
      if (!ctx) return;

      if (ctx.state === 'suspended') {
        ctx.resume().catch(() => {});
      }

      const tocarBeep = (frequencia: number, atraso: number) => {
        setTimeout(() => {
          const osc = ctx.createOscillator();
          const gain = ctx.createGain();
          osc.type = 'sine';
          osc.frequency.value = frequencia;
          gain.gain.setValueAtTime(0.3, ctx.currentTime);
          osc.connect(gain);
          gain.connect(ctx.destination);
          osc.start();
          osc.stop(ctx.currentTime + 0.3);
        }, atraso);
      };

      tocarBeep(880, 0);
      tocarBeep(660, 350);
    } catch (e) {
      console.warn('⚠️ Não foi possível tocar o som de alerta:', e);
    }
  }

  // ✅ Reaproveita um único AudioContext (em vez de criar um novo a cada
  // alerta) e prepara o "destravamento" dele: navegadores só permitem tocar
  // áudio automaticamente depois que o usuário interagiu com a página
  // (clique, tecla, toque). Assim que a primeira interação acontecer em
  // QUALQUER lugar da tela, o áudio fica liberado para os próximos alertas.
  private audioCtx?: AudioContext;

  private obterAudioContext(): AudioContext | null {
    try {
      const AudioContextClass = (window as any).AudioContext || (window as any).webkitAudioContext;
      if (!AudioContextClass) return null;
      if (!this.audioCtx) {
        this.audioCtx = new AudioContextClass();
      }
      return this.audioCtx ?? null;
    } catch (e) {
      console.warn('⚠️ AudioContext indisponível:', e);
      return null;
    }
  }

  private prepararDesbloqueioDeAudio(): void {
    const desbloquear = () => {
      const ctx = this.obterAudioContext();
      if (ctx && ctx.state === 'suspended') {
        ctx.resume().catch(() => {});
      }
      document.removeEventListener('click', desbloquear);
      document.removeEventListener('keydown', desbloquear);
      document.removeEventListener('touchstart', desbloquear);
    };

    document.addEventListener('click', desbloquear);
    document.addEventListener('keydown', desbloquear);
    document.addEventListener('touchstart', desbloquear);
  }

  verificarCaixaAberto(): void {
  console.log('🔵 Verificando caixa...');
  
  if (this.verificandoCaixa) {
    return;
  }
  this.verificandoCaixa = true;
  this.subscription = this.fechamentoCaixaService.buscarCaixaAberto(this.usuarioId).subscribe({
    next: (caixa) => {
      this.verificandoCaixa = false;
      if (caixa && caixa.id) {
        this.caixaAberto = caixa;
        console.log('✅ Caixa aberto - ID:', caixa.id);
      } else {
        this.caixaAberto = null;
        console.log('📭 Nenhum caixa aberto');
      }

      // ✅ Verificar outros caixas abertos
      this.http.get<any[]>('/api/fechamento-caixa/todos-abertos').subscribe({
        next: (caixas) => {
          const outrosCaixas = caixas.filter(c => c.usuarioId !== this.usuarioId);
          if (outrosCaixas.length > 0) {
            const nomes = outrosCaixas.map((c: any) => c.usuarioNome).join(', ');
            console.warn(`⚠️ Caixas abertos de outros usuários: ${nomes}`);
            this.avisoOutrosCaixas = `⚠️ Caixa aberto por: ${nomes}`;
          } else {
            this.avisoOutrosCaixas = '';
          }
        },
        error: () => { this.avisoOutrosCaixas = ''; }
      });
    },
    error: () => {
      this.verificandoCaixa = false;
      this.caixaAberto = null;
      console.log('📭 Nenhum caixa aberto (erro)');
    }
  });
}

  logout(): void {
    if (confirm('🚪 Deseja realmente sair?')) {
      localStorage.removeItem('token');
      localStorage.removeItem('usuario');
      localStorage.removeItem('user');
      this.router.navigate(['/login']);
    }
  }
}
