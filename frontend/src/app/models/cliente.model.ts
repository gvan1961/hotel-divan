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
  autorizadoJantar?: boolean;
  tipoCliente?: TipoCliente;
  menorDeIdade?: boolean;
  responsavelId?: number;
  responsavelNome?: string;
  responsavelCpf?: string;
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
  autorizadoJantar?: boolean;
  tipoCliente?: TipoCliente;
  menorDeIdade?: boolean;
  responsavelId?: number;
}

export enum TipoCliente {
  HOSPEDE = 'HOSPEDE',
  FUNCIONARIO = 'FUNCIONARIO'
}

export const TIPO_CLIENTE_LABELS: { [key in TipoCliente]: string } = {
  HOSPEDE: 'Hóspede',
  FUNCIONARIO: 'Funcionário'
};