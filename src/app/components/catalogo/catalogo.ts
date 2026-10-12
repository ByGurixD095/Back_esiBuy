import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { PaginaProducto, Producto } from '../../models/producto.model';
import { PanelFiltrosProductos } from '../panel-filtros-productos/panel-filtros-productos';
import { FiltrosProductos, ProductApiError, ProductService, filtrarProductos } from '../../services/product.service';

@Component({
  selector: 'app-catalogo',
  imports: [RouterLink, PanelFiltrosProductos],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Catalogo implements OnInit {
  private readonly productService = inject(ProductService);
  readonly auth = inject(AuthService);

  readonly productos = signal<Producto[]>([]);
  readonly totalCatalogo = signal<number | null>(null);
  readonly totalElementos = signal(0);
  readonly totalPaginas = signal(0);
  readonly paginaActual = signal(0);
  readonly tamanoPagina = 36;
  readonly cargando = signal(true);
  readonly error = signal<{ status: number; message: string } | null>(null);
  private readonly filtros = signal<FiltrosProductos>({});

  ngOnInit(): void {
    this.cargarCatalogo();
  }

  cargarCatalogo(filtros: FiltrosProductos = this.filtros()): void {
    this.cargando.set(true);
    this.error.set(null);
    this.productService.obtenerDisponibles(filtros, this.paginaActual(), this.tamanoPagina).subscribe({
      next: (paginaProductos: PaginaProducto) => {
        this.productos.set(filtrarProductos(paginaProductos.content, filtros));
        this.totalPaginas.set(paginaProductos.totalPages);
        this.totalElementos.set(paginaProductos.totalElements);
        if (paginaProductos.number === 0 && !this.hayFiltrosRestrictivos(filtros)) {
          this.totalCatalogo.set(paginaProductos.totalElements);
        }
        this.paginaActual.set(paginaProductos.number);
        this.cargando.set(false);
      },
      error: (error: ProductApiError) => {
        this.error.set({ status: error.status, message: error.message });
        this.cargando.set(false);
      },
    });
  }

  private hayFiltrosRestrictivos(filtros: FiltrosProductos): boolean {
    return Boolean(
      filtros.busqueda?.trim() ||
        (filtros.categoria && filtros.categoria !== 'Todas' && filtros.categoria !== 'TODAS') ||
        filtros.precioMinCent != null ||
        filtros.precioMaxCent != null ||
        filtros.soloOfertas ||
        (filtros.estado && filtros.estado !== 'todos'),
    );
  }

  aplicarFiltros(filtros: FiltrosProductos): void {
    this.filtros.set(filtros);
    this.paginaActual.set(0);
    this.cargarCatalogo();
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

  formatearPrecioConDescuento(precioCent: number, descuento: number): string {
    return this.formatearPrecio(Math.round(precioCent * (1 - descuento / 100)));
  }
}
