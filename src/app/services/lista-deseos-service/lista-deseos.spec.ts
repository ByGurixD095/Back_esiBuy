import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ListaDeseosApiError, ListaDeseosService } from './lista-deseos';

describe('ListaDeseosService', () => {
  let servicio: ListaDeseosService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(ListaDeseosService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('crea una lista para el cliente con nombre y credenciales', () => {
    const lista = {
      id: 'lista-1',
      idCliente: 'cliente-1',
      nombre: 'Para comprar',
      productosIds: [],
    };
    let resultado: unknown;

    servicio.crear('Para comprar').subscribe((creada) => (resultado = creada));

    const peticion = http.expectOne('http://localhost:8080/listas-deseos');
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual({ nombre: 'Para comprar' });
    expect(peticion.request.withCredentials).toBe(true);
    peticion.flush(lista, { status: 201, statusText: 'Created' });

    expect(resultado).toEqual(lista);
  });

  it('traduce un 403 a un mensaje de rol no permitido', () => {
    let error: ListaDeseosApiError | undefined;

    servicio.crear('Para comprar').subscribe({ error: (e) => (error = e) });
    http
      .expectOne('http://localhost:8080/listas-deseos')
      .flush(null, { status: 403, statusText: 'Forbidden' });

    expect(error).toBeInstanceOf(ListaDeseosApiError);
    expect(error?.message).toBe('Solo los clientes pueden crear listas de deseos.');
  });
});
