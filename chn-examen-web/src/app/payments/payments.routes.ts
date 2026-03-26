import { Routes } from '@angular/router';

export const PAYMENTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/payments-page.component').then(m => m.PaymentsPageComponent)
  }
];
