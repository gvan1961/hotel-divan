import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { PontoService } from '../../services/ponto.service';
import { ReconhecimentoFacialService } from '../../services/reconhecimento-facial.service';

@Component({
  selector: 'app-ponto-registrar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>⏱️ Ponto Eletrônico</h1>
        <div class="header-btns">
          <button class="btn-relatorio" (click)="router.navigate(['/ponto/relatorio'])">📊 Relatório</button>
          <button class="btn-foto" (click)="router.navigate(['/ponto/foto'])">📸 Cadastrar Foto</button>
        </div>
      </div>

      <!-- RELÓGIO -->
      <div class="relogio">
        <div class="hora">{{ horaAtual }}</div>
        <div class="data">{{ dataAtual }}</div>
      </div>

      <!-- STATUS MODELOS -->
      <div class="status-modelos" *ngIf="!modelosCarregados">
        <div class="spinner"></div>
        <p>Carregando modelos de reconhecimento facial...</p>
      </div>

      <!-- CÂMERA E RECONHECIMENTO -->
      <div class="camera-card" *ngIf="modelosCarregados">
        <div class="camera-wrapper">
          <video #videoElement autoplay playsinline class="video"></video>
          <canvas #canvasElement style="display:none"></canvas>

          <div class="camera-overlay" *ngIf="!cameraAtiva">
            <button class="btn-iniciar-camera" (click)="iniciarCamera()">
              📷 Iniciar Câmera
            </button>
          </div>

          <div class="reconhecendo-overlay" *ngIf="reconhecendo">
            <div class="spinner"></div>
            <p>Reconhecendo rosto...</p>
          </div>

          <!-- INDICADOR DE DETECÇÃO -->
          <div class="detector-status" *ngIf="cameraAtiva && !reconhecendo && !funcionarioReconhecido">
            <span class="pulsando">🔍 Procurando rosto...</span>
          </div>
        </div>

        <!-- FUNCIONÁRIO RECONHECIDO -->
        <div class="funcionario-card" *ngIf="funcionarioReconhecido">
          <div class="func-foto">
            <img *ngIf="fotoFuncionario" [src]="'data:image/jpeg;base64,' + fotoFuncionario" />
            <span *ngIf="!fotoFuncionario" class="func-icone">👤</span>
          </div>
          <div class="func-info">
            <h2>{{ funcionarioReconhecido.nome }}</h2>
            <p>CPF: {{ funcionarioReconhecido.cpf }}</p>
            <p class="confianca" *ngIf="confianca">
              🤖 Confiança: {{ (confianca * 100).toFixed(0) }}%
            </p>
            <p class="manual" *ngIf="!confianca">👆 Identificação manual</p>
          </div>
          <button class="btn-trocar" (click)="limparFuncionario()">🔄 Trocar</button>
        </div>

        <!-- BOTÕES DE REGISTRO -->
        <div class="btns-registro" *ngIf="funcionarioReconhecido">
          <button class="btn-tipo btn-entrada" (click)="registrarPonto('ENTRADA')">
            🟢 Entrada
          </button>
          <button class="btn-tipo btn-saida-intervalo" (click)="registrarPonto('SAIDA_INTERVALO')">
            🟡 Saída Intervalo
          </button>
          <button class="btn-tipo btn-retorno-intervalo" (click)="registrarPonto('RETORNO_INTERVALO')">
            🔵 Retorno Intervalo
          </button>
          <button class="btn-tipo btn-saida" (click)="registrarPonto('SAIDA')">
            🔴 Saída
          </button>
        </div>

        <!-- BUSCA MANUAL -->
        <div class="busca-manual" *ngIf="cameraAtiva && !funcionarioReconhecido">
          <p class="ou">— ou identificar manualmente —</p>
          <input type="text" [(ngModel)]="buscaManual" (input)="filtrarFuncionarios()"
                 placeholder="Digite o nome ou CPF..." />
          <div class="lista-resultados" *ngIf="funcionariosFiltrados.length > 0">
            <div class="resultado-item" *ngFor="let f of funcionariosFiltrados"
                 (click)="selecionarFuncionarioManual(f)">
              <strong>{{ f.nome }}</strong>
              <small>CPF: {{ f.cpf }}</small>
            </div>
          </div>
        </div>

        <!-- MENSAGEM DE SUCESSO -->
        <div class="mensagem-sucesso" *ngIf="mensagemSucesso">
          <div class="sucesso-icone">✅</div>
          <h2>{{ mensagemSucesso }}</h2>
          <p>{{ dataHoraRegistro }}</p>
        </div>
      </div>

      <!-- REGISTROS DO DIA -->
      <div class="registros-card">
        <h2>📋 Registros de Hoje</h2>
        <div *ngIf="registrosHoje.length === 0" class="empty">Nenhum registro hoje</div>
        <div class="tabela-registros" *ngIf="registrosHoje.length > 0">
          <table>
            <thead>
              <tr>
                <th>Funcionário</th>
                <th>Tipo</th>
                <th>Hora</th>
                <th>Reconh.</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let r of registrosHoje">
                <td>{{ r.cliente?.nome }}</td>
                <td><span [class]="'badge badge-' + r.tipo.toLowerCase()">{{ formatarTipo(r.tipo) }}</span></td>
                <td>{{ formatarHora(r.dataHora) }}</td>
                <td>{{ r.reconhecimentoFacial ? '🤖' : '👆' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 900px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h1 { color: #333; margin: 0; }
    .header-btns { display: flex; gap: 10px; }
    .btn-relatorio { background: #667eea; color: white; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }
    .btn-foto { background: #27ae60; color: white; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }

    .relogio { text-align: center; margin-bottom: 20px; }
    .hora { font-size: 3em; font-weight: 700; color: #2c3e50; }
    .data { font-size: 1.1em; color: #666; }

    .status-modelos { text-align: center; padding: 40px; background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-bottom: 20px; }
    .status-modelos p { color: #666; margin-top: 15px; }

    .camera-card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-bottom: 20px; }
    .camera-wrapper { position: relative; width: 100%; max-width: 480px; margin: 0 auto; }
    .video { width: 100%; border-radius: 10px; border: 3px solid #667eea; display: block; min-height: 300px; background: #1a1a2e; }
    .camera-overlay { position: absolute; top: 0; left: 0; right: 0; bottom: 0; display: flex; align-items: center; justify-content: center; background: rgba(0,0,0,0.8); border-radius: 10px; }
    .btn-iniciar-camera { background: #667eea; color: white; border: none; padding: 15px 30px; border-radius: 8px; cursor: pointer; font-size: 18px; font-weight: 600; }
    .reconhecendo-overlay { position: absolute; top: 0; left: 0; right: 0; bottom: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; background: rgba(0,0,0,0.6); border-radius: 10px; color: white; }
    .detector-status { position: absolute; bottom: 10px; left: 0; right: 0; text-align: center; }
    .pulsando { background: rgba(0,0,0,0.6); color: white; padding: 5px 15px; border-radius: 20px; font-size: 13px; animation: pulsar 2s ease-in-out infinite; }
    @keyframes pulsar { 0%,100% { opacity: 1; } 50% { opacity: 0.5; } }

    .spinner { width: 40px; height: 40px; border: 4px solid rgba(255,255,255,0.3); border-top-color: white; border-radius: 50%; animation: spin 1s linear infinite; margin-bottom: 10px; }
    @keyframes spin { to { transform: rotate(360deg); } }

    .funcionario-card { display: flex; align-items: center; gap: 20px; background: #e8f5e9; border: 2px solid #27ae60; border-radius: 10px; padding: 20px; margin-top: 20px; }
    .func-foto img { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; border: 3px solid #27ae60; }
    .func-icone { font-size: 4em; }
    .func-info { flex: 1; }
    .func-info h2 { margin: 0 0 5px; color: #2c3e50; }
    .func-info p { margin: 0; color: #666; font-size: 14px; }
    .confianca { color: #27ae60 !important; font-weight: 600 !important; }
    .manual { color: #e67e22 !important; font-weight: 600 !important; }
    .btn-trocar { background: #e67e22; color: white; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }

    .btns-registro { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 20px; }
    .btn-tipo { padding: 15px; border: none; border-radius: 8px; cursor: pointer; font-size: 16px; font-weight: 600; transition: all 0.2s; }
    .btn-tipo:hover { transform: translateY(-2px); box-shadow: 0 4px 8px rgba(0,0,0,0.2); }
    .btn-entrada { background: #d4edda; color: #155724; }
    .btn-saida-intervalo { background: #fff3cd; color: #856404; }
    .btn-retorno-intervalo { background: #d1ecf1; color: #0c5460; }
    .btn-saida { background: #f8d7da; color: #721c24; }

    .busca-manual { margin-top: 20px; }
    .ou { text-align: center; color: #999; margin-bottom: 10px; }
    .busca-manual input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; box-sizing: border-box; }
    .lista-resultados { background: white; border: 1px solid #ddd; border-radius: 5px; max-height: 200px; overflow-y: auto; margin-top: 5px; }
    .resultado-item { padding: 10px 14px; cursor: pointer; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
    .resultado-item:hover { background: #f5f5f5; }

    .mensagem-sucesso { text-align: center; padding: 30px; background: #d4edda; border-radius: 10px; margin-top: 20px; animation: fadeIn 0.5s ease; }
    .sucesso-icone { font-size: 3em; margin-bottom: 10px; }
    .mensagem-sucesso h2 { color: #155724; margin: 0 0 5px; }
    .mensagem-sucesso p { color: #666; margin: 0; }
    @keyframes fadeIn { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }

    .registros-card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
    .registros-card h2 { color: #2c3e50; margin: 0 0 20px; }
    table { width: 100%; border-collapse: collapse; font-size: 14px; }
    th { background: #f8f9fa; padding: 10px; text-align: left; font-weight: 600; border-bottom: 2px solid #dee2e6; }
    td { padding: 10px; border-bottom: 1px solid #dee2e6; }
    .badge { padding: 3px 8px; border-radius: 12px; font-size: 12px; font-weight: 600; }
    .badge-entrada { background: #d4edda; color: #155724; }
    .badge-saida { background: #f8d7da; color: #721c24; }
    .badge-saida_intervalo { background: #fff3cd; color: #856404; }
    .badge-retorno_intervalo { background: #d1ecf1; color: #0c5460; }
    .empty { text-align: center; padding: 30px; color: #666; }
  `]
})
export class PontoRegistrarApp implements OnInit, OnDestroy {
  router = inject(Router);
  private http = inject(HttpClient);
  private pontoService = inject(PontoService);
  private reconhecimentoService = inject(ReconhecimentoFacialService);

  horaAtual = '';
  dataAtual = '';
  cameraAtiva = false;
  reconhecendo = false;
  modelosCarregados = false;
  funcionarioReconhecido: any = null;
  fotoFuncionario: string | null = null;
  confianca: number | null = null;
  mensagemSucesso = '';
  dataHoraRegistro = '';
  registrosHoje: any[] = [];
  buscaManual = '';
  funcionarios: any[] = [];
  funcionariosFiltrados: any[] = [];

  private stream: MediaStream | null = null;
  private videoEl: HTMLVideoElement | null = null;
  private canvasEl: HTMLCanvasElement | null = null;
  private intervaloRelogio: any;
  private intervaloReconhecimento: any;

  async ngOnInit(): Promise<void> {
    this.atualizarRelogio();
    this.intervaloRelogio = setInterval(() => this.atualizarRelogio(), 1000);
    this.carregarRegistrosHoje();
    this.carregarFuncionariosComFoto();

    // Carregar modelos face-api.js
    try {
      await this.reconhecimentoService.carregarModelos();
      this.modelosCarregados = true;
    } catch (e) {
      console.error('Erro ao carregar modelos:', e);
      this.modelosCarregados = true; // Permite uso manual mesmo sem modelos
    }
  }

  ngOnDestroy(): void {
    clearInterval(this.intervaloRelogio);
    clearInterval(this.intervaloReconhecimento);
    this.pararCamera();
  }

  atualizarRelogio(): void {
    const agora = new Date();
    this.horaAtual = agora.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    this.dataAtual = agora.toLocaleDateString('pt-BR', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' });
  }

  carregarRegistrosHoje(): void {
    this.pontoService.listarHoje().subscribe({
      next: (data) => this.registrosHoje = data,
      error: () => {}
    });
  }

  carregarFuncionariosComFoto(): void {
    this.pontoService.listarFotos().subscribe({
      next: (fotos) => {
        this.funcionarios = fotos.map(f => ({
          id: f.cliente?.id,
          nome: f.cliente?.nome,
          cpf: f.cliente?.cpf,
          fotoBase64: f.fotoBase64
        }));
        console.log(`✅ ${this.funcionarios.length} funcionário(s) com foto carregado(s)`);
      },
      error: () => {}
    });
  }

  iniciarCamera(): void {
    setTimeout(() => {
      this.videoEl = document.querySelector('video');
      this.canvasEl = document.querySelector('canvas');

      navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'user', width: { ideal: 640 }, height: { ideal: 480 } },
        audio: false
      }).then(stream => {
        this.stream = stream;
        if (this.videoEl) this.videoEl.srcObject = stream;
        this.cameraAtiva = true;
        this.iniciarReconhecimentoAutomatico();
      }).catch(() => alert('Erro ao acessar câmera. Verifique as permissões.'));
    }, 100);
  }

  iniciarReconhecimentoAutomatico(): void {
    this.intervaloReconhecimento = setInterval(async () => {
      if (!this.funcionarioReconhecido && !this.reconhecendo && this.videoEl) {
        await this.tentarReconhecer();
      }
    }, 2000);
  }

  async tentarReconhecer(): Promise<void> {
    if (!this.videoEl || this.reconhecendo || this.funcionarios.length === 0) return;
    this.reconhecendo = true;

    try {
      const descriptor = await this.reconhecimentoService.obterDescriptorDeVideo(this.videoEl);

      if (descriptor) {
        const resultado = await this.reconhecimentoService.reconhecerNaLista(descriptor, this.funcionarios);

        if (resultado) {
          this.funcionarioReconhecido = resultado.funcionario;
          this.fotoFuncionario = resultado.funcionario.fotoBase64;
          this.confianca = resultado.confianca;
          clearInterval(this.intervaloReconhecimento);
          console.log(`✅ Reconhecido: ${resultado.funcionario.nome} (${(resultado.confianca * 100).toFixed(0)}%)`);
        }
      }
    } catch (e) {
      console.error('Erro no reconhecimento:', e);
    }

    this.reconhecendo = false;
  }

  selecionarFuncionarioManual(f: any): void {
    this.funcionarioReconhecido = f;
    this.fotoFuncionario = f.fotoBase64 || null;
    this.confianca = null;
    this.buscaManual = '';
    this.funcionariosFiltrados = [];
    clearInterval(this.intervaloReconhecimento);

    if (!this.fotoFuncionario) {
      this.pontoService.buscarFotoFuncionario(f.id).subscribe({
        next: (data) => this.fotoFuncionario = data.fotoBase64,
        error: () => {}
      });
    }
  }

  limparFuncionario(): void {
    this.funcionarioReconhecido = null;
    this.fotoFuncionario = null;
    this.confianca = null;
    this.iniciarReconhecimentoAutomatico();
  }

  filtrarFuncionarios(): void {
    if (this.buscaManual.length < 2) { this.funcionariosFiltrados = []; return; }
    this.http.get<any[]>(`/api/clientes/funcionarios/buscar?termo=${this.buscaManual}`).subscribe({
      next: (data) => this.funcionariosFiltrados = data,
      error: () => {}
    });
  }

  registrarPonto(tipo: string): void {
    if (!this.funcionarioReconhecido) return;

    this.pontoService.registrar({
      clienteId: this.funcionarioReconhecido.id,
      tipo,
      reconhecimentoFacial: !!this.confianca,
      confianca: this.confianca || undefined
    }).subscribe({
      next: () => {
        this.mensagemSucesso = `${this.formatarTipo(tipo)} — ${this.funcionarioReconhecido.nome}`;
        this.dataHoraRegistro = new Date().toLocaleString('pt-BR');
        this.carregarRegistrosHoje();
        setTimeout(() => {
          this.mensagemSucesso = '';
          this.limparFuncionario();
        }, 4000);
      },
      error: (e) => alert(e.error?.erro || 'Erro ao registrar ponto')
    });
  }

  pararCamera(): void {
    this.stream?.getTracks().forEach(t => t.stop());
    this.stream = null;
    this.cameraAtiva = false;
    clearInterval(this.intervaloReconhecimento);
  }

  formatarTipo(tipo: string): string {
    const labels: any = {
      ENTRADA: '🟢 Entrada',
      SAIDA_INTERVALO: '🟡 Saída Intervalo',
      RETORNO_INTERVALO: '🔵 Retorno Intervalo',
      SAIDA: '🔴 Saída'
    };
    return labels[tipo] || tipo;
  }

  formatarHora(dataHora: string): string {
    if (!dataHora) return '-';
    return new Date(dataHora).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }
}