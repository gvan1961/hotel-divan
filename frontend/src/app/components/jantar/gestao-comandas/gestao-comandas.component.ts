import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { JantarService } from '../../../services/jantar.service';

@Component({
  selector: 'app-gestao-comandas',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './gestao-comandas.component.html',
  styleUrls: ['./gestao-comandas.component.css']
})
export class GestaoComandasComponent {
  dataInicio = '';
  dataFim = '';
  apartamentoFiltro = '';
  statusFiltro = 'ATIVAS'; // TODAS, ATIVAS, CANCELADAS
  
  comandas: any[] = [];
  comandasFiltradas: any[] = [];
  carregando = false;

  constructor(private jantarService: JantarService) {
    // Últimos 7 dias por padrão
    const hoje = new Date();
    const semanaAtras = new Date(hoje);
    semanaAtras.setDate(hoje.getDate() - 7);
    
    this.dataInicio = semanaAtras.toISOString().split('T')[0];
    this.dataFim = hoje.toISOString().split('T')[0];
  }

  buscarComandas() {
    if (!this.dataInicio || !this.dataFim) {
      alert('Preencha as datas!');
      return;
    }

    this.carregando = true;

    this.jantarService.gerarRelatorioComandas(this.dataInicio, this.dataFim).subscribe({
      next: (resultado) => {
        console.log('✅ Comandas carregadas:', resultado);
        this.comandas = resultado.comandas || [];
       
        // ✅ DEBUG
        this.comandas.forEach(c => console.log(`Comanda #${c.notaId} - status: ${c.status}`));

        this.aplicarFiltros();
        this.carregando = false;
      },
      error: (error) => {
        console.error('❌ Erro ao carregar comandas:', error);
        alert('Erro ao carregar comandas!');
        this.carregando = false;
      }
    });
  }

  aplicarFiltros() {
  this.comandasFiltradas = this.comandas.filter(comanda => {
    if (this.apartamentoFiltro && comanda.apartamento !== this.apartamentoFiltro) {
      return false;
    }    

    if (this.statusFiltro === 'ATIVAS' && comanda.reservaStatus !== 'ATIVA') {
      return false;
    }   

    if (this.statusFiltro === 'FINALIZADAS' && comanda.reservaStatus !== 'FINALIZADA') {
      return false;
    }

    if (this.statusFiltro === 'CANCELADAS' && comanda.status !== 'CANCELADA') {
      return false;
    }

    return true;
  });

  console.log(`📊 Filtradas: ${this.comandasFiltradas.length} de ${this.comandas.length}`);
}

  cancelarComanda(comanda: any) {
    // Verificar se já está cancelada
    if (comanda.observacao?.includes('[CANCELADA]')) {
      alert('Esta comanda já está cancelada!');
      return;
    }

    const confirmacao = confirm(
      `🚫 CANCELAR COMANDA?\n\n` +
      `Comanda #${comanda.notaId}\n` +
      `Apartamento: ${comanda.apartamento}\n` +
      `Cliente: ${comanda.cliente}\n` +
      `Total: R$ ${comanda.total.toFixed(2)}\n\n` +
      `O estoque será DEVOLVIDO.\n\n` +
      `Confirma o cancelamento?`
    );

    if (!confirmacao) {
      return;
    }

    console.log('🚫 Cancelando comanda:', comanda.notaId);

    this.jantarService.cancelarComanda(comanda.notaId).subscribe({
      next: (resultado) => {
        console.log('✅ Comanda cancelada:', resultado);
        alert('✅ Comanda cancelada com sucesso!\nEstoque devolvido.');
        
        // Atualizar a lista
        this.buscarComandas();
      },
      error: (error) => {
        console.error('❌ Erro ao cancelar comanda:', error);
        alert('❌ Erro ao cancelar comanda!\n' + (error.error?.mensagem || error.message));
      }
    });
  }

  isCancelada(comanda: any): boolean {
  return comanda.status === 'CANCELADA';
}

 getTotalAtivas(): number {
  return this.comandas.filter(c => c.reservaStatus === 'ATIVA').length;
}

getTotalCanceladas(): number {
  return this.comandas.filter(c => c.status === 'CANCELADA').length;
}

getValorTotalAtivas(): number {
  return this.comandas
    .filter(c => c.reservaStatus === 'ATIVA')
    .reduce((sum, c) => sum + c.total, 0);
}
}