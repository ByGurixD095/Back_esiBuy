import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService, Rol } from '../services/auth.service';

export const roleGuard = (roles: readonly Rol[]): CanActivateFn => (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // Función auxiliar para verificar estrictamente el rol
  const checkRoles = () =>
    auth.hasRole(roles)
      ? true
      : router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });

  if (auth.sessionChecked()) {
    // Renovar solo cuando hay una expiración conocida; las sesiones por cookie no guardan este dato.
    if (auth.isAccessTokenExpired()) {
      return auth.refresh().pipe(
        map(() => checkRoles()),
        catchError(() => of(router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } })))
      );
    }
    return checkRoles();
  }

  // Si se recarga la página por primera vez, validamos contra el back directamente
  return auth.checkSessionOnLoad().pipe(
    map(() => checkRoles())
  );
};
