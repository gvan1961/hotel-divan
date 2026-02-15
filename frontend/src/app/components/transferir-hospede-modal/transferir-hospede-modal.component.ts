import { Component, inject, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReservaService } from '../../services/reserva.service';
import { ApartamentoService } from '../../services/apartamento.service';

interface Hospede {
  id: number;
  cliente: {
    id: number;
    nome: string;
  };
  status: string;
  titular: boolean;
  reserva: {
    id: number;
    apartamento: {
      id: number;
      numeroApartamento: string;
    };
  };
}

interface ApartamentoDisponivel {
  id: number;
  numeroApartamento: string;
  tipoApartamentoNome: string;
  capacidade: number;
  status: string;
  observacao?: string; // ✅ NOVO: "Disponível - 4 vagas" ou "Ocupado - 2/4 hóspedes - 2 vagas"
}

@Component({
  selector: 'app-transferir-hospede-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transferir-hospede-modal.component.html',
  styleUrl: './transferir-hospede-modal.component.css'
})
export class TransferirHospedeModalComponent {
  private reservaService = inject(ReservaService);
  private apartamentoService = inject(ApartamentoService);

  // ✅ EVENTO PARA NOTIFICAR PAI
  @Output() transferenciaRealizada = new EventEmitter<void>();

  isVisible = false;
  hospedeSelecionado: Hospede | null = null;
  apartamentosDisponiveis: ApartamentoDisponivel[] = [];
  
  // Dados do formulário
  apartamentoDestinoId: number | null = null;
  motivoTransferencia = '';
  
  carregando = false;
  erro = '';

  abrir(hospede: Hospede): void {
  console.log('🔄 Abrindo modal de transferência para:', hospede);
  
  this.hospedeSelecionado = hospede;
  this.isVisible = true;
  this.erro = '';
  this.apartamentoDestinoId = null;  // ✅ CORRETO
  this.motivoTransferencia = '';
  
  this.carregarApartamentosDisponiveis();
}

fechar(): void {
  this.isVisible = false;
  this.hospedeSelecionado = null;
  this.apartamentosDisponiveis = [];
  this.apartamentoDestinoId = null;  // ✅ CORRETO
  this.motivoTransferencia = '';
  this.erro = '';
}

  carregarApartamentosDisponiveis(): void {
    if (!this.hospedeSelecionado) {
      console.warn('⚠️ Hóspede não selecionado');
      return;
    }

    this.carregando = true;
    this.erro = '';

    const apartamentoOrigemId = this.hospedeSelecionado.reserva.apartamento.id;

    // ✅ CHAMAR ENDPOINT ESPECÍFICO PARA TRANSFERÊNCIA
    this.reservaService.buscarApartamentosDisponiveisParaTransferencia(apartamentoOrigemId).subscribe({
      next: (apartamentos: ApartamentoDisponivel[]) => {
        this.apartamentosDisponiveis = apartamentos;
        
        console.log('✅ Apartamentos disponíveis:', this.apartamentosDisponiveis.length);
        
        if (this.apartamentosDisponiveis.length === 0) {
          this.erro = 'Nenhum apartamento disponível para transferência';
        }
        
        this.carregando = false;
      },
      error: (err: any) => {
        console.error('❌ Erro ao carregar apartamentos:', err);
        this.erro = 'Erro ao carregar apartamentos disponíveis';
        this.carregando = false;
      }
    });
  }

  confirmarTransferencia(): void {
    // Validações
    if (!this.apartamentoDestinoId) {
      this.erro = 'Selecione o apartamento de destino';
      return;
    }

    // ✅ ADICIONE ESTES LOGS AQUI:
  console.log('═══════════════════════════════════════');
  console.log('🔍 DEBUG - DADOS ANTES DE ENVIAR:');
  console.log('   Hóspede ID:', this.hospedeSelecionado?.id);
  console.log('   Apartamento Destino ID:', this.apartamentoDestinoId);
  console.log('   Tipo do ID:', typeof this.apartamentoDestinoId);
  console.log('   Motivo:', this.motivoTransferencia);
  console.log('═══════════════════════════════════════');

    if (!this.motivoTransferencia || this.motivoTransferencia.trim() === '') {
      this.erro = 'Informe o motivo da transferência';
      return;
    }

    if (!this.hospedeSelecionado) {
      this.erro = 'Hóspede não selecionado';
      return;
    }

    const apartamentoDestino = this.apartamentosDisponiveis.find(
      a => a.id === this.apartamentoDestinoId
    );

    if (!apartamentoDestino) {
      this.erro = 'Apartamento de destino não encontrado';
      return;
    }

    // ✅ MENSAGEM DE CONFIRMAÇÃO DINÂMICA
    let mensagemConfirmacao = 
      `🔄 CONFIRMAR TRANSFERÊNCIA?\n\n` +
      `Hóspede: ${this.hospedeSelecionado.cliente.nome}\n` +
      `De: Apt ${this.hospedeSelecionado.reserva.apartamento.numeroApartamento}\n` +
      `Para: Apt ${apartamentoDestino.numeroApartamento}\n`;

    if (apartamentoDestino.observacao && apartamentoDestino.observacao.includes('Ocupado')) {
      mensagemConfirmacao += `\n⚠️ ATENÇÃO: ${apartamentoDestino.observacao}\n`;
      mensagemConfirmacao += `O hóspede será ADICIONADO à reserva existente.\n`;
    } else {
      mensagemConfirmacao += `\n✅ Apartamento vazio - nova reserva será criada.\n`;
    }

    mensagemConfirmacao += `\nMotivo: ${this.motivoTransferencia}`;

    const confirmacao = confirm(mensagemConfirmacao);

    if (!confirmacao) return;

    // ✅ MONTAR DTO SIMPLIFICADO
    const dto = {
      hospedagemHospedeId: this.hospedeSelecionado.id,
      novoApartamentoId: this.apartamentoDestinoId,
      motivo: this.motivoTransferencia
    };

    console.log('📤 Enviando transferência:', dto);
    this.carregando = true;
    this.erro = '';

    this.reservaService.transferirHospede(dto).subscribe({
      next: (response: any) => {
        console.log('✅ Transferência realizada:', response);
        
        let mensagemSucesso = '✅ Hóspede transferido com sucesso!\n\n';
        
        if (response.apartamentoOrigemFicouVazio) {
          mensagemSucesso += `🧹 Apartamento ${this.hospedeSelecionado!.reserva.apartamento.numeroApartamento} foi enviado para LIMPEZA\n`;
        }
        
        if (response.mensagem) {
          mensagemSucesso += `\n${response.mensagem}`;
        }
        
        alert(mensagemSucesso);
        
        this.fechar();
        
        // ✅ EMITIR EVENTO PARA PAI RECARREGAR
        this.transferenciaRealizada.emit();
      },
      error: (err: any) => {
        console.error('❌ Erro na transferência:', err);
        
        let mensagemErro = 'Erro ao transferir hóspede';
        
        if (err.error) {
          if (typeof err.error === 'string') {
            mensagemErro = err.error;
          } else if (err.error.message) {
            mensagemErro = err.error.message;
          } else if (err.error.erro) {
            mensagemErro = err.error.erro;
          }
        }
        
        this.erro = mensagemErro;
        this.carregando = false;
      }
    });
  }

  obterInfoApartamento(apt: ApartamentoDisponivel): string {
    return `${apt.numeroApartamento} - ${apt.tipoApartamentoNome} (${apt.observacao || 'Disponível'})`;
  }
}