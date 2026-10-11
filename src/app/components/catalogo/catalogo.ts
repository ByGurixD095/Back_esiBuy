import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { PaginaProducto, Producto } from '../../models/producto.model';
import { FiltrosProductos, ProductApiError, ProductService } from '../../services/product.service';

type OrdenCatalogo = 'relevancia' | 'precioAsc' | 'precioDesc' | 'descuento';

@Component({
  selector: 'app-catalogo',
  imports: [RouterLink],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Catalogo implements OnInit {
  private readonly productService = inject(ProductService);
  readonly auth = inject(AuthService);

  readonly productos = signal<Producto[]>([]);
  readonly totalElementos = signal(0);
  readonly totalPaginas = signal(0);
  readonly paginaActual = signal(0);
  readonly tamanoPagina = 36;
  readonly cargando = signal(true);
  readonly error = signal<{ status: number; message: string } | null>(null);
  readonly busqueda = signal('');
  readonly busquedaAplicada = signal('');
  readonly categoriaSeleccionada = signal('Todas');
  readonly categoriaAplicada = signal('Todas');
  readonly orden = signal<OrdenCatalogo>('relevancia');
  readonly ordenAplicada = signal<OrdenCatalogo>('relevancia');
  readonly precioMinimo = signal<number | null>(null);
  readonly precioMaximo = signal<number | null>(null);
  readonly precioMinimoAplicado = signal<number | null>(null);
  readonly precioMaximoAplicado = signal<number | null>(null);
  readonly soloConDescuento = signal(false);
  readonly soloConDescuentoAplicado = signal(false);
  readonly categorias = computed(() => ['Todas', ...new Set(this.productos().map((producto) => producto.categoria))]);
  readonly precioEscalaMaxima = computed(() => {
    const maximo = this.productos().reduce((mayor, producto) => Math.max(mayor, producto.precioCent), 0);
    return Math.max(100_000, Math.ceil(maximo / 100) * 100);
  });
  readonly precioMinimoSlider = computed(() => this.precioMinimo() ?? 0);
  readonly precioMaximoSlider = computed(() => this.precioMaximo() ?? this.precioEscalaMaxima());
  ngOnInit(): void {
    this.cargarCatalogo();
  }

  cargarCatalogo(filtros: FiltrosProductos = this.filtrosActuales()): void {
    this.cargando.set(true);
    this.error.set(null);
    this.productService.obtenerDisponibles(filtros, this.paginaActual(), this.tamanoPagina).subscribe({
      next: (paginaProductos: PaginaProducto) => {
        this.productos.set(paginaProductos.content);
        this.totalPaginas.set(paginaProductos.totalPages);
        this.totalElementos.set(paginaProductos.totalElements);
        this.paginaActual.set(paginaProductos.number);
        this.cargando.set(false);
      },
      error: (error: ProductApiError) => {
        this.error.set({ status: error.status, message: error.message });
        this.cargando.set(false);
      },
    });
  }

  actualizarBusqueda(event: Event): void {
    this.busqueda.set((event.target as HTMLInputElement).value);
  }

  actualizarCategoria(event: Event): void {
    this.categoriaSeleccionada.set((event.target as HTMLSelectElement).value);
  }

  actualizarOrden(event: Event): void {
    this.orden.set((event.target as HTMLSelectElement).value as OrdenCatalogo);
  }

  actualizarPrecioMinimo(event: Event): void {
    this.precioMinimo.set(this.precioDesdeEntrada((event.target as HTMLInputElement).value));
  }

  actualizarPrecioMaximo(event: Event): void {
    this.precioMaximo.set(this.precioDesdeEntrada((event.target as HTMLInputElement).value));
  }

  actualizarPrecioTexto(event: Event, limite: 'minimo' | 'maximo'): void {
    const valor = (event.target as HTMLInputElement).value;
    const precioValido = this.precioDesdeEntrada(valor, true);

    if (limite === 'minimo') {
      this.precioMinimo.set(precioValido);
    } else {
      this.precioMaximo.set(precioValido);
    }
  }

  alternarDescuento(event: Event): void {
    this.soloConDescuento.set((event.target as HTMLInputElement).checked);
  }

  aplicarFiltros(): void {
    const minimo = this.precioMinimo();
    const maximo = this.precioMaximo();
    const rangoInvertido = minimo !== null && maximo !== null && minimo > maximo;

    this.busquedaAplicada.set(this.busqueda());
    this.categoriaAplicada.set(this.categoriaSeleccionada());
    this.ordenAplicada.set(this.orden());
    this.precioMinimoAplicado.set(rangoInvertido ? maximo : minimo);
    this.precioMaximoAplicado.set(rangoInvertido ? minimo : maximo);
    this.soloConDescuentoAplicado.set(this.soloConDescuento());
    if (rangoInvertido) {
      this.precioMinimo.set(maximo);
      this.precioMaximo.set(minimo);
    }
    this.recargarDesdePrimeraPagina();
  }

  paginaAnterior(): void {
    if (this.paginaActual() === 0) return;
    this.paginaActual.update((pagina) => pagina - 1);
    this.cargarCatalogo();
  }

  paginaSiguiente(): void {
    if (this.paginaActual() + 1 >= this.totalPaginas()) return;
    this.paginaActual.update((pagina) => pagina + 1);
    this.cargarCatalogo();
  }

  limpiarFiltros(): void {
    this.busqueda.set('');
    this.busquedaAplicada.set('');
    this.categoriaSeleccionada.set('Todas');
    this.categoriaAplicada.set('Todas');
    this.orden.set('relevancia');
    this.ordenAplicada.set('relevancia');
    this.precioMinimo.set(null);
    this.precioMaximo.set(null);
    this.precioMinimoAplicado.set(null);
    this.precioMaximoAplicado.set(null);
    this.soloConDescuento.set(false);
    this.soloConDescuentoAplicado.set(false);
    this.recargarDesdePrimeraPagina();
  }

  private recargarDesdePrimeraPagina(): void {
    this.paginaActual.set(0);
    this.cargarCatalogo();
  }

  private filtrosActuales(): FiltrosProductos {
    return {
      busqueda: this.busquedaAplicada(),
      categoria: this.categoriaAplicada(),
      precioMinCent: this.precioMinimoAplicado(),
      precioMaxCent: this.precioMaximoAplicado(),
      soloOfertas: this.soloConDescuentoAplicado(),
      orden: this.ordenAplicada(),
    };
  }

  private precioDesdeEntrada(valor: string, enEuros = false): number | null {
    if (!valor.trim()) return null;
    const numero = Number(valor);
    if (!Number.isFinite(numero)) return null;
    return Math.max(0, Math.round(enEuros ? numero * 100 : numero));
  }

  formatearPrecio(precioCent: number): string {
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(precioCent / 100);
  }
}
