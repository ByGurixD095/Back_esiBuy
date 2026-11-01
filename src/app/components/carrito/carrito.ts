import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CarritoService, ItemCarrito } from '../../services/carrito.service';
import { MetodoPago, MetodosPagoService } from '../../services/metodos-pago-service/metodos-pago';
import { CrearPedidoRequest, PedidoCreado, PedidosService } from '../../services/pedidos.service';
import { MarcoPagina } from '../marco-pagina/marco-pagina';

@Component({
  selector: 'app-carrito',
  imports: [MarcoPagina, ReactiveFormsModule, RouterLink],
  templateUrl: './carrito.html',
  styleUrl: './carrito.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Carrito implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly carritoService = inject(CarritoService);
  private readonly metodosPagoService = inject(MetodosPagoService);
  private readonly pedidosService = inject(PedidosService);

  readonly items = signal<ItemCarrito[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly actualizandoIds = signal<ReadonlySet<string>>(new Set());
  readonly productoPendienteEliminar = signal<ItemCarrito | null>(null);
  readonly checkoutAbierto = signal(false);
  readonly metodosPago = signal<MetodoPago[]>([]);
  readonly cargandoMetodosPago = signal(false);
  readonly errorMetodosPago = signal('');
  readonly creandoPedido = signal(false);
  readonly errorPedido = signal('');
  readonly pedidoConfirmado = signal<PedidoCreado | null>(null);
  readonly cantidadUnidades = computed(() => this.items().reduce((total, item) => total + item.cantidad, 0));
  readonly totalCentimos = computed(() =>
    this.items().reduce((total, item) => total + item.precioCent * item.cantidad, 0),
  );

  readonly checkoutForm = new FormGroup({
    metodoPagoId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    nombreCompleto: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(120)],
    }),
    direccion: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(200)],
    }),
    ciudad: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)],
    }),
    provincia: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)],
    }),
    codigoPostal: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(20)],
    }),
    pais: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)],
    }),
    telefono: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(30)],
    }),
  });

  ngOnInit(): void {
    this.cargarCarrito();
  }

  cargarCarrito(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.carritoService.obtenerCarrito().subscribe({
      next: (items) => {
        this.items.set(items);
        this.cargando.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(this.mensajeParaError(error));
        this.cargando.set(false);
      },
    });
  }

  editarCantidad(event: Event, item: ItemCarrito): void {
    const entrada = event.target;
    if (!(entrada instanceof HTMLInputElement)) return;

    const cantidad = entrada.valueAsNumber;
    if (!Number.isInteger(cantidad) || cantidad < 1) {
      this.error.set('La cantidad debe ser un número entero mayor que cero.');
      entrada.value = String(item.cantidad);
      return;
    }
    this.actualizarCantidad(item, cantidad);
  }

  actualizarCantidad(item: ItemCarrito, cantidad: number): void {
    if (cantidad < 1 || cantidad === item.cantidad || this.actualizandoIds().has(item.productoId)) return;

    this.error.set(null);
    this.actualizandoIds.update((ids) => new Set(ids).add(item.productoId));
    this.carritoService.actualizarCantidad(item.productoId, cantidad).subscribe({
      next: () => {
        this.items.update((items) =>
          items.map((actual) =>
            actual.productoId === item.productoId ? { ...actual, cantidad } : actual,
          ),
        );
        this.limpiarActualizando(item.productoId);
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(this.mensajeParaError(error));
        this.limpiarActualizando(item.productoId);
      },
    });
  }

  eliminarProducto(item: ItemCarrito): void {
    if (this.actualizandoIds().has(item.productoId)) return;
    this.productoPendienteEliminar.set(item);
  }

  cancelarEliminacion(): void {
    this.productoPendienteEliminar.set(null);
  }

  confirmarEliminacion(): void {
    const item = this.productoPendienteEliminar();
    if (!item || this.actualizandoIds().has(item.productoId)) return;

    this.productoPendienteEliminar.set(null);
    this.error.set(null);
    this.actualizandoIds.update((ids) => new Set(ids).add(item.productoId));
    this.carritoService.eliminarProducto(item.productoId).subscribe({
      next: () => {
        this.items.update((items) => items.filter((actual) => actual.productoId !== item.productoId));
        this.limpiarActualizando(item.productoId);
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(this.mensajeParaError(error));
        this.limpiarActualizando(item.productoId);
      },
    });
  }

  abrirCheckout(): void {
    this.checkoutAbierto.set(true);
    this.errorPedido.set('');
    this.cargarMetodosPago();
  }

  cerrarCheckout(): void {
    if (!this.creandoPedido()) {
      this.checkoutAbierto.set(false);
      this.errorPedido.set('');
    }
  }

  cargarMetodosPago(): void {
    this.cargandoMetodosPago.set(true);
    this.errorMetodosPago.set('');
    this.metodosPagoService.listar().subscribe({
      next: (metodos) => {
        this.metodosPago.set(metodos);
        if (
          metodos.length > 0 &&
          !metodos.some((metodo) => metodo.id === this.checkoutForm.controls.metodoPagoId.value)
        ) {
          this.checkoutForm.controls.metodoPagoId.setValue(metodos[0].id);
        }
        this.cargandoMetodosPago.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMetodosPago.set(
          this.auth.errorMessage(error, 'No se han podido cargar tus métodos de pago.'),
        );
        this.cargandoMetodosPago.set(false);
      },
    });
  }

  confirmarPedido(): void {
    if (this.creandoPedido()) return;
    if (this.checkoutForm.invalid || this.metodosPago().length === 0) {
      this.checkoutForm.markAllAsTouched();
      this.errorPedido.set('Completa los datos de envío y selecciona un método de pago.');
      return;
    }

    const valores = this.checkoutForm.getRawValue();
    const datos: CrearPedidoRequest = {
      metodoPagoId: valores.metodoPagoId,
      direccionEnvio: {
        nombreCompleto: valores.nombreCompleto.trim(),
        direccion: valores.direccion.trim(),
        ciudad: valores.ciudad.trim(),
        provincia: valores.provincia.trim(),
        codigoPostal: valores.codigoPostal.trim(),
        pais: valores.pais.trim(),
        telefono: valores.telefono.trim(),
      },
    };

    this.creandoPedido.set(true);
    this.errorPedido.set('');
    this.pedidosService.crear(datos).subscribe({
      next: (pedido) => {
        this.pedidoConfirmado.set(pedido);
        this.items.set([]);
        this.checkoutAbierto.set(false);
        this.creandoPedido.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorPedido.set(
          this.auth.errorMessage(error, 'No se ha podido realizar el pedido. Revisa la cesta e inténtalo de nuevo.'),
        );
        this.creandoPedido.set(false);
      },
    });
  }

  estaActualizando(productoId: string): boolean {
    return this.actualizandoIds().has(productoId);
  }

  campoEnvioInvalido(campo: keyof typeof this.checkoutForm.controls): boolean {
    const control = this.checkoutForm.controls[campo];
    return control.invalid && (control.touched || control.dirty);
  }

  formatearPrecio(precioCent: number): string {
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(precioCent / 100);
  }

  private limpiarActualizando(productoId: string): void {
    this.actualizandoIds.update((ids) => {
      const restantes = new Set(ids);
      restantes.delete(productoId);
      return restantes;
    });
  }

  private mensajeParaError(error: HttpErrorResponse): string {
    if (error.status === 0) return 'No se puede conectar con el servidor. Inténtalo de nuevo.';
    if (error.status === 401) return 'Tu sesión ha caducado. Inicia sesión de nuevo para consultar la cesta.';
    if (error.status === 403) return 'Solo los clientes pueden consultar y modificar su cesta.';
    if (error.status === 404) return 'No se ha encontrado el carrito o el producto que intentas modificar.';
    if (error.status === 400) return 'La cantidad indicada no es válida.';
    return `No se ha podido actualizar el carrito (HTTP ${error.status}).`;
  }
}
