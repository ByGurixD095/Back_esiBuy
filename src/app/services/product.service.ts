import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { Producto } from '../models/producto.model';

export class ProductApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = 'ProductApiError';
  }
}

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/products/disponibles`;

  obtenerDisponibles(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.apiUrl, { withCredentials: true }).pipe(
      map((productos) => productos.filter((producto) => producto.activo && producto.numStock > 0)),
      catchError((error: HttpErrorResponse) => {
        return throwError(() => new ProductApiError(error.status, mensajeParaStatus(error)));
      }),
    );
  }

  // TODO: añadir aquí los parámetros/endpoints de paginación, búsqueda y filtros
  // cuando estén disponibles en el backend.
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
