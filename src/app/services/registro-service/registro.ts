import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginResponse } from '../auth.service';

const API_URL = `${environment.apiUrl}/users`;

interface DatosComunesRegistro {
  nombre: string;
  apellidos: string;
  email: string;
  contrasena: string;
  telefono: string;
  imagenPerfil: string;
}

export interface RegistroCliente extends DatosComunesRegistro {
  dni: string;
  fechaNacimiento: string;
}

export interface RegistroVendedor extends DatosComunesRegistro {
  nombreComercial: string;
  cifNif: string;
  categoriaPrincipalId: string;
}

@Injectable({
  providedIn: 'root',
})
export class Registro {
  private readonly http = inject(HttpClient);

  registrarCliente(datos: RegistroCliente): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_URL}/register/clientes`, datos, {
      withCredentials: true,
    });
  }

  registrarVendedor(datos: RegistroVendedor): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_URL}/register/vendedor`, datos, {
      withCredentials: true,
    });
  }
}
