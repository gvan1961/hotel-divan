import { Component, OnInit, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ValeService } from '../../services/vale.service';
import { Vale, TIPO_VALE_LABELS } from '../../models/vale.model';

@Component({
  selector: 'app-vale-assinatura',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <!-- CABEÇALHO -->
      <div class="header no-print">
        <h1>✍️ Assinatura Digital do Vale</h1>
        <button class="btn-voltar" (click)="voltar()">← Voltar</button>
      </div>

      <!-- LOADING -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Carregando vale...</p>
      </div>

      <!-- CONTEÚDO -->
      <div *ngIf="!loading && vale" class="conteudo">
        
        <!-- INFORMAÇÕES DO VALE -->
        <div class="info-vale">
          <h2>📋 Vale #{{ vale.id }}</h2>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">Funcionário:</span>
              <span class="valor">{{ vale.clienteNome }}</span>
            </div>
            <div class="info-item">
              <span class="label">CPF:</span>
              <span class="valor">{{ vale.clienteCpf }}</span>
            </div>
            <div class="info-item">
              <span class="label">Tipo:</span>
              <span class="valor">{{ obterLabelTipo(vale.tipoVale) }}</span>
            </div>
            <div class="info-item">
              <span class="label">Valor:</span>
              <span class="valor destaque">R$ {{ vale.valor | number:'1.2-2' }}</span>
            </div>
          </div>
        </div>

        <!-- ÁREA DE ASSINATURA -->
        <div class="area-assinatura">
          <h3>✍️ Assine abaixo:</h3>
          <p class="instrucao">Use o dedo ou caneta stylus para assinar no quadro abaixo</p>
          
          <!-- CANVAS -->
          <div class="canvas-container" [class.assinado]="assinado">
            <canvas 
              #canvasAssinatura
              (touchstart)="iniciarDesenho($event)"
              (touchmove)="desenhar($event)"
              (touchend)="finalizarDesenho()"
              (mousedown)="iniciarDesenhoMouse($event)"
              (mousemove)="desenharMouse($event)"
              (mouseup)="finalizarDesenho()"
              (mouseleave)="finalizarDesenho()">
            </canvas>
            
            <div class="linha-guia"></div>
          </div>

          <!-- BOTÕES DE AÇÃO -->
          <div class="acoes">
            <button class="btn btn-limpar" (click)="limparAssinatura()">
              🗑️ Limpar
            </button>
            <button class="btn btn-confirmar" (click)="confirmarAssinatura()" [disabled]="!assinado">
              ✅ Confirmar Assinatura
            </button>
          </div>

          <!-- MENSAGEM DE SUCESSO -->
          <div class="sucesso" *ngIf="assinaturaConfirmada">
            <div class="icone-sucesso">✅</div>
            <p>Assinatura registrada com sucesso!</p>
          </div>
        </div>

        <!-- TERMO DE CONCORDÂNCIA -->
        <div class="termo">
          <h4>📜 Termo de Concordância</h4>
          <p>
            Declaro que recebi o valor de <strong>R$ {{ vale.valor | number:'1.2-2' }}</strong> 
            referente ao {{ obterLabelTipo(vale.tipoVale) }} e me comprometo a quitar este vale 
            conforme as condições estabelecidas pela empresa.
          </p>
          <p class="data-termo">
            {{ obterDataAtual() }}
          </p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }

    .container {
      padding: 20px;
      max-width: 900px;
      margin: 0 auto;
      background: #f5f5f5;
      min-height: 100vh;
    }

    /* CABEÇALHO */
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
      background: white;
      padding: 20px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .header h1 {
      margin: 0;
      color: #2c3e50;
    }

    .btn-voltar {
      padding: 10px 20px;
      background: #95a5a6;
      color: white;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn-voltar:hover {
      background: #7f8c8d;
    }

    /* LOADING */
    .loading {
      text-align: center;
      padding: 60px;
      background: white;
      border-radius: 12px;
    }

    .spinner {
      border: 4px solid #f3f3f3;
      border-top: 4px solid #667eea;
      border-radius: 50%;
      width: 50px;
      height: 50px;
      animation: spin 1s linear infinite;
      margin: 0 auto 20px;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    /* INFORMAÇÕES DO VALE */
    .info-vale {
      background: white;
      padding: 25px;
      border-radius: 12px;
      margin-bottom: 25px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .info-vale h2 {
      margin-bottom: 20px;
      color: #2c3e50;
      font-size: 1.5em;
    }

    .info-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 15px;
    }

    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 12px;
      background: #f8f9fa;
      border-radius: 6px;
      border-left: 4px solid #3498db;
    }

    .info-item .label {
      font-weight: 600;
      color: #7f8c8d;
    }

    .info-item .valor {
      color: #2c3e50;
      font-weight: 500;
    }

    .info-item .valor.destaque {
      color: #27ae60;
      font-size: 1.2em;
      font-weight: 700;
    }

    /* ÁREA DE ASSINATURA */
    .area-assinatura {
      background: white;
      padding: 30px;
      border-radius: 12px;
      margin-bottom: 25px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .area-assinatura h3 {
      color: #2c3e50;
      margin-bottom: 10px;
      font-size: 1.3em;
    }

    .instrucao {
      color: #7f8c8d;
      margin-bottom: 20px;
      font-size: 0.95em;
    }

    /* CANVAS */
    .canvas-container {
      position: relative;
      background: white;
      border: 3px dashed #3498db;
      border-radius: 8px;
      margin-bottom: 20px;
      overflow: hidden;
      cursor: crosshair;
      transition: all 0.3s;
    }

    .canvas-container:hover {
      border-color: #2980b9;
      box-shadow: 0 0 20px rgba(52, 152, 219, 0.3);
    }

    .canvas-container.assinado {
      border-color: #27ae60;
      border-style: solid;
    }

    canvas {
      display: block;
      width: 100%;
      height: 300px;
      touch-action: none;
    }

    .linha-guia {
      position: absolute;
      bottom: 80px;
      left: 40px;
      right: 40px;
      height: 2px;
      background: repeating-linear-gradient(
        90deg,
        #e0e0e0,
        #e0e0e0 10px,
        transparent 10px,
        transparent 20px
      );
      pointer-events: none;
    }

    /* BOTÕES DE AÇÃO */
    .acoes {
      display: flex;
      gap: 15px;
      justify-content: center;
    }

    .btn {
      padding: 15px 30px;
      border: none;
      border-radius: 8px;
      font-size: 1em;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .btn-limpar {
      background: #e74c3c;
      color: white;
    }

    .btn-limpar:hover {
      background: #c0392b;
      transform: translateY(-2px);
    }

    .btn-confirmar {
      background: #27ae60;
      color: white;
      font-size: 1.1em;
      padding: 15px 40px;
    }

    .btn-confirmar:hover:not(:disabled) {
      background: #229954;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(39, 174, 96, 0.4);
    }

    .btn-confirmar:disabled {
      background: #95a5a6;
      cursor: not-allowed;
      opacity: 0.6;
    }

    /* MENSAGEM DE SUCESSO */
    .sucesso {
      margin-top: 25px;
      padding: 20px;
      background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%);
      border: 2px solid #28a745;
      border-radius: 8px;
      text-align: center;
      animation: slideDown 0.5s ease;
    }

    @keyframes slideDown {
      from {
        transform: translateY(-20px);
        opacity: 0;
      }
      to {
        transform: translateY(0);
        opacity: 1;
      }
    }

    .icone-sucesso {
      font-size: 3em;
      margin-bottom: 10px;
      animation: bounce 1s ease infinite;
    }

    @keyframes bounce {
      0%, 100% { transform: translateY(0); }
      50% { transform: translateY(-10px); }
    }

    .sucesso p {
      font-size: 1.1em;
      font-weight: 600;
      color: #155724;
      margin: 0;
    }

    /* TERMO */
    .termo {
      background: #fff9e6;
      padding: 25px;
      border-radius: 12px;
      border: 2px solid #f39c12;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .termo h4 {
      color: #2c3e50;
      margin-bottom: 15px;
      font-size: 1.2em;
    }

    .termo p {
      line-height: 1.6;
      color: #2c3e50;
      margin-bottom: 10px;
      text-align: justify;
    }

    .termo strong {
      color: #27ae60;
    }

    .data-termo {
      text-align: right;
      font-style: italic;
      color: #7f8c8d;
      margin-top: 15px;
    }

    /* RESPONSIVE */
    @media (max-width: 768px) {
      .info-grid {
        grid-template-columns: 1fr;
      }

      .acoes {
        flex-direction: column;
      }

      .btn {
        width: 100%;
        justify-content: center;
      }

      canvas {
        height: 250px;
      }
    }

    /* TABLET LANDSCAPE */
    @media (min-width: 768px) and (max-width: 1024px) and (orientation: landscape) {
      canvas {
        height: 350px;
      }
    }
  `]
})
export class ValeAssinaturaComponent implements OnInit {
  @ViewChild('canvasAssinatura', { static: false }) canvasRef!: ElementRef<HTMLCanvasElement>;

  private valeService = inject(ValeService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  vale: Vale | null = null;
  loading = false;
  assinado = false;
  assinaturaConfirmada = false;

  private ctx!: CanvasRenderingContext2D;
  private desenhando = false;
  private ultimaX = 0;
  private ultimaY = 0;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.carregarVale(+id);
    }
  }

  ngAfterViewInit(): void {
    this.inicializarCanvas();
  }

  carregarVale(id: number): void {
    this.loading = true;
    this.valeService.buscarPorId(id).subscribe({
      next: (data) => {
        this.vale = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar vale:', err);
        alert('Erro ao carregar vale');
        this.voltar();
      }
    });
  }

  inicializarCanvas(): void {
    const canvas = this.canvasRef.nativeElement;
    const container = canvas.parentElement!;
    
    // Ajustar tamanho do canvas
    canvas.width = container.clientWidth;
    canvas.height = 300;

    this.ctx = canvas.getContext('2d')!;
    
    // Configurar estilo do desenho
    this.ctx.lineWidth = 3;
    this.ctx.lineCap = 'round';
    this.ctx.lineJoin = 'round';
    this.ctx.strokeStyle = '#2c3e50';
  }

  // ===== TOUCH (TABLET/MOBILE) =====
  
  iniciarDesenho(event: TouchEvent): void {
    event.preventDefault();
    this.desenhando = true;
    
    const touch = event.touches[0];
    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    
    this.ultimaX = touch.clientX - rect.left;
    this.ultimaY = touch.clientY - rect.top;
    
    this.assinado = true;
  }

  desenhar(event: TouchEvent): void {
    if (!this.desenhando) return;
    
    event.preventDefault();
    
    const touch = event.touches[0];
    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    
    const x = touch.clientX - rect.left;
    const y = touch.clientY - rect.top;
    
    this.ctx.beginPath();
    this.ctx.moveTo(this.ultimaX, this.ultimaY);
    this.ctx.lineTo(x, y);
    this.ctx.stroke();
    
    this.ultimaX = x;
    this.ultimaY = y;
  }

  // ===== MOUSE (DESKTOP) =====
  
  iniciarDesenhoMouse(event: MouseEvent): void {
    this.desenhando = true;
    
    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    
    this.ultimaX = event.clientX - rect.left;
    this.ultimaY = event.clientY - rect.top;
    
    this.assinado = true;
  }

  desenharMouse(event: MouseEvent): void {
    if (!this.desenhando) return;
    
    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    
    this.ctx.beginPath();
    this.ctx.moveTo(this.ultimaX, this.ultimaY);
    this.ctx.lineTo(x, y);
    this.ctx.stroke();
    
    this.ultimaX = x;
    this.ultimaY = y;
  }

  finalizarDesenho(): void {
    this.desenhando = false;
  }

  // ===== AÇÕES =====

  limparAssinatura(): void {
    const canvas = this.canvasRef.nativeElement;
    this.ctx.clearRect(0, 0, canvas.width, canvas.height);
    this.assinado = false;
    this.assinaturaConfirmada = false;
  }

  confirmarAssinatura(): void {
    if (!this.assinado) {
      alert('⚠️ Por favor, assine antes de confirmar');
      return;
    }

    const confirmacao = confirm(
      '✅ Confirmar assinatura?\n\n' +
      'A assinatura será registrada permanentemente no sistema.'
    );

    if (!confirmacao) return;

    // Capturar assinatura em base64
    const canvas = this.canvasRef.nativeElement;
    const assinaturaBase64 = canvas.toDataURL('image/png');

    // Enviar para o backend
    this.valeService.assinarVale(this.vale!.id!, assinaturaBase64).subscribe({
      next: () => {
        this.assinaturaConfirmada = true;
        
        setTimeout(() => {
          const imprimir = confirm(
            '✅ Assinatura registrada com sucesso!\n\n' +
            'Deseja imprimir o vale agora?'
          );
          
          if (imprimir) {
            this.router.navigate(['/vales/imprimir', this.vale!.id]);
          } else {
            this.router.navigate(['/vales']);
          }
        }, 2000);
      },
      error: (err) => {
        console.error('Erro ao registrar assinatura:', err);
        alert('❌ Erro ao registrar assinatura');
      }
    });
  }

  obterLabelTipo(tipo: string): string {
    return TIPO_VALE_LABELS[tipo as keyof typeof TIPO_VALE_LABELS] || tipo;
  }

  obterDataAtual(): string {
    return new Date().toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  voltar(): void {
    this.router.navigate(['/vales']);
  }
}