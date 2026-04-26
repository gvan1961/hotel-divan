import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard = (roles: string[]): CanActivateFn => {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      router.navigate(['/login']);
      return false;
    }

    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    const perfis: string[] = usuario.perfis || [];

    const temPermissao = roles.some(role =>
      perfis.some(p => p.toUpperCase().includes(role.toUpperCase()))
    );

    if (temPermissao) {
      return true;
    }

    router.navigate(['/home']);
    return false;
  };
};