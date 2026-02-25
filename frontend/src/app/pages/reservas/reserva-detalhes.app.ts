import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { ReservaService } from '../../services/reserva.service';
import { TransferirHospedeModalComponent } from '../../components/transferir-hospede-modal/transferir-hospede-modal.component';
import { CurrencyInputDirective } from '../../directives/currency-input.directive';
import { AlertasStateService } from '../../services/alertas-state.service';
import { ViewChild } from '@angular/core';
import { SignaturePadComponent } from '../../components/signature-pad/signature-pad.component';
import { HasPermissionDirective } from '../../directives/has-permission.directive';

console.log('🚗🚗🚗 ARQUIVO RESERVA-DETALHES CARREGADO! 🚗🚗🚗');
interface DescontoSimples {
  id: number;
  valor: number;
  motivo: string;
  dataHoraDesconto: string;
}

interface ReservaDetalhes {
  id: number;
  cliente?: {
    id: number;
    nome: string;
    cpf: string;
    telefone?: string;
    celular?: string;
    dataNascimento?: string;
    creditoAprovado?: boolean; 
  };
  apartamento?: {
    id: number;
    numeroApartamento: string;
    capacidade: number;
    tipoApartamentoNome?: string;
  };

   hospedes?: Array<{
    id: number;
    clienteId?: number;
    nomeCompleto: string;
    cpf?: string;
    telefone?: string;
    titular: boolean;
    status: string;
  }>;
  quantidadeHospede: number;
  dataCheckin: string;
  dataCheckout: string;
  quantidadeDiaria: number;
  valorDiaria: number;
  totalDiaria: number;
  totalHospedagem: number;
  totalRecebido: number;
  totalApagar: number;
  totalProduto?: number;
  desconto?: number;
  descontos?: DescontoSimples[];
  totalConsumo?: number;
  status: string;
  extratos?: any[];
  historicos?: any[];
  observacoes?: string;
  criadoPor?: string;
  dataCriacao?: string;
  finalizadoPor?: string;
  dataFinalizacao?: string;
  canceladoPor?: string;
  dataCancelamento?: string;
  motivoCancelamento?: string;    
}

interface Produto {
  id: number;
  nomeProduto: string;
  valorVenda: number;
  quantidade: number;
}

interface Apartamento {
  id: number;
  numeroApartamento: string;
  capacidade: number;
  tipoApartamento?: {
    tipo: string;
  };
  tipoApartamentoNome?: string;

  
}

@Component({
  selector: 'app-reserva-detalhes',
  standalone: true,
  imports: [CommonModule, FormsModule, TransferirHospedeModalComponent, 
    CurrencyInputDirective, SignaturePadComponent, HasPermissionDirective ],
  
  

 template: `
  <div class="container-detalhes">
    <!-- LOADING -->
    <div *ngIf="loading" class="loading">
      <div class="spinner"></div>
      <p>Carregando reserva...</p>
    </div>

    <!-- ERRO -->
    <div *ngIf="erro && !loading" class="erro">
      <h2>❌ Erro</h2>
      <p>{{ erro }}</p>
      <button (click)="voltar()">← Voltar</button>
    </div>

    <!-- DETALHES -->
    <div *ngIf="reserva && !loading" class="detalhes">
      <!-- HEADER -->
      <div class="header">
        <div>
          <h1>📋 Reserva #{{ reserva.id }}</h1>
          <span [class]="'badge-status ' + obterStatusClass()">
            {{ reserva.status }}
          </span>
        </div>
        <button class="btn-voltar" (click)="voltar()">
          ← Voltar
        </button>
      </div>

    <!-- ✅ GRID DE 4 CARDS NA HORIZONTAL -->
      <div class="grid-quatro-colunas">
        <!-- CLIENTE -->
        <div class="card card-mini">
          <h2>👤 Cliente</h2>
          <div class="info-item-mini">
            <span class="label">Nome:</span>
            <span class="value">{{ reserva.cliente?.nome || 'N/A' }}</span>
          </div>
          <div class="info-item-mini">
            <span class="label">CPF:</span>
            <span class="value">{{ reserva.cliente?.cpf || 'N/A' }}</span>
          </div>
          <div class="info-item-mini">
            <span class="label">Tel:</span>
            <span class="value">{{ reserva.cliente?.celular || reserva.cliente?.telefone || 'N/A' }}</span>
          </div>
        </div>

        <!-- APARTAMENTO -->
        <div class="card card-mini">
          <h2>🏨 Apartamento</h2>
          <div class="info-item-mini">
            <span class="label">Número:</span>
            <span class="value numero-apt">
              {{ reserva.apartamento?.numeroApartamento || 'N/A' }}
            </span>
          </div>
          <div class="info-item-mini">
            <span class="label">Tipo:</span>
            <span class="value">{{ reserva.apartamento?.tipoApartamentoNome || 'N/A' }}</span>
          </div>
          <div class="info-item-mini">
            <span class="label">Cap:</span>
            <span class="value">{{ reserva.apartamento?.capacidade || 0 }} pessoas</span>
          </div>
        </div>

        <!-- HOSPEDAGEM -->
        <div class="card card-mini">
          <h2>📅 Hospedagem</h2>
          <div class="info-item-mini">
            <span class="label">Check-in:</span>
            <span class="value-pequeno">{{ formatarData(reserva.dataCheckin) }}</span>
          </div>
          <div class="info-item-com-botao-mini">
            <div class="info-item-mini">
              <span class="label">Check-out:</span>
              <span class="value-pequeno">{{ formatarData(reserva.dataCheckout) }}</span>
            </div>
            <button 
              *ngIf="reserva.status === 'ATIVA'"
              class="btn-mini"
              (click)="abrirModalAlterarCheckout()"
              title="Alterar checkout">
              ✏️
            </button>
          </div>
          <div class="info-item-mini">
            <span class="label">Hóspedes:</span>
            <span class="value">{{ reserva.quantidadeHospede }}</span>
          </div>
          <div class="info-item-mini">
            <span class="label">Diárias:</span>
            <span class="value">{{ reserva.quantidadeDiaria }}</span>
          </div>
        </div>

       <!-- FINANCEIRO -->
        <div class="card card-mini">
          <h2>💰 Financeiro</h2>
          
          <div class="info-item-mini">
            <span class="label">Diárias:</span>
            <span class="value-pequeno">R$ {{ formatarMoeda(reserva.totalDiaria) }}</span>
          </div>
          
          <div class="info-item-mini">
            <span class="label">Consumo:</span>
            <span class="value-pequeno">R$ {{ formatarMoeda(reserva.totalProduto || 0) }}</span>
          </div>
          
          <!-- ✅ TOTAL (Diárias + Consumo) -->
          <div class="info-item-mini separador-mini">
            <span class="label">Total:</span>
            <span class="value-pequeno">R$ {{ formatarMoeda((reserva.totalDiaria || 0) + (reserva.totalProduto || 0)) }}</span>
          </div>
          
          <!-- ✅ DESCONTO -->
          <div class="info-item-com-botao-mini">
            <div class="info-item-mini">
              <span class="label">Desconto:</span>
              <span class="value-pequeno valor-positivo">-R$ {{ formatarMoeda(reserva.desconto || 0) }}</span>
            </div>
            <button 
              *ngIf="reserva.status === 'ATIVA'"
              class="btn-mini btn-desconto"
              (click)="abrirModalDesconto()"
              title="Adicionar desconto">
              ➕
            </button>
          </div>
          
          <!-- ✅ JÁ RECEBIDO -->
          <div class="info-item-mini">
            <span class="label">Recebido:</span>
            <span class="value-pequeno valor-positivo">-R$ {{ formatarMoeda(reserva.totalRecebido || 0) }}</span>
          </div>
          
          <!-- ✅ SALDO FINAL - AGORA VINDO DO BACKEND -->
          <div class="info-item-mini destaque-mini">
            <span class="label">SALDO:</span>
            <span class="value-destaque" [class.valor-negativo]="(reserva.totalApagar || 0) > 0">
              R$ {{ formatarMoeda(reserva.totalApagar) }}
            </span>
          </div>
        </div>
      </div>

      <!-- ========================================== -->
<!-- CARD DE AUDITORIA -->
<!-- ========================================== -->
<div class="card card-auditoria" *ngIf="temAuditoria()">
  <h2>📝 Informações de Auditoria</h2>
  
  <div class="auditoria-grid">
    <!-- CRIADO POR -->
    <div class="auditoria-item" *ngIf="reserva.criadoPor">
      <span class="auditoria-icone">✏️</span>
      <div class="auditoria-info">
        <span class="auditoria-label">Criado por:</span>
        <span class="auditoria-valor">{{ reserva.criadoPor }}</span>
        <span class="auditoria-data" *ngIf="reserva.dataCriacao">
          {{ formatarDataHora(reserva.dataCriacao) }}
        </span>
      </div>
    </div>

    <!-- FINALIZADO POR -->
    <div class="auditoria-item" *ngIf="reserva.finalizadoPor">
      <span class="auditoria-icone">✅</span>
      <div class="auditoria-info">
        <span class="auditoria-label">Finalizado por:</span>
        <span class="auditoria-valor">{{ reserva.finalizadoPor }}</span>
        <span class="auditoria-data" *ngIf="reserva.dataFinalizacao">
          {{ formatarDataHora(reserva.dataFinalizacao) }}
        </span>
      </div>
    </div>

    <!-- CANCELADO POR -->
    <div class="auditoria-item" *ngIf="reserva.canceladoPor">
      <span class="auditoria-icone">❌</span>
      <div class="auditoria-info">
        <span class="auditoria-label">Cancelado por:</span>
        <span class="auditoria-valor">{{ reserva.canceladoPor }}</span>
        <span class="auditoria-data" *ngIf="reserva.dataCancelamento">
          {{ formatarDataHora(reserva.dataCancelamento) }}
        </span>
        <span class="auditoria-motivo" *ngIf="reserva.motivoCancelamento">
          <strong>Motivo:</strong> {{ reserva.motivoCancelamento }}
        </span>
      </div>
    </div>
  </div>
</div>
 

      <!-- GRID NORMAL (EXTRATO) -->
      <div class="grid">
        <!-- EXTRATO -->
        <div class="card extrato-card" *ngIf="reserva.extratos && reserva.extratos.length > 0">
          <h2>📊 Extrato</h2>
          <table class="tabela-extrato">
            <thead>
              <tr>
                <th>Data</th>
                <th>Descrição</th>
                <th>Qtd</th>
                <th>Valor Unit.</th>
                <th>Total</th>
                <th *ngIf="reserva.status === 'ATIVA'">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let extrato of reserva.extratos">
                <td>{{ formatarDataHora(extrato.dataHoraLancamento) }}</td>
                <td>
                  <span [class]="'badge-extrato badge-' + extrato.statusLancamento.toLowerCase()">
                    {{ extrato.statusLancamento }}
                  </span>
                  {{ extrato.descricao }}
                </td>
                <td>{{ extrato.quantidade }}</td>
                <td>R$ {{ formatarMoeda(extrato.valorUnitario) }}</td>
                <td [class.valor-negativo]="extrato.totalLancamento < 0"
                    [class.valor-positivo]="extrato.totalLancamento > 0">
                  R$ {{ formatarMoeda(extrato.totalLancamento) }}
                </td>
                
                <td *ngIf="reserva.status === 'ATIVA'">
                  <!-- ✅ BOTÃO ESTORNAR PRODUTO -->
                  <button 
                    *ngIf="extrato.statusLancamento === 'PRODUTO' && extrato.totalLancamento > 0"
                   [disabled]="jaFoiEstornado(extrato)"
                   [title]="jaFoiEstornado(extrato) ? 'Este produto já foi estornado' : 'Estornar este produto'"
                   [class.btn-estornado]="jaFoiEstornado(extrato)"
                    class="btn-estornar"
                    (click)="abrirModalEstorno(extrato)"
                    title="Estornar este produto">
                    ❌ Estornar
                  </button>
                  
                 <!-- 🔍 DEBUG: MOSTRAR TODAS AS CONDIÇÕES -->


<!-- ✅ BOTÃO REAL -->
<button 
  *ngIf="extrato.statusLancamento === 'ESTORNO' && 
         extrato.descricao?.startsWith('Desconto aplicado') &&
         !extrato.estornado"
  class="btn-estornar btn-estornar-desconto"
  (click)="confirmarEstornoDescontoDoExtrato(extrato)"
  title="Estornar este desconto">
  ❌ Estornar
</button>
                  
                  <!-- ✅ INDICADOR DE JÁ ESTORNADO -->
                  <span *ngIf="extrato.estornado" class="badge-estornado">
                    ✅ Estornado
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- HÓSPEDES -->
      <div class="secao-hospedes" *ngIf="reserva && hospedes && hospedes.length > 0">
        <div class="secao-header">
          <h3>👥 Hóspedes da Reserva</h3>
          <span class="badge-hospedes">{{ hospedes.length }}</span>
          
          <button 
            *ngIf="reserva.status === 'ATIVA'"
            type="button"
            class="btn-adicionar-hospede"
            (click)="abrirModalAdicionarHospede()">
            ➕ Adicionar Hóspede
          </button>
        </div>

        <div class="lista-hospedes">
          <div class="hospede-item" 
               *ngFor="let hospede of hospedes; let i = index"
               [class.hospede-checkout]="hospede.status === 'CHECKOUT_REALIZADO'">
            
            <div class="hospede-numero" 
                 [class.checkout]="hospede.status === 'CHECKOUT_REALIZADO'">
              {{ i + 1 }}
            </div>
            
            <div class="hospede-info">
              <div class="hospede-nome">
                {{ hospede.cliente?.nome || hospede.nomeCompleto }}
                
                <span class="badge-titular" 
                      *ngIf="hospede.titular && hospede.status !== 'CHECKOUT_REALIZADO'">
                  ★ TITULAR
                </span>
                
                <span class="badge-checkout" 
                      *ngIf="hospede.status === 'CHECKOUT_REALIZADO'">
                  🚪 SAIU
                </span>
              </div>
              
              <div class="hospede-empresa" *ngIf="hospede.cliente?.empresa?.nomeEmpresa">
                🏢 {{ hospede.cliente.empresa.nomeEmpresa }}
              </div>
              
              <div class="hospede-detalhes">
  CPF: {{ hospede.cliente?.cpf || hospede.cpf || 'Não informado' }} | 
  Tel: {{ hospede.cliente?.celular || hospede.telefone || 'Não informado' }}
  
  <!-- ✅ PLACA COM BOTÃO EDITAR -->
  <span class="hospede-placa-container">
    <span *ngIf="hospede.placaCarro" class="hospede-placa">
      | 🚗 {{ hospede.placaCarro }}
    </span>
    <span *ngIf="!hospede.placaCarro" class="hospede-placa sem-placa">
      | ⚠️ Sem placa
    </span>
    
    <!-- BOTÃO EDITAR/ADICIONAR PLACA -->
    <button 
      *ngIf="reserva.status === 'ATIVA' && hospede.status !== 'CHECKOUT_REALIZADO'"
      class="btn-mini-placa"
      (click)="editarPlacaHospede(hospede)"
      [title]="hospede.placaCarro ? 'Editar placa' : 'Adicionar placa'">
      {{ hospede.placaCarro ? '✏️' : '➕' }}
    </button>
  </span>
</div>
              
              <div class="hospede-status" *ngIf="hospede.status === 'CHECKOUT_REALIZADO'">
                <small>
                  Checkout em: {{ hospede.dataHoraSaida ? formatarDataHora(hospede.dataHoraSaida) : 'Não registrado' }}
                </small>
              </div>
            </div>
            
            <div class="hospede-acoes" 
                 *ngIf="reserva.status === 'ATIVA' && hospede.status !== 'CHECKOUT_REALIZADO'">
              <button 
                type="button"
                class="btn-acao-hospede btn-transferir-hospede"
                (click)="abrirModalTransferirHospede(hospede)"
                title="Transferir para outro apartamento">
                🔄
              </button>
              
              <button 
                type="button"
                class="btn-acao-hospede btn-checkout-hospede"
                (click)="confirmarRemocaoHospede(hospede)"
                [title]="hospede.titular ? 'Fazer checkout do titular (próximo será promovido)' : 'Fazer checkout do hóspede'">
                🚪
              </button>
            </div>
            
            <span class="hospede-finalizado" 
                  *ngIf="hospede.status === 'CHECKOUT_REALIZADO'">
              ✅
            </span>
          </div>
        </div>
      </div>

             
   <!-- ========================================== -->
<!-- CARD DE AÇÕES -->
<!-- ========================================== -->
<div class="card acoes-card">
  <h2>⚙️ Ações</h2>
  <div class="botoes-acoes">
    
    <!-- ✅ ATIVAR PRÉ-RESERVA (CHECK-IN MANUAL) -->
   
   <!-- ✅ CORRETO - Permissão no ng-container, *ngIf no botão -->
<ng-container *hasPermission="'RESERVA_EDITAR'">
  <button class="btn-acao btn-ativar-reserva" 
          *ngIf="reserva.status === 'PRE_RESERVA' && podeAtivarPreReserva()"
          (click)="ativarPreReserva()">
    🔓 Fazer Check-in (Ativar Reserva)
  </button>
</ng-container>


    <!-- IMPRIMIR CHECK-IN -->
    <ng-container *hasPermission="'RESERVA_VISUALIZAR'">
  <button class="btn-acao btn-checkin" 
          *ngIf="reserva.status === 'ATIVA'"
          (click)="imprimirCheckin()">
    🖨️ Imprimir Check-in
  </button>
</ng-container>

   <ng-container *hasPermission="'RESERVA_VISUALIZAR'">
  <button class="btn-acao btn-extrato" 
          *ngIf="reserva.status === 'ATIVA'"
          (click)="imprimirExtrato()">
    📊 Imprimir Extrato
  </button>
</ng-container>

    <!-- COMANDA DE CONSUMO -->
    <ng-container *hasPermission="'RESERVA_CRIAR'">
  <button class="btn-acao btn-comanda" 
          *ngIf="reserva.status === 'ATIVA'"
          (click)="abrirComanda()">
    🏨 Comanda de Consumo
  </button>
</ng-container>

    <!-- REGISTRAR PAGAMENTO -->
    <ng-container *hasPermission="'CONTA_RECEBER_PAGAMENTO'">
  <button class="btn-acao btn-pagamento" 
          *ngIf="reserva.status === 'ATIVA' && (reserva.totalApagar || 0) > 0"
          (click)="abrirModalPagamento()">
    💳 Registrar Pagamento
  </button>
</ng-container>


    <!-- TRANSFERIR APARTAMENTO -->
    <ng-container *hasPermission="'RESERVA_EDITAR'">
  <button class="btn-acao btn-transferir" 
          *ngIf="reserva.status === 'ATIVA'"
          (click)="abrirModalTransferencia()">
    🔄 Transferir Apartamento
  </button>
</ng-container>

    <!-- FINALIZAR RESERVA -->
    <ng-container *hasPermission="'RESERVA_FINALIZAR'">
  <button class="btn-acao" 
          *ngIf="reserva.status === 'ATIVA'"
          [ngClass]="{
            'btn-finalizar-paga': (reserva.totalApagar || 0) === 0,
            'btn-finalizar': (reserva.totalApagar || 0) > 0
          }"
          (click)="finalizarCheckout()">
    {{ (reserva.totalApagar || 0) === 0 ? '💚 Finalizar Paga' : '✅ Finalizar Faturada' }}
  </button>
</ng-container>

    <!-- IMPRIMIR RECIBO/FATURA -->
    <ng-container *hasPermission="'RESERVA_VISUALIZAR'">
  <button class="btn-acao btn-recibo" 
          *ngIf="reserva.status === 'FINALIZADA'"
          (click)="imprimirRecibo()">
    📄 {{ (reserva.totalApagar || 0) > 0 ? 'Imprimir Fatura' : 'Imprimir Recibo' }}
  </button>
</ng-container>

    <!-- CANCELAR RESERVA -->
    <ng-container *hasPermission="'RESERVA_CANCELAR'">
  <button class="btn-acao btn-cancelar" 
          *ngIf="reserva.status === 'ATIVA'"
          (click)="cancelarReserva()">
    ❌ Cancelar Reserva
  </button>
</ng-container>
  </div>
</div>

      <!-- HISTÓRICO -->
      <div class="card historico-card" *ngIf="reserva.historicos && reserva.historicos.length > 0">
        <h2>📜 Histórico</h2>
        <div class="timeline">
          <div class="timeline-item" *ngFor="let hist of reserva.historicos">
            <div class="timeline-marker"></div>
            <div class="timeline-content">
              <div class="timeline-header">
                <span class="timeline-date">{{ formatarDataHora(hist.dataHora) }}</span>
              </div>
              <div class="timeline-body">
                <p>{{ hist.motivo }}</p>
                <small *ngIf="hist.quantidadeAnterior !== hist.quantidadeNova">
                  Hóspedes: {{ hist.quantidadeAnterior }} → {{ hist.quantidadeNova }}
                </small>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ========================================== -->
<!-- MODAL ADICIONAR HÓSPEDE -->
<!-- ========================================== -->
<div class="modal-overlay" *ngIf="modalAdicionarHospede" (click)="fecharModalAdicionarHospede()">
  <div class="modal-content" (click)="$event.stopPropagation()">
    <h2>👤 Adicionar Hóspede</h2>
    
    <div class="modal-tabs">
      <button 
        [class.active]="modoModalHospede === 'buscar'"
        (click)="alternarModoModal('buscar')">
        🔍 Buscar Existente
      </button>
      <button 
        [class.active]="modoModalHospede === 'cadastrar'"
        (click)="alternarModoModal('cadastrar')">
        ➕ Cadastrar Novo
      </button>
    </div>

    <!-- ABA BUSCAR EXISTENTE -->
    <div *ngIf="modoModalHospede === 'buscar'" class="modal-tab-content">
      <input 
        type="text"
        [(ngModel)]="termoBuscaHospede"
        (input)="buscarClientesModal()"
        placeholder="Digite nome ou CPF (mínimo 2 caracteres)"
        class="input-busca-modal">
      
      <div class="resultados-modal" *ngIf="clientesFiltradosModal.length > 0">
        <div 
          class="resultado-modal-item"
          *ngFor="let cliente of clientesFiltradosModal"
          (click)="selecionarHospedeExistente(cliente)">
          <div class="resultado-nome">{{ cliente.nome }}</div>
          <div class="resultado-info">
            CPF: {{ formatarCPF(cliente.cpf) }} | 
            Tel: {{ cliente.celular || 'Não informado' }}
          </div>
        </div>
      </div>

      <div *ngIf="termoBuscaHospede.length >= 2 && clientesFiltradosModal.length === 0" class="sem-resultado-modal">
        ❌ Nenhum cliente encontrado
      </div>
    </div>

    <!-- ABA CADASTRAR NOVO -->
    <div *ngIf="modoModalHospede === 'cadastrar'" class="modal-tab-content">
      <div class="form-group">
        <label>Nome Completo *</label>
        <input 
          type="text"
          [(ngModel)]="novoHospede.nome"
          placeholder="Nome completo do hóspede">
      </div>

      <div class="form-group">
        <label>CPF <small>(opcional)</small></label>
        <input 
          type="text"
          [(ngModel)]="novoHospede.cpf"
          placeholder="000.000.000-00">
      </div>

            
      <!-- ✅ CAMPO PLACA DO CARRO (NOVO) -->

        <div class="form-group">
  <label>Celular</label>
  <input 
    type="text"
    [(ngModel)]="novoHospede.celular"
    placeholder="(00) 00000-0000">
</div>

<!-- ✅ TESTE: ISSO AQUI DEVE APARECER -->
<div style="background: red; color: white; padding: 10px;">
  🚨 SE VOCÊ VÊ ISSO, O HTML ESTÁ CARREGANDO!
</div>



      <!-- ✅ CAMPO PLACA DO CARRO (NOVO) -->
      <div class="form-group">
        <label>
          🚗 Placa do Carro 
          <small>(opcional)</small>
        </label>
        <input 
          type="text"
          [(ngModel)]="novoHospede.placaCarro"
          (input)="formatarPlaca()"
          placeholder="ABC-1234"
          maxlength="8"
          style="text-transform: uppercase;"
          class="input-placa">
        <small class="form-text-hint">
          Formato: ABC-1234 ou ABC-1D23 (Mercosul)
        </small>
      </div>

      <div class="info-cadastro">
        ℹ️ Somente o nome é obrigatório. CPF é opcional para menores de idade.
      </div>

      <button 
        type="button"
        class="btn-salvar-hospede"
        (click)="salvarNovoHospede()">
        ✅ Adicionar Hóspede
      </button>
    </div>

    <button type="button" class="btn-fechar-modal" (click)="fecharModalAdicionarHospede()">
      ✕
    </button>
  </div>
</div>
      <!-- MODAL ALTERAR CHECKOUT -->
      <div class="modal-overlay" *ngIf="modalAlterarCheckout" (click)="fecharModalAlterarCheckout()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>📅 Alterar Data de Check-out</h2>
          
          <div class="info-box">
            <p><strong>Check-in:</strong> {{ formatarData(reserva.dataCheckin) }}</p>
            <p><strong>Check-out atual:</strong> {{ formatarData(reserva.dataCheckout) }}</p>
          </div>

          <div class="campo">
            <label>Nova Data de Check-out *</label>
            <input type="date" [(ngModel)]="novaDataCheckout" [min]="obterDataMinimaCheckout()">
          </div>

          <div class="campo">
            <label>Motivo</label>
            <textarea [(ngModel)]="motivoAlteracaoCheckout" rows="3"
                      placeholder="Informe o motivo da alteração..."></textarea>
          </div>

          <div class="modal-footer">
            <button class="btn-cancelar-modal" (click)="fecharModalAlterarCheckout()">
              Cancelar
            </button>
            <button class="btn-confirmar" (click)="confirmarAlteracaoCheckout()">
              Confirmar Alteração
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL ALTERAR HÓSPEDES -->
      <div class="modal-overlay" *ngIf="modalAlterarHospedes" (click)="fecharModalAlterarHospedes()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>👥 Alterar Quantidade de Hóspedes</h2>
          
          <div class="info-box">
            <p><strong>Quantidade atual:</strong> {{ reserva.quantidadeHospede }} hóspede(s)</p>
            <p><strong>Capacidade do apartamento:</strong> {{ reserva.apartamento?.capacidade }} pessoa(s)</p>
          </div>

          <div class="campo">
            <label>Nova Quantidade *</label>
            <input type="number" [(ngModel)]="novaQuantidadeHospedes" 
                   min="1" [max]="reserva.apartamento?.capacidade || 10">
          </div>

          <div class="campo">
            <label>Motivo</label>
            <textarea [(ngModel)]="motivoAlteracaoHospedes" rows="3"
                      placeholder="Informe o motivo da alteração..."></textarea>
          </div>

          <div class="modal-footer">
            <button class="btn-cancelar-modal" (click)="fecharModalAlterarHospedes()">
              Cancelar
            </button>
            <button class="btn-confirmar" (click)="confirmarAlteracaoHospedes()">
              Confirmar Alteração
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL PAGAMENTO -->
      <div class="modal-overlay" *ngIf="modalPagamento" (click)="fecharModalPagamento()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>💳 Registrar Pagamento</h2>
          
          <div class="campo">
            <label>Valor a Pagar *</label>
            <input 
              type="text" 
              [(ngModel)]="pagValor" 
              appCurrencyInput
              placeholder="R$ 0,00">
            <small>Saldo devedor: R$ {{ formatarMoeda(reserva.totalApagar) }}</small>
          </div>

          <div class="campo">
            <label>Forma de Pagamento *</label>
            <select [(ngModel)]="pagFormaPagamento">
              <option value="">Selecione...</option>
              <option *ngFor="let forma of formasPagamento" [value]="forma.codigo">
                {{ forma.nome }}
              </option>
            </select>
          </div>

          <div class="campo">
            <label>Observação</label>
            <textarea [(ngModel)]="pagObs" rows="3"></textarea>
          </div>

          <div class="modal-footer">
            <button class="btn-cancelar-modal" (click)="fecharModalPagamento()">
              Cancelar
            </button>
            <button class="btn-confirmar" (click)="salvarPagamento()">
              Confirmar Pagamento
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL CONSUMO -->
      <div class="modal-overlay" *ngIf="modalConsumo" (click)="fecharModalConsumo()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>🛒 Adicionar Consumo</h2>
          
          <div class="campo">
            <label>Produto *</label>
            <select [(ngModel)]="produtoSelecionadoId">
              <option value="0">Selecione um produto...</option>
              <option *ngFor="let produto of produtos" [value]="produto.id">
                {{ produto.nomeProduto }} - R$ {{ formatarMoeda(produto.valorVenda) }} 
                (Estoque: {{ produto.quantidade }})
              </option>
            </select>
          </div>

          <div class="campo">
            <label>Quantidade *</label>
            <input type="number" [(ngModel)]="quantidadeConsumo" min="1">
          </div>

          <div class="campo">
            <label>Observação</label>
            <textarea [(ngModel)]="observacaoConsumo" rows="3"></textarea>
          </div>

          <div class="modal-footer">
            <button class="btn-cancelar-modal" (click)="fecharModalConsumo()">
              Cancelar
            </button>
            <button class="btn-confirmar" (click)="salvarConsumo()">
              Adicionar ao Consumo
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL TRANSFERÊNCIA -->
      <div class="modal-overlay" *ngIf="modalTransferencia" (click)="fecharModalTransferencia()">
        <div class="modal-content modal-grande" (click)="$event.stopPropagation()">
          <h2>🔄 Transferir Apartamento</h2>
          
          <div class="campo">
            <label>Novo Apartamento *</label>
            <select [(ngModel)]="novoApartamentoId">
              <option value="0">Selecione um apartamento...</option>
              <option *ngFor="let apt of apartamentosDisponiveis" [value]="apt.id">
                {{ apt.numeroApartamento }} - 
                {{ obterNomeTipoApartamento(apt) }} 
                (Cap: {{ apt.capacidade }})
              </option>
            </select>
          </div>

          <div class="campo">
            <label>
              <input type="checkbox" [(ngModel)]="transferenciaImediata">
              Transferência Imediata
            </label>
            <small>Se desmarcado, informe a data da transferência</small>
          </div>

          <div class="campo" *ngIf="!transferenciaImediata">
            <label>Data da Transferência *</label>
            <input type="date" [(ngModel)]="dataTransferencia" [min]="obterDataMinima()">
          </div>

          <div class="campo">
            <label>Motivo *</label>
            <textarea [(ngModel)]="motivoTransferencia" rows="3" 
                      placeholder="Informe o motivo da transferência..."></textarea>
          </div>

          <div class="modal-footer">
            <button class="btn-cancelar-modal" (click)="fecharModalTransferencia()">
              Cancelar
            </button>
            <button class="btn-confirmar" (click)="confirmarTransferencia()">
              Confirmar Transferência
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL ESTORNO -->
      <div class="modal-overlay" *ngIf="modalEstorno" (click)="fecharModalEstorno()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>❌ Estornar Lançamento</h2>
          
          <div class="info-box info-box-alerta">
            <p><strong>⚠️ ATENÇÃO:</strong></p>
            <p>Esta ação criará um lançamento de estorno negativo no extrato.</p>
            <p>O produto será devolvido ao estoque.</p>
          </div>

          <div class="info-box" *ngIf="extratoParaEstornar">
            <p><strong>Lançamento a estornar:</strong></p>
            <p>{{ extratoParaEstornar.descricao }}</p>
            <p><strong>Quantidade:</strong> {{ extratoParaEstornar.quantidade }}</p>
            <p><strong>Valor:</strong> R$ {{ formatarMoeda(extratoParaEstornar.totalLancamento) }}</p>
          </div>

          <div class="campo">
            <label>Motivo do Estorno *</label>
            <textarea [(ngModel)]="motivoEstorno" rows="3"
                      placeholder="Ex: Produto lançado errado, quantidade incorreta, etc."
                      required></textarea>
            <small>Obrigatório - Informe o motivo do estorno para auditoria</small>
          </div>

          <div class="campo">
            <label>
              <input type="checkbox" [(ngModel)]="criarLancamentoCorreto">
              Criar lançamento correto automaticamente
            </label>
            <small>Marque se deseja já lançar o produto correto</small>
          </div>

          <div *ngIf="criarLancamentoCorreto" class="secao-correcao">
            <h3>📝 Dados do Lançamento Correto</h3>
            
            <div class="campo">
              <label>Produto Correto *</label>
              <select [(ngModel)]="produtoCorretoId">
                <option value="0">Selecione um produto...</option>
                <option *ngFor="let produto of produtos" [value]="produto.id">
                  {{ produto.nomeProduto }} - R$ {{ formatarMoeda(produto.valorVenda) }} 
                  (Estoque: {{ produto.quantidade }})
                </option>
              </select>
            </div>

            <div class="campo">
              <label>Quantidade Correta *</label>
              <input type="number" [(ngModel)]="quantidadeCorreta" min="1">
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn-cancelar-modal" (click)="fecharModalEstorno()">
              Cancelar
            </button>
            <button class="btn-confirmar btn-estornar-confirmar" (click)="confirmarEstorno()">
              ✅ Confirmar Estorno
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL DESCONTO -->
      <div class="modal-overlay" *ngIf="modalDesconto" (click)="fecharModalDesconto()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>💰 Aplicar Desconto</h2>
          
          <div class="info-box">
            <p><strong>Total da Hospedagem:</strong> R$ {{ formatarMoeda(reserva.totalHospedagem) }}</p>
            <p><strong>Já Recebido:</strong> R$ {{ formatarMoeda(reserva.totalRecebido) }}</p>
            <p><strong>Saldo Atual:</strong> R$ {{ formatarMoeda(reserva.totalApagar) }}</p>
          </div>

          <div class="campo">
            <label>Valor do Desconto (R$) *</label>
            <input 
              type="text" 
              [(ngModel)]="valorDesconto" 
              appCurrencyInput
              placeholder="R$ 0,00">
            <small>Máximo: R$ {{ formatarMoeda(reserva.totalHospedagem) }}</small>
          </div>

          <div class="campo" *ngIf="valorDesconto > 0">
            <div class="info-box" style="background: #d4edda; border-color: #28a745;">
              <p style="color: #155724;"><strong>Novo Total:</strong> R$ {{ formatarMoeda((reserva.totalHospedagem || 0) - valorDesconto) }}</p>
              <p style="color: #155724;"><strong>Novo Saldo:</strong> R$ {{ formatarMoeda((reserva.totalHospedagem || 0) - valorDesconto - (reserva.totalRecebido || 0)) }}</p>
            </div>
          </div>

          <div class="campo">
            <label>Motivo do Desconto</label>
            <textarea [(ngModel)]="motivoDesconto" rows="3"
                      placeholder="Ex: Desconto promocional, cortesia, problema na hospedagem, etc."></textarea>
            <small>Opcional - Informe o motivo do desconto para controle</small>
          </div>

          <div class="modal-footer">
            <button class="btn-cancelar-modal" (click)="fecharModalDesconto()">
              Cancelar
            </button>
            <button class="btn-confirmar" (click)="confirmarDesconto()">
              💰 Aplicar Desconto
            </button>
          </div>
        </div>
      </div>

      <!-- ✨ MODAL CHECKOUT PARCIAL DE HÓSPEDE -->
      <div class="modal-overlay" *ngIf="modalCheckoutParcial" (click)="fecharModalCheckoutParcial()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>🚪 Checkout Parcial de Hóspede</h2>
          
          <div class="info-box info-box-checkout">
            <p><strong>Hóspede:</strong> {{ hospedeParaCheckout?.cliente?.nome || hospedeParaCheckout?.nomeCompleto }}</p>
            <p *ngIf="hospedeParaCheckout?.titular" class="aviso-titular">
              ⚠️ Este é o hóspede TITULAR. O próximo hóspede da lista será automaticamente promovido a titular.
            </p>
          </div>

          <div class="info-box">
            <p><strong>📋 Informações da Reserva:</strong></p>
            <p>Reserva #{{ reserva.id }} - Apartamento {{ reserva.apartamento?.numeroApartamento }}</p>
            <p>Hóspedes atuais: {{ reserva.quantidadeHospede }}</p>
            <p>Nova quantidade após checkout: {{ (reserva.quantidadeHospede || 0) - 1 }}</p>
          </div>

          <div class="campo">
            <label>Motivo do Checkout *</label>
            <textarea 
              [(ngModel)]="motivoCheckoutParcial"
              rows="3"
              placeholder="Ex: Hóspede finalizou sua estadia antes do grupo, mudou de apartamento, etc.">
            </textarea>
            <small>Obrigatório - Este registro será salvo no histórico da reserva</small>
          </div>

          <div class="info-box info-box-alerta">
            <p><strong>⚠️ ATENÇÃO:</strong></p>
            <ul>
              <li>✅ Diárias não fechadas serão canceladas automaticamente</li>
              <li>✅ Diárias futuras serão recalculadas para a nova quantidade de hóspedes</li>
              <li>✅ Total da reserva será atualizado</li>
              <li>✅ Esta ação ficará registrada no histórico</li>
            </ul>
          </div>

          <div class="modal-footer">
            <button class="btn-cancelar-modal" (click)="fecharModalCheckoutParcial()">
              Cancelar
            </button>
            <button class="btn-confirmar btn-checkout" (click)="confirmarCheckoutParcial()">
              🚪 Confirmar Checkout
            </button>
          </div>
        </div>
      </div>

    </div>

    <!-- MODAL DE ASSINATURA - FATURA -->
<div class="modal-overlay" *ngIf="modalAssinatura" (click)="cancelarAssinatura()">
  <div class="modal-content modal-assinatura" (click)="$event.stopPropagation()">
    <div class="modal-header">
      <h3>✍️ Assinatura do Hóspede</h3>
      <button class="btn-fechar-modal" (click)="cancelarAssinatura()">&times;</button>
    </div>

    <div class="modal-body">
      <p class="modal-info">
        <strong>Cliente:</strong> {{ reserva?.cliente?.nome }}<br>
        <strong>Apartamento:</strong> {{ reserva?.apartamento?.numeroApartamento }}<br>
        <strong>Valor a Pagar:</strong> R$ {{ formatarMoeda(reserva?.totalApagar) }}
      </p>

      <p class="modal-info" style="color: #e67e22; font-weight: bold;">
        Ao assinar, você confirma o valor faturado para pagamento posterior.
      </p>

      <!-- SIGNATURE PAD -->
      <app-signature-pad #signaturePad></app-signature-pad>
    </div>

    <div class="modal-footer">
      <button class="btn-cancelar" (click)="cancelarAssinatura()">❌ Cancelar</button>
      <button class="btn-confirmar" (click)="confirmarAssinaturaFaturada()">✅ Confirmar e Finalizar</button>
    </div>
  </div>
</div>


    <!-- ========================================== -->
<!-- MODAL EDITAR PLACA DO HÓSPEDE -->
<!-- ========================================== -->
<div class="modal-overlay" *ngIf="modalEditarPlaca" (click)="fecharModalEditarPlaca()">
  <div class="modal-content modal-placa" (click)="$event.stopPropagation()">
    <h2>🚗 {{ hospedeEditandoPlaca?.placaCarro ? 'Editar' : 'Adicionar' }} Placa do Veículo</h2>
    
    <div class="info-box">
      <p><strong>Hóspede:</strong> {{ hospedeEditandoPlaca?.cliente?.nome || hospedeEditandoPlaca?.nomeCompleto }}</p>
      <p><strong>Apartamento:</strong> {{ reserva?.apartamento?.numeroApartamento }}</p>
      <p *ngIf="hospedeEditandoPlaca?.placaCarro">
        <strong>Placa atual:</strong> <span class="placa-destaque">{{ hospedeEditandoPlaca.placaCarro }}</span>
      </p>
    </div>

    <div class="campo">
      <label>Placa do Veículo *</label>
      <input 
        type="text"
        [(ngModel)]="placaEditando"
        (input)="formatarPlacaEdicao()"
        placeholder="ABC-1234 ou ABC-1D23"
        maxlength="8"
        class="input-placa-grande"
        autofocus>
      <small class="form-text-hint">
        💡 Formato: ABC-1234 (padrão) ou ABC-1D23 (Mercosul)
      </small>
    </div>

    <div class="info-box info-box-sucesso" *ngIf="placaEditando && placaEditando.length >= 7">
      <p>✅ Placa válida: <strong>{{ placaEditando }}</strong></p>
    </div>

    <div class="modal-footer">
      <button class="btn-cancelar-modal" (click)="fecharModalEditarPlaca()">
        Cancelar
      </button>
      <button 
        class="btn-confirmar"
        (click)="salvarPlacaHospede()"
        [disabled]="!placaEditando || placaEditando.length < 7">
        💾 Salvar Placa
      </button>
    </div>

    <button type="button" class="btn-fechar-modal" (click)="fecharModalEditarPlaca()">
      ✕
    </button>
  </div>
</div>

    <!-- ✅ COMPONENTE DO MODAL DE TRANSFERÊNCIA -->
    <app-transferir-hospede-modal 
      (transferenciaRealizada)="recarregarReserva()">
    </app-transferir-hospede-modal>    
  </div> 

`,
  styles: [`
    /* CONTAINER PRINCIPAL */
    .container-detalhes {
      padding: 20px;
      max-width: 1600px;
      margin: 0 auto;
      min-height: 100vh;
      background: #f5f7fa;
    }

    /* LOADING */
    .loading {
      text-align: center;
      padding: 60px;
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

    /* ERRO */
    .erro {
      text-align: center;
      padding: 40px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .erro h2 {
      color: #e74c3c;
      margin-bottom: 15px;
    }

    .erro button {
      margin-top: 20px;
      padding: 10px 20px;
      background: #667eea;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
    }

    /* HEADER */
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

    .header h1 {
      margin: 0;
      color: #2c3e50;
      font-size: 2em;
    }

    .badge-status {
      display: inline-block;
      padding: 6px 16px;
      border-radius: 20px;
      font-size: 0.85em;
      font-weight: 600;
      margin-left: 15px;
      text-transform: uppercase;
    }

    .status-ativa {
      background: #d4edda;
      color: #155724;
    }

    .status-pre_reserva {
      background: #d1ecf1;
      color: #0c5460;
    }

    .status-finalizada {
      background: #cce5ff;
      color: #004085;
    }

    .status-cancelada {
      background: #f8d7da;
      color: #721c24;
    }

    .btn-voltar {
      background: #95a5a6;
      color: white;
      border: none;
      padding: 10px 20px;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.3s;
    }

    .btn-voltar:hover {
      background: #7f8c8d;
      transform: translateY(-2px);
    }

    /* ✅ GRID DE 4 COLUNAS - HORIZONTAL */
    .grid-quatro-colunas {
      display: grid;
      grid-template-columns: 1fr 1fr 1fr 1fr;
      gap: 15px;
      margin-bottom: 20px;
    }

    /* GRID NORMAL */
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 20px;
      margin-bottom: 20px;
    }

    /* CARDS */
    .card {
      background: white;
      border-radius: 12px;
      padding: 25px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      transition: all 0.3s;
    }

    .card:hover {
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }

    .card h2 {
      margin: 0 0 20px 0;
      color: #2c3e50;
      font-size: 1.3em;
      border-bottom: 2px solid #667eea;
      padding-bottom: 10px;
    }

    /* ✅ CARDS MINI - COMPACTOS */
    .card-mini {
      background: white;
      border-radius: 10px;
      padding: 15px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      transition: all 0.3s;
    }

    .card-mini:hover {
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }

    .card-mini h2 {
      margin: 0 0 12px 0;
      color: #2c3e50;
      font-size: 1em;
      border-bottom: 2px solid #667eea;
      padding-bottom: 8px;
    }

    /* ✅ INFO ITEMS MINI */
    .info-item-mini {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 6px 0;
      border-bottom: 1px solid #f0f0f0;
      font-size: 0.85em;
    }

    .info-item-mini:last-child {
      border-bottom: none;
    }

    .info-item-mini .label {
      color: #7f8c8d;
      font-weight: 500;
      font-size: 0.9em;
      min-width: 65px;
    }

    .info-item-mini .value,
    .info-item-mini .value-pequeno {
      color: #2c3e50;
      font-weight: 600;
      font-size: 0.9em;
      text-align: right;
      flex: 1;
      word-wrap: break-word;
    }

    .info-item-mini .value-pequeno {
      font-size: 0.85em;
    }

    .info-item-mini .value-destaque {
      color: #2c3e50;
      font-weight: 700;
      font-size: 1em;
    }

    /* ✅ DESTAQUE MINI */
    .destaque-mini {
      background: #f8f9fa;
      margin: 5px -5px 0 -5px;
      padding: 8px 5px;
      border-radius: 4px;
      border-bottom: none !important;
    }

    /* ✅ INFO ITEM COM BOTÃO MINI */
    .info-item-com-botao-mini {
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1px solid #f0f0f0;
      padding: 6px 0;
    }

    .info-item-com-botao-mini .info-item-mini {
      flex: 1;
      border: none;
      padding: 0;
    }

    .info-item-com-botao-mini .btn-mini {
      margin-left: 5px;
      padding: 4px 8px;
      font-size: 0.9em;
    }

    /* INFO ITEMS NORMAIS */
    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid #ecf0f1;
    }

    .info-item:last-child {
      border-bottom: none;
    }

    .info-item .label {
      color: #7f8c8d;
      font-weight: 500;
    }

    .info-item .value {
      color: #2c3e50;
      font-weight: 600;
    }

    .info-item.destaque {
      background: #f8f9fa;
      margin: 0 -10px;
      padding: 12px 10px;
      border-radius: 6px;
      border-bottom: none;
    }

    .numero-apt {
      font-size: 1.3em;
      color: #667eea;
    }

    .valor-positivo {
      color: #27ae60;
    }

    .valor-negativo {
      color: #e74c3c;
    }

    /* INFO ITEM COM BOTÃO */
    .info-item-com-botao {
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1px solid #ecf0f1;
      padding: 12px 0;
    }

    .info-item-com-botao .info-item {
      flex: 1;
      border: none;
      padding: 0;
    }

    .btn-mini {
      background: #3498db;
      color: white;
      border: none;
      padding: 6px 10px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 1em;
      transition: all 0.3s;
      margin-left: 10px;
    }

    .btn-mini:hover {
      background: #2980b9;
      transform: scale(1.1);
    }

    /* BOTÕES DE DESCONTO */
    .btn-desconto {
      background: #28a745 !important;
    }

    .btn-desconto:hover {
      background: #218838 !important;
    }

    /* LISTA DE DESCONTOS */
    .lista-descontos {
      margin-top: 8px;
      padding: 8px;
      background: #f8f9fa;
      border-radius: 6px;
      border-left: 3px solid #28a745;
    }

    .desconto-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 6px 8px;
      margin-bottom: 4px;
      background: white;
      border-radius: 4px;
      border-left: 2px solid #28a745;
    }

    .desconto-item:last-child {
      margin-bottom: 0;
    }

    .desconto-valor {
      font-weight: bold;
      color: #28a745;
      min-width: 80px;
    }

    .desconto-motivo {
      flex: 1;
      margin: 0 10px;
      font-size: 0.9em;
      color: #666;
    }

    .btn-remover-desc {
      background: #dc3545;
      color: white;
      border: none;
      padding: 4px 8px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.9em;
      transition: all 0.3s;
    }

    .btn-remover-desc:hover {
      background: #c82333;
      transform: scale(1.1);
    }

    /* EXTRATO */
    .extrato-card {
      grid-column: 1 / -1;
    }

    .tabela-extrato {
      width: 100%;
      border-collapse: collapse;
      margin-top: 15px;
    }

    .tabela-extrato th {
      background: #f8f9fa;
      padding: 12px;
      text-align: left;
      font-weight: 600;
      color: #2c3e50;
      border-bottom: 2px solid #dee2e6;
    }

    .tabela-extrato td {
      padding: 12px;
      border-bottom: 1px solid #ecf0f1;
    }

    .tabela-extrato tr:hover {
      background: #f8f9fa;
    }

    .badge-extrato {
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 0.8em;
      font-weight: 600;
      margin-right: 8px;
    }

    .badge-diaria {
      background: #d4edda;
      color: #155724;
    }

    .badge-produto {
      background: #fff3cd;
      color: #856404;
    }

    .badge-estorno {
      background: #f8d7da;
      color: #721c24;
    }

    .badge-pagamento {
      background: #d1ecf1;
      color: #0c5460;
    }

    .btn-estornar {
      background: #e74c3c;
      color: white;
      border: none;
      padding: 6px 12px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.85em;
      font-weight: 600;
      transition: all 0.3s;
    }

    .btn-estornar:hover {
      background: #c0392b;
      transform: scale(1.05);
    }

    .btn-estornar:disabled {
      background: #95a5a6 !important;
      cursor: not-allowed !important;
      opacity: 0.6;
    }

    .btn-estornar:disabled:hover {
      background: #95a5a6 !important;
      transform: none !important;
    }

    .tabela-extrato th:last-child,
    .tabela-extrato td:last-child {
      text-align: center;
      width: 120px;
    }

    /* HISTÓRICO */
    .historico-card {
      grid-column: 1 / -1;
    }

    .timeline {
      position: relative;
      padding: 20px 0 20px 40px;
    }

    .timeline::before {
      content: '';
      position: absolute;
      left: 10px;
      top: 0;
      bottom: 0;
      width: 2px;
      background: #dee2e6;
    }

    .timeline-item {
      position: relative;
      margin-bottom: 20px;
    }

    .timeline-marker {
      position: absolute;
      left: -35px;
      width: 12px;
      height: 12px;
      border-radius: 50%;
      background: #667eea;
      border: 3px solid white;
      box-shadow: 0 0 0 2px #667eea;
    }

    .timeline-content {
      background: #f8f9fa;
      padding: 15px;
      border-radius: 8px;
      border-left: 3px solid #667eea;
    }

    .timeline-header {
      margin-bottom: 8px;
    }

    .timeline-date {
      font-size: 0.9em;
      color: #7f8c8d;
      font-weight: 600;
    }

    .timeline-body p {
      margin: 0 0 5px 0;
      color: #2c3e50;
    }

    .timeline-body small {
      color: #95a5a6;
    }

    /* AÇÕES */
    .acoes-card {
      grid-column: 1 / -1;
    }

    .botoes-acoes {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 15px;
      margin-top: 15px;
    }

    .btn-acao {
      padding: 15px 20px;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 600;
      font-size: 1em;
      transition: all 0.3s;
      text-align: center;
    }

    .btn-acao:hover {
      transform: translateY(-3px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.2);
    }

    .btn-checkin {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }

    .btn-comanda {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      color: white;
    }

    .btn-pagamento {
      background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
      color: white;
    }

    /* ========================================== */
    /* GESTÃO DE HÓSPEDES */
    /* ========================================== */

    .secao-hospedes {
      background: #f8f9fa;
      border: 2px solid #dee2e6;
      border-radius: 8px;
      padding: 20px;
      margin: 25px 0;
    }

    .secao-header {
      display: flex;
      align-items: center;
      gap: 15px;
      margin-bottom: 20px;
      padding-bottom: 15px;
      border-bottom: 2px solid #dee2e6;
    }

    .secao-header h3 {
      margin: 0;
      color: #2c3e50;
      font-size: 1.3em;
      flex: 1;
    }

    .badge-hospedes {
      background: #667eea;
      color: white;
      padding: 5px 12px;
      border-radius: 20px;
      font-weight: 600;
      font-size: 0.9em;
    }

    .btn-adicionar-hospede {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      padding: 10px 20px;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.3s ease;
    }

    .btn-adicionar-hospede:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(102, 126, 234, 0.4);
    }

    .lista-hospedes {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .hospede-item {
      display: flex;
      align-items: center;
      gap: 15px;
      background: white;
      padding: 15px;
      border-radius: 6px;
      border: 1px solid #dee2e6;
    }

    .hospede-numero {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 35px;
      height: 35px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 50%;
      font-weight: 700;
      font-size: 1.1em;
    }

    .hospede-info {
      flex: 1;
    }

    .hospede-nome {
      font-weight: 600;
      color: #2c3e50;
      margin-bottom: 5px;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .badge-titular {
      background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
      color: white;
      padding: 3px 10px;
      border-radius: 12px;
      font-size: 0.75em;
      font-weight: 700;
    }

    .hospede-detalhes {
      font-size: 0.9em;
      color: #7f8c8d;
    }

    /* ✅ NOVO: Estilo para empresa do hóspede */
    .hospede-empresa {
      background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
      color: #1565c0;
      padding: 6px 12px;
      border-radius: 8px;
      font-size: 0.85em;
      font-weight: 600;
      margin-top: 8px;
      margin-bottom: 5px;
      display: inline-block;
      border-left: 3px solid #1976d2;
    }

    .btn-consumo {
      background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
      color: white;
    }

    .btn-transferir {
      background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
      color: white;
    }

    .btn-finalizar {
      background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
      color: white;
    }

    .btn-finalizar-paga {
      background: linear-gradient(135deg, #27ae60 0%, #229954 100%);
      color: white;
    }

    .btn-recibo {
      background: linear-gradient(135deg, #9b59b6 0%, #8e44ad 100%);
      color: white;
    }

    .btn-cancelar {
      background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
      color: white;
    }

    /* MODAIS */
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
      position: relative;
    }

    .modal-content.modal-grande {
      max-width: 700px;
    }

    .modal-content h2 {
      margin: 0 0 20px 0;
      color: #2c3e50;
    }

    .info-box {
      background: #e3f2fd;
      padding: 15px;
      border-radius: 8px;
      margin-bottom: 20px;
      border-left: 4px solid #2196f3;
    }

    .info-box p {
      margin: 5px 0;
      color: #1976d2;
    }

    .info-box-alerta {
      background: #fff3cd;
      border-left: 4px solid #ffc107;
    }

    .info-box-alerta p {
      color: #856404;
    }

    .info-box-alerta p:first-child {
      font-weight: bold;
      margin-bottom: 10px;
    }

    .info-box-alerta ul {
      margin: 10px 0 0 20px;
      padding: 0;
    }

    .info-box-alerta li {
      margin: 6px 0;
      color: #856404;
    }

    .secao-correcao {
      background: #e3f2fd;
      padding: 15px;
      border-radius: 8px;
      margin-top: 15px;
      border-left: 4px solid #2196f3;
    }

    .secao-correcao h3 {
      margin: 0 0 15px 0;
      color: #1976d2;
      font-size: 1.1em;
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

    .campo input[type="text"],
    .campo input[type="number"],
    .campo input[type="date"],
    .campo select,
    .campo textarea {
      width: 100%;
      padding: 10px;
      border: 2px solid #e0e0e0;
      border-radius: 6px;
      font-size: 1em;
      transition: all 0.3s;
      box-sizing: border-box;
    }

    .campo input:focus,
    .campo select:focus,
    .campo textarea:focus {
      outline: none;
      border-color: #667eea;
    }

    .campo small {
      display: block;
      margin-top: 5px;
      color: #7f8c8d;
      font-size: 0.9em;
    }

    .campo input[type="checkbox"] {
      margin-right: 8px;
    }

    .modal-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 25px;
      padding-top: 20px;
      border-top: 1px solid #ecf0f1;
    }

    .btn-cancelar-modal,
    .btn-confirmar {
      padding: 10px 20px;
      border: none;
      border-radius: 6px;
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
      background: #667eea;
      color: white;
    }

    .btn-confirmar:hover {
      background: #5568d3;
      transform: translateY(-2px);
    }

    .btn-estornar-confirmar {
      background: #e74c3c !important;
    }

    .btn-estornar-confirmar:hover {
      background: #c0392b !important;
    }

    .modal-tabs {
      display: flex;
      gap: 10px;
      margin-bottom: 25px;
      border-bottom: 2px solid #dee2e6;
    }

    .modal-tabs button {
      flex: 1;
      padding: 12px;
      background: transparent;
      border: none;
      border-bottom: 3px solid transparent;
      cursor: pointer;
      font-weight: 600;
      color: #7f8c8d;
    }

    .modal-tabs button.active {
      color: #667eea;
      border-bottom-color: #667eea;
    }

    .input-busca-modal {
      width: 100%;
      padding: 12px;
      border: 2px solid #dee2e6;
      border-radius: 6px;
      font-size: 14px;
      margin-bottom: 15px;
      box-sizing: border-box;
    }

    .resultados-modal {
      max-height: 300px;
      overflow-y: auto;
      border: 1px solid #dee2e6;
      border-radius: 6px;
    }

    .resultado-modal-item {
      padding: 12px;
      cursor: pointer;
      border-bottom: 1px solid #f0f0f0;
    }

    .resultado-modal-item:hover {
      background: #f8f9fa;
    }

    .resultado-nome {
      font-weight: 600;
      margin-bottom: 4px;
    }

    .resultado-info {
      font-size: 0.9em;
      color: #666;
    }

    .sem-resultado-modal {
      padding: 20px;
      text-align: center;
      color: #999;
    }

    .form-group {
      margin-bottom: 15px;
    }

    .form-group label {
      display: block;
      margin-bottom: 5px;
      font-weight: 600;
      color: #2c3e50;
    }

    .form-group input {
      width: 100%;
      padding: 10px;
      border: 2px solid #dee2e6;
      border-radius: 6px;
      font-size: 14px;
      box-sizing: border-box;
    }

    .btn-salvar-hospede {
      width: 100%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      padding: 14px;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      margin-top: 15px;
    }

    .btn-fechar-modal {
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
    }

    .info-cadastro {
      background: #e3f2fd;
      border-left: 4px solid #2196f3;
      padding: 12px;
      border-radius: 4px;
      margin: 15px 0;
      font-size: 0.9em;
      color: #1565c0;
    }

    .info-box-checkout {
      background: #fff3cd;
      border-left: 4px solid #ffc107;
    }

    .info-box-checkout p {
      color: #856404;
      margin: 8px 0;
    }

    .aviso-titular {
      font-weight: 600;
      padding: 8px;
      background: #ffe5b4;
      border-radius: 4px;
      margin-top: 8px;
    }

    .btn-checkout {
      background: #e67e22 !important;
    }

    .btn-checkout:hover {
      background: #d35400 !important;
    }

    .hospede-item.hospede-checkout {
      opacity: 0.6;
      background: #f8f9fa;
      border-left: 4px solid #95a5a6;
    }

    .hospede-numero.checkout {
      background: linear-gradient(135deg, #95a5a6 0%, #7f8c8d 100%);
    }

    .badge-checkout {
      background: linear-gradient(135deg, #95a5a6 0%, #7f8c8d 100%);
      color: white;
      padding: 3px 10px;
      border-radius: 12px;
      font-size: 0.75em;
      font-weight: 700;
      margin-left: 8px;
    }

    .hospede-status {
      margin-top: 5px;
      padding-top: 5px;
      border-top: 1px solid #dee2e6;
    }

    .hospede-status small {
      color: #6c757d;
      font-style: italic;
    }

    .hospede-finalizado {
      font-size: 1.8em;
      color: #28a745;
      opacity: 0.7;
    }

    .hospede-acoes {
      display: flex;
      gap: 8px;
      align-items: center;
    }

    .btn-acao-hospede {
      border: none;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      cursor: pointer;
      font-size: 1.3em;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .btn-transferir-hospede {
      background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
      color: white;
    }

    .btn-transferir-hospede:hover {
      background: linear-gradient(135deg, #2980b9 0%, #21618c 100%);
      transform: scale(1.1) rotate(180deg);
      box-shadow: 0 4px 12px rgba(52, 152, 219, 0.4);
    }

    .btn-checkout-hospede {
      background: #e67e22;
      color: white;
    }

    .btn-checkout-hospede:hover {
      background: #d35400;
      transform: scale(1.1);
      box-shadow: 0 4px 12px rgba(230, 126, 34, 0.4);
    }

    .separador-mini {
      border-top: 1px solid #dee2e6;
      padding-top: 6px !important;
      margin-top: 4px;
    }

    /* RESPONSIVO */
    @media (max-width: 1400px) {
      .grid-quatro-colunas {
        grid-template-columns: 1fr 1fr;
      }
    }
    
    .btn-extrato {
  background: linear-gradient(135deg, #17a2b8 0%, #138496 100%);
  color: white;
}

    @media (max-width: 768px) {
      .grid {
        grid-template-columns: 1fr;
      }

      .grid-quatro-colunas {
        grid-template-columns: 1fr;
      }

      .botoes-acoes {
        grid-template-columns: 1fr;
      }

      .modal-content {
        padding: 20px;
        max-width: 95%;
      }

      .header {
        flex-direction: column;
        align-items: flex-start;
      }

      .header h1 {
        font-size: 1.5em;
        margin-bottom: 10px;
      }
      
      .btn-voltar {
        width: 100%;
        margin-top: 10px;
      }

      .hospede-placa {
        background: #1976d2;
        color: white;
        padding: 2px 8px;
        border-radius: 4px;
        font-weight: bold;
        font-family: monospace;
        font-size: 11px;
        margin-left: 5px;
      }

      /* Campo de placa */
.input-placa {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  letter-spacing: 1px;
}

.form-text-hint {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: #666;
  font-style: italic;
}

   /* ════════════════════════════════════════════
   PLACA DO HÓSPEDE
   ════════════════════════════════════════════ */

.hospede-placa-container {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.hospede-placa {
  color: #2196f3;
  font-weight: 600;
  font-family: 'Courier New', monospace;
  letter-spacing: 1px;
}

.hospede-placa.sem-placa {
  color: #e67e22;
  font-style: italic;
  font-family: inherit;
  letter-spacing: normal;
}

.btn-mini-placa {
  background: #3498db;
  color: white;
  border: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 0.9em;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
}

.btn-mini-placa:hover {
  background: #2980b9;
  transform: scale(1.15);
}

/* MODAL DE PLACA */
.modal-placa {
  max-width: 500px;
}

.placa-destaque {
  color: #2196f3;
  font-family: 'Courier New', monospace;
  font-size: 1.1em;
  font-weight: bold;
  letter-spacing: 2px;
}

.input-placa-grande {
  width: 100%;
  padding: 16px 20px;
  border: 3px solid #2196f3;
  border-radius: 10px;
  font-family: 'Courier New', monospace;
  font-size: 1.5em;
  font-weight: bold;
  letter-spacing: 3px;
  text-transform: uppercase;
  text-align: center;
  transition: all 0.3s;
}

.input-placa-grande:focus {
  outline: none;
  border-color: #1976d2;
  box-shadow: 0 0 0 4px rgba(33, 150, 243, 0.2);
  transform: scale(1.02);
}

.info-box-sucesso {
  background: #d4edda;
  border-color: #28a745;
  color: #155724;
}

.form-text-hint {
  display: block;
  margin-top: 8px;
  font-size: 0.85em;
  color: #7f8c8d;
  font-style: italic;
}

.btn-ativar-reserva {
  background: linear-gradient(135deg, #27ae60 0%, #229954 100%);
  color: white;
  animation: pulse 2s ease-in-out infinite;
  font-weight: 700;
  box-shadow: 0 4px 15px rgba(39, 174, 96, 0.4);
}

.btn-ativar-reserva:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(39, 174, 96, 0.6);
}

.btn-estornado {
  background: #95a5a6 !important;
  cursor: not-allowed !important;
  opacity: 0.6;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 4px 15px rgba(39, 174, 96, 0.4);
  }
  50% {
    box-shadow: 0 4px 25px rgba(39, 174, 96, 0.7);
  }
}
    }
  `]
})

export class ReservaDetalhesApp implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  private authService = inject(AuthService);  
  private cdr = inject(ChangeDetectorRef); 
  private reservaService = inject(ReservaService); 
  private alertasStateService = inject(AlertasStateService);

  private apiUrl = 'http://localhost:8080/api';

   @ViewChild(TransferirHospedeModalComponent, { static: false }) 
modalTransferirHospede?: TransferirHospedeModalComponent;

  reserva: ReservaDetalhes | null = null;
  reservaId: number = 0;
  loading = false;
  erro = '';

  // ALTERAR CHECKOUT
  modalAlterarCheckout = false;
  novaDataCheckout = '';
  motivoAlteracaoCheckout = '';

  // ALTERAR HÓSPEDES
  modalAlterarHospedes = false;
  novaQuantidadeHospedes = 1;
  motivoAlteracaoHospedes = '';

  // Gestão de hóspedes
modalAdicionarHospede = false;
modoModalHospede: 'buscar' | 'cadastrar' = 'buscar';
clientesFiltradosModal: any[] = [];
termoBuscaHospede = '';

novoHospede = {
  nome: '',
  cpf: '',
  celular: '',
  placaCarro: ''
};

hospedes: any[] = [];

  // PAGAMENTO
  modalPagamento = false;
  pagValor = 0;
  pagFormaPagamento = '';
  pagObs = '';
  formasPagamento = [
    { codigo: 'DINHEIRO', nome: 'Dinheiro' },
    { codigo: 'PIX', nome: 'PIX' },
    { codigo: 'CARTAO_DEBITO', nome: 'Cartão Débito' },
    { codigo: 'CARTAO_CREDITO', nome: 'Cartão Crédito' },
    { codigo: 'TRANSFERENCIA_BANCARIA', nome: 'Transferência' }
  ];

  // CONSUMO
  modalConsumo = false;
  produtos: Produto[] = [];
  produtoSelecionadoId = 0;
  quantidadeConsumo = 1;
  observacaoConsumo = '';

  // TRANSFERÊNCIA
  modalTransferencia = false;
  apartamentosDisponiveis: Apartamento[] = [];
  novoApartamentoId = 0;
  dataTransferencia = '';
  transferenciaImediata = true;
  motivoTransferencia = '';

  // ESTORNO
  modalEstorno = false;
  extratoParaEstornar: any = null;
  motivoEstorno = '';
  criarLancamentoCorreto = false;
  produtoCorretoId = 0;
  quantidadeCorreta = 1;

  // PLACAS DE CARRO
  modalEditarPlaca = false;
  hospedeEditandoPlaca: any = null;
  placaEditando = '';

  // DESCONTO
  modalDesconto = false;
  valorDesconto = 0;
  motivoDesconto = '';

   // CHECKOUT PARCIAL - NOVO
  modalCheckoutParcial = false;
  hospedeParaCheckout: any = null;
  motivoCheckoutParcial = '';
 
  modalAssinatura = false;
  assinaturaCapturada: string | null = null; 

  @ViewChild('signaturePad') signaturePad!: SignaturePadComponent;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.reservaId = Number(id);
      this.carregarReserva(this.reservaId);
    } else {
      this.erro = 'ID da reserva não fornecido';
    }
  }

 carregarReserva(id: number): void {
  this.loading = true;
  this.erro = '';
  this.http.get<ReservaDetalhes>(`http://localhost:8080/api/reservas/${id}`).subscribe({
    next: (data) => {
      this.reserva = data;
      this.loading = false;
      
      // ✅ DEBUG AUDITORIA
      console.log('🔍 AUDITORIA:', {
        criadoPor: data.criadoPor,
        dataCriacao: data.dataCriacao,
        finalizadoPor: data.finalizadoPor,
        dataFinalizacao: data.dataFinalizacao,
        canceladoPor: data.canceladoPor,
        dataCancelamento: data.dataCancelamento,
        motivoCancelamento: data.motivoCancelamento
      });
      
      // ✅ LOG CORRIGIDO:
      console.log('═══════════════════════════════════════');
      console.log('📋 TODOS OS EXTRATOS:');
      console.table(data.extratos);
      
      console.log('💰 EXTRATOS COM DESCONTO:');
      const extratosDesconto = (data.extratos || []).filter((e: any) => 
        e.descricao && e.descricao.includes('Desconto')
      );
      console.table(extratosDesconto);
      
      console.log('🔍 VERIFICANDO CONDIÇÕES DOS BOTÕES:');
      extratosDesconto.forEach((ext: any, i: number) => {
        console.log(`Extrato ${i + 1}:`, {
          id: ext.id,
          descricao: ext.descricao,
          statusLancamento: ext.statusLancamento,
          estornado: ext.estornado,
          'Começa com "Desconto aplicado"?': ext.descricao?.startsWith('Desconto aplicado'),
          'Status é ESTORNO?': ext.statusLancamento === 'ESTORNO',
          'NÃO está estornado?': !ext.estornado,
          'DEVE MOSTRAR BOTÃO?': 
            ext.statusLancamento === 'ESTORNO' && 
            ext.descricao?.startsWith('Desconto aplicado') &&
            !ext.estornado
        });
      });
      console.log('═══════════════════════════════════════');
      console.log('✅ Reserva carregada:', data);
      
      // ✅ CARREGAR HÓSPEDES PARA QUALQUER STATUS
      // (antes só carregava para ATIVA)
      if (this.reserva.hospedes && this.reserva.hospedes.length > 0) {
        // ✅ HÓSPEDES JÁ VEM NO DTO
        this.hospedes = this.reserva.hospedes;
        console.log('✅ Hóspedes do DTO:', this.hospedes.length);
      } else {
        // ✅ FALLBACK: Buscar hóspedes separadamente
        this.carregarHospedes();
      }
    },
    error: (err: any) => {
      console.error('❌ Erro:', err);
      this.erro = err.error?.message || 'Erro ao carregar reserva';
      this.loading = false;
    }
  });
}

  /**
   * ✅ MÉTODO PARA RECARREGAR RESERVA APÓS ALTERAÇÃO DE HÓSPEDES
   */ 
  
  recarregarReserva(): void {
  if (this.reservaId) {
    console.log('🔄 Recarregando reserva após alteração de hóspedes...');
    this.carregarReserva(this.reservaId);
  }
}



  voltar(): void {
    this.router.navigate(['/reservas']);
  }

  abrirComanda(): void {
  this.abrirModalConsumo();
}

  formatarData(data: any): string {
    if (!data) return '-';
    const d = new Date(data);
    return d.toLocaleDateString('pt-BR');
  }

  formatarDataHora(data: any): string {
    if (!data) return '-';
    const d = new Date(data);
    return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  formatarMoeda(valor: any): string {
    if (valor === null || valor === undefined) return '0,00';
    return Number(valor).toFixed(2).replace('.', ',');
  }

  obterStatusClass(): string {
    if (!this.reserva?.status) return 'status-ativa';
    return 'status-' + this.reserva.status.toLowerCase();
  }

  dataAtualCompleta(): string {
    return new Date().toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  dataAtualSimples(): string {
    return new Date().toLocaleDateString('pt-BR');
  }

  formatarDataCompleta(data: any): string {
    if (!data) return '-';
    const d = new Date(data);
    return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  formatarCPF(cpf: any): string {
    if (!cpf) return '';
    const apenasNumeros = cpf.replace(/\D/g, '');
    return apenasNumeros.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }

  // ============= IMPRIMIR CHECK-IN =============
  imprimirCheckin(): void {
  if (!this.reserva) return;
  
  const htmlImpressao = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>Ficha de Check-in - Reserva #${this.reserva.id}</title>
      <style>
        @page { 
          size: 80mm auto; 
          margin: 0; 
        }
        
        * {
          font-family: 'Courier New', monospace !important;
          font-weight: 700 !important;  /* ✅ TUDO EM NEGRITO */
          color: #000 !important;
          -webkit-print-color-adjust: exact !important;
          print-color-adjust: exact !important;
        }
        
        body { 
          width: 80mm; 
          margin: 0; 
          padding: 3mm;
          line-height: 1.4;
          font-size: 11pt !important;  /* ✅ AUMENTADO */
        }
        
        .cabecalho { 
          text-align: left; 
          margin-bottom: 6px; 
        }
        
        .cabecalho h1 { 
          font-size: 16pt !important;  /* ✅ AUMENTADO */
          font-weight: 900 !important;  /* ✅ EXTRA BOLD */
          margin: 0 0 2px 0; 
          letter-spacing: 2px; 
        }
        
        .cnpj, .endereco { 
          font-size: 11pt !important;  /* ✅ AUMENTADO */
          font-weight: 700 !important;
          margin: 2px 0; 
        }
        
        .separador { 
          text-align: center; 
          margin: 5px 0; 
          font-size: 11pt !important;  /* ✅ AUMENTADO */
          font-weight: 700 !important;
        }
        
        .titulo-documento { 
          text-align: left; 
          margin: 6px 0; 
        }
        
        .titulo-documento h2 { 
          font-size: 14pt !important;  /* ✅ AUMENTADO */
          font-weight: 900 !important;
          margin: 0; 
        }
        
        .numero-reserva { 
          font-size: 12pt !important;  /* ✅ AUMENTADO */
          font-weight: 900 !important; 
          margin: 3px 0; 
        }
        
        .data-emissao { 
          font-size: 11pt !important;  /* ✅ AUMENTADO */
          font-weight: 700 !important;
          margin: 2px 0; 
        }
        
        .secao { 
          margin: 6px 0; 
        }
        
        .secao h3 { 
          font-size: 12pt !important;  /* ✅ AUMENTADO */
          font-weight: 900 !important;
          margin: 0 0 4px 0; 
          text-decoration: underline; 
        }
        
        .secao p { 
          margin: 3px 0; 
          font-size: 11pt !important;  /* ✅ AUMENTADO */
          font-weight: 700 !important;
          line-height: 1.4; 
        }
        
        .secao strong {
          font-weight: 900 !important;  /* ✅ EXTRA BOLD */
        }
        
        .linha-valor { 
          display: flex; 
          justify-content: space-between;
          margin: 3px 0; 
          font-size: 11pt !important;  /* ✅ AUMENTADO */
          font-weight: 700 !important;
        }
        
        .linha-valor.destaque { 
          font-size: 13pt !important;  /* ✅ AUMENTADO */
          font-weight: 900 !important; 
          margin: 5px 0; 
        }
        
        .assinatura { 
          margin-top: 12px; 
          text-align: center; 
        }
        
        .texto-assinatura { 
          font-size: 11pt !important;  /* ✅ AUMENTADO */
          font-weight: 700 !important;
          margin: 2px 0; 
        }
        
        .linha-assinatura { 
          border-top: 2px solid #000;  /* ✅ LINHA MAIS GROSSA */
          margin: 10px 15mm 4px 15mm;
          width: auto;
        }
        
        .label-assinatura { 
          font-size: 10pt !important;  /* ✅ AUMENTADO */
          font-weight: 700 !important;
          margin: 2px 0; 
        }
        
        .rodape { 
          text-align: left; 
          margin-top: 10px; 
          font-size: 11pt !important;  /* ✅ AUMENTADO */
        }
        
        .rodape p { 
          margin: 2px 0; 
          font-weight: 700 !important;
        }

        .imagem-assinatura {
            max-width: 100%;
            max-height: 22mm;
            margin-bottom: 2px;
          }

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
        <h2>FICHA DE CHECK-IN</h2>
        <p class="numero-reserva">Reserva Nº ${this.reserva.id}</p>
        <p class="data-emissao">${this.dataAtualCompleta()}</p>
      </div>

      <div class="separador">================================</div>

      <div class="secao">
        <h3>DADOS DO HOSPEDE</h3>
        <p><strong>Nome:</strong> ${this.reserva.cliente?.nome}</p>
        <p><strong>Telefone:</strong> ${this.reserva.cliente?.celular || this.reserva.cliente?.telefone || 'Nao informado'}</p>
      </div>

      <div class="separador">- - - - - - - - - - - - - - - -</div>

      <div class="secao">
        <h3>INFORMACOES DA RESERVA</h3>
        <p><strong>Apartamento:</strong> ${this.reserva.apartamento?.numeroApartamento}</p>
        <p><strong>Check-in:</strong> ${this.formatarDataCompleta(this.reserva.dataCheckin)}</p>
        <p><strong>Check-out:</strong> ${this.formatarDataCompleta(this.reserva.dataCheckout)}</p>
        <p><strong>Diarias:</strong> ${this.reserva.quantidadeDiaria} dia(s)</p>
        <p><strong>Hospedes:</strong> ${this.reserva.quantidadeHospede} pessoa(s)</p>
      </div>

      <div class="separador">- - - - - - - - - - - - - - - -</div>

      <div class="secao">
        <h3>VALORES</h3>
        <div class="linha-valor">
          <span>Valor da Diaria:</span>
          <span>R$ ${this.formatarMoeda(this.reserva.valorDiaria)}</span>
        </div>
        <div class="linha-valor destaque">
          <span>Total Estimado:</span>
          <span>R$ ${this.formatarMoeda(this.reserva.totalDiaria)}</span>
        </div>
      </div>

      <div class="separador">================================</div>

      <div class="assinatura">
        <p class="texto-assinatura">Declaro estar ciente das condicoes</p>
        <p class="texto-assinatura">da reserva e dos valores cobrados.</p>
        <div class="linha-assinatura"></div>
        <p class="label-assinatura">Assinatura do Hospede</p>
        <div class="linha-assinatura"></div>
        <p class="label-assinatura">Data: ____/____/________</p>
      </div>

      <div class="rodape">
        <p>Obrigado pela preferencia!</p>
        <p>Tenha uma excelente estadia!</p>
      </div>

      <script>
        window.onload = function() {
          window.print();
          window.onafterprint = function() {
            window.close();
          };
        };
      </script>
    </body>
    </html>
  `;

  const janelaImpressao = window.open('', '_blank', 'width=800,height=600');
  if (janelaImpressao) {
    janelaImpressao.document.write(htmlImpressao);
    janelaImpressao.document.close();
  }
}

  // ============= RECIBO/FATURA =============
  imprimirRecibo(): void {
    if (!this.reserva) return;

    const temSaldo = (this.reserva.totalApagar || 0) > 0;
    
    if (temSaldo) {
      console.log('🔸 Gerando FATURA');
      this.gerarFatura();
    } else {
      console.log('🔸 Gerando RECIBO');
      this.gerarRecibo();
    }
  }

  gerarRecibo(): void {
  if (!this.reserva) return;

  const totalComDesconto = (this.reserva.totalHospedagem || 0) - (this.reserva.desconto || 0);

  const htmlImpressao = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>Recibo - Reserva #${this.reserva.id}</title>
      <style>
        @page { 
          size: 80mm auto; 
          margin: 0; 
        }
        
        * {
          font-family: 'Courier New', monospace !important;
          font-weight: 700 !important;  /* ✅ TUDO EM NEGRITO */
          color: #000 !important;
          -webkit-print-color-adjust: exact !important;
          print-color-adjust: exact !important;
        }
        
        body { 
          width: 80mm; 
          margin: 0; 
          padding: 3mm;
          font-size: 11pt !important;  /* ✅ AUMENTADO */
          line-height: 1.4;
        }
        
        .cabecalho { 
          text-align: left; 
          margin-bottom: 6px; 
        }
        
        .cabecalho h1 { 
          font-size: 16pt !important;  /* ✅ AUMENTADO */
          font-weight: 900 !important; 
          margin: 0; 
          letter-spacing: 2px; 
        }
        
        .cnpj, .endereco { 
          font-size: 11pt !important;  /* ✅ AUMENTADO */
          font-weight: 700 !important;
          margin: 2px 0; 
        }
        
        .separador { 
          text-align: center; 
          margin: 5px 0; 
          font-size: 11pt !important;  /* ✅ AUMENTADO */
          font-weight: 700 !important;
        }
        
        .titulo-documento { 
          text-align: left; 
          margin: 6px 0; 
        }
        
        .titulo-documento h2 { 
          font-size: 14pt !important;  /* ✅ AUMENTADO */
          font-weight: 900 !important;
          margin: 0; 
        }
        
        .numero-reserva { 
          font-size: 12pt !important;  /* ✅ AUMENTADO */
          font-weight: 900 !important; 
          margin: 4px 0; 
        }
        
        .data-emissao { 
          font-size: 11pt !important;  /* ✅ AUMENTADO */
          font-weight: 700 !important;
          margin: 2px 0; 
        }
        
        .secao { 
          margin: 6px 0; 
        }
        
        .secao h3 { 
          font-size: 12pt !important;  /* ✅ AUMENTADO */
          font-weight: 900 !important;
          margin: 0 0 5px 0; 
          text-decoration: underline; 
        }
        
        .secao p { 
          margin: 3px 0; 
          font-size: 11pt !important;  /* ✅ AUMENTADO */
          font-weight: 700 !important;
          line-height: 1.4; 
        }
        
        .secao strong {
          font-weight: 900 !important;  /* ✅ EXTRA BOLD */
        }
        
        .linha-valor { 
          display: flex; 
          justify-content: space-between; 
          margin: 4px 0; 
          font-size: 11pt !important;  /* ✅ AUMENTADO */
          font-weight: 700 !important;
        }
        
        .linha-valor.total { 
          font-size: 14pt !important;  /* ✅ AUMENTADO */
          font-weight: 900 !important; 
          margin: 6px 0; 
        }
        
        .declaracao { 
          text-align: left; 
          margin: 10px 0; 
          font-size: 11pt !important;  /* ✅ AUMENTADO */
        }
        
        .declaracao p { 
          margin: 2px 0; 
          font-weight: 700 !important;
        }
        
        .assinatura { 
          margin-top: 12px; 
          text-align: center; 
        }
        
        .linha-assinatura { 
          border-top: 2px solid #000;  /* ✅ LINHA MAIS GROSSA */
          margin: 10mm 12mm 4px 12mm; 
        }
        
        .label-assinatura { 
          font-size: 10pt !important;  /* ✅ AUMENTADO */
          font-weight: 700 !important;
          margin: 2px 0; 
        }
        
        .rodape { 
          text-align: left; 
          margin-top: 10px; 
          font-size: 11pt !important;  /* ✅ AUMENTADO */
        }
        
        .rodape p { 
          margin: 2px 0; 
          font-weight: 700 !important;
        }
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
        <h2>RECIBO DE PAGAMENTO</h2>
        <p class="numero-reserva">Reserva Nº ${this.reserva.id}</p>
        <p class="data-emissao">${this.dataAtualCompleta()}</p>
      </div>

      <div class="separador">================================</div>

      <div class="secao">
        <h3>DADOS DO HOSPEDE</h3>
        <p><strong>Nome:</strong> ${this.reserva.cliente?.nome}</p>
        <p><strong>CPF:</strong> ${this.formatarCPF(this.reserva.cliente?.cpf)}</p>
        <p><strong>Telefone:</strong> ${this.reserva.cliente?.celular || this.reserva.cliente?.telefone || 'Nao informado'}</p>
      </div>

      <div class="separador">- - - - - - - - - - - - - - - -</div>

      <div class="secao">
        <h3>PERIODO DA HOSPEDAGEM</h3>
        <p><strong>Apartamento:</strong> ${this.reserva.apartamento?.numeroApartamento}</p>
        <p><strong>Check-in:</strong> ${this.formatarDataCompleta(this.reserva.dataCheckin)}</p>
        <p><strong>Check-out:</strong> ${this.formatarDataCompleta(this.reserva.dataCheckout)}</p>
        <p><strong>Total de Diarias:</strong> ${this.reserva.quantidadeDiaria} dia(s)</p>
        <p><strong>Hospedes:</strong> ${this.reserva.quantidadeHospede} pessoa(s)</p>
      </div>

      <div class="separador">================================</div>

      <div class="secao">
        <h3>DISCRIMINACAO DE VALORES</h3>
        <div class="linha-valor">
          <span>Diarias (${this.reserva.quantidadeDiaria}x):</span>
          <span>R$ ${this.formatarMoeda(this.reserva.totalDiaria)}</span>
        </div>
        <div class="linha-valor">
          <span>Consumo:</span>
          <span>R$ ${this.formatarMoeda(this.reserva.totalProduto || 0)}</span>
        </div>
        ${(this.reserva.desconto || 0) > 0 ? `
          <div class="linha-valor">
            <span>Desconto:</span>
            <span>- R$ ${this.formatarMoeda(this.reserva.desconto)}</span>
          </div>
        ` : ''}
        <div class="separador">================================</div>
        <div class="linha-valor total">
          <span>TOTAL PAGO:</span>
          <span>R$ ${this.formatarMoeda(totalComDesconto)}</span>
        </div>
      </div>

      <div class="separador">================================</div>

      <div class="declaracao">
        <p>Recebi(emos) de ${this.reserva.cliente?.nome}</p>
        <p>a importancia de <strong>R$ ${this.formatarMoeda(totalComDesconto)}</strong></p>
        <p>referente a hospedagem no periodo citado.</p>
      </div>

      <div class="assinatura">
        <div class="linha-assinatura"></div>
        <p class="label-assinatura">Hotel Di Van</p>
        <p class="label-assinatura">Data: ${this.dataAtualSimples()}</p>
      </div>

      <div class="rodape">
        <p>Obrigado pela preferencia!</p>
        <p>Volte sempre!</p>
      </div>

      <script>
        window.onload = function() {
          window.print();
          window.onafterprint = function() {
            window.close();
          };
        };
      </script>
    </body>
    </html>
  `;

  const janelaImpressao = window.open('', '_blank', 'width=800,height=600');
  if (janelaImpressao) {
    janelaImpressao.document.write(htmlImpressao);
    janelaImpressao.document.close();
  }
}
  gerarFatura(): void {
    if (!this.reserva) return;

    const htmlImpressao = `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
        <title>Fatura - Reserva #${this.reserva.id}</title>
        <style>
          @page { size: 80mm auto; margin: 0; }
          body { 
            font-family: 'Courier New', monospace; 
            font-size: 10px; 
            width: 80mm; 
            margin: 0; 
            padding: 5mm;
          }
          .cabecalho { text-align: left; margin-bottom: 8px; }
          .cabecalho h1 { font-size: 14px; margin: 0; letter-spacing: 2px; }
          .cnpj, .endereco { font-size: 9px; margin: 2px 0; }
          .separador { text-align: center; margin: 6px 0; font-size: 10px; }
          .titulo-documento { text-align: left; margin: 8px 0; }
          .titulo-documento h2 { font-size: 11px; margin: 0; }
          .numero-reserva { font-size: 10px; font-weight: bold; margin: 4px 0; }
          .data-emissao { font-size: 8px; margin: 2px 0; }
          .secao { margin: 8px 0; }
          .secao h3 { font-size: 10px; margin: 0 0 6px 0; text-decoration: underline; }
          .secao p { margin: 3px 0; font-size: 9px; line-height: 1.3; }
          .linha-valor { display: flex; justify-content: space-between; margin: 4px 0; font-size: 9px; }
          .linha-valor.subtotal { font-weight: bold; margin-top: 6px; }
          .linha-valor.total { font-size: 11px; font-weight: bold; margin: 6px 0; }
          .declaracao { text-align: left; margin: 12px 0; font-size: 9px; }
          .declaracao p { margin: 2px 0; }
          .assinatura { margin-top: 15px; text-align: center; }
          .linha-assinatura { border-top: 1px solid #000; margin: 12px 15px 4px 15px; }
          .label-assinatura { font-size: 8px; margin: 2px 0; }
          .rodape { text-align: left; margin-top: 12px; font-size: 9px; }
          .rodape p { margin: 2px 0; }
          .destaque-apagar { background: #000; color: #fff; padding: 6px; text-align: center; margin: 8px 0; }
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
          <h2>FATURA - PAGAMENTO FATURADO</h2>
          <p class="numero-reserva">Reserva Nº ${this.reserva.id}</p>
          <p class="data-emissao">${this.dataAtualCompleta()}</p>
        </div>

        <div class="separador">================================</div>

        <div class="secao">
          <h3>DADOS DO HOSPEDE</h3>
          <p><strong>Nome:</strong> ${this.reserva.cliente?.nome}</p>
          <p><strong>CPF:</strong> ${this.formatarCPF(this.reserva.cliente?.cpf)}</p>
          <p><strong>Telefone:</strong> ${this.reserva.cliente?.celular || this.reserva.cliente?.telefone || 'Nao informado'}</p>
        </div>

        <div class="separador">- - - - - - - - - - - - - - - -</div>

        <div class="secao">
          <h3>PERIODO DA HOSPEDAGEM</h3>
          <p><strong>Apartamento:</strong> ${this.reserva.apartamento?.numeroApartamento}</p>
          <p><strong>Check-in:</strong> ${this.formatarDataCompleta(this.reserva.dataCheckin)}</p>
          <p><strong>Check-out:</strong> ${this.formatarDataCompleta(this.reserva.dataCheckout)}</p>
          <p><strong>Total de Diarias:</strong> ${this.reserva.quantidadeDiaria} dia(s)</p>
          <p><strong>Hospedes:</strong> ${this.reserva.quantidadeHospede} pessoa(s)</p>
        </div>

        <div class="separador">================================</div>

        <div class="secao">
          <h3>DISCRIMINACAO DE VALORES</h3>
          <div class="linha-valor">
            <span>Diarias (${this.reserva.quantidadeDiaria}x):</span>
            <span>R$ ${this.formatarMoeda(this.reserva.totalDiaria)}</span>
          </div>
          <div class="linha-valor">
            <span>Consumo:</span>
            <span>R$ ${this.formatarMoeda(this.reserva.totalProduto || 0)}</span>
          </div>
          ${(this.reserva.desconto || 0) > 0 ? `
            <div class="linha-valor">
              <span>Desconto:</span>
              <span>- R$ ${this.formatarMoeda(this.reserva.desconto)}</span>
            </div>
          ` : ''}
          <div class="separador">- - - - - - - - - - - - - - - -</div>
          <div class="linha-valor subtotal">
            <span>Subtotal:</span>
            <span>R$ ${this.formatarMoeda(this.reserva.totalHospedagem)}</span>
          </div>
          ${(this.reserva.totalRecebido || 0) > 0 ? `
            <div class="linha-valor">
              <span>Ja Recebido:</span>
              <span>- R$ ${this.formatarMoeda(this.reserva.totalRecebido)}</span>
            </div>
          ` : ''}
          <div class="separador">================================</div>
          <div class="linha-valor total">
            <span>VALOR A PAGAR:</span>
            <span>R$ ${this.formatarMoeda(this.reserva.totalApagar)}</span>
          </div>
        </div>

        <div class="destaque-apagar">
          <p style="margin: 0; font-size: 10px; font-weight: bold;">
            VALOR A PAGAR: R$ ${this.formatarMoeda(this.reserva.totalApagar)}
          </p>
        </div>

        <div class="separador">================================</div>

        <div class="declaracao">
          <p>O Sr(a). ${this.reserva.cliente?.nome}</p>
          <p>devera pagar a importancia de</p>
          <p><strong>R$ ${this.formatarMoeda(this.reserva.totalApagar)}</strong></p>
          <p>referente a hospedagem no periodo citado.</p>
        </div>

        <div class="assinatura">
          ${this.assinaturaCapturada ? `
            <img src="${this.assinaturaCapturada}" class="imagem-assinatura" alt="Assinatura">
          ` : `
            <div class="linha-assinatura"></div>
          `}
          <p class="label-assinatura">Assinatura do Hospede</p>
          <div class="linha-assinatura"></div>
          <p class="label-assinatura">Hotel Di Van</p>
          <p class="label-assinatura">Data: ${this.dataAtualSimples()}</p>
        </div>

        <div class="rodape">
          <p>Esta fatura devera ser paga</p>
          <p>conforme acordado.</p>
        </div>

        <script>
          window.onload = function() {
            window.print();
            window.onafterprint = function() {
              window.close();
            };
          };
        </script>
      </body>
      </html>
    `;

    const janelaImpressao = window.open('', '_blank', 'width=800,height=600');
    if (janelaImpressao) {
      janelaImpressao.document.write(htmlImpressao);
      janelaImpressao.document.close();
    }
  }

  // ============= ALTERAR CHECKOUT =============
  abrirModalAlterarCheckout(): void {
    if (!this.reserva) return;
    
    const dataCheckout = new Date(this.reserva.dataCheckout);
    this.novaDataCheckout = dataCheckout.toISOString().split('T')[0];
    this.motivoAlteracaoCheckout = '';
    this.modalAlterarCheckout = true;
  }

  fecharModalAlterarCheckout(): void {
    this.modalAlterarCheckout = false;
  }

  obterDataMinimaCheckout(): string {
    if (!this.reserva) return '';
    const dataCheckin = new Date(this.reserva.dataCheckin);
    dataCheckin.setDate(dataCheckin.getDate() + 1);
    return dataCheckin.toISOString().split('T')[0];
  }

  confirmarAlteracaoCheckout(): void {
  if (!this.reserva) {
    alert('❌ Reserva não encontrada');
    return;
  }

  if (!this.novaDataCheckout) {
    alert('⚠️ Informe a nova data de check-out');
    return;
  }

  // Validar se a nova data é posterior à atual
  const dataAtual = new Date(this.reserva.dataCheckout);
  const novaData = new Date(this.novaDataCheckout);
  
  if (novaData <= dataAtual) {
    alert('⚠️ A nova data deve ser posterior ao checkout atual');
    return;
  }

  console.log('📤 Enviando prorrogação:', {
    reservaId: this.reserva.id,
    novaDataCheckout: this.novaDataCheckout + 'T13:00:00',
    motivo: this.motivoAlteracaoCheckout
  });

  // ✅ USAR O MÉTODO DO SERVIÇO
  this.reservaService.prorrogarCheckout(
    this.reserva.id,
    this.novaDataCheckout + 'T13:00:00',
    this.motivoAlteracaoCheckout || 'Prorrogação solicitada'
  ).subscribe({
    next: (response) => {
      console.log('✅ Prorrogação realizada:', response);
      alert(`✅ Check-out prorrogado para ${this.formatarData(this.novaDataCheckout)}!`);
      this.fecharModalAlterarCheckout();
      this.carregarReserva(this.reserva!.id);
    },
    error: (err: any) => {
      console.error('❌ Erro completo:', err);
      
      // ✅ CAPTURAR MENSAGEM DE ERRO DO BACKEND
      let mensagemErro = 'Erro ao prorrogar checkout';
      
      if (err.error) {
        // Se for string
        if (typeof err.error === 'string') {
          mensagemErro = err.error;
        }
        // Se for objeto com message
        else if (err.error.message) {
          mensagemErro = err.error.message;
        }
        // Se for objeto com erro
        else if (err.error.erro) {
          mensagemErro = err.error.erro;
        }
      }
      
      // ✅ EXIBIR ERRO COM FORMATAÇÃO
      alert(`❌ ERRO AO PRORROGAR\n\n${mensagemErro}`);
    }
  });
}

  // ============= ALTERAR HÓSPEDES =============
  abrirModalAlterarHospedes(): void {
    if (!this.reserva) return;
    
    this.novaQuantidadeHospedes = this.reserva.quantidadeHospede;
    this.motivoAlteracaoHospedes = '';
    this.modalAlterarHospedes = true;
  }

  fecharModalAlterarHospedes(): void {
    this.modalAlterarHospedes = false;
  }

  confirmarAlteracaoHospedes(): void {
    if (!this.reserva) return;

    if (this.novaQuantidadeHospedes < 1) {
      alert('⚠️ Quantidade inválida');
      return;
    }

    if (this.novaQuantidadeHospedes > (this.reserva.apartamento?.capacidade || 0)) {
      alert('⚠️ Quantidade excede a capacidade do apartamento');
      return;
    }

    const params: any = {
      quantidade: this.novaQuantidadeHospedes
    };

    if (this.motivoAlteracaoHospedes.trim()) {
      params.motivo = this.motivoAlteracaoHospedes;
    }

    this.http.patch(`http://localhost:8080/api/reservas/${this.reserva.id}/alterar-hospedes`, null, { params }).subscribe({
      next: () => {
        alert('✅ Quantidade de hóspedes alterada com sucesso!');
        this.fecharModalAlterarHospedes();
        if (this.reserva) {
          this.carregarReserva(this.reserva.id);
        }
      },
      error: (err: any) => {
        alert('❌ Erro: ' + (err.error || err.message));
      }
    });
  }

  // ============= PAGAMENTO =============
  abrirModalPagamento(): void {
    if (!this.reserva) return;
    this.pagValor = Number(this.reserva.totalApagar);
    this.pagFormaPagamento = '';
    this.pagObs = '';
    this.modalPagamento = true;
  }

  fecharModalPagamento(): void {
    this.modalPagamento = false;
  }

  salvarPagamento(): void {
    console.log('═══════════════════════════════════════════');
    console.log('💰 INICIANDO SALVAMENTO DE PAGAMENTO');
    console.log('═══════════════════════════════════════════');
    
    if (!this.reserva) {
      console.error('❌ Reserva não encontrada');
      alert('Reserva não encontrada');
      return;
    }

    if (!this.pagFormaPagamento) {
      console.error('❌ Forma de pagamento não selecionada');
      alert('Selecione uma forma de pagamento');
      return;
    }

    if (this.pagValor <= 0) {
      console.error('❌ Valor inválido:', this.pagValor);
      alert('Valor inválido');
      return;
    }

    if (this.pagValor > (this.reserva.totalApagar || 0)) {
      console.error('❌ Valor maior que saldo:', this.pagValor, '>', this.reserva.totalApagar);
      alert(`Valor maior que saldo (R$ ${(this.reserva.totalApagar || 0).toFixed(2)})`);
      return;
    }

    const usuarioId = this.authService.getUsuarioId();
    console.log('👤 Usuario ID:', usuarioId);

    const dto = {
      reservaId: this.reserva.id,
      valor: this.pagValor,
      formaPagamento: this.pagFormaPagamento,
      observacao: this.pagObs || undefined
    };

    console.log('📦 DTO preparado:', dto);

    const url = `http://localhost:8080/api/pagamentos?usuarioId=${usuarioId}`;
    console.log('🌐 URL da requisição:', url);

    this.http.post(url, dto).subscribe({
      next: (response: any) => {
        console.log('═══════════════════════════════════════════');
        console.log('✅ RESPOSTA DO BACKEND:');
        console.log('═══════════════════════════════════════════');
        console.log('📊 Response completo:', response);
        console.log('✔️ Sucesso:', response.sucesso);
        console.log('💰 Pagamento:', response.pagamento);
        
        if (response.sucesso || response.pagamento) {
          alert('✅ Pagamento registrado com sucesso!');
          this.fecharModalPagamento();
          if (this.reserva) {
            this.carregarReserva(this.reserva.id);
          }
        } else {
          alert('⚠️ ' + (response.mensagem || 'Pagamento registrado, mas sem confirmação'));
        }
      },
      error: (err: any) => {
        console.log('═══════════════════════════════════════════');
        console.error('❌ ERRO AO REGISTRAR PAGAMENTO');
        console.log('═══════════════════════════════════════════');
        console.error('📊 Erro completo:', err);
        console.error('🔢 Status Code:', err.status);
        console.error('📝 Status Text:', err.statusText);
        console.error('🌐 URL:', err.url);
        console.error('📋 Headers:', err.headers);
        
        if (err.error) {
          console.error('💥 Corpo do erro (error):', err.error);
          console.error('📄 Tipo do erro:', typeof err.error);
          
          if (typeof err.error === 'object') {
            console.error('🔍 Erro em JSON:', JSON.stringify(err.error, null, 2));
          }
        }
        
        console.error('📨 Message:', err.message);
        console.log('═══════════════════════════════════════════');
        
        let mensagemErro = 'Erro ao registrar pagamento';
        
        if (err.status === 403 && err.error?.tipo === 'CAIXA_FECHADO') {
          alert('⚠️ CAIXA FECHADO!\n\n' + err.error.erro + '\n\nAbra o caixa antes de fazer pagamentos.');
          return;
        }
        
        if (err.status === 0) {
          alert('❌ Erro de conexão!\n\nVerifique se o backend está rodando em http://localhost:8080');
          return;
        }
        
        if (err.status === 401) {
          alert('⚠️ Não autorizado!\n\nFaça login novamente.');
          this.router.navigate(['/login']);
          return;
        }
        
        if (err.error) {
          if (typeof err.error === 'string') {
            mensagemErro = err.error;
          } else if (err.error.message) {
            mensagemErro = err.error.message;
          } else if (err.error.erro) {
            mensagemErro = err.error.erro;
          }
        }
        
        alert('❌ Erro: ' + mensagemErro);
      }
    });
  }

  // ============= CONSUMO =============
  abrirModalConsumo(): void {
    this.carregarProdutosDisponiveis();
    this.produtoSelecionadoId = 0;
    this.quantidadeConsumo = 1;
    this.observacaoConsumo = '';
    this.modalConsumo = true;
  }

  fecharModalConsumo(): void {
    this.modalConsumo = false;
  }

  carregarProdutosDisponiveis(): void {
    this.http.get<Produto[]>('http://localhost:8080/api/produtos').subscribe({
      next: (data) => {
        this.produtos = data.filter(p => p.quantidade > 0);
        if (this.produtos.length === 0) {
          alert('⚠️ Nenhum produto disponível!');
        }
      },
      error: (err: any) => {
        console.error('❌ Erro:', err);
        alert('Erro ao carregar produtos');
      }
    });
  }
  
  calcularTotalDescontos(): string {
    if (!this.reserva?.descontos || this.reserva.descontos.length === 0) {
      return '0,00';
    }
    const total = this.reserva.descontos.reduce((sum, desc) => sum + desc.valor, 0);
    return this.formatarMoeda(total);
  }

  // ============= TRANSFERÊNCIA =============
  abrirModalTransferencia(): void {
    if (!this.reserva) return;

    this.http.get<Apartamento[]>('http://localhost:8080/api/apartamentos').subscribe({
      next: (data) => {
        this.apartamentosDisponiveis = data.filter(
          (apt: Apartamento) => 
            apt.id !== this.reserva?.apartamento?.id &&
            apt.capacidade >= (this.reserva?.quantidadeHospede || 0)
        );

        if (this.apartamentosDisponiveis.length === 0) {
          alert('⚠️ Nenhum apartamento disponível para transferência');
          return;
        }

        this.novoApartamentoId = 0;
        this.dataTransferencia = '';
        this.transferenciaImediata = true;
        this.motivoTransferencia = '';
        this.modalTransferencia = true;
      },
      error: (err: any) => {
        console.error('❌ Erro:', err);
        alert('Erro ao carregar apartamentos disponíveis');
      }
    });
  }

  fecharModalTransferencia(): void {
    this.modalTransferencia = false;
  }

  confirmarTransferencia(): void {
    if (!this.reserva) {
      alert('Reserva não encontrada');
      return;
    }

    if (this.novoApartamentoId === 0) {
      alert('⚠️ Selecione um apartamento');
      return;
    }

    if (!this.transferenciaImediata && !this.dataTransferencia) {
      alert('⚠️ Informe a data da transferência');
      return;
    }

    if (!this.motivoTransferencia.trim()) {
      alert('⚠️ Informe o motivo da transferência');
      return;
    }

    const confirmacao = confirm(
      this.transferenciaImediata
        ? '⚠️ Confirma transferência IMEDIATA de apartamento?'
        : `⚠️ Confirma transferência para o dia ${this.dataTransferencia}?`
    );

    if (!confirmacao) return;

    const dto = {
      reservaId: this.reserva.id,
      novoApartamentoId: this.novoApartamentoId,
      dataTransferencia: this.transferenciaImediata ? null : this.dataTransferencia + 'T00:00:00',
      motivo: this.motivoTransferencia
    };

    this.http.post('http://localhost:8080/api/reservas/transferir-apartamento', dto).subscribe({
      next: () => {
        alert('✅ Transferência realizada com sucesso!');
        this.fecharModalTransferencia();
        if (this.reserva) {
          this.carregarReserva(this.reserva.id);
        }
      },
      error: (err: any) => {
        console.error('❌ Erro:', err);
        alert('❌ Erro: ' + (err.error?.erro || err.error || err.message));
      }
    });
  }

  obterNomeTipoApartamento(apt: Apartamento): string {
    return apt.tipoApartamento?.tipo || apt.tipoApartamentoNome || 'Sem tipo';
  }

  obterDataMinima(): string {
    const amanha = new Date();
    amanha.setDate(amanha.getDate() + 1);
    return amanha.toISOString().split('T')[0];
  }

  // ============= FINALIZAR / CANCELAR =============
 finalizarCheckout(): void {
  if (!this.reserva) return;

  const temSaldo = (this.reserva.totalApagar || 0) > 0;
  
  if (temSaldo) {
    this.finalizarReservaFaturada();  // ✅ Chama FATURADA
  } else {
    this.finalizarReservaPaga();      // ✅ Chama PAGA
  }
 
}

  finalizarReservaFaturada(): void {
  if (!this.reserva) return;
  
  const saldoDevedor = this.reserva.totalApagar || 0;
  const cliente = this.reserva.cliente;
  
  // ✅ VERIFICAR CRÉDITO ANTES DE TENTAR FINALIZAR
  if (!cliente?.creditoAprovado) {
    alert(
      `❌ CLIENTE SEM APROVAÇÃO DE CRÉDITO!\n\n` +
      `Cliente: ${cliente?.nome}\n` +
      `Saldo devedor: R$ ${this.formatarMoeda(saldoDevedor)}\n\n` +
      `Para finalizar esta reserva:\n` +
      `1️⃣ Receba o pagamento de R$ ${this.formatarMoeda(saldoDevedor)}, OU\n` +
      `2️⃣ Aprove crédito do cliente no cadastro\n\n` +
      `Apenas clientes com crédito aprovado podem ter reservas faturadas.`
    );
    return;
  }
  
  const confirmacao = confirm(
    `⚠️ FINALIZAR FATURADA?\n\n` +
    `Cliente: ${cliente?.nome}\n` +
    `Saldo devedor: R$ ${this.formatarMoeda(saldoDevedor)}\n\n` +
    `✅ Reserva será finalizada\n` +
    `🧹 Apartamento irá para LIMPEZA\n` +
    `💳 Valor será enviado para Contas a Receber`
  );
  if (!confirmacao) return;
  
  // ✅ ABRIR MODAL DE ASSINATURA (ao invés de finalizar diretamente)
  this.modalAssinatura = true;
  this.assinaturaCapturada = null;
}

// ✅ CHAMADO PELO MODAL APÓS ASSINAR
confirmarAssinaturaFaturada(): void {
  if (!this.signaturePad || !this.signaturePad.temAssinatura) {
    alert('⚠️ Por favor, assine antes de confirmar!');
    return;
  }

  this.assinaturaCapturada = this.signaturePad.obterAssinatura();

  if (!this.assinaturaCapturada) {
    alert('⚠️ Erro ao capturar assinatura. Tente novamente.');
    return;
  }

  this.modalAssinatura = false;

  // ✅ FINALIZAR RESERVA NO BACKEND
  this.http.patch(`http://localhost:8080/api/reservas/${this.reserva!.id}/finalizar`, {}).subscribe({
    next: () => {
      // ✅ SALVAR ASSINATURA
      this.salvarAssinatura();

      // ✅ NOTIFICAR SIDEBAR
      this.alertasStateService.notificarAlertasAtualizados();

      alert('✅ Reserva finalizada! Valor enviado para Contas a Receber.');

      this.carregarReserva(this.reserva!.id);

      // ✅ IMPRIMIR FATURA COM ASSINATURA
      setTimeout(() => {
        this.gerarFatura();
      }, 800);
    },
    error: (err: any) => {
      let mensagemErro = 'Erro ao finalizar reserva';
      
      if (err.error && err.error.erro) {
        mensagemErro = err.error.erro;
      } else if (err.error && err.error.message) {
        mensagemErro = err.error.message;
      }
      
      alert(mensagemErro);
    }
  });
}

// ✅ SALVAR ASSINATURA NO BACKEND
private salvarAssinatura(): void {
  if (!this.assinaturaCapturada || !this.reserva) return;

  this.http.post(`http://localhost:8080/api/reservas/${this.reserva.id}/assinatura`, {
    assinatura: this.assinaturaCapturada
  }).subscribe({
    next: () => console.log('✅ Assinatura salva'),
    error: (err) => console.error('❌ Erro ao salvar assinatura:', err)
  });
}

// ✅ CANCELAR MODAL DE ASSINATURA
cancelarAssinatura(): void {
  this.modalAssinatura = false;
  this.assinaturaCapturada = null;
}

  finalizarReservaPaga(): void {
  if (!this.reserva) return;
  
  const confirmacao = confirm(
    `💚 FINALIZAR PAGA?\n\n` +
    `Total: R$ ${this.formatarMoeda(this.reserva.totalHospedagem)}\n` +
    `Recebido: R$ ${this.formatarMoeda(this.reserva.totalRecebido)}\n\n` +
    `✅ Reserva será finalizada\n` +
    `🧹 Apartamento irá para LIMPEZA`
  );
  if (!confirmacao) return;
  
  this.http.patch(`http://localhost:8080/api/reservas/${this.reserva.id}/finalizar-paga`, {}).subscribe({
    next: () => {
      alert('✅ Reserva finalizada com sucesso!');
      
      // ✅ NOTIFICAR SIDEBAR
      this.alertasStateService.notificarAlertasAtualizados();
      
      this.carregarReserva(this.reserva!.id);
      
      setTimeout(() => {
        const imprimir = confirm('📄 Deseja imprimir o RECIBO agora?');
        if (imprimir) {
          this.gerarRecibo();
        }
      }, 500);
    },
    error: (err: any) => {
      alert('❌ Erro: ' + (err.error?.erro || err.error?.message || err.message));
    }
  });
}

  realizarCheckOut(): void {
    if (!confirm('🚪 Confirma o CHECK-OUT desta reserva?\n\nTodos os hóspedes serão marcados como checkout realizado.')) {
      return;
    }

    this.loading = true;

    this.http.post(`${this.apiUrl}/reservas/${this.reservaId}/checkout`, {})
      .subscribe({
        next: (response: any) => {
          console.log('✅ Check-out realizado:', response);
          alert('✅ Check-out realizado com sucesso!');
          
          // ✅ RECARREGAR DADOS
          this.buscarDetalhes();
        },
        error: (err) => {
          console.error('❌ Erro no checkout:', err);
          
          const mensagemErro = err.error?.erro || err.error?.mensagem || err.message || 'Erro desconhecido';
          
          alert('❌ Erro ao realizar checkout:\n\n' + mensagemErro);
          this.loading = false;
        }
      });
  }

  cancelarReserva(): void {
    if (!this.reserva) return;

    const motivo = prompt('❌ Informe o motivo do cancelamento:');
    if (!motivo || motivo.trim() === '') {
      alert('⚠️ Motivo é obrigatório!');
      return;
    }

    const confirmacao = confirm(`❌ Confirma o cancelamento da reserva?\n\nMotivo: ${motivo}\n\nO apartamento será liberado.`);
    if (!confirmacao) return;

    this.http.patch(`http://localhost:8080/api/reservas/${this.reserva.id}/cancelar`, null, { params: { motivo } }).subscribe({
      next: () => {
        alert('✅ Reserva cancelada!');
        this.alertasStateService.notificarAlertasAtualizados();

        this.carregarReserva(this.reserva!.id);
      },
      error: (err: any) => {
        alert('❌ Erro: ' + (err.error?.message || err.message));
      }
      
    });
    
  }

  salvarConsumo(): void {
  console.log('🎯🎯🎯 FRONTEND - salvarConsumo() CHAMADO! 🎯🎯🎯');
  
  if (!this.reserva) {
    alert('Reserva não encontrada');
    return;
  }

  if (this.produtoSelecionadoId === 0) {
    alert('⚠️ Selecione um produto');
    return;
  }

  if (this.quantidadeConsumo <= 0) {
    alert('⚠️ Quantidade inválida');
    return;
  }

  const dto = {
    produtoId: this.produtoSelecionadoId,
    quantidade: this.quantidadeConsumo,
    observacao: this.observacaoConsumo
  };

  const url = `http://localhost:8080/api/reservas/${this.reserva.id}/consumo`;
  
  console.log('📤 FRONTEND - Enviando requisição:');
  console.log('   URL:', url);
  console.log('   Payload:', dto);
  console.log('═══════════════════════════════════════════');

  this.http.post(url, dto).subscribe({
    next: (response) => {
      console.log('✅ FRONTEND - Produto adicionado com sucesso:', response);
      alert('✅ Produto adicionado ao consumo!');
      this.fecharModalConsumo();
      if (this.reserva) {
        this.carregarReserva(this.reserva.id);
      }
    },
    error: (err: any) => {
      console.error('❌ FRONTEND - Erro na requisição:', err);
      alert('❌ Erro: ' + (err.error?.erro || err.error?.message || err.message));
    }
  });
}

  // ============= ESTORNO =============
  abrirModalEstorno(extrato: any): void {
    this.extratoParaEstornar = extrato;
    this.motivoEstorno = '';
    this.criarLancamentoCorreto = false;
    this.produtoCorretoId = 0;
    this.quantidadeCorreta = 1;
    
    this.carregarProdutosDisponiveis();
    
    this.modalEstorno = true;
  }

  fecharModalEstorno(): void {
    this.modalEstorno = false;
    this.extratoParaEstornar = null;
  }

  confirmarEstorno(): void {
    if (!this.extratoParaEstornar) {
      alert('⚠️ Erro: Lançamento não encontrado');
      return;
    }

    if (!this.motivoEstorno || this.motivoEstorno.trim() === '') {
      alert('⚠️ Motivo do estorno é obrigatório');
      return;
    }

    if (this.criarLancamentoCorreto) {
      if (this.produtoCorretoId === 0) {
        alert('⚠️ Selecione o produto correto');
        return;
      }
      if (this.quantidadeCorreta <= 0) {
        alert('⚠️ Quantidade correta deve ser maior que zero');
        return;
      }
    }

    const confirmacao = confirm(
      `⚠️ CONFIRMA O ESTORNO?\n\n` +
      `Lançamento: ${this.extratoParaEstornar.descricao}\n` +
      `Valor: R$ ${this.formatarMoeda(this.extratoParaEstornar.totalLancamento)}\n` +
      `Motivo: ${this.motivoEstorno}\n\n` +
      `Esta ação será registrada para auditoria.`
    );

    if (!confirmacao) return;

    const request: any = {
      extratoId: this.extratoParaEstornar.id,
      motivo: this.motivoEstorno,
      criarLancamentoCorreto: this.criarLancamentoCorreto
    };

    if (this.criarLancamentoCorreto) {
      request.correcao = {
        produtoId: this.produtoCorretoId,
        quantidade: this.quantidadeCorreta
      };
    }

    this.http.post('http://localhost:8080/api/estornos/consumo-apartamento', request).subscribe({
      next: (response: any) => {
        alert('✅ Estorno realizado com sucesso!');
        this.fecharModalEstorno();
        if (this.reserva) {
          this.carregarReserva(this.reserva.id);
        }
      },
      error: (err: any) => {
        console.error('❌ Erro no estorno:', err);
        alert('❌ Erro: ' + (err.error?.erro || err.error?.message || err.message));
      }
    });
  }

  // ============= DESCONTO =============
  abrirModalDesconto(): void {
    this.valorDesconto = 0;
    this.motivoDesconto = '';
    this.modalDesconto = true;
  }

  fecharModalDesconto(): void {
    this.modalDesconto = false;
  }

  confirmarDesconto(): void {
    if (!this.reserva) {
      alert('⚠️ Reserva não encontrada');
      return;
    }

    if (this.valorDesconto <= 0) {
      alert('⚠️ Valor do desconto deve ser maior que zero');
      return;
    }

    const totalDescontosAtual = this.reserva.descontos ? 
      this.reserva.descontos.reduce((sum, d) => sum + d.valor, 0) : 0;
    
    const novoTotalDescontos = totalDescontosAtual + this.valorDesconto;

    if (novoTotalDescontos > (this.reserva.totalHospedagem || 0)) {
      alert('⚠️ Total de descontos não pode ser maior que o total da hospedagem');
      return;
    }

    const novoTotal = (this.reserva.totalHospedagem || 0) - novoTotalDescontos;
    const novoSaldo = novoTotal - (this.reserva.totalRecebido || 0);

    const confirmacao = confirm(
      `💰 CONFIRMA APLICAÇÃO DO DESCONTO?\n\n` +
      `Valor do Desconto: R$ ${this.formatarMoeda(this.valorDesconto)}\n` +
      `Descontos Atuais: R$ ${this.formatarMoeda(totalDescontosAtual)}\n` +
      `Novo Total de Descontos: R$ ${this.formatarMoeda(novoTotalDescontos)}\n` +
      `Total da Hospedagem: R$ ${this.formatarMoeda(this.reserva.totalHospedagem)}\n` +
      `Novo Saldo: R$ ${this.formatarMoeda(novoSaldo)}\n\n` +
      `${this.motivoDesconto ? 'Motivo: ' + this.motivoDesconto : ''}`
    );

    if (!confirmacao) return;

    const usuarioId = this.authService.getUsuarioId();

    const dto = {
      reservaId: this.reserva.id,
      valor: this.valorDesconto,
      motivo: this.motivoDesconto || 'Desconto aplicado',
      usuarioId: usuarioId
    };

    this.http.post(`http://localhost:8080/api/descontos`, dto).subscribe({
      next: (response: any) => {
        alert('✅ Desconto aplicado com sucesso!');
        this.fecharModalDesconto();
        if (this.reserva) {
          this.carregarReserva(this.reserva.id);
        }
      },
      error: (err: any) => {
        console.error('❌ Erro ao aplicar desconto:', err);
        alert('❌ Erro: ' + (err.error?.erro || err.error?.message || err.message));
      }
    });
  }
  

  removerDesconto(descontoId: number): void {
    if (!this.reserva) return;

    const desc = this.reserva.descontos?.find(d => d.id === descontoId);
    if (!desc) return;

    const confirmacao = confirm(
      `⚠️ CONFIRMA REMOÇÃO DO DESCONTO?\n\n` +
      `Valor: R$ ${this.formatarMoeda(desc.valor)}\n` +
      `Motivo: ${desc.motivo || 'Sem motivo'}\n\n` +
      `Este desconto será removido permanentemente.`
    );

    if (!confirmacao) return;

    const usuarioId = this.authService.getUsuarioId();

    this.http.delete(
      `http://localhost:8080/api/descontos/${descontoId}?usuarioId=${usuarioId}`
    ).subscribe({
      next: (response: any) => {
        alert('✅ Desconto removido com sucesso!');
        if (this.reserva) {
          this.carregarReserva(this.reserva.id);
        }
      },
      error: (err: any) => {
        console.error('❌ Erro ao remover desconto:', err);
        alert('❌ Erro: ' + (err.error?.erro || err.error?.message || err.message));
      }
    });
  }

   // ============================================
  // ✅ ADICIONE TODOS OS MÉTODOS ABAIXO AQUI
  // ============================================

 carregarHospedes(): void {
  this.http.get<any[]>(`http://localhost:8080/api/hospedagem-hospedes/reserva/${this.reserva!.id}`).subscribe({
    next: (data) => {
      // ✅ CARREGAR TODOS OS HÓSPEDES (sem filtrar por status)
      this.hospedes = data;
      
      console.log('✅ Hóspedes carregados:', this.hospedes.length);
      
      // Debug: Mostrar todos os status
      data.forEach(h => {
        console.log(`   - ${h.cliente?.nome || h.nomeCompleto}: ${h.status}`);
      });
    },
    error: (err) => {
      console.error('❌ Erro ao carregar hóspedes:', err);
      this.hospedes = [];
    }
  });
}

  abrirModalAdicionarHospede(): void {

    console.log('🚗 ABRINDO MODAL DE ADICIONAR HÓSPEDE');
  console.log('🚗 novoHospede:', this.novoHospede);
  console.log('🚗 tem placaCarro?', 'placaCarro' in this.novoHospede);

    if (!this.reserva) return;
    
    const capacidade = this.reserva.apartamento?.capacidade || 0;
    const hospedesAtuais = this.reserva.quantidadeHospede || 0;
    
    if (hospedesAtuais >= capacidade) {
      alert(`❌ Capacidade máxima do apartamento já atingida: ${capacidade} hóspede(s)`);
      return;
    }
    
    this.modalAdicionarHospede = true;
    this.modoModalHospede = 'buscar';
    this.termoBuscaHospede = '';
    this.clientesFiltradosModal = [];
    this.limparFormularioNovoHospede();
  }

  fecharModalAdicionarHospede(): void {
    this.modalAdicionarHospede = false;
    this.limparFormularioNovoHospede();
  }

  alternarModoModal(modo: 'buscar' | 'cadastrar'): void {
    this.modoModalHospede = modo;
  }

  buscarClientesModal(): void {
    const busca = this.termoBuscaHospede.trim();
    
    if (busca.length < 2) {
      this.clientesFiltradosModal = [];
      return;
    }

    this.http.get<any[]>(`http://localhost:8080/api/clientes/buscar?termo=${busca}`).subscribe({
      next: (data) => {
        this.clientesFiltradosModal = data;
      },
      error: (err) => {
        console.error('❌ Erro na busca:', err);
        this.clientesFiltradosModal = [];
      }
    });
  }

  selecionarHospedeExistente(cliente: any): void {
    if (!this.reserva) return;
    
    const request = {
      clienteId: cliente.id,
      cadastrarNovo: false
    };
    
    this.http.post(`http://localhost:8080/api/reservas/${this.reserva.id}/hospedes`, request).subscribe({
      next: (response: any) => {
        alert('✅ Hóspede adicionado com sucesso!');
        this.fecharModalAdicionarHospede();
        this.carregarReserva(this.reserva!.id);
      },
      error: (err) => {
        const erro = err.error?.erro || 'Erro ao adicionar hóspede';
        alert(`❌ ${erro}`);
      }
    });
  }

  buscarDetalhes(): void {
    this.loading = true;

    this.http.get(`${this.apiUrl}/reservas/${this.reservaId}/detalhes`)
      .subscribe({
        next: (data: any) => {
          console.log('✅ Detalhes carregados:', data);
          this.reserva = data;
          this.loading = false;
        },
        error: (err) => {
          console.error('❌ Erro ao carregar detalhes:', err);
          alert('Erro ao carregar detalhes da reserva');
          this.loading = false;
        }
      });
  }

  

  salvarNovoHospede(): void {
  if (!this.novoHospede.nome || this.novoHospede.nome.trim() === '') {
    alert('⚠️ Nome completo é obrigatório!');
    return;
  }
  
  // ✅ VALIDAR PLACA SE FOI PREENCHIDA
  if (this.novoHospede.placaCarro && !this.validarPlaca(this.novoHospede.placaCarro)) {
    alert('⚠️ Placa inválida!\n\nFormato correto: ABC-1234 ou ABC-1D23');
    return;
  }
  
  if (!this.reserva) return;
  
  const request = {
    nome: this.novoHospede.nome,
    cpf: this.novoHospede.cpf,
    celular: this.novoHospede.celular,
    placaCarro: this.novoHospede.placaCarro || null,
    cadastrarNovo: true
  };
  
  // ✅ CORRIGIDO: Adicionar ( depois de post
  this.http.post(`http://localhost:8080/api/reservas/${this.reserva.id}/hospedes`, request).subscribe({
    next: (response: any) => {
      alert('✅ Hóspede adicionado com sucesso!');
      this.fecharModalAdicionarHospede();
      this.carregarReserva(this.reserva!.id);
    },
    error: (err) => {
      const erro = err.error?.erro || 'Erro ao adicionar hóspede';
      alert(`❌ ${erro}`);  // ✅ CORRIGIDO: Adicionar ( antes da string
    }
  });
}

  limparFormularioNovoHospede(): void {
    this.novoHospede = {
      nome: '',
      cpf: '',
      celular: '',
      placaCarro: ''
    };
  }
  
  /**
 * 🚗 VALIDAR PLACA BRASILEIRA
 */
validarPlaca(placa: string): boolean {
  if (!placa || placa.trim() === '') return true; // Placa é opcional
  
  // Remove espaços e hífen
  const placaLimpa = placa.replace(/[\s-]/g, '').toUpperCase();
  
  // Formato antigo: ABC1234 ou ABC-1234
  const padraoAntigo = /^[A-Z]{3}[0-9]{4}$/;
  
  // Formato Mercosul: ABC1D23 ou ABC-1D23
  const padraoMercosul = /^[A-Z]{3}[0-9][A-Z][0-9]{2}$/;
  
  return padraoAntigo.test(placaLimpa) || padraoMercosul.test(placaLimpa);
}

/**
 * 🚗 FORMATAR PLACA AUTOMATICAMENTE (ABC-1234)
 */
formatarPlaca(): void {
  if (!this.novoHospede.placaCarro) return;
  
  let placa = this.novoHospede.placaCarro.toUpperCase().replace(/[^A-Z0-9]/g, '');
  
  if (placa.length > 3) {
    placa = placa.substring(0, 3) + '-' + placa.substring(3, 7);
  }
  
  this.novoHospede.placaCarro = placa;
}

 confirmarRemocaoHospede(hospede: any): void {
  if (!this.reserva) return;
  
  const nomeHospede = hospede.cliente?.nome || hospede.nomeCompleto;
  
  // ✅ VERIFICAR SE É O ÚLTIMO HÓSPEDE
  const totalHospedes = this.hospedes.filter(h => 
    h.status !== 'CHECKOUT_REALIZADO'
  ).length;
  
  if (totalHospedes === 1) {
    alert('❌ NÃO É POSSÍVEL FAZER CHECKOUT DO ÚLTIMO HÓSPEDE!\n\nPara finalizar a reserva, use o botão "Finalizar Paga" ou "Finalizar Faturada".');
    return;
  }
  
  // ✅ ABRIR MODAL DE CHECKOUT PARCIAL
  this.hospedeParaCheckout = hospede;
  this.motivoCheckoutParcial = hospede.titular 
    ? 'Checkout do titular - próximo hóspede será promovido'
    : `Checkout de ${nomeHospede}`;
  this.modalCheckoutParcial = true;
}

  // ═══════════════════════════════════════════════════════════════
// ✨ NOVOS MÉTODOS - CHECKOUT PARCIAL DE HÓSPEDE
// ═══════════════════════════════════════════════════════════════

fecharModalCheckoutParcial(): void {
  this.modalCheckoutParcial = false;
  this.hospedeParaCheckout = null;
  this.motivoCheckoutParcial = '';
}

confirmarCheckoutParcial(): void {
  if (!this.reserva || !this.hospedeParaCheckout) return;
  
  if (!this.motivoCheckoutParcial || this.motivoCheckoutParcial.trim() === '') {
    alert('⚠️ Informe o motivo do checkout');
    return;
  }
  
  const dto = {
    hospedagemHospedeId: this.hospedeParaCheckout.id,
    dataHoraSaida: null, // null = agora
    motivo: this.motivoCheckoutParcial
  };
  
  console.log('🚪 Enviando checkout parcial:', dto);
  
  this.reservaService.checkoutParcial(this.reserva.id, dto).subscribe({
    next: (response: any) => {
      console.log('✅ Checkout parcial realizado:', response);
      
      const mensagem = this.hospedeParaCheckout.titular
        ? '✅ Checkout do titular realizado!\n\nO próximo hóspede foi promovido a titular.'
        : '✅ Checkout realizado com sucesso!';
      
      alert(mensagem + '\n\nQuantidade de hóspedes atualizada: ' + response.novaQuantidadeHospedes);
      
      this.fecharModalCheckoutParcial();
      this.carregarReserva(this.reserva!.id);
    },
    error: (err: any) => {
      console.error('❌ Erro no checkout parcial:', err);
      const erro = err.error?.erro || err.error?.message || err.message || 'Erro desconhecido';
      alert('❌ Erro ao fazer checkout: ' + erro);
    }
  });
}

abrirModalTransferirHospede(hospede: any): void {
  if (!hospede) {
    alert('❌ Hóspede não encontrado');
    return;
  }
  
  // ✅ VALIDAR SE A RESERVA EXISTE
  if (!this.reserva) {
    alert('❌ Reserva não carregada');
    return;
  }

  // ✅ VALIDAR SE O APARTAMENTO EXISTE
  if (!this.reserva.apartamento) {
    alert('❌ Apartamento da reserva não encontrado');
    return;
  }
  
  if (!this.modalTransferirHospede) {
    console.error('❌ Modal não carregado ainda');
    alert('❌ Erro: Aguarde a página carregar completamente');
    return;
  }
  
  console.log('🔄 Abrindo modal para transferir:', hospede.cliente?.nome || hospede.nomeCompleto);
  
  // ✅ AGORA TEMOS CERTEZA QUE NÃO É UNDEFINED
  const hospedeFormatado = {
    id: hospede.id,
    cliente: {
      id: hospede.cliente?.id || hospede.clienteId,
      nome: hospede.cliente?.nome || hospede.nomeCompleto
    },
    status: hospede.status,
    titular: hospede.titular,
    reserva: {
      id: this.reserva.id,  // ✅ Agora é number, não number | undefined
      apartamento: {
        id: this.reserva.apartamento.id,  // ✅ Agora é number
        numeroApartamento: this.reserva.apartamento.numeroApartamento  // ✅ Agora é string
      }
    }
  };
  
  this.modalTransferirHospede.abrir(hospedeFormatado);
}

obterStatusHospede(hospede: any): string {
  if (hospede.status === 'CHECKOUT_REALIZADO') {
    return '🚪 Checkout Realizado';
  }
  if (hospede.status === 'HOSPEDADO') {
    return hospede.titular ? '🏠 Hospedado (Titular)' : '🏠 Hospedado';
  }
  return hospede.status || 'Desconhecido';
}

// ✅ CONFIRMAR ESTORNO DE DESCONTO DO EXTRATO
  confirmarEstornoDescontoDoExtrato(extrato: any): void {
    const valorDesconto = Math.abs(extrato.valorUnitario);
    
    if (confirm(`Deseja realmente estornar este desconto de R$ ${this.formatarMoeda(valorDesconto)}?\n\n${extrato.descricao}`)) {
      this.estornoDescontoDoExtrato(extrato);
    }
  }

  // ✅ ESTORNAR DESCONTO DO EXTRATO
estornoDescontoDoExtrato(extrato: any): void {
  // ✅ PEGAR usuarioId DO AuthService, não do localStorage
  const usuarioId = this.authService.getUsuarioId();
  
  if (!usuarioId) {
    alert('Usuário não identificado!');
    return;
  }

  if (!this.reserva) {
    alert('Reserva não carregada!');
    return;
  }

  // Buscar o desconto pela reserva e encontrar pelo valor e data
  this.http.get<any[]>(`http://localhost:8080/api/descontos/reserva/${this.reserva.id}`)
    .subscribe({
      next: (descontos) => {
        // Encontrar o desconto pelo valor e data próxima
        const desconto = descontos.find(d => {
          const diff = Math.abs(new Date(d.dataHoraDesconto).getTime() - new Date(extrato.dataHoraLancamento).getTime());
          return Math.abs(d.valor - Math.abs(extrato.valorUnitario)) < 0.01 && diff < 5000;
        });
        
        if (desconto) {
          this.estornarDesconto(desconto.id);
        } else {
          alert('Desconto não encontrado para estorno!');
        }
      },
      error: (error) => {
        console.error('Erro ao buscar descontos:', error);
        alert('Erro ao buscar desconto!');
      }
    });
}

// ✅ ESTORNAR DESCONTO
estornarDesconto(descontoId: number): void {
  // ✅ PEGAR usuarioId DO AuthService
  const usuarioId = this.authService.getUsuarioId();

  this.http.delete(`http://localhost:8080/api/descontos/${descontoId}?usuarioId=${usuarioId}`)
    .subscribe({
      next: () => {
        alert('Desconto estornado com sucesso!');
        
        if (this.reserva) {
          this.carregarReserva(this.reserva.id);
        }
      },
      error: (error) => {
        console.error('Erro ao estornar desconto:', error);
        alert(error.error?.message || 'Erro ao estornar desconto!');
      }
    });
}

// ============= IMPRIMIR EXTRATO =============
imprimirExtrato(): void {
  if (!this.reserva) return;

  const totalComDesconto = (this.reserva.totalHospedagem || 0) - (this.reserva.desconto || 0);
  const saldo = totalComDesconto - (this.reserva.totalRecebido || 0);

  // Agrupar extratos por tipo
  const diarias = (this.reserva.extratos || []).filter(e => 
    e.statusLancamento === 'DIARIA'
  );
  
  const produtos = (this.reserva.extratos || []).filter(e => 
    e.statusLancamento === 'PRODUTO'
  );
  
  const pagamentos = (this.reserva.extratos || []).filter(e => 
    e.statusLancamento === 'PAGAMENTO'
  );
  
  const estornos = (this.reserva.extratos || []).filter(e => 
    e.statusLancamento === 'ESTORNO'
  );

  const htmlImpressao = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>Extrato - Reserva #${this.reserva.id}</title>
 
  <style>
    @page { 
    size: 80mm auto; 
    margin: 0; 
  }
  
  * {
    font-family: 'Courier New', monospace !important;
    font-weight: 700 !important;  /* ✅ TUDO EM NEGRITO */
    color: #000 !important;
    -webkit-print-color-adjust: exact !important;
    print-color-adjust: exact !important;
  }
  
  body { 
    width: 80mm; 
    margin: 0; 
    padding: 3mm;
    font-size: 10pt !important;  /* ✅ AUMENTADO */
    line-height: 1.3;
  }
  
  .cabecalho { 
    text-align: left; 
    margin-bottom: 6px; 
  }
  
  .cabecalho h1 { 
    font-size: 16pt !important;  /* ✅ AUMENTADO */
    font-weight: 900 !important;
    margin: 0 0 2px 0; 
    letter-spacing: 1px; 
  }
  
  .cnpj, .endereco { 
    font-size: 10pt !important;  /* ✅ AUMENTADO */
    font-weight: 700 !important;
    margin: 1px 0; 
  }
  
  .separador { 
    text-align: center; 
    margin: 5px 0; 
    font-size: 10pt !important;  /* ✅ AUMENTADO */
    font-weight: 700 !important;
  }
  
  .titulo-documento { 
    text-align: left; 
    margin: 6px 0; 
  }
  
  .titulo-documento h2 { 
    font-size: 14pt !important;  /* ✅ AUMENTADO */
    font-weight: 900 !important;
    margin: 0; 
  }
  
  .numero-reserva { 
    font-size: 11pt !important;  /* ✅ AUMENTADO */
    font-weight: 900 !important; 
    margin: 3px 0; 
  }
  
  .data-emissao { 
    font-size: 10pt !important;  /* ✅ AUMENTADO */
    font-weight: 700 !important;
    margin: 2px 0; 
  }
  
  .secao { 
    margin: 6px 0; 
  }
  
  .secao h3 { 
    font-size: 11pt !important;  /* ✅ AUMENTADO */
    font-weight: 900 !important;
    margin: 5px 0 3px 0; 
    text-decoration: underline;
    border-bottom: 1px dashed #000;
    padding-bottom: 3px;
  }
  
  .secao p { 
    margin: 3px 0; 
    font-size: 10pt !important;  /* ✅ AUMENTADO */
    font-weight: 700 !important;
    line-height: 1.3; 
  }
  
  .secao strong {
    font-weight: 900 !important;  /* ✅ EXTRA BOLD */
  }
  
  .item-extrato { 
    display: flex; 
    justify-content: space-between;
    margin: 2px 0; 
    font-size: 10pt !important;  /* ✅ AUMENTADO */
    font-weight: 700 !important;
    padding: 2px 0;
  }
  
  .item-descricao { 
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 50mm;
  }
  
  .item-valor { 
    min-width: 20mm;
    text-align: right;
    font-weight: 900 !important;  /* ✅ EXTRA BOLD */
  }
  
  .linha-total { 
    display: flex; 
    justify-content: space-between;
    margin: 4px 0; 
    font-size: 11pt !important;  /* ✅ AUMENTADO */
    font-weight: 900 !important;
    padding: 4px 0;
    border-top: 2px solid #000;  /* ✅ LINHA MAIS GROSSA */
  }
  
  .linha-saldo {
    display: flex; 
    justify-content: space-between;
    margin: 5px 0; 
    font-size: 13pt !important;  /* ✅ AUMENTADO */
    font-weight: 900 !important;
    padding: 5px 0;
    border-top: 3px solid #000;  /* ✅ LINHA MAIS GROSSA */
    border-bottom: 3px solid #000;
  }
  
  .rodape { 
    text-align: left; 
    margin-top: 10px; 
    font-size: 10pt !important;  /* ✅ AUMENTADO */
    font-weight: 700 !important;
    border-top: 1px dashed #000;
    padding-top: 6px;
  }
  
  .rodape p { 
    margin: 2px 0; 
  }
  
  .observacao {
    background: #f0f0f0;
    padding: 6px;
    margin: 6px 0;
    border-left: 4px solid #000;  /* ✅ BORDA MAIS GROSSA */
    font-size: 10pt !important;  /* ✅ AUMENTADO */
    font-weight: 700 !important;
  }
  
  .observacao strong {
    font-weight: 900 !important;  /* ✅ EXTRA BOLD */
  }
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
        <h2>EXTRATO DA HOSPEDAGEM</h2>
        <p class="numero-reserva">Reserva Nº ${this.reserva.id}</p>
        <p class="data-emissao">${this.dataAtualCompleta()}</p>
      </div>

      <div class="separador">================================</div>

      <div class="secao">
        <p><strong>Hospede:</strong> ${this.reserva.cliente?.nome}</p>
        <p><strong>Apartamento:</strong> ${this.reserva.apartamento?.numeroApartamento}</p>
        <p><strong>Check-in:</strong> ${this.formatarDataCompleta(this.reserva.dataCheckin)}</p>
        <p><strong>Check-out:</strong> ${this.formatarDataCompleta(this.reserva.dataCheckout)}</p>
        <p><strong>Hospedes:</strong> ${this.reserva.quantidadeHospede} pessoa(s)</p>
      </div>

      <div class="separador">================================</div>

      <!-- DIÁRIAS -->
      ${diarias.length > 0 ? `
        <div class="secao">
          <h3>DIARIAS (${diarias.length})</h3>
          ${diarias.map(d => `
            <div class="item-extrato">
              <span class="item-descricao">${d.descricao}</span>
              <span class="item-valor">R$ ${this.formatarMoeda(d.totalLancamento)}</span>
            </div>
          `).join('')}
        </div>
      ` : ''}

      <!-- PRODUTOS/CONSUMO -->
      ${produtos.length > 0 ? `
        <div class="secao">
          <h3>CONSUMO (${produtos.length} item/ns)</h3>
          ${produtos.map(p => `
            <div class="item-extrato">
              <span class="item-descricao">${p.descricao} (${p.quantidade}x)</span>
              <span class="item-valor">R$ ${this.formatarMoeda(p.totalLancamento)}</span>
            </div>
          `).join('')}
        </div>
      ` : ''}

      <!-- ESTORNOS -->
      ${estornos.length > 0 ? `
        <div class="secao">
          <h3>AJUSTES/ESTORNOS</h3>
          ${estornos.map(e => `
            <div class="item-extrato">
              <span class="item-descricao">${e.descricao}</span>
              <span class="item-valor">R$ ${this.formatarMoeda(e.totalLancamento)}</span>
            </div>
          `).join('')}
        </div>
      ` : ''}

      <div class="separador">- - - - - - - - - - - - - - - -</div>

      <!-- SUBTOTAIS -->
      <div class="secao">
        <div class="item-extrato">
          <span>Total Diarias:</span>
          <span class="item-valor">R$ ${this.formatarMoeda(this.reserva.totalDiaria)}</span>
        </div>
        <div class="item-extrato">
          <span>Total Consumo:</span>
          <span class="item-valor">R$ ${this.formatarMoeda(this.reserva.totalProduto || 0)}</span>
        </div>
        <div class="linha-total">
          <span>SUBTOTAL:</span>
          <span>R$ ${this.formatarMoeda(this.reserva.totalHospedagem)}</span>
        </div>
      </div>

      <!-- DESCONTOS -->
      ${(this.reserva.desconto || 0) > 0 ? `
        <div class="secao">
          <div class="item-extrato">
            <span>(-) Desconto:</span>
            <span class="item-valor">R$ ${this.formatarMoeda(this.reserva.desconto)}</span>
          </div>
        </div>
      ` : ''}

      <!-- PAGAMENTOS -->
      ${pagamentos.length > 0 ? `
        <div class="secao">
          <h3>PAGAMENTOS REALIZADOS</h3>
          ${pagamentos.map(pag => `
            <div class="item-extrato">
              <span class="item-descricao">${pag.descricao}</span>
              <span class="item-valor">R$ ${this.formatarMoeda(Math.abs(pag.totalLancamento))}</span>
            </div>
          `).join('')}
          <div class="linha-total">
            <span>Total Recebido:</span>
            <span>R$ ${this.formatarMoeda(this.reserva.totalRecebido)}</span>
          </div>
        </div>
      ` : ''}

      <div class="separador">================================</div>

      <!-- SALDO FINAL -->
      <div class="linha-saldo">
        <span>SALDO A PAGAR:</span>
        <span>R$ ${this.formatarMoeda(this.reserva.totalApagar)}</span>
      </div>

      ${saldo > 0 ? `
        <div class="observacao">
          <p><strong>ATENÇÃO:</strong></p>
          <p>Saldo devedor de R$ ${this.formatarMoeda(saldo)}</p>
          <p>Favor regularizar no checkout.</p>
        </div>
      ` : saldo < 0 ? `
        <div class="observacao">
          <p><strong>CRÉDITO:</strong></p>
          <p>Valor a devolver: R$ ${this.formatarMoeda(Math.abs(saldo))}</p>
        </div>
      ` : `
        <div class="observacao">
          <p><strong>✅ QUITADO</strong></p>
          <p>Hospedagem totalmente paga.</p>
        </div>
      `}

      <div class="rodape">
        <p>Este e um extrato parcial.</p>
        <p>Valores sujeitos a alteracao ate o checkout.</p>
        <p>Emitido em: ${this.dataAtualCompleta()}</p>
      </div>

      <script>
        window.onload = function() {
          window.print();
          window.onafterprint = function() {
            window.close();
          };
        };
      </script>
    </body>
    </html>
  `;

  const janelaImpressao = window.open('', '_blank', 'width=800,height=600');
  if (janelaImpressao) {
    janelaImpressao.document.write(htmlImpressao);
    janelaImpressao.document.close();
  }
}

/**
 * Abre modal para editar/adicionar placa do hóspede
 */
editarPlacaHospede(hospede: any): void {
  console.log('✏️ Editando placa do hóspede:', hospede);
  
  this.hospedeEditandoPlaca = hospede;
  this.placaEditando = hospede.placaCarro || '';
  this.modalEditarPlaca = true;
  
  console.log('   Hóspede:', hospede.cliente?.nome || hospede.nomeCompleto);
  console.log('   Placa atual:', hospede.placaCarro || 'Sem placa');
}

/**
 * Fecha modal de edição de placa
 */
fecharModalEditarPlaca(): void {
  console.log('🔒 Fechando modal de placa');
  
  this.modalEditarPlaca = false;
  this.hospedeEditandoPlaca = null;
  this.placaEditando = '';
}

/**
 * Formata placa durante digitação
 */
formatarPlacaEdicao(): void {
  if (!this.placaEditando) return;
  
  // Remove tudo que não é letra ou número
  let placa = this.placaEditando.toUpperCase().replace(/[^A-Z0-9]/g, '');
  
  // Adiciona hífen após 3 caracteres
  if (placa.length > 3) {
    placa = placa.substring(0, 3) + '-' + placa.substring(3, 7);
  }
  
  this.placaEditando = placa;
}

/**
 * Salva placa do hóspede
 */
salvarPlacaHospede(): void {
  if (!this.placaEditando || this.placaEditando.length < 7) {
    alert('⚠️ Digite uma placa válida (mínimo 7 caracteres)!');
    return;
  }

  if (!this.hospedeEditandoPlaca || !this.hospedeEditandoPlaca.id) {
    alert('❌ Erro: Hóspede não identificado!');
    return;
  }

  console.log('════════════════════════════════════════');
  console.log('💾 SALVANDO PLACA DO HÓSPEDE');
  console.log('════════════════════════════════════════');
  console.log('   Hóspede ID:', this.hospedeEditandoPlaca.id);
  console.log('   Placa:', this.placaEditando);

  this.http.patch(
    `http://localhost:8080/api/reservas/hospedes/${this.hospedeEditandoPlaca.id}/atualizar-placa`,
    null,
    { params: { placa: this.placaEditando } }
  ).subscribe({
    next: (response: any) => {
      console.log('✅ Placa atualizada com sucesso:', response);
      
      alert(`✅ Placa ${response.placa} cadastrada com sucesso!`);
      
      // Atualizar na lista de hóspedes
      this.hospedeEditandoPlaca.placaCarro = response.placa;
      
      // Fechar modal
      this.fecharModalEditarPlaca();
      
      // Opcional: recarregar reserva completa
      // this.carregarReserva();
    },
    error: (erro) => {
      console.error('❌ Erro ao salvar placa:', erro);
      
      const mensagemErro = erro.error?.erro || erro.error?.message || erro.message || 'Erro desconhecido';
      
      alert(`❌ Erro ao salvar placa: ${mensagemErro}`);
    }
  });
}

/**
 * 🔓 ATIVAR PRÉ-RESERVA (CHECK-IN MANUAL)
 */
ativarPreReserva(): void {
  if (!confirm('🔓 Confirma o CHECK-IN desta pré-reserva?\n\nEla será ativada e a data/hora de check-in será atualizada para agora.')) {
    return;
  }

  console.log('═══════════════════════════════════════════');
  console.log('🔓 ATIVANDO PRÉ-RESERVA #' + this.reserva?.id);
  console.log('═══════════════════════════════════════════');

  this.http.patch(
    `http://localhost:8080/api/reservas/${this.reserva?.id}/ativar-pre-reserva`,
    {}
  ).subscribe({
    next: (response: any) => {
      console.log('✅ Pré-reserva ativada:', response);
      
      alert('✅ Check-in realizado com sucesso!\n\nReserva agora está ATIVA.');
      
      // ✅ RECARREGAR COM O ID
      this.carregarReserva(this.reservaId);
    },
    error: (erro) => {
      console.error('❌ Erro ao ativar pré-reserva:', erro);
      
      const mensagemErro = erro.error?.erro || erro.error?.message || erro.message || 'Erro desconhecido';
      
      alert(`❌ Erro ao ativar reserva:\n\n${mensagemErro}`);
    }
  });
}
   /**
 * 🔍 VERIFICA SE PODE ATIVAR PRÉ-RESERVA
 * Só permite se estiver no dia do check-in ou 1 dia antes
 */
podeAtivarPreReserva(): boolean {
  if (!this.reserva || !this.reserva.dataCheckin) {
    return false;
  }

  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);

  const dataCheckin = new Date(this.reserva.dataCheckin);
  dataCheckin.setHours(0, 0, 0, 0);

  const diffTime = dataCheckin.getTime() - hoje.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  return diffDays <= 1;
}
   temAuditoria(): boolean {
  return !!(this.reserva?.criadoPor || 
            this.reserva?.finalizadoPor || 
            this.reserva?.canceladoPor);
}

jaFoiEstornado(extrato: any): boolean {
  if (!this.reserva?.extratos) return false;
  
  // Verificar se existe um lançamento de ESTORNO referenciando este extrato
  return this.reserva.extratos.some(e => 
    e.statusLancamento === 'ESTORNO' && 
    e.totalLancamento < 0 &&
    e.descricao?.includes(extrato.descricao)
  );
}

}