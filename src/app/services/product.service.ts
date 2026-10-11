import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { PaginaProducto, Producto } from '../models/producto.model';

export class ProductApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = 'ProductApiError';
  }
}

export type OrdenCatalogo = 'relevancia' | 'precioAsc' | 'precioDesc' | 'descuento';
export type EstadoProducto = 'todos' | 'activos' | 'inactivos';

export interface FiltrosProductos {
  busqueda?: string;
  categoria?: string;
  precioMinCent?: number | null;
  precioMaxCent?: number | null;
  soloOfertas?: boolean;
  orden?: string;
  estado?: EstadoProducto;
}

function paramsDeFiltros(filtros: FiltrosProductos, pagina: number, tamanoPagina: number): HttpParams {
  let params = new HttpParams();

  if (filtros.busqueda?.trim()) params = params.set('busqueda', filtros.busqueda.trim());
  if (filtros.categoria && filtros.categoria !== 'Todas' && filtros.categoria !== 'TODAS') params = params.set('categoria', filtros.categoria);
  if (filtros.precioMinCent != null) params = params.set('precioMinCent', filtros.precioMinCent);
  if (filtros.precioMaxCent != null) params = params.set('precioMaxCent', filtros.precioMaxCent);
  if (filtros.soloOfertas) params = params.set('soloOfertas', true);
  if (filtros.orden && filtros.orden !== 'relevancia') params = params.set('orden', filtros.orden);
  if (filtros.estado && filtros.estado !== 'todos') params = params.set('activo', filtros.estado === 'activos');
  return params.set('page', pagina).set('size', tamanoPagina);
}

// Garantiza el filtrado aunque el backend ignore algún parámetro.
export function filtrarProductos(productos: Producto[], filtros: FiltrosProductos): Producto[] {
  const texto = filtros.busqueda?.trim().toLowerCase();
  const categoria = filtros.categoria && filtros.categoria !== 'Todas' && filtros.categoria !== 'TODAS' ? filtros.categoria : null;

  return productos.filter(
    (p) =>
      (!texto || p.nombre.toLowerCase().includes(texto) || p.referencia.toLowerCase().includes(texto)) &&
      (!categoria || p.categoria === categoria) &&
      (filtros.precioMinCent == null || p.precioCent >= filtros.precioMinCent) &&
      (filtros.precioMaxCent == null || p.precioCent <= filtros.precioMaxCent) &&
      (!filtros.soloOfertas || p.descuento > 0) &&
      (!filtros.estado || filtros.estado === 'todos' || p.activo === (filtros.estado === 'activos')),
  );
}

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/products`;

  obtenerDisponibles(filtros: FiltrosProductos = {}, pagina = 0, tamanoPagina = 36): Observable<PaginaProducto> {
    const params = paramsDeFiltros(filtros, pagina, tamanoPagina);

    return this.http.get<PaginaProducto>(`${this.apiUrl}/disponibles`, { params, withCredentials: true }).pipe(
      catchError((error: HttpErrorResponse) => {
        return throwError(() => new ProductApiError(error.status, mensajeParaStatus(error)));
      }),
    );
  }

  obtenerCatalogoVendedor(pagina = 0, tamanoPagina = 36, filtros: FiltrosProductos = {}): Observable<PaginaProducto> {
    const params = paramsDeFiltros(filtros, pagina, tamanoPagina).set('sort', 'activo,desc');

    return this.http.get<PaginaProducto>(`${this.apiUrl}/catalogo`, {
      params,
      withCredentials: true,
    }).pipe(
      catchError((error: HttpErrorResponse) => {
        return throwError(() => new ProductApiError(error.status, mensajeParaCatalogoVendedor(error)));
      }),
    );
  }
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

function mensajeParaCatalogoVendedor(error: HttpErrorResponse): string {
  switch (error.status) {
    case 401:
      return 'Tu sesión no está autenticada o ha caducado. Inicia sesión de nuevo.';
    case 403:
      return 'No tienes permisos para consultar tus productos.';
    case 500:
      return 'El servidor no ha podido cargar tus productos. Inténtalo de nuevo más tarde.';
    case 0:
      return 'No se puede conectar con el backend en http://localhost:8080.';
    default:
      return `No se han podido cargar tus productos (HTTP ${error.status}).`;
  }
}
