import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Producto } from '../models/producto.model';
import { ProductApiError, ProductService } from '../services/product.service';

@Component({
  selector: 'app-catalogo',
  imports: [RouterLink],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Catalogo implements OnInit {
  private readonly productService = inject(ProductService);

  readonly productos = signal<Producto[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<{ status: number; message: string } | null>(null);
  readonly busqueda = signal('');
  readonly categoriaSeleccionada = signal('Todas');
  readonly precioMaximo = signal<number | null>(null);
  readonly soloConDescuento = signal(false);
  readonly categorias = computed(() => ['Todas', ...new Set(this.productos().map((producto) => producto.categoria))]);
  readonly productosVisibles = computed(() => {
    const termino = this.busqueda().trim().toLocaleLowerCase();
    const categoria = this.categoriaSeleccionada();
    const maximo = this.precioMaximo();

    return this.productos().filter((producto) => {
      const coincideBusqueda = !termino || `${producto.nombre} ${producto.descripcion ?? ''} ${producto.referencia}`.toLocaleLowerCase().includes(termino);
      const coincideCategoria = categoria === 'Todas' || producto.categoria === categoria;
      const coincidePrecio = maximo === null || producto.precioCent <= maximo;
      const coincideDescuento = !this.soloConDescuento() || producto.descuento > 0;
      return coincideBusqueda && coincideCategoria && coincidePrecio && coincideDescuento;
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

  actualizarPrecioMaximo(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.precioMaximo.set(value ? Number(value) : null);
  }

  alternarDescuento(event: Event): void {
    this.soloConDescuento.set((event.target as HTMLInputElement).checked);
  }

  limpiarFiltros(): void {
    this.busqueda.set('');
    this.categoriaSeleccionada.set('Todas');
    this.precioMaximo.set(null);
    this.soloConDescuento.set(false);
  }

  formatearPrecio(precioCent: number): string {
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(precioCent / 100);
  }
}
