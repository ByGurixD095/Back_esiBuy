import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { PaginaProducto } from '../models/producto.model';

export class ProductApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = 'ProductApiError';
  }
}

export interface FiltrosProductos {
  busqueda?: string;
  categoria?: string;
  precioMinCent?: number | null;
  precioMaxCent?: number | null;
  soloOfertas?: boolean;
  orden?: string;
}

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/products`;

  obtenerDisponibles(filtros: FiltrosProductos = {}, pagina = 0, tamanoPagina = 36): Observable<PaginaProducto> {
    let params = new HttpParams();

    if (filtros.busqueda?.trim()) params = params.set('busqueda', filtros.busqueda.trim());
    if (filtros.categoria && filtros.categoria !== 'Todas' && filtros.categoria !== 'TODAS') params = params.set('categoria', filtros.categoria);
    if (filtros.precioMinCent != null) params = params.set('precioMinCent', filtros.precioMinCent);
    if (filtros.precioMaxCent != null) params = params.set('precioMaxCent', filtros.precioMaxCent);
    if (filtros.soloOfertas) params = params.set('soloOfertas', true);
    if (filtros.orden && filtros.orden !== 'relevancia') params = params.set('orden', filtros.orden);
    params = params.set('page', pagina).set('size', tamanoPagina);

    return this.http.get<PaginaProducto>(`${this.apiUrl}/disponibles`, { params, withCredentials: true }).pipe(
      catchError((error: HttpErrorResponse) => {
        return throwError(() => new ProductApiError(error.status, mensajeParaStatus(error)));
      }),
    );
  }

  // TODO: añadir aquí paginación cuando el backend exponga sus parámetros.
}

function mensajeParaStatus(error: HttpErrorResponse): string {
  switch (error.status) {
    case 401:
      return 'Tu sesión no está autenticada o la cookie JWT no está disponible.';
    case 403:
      return 'Tu usuario está autenticado, pero no tiene permiso para ver el catálogo.';
    case 500:
      return 'El servidor no ha podido cargar el catálogo. Inténtalo de nuevo más tarde.';
    case 0:
      return 'No se puede conectar con el backend en http://localhost:8080.';
    default:
      return `No se ha podido cargar el catálogo (HTTP ${error.status}).`;
  }
}
