import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

// ✅ Flag global para evitar múltiplos alerts simultâneos
let sessaoExpiradaAvisada = false;

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  const rotasPublicas = [
    '/api/auth/login',
    '/api/auth/register',
    '/api/public/'
  ];

  const isRotaPublica = rotasPublicas.some(rota => req.url.includes(rota));

  if (isRotaPublica) {
    return next(req);
  }

  const token = localStorage.getItem('token');

  let reqClonada = req;
  if (token) {
    reqClonada = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(reqClonada).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        if (!sessaoExpiradaAvisada) {
          sessaoExpiradaAvisada = true;
          console.log('⏰ Token expirado — redirecionando para login');
          localStorage.clear();
          alert('⏰ Sua sessão expirou. Faça login novamente.');
          router.navigate(['/login']);
          
          setTimeout(() => {
            sessaoExpiradaAvisada = false;
          }, 3000);
        }
      }
      return throwError(() => error);
    })
  );
};
