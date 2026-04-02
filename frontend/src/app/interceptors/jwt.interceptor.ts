import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  console.log('🔐 Interceptor executado para:', req.url);

  const router = inject(Router);

  const rotasPublicas = [
    '/api/auth/login',
    '/api/auth/register',
    '/api/public/'
  ];

  const isRotaPublica = rotasPublicas.some(rota => req.url.includes(rota));

  if (isRotaPublica) {
    console.log('⚠️ Rota pública detectada, pulando interceptor');
    return next(req);
  }

  const token = localStorage.getItem('token');
  console.log('📝 Token encontrado:', token ? 'SIM' : 'NÃO');

  let reqClonada = req;
  if (token) {
    console.log('✅ Adicionando token ao header Authorization');
    reqClonada = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  } else {
    console.log('⚠️ Sem token, enviando requisição sem autenticação');
  }

  return next(reqClonada).pipe(
    catchError((error: HttpErrorResponse) => {
     if (error.status === 401) {
    console.log('⏰ Token expirado — redirecionando para login');
    localStorage.clear();
    alert('⏰ Sua sessão expirou. Faça login novamente.');
    router.navigate(['/login']);
}
      return throwError(() => error);
    })
  );
};
