import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Campana, Cosecha } from '../../core/models/models';
import { AuthService } from '../../core/services/auth.service';
import { CampanaService } from '../../core/services/campana.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { CosechaService } from '../../core/services/cosecha.service';
import { ToastService } from '../../core/services/toast.service';
import { aplicarErroresServidor, obtenerMensajeError } from '../../core/utils/http-error';
import { EstadoBadgeComponent } from '../../shared/ui/estado-badge.component';
import { FieldErrorComponent } from '../../shared/ui/field-error.component';

/**
 * Detalle de campaña: datos, cosechas, total en kg (JPQL SUM) y cierre de la campaña
 * con POST /api/campanas/{id}/finalizar (operación @Transactional en el backend).
 */
@Component({
  selector: 'app-campana-detalle',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, DatePipe, DecimalPipe, EstadoBadgeComponent, FieldErrorComponent],
  templateUrl: './campana-detalle.component.html',
})
export class CampanaDetalleComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(CampanaService);
  private readonly cosechaService = inject(CosechaService);
  private readonly toast = inject(ToastService);
  private readonly confirmacion = inject(ConfirmService);

  readonly cargando = signal(true);
  readonly campana = signal<Campana | null>(null);
  readonly cosechas = signal<Cosecha[]>([]);
  readonly totalKg = signal(0);
  readonly mostrarCierre = signal(false);
  readonly finalizando = signal(false);
  readonly errorCierre = signal('');

  readonly formCierre = this.fb.nonNullable.group({
    fecha: ['', [Validators.required]],
    cantidadKg: [null as number | null, [Validators.required, Validators.min(0.01)]],
    observacion: ['', [Validators.maxLength(255)]],
  });

  get c() {
    return this.formCierre.controls;
  }

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    forkJoin({
      campana: this.service.obtener(id),
      cosechas: this.cosechaService.listarPorCampana(id),
      total: this.cosechaService.totalPorCampana(id),
    }).subscribe({
      next: (r) => {
        this.campana.set(r.campana);
        this.cosechas.set(r.cosechas);
        this.totalKg.set(r.total.totalKg ?? 0);
        this.cargando.set(false);
      },
      error: (err) => {
        this.toast.desdeError(err);
        this.router.navigate(['/campanas']);
      },
    });
  }

  abrirCierre(): void {
    this.formCierre.reset({ fecha: new Date().toISOString().slice(0, 10), cantidadKg: null, observacion: '' });
    this.errorCierre.set('');
    this.mostrarCierre.set(true);
  }

  finalizar(): void {
    const camp = this.campana();
    if (!camp) {
      return;
    }
    if (this.formCierre.invalid) {
      this.formCierre.markAllAsTouched();
      return;
    }
    const v = this.formCierre.getRawValue();
    this.finalizando.set(true);
    this.errorCierre.set('');

    this.service
      .finalizar(camp.id, { fecha: v.fecha, cantidadKg: Number(v.cantidadKg), observacion: v.observacion.trim() || null })
      .subscribe({
        next: () => {
          this.toast.exito('Campaña finalizada y cosecha registrada correctamente.');
          this.finalizando.set(false);
          this.mostrarCierre.set(false);
          this.cargando.set(true);
          this.cargar();
        },
        error: (err) => {
          this.finalizando.set(false);
          if (!aplicarErroresServidor(err, this.formCierre)) {
            this.errorCierre.set(obtenerMensajeError(err));
          }
        },
      });
  }

  async eliminar(): Promise<void> {
    const camp = this.campana();
    if (!camp) {
      return;
    }
    const confirmado = await this.confirmacion.confirmar({
      titulo: 'Eliminar campaña',
      mensaje: `¿Deseas eliminar la campaña de ${camp.cultivo.nombre} en "${camp.parcela.nombre}"? También se eliminarán sus cosechas.`,
      textoConfirmar: 'Eliminar',
    });
    if (!confirmado) {
      return;
    }
    this.service.eliminar(camp.id).subscribe({
      next: () => {
        this.toast.exito('Campaña eliminada correctamente.');
        this.router.navigate(['/campanas']);
      },
      error: (err) => this.toast.desdeError(err),
    });
  }
}
