import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

/**
 * Manejo global de errores de seguridad y conectividad:
 *  - 401 (token ausente, inválido o vencido) -> cierra la sesión y redirige al login
 *  - 403 (rol sin permiso)                    -> avisa al usuario
 *  - 0   (backend caído / CORS)               -> avisa que no hay conexión
 * El resto de errores (400, 404...) los muestra cada componente.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const esAuth = req.url.includes('/auth/');

      if (err.status === 401 && !esAuth) {
        const returnUrl = router.url;
        auth.logout(false);
        toast.info('Tu sesión expiró. Inicia sesión nuevamente.');
        router.navigate(['/login'], { queryParams: { returnUrl } });
      } else if (err.status === 403) {
        toast.error('Acceso denegado: tu rol no tiene permisos para esta operación.');
      } else if (err.status === 0) {
        toast.error('No hay conexión con el servidor. Verifica que el backend esté activo en el puerto 8082.');
      }
      return throwError(() => err);
    }),
  );
};
