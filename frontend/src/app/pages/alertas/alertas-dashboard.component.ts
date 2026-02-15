import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AlertasService, AlertaDTO, TodosAlertasResponse } from '../../services/alertas.service';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-alertas-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="alertas-container">
      
      <!-- ===== HEADER ===== -->
      <div class="header">
        <div class="header-left">
          <h1>🚨 Central de Alertas</h1>
          <p class="subtitulo">Monitoramento em tempo real do hotel</p>
        </div>
        <div class="header-right">
          <div class="ultima-atualizacao">
            🕐 Última atualização: {{ ultimaAtualizacao | date:'HH:mm:ss' }}
          </div>
          <button class="btn-atualizar" (click)="buscarAlertas()" [disabled]="carregando">
            {{ carregando ? '⏳ Atualizando...' : '🔄 Atualizar' }}
          </button>
        </div>
      </div>

      <!-- ===== RESUMO ===== -->
      <div class="cards-resumo">
        <div class="card-resumo critico">
          <div class="card-icon">🚨</div>
          <div class="card-info">
            <div class="card-numero">{{ totalConflitos }}</div>
            <div class="card-label">Conflitos de Reserva</div>
          </div>
        </div>

        <div class="card-resumo alto">
          <div class="card-icon">⏰</div>
          <div class="card-info">
            <div class="card-numero">{{ totalCheckoutsVencidos }}</div>
            <div class="card-label">Checkouts Vencidos</div>
          </div>
        </div>

        <div class="card-resumo medio">
          <div class="card-icon">🔴</div>
          <div class="card-info">
            <div class="card-numero">{{ totalNoShows }}</div>
            <div class="card-label">No-Show (Não Compareceu)</div>
          </div>
        </div>

        <div class="card-resumo total">
          <div class="card-icon">📊</div>
          <div class="card-info">
            <div class="card-numero">{{ totalGeral }}</div>
            <div class="card-label">Total de Alertas</div>
          </div>
        </div>
      </div>

      <!-- ===== ABAS ===== -->
      <div class="abas">
        <button 
          class="aba" 
          [class.ativa]="abaAtiva === 'conflitos'"
          (click)="abaAtiva = 'conflitos'">
          🚨 Conflitos ({{ totalConflitos }})
        </button>
        <button 
          class="aba" 
          [class.ativa]="abaAtiva === 'checkouts'"
          (click)="abaAtiva = 'checkouts'">
          ⏰ Checkouts Vencidos ({{ totalCheckoutsVencidos }})
        </button>
        <button 
          class="aba" 
          [class.ativa]="abaAtiva === 'noshow'"
          (click)="abaAtiva = 'noshow'">
          🔴 No-Show ({{ totalNoShows }})
        </button>
      </div>

      <!-- ===== LOADING ===== -->
      <div *ngIf="carregando" class="loading">
        <div class="spinner"></div>
        <p>Carregando alertas...</p>
      </div>

      <!-- ===== SEM ALERTAS ===== -->
      <div *ngIf="!carregando && totalGeral === 0" class="sem-alertas">
        <div class="sem-alertas-icon">✅</div>
        <h2>Tudo em Ordem!</h2>
        <p>Não há alertas no momento.</p>
      </div>

      <!-- ===== LISTA DE ALERTAS ===== -->
      <div *ngIf="!carregando && totalGeral > 0" class="lista-alertas">

        <!-- ABA: CONFLITOS -->
        <div *ngIf="abaAtiva === 'conflitos'">
          <div *ngIf="conflitos.length === 0" class="sem-alertas-aba">
            ✅ Nenhum conflito de reserva
          </div>
          <div *ngFor="let alerta of conflitos" class="card-alerta" [style.border-left-color]="obterCor(alerta.nivelGravidade)">
            <div class="alerta-header">
              <div class="alerta-tipo">
                <span class="alerta-icone">{{ obterIcone(alerta.tipoAlerta) }}</span>
                <span class="alerta-titulo">{{ alerta.titulo }}</span>
              </div>
              <div class="alerta-gravidade" [style.background]="obterCor(alerta.nivelGravidade)">
                {{ alerta.nivelGravidade }}
              </div>
            </div>

            <div class="alerta-info">
              <div class="info-linha">
                <strong>Apartamento:</strong> {{ alerta.numeroApartamento }} ({{ alerta.tipoApartamento }})
              </div>
              <div class="info-linha">
                <strong>Cliente (Pré-reserva):</strong> {{ alerta.clienteNome }}
              </div>
              <div class="info-linha">
                <strong>Check-in previsto:</strong> {{ alerta.dataCheckin | date:'dd/MM/yyyy HH:mm' }}
              </div>
              <div class="info-linha">
                <strong>Atraso do checkout atual:</strong> {{ alerta.horasAtraso }} horas
              </div>
            </div>

            <div class="alerta-descricao">
              {{ alerta.descricao }}
            </div>

            <div class="alerta-recomendacao">
              💡 <strong>Recomendação:</strong> {{ alerta.recomendacao }}
            </div>

            <!-- Apartamentos Disponíveis -->
            <div *ngIf="alerta.apartamentosDisponiveis && alerta.apartamentosDisponiveis.length > 0" class="apartamentos-disponiveis">
              <strong>🏨 Apartamentos disponíveis para transferência:</strong>
              <div class="lista-aptos">
                <div 
                  *ngFor="let apto of alerta.apartamentosDisponiveis" 
                  class="apto-disponivel"
                  [class.recomendado]="apto.recomendado">
                  <span class="apto-numero">{{ apto.numeroApartamento }}</span>
                  <span class="apto-tipo">{{ apto.tipoApartamento }}</span>
                  <span *ngIf="apto.recomendado" class="badge-recomendado">⭐ Recomendado</span>
                  <button 
                    class="btn-transferir-mini" 
                    (click)="transferirPreReserva(alerta, apto)">
                    Transferir
                  </button>
                </div>
              </div>
            </div>

            <!-- ✅ AÇÕES COM BOTÃO VER DETALHES -->
            <div class="alerta-acoes">
  <button 
    class="btn-detalhes"
    (click)="verDetalhesReserva(alerta.reservaId!)"
    title="Ver todos os detalhes e fazer alterações">
    👁️ Detalhes
  </button>
  <button 
    class="btn-acao-comunicacao telefone"
    (click)="ligarCliente(alerta)"
    title="Ligar para o cliente">
    📞 Ligar
  </button>
  <button 
    class="btn-acao-comunicacao sms"
    (click)="enviarSMS(alerta)"
    title="Enviar SMS (em breve)">
    📱 SMS
  </button>
</div>
          </div>
        </div>

        <!-- ABA: CHECKOUTS VENCIDOS -->
        <div *ngIf="abaAtiva === 'checkouts'">
          <div *ngIf="checkoutsVencidos.length === 0" class="sem-alertas-aba">
            ✅ Nenhum checkout vencido
          </div>
          <div *ngFor="let alerta of checkoutsVencidos" class="card-alerta" [style.border-left-color]="obterCor(alerta.nivelGravidade)">
            <div class="alerta-header">
              <div class="alerta-tipo">
                <span class="alerta-icone">{{ obterIcone(alerta.tipoAlerta) }}</span>
                <span class="alerta-titulo">{{ alerta.titulo }}</span>
              </div>
              <div class="alerta-gravidade" [style.background]="obterCor(alerta.nivelGravidade)">
                {{ alerta.nivelGravidade }}
              </div>
            </div>

            <div class="alerta-info">
              <div class="info-linha">
                <strong>Apartamento:</strong> {{ alerta.numeroApartamento }} ({{ alerta.tipoApartamento }})
              </div>
              <div class="info-linha">
                <strong>Hóspede:</strong> {{ alerta.clienteNome }}
              </div>
              <div class="info-linha">
                <strong>Checkout previsto:</strong> {{ alerta.dataCheckout | date:'dd/MM/yyyy HH:mm' }}
              </div>
              <div class="info-linha alerta-destaque">
                <strong>⏰ Atraso:</strong> {{ alerta.horasAtraso }} hora(s) {{ alerta.minutosAtraso ? 'e ' + (alerta.minutosAtraso % 60) + ' minuto(s)' : '' }}
              </div>
            </div>

            <div class="alerta-descricao">
              {{ alerta.descricao }}
            </div>

            <div class="alerta-recomendacao">
              💡 <strong>Recomendação:</strong> {{ alerta.recomendacao }}
            </div>

            <!-- ✅ AÇÕES COM BOTÃO VER DETALHES -->
            <div class="alerta-acoes">
  <button 
    class="btn-detalhes"
    (click)="verDetalhesReserva(alerta.reservaId!)"
    title="Ver todos os detalhes e fazer alterações">
    👁️ Detalhes
  </button>
  <button 
    class="btn-acao-comunicacao telefone"
    (click)="ligarCliente(alerta)"
    title="Ligar para o hóspede">
    📞 Ligar
  </button>
  <button 
    class="btn-acao-comunicacao sms"
    (click)="enviarSMS(alerta)"
    title="Enviar SMS (em breve)">
    📱 SMS
  </button>
</div>
          </div>
        </div>

        <!-- ABA: NO-SHOW -->
        <div *ngIf="abaAtiva === 'noshow'">
          <div *ngIf="noShows.length === 0" class="sem-alertas-aba">
            ✅ Nenhum no-show detectado
          </div>
          <div *ngFor="let alerta of noShows" class="card-alerta" [style.border-left-color]="obterCor(alerta.nivelGravidade)">
            <div class="alerta-header">
              <div class="alerta-tipo">
                <span class="alerta-icone">{{ obterIcone(alerta.tipoAlerta) }}</span>
                <span class="alerta-titulo">{{ alerta.titulo }}</span>
              </div>
              <div class="alerta-gravidade" [style.background]="obterCor(alerta.nivelGravidade)">
                {{ alerta.nivelGravidade }}
              </div>
            </div>

            <div class="alerta-info">
              <div class="info-linha">
                <strong>Apartamento:</strong> {{ alerta.numeroApartamento }} ({{ alerta.tipoApartamento }})
              </div>
              <div class="info-linha">
                <strong>Cliente:</strong> {{ alerta.clienteNome }}
              </div>
              <div class="info-linha">
                <strong>Check-in previsto:</strong> {{ alerta.dataCheckin | date:'dd/MM/yyyy HH:mm' }}
              </div>
              <div class="info-linha">
                <strong>Valor total:</strong> R$ {{ alerta.totalReserva?.toFixed(2) }}
              </div>
              <div class="info-linha" [class.alerta-destaque]="(alerta.percentualPago || 0) === 0">
                <strong>💰 Pago:</strong> R$ {{ alerta.totalPago?.toFixed(2) }} 
                ({{ alerta.percentualPago?.toFixed(0) }}%)
                <span *ngIf="(alerta.percentualPago || 0) === 0" class="badge-perigo">SEM PAGAMENTO</span>
              </div>
            </div>

            <div class="alerta-descricao">
              {{ alerta.descricao }}
            </div>

            <div class="alerta-recomendacao">
              💡 <strong>Recomendação:</strong> {{ alerta.recomendacao }}
            </div>

            <!-- ✅ AÇÕES COM BOTÃO VER DETALHES -->
            <div class="alerta-acoes">
  <button 
    class="btn-detalhes"
    (click)="verDetalhesReserva(alerta.reservaId!)"
    title="Ver todos os detalhes e fazer alterações">
    👁️ Detalhes
  </button>
  <button 
    class="btn-acao-comunicacao telefone"
    (click)="ligarCliente(alerta)"
    title="Ligar para o cliente">
    📞 Ligar
  </button>
  <button 
    class="btn-acao-comunicacao sms"
    (click)="enviarSMS(alerta)"
    title="Enviar SMS (em breve)">
    📱 SMS
  </button>
</div>
          </div>
        </div>
      </div> 

      <!-- ===== MODAL DE PRORROGAÇÃO ===== -->
      <div *ngIf="modalProrrogacao" class="modal-overlay" (click)="fecharModalProrrogacao()">
        <div class="modal-box" (click)="$event.stopPropagation()">
          
          <div class="modal-header">
            <h2>🔄 Prorrogar Checkout</h2>
            <button class="btn-fechar" (click)="fecharModalProrrogacao()">✖</button>
          </div>

          <div class="modal-body">
            
            <!-- INFO CLIENTE -->
            <div class="info-cliente">
              <p><strong>Cliente:</strong> {{ alertaSelecionado?.clienteNome }}</p>
              <p><strong>Apartamento:</strong> {{ alertaSelecionado?.numeroApartamento }}</p>
              <p><strong>Checkout atual:</strong> {{ alertaSelecionado?.dataCheckout | date:'dd/MM/yyyy HH:mm' }}</p>
              <p class="destaque-atraso"><strong>⏰ Atraso:</strong> {{ alertaSelecionado?.horasAtraso }} hora(s)</p>
            </div>

            <!-- DATA -->
            <div class="form-group">
              <label><strong>📅 Nova data de checkout:</strong></label>
              <div class="data-manual">
                <select [(ngModel)]="dia" class="select-data">
                  <option value="">Dia</option>
                  <option *ngFor="let d of dias" [value]="d">{{ d }}</option>
                </select>
                <span class="separador">/</span>
                <select [(ngModel)]="mes" class="select-data">
                  <option value="">Mês</option>
                  <option value="01">Janeiro</option>
                  <option value="02">Fevereiro</option>
                  <option value="03">Março</option>
                  <option value="04">Abril</option>
                  <option value="05">Maio</option>
                  <option value="06">Junho</option>
                  <option value="07">Julho</option>
                  <option value="08">Agosto</option>
                  <option value="09">Setembro</option>
                  <option value="10">Outubro</option>
                  <option value="11">Novembro</option>
                  <option value="12">Dezembro</option>
                </select>
                <span class="separador">/</span>
                <select [(ngModel)]="ano" class="select-data">
                  <option value="">Ano</option>
                  <option *ngFor="let a of anos" [value]="a">{{ a }}</option>
                </select>
              </div>
            </div>

            <!-- HORA -->
            <div class="form-group">
              <label><strong>🕐 Nova hora de checkout:</strong></label>
              <div class="hora-manual">
                <select [(ngModel)]="hora" class="select-hora">
                  <option value="">Hora</option>
                  <option *ngFor="let h of horas" [value]="h">{{ h }}</option>
                </select>
                <span class="separador">:</span>
                <select [(ngModel)]="minuto" class="select-hora">
                  <option value="">Min</option>
                  <option value="00">00</option>
                  <option value="15">15</option>
                  <option value="30">30</option>
                  <option value="45">45</option>
                </select>
              </div>
            </div>

            <!-- PREVIEW -->
            <div class="preview-data">
              <strong>📌 Novo checkout:</strong> 
              <span class="preview-destaque">{{ formatarDataPreview() }}</span>
            </div>

            <!-- MOTIVO -->
            <div class="form-group">
              <label for="motivo"><strong>Motivo:</strong></label>
              <textarea 
                id="motivo"
                [(ngModel)]="motivoProrrogacao" 
                rows="3"
                class="input-texto"
                placeholder="Ex: Solicitação do hóspede"></textarea>
            </div>
            
          </div>

          <!-- FOOTER -->
          <div class="modal-footer">
            <button class="btn-modal-cancelar" (click)="fecharModalProrrogacao()">
              Cancelar
            </button>
            <button class="btn-modal-confirmar" (click)="confirmarProrrogacao()">
              ✅ Confirmar
            </button>
          </div>

        </div>
      </div>

    </div>
    
  `,
  styles: [`
    /* ===== CONTAINER ===== */
    .alertas-container {
      max-width: 1400px;
      margin: 0 auto;
      padding: 30px;
      background: #f5f7fa;
      min-height: 100vh;
    }

    /* ===== HEADER ===== */
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
      background: white;
      padding: 25px 30px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .header-left h1 {
      margin: 0 0 8px 0;
      font-size: 2em;
      color: #2c3e50;
    }

    .subtitulo {
      margin: 0;
      color: #7f8c8d;
      font-size: 1em;
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 15px;
    }

    .ultima-atualizacao {
      color: #7f8c8d;
      font-size: 0.9em;
    }

    .btn-atualizar {
      padding: 10px 20px;
      background: #3498db;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.3s;
    }

    .btn-atualizar:hover:not(:disabled) {
      background: #2980b9;
      transform: translateY(-2px);
    }

    .btn-atualizar:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    /* ===== CARDS RESUMO ===== */
    .cards-resumo {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 20px;
      margin-bottom: 30px;
    }

    .card-resumo {
      background: white;
      padding: 25px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      gap: 20px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      border-left: 5px solid;
    }

    .card-resumo.critico {
      border-left-color: #dc3545;
    }

    .card-resumo.alto {
      border-left-color: #fd7e14;
    }

    .card-resumo.medio {
      border-left-color: #ffc107;
    }

    .card-resumo.total {
      border-left-color: #3498db;
    }

    .card-icon {
      font-size: 3em;
    }

    .card-numero {
      font-size: 2.5em;
      font-weight: 700;
      color: #2c3e50;
      line-height: 1;
    }

    .card-label {
      color: #7f8c8d;
      font-size: 0.9em;
      margin-top: 5px;
    }

    /* ===== ABAS ===== */
    .abas {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;
      background: white;
      padding: 15px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .aba {
      flex: 1;
      padding: 15px 20px;
      background: #ecf0f1;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 600;
      font-size: 1em;
      transition: all 0.3s;
    }

    .aba:hover {
      background: #bdc3c7;
    }

    .aba.ativa {
      background: #3498db;
      color: white;
    }

    /* ===== LOADING ===== */
    .loading {
      text-align: center;
      padding: 60px 20px;
      background: white;
      border-radius: 12px;
    }

    .spinner {
      width: 50px;
      height: 50px;
      border: 5px solid #ecf0f1;
      border-top-color: #3498db;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin: 0 auto 20px;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    /* ===== SEM ALERTAS ===== */
    .sem-alertas {
      text-align: center;
      padding: 80px 20px;
      background: white;
      border-radius: 12px;
    }

    .sem-alertas-icon {
      font-size: 5em;
      margin-bottom: 20px;
    }

    .sem-alertas h2 {
      color: #27ae60;
      margin: 0 0 10px 0;
    }

    .sem-alertas p {
      color: #7f8c8d;
      margin: 0;
    }

    .sem-alertas-aba {
      text-align: center;
      padding: 40px;
      background: #ecf0f1;
      border-radius: 8px;
      color: #7f8c8d;
      font-size: 1.1em;
    }

    /* ===== CARDS DE ALERTA ===== */
    .lista-alertas {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .card-alerta {
      background: white;
      padding: 25px;
      border-radius: 12px;
      border-left: 6px solid;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .alerta-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 15px;
      border-bottom: 2px solid #ecf0f1;
    }

    .alerta-tipo {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .alerta-icone {
      font-size: 1.8em;
    }

    .alerta-titulo {
      font-size: 1.3em;
      font-weight: 700;
      color: #2c3e50;
    }

    .alerta-gravidade {
      padding: 8px 16px;
      border-radius: 6px;
      color: white;
      font-weight: 700;
      font-size: 0.85em;
    }

    .alerta-info {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 12px;
      margin-bottom: 20px;
    }

    .info-linha {
      color: #555;
      font-size: 0.95em;
    }

    .info-linha.alerta-destaque {
      background: #fff3cd;
      padding: 10px;
      border-radius: 6px;
      border-left: 4px solid #ffc107;
    }

    .alerta-descricao {
      background: #f8f9fa;
      padding: 15px;
      border-radius: 8px;
      margin-bottom: 15px;
      color: #555;
      line-height: 1.6;
    }

    .alerta-recomendacao {
      background: #e7f3ff;
      padding: 15px;
      border-radius: 8px;
      border-left: 4px solid #3498db;
      margin-bottom: 20px;
      color: #2c3e50;
    }

    /* ===== APARTAMENTOS DISPONÍVEIS ===== */
    .apartamentos-disponiveis {
      background: #f0fff4;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 20px;
      border-left: 4px solid #27ae60;
    }

    .lista-aptos {
      display: grid;
      gap: 10px;
      margin-top: 15px;
    }

    .apto-disponivel {
      display: flex;
      align-items: center;
      gap: 15px;
      background: white;
      padding: 15px;
      border-radius: 6px;
      border: 2px solid #ecf0f1;
    }

    .apto-disponivel.recomendado {
      border-color: #27ae60;
      background: #f0fff4;
    }

    .apto-numero {
      font-weight: 700;
      font-size: 1.2em;
      color: #2c3e50;
      min-width: 60px;
    }

    .apto-tipo {
      color: #7f8c8d;
    }

    .badge-recomendado {
      background: #27ae60;
      color: white;
      padding: 5px 12px;
      border-radius: 20px;
      font-size: 0.85em;
      font-weight: 600;
    }

    .badge-perigo {
      background: #dc3545;
      color: white;
      padding: 5px 12px;
      border-radius: 20px;
      font-size: 0.85em;
      font-weight: 600;
      margin-left: 10px;
    }

    .btn-transferir-mini {
      margin-left: auto;
      padding: 8px 16px;
      background: #27ae60;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.3s;
    }

    .btn-transferir-mini:hover {
      background: #229954;
      transform: translateY(-2px);
    }

    /* ✅ BOTÃO VER DETALHES */
    .btn-detalhes {
      padding: 12px 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      font-size: 0.95em;
      transition: all 0.3s ease;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
    }

    .btn-detalhes:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
      background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
    }

    .btn-detalhes:active {
      transform: translateY(0);
    }

    /* ===== AÇÕES ===== */
    .alerta-acoes {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      align-items: center;
    }

    .btn-acao {
      padding: 12px 20px;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.3s;
      font-size: 0.95em;
    }

    .btn-acao.primario {
      background: #3498db;
      color: white;
    }

    .btn-acao.primario:hover {
      background: #2980b9;
      transform: translateY(-2px);
    }

    .btn-acao.secundario {
      background: #95a5a6;
      color: white;
    }

    .btn-acao.secundario:hover {
      background: #7f8c8d;
      transform: translateY(-2px);
    }

    .btn-acao.atencao {
      background: #ffc107;
      color: #2c3e50;
    }

    .btn-acao.atencao:hover {
      background: #e0a800;
      transform: translateY(-2px);
    }

    .btn-acao.perigo {
      background: #dc3545;
      color: white;
    }

    .btn-acao.perigo:hover {
      background: #c82333;
      transform: translateY(-2px);
    }

    /* ===== MODAL ===== */
    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.7);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 9999;
      animation: fadeIn 0.3s;
    }

    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }

    .modal-box {
      background: white;
      border-radius: 12px;
      box-shadow: 0 10px 40px rgba(0,0,0,0.3);
      width: 90%;
      max-width: 600px;
      max-height: 90vh;
      overflow-y: auto;
      animation: slideUp 0.3s;
    }

    @keyframes slideUp {
      from {
        transform: translateY(50px);
        opacity: 0;
      }
      to {
        transform: translateY(0);
        opacity: 1;
      }
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 25px 30px;
      border-bottom: 2px solid #ecf0f1;
    }

    .modal-header h2 {
      margin: 0;
      color: #2c3e50;
      font-size: 1.5em;
    }

    .btn-fechar {
      background: none;
      border: none;
      font-size: 1.5em;
      color: #95a5a6;
      cursor: pointer;
      padding: 5px 10px;
      transition: all 0.3s;
    }

    .btn-fechar:hover {
      color: #e74c3c;
      transform: rotate(90deg);
    }

    .modal-body {
      padding: 30px;
    }

    .info-cliente {
      background: #f8f9fa;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 25px;
      border-left: 4px solid #3498db;
    }

    .info-cliente p {
      margin: 8px 0;
      color: #2c3e50;
    }

    .destaque-atraso {
      background: #fff3cd;
      padding: 10px;
      border-radius: 6px;
      border-left: 4px solid #ffc107;
      margin-top: 10px;
    }

    .form-group {
      margin-bottom: 20px;
    }

    .form-group label {
      display: block;
      margin-bottom: 8px;
      color: #2c3e50;
    }

    .input-texto {
      width: 100%;
      padding: 12px;
      border: 2px solid #ecf0f1;
      border-radius: 6px;
      font-size: 1em;
      transition: all 0.3s;
      font-family: inherit;
      resize: vertical;
      min-height: 80px;
    }

    .input-texto:focus {
      outline: none;
      border-color: #3498db;
      box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
    }

    .data-manual,
    .hora-manual {
      display: flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
    }

    .select-data,
    .select-hora {
      padding: 12px;
      border: 2px solid #ecf0f1;
      border-radius: 6px;
      font-size: 1em;
      background: white;
      cursor: pointer;
      transition: all 0.3s;
      font-weight: 600;
    }

    .select-data {
      flex: 1;
      min-width: 100px;
    }

    .select-hora {
      width: 80px;
    }

    .select-data:focus,
    .select-hora:focus {
      outline: none;
      border-color: #3498db;
      box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
    }

    .separador {
      font-size: 1.5em;
      font-weight: 700;
      color: #7f8c8d;
    }

    .preview-data {
      background: #e7f3ff;
      padding: 15px;
      border-radius: 8px;
      border-left: 4px solid #3498db;
      margin-bottom: 20px;
      font-size: 1.1em;
      color: #2c3e50;
    }

    .preview-destaque {
      color: #2980b9;
      font-size: 1.2em;
      font-weight: 700;
    }

    .modal-footer {
      display: flex;
      gap: 15px;
      padding: 20px 30px;
      border-top: 2px solid #ecf0f1;
      background: #f8f9fa;
      border-radius: 0 0 12px 12px;
    }

    .btn-modal-cancelar,
    .btn-modal-confirmar {
      flex: 1;
      padding: 14px 20px;
      border: none;
      border-radius: 8px;
      font-weight: 700;
      font-size: 1em;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn-modal-cancelar {
      background: #95a5a6;
      color: white;
    }

    .btn-modal-cancelar:hover {
      background: #7f8c8d;
      transform: translateY(-2px);
    }

    .btn-modal-confirmar {
      background: #27ae60;
      color: white;
    }

    .btn-modal-confirmar:hover {
      background: #229954;
      transform: translateY(-2px);
      box-shadow: 0 5px 15px rgba(39, 174, 96, 0.3);
    }

    /* ✅ Botões de Comunicação */
.btn-acao-comunicacao {
  padding: 12px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
  font-size: 0.95em;
  color: white;
}

.btn-acao-comunicacao.telefone {
  background: #27ae60;
}

.btn-acao-comunicacao.telefone:hover {
  background: #229954;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(39, 174, 96, 0.3);
}

.btn-acao-comunicacao.sms {
  background: #3498db;
}

.btn-acao-comunicacao.sms:hover {
  background: #2980b9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.btn-acao-comunicacao:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

    /* ===== RESPONSIVO ===== */
    @media (max-width: 768px) {
      .alertas-container {
        padding: 15px;
      }

      .header {
        flex-direction: column;
        gap: 20px;
      }

      .cards-resumo {
        grid-template-columns: 1fr;
      }

      .abas {
        flex-direction: column;
      }

      .alerta-info {
        grid-template-columns: 1fr;
      }

      .alerta-acoes {
        flex-direction: column;
      }

      .btn-acao,
      .btn-detalhes {
        width: 100%;
      }

      .modal-box {
        width: 95%;
        max-height: 95vh;
      }

      .modal-header,
      .modal-body,
      .modal-footer {
        padding: 20px;
      }

      .modal-footer {
        flex-direction: column;
      }

      .data-manual {
        flex-direction: column;
        align-items: stretch;
      }

      .select-data {
        width: 100%;
      }
    }
  `]
})
export class AlertasDashboardComponent implements OnInit, OnDestroy {

  private router = inject(Router);
  private alertasService = inject(AlertasService);

  // Dados
  conflitos: AlertaDTO[] = [];
  checkoutsVencidos: AlertaDTO[] = [];
  noShows: AlertaDTO[] = [];

  // Totais
  totalConflitos = 0;
  totalCheckoutsVencidos = 0;
  totalNoShows = 0;
  totalGeral = 0;  

  modalProrrogacao = false;
  alertaSelecionado: AlertaDTO | null = null;
  motivoProrrogacao = 'Solicitação do hóspede';

  // Campos separados para data/hora
  dia = '';
  mes = '';
  ano = '';
  hora = '';
  minuto = '';

  // Opções para os selects
  dias: string[] = [];
  anos: number[] = [];
  horas: string[] = [];

  // Controles
  abaAtiva = 'conflitos';
  carregando = false;
  ultimaAtualizacao = new Date();

  // Subscriptions
  private refreshSubscription?: Subscription;

  ngOnInit(): void {
    this.buscarAlertas();
    this.iniciarAutoRefresh();
    this.popularOpcoesData();
  }

  ngOnDestroy(): void {
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
  }

  popularOpcoesData(): void {
    // Dias (01 a 31)
    for (let i = 1; i <= 31; i++) {
      this.dias.push(i.toString().padStart(2, '0'));
    }
    
    // Anos (ano atual até +2)
    const anoAtual = new Date().getFullYear();
    for (let i = 0; i < 3; i++) {
      this.anos.push(anoAtual + i);
    }
    
    // Horas (00 a 23)
    for (let i = 0; i < 24; i++) {
      this.horas.push(i.toString().padStart(2, '0'));
    }
  }

  buscarAlertas(): void {
    this.carregando = true;

    this.alertasService.buscarTodosAlertas().subscribe({
      next: (response) => {
        this.conflitos = response.conflitos || [];
        this.checkoutsVencidos = response.checkoutsVencidos || [];
        this.noShows = response.noShows || [];

        this.totalConflitos = this.conflitos.length;
        this.totalCheckoutsVencidos = this.checkoutsVencidos.length;
        this.totalNoShows = this.noShows.length;
        this.totalGeral = this.totalConflitos + this.totalCheckoutsVencidos + this.totalNoShows;

        this.ultimaAtualizacao = new Date();
        this.carregando = false;

        console.log('✅ Alertas carregados:', {
          conflitos: this.totalConflitos,
          checkouts: this.totalCheckoutsVencidos,
          noShows: this.totalNoShows,
          total: this.totalGeral
        });
      },
      error: (error) => {
        console.error('❌ Erro ao buscar alertas:', error);
        this.carregando = false;
        alert('Erro ao carregar alertas. Verifique o console.');
      }
    });
  }

  iniciarAutoRefresh(): void {
    // Atualiza automaticamente a cada 5 minutos
    this.refreshSubscription = interval(5 * 60 * 1000).subscribe(() => {
      console.log('🔄 Auto-refresh: Buscando alertas...');
      this.buscarAlertas();
    });
  }

  obterCor(gravidade: string): string {
    return this.alertasService.obterCorGravidade(gravidade);
  }

  obterIcone(tipo: string): string {
    return this.alertasService.obterIconeTipoAlerta(tipo);
  }

  /**
   * ✅ NAVEGAR PARA DETALHES DA RESERVA
   */
  verDetalhesReserva(reservaId: number): void {
    console.log('🔍 Navegando para reserva:', reservaId);
    this.router.navigate(['/reservas', reservaId]);
  }

  // ===== AÇÕES - CONFLITOS =====

  transferirPreReserva(alerta: AlertaDTO, apto: any): void {
    if (confirm(`Transferir pré-reserva de ${alerta.clienteNome} para o apartamento ${apto.numeroApartamento}?`)) {
      this.alertasService.transferirPreReserva(
        alerta.reservaId!,
        apto.apartamentoId,
        'Conflito de reserva - checkout atrasado'
      ).subscribe({
        next: (response) => {
          alert('✅ Pré-reserva transferida com sucesso!');
          this.buscarAlertas();
        },
        error: (error) => {
          console.error('❌ Erro ao transferir:', error);
          alert('Erro ao transferir pré-reserva: ' + (error.error?.erro || error.message));
        }
      });
    }
  }

  abrirModalTransferencia(alerta: AlertaDTO): void {
    if (alerta.apartamentosDisponiveis && alerta.apartamentosDisponiveis.length > 0) {
      const recomendados = alerta.apartamentosDisponiveis
        .filter(a => a.recomendado)
        .map(a => `${a.numeroApartamento} (${a.tipoApartamento})`)
        .join(', ');
      
      alert(`Apartamentos recomendados:\n${recomendados}\n\nClique em "Transferir" ao lado do apartamento desejado.`);
    } else {
      alert('Não há apartamentos disponíveis para transferência.');
    }
  }

  ligarCliente(alerta: AlertaDTO): void {
    const telefone = alerta.clienteNome;
    navigator.clipboard.writeText(telefone || '');
    alert(`📞 Ligar para: ${alerta.clienteNome}\n\n(Telefone copiado para área de transferência)`);
  }

  cancelarPreReserva(alerta: AlertaDTO): void {
    const motivo = prompt(`Motivo do cancelamento da pré-reserva de ${alerta.clienteNome}:`);
    
    if (motivo !== null) {
      this.alertasService.cancelarReserva(alerta.reservaId!, motivo).subscribe({
        next: (response) => {
          alert('✅ Pré-reserva cancelada com sucesso!');
          this.buscarAlertas();
        },
        error: (error) => {
          console.error('❌ Erro ao cancelar:', error);
          alert('Erro ao cancelar: ' + (error.error?.erro || error.message));
        }
      });
    }
  }

  // ===== AÇÕES - CHECKOUTS VENCIDOS =====

  cobrarDiaria(alerta: AlertaDTO, tipo: string): void {
    const tipoTexto = tipo === 'COMPLETA' ? 'diária completa' : 'meia diária';
    
    if (confirm(`Confirma cobrança de ${tipoTexto} para ${alerta.clienteNome}?`)) {
      this.alertasService.cobrarDiaria(
        alerta.reservaId!,
        tipo,
        `Checkout atrasado - ${alerta.horasAtraso} hora(s)`
      ).subscribe({
        next: (response) => {
          alert(`✅ ${tipoTexto} cobrada com sucesso!`);
          this.buscarAlertas();
        },
        error: (error) => {
          console.error('❌ Erro ao cobrar:', error);
          alert('Erro ao cobrar: ' + (error.error?.erro || error.message));
        }
      });
    }
  }

  fazerCheckout(alerta: AlertaDTO): void {
    const observacao = prompt(`Observação para checkout de ${alerta.clienteNome}:`, '');
    
    if (observacao !== null) {
      this.alertasService.fazerCheckout(alerta.reservaId!, observacao).subscribe({
        next: (response) => {
          console.log('📋 Resposta do checkout:', response);
          
          if (response.permitido === false) {
            alert(
              `❌ ${response.mensagem}\n\n` +
              `💰 Total: R$ ${response.totalHospedagem?.toFixed(2)}\n` +
              `💵 Pago: R$ ${response.totalRecebido?.toFixed(2)}\n` +
              `📊 Falta: R$ ${Math.abs(response.saldo).toFixed(2)}\n\n` +
              `O cliente precisa pagar antes do checkout!`
            );
          } else {
            let mensagem = '✅ Checkout realizado com sucesso!';
            
            if (response.motivo === 'SALDO_CREDOR') {
              mensagem += `\n\n💵 ATENÇÃO: Devolver R$ ${response.saldo.toFixed(2)} ao cliente!`;
            } else if (response.saldo === 0) {
              mensagem += '\n\n✅ Conta quitada!';
            }
            
            alert(mensagem);
            this.buscarAlertas();
          }
        },
        error: (error) => {
          console.error('❌ Erro ao fazer checkout:', error);
          alert('Erro ao fazer checkout: ' + (error.error?.erro || error.message));
        }
      });
    }
  }

  prorrogarCheckout(alerta: AlertaDTO): void {
    this.abrirModalProrrogacao(alerta);
  }

  // ===== AÇÕES - NO-SHOW =====

  confirmarChegada(alerta: AlertaDTO): void {
    if (confirm(`Confirmar que ${alerta.clienteNome} chegou ao hotel?\n\nIsso fará o check-in da reserva.`)) {
      this.alertasService.confirmarChegada(alerta.reservaId!).subscribe({
        next: (response) => {
          alert('✅ Check-in realizado com sucesso!');
          this.buscarAlertas();
        },
        error: (error) => {
          console.error('❌ Erro ao confirmar chegada:', error);
          alert('Erro ao confirmar chegada: ' + (error.error?.erro || error.message));
        }
      });
    }
  }

  marcarNoShow(alerta: AlertaDTO): void {
    const observacao = prompt(
      `Confirmar NO-SHOW de ${alerta.clienteNome}?\n\n` +
      `Valor pago: R$ ${alerta.totalPago?.toFixed(2)} (${alerta.percentualPago?.toFixed(0)}%)\n\n` +
      `Observação:`,
      'Cliente não compareceu'
    );
    
    if (observacao !== null) {
      this.alertasService.marcarNoShow(alerta.reservaId!, observacao).subscribe({
        next: (response) => {
          alert('✅ No-show registrado com sucesso!');
          this.buscarAlertas();
        },
        error: (error) => {
          console.error('❌ Erro ao marcar no-show:', error);
          alert('Erro ao marcar no-show: ' + (error.error?.erro || error.message));
        }
      });
    }
  }

  cancelarReserva(alerta: AlertaDTO): void {
    const motivo = prompt(
      `Cancelar reserva de ${alerta.clienteNome}?\n\n` +
      `Motivo:`,
      'No-show - Cliente não compareceu'
    );
    
    if (motivo !== null) {
      this.alertasService.cancelarReserva(alerta.reservaId!, motivo).subscribe({
        next: (response) => {
          alert('✅ Reserva cancelada com sucesso!');
          this.buscarAlertas();
        },
        error: (error) => {
          console.error('❌ Erro ao cancelar:', error);
          alert('Erro ao cancelar: ' + (error.error?.erro || error.message));
        }
      });
    }
  }

  // ===== MODAL PRORROGAÇÃO =====

  abrirModalProrrogacao(alerta: AlertaDTO): void {
    this.alertaSelecionado = alerta;
    this.modalProrrogacao = true;
    
    const amanha = new Date();
    amanha.setDate(amanha.getDate() + 1);
    
    this.dia = amanha.getDate().toString().padStart(2, '0');
    this.mes = (amanha.getMonth() + 1).toString().padStart(2, '0');
    this.ano = amanha.getFullYear().toString();
    this.hora = '12';
    this.minuto = '00';
  }

  fecharModalProrrogacao(): void {
    this.modalProrrogacao = false;
    this.alertaSelecionado = null;
    this.dia = '';
    this.mes = '';
    this.ano = '';
    this.hora = '';
    this.minuto = '';
    this.motivoProrrogacao = 'Solicitação do hóspede';
  }

  formatarDataPreview(): string {
    if (!this.dia || !this.mes || !this.ano || !this.hora || !this.minuto) {
      return '⚠️ Selecione data e hora completas';
    }
    
    return `${this.dia}/${this.mes}/${this.ano} às ${this.hora}:${this.minuto}`;
  }

  confirmarProrrogacao(): void {
    if (!this.alertaSelecionado || !this.dia || !this.mes || !this.ano || !this.hora || !this.minuto) {
      alert('⚠️ Preencha todos os campos de data e hora');
      return;
    }

    const novoCheckoutISO = `${this.ano}-${this.mes}-${this.dia}T${this.hora}:${this.minuto}:00`;

    console.log('📅 Data montada:', `${this.dia}/${this.mes}/${this.ano} ${this.hora}:${this.minuto}`);
    console.log('📤 Enviando para backend:', novoCheckoutISO);

    this.alertasService.prorrogarCheckout(
      this.alertaSelecionado.reservaId!,
      novoCheckoutISO,
      this.motivoProrrogacao
    ).subscribe({
      next: (response) => {
        alert('✅ Checkout prorrogado com sucesso!');
        this.fecharModalProrrogacao();
        this.buscarAlertas();
      },
      error: (error) => {
        console.error('❌ Erro ao prorrogar:', error);
        alert('Erro ao prorrogar: ' + (error.error?.erro || error.message));
      }
    });
  }

  /**
 * 📱 ENVIAR SMS (PLACEHOLDER PARA FUTURA INTEGRAÇÃO)
 */
enviarSMS(alerta: AlertaDTO): void {
  alert(
    `📱 ENVIO DE SMS\n\n` +
    `Função em desenvolvimento.\n\n` +
    `Em breve você poderá enviar SMS automáticos para:\n` +
    `• ${alerta.clienteNome}\n` +
    `• Apartamento: ${alerta.numeroApartamento}\n\n` +
    `Integração via Make.com + Twilio/Zenvia`
  );
  
  console.log('📱 SMS solicitado para:', {
    cliente: alerta.clienteNome,
    apartamento: alerta.numeroApartamento,
    tipo: alerta.tipoAlerta,
    reservaId: alerta.reservaId
  });
}
}