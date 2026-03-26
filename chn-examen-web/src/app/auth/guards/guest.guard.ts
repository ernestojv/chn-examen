import { inject } from '@angular/core';
import { CanActivateFn, CanMatchFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

function redirectIfAuthenticated(): true | ReturnType<Router['createUrlTree']> {
  const authService = inject(AuthService);

  if (!authService.isAuthenticated()) {
    return true;
  }

  return inject(Router).createUrlTree(['/dashboard']);
}

export const guestGuard: CanActivateFn = () => redirectIfAuthenticated();

export const guestMatchGuard: CanMatchFn = () => redirectIfAuthenticated();
