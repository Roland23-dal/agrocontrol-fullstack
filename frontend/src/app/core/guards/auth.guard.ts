import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { NombreRol } from '../models/models';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

/** Solo permite entrar a usuarios autenticados; si no, redirige al login. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.estaAutenticado()) {
    return true;
  }
  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

/** Para /login y /registro: si ya hay sesión, redirige a la pantalla inicial según el rol. */
export const publicGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.estaAutenticado() ? router.createUrlTree([auth.rutaInicio()]) : true;
};

/** Restringe una ruta a ciertos roles (autorización en el front-end). */
export const roleGuard = (rolesPermitidos: NombreRol[]): CanActivateFn => {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const toast = inject(ToastService);
    if (auth.tieneAlgunRol(rolesPermitidos)) {
      return true;
    }
    toast.error('No tienes permisos para acceder a esa sección.');
    return router.createUrlTree([auth.rutaInicio()]);
  };
};
