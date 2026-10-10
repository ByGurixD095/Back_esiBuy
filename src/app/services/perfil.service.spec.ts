import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PerfilService, PerfilUsuario } from './perfil.service';

describe('PerfilService', () => {
  let service: PerfilService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PerfilService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(PerfilService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('obtiene el perfil autenticado desde GET /users/me', () => {
    const respuesta: PerfilUsuario = {
      id: 'cliente-1',
      name: 'Ana',
      apellidos: 'García',
      email: 'ana@example.com',
      telefono: '600123456',
      imagenPerfil: null,
      rol: 'CLIENTE',
      tipoCliente: 'NORMAL',
      dni: '12345678A',
      fechaNacimiento: '1995-04-12',
    };
    let perfil: PerfilUsuario | undefined;

    service.obtenerMiPerfil().subscribe((resultado) => (perfil = resultado));

    const request = http.expectOne('http://localhost:8080/users/me');
    expect(request.request.method).toBe('GET');
    expect(request.request.withCredentials).toBe(true);
    request.flush(respuesta);

    expect(perfil).toEqual(respuesta);
  });

  it('envía cambios con PATCH /users/me/update y no espera un cuerpo de respuesta', () => {
    const datos = {
      nombre: 'Ana María',
      apellidos: 'García',
      telefono: '600123456',
      imagenPerfil: '',
      tipoCliente: 'PREMIUM' as const,
    };
    let completado = false;

    service.actualizarMiPerfil(datos).subscribe(() => (completado = true));

    const request = http.expectOne('http://localhost:8080/users/me/update');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual(datos);
    expect(request.request.withCredentials).toBe(true);
    request.flush(null, { status: 204, statusText: 'No Content' });

    expect(completado).toBe(true);
  });
});
