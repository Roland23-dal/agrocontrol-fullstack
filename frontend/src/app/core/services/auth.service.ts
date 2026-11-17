import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  LoginRequest,
  NombreRol,
  RegisterRequest,
  Sesion,
} from '../models/models';

const CLAVE_SESION = 'agrocontrol.sesion';

/**
 * Autenticación con JWT:
 *  - login/registro contra /api/auth/**
 *  - guarda el token en localStorage (sobrevive a recargas de la página)
 *  - expone señales para saber quién está autenticado y con qué rol
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly url = `${environment.apiUrl}/auth`;

  private readonly _sesion = signal<Sesion | null>(this.leerSesionGuardada());

  readonly sesion = this._sesion.asReadonly();
  readonly token = computed(() => this._sesion()?.token ?? null);
  readonly username = computed(() => this._sesion()?.username ?? '');
  readonly roles = computed<NombreRol[]>(() => this._sesion()?.roles ?? []);

  // Permisos según la matriz de la API: lectura (todos), escritura (ADMIN/USER), eliminación (ADMIN)
  readonly esAdmin = computed(() => this.roles().includes('ADMIN'));
  readonly puedeEscribir = computed(() => this.roles().some((r) => r === 'ADMIN' || r === 'USER'));
  readonly puedeEliminar = computed(() => this.esAdmin());

  login(datos: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.url}/login`, datos)
      .pipe(tap((resp) => this.guardarSesion(resp)));
  }

  registrar(datos: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.url}/register`, datos)
      .pipe(tap((resp) => this.guardarSesion(resp)));
  }

  /** Cierra la sesión y borra el token. Si navegar=true, vuelve al login. */
  logout(navegar = true): void {
    localStorage.removeItem(CLAVE_SESION);
    this._sesion.set(null);
    if (navegar) {
      this.router.navigate(['/login']);
    }
  }

  /** True si hay un token guardado y todavía no venció (si venció, cierra la sesión). */
  estaAutenticado(): boolean {
    const sesion = this._sesion();
    if (!sesion) {
      return false;
    }
    if (Date.now() >= sesion.expiraEn) {
      this.logout(false);
      return false;
    }
    return true;
  }

  tieneAlgunRol(roles: NombreRol[]): boolean {
    return this.roles().some((r) => roles.includes(r));
  }

  /** Redirección inicial según el tipo de usuario autenticado. */
  rutaInicio(): string {
    const roles = this.roles();
    if (roles.includes('ADMIN')) {
      return '/dashboard'; // administrador: panel con indicadores y gestión de usuarios
    }
    if (roles.includes('USER')) {
      return '/campanas'; // técnico/productor: trabaja directamente con sus campañas
    }
    return '/dashboard'; // invitado: panel de consulta (solo lectura)
  }

  private guardarSesion(resp: AuthResponse): void {
    const sesion: Sesion = {
      token: resp.token,
      username: resp.username,
      roles: resp.roles.map((r) => r.replace('ROLE_', '') as NombreRol),
      expiraEn: Date.now() + resp.expiraEnMs,
    };
    localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
    this._sesion.set(sesion);
  }

  private leerSesionGuardada(): Sesion | null {
    try {
      const texto = localStorage.getItem(CLAVE_SESION);
      if (!texto) {
        return null;
      }
      const sesion = JSON.parse(texto) as Sesion;
      return sesion.token && Date.now() < sesion.expiraEn ? sesion : null;
    } catch {
      return null;
    }
  }
}
