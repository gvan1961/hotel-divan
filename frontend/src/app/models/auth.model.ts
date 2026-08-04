export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  id: number;
  username: string;
  nome: string;
  email: string;
  fotoBase64?: string;
  perfis: string[];
  permissoes: string[];
}
