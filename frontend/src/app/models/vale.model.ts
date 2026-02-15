export interface Vale {
  id?: number;
  clienteId: number;
  clienteNome?: string;
  clienteCpf?: string;
  dataConcessao: string;
  dataVencimento: string;
  dataPagamento?: string;
  tipoVale: TipoVale;
  tipoValeDescricao?: string;
  valor: number;
  status: StatusVale;
  statusDescricao?: string;
  observacao?: string;
  assinaturaBase64?: string;        // ⭐ NOVO - Assinatura em base64
  dataAssinatura?: string;          // ⭐ NOVO - Data/hora da assinatura
  dataCancelamento?: string;        // ⭐ NOVO - Data do cancelamento
  motivoCancelamento?: string;
  dataCriacao?: string;
  criadoPor?: string;
}

export interface ValeRequest {
  clienteId: number;
  dataConcessao: string;
  dataVencimento: string;
  tipoVale: TipoVale;
  valor: number;
  observacao?: string;
}

export enum TipoVale {
  ADIANTAMENTO = 'ADIANTAMENTO',
  EMPRESTIMO = 'EMPRESTIMO',
  OUTROS = 'OUTROS'
}

export enum StatusVale {
  PENDENTE = 'PENDENTE',
  PAGO = 'PAGO',
  CANCELADO = 'CANCELADO',
  VENCIDO = 'VENCIDO'
}

export const TIPO_VALE_LABELS: { [key in TipoVale]: string } = {
  ADIANTAMENTO: '💰 Adiantamento Salarial',
  EMPRESTIMO: '💵 Empréstimo',
  OUTROS: '📋 Outros'
};

export const STATUS_VALE_LABELS: { [key in StatusVale]: string } = {
  PENDENTE: '⏳ Pendente',
  PAGO: '✅ Pago',
  CANCELADO: '❌ Cancelado',
  VENCIDO: '⚠️ Vencido'
};