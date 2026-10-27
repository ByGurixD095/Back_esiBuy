import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { AuthService } from '../../services/auth.service';
import { Categorias } from '../../services/categorias.service';
import { ListarCategorias } from './listar-categorias';

describe('ListarCategorias', () => {
  let fixture: ComponentFixture<ListarCategorias>;
  const servicioFalso = { listar: vi.fn() };
  const authFalso = { isAuthenticated: () => true, hasRole: () => true };

  async function crear(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [ListarCategorias],
      providers: [
        provideRouter([]),
        { provide: Categorias, useValue: servicioFalso },
        { provide: AuthService, useValue: authFalso },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ListarCategorias);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  const html = () => fixture.nativeElement as HTMLElement;

  beforeEach(() => servicioFalso.listar.mockReset());

  it('se crea y pide las categorías al servicio', async () => {
    servicioFalso.listar.mockReturnValue(of([]));
    await crear();

    expect(fixture.componentInstance).toBeTruthy();
    expect(servicioFalso.listar).toHaveBeenCalledOnce();
    expect(html().textContent).toContain('catálogo.');
  });

  it('muestra cada categoría con su nombre y el contador', async () => {
    servicioFalso.listar.mockReturnValue(
      of([
        { id: 'c1', nombre: 'Hogar' },
        { id: 'c2', nombre: 'Ropa' },
      ]),
    );
    await crear();

    const elementos = html().querySelectorAll('.elemento');
    expect(elementos.length).toBe(2);
    expect(elementos[0].textContent).toContain('Hogar');
    expect(elementos[1].textContent).toContain('Ropa');
    expect(html().textContent).toContain('2 categorías');
  });

  it('usa el singular cuando solo hay una categoría', async () => {
    servicioFalso.listar.mockReturnValue(of([{ id: 'c1', nombre: 'Hogar' }]));
    await crear();

    expect(html().textContent).toContain('1 categoría');
    expect(html().textContent).not.toContain('1 categorías');
  });

  it('muestra el estado vacío cuando no hay categorías', async () => {
    servicioFalso.listar.mockReturnValue(of([]));
    await crear();

    expect(html().textContent).toContain('Todavía no hay categorías');
    expect(html().querySelectorAll('.elemento').length).toBe(0);
  });

  it('ofrece un enlace para crear una categoría nueva', async () => {
    servicioFalso.listar.mockReturnValue(of([]));
    await crear();

    const enlace = html().querySelector('a[href="/admin/categorias/nueva"]');
    expect(enlace?.textContent).toContain('Nueva categoría');
  });

  it('muestra el error de permisos y permite reintentar', async () => {
    servicioFalso.listar.mockReturnValueOnce(
      throwError(() => new HttpErrorResponse({ status: 403 })),
    );
    await crear();

    const alerta = html().querySelector('[role="alert"]');
    expect(alerta?.textContent).toContain('No tienes permisos para ver las categorías');

    servicioFalso.listar.mockReturnValue(of([{ id: 'c1', nombre: 'Hogar' }]));
    (alerta?.querySelector('button') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(servicioFalso.listar).toHaveBeenCalledTimes(2);
    expect(html().querySelectorAll('.elemento').length).toBe(1);
  });

  it('avisa si no hay conexión con el servidor', async () => {
    servicioFalso.listar.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 0 })));
    await crear();

    expect(html().querySelector('[role="alert"]')?.textContent).toContain(
      'No se ha podido conectar con el servidor',
    );
  });
});
