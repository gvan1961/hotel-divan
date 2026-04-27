import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-ponto-ajuste',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>⏱️ Ajuste de Ponto</h1>
        <button class="btn-back" (click)="router.navigate(['/ponto/relatorio'])">← Voltar</button>
      </div>

      <!-- FILTROS -->
      <div class="form-card">
        <h2>🔍 Buscar Registros</h2>
        <div class="form-row">
          <div class="form-group">
            <label>Funcionário *</label>
            <div class="busca-wrapper">
              <input type="text" [(ngModel)]="buscaFuncionario" (input)="filtrarFuncionarios()"
                     placeholder="Buscar funcionário por nome..." />
              <div class="lista-resultados" *ngIf="funcionariosFiltrados.length > 0 && !funcionarioSelecionado">
                <div class="resultado-item" *ngFor="let f of funcionariosFiltrados"
                     (click)="selecionarFuncionario(f)">
                  <strong>{{ f.nome }}</strong>
                  <small>{{ f.cpf }}</small>
                </div>
              </div>
            </div>
            <div class="funcionario-selecionado" *ngIf="funcionarioSelecionado">
              ✅ {{ funcionarioSelecionado.nome }}
              <button type="button" class="btn-limpar" (click)="limparFuncionario()">✕</button>
            </div>
          </div>
          <div class="form-group">
            <label>Data *</label>
            <input type="date" [(ngModel)]="dataSelecionada" />
          </div>
        </div>
        <div class="form-actions">
          <button class="btn-buscar" (click)="buscarRegistros()"
                  [disabled]="!funcionarioSelecionado || !dataSelecionada">
            🔍 Buscar Registros
          </button>
        </div>
      </div>

      <!-- REGISTROS DO DIA -->
      <div class="form-card" *ngIf="registros.length > 0 || buscaRealizada">
        <div class="registros-header">
          <h2>📋 Registros de {{ formatarData(dataSelecionada) }} — {{ funcionarioSelecionado?.nome }}</h2>
          <button class="btn-adicionar" (click)="abrirModalAdicionar()">
            ➕ Adicionar Registro
          </button>
        </div>

        <div *ngIf="registros.length === 0" class="empty">
          Nenhum registro encontrado para esta data.
        </div>

        <div class="lista-registros" *ngIf="registros.length > 0">
          <div class="registro-item" *ngFor="let r of registros"
               [class.registro-ajustado]="r.ajustado">
            <div class="registro-tipo">
              <span [class]="'badge badge-' + r.tipo.toLowerCase()">{{ formatarTipo(r.tipo) }}</span>
              <span class="badge-ajuste" *ngIf="r.ajustado">✏️ Ajustado</span>
            </div>
            <div class="registro-hora">
              🕐 {{ formatarHora(r.dataHora) }}
            </div>
            <div class="registro-obs" *ngIf="r.motivoAjuste">
              📝 {{ r.motivoAjuste }}
            </div>
            <div class="registro-acoes">
              <button class="btn-editar" (click)="abrirModalEditar(r)">✏️ Editar</button>
              <button class="btn-excluir" (click)="excluirRegistro(r)">🗑️</button>
            </div>
          </div>
        </div>
      </div>

      <!-- MODAL ADICIONAR/EDITAR -->
      <div class="modal-overlay" *ngIf="modalAberto" (click)="fecharModal()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>{{ modoModal === 'adicionar' ? '➕ Adicionar Registro' : '✏️ Editar Registro' }}</h2>

          <div class="form-group">
            <label>Tipo *</label>
            <select [(ngModel)]="ajuste.tipo">
              <option value="ENTRADA">Entrada</option>
              <option value="SAIDA_INTERVALO">Saída Intervalo</option>
              <option value="RETORNO_INTERVALO">Retorno Intervalo</option>
              <option value="SAIDA">Saída</option>
            </select>
          </div>

          <div class="form-group">
            <label>Data e Hora *</label>
            <input type="datetime-local" [(ngModel)]="ajuste.dataHora" />
          </div>

          <div class="form-group">
            <label>Motivo do Ajuste *</label>
            <select [(ngModel)]="ajuste.motivoAjuste">
              <option value="">Selecione o motivo...</option>
              <option value="Funcionário esqueceu de registrar entrada">Funcionário esqueceu de registrar entrada</option>
              <option value="Funcionário esqueceu de registrar saída">Funcionário esqueceu de registrar saída</option>
              <option value="Erro no reconhecimento facial">Erro no reconhecimento facial</option>
              <option value="Sistema indisponível no momento">Sistema indisponível no momento</option>
              <option value="Correção de horário incorreto">Correção de horário incorreto</option>
              <option value="Outro">Outro</option>
            </select>
          </div>

          <div *ngIf="errorMessage" class="error-message">{{ errorMessage }}</div>

          <div class="form-actions">
            <button class="btn-cancel" (click)="fecharModal()">Cancelar</button>
            <button class="btn-save" (click)="salvarAjuste()" [disabled]="loading">
              {{ loading ? 'Salvando...' : '✅ Salvar' }}
            </button>
          </div>

          <button class="btn-fechar-modal" (click)="fecharModal()">✕</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 1000px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h1 { color: #333; margin: 0; }
    .btn-back { background: #6c757d; color: white; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }

    .form-card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }
    h2 { color: #2c3e50; margin: 0 0 20px; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
    .form-group { margin-bottom: 16px; }
    .form-group label { display: block; margin-bottom: 6px; font-weight: 600; color: #555; }
    .form-group input, .form-group select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; box-sizing: border-box; }

    .busca-wrapper { position: relative; }
    .lista-resultados { position: absolute; top: 100%; left: 0; right: 0; background: white; border: 1px solid #ddd; border-radius: 5px; max-height: 200px; overflow-y: auto; z-index: 100; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
    .resultado-item { padding: 10px 14px; cursor: pointer; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
    .resultado-item:hover { background: #f5f5f5; }
    .funcionario-selecionado { background: #e8f5e9; padding: 10px 14px; border-radius: 5px; border: 2px solid #27ae60; color: #1b5e20; font-size: 14px; display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
    .btn-limpar { background: none; border: none; color: #e53935; cursor: pointer; font-size: 16px; font-weight: bold; }

    .registros-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .registros-header h2 { margin: 0; }
    .btn-adicionar { background: #27ae60; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; font-weight: 600; }

    .lista-registros { display: flex; flex-direction: column; gap: 10px; }
    .registro-item { display: flex; align-items: center; gap: 15px; background: #f8f9fa; padding: 15px; border-radius: 6px; border-left: 4px solid #667eea; }
    .registro-ajustado { border-left-color: #f39c12; background: #fffbf0; }
    .registro-tipo { display: flex; gap: 8px; align-items: center; min-width: 200px; }
    .registro-hora { flex: 1; font-weight: 600; color: #2c3e50; }
    .registro-obs { flex: 2; font-size: 13px; color: #666; font-style: italic; }
    .registro-acoes { display: flex; gap: 8px; }

    .badge { padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
    .badge-entrada { background: #d4edda; color: #155724; }
    .badge-saida { background: #f8d7da; color: #721c24; }
    .badge-saida_intervalo { background: #fff3cd; color: #856404; }
    .badge-retorno_intervalo { background: #cce5ff; color: #004085; }
    .badge-ajuste { background: #fff3cd; color: #856404; font-size: 11px; padding: 3px 8px; border-radius: 10px; }

    .btn-editar { background: #3498db; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 12px; }
    .btn-excluir { background: #e74c3c; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 12px; }

    .empty { text-align: center; padding: 30px; color: #666; }

    .modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 9999; }
    .modal-content { background: white; border-radius: 12px; padding: 30px; max-width: 500px; width: 90%; position: relative; box-shadow: 0 10px 40px rgba(0,0,0,0.3); }
    .modal-content h2 { margin: 0 0 20px; color: #2c3e50; }
    .btn-fechar-modal { position: absolute; top: 15px; right: 15px; background: #e74c3c; color: white; border: none; width: 30px; height: 30px; border-radius: 50%; cursor: pointer; font-size: 1.2em; }

    .error-message { background: #fee; color: #c33; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
    .form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
    .btn-cancel { background: #6c757d; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
    .btn-save { background: #667eea; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; font-weight: 600; }
    .btn-save:disabled { background: #aaa; cursor: not-allowed; }
    .btn-buscar { background: #667eea; color: white; border: none; padding: 10px 24px; border-radius: 5px; cursor: pointer; font-weight: 600; }
    .btn-buscar:disabled { background: #aaa; cursor: not-allowed; }
    @media (max-width: 768px) { .form-row { grid-template-columns: 1fr; } }
  `]
})
export class PontoAjusteApp implements OnInit {
  router = inject(Router);
  private http = inject(HttpClient);

  buscaFuncionario = '';
  funcionarios: any[] = [];
  funcionariosFiltrados: any[] = [];
  funcionarioSelecionado: any = null;
  dataSelecionada = '';
  registros: any[] = [];
  buscaRealizada = false;
  loading = false;
  errorMessage = '';

  modalAberto = false;
  modoModal: 'adicionar' | 'editar' = 'adicionar';
  registroEditando: any = null;

  ajuste = {
    tipo: 'ENTRADA',
    dataHora: '',
    motivoAjuste: ''
  };

  ngOnInit(): void {
    const hoje = new Date();
    this.dataSelecionada = `${hoje.getFullYear()}-${String(hoje.getMonth()+1).padStart(2,'0')}-${String(hoje.getDate()).padStart(2,'0')}`;
    this.carregarFuncionarios();
  }

  carregarFuncionarios(): void {
  this.http.get<any[]>('/api/clientes/funcionarios').subscribe({
    next: (data) => {
      this.funcionarios = data;
      console.log('Funcionários carregados:', this.funcionarios.length);
    },
    error: (e) => console.error('Erro:', e)
  });
}

  filtrarFuncionarios(): void {
    if (this.buscaFuncionario.length < 2) { this.funcionariosFiltrados = []; return; }
    const termo = this.buscaFuncionario.toLowerCase();
    this.funcionariosFiltrados = this.funcionarios.filter(f =>
      f.nome.toLowerCase().includes(termo)
    );
  }

  selecionarFuncionario(f: any): void {
    this.funcionarioSelecionado = f;
    this.buscaFuncionario = f.nome;
    this.funcionariosFiltrados = [];
  }

  limparFuncionario(): void {
    this.funcionarioSelecionado = null;
    this.buscaFuncionario = '';
    this.funcionariosFiltrados = [];
    this.registros = [];
    this.buscaRealizada = false;
  }

  buscarRegistros(): void {
    if (!this.funcionarioSelecionado || !this.dataSelecionada) return;
    this.http.get<any[]>(`/api/ponto/ajuste/funcionario/${this.funcionarioSelecionado.id}/data/${this.dataSelecionada}`).subscribe({
      next: (data) => {
        this.registros = data;
        this.buscaRealizada = true;
      },
      error: () => { this.buscaRealizada = true; }
    });
  }

  abrirModalAdicionar(): void {
    this.modoModal = 'adicionar';
    this.registroEditando = null;
    this.ajuste = {
      tipo: 'ENTRADA',
      dataHora: `${this.dataSelecionada}T08:00`,
      motivoAjuste: ''
    };
    this.errorMessage = '';
    this.modalAberto = true;
  }

  abrirModalEditar(registro: any): void {
    this.modoModal = 'editar';
    this.registroEditando = registro;
    this.ajuste = {
      tipo: registro.tipo,
      dataHora: registro.dataHora.substring(0, 16),
      motivoAjuste: registro.motivoAjuste || ''
    };
    this.errorMessage = '';
    this.modalAberto = true;
  }

  fecharModal(): void {
    this.modalAberto = false;
    this.errorMessage = '';
  }

  salvarAjuste(): void {
    if (!this.ajuste.tipo || !this.ajuste.dataHora || !this.ajuste.motivoAjuste) {
      this.errorMessage = 'Preencha todos os campos obrigatórios';
      return;
    }
    this.loading = true;
    this.errorMessage = '';

    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    const ajustadoPor = usuario.nome || usuario.username || 'Admin';

    if (this.modoModal === 'adicionar') {
      const payload = {
        clienteId: this.funcionarioSelecionado.id,
        tipo: this.ajuste.tipo,
        dataHora: this.ajuste.dataHora + ':00',
        motivoAjuste: this.ajuste.motivoAjuste,
        ajustadoPor
      };
      this.http.post('/api/ponto/ajuste', payload).subscribe({
        next: () => {
          this.loading = false;
          this.fecharModal();
          this.buscarRegistros();
        },
        error: (e) => {
          this.loading = false;
          this.errorMessage = e.error?.erro || 'Erro ao salvar ajuste';
        }
      });
    } else {
      const payload = {
        tipo: this.ajuste.tipo,
        dataHora: this.ajuste.dataHora + ':00',
        motivoAjuste: this.ajuste.motivoAjuste,
        ajustadoPor
      };
      this.http.put(`/api/ponto/ajuste/${this.registroEditando.id}`, payload).subscribe({
        next: () => {
          this.loading = false;
          this.fecharModal();
          this.buscarRegistros();
        },
        error: (e) => {
          this.loading = false;
          this.errorMessage = e.error?.erro || 'Erro ao editar ajuste';
        }
      });
    }
  }

  excluirRegistro(registro: any): void {
    if (!confirm(`Deseja excluir o registro de ${this.formatarTipo(registro.tipo)} às ${this.formatarHora(registro.dataHora)}?`)) return;
    this.http.delete(`/api/ponto/${registro.id}`).subscribe({
      next: () => this.buscarRegistros(),
      error: () => alert('Erro ao excluir registro')
    });
  }

  formatarTipo(tipo: string): string {
    const tipos: any = {
      'ENTRADA': 'Entrada',
      'SAIDA': 'Saída',
      'SAIDA_INTERVALO': 'Saída Intervalo',
      'RETORNO_INTERVALO': 'Retorno Intervalo'
    };
    return tipos[tipo] || tipo;
  }

  formatarHora(dataHora: string): string {
    if (!dataHora) return '-';
    return new Date(dataHora).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  formatarData(data: string): string {
    if (!data) return '-';
    const [ano, mes, dia] = data.split('-');
    return `${dia}/${mes}/${ano}`;
  }
}