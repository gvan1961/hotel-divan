export interface Empresa {
  id?: number;
  nomeEmpresa: string;
  cnpj: string;
  contato: string;
  celular: string;
  // NOVOS CAMPOS
  contatoFinanceiroNome?: string;
  contatoFinanceiroDdi?: string;
  contatoFinanceiroCelular?: string;
}


export interface EmpresaRequest {
  nomeEmpresa: string;
  cnpj: string;
  contato: string;
  celular: string;
  // NOVOS CAMPOS
  contatoFinanceiroNome?: string;
  contatoFinanceiroDdi?: string;
  contatoFinanceiroCelular?: string;
}
