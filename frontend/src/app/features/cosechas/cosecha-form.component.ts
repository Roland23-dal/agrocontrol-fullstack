import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Campana, CosechaRequest } from '../../core/models/models';
import { CampanaService } from '../../core/services/campana.service';
import { CosechaService } from '../../core/services/cosecha.service';
import { ToastService } from '../../core/services/toast.service';
import { aplicarErroresServidor, obtenerMensajeError } from '../../core/utils/http-error';
import { FieldErrorComponent } from '../../shared/ui/field-error.component';

/** Formulario reactivo de cosecha (POST / PUT). Admite ?campanaId= para preseleccionar la campaña. */
@Component({
  selector: 'app-cosecha-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  templateUrl: './cosecha-form.component.html',
})
export class CosechaFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(CosechaService);
  private readonly campanaService = inject(CampanaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly id = signal<number | null>(null);
  readonly cargando = signal(true);
  readonly enviando = signal(false);
  readonly error = signal('');
  readonly campanas = signal<Campana[]>([]);

  readonly form = this.fb.nonNullable.group({
    campanaId: [null as number | null, [Validators.required]],
    fecha: ['', [Validators.required]],
    cantidadKg: [null as number | null, [Validators.required, Validators.min(0.01)]],
    observacion: ['', [Validators.maxLength(255)]],
  });

  get f() {
    return this.form.controls;
  }

  get esEdicion(): boolean {
    return this.id() !== null;
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const campanaParam = this.route.snapshot.queryParamMap.get('campanaId');
    if (idParam) {
      this.id.set(Number(idParam));
    } else {
      this.form.patchValue({ fecha: new Date().toISOString().slice(0, 10) });
    }
    if (campanaParam) {
      this.form.patchValue({ campanaId: Number(campanaParam) });
    }

    this.campanaService.listar().subscribe({
      next: (lista) => {
        // Una cosecha no se puede registrar en una campaña PLANIFICADA (regla del backend)
        this.campanas.set(idParam ? lista : lista.filter((c) => c.estado !== 'PLANIFICADA'));
        if (idParam) {
          this.cargarCosecha(Number(idParam));
        } else {
          this.cargando.set(false);
        }
      },
      error: (err) => {
        this.toast.desdeError(err);
        this.router.navigate(['/cosechas']);
      },
    });
  }

  private cargarCosecha(id: number): void {
    this.service.obtener(id).subscribe({
      next: (c) => {
        this.form.patchValue({
          campanaId: c.campana.id,
          fecha: c.fecha,
          cantidadKg: c.cantidadKg,
          observacion: c.observacion ?? '',
        });
        this.form.controls.campanaId.disable(); // la campaña de una cosecha no se puede cambiar
        this.cargando.set(false);
      },
      error: (err) => {
        this.toast.desdeError(err);
        this.router.navigate(['/cosechas']);
      },
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const datos: CosechaRequest = {
      fecha: v.fecha,
      cantidadKg: Number(v.cantidadKg),
      observacion: v.observacion.trim() || null,
      campanaId: Number(v.campanaId),
    };

    this.enviando.set(true);
    this.error.set('');
    const idActual = this.id();
    const peticion = idActual === null ? this.service.crear(datos) : this.service.actualizar(idActual, datos);

    peticion.subscribe({
      next: () => {
        this.toast.exito(idActual === null ? 'Cosecha registrada correctamente.' : 'Cosecha actualizada correctamente.');
        this.router.navigate(['/cosechas']);
      },
      error: (err) => {
        this.enviando.set(false);
        if (!aplicarErroresServidor(err, this.form)) {
          this.error.set(obtenerMensajeError(err));
        }
      },
    });
  }
}
