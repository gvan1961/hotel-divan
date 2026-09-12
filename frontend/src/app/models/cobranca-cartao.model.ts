export interface CobrancaCartao {
  id: number;
  correlationId: string;
  valor: number;
  formaPagamento: 'credit_card' | 'debit_card';
  ordemMercadoPago?: string;
  reservaId?: number;
  numeroApartamento?: string;
  status: 'PENDENTE' | 'PAGO' | 'CANCELADO';
  dataCriacao: string;
}