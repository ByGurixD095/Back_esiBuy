import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Producto } from '../../models/producto.model';
import { ProductoApiError, ProductoService } from '../../services/producto.service';

@Component({
  selector: 'app-producto-detalle',
  imports: [RouterLink],
  templateUrl: './producto-detalle.html',
  styleUrl: './producto-detalle.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductoDetalle implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly productoService = inject(ProductoService);

  readonly producto = signal<Producto | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly productoId = signal<string | null>(null);

  ngOnInit(): void {
    this.cargarProducto();
  }

  cargarProducto(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('No se ha indicado ningún producto.');
      this.cargando.set(false);
      return;
    }
    this.productoId.set(id);

    this.productoService.obtenerProducto(id).subscribe({
      next: (producto) => {
        this.producto.set(producto);
        this.cargando.set(false);
      },
      error: (error: ProductoApiError) => {
        this.error.set(`${error.message} (ID solicitado: ${id})`);
        this.cargando.set(false);
      },
    });
  }

  formatearPrecio(precioCent: number): string {
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(precioCent / 100);
  }
}
