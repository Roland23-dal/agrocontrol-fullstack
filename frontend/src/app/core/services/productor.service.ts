import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Productor, ProductorRequest } from '../models/models';

/** Consume /api/productores (GET, POST, PUT, DELETE + búsqueda JPQL) */
@Injectable({ providedIn: 'root' })
export class ProductorService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/productores`;

  listar(): Observable<Productor[]> {
    return this.http.get<Productor[]>(this.url);
  }

  buscarPorTexto(texto: string): Observable<Productor[]> {
    return this.http.get<Productor[]>(`${this.url}/buscar`, { params: new HttpParams().set('texto', texto) });
  }

  obtener(id: number): Observable<Productor> {
    return this.http.get<Productor>(`${this.url}/${id}`);
  }

  crear(datos: ProductorRequest): Observable<Productor> {
    return this.http.post<Productor>(this.url, datos);
  }

  actualizar(id: number, datos: ProductorRequest): Observable<Productor> {
    return this.http.put<Productor>(`${this.url}/${id}`, datos);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
