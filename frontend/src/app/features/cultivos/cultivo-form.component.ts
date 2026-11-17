import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CultivoRequest } from '../../core/models/models';
import { CultivoService } from '../../core/services/cultivo.service';
import { ToastService } from '../../core/services/toast.service';
import { aplicarErroresServidor, obtenerMensajeError } from '../../core/utils/http-error';
import { FieldErrorComponent } from '../../shared/ui/field-error.component';

/** Formulario reactivo de cultivo (POST / PUT). */
@Component({
  selector: 'app-cultivo-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  templateUrl: './cultivo-form.component.html',
})
export class CultivoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(CultivoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly id = signal<number | null>(null);
  readonly cargando = signal(false);
  readonly enviando = signal(false);
  readonly error = signal('');

  readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80)]],
    variedad: ['', [Validators.maxLength(80)]],
    cicloDiasEstimado: [null as number | null, [Validators.min(1), Validators.max(1500), Validators.pattern(/^\d+$/)]],
  });

  get f() {
    return this.form.controls;
  }

  get esEdicion(): boolean {
    return this.id() !== null;
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id.set(Number(idParam));
      this.cargando.set(true);
      this.service.obtener(Number(idParam)).subscribe({
        next: (c) => {
          this.form.patchValue({
            nombre: c.nombre,
            variedad: c.variedad ?? '',
            cicloDiasEstimado: c.cicloDiasEstimado ?? null,
          });
          this.cargando.set(false);
        },
        error: (err) => {
          this.toast.desdeError(err);
          this.router.navigate(['/cultivos']);
        },
      });
    }
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const datos: CultivoRequest = {
      nombre: v.nombre.trim(),
      variedad: v.variedad.trim() || null,
      cicloDiasEstimado: v.cicloDiasEstimado === null ? null : Number(v.cicloDiasEstimado),
    };

    this.enviando.set(true);
    this.error.set('');
    const idActual = this.id();
    const peticion = idActual === null ? this.service.crear(datos) : this.service.actualizar(idActual, datos);

    peticion.subscribe({
      next: () => {
        this.toast.exito(idActual === null ? 'Cultivo registrado correctamente.' : 'Cultivo actualizado correctamente.');
        this.router.navigate(['/cultivos']);
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
