import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Parcela, ParcelaRequest } from '../models/models';

/** Consume /api/parcelas (CRUD, filtros JPQL y transferencia transaccional) */
@Injectable({ providedIn: 'root' })
export class ParcelaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/parcelas`;

  listar(): Observable<Parcela[]> {
    return this.http.get<Parcela[]>(this.url);
  }

  listarPorProductor(productorId: number): Observable<Parcela[]> {
    return this.http.get<Parcela[]>(this.url, { params: new HttpParams().set('productorId', productorId) });
  }

  buscarPorUbicacion(ubicacion: string): Observable<Parcela[]> {
    return this.http.get<Parcela[]>(`${this.url}/buscar`, { params: new HttpParams().set('ubicacion', ubicacion) });
  }

  listarPorAreaMinima(hectareas: number): Observable<Parcela[]> {
    return this.http.get<Parcela[]>(`${this.url}/area-minima`, { params: new HttpParams().set('hectareas', hectareas) });
  }

  areaTotalPorProductor(productorId: number): Observable<{ productorId: number; areaTotalHectareas: number }> {
    return this.http.get<{ productorId: number; areaTotalHectareas: number }>(
      `${this.url}/productor/${productorId}/area-total`,
    );
  }

  obtener(id: number): Observable<Parcela> {
    return this.http.get<Parcela>(`${this.url}/${id}`);
  }

  crear(datos: ParcelaRequest): Observable<Parcela> {
    return this.http.post<Parcela>(this.url, datos);
  }

  actualizar(id: number, datos: ParcelaRequest): Observable<Parcela> {
    return this.http.put<Parcela>(`${this.url}/${id}`, datos);
  }

  transferir(id: number, nuevoProductorId: number): Observable<Parcela> {
    return this.http.put<Parcela>(`${this.url}/${id}/transferir`, { nuevoProductorId });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
