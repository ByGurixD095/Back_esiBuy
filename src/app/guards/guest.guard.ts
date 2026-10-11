import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.sessionChecked()) {
    return auth.isAuthenticated() ? router.createUrlTree(['/']) : true;
  }

  return auth.checkSessionOnLoad().pipe(
    map(() => (auth.isAuthenticated() ? router.createUrlTree(['/']) : true)),
  );
};
