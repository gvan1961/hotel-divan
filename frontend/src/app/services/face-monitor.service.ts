import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';
import * as faceapi from 'face-api.js';

export interface FaceResultado {
  reconhecido: boolean;
  clienteId?: number;
  nomeCliente?: string;
  classificacao?: string;
  fotoBase64?: string;
  mensagem?: string;
  alertaId?: number;
  hospedadoAtualmente?: boolean;
}

@Injectable({ providedIn: 'root' })
export class FaceMonitorService implements OnDestroy {

  private videoStream: MediaStream | null = null;
  private videoEl: HTMLVideoElement | null = null;
  private modelsCarregados = false;
  private loopAtivo = false;

  // Observáveis para o painel assinar
  ativo$ = new BehaviorSubject<boolean>(false);
  resultado$ = new BehaviorSubject<FaceResultado | null>(null);
  statusMsg$ = new BehaviorSubject<string>('Desativado');

  constructor(private http: HttpClient) {}

  async ativar() {
    if (this.loopAtivo) return;

    this.statusMsg$.next('Carregando modelos...');

    if (!this.modelsCarregados) {
      await faceapi.nets.tinyFaceDetector.loadFromUri('/assets/models');
      await faceapi.nets.faceLandmark68Net.loadFromUri('/assets/models');
      await faceapi.nets.faceRecognitionNet.loadFromUri('/assets/models');
      this.modelsCarregados = true;
    }

    // Cria elemento de vídeo oculto
    this.videoEl = document.createElement('video');
    this.videoEl.muted = true;
    this.videoEl.autoplay = true;
    this.videoEl.style.display = 'none';
    document.body.appendChild(this.videoEl);

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

    this.videoEl.srcObject = this.videoStream;

    await new Promise<void>(resolve => {
      this.videoEl!.onloadedmetadata = () =>
        this.videoEl!.play().then(() => resolve());
    });

    await new Promise(resolve => setTimeout(resolve, 1000));

    this.loopAtivo = true;
    this.ativo$.next(true);
    this.statusMsg$.next('🟢 Monitorando...');

    this.loop();
  }

  pausar() {
    this.loopAtivo = false;
    this.videoStream?.getTracks().forEach(t => t.stop());
    this.videoStream = null;
    if (this.videoEl) {
      document.body.removeChild(this.videoEl);
      this.videoEl = null;
    }
    this.ativo$.next(false);
    this.statusMsg$.next('⏸ Pausado');
    this.resultado$.next(null);
  }

  limparResultado() {
    this.resultado$.next(null);
    if (this.ativo$.value) {
      this.statusMsg$.next('🟢 Monitorando...');
    }
  }

  private async loop() {
    while (this.loopAtivo && this.videoEl) {
      try {       

        const detection = await faceapi
          .detectSingleFace(
            this.videoEl,
            new faceapi.TinyFaceDetectorOptions()
          )
          .withFaceLandmarks()
          .withFaceDescriptor();
          
       if (detection && detection.detection.score > 0.5) {
  console.log('✅ Rosto detectado! Enviando ao backend...');
  this.statusMsg$.next('🔍 Rosto detectado...');

  const canvas = document.createElement('canvas');
  canvas.width = this.videoEl.videoWidth;
  canvas.height = this.videoEl.videoHeight;
  canvas.getContext('2d')!.drawImage(this.videoEl, 0, 0);
  const fotoBase64 = canvas.toDataURL('image/jpeg', 0.7);

  const payload = {
    descriptor: Array.from(detection.descriptor),
    fotoBase64
  };

  // Aguarda resposta antes de continuar
  try {
    const res = await this.http.post<FaceResultado>(
      '/api/face/verificar-entrada', payload
    ).toPromise();

    console.log('Resposta backend:', res);
    this.resultado$.next(res!);

    if (res!.reconhecido) {
      this.statusMsg$.next(`✅ ${res!.nomeCliente}`);
    } else {
      this.statusMsg$.next('⚠️ Desconhecido detectado!');
    }
  } catch (e) {
    console.error('Erro ao verificar:', e);
  }

  await new Promise(resolve => setTimeout(resolve, 6000));
  this.resultado$.next(null);
  if (this.loopAtivo) {
    this.statusMsg$.next('🟢 Monitorando...');
  }
}
      } catch (e) {}

      await new Promise(resolve => setTimeout(resolve, 2000));
    }
  }

  ngOnDestroy() {
    this.pausar();
  }
}