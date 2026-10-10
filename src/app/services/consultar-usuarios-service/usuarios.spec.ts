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

  it('consulta los datos detallados de un usuario con credenciales', () => {
    servicio.consultarPorId('user-db-1').subscribe();

    const peticion = http.expectOne('http://localhost:8080/admin/user-db-1');
    expect(peticion.request.method).toBe('GET');
    expect(peticion.request.withCredentials).toBe(true);
    peticion.flush({
      id: 'user-db-1',
      name: 'Ana',
      apellidos: 'López',
      email: 'ana@test.com',
      rol: 'CLIENTE',
      activo: true,
      bloqueado: false,
      eliminado: false,
      dni: '12345678A',
      fechaNacimiento: '1990-01-01',
      tipoCliente: 'NORMAL',
    });
  });

  it('actualiza los datos del usuario con PATCH y credenciales', () => {
    servicio
      .modificar('user-db-1', {
        nombre: 'Ana María',
        apellidos: 'López',
        imagenPerfil: '',
        dni: '12345678A',
        fechaNacimiento: '1990-01-01',
        tipoCliente: 'PREMIUM',
      })
      .subscribe();

    const peticion = http.expectOne('http://localhost:8080/admin/user-db-1');
    expect(peticion.request.method).toBe('PATCH');
    expect(peticion.request.withCredentials).toBe(true);
    expect(peticion.request.body).toEqual({
      nombre: 'Ana María',
      apellidos: 'López',
      imagenPerfil: '',
      dni: '12345678A',
      fechaNacimiento: '1990-01-01',
      tipoCliente: 'PREMIUM',
    });
    peticion.flush(null);
  });

  it('solicita la eliminación lógica del usuario con credenciales', () => {
    servicio.eliminar('user-db-1').subscribe();

    const peticion = http.expectOne('http://localhost:8080/admin/user-db-1');
    expect(peticion.request.method).toBe('DELETE');
    expect(peticion.request.withCredentials).toBe(true);
    peticion.flush(null);
  });
});
