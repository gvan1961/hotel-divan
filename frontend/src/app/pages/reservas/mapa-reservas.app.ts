import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ReservaService } from '../../services/reserva.service';
import { ReservaResponse } from '../../models/reserva.model';

interface ReservaMapa {
  id: number;
  apartamentoId: number;
  apartamentoNumero: string;
  clienteNome: string;
  dataCheckin: string;
  dataCheckout: string;
  status: string;
  quantidadeHospede: number;
}

interface ApartamentoMapa {
  id: number;
  numeroApartamento: string;
  status: string;
  reservas: ReservaMapa[];
}

@Component({
  selector: 'app-mapa-reservas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <!-- HEADER -->
      <div class="header">
        <h1>📅 Mapa de Reservas</h1>
        <div class="header-actions">
          <button class="btn-imprimir" (click)="imprimir()">🖨️ Imprimir Mapa</button>
          <button class="btn-voltar" (click)="voltar()">← Voltar</button>
        </div>
      </div>

      <!-- FILTROS -->
      <div class="filtros">
        <div class="filtro-periodo">
          <label>Período:</label>
          <select [(ngModel)]="periodoSelecionado" (change)="mudarPeriodo()">
            <option value="7">Próximos 7 dias</option>
            <option value="15">Próximos 15 dias</option>
            <option value="30">Próximos 30 dias</option>
            <option value="60">Próximos 60 dias</option>
          </select>
        </div>

        <div class="filtro-data">
          <label>Data inicial:</label>
          <input type="date" [(ngModel)]="dataInicio" (change)="carregarMapa()">
        </div>

        <button class="btn-hoje" (click)="voltarParaHoje()">📅 Hoje</button>
        <button class="btn-atualizar" (click)="carregarMapa()">🔄 Atualizar</button>
      </div>

      <!-- LEGENDA -->
      <div class="legenda">
  <div class="legenda-item">
    <span class="cor-disponivel"></span>
    <span>Disponível</span>
  </div>
  <div class="legenda-item">
    <span class="cor-ocupado"></span>
    <span>Ocupado</span>
  </div>
  <div class="legenda-item">
    <span class="cor-pre-reserva"></span>
    <span>Pré-Reserva</span>
  </div>
  
  <!-- ✅ NOVO -->
  <div class="legenda-item">
    <span class="cor-finalizada"></span>
    <span>Finalizada</span>
  </div>
  
  <!-- ✅ NOVO -->
  <div class="legenda-item">
    <span class="cor-checkout-vencido"></span>
    <span>Checkout Vencido</span>
  </div>
  
  <div class="legenda-item">
    <span class="cor-limpeza"></span>
    <span>Limpeza</span>
  </div>
  <div class="legenda-item">
    <span class="cor-manutencao"></span>
    <span>Manutenção</span>
  </div>
</div>

      <!-- LOADING -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Carregando mapa...</p>
      </div>

      <!-- MAPA DE RESERVAS -->
      <div *ngIf="!loading" class="mapa-container">
        <div class="mapa-scroll">
          <table class="mapa-table">
            <!-- CABEÇALHO COM DATAS -->
            <thead>
              <tr>
                <th class="col-apartamento">Apt</th>
                <th *ngFor="let data of datas" class="col-data" [class.hoje]="isHoje(data)">
                  <div class="data-header">
                    <span class="dia-semana">{{ getDiaSemana(data) }}</span>
                    <span class="dia-mes">{{ getDiaMes(data) }}</span>
                  </div>
                </th>
              </tr>
            </thead>

            <!-- CORPO COM APARTAMENTOS -->
            <tbody>
  <tr *ngFor="let apt of apartamentos">
    <td class="col-apartamento">
      <div class="apt-info">
        <span class="apt-numero">{{ apt.numeroApartamento }}</span>
      </div>
    </td>
    
    <!-- ✅ NOVA LÓGICA: Processar células com colspan -->
    <ng-container *ngFor="let data of datas; let i = index">
      <ng-container *ngIf="!isCelulaOculta(apt, data)">
        <td [attr.colspan]="getColspan(apt, data)"
            class="col-reserva"
            [class.hoje]="isHoje(data)"
            (click)="clicarCelula(apt, data)">
          
          <div class="celula-reserva" 
               [class]="getClasseReserva(apt, data)"
               [title]="getTituloReserva(apt, data)">
            
            <span class="reserva-info" *ngIf="getReservaInfo(apt, data)">
              {{ getReservaInfo(apt, data) }}
            </span>
          </div>
        </td>
      </ng-container>
    </ng-container>
  </tr>
</tbody>
          </table>
        </div>
      </div>

      <!-- MODAL DETALHES -->
      <div class="modal-overlay" *ngIf="modalDetalhes" (click)="fecharModal()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>{{ modalTitulo }}</h2>
          
          <div *ngIf="reservaSelecionada" class="modal-info">
            <p><strong>Reserva:</strong> #{{ reservaSelecionada.id }}</p>
            <p><strong>Cliente:</strong> {{ reservaSelecionada.clienteNome }}</p>
            <p><strong>Apartamento:</strong> {{ reservaSelecionada.apartamentoNumero }}</p>
            <p><strong>Check-in:</strong> {{ formatarData(reservaSelecionada.dataCheckin) }}</p>
            <p><strong>Check-out:</strong> {{ formatarData(reservaSelecionada.dataCheckout) }}</p>
            <p><strong>Hóspedes:</strong> {{ reservaSelecionada.quantidadeHospede }}</p>
            <p><strong>Status:</strong> <span [class]="'badge-' + reservaSelecionada.status.toLowerCase()">{{ reservaSelecionada.status }}</span></p>
            
            <!-- AVISO PARA PRÉ-RESERVA -->
            <div class="aviso-pre-reserva" *ngIf="reservaSelecionada.status === 'PRE_RESERVA'">
              ℹ️ Esta é uma pré-reserva. Você pode pagar para ativar, editar ou excluir.
            </div>
          </div>

          <div *ngIf="!reservaSelecionada" class="modal-info">
            <p><strong>Apartamento:</strong> {{ apartamentoSelecionado?.numeroApartamento }}</p>
            <p><strong>Data:</strong> {{ formatarDataSimples(dataSelecionada) }}</p>
            
            <div class="alerta-sucesso">
              <p class="texto-disponivel">✅ Este apartamento está LIVRE nesta data!</p>
              <p class="texto-info">Você pode criar uma reserva para este dia.</p>
            </div>
          </div>

          <div class="modal-footer">
            <!-- COLUNA ESQUERDA: Fechar -->
            <button class="btn-cancelar" (click)="fecharModal()">Fechar</button>
            
            <!-- COLUNA DIREITA: Ações -->
            <div class="acoes-direita">
              <!-- ✅ PAGAR E ATIVAR - PRIMEIRO (mais importante para PRE_RESERVA) -->
              <button *ngIf="reservaSelecionada && reservaSelecionada.status === 'PRE_RESERVA'" 
                      class="btn-pagar" 
                      (click)="abrirModalPagamento()">
                💳 Pagar e Ativar
              </button>
              
              <!-- EDITAR -->
              <button *ngIf="reservaSelecionada && reservaSelecionada.status === 'PRE_RESERVA'" 
                      class="btn-editar" 
                      (click)="editarPreReserva()">
                ✏️ Editar
              </button>
              
              <!-- CANCELAR (mantém histórico) -->
                <button *ngIf="reservaSelecionada && reservaSelecionada.status === 'PRE_RESERVA'" 
                  class="btn-cancelar-reserva" 
                  (click)="abrirModalCancelar()">
                  ❌ Cancelar
              </button>

              <!-- EXCLUIR (remove permanentemente) -->
                <button *ngIf="reservaSelecionada && reservaSelecionada.status === 'PRE_RESERVA'" 
                class="btn-excluir" 
                (click)="excluirPreReserva()">
                🗑️ Excluir
              </button>
              
              <!-- VER DETALHES COMPLETOS -->
              <button *ngIf="reservaSelecionada" 
                      class="btn-ver-detalhes" 
                      (click)="verDetalhesReserva()">
                📋 Detalhes Completos
              </button>
              
              <!-- CRIAR RESERVA (para disponíveis) -->
              <button *ngIf="!reservaSelecionada && podeReservar()" 
                      class="btn-criar-reserva" 
                      (click)="criarNovaReserva()">
                ➕ Criar Reserva
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- MODAL PAGAMENTO PRÉ-RESERVA -->
      <div class="modal-overlay" *ngIf="modalPagamento" (click)="fecharModalPagamento()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>💳 Pagamento de Pré-Reserva</h2>
          
          <div class="info-box" *ngIf="reservaSelecionada">
            <p><strong>Reserva:</strong> #{{ reservaSelecionada.id }}</p>
            <p><strong>Cliente:</strong> {{ reservaSelecionada.clienteNome }}</p>
            <p><strong>Apartamento:</strong> {{ reservaSelecionada.apartamentoNumero }}</p>
            <p><strong>Check-in:</strong> {{ formatarData(reservaSelecionada.dataCheckin) }}</p>
            <p><strong>Check-out:</strong> {{ formatarData(reservaSelecionada.dataCheckout) }}</p>
            <p><strong>Hóspedes:</strong> {{ reservaSelecionada.quantidadeHospede }}</p>
          </div>

          <div class="aviso-ativacao">
            ✅ Após o pagamento, a reserva será <strong>ATIVADA</strong> automaticamente e o apartamento ficará <strong>OCUPADO</strong>.
          </div>
          
          <div class="campo">
            <label>Valor a Pagar *</label>
            <input type="number" [(ngModel)]="pagPreReservaValor" step="0.01" min="0">
            <small>Valor total da hospedagem</small>
          </div>

          <div class="campo">
            <label>Forma de Pagamento *</label>
            <select [(ngModel)]="pagPreReservaFormaPagamento">
  <option value="">Selecione...</option>
  <option value="DINHEIRO">💵 Dinheiro</option>
  <option value="PIX">📱 PIX</option>
  <option value="CARTAO_DEBITO">💳 Cartão Débito</option>
  <option value="CARTAO_CREDITO">💳 Cartão Crédito</option>
  <option value="TRANSFERENCIA_BANCARIA">🏦 Transferência</option>
  <option value="LINK_PIX">🔗 Link Pix</option>
  <option value="LINK_CARTAO">🔗 Link Cartão</option>
</select>
          </div>

          <div class="campo">
            <label>Observação</label>
            <textarea [(ngModel)]="pagPreReservaObs" rows="3" 
                      placeholder="Observações sobre o pagamento (opcional)..."></textarea>
          </div>

          <div class="modal-footer">
            <button class="btn-cancelar" (click)="fecharModalPagamento()">
              Cancelar
            </button>
            <button class="btn-confirmar-pagamento" (click)="confirmarPagamentoPreReserva()">
              💳 Confirmar Pagamento
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL CANCELAR PRÉ-RESERVA -->
<div class="modal-overlay" *ngIf="modalCancelar" (click)="fecharModalCancelar()">
  <div class="modal-content modal-pequeno" (click)="$event.stopPropagation()">
    <h2>❌ Cancelar Pré-Reserva</h2>
    
    <div class="info-box" *ngIf="reservaSelecionada">
      <p><strong>Reserva:</strong> #{{ reservaSelecionada.id }}</p>
      <p><strong>Cliente:</strong> {{ reservaSelecionada.clienteNome }}</p>
      <p><strong>Check-in:</strong> {{ formatarData(reservaSelecionada.dataCheckin) }}</p>
    </div>

    <div class="aviso-cancelamento">
      ⚠️ A reserva será <strong>CANCELADA</strong> mas permanecerá no histórico.
    </div>
    
    <div class="campo">
      <label>Motivo do Cancelamento *</label>
      <textarea [(ngModel)]="motivoCancelamento" 
                rows="3" 
                placeholder="Informe o motivo do cancelamento..."
                required></textarea>
    </div>

    <div class="modal-footer">
      <button class="btn-cancelar" (click)="fecharModalCancelar()">
        Voltar
      </button>
      <button class="btn-confirmar-cancelamento" 
              (click)="confirmarCancelamento()"
              [disabled]="!motivoCancelamento || motivoCancelamento.trim() === ''">
        ❌ Confirmar Cancelamento
      </button>
    </div>
  </div>
</div>

    </div>
  `,
  styles: [`
/* ═══════════════════════════════════════════════════════════ */
/* LAYOUT PRINCIPAL */
/* ═══════════════════════════════════════════════════════════ */

.container {
  padding: 20px;
  max-width: 100%;
  margin: 0 auto;
  background: #f5f7fa;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

h1 {
  margin: 0;
  color: #2c3e50;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* ═══════════════════════════════════════════════════════════ */
/* BOTÕES */
/* ═══════════════════════════════════════════════════════════ */

.btn-voltar,
.btn-imprimir,
.btn-hoje,
.btn-atualizar {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
  color: white;
}

.btn-voltar,
.btn-imprimir {
  background: #95a5a6;
}

.btn-voltar:hover,
.btn-imprimir:hover {
  background: #7f8c8d;
}

.btn-hoje {
  background: #3498db;
}

.btn-hoje:hover {
  background: #2980b9;
}

.btn-atualizar {
  background: #27ae60;
}

.btn-atualizar:hover {
  background: #229954;
}

/* ═══════════════════════════════════════════════════════════ */
/* FILTROS */
/* ═══════════════════════════════════════════════════════════ */

.filtros {
  display: flex;
  gap: 15px;
  align-items: center;
  background: white;
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 15px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  flex-wrap: wrap;
}

.filtro-periodo,
.filtro-data {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filtros label {
  font-weight: 600;
  color: #2c3e50;
}

.filtros select,
.filtros input[type="date"] {
  padding: 8px 12px;
  border: 2px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

/* ═══════════════════════════════════════════════════════════ */
/* LEGENDA */
/* ═══════════════════════════════════════════════════════════ */

.legenda {
  display: flex;
  gap: 20px;
  background: white;
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  flex-wrap: wrap;
}

.legenda-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.legenda-item span:first-child {
  width: 30px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.cor-disponivel { background: #d4edda; }
.cor-ocupado { background: #f8d7da; }
.cor-pre-reserva { background: #d1ecf1; }
.cor-finalizada { background: #cce5ff; }
.cor-checkout-vencido { background: #d6001c; }
.cor-limpeza { background: #fff3cd; }
.cor-manutencao { background: #e2d5f0; }

/* ═══════════════════════════════════════════════════════════ */
/* LOADING */
/* ═══════════════════════════════════════════════════════════ */

.loading {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 12px;
}

.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
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

/* ═══════════════════════════════════════════════════════════ */
/* TABELA DO MAPA */
/* ═══════════════════════════════════════════════════════════ */

.mapa-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
}

.mapa-scroll {
  overflow-x: auto;
  overflow-y: auto;
  max-height: calc(100vh - 350px);
}

.mapa-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  min-width: 800px;
}

/* CABEÇALHO */
thead {
  position: sticky;
  top: 0;
  z-index: 10;
  background: white;
}

th {
  background: #2c3e50;
  color: white;
  padding: 12px 8px;
  text-align: center;
  border: 1px solid #34495e;
  font-weight: 600;
  font-size: 0.9em;
}

.col-apartamento {
  position: sticky;
  left: 0;
  z-index: 20;
  background: #2c3e50 !important;
  min-width: 80px;
  max-width: 80px;
}

.col-data {
  min-width: 80px;
  max-width: 80px;
}

.col-data.hoje {
  background: #667eea !important;
}

.data-header {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dia-semana {
  font-size: 0.75em;
  opacity: 0.9;
}

.dia-mes {
  font-size: 1em;
  font-weight: 700;
}

/* CORPO DA TABELA */
tbody tr {
  border-bottom: 1px solid #ecf0f1;
}

tbody tr:hover {
  background: #f8f9fa;
}

td {
  padding: 0;
  margin: 0;
  text-align: center;
  vertical-align: middle;
  height: 50px;
}

td.col-apartamento {
  position: sticky;
  left: 0;
  z-index: 5;
  background: white;
  font-weight: 600;
  color: #2c3e50;
  border-right: 2px solid #bdc3c7;
  padding: 12px 8px;
}

.apt-info {
  padding: 0;
}

.apt-numero {
  font-size: 1.1em;
  font-weight: 700;
  color: #667eea;
}

/* CÉLULAS DE RESERVA */
td.col-reserva {
  cursor: pointer;
  transition: all 0.2s;
  padding: 0 !important;
  border: none !important;
  position: relative;
}

td.col-reserva.hoje {
  background: #e3f2fd;
}

.celula-reserva {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85em;
  font-weight: 600;
  transition: all 0.2s;
  position: relative;
  margin: 2px;
}

.celula-reserva:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
  z-index: 100;
}

.reserva-info {
  font-size: 0.75em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0 8px;
}

/* ═══════════════════════════════════════════════════════════ */
/* CORES DAS CÉLULAS */
/* ═══════════════════════════════════════════════════════════ */

.celula-disponivel {
  background: #d4edda;
  color: #155724;
}

.celula-ocupado {
  background: #f8d7da;
  color: #721c24;
}

.celula-pre-reserva {
  background: #d1ecf1;
  color: #0c5460;
}

.celula-pre-reserva-hoje {
  background: linear-gradient(135deg, #ff9800 0%, #ff6f00 100%);
  color: white;
  font-weight: 700;
  animation: pulseAlert 2s ease-in-out infinite;
  box-shadow: 0 0 15px rgba(255, 152, 0, 0.6);
}

.celula-pre-reserva-amanha {
  background: linear-gradient(135deg, #ffd54f 0%, #ffc107 100%);
  color: #333;
  font-weight: 700;
  animation: pulseAlert 3s ease-in-out infinite;
}

.celula-finalizada {
  background: #cce5ff;
  color: #004085;
  font-style: italic;
}

.celula-checkout-vencido {
  background: #d6001c;
  color: white;
  font-weight: 700;
  animation: pulseAlert 2s ease-in-out infinite;
}

.celula-limpeza {
  background: #fff3cd;
  color: #856404;
}

.celula-manutencao {
  background: #e2d5f0;
  color: #5a3d7a;
}

.celula-indisponivel {
  background: #d6d8db;
  color: #383d41;
}

@keyframes pulseAlert {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.9;
  }
}

/* ═══════════════════════════════════════════════════════════ */
/* VISUAL DE RESERVA CONTÍNUA */
/* ═══════════════════════════════════════════════════════════ */

/* Reserva de dia único - arredondar tudo */
.reserva-unico {
  border-radius: 6px;
  margin: 2px;
}


/* ═══════════════════════════════════════════════════════════ */
/* MODAIS */
/* ═══════════════════════════════════════════════════════════ */

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 30px;
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0,0,0,0.3);
}

.modal-pequeno {
  max-width: 500px !important;
}

.modal-content h2 {
  margin: 0 0 20px 0;
  color: #2c3e50;
}

.modal-info {
  margin-bottom: 20px;
}

.modal-info p {
  margin: 10px 0;
  color: #2c3e50;
}

.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #ecf0f1;
}

.acoes-direita {
  display: flex;
  gap: 10px;
}

/* AVISOS E ALERTAS */
.aviso-pre-reserva,
.info-box {
  background: #e3f2fd;
  border-left: 4px solid #2196f3;
  padding: 12px;
  border-radius: 6px;
  margin-top: 15px;
  color: #1976d2;
  font-weight: 600;
}

.aviso-ativacao,
.aviso-cancelamento {
  background: #fff3cd;
  border-left: 4px solid #ffc107;
  padding: 12px;
  margin: 15px 0;
  border-radius: 4px;
  font-size: 14px;
  color: #856404;
}

.aviso-cancelamento strong {
  color: #d63031;
}

.alerta-sucesso {
  background: #d4edda;
  border-left: 4px solid #28a745;
  padding: 15px;
  border-radius: 6px;
  margin: 15px 0;
}

.texto-disponivel {
  color: #27ae60;
  font-weight: 600;
  font-size: 1.1em;
  margin-top: 15px;
}

.texto-info {
  color: #155724;
  font-size: 0.95em;
  margin-top: 8px;
}

/* BADGES */
.badge-ativa {
  background: #d4edda;
  color: #155724;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 600;
}

.badge-pre_reserva {
  background: #d1ecf1;
  color: #0c5460;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 600;
}

.badge-finalizada {
  background: #cce5ff;
  color: #004085;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 600;
}

/* CAMPOS DE FORMULÁRIO */
.campo {
  margin-bottom: 20px;
}

.campo label {
  display: block;
  margin-bottom: 8px;
  color: #2c3e50;
  font-weight: 600;
}

.campo input[type="number"],
.campo select,
.campo textarea {
  width: 100%;
  padding: 10px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  font-size: 1em;
  transition: all 0.3s;
  box-sizing: border-box;
  font-family: inherit;
}

.campo input:focus,
.campo select:focus,
.campo textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.campo small {
  display: block;
  margin-top: 5px;
  color: #7f8c8d;
  font-size: 0.9em;
}

.campo textarea {
  resize: vertical;
  min-height: 80px;
}

/* BOTÕES DO MODAL */
.btn-cancelar,
.btn-excluir,
.btn-editar,
.btn-ver-detalhes,
.btn-criar-reserva,
.btn-pagar,
.btn-confirmar-pagamento,
.btn-cancelar-reserva,
.btn-confirmar-cancelamento {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
  color: white;
}

.btn-cancelar {
  background: #95a5a6;
}

.btn-cancelar:hover {
  background: #7f8c8d;
}

.btn-excluir,
.btn-cancelar-reserva,
.btn-confirmar-cancelamento {
  background: #e74c3c;
}

.btn-excluir:hover,
.btn-cancelar-reserva:hover,
.btn-confirmar-cancelamento:hover:not(:disabled) {
  background: #c0392b;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.2);
}

.btn-confirmar-cancelamento:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-editar {
  background: #ff9800;
}

.btn-editar:hover {
  background: #f57c00;
  transform: translateY(-2px);
}

.btn-ver-detalhes {
  background: #3498db;
}

.btn-ver-detalhes:hover {
  background: #2980b9;
  transform: translateY(-2px);
}

.btn-criar-reserva,
.btn-pagar,
.btn-confirmar-pagamento {
  background: #27ae60;
}

.btn-criar-reserva:hover,
.btn-pagar:hover,
.btn-confirmar-pagamento:hover {
  background: #229954;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(39, 174, 96, 0.3);
}

/* ═══════════════════════════════════════════════════════════ */
/* RESPONSIVO */
/* ═══════════════════════════════════════════════════════════ */

@media (max-width: 768px) {
  .filtros {
    flex-direction: column;
    align-items: stretch;
  }

  .filtro-periodo,
  .filtro-data {
    flex-direction: column;
    align-items: stretch;
  }

  .legenda {
    flex-direction: column;
  }

  .mapa-scroll {
    max-height: calc(100vh - 450px);
  }

  .modal-footer {
    flex-direction: column;
    align-items: stretch;
  }
  
  .acoes-direita {
    flex-direction: column;
  }
}

/* ═══════════════════════════════════════════════════════════ */
/* IMPRESSÃO */
/* ═══════════════════════════════════════════════════════════ */

@media print {
  .header,
  .filtros,
  .legenda,
  .btn-voltar,
  .btn-imprimir,
  .btn-hoje,
  .btn-atualizar {
    display: none !important;
  }

  .container {
    background: white;
    padding: 10px;
  }

  .mapa-scroll {
    max-height: none;
    overflow: visible;
  }
}
`]
})
export class MapaReservasApp implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  private reservaService = inject(ReservaService);

  loading = false;
  apartamentos: ApartamentoMapa[] = [];
  datas: string[] = [];
  dataInicio = '';
  periodoSelecionado = '15';

  // Modal Detalhes
  modalDetalhes = false;
  modalTitulo = '';
  reservaSelecionada: ReservaMapa | null = null;
  apartamentoSelecionado: ApartamentoMapa | null = null;
  dataSelecionada = '';

  // Modal Pagamento
  modalPagamento = false;
  pagPreReservaValor = 0;
  pagPreReservaFormaPagamento = '';
  pagPreReservaObs = '';

  // ✅ ADICIONAR ESTAS LINHAS:
// Modal Cancelar
modalCancelar = false;
motivoCancelamento = '';

  mapaReservas: Map<string, ReservaMapa> = new Map();

  ngOnInit(): void {
    this.voltarParaHoje();
  }

  verDetalhesReserva(): void {
  if (!this.reservaSelecionada) {
    alert('❌ Erro: Nenhuma reserva selecionada');
    return;
  }

  const reservaId = this.reservaSelecionada.id;

  setTimeout(() => {
    this.fecharModal();
  }, 100);

  setTimeout(() => {
    this.router.navigate(['/reservas', reservaId]);
  }, 200);
}

  voltarParaHoje(): void {
  // ✅ Usar data LOCAL (não UTC)
  const agora = new Date();
  const ano = agora.getFullYear();
  const mes = String(agora.getMonth() + 1).padStart(2, '0');
  const dia = String(agora.getDate()).padStart(2, '0');
  this.dataInicio = `${ano}-${mes}-${dia}`;
  this.carregarMapa();
}

  mudarPeriodo(): void {
    this.carregarMapa();
  }

  /**
 * Verifica se uma pré-reserva é HOJE
 */
isPreReservaHoje(reserva: ReservaMapa): boolean {
  if (reserva.status !== 'PRE_RESERVA') return false;
  
  const hoje = new Date().toISOString().split('T')[0];
  const checkin = new Date(reserva.dataCheckin).toISOString().split('T')[0];
  
  return checkin === hoje;
}

/**
 * Verifica se uma pré-reserva é AMANHÃ
 */
isPreReservaAmanha(reserva: ReservaMapa): boolean {
  if (reserva.status !== 'PRE_RESERVA') return false;
  
  const amanha = new Date();
  amanha.setDate(amanha.getDate() + 1);
  const amanhaStr = amanha.toISOString().split('T')[0];
  
  const checkin = new Date(reserva.dataCheckin).toISOString().split('T')[0];
  
  return checkin === amanhaStr;
}

  carregarMapa(): void {
  this.loading = true;
  this.mapaReservas.clear();

  this.gerarDatas();

  console.log('═══════════════════════════════════════');
  console.log('📋 CARREGANDO MAPA DE RESERVAS');
  console.log('═══════════════════════════════════════');

  // 1️⃣ BUSCAR APARTAMENTOS
  this.http.get<any[]>('/api/apartamentos').subscribe({
    next: (apartamentos) => {
      console.log('🏢 Apartamentos carregados:', apartamentos.length);

      // 2️⃣ BUSCAR RESERVAS DO MAPA (apenas ATIVAS e PRÉ-RESERVAS)
      this.reservaService.buscarParaMapa().subscribe({
        next: (reservas: any[]) => {
          console.log('📋 Total de reservas no mapa:', reservas.length);

          // ✅ LOG DA PRIMEIRA RESERVA PARA VER ESTRUTURA
          if (reservas.length > 0) {
            console.log('🔍 ESTRUTURA DA PRIMEIRA RESERVA:');
            console.log(JSON.stringify(reservas[0], null, 2));
          }

          // 3️⃣ PROCESSAR APARTAMENTOS
          this.apartamentos = apartamentos
            .filter(apt => apt.status !== 'INDISPONIVEL')
            .map(apt => ({
              id: apt.id,
              numeroApartamento: apt.numeroApartamento,
              status: apt.status,
              reservas: []
            }))
            .sort((a, b) => {
              const numA = parseInt(a.numeroApartamento) || 0;
              const numB = parseInt(b.numeroApartamento) || 0;
              return numA - numB;
            });

          console.log('🏠 Apartamentos processados:', this.apartamentos.length);

          // 4️⃣ PROCESSAR RESERVAS (backend já filtrou, não precisa filtrar aqui)
          console.log('✅ Reservas do mapa:', reservas.length);

          reservas.forEach((reserva, index) => {
            console.log(`\n📌 Processando reserva ${index + 1}:`);
            console.log('   ID:', reserva.id);
            console.log('   Status:', reserva.status);

            // ✅ DETECTAR ESTRUTURA CORRETA
            const apartamentoId = reserva.apartamento?.id || reserva.apartamentoId;
            const apartamentoNumero = reserva.apartamento?.numeroApartamento || reserva.apartamentoNumero;
            const clienteNome = reserva.cliente?.nome || reserva.clienteNome;

            console.log('   Apartamento ID:', apartamentoId);
            console.log('   Apartamento Nº:', apartamentoNumero);
            console.log('   Cliente:', clienteNome);

            if (!apartamentoId) {
              console.error('   ❌ APARTAMENTO ID NÃO ENCONTRADO!');
              return;
            }
           
            // ✅ CONVERTER DATAS
            const checkinStr = reserva.dataCheckin;
            const checkoutStr = reserva.dataCheckout;

            console.log('   Check-in (original):', checkinStr);
            console.log('   Check-out (original):', checkoutStr);

            const checkin = new Date(checkinStr.substring(0, 10) + 'T00:00:00');
            const checkout = new Date(checkoutStr.substring(0, 10) + 'T00:00:00');

            console.log('   Check-in (Date):', checkin.toISOString());
            console.log('   Check-out (Date):', checkout.toISOString());

            // ✅ MAPEAR CADA DIA DA RESERVA
            let diasMapeados = 0;
            
            for (let d = new Date(checkin); d < checkout; d.setDate(d.getDate() + 1)) {
              const dataStr = d.toISOString().split('T')[0];
              const chave = `${apartamentoId}-${dataStr}`;

              this.mapaReservas.set(chave, {
                id: reserva.id,
                apartamentoId: apartamentoId,
                apartamentoNumero: apartamentoNumero,
                clienteNome: clienteNome || 'Sem nome',
                dataCheckin: reserva.dataCheckin,
                dataCheckout: reserva.dataCheckout,
                status: reserva.status,
                quantidadeHospede: reserva.quantidadeHospede || 1
              });

              diasMapeados++;
            }

            console.log(`   ✅ ${diasMapeados} dias mapeados`);
          });

          console.log('\n═══════════════════════════════════════');
          console.log('✅ MAPA CARREGADO COM SUCESSO!');
          console.log('📊 Total de células preenchidas:', this.mapaReservas.size);
          console.log('═══════════════════════════════════════');

          this.loading = false;
        },
        error: (err) => {
          console.error('❌ Erro ao carregar reservas:', err);
          alert('❌ Erro ao carregar reservas do servidor');
          this.loading = false;
        }
      });
    },
    error: (err) => {
      console.error('❌ Erro ao carregar apartamentos:', err);
      alert('❌ Erro ao carregar apartamentos do servidor');
      this.loading = false;
    }
  });
}

  gerarDatas(): void {
    const dias = parseInt(this.periodoSelecionado);
    const inicio = new Date(this.dataInicio);
    this.datas = [];

    for (let i = 0; i < dias; i++) {
      const data = new Date(inicio);
      data.setDate(data.getDate() + i);
      this.datas.push(data.toISOString().split('T')[0]);
    }

    console.log('📅 Datas geradas:', this.datas.length);
  }

  getDiaSemana(data: string): string {
    const d = new Date(data + 'T00:00:00');
    return d.toLocaleDateString('pt-BR', { weekday: 'short' }).toUpperCase();
  }

  getDiaMes(data: string): string {
    const d = new Date(data + 'T00:00:00');
    return d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
  }

  isHoje(data: string): boolean {
    const hoje = new Date().toISOString().split('T')[0];
    return data === hoje;
  }

  getClasseReserva(apt: ApartamentoMapa, data: string): string {
  const chave = `${apt.id}-${data}`;
  const reserva = this.mapaReservas.get(chave);

  if (reserva) {
    if (reserva.status === 'ATIVA') {
      return 'celula-ocupado';
    } else if (reserva.status === 'PRE_RESERVA') {
      if (this.isPreReservaHoje(reserva)) {
        return 'celula-pre-reserva-hoje';
      } else if (this.isPreReservaAmanha(reserva)) {
        return 'celula-pre-reserva-amanha';
      } else {
        return 'celula-pre-reserva';
      }
    } else if (reserva.status === 'FINALIZADA') {
      return 'celula-finalizada';
    } else if (reserva.status === 'CHECKOUT_VENCIDO') {
      return 'celula-checkout-vencido';
    }
  }

  const hoje = new Date().toISOString().split('T')[0];
  const dataClicada = new Date(data + 'T00:00:00');
  const dataHoje = new Date(hoje + 'T00:00:00');

  if (dataClicada > dataHoje) {
    return 'celula-disponivel';
  }

  if (apt.status === 'LIMPEZA') return 'celula-limpeza';
  if (apt.status === 'MANUTENCAO') return 'celula-manutencao';

  return 'celula-disponivel';
}

  getTituloReserva(apt: ApartamentoMapa, data: string): string {
    const chave = `${apt.id}-${data}`;
    const reserva = this.mapaReservas.get(chave);

    if (reserva) {
      const checkin = this.formatarData(reserva.dataCheckin);
      const checkout = this.formatarData(reserva.dataCheckout);
      
      return `🔴 OCUPADO\n\n` +
             `Reserva #${reserva.id}\n` +
             `Cliente: ${reserva.clienteNome}\n` +
             `Hóspedes: ${reserva.quantidadeHospede}\n\n` +
             `Check-in: ${checkin}\n` +
             `Check-out: ${checkout}`;
    }

    const dataObj = new Date(data + 'T00:00:00');
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);

    if (dataObj >= hoje) {
      return `✅ DISPONÍVEL\n\n` +
             `Apartamento ${apt.numeroApartamento}\n` +
             `Data: ${this.getDiaMes(data)}\n\n` +
             `Clique para criar uma reserva`;
    }

    return `Apt ${apt.numeroApartamento} - ${data}`;
  }

 getReservaInfo(apt: ApartamentoMapa, data: string): string {
  const chave = `${apt.id}-${data}`;
  const reserva = this.mapaReservas.get(chave);

  if (!reserva) {
    return '';
  }

  // Com colspan, só mostramos no primeiro dia (que é a única célula renderizada)
  const primeiroNome = reserva.clienteNome.split(' ')[0];
  
  // Adicionar badges de alerta
  if (this.isPreReservaHoje(reserva)) {
    return '🔔 HOJE - ' + primeiroNome;
  }
  
  if (this.isPreReservaAmanha(reserva)) {
    return '⏰ AMANHÃ - ' + primeiroNome;
  }
  
  return primeiroNome;
}

/**
 * ✅ Determina a posição do dia dentro da reserva (inicio, meio, fim)
 */
getPosicaoNaReserva(apt: ApartamentoMapa, data: string): string {
  const chave = `${apt.id}-${data}`;
  const reserva = this.mapaReservas.get(chave);
  
  if (!reserva) return '';
  
  const dataAtual = new Date(data + 'T00:00:00');
  const dataCheckin = new Date(reserva.dataCheckin);
  const dataCheckout = new Date(reserva.dataCheckout);
  
  // Normalizar
  dataAtual.setHours(0, 0, 0, 0);
  dataCheckin.setHours(0, 0, 0, 0);
  dataCheckout.setHours(0, 0, 0, 0);
  
  const isPrimeiroDia = dataAtual.getTime() === dataCheckin.getTime();
  
  // Calcular último dia (checkout - 1 dia)
  const ultimoDia = new Date(dataCheckout);
  ultimoDia.setDate(ultimoDia.getDate() - 1);
  ultimoDia.setHours(0, 0, 0, 0);
  
  const isUltimoDia = dataAtual.getTime() === ultimoDia.getTime();
  
  if (isPrimeiroDia && isUltimoDia) {
    return 'unico'; // Reserva de 1 dia só
  } else if (isPrimeiroDia) {
    return 'inicio';
  } else if (isUltimoDia) {
    return 'fim';
  } else {
    return 'meio';
  }
}

  clicarCelula(apt: ApartamentoMapa, data: string): void {
  const chave = `${apt.id}-${data}`;
  const reserva = this.mapaReservas.get(chave);

  console.log('🖱️ Clique na célula:', {
    apartamento: apt.numeroApartamento,
    data: data,
    temReserva: !!reserva
  });

  // ✅ SE TEM RESERVA → ABRIR MODAL COM OPÇÕES
  if (reserva) {
    console.log('📋 Abrindo modal para reserva #' + reserva.id);
    
    this.reservaSelecionada = reserva;
    this.apartamentoSelecionado = apt;
    this.dataSelecionada = data;
    this.modalTitulo = `Reserva #${reserva.id}`;
    this.modalDetalhes = true;
    return;
  }

  // ✅ SE NÃO TEM RESERVA → ABRIR MODAL PARA CRIAR
  this.apartamentoSelecionado = apt;
  this.dataSelecionada = data;
  this.reservaSelecionada = null;
  this.modalTitulo = `Criar Nova Reserva`;
  this.modalDetalhes = true;
}

  formatarDataSimples(data: string): string {
    const d = new Date(data + 'T00:00:00');
    return d.toLocaleDateString('pt-BR', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  podeReservar(): boolean {
  if (!this.apartamentoSelecionado || !this.dataSelecionada) {
    console.log('❌ podeReservar: Faltam dados');
    return false;
  }

  console.log('═══════════════════════════════════════');
  console.log('🔍 VERIFICANDO SE PODE RESERVAR');
  console.log('═══════════════════════════════════════');
  console.log('🏠 Apartamento:', this.apartamentoSelecionado.numeroApartamento);
  console.log('📅 Data:', this.dataSelecionada);

  // ✅ 1. VERIFICAR SE JÁ TEM RESERVA NESTA DATA ESPECÍFICA
  const chave = `${this.apartamentoSelecionado.id}-${this.dataSelecionada}`;
  const reserva = this.mapaReservas.get(chave);

  if (reserva) {
    console.log('❌ JÁ EXISTE RESERVA NESTA DATA');
    console.log('   Reserva ID:', reserva.id);
    console.log('   Cliente:', reserva.clienteNome);
    console.log('   Status:', reserva.status);
    console.log('═══════════════════════════════════════');
    return false;
  }

  console.log('✅ Não há reserva nesta data específica');

  if (this.apartamentoSelecionado.status === 'INDISPONIVEL') {
  console.log('❌ Apartamento INDISPONÍVEL');
  console.log('═══════════════════════════════════════');
  return false;
}

  console.log('✅ Status do apartamento permite reserva');

  // ✅ 3. VERIFICAR SE A DATA É VÁLIDA (não é passado)
  const dataClicada = new Date(this.dataSelecionada + 'T00:00:00');
  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);

  if (dataClicada < hoje) {
    console.log('❌ Data é PASSADA');
    console.log('   Data clicada:', dataClicada.toISOString());
    console.log('   Hoje:', hoje.toISOString());
    console.log('═══════════════════════════════════════');
    return false;
  }

  console.log('✅ Data é válida (hoje ou futura)');

  // ✅ PODE RESERVAR!
  console.log('');
  console.log('✅✅✅ PODE CRIAR RESERVA! ✅✅✅');
  console.log('═══════════════════════════════════════');
  return true;
}

 criarNovaReserva(): void {
  console.log('═══════════════════════════════════════');
  console.log('➕ CRIAR NOVA RESERVA - INICIANDO');
  
  const apartamentoId = this.apartamentoSelecionado?.id;
  const apartamentoNumero = this.apartamentoSelecionado?.numeroApartamento;
  const dataCheckin = this.dataSelecionada;
  const statusApartamento = this.apartamentoSelecionado?.status;

  // ✅ AVISO SE APARTAMENTO EM MANUTENÇÃO — permite continuar
  if (statusApartamento === 'MANUTENCAO') {
    const continuar = confirm(
      `⚠️ ATENÇÃO!\n\n` +
      `O apartamento ${apartamentoNumero} está em MANUTENÇÃO.\n\n` +
      `Você pode criar a pré-reserva se a manutenção for concluída antes do check-in.\n\n` +
      `Deseja continuar mesmo assim?`
    );
    if (!continuar) return;
  }

  console.log('📋 Valores capturados:');
  console.log('   Apartamento ID:', apartamentoId);
  console.log('   Apartamento Nº:', apartamentoNumero);
  console.log('   Data:', dataCheckin);

  if (!apartamentoId || !dataCheckin) {
    console.error('❌ ERRO: Dados incompletos!');
    alert('❌ Erro: Dados incompletos para criar a reserva');
    return;
  }

  const queryParams = {
    apartamentoId: apartamentoId.toString(),
    dataCheckin: dataCheckin,
    bloqueado: 'true'
  };

  console.log('📤 Query Params:', queryParams);
  console.log('🔒 Fechando modal...');
  this.fecharModal();

  console.log('🚀 Navegando para /reservas/novo...');
  
  setTimeout(() => {
    this.router.navigate(['/reservas/novo'], {
      queryParams: queryParams
    }).then(
      (success) => {
        console.log('✅ Navegação concluída:', success);
      },
      (error) => {
        console.error('❌ ERRO na navegação:', error);
        alert('❌ Erro ao navegar: ' + error);
      }
    );
  }, 50);

  console.log('═══════════════════════════════════════');
}
  fecharModal(): void {
    this.modalDetalhes = false;
    
    setTimeout(() => {
      this.reservaSelecionada = null;
      this.apartamentoSelecionado = null;
      this.dataSelecionada = '';
      console.log('🧹 Variáveis do modal limpas');
    }, 200);
  }

  formatarData(data: string): string {
    const d = new Date(data);
    return d.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  excluirPreReserva(): void {
    if (!this.reservaSelecionada) {
      console.error('❌ Nenhuma reserva selecionada');
      return;
    }

    const reservaId = this.reservaSelecionada.id;
    const clienteNome = this.reservaSelecionada.clienteNome;
    const apartamentoNumero = this.reservaSelecionada.apartamentoNumero;
    const dataCheckin = this.formatarData(this.reservaSelecionada.dataCheckin);
    
    console.log('🗑️ Solicitação de exclusão da pré-reserva:', reservaId);
    
    const confirmacao = confirm(
      `⚠️ CONFIRMA A EXCLUSÃO DESTA PRÉ-RESERVA?\n\n` +
      `Reserva: #${reservaId}\n` +
      `Cliente: ${clienteNome}\n` +
      `Apartamento: ${apartamentoNumero}\n` +
      `Check-in: ${dataCheckin}\n\n` +
      `Esta ação NÃO pode ser desfeita!`
    );

    if (!confirmacao) {
      console.log('❌ Exclusão cancelada pelo usuário');
      return;
    }

    console.log('✅ Exclusão confirmada, enviando requisição...');
    this.fecharModal();

    this.http.delete(`/api/reservas/${reservaId}/pre-reserva`).subscribe({
      next: () => {
        console.log('✅ Pré-reserva excluída com sucesso');
        alert('✅ Pré-reserva excluída com sucesso!');
        this.carregarMapa();
      },
      error: (err: any) => {
        console.error('❌ Erro ao excluir:', err);
        alert('❌ Erro ao excluir pré-reserva: ' + (err.error?.message || err.message || 'Erro desconhecido'));
      }
    });
  }

  editarPreReserva(): void {
  if (!this.reservaSelecionada) return;
  this.fecharModal();
  this.router.navigate(['/reservas', this.reservaSelecionada.id]);
}

  // ============= PAGAMENTO PRÉ-RESERVA =============
  abrirModalPagamento(): void {
    if (!this.reservaSelecionada) return;
    
    console.log('💳 Abrindo modal de pagamento para reserva:', this.reservaSelecionada.id);
    
    this.http.get<any>(`/api/reservas/${this.reservaSelecionada.id}`).subscribe({
      next: (reserva) => {
        this.pagPreReservaValor = reserva.totalHospedagem;
        this.pagPreReservaFormaPagamento = '';
        this.pagPreReservaObs = '';
        
        this.modalDetalhes = false;
        this.modalPagamento = true;
      },
      error: (err: any) => {
        console.error('❌ Erro ao carregar reserva:', err);
        alert('Erro ao carregar dados da reserva');
      }
    });
  }

  fecharModalPagamento(): void {
    this.modalPagamento = false;
    this.pagPreReservaValor = 0;
    this.pagPreReservaFormaPagamento = '';
    this.pagPreReservaObs = '';
  }

  confirmarPagamentoPreReserva(): void {
  if (!this.reservaSelecionada) return;

  if (!this.pagPreReservaFormaPagamento) {
    alert('⚠️ Selecione uma forma de pagamento');
    return;
  }

  if (this.pagPreReservaValor <= 0) {
    alert('⚠️ Valor inválido');
    return;
  }

  const dto = {
    reservaId: this.reservaSelecionada.id,
    valor: this.pagPreReservaValor,
    formaPagamento: this.pagPreReservaFormaPagamento,
    observacao: this.pagPreReservaObs || undefined
  };

  this.http.post<any>('/api/pagamentos/pre-reserva', dto).subscribe({
    next: (response) => {
      if (response.ativada) {
        alert('✅ Pagamento registrado e reserva ATIVADA automaticamente!');
      } else {
        alert('✅ Pagamento registrado na pré-reserva!');
      }
      this.fecharModalPagamento();
      this.reservaSelecionada = null;
      this.apartamentoSelecionado = null;
      this.dataSelecionada = '';
      this.carregarMapa();
    },
    error: (err: any) => {
      console.error('❌ Erro:', err);
      let mensagemErro = 'Erro ao processar pagamento';
      if (err.error) {
        if (typeof err.error === 'string') mensagemErro = err.error;
        else if (err.error.erro) mensagemErro = err.error.erro;
        else if (err.error.message) mensagemErro = err.error.message;
      }
      alert('❌ Erro: ' + mensagemErro);
    }
  });
}

  // ============= CANCELAR PRÉ-RESERVA =============
abrirModalCancelar(): void {
  if (!this.reservaSelecionada) {
    console.error('❌ Nenhuma reserva selecionada');
    return;
  }

  console.log('❌ Abrindo modal de cancelamento para reserva:', this.reservaSelecionada.id);
  
  this.motivoCancelamento = '';
  this.modalDetalhes = false;
  this.modalCancelar = true;
}

fecharModalCancelar(): void {
  this.modalCancelar = false;
  this.motivoCancelamento = '';
  
  // Reabrir modal de detalhes
  setTimeout(() => {
    this.modalDetalhes = true;
  }, 100);
}

confirmarCancelamento(): void {
  if (!this.reservaSelecionada) {
    console.error('❌ Nenhuma reserva selecionada');
    return;
  }

  if (!this.motivoCancelamento || this.motivoCancelamento.trim() === '') {
    alert('⚠️ Por favor, informe o motivo do cancelamento');
    return;
  }

  const reservaId = this.reservaSelecionada.id;
  const clienteNome = this.reservaSelecionada.clienteNome;
  const apartamentoNumero = this.reservaSelecionada.apartamentoNumero;
  const dataCheckin = this.formatarData(this.reservaSelecionada.dataCheckin);
  
  console.log('═══════════════════════════════════════');
  console.log('❌ CANCELANDO PRÉ-RESERVA');
  console.log('═══════════════════════════════════════');
  console.log('   ID:', reservaId);
  console.log('   Cliente:', clienteNome);
  console.log('   Motivo:', this.motivoCancelamento);
  
  const confirmacao = confirm(
    `⚠️ CONFIRMA O CANCELAMENTO DESTA PRÉ-RESERVA?\n\n` +
    `Reserva: #${reservaId}\n` +
    `Cliente: ${clienteNome}\n` +
    `Apartamento: ${apartamentoNumero}\n` +
    `Check-in: ${dataCheckin}\n\n` +
    `Motivo: ${this.motivoCancelamento}\n\n` +
    `A reserva será CANCELADA mas permanecerá no histórico.`
  );

  if (!confirmacao) {
    console.log('❌ Cancelamento não confirmado pelo usuário');
    return;
  }

  console.log('✅ Cancelamento confirmado, enviando requisição...');
// ✅ Salvar motivo ANTES de fechar o modal (fechar limpa a variável)
const motivo = this.motivoCancelamento;

this.fecharModalCancelar();
this.fecharModal();

this.reservaService.cancelarPreReserva(reservaId, motivo).subscribe({
    next: () => {
      console.log('✅ Pré-reserva cancelada com sucesso');
      console.log('═══════════════════════════════════════');
      
      alert('✅ Pré-reserva cancelada com sucesso!');
      
      this.carregarMapa();
    },
    error: (err: any) => {
      console.error('❌ Erro ao cancelar:', err);
      console.log('═══════════════════════════════════════');
      
      let mensagemErro = 'Erro ao cancelar pré-reserva';
      
      if (err.error) {
        if (typeof err.error === 'string') {
          mensagemErro = err.error;
        } else if (err.error.message) {
          mensagemErro = err.error.message;
        }
      }
      
      alert('❌ ' + mensagemErro);
    }
  });
}

/**
 * ✅ Calcula quantos dias a reserva ocupa (colspan)
 */
/**
 * ✅ Calcula quantos dias a reserva ocupa (colspan) - CORRIGIDO
 */
getColspan(apt: ApartamentoMapa, data: string): number {
  const chave = `${apt.id}-${data}`;
  const reserva = this.mapaReservas.get(chave);
  
  if (!reserva) {
    return 1; // Célula vazia = 1 dia
  }
  
  // Verificar se é o primeiro dia VISÍVEL da reserva no mapa
  const dataAtual = new Date(data + 'T00:00:00');
  const dataCheckin = new Date(reserva.dataCheckin);
  const dataCheckout = new Date(reserva.dataCheckout);
  
  dataAtual.setHours(0, 0, 0, 0);
  dataCheckin.setHours(0, 0, 0, 0);
  dataCheckout.setHours(0, 0, 0, 0);
  
  // ✅ VERIFICAR SE É O PRIMEIRO DIA VISÍVEL
  // Pode não ser o check-in real, mas o primeiro dia que aparece no mapa
  const isPrimeiroDiaVisivel = this.datas.indexOf(data) === 
    this.datas.findIndex(d => {
      const chaveTemp = `${apt.id}-${d}`;
      return this.mapaReservas.get(chaveTemp)?.id === reserva.id;
    });
  
  if (!isPrimeiroDiaVisivel) {
    return 0; // Não é primeiro dia visível, célula será ocultada
  }
  
  // ✅ CALCULAR QUANTOS DIAS CONSECUTIVOS A RESERVA OCUPA A PARTIR DESTE DIA
  let diasConsecutivos = 0;
  const indexAtual = this.datas.indexOf(data);
  
  for (let i = indexAtual; i < this.datas.length; i++) {
    const dataVista = this.datas[i];
    const chaveTemp = `${apt.id}-${dataVista}`;
    const reservaTemp = this.mapaReservas.get(chaveTemp);
    
    // Contar apenas enquanto for a MESMA reserva
    if (reservaTemp && reservaTemp.id === reserva.id) {
      diasConsecutivos++;
    } else {
      break; // Parar quando não for mais a mesma reserva
    }
  }
  
  console.log(`Colspan para ${data}: ${diasConsecutivos} dias (Reserva #${reserva.id})`);
  
  return diasConsecutivos > 0 ? diasConsecutivos : 1;
}

/**
 * ✅ Verifica se a célula deve ser ocultada (faz parte de uma reserva mas não é o primeiro dia)
 */
/**
 * ✅ Verifica se a célula deve ser ocultada - CORRIGIDO
 */
isCelulaOculta(apt: ApartamentoMapa, data: string): boolean {
  const chave = `${apt.id}-${data}`;
  const reserva = this.mapaReservas.get(chave);
  
  if (!reserva) {
    return false; // Célula vazia, não ocultar
  }
  
  // ✅ OCULTAR SE NÃO FOR O PRIMEIRO DIA VISÍVEL DESTA RESERVA
  const isPrimeiroDiaVisivel = this.datas.indexOf(data) === 
    this.datas.findIndex(d => {
      const chaveTemp = `${apt.id}-${d}`;
      return this.mapaReservas.get(chaveTemp)?.id === reserva.id;
    });
  
  return !isPrimeiroDiaVisivel;
}

  imprimir(): void {
  window.print();
}
  
  voltar(): void {    
    this.router.navigate(['/reservas']);
  }
}
