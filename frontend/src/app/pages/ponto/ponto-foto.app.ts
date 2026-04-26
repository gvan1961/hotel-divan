import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { PontoService } from '../../services/ponto.service';

@Component({
  selector: 'app-ponto-foto',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>📸 Cadastro de Foto — Ponto Eletrônico</h1>
        <button class="btn-back" (click)="router.navigate(['/ponto'])">← Voltar</button>
      </div>

      <!-- BUSCA DE FUNCIONÁRIO -->
      <div class="form-card">
        <div class="form-group">
          <label>Funcionário *</label>
          <div class="busca-wrapper">
            <input type="text" [(ngModel)]="buscaFuncionario" (input)="filtrarFuncionarios()"
                   placeholder="Digite o nome ou CPF do funcionário..." />
            <div class="lista-resultados" *ngIf="funcionariosFiltrados.length > 0 && !funcionarioSelecionado">
              <div class="resultado-item" *ngFor="let f of funcionariosFiltrados" (click)="selecionarFuncionario(f)">
                <strong>{{ f.nome }}</strong>
                <small>CPF: {{ f.cpf }}</small>
              </div>
            </div>
          </div>
          <div class="funcionario-selecionado" *ngIf="funcionarioSelecionado">
            ✅ {{ funcionarioSelecionado.nome }}
            <button type="button" class="btn-limpar" (click)="limparFuncionario()">✕</button>
          </div>
        </div>

        <!-- CÂMERA -->
        <div class="camera-section" *ngIf="funcionarioSelecionado">
          
          <div class="foto-atual" *ngIf="fotoAtual">
            <h3>📷 Foto Atual</h3>
            <img [src]="'data:image/jpeg;base64,' + fotoAtual" class="foto-preview" />
          </div>

          <div class="camera-container">
            <h3>{{ fotoAtual ? '🔄 Nova Foto' : '📸 Tirar Foto' }}</h3>
            <video #videoElement autoplay playsinline class="video-preview"></video>
            <canvas #canvasElement style="display:none"></canvas>
            
            <div class="camera-btns">
              <button type="button" class="btn-camera" (click)="iniciarCamera()" *ngIf="!cameraAtiva">
                📷 Abrir Câmera
              </button>
              <button type="button" class="btn-capturar" (click)="capturarFoto()" *ngIf="cameraAtiva">
                📸 Capturar Foto
              </button>
              <button type="button" class="btn-cancelar-camera" (click)="pararCamera()" *ngIf="cameraAtiva">
                ❌ Cancelar
              </button>
            </div>
          </div>

          <!-- PREVIEW DA FOTO CAPTURADA -->
          <div class="foto-capturada" *ngIf="fotoCapturada">
            <h3>✅ Foto Capturada</h3>
            <img [src]="fotoCapturada" class="foto-preview" />
            <div class="btns-confirmar">
              <button type="button" class="btn-cancelar" (click)="descartarFoto()">🔄 Tirar Outra</button>
              <button type="button" class="btn-salvar" (click)="salvarFoto()" [disabled]="salvando">
                {{ salvando ? 'Salvando...' : '✅ Salvar Foto' }}
              </button>
            </div>
          </div>

          <div *ngIf="mensagem" class="mensagem" [class.erro]="tipoMensagem === 'erro'">
            {{ mensagem }}
          </div>
        </div>
      </div>

      <!-- LISTA DE FUNCIONÁRIOS COM FOTO -->
      <div class="form-card" style="margin-top:20px">
        <h2>👥 Funcionários Cadastrados</h2>
        <div *ngIf="fotos.length === 0" class="empty">Nenhum funcionário com foto cadastrada</div>
        <div class="lista-fotos" *ngIf="fotos.length > 0">
          <div class="foto-item" *ngFor="let f of fotos">
            <img [src]="'data:image/jpeg;base64,' + f.fotoBase64" class="foto-mini" />
            <span>{{ f.cliente?.nome }}</span>
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
    .form-group { margin-bottom: 20px; }
    .form-group label { display: block; margin-bottom: 6px; font-weight: 600; color: #555; }
    .form-group input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; box-sizing: border-box; }
    .busca-wrapper { position: relative; }
    .lista-resultados { position: absolute; top: 100%; left: 0; right: 0; background: white; border: 1px solid #ddd; border-radius: 5px; max-height: 200px; overflow-y: auto; z-index: 100; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
    .resultado-item { padding: 10px 14px; cursor: pointer; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; }
    .resultado-item:hover { background: #f5f5f5; }
    .funcionario-selecionado { background: #e8f5e9; padding: 10px 14px; border-radius: 5px; border: 2px solid #27ae60; color: #1b5e20; display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
    .btn-limpar { background: none; border: none; color: #e53935; cursor: pointer; font-size: 16px; }
    .camera-section { margin-top: 20px; }
    .camera-container { margin-top: 20px; }
    .camera-container h3 { color: #2c3e50; margin-bottom: 10px; }
    .video-preview { width: 100%; max-width: 480px; border-radius: 8px; border: 3px solid #667eea; display: block; margin: 0 auto; }
    .foto-preview { width: 100%; max-width: 300px; border-radius: 8px; border: 3px solid #27ae60; display: block; margin: 10px auto; }
    .camera-btns { display: flex; gap: 10px; justify-content: center; margin-top: 15px; }
    .btn-camera { background: #667eea; color: white; border: none; padding: 12px 24px; border-radius: 6px; cursor: pointer; font-size: 16px; font-weight: 600; }
    .btn-capturar { background: #27ae60; color: white; border: none; padding: 12px 24px; border-radius: 6px; cursor: pointer; font-size: 16px; font-weight: 600; }
    .btn-cancelar-camera { background: #e74c3c; color: white; border: none; padding: 12px 24px; border-radius: 6px; cursor: pointer; font-size: 16px; }
    .btns-confirmar { display: flex; gap: 10px; justify-content: center; margin-top: 15px; }
    .btn-cancelar { background: #6c757d; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
    .btn-salvar { background: #27ae60; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; font-weight: 600; }
    .btn-salvar:disabled { background: #aaa; cursor: not-allowed; }
    .mensagem { padding: 12px; border-radius: 5px; margin-top: 15px; background: #d4edda; color: #155724; text-align: center; font-weight: 600; }
    .mensagem.erro { background: #f8d7da; color: #721c24; }
    .foto-atual { margin-bottom: 20px; }
    .foto-capturada { margin-top: 20px; text-align: center; }
    .lista-fotos { display: flex; flex-wrap: wrap; gap: 15px; margin-top: 15px; }
    .foto-item { display: flex; flex-direction: column; align-items: center; gap: 8px; }
    .foto-mini { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; border: 3px solid #667eea; }
    .empty { text-align: center; padding: 30px; color: #666; }
    h2 { color: #2c3e50; margin: 0 0 20px; }
  `]
})
export class PontoFotoApp implements OnInit {
  router = inject(Router);
  private http = inject(HttpClient);
  private pontoService = inject(PontoService);

  buscaFuncionario = '';
  funcionarios: any[] = [];
  funcionariosFiltrados: any[] = [];
  funcionarioSelecionado: any = null;

  cameraAtiva = false;
  fotoCapturada: string | null = null;
  fotoAtual: string | null = null;
  salvando = false;
  mensagem = '';
  tipoMensagem = 'sucesso';
  fotos: any[] = [];

  private stream: MediaStream | null = null;
  private videoEl: HTMLVideoElement | null = null;
  private canvasEl: HTMLCanvasElement | null = null;

  ngOnInit(): void {
    this.carregarFuncionarios();
    this.carregarFotos();
  }

  carregarFuncionarios(): void {
  this.http.get<any[]>('/api/clientes/funcionarios/buscar?termo=a').subscribe({
    next: (data) => this.funcionarios = data,
    error: () => {}
  });
}

  carregarFotos(): void {
    this.pontoService.listarFotos().subscribe({
      next: (data) => this.fotos = data,
      error: () => {}
    });
  }

  filtrarFuncionarios(): void {
  if (this.buscaFuncionario.length < 2) { this.funcionariosFiltrados = []; return; }
  this.http.get<any[]>(`/api/clientes/funcionarios/buscar?termo=${this.buscaFuncionario}`).subscribe({
    next: (data) => this.funcionariosFiltrados = data,
    error: () => {}
  });
}

  selecionarFuncionario(f: any): void {
    this.funcionarioSelecionado = f;
    this.buscaFuncionario = f.nome;
    this.funcionariosFiltrados = [];
    this.carregarFotoAtual(f.id);
  }

  limparFuncionario(): void {
    this.funcionarioSelecionado = null;
    this.buscaFuncionario = '';
    this.fotoAtual = null;
    this.fotoCapturada = null;
    this.pararCamera();
  }

  carregarFotoAtual(clienteId: number): void {
    this.pontoService.buscarFotoFuncionario(clienteId).subscribe({
      next: (data) => this.fotoAtual = data.fotoBase64,
      error: () => this.fotoAtual = null
    });
  }

  iniciarCamera(): void {
    setTimeout(() => {
      this.videoEl = document.querySelector('video');
      this.canvasEl = document.querySelector('canvas');

      navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user' }, audio: false })
        .then(stream => {
          this.stream = stream;
          if (this.videoEl) this.videoEl.srcObject = stream;
          this.cameraAtiva = true;
        })
        .catch(() => {
          this.mensagem = 'Erro ao acessar a câmera. Verifique as permissões.';
          this.tipoMensagem = 'erro';
        });
    }, 100);
  }

  capturarFoto(): void {
    if (!this.videoEl || !this.canvasEl) return;
    this.canvasEl.width = this.videoEl.videoWidth;
    this.canvasEl.height = this.videoEl.videoHeight;
    this.canvasEl.getContext('2d')?.drawImage(this.videoEl, 0, 0);
    this.fotoCapturada = this.canvasEl.toDataURL('image/jpeg', 0.8);
    this.pararCamera();
  }

  pararCamera(): void {
    this.stream?.getTracks().forEach(t => t.stop());
    this.stream = null;
    this.cameraAtiva = false;
  }

  descartarFoto(): void {
    this.fotoCapturada = null;
    this.iniciarCamera();
  }

  salvarFoto(): void {
    if (!this.fotoCapturada || !this.funcionarioSelecionado) return;
    this.salvando = true;
    const base64 = this.fotoCapturada.split(',')[1];
    this.pontoService.cadastrarFoto(this.funcionarioSelecionado.id, base64).subscribe({
      next: () => {
        this.salvando = false;
        this.mensagem = '✅ Foto cadastrada com sucesso!';
        this.tipoMensagem = 'sucesso';
        this.fotoAtual = base64;
        this.fotoCapturada = null;
        this.carregarFotos();
      },
      error: (e) => {
        this.salvando = false;
        this.mensagem = e.error?.erro || 'Erro ao salvar foto';
        this.tipoMensagem = 'erro';
      }
    });
  }
}