import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface NuevoAdministrador {
  nombre: string;
  apellidos: string;
  email: string;
  contrasena: string;
  confirmarContrasena: string;
  sede: string;
  imagenPerfil: string;
}

export interface AdministradorCreado {
  id: string;
  nombre: string;
  apellidos: string;
  email: string;
  sede: string;
  rol: string;
  mensaje: string;
}

@Injectable({
  providedIn: 'root',
})
export class Administradores {
  private readonly http = inject(HttpClient);

  crear(datos: NuevoAdministrador): Observable<AdministradorCreado> {
    return this.http.post<AdministradorCreado>(`${environment.apiUrl}/admin`, datos, {
      withCredentials: true,
    });
  }
}