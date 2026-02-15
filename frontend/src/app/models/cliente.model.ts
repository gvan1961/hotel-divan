export interface Cliente {
  id?: number;
  nome: string;
  cpf: string;
  celular: string;
  endereco?: string;
  cep?: string;
  cidade?: string;
  estado?: string;
  dataNascimento: string;
  empresaId?: number;
  empresa?: any;
  creditoAprovado?: boolean;
  autorizadoJantar?: boolean;  // ⭐ ADICIONAR ESTA LINHA
  tipoCliente?: TipoCliente;
}

export interface ClienteRequest {
  nome: string;
  cpf: string;
  celular: string;
  endereco?: string;
  cep?: string;
  cidade?: string;
  estado?: string;
  dataNascimento: string;
  empresaId?: number;
  creditoAprovado?: boolean;
  autorizadoJantar?: boolean;  // ⭐ ADICIONAR ESTA LINHA
  tipoCliente?: TipoCliente;
}

// ⭐ ADICIONAR ENUM
export enum TipoCliente {
  HOSPEDE = 'HOSPEDE',
  FUNCIONARIO = 'FUNCIONARIO'
}

// ⭐ ADICIONAR LABELS
export const TIPO_CLIENTE_LABELS: { [key in TipoCliente]: string } = {
  HOSPEDE: 'Hóspede',
  FUNCIONARIO: 'Funcionário'
};