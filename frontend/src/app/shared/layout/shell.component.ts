import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UsuarioService } from '../../core/services/usuario.service';

interface EnlaceMenu {
  ruta: string;
  texto: string;
  soloAdmin?: boolean;
}

/**
 * Estructura de la aplicación autenticada: menú lateral (sidebar) + contenido.
 * En pantallas pequeñas el menú se oculta y se abre con un botón (diseño responsivo).
 */
@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly usuarioService = inject(UsuarioService);

  readonly menuAbierto = signal(false);
  readonly nombreCompleto = signal('');

  readonly enlaces: EnlaceMenu[] = [
    { ruta: '/dashboard', texto: 'Panel general' },
    { ruta: '/productores', texto: 'Productores' },
    { ruta: '/parcelas', texto: 'Parcelas' },
    { ruta: '/cultivos', texto: 'Cultivos' },
    { ruta: '/campanas', texto: 'Campañas' },
    { ruta: '/cosechas', texto: 'Cosechas' },
    { ruta: '/usuarios', texto: 'Usuarios', soloAdmin: true },
  ];

  ngOnInit(): void {
    // GET /api/usuarios/me: datos del usuario autenticado (cualquier rol)
    this.usuarioService.perfil().subscribe({
      next: (u) => this.nombreCompleto.set(u.nombreCompleto),
      error: () => this.nombreCompleto.set(this.auth.username()),
    });
  }

  visibles(): EnlaceMenu[] {
    return this.enlaces.filter((e) => !e.soloAdmin || this.auth.esAdmin());
  }

  rolPrincipal(): string {
    const roles = this.auth.roles();
    return roles.includes('ADMIN') ? 'Administrador' : roles.includes('USER') ? 'Técnico de campo' : 'Invitado (solo lectura)';
  }

  alternarMenu(): void {
    this.menuAbierto.update((v) => !v);
  }

  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }

  salir(): void {
    this.auth.logout();
  }
}
