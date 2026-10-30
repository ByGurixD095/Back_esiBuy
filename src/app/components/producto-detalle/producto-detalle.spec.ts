import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { BehaviorSubject, of } from 'rxjs';
import { vi } from 'vitest';
import { ProductInfo } from '../../models/producto.model';
import { AuthService } from '../../services/auth.service';
import { ProductService } from '../../services/product.service';
import { CarritoService } from '../../services/carrito.service';
import { ProductoDetalle } from './producto-detalle';

const detalleProducto: ProductInfo = {
  nombre: 'Camiseta ESI',
  numStock: 3,
  precioCent: 2000,
  descripcion: 'Camiseta oficial de la escuela.',
  categoria: 'Ropa',
  urlImagen: null,
  descuento: 10,
  descuentoPremium: 5,
  vendedor: 'Tienda ESI',
};

describe('ProductoDetalle', () => {
  const productoServiceFalso = { obtenerInfo: vi.fn() };
  const carritoFalso = { anadirProducto: vi.fn(), obtenerCarrito: vi.fn() };
  const authFalso = { hasRole: vi.fn() };
  let paramMap: BehaviorSubject<ReturnType<typeof convertToParamMap>>;

  beforeEach(() => {
    paramMap = new BehaviorSubject(convertToParamMap({ id: 'producto-1' }));
    productoServiceFalso.obtenerInfo.mockReset().mockReturnValue(of(detalleProducto));
    carritoFalso.anadirProducto.mockReset();
    carritoFalso.obtenerCarrito.mockReset().mockReturnValue(of([]));
    authFalso.hasRole.mockReset().mockReturnValue(false);
  });

  it('muestra la información disponible y una opción de compra claramente deshabilitada', async () => {
    await TestBed.configureTestingModule({
      imports: [ProductoDetalle],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { paramMap } },
        { provide: ProductService, useValue: productoServiceFalso },
        { provide: CarritoService, useValue: carritoFalso },
        { provide: AuthService, useValue: authFalso },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(ProductoDetalle);
    fixture.detectChanges();

    const html = fixture.nativeElement as HTMLElement;
    expect(productoServiceFalso.obtenerInfo).toHaveBeenCalledWith('producto-1');
    expect(html.querySelector('h1')?.textContent).toContain('Camiseta ESI');
    expect(html.textContent).toContain('Tienda ESI');
    expect(html.textContent).toContain('18,00');
    expect(html.textContent).toContain('3 unidades en stock');
    expect(html.textContent).toContain(
      'Los comentarios y las estadísticas estarán disponibles próximamente.',
    );
    expect(html.querySelector<HTMLButtonElement>('.checkout-button')?.disabled).toBe(true);
    expect(html.textContent).toContain('La compra online todavía no está disponible en EsiBuy.');
  });

  it('vuelve a consultar la información cuando cambia el producto de la ruta', async () => {
    await TestBed.configureTestingModule({
      imports: [ProductoDetalle],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { paramMap } },
        { provide: ProductService, useValue: productoServiceFalso },
        { provide: CarritoService, useValue: carritoFalso },
        { provide: AuthService, useValue: authFalso },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(ProductoDetalle);
    fixture.detectChanges();

    paramMap.next(convertToParamMap({ id: 'producto-2' }));
    fixture.detectChanges();

    expect(productoServiceFalso.obtenerInfo).toHaveBeenNthCalledWith(2, 'producto-2');
  });

  it('permite al cliente añadir el producto a la cesta desde el detalle', async () => {
    authFalso.hasRole.mockImplementation((roles: string[]) => roles.includes('CLIENTE'));
    carritoFalso.anadirProducto.mockReturnValue(of(undefined));
    carritoFalso.obtenerCarrito.mockReturnValue(of([
      { productoId: 'otro', nombre: 'Otro', precioCent: 100, urlImagen: null, cantidad: 2 },
    ]));
    await TestBed.configureTestingModule({
      imports: [ProductoDetalle],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { paramMap } },
        { provide: ProductService, useValue: productoServiceFalso },
        { provide: CarritoService, useValue: carritoFalso },
        { provide: AuthService, useValue: authFalso },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(ProductoDetalle);
    fixture.detectChanges();

    const html = fixture.nativeElement as HTMLElement;
    html.querySelector<HTMLButtonElement>('.buy-button:not(.checkout-button)')?.click();
    fixture.detectChanges();

    expect(carritoFalso.anadirProducto).toHaveBeenCalledWith('producto-1');
    expect(html.textContent).toContain('Camiseta ESI se ha añadido a tu cesta.');
    expect(html.querySelector('.cart-link[href="/cart"]')?.textContent).toContain('3');
    expect(html.querySelector('.cart-link')?.getAttribute('aria-label')).toContain('3 productos');
  });
});
