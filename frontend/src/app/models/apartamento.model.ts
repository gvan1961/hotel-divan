import { StatusApartamento } from './enums';

export interface Apartamento {
  id?: number;
  numeroApartamento: string;
  tipoApartamentoId: number;
  tipoApartamentoNome?: string;
  tipoApartamentoDescricao?: string;
  tipoApartamento?: any;
  capacidade: number;
  camasDoApartamento: string;
  tv?: string;
  status?: StatusApartamento;  

  reservaAtiva?: {
    reservaId: number;
    nomeHospede: string;
    quantidadeHospede: number;
    dataCheckin: string;
    dataCheckout: string;
    status?: string;  // ← ADICIONADO
  };

  preReservaFutura?: {  // ← ADICIONE ESTE CAMPO
    reservaId: number;
    nomeHospede: string;
    quantidadeHospede: number;
    dataCheckin: string;
    dataCheckout: string;
    status?: string;
  };

}

export interface ApartamentoRequest {
  numeroApartamento: string;
  tipoApartamentoId: number;
  capacidade: number;
  camasDoApartamento: string;
  tv?: string;
}