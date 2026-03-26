import { Routes } from '@angular/router';
import { authGuard, authMatchGuard } from './auth/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    canMatch: [authMatchGuard],
    loadComponent: () =>
      import('./dashboard/layout/dashboard-layout.component').then(m => m.DashboardLayoutComponent),
    children: [
      {
        path: 'customers',
        loadChildren: () => import('./customers/customers.routes').then(m => m.CUSTOMERS_ROUTES)
      },
      {
        path: 'employees',
        loadChildren: () => import('./employees/employees.routes').then(m => m.EMPLOYEES_ROUTES)
      },
      {
        path: 'users',
        loadChildren: () => import('./users/users.routes').then(m => m.USERS_ROUTES)
      },
      {
        path: 'loan-applications',
        loadChildren: () => import('./loan-applications/loan-applications.routes').then(m => m.LOAN_APPLICATIONS_ROUTES)
      },
      {
        path: 'loans',
        loadChildren: () => import('./loans/loans.routes').then(m => m.LOANS_ROUTES)
      },
      {
        path: 'payments',
        loadChildren: () => import('./payments/payments.routes').then(m => m.PAYMENTS_ROUTES)
      },
      {
        path: '',
        redirectTo: 'customers',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '',
    redirectTo: 'auth/login',
    pathMatch: 'full'
  }
];
