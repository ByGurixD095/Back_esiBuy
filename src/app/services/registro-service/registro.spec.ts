import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Registro } from './registro';

describe('Registro', () => {
  let servicio: Registro;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(Registro);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('registra un cliente en el endpoint correcto enviando cookies', () => {
    const datos = {
      nombre: 'Ana',
      apellidos: 'López García',
      email: 'ana@example.es',
      contrasena: 'Clave123!',
      confirmarContrasena: 'Clave123!',
      telefono: '600123456',
      imagenPerfil: '',
      dni: '12345678A',
      fechaNacimiento: '2000-01-01',
    };

    servicio.registrarCliente(datos).subscribe();

    const peticion = http.expectOne('http://localhost:8080/users/register/clientes');
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual(datos);
    expect(peticion.request.withCredentials).toBe(true);
    peticion.flush(null);
  });

  it('registra un vendedor en el endpoint correcto enviando cookies', () => {
    const datos = {
      nombre: 'Ana',
      apellidos: 'López García',
      email: 'ana@example.es',
      contrasena: 'Clave123!',
      confirmarContrasena: 'Clave123!',
      telefono: '',
      imagenPerfil: '',
      nombreComercial: 'Tienda Ana',
      cifNif: 'B12345678',
      categoriaPrincipalId: 'categoria-1',
    };

    servicio.registrarVendedor(datos).subscribe();

    const peticion = http.expectOne('http://localhost:8080/users/register/vendedor');
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual(datos);
    expect(peticion.request.withCredentials).toBe(true);
    peticion.flush(null);
  });
});
