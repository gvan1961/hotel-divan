import { Injectable } from '@angular/core';
import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
  sub: string;
  authorities: string[];
  iat: number;
  exp: number;
}

@Injectable({
  providedIn: 'root'
})
export class PermissionService {

  private permissions: string[] = [];

  constructor() {
    this.loadPermissions();
  }

  /**
   * Carregar permissões do JWT armazenado
   */
  private loadPermissions(): void {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const decoded: JwtPayload = jwtDecode(token);
        this.permissions = decoded.authorities || [];
        console.log('🔐 Permissões carregadas:', this.permissions);
      } catch (error) {
        console.error('❌ Erro ao decodificar token:', error);
        this.permissions = [];
      }
    }
  }

  /**
   * Recarregar permissões (chamar após login)
   */
  reloadPermissions(): void {
    this.loadPermissions();
  }

  /**
   * Verificar se tem UMA permissão específica
   */
  hasPermission(permission: string): boolean {
    return this.permissions.includes(permission);
  }

  /**
   * Verificar se tem TODAS as permissões da lista
   */
  hasAllPermissions(permissions: string[]): boolean {
    return permissions.every(p => this.permissions.includes(p));
  }

  /**
   * Verificar se tem QUALQUER UMA das permissões da lista
   */
  hasAnyPermission(permissions: string[]): boolean {
    return permissions.some(p => this.permissions.includes(p));
  }

  /**
   * Verificar se tem uma role específica
   */
  hasRole(role: string): boolean {
    return this.permissions.includes(`ROLE_${role}`);
  }

  /**
   * Obter todas as permissões
   */
  getPermissions(): string[] {
    return [...this.permissions];
  }

  /**
   * Limpar permissões (chamar no logout)
   */
  clearPermissions(): void {
    this.permissions = [];
  }
}
