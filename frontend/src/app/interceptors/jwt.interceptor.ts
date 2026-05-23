import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

let sessaoExpiradaAvisada = false;

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  const rotasPublicas = [
    '/api/auth/login',
    '/api/auth/register',
    '/api/public/',
    '/api/ponto/registrar',
    '/api/ponto/hoje',
    '/api/ponto/ultimo-registro/',
    '/api/ponto/funcionarios-com-foto'    
  ];

  // ✅ Rotas Angular que não precisam de autenticação
  const rotasAngularPublicas = ['/ponto', '/login'];
  const rotaAtual = router.url;
  const isRotaAngularPublica = rotasAngularPublicas.some(r => rotaAtual.startsWith(r));

  const isRotaPublica = rotasPublicas.some(rota => req.url.includes(rota));
  if (isRotaPublica) {
    return next(req);
  }

  const token = localStorage.getItem('token');

  // ✅ Se está numa rota pública e token expirado, não redireciona
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      if (Date.now() >= payload.exp * 1000 && !isRotaAngularPublica) {
        if (!sessaoExpiradaAvisada) {
          sessaoExpiradaAvisada = true;
          localStorage.clear();
          alert('⏰ Sua sessão expirou. Faça login novamente.');
          router.navigate(['/login']);
          setTimeout(() => sessaoExpiradaAvisada = false, 3000);
        }
        return throwError(() => new Error('Token expirado'));
      }
    } catch (e) {}
  }

  let reqClonada = req;
  if (token) {
    reqClonada = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(reqClonada).pipe(
    catchError((error: HttpErrorResponse) => {
      const rotaAtual = router.url;
      const isRotaAngularPublica = ['/ponto', '/login'].some(r => rotaAtual.startsWith(r));
      
      if (error.status === 401 && !isRotaAngularPublica) {
        if (!sessaoExpiradaAvisada) {
          sessaoExpiradaAvisada = true;
          localStorage.clear();
          alert('⏰ Sua sessão expirou. Faça login novamente.');
          router.navigate(['/login']);
          setTimeout(() => sessaoExpiradaAvisada = false, 3000);
        }
      }
      return throwError(() => error);
    })
  );
};
