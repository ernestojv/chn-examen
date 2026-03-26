import { inject } from '@angular/core';
import { CanActivateFn, CanMatchFn, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

function toLoginUrlTree(returnUrl?: string): UrlTree {
  const router = inject(Router);

  return router.createUrlTree(['/auth/login'], {
    queryParams: returnUrl ? { returnUrl } : undefined
  });
}

export const authGuard: CanActivateFn = (_route, state) => {
  const authService = inject(AuthService);

  if (authService.isAuthenticated()) {
    return true;
  }

  return toLoginUrlTree(state.url);
};

export const authMatchGuard: CanMatchFn = (_route, segments) => {
  const authService = inject(AuthService);

  if (authService.isAuthenticated()) {
    return true;
  }

  const attemptedPath = `/${segments.map(segment => segment.path).join('/')}`;
  return toLoginUrlTree(attemptedPath);
};
