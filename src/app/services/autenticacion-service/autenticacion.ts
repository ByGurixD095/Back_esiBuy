import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

const API_URL = 'http://localhost:8080/users';

export interface LoginResponseDTO {
  accessToken: string | null;
  refreshToken: string | null;
  rol: string;
  tipoCliente: string | null;
  mfaStatus: string;
  email: string | null;
  mfaSetupToken: string | null;
}

export interface SesionUsuario {
  email: string;
  rol: string;
  tipoCliente: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class Autenticacion {
  private readonly http = inject(HttpClient);

  readonly usuario = signal<SesionUsuario | null>(null);
  readonly autenticado = computed(() => this.usuario() !== null);

  iniciarSesion(email: string, contrasena: string): Observable<LoginResponseDTO> {
    return this.http
      .post<LoginResponseDTO>(
        `${API_URL}/login`,
        { username: email, password: contrasena },
        { withCredentials: true },
      )
      .pipe(
        tap((respuesta) => {
          if (respuesta.mfaStatus === 'SUCCESS') {
            this.usuario.set({
              email: respuesta.email ?? email,
              rol: respuesta.rol,
              tipoCliente: respuesta.tipoCliente,
            });
          } else {
            this.usuario.set(null);
          }
        }),
      );
  }

  cerrarSesion(): void {
    this.usuario.set(null);
  }
}
