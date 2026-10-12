import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { afterEach, vi } from 'vitest';
import { AuthService } from '../../services/auth.service';
import { Usuarios, type Usuario } from '../../services/consultar-usuarios-service/usuarios';
import { ConsultarUsuarios } from './consultar-usuarios';

describe('ConsultarUsuarios', () => {
  let fixture: ComponentFixture<ConsultarUsuarios>;
  const servicioFalso = {
    consultar: vi.fn(),
    consultarPorId: vi.fn(),
    eliminar: vi.fn(),
    bloquear: vi.fn(),
    desbloquear: vi.fn(),
    activar: vi.fn(),
  };
  const authServiceFalso = { email: signal<string | null>('admin@test.com') };
  const usuarios: Usuario[] = [
    {
      id: 'admin-1',
      name: 'Admin',
      apellidos: 'Prueba',
      email: 'admin@test.com',
      rol: 'ADMINISTRADOR',
      activo: true,
      bloqueado: false,
      eliminado: false,
    },
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
    servicioFalso.consultarPorId.mockReset();
    servicioFalso.consultarPorId.mockReturnValue(
      of({
        ...usuarios[1],
        telefono: '600123456',
        dni: '12345678A',
        fechaNacimiento: '1990-01-01',
        tipoCliente: 'PREMIUM',
      }),
    );
    servicioFalso.eliminar.mockReset();
    servicioFalso.eliminar.mockReturnValue(of(undefined));
    servicioFalso.bloquear.mockReset();
    servicioFalso.bloquear.mockReturnValue(of({ bloqueado: true }));
    servicioFalso.desbloquear.mockReset();
    servicioFalso.desbloquear.mockReturnValue(of({ bloqueado: false }));
    servicioFalso.activar.mockReset();
    servicioFalso.activar.mockReturnValue(of({ activo: true }));
    authServiceFalso.email.set('admin@test.com');
    await TestBed.configureTestingModule({
      imports: [ConsultarUsuarios],
      providers: [
        provideRouter([]),
        { provide: Usuarios, useValue: servicioFalso },
        { provide: AuthService, useValue: authServiceFalso },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ConsultarUsuarios);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
  });

  it('carga desde el servicio los usuarios y sus tres estados', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(servicioFalso.consultar).toHaveBeenCalledOnce();
    expect(el.textContent).toContain('Gestionar usuarios.');
    expect(el.textContent).toContain('user-db-1');
    expect(el.textContent).toContain('Ana López');
    expect(el.textContent).toContain('user-db-2');
    expect(el.textContent).toContain('Inactivo');
    expect(el.querySelector('.bloqueado')?.textContent?.trim()).toBe('Sí');
    expect(el.querySelector('.eliminado')?.textContent?.trim()).toBe('Sí');
  });

  it('pagina en el frontend y vuelve a la primera página al filtrar', () => {
    const usuariosPaginados: Usuario[] = Array.from({ length: 40 }, (_, indice) => ({
      id: `user-page-${indice}`,
      name: `Usuario${indice}`,
      apellidos: 'Prueba',
      email: `usuario${indice}@test.com`,
      rol: 'CLIENTE',
      activo: true,
      bloqueado: false,
      eliminado: false,
    }));
    const componente = fixture.componentInstance;
    const el = fixture.nativeElement as HTMLElement;
    componente.usuarios.set(usuariosPaginados);
    fixture.detectChanges();

    expect(el.querySelectorAll('tbody tr').length).toBe(componente.tamanoPagina);
    (el.querySelector('.paginacion button:last-child') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(el.querySelector('.paginacion')?.textContent).toContain('Página 2 de 2');
    expect(el.querySelectorAll('tbody tr').length).toBe(4);
    expect(el.textContent).toContain('user-page-39');

    const campoBusqueda = el.querySelector('#buscar-id') as HTMLInputElement;
    campoBusqueda.value = 'user-page-0';
    campoBusqueda.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(componente.paginaActual()).toBe(0);
    expect(el.querySelectorAll('tbody tr').length).toBe(1);
    expect(el.querySelector('.paginacion')).toBeNull();
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
    expect(el.textContent).toContain(
      'No hay usuarios que coincidan con los filtros seleccionados.',
    );

    campoBusqueda.value = '';
    campoBusqueda.dispatchEvent(new Event('input'));
    filtro.value = 'no-eliminados';
    filtro.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    filas = el.querySelectorAll('tbody tr');
    expect(filas.length).toBe(2);
    expect(Array.from(filas).some((fila) => fila.textContent?.includes('user-db-1'))).toBe(true);
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

  it('abre la ficha del usuario al seleccionar su fila y carga sus datos completos', () => {
    const el = fixture.nativeElement as HTMLElement;
    const filaUsuario = Array.from(el.querySelectorAll('tbody tr')).find((fila) =>
      fila.textContent?.includes('Ana López'),
    ) as HTMLTableRowElement;

    filaUsuario.click();
    fixture.detectChanges();

    expect(servicioFalso.consultarPorId).toHaveBeenCalledWith('user-db-1');
    expect(el.querySelector('dialog.detalle-dialogo[open]')).not.toBeNull();
    expect(el.textContent).toContain('Ficha de usuario');
    expect(el.textContent).toContain('12345678A');
    expect(el.textContent).toContain('PREMIUM');
    expect(el.textContent).toContain('600123456');

    (el.querySelector('[aria-label="Cerrar ficha de usuario"]') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(el.querySelector('dialog.detalle-dialogo[open]')).toBeNull();
  });

  it('permite abrir la edición y ofrece bloquear o desbloquear según el estado', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('[aria-label="Modificar usuario Ana López"]')).not.toBeNull();
    expect(
      (
        el.querySelector('[aria-label="Modificar usuario Ana López"]') as HTMLAnchorElement
      ).getAttribute('href'),
    ).toBe('/admin/usuarios/user-db-1/editar');
    expect(
      el.querySelector('[aria-label="No se puede modificar un usuario eliminado"]'),
    ).not.toBeNull();
    expect(el.querySelector('[aria-label="Bloquear usuario Ana López"]')).not.toBeNull();
    expect(el.querySelector('[aria-label="Desbloquear usuario Luis Pérez"]')).not.toBeNull();
    expect(el.querySelectorAll('.accion-bloqueo:disabled').length).toBe(1);
    expect(el.querySelector('[aria-label="Eliminar usuario"]:disabled')).toBeNull();
    expect(el.querySelector('.aviso-boceto')).toBeNull();

    expect(el.querySelectorAll('.bloqueado').length).toBe(1);
    expect(el.querySelectorAll('.eliminado').length).toBe(1);
    expect(
      el.querySelector('[aria-label="No puedes eliminar tu propia cuenta"]:disabled'),
    ).not.toBeNull();
    expect(servicioFalso.consultar).toHaveBeenCalledOnce();
  });

  it('confirma el bloqueo y actualiza el estado del usuario', () => {
    const el = fixture.nativeElement as HTMLElement;
    (el.querySelector('[aria-label="Bloquear usuario Ana López"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(el.querySelector('dialog[open]')).not.toBeNull();
    expect(el.textContent).toContain('¿Quieres bloquear a Ana López?');
    expect(servicioFalso.bloquear).not.toHaveBeenCalled();

    (el.querySelector('.confirmacion-eliminar') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(servicioFalso.bloquear).toHaveBeenCalledWith('user-db-1');
    expect(el.textContent).toContain('Se ha bloqueado a Ana López.');
    expect(el.querySelector('[aria-label="Desbloquear usuario Ana López"]')).not.toBeNull();
  });

  it('confirma el desbloqueo y actualiza el estado del usuario', () => {
    const el = fixture.nativeElement as HTMLElement;
    fixture.componentInstance.usuarios.update((lista) =>
      lista.map((usuario) =>
        usuario.id === 'user-db-2' ? { ...usuario, eliminado: false } : usuario,
      ),
    );
    fixture.detectChanges();
    (
      el.querySelector('[aria-label="Desbloquear usuario Luis Pérez"]') as HTMLButtonElement
    ).click();
    fixture.detectChanges();

    expect(el.textContent).toContain('¿Quieres permitir de nuevo el acceso a Luis Pérez?');
    (el.querySelector('.confirmacion-eliminar') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(servicioFalso.desbloquear).toHaveBeenCalledWith('user-db-2');
    expect(el.textContent).toContain('Se ha desbloqueado a Luis Pérez.');
    expect(el.querySelector('[aria-label="Bloquear usuario Luis Pérez"]')).not.toBeNull();
  });

  it('confirma la activación de un usuario inactivo y actualiza su estado', () => {
    const el = fixture.nativeElement as HTMLElement;
    fixture.componentInstance.usuarios.update((lista) =>
      lista.map((usuario) =>
        usuario.id === 'user-db-2'
          ? { ...usuario, eliminado: false, bloqueado: false, activo: false }
          : usuario,
      ),
    );
    fixture.detectChanges();
    expect(el.querySelector('[aria-label="Activar usuario Ana López"]')).toBeNull();
    (el.querySelector('[aria-label="Activar usuario Luis Pérez"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(el.textContent).toContain('¿Quieres activar a Luis Pérez?');
    expect(servicioFalso.activar).not.toHaveBeenCalled();
    (el.querySelector('.confirmacion-eliminar') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(servicioFalso.activar).toHaveBeenCalledWith('user-db-2');
    expect(el.textContent).toContain('Se ha activado a Luis Pérez.');
    expect(el.querySelector('[aria-label="Activar usuario Luis Pérez"]')).toBeNull();
  });

  it('no abre confirmación ni envía la petición para la cuenta de la sesión', () => {
    const el = fixture.nativeElement as HTMLElement;
    const botonEliminar = el.querySelector(
      '[aria-label="No puedes eliminar tu propia cuenta"]',
    ) as HTMLButtonElement;

    expect(botonEliminar.disabled).toBe(true);
    botonEliminar.click();
    expect(el.querySelector('dialog[open]')).toBeNull();
    expect(servicioFalso.eliminar).not.toHaveBeenCalled();
  });

  it('muestra una ventana en página y elimina tras confirmar', () => {
    const el = fixture.nativeElement as HTMLElement;
    const botonEliminar = el.querySelector('[aria-label="Eliminar usuario"]') as HTMLButtonElement;

    botonEliminar.click();
    fixture.detectChanges();

    expect(el.querySelector('dialog[open]')).not.toBeNull();
    expect(el.textContent).toContain('¿Estás segura de que quieres eliminar a Ana López?');
    expect(servicioFalso.eliminar).not.toHaveBeenCalled();

    (el.querySelector('.confirmacion-eliminar') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(servicioFalso.eliminar).toHaveBeenCalledWith('user-db-1');
    expect(el.textContent).toContain('Se ha marcado como eliminado a Ana López.');
    expect(el.querySelector('.mensaje-accion')).not.toBeNull();
    expect(el.querySelectorAll('.eliminado').length).toBe(2);
    expect(el.querySelector('[aria-label="Usuario ya eliminado"]')).not.toBeNull();
    expect(el.querySelector('dialog[open]')).toBeNull();
  });

  it('oculta el mensaje de éxito a los cinco segundos', () => {
    vi.useFakeTimers();
    const el = fixture.nativeElement as HTMLElement;
    (el.querySelector('[aria-label="Eliminar usuario"]') as HTMLButtonElement).click();
    fixture.detectChanges();
    (el.querySelector('.confirmacion-eliminar') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(el.querySelector('.mensaje-accion')).not.toBeNull();
    vi.advanceTimersByTime(4999);
    fixture.detectChanges();
    expect(el.querySelector('.mensaje-accion')).not.toBeNull();

    vi.advanceTimersByTime(1);
    fixture.detectChanges();
    expect(el.querySelector('.mensaje-accion')).toBeNull();
  });

  it('cierra la ventana al cancelar sin eliminar al usuario', () => {
    const el = fixture.nativeElement as HTMLElement;
    (el.querySelector('[aria-label="Eliminar usuario"]') as HTMLButtonElement).click();
    fixture.detectChanges();
    (el.querySelector('.confirmacion-acciones button') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(el.querySelector('dialog[open]')).toBeNull();
    expect(servicioFalso.eliminar).not.toHaveBeenCalled();
  });

  it('muestra el error devuelto si la eliminación falla', () => {
    servicioFalso.eliminar.mockReturnValueOnce(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 403,
            error: { message: 'No puedes eliminar tu propia cuenta' },
          }),
      ),
    );
    const el = fixture.nativeElement as HTMLElement;
    (el.querySelector('[aria-label="Eliminar usuario"]') as HTMLButtonElement).click();
    fixture.detectChanges();
    (el.querySelector('.confirmacion-eliminar') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(el.querySelector('[role="alert"]')?.textContent).toContain(
      'No puedes eliminar tu propia cuenta',
    );
    expect(el.querySelectorAll('.eliminado').length).toBe(1);
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
