import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Campana, Cultivo } from '../../core/models/models';
import { AuthService } from '../../core/services/auth.service';
import { CampanaService } from '../../core/services/campana.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { CultivoService } from '../../core/services/cultivo.service';
import { ToastService } from '../../core/services/toast.service';
import { EstadoBadgeComponent } from '../../shared/ui/estado-badge.component';

/** Detalle de cultivo y campañas donde se usa (GET /api/campanas/cultivo?nombre=). */
@Component({
  selector: 'app-cultivo-detalle',
  standalone: true,
  imports: [RouterLink, DatePipe, EstadoBadgeComponent],
  templateUrl: './cultivo-detalle.component.html',
})
export class CultivoDetalleComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(CultivoService);
  private readonly campanaService = inject(CampanaService);
  private readonly toast = inject(ToastService);
  private readonly confirmacion = inject(ConfirmService);

  readonly cargando = signal(true);
  readonly cultivo = signal<Cultivo | null>(null);
  readonly campanas = signal<Campana[]>([]);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.obtener(id).subscribe({
      next: (c) => {
        this.cultivo.set(c);
        this.cargando.set(false);
        // La búsqueda por nombre es parcial (LIKE): se filtra por id exacto del cultivo
        this.campanaService.listarPorCultivo(c.nombre).subscribe({
          next: (lista) => this.campanas.set(lista.filter((x) => x.cultivo.id === c.id)),
          error: (err) => this.toast.desdeError(err),
        });
      },
      error: (err) => {
        this.toast.desdeError(err);
        this.router.navigate(['/cultivos']);
      },
    });
  }

  async eliminar(): Promise<void> {
    const c = this.cultivo();
    if (!c) {
      return;
    }
    const confirmado = await this.confirmacion.confirmar({
      titulo: 'Eliminar cultivo',
      mensaje: `¿Deseas eliminar el cultivo "${c.nombre}" del catálogo?`,
      textoConfirmar: 'Eliminar',
    });
    if (!confirmado) {
      return;
    }
    this.service.eliminar(c.id).subscribe({
      next: () => {
        this.toast.exito('Cultivo eliminado correctamente.');
        this.router.navigate(['/cultivos']);
      },
      error: (err) => {
        if (err instanceof HttpErrorResponse && err.status === 400) {
          this.toast.error('No se puede eliminar el cultivo porque tiene campañas agrícolas asociadas.');
        } else {
          this.toast.desdeError(err);
        }
      },
    });
  }
}
