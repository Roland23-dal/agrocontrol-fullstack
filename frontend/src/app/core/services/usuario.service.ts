import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NombreRol, Usuario } from '../models/models';

/** Consume /api/usuarios (perfil propio y administración de usuarios: solo ADMIN) */
@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/usuarios`;

  perfil(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.url}/me`);
  }

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.url);
  }

  asignarRoles(id: number, roles: NombreRol[]): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.url}/${id}/roles`, { roles });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
