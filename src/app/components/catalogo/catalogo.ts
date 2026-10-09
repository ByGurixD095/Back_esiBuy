import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Producto } from '../../models/producto.model';
import { AuthService } from '../../services/auth.service';
import { ProductApiError, ProductService } from '../../services/product.service';

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
  readonly cargando = signal(true);
  readonly error = signal<{ status: number; message: string } | null>(null);
  readonly busqueda = signal('');
  readonly categoriaSeleccionada = signal('Todas');
  readonly orden = signal<OrdenCatalogo>('relevancia');
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
  readonly productosVisibles = computed(() => {
    const termino = this.busqueda().trim().toLocaleLowerCase();
    const categoria = this.categoriaSeleccionada();
    const minimo = this.precioMinimo();
    const maximo = this.precioMaximo();

    const visibles = this.productos().filter((producto) => {
      const coincideBusqueda = !termino || `${producto.nombre} ${producto.descripcion ?? ''} ${producto.referencia}`.toLocaleLowerCase().includes(termino);
      const coincideCategoria = categoria === 'Todas' || producto.categoria === categoria;
      const coincidePrecio = (minimo === null || producto.precioCent >= minimo) && (maximo === null || producto.precioCent <= maximo);
      const coincideDescuento = !this.soloConDescuento() || producto.descuento > 0;
      return coincideBusqueda && coincideCategoria && coincidePrecio && coincideDescuento;
    });

    return [...visibles].sort((a, b) => {
      switch (this.orden()) {
        case 'precioAsc': return a.precioCent - b.precioCent;
        case 'precioDesc': return b.precioCent - a.precioCent;
        case 'descuento': return b.descuento - a.descuento;
        default: return 0;
      }
    });
  });

  ngOnInit(): void {
    this.cargarCatalogo();
  }

  cargarCatalogo(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.productService.obtenerDisponibles().subscribe({
      next: (productos) => {
        this.productos.set(productos);
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
    this.precioMinimo.set(Number((event.target as HTMLInputElement).value));
  }

  actualizarPrecioMaximo(event: Event): void {
    this.precioMaximo.set(Number((event.target as HTMLInputElement).value));
  }

  actualizarPrecioTexto(event: Event, limite: 'minimo' | 'maximo'): void {
    const valor = (event.target as HTMLInputElement).value;
    const precioCent = valor === '' ? null : Number(valor) * 100;
    const precioValido = precioCent === null || Number.isFinite(precioCent) ? precioCent : null;

    if (limite === 'minimo') {
      this.precioMinimo.set(precioValido);
    } else {
      this.precioMaximo.set(precioValido);
    }
  }

  alternarDescuento(event: Event): void {
    this.soloConDescuento.set((event.target as HTMLInputElement).checked);
  }

  limpiarFiltros(): void {
    this.busqueda.set('');
    this.categoriaSeleccionada.set('Todas');
    this.orden.set('relevancia');
    this.precioMinimo.set(null);
    this.precioMaximo.set(null);
    this.soloConDescuento.set(false);
  }

  formatearPrecio(precioCent: number): string {
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(precioCent / 100);
  }
}
