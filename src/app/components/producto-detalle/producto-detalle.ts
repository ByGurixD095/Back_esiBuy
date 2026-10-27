import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, map, of, switchMap, tap } from 'rxjs';
import { ProductInfo } from '../../models/producto.model';
import { AuthService } from '../../services/auth.service';
import { ProductApiError, ProductService } from '../../services/product.service';

@Component({
  selector: 'app-producto-detalle',
  imports: [RouterLink],
  templateUrl: './producto-detalle.html',
  styleUrl: './producto-detalle.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductoDetalle implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly productoService = inject(ProductService);
  readonly auth = inject(AuthService);

  readonly producto = signal<ProductInfo | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly productoId = signal<string | null>(null);

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        map((params) => params.get('id')),
        tap((id) => {
          this.productoId.set(id);
          this.producto.set(null);
          this.error.set(id ? null : 'No se ha indicado ningún producto.');
          this.cargando.set(Boolean(id));
        }),
        switchMap((id) =>
          id
            ? this.productoService.obtenerInfo(id).pipe(
                map((producto) => ({ producto, error: null })),
                catchError((error: ProductApiError) =>
                  of({ producto: null, error: error.message }),
                ),
              )
            : of({ producto: null, error: 'No se ha indicado ningún producto.' }),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ producto, error }) => {
        this.producto.set(producto);
        this.error.set(error);
        this.cargando.set(false);
      });
  }

  formatearPrecio(precioCent: number): string {
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(
      precioCent / 100,
    );
  }

  formatearPrecioConDescuento(precioCent: number, descuento: number): string {
    return this.formatearPrecio(Math.round(precioCent * (1 - descuento / 100)));
  }

  esVendedor(): boolean {
    return this.auth.hasRole(['VENDEDOR']);
  }
}
