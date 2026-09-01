export interface FaixaConsumoAgua {
  id?: number;
  empresaId: number;
  empresaNome?: string;
  qtdHospedes: number;
  valorLimiteDiario: number;
}
export interface FaixaConsumoAguaRequest {
  empresaId: number;
  qtdHospedes: number;
  valorLimiteDiario: number;
}