import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { Autenticacion } from '../../services/autenticacion-service/autenticacion';
import { Registro as RegistroService } from '../../services/registro-service/registro';
import { RegistroComponent } from './registro';

describe('RegistroComponent', () => {
  let component: RegistroComponent;
  let fixture: ComponentFixture<RegistroComponent>;

  const servicioFalso = {
    registrarCliente: vi.fn(),
    registrarVendedor: vi.fn(),
  };
  const autenticacionFalsa = { iniciarSesion: vi.fn() };
  const routerFalso = { navigateByUrl: vi.fn().mockResolvedValue(true) };
  const respuestaLoginOk = {
    accessToken: null,
    refreshToken: null,
    rol: 'CLIENTE',
    tipoCliente: null,
    mfaStatus: 'SUCCESS',
    email: 'ana@example.es',
    mfaSetupToken: null,
  };

  async function configurar(tipoRegistro: 'cliente' | 'vendedor'): Promise<void> {
    servicioFalso.registrarCliente.mockReset();
    servicioFalso.registrarVendedor.mockReset();
    autenticacionFalsa.iniciarSesion.mockReset();
    autenticacionFalsa.iniciarSesion.mockReturnValue(of(respuestaLoginOk));
    routerFalso.navigateByUrl.mockClear();
    await TestBed.configureTestingModule({
      imports: [RegistroComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { data: { tipoRegistro } } } },
        { provide: RegistroService, useValue: servicioFalso },
        { provide: Autenticacion, useValue: autenticacionFalsa },
        { provide: Router, useValue: routerFalso },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegistroComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  }

  it('valida los campos obligatorios de cliente y rechaza fechas futuras', async () => {
    await configurar('cliente');
    expect(component.form.valid).toBe(false);

    component.form.patchValue({
      nombre: 'Ana',
      apellidos: 'López',
      email: 'ana@example.es',
      contrasena: 'Clave123!',
    });
    expect(component.form.valid).toBe(false);

    component.form.patchValue({
      dni: '12345678A',
      fechaNacimiento: '2099-01-01',
    });

    expect(component.form.valid).toBe(false);
    expect(component.form.controls.fechaNacimiento.errors).toHaveProperty('past');
  });

  it('envía los campos de cliente esperados y muestra el éxito', async () => {
    await configurar('cliente');
    servicioFalso.registrarCliente.mockReturnValue(of(undefined));
    component.form.patchValue({
      nombre: 'Ana',
      apellidos: 'López',
      email: 'ana@example.es',
      contrasena: 'Clave123!',
      telefono: '600123456',
      imagenPerfil: 'https://example.es/ana.png',
      dni: '12345678A',
      fechaNacimiento: '2000-01-01',
    });

    component.enviar();
    await fixture.whenStable();

    expect(servicioFalso.registrarCliente).toHaveBeenCalledWith({
      nombre: 'Ana',
      apellidos: 'López',
      email: 'ana@example.es',
      contrasena: 'Clave123!',
      telefono: '600123456',
      imagenPerfil: 'https://example.es/ana.png',
      dni: '12345678A',
      fechaNacimiento: '2000-01-01',
    });
    expect(autenticacionFalsa.iniciarSesion).toHaveBeenCalledWith(
      'ana@example.es',
      'Clave123!',
    );
    expect(component.mensajeExito()).toContain('inicio de sesión completado');
    expect(routerFalso.navigateByUrl).toHaveBeenCalledWith('/products');
  });

  it('valida y envía los campos obligatorios de vendedor', async () => {
    await configurar('vendedor');
    expect(component.form.valid).toBe(false);
    component.form.patchValue({
      nombre: 'Ana',
      apellidos: 'López',
      email: 'ana@example.es',
      contrasena: 'Clave123!',
    });
    expect(component.form.valid).toBe(false);

    servicioFalso.registrarVendedor.mockReturnValue(of(undefined));
    component.form.patchValue({
      nombreComercial: 'Tienda Ana',
      cifNif: 'B12345678',
      categoriaPrincipalId: 'categoria-1',
    });

    component.enviar();
    await fixture.whenStable();

    expect(servicioFalso.registrarVendedor).toHaveBeenCalledWith({
      nombre: 'Ana',
      apellidos: 'López',
      email: 'ana@example.es',
      contrasena: 'Clave123!',
      telefono: '',
      imagenPerfil: '',
      nombreComercial: 'Tienda Ana',
      cifNif: 'B12345678',
      categoriaPrincipalId: 'categoria-1',
    });
    expect(autenticacionFalsa.iniciarSesion).toHaveBeenCalledWith(
      'ana@example.es',
      'Clave123!',
    );
    expect(routerFalso.navigateByUrl).toHaveBeenCalledWith('/products');
  });

  it('no intenta registrar otra vez si falla el inicio de sesión tras crear la cuenta', async () => {
    await configurar('cliente');
    servicioFalso.registrarCliente.mockReturnValue(of(undefined));
    autenticacionFalsa.iniciarSesion.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 0 })),
    );
    component.form.patchValue({
      nombre: 'Ana',
      apellidos: 'López',
      email: 'ana@example.es',
      contrasena: 'Clave123!',
      dni: '12345678A',
      fechaNacimiento: '2000-01-01',
    });

    component.enviar();
    await fixture.whenStable();

    expect(component.cuentaCreada()).toBe(true);
    expect(component.puedeReintentarLogin()).toBe(true);
    expect(component.mensajeError()).toContain('La cuenta se creó');
    expect(servicioFalso.registrarCliente).toHaveBeenCalledTimes(1);

    component.reintentarInicioSesion();

    expect(servicioFalso.registrarCliente).toHaveBeenCalledTimes(1);
    expect(autenticacionFalsa.iniciarSesion).toHaveBeenCalledTimes(2);
  });

  it('informa si el login del vendedor requiere MFA, sin considerarlo sesión iniciada', async () => {
    await configurar('vendedor');
    servicioFalso.registrarVendedor.mockReturnValue(of(undefined));
    autenticacionFalsa.iniciarSesion.mockReturnValue(
      of({
        ...respuestaLoginOk,
        rol: 'VENDEDOR',
        mfaStatus: 'REQUIRES_MFA_SETUP',
        mfaSetupToken: 'setup-token',
      }),
    );
    component.form.patchValue({
      nombre: 'Ana',
      apellidos: 'López',
      email: 'ana@example.es',
      contrasena: 'Clave123!',
      nombreComercial: 'Tienda Ana',
      cifNif: 'B12345678',
      categoriaPrincipalId: 'categoria-1',
    });

    component.enviar();
    await fixture.whenStable();

    expect(component.mensajeError()).toContain('requiere completar la verificación MFA');
    expect(routerFalso.navigateByUrl).not.toHaveBeenCalled();
    expect(component.puedeReintentarLogin()).toBe(false);
  });

  it('muestra un mensaje específico si el correo ya está registrado', async () => {
    await configurar('cliente');
    servicioFalso.registrarCliente.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 409 })),
    );
    component.form.patchValue({
      nombre: 'Ana',
      apellidos: 'López',
      email: 'ana@example.es',
      contrasena: 'Clave123!',
      dni: '12345678A',
      fechaNacimiento: '2000-01-01',
    });

    component.enviar();

    expect(component.mensajeError()).toContain('Ya existe una cuenta');
  });
});
