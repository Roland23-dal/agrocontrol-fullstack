import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cosecha, CosechaRequest } from '../models/models';

/** Consume /api/cosechas */
@Injectable({ providedIn: 'root' })
export class CosechaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/cosechas`;

  listar(): Observable<Cosecha[]> {
    return this.http.get<Cosecha[]>(this.url);
  }

  listarPorCampana(campanaId: number): Observable<Cosecha[]> {
    return this.http.get<Cosecha[]>(this.url, { params: new HttpParams().set('campanaId', campanaId) });
  }

  listarPorFechas(desde: string, hasta: string): Observable<Cosecha[]> {
    return this.http.get<Cosecha[]>(`${this.url}/fechas`, { params: new HttpParams().set('desde', desde).set('hasta', hasta) });
  }

  totalPorCampana(campanaId: number): Observable<{ campanaId: number; totalKg: number }> {
    return this.http.get<{ campanaId: number; totalKg: number }>(`${this.url}/campana/${campanaId}/total`);
  }

  obtener(id: number): Observable<Cosecha> {
    return this.http.get<Cosecha>(`${this.url}/${id}`);
  }

  crear(datos: CosechaRequest): Observable<Cosecha> {
    return this.http.post<Cosecha>(this.url, datos);
  }

  actualizar(id: number, datos: CosechaRequest): Observable<Cosecha> {
    return this.http.put<Cosecha>(`${this.url}/${id}`, datos);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
