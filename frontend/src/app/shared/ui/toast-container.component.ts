import { Component, inject } from '@angular/core';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  template: `
    <div class="toasts" aria-live="polite">
      @for (t of toast.toasts(); track t.id) {
        <div class="toast" [class]="'toast toast--' + t.tipo" role="status">
          <span>{{ t.mensaje }}</span>
          <button type="button" class="toast__cerrar" (click)="toast.cerrar(t.id)" aria-label="Cerrar mensaje">&times;</button>
        </div>
      }
    </div>
  `,
  styles: `
    .toasts {
      position: fixed;
      right: 1rem;
      bottom: 1rem;
      z-index: 1000;
      display: grid;
      gap: 0.5rem;
      width: min(380px, calc(100vw - 2rem));
    }
    .toast {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 0.75rem;
      padding: 0.8rem 1rem;
      border-radius: 8px;
      font-size: 0.925rem;
      color: #fff;
      box-shadow: 0 8px 24px rgba(16, 38, 29, 0.28);
      animation: entrar 0.2s ease-out;
    }
    .toast--exito { background: #1f6b4a; }
    .toast--error { background: #b3321f; }
    .toast--info { background: #2c4a6b; }
    .toast__cerrar {
      background: none;
      border: 0;
      color: inherit;
      font-size: 1.2rem;
      line-height: 1;
      cursor: pointer;
    }
    @keyframes entrar {
      from { transform: translateY(8px); opacity: 0; }
      to { transform: none; opacity: 1; }
    }
  `,
})
export class ToastContainerComponent {
  readonly toast = inject(ToastService);
}
