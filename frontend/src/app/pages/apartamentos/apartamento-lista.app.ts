import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApartamentoService } from '../../services/apartamento.service';
import { TipoApartamentoService } from '../../services/tipo-apartamento.service';
import { Apartamento } from '../../models/apartamento.model';
import { RouterModule } from '@angular/router';
import { ReservaService } from '../../services/reserva.service';

@Component({
  selector: 'app-apartamento-lista',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  
  template: `
    <div class="container">
      <div class="header">        
        <h1>🏨 Apartamentos</h1>
        <div class="header-actions">
          <button class="btn-limpeza" (click)="abrirLimpeza()">
            🧹 Gestão de Limpeza
          </button>
          <button class="btn-gestao" routerLink="/apartamentos/gestao">
            🔧 Gestão de Status
          </button>
          <button class="btn-novo" (click)="novoApartamento()">
            ➕ Novo Apartamento
          </button>
        </div>
      </div>

      <!-- CARDS DE RESUMO -->
      <div class="resumo-cards">
        <div class="card-status disponivel" (click)="filtrarPorStatus('DISPONIVEL')">
          <div class="card-icon">✅</div>
          <div class="card-info">
            <span class="card-numero">{{ contarPorStatus('DISPONIVEL') }}</span>
            <span class="card-label">Disponíveis</span>
          </div>
        </div>

        <div class="card-status ocupado" (click)="filtrarPorStatus('OCUPADO')">
          <div class="card-icon">🔴</div>
          <div class="card-info">
            <span class="card-numero">{{ contarPorStatus('OCUPADO') }}</span>
            <span class="card-label">Ocupados</span>
          </div>
        </div>

        <div class="card-status pre-reserva" (click)="filtrarPorStatus('PRE_RESERVA')">
         <div class="card-icon">🔶</div>
         <div class="card-info">
          <span class="card-numero">{{ estatisticas.preReservas }}</span>
          <span class="card-label">Pré-Reservas</span>
         </div>
        </div>

        <div class="card-status limpeza" (click)="filtrarPorStatus('LIMPEZA')">
          <div class="card-icon">🧹</div>
          <div class="card-info">
            <span class="card-numero">{{ contarPorStatus('LIMPEZA') }}</span>
            <span class="card-label">Limpeza</span>
          </div>
        </div>

        <div class="card-status manutencao" (click)="filtrarPorStatus('MANUTENCAO')">
          <div class="card-icon">🔧</div>
          <div class="card-info">
            <span class="card-numero">{{ contarPorStatus('MANUTENCAO') }}</span>
            <span class="card-label">Manutenção</span>
          </div>
        </div>

        <div class="card-status bloqueado" (click)="filtrarPorStatus('INDISPONIVEL')">
          <div class="card-icon">🚫</div>
          <div class="card-info">
            <span class="card-numero">{{ contarPorStatus('INDISPONIVEL') }}</span>
            <span class="card-label">Bloqueados</span>
          </div>
        </div>
      </div>

      <!-- FILTROS -->
      <div class="area-filtros">
        <!-- FILTRO POR STATUS -->
        <div class="filtro-grupo">
          <label>Status:</label>
          <select [(ngModel)]="filtroStatus" (change)="aplicarFiltros()">
            <option value="">Todos os status</option>
            <option value="DISPONIVEL">✅ Disponíveis</option>
            <option value="OCUPADO">🔴 Ocupados</option>
            <option value="PRE_RESERVA">🔶 Pré-Reserva</option>
            <option value="LIMPEZA">🧹 Limpeza</option>            
            <option value="MANUTENCAO">🔧 Manutenção</option>
            <option value="INDISPONIVEL">🚫 Bloqueados</option>
          </select>
        </div>

        <!-- FILTRO POR TIPO -->
        <div class="filtro-grupo">
          <label>Tipo de Apartamento:</label>
          <select [(ngModel)]="filtroTipo" (change)="aplicarFiltros()">
            <option value="">Todos os tipos</option>
            <option *ngFor="let tipo of tiposApartamento" [value]="tipo.tipo">
              {{ tipo.tipo }} - {{ tipo.descricao }}
            </option>
          </select>
        </div>

        <!-- BUSCA POR NÚMERO -->
        <div class="filtro-grupo">
          <label>Buscar número:</label>
          <input 
            type="text" 
            placeholder="Ex: 101, 202..."
            [(ngModel)]="filtroNumero"
            (input)="aplicarFiltros()"
          />
        </div>

        <!-- LIMPAR FILTROS -->
        <button 
          class="btn-limpar-filtros" 
          *ngIf="temFiltrosAtivos()"
          (click)="limparFiltros()">
          🗑️ Limpar Filtros
        </button>
      </div>

      <!-- LOADING -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Carregando apartamentos...</p>
      </div>

      <!-- GRID DE APARTAMENTOS -->
      <div *ngIf="!loading" class="apartamentos-grid">
        <div *ngFor="let apt of apartamentosFiltrados" 
             [class]="'apartamento-card status-' + apt.status?.toLowerCase()">
          
          <!-- CABEÇALHO -->
          <div class="card-header">
            <h2 
              [class]="apt.reservaAtiva ? 'numero-clickable' : ''"
              (click)="abrirReserva(apt)"
              [title]="apt.reservaAtiva ? 'Ver detalhes da reserva' : ''">
              {{ apt.numeroApartamento }}
            </h2>
            <span [class]="'badge badge-' + apt.status?.toLowerCase()">
              {{ obterTextoStatus(apt.status || '') }}
            </span>
          </div>

          <!-- INFORMAÇÕES -->
          <div class="card-body">
            <div class="info-row">
              <span class="icon">🏷️</span>
              <span>{{ apt.tipoApartamentoNome }} - {{ apt.tipoApartamentoDescricao }}</span>
            </div>
            <div class="info-row">
              <span class="icon">👥</span>
              <span>{{ apt.capacidade }} pessoa(s)</span>
            </div>
            <div class="info-row">
              <span class="icon">🛏️</span>
              <span>{{ apt.camasDoApartamento }}</span>
            </div>
            <div class="info-row" *ngIf="apt.tv">
              <span class="icon">📺</span>
              <span>{{ apt.tv }}</span>
            </div>
          </div>

          <!-- INFORMAÇÕES DA PRÉ-RESERVA -->
          <div class="pre-reserva-info" *ngIf="apt.status === 'PRE_RESERVA' && apt.reservaAtiva">
            <div class="pre-reserva-titulo">
              <span class="icone-pulsante">🔶</span> Pré-Reserva
            </div>
            <div class="pre-reserva-hospede">
              <strong>👤 {{ apt.reservaAtiva.nomeHospede }}</strong>
            </div>
            <div class="pre-reserva-detalhes">
              <span>👥 {{ apt.reservaAtiva.quantidadeHospede }} hóspede(s)</span>
            </div>
            <div class="pre-reserva-checkin">
              <div class="checkin-badge">
                ✅ Check-in: {{ formatarData(apt.reservaAtiva.dataCheckin) }}
              </div>
            </div>
          </div>

          <!-- INFORMAÇÕES DA RESERVA ATIVA (SE OCUPADO) -->
         <!-- INFORMAÇÕES DA RESERVA ATIVA (OCUPADO) -->
<div class="reserva-info" *ngIf="apt.status === 'OCUPADO' && apt.reservaAtiva">
  <div class="reserva-titulo">📋 Reserva Ativa</div>
  <div class="reserva-hospede">
    <strong>👤 {{ apt.reservaAtiva.nomeHospede }}</strong>
  </div>
  <div class="reserva-detalhes">
    <span>👥 {{ apt.reservaAtiva.quantidadeHospede }} hóspede(s)</span>
  </div>
  <div class="reserva-datas">
    <div>✅ {{ formatarData(apt.reservaAtiva.dataCheckin) }}</div>
    <div>📤 {{ formatarData(apt.reservaAtiva.dataCheckout) }}</div>
  </div>
  
  <!-- ✅ SE TEM PRÉ-RESERVA FUTURA, MOSTRAR SÓ A DATA -->
  <div class="proxima-reserva" *ngIf="apt.preReservaFutura">
    <div class="proxima-titulo">🔶 Próxima Reserva:</div>
    <div class="proxima-data">
      📅 {{ formatarData(apt.preReservaFutura.dataCheckin) }}
    </div>
  </div>
</div>

<!-- INFORMAÇÕES DA PRÉ-RESERVA (SÓ SE NÃO TIVER RESERVA ATIVA) -->
<div class="pre-reserva-info" *ngIf="!apt.reservaAtiva && apt.preReservaFutura">
  <div class="pre-reserva-titulo">
    <span class="icone-pulsante">🔶</span> Pré-Reserva
  </div>
  <div class="pre-reserva-hospede">
    <strong>👤 {{ apt.preReservaFutura.nomeHospede }}</strong>
  </div>
  <div class="pre-reserva-detalhes">
    <span>👥 {{ apt.preReservaFutura.quantidadeHospede }} hóspede(s)</span>
  </div>
  <div class="pre-reserva-checkin">
    <div class="checkin-badge">
      ✅ Check-in: {{ formatarData(apt.preReservaFutura.dataCheckin) }}
    </div>
  </div>
</div>

          <!-- AÇÕES -->
          <div class="card-actions">
            <!-- NOVA RESERVA (DISPONÍVEL) -->
            <button 
              *ngIf="apt.status === 'DISPONIVEL'"
              class="btn-acao btn-reservar"
              (click)="novaReserva(apt)"
              title="Criar nova reserva">
              ➕ Nova Reserva
            </button>

            <!-- VER RESERVA (OCUPADO OU PRÉ-RESERVA) -->
            <button 
              *ngIf="apt.reservaAtiva"
              class="btn-acao btn-ver-reserva"
              (click)="abrirReserva(apt)"
              title="Ver reserva">
              📋 Ver Reserva
            </button>

            <!-- EDITAR -->
            <button 
              class="btn-acao btn-edit"
              (click)="editar(apt.id!)"
              [disabled]="apt.status === 'OCUPADO'"
              [title]="apt.status === 'OCUPADO' ? 'Não pode editar apartamento ocupado' : 'Editar apartamento'">
              ✏️ Editar
            </button>
          </div>
        </div>
      </div>

      <!-- VAZIO -->
      <div *ngIf="!loading && apartamentosFiltrados.length === 0" class="vazio">
        <p>📭 Nenhum apartamento encontrado</p>
        <button class="btn" (click)="limparFiltros()">🔍 Limpar Filtros</button>
      </div>
    </div>
  `,
  
  styles: [`
    .container {
      padding: 20px;
      max-width: 1600px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
    }

    h1 {
      color: #2c3e50;
      margin: 0;
    }

    .header-actions {
      display: flex;
      gap: 10px;
    }

    .btn-limpeza, .btn-gestao, .btn-novo {
      background: #667eea;
      color: white;
      border: none;
      padding: 10px 20px;
      border-radius: 6px;
      cursor: pointer;
      font-size: 14px;
      font-weight: 600;
      transition: all 0.3s;
    }

    .btn-limpeza:hover, .btn-gestao:hover, .btn-novo:hover {
      background: #5568d3;
      transform: translateY(-2px);
    }

    /* CARDS DE RESUMO */
    .resumo-cards {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 15px;
      margin-bottom: 25px;
    }

    .card-status {
      background: white;
      border-radius: 12px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 15px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      transition: all 0.3s;
      cursor: pointer;
      border-left: 4px solid;
    }

    .card-status:hover {
      transform: translateY(-5px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }

    .card-status.disponivel { border-left-color: #27ae60; }
    .card-status.ocupado { border-left-color: #e74c3c; }
    .card-status.pre-reserva { 
      border-left-color: #f39c12; 
      animation: pulseCard 2s ease-in-out infinite;
    }
    .card-status.limpeza { border-left-color: #f39c12; }
    .card-status.manutencao { border-left-color: #9b59b6; }
    .card-status.bloqueado { border-left-color: #95a5a6; }

    @keyframes pulseCard {
      0%, 100% { box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
      50% { box-shadow: 0 4px 20px rgba(243, 156, 18, 0.4); }
    }

    .card-icon {
      font-size: 2.5em;
    }

    .card-info {
      display: flex;
      flex-direction: column;
    }

    .card-numero {
      font-size: 2em;
      font-weight: 700;
      color: #2c3e50;
      line-height: 1;
    }

    .card-label {
      font-size: 0.9em;
      color: #7f8c8d;
      margin-top: 5px;
    }

    /* ÁREA DE FILTROS */
    .area-filtros {
      background: white;
      padding: 20px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      margin-bottom: 25px;
      display: flex;
      gap: 15px;
      flex-wrap: wrap;
      align-items: flex-end;
    }

    .filtro-grupo {
      flex: 1;
      min-width: 200px;
    }

    .filtro-grupo label {
      display: block;
      margin-bottom: 8px;
      font-weight: 600;
      color: #2c3e50;
      font-size: 0.9em;
    }

    .filtro-grupo select,
    .filtro-grupo input {
      width: 100%;
      padding: 10px;
      border: 2px solid #e0e0e0;
      border-radius: 6px;
      font-size: 1em;
      transition: all 0.3s;
    }

    .filtro-grupo select:focus,
    .filtro-grupo input:focus {
      outline: none;
      border-color: #3498db;
    }

    .btn-limpar-filtros {
      padding: 10px 20px;
      background: #e74c3c;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.3s;
      white-space: nowrap;
    }

    .btn-limpar-filtros:hover {
      background: #c0392b;
    }

    /* LOADING */
    .loading {
      text-align: center;
      padding: 60px;
    }

    .spinner {
      border: 4px solid #f3f3f3;
      border-top: 4px solid #3498db;
      border-radius: 50%;
      width: 50px;
      height: 50px;
      animation: spin 1s linear infinite;
      margin: 0 auto 20px;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    /* GRID DE APARTAMENTOS */
    .apartamentos-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 20px;
    }

    .apartamento-card {
      background: white;
      border-radius: 12px;
      padding: 20px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      transition: all 0.3s;
      border-left: 4px solid #e0e0e0;
    }

    .apartamento-card:hover {
      transform: translateY(-3px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }

    .apartamento-card.status-disponivel { border-left-color: #27ae60; }
    .apartamento-card.status-ocupado { border-left-color: #e74c3c; }
    .apartamento-card.status-pre_reserva { 
      border-left-color: #f39c12;
      animation: pulseCardBorder 2s ease-in-out infinite;
    }
    .apartamento-card.status-limpeza { border-left-color: #f39c12; }
    .apartamento-card.status-manutencao { border-left-color: #9b59b6; }

    @keyframes pulseCardBorder {
      0%, 100% { 
        border-left-color: #f39c12;
        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      }
      50% { 
        border-left-color: #e67e22;
        box-shadow: 0 4px 16px rgba(243, 156, 18, 0.3);
      }
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      padding-bottom: 10px;
      border-bottom: 2px solid #e0e0e0;
    }

    .card-header h2 {
      margin: 0;
      color: #2c3e50;
      font-size: 1.8em;
    }

    .numero-clickable {
      color: #3498db;
      cursor: pointer;
      transition: all 0.3s;
    }

    .numero-clickable:hover {
      color: #2980b9;
      transform: scale(1.1);
    }

    .badge {
      padding: 5px 12px;
      border-radius: 12px;
      font-size: 0.75em;
      font-weight: 600;
      text-transform: uppercase;
    }

    .badge-disponivel { background: #d4edda; color: #155724; }
    .badge-ocupado { background: #f8d7da; color: #721c24; }
    .badge-pre_reserva { 
      background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
      color: white;
      animation: pulseBadge 2s ease-in-out infinite;
    }
    .badge-limpeza { background: #fff3cd; color: #856404; }
    .badge-sujo { background: #ffe5d0; color: #8a4b00; }
    .badge-manutencao { background: #e2d5f0; color: #5a3d7a; }
    .badge-indisponivel { background: #d6d8db; color: #383d41; }

    @keyframes pulseBadge {
      0%, 100% { transform: scale(1); opacity: 1; }
      50% { transform: scale(1.05); opacity: 0.9; }
    }

    .card-body {
      margin-bottom: 15px;
    }

    .info-row {
      display: flex;
      align-items: center;
      gap: 10px;
      margin: 8px 0;
      color: #555;
      font-size: 0.9em;
    }

    .info-row .icon {
      font-size: 1.2em;
    }

    /* PRÉ-RESERVA INFO */
    .pre-reserva-info {
      background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
      border-left: 4px solid #f39c12;
      padding: 15px;
      border-radius: 8px;
      margin-bottom: 15px;
      font-size: 0.85em;
      box-shadow: 0 2px 8px rgba(243, 156, 18, 0.2);
    }

    .pre-reserva-titulo {
      font-weight: 700;
      color: #e67e22;
      margin-bottom: 10px;
      font-size: 1.1em;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .icone-pulsante {
      display: inline-block;
      animation: pulseIcon 1.5s ease-in-out infinite;
    }

    @keyframes pulseIcon {
      0%, 100% { transform: scale(1); }
      50% { transform: scale(1.2); }
    }

    .pre-reserva-hospede {
      margin-bottom: 8px;
      color: #2c3e50;
      font-size: 1.05em;
    }

    .pre-reserva-detalhes {
      margin: 6px 0;
      color: #7f8c8d;
    }

    .pre-reserva-checkin {
      margin-top: 10px;
    }

    .checkin-badge {
      display: inline-block;
      padding: 6px 12px;
      background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
      color: white;
      border-radius: 6px;
      font-weight: 600;
      font-size: 0.95em;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    /* RESERVA INFO (OCUPADO) */
    .reserva-info {
      background: #e3f2fd;
      border-left: 3px solid #3498db;
      padding: 12px;
      border-radius: 6px;
      margin-bottom: 15px;
      font-size: 0.85em;
    }

    .reserva-titulo {
      font-weight: 700;
      color: #2c3e50;
      margin-bottom: 8px;
    }

    .reserva-hospede {
      margin-bottom: 6px;
      color: #2c3e50;
    }

    .reserva-detalhes {
      margin: 5px 0;
      color: #7f8c8d;
    }

    .reserva-datas {
      display: flex;
      gap: 10px;
      margin-top: 8px;
    }

    .reserva-datas div {
      padding: 4px 8px;
      background: #c8e6c9;
      border-radius: 4px;
      font-size: 0.9em;
    }

    .card-actions {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    .btn-acao {
      flex: 1;
      padding: 10px;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      font-size: 0.85em;
      transition: all 0.3s;
      min-width: 100px;
    }

    .btn-acao:hover:not(:disabled) {
      transform: translateY(-2px);
    }

    .btn-acao:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .btn-reservar {
      background: #27ae60;
      color: white;
    }

    .btn-reservar:hover {
      background: #229954;
    }

    .btn-ver-reserva {
      background: #3498db;
      color: white;
    }

    .btn-ver-reserva:hover {
      background: #2980b9;
    }

    .btn-edit {
      background: #f39c12;
      color: white;
    }

    .btn-edit:hover:not(:disabled) {
      background: #e67e22;
    }

    .vazio {
      text-align: center;
      padding: 60px;
      color: #7f8c8d;
    }

    .vazio p {
      font-size: 1.2em;
      margin-bottom: 20px;
    }

    .btn {
      background: #3498db;
      color: white;
      border: none;
      padding: 10px 20px;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.3s;
    }

    .btn:hover {
      background: #2980b9;
    }

    @media (max-width: 768px) {
      .resumo-cards {
        grid-template-columns: repeat(2, 1fr);
      }

      .area-filtros {
        flex-direction: column;
      }

      .filtro-grupo {
        width: 100%;
      }

      .apartamentos-grid {
        grid-template-columns: 1fr;
      }

      /* PRÓXIMA RESERVA (dentro de apartamento ocupado) */
.proxima-reserva {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 2px dashed #f39c12;
}

.proxima-titulo {
  font-size: 0.9em;
  font-weight: 700;
  color: #e67e22;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.proxima-data {
  padding: 6px 10px;
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  border-left: 3px solid #f39c12;
  border-radius: 4px;
  font-size: 0.9em;
  font-weight: 600;
  color: #e65100;
}

    }
  `]
})
export class ApartamentoListaApp implements OnInit {
  private apartamentoService = inject(ApartamentoService);
  private tipoApartamentoService = inject(TipoApartamentoService);
  private reservaService = inject(ReservaService);
  private router = inject(Router);

  apartamentos: Apartamento[] = [];
  apartamentosFiltrados: Apartamento[] = [];
  tiposApartamento: any[] = [];

  estatisticas = {          
    ativas: 0,
    preReservas: 0,
    canceladas: 0,
    finalizadas: 0
  };
  
  filtroStatus = '';
  filtroTipo = '';
  filtroNumero = '';
  
  loading = true;

  ngOnInit(): void {
    this.carregarDados();
  }

  carregarDados(): void {
  this.loading = true;
  
  // ✅ Carregar apartamentos
  this.apartamentoService.getAll().subscribe({
    next: (data) => {
      this.apartamentos = data;
      this.apartamentosFiltrados = data;
      
      // ═══════════════════════════════════════════
      // 🔍 DEBUG - APARTAMENTO 205
      // ═══════════════════════════════════════════
      console.log('═══════════════════════════════════════════');
      console.log('🏨 TOTAL DE APARTAMENTOS:', data.length);
      
      const apt205 = data.find(a => a.numeroApartamento === '205');
      
      if (apt205) {
        console.log('');
        console.log('🔍 APARTAMENTO 205 ENCONTRADO:');
        console.log('   📌 ID:', apt205.id);
        console.log('   📌 Número:', apt205.numeroApartamento);
        console.log('   📌 Status:', apt205.status);
        console.log('   📌 Tipo:', apt205.tipoApartamentoDescricao);
        console.log('');
        console.log('   📋 reservaAtiva:', apt205.reservaAtiva);
        console.log('   🔶 preReservaFutura:', apt205.preReservaFutura);
        console.log('');
        console.log('   📦 Objeto completo:', apt205);
      } else {
        console.log('❌ APARTAMENTO 205 NÃO ENCONTRADO NA LISTA');
        console.log('Apartamentos disponíveis:', data.map(a => a.numeroApartamento));
      }
      
      console.log('═══════════════════════════════════════════');
      // ═══════════════════════════════════════════
      
      this.loading = false;
      console.log('✅ Apartamentos carregados:', data);
    },
    error: (err) => {
      console.error('❌ Erro ao carregar apartamentos', err);
      this.loading = false;
    }
  });
  
  // ✅ Carregar tipos
  this.tipoApartamentoService.getAll().subscribe({
    next: (tipos) => {
      this.tiposApartamento = tipos;
      console.log('🏷️ Tipos carregados:', tipos);
    },
    error: (err) => console.error('Erro ao carregar tipos:', err)
  });

  // ✅ Carregar estatísticas de reservas
  this.reservaService.obterEstatisticas().subscribe({
    next: (stats) => {
      this.estatisticas = stats;
      console.log('📊 Estatísticas carregadas:', stats);
    },
    error: (err) => console.error('Erro ao carregar estatísticas:', err)
  });
}

  aplicarFiltros(): void {
  let resultado = [...this.apartamentos];

  // Filtro por status
  if (this.filtroStatus) {
    if (this.filtroStatus === 'PRE_RESERVA') {
      // ✅ Mostra apartamentos que têm PRÉ-RESERVA (ativa ou futura)
      resultado = resultado.filter(a => 
        (a.reservaAtiva && a.reservaAtiva.status === 'PRE_RESERVA') ||
        (a.preReservaFutura && a.preReservaFutura.status === 'PRE_RESERVA')
      );
    } else {
      resultado = resultado.filter(a => a.status === this.filtroStatus);
    }
  }

  // Filtro por tipo
  if (this.filtroTipo) {
    resultado = resultado.filter(a => a.tipoApartamentoNome === this.filtroTipo);
  }

  // Filtro por número
  if (this.filtroNumero) {
    const termo = this.filtroNumero.toLowerCase();
    resultado = resultado.filter(a => 
      a.numeroApartamento.toLowerCase().includes(termo)
    );
  }

  this.apartamentosFiltrados = resultado;
  console.log('🔍 Filtros aplicados:', {
    status: this.filtroStatus,
    tipo: this.filtroTipo,
    numero: this.filtroNumero,
    total: this.apartamentosFiltrados.length
  });
}

  filtrarPorStatus(status: string): void {
    this.filtroStatus = status;
    this.aplicarFiltros();
  }

  limparFiltros(): void {
    this.filtroStatus = '';
    this.filtroTipo = '';
    this.filtroNumero = '';
    this.aplicarFiltros();
  }

  temFiltrosAtivos(): boolean {
    return !!(this.filtroStatus || this.filtroTipo || this.filtroNumero);
  }

  contarPorStatus(status: string): number {
    return this.apartamentos.filter(a => a.status === status).length;
  }

  obterTextoStatus(status: string): string {
    const textos: any = {
      'DISPONIVEL': 'Disponível',
      'OCUPADO': 'Ocupado',
      'PRE_RESERVA': 'Pré-Reserva',
      'LIMPEZA': 'Limpeza',
      'SUJO': 'Sujo',
      'MANUTENCAO': 'Manutenção',
      'INDISPONIVEL': 'Bloqueado'
    };
    return textos[status] || status;
  }

  formatarData(data: string): string {
    return new Date(data).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  // ========== AÇÕES ==========

  novaReserva(apartamento: Apartamento): void {
    console.log('➕ Nova reserva para apartamento:', apartamento.numeroApartamento);
    // Navegar para página de nova reserva com apartamento pré-selecionado
    this.router.navigate(['/reservas/novo'], {
      queryParams: { apartamentoId: apartamento.id }
    });
  }

  abrirReserva(apartamento: Apartamento): void {
    if (apartamento.reservaAtiva) {
      console.log('📋 Abrindo reserva:', apartamento.reservaAtiva.reservaId);
      this.router.navigate(['/reservas', apartamento.reservaAtiva.reservaId]);
    }
  }

  editar(id: number): void {
    this.router.navigate(['/apartamentos/editar', id]);
  }

  novoApartamento(): void {
    this.router.navigate(['/apartamentos/novo']);
  }

  abrirLimpeza(): void {
    this.router.navigate(['/apartamentos/limpeza']);
  }
}