import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';

interface ProximaReserva {
  id: number;
  clienteNome: string;
  dataCheckin: string;
  dataCheckout: string;
}

interface Reserva {
  id: number;
  status: string;
  clienteNome: string;
  dataCheckin: string;
  dataCheckout: string;
  quantidadeHospedes: number;
  saiHoje: boolean;
  entraHoje: boolean;
  atrasado: boolean;
  proximaReserva?: ProximaReserva;
}

interface ApartamentoCard {
  id: number;
  numero: string;
  capacidade: number;
  statusApt: string;
  descricao: string;
  tipo: string;
  camas: string;
  tv: string;
  reserva: Reserva | null;
  temPreReservaFutura: boolean;
}

interface Contadores {
  total: number;
  disponivel: number;
  ocupado: number;
  hospedesOcupados: number;
  limpeza: number;
  bloqueado: number;
  preReserva: number;
  entraHoje: number;
  saiHoje: number;
  atrasado: number;
}

@Component({
  selector: 'app-painel-recepcao',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="painel-wrapper">

      <!-- CONTADORES -->
      <div class="contadores-bar" *ngIf="contadores">
        <div class="contador-grupo">
          <span class="cnt cnt-todos"
                [class.ativo]="filtroAtivo === 'todos'"
                (click)="setFiltro('todos')">
            todos: <strong>{{ contadores.total }}</strong>
          </span>
          <span class="cnt cnt-disponivel"
                [class.ativo]="filtroAtivo === 'disponivel'"
                (click)="setFiltro('disponivel')">
            disponível: <strong>{{ contadores.disponivel }}</strong>
          </span>
          <span class="cnt cnt-ocupado"
                [class.ativo]="filtroAtivo === 'ocupado'"
                (click)="setFiltro('ocupado')">
            ocupado: <strong>{{ contadores.ocupado }}</strong>
            <span class="cnt-detalhe">({{ contadores.hospedesOcupados }} hósp.)</span>
          </span>
          <span class="cnt cnt-prereserva"
                [class.ativo]="filtroAtivo === 'prereserva'"
                (click)="setFiltro('prereserva')">
            pré-reserva: <strong>{{ contadores.preReserva }}</strong>
          </span>
          <span class="cnt cnt-limpeza"
                [class.ativo]="filtroAtivo === 'limpeza'"
                (click)="setFiltro('limpeza')">
            em limpeza: <strong>{{ contadores.limpeza }}</strong>
          </span>
          <span class="cnt cnt-bloqueado"
                [class.ativo]="filtroAtivo === 'bloqueado'"
                (click)="setFiltro('bloqueado')">
            bloqueado: <strong>{{ contadores.bloqueado }}</strong>
          </span>
        </div>
        <div class="divisor">|</div>
        <div class="contador-grupo">
          <span class="cnt cnt-entra"
                [class.ativo]="filtroAtivo === 'entraHoje'"
                (click)="setFiltro('entraHoje')">
            entra hoje: <strong>{{ contadores.entraHoje }}</strong>
          </span>
          <span class="cnt cnt-sai"
                [class.ativo]="filtroAtivo === 'saiHoje'"
                (click)="setFiltro('saiHoje')">
            sai hoje: <strong>{{ contadores.saiHoje }}</strong>
          </span>
          <span class="cnt cnt-atrasado"
                [class.ativo]="filtroAtivo === 'atrasado'"
                (click)="setFiltro('atrasado')">
            atrasado: <strong>{{ contadores.atrasado }}</strong>
          </span>
        </div>
        <button class="btn-pdv" (click)="irParaPDV()" title="PDV - Vendas">💳 PDV</button>
        <button class="btn-atualizar" (click)="carregarDados()" title="Atualizar">↻</button>
      </div>

      <!-- FILTROS -->
      <div class="filtros-bar">
        <input
          class="filtro-input"
          type="text"
          placeholder="🛏️ Filtrar por camas (ex: casal, solteiro...)"
          [(ngModel)]="filtroCamas"
          (input)="aplicarFiltroLocal()" />
        <input
          class="filtro-input"
          type="text"
          placeholder="📺 Filtrar por TV (ex: Smart, 55...)"
          [(ngModel)]="filtroTv"
          (input)="aplicarFiltroLocal()" />
        <button class="btn-limpar" (click)="limparFiltros()" *ngIf="filtroCamas || filtroTv">
          ✕ Limpar
        </button>
      </div>

      <!-- BUSCA POR HÓSPEDE / PLACA -->
<div class="busca-bar">
  <div class="busca-grupo">
    <input
      class="filtro-input"
      type="text"
      placeholder="🔍 Buscar hóspede (nome)..."
      [(ngModel)]="buscaHospede"
      (keyup.enter)="buscarNoPainel()" />
    <input
      class="filtro-input"
      type="text"
      placeholder="🚗 Buscar Placa: ABC-1234...."
      [(ngModel)]="buscaPlaca"
      (keyup.enter)="buscarNoPainel()" />
    <button class="btn-buscar" (click)="buscarNoPainel()" [disabled]="buscando">
      {{ buscando ? '⏳' : '🔍 Buscar' }}
    </button>
    <button class="btn-limpar-busca" (click)="limparBusca()"
            *ngIf="mostrarResultados">✕</button>
  </div>

  <!-- RESULTADOS DA BUSCA -->
  <div class="resultados-busca" *ngIf="mostrarResultados">
    <div *ngIf="resultadosBusca.length === 0" class="sem-resultado-busca">
      📭 Nenhum hóspede encontrado
    </div>
    <div class="resultado-item" *ngFor="let r of resultadosBusca"
         (click)="irParaReservaPorId(r.reservaId)">
      <span class="resultado-hospede">
        👤 {{ r.hospedeNome }}
        <span class="badge-titular" *ngIf="r.titular">Titular</span>
      </span>
      <span class="resultado-apt">🏨 Apt {{ r.apartamento }}</span>
      <span class="resultado-datas">
        📅 {{ formatarData(r.dataCheckin) }} – {{ formatarData(r.dataCheckout) }}
      </span>
      <span class="resultado-placa" *ngIf="r.placaCarro">
        🚗 {{ r.placaCarro }}
      </span>
    </div>
  </div>
</div>

      <!-- LOADING -->
      <div class="loading" *ngIf="carregando">
        <div class="spinner"></div>
        <p>Carregando painel...</p>
      </div>

      <!-- SEM RESULTADO -->
      <div class="sem-resultado" *ngIf="!carregando && apartamentosFiltrados().length === 0">
        <span>📭 Nenhum apartamento encontrado para o filtro selecionado.</span>
        <button class="btn-acao btn-reservar" (click)="setFiltro('todos')">Ver todos</button>
      </div>

      <!-- GRID -->
      <div class="apt-grid" *ngIf="!carregando && apartamentosFiltrados().length > 0">
        <div
          class="apt-card"
          *ngFor="let apt of apartamentosFiltrados()"
          [class.card-ocupado]="getStatusFinal(apt) === 'ATIVA' && !apt.reserva?.atrasado"
          [class.card-prereserva]="getStatusFinal(apt) === 'PRE_RESERVA'"
          [class.card-limpeza]="getStatusFinal(apt) === 'LIMPEZA'"
          [class.card-disponivel]="getStatusFinal(apt) === 'DISPONIVEL'"
          [class.card-bloqueado]="getStatusFinal(apt) === 'BLOQUEADO' || getStatusFinal(apt) === 'INDISPONIVEL'"
          [class.card-atrasado]="apt.reserva?.atrasado">

          <!-- HEADER DO CARD -->
          <div class="card-header">
            <span class="apt-numero">{{ apt.numero }}</span>
            <span class="apt-tipo" *ngIf="apt.tipo">{{ apt.tipo }}</span>
            <div class="header-badges">
              <span class="badge-sai"    *ngIf="isSaiHoje(apt)">SAI HOJE</span>
              <span class="badge-entra"  *ngIf="apt.reserva?.entraHoje && !apt.reserva?.atrasado">ENTRA HOJE</span>
              <span class="badge-atraso" *ngIf="apt.reserva?.atrasado">ATRASADO</span>
            </div>
          </div>

          <!-- CORPO DO CARD -->
          <div class="card-body">

            <!-- COM RESERVA -->
            <ng-container *ngIf="apt.reserva">
              <div class="info-linha" *ngIf="apt.descricao">
                <span class="info-icon">ℹ️</span>
                <span class="info-texto truncate">{{ apt.descricao }}</span>
              </div>
              <div class="info-linha" *ngIf="apt.camas">
                <span class="info-icon">🛏️</span>
                <span class="info-texto">{{ apt.camas }}</span>
              </div>
              <div class="info-linha" *ngIf="apt.tv">
                <span class="info-icon">📺</span>
                <span class="info-texto">{{ apt.tv }}</span>
              </div>
              <div class="info-linha">
                <span class="info-icon">👤</span>
                <span class="info-texto truncate cliente-nome">{{ apt.reserva.clienteNome }}</span>
              </div>
              <div class="info-linha">
                <span class="info-icon">📅</span>
                <span class="info-texto">
                  {{ formatarData(apt.reserva.dataCheckin) }} – {{ formatarData(apt.reserva.dataCheckout) }}
                </span>
              </div>
              <div class="info-linha" *ngIf="apt.reserva.quantidadeHospedes">
                <span class="info-icon">🧑‍🤝‍🧑</span>
                <span class="info-texto">{{ apt.reserva.quantidadeHospedes }} hóspede(s)</span>
              </div>
            </ng-container>

            <!-- SEM RESERVA -->
            <ng-container *ngIf="!apt.reserva">
              <div class="sem-reserva">
                <span class="status-label">{{ getLabelStatus(apt) }}</span>
                <div class="info-linha" *ngIf="apt.descricao">
                  <span class="info-icon">ℹ️</span>
                  <span class="info-texto truncate">{{ apt.descricao }}</span>
                </div>
                <div class="info-linha" *ngIf="apt.camas">
                  <span class="info-icon">🛏️</span>
                  <span class="info-texto">{{ apt.camas }}</span>
                </div>
                <div class="info-linha" *ngIf="apt.tv">
                  <span class="info-icon">📺</span>
                  <span class="info-texto">{{ apt.tv }}</span>
                </div>
                <span class="capacidade-info">Capacidade: {{ apt.capacidade }} pessoa(s)</span>
              </div>
            </ng-container>

          </div>

          <!-- AÇÕES -->
          <div class="card-acoes">

            <!-- EM LIMPEZA -->
            <ng-container *ngIf="getStatusFinal(apt) === 'LIMPEZA'">
              <button class="btn-acao btn-liberar" (click)="liberarApartamento(apt)">↩ Liberar UH</button>
              <button class="btn-icone" (click)="irParaReserva(apt)" title="Ver reserva">➜</button>
            </ng-container>

            <!-- ATRASADO -->
            <ng-container *ngIf="apt.reserva?.atrasado">
              <button class="btn-acao btn-checkout-atraso" (click)="irCheckout(apt)">🚪 Fazer Checkout</button>
              <button class="btn-icone" (click)="irParaReserva(apt)" title="Ver detalhes">📋</button>
            </ng-container>

            <!-- ATIVA (não atrasado) -->
            <ng-container *ngIf="getStatusFinal(apt) === 'ATIVA' && !apt.reserva?.atrasado">
              <button class="btn-icone" title="Checkout"          (click)="irCheckout(apt)">🚪</button>
              <button class="btn-icone" title="Ver extrato"       (click)="irParaReserva(apt)">📋</button>
              <button class="btn-icone" title="Adicionar produto" (click)="irParaReserva(apt)">🛒</button>
              <button class="btn-icone" title="Hóspedes"          (click)="irParaReserva(apt)">👥</button>
              <button class="btn-icone" title="Transferir"        (click)="irParaReserva(apt)">➜</button>
            </ng-container>

            <!-- PRÓXIMA RESERVA -->
            <div class="proxima-reserva" *ngIf="apt.reserva?.proximaReserva">
              <span class="proxima-label">📌 Próxima:</span>
              <span class="proxima-cliente">{{ apt.reserva!.proximaReserva!.clienteNome }}</span>
              <span class="proxima-data">{{ formatarData(apt.reserva!.proximaReserva!.dataCheckin) }}</span>
              <button class="btn-transferir-pre"
                      (click)="transferirPreReserva(apt)"
                      title="Transferir pré-reserva para outro apartamento">
                🔄 Transferir
              </button>
            </div>

            <!-- PRÉ-RESERVA -->
            <ng-container *ngIf="getStatusFinal(apt) === 'PRE_RESERVA'">
              <button class="btn-acao btn-ativar" (click)="ativarPreReserva(apt)">✅ Ativar</button>
              <button class="btn-icone" (click)="irParaReserva(apt)" title="Ver detalhes">📋</button>
              <button class="btn-icone" (click)="cancelarReserva(apt)" title="Cancelar">✕</button>
            </ng-container>

            <!-- DISPONÍVEL -->
            <ng-container *ngIf="getStatusFinal(apt) === 'DISPONIVEL'">
              <button class="btn-acao btn-reservar" (click)="novaReserva(apt)">+ Nova Reserva</button>
              <div class="pre-reserva-futura" *ngIf="apt.temPreReservaFutura">
                📌 Pré-reserva agendada
              </div>
            </ng-container>

          </div>
        </div>
      </div>

    </div>
  `,
  styles: [`
    @import url('https://fonts.googleapis.com/css2?family=Rajdhani:wght@500;600;700&family=Noto+Sans:wght@400;500;600&display=swap');

    * { box-sizing: border-box; margin: 0; padding: 0; }

    .painel-wrapper {
      font-family: 'Noto Sans', sans-serif;
      background: #f0f2f5;
      min-height: 100vh;
      padding: 12px;
    }

    /* ── CONTADORES ─────────────────────────────── */
    .contadores-bar {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 6px;
      background: #fff;
      border: 1px solid #ddd;
      border-radius: 8px;
      padding: 8px 14px;
      margin-bottom: 10px;
      font-size: 0.82rem;
    }
    .contador-grupo { display: flex; flex-wrap: wrap; gap: 6px; align-items: center; }
    .divisor { color: #bbb; font-size: 1.2rem; margin: 0 4px; }

    .cnt {
      padding: 3px 10px;
      border-radius: 20px;
      border: 1px solid transparent;
      cursor: pointer;
      white-space: nowrap;
      transition: opacity .15s, box-shadow .15s, transform .1s;
    }
    .cnt:hover  { opacity: .85; transform: translateY(-1px); }
    .cnt.ativo  { box-shadow: 0 0 0 2px #2980b9; transform: translateY(-1px); }
    .cnt strong { font-weight: 700; }
    .cnt-detalhe { font-size: 0.75rem; opacity: 0.8; margin-left: 2px; }

    .cnt-todos      { background: #ecf0f1; border-color: #bdc3c7; color: #2c3e50; }
    .cnt-disponivel { background: #d5f5e3; border-color: #27ae60; color: #1e8449; }
    .cnt-ocupado    { background: #fadbd8; border-color: #e74c3c; color: #c0392b; }
    .cnt-prereserva { background: #d6eaf8; border-color: #2980b9; color: #1a5276; }
    .cnt-limpeza    { background: #fef9e7; border-color: #f39c12; color: #b7770d; }
    .cnt-bloqueado  { background: #f2f3f4; border-color: #95a5a6; color: #566573; }
    .cnt-entra      { background: #d5f5e3; border-color: #27ae60; color: #1e8449; }
    .cnt-sai        { background: #fdebd0; border-color: #e67e22; color: #a04000; }
    .cnt-atrasado   { background: #fadbd8; border-color: #e74c3c; color: #c0392b; }

    .btn-pdv {
      background: #8e44ad;
      color: #fff;
      border: none;
      border-radius: 6px;
      padding: 4px 14px;
      cursor: pointer;
      font-size: 0.85rem;
      font-weight: 700;
      transition: background .15s;
      margin-left: auto;
    }
    .btn-pdv:hover { background: #6c3483; }

    .btn-atualizar {
      background: none;
      border: 1px solid #bbb;
      border-radius: 6px;
      padding: 4px 10px;
      cursor: pointer;
      font-size: 1rem;
      color: #555;
      transition: background .15s;
    }
    .btn-atualizar:hover { background: #e8e8e8; }

    /* ── FILTROS ────────────────────────────────── */
    .filtros-bar {
      display: flex;
      gap: 8px;
      margin-bottom: 12px;
      flex-wrap: wrap;
    }
    .filtro-input {
      flex: 1;
      min-width: 200px;
      padding: 7px 12px;
      border: 1px solid #ddd;
      border-radius: 6px;
      font-size: 0.85rem;
      outline: none;
      transition: border-color .15s;
    }
    .filtro-input:focus { border-color: #3498db; }
    .btn-limpar {
      padding: 7px 14px;
      background: #e74c3c;
      color: #fff;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-size: 0.85rem;
      font-weight: 600;
    }
    .btn-limpar:hover { background: #c0392b; }

    /* ── LOADING ────────────────────────────────── */
    .loading {
      display: flex; flex-direction: column;
      align-items: center; justify-content: center;
      padding: 60px; gap: 12px; color: #777;
    }
    .spinner {
      width: 36px; height: 36px;
      border: 3px solid #ddd;
      border-top-color: #3498db;
      border-radius: 50%;
      animation: spin .7s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .sem-resultado {
      display: flex; flex-direction: column;
      align-items: center; gap: 12px;
      padding: 40px; color: #888; font-size: 0.95rem;
    }

    /* ── GRID ───────────────────────────────────── */
    .apt-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 12px;
    }

    /* ── CARD ───────────────────────────────────── */
    .apt-card {
      background: #fff;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 1px 4px rgba(0,0,0,.12);
      display: flex;
      flex-direction: column;
      transition: transform .15s, box-shadow .15s;
    }
    .apt-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0,0,0,.18);
    }

    .card-header {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 9px 12px;
  color: #fff;
  font-family: 'Rajdhani', sans-serif;
  font-weight: 700;
  font-size: 0.95rem;
  min-height: 40px;
}
    .card-ocupado    .card-header { background: #c0392b; }
.card-atrasado   .card-header { background: #6e2222; }
.card-prereserva .card-header { background: #2980b9; }
.card-limpeza    .card-header { background: #7f8c8d; }
.card-disponivel .card-header { background: #117a65; }
.card-bloqueado  .card-header { background: #626567; }
.card-limpeza    .card-body   { background: #f2f3f4; }
.card-prereserva .card-body   { background: #d6eaf8; }

    .apt-numero { letter-spacing: .5px; font-size: 1rem; }
    .apt-tipo {
  font-size: 0.7rem; font-weight: 500;
  background: rgba(255,255,255,.2);
  border-radius: 3px; padding: 1px 5px;
  font-family: 'Noto Sans', sans-serif;
}
    .header-badges { display: flex; gap: 4px; flex-wrap: wrap; }
    .badge-sai, .badge-entra, .badge-atraso {
      font-size: 0.62rem; font-weight: 700;
      padding: 1px 5px; border-radius: 3px;
      font-family: 'Noto Sans', sans-serif; white-space: nowrap;
    }
    .badge-sai    { background: #e67e22; }
    .badge-entra  { background: #27ae60; }
    .badge-atraso { background: #922b21; }

    .card-body {
      padding: 10px 12px; flex: 1;
      display: flex; flex-direction: column; gap: 5px;
    }
    .info-linha {
      display: flex; align-items: flex-start;
      gap: 5px; font-size: 0.8rem; color: #555; line-height: 1.4;
    }
    .info-icon  { flex-shrink: 0; font-size: 0.75rem; margin-top: 1px; }
    .info-texto { flex: 1; }
    .truncate   { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 170px; }
    .cliente-nome { font-weight: 600; color: #2c3e50; font-size: 0.82rem; }

    .sem-reserva { display: flex; flex-direction: column; gap: 6px; align-items: flex-start; }
    .status-label { font-size: 0.8rem; font-weight: 600; color: #888; text-transform: uppercase; letter-spacing: .5px; }
    .capacidade-info { font-size: 0.75rem; color: #aaa; }

    /* ── AÇÕES ──────────────────────────────────── */
    .card-acoes {
      display: flex;
      flex-direction: row;
      align-items: center;
      gap: 5px;
      padding: 8px 10px;
      border-top: 1px solid #f0f0f0;
      flex-wrap: wrap;
    }
    .btn-acao {
      font-size: 0.75rem; padding: 4px 10px;
      border: none; border-radius: 5px;
      cursor: pointer; font-weight: 600;
      transition: opacity .15s; flex: 1;
      white-space: nowrap;
    }
    .btn-acao:hover { opacity: .85; }
    .btn-liberar         { background: #1e6b45; color: #fff; }
    .btn-ativar          { background: #1a5276; color: #fff; }
    .btn-reservar        { background: #117a65; color: #fff; }
    .btn-checkout-atraso { background: #922b21; color: #fff; }

    .btn-icone {
      background: #f4f6f7; border: 1px solid #e0e0e0;
      border-radius: 5px; padding: 4px 7px;
      cursor: pointer; font-size: 0.8rem;
      transition: background .15s; line-height: 1;
      white-space: nowrap;
    }
    .btn-icone:hover { background: #e8eaec; }

    /* ── PRÓXIMA RESERVA ────────────────────────── */
    .proxima-reserva {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 4px;
      width: 100%;
      padding: 5px 8px;
      background: #eaf4fb;
      border-left: 3px solid #2980b9;
      border-radius: 4px;
      font-size: 0.78rem;
      margin-top: 2px;
    }
    .proxima-label   { font-weight: 700; color: #1a5276; }
    .proxima-cliente { color: #2c3e50; font-weight: 600; }
    .proxima-data    { color: #7f8c8d; }
    .btn-transferir-pre {
      margin-left: auto;
      background: #2980b9;
      color: #fff;
      border: none;
      border-radius: 4px;
      padding: 2px 8px;
      cursor: pointer;
      font-size: 0.75rem;
      font-weight: 600;
      white-space: nowrap;
    }
    .btn-transferir-pre:hover { background: #1a5276; }

    /* ── RESPONSIVO ─────────────────────────────── */
    @media (max-width: 600px) {
      .apt-grid { grid-template-columns: repeat(2, 1fr); gap: 8px; }
    }
    @media (min-width: 1400px) {
      .apt-grid { grid-template-columns: repeat(5, 1fr); }
    }

    .pre-reserva-futura {
  font-size: 0.75rem;
  color: #1a5276;
  background: #d6eaf8;
  border-left: 3px solid #2980b9;
  padding: 4px 8px;
  border-radius: 4px;
  margin-top: 4px;
}

  .busca-bar {
  margin-bottom: 12px;
}
.busca-grupo {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}
.btn-buscar {
  padding: 7px 16px;
  background: #2980b9;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 600;
}
.btn-buscar:hover { background: #1a5276; }
.btn-buscar:disabled { opacity: .6; cursor: default; }
.btn-limpar-busca {
  padding: 7px 12px;
  background: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
}
.resultados-busca {
  margin-top: 8px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
}
.sem-resultado-busca {
  padding: 16px;
  text-align: center;
  color: #888;
}
.resultado-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background .15s;
  flex-wrap: wrap;
}
.resultado-item:hover { background: #f0f7ff; }
.resultado-item:last-child { border-bottom: none; }
.resultado-hospede { font-weight: 600; color: #2c3e50; }
.badge-titular {
  font-size: 0.7rem;
  background: #27ae60;
  color: #fff;
  padding: 1px 6px;
  border-radius: 3px;
  margin-left: 4px;
}
.resultado-apt   { color: #2980b9; font-weight: 600; }
.resultado-datas { color: #7f8c8d; font-size: 0.8rem; }
.resultado-placa { color: #e67e22; font-weight: 600; }

  `]
})
export class PainelRecepcaoApp implements OnInit, OnDestroy {

  apartamentos: ApartamentoCard[] = [];
  apartamentosFiltradosLocal: ApartamentoCard[] = [];
  contadores: Contadores | null = null;
  carregando = true;
  filtroAtivo = 'todos';
  filtroCamas = '';
  filtroTv    = '';

  buscaHospede = '';
  buscaPlaca   = '';
  resultadosBusca: any[] = [];
  buscando = false;
  mostrarResultados = false;

  private intervalo: any;
  private apiUrl = '/api/apartamentos/painel';

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.carregarDados();
    this.intervalo = setInterval(() => this.carregarDados(), 60000);
  }

  ngOnDestroy(): void {
    if (this.intervalo) clearInterval(this.intervalo);
  }

  carregarDados(): void {
    this.carregando = true;
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    this.http.get<any>(this.apiUrl, { headers }).subscribe({
      next: (resp) => {
        this.apartamentos = resp.apartamentos;
        this.apartamentosFiltradosLocal = resp.apartamentos;
        this.contadores   = resp.contadores;
        this.carregando   = false;
      },
      error: (err) => {
        console.error('Erro ao carregar painel:', err);
        this.carregando = false;
      }
    });
  }

  setFiltro(filtro: string): void {
    this.filtroAtivo = filtro;
  }

  aplicarFiltroLocal(): void {
    this.apartamentosFiltradosLocal = this.apartamentos.filter(apt => {
      const okCamas = !this.filtroCamas ||
        (apt.camas?.toLowerCase().includes(this.filtroCamas.toLowerCase()));
      const okTv = !this.filtroTv ||
        (apt.tv?.toLowerCase().includes(this.filtroTv.toLowerCase()));
      return okCamas && okTv;
    });
  }

  limparFiltros(): void {
    this.filtroCamas = '';
    this.filtroTv    = '';
    this.apartamentosFiltradosLocal = [...this.apartamentos];
  }

  apartamentosFiltrados(): ApartamentoCard[] {
    const lista = (this.filtroCamas || this.filtroTv)
      ? this.apartamentosFiltradosLocal
      : this.apartamentos;

    if (this.filtroAtivo === 'todos') return lista;
return lista.filter(apt => {
  const s = this.getStatusFinal(apt);
  switch (this.filtroAtivo) {
    case 'disponivel': return s === 'DISPONIVEL';
    case 'ocupado':    return s === 'ATIVA' && !apt.reserva?.atrasado;
    case 'prereserva': return s === 'PRE_RESERVA' || apt.temPreReservaFutura === true;
    case 'limpeza':    return s === 'LIMPEZA';
    case 'bloqueado':  return s === 'BLOQUEADO' || s === 'INDISPONIVEL';
    case 'entraHoje':  return apt.reserva?.entraHoje === true;
    case 'saiHoje':    return this.isSaiHoje(apt);
    case 'atrasado':   return apt.reserva?.atrasado === true;
    default: return true;
  }
});
  }

  getStatusFinal(apt: ApartamentoCard): string {
    if (apt.reserva) return apt.reserva.status;
    return apt.statusApt || 'DISPONIVEL';
  }

  getLabelStatus(apt: ApartamentoCard): string {
    switch (apt.statusApt) {
      case 'LIMPEZA':     return '🧹 Em Limpeza';
      case 'BLOQUEADO':   return '🔒 Bloqueado';
      case 'INDISPONIVEL':return '🔒 Bloqueado';
      case 'MANUTENCAO':  return '🔧 Manutenção';
      default:            return '✅ Disponível';
    }
  }

  isSaiHoje(apt: ApartamentoCard): boolean {
    if (!apt.reserva) return false;
    if (apt.reserva.saiHoje) return true;
    // ✅ Usar data LOCAL (não UTC) para evitar problema de timezone
    const agora = new Date();
    const hoje = `${agora.getFullYear()}-${String(agora.getMonth() + 1).padStart(2, '0')}-${String(agora.getDate()).padStart(2, '0')}`;
    return apt.reserva.dataCheckout === hoje;
  }

  formatarData(data: string): string {
    if (!data) return '';
    const [ano, mes, dia] = data.split('-');
    return `${dia}/${mes}/${ano}`;
  }

  // ── NAVEGAÇÃO ────────────────────────────────

  irParaReserva(apt: ApartamentoCard): void {
    if (apt.reserva?.id) this.router.navigate(['/reservas', apt.reserva.id]);
  }

  irCheckout(apt: ApartamentoCard): void {
    if (apt.reserva?.id) this.router.navigate(['/reservas', apt.reserva.id]);
  }

  irParaPDV(): void {
    this.router.navigate(['/pdv'], { queryParams: { origem: 'painel-recepcao' } });
  }

  novaReserva(apt: ApartamentoCard): void {
    if (!apt.id || isNaN(Number(apt.id))) { alert('Apartamento inválido'); return; }
    this.router.navigate(['/reservas/novo'], { queryParams: { apartamentoId: apt.id } });
  }

  transferirPreReserva(apt: ApartamentoCard): void {
    if (!apt.reserva?.proximaReserva?.id) return;
    this.router.navigate(['/reservas', apt.reserva.proximaReserva.id]);
  }

  // ── AÇÕES ────────────────────────────────────

  liberarApartamento(apt: ApartamentoCard): void {
    if (!confirm(`Liberar apartamento ${apt.numero} da limpeza?`)) return;
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    const headers = new HttpHeaders({ Authorization: `Bearer ${localStorage.getItem('token')}` });
    this.http.patch(`/api/apartamentos/${apt.id}/liberar-limpeza`, {
      usuarioId: usuario.id,
      motivo: 'Liberação via Painel de Recepção'
    }, { headers }).subscribe({
      next: () => this.carregarDados(),
      error: (err) => alert('Erro: ' + (err.error?.erro || err.message))
    });
  }

  ativarPreReserva(apt: ApartamentoCard): void {
    if (!apt.reserva?.id) return;
    if (!confirm(`Ativar pré-reserva #${apt.reserva.id} do apartamento ${apt.numero}?`)) return;
    const headers = new HttpHeaders({ Authorization: `Bearer ${localStorage.getItem('token')}` });
    this.http.post(`/api/reservas/${apt.reserva.id}/ativar-pre-reserva`, {}, { headers })
      .subscribe({
        next: () => this.carregarDados(),
        error: (err) => alert('Erro: ' + (err.error?.erro || err.message))
      });
  }

  cancelarReserva(apt: ApartamentoCard): void {
    if (!apt.reserva?.id) return;
    const motivo = prompt('Motivo do cancelamento:');
    if (!motivo) return;
    const headers = new HttpHeaders({ Authorization: `Bearer ${localStorage.getItem('token')}` });
    this.http.patch(`/api/reservas/${apt.reserva.id}/cancelar`,
      null, { headers, params: { motivo } })
      .subscribe({
        next: () => this.carregarDados(),
        error: (err) => alert('Erro: ' + (err.error?.erro || err.message))
      });
  }

  buscarNoPainel(): void {
  if (!this.buscaHospede && !this.buscaPlaca) return;
  this.buscando = true;
  this.mostrarResultados = false;
  const headers = new HttpHeaders({ Authorization: `Bearer ${localStorage.getItem('token')}` });
  const params: any = {};
  if (this.buscaHospede) params.hospede = this.buscaHospede;
  if (this.buscaPlaca)   params.placa   = this.buscaPlaca;

  this.http.get<any[]>('/api/apartamentos/painel/buscar', { headers, params })
    .subscribe({
      next: (res) => {
        this.resultadosBusca  = res;
        this.mostrarResultados = true;
        this.buscando = false;
      },
      error: (err) => {
        alert('Erro: ' + (err.error?.erro || err.message));
        this.buscando = false;
      }
    });
}

limparBusca(): void {
  this.buscaHospede     = '';
  this.buscaPlaca       = '';
  this.resultadosBusca  = [];
  this.mostrarResultados = false;
}

irParaReservaPorId(reservaId: number): void {
  this.router.navigate(['/reservas', reservaId]);
}
}
