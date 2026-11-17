import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Parcela, Productor } from '../../core/models/models';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { ParcelaService } from '../../core/services/parcela.service';
import { ProductorService } from '../../core/services/productor.service';
import { ToastService } from '../../core/services/toast.service';

/** Detalle de un productor con sus parcelas y el área total (JPQL SUM). */
@Component({
  selector: 'app-productor-detalle',
  standalone: true,
  imports: [RouterLink, DecimalPipe],
  templateUrl: './productor-detalle.component.html',
})
export class ProductorDetalleComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productorService = inject(ProductorService);
  private readonly parcelaService = inject(ParcelaService);
  private readonly toast = inject(ToastService);
  private readonly confirmacion = inject(ConfirmService);

  readonly cargando = signal(true);
  readonly productor = signal<Productor | null>(null);
  readonly parcelas = signal<Parcela[]>([]);
  readonly areaTotal = signal(0);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    forkJoin({
      productor: this.productorService.obtener(id),
      parcelas: this.parcelaService.listarPorProductor(id),
      area: this.parcelaService.areaTotalPorProductor(id),
    }).subscribe({
      next: (r) => {
        this.productor.set(r.productor);
        this.parcelas.set(r.parcelas);
        this.areaTotal.set(r.area.areaTotalHectareas ?? 0);
        this.cargando.set(false);
      },
      error: (err) => {
        this.toast.desdeError(err);
        this.router.navigate(['/productores']);
      },
    });
  }

  async eliminar(): Promise<void> {
    const p = this.productor();
    if (!p) {
      return;
    }
    const confirmado = await this.confirmacion.confirmar({
      titulo: 'Eliminar productor',
      mensaje: `¿Deseas eliminar a ${p.nombres} ${p.apellidos}? También se eliminarán sus parcelas y campañas asociadas.`,
      textoConfirmar: 'Eliminar',
    });
    if (!confirmado) {
      return;
    }
    this.productorService.eliminar(p.id).subscribe({
      next: () => {
        this.toast.exito('Productor eliminado correctamente.');
        this.router.navigate(['/productores']);
      },
      error: (err) => this.toast.desdeError(err),
    });
  }
}
