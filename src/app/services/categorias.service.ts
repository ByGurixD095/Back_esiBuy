import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface NuevaCategoria {
  nombre: string;
}

export interface CategoriaCreada extends NuevaCategoria {
  id: string;
}

export interface Categoria {
  id: string;
  nombre: string;
}

@Injectable({
  providedIn: 'root',
})
export class Categorias {
  private readonly http = inject(HttpClient);

  crear(datos: NuevaCategoria): Observable<CategoriaCreada> {
    return this.http.post<CategoriaCreada>(
      `${environment.apiUrl}/admin/categorias`,
      datos,
      { withCredentials: true },
    );
  }

  listar(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${environment.apiUrl}/admin/categorias`, {
      withCredentials: true,
    });
  }
  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(
      `${environment.apiUrl}/admin/categorias/${encodeURIComponent(id)}`,
      { withCredentials: true },
    );
  }
}
