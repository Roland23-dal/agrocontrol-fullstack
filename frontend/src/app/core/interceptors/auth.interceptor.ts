import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

/**
 * Agrega el token JWT (Authorization: Bearer <token>) a TODAS las peticiones
 * dirigidas a la API, excepto a /auth/login y /auth/register (endpoints públicos).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).token();
  const esApi = req.url.startsWith(environment.apiUrl);
  const esPublico = req.url.includes('/auth/');

  if (token && esApi && !esPublico) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req);
};
