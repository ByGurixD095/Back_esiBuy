import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { Rol } from '../auth.service';

export interface Usuario {
  id: string;
  name: string;
  apellidos: string;
  email: string;
  rol: Rol;
  activo: boolean;
  bloqueado: boolean;
  eliminado: boolean;
}

export interface UsuarioDetalle extends Usuario {
  telefono?: string | null;
  imagenPerfil?: string | null;
  fechaAlta?: string | null;
  fechaModificacion?: string | null;
  fechaCambioContrasena?: string | null;
  mfaConfigurado?: boolean;
  dosFactorActivoCliente?: boolean;
  tresFactorActivoCliente?: boolean;
  dni?: string | null;
  fechaNacimiento?: string | null;
  tipoCliente?: 'NORMAL' | 'PREMIUM' | null;
  nombreComercial?: string | null;
  cifNif?: string | null;
  categoriaPrincipalId?: string | null;
  sede?: string | null;
  fechaIncorporacion?: string | null;
}

export interface ModificarUsuarioRequest {
  nombre: string;
  apellidos: string;
  telefono?: string;
  imagenPerfil: string;
  dni?: string;
  fechaNacimiento?: string;
  tipoCliente?: 'NORMAL' | 'PREMIUM';
  nombreComercial?: string;
  cifNif?: string;
  categoriaPrincipalId?: string;
  sede?: string;
}

@Injectable({ providedIn: 'root' })
export class Usuarios {
  private readonly http = inject(HttpClient);

  consultar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${environment.apiUrl}/admin/users`, {
      withCredentials: true,
    });
  }

  consultarPorId(id: string): Observable<UsuarioDetalle> {
    return this.http.get<UsuarioDetalle>(`${environment.apiUrl}/admin/${encodeURIComponent(id)}`, {
      withCredentials: true,
    });
  }

  modificar(id: string, datos: ModificarUsuarioRequest): Observable<void> {
    return this.http.patch<void>(`${environment.apiUrl}/admin/${encodeURIComponent(id)}`, datos, {
      withCredentials: true,
    });
  }

  bloquear(id: string): Observable<Usuario> {
    return this.http.patch<Usuario>(
      `${environment.apiUrl}/admin/bloquear/${encodeURIComponent(id)}`,
      {},
      { withCredentials: true },
    );
  }

  desbloquear(id: string): Observable<Usuario> {
    return this.http.patch<Usuario>(
      `${environment.apiUrl}/admin/desbloquear/${encodeURIComponent(id)}`,
      {},
      { withCredentials: true },
    );
  }

  activar(id: string): Observable<Usuario> {
    return this.http.patch<Usuario>(
      `${environment.apiUrl}/admin/activar/${encodeURIComponent(id)}`,
      {},
      { withCredentials: true },
    );
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/admin/${encodeURIComponent(id)}`, {
      withCredentials: true,
    });
  }
}
