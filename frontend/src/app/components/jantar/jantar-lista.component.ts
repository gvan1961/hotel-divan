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
      
      <button (click)="buscar()" class="btn-buscar">
        🔍 Buscar
      </button>
      
      <button (click)="limparBusca()" class="btn-limpar">
        🗑️ Limpar
      </button>
    </div>

    <!-- RESULTADO DA BUSCA -->
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
          <button 
            *ngIf="hospede.autorizadoJantar"
            (click)="abrirModalComandaBusca(hospede)" 
            class="btn-comanda">
            📝 Lançar Comanda
          </button>
          <button 
            *ngIf="!hospede.autorizadoJantar"
            disabled
            class="btn-comanda-disabled">
            🚫 Pagamento à Vista
          </button>
        </div>
      </div>
    </div>
  </div>

  <hr class="separador">

  <div *ngIf="loading" class="loading">
    Carregando...
  </div>

  <div *ngIf="erro" class="erro">
    {{ erro }}
  </div>

  <div *ngIf="!loading && apartamentos.length === 0" class="vazio">
    😴 Nenhum apartamento com hóspedes autorizados
  </div>

  <!-- ✅ LISTA DE APARTAMENTOS - UMA LINHA POR APARTAMENTO -->
  <div class="apartamentos-lista">
    <div *ngFor="let apto of apartamentos" class="apartamento-linha">

      <!-- COLUNA APARTAMENTO -->
      <div class="apto-numero">
        🏢 <strong>{{ apto.numeroApartamento }}</strong>
        <span class="badge">{{ apto.hospedes.length }}</span>
      </div>

      <!-- COLUNA HÓSPEDES -->
      <div class="apto-hospedes">
        <div *ngFor="let hospede of apto.hospedes" class="hospede-linha">
          <span class="nome">{{ hospede.nomeCompleto }}</span>
          <span *ngIf="hospede.titular" class="titular">👑</span>
        </div>
      </div>

      <!-- COLUNA AÇÃO -->
      <div class="apto-acoes">
        <button 
          *ngFor="let hospede of apto.hospedes"
          (click)="abrirModalComanda(hospede, apto.numeroApartamento)" 
          class="btn-comanda">
          📝 {{ hospede.nomeCompleto.split(' ')[0] }}
        </button>
      </div>

    </div>
  </div>

  <!-- MODAL DE COMANDA -->
  <div *ngIf="modalAberto" class="modal-overlay" (click)="fecharModal()">
    <div class="modal-content" (click)="$event.stopPropagation()">
      <div class="modal-header">
        <h3>📝 Lançar Comanda</h3>
        <button class="btn-fechar" (click)="fecharModal()">✕</button>
      </div>

      <div class="modal-body">
        <div class="info-hospede">
          <p><strong>👤 Hóspede:</strong> {{ hospedeSelecionado?.nomeCompleto }}</p>
          <p><strong>🏢 Apartamento:</strong> {{ apartamentoSelecionado }}</p>
        </div>

        <!-- PRODUTOS DISPONÍVEIS -->
        <div class="produtos-section">
          <h4>Produtos do Restaurante:</h4>
          
          <div *ngIf="produtosCarregando" class="temp-message">
            ⏳ Carregando produtos...
          </div>

          <div *ngIf="!produtosCarregando" class="produtos-lista">
            <div *ngFor="let produto of produtos" class="produto-item">
              <div class="produto-info">
                <span class="produto-nome">{{ produto.nomeProduto }}</span>
                <span class="produto-preco">R$ {{ produto.valorVenda.toFixed(2) }}</span>
              </div>
              <button (click)="adicionarProduto(produto)" class="btn-adicionar">
                ➕ Adicionar
              </button>
            </div>
          </div>
        </div>

        <!-- PRODUTOS SELECIONADOS -->
        <div *ngIf="produtosSelecionados.length > 0" class="selecionados-section">
          <h4>Itens da Comanda:</h4>
          
          <div class="selecionados-lista">
            <div *ngFor="let item of produtosSelecionados; let i = index" class="item-selecionado">
              <div class="item-info">
                <span class="item-nome">{{ item.produto.nomeProduto }}</span>
                <div class="item-controles">
                  <input 
                    type="number" 
                    [(ngModel)]="item.quantidade"
                    (change)="alterarQuantidade(i, item.quantidade)"
                    min="1"
                    class="input-quantidade">
                  <span class="item-subtotal">
                    R$ {{ (item.produto.valorVenda * item.quantidade).toFixed(2) }}
                  </span>
                  <button (click)="removerProduto(i)" class="btn-remover">🗑️</button>
                </div>
              </div>
            </div>
          </div>

          <div class="total-section">
            <strong>TOTAL:</strong>
            <span class="total-valor">R$ {{ calcularTotal().toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancelar" (click)="fecharModal()">Cancelar</button>
        <button class="btn-confirmar" (click)="confirmarComanda()">
          Confirmar Comanda ({{ produtosSelecionados.length }} item(ns))
        </button>
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

.header h2 {
  margin: 0;
  color: #333;
}

.btn-atualizar {
  padding: 10px 20px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
}

.btn-atualizar:hover {
  background-color: #45a049;
}

.loading, .erro, .vazio {
  text-align: center;
  padding: 40px;
  font-size: 18px;
  color: #666;
}

.erro {
  color: #f44336;
}

/* ✅ LISTA DE APARTAMENTOS - UMA LINHA POR APARTAMENTO */
.apartamentos-lista {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

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

.apto-hospedes {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hospede-linha {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #f5f5f5;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.9em;
}

.apto-acoes {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.badge {
  background-color: #ff9800;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.nome {
  font-weight: 500;
  color: #333;
}

.titular {
  font-size: 12px;
  color: #ff9800;
  font-weight: bold;
}

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

.btn-comanda:hover {
  background-color: #1976D2;
}

/* MODAL */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
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

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #ddd;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.btn-fechar {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.btn-fechar:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.info-hospede {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 5px;
  margin-bottom: 20px;
}

.info-hospede p {
  margin: 5px 0;
}

.produtos-section h4 {
  margin-bottom: 15px;
  color: #333;
}

.temp-message {
  text-align: center;
  padding: 20px;
  color: #666;
}

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

.btn-cancelar:hover {
  background-color: #da190b;
}

.btn-confirmar {
  padding: 10px 20px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.btn-confirmar:hover {
  background-color: #45a049;
}

/* PRODUTOS */
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
}

.produto-nome {
  font-weight: 500;
  color: #333;
}

.produto-preco {
  color: #4CAF50;
  font-weight: bold;
}

.btn-adicionar {
  padding: 6px 12px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-adicionar:hover {
  background-color: #45a049;
}

/* ITENS SELECIONADOS */
.selecionados-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid #ddd;
}

.selecionados-section h4 {
  margin-bottom: 15px;
  color: #333;
}

.selecionados-lista {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

.item-selecionado {
  padding: 12px;
  background-color: #e8f5e9;
  border-radius: 5px;
  border-left: 4px solid #4CAF50;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-nome {
  font-weight: 500;
  color: #333;
}

.item-controles {
  display: flex;
  align-items: center;
  gap: 10px;
}

.input-quantidade {
  width: 60px;
  padding: 5px;
  border: 1px solid #ddd;
  border-radius: 4px;
  text-align: center;
}

.item-subtotal {
  color: #4CAF50;
  font-weight: bold;
  flex: 1;
}

.btn-remover {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
}

.btn-remover:hover {
  transform: scale(1.2);
}

/* TOTAL */
.total-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 5px;
  font-size: 18px;
}

.total-valor {
  color: #4CAF50;
  font-weight: bold;
  font-size: 20px;
}

/* BUSCA DE HÓSPEDE */
.busca-hospede {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.busca-hospede h3 {
  margin: 0 0 15px 0;
  color: #2196F3;
}

.busca-form {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

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

.btn-buscar:hover {
  background-color: #1976D2;
}

.btn-limpar {
  padding: 10px 20px;
  background-color: #9E9E9E;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
}

.btn-limpar:hover {
  background-color: #757575;
}

/* RESULTADO DA BUSCA */
.resultado-busca {
  margin-top: 20px;
}

.busca-nao-encontrado {
  padding: 15px;
  background-color: #FFF3CD;
  border: 1px solid #FFC107;
  border-radius: 5px;
  color: #856404;
  text-align: center;
}

.busca-encontrados h4 {
  margin: 0 0 15px 0;
  color: #333;
}

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

.hospede-busca-info {
  flex: 1;
}

.info-linha {
  margin: 5px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.nome-destaque {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

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

.separador {
  border: none;
  border-top: 2px dashed #ddd;
  margin: 30px 0;
}
  `]
})
export class JantarListaComponent implements OnInit {
  apartamentos: { numeroApartamento: string, hospedes: HospedeJantar[] }[] = [];
  loading = false;
  erro = '';

  // Modal
  modalAberto = false;
  hospedeSelecionado: HospedeJantar | null = null;
  apartamentoSelecionado = '';

  // Produtos
  produtos: any[] = [];
  produtosCarregando = false;
  produtosSelecionados: { produto: any, quantidade: number }[] = [];

  nomeBusca = '';
  apartamentoBusca = '';
  resultadoBusca: any = null;

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
        console.log('✅ Dados do backend:', data);
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
        console.log('✅ Apartamentos processados:', this.apartamentos);
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
    console.log('🍽️ Abrindo modal para:', hospede.nomeCompleto, '- Apto:', numeroApartamento);
    this.hospedeSelecionado = hospede;
    this.apartamentoSelecionado = numeroApartamento;
    this.modalAberto = true;
    this.carregarProdutos();
  }

  carregarProdutos() {
    this.produtosCarregando = true;
    this.http.get<any[]>('/api/jantar/produtos-restaurante').subscribe({
      next: (produtos) => {
        console.log('✅ Produtos carregados:', produtos);
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
    console.log('🔍 DEBUG - hospedeSelecionado:', this.hospedeSelecionado);
    console.log('🔍 DEBUG - hospedeSelecionado.id:', this.hospedeSelecionado?.id);
    console.log('🔍 DEBUG - hospedeSelecionado.hospedagemHospedeId:', this.hospedeSelecionado?.hospedagemHospedeId);

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
        alert('❌ Erro ao salvar comanda:\n' + (error.error || error.message));
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
      next: (resultado) => {
        console.log('✅ Resultado da busca:', resultado);
        this.resultadoBusca = resultado;
      },
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
    console.log('🍽️ Abrindo modal para hóspede da busca:', hospede);
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
}
