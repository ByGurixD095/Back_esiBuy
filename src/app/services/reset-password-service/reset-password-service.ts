import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PasswordResetRequest {
  token: string;
  pwd1: string;
  pwd2: string;
}

@Injectable({
  providedIn: 'root',
})
export class ResetPasswordService {
  private readonly http = inject(HttpClient);

  restablecer(datos: PasswordResetRequest): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/users/reset-password`, datos);
  }
}
