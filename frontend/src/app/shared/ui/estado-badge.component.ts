import { Component, Input } from '@angular/core';
import { EstadoCampana } from '../../core/models/models';

const ETIQUETAS: Record<string, string> = {
  PLANIFICADA: 'Planificada',
  EN_CURSO: 'En curso',
  FINALIZADA: 'Finalizada',
};

/** Insignia de color para el estado de una campaña agrícola. */
@Component({
  selector: 'app-estado-badge',
  standalone: true,
  template: `<span [class]="'insignia insignia--' + clase()">{{ etiqueta() }}</span>`,
})
export class EstadoBadgeComponent {
  @Input({ required: true }) estado!: EstadoCampana | string;

  clase(): string {
    return String(this.estado).toLowerCase();
  }

  etiqueta(): string {
    return ETIQUETAS[this.estado] ?? String(this.estado);
  }
}
