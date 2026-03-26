import { Routes } from '@angular/router';

export const LOANS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/loans-page.component').then(m => m.LoansPageComponent)
  }
];
