import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PaginaProducto, Producto } from '../../models/producto.model';
import { ProductApiError, ProductService } from '../../services/product.service';

@Component({
  selector: 'app-catalogo-vendedor',
  imports: [RouterLink],
  templateUrl: './catalogo-vendedor.html',
  styleUrl: './catalogo-vendedor.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CatalogoVendedor implements OnInit {
  private readonly productService = inject(ProductService);

  readonly productos = signal<Producto[]>([]);
  readonly totalElementos = signal(0);
  readonly totalPaginas = signal(0);
  readonly paginaActual = signal(0);
  readonly tamanoPagina = 36;
  readonly cargando = signal(true);
  readonly error = signal<{ status: number; message: string } | null>(null);

  ngOnInit(): void {
    this.cargarCatalogo();
  }

  cargarCatalogo(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.productService.obtenerCatalogoVendedor(this.paginaActual(), this.tamanoPagina).subscribe({
      next: (paginaProductos: PaginaProducto) => {
        this.productos.set(paginaProductos.content);
        this.totalElementos.set(paginaProductos.totalElements);
        this.totalPaginas.set(paginaProductos.totalPages);
        this.paginaActual.set(paginaProductos.number);
        this.cargando.set(false);
      },
      error: (error: ProductApiError) => {
        this.error.set({ status: error.status, message: error.message });
        this.cargando.set(false);
      },
    });
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

  formatearPrecio(precioCent: number): string {
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(precioCent / 100);
  }
}
