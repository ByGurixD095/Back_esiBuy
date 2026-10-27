import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CarritoService, ItemCarrito } from '../../services/carrito.service';
import { MarcoPagina } from '../marco-pagina/marco-pagina';

@Component({
  selector: 'app-carrito',
  imports: [MarcoPagina, RouterLink],
  templateUrl: './carrito.html',
  styleUrl: './carrito.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Carrito implements OnInit {
  private readonly carritoService = inject(CarritoService);

  readonly items = signal<ItemCarrito[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly actualizandoIds = signal<ReadonlySet<string>>(new Set());
  readonly cantidadUnidades = computed(() => this.items().reduce((total, item) => total + item.cantidad, 0));
  readonly totalCentimos = computed(() =>
    this.items().reduce((total, item) => total + item.precioCent * item.cantidad, 0),
  );

  ngOnInit(): void {
    this.cargarCarrito();
  }

  cargarCarrito(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.carritoService.obtenerCarrito().subscribe({
      next: (items) => {
        this.items.set(items);
        this.cargando.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(this.mensajeParaError(error));
        this.cargando.set(false);
      },
    });
  }

  editarCantidad(event: Event, item: ItemCarrito): void {
    const entrada = event.target;
    if (!(entrada instanceof HTMLInputElement)) return;

    const cantidad = entrada.valueAsNumber;
    if (!Number.isInteger(cantidad) || cantidad < 1) {
      this.error.set('La cantidad debe ser un número entero mayor que cero.');
      entrada.value = String(item.cantidad);
      return;
    }
    this.actualizarCantidad(item, cantidad);
  }

  actualizarCantidad(item: ItemCarrito, cantidad: number): void {
    if (cantidad < 1 || cantidad === item.cantidad || this.actualizandoIds().has(item.productoId)) return;

    this.error.set(null);
    this.actualizandoIds.update((ids) => new Set(ids).add(item.productoId));
    this.carritoService.actualizarCantidad(item.productoId, cantidad).subscribe({
      next: () => {
        this.items.update((items) =>
          items.map((actual) =>
            actual.productoId === item.productoId ? { ...actual, cantidad } : actual,
          ),
        );
        this.limpiarActualizando(item.productoId);
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(this.mensajeParaError(error));
        this.limpiarActualizando(item.productoId);
      },
    });
  }

  estaActualizando(productoId: string): boolean {
    return this.actualizandoIds().has(productoId);
  }

  formatearPrecio(precioCent: number): string {
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(precioCent / 100);
  }

  private limpiarActualizando(productoId: string): void {
    this.actualizandoIds.update((ids) => {
      const restantes = new Set(ids);
      restantes.delete(productoId);
      return restantes;
    });
  }

  private mensajeParaError(error: HttpErrorResponse): string {
    if (error.status === 0) return 'No se puede conectar con el servidor. Inténtalo de nuevo.';
    if (error.status === 401) return 'Tu sesión ha caducado. Inicia sesión de nuevo para consultar la cesta.';
    if (error.status === 403) return 'Solo los clientes pueden consultar y modificar su cesta.';
    if (error.status === 404) return 'No se ha encontrado el carrito o el producto que intentas modificar.';
    if (error.status === 400) return 'La cantidad indicada no es válida.';
    return `No se ha podido actualizar el carrito (HTTP ${error.status}).`;
  }
}
