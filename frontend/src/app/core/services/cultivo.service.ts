import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cultivo, CultivoRequest } from '../models/models';

/** Consume /api/cultivos (catálogo de cultivos) */
@Injectable({ providedIn: 'root' })
export class CultivoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/cultivos`;

  listar(): Observable<Cultivo[]> {
    return this.http.get<Cultivo[]>(this.url);
  }

  obtener(id: number): Observable<Cultivo> {
    return this.http.get<Cultivo>(`${this.url}/${id}`);
  }

  crear(datos: CultivoRequest): Observable<Cultivo> {
    return this.http.post<Cultivo>(this.url, datos);
  }

  actualizar(id: number, datos: CultivoRequest): Observable<Cultivo> {
    return this.http.put<Cultivo>(`${this.url}/${id}`, datos);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
