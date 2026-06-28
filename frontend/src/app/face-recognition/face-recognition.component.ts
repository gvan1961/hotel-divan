import { Component, OnDestroy, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import * as faceapi from 'face-api.js';

@Component({
  selector: 'app-face-recognition',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <div class="header">
        <h1>📷 Reconhecimento Facial</h1>
        <button class="btn-back" (click)="voltar()">← Voltar</button>
      </div>

      <div class="content">

        <!-- CÂMERA -->
        <div class="camera-panel">
          <video #videoEl autoplay muted playsinline
            style="width:100%; border-radius:8px; border: 3px solid"
            [style.border-color]="statusCor">
          </video>
          <div class="status-bar" [style.background]="statusCor">
            {{ statusMsg }}
          </div>
          <div class="camera-controls">
            <button type="button" *ngIf="!monitorando" (click)="iniciarMonitoramento()"
              class="btn-iniciar">
              ▶ Iniciar Monitoramento
            </button>
            <button type="button" *ngIf="monitorando" (click)="pausarMonitoramento()"
              class="btn-pausar">
              ⏸ Pausar
            </button>
          </div>
        </div>

        <!-- RESULTADO -->
        <div class="resultado-panel">

          <!-- Reconhecido -->
          <div *ngIf="resultado && resultado.reconhecido" class="resultado-ok">
            <img *ngIf="resultado.fotoBase64" [src]="resultado.fotoBase64"
              width="120" height="100" style="border-radius:8px; border: 2px solid green;" />
            <h2>✅ {{ resultado.nomeCliente }}</h2>
            <span class="badge" [ngClass]="badgeClass(resultado.classificacao)">
              {{ resultado.classificacao || 'Sem classificação' }}
            </span>
            <button type="button" (click)="verFicha(resultado.clienteId)"
              class="btn-ficha">
              👤 Ver Ficha
            </button>
          </div>

          <!-- Desconhecido -->
          <div *ngIf="resultado && !resultado.reconhecido" class="resultado-nok">
            <div style="font-size:64px">⚠️</div>
            <h2>Pessoa não identificada</h2>
            <small>Alerta registrado automaticamente</small>
          </div>

          <!-- Aguardando -->
          <div *ngIf="!resultado" class="resultado-aguardando">
            <div style="font-size:64px">👤</div>
            <p>Aguardando detecção...</p>
          </div>

        </div>
      </div>

      <!-- ALERTAS PENDENTES -->
      <div class="alertas-panel" *ngIf="alertas.length > 0">
        <h3>🔔 Alertas Pendentes ({{ alertas.length }})</h3>
        <div class="alertas-lista">
          <div *ngFor="let alerta of alertas" class="alerta-item">
            <img *ngIf="alerta.fotoBase64" [src]="alerta.fotoBase64"
              width="80" height="60" style="border-radius:6px;" />
            <div class="alerta-info">
              <strong>⚠️ Desconhecido</strong>
              <small>{{ formatarData(alerta.criadoEm) }}</small>
            </div>
            <div class="alerta-acoes">
              <button type="button" (click)="resolverAlerta(alerta.id, 'Ignorado')"
                class="btn-ignorar">Ignorar</button>
              <button type="button" (click)="resolverAlerta(alerta.id, 'Segurança acionada')"
                class="btn-seguranca">🚨 Segurança</button>
            </div>
          </div>
        </div>
      </div>

    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 1200px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h1 { margin: 0; color: #333; }
    .btn-back { background: #6c757d; color: white; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }
    .content { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; }
    .camera-panel { background: #000; border-radius: 8px; overflow: hidden; }
    .status-bar { color: white; text-align: center; padding: 8px; font-weight: bold; font-size: 14px; }
    .camera-controls { padding: 12px; background: #1a1a1a; text-align: center; }
    .btn-iniciar { background: #4caf50; color: white; border: none; padding: 10px 24px; border-radius: 5px; cursor: pointer; font-size: 15px; }
    .btn-pausar { background: #ff9800; color: white; border: none; padding: 10px 24px; border-radius: 5px; cursor: pointer; font-size: 15px; }
    .resultado-panel { background: white; border-radius: 8px; padding: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 300px; }
    .resultado-ok { text-align: center; }
    .resultado-ok h2 { color: #2e7d32; margin: 12px 0 8px; }
    .resultado-nok { text-align: center; color: #c62828; }
    .resultado-nok h2 { color: #c62828; }
    .resultado-aguardando { text-align: center; color: #999; }
    .badge { padding: 4px 12px; border-radius: 20px; font-weight: bold; font-size: 13px; }
    .badge-ouro { background: #fff8e1; color: #f57f17; border: 1px solid #f57f17; }
    .badge-prata { background: #f5f5f5; color: #616161; border: 1px solid #616161; }
    .badge-bronze { background: #fbe9e7; color: #bf360c; border: 1px solid #bf360c; }
    .badge-none { background: #f5f5f5; color: #999; border: 1px solid #ddd; }
    .btn-ficha { margin-top: 12px; background: #1976d2; color: white; border: none; padding: 8px 16px; border-radius: 5px; cursor: pointer; }
    .alertas-panel { background: white; border-radius: 8px; padding: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
    .alertas-panel h3 { margin: 0 0 16px; color: #c62828; }
    .alertas-lista { display: flex; flex-direction: column; gap: 12px; }
    .alerta-item { display: flex; align-items: center; gap: 12px; padding: 12px; background: #fff3e0; border-radius: 8px; border: 1px solid #ff9800; }
    .alerta-info { flex: 1; display: flex; flex-direction: column; gap: 4px; }
    .alerta-acoes { display: flex; gap: 8px; }
    .btn-ignorar { background: #6c757d; color: white; border: none; padding: 6px 12px; border-radius: 5px; cursor: pointer; }
    .btn-seguranca { background: #c62828; color: white; border: none; padding: 6px 12px; border-radius: 5px; cursor: pointer; }
    @media (max-width: 768px) { .content { grid-template-columns: 1fr; } }
  `]
})
export class FaceRecognitionComponent implements OnDestroy {

  @ViewChild('videoEl') videoEl!: ElementRef<HTMLVideoElement>;

  private http = inject(HttpClient);
  private router = inject(Router);

  videoStream: MediaStream | null = null;
  monitorando = false;
  modelsCarregados = false;
  resultado: any = null;
  alertas: any[] = [];
  statusMsg = 'Câmera desligada';
  statusCor = '#666';
  loopAtivo = false;

  async iniciarMonitoramento() {
    this.statusMsg = 'Carregando modelos...';
    this.statusCor = '#ff9800';

    if (!this.modelsCarregados) {
      await faceapi.nets.tinyFaceDetector.loadFromUri('/assets/models');
      await faceapi.nets.faceLandmark68Net.loadFromUri('/assets/models');
      await faceapi.nets.faceRecognitionNet.loadFromUri('/assets/models');
      this.modelsCarregados = true;
    }

    const devices = await navigator.mediaDevices.enumerateDevices();
    const cameras = devices.filter(d => d.kind === 'videoinput');
    const redragon = cameras.find(c =>
      c.label.toLowerCase().includes('redragon') ||
      c.label.toLowerCase().includes('gw910') ||
      c.label.toLowerCase().includes('oneshot')
    );
    const deviceId = redragon ? redragon.deviceId : undefined;

    this.videoStream = await navigator.mediaDevices.getUserMedia({
      video: deviceId ? { deviceId: { exact: deviceId } } : true
    });

    const video = this.videoEl.nativeElement;
    video.srcObject = this.videoStream;

    await new Promise<void>(resolve => {
      video.onloadedmetadata = () => video.play().then(() => resolve());
    });

    await new Promise(resolve => setTimeout(resolve, 1000));

    this.monitorando = true;
    this.loopAtivo = true;
    this.statusMsg = '🟢 Monitorando...';
    this.statusCor = '#4caf50';

    this.carregarAlertas();
    this.loopReconhecimento();
  }

  pausarMonitoramento() {
    this.loopAtivo = false;
    this.monitorando = false;
    this.videoStream?.getTracks().forEach(t => t.stop());
    this.videoStream = null;
    this.statusMsg = '⏸ Pausado';
    this.statusCor = '#ff9800';
    this.resultado = null;
  }

  async loopReconhecimento() {
    while (this.loopAtivo) {
      try {
        const video = this.videoEl.nativeElement;

        // Detecção leve primeiro
        const detection = await faceapi
          .detectSingleFace(video, new faceapi.TinyFaceDetectorOptions())
          .withFaceLandmarks()
          .withFaceDescriptor();

        if (detection && detection.detection.score > 0.5) {
          this.statusMsg = '🔍 Rosto detectado — verificando...';
          this.statusCor = '#1976d2';

          // Captura foto
          const canvas = document.createElement('canvas');
          canvas.width = video.videoWidth;
          canvas.height = video.videoHeight;
          canvas.getContext('2d')!.drawImage(video, 0, 0);
          const fotoBase64 = canvas.toDataURL('image/jpeg', 0.7);

          const payload = {
            descriptor: Array.from(detection.descriptor),
            fotoBase64
          };

          // Envia para verificação
          this.http.post<any>('/api/face/verificar-entrada', payload).subscribe({
            next: (res) => {
              this.resultado = res;
              if (res.reconhecido) {
                this.statusMsg = `✅ ${res.nomeCliente}`;
                this.statusCor = '#4caf50';
              } else {
                this.statusMsg = '⚠️ Desconhecido — alerta registrado!';
                this.statusCor = '#c62828';
                this.carregarAlertas();
              }
            },
            error: () => {
              this.statusMsg = '🟢 Monitorando...';
              this.statusCor = '#4caf50';
            }
          });

          // Aguarda 5 segundos antes de verificar novamente
          await new Promise(resolve => setTimeout(resolve, 5000));
          this.resultado = null;
          this.statusMsg = '🟢 Monitorando...';
          this.statusCor = '#4caf50';

        } else {
          this.statusMsg = '🟢 Monitorando...';
          this.statusCor = '#4caf50';
        }

      } catch (e) {
        // Silencia erros do loop
      }

      // Intervalo entre detecções
      await new Promise(resolve => setTimeout(resolve, 2000));
    }
  }

  carregarAlertas() {
    this.http.get<any[]>('/api/face/alertas').subscribe({
      next: (data) => this.alertas = data,
      error: () => {}
    });
  }

  resolverAlerta(id: number, observacao: string) {
    this.http.patch(`/api/face/alertas/${id}/resolver`, {
      resolvidoPor: 'Recepcionista',
      observacao
    }, { responseType: 'text' }).subscribe({
      next: () => this.carregarAlertas(),
      error: () => {}
    });
  }

  badgeClass(classificacao: string) {
    if (classificacao === 'OURO') return 'badge badge-ouro';
    if (classificacao === 'PRATA') return 'badge badge-prata';
    if (classificacao === 'BRONZE') return 'badge badge-bronze';
    return 'badge badge-none';
  }

  formatarData(data: string) {
    if (!data) return '';
    return new Date(data).toLocaleString('pt-BR');
  }

  verFicha(clienteId: number) {
  this.pausarMonitoramento();
  this.router.navigate(['/clientes/editar', clienteId]);
}
  voltar() {
  this.pausarMonitoramento();
  this.router.navigate(['/painel-recepcao']);
}

  ngOnDestroy() {
    this.pausarMonitoramento();
  }
}