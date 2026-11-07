import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Rol } from './auth.service';

export type TipoCliente = 'NORMAL' | 'PREMIUM';

export interface PerfilUsuario {
  id: string;
  name: string;
  apellidos: string;
  email: string;
  telefono: string | null;
  imagenPerfil: string | null;
  rol: Rol;
  dni?: string | null;
  fechaNacimiento?: string | null;
  tipoCliente?: TipoCliente | null;
  nombreComercial?: string | null;
  cifNif?: string | null;
  categoriaPrincipalId?: string | null;
  sede?: string | null;
  fechaIncorporacion?: string | null;
  dosFactorActivoCliente?: boolean;
  tresFactorActivoCliente?: boolean;
}

export interface ActualizarPerfilRequest {
  nombre: string;
  apellidos: string;
  telefono?: string;
  imagenPerfil?: string;
  tipoCliente?: TipoCliente;
  categoriaPrincipalId?: string;
  nombreComercial?: string;
  sede?: string;
}

@Injectable({ providedIn: 'root' })
export class PerfilService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/users`;

  obtenerMiPerfil(): Observable<PerfilUsuario> {
    return this.http.get<PerfilUsuario>(`${this.apiUrl}/me`, { withCredentials: true });
  }

  actualizarMiPerfil(datos: ActualizarPerfilRequest): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/me/update`, datos, { withCredentials: true });
  }

  eliminarMiCuenta(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/me`, { withCredentials: true });
  }
}
