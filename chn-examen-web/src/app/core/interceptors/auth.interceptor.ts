import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../../auth/services/auth.service';

/**
 * Attaches the stored JWT token as a Bearer Authorization header
 * to every outgoing HTTP request when the user is authenticated.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();
  const isAuthRequest = req.url.includes('/auth/login');

  if (!token) {
    return next(req).pipe(
      catchError(error => {
        if (error instanceof HttpErrorResponse && error.status === 401 && !isAuthRequest) {
          authService.logout();
          void router.navigate(['/auth/login']);
        }

        return throwError(() => error);
      })
    );
  }

  const cloned = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(cloned).pipe(
    catchError(error => {
      if (error instanceof HttpErrorResponse && error.status === 401 && !isAuthRequest) {
        authService.logout();
        void router.navigate(['/auth/login']);
      }

      return throwError(() => error);
    })
  );
};
