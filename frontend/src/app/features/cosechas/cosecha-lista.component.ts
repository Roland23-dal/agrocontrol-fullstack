import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription, debounceTime } from 'rxjs';
import { Campana, Cosecha } from '../../core/models/models';
import { AuthService } from '../../core/services/auth.service';
import { CampanaService } from '../../core/services/campana.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { CosechaService } from '../../core/services/cosecha.service';
import { ToastService } from '../../core/services/toast.service';
import { PaginadorComponent } from '../../shared/ui/paginador.component';

/** Listado de cosechas: GET /api/cosechas, ?campanaId= y /fechas?desde=&hasta= (JPQL). */
@Component({
  selector: 'app-cosecha-lista',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, DatePipe, DecimalPipe, PaginadorComponent],
  templateUrl: './cosecha-lista.component.html',
})
export class CosechaListaComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly service = inject(CosechaService);
  private readonly campanaService = inject(CampanaService);
  private readonly toast = inject(ToastService);
  private readonly confirmacion = inject(ConfirmService);

  readonly filtros = new FormGroup({
    campanaId: new FormControl<number | null>(null),
    desde: new FormControl('', { nonNullable: true }),
    hasta: new FormControl('', { nonNullable: true }),
  });

  readonly cosechas = signal<Cosecha[]>([]);
  readonly campanas = signal<Campana[]>([]);
  readonly cargando = signal(true);
  readonly pagina = signal(1);
  readonly tamano = 8;
  readonly visibles = computed(() => {
    const inicio = (this.pagina() - 1) * this.tamano;
    return this.cosechas().slice(inicio, inicio + this.tamano);
  });
  readonly totalKg = computed(() => this.cosechas().reduce((s, c) => s + c.cantidadKg, 0));

  private peticion?: Subscription;

  constructor() {
    this.filtros.valueChanges.pipe(debounceTime(300), takeUntilDestroyed()).subscribe(() => {
      this.pagina.set(1);
      this.cargar();
    });
  }

  ngOnInit(): void {
    this.campanaService.listar().subscribe({
      next: (lista) => this.campanas.set(lista),
      error: (err) => this.toast.desdeError(err),
    });
    this.cargar();
  }

  cargar(): void {
    const { campanaId, desde, hasta } = this.filtros.getRawValue();
    if (desde && hasta && desde > hasta) {
      this.toast.error('La fecha "desde" no puede ser posterior a la fecha "hasta".');
      return;
    }
    this.peticion?.unsubscribe();
    this.cargando.set(true);

    let fuente;
    if (desde && hasta) {
      fuente = this.service.listarPorFechas(desde, hasta);
    } else if (campanaId !== null) {
      fuente = this.service.listarPorCampana(campanaId);
    } else {
      fuente = this.service.listar();
    }

    this.peticion = fuente.subscribe({
      next: (lista) => {
        const filtradas = lista.filter((c) => campanaId === null || c.campana.id === campanaId);
        this.cosechas.set(filtradas);
        const ultimaPagina = Math.max(1, Math.ceil(filtradas.length / this.tamano));
        if (this.pagina() > ultimaPagina) {
          this.pagina.set(ultimaPagina);
        }
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.desdeError(err);
      },
    });
  }

  limpiar(): void {
    this.filtros.reset({ campanaId: null, desde: '', hasta: '' });
  }

  async eliminar(c: Cosecha): Promise<void> {
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
        this.cargar();
      },
      error: (err) => this.toast.desdeError(err),
    });
  }
}
