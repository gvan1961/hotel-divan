export interface ItemPixPendente {
  produtoId: number;
  nome: string;
  quantidade: number;
  valorUnitario: number;
}

export interface CobrancaPix {
  id: number;
  correlationId: string;
  valor: number;
  brCode: string;
  qrCodeImage: string;
  comentario?: string;
  reservaId?: number;
  status: 'PENDENTE' | 'PAGO' | 'EXPIRADO' | 'CANCELADO';
  dataCriacao: string;
  itensJson?: string;
}