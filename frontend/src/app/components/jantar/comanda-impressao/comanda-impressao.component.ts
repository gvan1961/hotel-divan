import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { JantarService } from '../../../services/jantar.service';

@Component({
  selector: 'app-comanda-impressao',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './comanda-impressao.component.html',
  styleUrls: ['./comanda-impressao.component.css']
})
export class ComandaImpressaoComponent implements OnInit {
  comanda: any = null;
  carregando = true;
  notaId: number = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private jantarService: JantarService
  ) {}

  ngOnInit() {
    this.notaId = Number(this.route.snapshot.paramMap.get('id'));
    this.carregarComanda();
  }

  carregarComanda() {
    // Buscar nos últimos 30 dias
    const hoje = new Date();
    const inicio = new Date(hoje);
    inicio.setDate(inicio.getDate() - 30);
    
    const dataInicio = inicio.toISOString().split('T')[0];
    const dataFim = hoje.toISOString().split('T')[0];
    
    this.jantarService.gerarRelatorioComandas(dataInicio, dataFim).subscribe({
      next: (resultado) => {
        this.comanda = resultado.comandas.find((c: any) => c.notaId === this.notaId);
        this.carregando = false;
        
        // Auto-imprimir após carregar
        setTimeout(() => {
          window.print();
        }, 500);
      },
      error: (error) => {
        console.error('Erro ao carregar comanda:', error);
        alert('Erro ao carregar comanda!');
        this.carregando = false;
      }
    });
  }

  imprimir() {
  window.print();
}

  voltar() {
  this.router.navigate(['/gestao-comandas']); 
}
}