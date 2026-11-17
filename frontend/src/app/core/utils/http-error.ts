import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormGroup } from '@angular/forms';
import { ApiError } from '../models/models';

/**
 * Convierte cualquier error HTTP en un mensaje legible para el usuario.
 * Usa el campo "error" que devuelve GlobalExceptionHandler del backend.
 */
export function obtenerMensajeError(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'No se pudo conectar con el servidor. Verifica que el backend esté en ejecución (puerto 8082).';
    }
    const cuerpo = err.error as Partial<ApiError> | null;
    if (cuerpo && typeof cuerpo === 'object' && cuerpo.error) {
      return cuerpo.error;
    }
    switch (err.status) {
      case 401:
        return 'Sesión no válida o expirada. Inicia sesión nuevamente.';
      case 403:
        return 'No tienes permisos para realizar esta operación.';
      case 404:
        return 'El recurso solicitado no existe.';
      default:
        return `Error inesperado del servidor (${err.status}).`;
    }
  }
  return 'Ocurrió un error inesperado.';
}

/**
 * Si el backend devolvió errores de validación por campo (400 con "detalles"),
 * los asigna a los controles del formulario para mostrarlos junto a cada campo.
 * Devuelve true si se asignó al menos un error.
 */
export function aplicarErroresServidor(err: unknown, form: FormGroup): boolean {
  if (!(err instanceof HttpErrorResponse) || err.status !== 400) {
    return false;
  }
  const detalles = (err.error as Partial<ApiError> | null)?.detalles;
  if (!detalles) {
    return false;
  }
  let asignado = false;
  for (const campo of Object.keys(detalles)) {
    const control: AbstractControl | null = form.get(campo);
    if (control) {
      control.setErrors({ servidor: detalles[campo] });
      control.markAsTouched();
      asignado = true;
    }
  }
  return asignado;
}
