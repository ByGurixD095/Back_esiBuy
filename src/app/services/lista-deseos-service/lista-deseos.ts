import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

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

  crear(nombre: string): Observable<ListaDeseos> {
    return this.http
      .post<ListaDeseos>(this.apiUrl, { nombre }, { withCredentials: true })
      .pipe(catchError((error: HttpErrorResponse) => throwError(() => this.convertir(error))));
  }

  private convertir(error: HttpErrorResponse): ListaDeseosApiError {
    switch (error.status) {
      case 0:
        return new ListaDeseosApiError(
          error.status,
          'No se puede conectar con el servidor para crear la lista.',
        );
      case 400:
        return new ListaDeseosApiError(error.status, 'Introduce un nombre válido para la lista.');
      case 401:
        return new ListaDeseosApiError(
          error.status,
          'Tu sesión ha caducado. Inicia sesión de nuevo para crear la lista.',
        );
      case 403:
        return new ListaDeseosApiError(
          error.status,
          'Solo los clientes pueden crear listas de deseos.',
        );
      default:
        return new ListaDeseosApiError(
          error.status,
          'No se ha podido crear la lista. Inténtalo de nuevo más tarde.',
        );
    }
  }
}
