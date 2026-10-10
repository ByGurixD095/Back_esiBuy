import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Usuarios } from './usuarios';

describe('Usuarios', () => {
  let servicio: Usuarios;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(Usuarios);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('consulta la lista administrativa incluyendo los estados enviados por el back', () => {
    servicio.consultar().subscribe();

    const peticion = http.expectOne('http://localhost:8080/admin/users');
    expect(peticion.request.method).toBe('GET');
    expect(peticion.request.withCredentials).toBe(true);
    peticion.flush([
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
    ]);
  });
});
