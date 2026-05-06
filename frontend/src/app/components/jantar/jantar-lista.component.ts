import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { JantarService, HospedeJantar } from '../../services/jantar.service';
@Component({
  selector: 'app-jantar-lista',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
<div class="jantar-container">
  <div class="header">
    <h2>🍽️ Hóspedes Autorizados - Jantar</h2>
    <button (click)="carregarApartamentos()" class="btn-atualizar">
      🔄 Atualizar
    </button>
  </div>
 
  <!-- BUSCA DE HÓSPEDE -->
  <div class="busca-hospede">
    <h3>🔍 Buscar Hóspede</h3>
    <div class="busca-form">
      <input 
        type="text" 
        [(ngModel)]="nomeBusca" 
        placeholder="Nome do hóspede (opcional)"
        class="input-busca">
      <input 
        type="text" 
        [(ngModel)]="apartamentoBusca" 
        placeholder="Nº Apartamento (opcional)"
        class="input-busca-apto">
      <button (click)="buscar()" class="btn-buscar">🔍 Buscar</button>
      <button (click)="limparBusca()" class="btn-limpar">🗑️ Limpar</button>
    </div>
 
    <div *ngIf="resultadoBusca" class="resultado-busca">
      <div *ngIf="!resultadoBusca.encontrado" class="busca-nao-encontrado">
        ⚠️ {{ resultadoBusca.mensagem }}
      </div>
      <div *ngIf="resultadoBusca.encontrado && resultadoBusca.hospedes" class="busca-encontrados">
        <h4>Hóspedes Encontrados:</h4>
        <div *ngFor="let hospede of resultadoBusca.hospedes" class="hospede-busca-item">
          <div class="hospede-busca-info">
            <div class="info-linha">
              <span class="nome-destaque">{{ hospede.nomeCompleto }}</span>
              <span *ngIf="hospede.titular" class="badge-titular">👑 Titular</span>
            </div>
            <div class="info-linha">
              <span>🏢 Apartamento {{ hospede.numeroApartamento }}</span>
            </div>
            <div class="info-linha">
              <span *ngIf="hospede.autorizadoJantar" class="autorizado">
                ✅ AUTORIZADO - Pode lançar no apartamento
              </span>
              <span *ngIf="!hospede.autorizadoJantar" class="nao-autorizado">
                ❌ NÃO AUTORIZADO - Somente à vista
              </span>
            </div>
          </div>
          <button *ngIf="hospede.autorizadoJantar" (click)="abrirModalComandaBusca(hospede)" class="btn-comanda">
            📝 Lançar Comanda
          </button>
          <button *ngIf="!hospede.autorizadoJantar" disabled class="btn-comanda-disabled">
            🚫 Pagamento à Vista
          </button>
        </div>
      </div>
    </div>
  </div>
 
  <hr class="separador">
 
  <div *ngIf="loading" class="loading">Carregando...</div>
  <div *ngIf="erro" class="erro">{{ erro }}</div>
  <div *ngIf="!loading && apartamentos.length === 0" class="vazio">
    😴 Nenhum apartamento com hóspedes autorizados
  </div>
 
  <!-- LISTA DE APARTAMENTOS -->
  <div class="apartamentos-lista">
    <div *ngFor="let apto of apartamentos" class="apartamento-linha">
      <div class="apto-numero">
        🏢 <strong>{{ apto.numeroApartamento }}</strong>
        <span class="badge">{{ apto.hospedes.length }}</span>
      </div>
      <div class="apto-hospedes">
        <div *ngFor="let hospede of apto.hospedes" class="hospede-linha">
          <span class="nome">{{ hospede.nomeCompleto }}</span>
          <span *ngIf="hospede.titular" class="titular">👑</span>
        </div>
      </div>
      <div class="apto-acoes">
        <button 
          *ngFor="let hospede of apto.hospedes"
          (click)="abrirModalComanda(hospede, apto.numeroApartamento)" 
          class="btn-comanda">
          📝 {{ hospede.nomeCompleto.split(' ')[0] }}
        </button>
        <button (click)="verConsumoHoje(apto)" class="btn-ver-consumo">
          👁️ Consumo
        </button>
      </div>
    </div>
  </div>
 
  <!-- MODAL DE COMANDA — DOIS PAINÉIS -->
  <div *ngIf="modalAberto" class="modal-overlay" (click)="fecharModal()">
    <div class="modal-content modal-dois-paineis" (click)="$event.stopPropagation()">
      <div class="modal-header">
        <h3>📝 Lançar Comanda</h3>
        <div class="modal-info-hospede">
          <span>👤 {{ hospedeSelecionado?.nomeCompleto }}</span>
          <span>🏢 Apt {{ apartamentoSelecionado }}</span>
        </div>
        <button class="btn-fechar" (click)="fecharModal()">✕</button>
      </div>
 
      <div class="modal-dois-paineis-body">
        <!-- PAINEL ESQUERDO — PRODUTOS -->
        <div class="painel-produtos">
          <h4>📦 Produtos</h4>
          <div *ngIf="produtosCarregando" class="temp-message">⏳ Carregando...</div>
          <div *ngIf="!produtosCarregando" class="produtos-lista">
            <div *ngFor="let produto of produtos" class="produto-item">
              <div class="produto-info">
                <span class="produto-nome">{{ produto.nomeProduto }}</span>
                <span class="produto-preco">R$ {{ produto.valorVenda.toFixed(2) }}</span>
              </div>
              <button (click)="adicionarProduto(produto)" class="btn-adicionar">➕</button>
            </div>
          </div>
        </div>
 
        <!-- PAINEL DIREITO — COMANDA -->
        <div class="painel-comanda">
          <h4>🧾 Comanda</h4>
          <div *ngIf="produtosSelecionados.length === 0" class="comanda-vazia">
            Nenhum item adicionado
          </div>
          <div class="selecionados-lista">
            <div *ngFor="let item of produtosSelecionados; let i = index" class="item-selecionado">
              <span class="item-nome">{{ item.produto.nomeProduto }}</span>
              <div class="item-controles">
                <button class="btn-qtd" (click)="alterarQuantidade(i, item.quantidade - 1)">-</button>
                <span class="item-qty">{{ item.quantidade }}</span>
                <button class="btn-qtd" (click)="alterarQuantidade(i, item.quantidade + 1)">+</button>
                <span class="item-subtotal">R$ {{ (item.produto.valorVenda * item.quantidade).toFixed(2) }}</span>
                <button (click)="removerProduto(i)" class="btn-remover">🗑️</button>
              </div>
            </div>
          </div>
          <div class="total-section" *ngIf="produtosSelecionados.length > 0">
            <strong>TOTAL:</strong>
            <span class="total-valor">R$ {{ calcularTotal().toFixed(2) }}</span>
          </div>
        </div>
      </div>
 
      <div class="modal-footer">
        <button class="btn-cancelar" (click)="fecharModal()">Cancelar</button>
        <button class="btn-confirmar" (click)="confirmarComanda()" [disabled]="produtosSelecionados.length === 0">
          ✅ Confirmar ({{ produtosSelecionados.length }} item(ns))
        </button>
      </div>
    </div>
  </div>
 
  <!-- MODAL VER CONSUMO DE HOJE -->
  <div *ngIf="modalConsumo" class="modal-overlay" (click)="modalConsumo = false">
    <div class="modal-content modal-consumo" (click)="$event.stopPropagation()">
      <div class="modal-header">
        <h3>👁️ Consumo Hoje — Apt {{ aptoConsulta }}</h3>
        <button class="btn-fechar" (click)="modalConsumo = false">✕</button>
      </div>
      <div class="consumo-body">
        <div *ngIf="carregandoConsumo" class="temp-message">⏳ Carregando...</div>
        <div *ngIf="!carregandoConsumo && consumoHoje.length === 0" class="consumo-vazio">
          🍽️ Nenhum produto lançado hoje para este apartamento.
        </div>
        <table *ngIf="!carregandoConsumo && consumoHoje.length > 0" class="tabela-consumo">
          <thead>
            <tr>
              <th>Produto</th>
              <th>Qtd</th>
              <th>Total</th>
              <th>Hora</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let item of consumoHoje">
              <td>{{ item.descricao }}</td>
              <td class="td-center">{{ item.quantidade }}</td>
              <td class="td-valor">R$ {{ item.total.toFixed(2) }}</td>
              <td class="td-hora">{{ formatarHora(item.hora) }}</td>
            </tr>
          </tbody>
          <tfoot>
            <tr class="tr-total">
              <td colspan="2"><strong>TOTAL DO DIA:</strong></td>
              <td class="td-valor"><strong>R$ {{ totalConsumoHoje().toFixed(2) }}</strong></td>
              <td></td>
            </tr>
          </tfoot>
        </table>
      </div>
      <div class="modal-footer">
        <button class="btn-cancelar" (click)="modalConsumo = false">Fechar</button>
      </div>
    </div>
  </div>
 
</div>
`,
  styles: [`
.jantar-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
 
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
 
.header h2 { margin: 0; color: #333; }
 
.btn-atualizar {
  padding: 10px 20px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
}
 
.btn-atualizar:hover { background-color: #45a049; }
 
.loading, .erro, .vazio {
  text-align: center;
  padding: 40px;
  font-size: 18px;
  color: #666;
}
 
.erro { color: #f44336; }
 
.apartamentos-lista { display: flex; flex-direction: column; gap: 8px; }
 
.apartamento-linha {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.08);
  border-left: 4px solid #2196F3;
  gap: 16px;
}
 
.apto-numero {
  min-width: 110px;
  font-size: 1.1em;
  color: #2196F3;
  display: flex;
  align-items: center;
  gap: 6px;
}
 
.apto-hospedes { flex: 1; display: flex; flex-wrap: wrap; gap: 8px; }
 
.hospede-linha {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #f5f5f5;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.9em;
}
 
.apto-acoes { display: flex; gap: 8px; flex-wrap: wrap; }
 
.badge {
  background-color: #ff9800;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}
 
.nome { font-weight: 500; color: #333; }
.titular { font-size: 12px; color: #ff9800; font-weight: bold; }
 
.btn-comanda {
  padding: 8px 16px;
  background-color: #2196F3;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 13px;
  white-space: nowrap;
}
 
.btn-comanda:hover { background-color: #1976D2; }
 
.btn-ver-consumo {
  padding: 8px 14px;
  background-color: #9c27b0;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 13px;
  white-space: nowrap;
}
 
.btn-ver-consumo:hover { background-color: #7b1fa2; }
 
/* MODAL */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: flex-start;
  align-items: center;
  z-index: 1000;
  padding-left: 16px;
}
 
.modal-content {
  background: white;
  border-radius: 10px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
 
.modal-consumo { max-width: 550px; }
 
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #ddd;
}
 
.modal-header h3 { margin: 0; color: #333; }
 
.btn-fechar {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}
 
.btn-fechar:hover { color: #333; }
 
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #ddd;
}
 
.btn-cancelar {
  padding: 10px 20px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}
 
.btn-cancelar:hover { background-color: #da190b; }
 
.btn-confirmar {
  padding: 10px 20px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}
 
.btn-confirmar:hover { background-color: #45a049; }
 
.produtos-lista {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 300px;
  overflow-y: auto;
}
 
.produto-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 5px;
  border: 1px solid #ddd;
}
 
.produto-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}
 
.produto-nome {
  font-weight: 500;
  color: #333;
  max-width: 160px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
 
.produto-preco { color: #4CAF50; font-weight: bold; }
 
.btn-adicionar {
  padding: 6px 12px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}
 
.btn-adicionar:hover { background-color: #45a049; }
 
.selecionados-lista {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}
 
.item-selecionado {
  padding: 8px;
  background: #e8f5e9;
  border-radius: 6px;
  border-left: 3px solid #4CAF50;
  margin-bottom: 8px;
}
 
.item-nome {
  display: block;
  font-weight: 600;
  font-size: 0.9em;
  margin-bottom: 4px;
}
 
.item-controles { display: flex; align-items: center; gap: 6px; }
 
.btn-qtd {
  width: 26px;
  height: 26px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-weight: bold;
}
 
.item-qty { min-width: 20px; text-align: center; font-weight: bold; }
 
.item-subtotal {
  flex: 1;
  text-align: right;
  color: #4CAF50;
  font-weight: bold;
  font-size: 0.9em;
}
 
.btn-remover { background: none; border: none; cursor: pointer; font-size: 18px; }
.btn-remover:hover { transform: scale(1.2); }
 
.total-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 5px;
  font-size: 18px;
}
 
.total-valor { color: #4CAF50; font-weight: bold; font-size: 20px; }
 
.busca-hospede {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}
 
.busca-hospede h3 { margin: 0 0 15px 0; color: #2196F3; }
 
.busca-form { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
 
.input-busca {
  flex: 2;
  padding: 10px;
  border: 2px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
}
 
.input-busca-apto {
  flex: 1;
  padding: 10px;
  border: 2px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
}
 
.btn-buscar {
  padding: 10px 20px;
  background-color: #2196F3;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
}
 
.btn-buscar:hover { background-color: #1976D2; }
 
.btn-limpar {
  padding: 10px 20px;
  background-color: #9E9E9E;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
}
 
.btn-limpar:hover { background-color: #757575; }
 
.resultado-busca { margin-top: 20px; }
 
.busca-nao-encontrado {
  padding: 15px;
  background-color: #FFF3CD;
  border: 1px solid #FFC107;
  border-radius: 5px;
  color: #856404;
  text-align: center;
}
 
.busca-encontrados h4 { margin: 0 0 15px 0; color: #333; }
 
.hospede-busca-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  margin-bottom: 10px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border-left: 4px solid #2196F3;
}
 
.hospede-busca-info { flex: 1; }
 
.info-linha { margin: 5px 0; display: flex; align-items: center; gap: 10px; }
 
.nome-destaque { font-size: 18px; font-weight: bold; color: #333; }
 
.badge-titular {
  background-color: #ff9800;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}
 
.autorizado {
  color: #4CAF50;
  font-weight: bold;
  padding: 5px 10px;
  background-color: #E8F5E9;
  border-radius: 5px;
}
 
.nao-autorizado {
  color: #f44336;
  font-weight: bold;
  padding: 5px 10px;
  background-color: #FFEBEE;
  border-radius: 5px;
}
 
.btn-comanda-disabled {
  padding: 8px 16px;
  background-color: #ccc;
  color: #666;
  border: none;
  border-radius: 5px;
  cursor: not-allowed;
  font-size: 13px;
}
 
.separador { border: none; border-top: 2px dashed #ddd; margin: 30px 0; }
 
/* MODAL DOIS PAINÉIS — DESKTOP/TABLET */
.modal-dois-paineis {
  width: 95%;
  max-width: 900px;
  max-height: 90vh;
}
 
.modal-info-hospede { display: flex; gap: 16px; font-size: 0.9em; color: #555; }
 
.modal-dois-paineis-body {
  display: flex;
  gap: 0;
  flex: 1;
  overflow: hidden;
  height: calc(90vh - 130px);
}
 
.painel-produtos {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  border-right: 2px solid #eee;
}
 
.painel-comanda {
  width: 320px;
  padding: 16px;
  overflow-y: auto;
  background: #f9fffe;
  display: flex;
  flex-direction: column;
}
 
.painel-produtos h4,
.painel-comanda h4 {
  margin: 0 0 12px 0;
  color: #333;
  border-bottom: 2px solid #eee;
  padding-bottom: 8px;
}
 
.comanda-vazia { text-align: center; color: #aaa; padding: 20px; font-style: italic; }
 
/* MODAL CONSUMO HOJE */
.consumo-body { padding: 16px; overflow-y: auto; flex: 1; }
 
.consumo-vazio {
  text-align: center;
  color: #aaa;
  padding: 30px;
  font-size: 1em;
  font-style: italic;
}
 
.tabela-consumo { width: 100%; border-collapse: collapse; }
.tabela-consumo thead tr { background: #f3e5f5; }
 
.tabela-consumo th {
  padding: 10px 12px;
  text-align: left;
  font-weight: 600;
  color: #6a1b9a;
  border-bottom: 2px solid #ce93d8;
  font-size: 0.9em;
}
 
.tabela-consumo td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; font-size: 0.95em; }
.tabela-consumo tbody tr:hover td { background: #fce4ec; }
.td-center { text-align: center; font-weight: bold; }
.td-valor { text-align: right; color: #4CAF50; font-weight: bold; }
.td-hora { text-align: center; color: #888; font-size: 0.85em; }
 
.tr-total td {
  background: #e8f5e9;
  padding: 12px;
  border-top: 2px solid #4CAF50;
  font-size: 1em;
}
 
.tr-total .td-valor { font-size: 1.1em; color: #2e7d32; }
 
/* ✅ CELULAR — empilha vertical (até 768px) */
@media (max-width: 768px) {
  .modal-overlay {
    padding-left: 0;
    justify-content: center;
    align-items: flex-end;
  }
 
  .modal-dois-paineis {
    width: 100%;
    max-width: 100%;
    max-height: 95vh;
    border-radius: 16px 16px 0 0;
  }
 
  .modal-dois-paineis-body {
    flex-direction: column;
    height: auto;
    max-height: calc(95vh - 130px);
    overflow-y: auto;
  }
 
  .painel-produtos {
    border-right: none;
    border-bottom: 2px solid #eee;
    max-height: 50vh;
    overflow-y: auto;
  }
 
  .painel-comanda {
    width: 100%;
    max-height: 40vh;
    overflow-y: auto;
  }
 
  .btn-adicionar {
    padding: 10px 16px;
    font-size: 18px;
  }
 
  .btn-qtd {
    width: 34px;
    height: 34px;
    font-size: 16px;
  }
 
  .produto-nome { max-width: 180px; }
 
  .apartamento-linha { flex-wrap: wrap; }
 
  .apto-acoes {
    width: 100%;
    justify-content: flex-end;
  }
}
  `]
})
export class JantarListaComponent implements OnInit {
  apartamentos: { numeroApartamento: string, hospedes: HospedeJantar[] }[] = [];
  loading = false;
  erro = '';
 
  modalAberto = false;
  hospedeSelecionado: HospedeJantar | null = null;
  apartamentoSelecionado = '';
 
  produtos: any[] = [];
  produtosCarregando = false;
  produtosSelecionados: { produto: any, quantidade: number }[] = [];
 
  nomeBusca = '';
  apartamentoBusca = '';
  resultadoBusca: any = null;
 
  modalConsumo = false;
  aptoConsulta = '';
  consumoHoje: any[] = [];
  carregandoConsumo = false;
 
  constructor(
    private jantarService: JantarService,
    private http: HttpClient
  ) {}
 
  ngOnInit() {
    this.carregarApartamentos();
  }
 
  carregarApartamentos() {
    this.loading = true;
    this.erro = '';
    this.jantarService.getApartamentosComHospedes().subscribe({
      next: (data: any[]) => {
        this.apartamentos = data.map(apto => ({
          numeroApartamento: apto.numeroApartamento,
          hospedes: apto.hospedes.map((h: any) => ({
            id: h.id,
            hospedagemHospedeId: h.id,
            nomeCompleto: h.nomeCliente,
            clienteId: h.clienteId,
            nomeCliente: h.nomeCliente,
            apartamentoId: apto.reservaId,
            numeroApartamento: apto.numeroApartamento,
            titular: h.titular
          }))
        }));
        this.loading = false;
      },
      error: (error) => {
        console.error('❌ Erro:', error);
        this.erro = 'Erro ao carregar apartamentos';
        this.loading = false;
      }
    });
  }
 
  abrirModalComanda(hospede: HospedeJantar, numeroApartamento: string) {
    this.hospedeSelecionado = hospede;
    this.apartamentoSelecionado = numeroApartamento;
    this.modalAberto = true;
    this.carregarProdutos();
  }
 
  carregarProdutos() {
    this.produtosCarregando = true;
    this.http.get<any[]>('/api/jantar/produtos-restaurante').subscribe({
      next: (produtos) => {
        this.produtos = produtos;
        this.produtosCarregando = false;
      },
      error: (error) => {
        console.error('❌ Erro ao carregar produtos:', error);
        this.produtosCarregando = false;
      }
    });
  }
 
  adicionarProduto(produto: any) {
    const existe = this.produtosSelecionados.find(p => p.produto.id === produto.id);
    if (existe) {
      existe.quantidade++;
    } else {
      this.produtosSelecionados.push({ produto: produto, quantidade: 1 });
    }
  }
 
  removerProduto(index: number) {
    this.produtosSelecionados.splice(index, 1);
  }
 
  alterarQuantidade(index: number, quantidade: number) {
    if (quantidade <= 0) {
      this.removerProduto(index);
    } else {
      this.produtosSelecionados[index].quantidade = quantidade;
    }
  }
 
  calcularTotal(): number {
    return this.produtosSelecionados.reduce((total, item) => {
      return total + (item.produto.valorVenda * item.quantidade);
    }, 0);
  }
 
  confirmarComanda() {
    if (this.produtosSelecionados.length === 0) {
      alert('Selecione pelo menos um produto!');
      return;
    }
    if (!this.hospedeSelecionado) {
      alert('Erro: Hóspede não selecionado!');
      return;
    }
 
    const itens = this.produtosSelecionados.map(item => ({
      produtoId: item.produto.id,
      quantidade: item.quantidade
    }));
 
    this.jantarService.salvarComanda(this.hospedeSelecionado.hospedagemHospedeId, itens).subscribe({
      next: (response: any) => {
        console.log('✅ Comanda salva com sucesso:', response);
        this.fecharModal();
        this.carregarApartamentos();
      },
      error: (error) => {
        console.error('❌ Erro ao salvar comanda:', error);
        let mensagem = 'Erro ao salvar comanda';
        if (error.error) {
          if (typeof error.error === 'string') {
            mensagem = error.error;
          } else if (error.error.erro) {
            mensagem = error.error.erro;
          } else if (error.error.message) {
            mensagem = error.error.message;
          }
        } else if (error.message) {
          mensagem = error.message;
        }
        alert('❌ ' + mensagem);
      }
    });
  }
 
  buscar() {
    const temNome = this.nomeBusca.trim().length > 0;
    const temApartamento = this.apartamentoBusca.trim().length > 0;
    if (!temNome && !temApartamento) {
      alert('Preencha pelo menos o nome OU o número do apartamento!');
      return;
    }
    this.jantarService.buscarHospede(this.nomeBusca.trim(), this.apartamentoBusca.trim()).subscribe({
      next: (resultado) => { this.resultadoBusca = resultado; },
      error: (error) => {
        console.error('❌ Erro na busca:', error);
        alert('Erro ao buscar hóspede!');
      }
    });
  }
 
  limparBusca() {
    this.nomeBusca = '';
    this.apartamentoBusca = '';
    this.resultadoBusca = null;
  }
 
  abrirModalComandaBusca(hospede: any) {
    this.hospedeSelecionado = {
      id: hospede.hospedagemHospedeId || hospede.id,
      hospedagemHospedeId: hospede.hospedagemHospedeId || hospede.id,
      nomeCompleto: hospede.nomeCliente || hospede.nomeCompleto,
      clienteId: hospede.clienteId,
      nomeCliente: hospede.nomeCliente || hospede.nomeCompleto,
      apartamentoId: hospede.reservaId || hospede.apartamentoId,
      numeroApartamento: hospede.numeroApartamento,
      titular: hospede.titular
    };
    this.apartamentoSelecionado = hospede.numeroApartamento;
    this.modalAberto = true;
    this.carregarProdutos();
  }
 
  fecharModal() {
    this.modalAberto = false;
    this.hospedeSelecionado = null;
    this.apartamentoSelecionado = '';
    this.produtos = [];
    this.produtosSelecionados = [];
  }
 
  verConsumoHoje(apto: any): void {
    this.aptoConsulta = apto.numeroApartamento;
    this.modalConsumo = true;
    this.carregandoConsumo = true;
    this.consumoHoje = [];
    const reservaId = apto.hospedes[0]?.apartamentoId;
    if (!reservaId) {
      this.carregandoConsumo = false;
      return;
    }
    this.http.get<any[]>(`/api/jantar/consumo-hoje/${reservaId}`).subscribe({
      next: (data) => {
        this.consumoHoje = data;
        this.carregandoConsumo = false;
      },
      error: (err) => {
        console.error('❌ Erro ao carregar consumo:', err);
        this.carregandoConsumo = false;
      }
    });
  }
 
  totalConsumoHoje(): number {
    return this.consumoHoje.reduce((sum, item) => sum + (item.total || 0), 0);
  }
 
  formatarHora(dataHora: string): string {
    if (!dataHora) return '-';
    return new Date(dataHora).toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
