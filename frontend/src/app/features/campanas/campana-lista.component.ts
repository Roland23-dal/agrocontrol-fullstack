import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription, debounceTime } from 'rxjs';
import { Campana, ESTADOS_CAMPANA, EstadoCampana } from '../../core/models/models';
import { AuthService } from '../../core/services/auth.service';
import { CampanaService } from '../../core/services/campana.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { ToastService } from '../../core/services/toast.service';
import { EstadoBadgeComponent } from '../../shared/ui/estado-badge.component';
import { PaginadorComponent } from '../../shared/ui/paginador.component';

/**
 * Listado de campañas con filtros que consumen las consultas JPQL del backend:
 * /estado/{estado}, /fechas, /cultivo?nombre= y /atrasadas.
 */
@Component({
  selector: 'app-campana-lista',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, DatePipe, EstadoBadgeComponent, PaginadorComponent],
  templateUrl: './campana-lista.component.html',
})
export class CampanaListaComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly service = inject(CampanaService);
  private readonly toast = inject(ToastService);
  private readonly confirmacion = inject(ConfirmService);

  readonly estados = ESTADOS_CAMPANA;
  readonly hoy = new Date().toISOString().slice(0, 10);

  readonly filtros = new FormGroup({
    estado: new FormControl<EstadoCampana | ''>('', { nonNullable: true }),
    cultivo: new FormControl('', { nonNullable: true }),
    desde: new FormControl('', { nonNullable: true }),
    hasta: new FormControl('', { nonNullable: true }),
    soloAtrasadas: new FormControl(false, { nonNullable: true }),
  });

  readonly campanas = signal<Campana[]>([]);
  readonly cargando = signal(true);
  readonly pagina = signal(1);
  readonly tamano = 8;
  readonly visibles = computed(() => {
    const inicio = (this.pagina() - 1) * this.tamano;
    return this.campanas().slice(inicio, inicio + this.tamano);
  });

  private peticion?: Subscription;

  constructor() {
    this.filtros.valueChanges.pipe(debounceTime(300), takeUntilDestroyed()).subscribe(() => {
      this.pagina.set(1);
      this.cargar();
    });
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    const { estado, cultivo, desde, hasta, soloAtrasadas } = this.filtros.getRawValue();
    const texto = cultivo.trim();

    if (desde && hasta && desde > hasta) {
      this.toast.error('La fecha "desde" no puede ser posterior a la fecha "hasta".');
      return;
    }

    this.peticion?.unsubscribe();
    this.cargando.set(true);

    let fuente;
    if (soloAtrasadas) {
      fuente = this.service.listarAtrasadas();
    } else if (estado) {
      fuente = this.service.listarPorEstado(estado);
    } else if (desde && hasta) {
      fuente = this.service.listarPorFechas(desde, hasta);
    } else if (texto) {
      fuente = this.service.listarPorCultivo(texto);
    } else {
      fuente = this.service.listar();
    }

    this.peticion = fuente.subscribe({
      next: (lista) => {
        const filtradas = lista.filter(
          (c) =>
            (!estado || c.estado === estado) &&
            (!texto || c.cultivo.nombre.toLowerCase().includes(texto.toLowerCase())) &&
            (!desde || !hasta || (c.fechaInicio >= desde && c.fechaInicio <= hasta)),
        );
        this.campanas.set(filtradas);
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

  esAtrasada(c: Campana): boolean {
    return c.estado === 'EN_CURSO' && !!c.fechaFinEstimada && c.fechaFinEstimada < this.hoy;
  }

  limpiar(): void {
    this.filtros.reset({ estado: '', cultivo: '', desde: '', hasta: '', soloAtrasadas: false });
  }

  async eliminar(c: Campana): Promise<void> {
    const confirmado = await this.confirmacion.confirmar({
      titulo: 'Eliminar campaña',
      mensaje: `¿Deseas eliminar la campaña de ${c.cultivo.nombre} en "${c.parcela.nombre}"? También se eliminarán sus cosechas.`,
      textoConfirmar: 'Eliminar',
    });
    if (!confirmado) {
      return;
    }
    this.service.eliminar(c.id).subscribe({
      next: () => {
        this.toast.exito('Campaña eliminada correctamente.');
        this.cargar();
      },
      error: (err) => this.toast.desdeError(err),
    });
  }
}
