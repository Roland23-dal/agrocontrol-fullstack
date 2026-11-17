import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Cultivo } from '../../core/models/models';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { CultivoService } from '../../core/services/cultivo.service';
import { ToastService } from '../../core/services/toast.service';

/** Catálogo de cultivos mostrado como tarjetas (GET /api/cultivos). */
@Component({
  selector: 'app-cultivo-lista',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './cultivo-lista.component.html',
})
export class CultivoListaComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly service = inject(CultivoService);
  private readonly toast = inject(ToastService);
  private readonly confirmacion = inject(ConfirmService);

  readonly busqueda = new FormControl('', { nonNullable: true });
  readonly textoBusqueda = signal('');
  readonly cultivos = signal<Cultivo[]>([]);
  readonly cargando = signal(true);
  readonly filtrados = computed(() => {
    const t = this.textoBusqueda().trim().toLowerCase();
    return t ? this.cultivos().filter((c) => `${c.nombre} ${c.variedad ?? ''}`.toLowerCase().includes(t)) : this.cultivos();
  });

  ngOnInit(): void {
    this.busqueda.valueChanges.subscribe((v) => this.textoBusqueda.set(v));
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.service.listar().subscribe({
      next: (lista) => {
        this.cultivos.set(lista);
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.desdeError(err);
      },
    });
  }

  async eliminar(c: Cultivo): Promise<void> {
    const confirmado = await this.confirmacion.confirmar({
      titulo: 'Eliminar cultivo',
      mensaje: `¿Deseas eliminar el cultivo "${c.nombre}"${c.variedad ? ' (' + c.variedad + ')' : ''} del catálogo?`,
      textoConfirmar: 'Eliminar',
    });
    if (!confirmado) {
      return;
    }
    this.service.eliminar(c.id).subscribe({
      next: () => {
        this.toast.exito('Cultivo eliminado correctamente.');
        this.cargar();
      },
      error: (err) => {
        if (err instanceof HttpErrorResponse && err.status === 400) {
          this.toast.error('No se puede eliminar el cultivo porque tiene campañas agrícolas asociadas.');
        } else {
          this.toast.desdeError(err);
        }
      },
    });
  }
}
