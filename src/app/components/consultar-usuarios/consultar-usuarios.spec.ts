import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { Usuario } from '../../models/usuario.model';
import { Usuarios } from '../../services/consultar-usuarios-service/usuarios';
import { ConsultarUsuarios } from './consultar-usuarios';

describe('ConsultarUsuarios', () => {
  let fixture: ComponentFixture<ConsultarUsuarios>;
  const servicioFalso = { consultar: vi.fn() };
  const usuarios: Usuario[] = [
    {
      id: 'user-db-1',
      name: 'Ana',
      apellidos: 'López',
      email: 'ana@test.com',
      rol: 'CLIENTE',
      activo: true,
      bloqueado: false,
      eliminado: false,
    },
    {
      id: 'user-db-2',
      name: 'Luis',
      apellidos: 'Pérez',
      email: 'luis@test.com',
      rol: 'VENDEDOR',
      activo: false,
      bloqueado: true,
      eliminado: true,
    },
  ];

  beforeEach(async () => {
    servicioFalso.consultar.mockReset();
    servicioFalso.consultar.mockReturnValue(of(usuarios));
    await TestBed.configureTestingModule({
      imports: [ConsultarUsuarios],
      providers: [provideRouter([]), { provide: Usuarios, useValue: servicioFalso }],
    }).compileComponents();

    fixture = TestBed.createComponent(ConsultarUsuarios);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('carga desde el servicio los usuarios y sus tres estados', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(servicioFalso.consultar).toHaveBeenCalledOnce();
    expect(el.textContent).toContain('gestionar usuarios.');
    expect(el.textContent).toContain('user-db-1');
    expect(el.textContent).toContain('Ana López');
    expect(el.textContent).toContain('user-db-2');
    expect(el.textContent).toContain('Inactivo');
    expect(el.querySelector('.bloqueado')?.textContent?.trim()).toBe('Sí');
    expect(el.querySelector('.eliminado')?.textContent?.trim()).toBe('Sí');
  });

  it('filtra los usuarios por coincidencia parcial del ID', () => {
    const campoBusqueda = (fixture.nativeElement as HTMLElement).querySelector(
      '#buscar-id',
    ) as HTMLInputElement;
    campoBusqueda.value = 'db-2';
    campoBusqueda.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const filas = (fixture.nativeElement as HTMLElement).querySelectorAll('tbody tr');
    expect(filas.length).toBe(1);
    expect(filas[0].textContent).toContain('user-db-2');
  });

  it('filtra solo usuarios eliminados y permite combinarlos con la búsqueda por ID', () => {
    const el = fixture.nativeElement as HTMLElement;
    const filtro = el.querySelector('#filtro-eliminados') as HTMLSelectElement;
    filtro.value = 'eliminados';
    filtro.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    let filas = el.querySelectorAll('tbody tr');
    expect(filas.length).toBe(1);
    expect(filas[0].textContent).toContain('user-db-2');

    const campoBusqueda = el.querySelector('#buscar-id') as HTMLInputElement;
    campoBusqueda.value = 'db-1';
    campoBusqueda.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(el.textContent).toContain('No hay usuarios que coincidan con los filtros seleccionados.');

    campoBusqueda.value = '';
    campoBusqueda.dispatchEvent(new Event('input'));
    filtro.value = 'no-eliminados';
    filtro.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    filas = el.querySelectorAll('tbody tr');
    expect(filas.length).toBe(1);
    expect(filas[0].textContent).toContain('user-db-1');
  });

  it('muestra un mensaje si los filtros no tienen resultados', () => {
    const campoBusqueda = (fixture.nativeElement as HTMLElement).querySelector(
      '#buscar-id',
    ) as HTMLInputElement;
    campoBusqueda.value = 'no-existe';
    campoBusqueda.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'No hay usuarios que coincidan con los filtros seleccionados.',
    );
  });

  it('muestra las acciones como boceto sin cambiar los estados de la base de datos', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('[aria-label="Modificar usuario, próximamente"]')).not.toBeNull();
    expect(el.querySelector('[aria-label="Bloquear usuario, próximamente"]')).not.toBeNull();
    expect(el.querySelector('[aria-label="Desbloquear usuario, próximamente"]')).not.toBeNull();
    expect(el.querySelector('[aria-label="Eliminar usuario, próximamente"]')).not.toBeNull();
    expect(el.querySelectorAll('.accion-boceto:disabled').length).toBeGreaterThan(0);
    expect(el.textContent).toContain('no realizan cambios');

    expect(el.querySelectorAll('.bloqueado').length).toBe(1);
    expect(el.querySelectorAll('.eliminado').length).toBe(1);
    expect(servicioFalso.consultar).toHaveBeenCalledOnce();
  });

  it('muestra el estado vacío si el back devuelve una lista vacía', async () => {
    servicioFalso.consultar.mockReturnValue(of([]));
    const nuevaFixture = TestBed.createComponent(ConsultarUsuarios);
    nuevaFixture.detectChanges();
    await nuevaFixture.whenStable();
    nuevaFixture.detectChanges();

    expect((nuevaFixture.nativeElement as HTMLElement).textContent).toContain(
      'Todavía no hay usuarios registrados.',
    );
  });

  it('muestra un error y permite reintentar si falla la consulta', async () => {
    servicioFalso.consultar
      .mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 500 })))
      .mockReturnValueOnce(of(usuarios));
    const nuevaFixture = TestBed.createComponent(ConsultarUsuarios);
    nuevaFixture.detectChanges();
    await nuevaFixture.whenStable();
    nuevaFixture.detectChanges();

    const el = nuevaFixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('No se han podido cargar los usuarios.');
    (el.querySelector('button') as HTMLButtonElement).click();
    await nuevaFixture.whenStable();
    nuevaFixture.detectChanges();
    expect(el.textContent).toContain('user-db-1');
  });
});
