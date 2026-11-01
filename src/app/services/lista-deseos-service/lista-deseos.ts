import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Producto } from '../../models/producto.model';

export interface ListaDeseos {
  id: string;
  idCliente: string;
  nombre: string;
  productosIds: string[];
}

export class ListaDeseosApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = 'ListaDeseosApiError';
  }
}

@Injectable({ providedIn: 'root' })
export class ListaDeseosService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/listas-deseos`;

  listar(): Observable<ListaDeseos[]> {
    return this.http
      .get<ListaDeseos[]>(this.apiUrl, { withCredentials: true })
      .pipe(catchError((error: HttpErrorResponse) => throwError(() => this.convertir(error, 'listar'))));
  }

  obtenerProductos(idLista: string): Observable<Producto[]> {
    return this.http
      .get<Producto[]>(`${this.apiUrl}/${encodeURIComponent(idLista)}/productos`, { withCredentials: true })
      .pipe(catchError((error: HttpErrorResponse) => throwError(() => this.convertir(error, 'productos'))));
  }

  crear(nombre: string): Observable<ListaDeseos> {
    return this.http
      .post<ListaDeseos>(this.apiUrl, { nombre }, { withCredentials: true })
      .pipe(catchError((error: HttpErrorResponse) => throwError(() => this.convertir(error, 'crear'))));
  }

  private convertir(error: HttpErrorResponse, accion: 'crear' | 'listar' | 'productos'): ListaDeseosApiError {
    switch (error.status) {
      case 0:
        return new ListaDeseosApiError(
          error.status,
          accion === 'crear'
            ? 'No se puede conectar con el servidor para crear la lista.'
            : 'No se puede conectar con el servidor para consultar tus listas de deseos.',
        );
      case 400:
        return new ListaDeseosApiError(error.status, 'Introduce un nombre válido para la lista.');
      case 401:
        return new ListaDeseosApiError(
          error.status,
          accion === 'crear'
            ? 'Tu sesión ha caducado. Inicia sesión de nuevo para crear la lista.'
            : 'Tu sesión ha caducado. Inicia sesión de nuevo para consultar tus listas de deseos.',
        );
      case 403:
        return new ListaDeseosApiError(
          error.status,
          accion === 'crear'
            ? 'Solo los clientes pueden crear listas de deseos.'
            : 'Solo los clientes pueden consultar listas de deseos.',
        );
      case 404:
        return new ListaDeseosApiError(error.status, 'No se ha encontrado esta lista de deseos.');
      default:
        return new ListaDeseosApiError(
          error.status,
          accion === 'crear'
            ? 'No se ha podido crear la lista. Inténtalo de nuevo más tarde.'
            : 'No se han podido cargar tus listas de deseos. Inténtalo de nuevo más tarde.',
        );
    }
  }
}
