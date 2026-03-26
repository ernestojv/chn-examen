import { Routes } from '@angular/router';

export const LOAN_APPLICATIONS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/loan-applications-page.component').then(m => m.LoanApplicationsPageComponent)
  }
];
