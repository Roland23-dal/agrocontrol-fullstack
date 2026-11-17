import { Component, OnInit, inject, signal } from '@angular/core';
import { NombreRol, ROLES, Usuario } from '../../core/models/models';
import { AuthService } from '../../core/services/auth.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { ToastService } from '../../core/services/toast.service';
import { UsuarioService } from '../../core/services/usuario.service';

/** Administración de usuarios (solo ADMIN): GET /api/usuarios, PUT /{id}/roles y DELETE /{id}. */
@Component({
  selector: 'app-usuario-lista',
  standalone: true,
  templateUrl: './usuario-lista.component.html',
})
export class UsuarioListaComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly service = inject(UsuarioService);
  private readonly toast = inject(ToastService);
  private readonly confirmacion = inject(ConfirmService);

  readonly roles = ROLES;
  readonly usuarios = signal<Usuario[]>([]);
  readonly cargando = signal(true);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.service.listar().subscribe({
      next: (lista) => {
        this.usuarios.set(lista);
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.desdeError(err);
      },
    });
  }

  esYo(u: Usuario): boolean {
    return u.username === this.auth.username();
  }

  rolActual(u: Usuario): NombreRol {
    return (u.roles[0] ?? 'ROLE_GUEST').replace('ROLE_', '') as NombreRol;
  }

  cambiarRol(u: Usuario, rol: string): void {
    this.service.asignarRoles(u.id, [rol as NombreRol]).subscribe({
      next: (actualizado) => {
        this.usuarios.update((lista) => lista.map((x) => (x.id === actualizado.id ? actualizado : x)));
        this.toast.exito(`Rol de ${u.username} actualizado a ${rol}.`);
      },
      error: (err) => {
        this.toast.desdeError(err);
        this.cargar(); // restaura el valor mostrado si falló
      },
    });
  }

  async eliminar(u: Usuario): Promise<void> {
    const confirmado = await this.confirmacion.confirmar({
      titulo: 'Eliminar usuario',
      mensaje: `¿Deseas eliminar al usuario "${u.username}"? Esta acción no se puede deshacer.`,
      textoConfirmar: 'Eliminar',
    });
    if (!confirmado) {
      return;
    }
    this.service.eliminar(u.id).subscribe({
      next: () => {
        this.toast.exito('Usuario eliminado correctamente.');
        this.cargar();
      },
      error: (err) => this.toast.desdeError(err),
    });
  }
}
