import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Favoritos, FavoritosApiError } from './favoritos';

describe('Favoritos (servicio)', () => {
  let servicio: Favoritos;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(Favoritos);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista los favoritos con credenciales', () => {
    let resultado: unknown;
    servicio.listar().subscribe((productos) => (resultado = productos));

    const peticion = http.expectOne('http://localhost:8080/favoritos');
    expect(peticion.request.method).toBe('GET');
    expect(peticion.request.withCredentials).toBe(true);
    peticion.flush([{ id: 'p1', nombre: 'Lámpara' }]);

    expect(resultado).toEqual([{ id: 'p1', nombre: 'Lámpara' }]);
  });

  it('marca un producto como favorito con POST y credenciales', () => {
    servicio.marcar('p1').subscribe();

    const peticion = http.expectOne('http://localhost:8080/favoritos/p1');
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.withCredentials).toBe(true);
    peticion.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('quita un producto de favoritos con DELETE y credenciales', () => {
    servicio.quitar('p1').subscribe();

    const peticion = http.expectOne('http://localhost:8080/favoritos/p1');
    expect(peticion.request.method).toBe('DELETE');
    expect(peticion.request.withCredentials).toBe(true);
    peticion.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('traduce un 404 al marcar en un mensaje amable', () => {
    let error: FavoritosApiError | undefined;
    servicio.marcar('p9').subscribe({ error: (e) => (error = e) });

    http.expectOne('http://localhost:8080/favoritos/p9').flush({ error: 'NOT_FOUND' }, { status: 404, statusText: 'Not Found' });

    expect(error).toBeInstanceOf(FavoritosApiError);
    expect(error?.status).toBe(404);
    expect(error?.message).toBe('Este producto ya no está disponible.');
  });

  it('traduce un 403 en el mensaje de rol no permitido', () => {
    let error: FavoritosApiError | undefined;
    servicio.listar().subscribe({ error: (e) => (error = e) });

    http.expectOne('http://localhost:8080/favoritos').flush(null, { status: 403, statusText: 'Forbidden' });

    expect(error?.message).toBe('Solo los clientes pueden gestionar favoritos.');
  });

  it('traduce un fallo de red (status 0) indicando la acción', () => {
    let error: FavoritosApiError | undefined;
    servicio.quitar('p1').subscribe({ error: (e) => (error = e) });

    http.expectOne('http://localhost:8080/favoritos/p1').error(new ProgressEvent('error'));

    expect(error?.status).toBe(0);
    expect(error?.message).toContain('quitar el producto de favoritos');
  });
});
