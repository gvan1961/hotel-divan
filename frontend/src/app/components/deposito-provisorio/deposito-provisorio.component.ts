import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { DepositoProvisorioService, DepositoProvisorio, DepositoProvisorioItem } from './deposito-provisorio.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-deposito-provisorio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div class="btn-deposito"
     (click)="onBtnClick()"
     (mousedown)="iniciarDrag($event)"
     (touchstart)="iniciarDragTouch($event)"
     [class.tem-itens]="deposito && (deposito.itens?.length ?? 0) > 0"
     [style.bottom]="posY + 'px'"
     [style.right]="posX + 'px'"
     title="Depósito Provisório (arraste para mover)">
  <span class="icone">📦</span>
  <span class="badge" *ngIf="totalPendente > 0">{{ totalPendente }}</span>
</div>

  <!-- Modal principal -->
  <div class="modal-overlay" *ngIf="modalAberto" (click)="fecharModal()">
    <div class="modal" (click)="$event.stopPropagation()">

      <div class="modal-header">
        <h2>📦 Depósito Provisório</h2>
        <button class="btn-fechar" (click)="fecharModal()">✕</button>
      </div>

      <!-- Fase 1: Adicionar produto -->
      <div class="secao">
        <h3>Adicionar Produto</h3>

        <!-- Busca por código de barras -->
        <div class="form-linha" style="margin-bottom: 12px;">
          <input
  type="text"
  placeholder="📷 Código de barras..."
  [(ngModel)]="termoCodigo"
  (keyup.enter)="buscarPorCodigo()"
  class="input-busca"
  #inputCodigo
  autofocus
/>
        </div>

        <!-- Busca por nome + quantidade + botão -->
        <div class="form-linha">
          <input
            type="text"
            placeholder="🔍 Buscar por nome..."
            [(ngModel)]="termoBusca"
            (input)="buscarProdutos()"
            class="input-busca"
          />
          <input
            type="number"
            placeholder="Qtd"
            [(ngModel)]="quantidadeAdicionar"
            min="1"
            class="input-qtd"
          />
          <button class="btn-add" (click)="adicionarItem()" [disabled]="!produtoSelecionado || !quantidadeAdicionar">
            + Adicionar
          </button>
        </div>

        <!-- Lista de produtos encontrados -->
        <div class="lista-busca" *ngIf="produtos.length > 0 && !produtoSelecionado">
          <div
            class="item-busca"
            *ngFor="let p of produtos"
            (click)="selecionarProduto(p)">
            {{ p.nomeProduto }} — R$ {{ p.valorVenda | number:'1.2-2' }}
          </div>
        </div>

        <!-- Produto selecionado -->
        <div class="produto-selecionado" *ngIf="produtoSelecionado">
          ✅ {{ produtoSelecionado.nomeProduto }} — R$ {{ produtoSelecionado.valorVenda | number:'1.2-2' }}
          <span class="btn-limpar" (click)="limparProduto()">✕</span>
        </div>
      </div>

      <!-- Fase 2: Itens no depósito -->
      <div class="secao" *ngIf="deposito && (deposito.itens?.length ?? 0) > 0">
        <h3>Itens para Distribuir</h3>
        <table class="tabela">
          <thead>
            <tr>
              <th>Produto</th>
              <th>Qtd</th>
              <th>Distribuído</th>
              <th>Pendente</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let item of deposito.itens">
              <td>{{ item.produto.nomeProduto }}</td>
              <td>{{ item.quantidade }}</td>
              <td>{{ item.quantidadeDistribuida }}</td>
              <td>{{ item.quantidade - item.quantidadeDistribuida }}</td>
              <td>
                <button
                  class="btn-distribuir"
                  (click)="abrirDistribuicao(item)"
                  [disabled]="item.quantidade === item.quantidadeDistribuida">
                  Distribuir
                </button>

                  <button
                   class="btn-pagar-avista"
                   (click)="abrirPagarAVista(item)"
                   [disabled]="item.quantidade === item.quantidadeDistribuida">
                   💵 À Vista
                  </button>

                  <button
                  class="btn-remover"
                  (click)="removerItem(item)"
                  [disabled]="item.quantidadeDistribuida > 0">
                  ✕
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="vazio" *ngIf="!deposito || deposito.itens?.length === 0">
        Nenhum item no depósito.
      </div>

    </div>
  </div>

  <div class="modal-overlay" *ngIf="modalPagarAVista" (click)="fecharPagarAVista()">
  <div class="modal modal-pequeno" (click)="$event.stopPropagation()">
    <div class="modal-header">
      <h2>💵 Pagar à Vista</h2>
      <button class="btn-fechar" (click)="fecharPagarAVista()">✕</button>
    </div>
    <div class="secao">
      <p><strong>{{ itemPagandoAVista?.produto?.nomeProduto }}</strong></p>
      <p>Pendente: <strong>{{ itemPagandoAVista ? (itemPagandoAVista.quantidade - itemPagandoAVista.quantidadeDistribuida) : 0 }}</strong></p>

     <div class="form-linha">
  <label>Quantidade:</label>
  <input type="number" [(ngModel)]="quantidadePagarAVista"
         class="input-qtd"
         [readonly]="true"
         disabled>
</div>

      <div class="form-linha">
        <label>Forma de Pagamento:</label>
        <select [(ngModel)]="formaPagamentoAVista">
          <option *ngFor="let f of formasPagamento" [value]="f.codigo">{{ f.nome }}</option>
        </select>
      </div>

      <button class="btn-add btn-confirmar" (click)="confirmarPagarAVista()"
              [disabled]="!quantidadePagarAVista">
        ✅ Confirmar Pagamento
      </button>
    </div>
  </div>
</div>

  <!-- Modal de distribuição -->
  <div class="modal-overlay" *ngIf="itemDistribuindo" (click)="fecharDistribuicao()">
    <div class="modal modal-pequeno" (click)="$event.stopPropagation()">
      <div class="modal-header">
        <h2>Distribuir: {{ itemDistribuindo.produto.nomeProduto }}</h2>
        <button class="btn-fechar" (click)="fecharDistribuicao()">✕</button>
      </div>

      <div class="secao">
        <p class="pendente-info">Pendente: <strong>{{ itemDistribuindo.quantidade - itemDistribuindo.quantidadeDistribuida }}</strong></p>

        <div class="form-linha">
          <input
            type="text"
            placeholder="🔍 Buscar reserva/apartamento..."
            [(ngModel)]="termoReserva"
            (input)="buscarReservas()"
            class="input-busca"
          />
          <input
            type="number"
            placeholder="Qtd"
            [(ngModel)]="quantidadeDistribuir"
            min="1"
            [max]="itemDistribuindo.quantidade - itemDistribuindo.quantidadeDistribuida"
            class="input-qtd"
          />
        </div>
              

        <div class="lista-busca" *ngIf="reservas.length > 0 && !reservaSelecionada">
          <div
            class="item-busca"
            *ngFor="let r of reservas"
            (click)="selecionarReserva(r)">
            Apto {{ r.apartamento?.numeroApartamento }} — {{ r.cliente?.nome }}
          </div>
        </div>

        <div class="produto-selecionado" *ngIf="reservaSelecionada">
          ✅ Apto {{ reservaSelecionada.apartamento?.numeroApartamento }} — {{ reservaSelecionada.cliente?.nome }}
          <span class="btn-limpar" (click)="limparReserva()">✕</span>
        </div>

        <button
          class="btn-add btn-confirmar"
          (click)="confirmarDistribuicao()"
          [disabled]="!reservaSelecionada || !quantidadeDistribuir">
          ✅ Confirmar Distribuição
        </button>
      </div>
    </div>
  </div>
`,
  styles: [`
  .btn-deposito {
  position: fixed;
  width: 64px;
  height: 64px;
  background: #1565c0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  box-shadow: 0 4px 16px rgba(0,0,0,0.35);
  z-index: 1000;
  transition: background 0.2s;
  user-select: none;
  touch-action: none; /* ← importante para tablet */
}
.btn-deposito:active { cursor: grabbing; }
  .btn-deposito.tem-itens { background: #e65100; }
  .btn-deposito:hover { filter: brightness(1.15); }
  .icone { font-size: 28px; }
  .badge {
    position: absolute;
    top: 4px;
    right: 4px;
    background: #fff;
    color: #e65100;
    border-radius: 50%;
    width: 22px;
    height: 22px;
    font-size: 12px;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0,0,0,0.55);
    z-index: 1100;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 16px;
  }
  .modal {
    background: #fff;
    border-radius: 10px;
    width: 95vw;
    max-width: 900px;
    max-height: 90vh;
    overflow-y: auto;
    padding: 32px;
  }
  .modal-pequeno {
    width: 95vw;
    max-width: 520px;
  }
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
  .modal-header h2 { margin: 0; font-size: 22px; color: #1a1a1a; }
  .btn-fechar {
    background: none;
    border: none;
    font-size: 24px;
    cursor: pointer;
    color: #666;
    padding: 4px 8px;
  }
  .secao { margin-bottom: 24px; }
  .secao h3 { font-size: 17px; color: #444; margin-bottom: 12px; font-weight: 600; }
  .form-linha { display: flex; gap: 10px; align-items: center; }
  .input-busca {
    flex: 1;
    padding: 14px;
    border: 1.5px solid #ccc;
    border-radius: 6px;
    font-size: 16px;
  }
  .input-busca:focus { outline: none; border-color: #1565c0; }
  .input-qtd {
    width: 90px;
    padding: 14px;
    border: 1.5px solid #ccc;
    border-radius: 6px;
    font-size: 16px;
    text-align: center;
  }
  .btn-add {
    padding: 14px 22px;
    background: #1565c0;
    color: #fff;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 16px;
    white-space: nowrap;
  }
  .btn-add:disabled { background: #aaa; cursor: not-allowed; }
  .btn-confirmar { margin-top: 16px; width: 100%; font-size: 17px; padding: 16px; }
  .lista-busca {
    border: 1.5px solid #ddd;
    border-radius: 6px;
    margin-top: 6px;
    max-height: 200px;
    overflow-y: auto;
  }
  .item-busca {
    padding: 14px 16px;
    cursor: pointer;
    font-size: 16px;
    border-bottom: 1px solid #f0f0f0;
  }
  .item-busca:hover { background: #e3f2fd; }
  .produto-selecionado {
    margin-top: 10px;
    font-size: 16px;
    color: #1565c0;
    display: flex;
    align-items: center;
    gap: 10px;
    background: #e3f2fd;
    padding: 10px 14px;
    border-radius: 6px;
  }
  .btn-limpar { cursor: pointer; color: #e53935; font-weight: bold; font-size: 18px; }
  .pendente-info { font-size: 16px; margin-bottom: 14px; }
  .tabela { width: 100%; border-collapse: collapse; font-size: 16px; }
  .tabela th {
    background: #f0f4ff;
    padding: 14px;
    text-align: left;
    font-size: 15px;
    color: #333;
  }
  .tabela td { padding: 14px; border-bottom: 1px solid #eee; font-size: 15px; }
  .tabela tr:hover td { background: #fafafa; }
  .btn-distribuir {
    padding: 10px 16px;
    background: #2e7d32;
    color: #fff;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    margin-right: 6px;
    font-size: 14px;
  }
  .btn-distribuir:disabled { background: #aaa; cursor: not-allowed; }
  .btn-remover {
    padding: 10px 12px;
    background: #e53935;
    color: #fff;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 14px;
  }
  .btn-remover:disabled { background: #aaa; cursor: not-allowed; }
  .vazio { text-align: center; color: #aaa; padding: 32px; font-size: 16px; }

  .btn-pagar-avista {
  padding: 10px 16px;
  background: #1565c0;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  margin-right: 6px;
  font-size: 14px;
}
.btn-pagar-avista:disabled { background: #aaa; cursor: not-allowed; }

`]
})
export class DepositoProvisorioComponent implements OnInit {

  modalAberto = false;
  deposito: DepositoProvisorio | null = null;
  totalPendente = 0;

  // Adicionar produto
  termoBusca = '';
  produtos: any[] = [];
  produtoSelecionado: any = null;
  quantidadeAdicionar: number | null = null;

  // Distribuir
  itemDistribuindo: DepositoProvisorioItem | null = null;
  termoReserva = '';
  reservas: any[] = [];
  reservaSelecionada: any = null;
  quantidadeDistribuir: number | null = null;

  // Pagar à vista
modalPagarAVista = false;
itemPagandoAVista: DepositoProvisorioItem | null = null;
quantidadePagarAVista: number | null = null;
formaPagamentoAVista = 'DINHEIRO';
formasPagamento = [
  { codigo: 'DINHEIRO', nome: 'Dinheiro' },
  { codigo: 'PIX', nome: 'PIX' },
  { codigo: 'CARTAO_DEBITO', nome: 'Cartão Débito' },
  { codigo: 'CARTAO_CREDITO', nome: 'Cartão Crédito' },
];

  termoCodigo = '';

  posX = 28;
posY = 28;
private arrastando = false;
private moveu = false;


  constructor(
    private service: DepositoProvisorioService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
  this.carregarDeposito();
  this.carregarPosicao();
}

carregarPosicao(): void {
  const salvo = localStorage.getItem('deposito-btn-pos');
  if (salvo) {
    const pos = JSON.parse(salvo);
    this.posX = pos.x;
    this.posY = pos.y;
  }
}

iniciarDrag(event: MouseEvent): void {
  this.arrastando = true;
  this.moveu = false;
  event.preventDefault();

  const mover = (e: MouseEvent) => {
    this.moveu = true;
    // Calcula posição a partir da direita e de baixo
    this.posX = window.innerWidth - e.clientX - 32;
    this.posY = window.innerHeight - e.clientY - 32;

    // Limites para não sair da tela
    this.posX = Math.max(8, Math.min(this.posX, window.innerWidth - 72));
    this.posY = Math.max(8, Math.min(this.posY, window.innerHeight - 72));
  };

  const soltar = () => {
    this.arrastando = false;
    localStorage.setItem('deposito-btn-pos', 
      JSON.stringify({ x: this.posX, y: this.posY }));
    window.removeEventListener('mousemove', mover);
    window.removeEventListener('mouseup', soltar);
  };

  window.addEventListener('mousemove', mover);
  window.addEventListener('mouseup', soltar);
}

iniciarDragTouch(event: TouchEvent): void {
  this.moveu = false;
  event.preventDefault();

  const mover = (e: TouchEvent) => {
    const touch = e.touches[0];
    this.moveu = true;
    this.posX = window.innerWidth - touch.clientX - 32;
    this.posY = window.innerHeight - touch.clientY - 32;

    // Limites para não sair da tela
    this.posX = Math.max(8, Math.min(this.posX, window.innerWidth - 72));
    this.posY = Math.max(8, Math.min(this.posY, window.innerHeight - 72));
  };

  const soltar = () => {
    localStorage.setItem('deposito-btn-pos',
      JSON.stringify({ x: this.posX, y: this.posY }));
    window.removeEventListener('touchmove', mover);
    window.removeEventListener('touchend', soltar);
  };

  window.addEventListener('touchmove', mover, { passive: false });
  window.addEventListener('touchend', soltar);
}

// ✅ Só abre o modal se não arrastou
onBtnClick(): void {
  if (!this.moveu) {
    this.abrirModal();
  }
}

  @HostListener('window:keydown', ['$event'])
onKeyDown(event: KeyboardEvent): void {
  if (event.ctrlKey && event.key === 'd') {
    event.preventDefault();
    event.stopPropagation();
    this.abrirModal();
  }
}

  carregarDeposito(): void {
    this.service.getAtual().subscribe({
      next: (dep) => {
        this.deposito = dep;
        this.calcularPendente();
      },
      error: () => {
        this.deposito = null;
        this.totalPendente = 0;
      }
    });
  }

  calcularPendente(): void {
    if (!this.deposito?.itens) { this.totalPendente = 0; return; }
    this.totalPendente = this.deposito.itens
      .filter(i => i.quantidade > i.quantidadeDistribuida).length;
  }

  abrirModal(): void {
  this.modalAberto = true;
  this.carregarDeposito();
  setTimeout(() => {
    const el = document.querySelector('input[placeholder*="Código"]') as HTMLInputElement;
    if (el) el.focus();
  }, 300);
}

  fecharModal(): void {
    this.modalAberto = false;
    this.limparProduto();
  }

  buscarProdutos(): void {
    if (this.termoBusca.length < 2) { this.produtos = []; return; }
    this.http.get<any[]>(`${environment.apiUrl}/produtos/buscar?nome=${this.termoBusca}`)
      .subscribe(p => this.produtos = p);
  }

  selecionarProduto(p: any): void {
  this.produtoSelecionado = p;
  this.quantidadeAdicionar = 1;
  this.termoBusca = '';
  this.produtos = [];
}

  limparProduto(): void {
    this.produtoSelecionado = null;
    this.termoBusca = '';
    this.produtos = [];
    this.quantidadeAdicionar = null;
    this.termoCodigo = '';
  }

 buscarPorCodigo(): void {
  if (this.termoCodigo.length < 3) return;
  this.http.get<any[]>(`${environment.apiUrl}/produtos/buscar-codigo?codigo=${this.termoCodigo}`)
    .subscribe({
      next: (produtos) => {
        if (produtos.length === 1) {
          this.produtoSelecionado = produtos[0];
          this.quantidadeAdicionar = 1;
          this.termoCodigo = '';
          this.adicionarItem();
        } else if (produtos.length > 1) {
          this.produtos = produtos;
          this.termoCodigo = '';
        }
      },
      error: () => {}
    });
}
 adicionarItem(): void {
  if (!this.produtoSelecionado || !this.quantidadeAdicionar) return;
  this.service.adicionarItem(this.produtoSelecionado.id, this.quantidadeAdicionar).subscribe({
    next: () => {
      this.limparProduto();
      this.carregarDeposito();
      setTimeout(() => {
        const el = document.querySelector('input[placeholder*="Código"]') as HTMLInputElement;
        if (el) el.focus();
      }, 100);
    },
    error: (e: any) => {
      alert('Erro ao adicionar: ' + e.error?.message);
      this.quantidadeAdicionar = null;
    }
  });
}

  removerItem(item: DepositoProvisorioItem): void {
    if (!confirm('Remover este item?')) return;
    this.service.removerItem(item.id).subscribe({
      next: () => this.carregarDeposito(),
      error: (e) => alert('Erro: ' + e.error?.message)
    });
  }

  abrirDistribuicao(item: DepositoProvisorioItem): void {
    this.itemDistribuindo = item;
    this.quantidadeDistribuir = item.quantidade - item.quantidadeDistribuida;
  }

  fecharDistribuicao(): void {
    this.itemDistribuindo = null;
    this.termoReserva = '';
    this.reservas = [];
    this.reservaSelecionada = null;
    this.quantidadeDistribuir = null;
  }

  buscarReservas(): void {
    if (this.termoReserva.length < 1) { this.reservas = []; return; }
    this.http.get<any[]>(`${environment.apiUrl}/reservas/ativas/buscar?termo=${this.termoReserva}`)
      .subscribe(r => this.reservas = r);
  }

  selecionarReserva(r: any): void {
    this.reservaSelecionada = r;
    this.termoReserva = '';
    this.reservas = [];
  }

  limparReserva(): void {
    this.reservaSelecionada = null;
    this.termoReserva = '';
    this.reservas = [];
  }

  confirmarDistribuicao(): void {
    if (!this.itemDistribuindo || !this.reservaSelecionada || !this.quantidadeDistribuir) return;
    this.service.distribuirItem(
      this.itemDistribuindo.id,
      this.reservaSelecionada.id,
      this.quantidadeDistribuir
    ).subscribe({
      next: () => {
        this.fecharDistribuicao();
        this.carregarDeposito();
      },
      error: (e) => alert('Erro: ' + e.error?.message)
    });
  }

  abrirPagarAVista(item: DepositoProvisorioItem): void {
  this.itemPagandoAVista = item;
  this.quantidadePagarAVista = item.quantidade - item.quantidadeDistribuida;
  this.formaPagamentoAVista = 'DINHEIRO';
  this.modalPagarAVista = true;
}

fecharPagarAVista(): void {
  this.modalPagarAVista = false;
  this.itemPagandoAVista = null;
  this.quantidadePagarAVista = null;
}

confirmarPagarAVista(): void {
  if (!this.itemPagandoAVista || !this.quantidadePagarAVista) return;
  this.service.pagarAVista(
    this.itemPagandoAVista.id,
    this.quantidadePagarAVista,
    this.formaPagamentoAVista
  ).subscribe({
    next: () => {
      alert('✅ Pagamento registrado!');
      this.fecharPagarAVista();
      this.carregarDeposito();
    },
    error: (e) => alert('Erro: ' + e.error?.message)
  });
}

}