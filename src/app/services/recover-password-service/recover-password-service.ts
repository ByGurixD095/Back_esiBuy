import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PasswordRecoveryRequest {
  email: string;
}

@Injectable({
  providedIn: 'root',
})
export class RecoverPasswordService {
  private readonly http = inject(HttpClient);

  solicitar(datos: PasswordRecoveryRequest): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/users/recover-password`, datos);
  }
}
