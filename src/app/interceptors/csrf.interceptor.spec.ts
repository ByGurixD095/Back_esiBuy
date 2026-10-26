import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { csrfInterceptor, resetCsrfTokenCacheForTesting } from './csrf.interceptor';

vi.mock('../../environments/environment', () => ({
  environment: { apiUrl: 'https://back-esibuy.onrender.com', csrfEnabled: true },
}));

describe('csrfInterceptor', () => {
  let http: HttpTestingController;
  const apiUrl = 'https://back-esibuy.onrender.com';

  beforeEach(() => {
    resetCsrfTokenCacheForTesting();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([csrfInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    resetCsrfTokenCacheForTesting();
  });

  it('obtiene CSRF y añade el token a las peticiones de escritura al backend', () => {
    let status = 0;
    TestBed.inject(HttpClient).post(`${apiUrl}/users/login`, {}).subscribe({
      next: (response) => (status = response ? 200 : 204),
    });

    const csrfRequest = http.expectOne(`${apiUrl}/csrf`);
    expect(csrfRequest.request.method).toBe('GET');
    expect(csrfRequest.request.withCredentials).toBe(true);
    csrfRequest.flush({ headerName: 'X-XSRF-TOKEN', token: 'csrf-token' });

    const writeRequest = http.expectOne(`${apiUrl}/users/login`);
    expect(writeRequest.request.headers.get('X-XSRF-TOKEN')).toBe('csrf-token');
    expect(writeRequest.request.withCredentials).toBe(true);
    writeRequest.flush({ ok: true });

    expect(status).toBe(200);
  });

  it('propaga el error CSRF sin enviar la petición de escritura', () => {
    let status = 0;
    TestBed.inject(HttpClient).post(`${apiUrl}/users/login`, {}).subscribe({
      error: (error: { status: number }) => (status = error.status),
    });

    http.expectOne(`${apiUrl}/csrf`).flush(
      { message: 'CSRF unavailable' },
      { status: 500, statusText: 'Internal Server Error' },
    );

    expect(status).toBe(500);
    http.expectNone(`${apiUrl}/users/login`);
  });
});
