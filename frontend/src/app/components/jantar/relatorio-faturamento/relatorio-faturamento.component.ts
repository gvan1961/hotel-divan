import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { JantarService } from '../../../services/jantar.service';

@Component({
  selector: 'app-relatorio-faturamento',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './relatorio-faturamento.component.html',
  styleUrls: ['./relatorio-faturamento.component.css']
})
export class RelatorioFaturamentoComponent {
  dataInicio = '';
  dataFim = '';
  relatorio: any = null;
  carregando = false;

  constructor(private jantarService: JantarService) {
    // Definir data de hoje como padrão
    const hoje = new Date().toISOString().split('T')[0];
    this.dataInicio = hoje;
    this.dataFim = hoje;
  }

  gerarRelatorio() {
    if (!this.dataInicio || !this.dataFim) {
      alert('Preencha as datas!');
      return;
    }

    this.carregando = true;

    this.jantarService.gerarRelatorioFaturamento(this.dataInicio, this.dataFim).subscribe({
      next: (resultado) => {
        console.log('✅ Relatório gerado:', resultado);
        this.relatorio = resultado;
        this.carregando = false;
      },
      error: (error) => {
        console.error('❌ Erro ao gerar relatório:', error);
        alert('Erro ao gerar relatório!');
        this.carregando = false;
      }
    });
  }

  imprimir() {
    window.print();
  }
}