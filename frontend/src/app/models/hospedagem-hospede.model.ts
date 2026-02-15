export interface HospedagemHospede {
  id: number;
  reservaId: number;
  clienteId: number;
  clienteNome: string;
  clienteCpf: string;
  clienteTelefone: string;
  titular: boolean;
  dataEntrada: string;
  dataSaida: string | null;
  status: 'HOSPEDADO' | 'SAIU';
}

export interface HospedeCheckin {
  clienteId?: number;
  titular: boolean;
  nomeCompleto?: string;
  cpf?: string;
  telefone?: string;
  cadastrarNovo: boolean;
}

export enum StatusHospedeIndividual {
  HOSPEDADO = 'HOSPEDADO',
  SAIU = 'SAIU'
}