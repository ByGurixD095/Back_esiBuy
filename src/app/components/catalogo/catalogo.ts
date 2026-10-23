import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { PaginaProducto, Producto } from '../../models/producto.model';
import { Favoritos, FavoritosApiError } from '../../services/favoritos-service/favoritos';
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
  private readonly favoritosService = inject(Favoritos);
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

  // HU 15: favoritos del cliente (solo se usan si el rol es CLIENTE)
  readonly favoritosIds = signal<ReadonlySet<string>>(new Set());
  readonly favoritosProcesando = signal<ReadonlySet<string>>(new Set());
  readonly errorFavorito = signal<string | null>(null);

  ngOnInit(): void {
    this.cargarCatalogo();
    if (this.esCliente()) {
      this.cargarFavoritos();
    }
  }

  esCliente(): boolean {
    return this.auth.hasRole(['CLIENTE']);
  }

  esFavorito(productoId: string): boolean {
    return this.favoritosIds().has(productoId);
  }

  favoritoProcesando(productoId: string): boolean {
    return this.favoritosProcesando().has(productoId);
  }

  private cargarFavoritos(): void {
    this.favoritosService.listar().subscribe({
      next: (productos) => this.favoritosIds.set(new Set(productos.map((p) => p.id))),
      error: (error: FavoritosApiError) => this.errorFavorito.set(error.message),
    });
  }

  alternarFavorito(producto: Producto): void {
    if (this.favoritoProcesando(producto.id)) return;

    const eraFavorito = this.esFavorito(producto.id);
    this.errorFavorito.set(null);
    this.favoritosProcesando.update((ids) => new Set(ids).add(producto.id));

    const peticion = eraFavorito
      ? this.favoritosService.quitar(producto.id)
      : this.favoritosService.marcar(producto.id);

    peticion.subscribe({
      next: () => {
        this.favoritosIds.update((ids) => {
          const siguiente = new Set(ids);
          if (eraFavorito) siguiente.delete(producto.id);
          else siguiente.add(producto.id);
          return siguiente;
        });
        this.terminarProceso(producto.id);
      },
      error: (error: FavoritosApiError) => {
        this.errorFavorito.set(error.message);
        this.terminarProceso(producto.id);
      },
    });
  }

  private terminarProceso(productoId: string): void {
    this.favoritosProcesando.update((ids) => {
      const siguiente = new Set(ids);
      siguiente.delete(productoId);
      return siguiente;
    });
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
