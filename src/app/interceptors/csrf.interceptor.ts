import {
  HttpBackend,
  HttpClient,
  HttpInterceptorFn,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, catchError, map, shareReplay, switchMap, throwError } from 'rxjs';
import { environment } from '../../environments/environment';

interface CsrfTokenResponse {
  headerName: string;
  token: string;
}

let csrfToken$: Observable<CsrfTokenResponse> | null = null;

const UNSAFE_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE']);

export const csrfInterceptor: HttpInterceptorFn = (request, next) => {
  const apiUrl = environment.apiUrl.replace(/\/+$/, '');
  const isApiRequest = request.url === apiUrl || request.url.startsWith(`${apiUrl}/`);

  if (!environment.csrfEnabled || !apiUrl || !isApiRequest || !UNSAFE_METHODS.has(request.method)) {
    return next(request);
  }

  if (!csrfToken$) {
    const http = new HttpClient(inject(HttpBackend));
    csrfToken$ = http.get<CsrfTokenResponse>(`${apiUrl}/csrf`, { withCredentials: true }).pipe(
      map((response) => {
        if (!response.headerName || !response.token) {
          throw new Error('El backend devolvió una respuesta CSRF incompleta.');
        }
        return response;
      }),
      catchError((error: unknown) => {
        csrfToken$ = null;
        return throwError(() => error);
      }),
      shareReplay({ bufferSize: 1, refCount: false }),
    );
  }

  return csrfToken$.pipe(
    switchMap(({ headerName, token }) =>
      next(
        request.clone({
          withCredentials: true,
          setHeaders: { [headerName]: token },
        }),
      ),
    ),
  );
};

export function resetCsrfTokenCacheForTesting(): void {
  csrfToken$ = null;
}
