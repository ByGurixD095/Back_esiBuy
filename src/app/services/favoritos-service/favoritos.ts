import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Producto } from '../../models/producto.model';

export type AccionFavorito = 'cargar' | 'marcar' | 'quitar';

/** Error del API de favoritos con un mensaje ya pensado para mostrar al cliente. */
export class FavoritosApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = 'FavoritosApiError';
  }
}

/** Acceso al API de favoritos del cliente (solo rol CLIENTE en el back). */
@Injectable({ providedIn: 'root' })
export class Favoritos {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/favoritos`;

  /** Productos activos que el cliente tiene guardados, los más recientes primero. */
  listar(): Observable<Producto[]> {
    return this.http
      .get<Producto[]>(this.apiUrl, { withCredentials: true })
      .pipe(catchError((error: HttpErrorResponse) => throwError(() => this.convertir(error, 'cargar'))));
  }

  /** Guarda el producto como favorito (idempotente en el back). */
  marcar(productoId: string): Observable<void> {
    return this.http
      .post<void>(`${this.apiUrl}/${encodeURIComponent(productoId)}`, null, { withCredentials: true })
      .pipe(catchError((error: HttpErrorResponse) => throwError(() => this.convertir(error, 'marcar'))));
  }

  /** Quita el producto de favoritos (idempotente en el back). */
  quitar(productoId: string): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrl}/${encodeURIComponent(productoId)}`, { withCredentials: true })
      .pipe(catchError((error: HttpErrorResponse) => throwError(() => this.convertir(error, 'quitar'))));
  }

  private convertir(error: HttpErrorResponse, accion: AccionFavorito): FavoritosApiError {
    return new FavoritosApiError(error.status, mensajeParaError(error.status, accion));
  }
}

function mensajeParaError(status: number, accion: AccionFavorito): string {
  const que =
    accion === 'cargar'
      ? 'cargar tus favoritos'
      : accion === 'marcar'
        ? 'guardar el producto en favoritos'
        : 'quitar el producto de favoritos';

  switch (status) {
    case 0:
      return `No se puede conectar con el servidor para ${que}.`;
    case 401:
      return `Tu sesión ha caducado. Inicia sesión de nuevo para ${que}.`;
    case 403:
      return 'Solo los clientes pueden gestionar favoritos.';
    case 404:
      return 'Este producto ya no está disponible.';
    default:
      return `No se ha podido ${que}. Inténtalo de nuevo más tarde.`;
  }
}
