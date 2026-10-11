import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { Producto } from '../../models/producto.model';
import { EstadoProducto, FiltrosProductos, OrdenCatalogo } from '../../services/product.service';

@Component({
  selector: 'app-panel-filtros-productos',
  templateUrl: './panel-filtros-productos.html',
  styleUrl: './panel-filtros-productos.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PanelFiltrosProductos {
  readonly productos = input<Producto[]>([]);
  readonly mostrarEstado = input(false);
  readonly filtrosChange = output<FiltrosProductos>();

  readonly busqueda = signal('');
  readonly categoria = signal('Todas');
  readonly orden = signal<OrdenCatalogo>('relevancia');
  readonly estado = signal<EstadoProducto>('todos');
  readonly precioMinimo = signal<number | null>(null);
  readonly precioMaximo = signal<number | null>(null);
  readonly soloConDescuento = signal(false);

  readonly categorias = computed(() => ['Todas', ...new Set(this.productos().map((producto) => producto.categoria))]);
  readonly precioEscalaMaxima = computed(() => {
    const maximo = this.productos().reduce((mayor, producto) => Math.max(mayor, producto.precioCent), 0);
    return Math.max(100_000, Math.ceil(maximo / 100) * 100);
  });
  readonly precioMinimoSlider = computed(() => this.precioMinimo() ?? 0);
  readonly precioMaximoSlider = computed(() => this.precioMaximo() ?? this.precioEscalaMaxima());
  readonly hayFiltros = computed(
    () =>
      !!this.busqueda() ||
      this.categoria() !== 'Todas' ||
      this.orden() !== 'relevancia' ||
      this.estado() !== 'todos' ||
      this.precioMinimo() !== null ||
      this.precioMaximo() !== null ||
      this.soloConDescuento(),
  );

  actualizarBusqueda(event: Event): void {
    this.busqueda.set((event.target as HTMLInputElement).value);
  }

  actualizarCategoria(event: Event): void {
    this.categoria.set((event.target as HTMLSelectElement).value);
  }

  actualizarOrden(event: Event): void {
    this.orden.set((event.target as HTMLSelectElement).value as OrdenCatalogo);
  }

  actualizarEstado(event: Event): void {
    this.estado.set((event.target as HTMLSelectElement).value as EstadoProducto);
  }

  actualizarPrecioMinimo(event: Event): void {
    this.precioMinimo.set(this.precioDesdeEntrada((event.target as HTMLInputElement).value));
  }

  actualizarPrecioMaximo(event: Event): void {
    this.precioMaximo.set(this.precioDesdeEntrada((event.target as HTMLInputElement).value));
  }

  actualizarPrecioTexto(event: Event, limite: 'minimo' | 'maximo'): void {
    const precioValido = this.precioDesdeEntrada((event.target as HTMLInputElement).value, true);
    (limite === 'minimo' ? this.precioMinimo : this.precioMaximo).set(precioValido);
  }

  alternarDescuento(event: Event): void {
    this.soloConDescuento.set((event.target as HTMLInputElement).checked);
  }

  aplicarFiltros(): void {
    const minimo = this.precioMinimo();
    const maximo = this.precioMaximo();

    if (minimo !== null && maximo !== null && minimo > maximo) {
      this.precioMinimo.set(maximo);
      this.precioMaximo.set(minimo);
    }
    this.filtrosChange.emit({
      busqueda: this.busqueda(),
      categoria: this.categoria(),
      precioMinCent: this.precioMinimo(),
      precioMaxCent: this.precioMaximo(),
      soloOfertas: this.soloConDescuento(),
      orden: this.orden(),
      estado: this.estado(),
    });
  }

  limpiarFiltros(): void {
    this.busqueda.set('');
    this.categoria.set('Todas');
    this.orden.set('relevancia');
    this.estado.set('todos');
    this.precioMinimo.set(null);
    this.precioMaximo.set(null);
    this.soloConDescuento.set(false);
    this.aplicarFiltros();
  }

  private precioDesdeEntrada(valor: string, enEuros = false): number | null {
    if (!valor.trim()) return null;
    const numero = Number(valor);
    if (!Number.isFinite(numero)) return null;
    return Math.max(0, Math.round(enEuros ? numero * 100 : numero));
  }
}
