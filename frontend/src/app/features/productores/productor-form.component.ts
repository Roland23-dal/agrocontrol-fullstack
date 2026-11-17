import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProductorRequest } from '../../core/models/models';
import { ProductorService } from '../../core/services/productor.service';
import { ToastService } from '../../core/services/toast.service';
import { aplicarErroresServidor, obtenerMensajeError } from '../../core/utils/http-error';
import { FieldErrorComponent } from '../../shared/ui/field-error.component';

/** Formulario reactivo para registrar (POST) o editar (PUT) un productor. */
@Component({
  selector: 'app-productor-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  templateUrl: './productor-form.component.html',
})
export class ProductorFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ProductorService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly id = signal<number | null>(null);
  readonly cargando = signal(false);
  readonly enviando = signal(false);
  readonly error = signal('');

  readonly form = this.fb.nonNullable.group({
    nombres: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    apellidos: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    telefono: ['', [Validators.pattern(/^9\d{8}$/)]],
    email: ['', [Validators.email, Validators.maxLength(120)]],
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
        next: (p) => {
          this.form.patchValue({
            nombres: p.nombres,
            apellidos: p.apellidos,
            dni: p.dni,
            telefono: p.telefono ?? '',
            email: p.email ?? '',
          });
          this.cargando.set(false);
        },
        error: (err) => {
          this.toast.desdeError(err);
          this.router.navigate(['/productores']);
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
    const datos: ProductorRequest = {
      nombres: v.nombres.trim(),
      apellidos: v.apellidos.trim(),
      dni: v.dni,
      telefono: v.telefono.trim() || null,
      email: v.email.trim() || null,
    };

    this.enviando.set(true);
    this.error.set('');
    const idActual = this.id();
    const peticion = idActual === null ? this.service.crear(datos) : this.service.actualizar(idActual, datos);

    peticion.subscribe({
      next: () => {
        this.toast.exito(idActual === null ? 'Productor registrado correctamente.' : 'Productor actualizado correctamente.');
        this.router.navigate(['/productores']);
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
