import { Component, OnDestroy, ViewChild, ElementRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-webcam-capture',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="text-align:center; padding: 16px;">
      <div *ngIf="capturando">
        <video #videoEl width="320" height="240" autoplay muted
          style="border-radius: 8px; border: 2px solid #1976d2;">
        </video>
      </div>

      <div *ngIf="fotoCapturada && !capturando">
        <img [src]="fotoCapturada" width="320" height="240"
          style="border-radius: 8px; border: 2px solid green;" />
      </div>

      <p style="margin: 12px 0; font-weight: bold;">{{ mensagem }}</p>

      <button type="button" *ngIf="capturando" (click)="tirarFoto()"
        style="margin: 4px; padding: 8px 16px; background: green; color: white; border: none; border-radius: 5px; cursor: pointer;">
        ✅ Capturar
      </button>

      <button type="button" *ngIf="capturando" (click)="cancelar()"
        style="margin: 4px; padding: 8px 16px; background: red; color: white; border: none; border-radius: 5px; cursor: pointer;">
        ❌ Cancelar
      </button>
    </div>
  `
})
export class WebcamCaptureComponent implements OnDestroy {
  @ViewChild('videoEl') videoEl!: ElementRef<HTMLVideoElement>;
  @Output() fotoCapturadaEvent = new EventEmitter<string>();
  @Output() cancelado = new EventEmitter<void>();

  videoStream: MediaStream | null = null;
  capturando = false;
  mensagem = '';
  fotoCapturada = '';

  async iniciar(): Promise<void> {
    this.fotoCapturada = '';
    this.capturando = true;
    this.mensagem = 'Iniciando câmera...';

    try {
      await new Promise(resolve => setTimeout(resolve, 100));

      const devices = await navigator.mediaDevices.enumerateDevices();
      const cameras = devices.filter(d => d.kind === 'videoinput');

      const preferida = cameras.find(c =>
        c.label.toLowerCase().includes('redragon') ||
        c.label.toLowerCase().includes('gw910') ||
        c.label.toLowerCase().includes('oneshot')
      );

      const deviceId = preferida ? preferida.deviceId : undefined;

      this.videoStream = await navigator.mediaDevices.getUserMedia({
        video: deviceId ? { deviceId: { exact: deviceId } } : true
      });

      this.videoEl.nativeElement.srcObject = this.videoStream;
      await this.videoEl.nativeElement.play();

      this.mensagem = preferida ? '📷 Câmera ativa — posicione-se' : '📷 Câmera padrão ativa';
    } catch (e) {
      console.error('❌ Erro ao iniciar câmera:', e);
      this.mensagem = '❌ Câmera não encontrada.';
      this.capturando = false;
    }
  }

  tirarFoto(): void {
    const video = this.videoEl.nativeElement;
    const canvas = document.createElement('canvas');
    canvas.width = video.videoWidth || 640;
    canvas.height = video.videoHeight || 480;
    const ctx = canvas.getContext('2d')!;
    ctx.drawImage(video, 0, 0);

    this.fotoCapturada = canvas.toDataURL('image/jpeg', 0.85);
    this.encerrarCamera();
    this.fotoCapturadaEvent.emit(this.fotoCapturada);
  }

  cancelar(): void {
    this.encerrarCamera();
    this.cancelado.emit();
  }

  private encerrarCamera(): void {
    this.videoStream?.getTracks().forEach(t => t.stop());
    this.videoStream = null;
    this.capturando = false;
  }

  ngOnDestroy(): void {
    this.encerrarCamera();
  }
}