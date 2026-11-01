import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AuthService } from '../../services/auth.service';
import { CarritoService, ItemCarrito } from '../../services/carrito.service';
import { MetodoPago, MetodosPagoService } from '../../services/metodos-pago-service/metodos-pago';
import { PedidosService } from '../../services/pedidos.service';
import { Carrito } from './carrito';

describe('Carrito', () => {
  let fixture: ComponentFixture<Carrito>;
  const carritoFalso = { obtenerCarrito: vi.fn(), actualizarCantidad: vi.fn(), eliminarProducto: vi.fn() };
  const metodosPagoFalso = { listar: vi.fn() };
  const pedidosFalso = { crear: vi.fn() };
  const authFalso = {
    isAuthenticated: () => true,
    hasRole: () => true,
    errorMessage: (_error: unknown, fallback: string) => fallback,
  };

  async function crear(items: ItemCarrito[]): Promise<void> {
    carritoFalso.obtenerCarrito.mockReturnValue(of(items));
    await TestBed.configureTestingModule({
      imports: [Carrito],
      providers: [
        provideRouter([]),
        { provide: CarritoService, useValue: carritoFalso },
        { provide: MetodosPagoService, useValue: metodosPagoFalso },
        { provide: PedidosService, useValue: pedidosFalso },
        { provide: AuthService, useValue: authFalso },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Carrito);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  const html = () => fixture.nativeElement as HTMLElement;

  beforeEach(() => {
    carritoFalso.obtenerCarrito.mockReset();
    carritoFalso.actualizarCantidad.mockReset();
    carritoFalso.eliminarProducto.mockReset();
    metodosPagoFalso.listar.mockReset();
    metodosPagoFalso.listar.mockReturnValue(of([]));
    pedidosFalso.crear.mockReset();
  });

  it('muestra un estado informativo y enlace al catálogo con la cesta vacía', async () => {
    await crear([]);

    expect(html().textContent).toContain('Tu cesta está vacía');
    expect(html().querySelector('a[href="/products"]')).not.toBeNull();
  });

  it('muestra los artículos y calcula cantidad y subtotal', async () => {
    await crear([
      { productoId: 'p1', nombre: 'Mochila', precioCent: 2599, urlImagen: null, cantidad: 2 },
      { productoId: 'p2', nombre: 'Cuaderno', precioCent: 500, urlImagen: 'cuaderno.jpg', cantidad: 1 },
    ]);

    expect(html().textContent).toContain('Mochila');
    expect(html().textContent).toContain('Cuaderno');
    expect(html().textContent).toContain('3 unidades');
    expect(html().textContent).toContain('56,98');
  });

  it('persiste un cambio de cantidad y actualiza el subtotal', async () => {
    carritoFalso.actualizarCantidad.mockReturnValue(of(undefined));
    await crear([
      { productoId: 'p1', nombre: 'Mochila', precioCent: 2599, urlImagen: null, cantidad: 1 },
    ]);

    html().querySelector<HTMLButtonElement>('[aria-label="Aumentar cantidad de Mochila"]')?.click();
    fixture.detectChanges();

    expect(carritoFalso.actualizarCantidad).toHaveBeenCalledWith('p1', 2);
    expect(html().textContent).toContain('51,98');
  });

  it('muestra la ventana de confirmación y elimina el producto al confirmar', async () => {
    carritoFalso.eliminarProducto.mockReturnValue(of(undefined));
    await crear([
      { productoId: 'p1', nombre: 'Mochila', precioCent: 2599, urlImagen: null, cantidad: 2 },
    ]);

    html().querySelector<HTMLButtonElement>('[aria-label="Eliminar Mochila del carrito"]')?.click();
    fixture.detectChanges();

    expect(html().textContent).toContain(
      '¿Está seguro de que quiere eliminar Mochila del carrito?',
    );
    expect(carritoFalso.eliminarProducto).not.toHaveBeenCalled();

    html().querySelector<HTMLButtonElement>('.confirm-remove-button')?.click();
    fixture.detectChanges();

    expect(carritoFalso.eliminarProducto).toHaveBeenCalledWith('p1');
    expect(html().textContent).toContain('Tu cesta está vacía');
  });

  it('mantiene el producto si se cancela la ventana de confirmación', async () => {
    await crear([
      { productoId: 'p1', nombre: 'Mochila', precioCent: 2599, urlImagen: null, cantidad: 1 },
    ]);

    html().querySelector<HTMLButtonElement>('[aria-label="Eliminar Mochila del carrito"]')?.click();
    fixture.detectChanges();
    html().querySelector<HTMLButtonElement>('.cancel-button')?.click();
    fixture.detectChanges();

    expect(carritoFalso.eliminarProducto).not.toHaveBeenCalled();
    expect(html().textContent).toContain('Mochila');
    expect(html().querySelector('[role="alertdialog"]')).toBeNull();
  });

  it('envía la dirección y el método de pago y muestra la confirmación del pedido', async () => {
    const metodo: MetodoPago = {
      id: 'pago-1',
      tipo: 'TARJETA',
      alias: 'Visa personal',
      referencia: '**** 1234',
    };
    metodosPagoFalso.listar.mockReturnValue(of([metodo]));
    pedidosFalso.crear.mockReturnValue(of({ id: 'pedido-1', totalCent: 5698, estado: 'RECIBIDO' }));
    await crear([
      { productoId: 'p1', nombre: 'Mochila', precioCent: 2599, urlImagen: null, cantidad: 2 },
      { productoId: 'p2', nombre: 'Cuaderno', precioCent: 500, urlImagen: null, cantidad: 1 },
    ]);

    fixture.componentInstance.abrirCheckout();
    fixture.componentInstance.checkoutForm.setValue({
      metodoPagoId: 'pago-1',
      nombreCompleto: ' Ana García ',
      direccion: ' Calle Mayor 1 ',
      ciudad: ' Madrid ',
      provincia: ' Madrid ',
      codigoPostal: ' 28001 ',
      pais: ' España ',
      telefono: ' +34600000000 ',
    });
    fixture.componentInstance.confirmarPedido();
    fixture.detectChanges();

    expect(pedidosFalso.crear).toHaveBeenCalledWith({
      metodoPagoId: 'pago-1',
      direccionEnvio: {
        nombreCompleto: 'Ana García',
        direccion: 'Calle Mayor 1',
        ciudad: 'Madrid',
        provincia: 'Madrid',
        codigoPostal: '28001',
        pais: 'España',
        telefono: '+34600000000',
      },
    });
    expect(html().textContent).toContain('¡Pedido realizado!');
    expect(html().textContent).toContain('pedido-1');
    expect(html().textContent).toContain('56,98');
    expect(html().textContent).not.toContain('Mochila');
  });

  it('no envía el pedido con datos de envío incompletos', async () => {
    metodosPagoFalso.listar.mockReturnValue(
      of([{ id: 'pago-1', tipo: 'TARJETA', alias: 'Visa personal', referencia: '**** 1234' }]),
    );
    await crear([{ productoId: 'p1', nombre: 'Mochila', precioCent: 2599, urlImagen: null, cantidad: 1 }]);

    fixture.componentInstance.abrirCheckout();
    fixture.componentInstance.confirmarPedido();
    fixture.detectChanges();

    expect(pedidosFalso.crear).not.toHaveBeenCalled();
    expect(html().textContent).toContain('Completa los datos de envío');
    expect(html().textContent).toContain('Mochila');
  });
});
