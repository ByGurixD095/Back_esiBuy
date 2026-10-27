import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
import { Categorias } from './categorias.service';

describe('Categorias.eliminar (HU 30)', () => {
  let servicio: Categorias;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(Categorias);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envía DELETE a /admin/categorias/{id} con credenciales', () => {
    servicio.eliminar('c1').subscribe();

    const peticion = http.expectOne(`${environment.apiUrl}/admin/categorias/c1`);
    expect(peticion.request.method).toBe('DELETE');
    expect(peticion.request.withCredentials).toBe(true);
    peticion.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('codifica el id en la URL', () => {
    servicio.eliminar('a/b').subscribe();

    const peticion = http.expectOne(`${environment.apiUrl}/admin/categorias/a%2Fb`);
    peticion.flush(null, { status: 204, statusText: 'No Content' });
  });
});
