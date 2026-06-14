// 🔧 AJUSTE os caminhos de import nos outros arquivos conforme sua estrutura
// (ex.: src/app/models/manutencao.model.ts)

export type TipoServico =
  | 'AR_CONDICIONADO'
  | 'LIMPEZA_FILTRO_AR'
  | 'CHUVEIRO'
  | 'ELETRICA'
  | 'HIDRAULICA'
  | 'MOVEIS'
  | 'PINTURA'
  | 'TV'
  | 'FECHADURA'
  | 'LIMPEZA_PESADA'
  | 'OUTROS';

export type StatusManutencao = 'PENDENTE' | 'EM_ANDAMENTO' | 'CONCLUIDO' | 'CANCELADO';

export interface Manutencao {
  id: number;
  apartamentoId: number;
  apartamentoNumero: string;
  dataServico: string;            // ISO yyyy-MM-dd
  tipoServico: TipoServico;
  tipoServicoDescricao: string;
  descricao: string;
  responsavel?: string;
  custo?: number;
  status: StatusManutencao;
  statusDescricao: string;
  dataConclusao?: string;
  observacoes?: string;
  createdAt?: string;
}

export interface ManutencaoRequest {
  apartamentoId: number;
  dataServico?: string;
  tipoServico: TipoServico;
  descricao: string;
  responsavel?: string;
  custo?: number;
  status?: StatusManutencao;
  dataConclusao?: string;
  observacoes?: string;
}

// Listas para popular os <select> (mesma ordem/labels do enum do backend)
export const TIPOS_SERVICO: { valor: TipoServico; descricao: string; icone: string }[] = [
  { valor: 'AR_CONDICIONADO',   descricao: 'Ar Condicionado',        icone: '❄️' },
  { valor: 'LIMPEZA_FILTRO_AR', descricao: 'Limpeza de Filtro de Ar', icone: '🌬️' },
  { valor: 'CHUVEIRO',          descricao: 'Chuveiro',               icone: '🚿' },
  { valor: 'ELETRICA',          descricao: 'Elétrica',               icone: '⚡' },
  { valor: 'HIDRAULICA',        descricao: 'Hidráulica',             icone: '🔧' },
  { valor: 'MOVEIS',            descricao: 'Móveis',                 icone: '🪑' },
  { valor: 'PINTURA',           descricao: 'Pintura',                icone: '🎨' },
  { valor: 'TV',                descricao: 'TV / Eletrônicos',       icone: '📺' },
  { valor: 'FECHADURA',         descricao: 'Fechadura / Chaves',     icone: '🔑' },
  { valor: 'LIMPEZA_PESADA',    descricao: 'Limpeza Pesada',         icone: '🧹' },
  { valor: 'OUTROS',            descricao: 'Outros',                 icone: '📦' },
];

export const STATUS_MANUTENCAO: { valor: StatusManutencao; descricao: string }[] = [
  { valor: 'PENDENTE',     descricao: 'Pendente' },
  { valor: 'EM_ANDAMENTO', descricao: 'Em Andamento' },
  { valor: 'CONCLUIDO',    descricao: 'Concluído' },
  { valor: 'CANCELADO',    descricao: 'Cancelado' },
];