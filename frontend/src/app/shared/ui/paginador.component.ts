import { Component, EventEmitter, Input, Output } from '@angular/core';

/** Paginación del lado del cliente para las tablas de listado. */
@Component({
  selector: 'app-paginador',
  standalone: true,
  template: `
    @if (total > 0) {
      <div class="paginador">
        <span>Mostrando {{ desde() }}-{{ hasta() }} de {{ total }}</span>
        @if (paginas() > 1) {
          <div class="paginador__botones">
            <button type="button" class="btn btn--contorno btn--sm" (click)="ir(pagina - 1)" [disabled]="pagina <= 1">Anterior</button>
            <span class="visually-hidden">Página {{ pagina }} de {{ paginas() }}</span>
            <button type="button" class="btn btn--contorno btn--sm" (click)="ir(pagina + 1)" [disabled]="pagina >= paginas()">Siguiente</button>
          </div>
        }
      </div>
    }
  `,
})
export class PaginadorComponent {
  @Input() total = 0;
  @Input() pagina = 1;
  @Input() tamano = 8;
  @Output() paginaChange = new EventEmitter<number>();

  paginas(): number {
    return Math.max(1, Math.ceil(this.total / this.tamano));
  }

  desde(): number {
    return (this.pagina - 1) * this.tamano + 1;
  }

  hasta(): number {
    return Math.min(this.pagina * this.tamano, this.total);
  }

  ir(destino: number): void {
    if (destino >= 1 && destino <= this.paginas()) {
      this.paginaChange.emit(destino);
    }
  }
}
