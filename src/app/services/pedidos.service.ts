import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface DireccionEnvio {
  nombreCompleto: string;
  direccion: string;
  ciudad: string;
  provincia: string;
  codigoPostal: string;
  pais: string;
  telefono: string;
}

export interface CrearPedidoRequest {
  metodoPagoId: string;
  direccionEnvio: DireccionEnvio;
}

export interface PedidoCreado {
  id: string;
  totalCent: number;
  estado: string;
}

export interface LineaPedido {
  productoId: string;
  nombre: string;
  referencia: string;
  urlImagen: string | null;
  precioUnitarioCent: number;
  cantidad: number;
  subtotalCent: number;
}

export interface Pedido {
  id: string;
  lineas: LineaPedido[];
  direccionEnvio: DireccionEnvio;
  metodoPagoId: string;
  tipoMetodoPago: string;
  aliasMetodoPago: string;
  referenciaMetodoPago: string;
  totalCent: number;
  estado: string;
  fechaPedido: string | null;
}

@Injectable({ providedIn: 'root' })
export class PedidosService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/pedidos`;

  crear(datos: CrearPedidoRequest): Observable<PedidoCreado> {
    return this.http.post<PedidoCreado>(this.apiUrl, datos);
  }

  listar(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(this.apiUrl);
  }
}
