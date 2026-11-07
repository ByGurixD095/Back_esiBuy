import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { throwError } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthService } from '../services/auth.service';
import { authSessionInterceptor } from './auth-session.interceptor';

describe('authSessionInterceptor', () => {
  let http: HttpTestingController;
  const authServiceFalso = {
    refresh: vi.fn(),
    clearSession: vi.fn(),
    sessionChecked: vi.fn(),
  };
  const routerFalso = { navigate: vi.fn() };

  beforeEach(() => {
    authServiceFalso.refresh.mockReset();
    authServiceFalso.refresh.mockReturnValue(
      throwError(() => new Error('Refresh should not run for forbidden responses')),
    );
    authServiceFalso.clearSession.mockReset();
    authServiceFalso.sessionChecked.mockReset();
    authServiceFalso.sessionChecked.mockReturnValue(true);
    routerFalso.navigate.mockReset();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authSessionInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceFalso },
        { provide: Router, useValue: routerFalso },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('no renueva ni borra la sesión cuando el servidor devuelve 403', () => {
    let statusError = 0;
    TestBed.inject(HttpClient).delete('/admin/users/admin-1').subscribe({
      error: (error: { status: number }) => (statusError = error.status),
    });

    http.expectOne('/admin/users/admin-1').flush(null, {
      status: 403,
      statusText: 'Forbidden',
    });

    expect(statusError).toBe(403);
    expect(authServiceFalso.refresh).not.toHaveBeenCalled();
    expect(authServiceFalso.clearSession).not.toHaveBeenCalled();
    expect(routerFalso.navigate).not.toHaveBeenCalled();
  });

  it('no redirige a login mientras la sesión se valida durante la carga inicial', () => {
    authServiceFalso.sessionChecked.mockReturnValue(false);
    let requestFailed = false;
    TestBed.inject(HttpClient).get('/admin/users').subscribe({
      error: () => (requestFailed = true),
    });

    http.expectOne('/admin/users').flush(null, {
      status: 401,
      statusText: 'Unauthorized',
    });

    expect(requestFailed).toBe(true);
    expect(authServiceFalso.refresh).toHaveBeenCalledOnce();
    expect(authServiceFalso.clearSession).toHaveBeenCalledOnce();
    expect(routerFalso.navigate).not.toHaveBeenCalled();
  });
});
