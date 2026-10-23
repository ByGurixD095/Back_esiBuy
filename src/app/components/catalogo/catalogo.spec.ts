import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { PaginaProducto, Producto } from '../../models/producto.model';
import { AuthService } from '../../services/auth.service';
import { Favoritos, FavoritosApiError } from '../../services/favoritos-service/favoritos';
import { ProductService } from '../../services/product.service';
import { Catalogo } from './catalogo';

function producto(id: string, nombre: string): Producto {
  return {
    id,
    nombre,
    referencia: `REF-${id}`,
    numStock: 5,
    precioCent: 10000,
    descripcion: null,
    categoria: 'HOGAR',
    urlImagen: null,
    descuento: 0,
    descuentoPremium: 0,
    activo: true,
  };
}

const pagina: PaginaProducto = {
  content: [producto('p1', 'Lámpara'), producto('p2', 'Mesa')],
  totalElements: 2,
  totalPages: 1,
  number: 0,
};

describe('Catalogo (favoritos HU 15)', () => {
  let fixture: ComponentFixture<Catalogo>;
  const productServiceFalso = { obtenerDisponibles: vi.fn() };
  const favoritosFalso = { listar: vi.fn(), marcar: vi.fn(), quitar: vi.fn() };
  const authFalso = { isAuthenticated: vi.fn(), hasRole: vi.fn() };

  async function crear(rol: 'CLIENTE' | 'VENDEDOR' | null): Promise<void> {
    authFalso.isAuthenticated.mockReturnValue(rol !== null);
    authFalso.hasRole.mockImplementation((roles: string[]) => rol !== null && roles.includes(rol));

    await TestBed.configureTestingModule({
      imports: [Catalogo],
      providers: [
        provideRouter([]),
        { provide: ProductService, useValue: productServiceFalso },
        { provide: Favoritos, useValue: favoritosFalso },
        { provide: AuthService, useValue: authFalso },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Catalogo);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  const html = () => fixture.nativeElement as HTMLElement;
  const corazones = () => html().querySelectorAll<HTMLButtonElement>('.favorite-button');

  beforeEach(() => {
    productServiceFalso.obtenerDisponibles.mockReset();
    productServiceFalso.obtenerDisponibles.mockReturnValue(of(pagina));
    favoritosFalso.listar.mockReset();
    favoritosFalso.listar.mockReturnValue(of([producto('p1', 'Lámpara')]));
    favoritosFalso.marcar.mockReset();
    favoritosFalso.quitar.mockReset();
  });

  it('no muestra el corazón ni pide favoritos a un visitante', async () => {
    await crear(null);

    expect(corazones().length).toBe(0);
    expect(favoritosFalso.listar).not.toHaveBeenCalled();
  });

  it('no muestra el corazón a un vendedor', async () => {
    await crear('VENDEDOR');

    expect(corazones().length).toBe(0);
    expect(favoritosFalso.listar).not.toHaveBeenCalled();
    expect(html().textContent).not.toContain('Mis favoritos');
  });

  it('a un cliente le muestra el enlace y marca los productos que ya son favoritos', async () => {
    await crear('CLIENTE');

    expect(html().textContent).toContain('Mis favoritos');
    expect(corazones().length).toBe(2);
    expect(corazones()[0].getAttribute('aria-pressed')).toBe('true');
    expect(corazones()[0].classList).toContain('is-favorite');
    expect(corazones()[1].getAttribute('aria-pressed')).toBe('false');
  });

  it('guarda un producto como favorito al pulsar el corazón vacío', async () => {
    favoritosFalso.marcar.mockReturnValue(of(undefined));
    await crear('CLIENTE');

    corazones()[1].click();
    fixture.detectChanges();

    expect(favoritosFalso.marcar).toHaveBeenCalledWith('p2');
    expect(corazones()[1].getAttribute('aria-pressed')).toBe('true');
  });

  it('quita el favorito al pulsar un corazón ya marcado', async () => {
    favoritosFalso.quitar.mockReturnValue(of(undefined));
    await crear('CLIENTE');

    corazones()[0].click();
    fixture.detectChanges();

    expect(favoritosFalso.quitar).toHaveBeenCalledWith('p1');
    expect(corazones()[0].getAttribute('aria-pressed')).toBe('false');
  });

  it('avisa del error y deja el corazón como estaba si falla guardar', async () => {
    favoritosFalso.marcar.mockReturnValue(
      throwError(() => new FavoritosApiError(404, 'Este producto ya no está disponible.')),
    );
    await crear('CLIENTE');

    corazones()[1].click();
    fixture.detectChanges();

    expect(html().querySelector('.favorite-error[role="alert"]')?.textContent).toContain('ya no está disponible');
    expect(corazones()[1].getAttribute('aria-pressed')).toBe('false');
  });
});
