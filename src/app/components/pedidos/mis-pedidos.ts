import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Pedido, PedidosService } from '../../services/pedidos.service';
import { MarcoPagina } from '../marco-pagina/marco-pagina';

@Component({
  selector: 'app-mis-pedidos',
  imports: [MarcoPagina, RouterLink],
  templateUrl: './mis-pedidos.html',
  styleUrl: './mis-pedidos.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MisPedidos implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly pedidosService = inject(PedidosService);

  readonly pedidos = signal<Pedido[]>([]);
  readonly cargando = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    this.cargarPedidos();
  }

  cargarPedidos(): void {
    this.cargando.set(true);
    this.error.set('');
    this.pedidosService.listar().subscribe({
      next: (pedidos) => {
        this.pedidos.set(pedidos);
        this.cargando.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(this.auth.errorMessage(error, 'No se han podido cargar tus pedidos.'));
        this.cargando.set(false);
      },
    });
  }

  formatearPrecio(precioCent: number): string {
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(precioCent / 100);
  }

  formatearFecha(fecha: string | null): string {
    if (!fecha) return 'Fecha no disponible';
    const fechaPedido = new Date(fecha);
    if (Number.isNaN(fechaPedido.getTime())) return 'Fecha no disponible';
    return new Intl.DateTimeFormat('es-ES', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(fechaPedido);
  }

  estadoLegible(estado: string): string {
    const etiquetas: Record<string, string> = {
      RECIBIDO: 'Recibido',
      EN_PREPARACION: 'En preparación',
      ENVIADO: 'Enviado',
      ENTREGADO: 'Entregado',
      CANCELADO: 'Cancelado',
    };
    return etiquetas[estado] ?? estado.replaceAll('_', ' ').toLowerCase();
  }
}
