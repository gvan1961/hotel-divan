import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ReservaService } from '../../services/reserva.service';
import { ClienteService } from '../../services/cliente.service';
import { ApartamentoService } from '../../services/apartamento.service';
import { DiariaService } from '../../services/diaria.service';

import { ReservaRequest } from '../../models/reserva.model';
import { Cliente } from '../../models/cliente.model';
import { Apartamento } from '../../models/apartamento.model';
import { Diaria } from '../../models/diaria.model';

@Component({
  selector: 'app-reserva-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div class="container">

    <!-- ========================================== -->
    <!-- 🔔 BANNER DE NOTIFICAÇÃO -->
    <!-- ========================================== -->
    <div class="banner-notificacao" 
         [class.mostrar]="mostrarBanner"
         [class.banner-erro]="tipoBanner === 'erro'"
         [class.banner-sucesso]="tipoBanner === 'sucesso'"
         [class.banner-aviso]="tipoBanner === 'aviso'">
      
      <div class="banner-conteudo">
        <div class="banner-icone">
          <span *ngIf="tipoBanner === 'erro'">🚫</span>
          <span *ngIf="tipoBanner === 'sucesso'">✅</span>
          <span *ngIf="tipoBanner === 'aviso'">⚠️</span>
        </div>
        
        <div class="banner-texto">
          {{ mensagemBanner }}
        </div>
        
        <button class="banner-fechar" (click)="fecharBanner()">✕</button>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- HEADER -->
    <!-- ========================================== -->
    <div class="header">
      <h1>🏨 Nova Reserva</h1>
      <button class="btn-back" (click)="voltar()">← Voltar</button>
    </div>

    <!-- ========================================== -->
    <!-- FORMULÁRIO -->
    <!-- ========================================== -->
    <div class="form-card">
      <form (ngSubmit)="salvar()">
        
        <!-- ========================================== -->
        <!-- BUSCA DE CLIENTE TITULAR -->
        <!-- ========================================== -->
        <div class="form-group campo-busca">
          <label>Cliente Titular *</label>
          
          <div class="busca-wrapper">
            <input 
              type="text" 
              [(ngModel)]="buscaCliente"
              name="buscaCliente"
              (input)="filtrarClientes()"
              (focus)="filtrarClientes()"
              placeholder="Digite o nome ou CPF do cliente..."
              class="input-busca"
              autocomplete="off">
            
            <button 
              type="button" 
              class="btn-limpar-busca" 
              *ngIf="buscaCliente"
              (click)="limparBuscaCliente()">
              ✕
            </button>
          </div>
      
          <!-- RESULTADOS DA BUSCA -->
          <div class="resultados-busca" *ngIf="mostrarResultados && clientesFiltrados.length > 0">
            <div 
              class="resultado-item" 
              *ngFor="let cliente of clientesFiltrados"
              (click)="selecionarCliente(cliente)">
              <div class="resultado-nome">{{ cliente.nome }}</div>
              <div class="resultado-cpf">CPF: {{ formatarCPF(cliente.cpf) }}</div>
              <div class="resultado-info" *ngIf="cliente.celular">
                📞 {{ cliente.celular }}
              </div>
            </div>
          </div>
          
          <div class="sem-resultado" *ngIf="mostrarResultados && clientesFiltrados.length === 0 && buscaCliente.length >= 2">
            ❌ Nenhum cliente encontrado
          </div>

          <small class="field-help">Digite pelo menos 2 caracteres para buscar</small>
        </div>

        <!-- ========================================== -->
<!-- CARD DO CLIENTE SELECIONADO -->
<!-- ========================================== -->
<div class="cliente-selecionado" *ngIf="clienteSelecionado">
  <div class="cliente-header">
    <h3>✅ Cliente Selecionado (Titular)</h3>
    <button 
      type="button"
      class="btn-trocar-cliente"
      (click)="limparBuscaCliente()"
      title="Trocar cliente">
      🔄 Trocar
    </button>
  </div>
  <div class="cliente-info">
    <div class="info-item">
      <span class="label">👤 Nome:</span>
      <span class="value">{{ clienteSelecionado.nome }}</span>
    </div>
    <div class="info-item">
      <span class="label">📄 CPF:</span>
      <span class="value">{{ formatarCPF(clienteSelecionado.cpf) }}</span>
    </div>
    <div class="info-item" *ngIf="clienteSelecionado.celular">
      <span class="label">📞 Celular:</span>
      <span class="value">{{ clienteSelecionado.celular }}</span>
    </div>
    <div class="info-item info-empresa" *ngIf="clienteSelecionado.empresa?.nomeEmpresa">
      <span class="label">🏢 Empresa:</span>
      <span class="value-empresa">{{ clienteSelecionado.empresa.nomeEmpresa }}</span>
    </div>
  </div>

  <!-- ✅ CAMPO DE PLACA DO TITULAR -->
  <div class="placa-titular-campo">
    <label>
      🚗 Placa do Veículo do Titular 
      <small>(opcional)</small>
    </label>
    <input 
      type="text"
      [(ngModel)]="placaTitular"
      name="placaTitular"
      (input)="formatarPlacaTitular()"
      placeholder="ABC-1234"
      maxlength="8"
      class="input-placa-titular">
    <small class="field-help">
      Formato: ABC-1234 ou ABC-1D23 (Mercosul)
    </small>
  </div>
</div>

        <!-- ========================================== -->
        <!-- APARTAMENTO E QUANTIDADE -->
        <!-- ========================================== -->
        <div class="form-row">
          
          <div class="form-group">
            <label>Apartamento *</label>
            
            <div class="aviso-mapa" *ngIf="apartamentoBloqueado">
              <span class="icone">🗺️</span>
              <div class="aviso-texto">
                <strong>Reserva iniciada pelo Mapa</strong>
                <p>Apartamento selecionado automaticamente e não pode ser alterado</p>
              </div>
            </div>

            <select [(ngModel)]="reserva.apartamentoId" 
              name="apartamentoId" 
              required
              [disabled]="apartamentoBloqueado"
              (change)="aoSelecionarApartamento()">
              <option [ngValue]="0">Selecione o apartamento</option>
              <option *ngFor="let apt of apartamentos" [ngValue]="apt.id">
                {{ apt.numeroApartamento }} - {{ apt.tipoApartamento?.tipo || apt.tipoApartamentoNome || 'Sem tipo' }} (Cap: {{ apt.capacidade }})                
              </option>
            </select>
            
            <small class="field-help" *ngIf="apartamentoSelecionado && !apartamentoBloqueado">
              ✅ Selecionado: Apt {{ apartamentoSelecionado.numeroApartamento }} - {{ apartamentoSelecionado.tipoApartamentoNome }} - Capacidade máxima: {{ apartamentoSelecionado.capacidade }} pessoa(s)
            </small>
          </div>

          <div class="form-group">
            <label>Quantidade de Hóspedes *</label>
            <input 
              type="number" 
              [(ngModel)]="reserva.quantidadeHospede" 
              name="quantidadeHospede" 
              required 
              min="1" 
              [max]="apartamentoSelecionado?.capacidade || 10"
              placeholder="Quantidade de pessoas"
              [disabled]="true"
              readonly />
            <small class="field-help">
              Calculado automaticamente: {{ hospedes.length }} hóspede(s) cadastrado(s)
            </small>
          </div>
        </div>

        <!-- ========================================== -->
        <!-- DATAS -->
        <!-- ========================================== -->
        <div class="form-row">
          <div class="form-group">
            <label>🗓️ Data e Hora de Check-in *</label>
            <input type="datetime-local" 
                   [(ngModel)]="reserva.dataCheckin" 
                   name="dataCheckin" 
                   required
                   [min]="dataMinima"
                   (change)="onDataChange()" />
            <small class="field-help" *ngIf="reserva.dataCheckin">
              {{ formatarDataHora(reserva.dataCheckin) }}
            </small>
          </div>

          <div class="form-group">
            <label>🗓️ Data e Hora de Check-out *</label>
            <input type="datetime-local" 
                   [(ngModel)]="reserva.dataCheckout" 
                   name="dataCheckout" 
                   required
                   [min]="reserva.dataCheckin || dataMinima"
                   (change)="onDataChange()" />
            <small class="field-help" *ngIf="reserva.dataCheckout && quantidadeDiarias > 0">
              {{ formatarDataHora(reserva.dataCheckout) }} - Total: {{ quantidadeDiarias }} diária(s)
            </small>  
          </div>
        </div>

        <!-- ========================================== -->
        <!-- SEÇÃO DE HÓSPEDES -->
        <!-- ========================================== -->
        <div class="secao-hospedes" *ngIf="reserva.clienteId">
          <div class="secao-header">
            <h3>👥 Hóspedes da Reserva</h3>
            <span class="badge-hospedes">{{ hospedes.length }}</span>
            <button 
              type="button"
              class="btn-adicionar-hospede"
              (click)="abrirModalAdicionarHospede()">
              ➕ Adicionar Hóspede
            </button>
          </div>

          <div class="lista-hospedes" *ngIf="hospedes.length > 0">
            <div class="hospede-item" *ngFor="let hospede of hospedes; let i = index">
              <div class="hospede-numero">{{ i + 1 }}</div>
              <div class="hospede-info">
                <div class="hospede-nome">
                  {{ hospede.nomeCompleto }}
                  <span class="badge-titular" *ngIf="hospede.titular">★ TITULAR</span>
                  <span class="badge-novo" *ngIf="hospede.cadastrarNovo">NOVO</span>
                </div>
                <div class="hospede-detalhes">
                  CPF: {{ hospede.cpf || 'Não informado' }} | 
                  Tel: {{ hospede.telefone || 'Não informado' }}
                </div>
              </div>

              <button 
                type="button"
                class="btn-remover-hospede"
                [class.btn-remover-titular]="hospede.titular"
                (click)="removerHospede(i)"
                [title]="hospede.titular ? 'Remover titular (próximo será promovido)' : 'Remover hóspede'">
                {{ hospede.titular ? '⭐🗑️' : '🗑️' }}
              </button>
              <span *ngIf="i === 0" class="hospede-bloqueado" title="Titular não pode ser removido">
                🔒
              </span>
            </div>
          </div>

          <div class="aviso-hospedes" *ngIf="hospedes.length === 0">
            ⚠️ Nenhum hóspede cadastrado. Selecione o cliente titular primeiro.
          </div>
        </div>
        
        <!-- ========================================== -->
        <!-- MENSAGEM DE ERRO -->
        <!-- ========================================== -->
        <div *ngIf="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <!-- ========================================== -->
        <!-- BOTÕES DE AÇÃO -->
        <!-- ========================================== -->
        <div class="form-actions">
          <button type="button" class="btn-cancel" (click)="voltar()">Cancelar</button>
          <button type="submit" class="btn-save" [disabled]="loading || hospedes.length === 0">
            {{ loading ? 'Criando...' : 'Criar Reserva' }}
          </button>
        </div>
      </form>
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
     


      <div *ngIf="modoModalHospede === 'buscar'" class="modal-tab-content">
  <!-- ✅ SE NÃO SELECIONOU AINDA, MOSTRA A BUSCA -->
  <div *ngIf="!hospedeExistenteSelecionado">
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
        (click)="selecionarClienteParaPlaca(cliente)">
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

  <!-- ✅ SE JÁ SELECIONOU, MOSTRA FORMULÁRIO COM PLACA -->
  <div *ngIf="hospedeExistenteSelecionado" class="hospede-existente-form">
    <h4>✅ Cliente Selecionado</h4>
    
    <div class="info-readonly">
      👤 Nome: {{ hospedeExistenteSelecionado.nome }}
    </div>
    <div class="info-readonly">
      📄 CPF: {{ formatarCPF(hospedeExistenteSelecionado.cpf) }}
    </div>
    <div class="info-readonly" *ngIf="hospedeExistenteSelecionado.celular">
      📞 Telefone: {{ hospedeExistenteSelecionado.celular }}
    </div>

    <div class="form-placa-existente">
      <label>
        🚗 Placa do Veículo 
        <small>(opcional)</small>
      </label>
      <input 
        type="text"
        [(ngModel)]="placaHospedeExistente"
        (input)="formatarPlacaHospedeExistente()"
        placeholder="ABC-1234"
        maxlength="8">
      <small class="field-help">
        Formato: ABC-1234 ou ABC-1D23 (Mercosul)
      </small>
    </div>

    <div class="btns-hospede-existente">
      <button 
        type="button"
        class="btn-cancelar-hospede"
        (click)="cancelarSelecaoHospedeExistente()">
        ← Voltar
      </button>
      <button 
        type="button"
        class="btn-confirmar-hospede"
        (click)="confirmarHospedeExistente()">
        ✅ Adicionar Hóspede
      </button>
    </div>
  </div>
</div>




      <div *ngIf="modoModalHospede === 'cadastrar'" class="modal-tab-content">
        <div class="form-group">
          <label>Nome Completo *</label>
          <input 
            type="text"
            [(ngModel)]="novoHospede.nome"
            placeholder="Nome completo do hóspede">
        </div>

        <div class="form-group">
          <label>CPF <small>(opcional para menores)</small></label>
          <input 
            type="text"
            [(ngModel)]="novoHospede.cpf"
            placeholder="000.000.000-00">
        </div>

        <div class="form-group">
  <label>Telefone</label>
  <input 
    type="text"
    [(ngModel)]="novoHospede.telefone"
    placeholder="(00) 00000-0000">
</div>

<!-- 🚨 TESTE: ESSA CAIXA DEVE APARECER -->
<div style="background: red; color: white; padding: 15px; margin: 10px 0; font-weight: bold; text-align: center;">
  🚨 SE VOCÊ VÊ ISSO, O HTML ESTÁ SENDO LIDO!
</div>

<!-- ✅ CAMPO PLACA DO CARRO -->
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
    style="text-transform: uppercase; font-family: 'Courier New', monospace; font-weight: bold; letter-spacing: 1px;">
  <small class="field-help">
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
`,
  styles: [`
    .container {
      padding: 20px;
      max-width: 900px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    h1 {
      color: #333;
      margin: 0;
    }

    .btn-back {
      background: #6c757d;
      color: white;
      border: none;
      padding: 8px 16px;
      border-radius: 5px;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-back:hover {
      background: #5a6268;
      transform: translateY(-1px);
    }

    .form-card {
      background: white;
      padding: 30px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
      margin-bottom: 20px;
    }

    .form-group {
      margin-bottom: 20px;
    }

    label {
      display: block;
      margin-bottom: 5px;
      color: #555;
      font-weight: 500;
    }

    input, select {
      width: 100%;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 5px;
      font-size: 14px;
      box-sizing: border-box;
    }

    input:focus, select:focus {
      outline: none;
      border-color: #667eea;
    }

    .field-help {
      display: block;
      font-size: 12px;
      color: #666;
      margin-top: 4px;
      font-style: italic;
    }

    /* ESTILOS DA BUSCA */
    .campo-busca {
      position: relative;
    }

    .busca-wrapper {
      position: relative;
      display: flex;
      align-items: center;
    }

    .input-busca {
      width: 100%;
      padding: 12px;
      padding-right: 40px;
      border: 2px solid #ddd;
      border-radius: 6px;
      font-size: 14px;
      transition: all 0.3s ease;
    }

    .input-busca:focus {
      outline: none;
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }

    .btn-limpar-busca {
      position: absolute;
      right: 10px;
      width: 30px;
      height: 30px;
      background: #e0e0e0;
      border: none;
      border-radius: 50%;
      cursor: pointer;
      font-size: 1.2em;
      color: #666;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s ease;
    }

    .btn-limpar-busca:hover {
      background: #d0d0d0;
      color: #333;
    }

    .resultados-busca {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      background: white;
      border: 2px solid #667eea;
      border-top: none;
      border-radius: 0 0 6px 6px;
      max-height: 300px;
      overflow-y: auto;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      z-index: 1000;
      margin-top: -2px;
    }

    .resultado-item {
      padding: 12px 15px;
      cursor: pointer;
      transition: background 0.2s ease;
      border-bottom: 1px solid #f0f0f0;
    }

    .resultado-item:last-child {
      border-bottom: none;
    }

    .resultado-item:hover {
      background: #f5f5f5;
    }

    .resultado-nome {
      font-weight: 600;
      color: #2c3e50;
      margin-bottom: 4px;
    }

    .resultado-cpf {
      font-size: 0.9em;
      color: #7f8c8d;
      margin-bottom: 2px;
    }

    .resultado-info {
      font-size: 0.85em;
      color: #95a5a6;
    }

    .sem-resultado {
      padding: 20px;
      text-align: center;
      color: #e74c3c;
      font-weight: 500;
    }

    .info-box {
      background: #e8f5e9;
      border-left: 4px solid #4caf50;
      padding: 20px;
      margin: 20px 0;
      border-radius: 4px;
    }

    .info-box strong {
      color: #2e7d32;
      display: block;
      margin-bottom: 15px;
      font-size: 16px;
    }

    .resumo-info {
      background: white;
      padding: 15px;
      border-radius: 5px;
      margin-bottom: 15px;
    }

    .info-linha {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px solid #eee;
      font-size: 14px;
    }

    .info-linha:last-child {
      border-bottom: none;
    }

    .info-linha.destaque {
      font-weight: 600;
      color: #2e7d32;
      border-bottom: 2px solid #4caf50 !important;
      padding-bottom: 10px;
      margin-bottom: 0;
    }

    .info-linha span:first-child {
      color: #666;
    }

    .info-linha span:last-child {
      color: #333;
      font-weight: 500;
    }

    .valor-estimado {
      background: white;
      padding: 15px;
      border-radius: 5px;
      margin-bottom: 10px;
    }

    .valor-estimado > div {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      font-size: 14px;
    }

    .valor-estimado .total {
      border-top: 2px solid #4caf50;
      margin-top: 10px;
      padding-top: 10px;
      font-weight: 600;
      font-size: 18px;
      color: #2e7d32;
    }

    .info-box small {
      color: #666;
      font-size: 12px;
    }

    .error-message {
      background: #fee;
      color: #c33;
      padding: 10px;
      border-radius: 5px;
      margin-bottom: 15px;
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 30px;
    }

    .btn-cancel, .btn-save {
      padding: 10px 20px;
      border: none;
      border-radius: 5px;
      cursor: pointer;
      font-size: 14px;
      transition: all 0.2s;
    }

    .btn-cancel {
      background: #6c757d;
      color: white;
    }

    .btn-cancel:hover {
      background: #5a6268;
    }

    .btn-save {
      background: #28a745;
      color: white;
    }

    .btn-save:hover:not(:disabled) {
      background: #218838;
    }

    .btn-save:disabled {
      background: #ccc;
      cursor: not-allowed;
    }

    @media (max-width: 768px) {
      .form-row {
        grid-template-columns: 1fr;
      }
    }

    /* ✅ ADICIONE NO FINAL DOS STYLES */

select:disabled {
  background: #f0f0f0;
  color: #666;
  cursor: not-allowed;
  border: 2px solid #ddd;
}

.field-help.bloqueado {
  color: #e67e22;
  font-weight: 600;
  background: #fff3cd;
  padding: 8px;
  border-radius: 4px;
  border-left: 3px solid #f39c12;
}

/* ✅ ADICIONE NO STYLES DO reserva-form.app.ts */

.aviso-mapa {
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.aviso-mapa .icone {
  font-size: 2em;
}

.aviso-mapa .aviso-texto {
  flex: 1;
}

.aviso-mapa strong {
  display: block;
  font-size: 1.1em;
  margin-bottom: 4px;
}

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
  box-shadow: 0 2px 5px rgba(102, 126, 234, 0.3);
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
  transition: all 0.2s ease;
}

.hospede-item:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border-color: #667eea;
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
  flex-shrink: 0;
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
  flex-wrap: wrap;
}

.badge-titular {
  background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
  color: white;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.75em;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.badge-novo {
  background: linear-gradient(135deg, #27ae60 0%, #229954 100%);
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

.btn-remover-hospede {
  background: #e74c3c;
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.2em;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.btn-remover-hospede:hover {
  background: #c0392b;
  transform: scale(1.1);
}

.hospede-bloqueado {
  font-size: 1.5em;
  color: #95a5a6;
  opacity: 0.6;
}

.aviso-hospedes {
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 6px;
  padding: 15px;
  text-align: center;
  color: #856404;
  font-weight: 500;
}

/* ========================================== */
/* ESTILOS DO MODAL */
/* ========================================== */

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 30px;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 10px 40px rgba(0,0,0,0.3);
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-content h2 {
  margin: 0 0 20px 0;
  color: #2c3e50;
  font-size: 1.5em;
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
  transition: all 0.3s ease;
}

.modal-tabs button:hover {
  color: #667eea;
}

.modal-tabs button.active {
  color: #667eea;
  border-bottom-color: #667eea;
}

.modal-tab-content {
  animation: fadeIn 0.2s ease;
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

.input-busca-modal:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
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
  transition: background 0.2s ease;
}

.resultado-modal-item:last-child {
  border-bottom: none;
}

.resultado-modal-item:hover {
  background: #f8f9fa;
}

.sem-resultado-modal {
  padding: 20px;
  text-align: center;
  color: #e74c3c;
  font-weight: 500;
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

.btn-salvar-hospede {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 14px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 1em;
  transition: all 0.3s ease;
  margin-top: 15px;
}

.btn-salvar-hospede:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
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
  font-weight: bold;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-fechar-modal:hover {
  background: #c0392b;
  transform: rotate(90deg);
}

.aviso-mapa p {
  margin: 0;
  font-size: 0.9em;
  opacity: 0.95;
}

/* ========================================== */
/* ✅ CARD DO CLIENTE SELECIONADO */
/* ========================================== */

.cliente-selecionado {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  border: 2px solid #4caf50;
  border-radius: 10px;
  padding: 20px;
  margin: 20px 0;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.2);
}

.cliente-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #4caf50;
}

.cliente-header h3 {
  margin: 0;
  color: #2e7d32;
  font-size: 1.2em;
}

.btn-trocar-cliente {
  background: #ff9800;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-trocar-cliente:hover {
  background: #f57c00;
  transform: scale(1.05);
}

.cliente-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cliente-info .info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 10px 15px;
  border-radius: 6px;
  border-left: 3px solid #4caf50;
}

.cliente-info .info-item.info-empresa {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border-left: 3px solid #1976d2;
}

.cliente-info .label {
  font-weight: 600;
  color: #555;
  font-size: 0.95em;
}

.cliente-info .value {
  color: #2c3e50;
  font-weight: 500;
}

.cliente-info .value-empresa {
  color: #1565c0;
  font-weight: 700;
  font-size: 1.05em;
}

 /* ========================================== */
/* 🔔 BANNER DE NOTIFICAÇÃO */
/* ========================================== */

.banner-notificacao {
  position: fixed;
  top: -200px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 800px;
  z-index: 10000;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  transition: top 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  animation: shake 0.5s ease-in-out;
}

.banner-notificacao.mostrar {
  top: 20px;
}

@keyframes shake {
  0%, 100% { transform: translateX(-50%) translateY(0); }
  25% { transform: translateX(-50%) translateY(-5px); }
  75% { transform: translateX(-50%) translateY(5px); }
}

.banner-conteudo {
  display: flex;
  align-items: center;
  padding: 20px 25px;
  gap: 15px;
}

.banner-icone {
  font-size: 2.5em;
  flex-shrink: 0;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.banner-texto {
  flex: 1;
  font-size: 1.1em;
  line-height: 1.5;
  font-weight: 500;
  white-space: pre-line;
}

.banner-fechar {
  background: rgba(255, 255, 255, 0.3);
  border: none;
  width: 35px;
  height: 35px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.4em;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.banner-fechar:hover {
  background: rgba(255, 255, 255, 0.5);
  transform: rotate(90deg);
}

/* TIPO: ERRO */
.banner-erro {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: white;
  border: 3px solid #a93226;
}

.banner-erro .banner-fechar:hover {
  background: rgba(255, 255, 255, 0.4);
}

/* TIPO: SUCESSO */
.banner-sucesso {
  background: linear-gradient(135deg, #27ae60 0%, #229954 100%);
  color: white;
  border: 3px solid #1e8449;
}

/* TIPO: AVISO */
.banner-aviso {
  background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
  color: white;
  border: 3px solid #d68910;
}

/* RESPONSIVO */
@media (max-width: 768px) {
  .banner-notificacao {
    width: 95%;
    max-width: none;
  }
  
  .banner-conteudo {
    padding: 15px;
  }
  
  .banner-icone {
    font-size: 2em;
  }
  
  .banner-texto {
    font-size: 0.95em;
  }

  /* Botão especial para remover titular */
.btn-remover-titular {
  background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
  animation: pulseWarning 2s ease-in-out infinite;
}

.btn-remover-titular:hover {
  background: linear-gradient(135deg, #e67e22 0%, #d35400 100%);
}

@keyframes pulseWarning {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

 /* ========================================== */
/* 🚗 CAMPO DE PLACA DO TITULAR */
/* ========================================== */

.placa-titular-campo {
  margin-top: 15px;
  padding: 15px;
  background: white;
  border-radius: 6px;
  border-left: 3px solid #2196f3;
}

.placa-titular-campo label {
  display: block;
  margin-bottom: 8px;
  color: #1565c0;
  font-weight: 600;
}

.input-placa-titular {
  width: 100%;
  padding: 10px;
  border: 2px solid #2196f3;
  border-radius: 5px;
  font-family: 'Courier New', monospace;
  font-weight: bold;
  letter-spacing: 1px;
  text-transform: uppercase;
  font-size: 14px;
  box-sizing: border-box;
}

.input-placa-titular:focus {
  outline: none;
  border-color: #1976d2;
  box-shadow: 0 0 0 3px rgba(25, 118, 210, 0.1);
}

/* ========================================== */
/* 🚗 FORMULÁRIO INTERMEDIÁRIO HÓSPEDE EXISTENTE */
/* ========================================== */

.hospede-existente-form {
  background: #f8f9fa;
  border: 2px solid #667eea;
  border-radius: 8px;
  padding: 20px;
  margin-top: 15px;
}

.hospede-existente-form h4 {
  margin: 0 0 15px 0;
  color: #667eea;
  font-size: 1.1em;
}

.info-readonly {
  background: white;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 8px;
  font-size: 0.9em;
  color: #555;
}

.form-placa-existente {
  margin-top: 15px;
}

.form-placa-existente label {
  display: block;
  margin-bottom: 5px;
  color: #333;
  font-weight: 600;
}

.form-placa-existente input {
  width: 100%;
  padding: 10px;
  border: 2px solid #667eea;
  border-radius: 5px;
  font-family: 'Courier New', monospace;
  font-weight: bold;
  letter-spacing: 1px;
  text-transform: uppercase;
  box-sizing: border-box;
}

.btns-hospede-existente {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.btn-confirmar-hospede {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-confirmar-hospede:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-cancelar-hospede {
  flex: 1;
  background: #6c757d;
  color: white;
  border: none;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-cancelar-hospede:hover {
  background: #5a6268;
}
}

  `]
})
export class ReservaFormApp implements OnInit {
  private reservaService = inject(ReservaService);
  private clienteService = inject(ClienteService);
  private apartamentoService = inject(ApartamentoService);
  private diariaService = inject(DiariaService);
  private router = inject(Router);
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);

  reserva: ReservaRequest = {
    clienteId: 0,
    apartamentoId: 0,
    quantidadeHospede: 1,
    dataCheckin: '',
    dataCheckout: ''
  };

  apartamentos: Apartamento[] = [];
  apartamentoSelecionado: Apartamento | null = null;
  diarias: Diaria[] = [];
  diariaAplicada: Diaria | null = null;
  dataMinima = '';
  
  clientesFiltrados: any[] = [];
  buscaCliente = '';
  mostrarResultados = false;
  clienteSelecionado: any = null;

  mensagemBanner = '';
  tipoBanner: 'erro' | 'sucesso' | 'aviso' = 'erro';
  mostrarBanner = false;

  quantidadeDiarias = 0;
  valorDiaria = 0;
  valorEstimado = 0;
  
  loading = false;
  errorMessage = '';

  apartamentoBloqueado = false;
  voltarParaMapa = false;

  hospedes: any[] = [];
  modalAdicionarHospede = false;
  modoModalHospede: 'buscar' | 'cadastrar' = 'buscar';
  clientesFiltradosModal: any[] = [];
  termoBuscaHospede = '';

  novoHospede = {
    nome: '',
    cpf: '',
    telefone: '',
    placaCarro: ''
  };

  placaTitular = ''; // ✅ Placa do cliente titular
  hospedeExistenteSelecionado: any = null; // ✅ Para mostrar formulário intermediário
  placaHospedeExistente = ''; // ✅ Placa do hóspede existente    

  ngOnInit(): void {
    console.log('🔵 Inicializando ReservaForm');
    
    this.setDatasPadrao();
    this.definirDataMinima();
    
    this.route.queryParams.subscribe(params => {
      console.log('📋 Query Params recebidos:', params);

      if (params['bloqueado'] === 'true') {
        this.apartamentoBloqueado = true;
        this.voltarParaMapa = true;
        console.log('🔒 Apartamento bloqueado (veio do mapa)');
      }

      if (params['dataCheckin']) {
        const dataCheckin = new Date(params['dataCheckin'] + 'T14:00:00');
        this.reserva.dataCheckin = this.formatDateTimeLocal(dataCheckin);
        
        const dataCheckout = new Date(dataCheckin);
        dataCheckout.setDate(dataCheckout.getDate() + 1);
        dataCheckout.setHours(13, 0, 0, 0);
        this.reserva.dataCheckout = this.formatDateTimeLocal(dataCheckout);
        
        console.log('📅 Datas do mapa:', this.reserva.dataCheckin, this.reserva.dataCheckout);
      }
      
      setTimeout(() => {
        this.carregarApartamentos();
      }, 300);
    });
  }

 /**
 * 📅 CHAMADO QUANDO QUALQUER DATA É ALTERADA
 */
onDataChange(): void {
  console.log('📅 Datas alteradas:');
  console.log('   Check-in:', this.reserva.dataCheckin);
  console.log('   Check-out:', this.reserva.dataCheckout);
  
  // ✅ Calcular diárias
  this.calcularDiarias();
  
  // ✅ Validar datas
  if (this.reserva.dataCheckin && this.reserva.dataCheckout) {
    const checkin = new Date(this.reserva.dataCheckin);
    const checkout = new Date(this.reserva.dataCheckout);
    
    // ✅ Garantir que checkout é posterior ao checkin
    if (checkout <= checkin) {
      console.warn('⚠️ Check-out deve ser posterior ao check-in');
      // Não mostrar alert aqui para não irritar o usuário enquanto digita
      return;
    }
    
    // ✅ RECARREGAR APARTAMENTOS DISPONÍVEIS PARA O NOVO PERÍODO
    console.log('🔄 Recarregando apartamentos para o novo período...');
    this.carregarApartamentos();
  }
}

  setDatasPadrao(): void {
    const hoje = new Date();
    hoje.setHours(14, 0, 0, 0);
    
    const amanha = new Date(hoje);
    amanha.setDate(amanha.getDate() + 1);
    amanha.setHours(13, 0, 0, 0);
    
    this.reserva.dataCheckin = this.formatDateTimeLocal(hoje);
    this.reserva.dataCheckout = this.formatDateTimeLocal(amanha);
  }

  definirDataMinima(): void {
    const agora = new Date();
    this.dataMinima = this.formatDateTimeLocal(agora);
    console.log('⏰ Data mínima permitida:', this.dataMinima);
  }

  formatDateTimeLocal(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  formatarDataHora(dataHora: string): string {
    if (!dataHora) return '';
    
    const data = new Date(dataHora);
    const dia = String(data.getDate()).padStart(2, '0');
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const ano = data.getFullYear();
    const hora = String(data.getHours()).padStart(2, '0');
    const minuto = String(data.getMinutes()).padStart(2, '0');
    
    return `${dia}/${mes}/${ano} às ${hora}:${minuto}`;
  }

  carregarApartamentos(): void {
  console.log('📋 Carregando apartamentos disponíveis...');
  
  if (this.reserva.dataCheckin && this.reserva.dataCheckout) {
    // ✅ Converter para ISO sem adicionar timezone
    const checkinDate = new Date(this.reserva.dataCheckin);
    const checkoutDate = new Date(this.reserva.dataCheckout);
    
    const checkinISO = new Date(checkinDate.getTime() - (checkinDate.getTimezoneOffset() * 60000))
      .toISOString();
    const checkoutISO = new Date(checkoutDate.getTime() - (checkoutDate.getTimezoneOffset() * 60000))
      .toISOString();
    
    // ✅ USAR NOVO ENDPOINT COM FILTROS
    const url = `http://localhost:8080/api/apartamentos/disponiveis?dataInicio=${checkinISO}&dataFim=${checkoutISO}`;
    
    console.log('═══════════════════════════════════════');
    console.log('🔍 BUSCANDO APARTAMENTOS DISPONÍVEIS');
    console.log('═══════════════════════════════════════');
    console.log('🕐 Check-in digitado:', this.reserva.dataCheckin);
    console.log('📤 Check-in enviado:', checkinISO);
    console.log('🕐 Check-out digitado:', this.reserva.dataCheckout);
    console.log('📤 Check-out enviado:', checkoutISO);
    console.log('🌐 URL:', url);
    console.log('═══════════════════════════════════════');
    
    this.http.get<any[]>(url).subscribe({
      next: (data) => {
        this.apartamentos = data;
        console.log('✅ Apartamentos DISPONÍVEIS carregados:', this.apartamentos.length);
        
        if (data.length === 0) {
          console.warn('⚠️ Nenhum apartamento disponível para o período!');
          alert('⚠️ Nenhum apartamento disponível para este período!\n\nTente outras datas.');
        }
        
        // ✅ Se veio do mapa com apartamento pré-selecionado
        this.route.queryParams.subscribe(params => {
          if (params['apartamentoId']) {
            const apartamentoId = Number(params['apartamentoId']);
            
            // Verificar se o apartamento está na lista de disponíveis
            const apartamentoDisponivel = this.apartamentos.find(a => a.id === apartamentoId);
            
            if (apartamentoDisponivel) {
              // ✅ APARTAMENTO DISPONÍVEL
              this.reserva.apartamentoId = apartamentoId;
              this.onApartamentoChange();
              console.log('✅ Apartamento do mapa selecionado:', apartamentoId);
              
            } else {
              // ❌ APARTAMENTO NÃO DISPONÍVEL
              console.warn('⚠️ Apartamento do mapa não está disponível para este período');
              
              // ✅ DESBLOQUEAR para permitir escolha de outro apartamento
              this.apartamentoBloqueado = false;
              
              // ✅ MOSTRAR BANNER DE AVISO
              alert(
                `⚠️ APARTAMENTO NÃO DISPONÍVEL!\n\n` +
                `O apartamento selecionado no mapa tem outra reserva neste período.\n\n` +
                `OPÇÕES:\n` +
                `1️⃣ Escolha OUTRO apartamento da lista\n` +
                `2️⃣ Ou altere as DATAS para liberar este apartamento`
              );
            }
          }
        });
      },
      error: (err) => {
        console.error('❌ Erro ao carregar apartamentos:', err);
        alert('❌ Erro ao carregar apartamentos disponíveis');
        this.apartamentos = [];
      }
    });
  } else {
    console.log('⏳ Aguardando datas para buscar apartamentos');
    this.apartamentos = [];
  }
}

  /**
 * 📅 RECARREGAR APARTAMENTOS AO MUDAR CHECK-IN
 */
onDataCheckinChange(): void {
  console.log('📅 Data de check-in alterada:', this.reserva.dataCheckin);
  
  // ✅ Validar se check-out já foi preenchido
  if (this.reserva.dataCheckin && this.reserva.dataCheckout) {
    const checkin = new Date(this.reserva.dataCheckin);
    const checkout = new Date(this.reserva.dataCheckout);
    
    // ✅ Garantir que checkout é posterior ao checkin
    if (checkout <= checkin) {
      alert('⚠️ Data de check-out deve ser posterior ao check-in!');
      return;
    }
    
    console.log('🔄 Recarregando apartamentos disponíveis...');
    this.carregarApartamentos();
  }
}

/**
 * 📅 RECARREGAR APARTAMENTOS AO MUDAR CHECK-OUT
 */
onDataCheckoutChange(): void {
  console.log('📅 Data de check-out alterada:', this.reserva.dataCheckout);
  
  // ✅ Validar se check-in já foi preenchido
  if (this.reserva.dataCheckin && this.reserva.dataCheckout) {
    const checkin = new Date(this.reserva.dataCheckin);
    const checkout = new Date(this.reserva.dataCheckout);
    
    // ✅ Garantir que checkout é posterior ao checkin
    if (checkout <= checkin) {
      alert('⚠️ Data de check-out deve ser posterior ao check-in!');
      return;
    }
    
    console.log('🔄 Recarregando apartamentos disponíveis...');
    this.carregarApartamentos();
  }
}

  filtrarClientes(): void {
    const busca = this.buscaCliente.trim();
    
    if (busca.length < 2) {
      this.clientesFiltrados = [];
      this.mostrarResultados = false;
      return;
    }

    this.http.get<any[]>(`http://localhost:8080/api/clientes/buscar?termo=${busca}`).subscribe({
      next: (data) => {
        this.clientesFiltrados = data;
        this.mostrarResultados = true;
      },
      error: (err) => {
        console.error('❌ Erro na busca:', err);
        this.clientesFiltrados = [];
        this.mostrarResultados = false;
      }
    });
  }

  selecionarCliente(cliente: any): void {
    if (!this.reserva.dataCheckin || !this.reserva.dataCheckout) {
      alert('⚠️ Selecione as datas de check-in e check-out antes de escolher o cliente!');
      return;
    }
    
    const payload = {
      clienteId: cliente.id,
      dataCheckin: new Date(this.reserva.dataCheckin).toISOString(),
      dataCheckout: new Date(this.reserva.dataCheckout).toISOString()
    };
    
    this.http.post<any>('http://localhost:8080/api/reservas/validar-hospede', payload).subscribe({
      next: (resposta) => {
        if (!resposta.disponivel) {
          this.mostrarBannerErro(`❌ CLIENTE INDISPONÍVEL!\n\n${resposta.mensagem}`);
          return;
        }
        
        this.adicionarClientePrincipal(cliente);
      },
      error: (erro) => {
        console.error('❌ ERRO AO VALIDAR:', erro);
        this.mostrarBannerErro('❌ Erro ao validar disponibilidade do cliente.');
      }
    });
  }

  private adicionarClientePrincipal(cliente: any): void {
    if (cliente.empresaId) {
      this.http.get<any>(`http://localhost:8080/api/empresas/${cliente.empresaId}`).subscribe({
        next: (empresa) => {
          cliente.empresa = empresa;
          this.clienteSelecionado = cliente;
        },
        error: (err) => {
          console.error('❌ Erro ao carregar empresa:', err);
          this.clienteSelecionado = cliente;
        }
      });
    } else {
      this.clienteSelecionado = cliente;
    }
    
    this.reserva.clienteId = cliente.id;
    this.buscaCliente = `${cliente.nome} - ${this.formatarCPF(cliente.cpf)}`;
    this.clientesFiltrados = [];
    this.mostrarResultados = false;

   this.hospedes = [{
  clienteId: cliente.id,
  nomeCompleto: cliente.nome,
  cpf: cliente.cpf || '',
  telefone: cliente.celular || '',
  placaCarro: null, // ✅ Será preenchida depois se informada
  cadastrarNovo: false
}];

    this.reserva.quantidadeHospede = 1;
    
    if (this.reserva.apartamentoId && this.reserva.dataCheckin && this.reserva.dataCheckout) {
      this.calcularDiarias();
    }

    console.log('✅ Cliente principal adicionado');
  }

  formatarCPF(cpf: string): string {
    if (!cpf) return '';
    const apenasNumeros = cpf.replace(/\D/g, '');
    return apenasNumeros.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }

  limparBuscaCliente(): void {
  this.buscaCliente = '';
  this.reserva.clienteId = 0;
  this.clienteSelecionado = null;
  this.clientesFiltrados = [];
  this.mostrarResultados = false;
  this.hospedes = [];
  this.reserva.quantidadeHospede = 0;
  this.placaTitular = ''; // ✅ Limpar placa do titular
}

  onApartamentoChange(): void {
    this.apartamentoSelecionado = this.apartamentos.find(a => a.id === this.reserva.apartamentoId) || null;
    
    if (this.apartamentoSelecionado?.tipoApartamentoId) {
      this.carregarDiarias(this.apartamentoSelecionado.tipoApartamentoId);
    }
  }

  carregarDiarias(tipoApartamentoId: number): void {
    this.diariaService.buscarPorTipoApartamento(tipoApartamentoId).subscribe({
      next: (data) => {
        this.diarias = data;
        this.calcularDiarias();
      },
      error: (err) => {
        console.error('❌ Erro ao carregar diárias:', err);
        this.diarias = [];
        this.valorDiaria = 0;
        this.valorEstimado = 0;
      }
    });
  }

  calcularDiarias(): void {
    if (!this.reserva.dataCheckin || !this.reserva.dataCheckout) return;

    const checkin = new Date(this.reserva.dataCheckin);
    const checkout = new Date(this.reserva.dataCheckout);
    
    const diffTime = checkout.getTime() - checkin.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    this.quantidadeDiarias = diffDays > 0 ? diffDays : 0;
    
    if (this.diarias.length > 0 && this.quantidadeDiarias > 0) {
      this.diariaAplicada = this.diarias
        .filter(d => d.quantidade <= this.quantidadeDiarias)
        .sort((a, b) => b.quantidade - a.quantidade)[0] || this.diarias[0];
      
      this.valorDiaria = this.diariaAplicada.valor;
      this.valorEstimado = this.quantidadeDiarias * this.valorDiaria;
    } else {
      this.valorDiaria = 0;
      this.valorEstimado = 0;
      this.diariaAplicada = null;
    }
  }

  abrirModalAdicionarHospede(): void {
    if (!this.reserva.clienteId) {
      this.mostrarBannerAviso('⚠️ Selecione o cliente principal primeiro!');
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
    if (modo === 'cadastrar') {
      this.limparFormularioNovoHospede();
    }
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
    const jaAdicionado = this.hospedes.some(h => h.clienteId === cliente.id);
    if (jaAdicionado) {
      this.mostrarBannerAviso('⚠️ Este hóspede já foi adicionado à lista!');
      return;
    }
    
    if (this.apartamentoSelecionado && this.hospedes.length >= this.apartamentoSelecionado.capacidade) {
      this.mostrarBannerErro(
        `❌ Capacidade máxima atingida!\n\n` +
        `O apartamento ${this.apartamentoSelecionado.numeroApartamento} ` +
        `suporta no máximo ${this.apartamentoSelecionado.capacidade} hóspede(s).`
      );
      return;
    }
    
    const payload = {
      clienteId: cliente.id,
      dataCheckin: new Date(this.reserva.dataCheckin).toISOString(),
      dataCheckout: new Date(this.reserva.dataCheckout).toISOString()
    };
    
    this.http.post<any>('http://localhost:8080/api/reservas/validar-hospede', payload).subscribe({
      next: (resposta) => {
        if (!resposta.disponivel) {
          this.mostrarBannerErro(`❌ HÓSPEDE INDISPONÍVEL!\n\n${resposta.mensagem}`);
          return;
        }
        
        this.adicionarHospedeNaLista(cliente);
      },
      error: (erro) => {
        console.error('❌ ERRO AO VALIDAR:', erro);
        this.mostrarBannerErro('❌ Erro ao validar disponibilidade do hóspede.');
      }
    });
  }

  private adicionarHospedeNaLista(cliente: any): void {
    this.hospedes.push({
      clienteId: cliente.id,
      nomeCompleto: cliente.nome,
      cpf: cliente.cpf || '',
      telefone: cliente.celular || '',
      cadastrarNovo: false
    });
    
    this.reserva.quantidadeHospede = this.hospedes.length;
    this.calcularDiarias();
    
    this.fecharModalAdicionarHospede();
    console.log('✅ Hóspede adicionado. Total:', this.hospedes.length);
  }

  limparFormularioNovoHospede(): void {
    this.novoHospede = {
      nome: '',
      cpf: '',
      telefone: '',
      placaCarro: ''
    };
  }

  salvarNovoHospede(): void {
    if (!this.novoHospede.nome || this.novoHospede.nome.trim() === '') {
      this.mostrarBannerErro('❌ Nome completo é obrigatório!');
      return;
    }

    // ✅ VALIDAR PLACA SE FOI PREENCHIDA
  if (this.novoHospede.placaCarro && !this.validarPlaca(this.novoHospede.placaCarro)) {
    this.mostrarBannerErro('❌ Placa inválida!\n\nFormato correto: ABC-1234 ou ABC-1D23');
    return;
  }
    
    if (this.novoHospede.cpf && this.novoHospede.cpf.trim() !== '') {
      const jaAdicionado = this.hospedes.some(h => h.cpf && h.cpf === this.novoHospede.cpf);
      if (jaAdicionado) {
        alert('❌ Já existe um hóspede com este CPF na lista!');
        return;
      }
    }
    
    if (this.apartamentoSelecionado && this.hospedes.length >= this.apartamentoSelecionado.capacidade) {
      alert(`❌ Capacidade máxima do apartamento atingida: ${this.apartamentoSelecionado.capacidade} hóspede(s)`);
      return;
    }
    
    this.hospedes.push({
  clienteId: null,
  nomeCompleto: this.novoHospede.nome,
  cpf: this.novoHospede.cpf || '',
  telefone: this.novoHospede.telefone || '',
  placaCarro: this.novoHospede.placaCarro || null,  // ✅ ADICIONAR
  cadastrarNovo: true
});
    
    this.reserva.quantidadeHospede = this.hospedes.length;
    this.calcularDiarias();
    
    this.fecharModalAdicionarHospede();
    console.log('✅ Novo hóspede adicionado. Total:', this.hospedes.length);
  }

  removerHospede(index: number): void {
    const hospede = this.hospedes[index];
    
    if (this.hospedes.length === 1) {
      this.mostrarBannerAviso('⚠️ É necessário ter pelo menos 1 hóspede na reserva!');
      return;
    }
    
    const confirma = confirm(`Deseja remover ${hospede.nomeCompleto} da lista?`);
    
    if (confirma) {
      this.hospedes.splice(index, 1);
      this.reserva.quantidadeHospede = this.hospedes.length;
      this.calcularDiarias();
      
      this.mostrarBannerSucesso(`✅ ${hospede.nomeCompleto} removido(a) da lista!`);
      console.log('🗑️ Hóspede removido. Total:', this.hospedes.length);
    }
  }
 
  salvar(): void {
  if (!this.validarFormulario()) {
    return;
  }

  this.loading = true;
  this.errorMessage = '';

  // ✅ CORRIGIR: Converter datas para ISO sem adicionar timezone
  const checkinDate = new Date(this.reserva.dataCheckin);
  const checkoutDate = new Date(this.reserva.dataCheckout);
  
  // ✅ Remover offset de timezone
  const checkinISO = new Date(checkinDate.getTime() - (checkinDate.getTimezoneOffset() * 60000))
    .toISOString();
  const checkoutISO = new Date(checkoutDate.getTime() - (checkoutDate.getTimezoneOffset() * 60000))
    .toISOString();

  console.log('═══════════════════════════════════════');
  console.log('📤 ENVIANDO RESERVA PARA BACKEND');
  console.log('═══════════════════════════════════════');
  console.log('🕐 Check-in digitado:', this.reserva.dataCheckin);
  console.log('📤 Check-in enviado:', checkinISO);
  console.log('🕐 Check-out digitado:', this.reserva.dataCheckout);
  console.log('📤 Check-out enviado:', checkoutISO);

  // ✅ Atualizar placa do titular se foi informada
if (this.placaTitular && this.hospedes.length > 0) {
  // Validar placa do titular
  if (!this.validarPlaca(this.placaTitular)) {
    this.loading = false;
    this.mostrarBannerErro('❌ Placa do titular inválida!\n\nFormato correto: ABC-1234 ou ABC-1D23');
    return;
  }
  this.hospedes[0].placaCarro = this.placaTitular;
}

const reservaRequest: any = {
  clienteId: Number(this.reserva.clienteId),
  apartamentoId: Number(this.reserva.apartamentoId),
  quantidadeHospede: Number(this.reserva.quantidadeHospede),
  dataCheckin: checkinISO,
  dataCheckout: checkoutISO,
  hospedes: this.hospedes
};

  console.log('📦 Request completo:', reservaRequest);
  console.log('═══════════════════════════════════════');

  this.reservaService.create(reservaRequest).subscribe({
    next: (response) => {
      console.log('✅ Reserva criada com sucesso:', response);
      
      if (this.voltarParaMapa) {
        this.router.navigate(['/reservas/mapa']);
      } else {
        this.router.navigate(['/reservas']);
      }
    },
    error: (err) => {
      console.error('❌ Erro ao criar reserva:', err);
      this.loading = false;
      this.errorMessage = err.error?.message || err.error || 'Erro ao criar reserva';
    }
  });
}
 
  aoSelecionarApartamento(): void {
    const apartamentoId = this.reserva.apartamentoId;

    if (!apartamentoId || apartamentoId === 0) {
      return;
    }

    this.apartamentoService.verificarCheckoutVencido(apartamentoId).subscribe({
      next: (response) => {
        if (response.temCheckoutVencido) {
          alert(
            `⚠️ APARTAMENTO COM CHECKOUT VENCIDO!\n\n` +
            `Hóspede: ${response.hospedeNome}\n` +
            `Checkout previsto: ${new Date(response.checkoutPrevisto).toLocaleString('pt-BR')}\n` +
            `Atraso: ${response.horasAtraso} hora(s)\n\n` +
            `É necessário fazer o checkout antes de criar nova reserva.`
          );

          this.reserva.apartamentoId = 0;
          this.apartamentoSelecionado = null;
        } else {
          this.onApartamentoChange();
        }
      },
      error: (error) => {
        console.error('❌ Erro ao verificar apartamento:', error);
      }
    });
  }

  validarFormulario(): boolean {
    if (!this.reserva.clienteId || this.reserva.clienteId === 0) {
      this.errorMessage = 'Selecione o cliente';
      return false;
    }
    
    if (!this.reserva.apartamentoId || this.reserva.apartamentoId === 0) {
      this.errorMessage = 'Selecione o apartamento';
      return false;
    }
    
    if (this.hospedes.length === 0) {
      this.errorMessage = 'É obrigatório cadastrar pelo menos 1 hóspede';
      this.mostrarBannerErro('❌ Adicione pelo menos 1 hóspede para continuar!');
      return false;
    }

    if (this.hospedes.length !== this.reserva.quantidadeHospede) {
      this.errorMessage = `Quantidade de hóspedes inconsistente`;
      return false;
    }

    if (this.apartamentoSelecionado && this.hospedes.length > this.apartamentoSelecionado.capacidade) {
      this.errorMessage = `Quantidade de hóspedes excede a capacidade do apartamento`;
      return false;
    }
    
    if (!this.reserva.dataCheckin) {
      this.errorMessage = 'Data de check-in é obrigatória';
      return false;
    }
    
    if (!this.reserva.dataCheckout) {
      this.errorMessage = 'Data de check-out é obrigatória';
      return false;
    }
    
    const checkin = new Date(this.reserva.dataCheckin);
    const checkout = new Date(this.reserva.dataCheckout);
    
    if (checkout <= checkin) {
      this.errorMessage = 'Data de check-out deve ser posterior ao check-in';
      return false;
    }
    
    return true;
  }

  mostrarBannerErro(mensagem: string): void {
    this.mensagemBanner = mensagem;
    this.tipoBanner = 'erro';
    this.mostrarBanner = true;
    
    setTimeout(() => {
      this.fecharBanner();
    }, 8000);
  }

  mostrarBannerSucesso(mensagem: string): void {
    this.mensagemBanner = mensagem;
    this.tipoBanner = 'sucesso';
    this.mostrarBanner = true;
    
    setTimeout(() => {
      this.fecharBanner();
    }, 5000);
  }

  mostrarBannerAviso(mensagem: string): void {
    this.mensagemBanner = mensagem;
    this.tipoBanner = 'aviso';
    this.mostrarBanner = true;
    
    setTimeout(() => {
      this.fecharBanner();
    }, 6000);
  }

  fecharBanner(): void {
    this.mostrarBanner = false;
    setTimeout(() => {
      this.mensagemBanner = '';
    }, 300);
  }

  /**
 * 🚗 VALIDAR PLACA BRASILEIRA
 */
validarPlaca(placa: string): boolean {
  if (!placa || placa.trim() === '') return true; // Placa é opcional
  
  const placaLimpa = placa.replace(/[\s-]/g, '').toUpperCase();
  
  const padraoAntigo = /^[A-Z]{3}[0-9]{4}$/;
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

/**
 * 🚗 FORMATAR PLACA DO TITULAR
 */
formatarPlacaTitular(): void {
  if (!this.placaTitular) return;
  
  let placa = this.placaTitular.toUpperCase().replace(/[^A-Z0-9]/g, '');
  
  if (placa.length > 3) {
    placa = placa.substring(0, 3) + '-' + placa.substring(3, 7);
  }
  
  this.placaTitular = placa;
}

/**
 * 🚗 FORMATAR PLACA DO HÓSPEDE EXISTENTE
 */
formatarPlacaHospedeExistente(): void {
  if (!this.placaHospedeExistente) return;
  
  let placa = this.placaHospedeExistente.toUpperCase().replace(/[^A-Z0-9]/g, '');
  
  if (placa.length > 3) {
    placa = placa.substring(0, 3) + '-' + placa.substring(3, 7);
  }
  
  this.placaHospedeExistente = placa;
}

/**
 * 📝 SELECIONAR CLIENTE PARA PEDIR PLACA (não adiciona direto)
 */
selecionarClienteParaPlaca(cliente: any): void {
  const jaAdicionado = this.hospedes.some(h => h.clienteId === cliente.id);
  if (jaAdicionado) {
    this.mostrarBannerAviso('⚠️ Este hóspede já foi adicionado à lista!');
    return;
  }
  
  if (this.apartamentoSelecionado && this.hospedes.length >= this.apartamentoSelecionado.capacidade) {
    this.mostrarBannerErro(
      `❌ Capacidade máxima atingida!\n\n` +
      `O apartamento ${this.apartamentoSelecionado.numeroApartamento} ` +
      `suporta no máximo ${this.apartamentoSelecionado.capacidade} hóspede(s).`
    );
    return;
  }
  
  // Validar disponibilidade do hóspede
  const payload = {
    clienteId: cliente.id,
    dataCheckin: new Date(this.reserva.dataCheckin).toISOString(),
    dataCheckout: new Date(this.reserva.dataCheckout).toISOString()
  };
  
  this.http.post<any>('http://localhost:8080/api/reservas/validar-hospede', payload).subscribe({
    next: (resposta) => {
      if (!resposta.disponivel) {
        this.mostrarBannerErro(`❌ HÓSPEDE INDISPONÍVEL!\n\n${resposta.mensagem}`);
        return;
      }
      
      // ✅ Mostrar formulário intermediário
      this.hospedeExistenteSelecionado = cliente;
      this.placaHospedeExistente = '';
    },
    error: (erro) => {
      console.error('❌ ERRO AO VALIDAR:', erro);
      this.mostrarBannerErro('❌ Erro ao validar disponibilidade do hóspede.');
    }
  });
}

/**
 * ✅ CONFIRMAR HÓSPEDE EXISTENTE COM PLACA
 */
confirmarHospedeExistente(): void {
  // Validar placa se foi preenchida
  if (this.placaHospedeExistente && !this.validarPlaca(this.placaHospedeExistente)) {
    this.mostrarBannerErro('❌ Placa inválida!\n\nFormato correto: ABC-1234 ou ABC-1D23');
    return;
  }
  
  // Adicionar hóspede na lista
  this.hospedes.push({
    clienteId: this.hospedeExistenteSelecionado.id,
    nomeCompleto: this.hospedeExistenteSelecionado.nome,
    cpf: this.hospedeExistenteSelecionado.cpf || '',
    telefone: this.hospedeExistenteSelecionado.celular || '',
    placaCarro: this.placaHospedeExistente || null,
    cadastrarNovo: false
  });
  
  this.reserva.quantidadeHospede = this.hospedes.length;
  this.calcularDiarias();
  
  // Limpar seleção
  this.hospedeExistenteSelecionado = null;
  this.placaHospedeExistente = '';
  
  this.fecharModalAdicionarHospede();
  console.log('✅ Hóspede existente adicionado com placa. Total:', this.hospedes.length);
}

/**
 * ❌ CANCELAR SELEÇÃO DE HÓSPEDE EXISTENTE
 */
cancelarSelecaoHospedeExistente(): void {
  this.hospedeExistenteSelecionado = null;
  this.placaHospedeExistente = '';
}

  voltar(): void {
    if (this.voltarParaMapa) {
      this.router.navigate(['/reservas/mapa']);
    } else {
      this.router.navigate(['/reservas']);
    }
  }
}