import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Campana, Parcela, Productor } from '../../core/models/models';
import { AuthService } from '../../core/services/auth.service';
import { CampanaService } from '../../core/services/campana.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { ParcelaService } from '../../core/services/parcela.service';
import { ProductorService } from '../../core/services/productor.service';
import { ToastService } from '../../core/services/toast.service';
import { EstadoBadgeComponent } from '../../shared/ui/estado-badge.component';

/** Detalle de parcela: datos, campañas históricas y transferencia a otro productor (PUT /transferir). */
@Component({
  selector: 'app-parcela-detalle',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, DecimalPipe, DatePipe, EstadoBadgeComponent],
  templateUrl: './parcela-detalle.component.html',
})
export class ParcelaDetalleComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(ParcelaService);
  private readonly campanaService = inject(CampanaService);
  private readonly productorService = inject(ProductorService);
  private readonly toast = inject(ToastService);
  private readonly confirmacion = inject(ConfirmService);

  readonly cargando = signal(true);
  readonly parcela = signal<Parcela | null>(null);
  readonly campanas = signal<Campana[]>([]);
  readonly productores = signal<Productor[]>([]);
  readonly transfiriendo = signal(false);

  readonly nuevoProductor = new FormControl<number | null>(null, Validators.required);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    forkJoin({
      parcela: this.service.obtener(id),
      campanas: this.campanaService.listarPorParcela(id),
      productores: this.productorService.listar(),
    }).subscribe({
      next: (r) => {
        this.parcela.set(r.parcela);
        this.campanas.set(r.campanas);
        this.productores.set(r.productores);
        this.cargando.set(false);
      },
      error: (err) => {
        this.toast.desdeError(err);
        this.router.navigate(['/parcelas']);
      },
    });
  }

  otrosProductores(): Productor[] {
    const actual = this.parcela()?.productor.id;
    return this.productores().filter((p) => p.id !== actual);
  }

  transferir(): void {
    const p = this.parcela();
    if (!p || this.nuevoProductor.invalid) {
      this.nuevoProductor.markAsTouched();
      return;
    }
    this.transfiriendo.set(true);
    this.service.transferir(p.id, Number(this.nuevoProductor.value)).subscribe({
      next: (actualizada) => {
        this.parcela.set(actualizada);
        this.nuevoProductor.reset(null);
        this.transfiriendo.set(false);
        this.toast.exito('Parcela transferida correctamente.');
      },
      error: (err) => {
        this.transfiriendo.set(false);
        this.toast.desdeError(err);
      },
    });
  }

  async eliminar(): Promise<void> {
    const p = this.parcela();
    if (!p) {
      return;
    }
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
        this.router.navigate(['/parcelas']);
      },
      error: (err) => this.toast.desdeError(err),
    });
  }
}
