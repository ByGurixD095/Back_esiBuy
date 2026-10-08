import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, catchError, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export type Rol = 'ADMINISTRADOR' | 'VENDEDOR' | 'CLIENTE';
export type MfaStatus = 'SUCCESS' | 'REQUIRES_MFA_SETUP' | 'REQUIRES_MFA_VERIFICATION';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string | null;
  refreshToken: string | null;
  rol: Rol;
  tipoCliente: string | null;
  mfaStatus: MfaStatus;
  email: string | null;
  mfaSetupToken: string | null;
}

export interface MfaSetupResponse {
  qrCodeUri: string;
  manualSecretKey: string;
}

export interface MfaBackupCodesResponse {
  backupCodes: string[];
  mensaje: string;
}

export interface MfaVerifyRequest {
  email: string;
  totpCode: string;
  emailOtpCode: string;
}

export interface MfaSetupConfirmRequest {
  email: string;
  code: string;
  setupToken: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly rolSignal = signal<Rol | null>(null);
  private readonly emailSignal = signal<string | null>(null);
  private readonly sessionCheckedSignal = signal(false);

  readonly rol = this.rolSignal.asReadonly();
  readonly email = this.emailSignal.asReadonly();
  readonly sessionChecked = this.sessionCheckedSignal.asReadonly();

  login(datos: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/users/login`, datos).pipe(
      tap((respuesta) => this.storeSessionIfComplete(respuesta)),
    );
  }

  initializeMfaSetup(email: string, setupToken: string): Observable<MfaSetupResponse> {
    return this.http.post<MfaSetupResponse>(`${environment.apiUrl}/mfa/setup-init`, {
      email,
      setupToken,
    });
  }

  confirmMfaSetup(datos: MfaSetupConfirmRequest): Observable<MfaBackupCodesResponse> {
    return this.http.post<MfaBackupCodesResponse>(
      `${environment.apiUrl}/mfa/setup-confirm`,
      datos,
    );
  }

  verifyMfa(datos: MfaVerifyRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/mfa/verify-mfa`, datos).pipe(
      tap((respuesta) => this.storeSessionIfComplete(respuesta)),
    );
  }

  refresh(): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/users/refresh`, {}).pipe(
      tap((respuesta) => this.storeSessionIfComplete(respuesta)),
    );
  }

  checkSessionOnLoad(): Observable<LoginResponse | null> {
    return this.refresh().pipe(
      tap(() => this.sessionCheckedSignal.set(true)),
      catchError(() => {
        this.clearSession();
        this.sessionCheckedSignal.set(true);
        return of(null);
      }),
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/users/logout`, {}).pipe(
      tap(() => this.clearSession()),
    );
  }

  isAuthenticated(): boolean {
    return this.rolSignal() !== null;
  }

  hasRole(roles: readonly Rol[]): boolean {
    const rol = this.rolSignal();
    return rol !== null && roles.includes(rol);
  }

  clearSession(): void {
    this.rolSignal.set(null);
    this.emailSignal.set(null);
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.removeItem('esiBuy.rol');
      sessionStorage.removeItem('esiBuy.email');
    }
    this.sessionCheckedSignal.set(true);
  }

  errorMessage(error: HttpErrorResponse, fallback: string): string {
    if (error.status === 0) {
      return 'No se ha podido conectar con el servidor.';
    }
    const message = error.error?.message ?? error.error?.mensaje;
    return typeof message === 'string' && message.length > 0 ? message : fallback;
  }

  private storeSessionIfComplete(respuesta: LoginResponse): void {
    if (respuesta.mfaStatus !== 'SUCCESS') {
      return;
    }
    this.rolSignal.set(respuesta.rol);
    this.emailSignal.set(respuesta.email);
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.setItem('esiBuy.rol', respuesta.rol);
      if (respuesta.email) {
        sessionStorage.setItem('esiBuy.email', respuesta.email);
      }
    }
  }

}
