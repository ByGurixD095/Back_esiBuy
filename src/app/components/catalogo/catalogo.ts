import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { PaginaProducto, Producto } from '../../models/producto.model';
import { Favoritos, FavoritosApiError } from '../../services/favoritos-service/favoritos';
import { CarritoService } from '../../services/carrito.service';
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
  private readonly carritoService = inject(CarritoService);
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
  readonly carritoProcesando = signal<ReadonlySet<string>>(new Set());
  readonly mensajeCarrito = signal<string | null>(null);
  readonly errorCarrito = signal<string | null>(null);

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

  anadirAlCarrito(producto: Producto): void {
    if (this.carritoProcesando().has(producto.id)) return;

    this.mensajeCarrito.set(null);
    this.errorCarrito.set(null);
    this.carritoProcesando.update((ids) => new Set(ids).add(producto.id));
    this.carritoService.anadirProducto(producto.id).subscribe({
      next: () => {
        this.mensajeCarrito.set(`${producto.nombre} se ha añadido a tu cesta.`);
        this.terminarAnadirAlCarrito(producto.id);
      },
      error: (error: { status: number }) => {
        this.errorCarrito.set(this.mensajeParaErrorCarrito(error.status));
        this.terminarAnadirAlCarrito(producto.id);
      },
    });
  }

  estaAnadiendoAlCarrito(productoId: string): boolean {
    return this.carritoProcesando().has(productoId);
  }

  private terminarProceso(productoId: string): void {
    this.favoritosProcesando.update((ids) => {
      const siguiente = new Set(ids);
      siguiente.delete(productoId);
      return siguiente;
    });
  }

  private terminarAnadirAlCarrito(productoId: string): void {
    this.carritoProcesando.update((ids) => {
      const siguientes = new Set(ids);
      siguientes.delete(productoId);
      return siguientes;
    });
  }

  private mensajeParaErrorCarrito(status: number): string {
    if (status === 0) return 'No se puede conectar con el servidor para añadir el producto.';
    if (status === 401) return 'Inicia sesión como cliente para añadir productos a la cesta.';
    if (status === 403) return 'Solo los clientes pueden añadir productos a la cesta.';
    if (status === 404) return 'El producto ya no está disponible.';
    return `No se ha podido añadir el producto a la cesta (HTTP ${status}).`;
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
