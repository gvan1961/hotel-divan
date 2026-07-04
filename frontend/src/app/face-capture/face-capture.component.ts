import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import * as faceapi from 'face-api.js';

import { Component, OnDestroy, ViewChild, ElementRef, inject, Output, EventEmitter } from '@angular/core';



@Component({
  selector: 'app-face-capture',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="text-align:center; padding: 16px;">

      <div *ngIf="isCapturing">
        <video #videoEl width="320" height="240" autoplay muted
          style="border-radius: 8px; border: 2px solid #1976d2;">
        </video>
      </div>

      <div *ngIf="fotoCapturada && !isCapturing">
        <img [src]="fotoCapturada" width="320" height="240"
          style="border-radius: 8px; border: 2px solid green;" />
      </div>

      <!-- BOTÃO TEMPORÁRIO DE TESTE SEM CÂMERA -->
<button type="button" *ngIf="!isCapturing"
  (click)="simularFoto()"
  style="margin: 4px; padding: 8px 16px; background: orange; color: white; border: none; border-radius: 5px; cursor: pointer;">
  🧪 Simular Foto (Teste)
</button>

      <p style="margin: 12px 0; font-weight: bold;">{{ mensagem }}</p>

     <button type="button" *ngIf="isCapturing" (click)="capturar()"
  style="margin: 4px; padding: 8px 16px; background: green; color: white; border: none; border-radius: 5px; cursor: pointer;">
  ✅ Capturar Rosto
</button>

<button type="button" *ngIf="isCapturing" (click)="encerrar()"
  style="margin: 4px; padding: 8px 16px; background: red; color: white; border: none; border-radius: 5px; cursor: pointer;">
  ❌ Cancelar
</button>

    </div>
  `
})
export class FaceCaptureComponent implements OnDestroy {

  @ViewChild('videoEl') videoEl!: ElementRef<HTMLVideoElement>;

  private http = inject(HttpClient);

  videoStream: MediaStream | null = null;
  isCapturing = false;
  mensagem = '';
  fotoCapturada = '';
  modelsCarregados = false;
  clienteId: number = 0;

  @Output() fotoCadastrada = new EventEmitter<void>();

 async iniciar(clienteId: number) {
  this.clienteId = clienteId;
  this.fotoCapturada = '';
  this.isCapturing = true;
  this.mensagem = 'Carregando modelos...';

  try {
    await new Promise(resolve => setTimeout(resolve, 100));

    if (!this.modelsCarregados) {
      await faceapi.nets.tinyFaceDetector.loadFromUri('/assets/models');
      await faceapi.nets.faceLandmark68Net.loadFromUri('/assets/models');
      await faceapi.nets.faceRecognitionNet.loadFromUri('/assets/models');
      this.modelsCarregados = true;
    }

    const devices = await navigator.mediaDevices.enumerateDevices();
    const cameras = devices.filter(d => d.kind === 'videoinput');

    console.log('Câmeras disponíveis:', cameras.map(c => c.label));

    const redragon = cameras.find(c =>
      c.label.toLowerCase().includes('redragon') ||
      c.label.toLowerCase().includes('gw910') ||
      c.label.toLowerCase().includes('oneshot')
    );

    const deviceId = redragon ? redragon.deviceId : undefined;

    this.videoStream = await navigator.mediaDevices.getUserMedia({
      video: deviceId ? { deviceId: { exact: deviceId } } : true
    });

    this.videoEl.nativeElement.srcObject = this.videoStream;
    await this.videoEl.nativeElement.play();

    this.mensagem = redragon
      ? '📷 Redragon GW910 ativa — posicione o rosto'
      : '📷 Câmera padrão ativa — posicione o rosto';

  } catch (e: any) {
  console.error('❌ Erro ao iniciar câmera:', e);
  this.mensagem = '❌ Câmera não encontrada. Clique em Simular para testar.';
  this.isCapturing = false; // ← isso vai mostrar o botão de simular
}
}

  async capturar() {
  const video = this.videoEl.nativeElement;
  this.mensagem = 'Detectando rosto...';

  // Garante frame real antes de detectar
  const canvas = document.createElement('canvas');
  canvas.width = video.videoWidth || 640;
  canvas.height = video.videoHeight || 480;
  const ctx = canvas.getContext('2d')!;

  let tamanho = 0;
  for (let i = 0; i < 10; i++) {
    ctx.drawImage(video, 0, 0);
    tamanho = canvas.toDataURL('image/jpeg', 0.8).length;
    if (tamanho > 10000) break;
    await new Promise(resolve => setTimeout(resolve, 200));
  }

  if (tamanho < 10000) {
    this.mensagem = '⚠️ Câmera sem imagem. Aguarde e tente novamente.';
    return;
  }

  // Detecta diretamente no elemento video (não no canvas)
  const detection = await faceapi
    .detectSingleFace(video, new faceapi.TinyFaceDetectorOptions())
    .withFaceLandmarks()
    .withFaceDescriptor();

    console.log('Detection completo:', detection);
    console.log('Score:', detection?.detection?.score);

  console.log('Detection:', detection ? 'encontrado' : 'não encontrado');

  if (!detection) {
    this.mensagem = '⚠️ Nenhum rosto detectado. Tente novamente.';
    return;
  }

  // Captura foto do frame atual
  ctx.drawImage(video, 0, 0);
  this.fotoCapturada = canvas.toDataURL('image/jpeg', 0.8);

  const payload = {
    clienteId: this.clienteId,
    descriptor: Array.from(detection.descriptor),
    fotoBase64: this.fotoCapturada
  };

  this.http.post('/api/face/cadastrar', payload, { responseType: 'text' }).subscribe({
  next: () => {
    this.mensagem = '✅ Rosto cadastrado com sucesso!';
    this.encerrar();
    this.fotoCadastrada.emit(); // ← avisa o pai
  },
  error: (err) => {
    console.error('Erro ao cadastrar:', err.status, err.error);
    this.mensagem = '❌ Erro ao cadastrar. Tente novamente.';
  }
});
}

  encerrar() {
    console.log('🔴 FaceCapture encerrar() chamado');
    this.videoStream?.getTracks().forEach(t => t.stop());
    this.videoStream = null;
    this.isCapturing = false;
  }

  // Botão temporário para teste
simularFoto(): void {
  this.mensagem = '✅ Foto simulada!';
  this.fotoCadastrada.emit();
}

  ngOnDestroy() {
    this.encerrar();
  }
}