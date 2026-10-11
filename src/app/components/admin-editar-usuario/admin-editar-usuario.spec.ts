import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { afterEach, vi } from 'vitest';
import { Usuarios } from '../../services/consultar-usuarios-service/usuarios';
import { AdminEditarUsuario } from './admin-editar-usuario';

describe('AdminEditarUsuario', () => {
  let fixture: ComponentFixture<AdminEditarUsuario>;
  const servicioFalso = { consultarPorId: vi.fn(), modificar: vi.fn() };
  const cliente = {
    id: 'user-1',
    name: 'Ana',
    apellidos: 'López',
    email: 'ana@test.com',
    telefono: '612345678',
    imagenPerfil: 'https://example.com/ana.jpg',
    rol: 'CLIENTE' as const,
    activo: true,
    bloqueado: false,
    eliminado: false,
    dni: '12345678A',
    fechaNacimiento: '1990-01-01',
    tipoCliente: 'NORMAL' as const,
  };

  beforeEach(async () => {
    servicioFalso.consultarPorId.mockReset().mockReturnValue(of(cliente));
    servicioFalso.modificar.mockReset().mockReturnValue(of(undefined));
    await TestBed.configureTestingModule({
      imports: [AdminEditarUsuario],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => 'user-1' } } } },
        { provide: Usuarios, useValue: servicioFalso },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminEditarUsuario);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  afterEach(() => vi.restoreAllMocks());

  it('carga los datos personales y los campos propios del rol', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(servicioFalso.consultarPorId).toHaveBeenCalledWith('user-1');
    expect((el.querySelector('#nombre') as HTMLInputElement).value).toBe('Ana');
    expect((el.querySelector('#dni') as HTMLInputElement).value).toBe('12345678A');
    expect((el.querySelector('#tipoCliente') as HTMLSelectElement).value).toBe('NORMAL');
    expect(el.querySelector('#fechaNacimiento')).not.toBeNull();
    expect(el.querySelector('#sede')).toBeNull();
    expect((el.querySelector('#usuario-email') as HTMLInputElement).readOnly).toBe(true);
  });

  it('envía los campos editables del usuario y confirma el guardado', () => {
    const el = fixture.nativeElement as HTMLElement;
    const nombre = el.querySelector('#nombre') as HTMLInputElement;
    nombre.value = '123456789012345678901234567890EXTRA';
    nombre.dispatchEvent(new Event('input'));
    const apellidos = el.querySelector('#apellidos') as HTMLInputElement;
    apellidos.value = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ123456';
    apellidos.dispatchEvent(new Event('input'));
    const tipoCliente = el.querySelector('#tipoCliente') as HTMLSelectElement;
    tipoCliente.value = 'PREMIUM';
    tipoCliente.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    (el.querySelector('button[type="submit"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(servicioFalso.modificar).toHaveBeenCalledWith('user-1', {
      nombre: '123456789012345678901234567890',
      apellidos: 'ABCDEFGHIJKLMNOPQRSTUVWXYZ1234',
      telefono: '612345678',
      imagenPerfil: 'https://example.com/ana.jpg',
      dni: '12345678A',
      fechaNacimiento: '1990-01-01',
      tipoCliente: 'PREMIUM',
    });
    expect(nombre.maxLength).toBe(30);
    expect(apellidos.maxLength).toBe(30);
    expect(el.textContent).toContain('Los datos del usuario se han actualizado correctamente.');
  });

  it('muestra el error del servidor si no se pueden guardar los cambios', () => {
    servicioFalso.modificar.mockReturnValueOnce(
      throwError(() => new HttpErrorResponse({ status: 400, error: { message: 'DNI no válido' } })),
    );
    const el = fixture.nativeElement as HTMLElement;
    (el.querySelector('button[type="submit"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(el.querySelector('[role="alert"]')?.textContent).toContain('DNI no válido');
  });

  it('muestra y guarda los campos editables de un vendedor', async () => {
    servicioFalso.consultarPorId.mockReturnValueOnce(
      of({
        ...cliente,
        rol: 'VENDEDOR',
        nombreComercial: 'Tienda de Ana',
        cifNif: 'B12345678',
        categoriaPrincipalId: 'categoria-1',
      }),
    );
    const vendedorFixture = TestBed.createComponent(AdminEditarUsuario);
    vendedorFixture.detectChanges();
    await vendedorFixture.whenStable();
    vendedorFixture.detectChanges();

    const el = vendedorFixture.nativeElement as HTMLElement;
    expect(el.querySelector('#nombreComercial')).not.toBeNull();
    expect(el.querySelector('#categoriaPrincipalId')).not.toBeNull();
    expect(el.querySelector('#dni')).toBeNull();
    (el.querySelector('button[type="submit"]') as HTMLButtonElement).click();
    vendedorFixture.detectChanges();

    expect(servicioFalso.modificar).toHaveBeenCalledWith('user-1', {
      nombre: 'Ana',
      apellidos: 'López',
      telefono: '612345678',
      imagenPerfil: 'https://example.com/ana.jpg',
      nombreComercial: 'Tienda de Ana',
      cifNif: 'B12345678',
      categoriaPrincipalId: 'categoria-1',
    });
  });

  it('muestra y guarda la sede de un administrador', async () => {
    servicioFalso.consultarPorId.mockReturnValueOnce(
      of({
        ...cliente,
        rol: 'ADMINISTRADOR',
        sede: 'Madrid',
        fechaIncorporacion: '2024-02-01',
      }),
    );
    const administradorFixture = TestBed.createComponent(AdminEditarUsuario);
    administradorFixture.detectChanges();
    await administradorFixture.whenStable();
    administradorFixture.detectChanges();

    const el = administradorFixture.nativeElement as HTMLElement;
    expect((el.querySelector('#sede') as HTMLInputElement).value).toBe('Madrid');
    expect((el.querySelector('#fechaIncorporacion') as HTMLInputElement).value).toBe('2024-02-01');
    expect((el.querySelector('#fechaIncorporacion') as HTMLInputElement).readOnly).toBe(true);
    expect(el.querySelector('#dni')).toBeNull();
    (el.querySelector('button[type="submit"]') as HTMLButtonElement).click();
    administradorFixture.detectChanges();

    expect(servicioFalso.modificar).toHaveBeenCalledWith('user-1', {
      nombre: 'Ana',
      apellidos: 'López',
      telefono: '612345678',
      imagenPerfil: 'https://example.com/ana.jpg',
      sede: 'Madrid',
    });
  });
});
