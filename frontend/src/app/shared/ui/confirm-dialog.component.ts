import { Component, inject } from '@angular/core';
import { ConfirmService } from '../../core/services/confirm.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  template: `
    @if (confirm.estado(); as e) {
      <div class="fondo" (click)="confirm.responder(false)">
        <div class="dialogo" role="alertdialog" aria-modal="true" aria-labelledby="dlg-titulo" (click)="$event.stopPropagation()">
          <h2 id="dlg-titulo">{{ e.titulo }}</h2>
          <p>{{ e.mensaje }}</p>
          <div class="dialogo__acciones">
            <button type="button" class="btn btn--contorno" (click)="confirm.responder(false)">Cancelar</button>
            <button type="button" class="btn btn--peligro" (click)="confirm.responder(true)">{{ e.textoConfirmar ?? 'Confirmar' }}</button>
          </div>
        </div>
      </div>
    }
  `,
  styles: `
    .fondo {
      position: fixed;
      inset: 0;
      z-index: 900;
      display: grid;
      place-items: center;
      padding: 1rem;
      background: rgba(16, 38, 29, 0.55);
    }
    .dialogo {
      width: min(440px, 100%);
      padding: 1.5rem;
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 20px 50px rgba(16, 38, 29, 0.35);
    }
    .dialogo p { color: #55645b; }
    .dialogo__acciones {
      display: flex;
      justify-content: flex-end;
      gap: 0.6rem;
      margin-top: 1.25rem;
    }
  `,
})
export class ConfirmDialogComponent {
  readonly confirm = inject(ConfirmService);
}
