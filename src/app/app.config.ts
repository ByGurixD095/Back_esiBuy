import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { authSessionInterceptor } from './interceptors/auth-session.interceptor';
import { csrfInterceptor } from './interceptors/csrf.interceptor';
import { AuthService } from './services/auth.service';
import { provideAppInitializer, inject } from '@angular/core';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withFetch(), withInterceptors([csrfInterceptor, authSessionInterceptor])),
    provideAppInitializer(() => {
      inject(AuthService).checkSessionOnLoad().subscribe();
    }),
  ],
};