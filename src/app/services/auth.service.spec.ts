import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    sessionStorage.removeItem('esiBuy.rol');
    sessionStorage.removeItem('esiBuy.email');
    sessionStorage.removeItem('esiBuy.expiresAt');
  });

  it('no fuerza una renovación si la sesión no almacena un vencimiento de token', () => {
    sessionStorage.removeItem('esiBuy.expiresAt');

    expect(service.isAccessTokenExpired()).toBe(false);
  });

  it('considera vencido un token cuya fecha de expiración ya pasó', () => {
    sessionStorage.setItem('esiBuy.expiresAt', (Date.now() - 10_000).toString());

    expect(service.isAccessTokenExpired()).toBe(true);
  });

  it('restaura la sesión desde el perfil usando el token de acceso actual', () => {
    let resultado: unknown;
    service.checkSessionOnLoad().subscribe((perfil) => (resultado = perfil));

    const request = http.expectOne('http://localhost:8080/users/me');
    expect(request.request.method).toBe('GET');
    request.flush({ rol: 'ADMINISTRADOR', email: 'admin@test.com' });

    expect(resultado).toEqual({ rol: 'ADMINISTRADOR', email: 'admin@test.com' });
    expect(service.hasRole(['ADMINISTRADOR'])).toBe(true);
    expect(service.email()).toBe('admin@test.com');
    expect(service.sessionChecked()).toBe(true);
  });

  it('limpia la sesión si no se puede validar el usuario actual', () => {
    let resultado: unknown;
    service.checkSessionOnLoad().subscribe((perfil) => (resultado = perfil));

    http
      .expectOne('http://localhost:8080/users/me')
      .flush({ message: 'Token expirado' }, { status: 401, statusText: 'Unauthorized' });

    expect(resultado).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
    expect(service.sessionChecked()).toBe(true);
  });
});
