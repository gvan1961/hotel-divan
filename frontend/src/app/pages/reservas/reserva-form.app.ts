import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ReservaService } from '../../services/reserva.service';
import { ClienteService } from '../../services/cliente.service';
import { ApartamentoService } from '../../services/apartamento.service';
import { DiariaService } from '../../services/diaria.service';
import { ReservaRequest } from '../../models/reserva.model';
import { Apartamento } from '../../models/apartamento.model';
import { Diaria } from '../../models/diaria.model';
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-reserva-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">

      <!-- BANNER -->
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
          <div class="banner-texto">{{ mensagemBanner }}</div>
          <button class="banner-fechar" (click)="fecharBanner()">✕</button>
        </div>
      </div>

      <!-- HEADER -->
      <div class="header">
        <h1>🏨 Nova Reserva</h1>
        <button class="btn-back" (click)="voltar()">← Voltar</button>
      </div>

      <!-- FORMULÁRIO -->
      <div class="form-card">
        <form (ngSubmit)="salvar()">

          <!-- BUSCA CLIENTE TITULAR -->
          <div class="form-group campo-busca">
            <label>Cliente Titular *</label>
            <div class="busca-wrapper">
              <input type="text" [(ngModel)]="buscaCliente" name="buscaCliente"
                (input)="filtrarClientes()" (focus)="filtrarClientes()"
                placeholder="Digite o nome ou CPF do cliente..."
                class="input-busca" autocomplete="off">
              <button type="button" class="btn-limpar-busca" *ngIf="buscaCliente"
                (click)="limparBuscaCliente()">✕</button>
            </div>

            <div class="resultados-busca" *ngIf="mostrarResultados && clientesFiltrados.length > 0">
              <div class="resultado-item" *ngFor="let cliente of clientesFiltrados"
                (click)="selecionarCliente(cliente)">
                <div class="resultado-nome">{{ cliente.nome }}</div>
                <div class="resultado-cpf">CPF: {{ formatarCPF(cliente.cpf) }}</div>
                <div class="resultado-info" *ngIf="cliente.celular">📞 {{ cliente.celular }}</div>
              </div>
            </div>

           <div class="sem-resultado" *ngIf="mostrarResultados && clientesFiltrados.length === 0 && buscaCliente.length >= 2">
  ❌ Nenhum cliente encontrado.
  <button type="button" class="btn-link" (click)="abrirCadastroTitular()">
    ➕ Cadastrar novo cliente
  </button>
</div>

          <!-- CLIENTE SELECIONADO -->
          <div class="cliente-selecionado" *ngIf="clienteSelecionado">
            <div class="cliente-header">
              <h3>✅ Cliente Selecionado (Titular)</h3>
              <button type="button" class="btn-trocar-cliente" (click)="limparBuscaCliente()">🔄 Trocar</button>
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
            <div class="placa-titular-campo">
              <label>🚗 Placa do Veículo do Titular <small>(opcional)</small></label>
              <input type="text" [(ngModel)]="placaTitular" name="placaTitular"
                (input)="formatarPlacaTitular()" placeholder="ABC-1234" maxlength="8"
                class="input-placa-titular">
              <small class="field-help">Formato: ABC-1234 ou ABC-1D23 (Mercosul)</small>
            </div>
          </div>
          </div>

          <!-- APARTAMENTO E QUANTIDADE -->
          <div class="form-row">
            <div class="form-group">
              <label>Apartamento *</label>
              <div class="aviso-mapa" *ngIf="apartamentoBloqueado">
                <span class="icone">🔒</span>
                <div class="aviso-texto">
                  <strong>Apartamento pré-selecionado</strong>
                  <p>Este apartamento foi escolhido na tela anterior e não pode ser alterado aqui</p>
                </div>
              </div>
              <select [(ngModel)]="reserva.apartamentoId" name="apartamentoId" required
                [disabled]="apartamentoBloqueado" (change)="aoSelecionarApartamento()">
                <option [ngValue]="0">Selecione o apartamento</option>
                <option *ngFor="let apt of apartamentos" [ngValue]="apt.id">
                  {{ apt.numeroApartamento }} - {{ apt.tipoApartamento?.tipo || apt.tipoApartamentoNome || 'Sem tipo' }} (Cap: {{ apt.capacidade }})
                </option>
              </select>
              <small class="field-help" *ngIf="apartamentoSelecionado && !apartamentoBloqueado">
                ✅ Apt {{ apartamentoSelecionado.numeroApartamento }} - Cap: {{ apartamentoSelecionado.capacidade }} pessoa(s)
              </small>
            </div>
            <div class="form-group">
              <label>Quantidade de Hóspedes *</label>
              <input type="number" [(ngModel)]="reserva.quantidadeHospede" name="quantidadeHospede"
                required min="1" [max]="apartamentoSelecionado?.capacidade || 10"
                [disabled]="true" readonly />
              <small class="field-help">Calculado automaticamente: {{ hospedes.length }} hóspede(s)</small>
            </div>
          </div>

          <!-- DATAS -->
          <div class="form-row">
            <div class="form-group">
              <label>🗓️ Data e Hora de Check-in *</label>
              <div style="display:flex; gap:8px; align-items:center; flex-wrap:wrap;">
                <input type="date" [(ngModel)]="checkinData" name="checkinData"
                  (change)="montarDataCheckin()" style="flex:1; min-width:140px;" />
                <select [(ngModel)]="checkinHora" name="checkinHora"
                  (change)="montarDataCheckin()" style="width:80px;">
                  <option *ngFor="let h of horas" [value]="h">{{h}}h</option>
                </select>
                <select [(ngModel)]="checkinMinuto" name="checkinMinuto"
                  (change)="montarDataCheckin()" style="width:80px;">
                  <option *ngFor="let m of minutos" [value]="m">{{m}}min</option>
                </select>
              </div>
              <small class="field-help" *ngIf="reserva.dataCheckin">{{ formatarDataHora(reserva.dataCheckin) }}</small>
            </div>
            <div class="form-group">
              <label>🗓️ Data e Hora de Check-out *</label>
              <input type="datetime-local" [(ngModel)]="reserva.dataCheckout" name="dataCheckout"
                required [min]="reserva.dataCheckin || dataMinima" (change)="onDataChange()" />
              <small class="field-help" *ngIf="reserva.dataCheckout && quantidadeDiarias > 0">
                {{ formatarDataHora(reserva.dataCheckout) }} - Total: {{ quantidadeDiarias }} diária(s)
              </small>
            </div>
          </div>

          <!-- HÓSPEDES -->
          <div class="secao-hospedes" *ngIf="reserva.clienteId">
            <div class="secao-header">
              <h3>👥 Hóspedes da Reserva</h3>
              <span class="badge-hospedes">{{ hospedes.length }}</span>
              <button type="button" class="btn-adicionar-hospede" (click)="abrirModalAdicionarHospede()">
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
                  </div>
                  <div class="hospede-detalhes">
                    CPF: {{ hospede.cpf || 'Não informado' }} | Tel: {{ hospede.telefone || 'Não informado' }}
                  </div>
                </div>
                <button type="button" class="btn-remover-hospede" (click)="removerHospede(i)">🗑️</button>
                <span *ngIf="i === 0" class="hospede-bloqueado">🔒</span>
              </div>
            </div>
            <div class="aviso-hospedes" *ngIf="hospedes.length === 0">
              ⚠️ Nenhum hóspede cadastrado. Selecione o cliente titular primeiro.
            </div>
          </div>

          <div *ngIf="errorMessage" class="error-message">{{ errorMessage }}</div>

          <div class="form-actions">
            <button type="button" class="btn-cancel" (click)="voltar()">Cancelar</button>
            <button type="submit" class="btn-save" [disabled]="loading || hospedes.length === 0">
              {{ loading ? 'Criando...' : 'Criar Reserva' }}
            </button>
          </div>
        </form>
      </div>
    </div>

   <!-- MODAL ADICIONAR HÓSPEDE -->
<div class="modal-overlay" *ngIf="modalAdicionarHospede" (click)="fecharModalAdicionarHospede()">
  <div class="modal-content" (click)="$event.stopPropagation()">
    <button type="button" class="btn-fechar-modal" (click)="fecharModalAdicionarHospede()">✕</button>
    <h2>👤 Adicionar Hóspede</h2>

    <!-- ABAS -->
    <div class="modal-tabs">
      <button type="button" [class.active]="modoFormHospede === 'buscar'"
              (click)="modoFormHospede = 'buscar'">
        🔍 Buscar Existente
      </button>
      <button type="button" [class.active]="modoFormHospede === 'cadastrar'"
              (click)="modoFormHospede = 'cadastrar'">
        ➕ Cadastrar Novo
      </button>
    </div>

    <!-- ABA BUSCAR -->
    <div *ngIf="modoFormHospede === 'buscar'">
      <div *ngIf="!hospedeExistenteSelecionado">
        <input type="text" [(ngModel)]="termoBuscaHospede"
          (input)="buscarClientesModal()"
          placeholder="Digite nome ou CPF (mínimo 2 caracteres)"
          class="input-busca-modal">

        <div class="resultados-modal" *ngIf="clientesFiltradosModal.length > 0">
          <div class="resultado-modal-item" *ngFor="let c of clientesFiltradosModal"
            (click)="selecionarHospedeParaConfirmar(c)">
            <div class="resultado-nome">{{ c.nome }}</div>
            <div class="resultado-info">
              CPF: {{ formatarCPF(c.cpf) }} | Tel: {{ c.celular || 'Não informado' }}
            </div>
          </div>
        </div>

        <div *ngIf="termoBuscaHospede.length >= 2 && clientesFiltradosModal.length === 0"
             class="sem-resultado-modal">
          ❌ Nenhum cliente encontrado.
          <button type="button" class="btn-link" (click)="modoFormHospede = 'cadastrar'">
            Cadastrar novo →
          </button>
        </div>
      </div>

      <div *ngIf="hospedeExistenteSelecionado" class="hospede-existente-form">
        <h4>✅ Cliente Selecionado</h4>
        <div class="info-readonly">👤 {{ hospedeExistenteSelecionado.nome }}</div>
        <div class="info-readonly">📄 CPF: {{ formatarCPF(hospedeExistenteSelecionado.cpf) }}</div>
        <div class="info-readonly" *ngIf="hospedeExistenteSelecionado.celular">
          📞 {{ hospedeExistenteSelecionado.celular }}
        </div>
        <div class="form-placa-existente">
          <label>🚗 Placa do Veículo <small>(opcional)</small></label>
          <input type="text" [(ngModel)]="placaHospedeExistente"
            (input)="formatarPlacaHospedeExistente()" placeholder="ABC-1234" maxlength="8">
        </div>
        <div class="btns-hospede-existente">
          <button type="button" class="btn-cancelar-hospede"
            (click)="hospedeExistenteSelecionado = null">← Voltar</button>
          <button type="button" class="btn-confirmar-hospede"
            (click)="confirmarHospedeExistente()">✅ Adicionar</button>
        </div>
      </div>
    </div>

    <!-- ABA CADASTRAR NOVO -->
    <div *ngIf="modoFormHospede === 'cadastrar'" class="modal-tab-content">

      <div class="form-group checkbox-group">
        <label class="checkbox-label">
          <input type="checkbox" [(ngModel)]="novoHospedeForm.menorDeIdade"
                 (change)="novoHospedeForm.menorDeIdade && (novoHospedeForm.cpf = '')">
          <span>👶 Menor de Idade (sem CPF)</span>
        </label>
      </div>

      <div class="form-group">
        <label>Nome Completo *</label>
        <input type="text" [(ngModel)]="novoHospedeForm.nome"
               placeholder="Nome completo do hóspede">
      </div>

    <div class="form-group" *ngIf="!novoHospedeForm.menorDeIdade">
  <label>CPF *</label>
  <input type="text"
       [(ngModel)]="novoHospedeForm.cpf"
       (ngModelChange)="novoHospedeForm.cpf = formatarCPFInput($event)"
       placeholder="000.000.000-00"
       maxlength="14">
  <small style="color:red" *ngIf="cpfNovoHospedeInvalido">
    ❌ CPF inválido
  </small>
</div>

     <div class="form-group">
  <label>Celular</label>
  <input type="text"
         [(ngModel)]="novoHospedeForm.celular"
         (ngModelChange)="novoHospedeForm.celular = formatarCelularInput($event)"
         placeholder="(00) 00000-0000"
         maxlength="15">
</div>

      <div class="form-group">
        <label>Empresa <small>(opcional)</small></label>
        <select [(ngModel)]="novoHospedeForm.empresaId">
          <option [ngValue]="null">Sem empresa</option>
          <option *ngFor="let emp of empresas" [ngValue]="emp.id">
            {{ emp.nomeEmpresa }}
          </option>
        </select>
      </div>

      <div class="form-group">
        <label>🚗 Placa <small>(opcional)</small></label>
        <input type="text" [(ngModel)]="novoHospedeForm.placaCarro"
               placeholder="ABC-1234" maxlength="8"
               style="text-transform:uppercase">
      </div>

      <div class="form-group">
        <label>Data de Nascimento</label>
        <input type="date" [(ngModel)]="novoHospedeForm.dataNascimento">
      </div>

      <div style="display:flex; gap:20px; flex-wrap:wrap;">
        <div class="form-group checkbox-group">
          <label class="checkbox-label">
            <input type="checkbox" [(ngModel)]="novoHospedeForm.creditoAprovado">
            <span>✅ Crédito Aprovado</span>
          </label>
        </div>
        <div class="form-group checkbox-group">
          <label class="checkbox-label">
            <input type="checkbox" [(ngModel)]="novoHospedeForm.autorizadoJantar">
            <span>🍽️ Autorizado Jantar</span>
          </label>
        </div>
      </div>

      <div class="info-cadastro">
        ℹ️ Só o nome é obrigatório. O cadastro completo pode ser feito depois.
      </div>

      <div class="btns-hospede-existente">
        <button type="button" class="btn-cancelar-hospede"
                (click)="modoFormHospede = 'buscar'">← Voltar</button>
        <button type="button" class="btn-confirmar-hospede"
                (click)="adicionarNovoHospedeForm()">✅ Adicionar</button>
      </div>
    </div>

  </div>
</div>
    
  `,
  styles: [`
    .container { padding: 20px; max-width: 900px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h1 { color: #333; margin: 0; }
    .btn-back { background: #6c757d; color: white; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }
    .form-card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; }
    .form-group { margin-bottom: 20px; }
    label { display: block; margin-bottom: 5px; color: #555; font-weight: 500; }
    input, select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; box-sizing: border-box; }
    input:focus, select:focus { outline: none; border-color: #667eea; }
    .field-help { display: block; font-size: 12px; color: #666; margin-top: 4px; font-style: italic; }
    .campo-busca { position: relative; }
    .busca-wrapper { position: relative; display: flex; align-items: center; }
    .input-busca { width: 100%; padding: 12px; padding-right: 40px; border: 2px solid #ddd; border-radius: 6px; font-size: 14px; }
    .input-busca:focus { outline: none; border-color: #667eea; }
    .btn-limpar-busca { position: absolute; right: 10px; width: 30px; height: 30px; background: #e0e0e0; border: none; border-radius: 50%; cursor: pointer; }
    .resultados-busca { position: absolute; top: 100%; left: 0; right: 0; background: white; border: 2px solid #667eea; border-top: none; border-radius: 0 0 6px 6px; max-height: 300px; overflow-y: auto; box-shadow: 0 4px 12px rgba(0,0,0,0.15); z-index: 1000; }
    .resultado-item { padding: 12px 15px; cursor: pointer; border-bottom: 1px solid #f0f0f0; }
    .resultado-item:hover { background: #f5f5f5; }
    .resultado-nome { font-weight: 600; color: #2c3e50; margin-bottom: 4px; }
    .resultado-cpf { font-size: 0.9em; color: #7f8c8d; }
    .resultado-info { font-size: 0.85em; color: #95a5a6; }
    .sem-resultado { padding: 16px; background: #fff3cd; border: 1px solid #ffc107; border-radius: 6px; color: #856404; font-size: 0.9em; margin-top: 8px; }
    .error-message { background: #fee; color: #c33; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
    .form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 30px; }
    .btn-cancel { padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; background: #6c757d; color: white; }
    .btn-save { padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; background: #28a745; color: white; }
    .btn-save:disabled { background: #ccc; cursor: not-allowed; }
    select:disabled { background: #f0f0f0; color: #666; cursor: not-allowed; }
    .aviso-mapa { display: flex; align-items: center; gap: 12px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px; border-radius: 8px; margin-bottom: 10px; }
    .aviso-mapa .icone { font-size: 2em; }
    .aviso-mapa .aviso-texto { flex: 1; }
    .aviso-mapa strong { display: block; font-size: 1.1em; margin-bottom: 4px; }
    .aviso-mapa p { margin: 0; font-size: 0.9em; }
    .cliente-selecionado { background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%); border: 2px solid #4caf50; border-radius: 10px; padding: 20px; margin: 20px 0; }
    .cliente-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 2px solid #4caf50; }
    .cliente-header h3 { margin: 0; color: #2e7d32; }
    .btn-trocar-cliente { background: #ff9800; color: white; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-weight: 600; }
    .cliente-info { display: flex; flex-direction: column; gap: 10px; }
    .cliente-info .info-item { display: flex; justify-content: space-between; align-items: center; background: white; padding: 10px 15px; border-radius: 6px; border-left: 3px solid #4caf50; }
    .cliente-info .info-item.info-empresa { background: #e3f2fd; border-left: 3px solid #1976d2; }
    .cliente-info .label { font-weight: 600; color: #555; }
    .cliente-info .value { color: #2c3e50; font-weight: 500; }
    .cliente-info .value-empresa { color: #1565c0; font-weight: 700; }
    .placa-titular-campo { margin-top: 15px; padding: 15px; background: white; border-radius: 6px; border-left: 3px solid #2196f3; }
    .placa-titular-campo label { color: #1565c0; font-weight: 600; margin-bottom: 8px; }
    .input-placa-titular { width: 100%; padding: 10px; border: 2px solid #2196f3; border-radius: 5px; font-family: 'Courier New', monospace; font-weight: bold; letter-spacing: 1px; text-transform: uppercase; box-sizing: border-box; }
    .secao-hospedes { background: #f8f9fa; border: 2px solid #dee2e6; border-radius: 8px; padding: 20px; margin: 25px 0; }
    .secao-header { display: flex; align-items: center; gap: 15px; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 2px solid #dee2e6; }
    .secao-header h3 { margin: 0; color: #2c3e50; flex: 1; }
    .badge-hospedes { background: #667eea; color: white; padding: 5px 12px; border-radius: 20px; font-weight: 600; }
    .btn-adicionar-hospede { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: 600; }
    .lista-hospedes { display: flex; flex-direction: column; gap: 12px; }
    .hospede-item { display: flex; align-items: center; gap: 15px; background: white; padding: 15px; border-radius: 6px; border: 1px solid #dee2e6; }
    .hospede-numero { display: flex; align-items: center; justify-content: center; width: 35px; height: 35px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border-radius: 50%; font-weight: 700; flex-shrink: 0; }
    .hospede-info { flex: 1; }
    .hospede-nome { font-weight: 600; color: #2c3e50; margin-bottom: 5px; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
    .badge-titular { background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%); color: white; padding: 3px 10px; border-radius: 12px; font-size: 0.75em; font-weight: 700; }
    .hospede-detalhes { font-size: 0.9em; color: #7f8c8d; }
    .btn-remover-hospede { background: #e74c3c; color: white; border: none; width: 40px; height: 40px; border-radius: 50%; cursor: pointer; font-size: 1.2em; flex-shrink: 0; }
    .hospede-bloqueado { font-size: 1.5em; color: #95a5a6; opacity: 0.6; }
    .aviso-hospedes { background: #fff3cd; border: 1px solid #ffc107; border-radius: 6px; padding: 15px; text-align: center; color: #856404; font-weight: 500; }
    .modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 9999; }
    .modal-content { background: white; border-radius: 12px; padding: 30px; max-width: 550px; width: 90%; max-height: 80vh; overflow-y: auto; position: relative; box-shadow: 0 10px 40px rgba(0,0,0,0.3); }
    .modal-content h2 { margin: 0 0 20px 0; color: #2c3e50; }
    .input-busca-modal { width: 100%; padding: 12px; border: 2px solid #dee2e6; border-radius: 6px; font-size: 14px; margin-bottom: 15px; box-sizing: border-box; }
    .input-busca-modal:focus { outline: none; border-color: #667eea; }
    .resultados-modal { max-height: 300px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: 6px; }
    .resultado-modal-item { padding: 12px; cursor: pointer; border-bottom: 1px solid #f0f0f0; }
    .resultado-modal-item:hover { background: #f8f9fa; }
    .sem-resultado-modal { padding: 16px; background: #fff3cd; border: 1px solid #ffc107; border-radius: 6px; color: #856404; font-size: 0.9em; margin-top: 8px; }
    .hospede-existente-form { background: #f8f9fa; border: 2px solid #667eea; border-radius: 8px; padding: 20px; margin-top: 15px; }
    .hospede-existente-form h4 { margin: 0 0 15px 0; color: #667eea; }
    .info-readonly { background: white; padding: 10px; border-radius: 4px; margin-bottom: 8px; font-size: 0.9em; color: #555; }
    .form-placa-existente { margin-top: 15px; }
    .form-placa-existente label { display: block; margin-bottom: 5px; font-weight: 600; }
    .form-placa-existente input { width: 100%; padding: 10px; border: 2px solid #667eea; border-radius: 5px; font-family: 'Courier New', monospace; font-weight: bold; text-transform: uppercase; box-sizing: border-box; }
    .btns-hospede-existente { display: flex; gap: 10px; margin-top: 15px; }
    .btn-confirmar-hospede { flex: 1; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none; padding: 12px; border-radius: 6px; cursor: pointer; font-weight: 600; }
    .btn-cancelar-hospede { flex: 1; background: #6c757d; color: white; border: none; padding: 12px; border-radius: 6px; cursor: pointer; font-weight: 600; }
    .btn-fechar-modal { position: absolute; top: 15px; right: 15px; background: #e74c3c; color: white; border: none; width: 35px; height: 35px; border-radius: 50%; cursor: pointer; font-size: 1.2em; font-weight: bold; display: flex; align-items: center; justify-content: center; }
    .banner-notificacao { position: fixed; top: -200px; left: 50%; transform: translateX(-50%); width: 90%; max-width: 800px; z-index: 10000; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.3); transition: top 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55); }
    .banner-notificacao.mostrar { top: 20px; }
    .banner-conteudo { display: flex; align-items: center; padding: 20px 25px; gap: 15px; }
    .banner-icone { font-size: 2.5em; flex-shrink: 0; }
    .banner-texto { flex: 1; font-size: 1.1em; line-height: 1.5; font-weight: 500; white-space: pre-line; }
    .banner-fechar { background: rgba(255,255,255,0.3); border: none; width: 35px; height: 35px; border-radius: 50%; cursor: pointer; font-size: 1.4em; color: white; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
    .banner-erro { background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%); color: white; border: 3px solid #a93226; }
    .banner-sucesso { background: linear-gradient(135deg, #27ae60 0%, #229954 100%); color: white; border: 3px solid #1e8449; }
    .banner-aviso { background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%); color: white; border: 3px solid #d68910; }
    @media (max-width: 768px) { .form-row { grid-template-columns: 1fr; } }

    /* ── CADASTRAR NOVO CLIENTE ─────────────────── */
.btn-link {
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  font-weight: 700;
  font-size: 0.95em;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
  text-decoration: underline;
}
.btn-link:hover {
  background: #f0f0ff;
  color: #764ba2;
}

/* ── ABAS DO MODAL ──────────────────────────── */
.modal-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 20px;
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
  font-size: 0.95em;
  transition: all 0.2s;
}
.modal-tabs button:hover {
  color: #667eea;
  background: #f8f9fa;
}
.modal-tabs button.active {
  color: #667eea;
  border-bottom-color: #667eea;
  background: #f0f0ff;
}

/* ── ABA CADASTRAR NOVO ─────────────────────── */
.modal-tab-content {
  padding: 5px 0;
}
.modal-tab-content .form-group {
  margin-bottom: 14px;
}
.modal-tab-content .form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.9em;
}
.modal-tab-content .form-group input,
.modal-tab-content .form-group select {
  width: 100%;
  padding: 9px 12px;
  border: 2px solid #dee2e6;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}
.modal-tab-content .form-group input:focus,
.modal-tab-content .form-group select:focus {
  outline: none;
  border-color: #667eea;
}
.checkbox-group {
  margin-bottom: 10px;
}
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: 600;
  color: #2c3e50;
}
.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}
.info-cadastro {
  background: #e3f2fd;
  border-left: 4px solid #2196f3;
  padding: 10px 14px;
  border-radius: 4px;
  margin: 12px 0;
  font-size: 0.88em;
  color: #1565c0;
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
  private cdr = inject(ChangeDetectorRef);

  reserva: ReservaRequest = { clienteId: 0, apartamentoId: 0, quantidadeHospede: 1, dataCheckin: '', dataCheckout: '' };
  apartamentos: Apartamento[] = [];
  apartamentoSelecionado: Apartamento | null = null;
  diarias: Diaria[] = [];
  diariaAplicada: Diaria | null = null;
  dataMinima = '';
  quantidadeDiarias = 0;
  valorDiaria = 0;
  valorEstimado = 0;
  loading = false;
  errorMessage = '';
  apartamentoBloqueado = false;
  voltarParaMapa = false;
  origem: string | null = null;
  hospedes: any[] = [];
  placaTitular = '';
  clientesFiltrados: any[] = [];
  buscaCliente = '';
  mostrarResultados = false;
  clienteSelecionado: any = null;
  checkinData = '';
  checkinHora = '14';
  checkinMinuto = '00';
  horas = Array.from({length: 24}, (_, i) => String(i).padStart(2, '0'));
  minutos = ['00', '15', '30', '45'];
  mensagemBanner = '';
  tipoBanner: 'erro' | 'sucesso' | 'aviso' = 'erro';
  mostrarBanner = false;
  modalAdicionarHospede = false;
  termoBuscaHospede = '';
  clientesFiltradosModal: any[] = [];
  hospedeExistenteSelecionado: any = null;
  placaHospedeExistente = '';

  modoFormHospede: 'buscar' | 'cadastrar' = 'buscar';
novoHospedeForm: any = {
  nome: '', cpf: '', celular: '', placaCarro: '',
  dataNascimento: '', menorDeIdade: false,
  empresaId: null, creditoAprovado: false, autorizadoJantar: true
};
  cadastrandoTitular = false;
  empresas: any[] = [];

  ngOnInit(): void {
    console.log('🚀 ReservaForm ngOnInit chamado');
  console.log('📋 QueryParams:', this.route.snapshot.queryParams);
    this.setDatasPadrao();
    this.definirDataMinima();
   
    this.http.get<any[]>('/api/empresas').subscribe({
    next: (data) => this.empresas = data.sort((a, b) => 
      a.nomeEmpresa.localeCompare(b.nomeEmpresa, 'pt-BR')),
    error: () => {}
  });


    this.route.queryParams.subscribe(params => {
      if (params['bloqueado'] === 'true') { this.apartamentoBloqueado = true; this.voltarParaMapa = true; }
      if (params['origem']) { this.origem = params['origem']; }
      if (params['dataCheckin']) {
        const dataCheckin = new Date(params['dataCheckin'] + 'T14:00:00');
        this.reserva.dataCheckin = this.formatDateTimeLocal(dataCheckin);
        const dataCheckout = new Date(dataCheckin);
        dataCheckout.setDate(dataCheckout.getDate() + 1);
        dataCheckout.setHours(12, 0, 0, 0);
        this.reserva.dataCheckout = this.formatDateTimeLocal(dataCheckout);

        this.http.get<any[]>('/api/empresas').subscribe({
        next: (data) => this.empresas = data.sort((a, b) => 
           a.nomeEmpresa.localeCompare(b.nomeEmpresa, 'pt-BR')),
         error: () => {}
        });        

      }
      setTimeout(() => this.carregarApartamentos(), 300);
    });
  }

  setDatasPadrao(): void {
    const agora = new Date();
    this.checkinData = `${agora.getFullYear()}-${String(agora.getMonth()+1).padStart(2,'0')}-${String(agora.getDate()).padStart(2,'0')}`;
    this.checkinHora = String(agora.getHours()).padStart(2, '0');
    this.checkinMinuto = String(agora.getMinutes() < 30 ? 0 : 30).padStart(2, '0');
    this.montarDataCheckin();
    const checkout = new Date(agora);
    checkout.setDate(checkout.getDate() + 1);
    checkout.setHours(12, 0, 0, 0);
    this.reserva.dataCheckout = this.formatDateTimeLocal(checkout);
  }

  definirDataMinima(): void {
    this.dataMinima = this.formatDateTimeLocal(new Date());
  }

  formatDateTimeLocal(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth()+1).padStart(2,'0');
    const d = String(date.getDate()).padStart(2,'0');
    const h = String(date.getHours()).padStart(2,'0');
    const min = String(date.getMinutes()).padStart(2,'0');
    return `${y}-${m}-${d}T${h}:${min}`;
  }

  formatarDataHora(dataHora: string): string {
    if (!dataHora) return '';
    const data = new Date(dataHora);
    return `${String(data.getDate()).padStart(2,'0')}/${String(data.getMonth()+1).padStart(2,'0')}/${data.getFullYear()} às ${String(data.getHours()).padStart(2,'0')}:${String(data.getMinutes()).padStart(2,'0')}`;
  }

  formatarCPF(cpf: string): string {
    if (!cpf) return '';
    return cpf.replace(/\D/g,'').replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }

  onDataChange(): void {
    this.calcularDiarias();
    if (this.reserva.dataCheckin && this.reserva.dataCheckout) {
      if (new Date(this.reserva.dataCheckout) <= new Date(this.reserva.dataCheckin)) return;
      this.carregarApartamentos();
    }
  }

  montarDataCheckin(): void {
    if (!this.checkinData) return;
    this.reserva.dataCheckin = `${this.checkinData}T${this.checkinHora}:${this.checkinMinuto}`;
    this.onDataChange();
  }

  formatarCelularInput(valor: string): string {
  const nums = valor.replace(/\D/g, '').substring(0, 11);
  if (nums.length <= 2) return `(${nums}`;
  if (nums.length <= 7) return `(${nums.slice(0,2)}) ${nums.slice(2)}`;
  return `(${nums.slice(0,2)}) ${nums.slice(2,7)}-${nums.slice(7,11)}`;
}

  carregarApartamentos(): void {
    if (!this.reserva.dataCheckin || !this.reserva.dataCheckout) { this.apartamentos = []; return; }
    const url = `/api/apartamentos/disponiveis?dataInicio=${this.reserva.dataCheckin}:00&dataFim=${this.reserva.dataCheckout}:00`;
    this.http.get<any[]>(url).subscribe({
      next: (data) => {
        this.apartamentos = data.sort((a,b) => (parseInt(a.numeroApartamento)||0) - (parseInt(b.numeroApartamento)||0));
        this.route.queryParams.subscribe(params => {
          if (params['apartamentoId']) {
            const aptId = Number(params['apartamentoId']);
            const disponivel = this.apartamentos.find(a => a.id === aptId);
            if (disponivel) {
              this.reserva.apartamentoId = aptId;
              this.onApartamentoChange();
            } else {
              this.http.get<any>(`/api/apartamentos/${aptId}`).subscribe({
                next: (apt) => {
                  if (apt.status === 'MANUTENCAO' || apt.status === 'LIMPEZA') {
                    const tipo = apt.status === 'MANUTENCAO' ? 'MANUTENÇÃO' : 'LIMPEZA';
                    if (confirm(`⚠️ Apt ${apt.numeroApartamento} está em ${tipo}.\n\nDeseja criar pré-reserva mesmo assim?`)) {
                      this.apartamentos.push(apt);
                      this.reserva.apartamentoId = aptId;
                      this.apartamentoBloqueado = true;
                      this.onApartamentoChange();
                    } else { this.apartamentoBloqueado = false; }
                  } else {
                    this.apartamentoBloqueado = false;
                    alert(`⚠️ Apartamento não disponível para este período.`);
                  }
                },
                error: () => this.apartamentoBloqueado = false
              });
            }
          }
        });
      },
      error: () => { alert('❌ Erro ao carregar apartamentos'); this.apartamentos = []; }
    });
  }

  filtrarClientes(): void {
    const busca = this.buscaCliente.trim();
    if (busca.length < 2) { this.clientesFiltrados = []; this.mostrarResultados = false; return; }
    this.http.get<any[]>(`/api/clientes/buscar?termo=${busca}`).subscribe({
      next: (data) => { this.clientesFiltrados = data; this.mostrarResultados = true; },
      error: () => { this.clientesFiltrados = []; this.mostrarResultados = false; }
    });
  }

  selecionarCliente(cliente: any): void {
    if (!this.reserva.dataCheckin || !this.reserva.dataCheckout) {
      alert('⚠️ Selecione as datas antes de escolher o cliente!'); return;
    }
    const payload = {
      clienteId: cliente.id,
      dataCheckin: new Date(this.reserva.dataCheckin).toISOString(),
      dataCheckout: new Date(this.reserva.dataCheckout).toISOString()
    };
    this.http.post<any>('/api/reservas/validar-hospede', payload).subscribe({
      next: (resposta) => {
        if (!resposta.disponivel) { this.mostrarBannerErro(`❌ CLIENTE INDISPONÍVEL!\n\n${resposta.mensagem}`); return; }
        this.adicionarClientePrincipal(cliente);
      },
      error: () => this.mostrarBannerErro('❌ Erro ao validar disponibilidade do cliente.')
    });
  }


  abrirCadastroTitular(): void {
  this.cadastrandoTitular = true;
  this.modoFormHospede = 'cadastrar';
  this.novoHospedeForm = {
    nome: '', cpf: '', celular: '', placaCarro: '',
    dataNascimento: '', menorDeIdade: false,
    empresaId: null, creditoAprovado: false, autorizadoJantar: true
  };
  this.modalAdicionarHospede = true;
}

  private adicionarClientePrincipal(cliente: any): void {
    if (cliente.empresaId) {
      this.http.get<any>(`/api/empresas/${cliente.empresaId}`).subscribe({
        next: (empresa) => { cliente.empresa = empresa; this.clienteSelecionado = cliente; },
        error: () => this.clienteSelecionado = cliente
      });
    } else { this.clienteSelecionado = cliente; }
    this.reserva.clienteId = cliente.id;
    this.buscaCliente = `${cliente.nome} - ${this.formatarCPF(cliente.cpf)}`;
    this.clientesFiltrados = [];
    this.mostrarResultados = false;
    this.hospedes = [{ clienteId: cliente.id, nomeCompleto: cliente.nome, cpf: cliente.cpf||'', telefone: cliente.celular||'', placaCarro: null, cadastrarNovo: false }];
    this.reserva.quantidadeHospede = 1;
    if (this.reserva.apartamentoId && this.reserva.dataCheckin && this.reserva.dataCheckout) this.calcularDiarias();
  }

  limparBuscaCliente(): void {
    this.buscaCliente = ''; this.reserva.clienteId = 0; this.clienteSelecionado = null;
    this.clientesFiltrados = []; this.mostrarResultados = false;
    this.hospedes = []; this.reserva.quantidadeHospede = 0; this.placaTitular = '';
  }

  onApartamentoChange(): void {
    this.apartamentoSelecionado = this.apartamentos.find(a => a.id === this.reserva.apartamentoId) || null;
    if (this.apartamentoSelecionado?.tipoApartamentoId) this.carregarDiarias(this.apartamentoSelecionado.tipoApartamentoId);
  }

  carregarDiarias(tipoApartamentoId: number): void {
    this.diariaService.buscarPorTipoApartamento(tipoApartamentoId).subscribe({
      next: (data) => { this.diarias = data; this.calcularDiarias(); },
      error: () => { this.diarias = []; this.valorDiaria = 0; this.valorEstimado = 0; }
    });
  }

  calcularDiarias(): void {
    if (!this.reserva.dataCheckin || !this.reserva.dataCheckout) return;
    const diffTime = new Date(this.reserva.dataCheckout).getTime() - new Date(this.reserva.dataCheckin).getTime();
    this.quantidadeDiarias = Math.max(Math.ceil(diffTime / (1000*60*60*24)), 0);
    if (this.diarias.length > 0 && this.quantidadeDiarias > 0) {
      this.diariaAplicada = this.diarias.filter(d => d.quantidade <= this.quantidadeDiarias).sort((a,b) => b.quantidade - a.quantidade)[0] || this.diarias[0];
      this.valorDiaria = this.diariaAplicada.valor;
      this.valorEstimado = this.quantidadeDiarias * this.valorDiaria;
    } else { this.valorDiaria = 0; this.valorEstimado = 0; this.diariaAplicada = null; }
  }

  aoSelecionarApartamento(): void {
    const apartamentoId = this.reserva.apartamentoId;
    if (!apartamentoId || apartamentoId === 0) return;
    this.apartamentoService.verificarCheckoutVencido(apartamentoId).subscribe({
      next: (response) => {
        if (response.temCheckoutVencido) {
          alert(`⚠️ APARTAMENTO COM CHECKOUT VENCIDO!\n\nHóspede: ${response.hospedeNome}\nAtraso: ${response.horasAtraso} hora(s)\n\nFaça o checkout antes de criar nova reserva.`);
          this.reserva.apartamentoId = 0; this.apartamentoSelecionado = null;
        } else { this.onApartamentoChange(); }
      },
      error: () => {}
    });
  }

  abrirModalAdicionarHospede(): void {
    if (!this.reserva.clienteId) { this.mostrarBannerAviso('⚠️ Selecione o cliente principal primeiro!'); return; }
    this.modalAdicionarHospede = true;
    this.termoBuscaHospede = '';
    this.clientesFiltradosModal = [];
    this.hospedeExistenteSelecionado = null;
    this.placaHospedeExistente = '';
  }

  fecharModalAdicionarHospede(): void {
    this.modalAdicionarHospede = false;
    this.hospedeExistenteSelecionado = null;
    this.placaHospedeExistente = '';
  }

  buscarClientesModal(): void {
    const busca = this.termoBuscaHospede.trim();
    if (busca.length < 2) { this.clientesFiltradosModal = []; return; }
    this.http.get<any[]>(`/api/clientes/buscar?termo=${busca}`).subscribe({
      next: (data) => this.clientesFiltradosModal = data,
      error: () => this.clientesFiltradosModal = []
    });
  }

  selecionarHospedeParaConfirmar(cliente: any): void {
    const jaAdicionado = this.hospedes.some(h => h.clienteId === cliente.id);
    if (jaAdicionado) { this.mostrarBannerAviso('⚠️ Este hóspede já foi adicionado!'); return; }
    if (this.apartamentoSelecionado && this.hospedes.length >= this.apartamentoSelecionado.capacidade) {
      this.mostrarBannerErro(`❌ Capacidade máxima: ${this.apartamentoSelecionado.capacidade} hóspede(s)`); return;
    }
    const payload = {
      clienteId: cliente.id,
      dataCheckin: new Date(this.reserva.dataCheckin).toISOString(),
      dataCheckout: new Date(this.reserva.dataCheckout).toISOString()
    };
    this.http.post<any>('/api/reservas/validar-hospede', payload).subscribe({
      next: (resposta) => {
        if (!resposta.disponivel) { this.mostrarBannerErro(`❌ HÓSPEDE INDISPONÍVEL!\n\n${resposta.mensagem}`); return; }
        this.hospedeExistenteSelecionado = cliente;
        this.placaHospedeExistente = '';
      },
      error: () => this.mostrarBannerErro('❌ Erro ao validar disponibilidade.')
    });
  }

  confirmarHospedeExistente(): void {
    if (this.placaHospedeExistente && !this.validarPlaca(this.placaHospedeExistente)) {
      this.mostrarBannerErro('❌ Placa inválida!'); return;
    }
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
    this.fecharModalAdicionarHospede();
  }

  removerHospede(index: number): void {
    if (this.hospedes.length === 1) { this.mostrarBannerAviso('⚠️ É necessário ter pelo menos 1 hóspede!'); return; }
    const hospede = this.hospedes[index];
    if (confirm(`Deseja remover ${hospede.nomeCompleto} da lista?`)) {
      this.hospedes.splice(index, 1);
      this.reserva.quantidadeHospede = this.hospedes.length;
      this.calcularDiarias();
    }
  }

  validarPlaca(placa: string): boolean {
    if (!placa?.trim()) return true;
    const p = placa.replace(/[\s-]/g,'').toUpperCase();
    return /^[A-Z]{3}[0-9]{4}$/.test(p) || /^[A-Z]{3}[0-9][A-Z][0-9]{2}$/.test(p);
  }

  formatarPlacaTitular(): void {
    if (!this.placaTitular) return;
    let p = this.placaTitular.toUpperCase().replace(/[^A-Z0-9]/g,'');
    if (p.length > 3) p = p.substring(0,3) + '-' + p.substring(3,7);
    this.placaTitular = p;
  }

  formatarPlacaHospedeExistente(): void {
    if (!this.placaHospedeExistente) return;
    let p = this.placaHospedeExistente.toUpperCase().replace(/[^A-Z0-9]/g,'');
    if (p.length > 3) p = p.substring(0,3) + '-' + p.substring(3,7);
    this.placaHospedeExistente = p;
  }

  salvar(): void {
  if (!this.validarFormulario()) return;
  this.loading = true;
  this.errorMessage = '';

  // Se titular não cadastrado ainda, cadastra primeiro
  if (this.reserva.clienteId === -1) {
    const titular = this.hospedes[0];
    const novoCliente = {
      nome: titular.nome,
      cpf: titular.cpf || null,
      celular: titular.celular || null,
      placaCarro: titular.placaCarro || null,
      dataNascimento: titular.dataNascimento || null,
      empresaId: titular.empresaId || null,
      creditoAprovado: titular.creditoAprovado || false,
      autorizadoJantar: titular.autorizadoJantar !== false,
      menorDeIdade: titular.menorDeIdade || false
    };

    this.http.post<any>('/api/clientes', novoCliente).subscribe({
      next: (clienteCriado) => {
        this.reserva.clienteId = clienteCriado.id;
        this.hospedes[0].clienteId = clienteCriado.id;
        this.hospedes[0].cadastrarNovo = false;
        this.criarReserva();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || err.error || 'Erro ao cadastrar cliente titular';
      }
    });
  } else {
    this.criarReserva();
  }
}

private criarReserva(): void {
  const pad = (n: number) => String(n).padStart(2, '0');
  const fmt = (d: Date) => `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;

  if (this.placaTitular && this.hospedes.length > 0) {
    if (!this.validarPlaca(this.placaTitular)) {
      this.loading = false;
      this.mostrarBannerErro('❌ Placa do titular inválida!');
      return;
    }
    this.hospedes[0].placaCarro = this.placaTitular;
  }

  // Cadastrar todos os hóspedes adicionais com cadastrarNovo: true
  const hospedesParaCadastrar = this.hospedes
    .slice(1)
    .filter((h: any) => h.cadastrarNovo && !h.clienteId);

  if (hospedesParaCadastrar.length > 0) {
    const cadastros = hospedesParaCadastrar.map((h: any) =>
      this.http.post<any>('/api/clientes', {
        nome: h.nome,
        cpf: h.cpf || null,
        celular: h.celular || null,
        placaCarro: h.placaCarro || null,
        dataNascimento: h.dataNascimento || null,
        empresaId: h.empresaId || null,
        creditoAprovado: h.creditoAprovado || false,
        autorizadoJantar: h.autorizadoJantar !== false,
        menorDeIdade: h.menorDeIdade || false
      }).toPromise()
    );

    Promise.all(cadastros).then((clientesCriados: any[]) => {
      clientesCriados.forEach((cliente, index) => {
        hospedesParaCadastrar[index].clienteId = cliente.id;
        hospedesParaCadastrar[index].cadastrarNovo = false;
      });
      this.enviarReserva(fmt);
    }).catch((err) => {
      this.loading = false;
      this.errorMessage = 'Erro ao cadastrar hóspedes adicionais';
    });
  } else {
    this.enviarReserva(fmt);
  }
}

private enviarReserva(fmt: Function): void {
  const reservaRequest: any = {
    clienteId: Number(this.reserva.clienteId),
    apartamentoId: Number(this.reserva.apartamentoId),
    quantidadeHospede: Number(this.reserva.quantidadeHospede),
    dataCheckin: fmt(new Date(this.reserva.dataCheckin)),
    dataCheckout: fmt(new Date(this.reserva.dataCheckout)),
    hospedes: this.hospedes,
    hospedesAdicionaisIds: this.hospedes.slice(1)
      .filter((h: any) => h.clienteId)
      .map((h: any) => Number(h.clienteId))
  };

  this.reservaService.create(reservaRequest).subscribe({
    next: (response: any) => {
      this.loading = false;
      const reservaId = response?.id;
      if (reservaId) this.router.navigate(['/reservas', reservaId]);
      else this.router.navigate(['/reservas']);
    },
    error: (err) => {
      this.loading = false;
      if (err.status === 201 || err.status === 200) {
        this.router.navigate(['/reservas']);
        return;
      }
      this.errorMessage = err.error?.message || err.error || 'Erro ao criar reserva';
    }
  });
}

  validarFormulario(): boolean {
    if (!this.reserva.clienteId || this.reserva.clienteId === 0) { this.errorMessage = 'Selecione o cliente'; return false; }
    if (!this.reserva.apartamentoId || this.reserva.apartamentoId === 0) { this.errorMessage = 'Selecione o apartamento'; return false; }
    if (this.hospedes.length === 0) { this.errorMessage = 'Adicione pelo menos 1 hóspede'; return false; }
    if (!this.reserva.dataCheckin) { this.errorMessage = 'Data de check-in é obrigatória'; return false; }
    if (!this.reserva.dataCheckout) { this.errorMessage = 'Data de check-out é obrigatória'; return false; }
    if (new Date(this.reserva.dataCheckout) <= new Date(this.reserva.dataCheckin)) { this.errorMessage = 'Check-out deve ser posterior ao check-in'; return false; }
    return true;
  }

  mostrarBannerErro(mensagem: string): void { this.mensagemBanner = mensagem; this.tipoBanner = 'erro'; this.mostrarBanner = true; setTimeout(() => this.fecharBanner(), 8000); }
  mostrarBannerSucesso(mensagem: string): void { this.mensagemBanner = mensagem; this.tipoBanner = 'sucesso'; this.mostrarBanner = true; setTimeout(() => this.fecharBanner(), 5000); }
  mostrarBannerAviso(mensagem: string): void { this.mensagemBanner = mensagem; this.tipoBanner = 'aviso'; this.mostrarBanner = true; setTimeout(() => this.fecharBanner(), 6000); }
  fecharBanner(): void { this.mostrarBanner = false; setTimeout(() => this.mensagemBanner = '', 300); }

  adicionarNovoHospedeForm(): void {
  if (!this.novoHospedeForm.nome?.trim()) {
    this.mostrarBannerErro('⚠️ Nome completo é obrigatório!');
    return;
  }
  if (!this.novoHospedeForm.menorDeIdade) {
    if (!this.novoHospedeForm.cpf?.trim()) {
      this.mostrarBannerErro('⚠️ CPF é obrigatório!');
      return;
    }
  }


  if (this.novoHospedeForm.placaCarro && !this.validarPlaca(this.novoHospedeForm.placaCarro)) {
    this.mostrarBannerErro('❌ Placa inválida!');
    return;
  }
  if (!this.cadastrandoTitular && this.apartamentoSelecionado &&
      this.hospedes.length >= this.apartamentoSelecionado.capacidade) {
    this.mostrarBannerErro(`❌ Capacidade máxima: ${this.apartamentoSelecionado.capacidade} hóspede(s)`);
    return;
  }

  // ✅ Cadastra o cliente no backend imediatamente
  const novoCliente = {
    nome: this.novoHospedeForm.nome,
    cpf: this.novoHospedeForm.menorDeIdade ? null : 
     (this.novoHospedeForm.cpf?.replace(/\D/g, '') || null),
    celular: this.novoHospedeForm.celular || null,
    placaCarro: this.novoHospedeForm.placaCarro || null,
    dataNascimento: this.novoHospedeForm.dataNascimento || null,
    empresaId: this.novoHospedeForm.empresaId || null,
    creditoAprovado: this.novoHospedeForm.creditoAprovado || false,
    autorizadoJantar: this.novoHospedeForm.autorizadoJantar !== false,
    menorDeIdade: this.novoHospedeForm.menorDeIdade || false
  };

  console.log('📨 Enviando cliente:', JSON.stringify(novoCliente));

  this.http.post<any>('/api/clientes', novoCliente).subscribe({
    next: (clienteCriado) => {
      // ✅ Adiciona com clienteId real
      this.hospedes.push({
        clienteId: clienteCriado.id,
        nomeCompleto: clienteCriado.nome,
        cpf: clienteCriado.cpf || '',
        telefone: clienteCriado.celular || '',
        placaCarro: this.novoHospedeForm.placaCarro || null,
        cadastrarNovo: false,
        titular: this.cadastrandoTitular
      });

      if (this.cadastrandoTitular) {
        this.reserva.clienteId = clienteCriado.id;
        this.cadastrandoTitular = false;
      }

      this.reserva.quantidadeHospede = this.hospedes.length;
      this.calcularDiarias();
      this.novoHospedeForm = {
        nome: '', cpf: '', celular: '', placaCarro: '',
        dataNascimento: '', menorDeIdade: false,
        empresaId: null, creditoAprovado: false, autorizadoJantar: true
      };
      this.modoFormHospede = 'buscar';
      this.fecharModalAdicionarHospede();
      this.mostrarBannerSucesso('✅ Hóspede cadastrado e adicionado!');
    },
    error: (err) => {
  const msg = err.error?.erro || err.error?.message || '';
  
  // Se CPF duplicado, busca o cliente existente e adiciona
  if (msg.includes('CPF já cadastrado')) {
    this.http.get<any>(`/api/clientes/cpf/${this.novoHospedeForm.cpf.replace(/\D/g, '')}`).subscribe({
      next: (clienteExistente) => {
        this.hospedes.push({
          clienteId: clienteExistente.id,
          nomeCompleto: clienteExistente.nome,
          cpf: clienteExistente.cpf || '',
          telefone: clienteExistente.celular || '',
          placaCarro: this.novoHospedeForm.placaCarro || null,
          cadastrarNovo: false,
          titular: this.cadastrandoTitular
        });

        if (this.cadastrandoTitular) {
          this.reserva.clienteId = clienteExistente.id;
          this.cadastrandoTitular = false;
        }

        this.reserva.quantidadeHospede = this.hospedes.length;
        this.calcularDiarias();
        this.fecharModalAdicionarHospede();
        this.mostrarBannerSucesso(`✅ Cliente ${clienteExistente.nome} já cadastrado — adicionado automaticamente!`);
      },
      error: () => {
        this.mostrarBannerErro('❌ Erro ao buscar cliente existente.');
      }
    });
  } else {
    this.mostrarBannerErro('❌ ' + msg);
  }
  this.loading = false;
}
  });
}

  validarCPF(cpf: string): boolean {
  if (!cpf) return true; // CPF é opcional
  
  // Remove formatação
  const nums = cpf.replace(/\D/g, '');
  
  // Deve ter 11 dígitos
  if (nums.length !== 11) return false;
  
  // Rejeita CPFs com todos dígitos iguais (ex: 000.000.000-00)
  const primeiroDigito = nums[0];
if (nums.split('').every(d => d === primeiroDigito)) return false;
  
  // Valida dígitos verificadores
  let sum = 0;
  for (let i = 0; i < 9; i++) sum += parseInt(nums[i]) * (10 - i);
  let rest = (sum * 10) % 11;
  if (rest === 10 || rest === 11) rest = 0;
  if (rest !== parseInt(nums[9])) return false;

  sum = 0;
  for (let i = 0; i < 10; i++) sum += parseInt(nums[i]) * (11 - i);
  rest = (sum * 10) % 11;
  if (rest === 10 || rest === 11) rest = 0;
  if (rest !== parseInt(nums[10])) return false;

  return true;
}

formatarCPFInput(valor: string): string {
  const nums = valor.replace(/\D/g, '').substring(0, 11);
  if (nums.length <= 3) return nums;
  if (nums.length <= 6) return `${nums.slice(0,3)}.${nums.slice(3)}`;
  if (nums.length <= 9) return `${nums.slice(0,3)}.${nums.slice(3,6)}.${nums.slice(6)}`;
  return `${nums.slice(0,3)}.${nums.slice(3,6)}.${nums.slice(6,9)}-${nums.slice(9,11)}`;
}

get cpfNovoHospedeInvalido(): boolean {
  const cpf = this.novoHospedeForm.cpf;
  if (!cpf) return false;
  const nums = cpf.replace(/\D/g, '');
  return nums.length === 11 && !this.validarCPF(cpf);
}

  voltar(): void {
    if (this.origem === 'painel-recepcao') this.router.navigate(['/painel-recepcao']);
    else if (this.voltarParaMapa) this.router.navigate(['/reservas/mapa']);
    else this.router.navigate(['/reservas']);
  }
}

