import { Component, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-signature-pad',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="signature-wrapper">
      <div class="signature-instrucao">✏️ Assine acima da linha</div>
      <canvas #canvas
              width="760"
              height="360"
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
      max-width: 760px;
      height: 300px;
      border: 2px dashed #999;
      border-radius: 8px;
      background: #fff;
      cursor: crosshair;
      touch-action: none;
      display: block;
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

  private get linhaBaseY(): number {
    return Math.floor(this.canvasRef.nativeElement.height * 0.80);
  }

  ngAfterViewInit() {
    this.ctx = this.canvasRef.nativeElement.getContext('2d')!;
    this.ctx.strokeStyle = '#000';
    this.ctx.lineWidth = 3;
    this.ctx.lineCap = 'round';
    this.ctx.lineJoin = 'round';
    this.desenharGuia();
  }

  private desenharGuia() {
    const canvas = this.canvasRef.nativeElement;
    const ctx = this.ctx;
    const y = this.linhaBaseY;
    const margemEsq = 40;
    const margemDir = canvas.width - 40;

    ctx.save();

    // Linha base tracejada
    ctx.strokeStyle = '#aaa';
    ctx.lineWidth = 1.5;
    ctx.setLineDash([8, 4]);
    ctx.beginPath();
    ctx.moveTo(margemEsq, y);
    ctx.lineTo(margemDir, y);
    ctx.stroke();
    ctx.setLineDash([]);

    // X de início
    const xPos = margemEsq;
    const tamanho = 14;
    ctx.strokeStyle = '#bbb';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(xPos - tamanho / 2, y - tamanho / 2);
    ctx.lineTo(xPos + tamanho / 2, y + tamanho / 2);
    ctx.moveTo(xPos + tamanho / 2, y - tamanho / 2);
    ctx.lineTo(xPos - tamanho / 2, y + tamanho / 2);
    ctx.stroke();

    // Texto guia
    ctx.fillStyle = '#ccc';
    ctx.font = '22px sans-serif';
    ctx.fillText('Assine aqui', margemEsq + 20, y - 10);

    ctx.restore();
  }

  // ── MOUSE ──
  iniciarDesenho(e: MouseEvent) {
    this.desenhando = true;
    this.temAssinatura = true;
    const { x, y } = this.getCoordenadas(e.clientX, e.clientY);
    this.ctx.strokeStyle = '#000';
    this.ctx.lineWidth = 3;
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

  // ── TOUCH (tablet / mesa digitalizadora) ──
  iniciarDesenhoTouch(e: TouchEvent) {
    const touch = e.touches[0];
    this.desenhando = true;
    this.temAssinatura = true;
    const { x, y } = this.getCoordenadas(touch.clientX, touch.clientY);
    this.ctx.strokeStyle = '#000';
    this.ctx.lineWidth = 3;
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
    const canvas = this.canvasRef.nativeElement;
    this.ctx.clearRect(0, 0, canvas.width, canvas.height);
    this.temAssinatura = false;
    this.desenharGuia();
  }

  obterAssinatura(): string | null {
    if (!this.temAssinatura) return null;
    return this.canvasRef.nativeElement.toDataURL('image/png');
  }
}