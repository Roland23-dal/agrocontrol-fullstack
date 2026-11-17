import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { obtenerMensajeError } from '../utils/http-error';

export type TipoToast = 'exito' | 'error' | 'info';

export interface Toast {
  id: number;
  tipo: TipoToast;
  mensaje: string;
}

/** Notificaciones emergentes (mensajes de éxito / error) */
@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);
  private contador = 0;

  exito(mensaje: string): void {
    this.mostrar('exito', mensaje);
  }

  error(mensaje: string): void {
    this.mostrar('error', mensaje, 6500);
  }

  info(mensaje: string): void {
    this.mostrar('info', mensaje);
  }

  /**
   * Muestra el error de una petición HTTP. Los errores 0, 401 y 403 ya los notifica
   * el interceptor de errores, por lo que aquí se omiten para no duplicar mensajes.
   */
  desdeError(err: unknown): void {
    if (err instanceof HttpErrorResponse && [0, 401, 403].includes(err.status)) {
      return;
    }
    this.error(obtenerMensajeError(err));
  }

  cerrar(id: number): void {
    this.toasts.update((lista) => lista.filter((t) => t.id !== id));
  }

  private mostrar(tipo: TipoToast, mensaje: string, duracionMs = 4500): void {
    const id = ++this.contador;
    this.toasts.update((lista) => [...lista, { id, tipo, mensaje }]);
    setTimeout(() => this.cerrar(id), duracionMs);
  }
}
