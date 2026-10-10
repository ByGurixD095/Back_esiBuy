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

@Injectable({ providedIn: 'root' })
export class Usuarios {
  private readonly http = inject(HttpClient);

  consultar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${environment.apiUrl}/admin/users`, {
      withCredentials: true,
    });
  }
}
