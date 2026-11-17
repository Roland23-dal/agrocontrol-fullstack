import { Component, OnInit, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CampanaRequest, Cultivo, ESTADOS_CAMPANA, EstadoCampana, Parcela } from '../../core/models/models';
import { CampanaService } from '../../core/services/campana.service';
import { CultivoService } from '../../core/services/cultivo.service';
import { ParcelaService } from '../../core/services/parcela.service';
import { ToastService } from '../../core/services/toast.service';
import { aplicarErroresServidor, obtenerMensajeError } from '../../core/utils/http-error';
import { FieldErrorComponent } from '../../shared/ui/field-error.component';

/** Validador de grupo: la fecha de fin estimada no puede ser anterior a la de inicio. */
function rangoFechasValido(grupo: AbstractControl): ValidationErrors | null {
  const inicio = grupo.get('fechaInicio')?.value as string;
  const fin = grupo.get('fechaFinEstimada')?.value as string;
  return inicio && fin && fin < inicio ? { rangoFechas: true } : null;
}

/** Formulario reactivo de campaña (POST / PUT). Admite ?parcelaId= para preseleccionar la parcela. */
@Component({
  selector: 'app-campana-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, FieldErrorComponent],
  templateUrl: './campana-form.component.html',
})
export class CampanaFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(CampanaService);
  private readonly parcelaService = inject(ParcelaService);
  private readonly cultivoService = inject(CultivoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly estados = ESTADOS_CAMPANA;
  readonly id = signal<number | null>(null);
  readonly cargando = signal(true);
  readonly enviando = signal(false);
  readonly error = signal('');
  readonly parcelas = signal<Parcela[]>([]);
  readonly cultivos = signal<Cultivo[]>([]);

  readonly form = this.fb.nonNullable.group(
    {
      parcelaId: [null as number | null, [Validators.required]],
      cultivoId: [null as number | null, [Validators.required]],
      fechaInicio: ['', [Validators.required]],
      fechaFinEstimada: [''],
      estado: ['PLANIFICADA' as EstadoCampana, [Validators.required]],
    },
    { validators: rangoFechasValido },
  );

  get f() {
    return this.form.controls;
  }

  get esEdicion(): boolean {
    return this.id() !== null;
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const parcelaParam = this.route.snapshot.queryParamMap.get('parcelaId');
    if (idParam) {
      this.id.set(Number(idParam));
    }
    if (parcelaParam) {
      this.form.patchValue({ parcelaId: Number(parcelaParam) });
    }

    forkJoin({ parcelas: this.parcelaService.listar(), cultivos: this.cultivoService.listar() }).subscribe({
      next: (r) => {
        this.parcelas.set(r.parcelas);
        this.cultivos.set(r.cultivos);
        if (idParam) {
          this.cargarCampana(Number(idParam));
        } else {
          this.cargando.set(false);
        }
      },
      error: (err) => {
        this.toast.desdeError(err);
        this.router.navigate(['/campanas']);
      },
    });
  }

  private cargarCampana(id: number): void {
    this.service.obtener(id).subscribe({
      next: (c) => {
        this.form.patchValue({
          parcelaId: c.parcela.id,
          cultivoId: c.cultivo.id,
          fechaInicio: c.fechaInicio,
          fechaFinEstimada: c.fechaFinEstimada ?? '',
          estado: c.estado,
        });
        // El backend solo actualiza fechas y estado: parcela y cultivo no se pueden cambiar al editar
        this.form.controls.parcelaId.disable();
        this.form.controls.cultivoId.disable();
        this.cargando.set(false);
      },
      error: (err) => {
        this.toast.desdeError(err);
        this.router.navigate(['/campanas']);
      },
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const datos: CampanaRequest = {
      fechaInicio: v.fechaInicio,
      fechaFinEstimada: v.fechaFinEstimada || null,
      estado: v.estado,
      cultivo: { id: Number(v.cultivoId) },
      parcela: { id: Number(v.parcelaId) },
    };

    this.enviando.set(true);
    this.error.set('');
    const idActual = this.id();
    const peticion = idActual === null ? this.service.crear(datos) : this.service.actualizar(idActual, datos);

    peticion.subscribe({
      next: () => {
        this.toast.exito(idActual === null ? 'Campaña registrada correctamente.' : 'Campaña actualizada correctamente.');
        this.router.navigate(['/campanas']);
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
