import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ParcelaRequest, Productor } from '../../core/models/models';
import { ParcelaService } from '../../core/services/parcela.service';
import { ProductorService } from '../../core/services/productor.service';
import { ToastService } from '../../core/services/toast.service';
import { aplicarErroresServidor, obtenerMensajeError } from '../../core/utils/http-error';
import { FieldErrorComponent } from '../../shared/ui/field-error.component';

/** Formulario reactivo de parcela (POST / PUT). Admite ?productorId= para preseleccionar el propietario. */
@Component({
  selector: 'app-parcela-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  templateUrl: './parcela-form.component.html',
})
export class ParcelaFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ParcelaService);
  private readonly productorService = inject(ProductorService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly id = signal<number | null>(null);
  readonly cargando = signal(true);
  readonly enviando = signal(false);
  readonly error = signal('');
  readonly productores = signal<Productor[]>([]);

  readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    ubicacion: ['', [Validators.maxLength(150)]],
    areaHectareas: [null as number | null, [Validators.required, Validators.min(0.01)]],
    productorId: [null as number | null, [Validators.required]],
  });

  get f() {
    return this.form.controls;
  }

  get esEdicion(): boolean {
    return this.id() !== null;
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const productorParam = this.route.snapshot.queryParamMap.get('productorId');
    if (idParam) {
      this.id.set(Number(idParam));
    }
    if (productorParam) {
      this.form.patchValue({ productorId: Number(productorParam) });
    }

    this.productorService.listar().subscribe({
      next: (lista) => {
        this.productores.set(lista);
        if (idParam) {
          this.cargarParcela(Number(idParam));
        } else {
          this.cargando.set(false);
        }
      },
      error: (err) => {
        this.toast.desdeError(err);
        this.router.navigate(['/parcelas']);
      },
    });
  }

  private cargarParcela(id: number): void {
    this.service.obtener(id).subscribe({
      next: (p) => {
        this.form.patchValue({
          nombre: p.nombre,
          ubicacion: p.ubicacion ?? '',
          areaHectareas: p.areaHectareas,
          productorId: p.productor.id,
        });
        this.cargando.set(false);
      },
      error: (err) => {
        this.toast.desdeError(err);
        this.router.navigate(['/parcelas']);
      },
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const datos: ParcelaRequest = {
      nombre: v.nombre.trim(),
      ubicacion: v.ubicacion.trim() || null,
      areaHectareas: Number(v.areaHectareas),
      productorId: Number(v.productorId),
    };

    this.enviando.set(true);
    this.error.set('');
    const idActual = this.id();
    const peticion = idActual === null ? this.service.crear(datos) : this.service.actualizar(idActual, datos);

    peticion.subscribe({
      next: () => {
        this.toast.exito(idActual === null ? 'Parcela registrada correctamente.' : 'Parcela actualizada correctamente.');
        this.router.navigate(['/parcelas']);
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
