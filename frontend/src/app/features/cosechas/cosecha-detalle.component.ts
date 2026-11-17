import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Cosecha } from '../../core/models/models';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { CosechaService } from '../../core/services/cosecha.service';
import { ToastService } from '../../core/services/toast.service';
import { EstadoBadgeComponent } from '../../shared/ui/estado-badge.component';

/** Detalle de una cosecha (GET /api/cosechas/{id}). */
@Component({
  selector: 'app-cosecha-detalle',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, EstadoBadgeComponent],
  templateUrl: './cosecha-detalle.component.html',
})
export class CosechaDetalleComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(CosechaService);
  private readonly toast = inject(ToastService);
  private readonly confirmacion = inject(ConfirmService);

  readonly cargando = signal(true);
  readonly cosecha = signal<Cosecha | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.obtener(id).subscribe({
      next: (c) => {
        this.cosecha.set(c);
        this.cargando.set(false);
      },
      error: (err) => {
        this.toast.desdeError(err);
        this.router.navigate(['/cosechas']);
      },
    });
  }

  async eliminar(): Promise<void> {
    const c = this.cosecha();
    if (!c) {
      return;
    }
    const confirmado = await this.confirmacion.confirmar({
      titulo: 'Eliminar cosecha',
      mensaje: `¿Deseas eliminar la cosecha del ${c.fecha} (${c.cantidadKg} kg)?`,
      textoConfirmar: 'Eliminar',
    });
    if (!confirmado) {
      return;
    }
    this.service.eliminar(c.id).subscribe({
      next: () => {
        this.toast.exito('Cosecha eliminada correctamente.');
        this.router.navigate(['/cosechas']);
      },
      error: (err) => this.toast.desdeError(err),
    });
  }
}
