import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Campana, CampanaRequest, Cosecha, CosechaRequest, EstadoCampana } from '../models/models';

/** Consume /api/campanas (CRUD, consultas JPQL y finalización transaccional) */
@Injectable({ providedIn: 'root' })
export class CampanaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/campanas`;

  listar(): Observable<Campana[]> {
    return this.http.get<Campana[]>(this.url);
  }

  listarPorParcela(parcelaId: number): Observable<Campana[]> {
    return this.http.get<Campana[]>(this.url, { params: new HttpParams().set('parcelaId', parcelaId) });
  }

  listarPorEstado(estado: EstadoCampana): Observable<Campana[]> {
    return this.http.get<Campana[]>(`${this.url}/estado/${estado}`);
  }

  listarPorFechas(desde: string, hasta: string): Observable<Campana[]> {
    return this.http.get<Campana[]>(`${this.url}/fechas`, { params: new HttpParams().set('desde', desde).set('hasta', hasta) });
  }

  listarPorCultivo(nombre: string): Observable<Campana[]> {
    return this.http.get<Campana[]>(`${this.url}/cultivo`, { params: new HttpParams().set('nombre', nombre) });
  }

  listarAtrasadas(): Observable<Campana[]> {
    return this.http.get<Campana[]>(`${this.url}/atrasadas`);
  }

  resumen(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.url}/resumen`);
  }

  obtener(id: number): Observable<Campana> {
    return this.http.get<Campana>(`${this.url}/${id}`);
  }

  crear(datos: CampanaRequest): Observable<Campana> {
    return this.http.post<Campana>(this.url, datos);
  }

  actualizar(id: number, datos: CampanaRequest): Observable<Campana> {
    return this.http.put<Campana>(`${this.url}/${id}`, datos);
  }

  /** Registra la cosecha y pasa la campaña a FINALIZADA en una sola transacción (backend). */
  finalizar(id: number, datos: CosechaRequest): Observable<Cosecha> {
    return this.http.post<Cosecha>(`${this.url}/${id}/finalizar`, datos);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
