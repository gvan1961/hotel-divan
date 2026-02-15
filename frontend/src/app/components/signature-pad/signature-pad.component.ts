import { Component, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-signature-pad',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="signature-wrapper">
      <div class="signature-instrucao">✍️ Desenhe sua assinatura abaixo</div>
      <canvas #canvas
              width="560"
              height="180"
              class="signature-canvas"
              (mousedown)="iniciarDesenho($event)"
              (mousemove)="desenhar($event)"
              (mouseup)="pararDesenho()"
              (mouseleave)="pararDesenho()"
              (touchstart)="iniciarDesenhoTouch($event); $event.preventDefault()"
              (touchmove)="desenharTouch($event); $event.preventDefault()"
              (touchend)="pararDesenho()">
      </canvas>
      <div class="signature-actions">
        <button class="btn-limpar" (click)="limpar()">🗑️ Limpar</button>
      </div>
    </div>
  `,
  styles: [`
    .signature-wrapper { width: 100%; }

    .signature-instrucao {
      font-size: 13px;
      color: #555;
      margin-bottom: 6px;
      text-align: center;
    }

    .signature-canvas {
      width: 100%;
      height: 180px;
      border: 2px dashed #999;
      border-radius: 8px;
      background: #fff;
      cursor: crosshair;
      touch-action: none;
    }

    .signature-canvas:active {
      border-color: #2980b9;
      border-style: solid;
    }

    .signature-actions {
      text-align: right;
      margin-top: 6px;
    }

    .btn-limpar {
      padding: 6px 14px;
      background: #e74c3c;
      color: #fff;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-size: 13px;
    }
  `]
})
export class SignaturePadComponent implements AfterViewInit {
  @ViewChild('canvas') canvasRef!: ElementRef<HTMLCanvasElement>;

  private ctx!: CanvasRenderingContext2D;
  private desenhando = false;
  temAssinatura = false;

  ngAfterViewInit() {
    this.ctx = this.canvasRef.nativeElement.getContext('2d')!;
    this.ctx.strokeStyle = '#000';
    this.ctx.lineWidth = 2.5;
    this.ctx.lineCap = 'round';
    this.ctx.lineJoin = 'round';
  }

  // ── MOUSE ──
  iniciarDesenho(e: MouseEvent) {
    this.desenhando = true;
    this.temAssinatura = true;
    const { x, y } = this.getCoordenadas(e.clientX, e.clientY);
    this.ctx.beginPath();
    this.ctx.moveTo(x, y);
  }

  desenhar(e: MouseEvent) {
    if (!this.desenhando) return;
    const { x, y } = this.getCoordenadas(e.clientX, e.clientY);
    this.ctx.lineTo(x, y);
    this.ctx.stroke();
  }

  pararDesenho() { this.desenhando = false; }

  // ── TOUCH (tablet) ──
  iniciarDesenhoTouch(e: TouchEvent) {
    const touch = e.touches[0];
    this.desenhando = true;
    this.temAssinatura = true;
    const { x, y } = this.getCoordenadas(touch.clientX, touch.clientY);
    this.ctx.beginPath();
    this.ctx.moveTo(x, y);
  }

  desenharTouch(e: TouchEvent) {
    if (!this.desenhando) return;
    const touch = e.touches[0];
    const { x, y } = this.getCoordenadas(touch.clientX, touch.clientY);
    this.ctx.lineTo(x, y);
    this.ctx.stroke();
  }

  // ── UTILITÁRIOS ──
  private getCoordenadas(clientX: number, clientY: number): { x: number, y: number } {
    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    const scaleX = this.canvasRef.nativeElement.width / rect.width;
    const scaleY = this.canvasRef.nativeElement.height / rect.height;
    return {
      x: (clientX - rect.left) * scaleX,
      y: (clientY - rect.top) * scaleY
    };
  }

  limpar() {
    this.ctx.clearRect(0, 0, this.canvasRef.nativeElement.width, this.canvasRef.nativeElement.height);
    this.temAssinatura = false;
  }

  obterAssinatura(): string | null {
    if (!this.temAssinatura) return null;
    return this.canvasRef.nativeElement.toDataURL('image/png');
  }
}