import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

const API_URL = 'http://localhost:8080/users';

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

  registrarCliente(datos: RegistroCliente): Observable<void> {
    return this.http.post<void>(`${API_URL}/register/clientes`, datos, {
      withCredentials: true,
    });
  }

  registrarVendedor(datos: RegistroVendedor): Observable<void> {
    return this.http.post<void>(`${API_URL}/register/vendedor`, datos, {
      withCredentials: true,
    });
  }
}
