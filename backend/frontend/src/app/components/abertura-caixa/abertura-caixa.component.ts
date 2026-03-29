import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FechamentoCaixaService } from '../../services/fechamento-caixa.service';
import { AuthService } from '../../services/auth.service';
import { CaixaStateService } from '../../services/caixa-state.service';



@Component({
  selector: 'app-abertura-caixa',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './abertura-caixa.component.html',
  styleUrls: ['./abertura-caixa.component.css']
})
export class AberturaCaixaComponent implements OnInit {
  
  turnoSelecionado: string = 'MANHA';
  valorAbertura: number = 0;
  observacoes: string = '';
  carregando: boolean = false;
  
  caixaAberto: boolean = false;  // ← IMPORTANTE
  caixaAtual: any = null;         // ← IMPORTANTE
  
  usuarioId: number = 1;
  dataHoraAtual: Date = new Date();
  
  turnos = [
    { valor: 'MANHA', label: '🌅 Manhã' },
    { valor: 'TARDE', label: '☀️ Tarde' },
    { valor: 'NOITE', label: '🌙 Noite' }
  ];
  
  constructor(
    private fechamentoCaixaService: FechamentoCaixaService,
    private authService: AuthService,
    private router: Router,
    private caixaStateService:CaixaStateService    
  ) {}
  
  ngOnInit(): void {
    // ✅ PEGAR USUÁRIO DO AUTH SERVICE
    this.usuarioId = this.authService.getUsuarioId();
    
    // Verificar se já existe caixa aberto
    this.verificarCaixaAberto();
  }
  
  /**
   * ✅ VERIFICAR SE JÁ TEM CAIXA ABERTO
   */
  verificarCaixaAberto(): void {
  this.carregando = true;
  
  console.log('═══════════════════════════════════════════');
  console.log('🔍 VERIFICANDO CAIXA ABERTO');
  console.log('   Usuário ID:', this.usuarioId);
  
  this.fechamentoCaixaService.buscarCaixaAberto(this.usuarioId).subscribe({
    next: (caixa) => {
      this.carregando = false;
      
      console.log('📦 RESPOSTA - BUSCAR CAIXA ABERTO');
      console.log('Caixa:', caixa);
      console.log('═══════════════════════════════════════════');
      
      // ✅ VERIFICAR SE TEM CAIXA
      if (caixa && caixa.id) {
        this.caixaAberto = true;
        this.caixaAtual = caixa;
        
        console.log('✅ CAIXA ABERTO ENCONTRADO!');
        console.log('   ID:', caixa.id);
        console.log('   Turno:', caixa.turno);
        console.log('   Status:', caixa.status);
      } else {
        this.caixaAberto = false;
        this.caixaAtual = null;
        
        console.log('📭 NENHUM CAIXA ABERTO');
      }
    },
    error: (error) => {
      this.carregando = false;
      
      console.error('═══════════════════════════════════════════');
      console.error('❌ ERRO AO VERIFICAR CAIXA');
      console.error('Status:', error.status);
      console.error('Error:', error);
      console.error('═══════════════════════════════════════════');
      
      this.caixaAberto = false;
      this.caixaAtual = null;
    }
  });
}
  
  /**
   * ✅ ABRIR NOVO CAIXA
   */
  abrirCaixa(): void {
  // ✅ VALIDAÇÃO: Já tem caixa aberto?
  if (this.caixaAberto) {
    alert('⚠️ Você já possui um caixa aberto!');
    return;
  }
  
  // ✅ VALIDAÇÃO: Turno selecionado?
  if (!this.turnoSelecionado) {
    alert('⚠️ Selecione um turno!');
    return;
  }
  
  this.carregando = true;
  
  console.log('📤 Abrindo caixa...');
  console.log('   Usuário ID:', this.usuarioId);
  console.log('   Turno:', this.turnoSelecionado);
  
  this.fechamentoCaixaService.abrirCaixa(
    this.usuarioId,
    this.turnoSelecionado,
    this.observacoes
  ).subscribe({
    next: (response) => {
      console.log('✅ Caixa aberto com sucesso:', response);
      
      this.carregando = false;
      
      // ✅ ATUALIZAR ESTADO LOCAL
      this.caixaAberto = true;
      this.caixaAtual = response.caixa || response;
      
      console.log('✅ Estado atualizado:', {
        caixaAberto: this.caixaAberto,
        caixaAtual: this.caixaAtual
      });
      
      // ✅ NOTIFICAR O SIDEBAR IMEDIATAMENTE
      console.log('🔔 Notificando sidebar sobre novo caixa...');
      this.caixaStateService.notificarAtualizacao();
      
      // ✅ MOSTRAR MENSAGEM DE SUCESSO
      alert('✅ Caixa aberto com sucesso!');
      
      // ✅ LIMPAR FORMULÁRIO
      this.limparFormulario();
      
      // ✅ AGUARDAR 500ms E NAVEGAR PARA O CAIXA
      setTimeout(() => {
        this.irParaCaixa();
      }, 500);
    },
    error: (error) => {
      this.carregando = false;
      console.error('❌ Erro ao abrir caixa:', error);
      
      // ✅ TRATAMENTO DE ERROS
      if (error.status === 0) {
        alert('❌ Não foi possível conectar ao servidor!');
      } else if (error.status === 400) {
        alert('❌ ' + (error.error?.erro || 'Dados inválidos!'));
      } else if (error.status === 409) {
        alert('⚠️ Você já possui um caixa aberto!');
        this.verificarCaixaAberto(); // Recarregar estado
      } else {
        alert('❌ Erro: ' + (error.error?.erro || error.message || 'Erro desconhecido'));
      }
    }
  });
}

  irParaCaixa(): void {
    if (this.caixaAtual && this.caixaAtual.id) {
      this.router.navigate(['/fechamento-caixa', this.caixaAtual.id]);
    }
  }
  
  /**
   * ✅ FECHAR CAIXA ATUAL
   */
  fecharCaixa(): void {
    if (!this.caixaAtual) {
      alert('⚠️ Nenhum caixa aberto para fechar!');
      return;
    }
    
    const confirmar = confirm(
      '🔒 FECHAR CAIXA?\n\n' +
      'Caixa #' + this.caixaAtual.id + '\n' +
      'Turno: ' + this.caixaAtual.turno + '\n' +
      'Aberto em: ' + this.formatarData(this.caixaAtual.dataHoraAbertura) + '\n\n' +
      'Deseja realmente ir para o fechamento?'
    );
    
    if (confirmar) {
      this.router.navigate(['/components/fechamento-caixa']);
    }
  }
  
  /**
   * ✅ LIMPAR FORMULÁRIO
   */
 limparFormulario(): void {
  this.turnoSelecionado = 'MANHA';
  this.valorAbertura = 0;
  this.observacoes = '';
}
  
  /**
   * 🎨 FORMATAR DATA
   */
  formatarData(data: string): string {
    if (!data) return '-';
    const d = new Date(data);
    return d.toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
 * ✅ IR PARA TELA DE FECHAMENTO
 */
irParaFechamento(): void {
  if (!this.caixaAtual || !this.caixaAtual.id) {
    alert('⚠️ Erro: ID do caixa não encontrado');
    return;
  }
  
  console.log('🔒 Indo para fechamento do caixa #' + this.caixaAtual.id);
  this.router.navigate(['/components/fechamento-caixa', this.caixaAtual.id]);
}
  
  /**
   * 🎨 FORMATAR MOEDA
   */
  formatarMoeda(valor: number): string {
    if (!valor) return 'R$ 0,00';
    return valor.toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    });
  }

  voltar(): void {
    this.router.navigate(['/dashboard']);
  }
}


  
  

