import { Component, OnInit, AfterViewInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ReservaService } from '../../services/reserva.service';
import { HasPermissionDirective } from '../../directives/has-permission.directive';

// ════════════════════════════════════════════
// INTERFACE
// ════════════════════════════════════════════

interface ReservaLista {
  id: number;
  cliente?: {
    id: number;
    nome: string;
  };
  apartamento?: {
    id: number;
    numeroApartamento: string;
  };
  quantidadeHospede: number;
  dataCheckin: string;
  dataCheckout: string;
  totalHospedagem: number;
  totalApagar: number;
  status: string;
}

// ════════════════════════════════════════════
// COMPONENT
// ════════════════════════════════════════════

@Component({
  selector: 'app-reserva-lista',
  standalone: true,
  imports: [CommonModule, FormsModule, HasPermissionDirective ],
  template: `
    <div class="container">
      <!-- ========================================== -->
      <!-- HEADER -->
      <!-- ========================================== -->
      <div class="header">
        <h1>📋 Reservas</h1>
        <div class="header-actions">
          <!-- DROPDOWN DE RELATÓRIOS -->
          <div class="dropdown-container">
            <button class="btn-relatorios" (click)="toggleDropdown(); $event.stopPropagation()">
              📊 Relatórios ▼
            </button>
            
            <!-- MENU DROPDOWN -->
            <div class="dropdown-menu" *ngIf="dropdownAberto">
              <button class="dropdown-item" (click)="abrirRelatorioOcupacao()">
                🏨 Apartamentos Ocupados
              </button>
              <button class="dropdown-item" (click)="abrirRelatorioCheckouts()">
                📤 Checkouts do Dia
              </button>
              <button class="dropdown-item" (click)="abrirMapaImpressao()">
                🗺️ Imprimir Mapa
              </button>
            </div>
          </div>

          <!-- BOTÃO BUSCAR POR PLACA -->
          <button class="btn-buscar-placa" (click)="abrirModalBuscarPlaca()">
            🚗 Buscar por Placa
          </button>

          <!-- BOTÃO NOVA RESERVA -->
          <button class="btn-novo" (click)="novaReserva()">
            ➕ Nova Reserva
          </button>
        </div>
      </div>

      <!-- ========================================== -->
      <!-- BUSCA RÁPIDA POR ID (DESTAQUE NO TOPO) -->
      <!-- ========================================== -->
      <div class="busca-rapida-id">
        <div class="busca-rapida-header">
          <h2>🆔 Busca Rápida por Número da Reserva</h2>
          <button 
            *ngIf="resultadoPesquisaId"
            class="btn-limpar-busca-id"
            (click)="limparPesquisaId()"
            title="Limpar pesquisa">
            ✖
          </button>
        </div>
        
        <div class="busca-rapida-content">
          <input 
            type="number"
            [(ngModel)]="idReservaPesquisa"
            (input)="limparResultadoPesquisaId()"
            (keyup.enter)="pesquisarPorId()"
            placeholder="Digite o número da reserva (ex: 1234)..."
            class="input-busca-id"
            min="1">
          <button 
            class="btn-buscar-id"
            (click)="pesquisarPorId()">
            🔍 Buscar
          </button>
        </div>
        
        <div class="info-busca-id">
          💡 Use esta busca quando o cliente informar o número da reserva (PRÉ-RESERVA, ATIVA, FINALIZADA ou CANCELADA)
        </div>
        
        <!-- RESULTADO DA PESQUISA POR ID -->
        <div class="resultado-busca-id" *ngIf="resultadoPesquisaId">
          <div class="resultado-id-header" [class.sucesso]="resultadoPesquisaId.sucesso" [class.erro]="!resultadoPesquisaId.sucesso">
            <strong>{{ resultadoPesquisaId.sucesso ? '✅' : '❌' }}</strong>
            <span *ngIf="!resultadoPesquisaId.sucesso">{{ resultadoPesquisaId.mensagem }}</span>
            <span *ngIf="resultadoPesquisaId.sucesso">RESERVA ENCONTRADA</span>
          </div>
          
          <div class="resultado-id-dados" *ngIf="resultadoPesquisaId.sucesso && resultadoPesquisaId.reserva">
            <div class="dados-grid">
              <div class="dado-destaque">
                <span class="dado-label">Reserva:</span>
                <span class="dado-valor-destaque">#{{ resultadoPesquisaId.reserva.id }}</span>
              </div>
              
              <div class="dado-destaque">
                <span class="dado-label">Status:</span>
                <span [class]="'badge-status-grande status-' + resultadoPesquisaId.reserva.status.toLowerCase()">
                  {{ formatarStatus(resultadoPesquisaId.reserva.status) }}
                </span>
              </div>
            </div>
            
            <div class="dados-grid-2">
              <div class="dado-item-id">
                <span class="dado-label">👤 Cliente:</span>
                <span class="dado-valor">{{ resultadoPesquisaId.reserva.cliente }}</span>
              </div>
              
              <div class="dado-item-id">
                <span class="dado-label">🏨 Apartamento:</span>
                <span class="dado-valor numero-apt">{{ resultadoPesquisaId.reserva.apartamento }}</span>
              </div>
              
              <div class="dado-item-id">
                <span class="dado-label">👥 Hóspedes:</span>
                <span class="dado-valor">{{ resultadoPesquisaId.reserva.quantidadeHospede }}</span>
              </div>
              
              <div class="dado-item-id">
                <span class="dado-label">💰 Total:</span>
                <span class="dado-valor">R$ {{ formatarMoeda(resultadoPesquisaId.reserva.totalHospedagem) }}</span>
              </div>
              
              <div class="dado-item-id">
                <span class="dado-label">📅 Check-in:</span>
                <span class="dado-valor">{{ formatarDataComHora(resultadoPesquisaId.reserva.dataCheckin) }}</span>
              </div>
              
              <div class="dado-item-id">
                <span class="dado-label">📅 Check-out:</span>
                <span class="dado-valor">{{ formatarDataComHora(resultadoPesquisaId.reserva.dataCheckout) }}</span>
              </div>
            </div>

            <button 
              class="btn-ver-detalhes-id"
              (click)="verDetalhes(resultadoPesquisaId.reserva.id)">
              👁️ Ver Detalhes da Reserva
            </button>
          </div>
        </div>
      </div>

      <!-- ========================================== -->
      <!-- SEÇÃO DE PESQUISAS (CLIENTE E EMPRESA) -->
      <!-- ========================================== -->
      <div class="secao-pesquisas">
        <!-- Pesquisa por Cliente -->
        <div class="pesquisa-box">
          <div class="pesquisa-header">
            <h3>🔍 Pesquisar Cliente</h3>
            <button 
              *ngIf="resultadoPesquisaCliente"
              class="btn-limpar-pesquisa"
              (click)="limparPesquisaCliente()"
              title="Limpar pesquisa">
              ✖
            </button>
          </div>
          <div class="pesquisa-input-group">
            <input 
              type="text"
              [(ngModel)]="pesquisaCliente"
              (input)="limparResultadoPesquisaCliente()"
              placeholder="Digite o nome do cliente..."
              class="input-pesquisa">
            <button 
              class="btn-pesquisar"
              (click)="pesquisarCliente()"
              [disabled]="pesquisaCliente.length < 2">
              🔍 Pesquisar
            </button>
          </div>
          
          <!-- Resultado da pesquisa de cliente -->
          <div class="resultado-pesquisa" *ngIf="resultadoPesquisaCliente">
            <div class="resultado-header">
              <strong>{{ resultadoPesquisaCliente.sucesso ? '✅' : '❌' }}</strong>
              <span>{{ resultadoPesquisaCliente.mensagem }}</span>
            </div>
            
            <div class="resultado-dados" *ngIf="resultadoPesquisaCliente.sucesso && resultadoPesquisaCliente.reserva">
              <div class="dado-item">
                <span class="dado-label">Cliente:</span>
                <span class="dado-valor">{{ resultadoPesquisaCliente.reserva.cliente }}</span>
              </div>
              <div class="dado-item">
                <span class="dado-label">Apartamento:</span>
                <span class="dado-valor numero-apt">{{ resultadoPesquisaCliente.reserva.apartamento }}</span>
              </div>
              <div class="dado-item">
                <span class="dado-label">Check-in:</span>
                <span class="dado-valor">{{ formatarDataComHora(resultadoPesquisaCliente.reserva.dataCheckin) }}</span>
              </div>
              <div class="dado-item">
                <span class="dado-label">Check-out:</span>
                <span class="dado-valor">{{ formatarDataComHora(resultadoPesquisaCliente.reserva.dataCheckout) }}</span>
              </div>
              <div class="dado-item">
                <span class="dado-label">Status:</span>
                <span [class]="'badge-status status-' + resultadoPesquisaCliente.reserva.status.toLowerCase()">
                  {{ resultadoPesquisaCliente.reserva.status }}
                </span>
              </div>

              <button 
                class="btn-ver-reserva"
                (click)="verDetalhes(resultadoPesquisaCliente.reserva.id)">
                👁️ Ver Reserva
              </button>
            </div>
          </div>
        </div>

        <!-- Pesquisa por Empresa -->
        <div class="pesquisa-box">
          <div class="pesquisa-header">
            <h3>🏢 Pesquisar Empresa</h3>
            <button 
              *ngIf="resultadoPesquisaEmpresa"
              class="btn-limpar-pesquisa"
              (click)="limparPesquisaEmpresa()"
              title="Limpar pesquisa">
              ✖
            </button>
          </div>
          <div class="pesquisa-input-group">
            <input 
              type="text"
              [(ngModel)]="pesquisaEmpresa"
              (input)="limparResultadoPesquisaEmpresa()"
              placeholder="Digite o nome da empresa..."
              class="input-pesquisa">
            <button 
              class="btn-pesquisar"
              (click)="pesquisarEmpresa()"
              [disabled]="pesquisaEmpresa.length < 2">
              🔍 Pesquisar
            </button>
          </div>
          
          <!-- Resultado da pesquisa de empresa -->
          <div class="resultado-pesquisa" *ngIf="resultadoPesquisaEmpresa">
            <div class="resultado-header">
              <strong>{{ resultadoPesquisaEmpresa.sucesso ? '✅' : '❌' }}</strong>
              <span>{{ resultadoPesquisaEmpresa.mensagem }}</span>
            </div>
            
            <div class="resultado-dados" *ngIf="resultadoPesquisaEmpresa.sucesso && resultadoPesquisaEmpresa.hospedes">
              <div class="empresa-titulo">
                Empresa: <strong>{{ resultadoPesquisaEmpresa.nomeEmpresa }}</strong>
              </div>
              
              <div class="lista-hospedes-empresa">
                <div class="hospede-empresa-item" *ngFor="let hospede of resultadoPesquisaEmpresa.hospedes">
                  <div class="hospede-apt">🏨 Apartamento {{ hospede.apartamento }}</div>
                  <div class="hospede-nome">
                    {{ hospede.nomeCliente }}
                    <span class="badge-titular" *ngIf="hospede.titular">★ TITULAR</span>
                  </div>
                  <button 
                    class="btn-ver-reserva-mini"
                    (click)="verDetalhes(hospede.reservaId)">
                    👁️
                  </button>
                </div>
              </div>
              
              <div class="totais-empresa">
                <div class="total-item">
                  <span class="total-icon">🏨</span>
                  <span class="total-label">Apartamentos:</span>
                  <span class="total-valor">{{ resultadoPesquisaEmpresa.totalApartamentos }}</span>
                </div>
                <div class="total-item">
                  <span class="total-icon">👥</span>
                  <span class="total-label">Hóspedes:</span>
                  <span class="total-valor">{{ resultadoPesquisaEmpresa.totalHospedes }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ========================================== -->
      <!-- CARDS DE ESTATÍSTICAS -->
      <!-- ========================================== -->
      <div class="cards-estatisticas">
        <div class="card-stat card-apartamentos">
          <div class="stat-icon">🏨</div>
          <div class="stat-info">
            <div class="stat-label">Apartamentos Ocupados</div>
            <div class="stat-valor">{{ totalApartamentosOcupados }}</div>
          </div>
        </div>

        <div class="card-stat card-hospedes">
          <div class="stat-icon">👥</div>
          <div class="stat-info">
            <div class="stat-label">Total de Hóspedes</div>
            <div class="stat-valor">{{ totalHospedes }}</div>
          </div>
        </div>
      </div>

      <!-- ========================================== -->
      <!-- FILTROS -->
      <!-- ========================================== -->
      <div class="filtros">
        <button 
          [class.active]="filtroStatus === ''"
          (click)="filtrarPorStatus('')">
          Todas
        </button>
        <button 
          [class.active]="filtroStatus === 'ATIVA'"
          (click)="filtrarPorStatus('ATIVA')">
          Ativas
        </button>
        <button 
          [class.active]="filtroStatus === 'FINALIZADA'"
          (click)="filtrarPorStatus('FINALIZADA')">
          Finalizadas
        </button>
        <button 
          [class.active]="filtroStatus === 'CANCELADA'"
          (click)="filtrarPorStatus('CANCELADA')">
          Canceladas
        </button>
        <button 
          [class.active]="filtroStatus === 'PRE_RESERVA'"
          (click)="filtrarPorStatus('PRE_RESERVA')">
          Pré-Reservas
        </button>
      </div>

      <!-- ========================================== -->
      <!-- LOADING -->
      <!-- ========================================== -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Carregando reservas...</p>
      </div>

      <!-- ========================================== -->
      <!-- TABELA -->
      <!-- ========================================== -->
      <div *ngIf="!loading" class="tabela-container">
        <table>
          <thead>
            <tr>
              <th>#ID</th>
              <th>Cliente</th>
              <th>Apartamento</th>
              <th>👥 Hóspedes</th>
              <th>Check-in</th>
              <th>Check-out</th>
              <th>Total</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngIf="reservasFiltradas.length === 0">
              <td colspan="9" class="sem-dados">
                Nenhuma reserva encontrada
              </td>
            </tr>
            <tr *ngFor="let reserva of reservasFiltradas">
              <td>{{ reserva.id }}</td>
              <td>{{ reserva.cliente?.nome || 'N/A' }}</td>
              <td>
                <span class="numero-apt">{{ reserva.apartamento?.numeroApartamento || 'N/A' }}</span>
              </td>
              <td class="coluna-hospedes">
                <span class="badge-hospedes">👥 {{ reserva.quantidadeHospede }}</span>
              </td>              
              <td>{{ formatarDataComHora(reserva.dataCheckin) }}</td>
              <td>{{ formatarDataComHora(reserva.dataCheckout) }}</td>
              <td>R$ {{ formatarMoeda(reserva.totalHospedagem) }}</td>
              <td>
                <!-- BADGES DE ALERTA -->
                <span *ngIf="isPreReservaHoje(reserva)" class="badge-alerta-hoje">
                  🔔 HOJE
                </span>
                <span *ngIf="isPreReservaAmanha(reserva)" class="badge-alerta-amanha">
                  ⏰ AMANHÃ
                </span>
                
                <span [class]="'badge-status status-' + reserva.status.toLowerCase()">
                  {{ formatarStatus(reserva.status) }}
                </span>
              </td>
              <td class="acoes">
  <button 
    class="btn-visualizar" 
    (click)="verDetalhes(reserva.id)"
    title="Ver detalhes">
    👁️
  </button>
  
  <ng-container *hasPermission="'RESERVA_FINALIZAR'">
    <button 
      *ngIf="reserva.status === 'ATIVA'"
      class="btn-finalizar" 
      (click)="confirmarFinalizacao(reserva)"
      title="Finalizar reserva">
      ✅
    </button>
  </ng-container>
  
  <ng-container *hasPermission="'RESERVA_CANCELAR'">
    <button 
      *ngIf="reserva.status === 'ATIVA'"
      class="btn-cancelar" 
      (click)="confirmarCancelamento(reserva)"
      title="Cancelar reserva">
      ❌
    </button>
  </ng-container>
</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- ========================================== -->
      <!-- MODAL FINALIZAR -->
      <!-- ========================================== -->
      <div class="modal-overlay" *ngIf="modalFinalizar" (click)="fecharModalFinalizar()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>✅ Finalizar Reserva</h2>
          <div class="modal-info">
            <p><strong>Reserva:</strong> #{{ reservaParaFinalizar?.id }}</p>
            <p><strong>Cliente:</strong> {{ reservaParaFinalizar?.cliente?.nome }}</p>
            <p><strong>Apartamento:</strong> {{ reservaParaFinalizar?.apartamento?.numeroApartamento }}</p>
          </div>
          
          <div class="aviso" *ngIf="temSaldoDevedor()">
            ⚠️ ATENÇÃO: Existe saldo devedor!
          </div>
          
          <div class="modal-footer">
            <button class="btn-cancelar-modal" (click)="fecharModalFinalizar()">
              Cancelar
            </button>
            <button 
              class="btn-confirmar" 
              (click)="finalizarReserva()">
              Confirmar Finalização
            </button>
          </div>
        </div>
      </div>

      <!-- ========================================== -->
      <!-- MODAL CANCELAR -->
      <!-- ========================================== -->
      <div class="modal-overlay" *ngIf="modalCancelar" (click)="fecharModalCancelar()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>❌ Cancelar Reserva</h2>
          <div class="modal-info">
            <p><strong>Reserva:</strong> #{{ reservaParaCancelar?.id }}</p>
            <p><strong>Cliente:</strong> {{ reservaParaCancelar?.cliente?.nome }}</p>
            <p><strong>Apartamento:</strong> {{ reservaParaCancelar?.apartamento?.numeroApartamento }}</p>
          </div>
          
          <div class="campo">
            <label>Motivo do Cancelamento *</label>
            <textarea 
              [(ngModel)]="motivoCancelamento"
              rows="4"
              placeholder="Informe o motivo do cancelamento...">
            </textarea>
          </div>
          
          <div class="modal-footer">
            <button class="btn-cancelar-modal" (click)="fecharModalCancelar()">
              Voltar
            </button>
            <button class="btn-confirmar" (click)="cancelarReserva()">
              Confirmar Cancelamento
            </button>
          </div>
        </div>
      </div>

      <!-- ========================================== -->
      <!-- MODAL BUSCAR POR PLACA -->
      <!-- ========================================== -->
      <div class="modal-overlay" *ngIf="modalBuscarPlaca" (click)="fecharModalBuscarPlaca()">
        <div class="modal-content modal-placa" (click)="$event.stopPropagation()">
          <h2>🚗 Buscar Hóspede por Placa</h2>
          
          <div class="busca-placa-form">
            <input 
              type="text"
              [(ngModel)]="placaBusca"
              (input)="formatarPlacaBusca()"
              (keyup.enter)="buscarPorPlaca()"
              placeholder="Digite a placa (ABC-1234)"
              maxlength="8"
              class="input-placa-busca"
              autofocus>
            
            <button 
              class="btn-buscar-placa-modal"
              (click)="buscarPorPlaca()"
              [disabled]="!placaBusca || placaBusca.length < 7">
              🔍 Buscar
            </button>
          </div>

          <!-- LOADING -->
          <div *ngIf="buscandoPlaca" class="loading-busca-placa">
            <div class="spinner-placa"></div>
            <p>Buscando...</p>
          </div>

          <!-- RESULTADOS -->
          <div *ngIf="resultadosBuscaPlaca && !buscandoPlaca" class="resultados-placa">
            
            <!-- NÃO ENCONTRADO -->
            <div *ngIf="!resultadosBuscaPlaca.encontrado" class="nao-encontrado-placa">
              ❌ Nenhum hóspede encontrado com a placa: <strong>{{ placaBusca }}</strong>
            </div>

            <!-- ENCONTRADO -->
            <div *ngIf="resultadosBuscaPlaca.encontrado" class="encontrado-placa">
              <div class="resultado-placa-header">
                ✅ Encontrado {{ resultadosBuscaPlaca.quantidade }} hóspede(s)
              </div>

              <div class="lista-resultados-placa">
                <div 
                  class="resultado-placa-item"
                  *ngFor="let hospedagem of resultadosBuscaPlaca.hospedagens">
                  
                  <div class="resultado-placa-principal">
                    <div class="hospede-placa-info">
                      <div class="hospede-placa-nome">
                        👤 {{ hospedagem.hospedeNome }}
                      </div>
                      <div class="hospede-placa-cpf">
                        📄 CPF: {{ formatarCPF(hospedagem.hospedeCpf) }}
                      </div>
                      <div class="hospede-placa-carro">
                        🚗 Placa: <strong>{{ hospedagem.placaCarro }}</strong>
                      </div>
                    </div>

                    <div class="reserva-placa-info">
                      <div class="reserva-placa-badge" [class]="'badge-' + hospedagem.status.toLowerCase()">
                        {{ formatarStatus(hospedagem.status) }}
                      </div>
                      <div class="apartamento-placa">
                        🏨 Apartamento {{ hospedagem.apartamento }}
                      </div>
                      <div class="datas-placa">
                        📅 {{ formatarData(hospedagem.dataCheckin) }} até {{ formatarData(hospedagem.dataCheckout) }}
                      </div>
                    </div>
                  </div>

                  <button 
                    class="btn-ver-reserva-placa"
                    (click)="verReservaPlaca(hospedagem.reservaId)">
                    👁️ Ver Reserva
                  </button>
                </div>
              </div>
            </div>
          </div>

          <button 
            type="button" 
            class="btn-fechar-modal-placa" 
            (click)="fecharModalBuscarPlaca()">
            ✕
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container {
      padding: 20px;
      max-width: 1400px;
      margin: 0 auto;
    }

    /* ════════════════════════════════════════════
       HEADER
       ════════════════════════════════════════════ */

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
      background: white;
      padding: 20px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    h1 {
      margin: 0;
      color: #2c3e50;
      font-size: 1.8em;
    }

    .header-actions {
      display: flex;
      gap: 10px;
      align-items: center;
      flex-wrap: wrap;
    }

    /* ════════════════════════════════════════════
       DROPDOWN DE RELATÓRIOS
       ════════════════════════════════════════════ */

    .dropdown-container {
      position: relative;
    }

    .btn-relatorios {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      padding: 12px 20px;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 600;
      font-size: 1em;
      transition: all 0.3s;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .btn-relatorios:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }

    .dropdown-menu {
      position: absolute;
      top: calc(100% + 8px);
      right: 0;
      background: white;
      border-radius: 8px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.15);
      min-width: 220px;
      z-index: 10000;
      overflow: hidden;
      animation: dropdownSlide 0.2s ease-out;
    }

    @keyframes dropdownSlide {
      from {
        opacity: 0;
        transform: translateY(-10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .dropdown-item {
      width: 100%;
      padding: 12px 20px;
      border: none;
      background: white;
      text-align: left;
      cursor: pointer;
      font-size: 0.95em;
      color: #2c3e50;
      transition: all 0.2s;
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .dropdown-item:hover {
      background: #f8f9fa;
      padding-left: 25px;
      color: #667eea;
    }

    /* ════════════════════════════════════════════
       BOTÕES DO HEADER
       ════════════════════════════════════════════ */

    .btn-buscar-placa {
      background: linear-gradient(135deg, #2196f3 0%, #1976d2 100%);
      color: white;
      border: none;
      padding: 12px 20px;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 600;
      font-size: 1em;
      transition: all 0.3s;
      white-space: nowrap;
    }

    .btn-buscar-placa:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(33, 150, 243, 0.4);
    }

    .btn-novo {
      background: linear-gradient(135deg, #27ae60 0%, #229954 100%);
      color: white;
      border: none;
      padding: 12px 20px;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 600;
      font-size: 1em;
      transition: all 0.3s;
    }

    .btn-novo:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(39, 174, 96, 0.4);
    }

    /* ════════════════════════════════════════════
       BUSCA RÁPIDA POR ID
       ════════════════════════════════════════════ */

    .busca-rapida-id {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      padding: 25px;
      margin-bottom: 25px;
      box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
      color: white;
    }

    .busca-rapida-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
    }

    .busca-rapida-header h2 {
      margin: 0;
      font-size: 1.4em;
      font-weight: 700;
    }

    .btn-limpar-busca-id {
      background: rgba(255, 255, 255, 0.2);
      color: white;
      border: 2px solid rgba(255, 255, 255, 0.5);
      width: 35px;
      height: 35px;
      border-radius: 50%;
      cursor: pointer;
      font-size: 1.2em;
      font-weight: bold;
      transition: all 0.3s;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .btn-limpar-busca-id:hover {
      background: rgba(255, 255, 255, 0.3);
      transform: rotate(90deg) scale(1.1);
    }

    .busca-rapida-content {
      display: flex;
      gap: 10px;
      margin-bottom: 12px;
    }

    .input-busca-id {
      flex: 1;
      padding: 15px 20px;
      border: 3px solid rgba(255, 255, 255, 0.3);
      border-radius: 10px;
      font-size: 1.1em;
      font-weight: 600;
      background: rgba(255, 255, 255, 0.95);
      color: #2c3e50;
      transition: all 0.3s;
    }

    .input-busca-id:focus {
      outline: none;
      border-color: white;
      background: white;
      box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.2);
    }

    .input-busca-id::placeholder {
      color: #95a5a6;
    }

    .btn-buscar-id {
      background: white;
      color: #667eea;
      border: none;
      padding: 15px 35px;
      border-radius: 10px;
      cursor: pointer;
      font-weight: 700;
      font-size: 1.1em;
      transition: all 0.3s;
      white-space: nowrap;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
    }

    .btn-buscar-id:hover {
      background: #f8f9fa;
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
    }

    .info-busca-id {
      font-size: 0.9em;
      opacity: 0.95;
      font-style: italic;
      margin-top: 8px;
    }

    .resultado-busca-id {
      margin-top: 20px;
      background: white;
      border-radius: 10px;
      overflow: hidden;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
      animation: slideDown 0.3s ease;
    }

    @keyframes slideDown {
      from {
        opacity: 0;
        transform: translateY(-20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .resultado-id-header {
      padding: 15px 20px;
      display: flex;
      align-items: center;
      gap: 12px;
      font-weight: 700;
      font-size: 1.1em;
    }

    .resultado-id-header.sucesso {
      background: linear-gradient(135deg, #27ae60 0%, #229954 100%);
      color: white;
    }

    .resultado-id-header.erro {
      background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
      color: white;
    }

    .resultado-id-header strong {
      font-size: 1.3em;
    }

    .resultado-id-dados {
      padding: 25px;
      color: #2c3e50;
    }

    .dados-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 15px;
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 2px solid #ecf0f1;
    }

    .dado-destaque {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .dado-valor-destaque {
      font-size: 2em;
      font-weight: 700;
      color: #667eea;
    }

    .badge-status-grande {
      padding: 10px 20px;
      border-radius: 8px;
      font-weight: 700;
      font-size: 1.1em;
      display: inline-block;
      text-transform: uppercase;
    }

    .dados-grid-2 {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 15px;
      margin-bottom: 20px;
    }

    .dado-item-id {
      display: flex;
      flex-direction: column;
      gap: 5px;
      padding: 12px;
      background: #f8f9fa;
      border-radius: 8px;
      border-left: 4px solid #667eea;
    }

    .dado-item-id .dado-label {
      font-size: 0.85em;
      color: #7f8c8d;
      font-weight: 600;
    }

    .dado-item-id .dado-valor {
      font-size: 1em;
      color: #2c3e50;
      font-weight: 600;
    }

    .btn-ver-detalhes-id {
      width: 100%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      padding: 15px 30px;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 700;
      font-size: 1.1em;
      transition: all 0.3s;
      box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
    }

    .btn-ver-detalhes-id:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
    }

    /* ════════════════════════════════════════════
       SEÇÃO DE PESQUISAS
       ════════════════════════════════════════════ */

    .secao-pesquisas {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
      gap: 20px;
      margin-bottom: 25px;
    }

    .pesquisa-box {
      background: white;
      padding: 20px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .pesquisa-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
    }

    .pesquisa-header h3 {
      margin: 0;
      color: #2c3e50;
      font-size: 1.1em;
    }

    .btn-limpar-pesquisa {
      background: #e74c3c;
      color: white;
      border: none;
      width: 30px;
      height: 30px;
      border-radius: 50%;
      cursor: pointer;
      font-size: 1em;
      font-weight: bold;
      transition: all 0.3s;
    }

    .btn-limpar-pesquisa:hover {
      background: #c0392b;
      transform: rotate(90deg) scale(1.1);
    }

    .pesquisa-input-group {
      display: flex;
      gap: 10px;
      margin-bottom: 15px;
    }

    .input-pesquisa {
      flex: 1;
      padding: 12px;
      border: 2px solid #ddd;
      border-radius: 8px;
      font-size: 1em;
      transition: all 0.3s;
    }

    .input-pesquisa:focus {
      outline: none;
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }

    .btn-pesquisar {
      background: #3498db;
      color: white;
      border: none;
      padding: 12px 20px;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.3s;
      white-space: nowrap;
    }

    .btn-pesquisar:hover:not(:disabled) {
      background: #2980b9;
      transform: translateY(-2px);
    }

    .btn-pesquisar:disabled {
      background: #bdc3c7;
      cursor: not-allowed;
      opacity: 0.6;
    }

    .resultado-pesquisa {
      margin-top: 15px;
      border-top: 2px solid #ecf0f1;
      padding-top: 15px;
    }

    .resultado-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 15px;
      font-size: 1em;
      color: #2c3e50;
    }

    .resultado-header strong {
      font-size: 1.2em;
    }

    .resultado-dados {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .dado-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid #ecf0f1;
    }

    .dado-label {
      font-weight: 600;
      color: #7f8c8d;
      font-size: 0.9em;
    }

    .dado-valor {
      font-weight: 500;
      color: #2c3e50;
    }

    .numero-apt {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 4px 12px;
      border-radius: 6px;
      font-weight: 700;
    }

    .btn-ver-reserva {
      background: #27ae60;
      color: white;
      border: none;
      padding: 10px 20px;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 600;
      margin-top: 10px;
      width: 100%;
      transition: all 0.3s;
    }

    .btn-ver-reserva:hover {
      background: #229954;
      transform: translateY(-2px);
    }

    .empresa-titulo {
      font-size: 1.05em;
      color: #2c3e50;
      margin-bottom: 12px;
      padding: 10px;
      background: #f8f9fa;
      border-radius: 6px;
    }

    .empresa-titulo strong {
      color: #667eea;
      font-size: 1.1em;
    }

    .lista-hospedes-empresa {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-bottom: 12px;
    }

    .hospede-empresa-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px;
      background: #f8f9fa;
      border-radius: 6px;
      border-left: 3px solid #667eea;
      transition: all 0.2s;
    }

    .hospede-empresa-item:hover {
      background: #e3f2fd;
      transform: translateX(5px);
    }

    .hospede-apt {
      font-size: 0.85em;
      color: #7f8c8d;
      min-width: 120px;
    }

    .hospede-nome {
      flex: 1;
      font-weight: 500;
      color: #2c3e50;
    }

    .badge-titular {
      background: #ffd700;
      color: #333;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 0.75em;
      font-weight: 700;
      margin-left: 5px;
    }

    .btn-ver-reserva-mini {
      background: #3498db;
      color: white;
      border: none;
      width: 36px;
      height: 36px;
      border-radius: 6px;
      cursor: pointer;
      font-size: 1.1em;
      transition: all 0.3s;
    }

    .btn-ver-reserva-mini:hover {
      background: #2980b9;
      transform: scale(1.1);
    }

    .totais-empresa {
      display: flex;
      gap: 10px;
      margin-top: 12px;
      padding-top: 12px;
      border-top: 2px solid #ecf0f1;
    }

    .total-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px;
      background: #f8f9fa;
      border-radius: 6px;
      flex: 1;
    }

    .total-icon {
      font-size: 1.3em;
    }

    .total-label {
      font-size: 0.85em;
      color: #7f8c8d;
    }

    .total-valor {
      font-weight: 700;
      color: #2c3e50;
      font-size: 1.1em;
      margin-left: auto;
    }

    /* ════════════════════════════════════════════
       CARDS DE ESTATÍSTICAS
       ════════════════════════════════════════════ */

    .cards-estatisticas {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 20px;
      margin-bottom: 25px;
    }

    .card-stat {
      background: white;
      padding: 20px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      display: flex;
      align-items: center;
      gap: 15px;
      transition: all 0.3s;
    }

    .card-stat:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 16px rgba(0,0,0,0.15);
    }

    .card-apartamentos {
      border-left: 4px solid #3498db;
    }

    .card-hospedes {
      border-left: 4px solid #27ae60;
    }

    .stat-icon {
      font-size: 2.5em;
      line-height: 1;
    }

    .stat-info {
      flex: 1;
    }

    .stat-label {
      font-size: 0.9em;
      color: #7f8c8d;
      margin-bottom: 5px;
    }

    .stat-valor {
      font-size: 2em;
      font-weight: 700;
      color: #2c3e50;
      line-height: 1;
    }

    /* ════════════════════════════════════════════
       BADGES DE ALERTA
       ════════════════════════════════════════════ */

    .badge-alerta-hoje {
      background: linear-gradient(135deg, #ff9800, #ff6f00);
      color: white;
      padding: 4px 10px;
      border-radius: 12px;
      font-weight: 700;
      font-size: 0.75em;
      animation: pulseAlert 2s ease-in-out infinite;
      box-shadow: 0 2px 8px rgba(255, 152, 0, 0.4);
      display: inline-block;
      margin-right: 5px;
    }

    .badge-alerta-amanha {
      background: linear-gradient(135deg, #ffd54f, #ffc107);
      color: #333;
      padding: 4px 10px;
      border-radius: 12px;
      font-weight: 700;
      font-size: 0.75em;
      animation: pulseAlert 3s ease-in-out infinite;
      display: inline-block;
      margin-right: 5px;
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

    /* ════════════════════════════════════════════
       FILTROS
       ════════════════════════════════════════════ */

    .filtros {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;
      flex-wrap: wrap;
    }

    .filtros button {
      padding: 10px 20px;
      border: 2px solid #ddd;
      background: white;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.3s;
      color: #7f8c8d;
    }

    .filtros button:hover {
      border-color: #667eea;
      color: #667eea;
      transform: translateY(-2px);
    }

    .filtros button.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-color: #667eea;
    }

    /* ════════════════════════════════════════════
       LOADING
       ════════════════════════════════════════════ */

    .loading {
      text-align: center;
      padding: 60px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
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

    /* ════════════════════════════════════════════
       TABELA
       ════════════════════════════════════════════ */

    .tabela-container {
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      overflow: hidden;
    }

    table {
      width: 100%;
      border-collapse: collapse;
    }

    thead {
      background: #2c3e50;
      color: white;
    }

    th {
      padding: 15px;
      text-align: left;
      font-weight: 600;
      font-size: 0.9em;
    }

    tbody tr {
      border-bottom: 1px solid #ecf0f1;
      transition: all 0.2s;
    }

    tbody tr:hover {
      background: #f8f9fa;
    }

    td {
      padding: 15px;
      color: #2c3e50;
    }

    .sem-dados {
      text-align: center;
      color: #95a5a6;
      font-style: italic;
      padding: 40px;
    }

    .badge-hospedes {
      background: #3498db;
      color: white;
      padding: 4px 10px;
      border-radius: 6px;
      font-size: 0.85em;
      font-weight: 600;
    }

    .badge-status {
      padding: 6px 12px;
      border-radius: 6px;
      font-weight: 600;
      font-size: 0.85em;
      display: inline-block;
    }

    .status-ativa {
      background: #d4edda;
      color: #155724;
    }

    .status-finalizada {
      background: #cce5ff;
      color: #004085;
    }

    .status-cancelada {
      background: #f8d7da;
      color: #721c24;
    }

    .status-pre_reserva {
      background: #fff3cd;
      color: #856404;
    }

    table th:nth-child(5),
    table td:nth-child(5),
    table th:nth-child(6),
    table td:nth-child(6) {
      min-width: 140px;
      white-space: nowrap;
      font-size: 0.9em;
    }

    /* ════════════════════════════════════════════
       AÇÕES
       ════════════════════════════════════════════ */

    .acoes {
      display: flex;
      gap: 8px;
    }

    .btn-visualizar,
    .btn-finalizar,
    .btn-cancelar {
      border: none;
      padding: 8px 12px;
      border-radius: 6px;
      cursor: pointer;
      font-size: 1.1em;
      transition: all 0.3s;
    }

    .btn-visualizar {
      background: #3498db;
      color: white;
    }

    .btn-visualizar:hover {
      background: #2980b9;
      transform: scale(1.1);
    }

    .btn-finalizar {
      background: #27ae60;
      color: white;
    }

    .btn-finalizar:hover {
      background: #229954;
      transform: scale(1.1);
    }

    .btn-cancelar {
      background: #e74c3c;
      color: white;
    }

    .btn-cancelar:hover {
      background: #c0392b;
      transform: scale(1.1);
    }

    /* ════════════════════════════════════════════
       MODAIS
       ════════════════════════════════════════════ */

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
      box-shadow: 0 10px 40px rgba(0,0,0,0.3);
      position: relative;
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

    .aviso {
      background: #fff3cd;
      color: #856404;
      padding: 15px;
      border-radius: 8px;
      border-left: 4px solid #ffc107;
      margin-bottom: 20px;
      font-weight: 600;
    }

    .campo {
      margin-bottom: 20px;
    }

    .campo label {
      display: block;
      margin-bottom: 8px;
      color: #2c3e50;
      font-weight: 600;
    }

    .campo textarea {
      width: 100%;
      padding: 10px;
      border: 2px solid #ddd;
      border-radius: 8px;
      font-size: 1em;
      font-family: inherit;
      resize: vertical;
    }

    .campo textarea:focus {
      outline: none;
      border-color: #667eea;
    }

    .modal-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 20px;
    }

    .btn-cancelar-modal,
    .btn-confirmar {
      padding: 10px 20px;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.3s;
    }

    .btn-cancelar-modal {
      background: #95a5a6;
      color: white;
    }

    .btn-cancelar-modal:hover {
      background: #7f8c8d;
    }

    .btn-confirmar {
      background: #27ae60;
      color: white;
    }

    .btn-confirmar:hover {
      background: #229954;
    }

    /* ════════════════════════════════════════════
       MODAL BUSCAR POR PLACA
       ════════════════════════════════════════════ */

    .modal-placa {
      max-width: 700px;
      max-height: 85vh;
      overflow-y: auto;
    }

    .busca-placa-form {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;
    }

    .input-placa-busca {
      flex: 1;
      padding: 14px 20px;
      border: 2px solid #2196f3;
      border-radius: 8px;
      font-family: 'Courier New', monospace;
      font-weight: bold;
      font-size: 1.2em;
      letter-spacing: 2px;
      text-transform: uppercase;
      transition: all 0.3s;
    }

    .input-placa-busca:focus {
      outline: none;
      border-color: #1976d2;
      box-shadow: 0 0 0 4px rgba(33, 150, 243, 0.1);
    }

    .btn-buscar-placa-modal {
      background: #2196f3;
      color: white;
      border: none;
      padding: 14px 30px;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 700;
      font-size: 1em;
      transition: all 0.3s;
      white-space: nowrap;
    }

    .btn-buscar-placa-modal:hover:not(:disabled) {
      background: #1976d2;
      transform: scale(1.05);
    }

    .btn-buscar-placa-modal:disabled {
      background: #bdc3c7;
      cursor: not-allowed;
      opacity: 0.6;
    }

    .loading-busca-placa {
      text-align: center;
      padding: 40px 20px;
      color: #7f8c8d;
    }

    .spinner-placa {
      border: 4px solid #f3f3f3;
      border-top: 4px solid #2196f3;
      border-radius: 50%;
      width: 40px;
      height: 40px;
      animation: spin 1s linear infinite;
      margin: 0 auto 15px;
    }

    .resultados-placa {
      margin-top: 20px;
    }

    .nao-encontrado-placa {
      background: #fff3cd;
      border: 2px solid #ffc107;
      border-radius: 8px;
      padding: 20px;
      text-align: center;
      color: #856404;
      font-size: 1em;
    }

    .nao-encontrado-placa strong {
      color: #2196f3;
      font-family: 'Courier New', monospace;
      font-size: 1.1em;
    }

    .encontrado-placa {
      margin-top: 15px;
    }

    .resultado-placa-header {
      background: linear-gradient(135deg, #27ae60 0%, #229954 100%);
      color: white;
      padding: 15px 20px;
      border-radius: 8px 8px 0 0;
      font-weight: 700;
      font-size: 1.1em;
    }

    .lista-resultados-placa {
      display: flex;
      flex-direction: column;
      gap: 15px;
      margin-top: 15px;
    }

    .resultado-placa-item {
      background: #f8f9fa;
      border: 2px solid #dee2e6;
      border-radius: 8px;
      padding: 20px;
      transition: all 0.3s;
    }

    .resultado-placa-item:hover {
      border-color: #2196f3;
      box-shadow: 0 4px 12px rgba(33, 150, 243, 0.2);
    }

    .resultado-placa-principal {
      display: flex;
      gap: 20px;
      margin-bottom: 15px;
    }

    .hospede-placa-info {
      flex: 1;
    }

    .hospede-placa-nome {
      font-weight: 700;
      font-size: 1.15em;
      color: #2c3e50;
      margin-bottom: 8px;
    }

    .hospede-placa-cpf,
    .hospede-placa-carro {
      font-size: 0.95em;
      color: #7f8c8d;
      margin-bottom: 5px;
    }

    .hospede-placa-carro strong {
      color: #2196f3;
      font-family: 'Courier New', monospace;
      font-size: 1.1em;
      letter-spacing: 1px;
    }

    .reserva-placa-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .reserva-placa-badge {
      padding: 8px 15px;
      border-radius: 6px;
      font-weight: 700;
      font-size: 0.9em;
      text-align: center;
    }

    .reserva-placa-badge.badge-ativa {
      background: #d4edda;
      color: #155724;
    }

    .reserva-placa-badge.badge-finalizada {
      background: #cce5ff;
      color: #004085;
    }

    .reserva-placa-badge.badge-pre_reserva {
      background: #fff3cd;
      color: #856404;
    }

    .apartamento-placa,
    .datas-placa {
      font-size: 0.9em;
      color: #555;
    }

    .btn-ver-reserva-placa {
      width: 100%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      padding: 12px 20px;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 700;
      font-size: 1em;
      transition: all 0.3s;
    }

    .btn-ver-reserva-placa:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }

    .btn-fechar-modal-placa {
      position: absolute;
      top: 20px;
      right: 20px;
      background: #e74c3c;
      color: white;
      border: none;
      width: 35px;
      height: 35px;
      border-radius: 50%;
      cursor: pointer;
      font-size: 1.3em;
      font-weight: bold;
      transition: all 0.3s;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .btn-fechar-modal-placa:hover {
      background: #c0392b;
      transform: rotate(90deg);
    }

    /* ════════════════════════════════════════════
       RESPONSIVO
       ════════════════════════════════════════════ */

    @media (max-width: 1400px) {
      table th:nth-child(5),
      table td:nth-child(5),
      table th:nth-child(6),
      table td:nth-child(6) {
        font-size: 0.85em;
      }
    }

    @media (max-width: 768px) {
      .secao-pesquisas {
        grid-template-columns: 1fr;
      }

      .header {
        flex-direction: column;
        gap: 15px;
      }

      .header-actions {
        width: 100%;
        flex-direction: column;
      }

      .btn-relatorios,
      .btn-buscar-placa,
      .btn-novo {
        width: 100%;
      }

      table {
        font-size: 0.85em;
      }

      th, td {
        padding: 10px;
      }

      .busca-rapida-content {
        flex-direction: column;
      }
      
      .dados-grid {
        grid-template-columns: 1fr;
      }
      
      .dados-grid-2 {
        grid-template-columns: 1fr;
      }

      .resultado-placa-principal {
        flex-direction: column;
      }
      
      .busca-placa-form {
        flex-direction: column;
      }
    }
  `]
})
export class ReservaListaApp implements OnInit, AfterViewInit {
  private router = inject(Router);
  private http = inject(HttpClient);
  private reservaService = inject(ReservaService);

  reservas: ReservaLista[] = [];
  reservasFiltradas: ReservaLista[] = [];
  loading = false;
  filtroStatus = 'ATIVA';
  totalApartamentosOcupados = 0;
  totalHospedes = 0;

  modalFinalizar = false;
  modalCancelar = false;
  reservaParaFinalizar: ReservaLista | null = null;
  reservaParaCancelar: ReservaLista | null = null;
  motivoCancelamento = '';

  // ✅ PROPRIEDADES PARA PESQUISAS
  pesquisaCliente = '';
  pesquisaEmpresa = '';
  resultadoPesquisaCliente: any = null;
  resultadoPesquisaEmpresa: any = null;

  idReservaPesquisa: number | null = null;
resultadoPesquisaId: any = null;

  // ✅ DROPDOWN DE RELATÓRIOS
  dropdownAberto = false;  
  modalBuscarPlaca = false;
  placaBusca = '';
  buscandoPlaca = false;
  resultadosBuscaPlaca: any = null;

  // ════════════════════════════════════════════
  // MÉTODOS DO CICLO DE VIDA
  // ════════════════════════════════════════════

  ngOnInit(): void {
    this.carregarReservas();
  }

  ngAfterViewInit(): void {
    // Fechar dropdown ao clicar fora
    document.addEventListener('click', (event) => {
      const target = event.target as HTMLElement;
      const dropdown = target.closest('.dropdown-container');
      
      if (!dropdown && this.dropdownAberto) {
        this.dropdownAberto = false;
      }
    });
  }

  // ════════════════════════════════════════════
  // CARREGAR RESERVAS E ESTATÍSTICAS
  // ════════════════════════════════════════════

  carregarReservas(): void {
    this.loading = true;
    
    this.http.get<any[]>('http://localhost:8080/api/reservas').subscribe({
      next: (data) => {
        // Ordenar por número de apartamento (crescente)
        this.reservas = data.sort((a, b) => {
          const numA = parseInt(a.apartamento?.numeroApartamento) || 0;
          const numB = parseInt(b.apartamento?.numeroApartamento) || 0;
          return numA - numB;
        });
        
        // ✅ CALCULAR ESTATÍSTICAS
        this.calcularEstatisticas();
        
        this.aplicarFiltro();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar reservas:', err);
        this.loading = false;
        alert('Erro ao carregar reservas');
      }
    });
  }

  /**
   * Calcula estatísticas (apartamentos ocupados e total de hóspedes)
   */
  calcularEstatisticas(): void {
    const reservasAtivas = this.reservas.filter(r => r.status === 'ATIVA');
    
    // Apartamentos ocupados (contar IDs únicos)
    const apartamentosUnicos = new Set(
      reservasAtivas.map(r => r.apartamento?.id).filter(id => id)
    );
    this.totalApartamentosOcupados = apartamentosUnicos.size;
    
    // Total de hóspedes
    this.totalHospedes = reservasAtivas.reduce(
      (total, r) => total + (r.quantidadeHospede || 0), 
      0
    );
    
    console.log('📊 Estatísticas atualizadas:');
    console.log('   🏨 Apartamentos ocupados: ' + this.totalApartamentosOcupados);
    console.log('   👥 Total de hóspedes: ' + this.totalHospedes);
  }

  // ════════════════════════════════════════════
  // FILTROS
  // ════════════════════════════════════════════

  filtrarPorStatus(status: string): void {
    this.filtroStatus = status;
    this.aplicarFiltro();
  }

  aplicarFiltro(): void {
    if (this.filtroStatus === '') {
      this.reservasFiltradas = [...this.reservas];
    } else {
      this.reservasFiltradas = this.reservas.filter(
        r => r.status === this.filtroStatus
      );
    }
  }

  // ════════════════════════════════════════════
  // DROPDOWN DE RELATÓRIOS
  // ════════════════════════════════════════════

  toggleDropdown(): void {
    this.dropdownAberto = !this.dropdownAberto;
  }

  abrirRelatorioOcupacao(): void {
    this.dropdownAberto = false;
    this.router.navigate(['/relatorios/ocupacao']);
  }

  abrirRelatorioCheckouts(): void {
    this.dropdownAberto = false;
    this.router.navigate(['/relatorios/checkouts']);
  }

  abrirMapaImpressao(): void {
    this.dropdownAberto = false;
    window.open('/mapa-reservas', '_blank');
  }

  // ════════════════════════════════════════════
  // PESQUISA DE CLIENTE
  // ════════════════════════════════════════════

  /**
   * Pesquisa cliente hospedado
   */
  pesquisarCliente(): void {
    if (this.pesquisaCliente.length < 2) {
      alert('⚠️ Digite pelo menos 2 caracteres do nome');
      return;
    }

    console.log('═══════════════════════════════════════════');
    console.log('🔍 PESQUISANDO CLIENTE: ' + this.pesquisaCliente);
    console.log('═══════════════════════════════════════════');

    this.http.get<any>('http://localhost:8080/api/reservas/pesquisar-cliente', {
      params: { nome: this.pesquisaCliente }
    }).subscribe({
      next: (resultado) => {
        console.log('✅ Resultado:', resultado);
        this.resultadoPesquisaCliente = resultado;
      },
      error: (erro) => {
        console.error('❌ Erro ao pesquisar cliente:', erro);
        this.resultadoPesquisaCliente = {
          sucesso: false,
          mensagem: 'Erro ao pesquisar: ' + (erro.error?.message || erro.message)
        };
      }
    });
  }

  /**
   * Limpa campo de pesquisa de cliente
   */
  limparPesquisaCliente(): void {
    this.pesquisaCliente = '';
    this.resultadoPesquisaCliente = null;
    console.log('🧹 Pesquisa de cliente limpa');
  }

  /**
   * Limpa apenas resultado (quando digitar)
   */
  limparResultadoPesquisaCliente(): void {
    this.resultadoPesquisaCliente = null;
  }

  // ════════════════════════════════════════════
  // PESQUISA DE EMPRESA
  // ════════════════════════════════════════════

  /**
   * Pesquisa hóspedes de uma empresa
   */
  pesquisarEmpresa(): void {
    if (this.pesquisaEmpresa.length < 2) {
      alert('⚠️ Digite pelo menos 2 caracteres do nome da empresa');
      return;
    }

    console.log('═══════════════════════════════════════════');
    console.log('🏢 PESQUISANDO EMPRESA: ' + this.pesquisaEmpresa);
    console.log('═══════════════════════════════════════════');

    this.http.get<any>('http://localhost:8080/api/reservas/pesquisar-empresa', {
      params: { nomeEmpresa: this.pesquisaEmpresa }
    }).subscribe({
      next: (resultado) => {
        console.log('✅ Resultado:', resultado);
        this.resultadoPesquisaEmpresa = resultado;
        
        if (resultado.sucesso) {
          console.log('   📊 Total de hóspedes: ' + resultado.totalHospedes);
          console.log('   🏨 Total de apartamentos: ' + resultado.totalApartamentos);
        }
      },
      error: (erro) => {
        console.error('❌ Erro ao pesquisar empresa:', erro);
        this.resultadoPesquisaEmpresa = {
          sucesso: false,
          mensagem: 'Erro ao pesquisar: ' + (erro.error?.message || erro.message)
        };
      }
    });
  }

  // ════════════════════════════════════════════════════════════
// PESQUISA POR ID DA RESERVA
// ════════════════════════════════════════════════════════════

/**
 * Pesquisa reserva por ID
 */
pesquisarPorId(): void {
  if (!this.idReservaPesquisa || this.idReservaPesquisa <= 0) {
    alert('⚠️ Digite o número da reserva');
    return;
  }

  console.log('═══════════════════════════════════════════');
  console.log('🔍 PESQUISANDO RESERVA POR ID: ' + this.idReservaPesquisa);
  console.log('═══════════════════════════════════════════');

  this.http.get<any>(`http://localhost:8080/api/reservas/${this.idReservaPesquisa}`).subscribe({
    next: (reserva) => {
      console.log('✅ Reserva encontrada:', reserva);
      
      this.resultadoPesquisaId = {
        sucesso: true,
        reserva: {
          id: reserva.id,
          cliente: reserva.cliente?.nome || 'N/A',
          apartamento: reserva.apartamento?.numeroApartamento || 'N/A',
          dataCheckin: reserva.dataCheckin,
          dataCheckout: reserva.dataCheckout,
          quantidadeHospede: reserva.quantidadeHospede,
          status: reserva.status,
          totalHospedagem: reserva.totalHospedagem
        }
      };
    },
    error: (err) => {
      console.error('❌ Erro ao pesquisar reserva:', err);
      
      this.resultadoPesquisaId = {
        sucesso: false,
        mensagem: err.status === 404 
          ? `Nenhuma reserva encontrada com o número ${this.idReservaPesquisa}` 
          : 'Erro ao buscar reserva. Tente novamente.'
      };
    }
  });
}

/**
 * Limpa campo de pesquisa por ID
 */
limparPesquisaId(): void {
 this.idReservaPesquisa = null; 
  this.resultadoPesquisaId = null;
  console.log('🧹 Pesquisa por ID limpa');
}

/**
 * Limpa apenas resultado (quando digitar)
 */
limparResultadoPesquisaId(): void {
  this.resultadoPesquisaId = null;
}

  /**
   * Limpa campo de pesquisa de empresa
   */
  limparPesquisaEmpresa(): void {
    this.pesquisaEmpresa = '';
    this.resultadoPesquisaEmpresa = null;
    console.log('🧹 Pesquisa de empresa limpa');
  }

  /**
   * Limpa apenas resultado (quando digitar)
   */
  limparResultadoPesquisaEmpresa(): void {
    this.resultadoPesquisaEmpresa = null;
  }

  

  // ════════════════════════════════════════════
  // ALERTAS DE PRÉ-RESERVAS
  // ════════════════════════════════════════════

  /**
   * Verifica se pré-reserva é HOJE
   */
  isPreReservaHoje(reserva: any): boolean {
    if (reserva.status !== 'PRE_RESERVA') return false;
    
    const hoje = new Date().toISOString().split('T')[0];
    const checkin = new Date(reserva.dataCheckin).toISOString().split('T')[0];
    
    return checkin === hoje;
  }

  /**
   * Verifica se pré-reserva é AMANHÃ
   */
  isPreReservaAmanha(reserva: any): boolean {
    if (reserva.status !== 'PRE_RESERVA') return false;
    
    const amanha = new Date();
    amanha.setDate(amanha.getDate() + 1);
    const amanhaStr = amanha.toISOString().split('T')[0];
    
    const checkin = new Date(reserva.dataCheckin).toISOString().split('T')[0];
    
    return checkin === amanhaStr;
  }

  // ════════════════════════════════════════════
  // NAVEGAÇÃO E AÇÕES
  // ════════════════════════════════════════════

  novaReserva(): void {
    this.router.navigate(['/reservas/novo']);
  }

  verDetalhes(id: number): void {
    this.router.navigate(['/reservas', id]);
  }

  // ════════════════════════════════════════════
  // FINALIZAR RESERVA
  // ════════════════════════════════════════════

  confirmarFinalizacao(reserva: ReservaLista): void {
    this.reservaParaFinalizar = reserva;
    this.modalFinalizar = true;
  }

  fecharModalFinalizar(): void {
    this.modalFinalizar = false;
    this.reservaParaFinalizar = null;
  }

  temSaldoDevedor(): boolean {
    if (!this.reservaParaFinalizar) return false;
    return this.reservaParaFinalizar.totalApagar > 0;
  }

  finalizarReserva(): void {
    if (!this.reservaParaFinalizar) return;

    this.http.patch(
      `http://localhost:8080/api/reservas/${this.reservaParaFinalizar.id}/finalizar`,
      {}
    ).subscribe({
      next: () => {
        alert('✅ Reserva finalizada com sucesso!');
        this.fecharModalFinalizar();
        this.carregarReservas();
      },
      error: (err) => {
        console.error('Erro ao finalizar:', err);
        alert('❌ Erro ao finalizar reserva: ' + (err.error?.erro || err.message));
      }
    });
  }

  // ════════════════════════════════════════════
  // CANCELAR RESERVA
  // ════════════════════════════════════════════

  confirmarCancelamento(reserva: ReservaLista): void {
    this.reservaParaCancelar = reserva;
    this.motivoCancelamento = '';
    this.modalCancelar = true;
  }

  fecharModalCancelar(): void {
    this.modalCancelar = false;
    this.reservaParaCancelar = null;
    this.motivoCancelamento = '';
  }

  cancelarReserva(): void {
    if (!this.reservaParaCancelar) return;

    if (!this.motivoCancelamento.trim()) {
      alert('⚠️ Por favor, informe o motivo do cancelamento');
      return;
    }

    this.http.patch(
      `http://localhost:8080/api/reservas/${this.reservaParaCancelar.id}/cancelar`,
      {},
      { params: { motivo: this.motivoCancelamento } }
    ).subscribe({
      next: () => {
        alert('✅ Reserva cancelada com sucesso!');
        this.fecharModalCancelar();
        this.carregarReservas();
      },
      error: (err) => {
        console.error('Erro ao cancelar:', err);
        alert('❌ Erro ao cancelar reserva: ' + err.message);
      }
    });
  }

  // ════════════════════════════════════════════
  // FORMATAÇÃO
  // ════════════════════════════════════════════

  formatarData(data: string | Date): string {
    if (!data) return 'N/A';
    const d = new Date(data);
    return d.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
 * Formata data com hora para exibição
 */
formatarDataComHora(data: string | Date): string {
  if (!data) return 'N/A';
  const d = new Date(data);
  
  const dataFormatada = d.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
  
  const horaFormatada = d.toLocaleTimeString('pt-BR', {
    hour: '2-digit',
    minute: '2-digit'
  });
  
  return `${dataFormatada} ${horaFormatada}`;
}

  formatarMoeda(valor: number): string {
    if (valor === null || valor === undefined) return '0,00';
    return valor.toFixed(2).replace('.', ',');
  }

  formatarStatus(status: string): string {
    const statusMap: { [key: string]: string } = {
      'ATIVA': 'Ativa',
      'FINALIZADA': 'Finalizada',
      'CANCELADA': 'Cancelada',
      'PRE_RESERVA': 'Pré-Reserva'
    };
    return statusMap[status] || status;
  }

  // ════════════════════════════════════════════
// BUSCA POR PLACA
// ════════════════════════════════════════════

/**
 * Abre modal de busca por placa
 */
abrirModalBuscarPlaca(): void {
  this.modalBuscarPlaca = true;
  this.placaBusca = '';
  this.resultadosBuscaPlaca = null;
  console.log('🚗 Modal de busca por placa aberto');
}

/**
 * Fecha modal de busca por placa
 */
fecharModalBuscarPlaca(): void {
  this.modalBuscarPlaca = false;
  this.placaBusca = '';
  this.resultadosBuscaPlaca = null;
  console.log('🚗 Modal de busca por placa fechado');
}

/**
 * Formata placa durante digitação
 */
formatarPlacaBusca(): void {
  if (!this.placaBusca) return;
  
  let placa = this.placaBusca.toUpperCase().replace(/[^A-Z0-9]/g, '');
  
  if (placa.length > 3) {
    placa = placa.substring(0, 3) + '-' + placa.substring(3, 7);
  }
  
  this.placaBusca = placa;
}

/**
 * Busca hóspedes por placa
 */
buscarPorPlaca(): void {
  if (!this.placaBusca || this.placaBusca.length < 7) {
    alert('⚠️ Digite uma placa válida!');
    return;
  }

  console.log('═══════════════════════════════════════════');
  console.log('🚗 BUSCANDO PLACA: ' + this.placaBusca);
  console.log('═══════════════════════════════════════════');

  this.buscandoPlaca = true;
  this.resultadosBuscaPlaca = null;

  this.http.get<any>(`http://localhost:8080/api/reservas/buscar-por-placa/${this.placaBusca}`)
    .subscribe({
      next: (resultado) => {
        this.buscandoPlaca = false;
        this.resultadosBuscaPlaca = resultado;
        
        console.log('✅ Resultado da busca:', resultado);
        
        if (resultado.encontrado) {
          console.log('   📊 Quantidade: ' + resultado.quantidade);
          console.log('   👥 Hóspedes:', resultado.hospedagens);
        }
      },
      error: (erro) => {
        this.buscandoPlaca = false;
        console.error('❌ Erro ao buscar placa:', erro);
        
        this.resultadosBuscaPlaca = {
          encontrado: false,
          mensagem: 'Erro ao buscar placa. Tente novamente.'
        };
      }
    });
}

/**
 * Ver reserva a partir da busca de placa
 */
verReservaPlaca(reservaId: number): void {
  this.fecharModalBuscarPlaca();
  this.verDetalhes(reservaId);
}

/**
 * Formata CPF
 */
formatarCPF(cpf: string): string {
  if (!cpf) return 'Não informado';
  const numeros = cpf.replace(/\D/g, '');
  return numeros.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
}

}