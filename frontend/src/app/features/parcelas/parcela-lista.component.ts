import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription, debounceTime } from 'rxjs';
import { Parcela, Productor } from '../../core/models/models';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { ParcelaService } from '../../core/services/parcela.service';
import { ProductorService } from '../../core/services/productor.service';
import { ToastService } from '../../core/services/toast.service';
import { PaginadorComponent } from '../../shared/ui/paginador.component';

/**
 * Listado de parcelas con filtros. Según el filtro usado se consulta el endpoint
 * correspondiente (/buscar, /area-minima, ?productorId=) y el resto se aplica en pantalla.
 */
@Component({
  selector: 'app-parcela-lista',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, DecimalPipe, PaginadorComponent],
  templateUrl: './parcela-lista.component.html',
})
export class ParcelaListaComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly service = inject(ParcelaService);
  private readonly productorService = inject(ProductorService);
  private readonly toast = inject(ToastService);
  private readonly confirmacion = inject(ConfirmService);

  readonly filtros = new FormGroup({
    ubicacion: new FormControl('', { nonNullable: true }),
    areaMinima: new FormControl<number | null>(null),
    productorId: new FormControl<number | null>(null),
  });

  readonly parcelas = signal<Parcela[]>([]);
  readonly productores = signal<Productor[]>([]);
  readonly cargando = signal(true);
  readonly pagina = signal(1);
  readonly tamano = 8;
  readonly visibles = computed(() => {
    const inicio = (this.pagina() - 1) * this.tamano;
    return this.parcelas().slice(inicio, inicio + this.tamano);
  });
  readonly areaTotal = computed(() => this.parcelas().reduce((s, p) => s + (p.areaHectareas ?? 0), 0));

  private peticion?: Subscription;

  constructor() {
    this.filtros.valueChanges.pipe(debounceTime(300), takeUntilDestroyed()).subscribe(() => {
      this.pagina.set(1);
      this.cargar();
    });
  }

  ngOnInit(): void {
    this.productorService.listar().subscribe({
      next: (lista) => this.productores.set(lista),
      error: (err) => this.toast.desdeError(err),
    });
    this.cargar();
  }

  cargar(): void {
    this.peticion?.unsubscribe();
    this.cargando.set(true);
    const { ubicacion, areaMinima, productorId } = this.filtros.getRawValue();
    const texto = ubicacion.trim();

    let fuente;
    if (texto) {
      fuente = this.service.buscarPorUbicacion(texto);
    } else if (areaMinima !== null && areaMinima > 0) {
      fuente = this.service.listarPorAreaMinima(areaMinima);
    } else if (productorId !== null) {
      fuente = this.service.listarPorProductor(productorId);
    } else {
      fuente = this.service.listar();
    }

    this.peticion = fuente.subscribe({
      next: (lista) => {
        const filtradas = lista.filter(
          (p) =>
            (areaMinima === null || areaMinima <= 0 || p.areaHectareas >= areaMinima) &&
            (productorId === null || p.productor.id === productorId),
        );
        this.parcelas.set(filtradas);
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
    this.filtros.reset({ ubicacion: '', areaMinima: null, productorId: null });
  }

  async eliminar(p: Parcela): Promise<void> {
    const confirmado = await this.confirmacion.confirmar({
      titulo: 'Eliminar parcela',
      mensaje: `¿Deseas eliminar la parcela "${p.nombre}"? También se eliminarán sus campañas y cosechas.`,
      textoConfirmar: 'Eliminar',
    });
    if (!confirmado) {
      return;
    }
    this.service.eliminar(p.id).subscribe({
      next: () => {
        this.toast.exito('Parcela eliminada correctamente.');
        this.cargar();
      },
      error: (err) => this.toast.desdeError(err),
    });
  }
}
