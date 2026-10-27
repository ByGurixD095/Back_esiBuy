import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AuthService } from '../../services/auth.service';
import { CarritoService, ItemCarrito } from '../../services/carrito.service';
import { Carrito } from './carrito';

describe('Carrito', () => {
  let fixture: ComponentFixture<Carrito>;
  const carritoFalso = { obtenerCarrito: vi.fn(), actualizarCantidad: vi.fn() };
  const authFalso = {
    isAuthenticated: () => true,
    hasRole: () => true,
  };

  async function crear(items: ItemCarrito[]): Promise<void> {
    carritoFalso.obtenerCarrito.mockReturnValue(of(items));
    await TestBed.configureTestingModule({
      imports: [Carrito],
      providers: [
        provideRouter([]),
        { provide: CarritoService, useValue: carritoFalso },
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
});
