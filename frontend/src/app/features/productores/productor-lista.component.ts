import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription, debounceTime } from 'rxjs';
import { Productor } from '../../core/models/models';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { ProductorService } from '../../core/services/productor.service';
import { ToastService } from '../../core/services/toast.service';
import { PaginadorComponent } from '../../shared/ui/paginador.component';

/** Listado de productores (tabla): GET /api/productores y GET /api/productores/buscar?texto= */
@Component({
  selector: 'app-productor-lista',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, PaginadorComponent],
  templateUrl: './productor-lista.component.html',
})
export class ProductorListaComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly service = inject(ProductorService);
  private readonly toast = inject(ToastService);
  private readonly confirmacion = inject(ConfirmService);

  readonly busqueda = new FormControl('', { nonNullable: true });
  readonly productores = signal<Productor[]>([]);
  readonly cargando = signal(true);
  readonly pagina = signal(1);
  readonly tamano = 8;
  readonly visibles = computed(() => {
    const inicio = (this.pagina() - 1) * this.tamano;
    return this.productores().slice(inicio, inicio + this.tamano);
  });

  private peticion?: Subscription;

  constructor() {
    this.busqueda.valueChanges.pipe(debounceTime(300), takeUntilDestroyed()).subscribe(() => {
      this.pagina.set(1);
      this.cargar();
    });
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.peticion?.unsubscribe();
    this.cargando.set(true);
    const texto = this.busqueda.value.trim();
    const fuente = texto ? this.service.buscarPorTexto(texto) : this.service.listar();

    this.peticion = fuente.subscribe({
      next: (lista) => {
        this.productores.set(lista);
        const ultimaPagina = Math.max(1, Math.ceil(lista.length / this.tamano));
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

  async eliminar(p: Productor): Promise<void> {
    const confirmado = await this.confirmacion.confirmar({
      titulo: 'Eliminar productor',
      mensaje: `¿Deseas eliminar a ${p.nombres} ${p.apellidos}? También se eliminarán sus parcelas y campañas asociadas.`,
      textoConfirmar: 'Eliminar',
    });
    if (!confirmado) {
      return;
    }
    this.service.eliminar(p.id).subscribe({
      next: () => {
        this.toast.exito('Productor eliminado correctamente.');
        this.cargar();
      },
      error: (err) => this.toast.desdeError(err),
    });
  }
}
