export interface Cliente {
  id?: number;
  nome: string;
  cpf: string;
  celular: string;
  ddi?: string;
  celular2?: string;
  ddi2?: string;
  endereco?: string;
  cep?: string;
  cidade?: string;
  estado?: string;
  dataNascimento: string;
  empresaId?: number;
  empresa?: any;
  empresaNome?: string; 
  creditoAprovado?: boolean;
  autorizadoJantar?: boolean;
  tipoCliente?: TipoCliente;
  menorDeIdade?: boolean;
  responsavelId?: number;
  responsavelNome?: string;
  responsavelCpf?: string;
  classificacao?: string | null;
  fumante?: boolean;
  fotoBase64?: string;
}

export interface ClienteRequest {
  nome: string;
  cpf: string;
  celular: string;
  ddi?: string;
 celular2?: string;
  ddi2?: string;
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
  classificacao?: string | null;
  fumante?: boolean;
  fotoBase64?: string;
}

export enum TipoCliente {
  HOSPEDE = 'HOSPEDE',
  FUNCIONARIO = 'FUNCIONARIO'
}

export const TIPO_CLIENTE_LABELS: { [key in TipoCliente]: string } = {
  HOSPEDE: 'Hóspede',
  FUNCIONARIO: 'Funcionário'
};