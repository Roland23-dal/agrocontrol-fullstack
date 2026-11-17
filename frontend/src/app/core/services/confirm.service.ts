import { Injectable, signal } from '@angular/core';

export interface ConfirmacionOpciones {
  titulo: string;
  mensaje: string;
  textoConfirmar?: string;
}

/** Diálogo de confirmación reutilizable (por ejemplo, antes de eliminar). */
@Injectable({ providedIn: 'root' })
export class ConfirmService {
  readonly estado = signal<ConfirmacionOpciones | null>(null);
  private resolver: ((valor: boolean) => void) | null = null;

  confirmar(opciones: ConfirmacionOpciones): Promise<boolean> {
    this.estado.set(opciones);
    return new Promise<boolean>((resolve) => {
      this.resolver = resolve;
    });
  }

  responder(valor: boolean): void {
    this.estado.set(null);
    this.resolver?.(valor);
    this.resolver = null;
  }
}
