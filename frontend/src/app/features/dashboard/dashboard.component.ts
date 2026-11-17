import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Campana, Cosecha, ESTADOS_CAMPANA } from '../../core/models/models';
import { AuthService } from '../../core/services/auth.service';
import { CampanaService } from '../../core/services/campana.service';
import { CosechaService } from '../../core/services/cosecha.service';
import { CultivoService } from '../../core/services/cultivo.service';
import { ParcelaService } from '../../core/services/parcela.service';
import { ProductorService } from '../../core/services/productor.service';
import { ToastService } from '../../core/services/toast.service';
import { EstadoBadgeComponent } from '../../shared/ui/estado-badge.component';

interface BarraEstado {
  estado: string;
  cantidad: number;
  porcentaje: number;
}

interface DatosPanel {
  productores: number;
  parcelas: number;
  hectareas: number;
  cultivos: number;
  totalKg: number;
  barras: BarraEstado[];
  atrasadas: Campana[];
  ultimasCosechas: Cosecha[];
}

/** Panel general: indicadores obtenidos con varias peticiones GET en paralelo (forkJoin). */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, DecimalPipe, DatePipe, EstadoBadgeComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly productorService = inject(ProductorService);
  private readonly parcelaService = inject(ParcelaService);
  private readonly cultivoService = inject(CultivoService);
  private readonly campanaService = inject(CampanaService);
  private readonly cosechaService = inject(CosechaService);
  private readonly toast = inject(ToastService);

  readonly cargando = signal(true);
  readonly error = signal('');
  readonly datos = signal<DatosPanel | null>(null);

  ngOnInit(): void {
    forkJoin({
      resumen: this.campanaService.resumen(),
      productores: this.productorService.listar(),
      parcelas: this.parcelaService.listar(),
      cultivos: this.cultivoService.listar(),
      atrasadas: this.campanaService.listarAtrasadas(),
      cosechas: this.cosechaService.listar(),
    }).subscribe({
      next: (r) => {
        const maximo = Math.max(1, ...ESTADOS_CAMPANA.map((e) => r.resumen[e] ?? 0));
        this.datos.set({
          productores: r.productores.length,
          parcelas: r.parcelas.length,
          hectareas: r.parcelas.reduce((suma, p) => suma + (p.areaHectareas ?? 0), 0),
          cultivos: r.cultivos.length,
          totalKg: r.cosechas.reduce((suma, c) => suma + c.cantidadKg, 0),
          barras: ESTADOS_CAMPANA.map((estado) => ({
            estado,
            cantidad: r.resumen[estado] ?? 0,
            porcentaje: ((r.resumen[estado] ?? 0) / maximo) * 100,
          })),
          atrasadas: r.atrasadas,
          ultimasCosechas: [...r.cosechas].sort((a, b) => b.fecha.localeCompare(a.fecha)).slice(0, 5),
        });
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set('No se pudieron cargar los indicadores.');
        this.toast.desdeError(err);
      },
    });
  }
}
