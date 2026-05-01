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
  
  // ✅ NOVO — sinaliza que apartamento tem cama de casal
  temCamaDeCasal?: boolean;

  reservaAtiva?: {
    reservaId: number;
    nomeHospede: string;
    quantidadeHospede: number;
    dataCheckin: string;
    dataCheckout: string;
    status?: string;
  };

  preReservaFutura?: {
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
  
  // ✅ NOVO — para o request também
  temCamaDeCasal?: boolean;
}
