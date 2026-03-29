import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { LoginRequest, LoginResponse } from '../models/auth.model';
import { Router } from '@angular/router';
import { PermissionService } from './permission.service'; // ✅ ADICIONAR

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private permissionService = inject(PermissionService); // ✅ ADICIONAR
  
  private apiUrl = '/api/auth';
  
  private currentUserSubject = new BehaviorSubject<LoginResponse | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  login(credentials: LoginRequest): Observable<LoginResponse> {
    console.log('🔐 Fazendo login...', credentials.username);
    
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          console.log('═══════════════════════════════════════');
          console.log('✅ LOGIN BEM-SUCEDIDO');
          console.log('═══════════════════════════════════════');
          console.log('Response completa:', response);
          
          // ✅ SALVAR TOKEN
          localStorage.setItem('token', response.token);
          console.log('✅ Token salvo');
          
          // ✅✅✅ SALVAR COMO "usuario" (NÃO "user") ✅✅✅
          const usuarioParaSalvar = {
            id: response.id,
            nome: response.nome,
            email: response.email,
            username: response.username,
            perfis: response.perfis,
            permissoes: response.permissoes
          };
          
          localStorage.setItem('usuario', JSON.stringify(usuarioParaSalvar));
          console.log('✅ Usuario salvo:', usuarioParaSalvar);
          
          // ✅ TAMBÉM SALVAR COMO "user" PARA COMPATIBILIDADE
          localStorage.setItem('user', JSON.stringify(response));
          
          // ✅✅✅ RECARREGAR PERMISSÕES APÓS LOGIN ✅✅✅
          this.permissionService.reloadPermissions();
          
          // Verificar o que foi salvo
          console.log('📋 LocalStorage após login:');
          console.log('   Token:', localStorage.getItem('token'));
          console.log('   Usuario:', localStorage.getItem('usuario'));
          console.log('═══════════════════════════════════════');
          
          this.currentUserSubject.next(response);
        })
      );
  }

  logout(): void {
    console.log('🚪 Fazendo logout...');
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('usuario');
    
    // ✅✅✅ LIMPAR PERMISSÕES NO LOGOUT ✅✅✅
    this.permissionService.clearPermissions();
    
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private getUserFromStorage(): LoginResponse | null {
    // Tentar buscar de 'usuario' primeiro (novo formato)
    let user = localStorage.getItem('usuario');
    
    // Se não encontrar, buscar de 'user' (formato antigo)
    if (!user) {
      user = localStorage.getItem('user');
    }
    
    return user ? JSON.parse(user) : null;
  }

  getCurrentUser(): LoginResponse | null {
    return this.currentUserSubject.value;
  }

  /**
   * ✅ OBTER ID DO USUÁRIO LOGADO
   */
  getUsuarioId(): number {
    const usuario = this.getCurrentUser();
    
    if (usuario && usuario.id) {
      console.log('✅ ID do usuário logado:', usuario.id);
      return usuario.id;
    }
    
    // ⚠️ FALLBACK: Tentar pegar direto do localStorage
    const usuarioStr = localStorage.getItem('usuario') || localStorage.getItem('user');
    
    if (usuarioStr) {
      try {
        const usuarioObj = JSON.parse(usuarioStr);
        if (usuarioObj.id) {
          console.log('✅ ID do usuário (do localStorage):', usuarioObj.id);
          return usuarioObj.id;
        }
      } catch (e) {
        console.error('❌ Erro ao parsear usuário do localStorage:', e);
      }
    }
    
    // ⚠️ ÚLTIMO FALLBACK: Retorna 1 se não houver usuário logado
    console.warn('⚠️ Nenhum usuário logado encontrado. Usando ID padrão: 1');
    return 1;
  }

  /**
 * ✅ OBTER NOME DO USUÁRIO LOGADO
 */
getUsuarioNome(): string {
  const usuario = this.getCurrentUser();
  
  // Tenta pegar o nome, se não tiver usa o username
  if (usuario) {
    return usuario.nome || usuario.username || 'Usuário';
  }
  
  return 'Usuário';
}

  /**
   * ✅ OBTER EMAIL DO USUÁRIO LOGADO
   */
  getUsuarioEmail(): string {
    const usuario = this.getCurrentUser();
    return usuario?.email || '';
  }

  /**
   * ✅ VERIFICAR SE TEM PERMISSÃO
   */
  hasPermission(permissao: string): boolean {
    const usuario = this.getCurrentUser();
    return usuario?.permissoes?.includes(permissao) || false;
  }

  /**
   * ✅ VERIFICAR SE TEM PERFIL
   */
  hasPerfil(perfil: string): boolean {
    const usuario = this.getCurrentUser();
    return usuario?.perfis?.includes(perfil) || false;
  }
}
