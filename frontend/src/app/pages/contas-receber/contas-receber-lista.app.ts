//import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ContaReceberService, ContaAReceber, PagamentoConta } from '../../services/conta-receber.service';
import { HttpClient } from '@angular/common/http';
import { HasPermissionDirective } from '../../directives/has-permission.directive';
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { ExportService } from '../../services/export.service';

interface FiltrosAvancados {
  empresaId?: number;
  clienteId?: number;
  clienteNome?: string;
  dataCheckInInicio?: string;
  dataCheckInFim?: string;
  dataCheckOutInicio?: string;
  dataCheckOutFim?: string;
  status?: string;
}

@Component({
  selector: 'app-contas-receber-lista',
  standalone: true,
  imports: [CommonModule, FormsModule, HasPermissionDirective],
  template: `
    <div class="container">
      <!-- HEADER -->
      <div class="header">
        <h1>💰 Contas a Receber</h1>
        <div class="header-actions">
          <button class="btn btn-filtros" (click)="abrirModalFiltros()">
            🔍 Filtros Avançados
          </button>
          <button class="btn btn-atualizar" (click)="atualizarVencidas()">
            🔄 Atualizar
          </button>
          <button class="btn btn-exportar" (click)="exportar()" *ngIf="contasFiltradas.length > 0">
            📊 Exportar Excel
          </button>

          <button class="btn btn-relatorio-detalhado" 
  *ngIf="filtrosAplicados.empresaId && contasFiltradas.length > 0"
  (click)="imprimirRelatorioDetalhado()">
  📋 Relatório Detalhado
</button>

          <button class="btn btn-baixa-lote" *ngIf="filtrosAplicados.empresaId && contasFiltradas.length > 0" (click)="abrirModalBaixaLote()">✅ Dar baixa em todas ({{ contasFiltradas.length }})</button>
         
          <button *hasPermission="'CONTA_RECEBER_CRIAR'" 
        class="btn btn-criar" 
        (click)="abrirModalCriar()">
           ➕ Nova Conta
        </button>
        </div>
      </div>

      <!-- FILTROS ATIVOS -->
      <div class="filtros-ativos" *ngIf="temFiltrosAtivos()">
        <div class="filtro-tag">
          <strong>📌 Filtros Ativos:</strong>
        </div>
        <div class="filtro-tag" *ngIf="filtrosAplicados.empresaId">
          🏢 Empresa: {{ obterNomeEmpresa(filtrosAplicados.empresaId) }}
          <button (click)="removerFiltro('empresaId')">✕</button>
        </div>
        <div class="filtro-tag" *ngIf="filtrosAplicados.clienteNome">
  👤 Cliente: {{ filtrosAplicados.clienteNome }}
  <button (click)="removerFiltro('clienteId')">✕</button>
</div>
        <div class="filtro-tag" *ngIf="filtrosAplicados.dataCheckInInicio && filtrosAplicados.dataCheckInFim">
          📅 Check-in: {{ formatarData(filtrosAplicados.dataCheckInInicio) }} a {{ formatarData(filtrosAplicados.dataCheckInFim) }}
          <button (click)="removerFiltro('checkin')">✕</button>
        </div>
        <div class="filtro-tag" *ngIf="filtrosAplicados.dataCheckOutInicio && filtrosAplicados.dataCheckOutFim">
          📅 Check-out: {{ formatarData(filtrosAplicados.dataCheckOutInicio) }} a {{ formatarData(filtrosAplicados.dataCheckOutFim) }}
          <button (click)="removerFiltro('checkout')">✕</button>
        </div>
        <div class="filtro-tag" *ngIf="filtrosAplicados.status">
          📊 Status: {{ obterTextoStatus(filtrosAplicados.status) }}
          <button (click)="removerFiltro('status')">✕</button>
        </div>
        <button class="btn-limpar-todos" (click)="limparTodosFiltros()">
          🗑️ Limpar Todos
        </button>
      </div>

      <!-- RESUMO -->
      <div class="resumo">
        <div class="card-resumo verde">
          <div class="icone">💵</div>
          <div class="info">
            <span class="label">Total a Receber</span>
            <span class="valor">R$ {{ calcularTotalAReceber() | number:'1.2-2' }}</span>
          </div>
        </div>

        <div class="card-resumo amarelo">
          <div class="icone">⚠️</div>
          <div class="info">
            <span class="label">Vencidas</span>
            <span class="valor">{{ contarVencidas() }} conta(s)</span>
          </div>
        </div>

        <div class="card-resumo azul">
          <div class="icone">📋</div>
          <div class="info">
            <span class="label">Filtradas</span>
            <span class="valor">{{ contasFiltradas.length }} conta(s)</span>
          </div>
        </div>

        <div class="card-resumo roxo">
          <div class="icone">💰</div>
          <div class="info">
            <span class="label">Total Filtrado</span>
            <span class="valor">R$ {{ calcularTotalFiltrado() | number:'1.2-2' }}</span>
          </div>
        </div>
      </div>

      <!-- LOADING -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Carregando...</p>
      </div>

      <!-- TABELA -->
      <div *ngIf="!loading" class="tabela-container no-print">
        <div class="tabela-header">
          <h3>📋 Resultados ({{ contasFiltradas.length }})</h3>
          <button class="btn-imprimir" (click)="imprimirRelatorio()" *ngIf="contasFiltradas.length > 0">
            🖨️ Imprimir
          </button>
        </div>

        <table class="tabela">
          <thead>
           <tr>
  <th>Reserva</th>
  <th>Apto</th>
  <th>Hóspedes</th>
  <th>Empresa</th>
  <th>Check-in/Check-out</th>
  <th>Saldo</th>
  <th>Vencimento</th>
  <th>Status</th>
  <th class="no-print">Ações</th>
</tr>
          </thead>
          <tbody>
            <tr *ngFor="let conta of contasFiltradas" [class]="'linha-' + conta.status.toLowerCase()">
             <td>#{{ conta.reservaId }}</td>
             <td>{{ conta.numeroApartamento || '-' }}</td>
             <td>{{ conta.todosHospedes || conta.clienteNome }}</td>
              <td>
                <span *ngIf="conta.empresaNome" class="badge-empresa">
                  🏢 {{ conta.empresaNome }}
                </span>
                <span *ngIf="!conta.empresaNome" class="sem-empresa">-</span>
              </td>
            
              <td class="datas">
                <div>✅ {{ formatarData(conta.dataCheckin) }}</div>
                <div>📤 {{ formatarData(conta.dataCheckout) }}</div>
              </td>
              <td class="saldo">R$ {{ conta.saldo | number:'1.2-2' }}</td>
              <td>
                {{ formatarData(conta.dataVencimento) }}
                <span *ngIf="conta.diasVencido > 0" class="badge-vencido">
                  {{ conta.diasVencido }} dia(s)
                </span>
              </td>
              <td>
                <span [class]="'badge badge-' + conta.status.toLowerCase()">
                  {{ obterTextoStatus(conta.status) }}
                </span>
              </td>

              <td class="acoes no-print">
                <button 
                  *hasPermission="'CONTA_RECEBER_PAGAMENTO'"
                  [hidden]="conta.status === 'PAGA'"
                  class="btn-acao btn-pagar"
                  (click)="abrirModalPagamento(conta)"
                  title="Registrar pagamento">
                  💳
                </button>

                <button
                  *ngIf="conta.status !== 'PAGA' && conta.status !== 'CANCELADA'"
                   class="btn-acao btn-desconto"
                   (click)="abrirModalDesconto(conta)"
                   title="Aplicar desconto">
                   💸
                </button>
                <button 
                  *hasPermission="'CONTA_RECEBER_PAGAMENTO'"
                  [hidden]="conta.status !== 'PAGA'"
                  class="btn-acao btn-excluir"
                  (click)="excluir(conta)"
                  title="Excluir">
                  🗑️
                </button>
                <!-- ✅ IMPRIMIR FATURA INDIVIDUAL -->
                <button 
                  *ngIf="temReserva(conta)"
                  class="btn-acao btn-fatura"
                  (click)="imprimirFatura(conta)"
                  title="Imprimir fatura">
                  🖨️
                </button>
              </td>
            </tr>
          </tbody>
          <tfoot *ngIf="contasFiltradas.length > 0">
            <tr class="total-row">
             <td colspan="4"><strong>TOTAL:</strong></td>
               <td><strong>R$ {{ calcularTotalSaldo() | number:'1.2-2' }}</strong></td>
               <td colspan="3"></td>
            </tr>
          </tfoot>
        </table>

        <div *ngIf="contasFiltradas.length === 0" class="vazio">
          <p>📭 Nenhuma conta encontrada com os filtros aplicados</p>
          <button class="btn" (click)="abrirModalFiltros()">🔍 Ajustar Filtros</button>
        </div>
      </div>

      <!-- MODAL FILTROS AVANÇADOS -->
      <div class="modal-overlay" *ngIf="modalFiltros" (click)="fecharModalFiltros()">
        <div class="modal-content modal-grande" (click)="$event.stopPropagation()">
          <h2>🔍 Filtros Avançados</h2>
          <p class="subtitle">Selecione os critérios desejados para filtrar as contas</p>

          <div class="filtros-grid">
            <!-- EMPRESA -->
            <div class="campo">
              <label>🏢 Empresa</label>
              <select [(ngModel)]="filtrosTemp.empresaId">
                <option [ngValue]="undefined">Todas as empresas</option>
                <option *ngFor="let empresa of empresas" [ngValue]="empresa.id">
                  {{ empresa.nomeEmpresa }}
                </option>
              </select>
            </div>

            <!-- CLIENTE -->
            <div class="campo">
              <label>👤 Cliente</label>
              <input type="text" 
  [(ngModel)]="filtroClienteTexto"
  (input)="filtrarClientesOpcoes()"
  placeholder="Digite o nome do cliente..."
  class="filtro-input">
<div class="lista-resultados" *ngIf="clientesFiltrados.length > 0">
  <div class="item-resultado" 
    *ngFor="let c of clientesFiltrados"
    (click)="selecionarClienteFiltro(c)">
    {{ c.nome }}
  </div>
</div>
<div *ngIf="filtrosTemp.clienteId" style="margin-top:5px; color:#27ae60;">
  ✅ {{ obterNomeCliente(filtrosTemp.clienteId) }}
  <span style="cursor:pointer; color:#e74c3c;" (click)="limparClienteFiltro()"> ✕</span>
</div>
            </div>

            <!-- STATUS -->
            <div class="campo">
              <label>📊 Status</label>
              <select [(ngModel)]="filtrosTemp.status">
                <option [ngValue]="undefined">Todos os status</option>
                <option value="EM_ABERTO">Em Aberto</option>
                <option value="VENCIDAS">Vencidas</option>
                <option value="PAGA">Pagas</option>
              </select>
            </div>

            <!-- PERÍODO CHECK-IN -->
            <div class="campo campo-duplo">
              <label>📅 Período de Check-in</label>
              <div class="periodo">
                <input type="date" [(ngModel)]="filtrosTemp.dataCheckInInicio" placeholder="De">
                <span>até</span>
                <input type="date" [(ngModel)]="filtrosTemp.dataCheckInFim" placeholder="Até">
              </div>
              <small>Filtra pelas reservas que deram check-in neste período</small>
            </div>

            <!-- PERÍODO CHECK-OUT -->
            <div class="campo campo-duplo">
              <label>📅 Período de Check-out</label>
              <div class="periodo">
                <input type="date" [(ngModel)]="filtrosTemp.dataCheckOutInicio" placeholder="De">
                <span>até</span>
                <input type="date" [(ngModel)]="filtrosTemp.dataCheckOutFim" placeholder="Até">
              </div>
              <small>Filtra pelas reservas que deram/darão check-out neste período</small>
            </div>
          </div>

          <!-- ATALHOS RÁPIDOS -->
          <div class="atalhos">
            <h3>⚡ Atalhos Rápidos</h3>
            <div class="atalhos-btns">
              <button class="btn-atalho" (click)="atalhoMesAtual()">
                📅 Mês Atual
              </button>
              <button class="btn-atalho" (click)="atalhoMesPassado()">
                📅 Mês Passado
              </button>
              <button class="btn-atalho" (click)="atalhoVencidas()">
                ⚠️ Apenas Vencidas
              </button>
              <button class="btn-atalho" (click)="atalhoPagas()">
                ✅ Apenas Pagas
              </button>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn-cancelar" (click)="fecharModalFiltros()">Cancelar</button>
            <button class="btn-limpar" (click)="limparFiltrosTemp()">🗑️ Limpar</button>
            <button class="btn-confirmar" (click)="aplicarFiltrosAvancados()">Aplicar Filtros</button>
          </div>
        </div>
      </div>

      <!-- MODAL CRIAR CONTA -->
      <div class="modal-overlay" *ngIf="modalCriar" (click)="fecharModalCriar()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>➕ Nova Conta a Receber</h2>

          <div class="campo">
            <label>Reserva *</label>
            <select [(ngModel)]="novaConta.reservaId">
              <option [ngValue]="0">Selecione uma reserva...</option>
              <option *ngFor="let reserva of reservasDisponiveis" [ngValue]="reserva.id">
                #{{ reserva.id }} - {{ reserva.clienteNome }} - R$ {{ reserva.totalApagar | number:'1.2-2' }}
              </option>
            </select>
          </div>

          <div class="campo">
            <label>Empresa (opcional)</label>
            <select [(ngModel)]="novaConta.empresaId">
              <option [ngValue]="0">Nenhuma</option>
              <option *ngFor="let empresa of empresas" [ngValue]="empresa.id">
                {{ empresa.nomeEmpresa }}
              </option>
            </select>
          </div>

          <div class="campo">
            <label>Valor *</label>
            <input type="number" [(ngModel)]="novaConta.valor" step="0.01" min="0.01">
          </div>

          <div class="campo">
            <label>Data de Vencimento *</label>
            <input type="date" [(ngModel)]="novaConta.dataVencimento">
          </div>

          <div class="campo">
            <label>Descrição *</label>
            <textarea [(ngModel)]="novaConta.descricao" rows="3"></textarea>
          </div>

          <div class="modal-footer">
            <button class="btn-cancelar" (click)="fecharModalCriar()">Cancelar</button>
            <button class="btn-confirmar" (click)="criarConta()">Criar Conta</button>
          </div>
        </div>
      </div>

      

      <!-- MODAL PAGAMENTO -->
      <div class="modal-overlay" *ngIf="modalPagamento" (click)="fecharModalPagamento()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>💳 Registrar Pagamento</h2>

          <div class="info-conta">
            <p><strong>Cliente:</strong> {{ contaSelecionada?.clienteNome }}</p>
            <p><strong>Descrição:</strong> {{ contaSelecionada?.descricao }}</p>
            <p><strong>Saldo:</strong> R$ {{ contaSelecionada?.saldo | number:'1.2-2' }}</p>
          </div>

          <div class="campo">
            <label>Valor a Pagar *</label>
            <input type="number" [(ngModel)]="pagamento.valorPago" step="0.01" min="0.01" 
                   [max]="contaSelecionada?.saldo || 0">
            <small>Máximo: R$ {{ contaSelecionada?.saldo | number:'1.2-2' }}</small>
          </div>

          <div class="campo">
            <label>Data do Pagamento *</label>
            <input type="date" [(ngModel)]="pagamento.dataPagamento">
          </div>

          <div class="campo">
            <label>Forma de Pagamento *</label>
            <select [(ngModel)]="pagamento.formaPagamento">
              <option value="">Selecione...</option>
              <option value="DINHEIRO">Dinheiro</option>
              <option value="PIX">PIX</option>
              <option value="CARTAO_DEBITO">Cartão Débito</option>
              <option value="CARTAO_CREDITO">Cartão Crédito</option>
              <option value="TRANSFERENCIA">Transferência</option>
            </select>
          </div>

          <div class="campo">
            <label>Observação</label>
            <textarea [(ngModel)]="pagamento.observacao" rows="3"></textarea>
          </div>

          <div class="modal-footer">
           <button class="btn-cancelar" (click)="fecharModalPagamento()">Cancelar</button>
            <button class="btn-confirmar" (click)="registrarPagamento()">Confirmar Pagamento</button>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL BAIXA EM LOTE -->
    <div class="modal-overlay" *ngIf="modalBaixaLote">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <h2>✅ Dar baixa em lote</h2>
        <div class="campo">
          <label>Data do Pagamento *</label>
          <input type="date" [(ngModel)]="pagamentoLote.dataPagamento">
        </div>
        <div class="campo">
          <label>Forma de Pagamento *</label>
          <select [(ngModel)]="pagamentoLote.formaPagamento">
            <option value="">Selecione...</option>
            <option value="DINHEIRO">Dinheiro</option>
            <option value="PIX">PIX</option>
            <option value="TRANSFERENCIA_BANCARIA">Transferência</option>
            <option value="CARTAO_CREDITO">Cartão Crédito</option>
            <option value="CARTAO_DEBITO">Cartão Débito</option>
          </select>
        </div>
        <div class="campo">
          <label>Observação</label>
          <textarea [(ngModel)]="pagamentoLote.observacao" rows="2"></textarea>
        </div>
        <div class="modal-footer">
          <button class="btn-cancelar" (click)="modalBaixaLote = false">Cancelar</button>
          <button class="btn-confirmar" (click)="darBaixaEmLote()">✅ Confirmar</button>
        </div>
      </div>
    </div>
    
     <!-- MODAL DESCONTO -->
    <div class="modal-overlay" *ngIf="modalDesconto">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <h2>💸 Aplicar Desconto</h2>
        <div class="info-conta">
          <p><strong>Cliente:</strong> {{ contaDescontoSelecionada?.clienteNome }}</p>
          <p><strong>Saldo:</strong> R$ {{ contaDescontoSelecionada?.saldo | number:'1.2-2' }}</p>
        </div>
        <div class="campo">
          <label>Valor do Desconto *</label>
          <input type="number" [(ngModel)]="valorDesconto" step="0.01" min="0.01"
                 [max]="contaDescontoSelecionada?.saldo || 0">
        </div>
        <div class="campo">
          <label>Motivo *</label>
          <input type="text" [(ngModel)]="motivoDesconto" placeholder="Informe o motivo...">
        </div>
        <div class="modal-footer">
          <button class="btn-cancelar" (click)="modalDesconto = false">Cancelar</button>
          <button class="btn-confirmar" (click)="confirmarDesconto()">✅ Confirmar</button>
        </div>
      </div>
    </div>

    <!-- ÁREA DE IMPRESSÃO (OCULTA NA TELA) -->
    <div class="print-only">
   <div class="print-header">
  <p style="font-size:11pt; font-weight:700; margin:0;">SANTOS E CORREIA LTDA</p>
  <h1>HOTEL DI VAN</h1>
  <p class="print-subtitle">CNPJ: 07.757.726/0001-12 | Arapiraca - AL</p>
  <div class="separador" style="margin:8px 0;">================================</div>
  <p class="print-subtitle">{{ obterTituloCabecalho() }} — Relatório de Contas a Receber</p>
  <p class="print-date">{{ dataHoraRelatorio }}</p>
    
    <div class="print-filters" *ngIf="temFiltrosAtivos()">
      <strong>Filtros Aplicados:</strong>
      <span *ngIf="filtrosAplicados.empresaId">Empresa: {{ obterNomeEmpresa(filtrosAplicados.empresaId) }}</span>
      <span *ngIf="filtrosAplicados.clienteId">Cliente: {{ obterNomeCliente(filtrosAplicados.clienteId) }}</span>
      <span *ngIf="filtrosAplicados.status">Status: {{ obterTextoStatus(filtrosAplicados.status) }}</span>
      <span *ngIf="filtrosAplicados.dataCheckOutInicio">Período: {{ formatarData(filtrosAplicados.dataCheckOutInicio) }} a {{ formatarData(filtrosAplicados.dataCheckOutFim) }}</span>
    </div>
  </div>

  <table class="print-table">
  <thead>
  <tr>
    <th>Reserva</th>
    <th>Hóspedes</th>
    <th>Check-in</th>
    <th>Check-out</th>
    <th>Apto</th>
    <th>Hósp.</th>
    <th>QT. Diár.</th>
    <th>Vlr Diárias</th>
    <th>Consumo</th>
    <th>Total Hosp.</th>
    <th>Desconto</th>
    <th>Pago à Vista</th>
    <th>Faturado</th>
  </tr>
</thead>
    <tbody>
  <tr *ngFor="let conta of contasFiltradas">
    <td>#{{ conta.reservaId }}</td>
    <td>{{ conta.todosHospedes || conta.clienteNome }}</td>
    <td>{{ formatarData($any(conta).dataCheckin) }}</td>
    <td>{{ formatarData($any(conta).dataCheckout) }}</td>
    <td>{{ conta.numeroApartamento || '-' }}</td>
    <td>{{ conta.quantidadeHospede || '-' }}</td>
    <td>{{ conta.quantidadeDiaria || '-' }}</td>
    <td class="valor">{{ formatarMoeda(conta.totalDiaria) }}</td>
    <td class="valor">{{ formatarMoeda(conta.totalConsumo) }}</td>
    <td class="valor">{{ formatarMoeda(conta.totalHospedagem) }}</td>
    <td class="valor">{{ (conta.desconto || 0) > 0 ? formatarMoeda(conta.desconto) : '-' }}</td>
    <td class="valor">{{ formatarMoeda(conta.pagoAVista || 0) }}</td>
    <td class="valor destaque">{{ formatarMoeda(conta.valor) }}</td>
  </tr>
</tbody>
    <tbody>
      <tr class="total-row">
        <td colspan="7"><strong>TOTAIS:</strong></td>
       <td class="valor"><strong>{{ formatarMoeda(calcularTotalGeralDiarias()) }}</strong></td>
       <td class="valor"><strong>{{ formatarMoeda(calcularTotalGeralConsumo()) }}</strong></td>
       <td class="valor"><strong>{{ formatarMoeda(calcularTotalGeralHospedagem()) }}</strong></td>
       <td class="valor"><strong>{{ formatarMoeda(calcularTotalGeralDesconto()) }}</strong></td>
       <td class="valor"><strong>{{ formatarMoeda(calcularTotalGeralRecebido()) }}</strong></td>
       <td class="valor destaque"><strong>{{ formatarMoeda(calcularTotalGeralAPagar()) }}</strong></td>
      </tr>
    </tbody>
  </table>  

  <div class="print-footer">
    <p>Total de registros: {{ contasFiltradas.length }}</p>
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

    .header h1 {
      margin: 0;
      color: #2c3e50;
    }

    .header-actions {
      display: flex;
      gap: 10px;
    }

    .btn {
      padding: 10px 20px;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
      color: white;
    }

    .btn-filtros { background: #9b59b6; }
    .btn-filtros:hover { background: #8e44ad; }

    .btn-atualizar { background: #3498db; }
    .btn-atualizar:hover { background: #2980b9; }

    .btn-criar { background: #27ae60; }
    .btn-criar:hover { background: #229954; }

    /* FILTROS ATIVOS */
    .filtros-ativos {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      background: white;
      padding: 15px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      margin-bottom: 20px;
      align-items: center;
    }

    .filtro-tag {
      background: #e3f2fd;
      color: #1976d2;
      padding: 8px 12px;
      border-radius: 20px;
      font-size: 0.9em;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .filtro-tag button {
      background: none;
      border: none;
      color: #1976d2;
      font-size: 1.2em;
      cursor: pointer;
      padding: 0;
      width: 20px;
      height: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .filtro-tag button:hover { color: #c62828; }

    .btn-limpar-todos {
      background: #e74c3c;
      color: white;
      padding: 8px 16px;
      border: none;
      border-radius: 20px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn-limpar-todos:hover { background: #c0392b; }

    /* RESUMO */
    .resumo {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 20px;
      margin-bottom: 30px;
    }

    .card-resumo {
      background: white;
      padding: 20px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      gap: 15px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      border-left: 4px solid;
    }

    .card-resumo.verde  { border-left-color: #27ae60; }
    .card-resumo.amarelo { border-left-color: #f39c12; }
    .card-resumo.azul   { border-left-color: #3498db; }
    .card-resumo.roxo   { border-left-color: #9b59b6; }

    .card-resumo .icone { font-size: 2.5em; }

    .card-resumo .info {
      display: flex;
      flex-direction: column;
    }

    .card-resumo .label {
      font-size: 0.9em;
      color: #7f8c8d;
      margin-bottom: 5px;
    }

    .card-resumo .valor {
      font-size: 1.3em;
      font-weight: 700;
      color: #2c3e50;
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
      0%   { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    /* TABELA */
    .tabela-container {
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      overflow: hidden;
    }

    .tabela-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      border-bottom: 2px solid #e0e0e0;
    }

    .tabela-header h3 {
      margin: 0;
      color: #2c3e50;
    }

    .btn-imprimir {
      padding: 10px 20px;
      background: #9b59b6;
      color: white;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn-imprimir:hover { background: #8e44ad; }

    .tabela {
      width: 100%;
      border-collapse: collapse;
    }

    .tabela th {
      background: #2c3e50;
      color: white;
      padding: 15px;
      text-align: left;
      font-weight: 600;
      font-size: 0.9em;
    }

    .tabela td {
      padding: 12px 15px;
      border-bottom: 1px solid #e0e0e0;
      font-size: 0.9em;
    }

    .tabela tbody tr:hover { background: #f5f5f5; }

    .linha-vencida { background: #ffebee !important; }
    .linha-paga    { opacity: 0.7; }

    .datas { font-size: 0.85em; }
    .datas div { padding: 2px 0; }

    .badge-empresa {
      background: #e3f2fd;
      color: #1976d2;
      padding: 4px 10px;
      border-radius: 8px;
      font-size: 0.85em;
      font-weight: 600;
    }

    .sem-empresa { color: #bdc3c7; }

    .saldo {
      font-weight: 700;
      color: #27ae60;
    }

    .badge {
      padding: 5px 10px;
      border-radius: 12px;
      font-size: 0.85em;
      font-weight: 600;
    }

    .badge-em_aberto  { background: #e3f2fd; color: #1976d2; }
    .badge-vencida    { background: #ffebee; color: #c62828; }
    .badge-paga       { background: #e8f5e9; color: #2e7d32; }

    .badge-vencido {
      background: #ffebee;
      color: #c62828;
      padding: 3px 8px;
      border-radius: 8px;
      font-size: 0.75em;
      margin-left: 5px;
      display: inline-block;
    }

    .total-row { background: #f5f5f5; font-weight: 700; }

    .total-row td {
      border-top: 2px solid #2c3e50;
      padding: 15px;
    }

    .acoes { display: flex; gap: 5px; }

    .btn-acao {
      padding: 8px;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-size: 1.2em;
      transition: all 0.3s;
    }

    .btn-pagar  { background: #27ae60; color: white; }
    .btn-pagar:hover  { background: #229954; transform: scale(1.1); }

    .btn-excluir { background: #e74c3c; color: white; }
    .btn-excluir:hover { background: #c0392b; transform: scale(1.1); }

    .vazio {
      text-align: center;
      padding: 60px;
      color: #7f8c8d;
    }

    .vazio p {
      font-size: 1.2em;
      margin-bottom: 20px;
    }

    /* MODAL */
    .modal-overlay {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.7);
      display: flex;
      justify-content: center;
      align-items: center;
      z-index: 1000;
    }

    .modal-content {
      background: white;
      padding: 30px;
      border-radius: 12px;
      min-width: 500px;
      max-width: 90%;
      max-height: 90vh;
      overflow-y: auto;
    }

    .modal-grande { min-width: 800px; }

    .modal-content h2 {
      margin: 0 0 10px 0;
      color: #2c3e50;
    }

    .subtitle {
      color: #7f8c8d;
      margin-bottom: 30px;
    }

    .filtros-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
      margin-bottom: 30px;
    }

    .campo-duplo { grid-column: 1 / -1; }

    .info-conta {
      background: #f5f5f5;
      padding: 15px;
      border-radius: 8px;
      margin-bottom: 20px;
    }

    .info-conta p { margin: 8px 0; }

    .campo { margin-bottom: 20px; }

    .campo label {
      display: block;
      margin-bottom: 8px;
      font-weight: 600;
      color: #2c3e50;
    }

    .campo input,
    .campo select,
    .campo textarea {
      width: 100%;
      padding: 10px;
      border: 2px solid #e0e0e0;
      border-radius: 6px;
      font-size: 1em;
      font-family: inherit;
    }

    .campo input:focus,
    .campo select:focus,
    .campo textarea:focus {
      outline: none;
      border-color: #3498db;
    }

    .campo small {
      display: block;
      margin-top: 5px;
      color: #7f8c8d;
      font-size: 0.85em;
    }

    .periodo {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .periodo input { flex: 1; }

    .periodo span {
      color: #7f8c8d;
      font-weight: 600;
    }

    .atalhos {
      background: #f5f5f5;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 20px;
    }

    .atalhos h3 {
      margin: 0 0 15px 0;
      color: #2c3e50;
      font-size: 1em;
    }

    .atalhos-btns {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 10px;
    }

    .btn-atalho {
      padding: 10px;
      background: white;
      border: 2px solid #e0e0e0;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.3s;
    }

    .btn-atalho:hover {
      background: #3498db;
      color: white;
      border-color: #3498db;
    }

    .modal-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 20px;
    }

    .btn-cancelar,
    .btn-limpar,
    .btn-confirmar {
      padding: 10px 20px;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn-cancelar { background: #95a5a6; color: white; }
    .btn-cancelar:hover { background: #7f8c8d; }

    .btn-limpar { background: #e74c3c; color: white; }
    .btn-limpar:hover { background: #c0392b; }

    .btn-confirmar { background: #27ae60; color: white; }
    .btn-confirmar:hover { background: #229954; }

    /* ─── IMPRESSÃO ──────────────────────────────── */
  .print-only {
  display: none;
}

@media print {
  .no-print {
    display: none !important;
  }
  .container > *:not(.print-only) {
    display: none !important;
  }
  .print-only {
    display: block !important;
    padding: 20px;
  }
  .print-header {
    text-align: center;
    margin-bottom: 30px;
    border-bottom: 2px solid #2c3e50;
    padding-bottom: 15px;
  }
  .print-header h1 {
    margin: 0 0 10px 0;
    color: #2c3e50;
    font-size: 24px;
  }
  .print-subtitle {
    margin: 5px 0;
    color: #7f8c8d;
    font-size: 14px;
  }
  .print-date {
    margin: 5px 0;
    color: #95a5a6;
    font-size: 12px;
  }
  .print-filters {
    margin-top: 15px;
    padding: 10px;
    background: #f5f5f5;
    border-radius: 6px;
    font-size: 12px;
  }
  .print-filters span {
    display: inline-block;
    margin: 0 10px;
  }
  .print-table {
    width: 100%;
    border-collapse: collapse;
    margin: 20px 0;
    font-size: 11px;
  }
  .btn-fatura { background: #2980b9; color: white; }
  .print-table th,
  .print-table td {
    border: 1px solid #ddd;
    padding: 8px;
    text-align: left;
  }
  .print-table th {
    background-color: #2c3e50;
    color: white;
    font-weight: bold;
    font-size: 10px;
  }
  .print-table td.valor {
    text-align: right;
    font-family: 'Courier New', monospace;
  }
  .print-table td.destaque {
    font-weight: bold;
    background: #f0f0f0;
  }
  .print-table tbody tr:nth-child(even) {
    background-color: #f9f9f9;
  }
  .print-table tfoot tr {
    background-color: #e8e8e8;
    font-weight: bold;
  }
  .print-table tfoot td {
    border-top: 2px solid #2c3e50;
    padding: 12px 8px;
  }
  .print-footer {
    margin-top: 30px;
    text-align: center;
    font-size: 10px;
    color: #7f8c8d;
    border-top: 1px solid #ddd;
    padding-top: 10px;
  }
  body {
    print-color-adjust: exact;
    -webkit-print-color-adjust: exact;
  }
  @page { margin: 1cm; }
}

    /* ─── RESPONSIVE ─────────────────────────────── */
    @media (max-width: 768px) {
      .resumo               { grid-template-columns: 1fr; }
      .filtros-grid         { grid-template-columns: 1fr; }
      .atalhos-btns         { grid-template-columns: 1fr; }
      .modal-content        { min-width: 90%; }
      .modal-grande         { min-width: 90%; }
      .tabela-container     { overflow-x: auto; }
    }

    .btn-baixa-lote { background: #27ae60; }
    .btn-baixa-lote:hover { background: #229954; }

    .lista-resultados {
  border: 1px solid #ddd;
  border-radius: 5px;
  max-height: 200px;
  overflow-y: auto;
  background: white;
  z-index: 100;
}

.item-resultado {
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}

.item-resultado:hover {
  background: #f0f0f0;
}

.item-resultado:last-child {
  border-bottom: none;
}

.btn-exportar { background: #27ae60; }
.btn-exportar:hover { background: #229954; }

.btn-relatorio-detalhado { background: #8e44ad; }
.btn-relatorio-detalhado:hover { background: #7d3c98; }

.btn-desconto { background: #e67e22; color: white; }
.btn-desconto:hover { background: #ca6f1e; }

  `]
})
export class ContasReceberListaApp implements OnInit {
  private contaReceberService = inject(ContaReceberService);
  private http = inject(HttpClient);
  private router = inject(Router);
  private exportService = inject(ExportService);

  private cdr = inject(ChangeDetectorRef);

  contas: ContaAReceber[] = [];
  contasFiltradas: ContaAReceber[] = [];
  reservas: any[] = [];
  loading = false;

  // FILTROS
  modalFiltros = false;
  filtrosTemp: FiltrosAvancados = {};
  filtrosAplicados: FiltrosAvancados = {};
  empresas: any[] = [];
  clientes: any[] = [];

  modalDesconto = false;
  contaDescontoSelecionada: ContaAReceber | null = null;
  valorDesconto = 0;
  motivoDesconto = '';

  // MODAL CRIAR
  modalCriar = false;
  novaConta: any = {
    reservaId: 0,
    empresaId: 0,
    valor: 0,
    dataVencimento: '',
    descricao: ''
  };
  reservasDisponiveis: any[] = [];

  filtroClienteTexto = '';
  clientesFiltrados: any[] = [];

  modalBaixaLote = false;
pagamentoLote = {
  dataPagamento: new Date().toISOString().split('T')[0],
  formaPagamento: '',
  observacao: ''
};

  // MODAL PAGAMENTO
  modalPagamento = false;
  contaSelecionada: ContaAReceber | null = null;
  pagamento: PagamentoConta = {
    valorPago: 0,
    dataPagamento: '',
    formaPagamento: '',
    observacao: ''
  };
ngOnInit(): void {
  this.carregarDados();
}

  carregarDados(): void {
    this.loading = true;

this.contaReceberService.listarTodas().subscribe({
  next: (contas) => {
    this.contas = contas;

    // ✅ Extrai clientes únicos das contas
    const clientesMap = new Map<string, any>();
    this.contas.forEach((conta: any) => {
      if (conta.clienteNome && !clientesMap.has(conta.clienteNome)) {
        clientesMap.set(conta.clienteNome, { id: conta.reservaId, nome: conta.clienteNome });
      }
    });
    this.clientes = Array.from(clientesMap.values())
      .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));

    this.aplicarFiltros();
    this.loading = false;
  },
  error: (err) => {
    console.error('❌ Erro ao carregar contas:', err);
    this.loading = false;
    alert('Erro ao carregar contas');
  }
});

    this.http.get<any[]>('/api/empresas').subscribe({
      next: (data) => this.empresas = data
    });

  }

  // ========== FILTROS ==========

  abrirModalFiltros(): void {
  this.filtrosTemp = { ...this.filtrosAplicados };
  this.filtroClienteTexto = this.filtrosAplicados.clienteNome || '';
  this.clientesFiltrados = [];
  this.modalFiltros = true;
}

  fecharModalFiltros(): void {
    this.modalFiltros = false;
  }

  aplicarFiltrosAvancados(): void {
    this.filtrosAplicados = { ...this.filtrosTemp };
    this.aplicarFiltros();
    this.fecharModalFiltros();
  }

  aplicarFiltros(): void {
    let resultado = [...this.contas];

    if (this.filtrosAplicados.empresaId) {
      const empresaSelecionada = this.empresas.find(e => e.id === this.filtrosAplicados.empresaId);
      resultado = resultado.filter(c => c.empresaNome === empresaSelecionada?.nomeEmpresa);
    }

   if (this.filtrosAplicados.clienteNome) {
  const termo = this.filtrosAplicados.clienteNome.toLowerCase();
  resultado = resultado.filter(c =>
    c.clienteNome?.toLowerCase().includes(termo) ||
    c.todosHospedes?.toLowerCase().includes(termo)
  );
}


    if (this.filtrosAplicados.status) {
  const status = this.filtrosAplicados.status;
  if (status === 'EM_ABERTO') {
    resultado = resultado.filter(c => c.status !== 'PAGA' && c.status !== 'CANCELADA');
  } else if (status === 'VENCIDAS') {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    resultado = resultado.filter(c => {
      const vencimento = new Date(c.dataVencimento);
      vencimento.setHours(0, 0, 0, 0);
      return vencimento < hoje && c.status !== 'PAGA' && c.status !== 'CANCELADA';
    });
  } else {
    resultado = resultado.filter(c => c.status === status);
  }
}

   if (this.filtrosAplicados.dataCheckInInicio && this.filtrosAplicados.dataCheckInFim) {
  resultado = resultado.filter(c => {
    const dataCheckinRaw = (c as any).dataCheckin;
    if (!dataCheckinRaw) return false;
    const dataCheckin = new Date(dataCheckinRaw).toISOString().split('T')[0];
    return dataCheckin >= this.filtrosAplicados.dataCheckInInicio! &&
           dataCheckin <= this.filtrosAplicados.dataCheckInFim!;
  });
}

    if (this.filtrosAplicados.dataCheckOutInicio && this.filtrosAplicados.dataCheckOutFim) {
  resultado = resultado.filter(c => {
    const dataCheckoutRaw = (c as any).dataCheckout;
    if (!dataCheckoutRaw) return false;
    const dataCheckout = new Date(dataCheckoutRaw).toISOString().split('T')[0];
    return dataCheckout >= this.filtrosAplicados.dataCheckOutInicio! &&
               dataCheckout <= this.filtrosAplicados.dataCheckOutFim!;
      });
    }

    this.contasFiltradas = resultado;
  }

  temFiltrosAtivos(): boolean {
    return Object.keys(this.filtrosAplicados).length > 0 &&
           Object.values(this.filtrosAplicados).some(v => v !== undefined);
  }

  removerFiltro(tipo: string): void {
    if (tipo === 'empresaId') delete this.filtrosAplicados.empresaId;
    if (tipo === 'clienteId') {
  delete this.filtrosAplicados.clienteId;
  delete this.filtrosAplicados.clienteNome;
}
    if (tipo === 'status') delete this.filtrosAplicados.status;
    if (tipo === 'checkin') {
      delete this.filtrosAplicados.dataCheckInInicio;
      delete this.filtrosAplicados.dataCheckInFim;
    }
    if (tipo === 'checkout') {
      delete this.filtrosAplicados.dataCheckOutInicio;
      delete this.filtrosAplicados.dataCheckOutFim;
    }
    this.aplicarFiltros();
  }

  limparTodosFiltros(): void {
    this.filtrosAplicados = {};
    this.aplicarFiltros();
  }

  limparFiltrosTemp(): void {
    this.filtrosTemp = {};
  }
  
   filtrarClientesOpcoes(): void {
  if (!this.filtroClienteTexto.trim()) {
    this.clientesFiltrados = [];
    return;
  }
  const termo = this.filtroClienteTexto.toLowerCase();
  this.clientesFiltrados = this.clientes
    .filter(c => c.nome.toLowerCase().includes(termo))
    .slice(0, 10);
}


selecionarClienteFiltro(cliente: any): void {
  this.filtrosTemp.clienteNome = cliente.nome;
  this.filtroClienteTexto = cliente.nome;
  this.clientesFiltrados = [];
}


limparClienteFiltro(): void {
  this.filtrosTemp.clienteId = undefined;
  this.filtrosTemp.clienteNome = undefined;
  this.filtroClienteTexto = '';
  this.clientesFiltrados = [];
}


  // ATALHOS
  atalhoMesAtual(): void {
    const hoje = new Date();
    const primeiro = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
    const ultimo = new Date(hoje.getFullYear(), hoje.getMonth() + 1, 0);
    this.filtrosTemp.dataCheckOutInicio = primeiro.toISOString().split('T')[0];
    this.filtrosTemp.dataCheckOutFim = ultimo.toISOString().split('T')[0];
  }

  atalhoMesPassado(): void {
    const hoje = new Date();
    const primeiro = new Date(hoje.getFullYear(), hoje.getMonth() - 1, 1);
    const ultimo = new Date(hoje.getFullYear(), hoje.getMonth(), 0);
    this.filtrosTemp.dataCheckOutInicio = primeiro.toISOString().split('T')[0];
    this.filtrosTemp.dataCheckOutFim = ultimo.toISOString().split('T')[0];
  }

  atalhoVencidas(): void {
    this.filtrosTemp.status = 'VENCIDAS';
  }

  atalhoPagas(): void {
    this.filtrosTemp.status = 'PAGA';
  }

  // ========== UTILITÁRIOS ==========

  obterNomeEmpresa(id: number): string {
    return this.empresas.find(e => e.id === id)?.nomeEmpresa || '';
  }

  obterNomeCliente(id: number): string {
    return this.clientes.find(c => c.id === id)?.nome || '';
  }

  calcularTotalAReceber(): number {
    return this.contas.filter(c => c.status !== 'PAGA').reduce((sum, c) => sum + c.saldo, 0);
  }

  contarVencidas(): number {
    const hoje = new Date().toISOString().split('T')[0];
    return this.contas.filter(c =>
      c.dataVencimento < hoje &&
      (c.status === 'PENDENTE' || c.status === 'PARCIAL' || c.status === 'ATRASADA')
    ).length;
  }

  calcularTotalFiltrado(): number {
    return this.contasFiltradas.filter(c => c.status !== 'PAGA').reduce((sum, c) => sum + c.saldo, 0);
  }

  calcularTotalValor(): number {
  return this.contasFiltradas.reduce((sum, c) => sum + (c.valorPago || 0) + c.saldo, 0);
}

  calcularTotalPago(): number {
  return this.contasFiltradas.reduce((sum, c) => sum + (c.valorPago || 0), 0);
}
  calcularTotalSaldo(): number {
    return this.contasFiltradas.reduce((sum, c) => sum + c.saldo, 0);
  }

  formatarData(data: any): string {
    if (!data) return '-';
    return new Date(data).toLocaleDateString('pt-BR');
  }

  obterTextoStatus(status: string): string {
    const textos: any = {
      'PENDENTE': 'Pendente',
      'PARCIAL': 'Parcial',
      'PAGA': 'Paga',
      'ATRASADA': 'Atrasada',
      'CANCELADA': 'Cancelada',
      'EM_ABERTO': 'Em Aberto',
      'VENCIDAS': 'Vencidas'
    };
    return textos[status] || status;
  }

 imprimirRelatorio(): void {
  const c = this.contasFiltradas.find((x: any) => x.reservaId === 473);
  console.log('🖨️ Imprimindo - conta #473 pagoAVista:', (c as any)?.pagoAVista);
  window.print();
}

  atualizarVencidas(): void {
    this.contaReceberService.atualizarVencidas().subscribe({
      next: () => {
        alert('✅ Status atualizado!');
        this.carregarDados();
      },
      error: () => alert('❌ Erro ao atualizar')
    });
  }

  // ========== IMPRIMIR FATURA INDIVIDUAL ==========

 imprimirFatura(conta: ContaAReceber): void {
  const reservaId = (conta as any).reservaId;
  if (!reservaId) {
    alert('Esta conta não possui reserva vinculada.');
    return;
  }

  this.http.get<any>(`/api/reservas/${reservaId}`).subscribe({
    next: (detalhes) => {
     this.http.get<any>(`/api/reservas/${reservaId}/assinatura`).subscribe({
        next: (resp) => this.gerarHTMLFatura(conta, detalhes, resp?.assinatura || null),
        error: () => this.gerarHTMLFatura(conta, detalhes, null)
      });
    },
    error: () => this.gerarHTMLFatura(conta, null, null)
  });
}

  private gerarHTMLFatura(conta: ContaAReceber, reserva: any, assinatura: string | null): void {
    const extratos: any[] = reserva.extratos || [];
    console.log('📋 Primeiro extrato:', JSON.stringify(extratos[0]));

    console.log('📋 Extratos:', extratos.map(e => ({ 
  descricao: e.descricao, 
  statusLancamento: e.statusLancamento,
  status: e.status,
  tipo: e.tipo
})));

    const totalDiaria = (conta as any).totalDiaria || 0;
    const totalConsumo = (conta as any).totalConsumo || 0;
    const desconto = (conta as any).desconto || 0;
    const totalHospedagem = (conta as any).totalHospedagem || 0;
    const totalRecebido = (conta as any).totalRecebido || 0;
    const pagoAVista = (conta as any).pagoAVista || conta.pagoAVista || 0;
    
    const saldo = conta.saldo || 0;

   const extratosSemPagamento = extratos.filter(e => 
  e.statusLancamento !== 'PAGAMENTO' && e.statusLancamento !== 'ADIANTAMENTO'
);

const linhasExtrato = extratosSemPagamento.length > 0 ? `
  <div class="separador">- - - - - - - - - - - - - - - -</div>
  <div class="secao">
    <h3>CONSUMO DETALHADO</h3>
    ${extratosSemPagamento.map(e => `
      <div class="linha-valor">
        <span>${e.descricao} (${e.quantidade || 1}x)</span>
        <span>R$ ${this.fmt(e.totalLancamento)}</span>
      </div>
    `).join('')}
  </div>
    ` : '';

    const htmlImpressao = `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
        <title>Fatura - Reserva #${reserva.id}</title>
        <style>
           * { font-weight: bold !important; }
           @page { size: 80mm auto; margin: 0; }
           body { font-family: 'Courier New', monospace; font-size: 10px; width: 80mm; margin: 0; padding: 5mm; }
           body { font-family: 'Courier New', monospace; font-size: 11px; width: 80mm; margin: 0; padding: 5mm; font-weight: bold; }
          .cabecalho { text-align: left; margin-bottom: 8px; }
          .cabecalho h1 { font-size: 14px; margin: 0; letter-spacing: 2px; }
          .cnpj, .endereco { font-size: 9px; margin: 2px 0; }
          .separador { text-align: center; margin: 6px 0; }
          .titulo-documento h2 { font-size: 11px; margin: 0; }
          .numero-reserva { font-size: 10px; font-weight: bold; margin: 4px 0; }
          .data-emissao { font-size: 8px; margin: 2px 0; }
          .secao { margin: 8px 0; }
          .secao h3 { font-size: 10px; margin: 0 0 6px 0; text-decoration: underline; }
          .secao p { margin: 3px 0; font-size: 9px; }
          .linha-valor { display: flex; justify-content: space-between; margin: 4px 0; font-size: 9px; }
          .linha-valor.subtotal { font-weight: bold; margin-top: 6px; }
          .linha-valor.total { font-size: 11px; font-weight: bold; margin: 6px 0; }
          .destaque-apagar { background: #000; color: #fff; padding: 6px; text-align: center; margin: 8px 0; }
          .declaracao { margin: 12px 0; font-size: 9px; }
          .declaracao p { margin: 2px 0; }
          .assinatura { margin-top: 15px; text-align: center; }
          .linha-assinatura { border-top: 1px solid #000; margin: 12px 15px 4px 15px; }
          .label-assinatura { font-size: 8px; margin: 2px 0; }
        </style>
      </head>
      <body>
        <div class="cabecalho">
          <h1>HOTEL DI VAN</h1>
          <p class="cnpj">CNPJ: 07.757.726/0001-12</p>
          <p class="endereco">Arapiraca - AL</p>
          <div class="separador">================================</div>
        </div>

        <div class="titulo-documento">
          <h2>${conta.status === 'PAGA' ? 'RECIBO DE PAGAMENTO' : 'FATURA - PAGAMENTO FATURADO'}</h2>
          <p class="numero-reserva">Reserva Nº ${reserva.id}</p>
          <p class="data-emissao">Emitida em: ${new Date().toLocaleString('pt-BR')}</p>
          <p class="data-emissao">Status: ${this.obterTextoStatus(conta.status)}</p>
        </div>

        <div class="separador">================================</div>

        <div class="secao">
          <h3>DADOS DO HÓSPEDE</h3>
          <p><strong>Nome:</strong> ${conta.clienteNome}</p>
          ${conta.empresaNome ? `<p><strong>Empresa:</strong> ${conta.empresaNome}</p>` : ''}
        </div>

        <div class="separador">- - - - - - - - - - - - - - - -</div>

        <div class="secao">
          <h3>PERÍODO DA HOSPEDAGEM</h3>
          <p><strong>Apartamento:</strong> ${(conta as any).numeroApartamento || '-'}</p>
          <p><strong>Check-in:</strong> ${this.formatarData(reserva.dataCheckin)}</p>
          <p><strong>Check-out:</strong> ${this.formatarData(reserva.dataCheckout)}</p>
          <p><strong>Diárias:</strong> ${(conta as any).quantidadeDiaria || '-'}</p>
          <p><strong>Hóspedes:</strong> ${(conta as any).quantidadeHospede || '-'}</p>
        </div>

        <div class="separador">================================</div>

        <div class="secao">
          <h3>DISCRIMINAÇÃO DE VALORES</h3>
          <div class="linha-valor">
            <span>Diárias (${(conta as any).quantidadeDiaria || 0}x):</span>
            <span>R$ ${this.fmt(totalDiaria)}</span>
          </div>
          ${totalConsumo > 0 ? `
          <div class="linha-valor">
            <span>Consumo:</span>
            <span>R$ ${this.fmt(totalConsumo)}</span>
          </div>` : ''}
          ${desconto > 0 ? `
          <div class="linha-valor">
            <span>Desconto:</span>
            <span>- R$ ${this.fmt(desconto)}</span>
          </div>` : ''}
          <div class="separador">- - - - - - - - - - - - - - - -</div>
          <div class="linha-valor subtotal">
            <span>Total Hospedagem:</span>
            <span>R$ ${this.fmt(totalHospedagem)}</span>
          </div>


          ${pagoAVista > 0 ? `
          <div class="linha-valor">
          <span>Pago à Vista:</span>
          <span>R$ ${this.fmt(pagoAVista)}</span>
          </div>` : ''}


          <div class="separador">================================</div>
          <div class="linha-valor total">
            <span>SALDO A PAGAR:</span>
            <span>R$ ${this.fmt(saldo)}</span>
          </div>
        </div>

        ${linhasExtrato}

        <div class="destaque-apagar">
          <p style="margin:0;font-size:10px;font-weight:bold;">
            SALDO A PAGAR: R$ ${this.fmt(saldo)}
          </p>
        </div>

        <div class="separador">================================</div>

        ${conta.status === 'PAGA' ? `
  <div class="declaracao">
    <p>Recebemos do(a) Sr(a). ${conta.clienteNome}</p>
    <p>a importância de</p>
    <p><strong>R$ ${this.fmt(totalHospedagem)}</strong></p>
    <p>referente à hospedagem no período citado.</p>
    <p>Pagamento em: ${new Date().toLocaleDateString('pt-BR')}</p>
  </div>
` : `
  <div class="declaracao">
    <p>O(A) Sr(a). ${conta.clienteNome}</p>
    <p>deverá pagar a importância de</p>
    <p><strong>R$ ${this.fmt(saldo)}</strong></p>
    <p>referente à hospedagem no período citado.</p>
    <p>Vencimento: ${this.formatarData(conta.dataVencimento)}</p>
  </div>
`}

        <div class="assinatura">
  ${assinatura ? `
    <img src="${assinatura}" style="max-width:200px; max-height:80px; display:block; margin:0 auto;">
  ` : `
    <div class="linha-assinatura"></div>
  `}
  <p class="label-assinatura">Assinatura do Hóspede</p>
  <div class="linha-assinatura"></div>
  <p class="label-assinatura">Hotel Di Van</p>
  <p class="label-assinatura">Data: ${new Date().toLocaleDateString('pt-BR')}</p>
</div>

        <script>
          window.onload = function() {
            window.print();
            window.onafterprint = function() { window.close(); };
          };
        </script>
      </body>
      </html>
    `;

    const janela = window.open('', '_blank', 'width=800,height=600');
    if (janela) {
      janela.document.write(htmlImpressao);
      janela.document.close();
    }
  }

  private fmt(valor: number): string {
    if (!valor) return '0,00';
    return valor.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  // ========== MODAL CRIAR ==========

  abrirModalCriar(): void {
    this.http.get<any[]>('/api/reservas').subscribe({
      next: (data) => {
        this.reservasDisponiveis = data.filter(r => r.status === 'ATIVA' && r.totalApagar > 0);
        this.modalCriar = true;
      }
    });
  }

  fecharModalCriar(): void {
    this.modalCriar = false;
    this.novaConta = {
      reservaId: 0,
      empresaId: 0,
      valor: 0,
      dataVencimento: '',
      descricao: ''
    };
  }

  criarConta(): void {
    if (!this.validarNovaConta()) return;

    const dados: any = {
      reservaId: this.novaConta.reservaId,
      valor: this.novaConta.valor,
      dataVencimento: this.novaConta.dataVencimento,
      descricao: this.novaConta.descricao
    };

    if (this.novaConta.empresaId > 0) {
      dados.empresaId = this.novaConta.empresaId;
    }

    this.contaReceberService.criar(dados).subscribe({
      next: () => {
        alert('✅ Conta criada!');
        this.fecharModalCriar();
        this.carregarDados();
      },
      error: (err) => alert('❌ Erro: ' + (err.error || err.message))
    });
  }

  validarNovaConta(): boolean {
    if (this.novaConta.reservaId === 0) { alert('Selecione uma reserva'); return false; }
    if (this.novaConta.valor <= 0) { alert('Valor inválido'); return false; }
    if (!this.novaConta.dataVencimento) { alert('Informe a data de vencimento'); return false; }
    if (!this.novaConta.descricao.trim()) { alert('Informe a descrição'); return false; }
    return true;
  }

  // ========== MODAL PAGAMENTO ==========

  abrirModalPagamento(conta: ContaAReceber): void {
    this.contaSelecionada = conta;
    this.pagamento = {
      valorPago: conta.saldo,
      dataPagamento: new Date().toISOString().split('T')[0],
      formaPagamento: '',
      observacao: ''
    };
    this.modalPagamento = true;
  }

  fecharModalPagamento(): void {
    this.modalPagamento = false;
    this.contaSelecionada = null;
  }

  registrarPagamento(): void {
    if (!this.validarPagamento()) return;

    this.contaReceberService.registrarPagamento(this.contaSelecionada!.id, this.pagamento).subscribe({
      next: () => {
        alert('✅ Pagamento registrado!');
        this.fecharModalPagamento();
        this.carregarDados();
      },
      error: (err) => alert('❌ Erro: ' + (err.error || err.message))
    });
  }

  validarPagamento(): boolean {
    if (this.pagamento.valorPago <= 0) { alert('Valor inválido'); return false; }
    if (this.pagamento.valorPago > this.contaSelecionada!.saldo) { alert('Valor maior que o saldo'); return false; }
    if (!this.pagamento.dataPagamento) { alert('Informe a data do pagamento'); return false; }
    if (!this.pagamento.formaPagamento) { alert('Selecione a forma de pagamento'); return false; }
    return true;
  }

  excluir(conta: ContaAReceber): void {
    const confirmacao = confirm(`🗑️ Excluir a conta #${conta.id}?\n\nApenas contas PAGAS podem ser excluídas.`);
    if (!confirmacao) return;

    this.contaReceberService.excluir(conta.id).subscribe({
      next: () => {
        alert('✅ Conta excluída!');
        this.carregarDados();
      },
      error: (err) => alert('❌ Erro: ' + (err.error || err.message))
    });
  }

  // ========== CABEÇALHO / RELATÓRIO ==========

  obterTituloCabecalho(): string {
    if (this.filtrosAplicados.empresaId) {
      const empresa = this.empresas.find(e => e.id === this.filtrosAplicados.empresaId);
      return empresa ? empresa.nomeEmpresa : 'Relatório de Contas a Receber';
    }
    if (this.filtrosAplicados.clienteId) {
      const cliente = this.clientes.find(c => c.id === this.filtrosAplicados.clienteId);
      return cliente ? cliente.nome : 'Relatório de Contas a Receber';
    }
    return 'Relatório de Contas a Receber';
  }

 dataHoraRelatorio: string = new Date().toLocaleString('pt-BR');

  formatarMoeda(valor?: number): string {
  if (valor === null || valor === undefined) return 'R$ 0,00';
  return 'R$ ' + valor.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

  calcularTotalGeralDiarias(): number {
    return this.contasFiltradas.reduce((sum, c) => sum + ((c as any).totalDiaria || 0), 0);
  }

  calcularTotalGeralConsumo(): number {
    return this.contasFiltradas.reduce((sum, c) => sum + ((c as any).totalConsumo || 0), 0);
  }

  calcularTotalGeralHospedagem(): number {
    return this.contasFiltradas.reduce((sum, c) => sum + ((c as any).totalHospedagem || 0), 0);
  }

 calcularTotalGeralRecebido(): number {
  return this.contasFiltradas.reduce((sum, c) => sum + ((c as any).pagoAVista || 0), 0);
}

  calcularTotalGeralDesconto(): number {
  return this.contasFiltradas.reduce((sum, c) => sum + ((c as any).desconto || 0), 0);
}

  calcularTotalGeralAPagar(): number {
    return this.contasFiltradas.reduce((sum, c) => sum + ((c as any).totalApagar || 0), 0);
  }

 temReserva(conta: ContaAReceber): boolean {
  return !!(conta as any).reservaId;
}

darBaixaEmLote(): void {
  if (!this.pagamentoLote.dataPagamento) { alert('Informe a data'); return; }
  if (!this.pagamentoLote.formaPagamento) { alert('Selecione a forma de pagamento'); return; }

  const contasEmAberto = this.contasFiltradas.filter(c => c.status !== 'PAGA');
  if (contasEmAberto.length === 0) { alert('Nenhuma conta em aberto'); return; }

  const confirmacao = confirm(`✅ Dar baixa em ${contasEmAberto.length} conta(s)?\n\nEssa ação não pode ser desfeita.`);
  if (!confirmacao) return;

  let processadas = 0;
  let erros = 0;

  contasEmAberto.forEach(conta => {
    const pag = {
      valorPago: conta.saldo,
      dataPagamento: this.pagamentoLote.dataPagamento,
      formaPagamento: this.pagamentoLote.formaPagamento,
      observacao: this.pagamentoLote.observacao
    };
    this.contaReceberService.registrarPagamento(conta.id, pag).subscribe({
      next: () => {
        processadas++;
        if (processadas + erros === contasEmAberto.length) {
          alert(`✅ ${processadas} conta(s) pagas com sucesso!${erros > 0 ? '\n❌ ' + erros + ' erro(s)' : ''}`);
          this.modalBaixaLote = false;
          this.carregarDados();
        }
      },
      error: () => {
        erros++;
        if (processadas + erros === contasEmAberto.length) {
          alert(`✅ ${processadas} conta(s) pagas!\n❌ ${erros} erro(s)`);
          this.modalBaixaLote = false;
          this.carregarDados();
        }
      }
    });
  });
}

abrirModalBaixaLote(): void {
  this.modalBaixaLote = true;
  this.cdr.detectChanges();
}

exportar(): void {
  const dados = this.contasFiltradas.map(c => ({
    'Reserva': c.reservaId ? '#' + c.reservaId : '-',
    'Hóspede': c.clienteNome,
    'Empresa': (c as any).empresaNome || '-',
    'Apartamento': (c as any).numeroApartamento || '-',
    'Check-in': this.formatarData((c as any).dataCheckin),
    'Check-out': this.formatarData((c as any).dataCheckout),
    'Hóspedes': (c as any).quantidadeHospede || '-',
    'Diárias': (c as any).quantidadeDiaria || '-',
    'Vlr Diárias': (c as any).totalDiaria || 0,
    'Consumo': (c as any).totalConsumo || 0,
    'Total Hosp.': (c as any).totalHospedagem || 0,
    'Desconto': (c as any).desconto || 0,
    'Pago à Vista': (c as any).pagoAVista || 0,
    'Faturado': c.valor || 0,
    'Status': c.status,
    'Vencimento': this.formatarData(c.dataVencimento)
  }));

  const titulo = this.filtrosAplicados.empresaId
    ? this.obterNomeEmpresa(this.filtrosAplicados.empresaId)
    : 'Contas-Receber';

  this.exportService.exportarExcel(dados, titulo, 'Contas a Receber');
}

imprimirRelatorioDetalhado(): void {
  const empresaId = this.filtrosAplicados.empresaId;
  if (!empresaId) return;
  const nomeEmpresa = this.obterNomeEmpresa(empresaId);

  // Pega apenas os reservaIds das contas já filtradas na tela
  const reservaIds = this.contasFiltradas
    .map(c => c.reservaId)
    .filter(id => id != null);

  if (reservaIds.length === 0) {
    alert('⚠️ Nenhuma conta filtrada para gerar relatório.');
    return;
  }

  this.http.get<any[]>(`/api/contas-receber/empresa/${empresaId}/relatorio-detalhado`).subscribe({
    next: (dados) => {
      // Filtra apenas as reservas que estão nas contasFiltradas
      const dadosFiltrados = dados.filter(d => reservaIds.includes(d.reservaId));
      const html = this.montarHtmlRelatorioDetalhado(dadosFiltrados, nomeEmpresa);
      const janela = window.open('', '_blank');
      if (janela) {
        janela.document.write(html);
        janela.document.close();
        janela.print();
      }
    },
    error: (err) => alert('❌ Erro ao gerar relatório: ' + err.message)
  });
}

montarHtmlRelatorioDetalhado(dados: any[], nomeEmpresa: string): string {
  const totalGeral = dados.reduce((sum, c) => sum + (c.valor || 0), 0);
  const totalPago = dados.reduce((sum, c) => sum + (c.valorPago || 0), 0);
  const totalSaldo = dados.reduce((sum, c) => sum + (c.saldo || 0), 0);

  const reservasHtml = dados.map(conta => {
    const extratosHtml = (conta.extratos || []).map((e: any) => `
      <tr>
        <td>${this.formatarDataHora(e.dataHora)}</td>
        <td><span class="badge-${e.status?.toLowerCase()}">${e.status}</span></td>
        <td>${e.descricao || '-'}</td>
        <td style="text-align:right">${e.quantidade || 1}</td>
        <td style="text-align:right">R$ ${(e.valorUnitario || 0).toFixed(2).replace('.', ',')}</td>
        <td style="text-align:right; font-weight:bold; color:${(e.total || 0) < 0 ? '#e74c3c' : '#27ae60'}">
          R$ ${(e.total || 0).toFixed(2).replace('.', ',')}
        </td>
      </tr>
    `).join('');

    return `
      <div class="reserva-bloco">
        <div class="reserva-header">
          <strong>Reserva #${conta.reservaId}</strong> — 
          Apto ${conta.numeroApartamento} | 
          ${this.formatarData(conta.dataCheckin)} → ${this.formatarData(conta.dataCheckout)} |
          ${conta.quantidadeDiaria} diária(s) | ${conta.quantidadeHospede} hóspede(s)
        </div>
        <div class="hospedes">👥 ${conta.todosHospedes || conta.clienteNome}</div>
        <table class="extrato-table">
          <thead>
            <tr>
              <th>Data</th><th>Tipo</th><th>Descrição</th>
              <th>Qtd</th><th>Valor Unit.</th><th>Total</th>
            </tr>
          </thead>
          <tbody>${extratosHtml}</tbody>
        </table>
        <div class="reserva-totais">
  <span>Diárias: <strong>R$ ${(conta.totalDiaria || 0).toFixed(2).replace('.', ',')}</strong></span>
  <span>Consumo: <strong>R$ ${(conta.totalConsumo || 0).toFixed(2).replace('.', ',')}</strong></span>
  <span>Total Hosp.: <strong>R$ ${(conta.totalHospedagem || 0).toFixed(2).replace('.', ',')}</strong></span>
  <span>Desconto: <strong style="color:#e74c3c">R$ ${(conta.desconto || 0).toFixed(2).replace('.', ',')}</strong></span>
  <span>Pago à Vista: <strong style="color:#27ae60">R$ ${(conta.pagoAVista || 0).toFixed(2).replace('.', ',')}</strong></span>
  
  <span>Faturado: <strong style="color:#8e44ad">R$ ${(conta.valor || 0).toFixed(2).replace('.', ',')}</strong></span>
  <span class="badge-status-${conta.status?.toLowerCase()}">${
  conta.status === 'EM_ABERTO' ? 'Em Aberto' :
  conta.status === 'PAGA' ? 'Paga' :
  conta.status === 'VENCIDA' ? 'Vencida' :
  conta.status === 'CANCELADA' ? 'Cancelada' : conta.status
}</span>
</div>
      </div>
    `;
  }).join('');

  return `<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Relatório Detalhado — ${nomeEmpresa}</title>
  <style>
    body { font-family: Arial, sans-serif; font-size: 11px; margin: 20px; color: #333; }
    h1 { font-size: 16px; margin: 0; }
    h2 { font-size: 13px; color: #555; margin: 2px 0; }
    .cabecalho { text-align: center; border-bottom: 2px solid #333; padding-bottom: 10px; margin-bottom: 15px; }
    .reserva-bloco { border: 1px solid #ddd; border-radius: 5px; margin-bottom: 15px; padding: 10px; page-break-inside: avoid; }
    .reserva-header { background: #f5f5f5; padding: 6px; font-size: 12px; border-radius: 3px; margin-bottom: 5px; }
    .hospedes { color: #555; margin-bottom: 8px; font-size: 11px; }
    .extrato-table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }
    .extrato-table th { background: #2c3e50; color: white; padding: 4px 6px; text-align: left; font-size: 10px; }
    .extrato-table td { padding: 3px 6px; border-bottom: 1px solid #eee; font-size: 10px; }
    .extrato-table tr:nth-child(even) { background: #f9f9f9; }
    .reserva-totais { 
  display: flex; 
  gap: 15px; 
  flex-wrap: wrap; 
  padding: 8px; 
  background: #f0f0f0; 
  border-radius: 3px; 
  font-size: 11px;
  align-items: center;
}
    .totais-gerais { margin-top: 15px; border-top: 2px solid #333; padding-top: 10px; font-size: 13px; display: flex; gap: 20px; justify-content: flex-end; }
    @media print {
      body { margin: 10mm; font-size: 10px; }
      @page { margin: 10mm; size: A4 portrait; }
    }
  </style>
</head>
<body>
  <div class="cabecalho">
    <h1>SANTOS E CORREIA LTDA — HOTEL DI VAN</h1>
    <h2>CNPJ: 07.757.726/0001-12 | Arapiraca - AL</h2>
    <h2>Relatório Detalhado — ${nomeEmpresa}</h2>
    <p>Emitido em: ${new Date().toLocaleString('pt-BR')}</p>
  </div>
  ${reservasHtml}
  <div class="totais-gerais">
    <span>Total Hospedagem: <strong>R$ ${totalGeral.toFixed(2).replace('.', ',')}</strong></span>
    <span>Total Pago: <strong style="color:#27ae60">R$ ${totalPago.toFixed(2).replace('.', ',')}</strong></span>
    <span>Saldo a Receber: <strong style="color:#e74c3c">R$ ${totalSaldo.toFixed(2).replace('.', ',')}</strong></span>
  </div>
</body>
</html>`;
}

formatarDataHora(dataHora: string): string {
  if (!dataHora) return '-';
  return new Date(dataHora).toLocaleString('pt-BR');
}

abrirModalDesconto(conta: ContaAReceber): void {
  this.contaDescontoSelecionada = conta;
  this.valorDesconto = 0;
  this.motivoDesconto = '';
  this.modalDesconto = true;
}

confirmarDesconto(): void {
  if (this.valorDesconto <= 0) { alert('Informe um valor válido'); return; }
  if (!this.motivoDesconto.trim()) { alert('Informe o motivo'); return; }
  if (this.valorDesconto > (this.contaDescontoSelecionada?.saldo || 0)) {
    alert('Desconto não pode ser maior que o saldo');
    return;
  }

  this.http.patch(`/api/contas-receber/${this.contaDescontoSelecionada!.id}/desconto`, {
    valorDesconto: this.valorDesconto,
    motivo: this.motivoDesconto
  }).subscribe({
    next: () => {
      alert('✅ Desconto aplicado com sucesso!');
      this.modalDesconto = false;
      this.carregarDados();
    },
    error: (err) => alert('❌ Erro: ' + (err.error?.erro || err.message))
  });
}

} 