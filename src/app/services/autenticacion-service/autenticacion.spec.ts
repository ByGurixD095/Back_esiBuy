import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Autenticacion } from './autenticacion';

describe('Autenticacion', () => {
  let servicio: Autenticacion;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(Autenticacion);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envía las credenciales a /users/login con cookies', () => {
    servicio.iniciarSesion('ana@example.es', 'Clave123!').subscribe();

    const peticion = http.expectOne('http://localhost:8080/users/login');
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual({
      username: 'ana@example.es',
      password: 'Clave123!',
    });
    expect(peticion.request.withCredentials).toBe(true);
    peticion.flush({
      accessToken: null,
      refreshToken: null,
      rol: 'CLIENTE',
      tipoCliente: null,
      mfaStatus: 'SUCCESS',
      email: 'ana@example.es',
      mfaSetupToken: null,
    });
    expect(servicio.usuario()).toEqual({
      email: 'ana@example.es',
      rol: 'CLIENTE',
      tipoCliente: null,
    });
    expect(servicio.autenticado()).toBe(true);
  });

  it('no inicia una sesión hasta que el MFA se gestione en el siguiente paso', () => {
    servicio.iniciarSesion('vendedor@example.es', 'Clave123!').subscribe();

    http.expectOne('http://localhost:8080/users/login').flush({
      accessToken: null,
      refreshToken: null,
      rol: 'VENDEDOR',
      tipoCliente: null,
      mfaStatus: 'REQUIRES_MFA_SETUP',
      email: 'vendedor@example.es',
      mfaSetupToken: 'setup-token',
    });

    expect(servicio.usuario()).toBeNull();
    expect(servicio.autenticado()).toBe(false);
  });
});
