import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { map } from 'rxjs';
import { AuthService, Rol } from '../services/auth.service';

export const roleGuard = (roles: readonly Rol[]): CanActivateFn => (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.sessionChecked()) {
    return auth.hasRole(roles)
      ? true
      : router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  }

  return auth.checkSessionOnLoad().pipe(
    map(() =>
      auth.hasRole(roles)
        ? true
        : router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } }),
    ),
  );
};
