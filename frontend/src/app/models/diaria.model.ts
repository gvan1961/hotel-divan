export interface Diaria {
  id?: number;
  tipoApartamentoId: number;
  tipoApartamento?: string; // A, B, C, D
  descricaoTipoApartamento?: string;
  quantidade: number;
  valor: number;
  
  // ✅ NOVO — só preenchido quando quantidade = 1
  modalidade?: 'SOLTEIRO' | 'CASAL' | null;
}

export interface DiariaRequest {
  tipoApartamentoId: number;
  quantidade: number;
  valor: number;
  
  // ✅ NOVO — obrigatório quando quantidade = 1
  modalidade?: 'SOLTEIRO' | 'CASAL' | null;
}

export interface DiariaResponse {
  id: number;
  tipoApartamentoId: number;
  tipoApartamento: string;
  descricaoTipoApartamento: string;
  quantidade: number;
  valor: number;
  
  // ✅ NOVO
  modalidade?: 'SOLTEIRO' | 'CASAL' | null;
}
